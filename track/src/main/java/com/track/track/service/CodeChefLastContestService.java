package com.track.track.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.track.dto.LastContestDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CodeChef has no public API for per-contest problem data, so this service
 * can only answer "did they attend, and when was the contest" — it CANNOT
 * report solvedCount or solvedProblemNames reliably. Those fields are always
 * left null for this platform; the UI should treat that as "not available"
 * rather than "zero solved".
 *
 * Attendance is recovered by scraping the `all_rating` JSON blob embedded in
 * the user's profile HTML page, which lists every rated contest they've
 * taken part in. This is fragile — if CodeChef changes their page markup,
 * this will start returning null (fails safe) rather than wrong data.
 */
@Service
public class CodeChefLastContestService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Pattern RATING_BLOB = Pattern.compile(
            "var all_rating\\s*=\\s*(\\[.*?\\]);", Pattern.DOTALL);

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Kolkata"));

    public LastContestDto getLastContestStatus(String username) {
        if (username == null || username.isBlank()) {
            return empty();
        }
        try {
            // 1. Most recent PAST contest, from CodeChef's public contest list API
            JsonNode contestList = getJson("https://www.codechef.com/api/list/contests/all");
            JsonNode pastContests = contestList.path("future_contests"); // placeholder guard
            JsonNode present = contestList.path("present_contests");
            JsonNode past = contestList.path("past_contests");
            if (!past.isArray() || past.size() == 0) return empty();

            JsonNode latestPast = past.get(0); // API returns most recent first
            String contestName = latestPast.path("contest_name").asText();
            String contestCode = latestPast.path("contest_code").asText();
            String contestEndIso = latestPast.path("contest_end_date_iso").asText();
            String contestDate = contestEndIso.length() >= 10 ? contestEndIso.substring(0, 10) : contestEndIso;

            // 2. Scrape the user's rating history to see if contestCode appears
            String profileHtml = restTemplate.getForObject(
                    "https://www.codechef.com/users/" + enc(username), String.class);
            if (profileHtml == null) return empty();

            Matcher m = RATING_BLOB.matcher(profileHtml);
            if (!m.find()) {
                // Profile exists but we couldn't find the ratings blob — treat as unknown
                return empty();
            }
            JsonNode ratingHistory = mapper.readTree(m.group(1));

            boolean attended = false;
            for (JsonNode entry : ratingHistory) {
                if (contestCode.equalsIgnoreCase(entry.path("code").asText())) {
                    attended = true;
                    break;
                }
            }

            return new LastContestDto("codechef", attended, contestName, contestDate,
                    null, null, null); // solved data not available for CodeChef

        } catch (Exception e) {
            return empty();
        }
    }

    private JsonNode getJson(String url) throws Exception {
        String body = restTemplate.getForObject(url, String.class);
        return mapper.readTree(body);
    }

    private String enc(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }

    private LastContestDto empty() {
        return new LastContestDto("codechef", null, null, null, null, null, null);
    }
}
