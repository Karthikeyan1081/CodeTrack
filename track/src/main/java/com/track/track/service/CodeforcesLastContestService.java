package com.track.track.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.track.dto.LastContestDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Codeforces is the most reliable of the three platforms for this feature:
 * the public API gives us the finished-contest list, whether the handle
 * participated (user.rating), and — for participants — every accepted
 * submission including the exact problem name (contest.status).
 *
 * No auth, no scraping. Codeforces does rate-limit unauthenticated calls
 * (~1 req/sec is safe); if you're calling this for many students in a batch
 * job, throttle it or cache per contest.
 */
@Service
public class CodeforcesLastContestService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Kolkata"));

    public LastContestDto getLastContestStatus(String username) {
        if (username == null || username.isBlank()) {
            return empty();
        }
        try {
            // 1. Most recent FINISHED contest (contest.list is sorted newest-first)
            JsonNode contests = fetch("https://codeforces.com/api/contest.list?gym=false").get("result");
            JsonNode latestFinished = null;
            for (JsonNode c : contests) {
                if ("FINISHED".equals(c.get("phase").asText())) {
                    latestFinished = c;
                    break;
                }
            }
            if (latestFinished == null) return empty();

            long contestId = latestFinished.get("id").asLong();
            String contestName = latestFinished.get("name").asText();
            String contestDate = DATE_FMT.format(
                    Instant.ofEpochSecond(latestFinished.get("startTimeSeconds").asLong()));

            // 2. Did this handle take part in that contest?
            JsonNode ratingChanges = fetch(
                    "https://codeforces.com/api/user.rating?handle=" + enc(username)).get("result");
            boolean attended = false;
            for (JsonNode rc : ratingChanges) {
                if (rc.get("contestId").asLong() == contestId) {
                    attended = true;
                    break;
                }
            }

            if (!attended) {
                return new LastContestDto("codeforces", false, contestName, contestDate,
                        null, null, null);
            }

            // 3. Pull accepted submissions for this handle in that contest
            JsonNode submissions = fetch("https://codeforces.com/api/contest.status?contestId="
                    + contestId + "&handle=" + enc(username)).get("result");

            LinkedHashMap<String, String> solvedIndexToName = new LinkedHashMap<>();
            for (JsonNode sub : submissions) {
                if ("OK".equals(sub.path("verdict").asText())) {
                    JsonNode problem = sub.get("problem");
                    solvedIndexToName.put(problem.get("index").asText(), problem.get("name").asText());
                }
            }

            // 4. Total problem count in the contest, for the "3/6" display
            Integer totalProblems = null;
            try {
                JsonNode standings = fetch("https://codeforces.com/api/contest.standings?contestId="
                        + contestId + "&from=1&count=1");
                totalProblems = standings.get("result").get("problems").size();
            } catch (Exception ignored) {
                // non-fatal — UI just won't show the "/total" part
            }

            return new LastContestDto("codeforces", true, contestName, contestDate,
                    solvedIndexToName.size(), totalProblems, new ArrayList<>(solvedIndexToName.values()));

        } catch (Exception e) {
            return empty();
        }
    }

    private JsonNode fetch(String url) throws Exception {
        String body = restTemplate.getForObject(url, String.class);
        return mapper.readTree(body);
    }

    private String enc(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    private LastContestDto empty() {
        return new LastContestDto("codeforces", null, null, null, null, null, null);
    }
}
