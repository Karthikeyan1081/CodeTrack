package com.track.track.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.track.dto.LastContestDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * LeetCode's public GraphQL endpoint (userContestRankingHistory) reliably
 * gives us: which contests a user attended, and how many problems they solved
 * in each. That part is solid.
 *
 * Getting the *names* of the solved questions is best-effort: it requires an
 * extra, undocumented call to LeetCode's contest ranking API to find the
 * user's row, matched against the contest's question list. This can break if
 * LeetCode changes that endpoint — it's wrapped so a failure there simply
 * omits solvedProblemNames rather than failing the whole request.
 */
@Service
public class LeetCodeLastContestService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String GRAPHQL_URL = "https://leetcode.com/graphql";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Kolkata"));

    public LastContestDto getLastContestStatus(String username) {
        if (username == null || username.isBlank()) {
            return empty();
        }
        try {
            String query = "query userContestRankingHistory($username: String!) { "
                    + "userContestRankingHistory(username: $username) { "
                    + "attended trophyDegree problemsSolved totalProblems "
                    + "contest { title startTime titleSlug } } }";

            Map<String, Object> body = new HashMap<>();
            body.put("query", query);
            body.put("variables", Map.of("username", username));

            JsonNode resp = postGraphQl(body);
            JsonNode history = resp.path("data").path("userContestRankingHistory");
            if (!history.isArray() || history.size() == 0) return empty();

            // History is ordered oldest -> newest; the last ATTENDED entry
            // still tells us the most recent contest was *available* even if
            // this user skipped it, so instead find the single most recent
            // contest by startTime across the whole array.
            JsonNode mostRecent = null;
            for (JsonNode entry : history) {
                if (mostRecent == null
                        || entry.path("contest").path("startTime").asLong()
                        > mostRecent.path("contest").path("startTime").asLong()) {
                    mostRecent = entry;
                }
            }
            if (mostRecent == null) return empty();

            String contestName = mostRecent.path("contest").path("title").asText();
            String titleSlug = mostRecent.path("contest").path("titleSlug").asText();
            String contestDate = DATE_FMT.format(
                    Instant.ofEpochSecond(mostRecent.path("contest").path("startTime").asLong()));
            boolean attended = mostRecent.path("attended").asBoolean(false);

            if (!attended) {
                return new LastContestDto("leetcode", false, contestName, contestDate,
                        null, null, null);
            }

            int solvedCount = mostRecent.path("problemsSolved").asInt(0);
            int totalProblems = mostRecent.path("totalProblems").asInt(0);

            List<String> solvedNames = tryResolveSolvedQuestionNames(titleSlug, username);

            return new LastContestDto("leetcode", true, contestName, contestDate,
                    solvedCount, totalProblems > 0 ? totalProblems : null, solvedNames);

        } catch (Exception e) {
            return empty();
        }
    }

    /**
     * Best-effort: matches the user's per-question submission status (question
     * index only, from the contest ranking API) against the contest's question
     * list (index -> title) to recover actual problem names.
     * Returns null if anything about this lookup fails or the endpoint shape
     * changes — callers should treat null as "names unavailable".
     */
    private List<String> tryResolveSolvedQuestionNames(String titleSlug, String username) {
        try {
            // Question list for the contest: index -> title
            JsonNode info = getJson("https://leetcode.com/contest/api/info/" + titleSlug + "/");
            JsonNode questions = info.path("questions");
            List<String> indexToTitle = new ArrayList<>();
            for (JsonNode q : questions) {
                indexToTitle.add(q.path("title").asText());
            }
            if (indexToTitle.isEmpty()) return null;

            // Search the contest's global ranking pages for this username to
            // find their per-question submission map. Capped at a handful of
            // pages to stay cheap; if the user isn't found, names are omitted.
            for (int page = 1; page <= 40; page++) {
                JsonNode ranking = getJson("https://leetcode.com/contest/api/ranking/" + titleSlug
                        + "/?pagination=" + page + "&region=global");
                JsonNode users = ranking.path("total_rank");
                JsonNode submissions = ranking.path("submissions");
                if (!users.isArray() || users.size() == 0) break;

                for (int i = 0; i < users.size(); i++) {
                    JsonNode u = users.get(i);
                    if (username.equalsIgnoreCase(u.path("username").asText())) {
                        JsonNode userSubs = submissions.get(i);
                        List<String> names = new ArrayList<>();
                        Iterator<String> fieldNames = userSubs.fieldNames();
                        while (fieldNames.hasNext()) {
                            String qIndexStr = fieldNames.next();
                            int qIndex = Integer.parseInt(qIndexStr);
                            if (qIndex >= 0 && qIndex < indexToTitle.size()) {
                                names.add(indexToTitle.get(qIndex));
                            }
                        }
                        return names.isEmpty() ? null : names;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null; // silently degrade — solvedCount from GraphQL still works
        }
    }

    private JsonNode postGraphQl(Map<String, Object> body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = mapper.writeValueAsString(body);
        String respBody = restTemplate.postForObject(GRAPHQL_URL, new HttpEntity<>(json, headers), String.class);
        return mapper.readTree(respBody);
    }

    private JsonNode getJson(String url) throws Exception {
        String body = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class).getBody();
        return mapper.readTree(body);
    }

    private LastContestDto empty() {
        return new LastContestDto("leetcode", null, null, null, null, null, null);
    }
}
