package com.track.track.service;

import com.track.track.dto.LeetCodeStatsDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeetCodeService {

    private final RestTemplate restTemplate;

    public LeetCodeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LeetCodeStatsDto getLeetCodeStats(String username) {

        try {

            String url = "https://leetcode.com/graphql";

            String query = """
                    query getUserProfile($username: String!) {
                      matchedUser(username: $username) {
                        submitStatsGlobal {
                          acSubmissionNum {
                            difficulty
                            count
                          }
                        }

                        submissionCalendar
                      }

                      userContestRanking(username: $username) {
                        rating
                        globalRanking
                        attendedContestsCount
                      }

                      userContestRankingHistory(username: $username) {
                        rating
                      }
                    }
                    """;

            Map<String, Object> variables = new HashMap<>();
            variables.put("username", username);

            Map<String, Object> body = new HashMap<>();
            body.put("query", query);
            body.put("variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || !responseBody.containsKey("data")) {
                return emptyDto();
            }

            Map<String, Object> data =
                    (Map<String, Object>) responseBody.get("data");

            return extractStats(data);

        } catch (Exception e) {
            return emptyDto();
        }
    }

    private LeetCodeStatsDto extractStats(Map<String, Object> data) {

        Map<String, Object> matchedUser =
                (Map<String, Object>) data.get("matchedUser");

        Map<String, Object> contestRanking =
                (Map<String, Object>) data.get("userContestRanking");

        List<Map<String, Object>> contestHistory =
                (List<Map<String, Object>>) data.get("userContestRankingHistory");

        String[] solved      = getAllSolved(matchedUser);
        String easySolved    = solved[0];
        String mediumSolved  = solved[1];
        String hardSolved    = solved[2];
        String totalSolved   = solved[3];

        String currentRating    = getCurrentRating(contestRanking);
        String globalRank       = getGlobalRank(contestRanking);
        String contestsAttended = getContestCount(contestRanking);
        String highestRating    = getHighestRating(contestHistory);
        String lastActive       = getLastActive(matchedUser);

        LeetCodeStatsDto dto = new LeetCodeStatsDto();
        dto.setCurrentRating(currentRating);
        dto.setHighestRating(highestRating);
        dto.setGlobalRank(globalRank);
        dto.setTotalSolved(totalSolved);
        dto.setContestsAttended(contestsAttended);
        dto.setLastActive(lastActive);
        dto.setEasySolved(easySolved);
        dto.setMediumSolved(mediumSolved);
        dto.setHardSolved(hardSolved);
        return dto;
    }

    private String[] getAllSolved(Map<String, Object> matchedUser) {
        String easy = "0", medium = "0", hard = "0", total = "0";

        if (matchedUser == null)
            return new String[]{easy, medium, hard, total};

        Map<String, Object> submitStatsGlobal =
                (Map<String, Object>) matchedUser.get("submitStatsGlobal");

        if (submitStatsGlobal == null)
            return new String[]{easy, medium, hard, total};

        List<Map<String, Object>> acSubmissionNum =
                (List<Map<String, Object>>) submitStatsGlobal.get("acSubmissionNum");

        if (acSubmissionNum == null)
            return new String[]{easy, medium, hard, total};

        for (Map<String, Object> item : acSubmissionNum) {
            String difficulty = String.valueOf(item.get("difficulty"));
            String count      = String.valueOf(item.get("count"));
            switch (difficulty) {
                case "Easy"   -> easy   = count;
                case "Medium" -> medium = count;
                case "Hard"   -> hard   = count;
                case "All"    -> total  = count;
            }
        }

        return new String[]{easy, medium, hard, total};
    }

    private String getCurrentRating(Map<String, Object> contestRanking) {

        if (contestRanking == null || contestRanking.get("rating") == null) {
            return "N/A";
        }

        double rating = Double.parseDouble(
                contestRanking.get("rating").toString()
        );

        return String.valueOf((int) rating);
    }

    private String getGlobalRank(Map<String, Object> contestRanking) {

        if (contestRanking == null || contestRanking.get("globalRanking") == null) {
            return "N/A";
        }

        return String.valueOf(contestRanking.get("globalRanking"));
    }

    private String getContestCount(Map<String, Object> contestRanking) {

        if (contestRanking == null || contestRanking.get("attendedContestsCount") == null) {
            return "N/A";
        }

        return String.valueOf(contestRanking.get("attendedContestsCount"));
    }

    private String getHighestRating(List<Map<String, Object>> contestHistory) {

        if (contestHistory == null || contestHistory.isEmpty()) {
            return "N/A";
        }

        double max = -1;

        for (Map<String, Object> entry : contestHistory) {

            Object rating = entry.get("rating");

            if (rating instanceof Number number) {
                max = Math.max(max, number.doubleValue());
            }
        }

        return max == -1 ? "N/A" : String.valueOf((int) max);
    }

    private String getLastActive(Map<String, Object> matchedUser) {

        try {

            if (matchedUser == null) return "N/A";

            String calendar =
                    String.valueOf(matchedUser.get("submissionCalendar"));

            if (calendar == null || calendar.equals("null")) {
                return "N/A";
            }

            String cleaned = calendar.replaceAll("[{}\"]", "");
            String[] entries = cleaned.split(",");

            long latest = 0;

            for (String entry : entries) {

                String[] pair = entry.split(":");

                if (pair.length > 0) {
                    long timestamp = Long.parseLong(pair[0].trim());

                    if (timestamp > latest) {
                        latest = timestamp;
                    }
                }
            }

            if (latest == 0) return "N/A";

            return Instant.ofEpochSecond(latest)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        } catch (Exception e) {
            return "N/A";
        }
    }

    private LeetCodeStatsDto emptyDto() {
        LeetCodeStatsDto dto = new LeetCodeStatsDto();
        dto.setCurrentRating("N/A");
        dto.setHighestRating("N/A");
        dto.setGlobalRank("N/A");
        dto.setTotalSolved("0");
        dto.setContestsAttended("N/A");
        dto.setLastActive("N/A");
        dto.setEasySolved("0");
        dto.setMediumSolved("0");
        dto.setHardSolved("0");
        return dto;
    }
}