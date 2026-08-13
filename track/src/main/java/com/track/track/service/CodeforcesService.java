package com.track.track.service;

import com.track.track.dto.CodeforcesStatsDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CodeforcesService {

    private final RestTemplate restTemplate;

    public CodeforcesService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CodeforcesStatsDto getCodeforcesStats(String username) {

        try {

            String userUrl =
                    "https://codeforces.com/api/user.info?handles=" + username;

            Map response =
                    restTemplate.getForObject(userUrl, Map.class);

            List<Map> result =
                    (List<Map>) response.get("result");

            Map user = result.get(0);

            String currentRating =
                    String.valueOf(user.getOrDefault("rating", "N/A"));

            String highestRating =
                    String.valueOf(user.getOrDefault("maxRating", "N/A"));

            String globalRank =
                    String.valueOf(user.getOrDefault("rank", "N/A"));

            String contests =
                    getContestCount(username);

            String solved =
                    getSolvedCount(username);

            String lastActive =
                    getLastActive(username);

            CodeforcesStatsDto dto = new CodeforcesStatsDto();
            dto.setCurrentRating(currentRating);
            dto.setHighestRating(highestRating);
            dto.setGlobalRank(globalRank);
            dto.setTotalSolved(solved);
            dto.setContestsAttended(contests);
            dto.setLastActive(lastActive);
            return dto;

        } catch (Exception e) {
            CodeforcesStatsDto dto = new CodeforcesStatsDto();
            dto.setCurrentRating("N/A");
            dto.setHighestRating("N/A");
            dto.setGlobalRank("N/A");
            dto.setTotalSolved("0");
            dto.setContestsAttended("N/A");
            dto.setLastActive("N/A");
            return dto;
        }
    }

    private String getContestCount(String username) {

        try {

            String url =
                    "https://codeforces.com/api/user.rating?handle=" + username;

            Map response =
                    restTemplate.getForObject(url, Map.class);

            List<?> contests =
                    (List<?>) response.get("result");

            return String.valueOf(contests.size());

        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getSolvedCount(String username) {

        try {

            String url =
                    "https://codeforces.com/api/user.status?handle=" + username;

            Map response =
                    restTemplate.getForObject(url, Map.class);

            List<Map> submissions =
                    (List<Map>) response.get("result");

            Set<String> solvedProblems = new HashSet<>();

            for (Map submission : submissions) {

                Object verdict = submission.get("verdict");

                if (verdict != null && verdict.toString().equals("OK")) {

                    Map problem = (Map) submission.get("problem");

                    if (problem != null) {

                        Object contestId = problem.get("contestId");
                        Object index = problem.get("index");

                        if (contestId != null && index != null) {
                            solvedProblems.add(contestId.toString() + index.toString());
                        }
                    }
                }
            }

            return String.valueOf(solvedProblems.size());

        } catch (Exception e) {
            return "0";
        }
    }

    private String getLastActive(String username) {

        try {

            String url =
                    "https://codeforces.com/api/user.status?handle=" + username;

            Map response =
                    restTemplate.getForObject(url, Map.class);

            List<Map> submissions =
                    (List<Map>) response.get("result");

            if (submissions == null || submissions.isEmpty()) {
                return "N/A";
            }

            Map latestSubmission = submissions.get(0);

            Object timeObj = latestSubmission.get("creationTimeSeconds");

            if (timeObj == null) {
                return "N/A";
            }

            long timestamp =
                    Long.parseLong(timeObj.toString());

            return Instant.ofEpochSecond(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        } catch (Exception e) {
            return "N/A";
        }
    }
}