package com.track.track.service;

import com.track.track.dto.GitHubStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${github.api.token:}")
    private String githubToken;

    private static final String BASE =
            "https://api.github.com";

    private HttpEntity<?> authEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github+json");
        return new HttpEntity<>(headers);
    }

    public GitHubStatsDto getGitHubStats(
            String username
    ) {
        try {

            // ── USER PROFILE ──
            String userUrl =
                    BASE + "/users/" + username;

            ResponseEntity<Map> userResp =
                    restTemplate.exchange(
                            userUrl, HttpMethod.GET,
                            authEntity(), Map.class);

            Map<?, ?> user = userResp.getBody();

            if (user == null) return empty(username);

            GitHubStatsDto dto = new GitHubStatsDto();

            dto.setUsername(username);
            dto.setName(str(user.get("name")));
            dto.setBio(str(user.get("bio")));
            dto.setAvatarUrl(str(user.get("avatar_url")));
            dto.setPublicRepos(num(user.get("public_repos")));
            dto.setFollowers(num(user.get("followers")));
            dto.setFollowing(num(user.get("following")));
            dto.setLocation(str(user.get("location")));
            dto.setCompany(str(user.get("company")));
            dto.setBlog(str(user.get("blog")));
            dto.setCreatedAt(str(user.get("created_at")));
            dto.setProfileUrl(
                    "https://github.com/" + username
            );

            // ── REPOS — stars, forks, top language ──
            String reposUrl =
                    BASE + "/users/" + username +
                            "/repos?per_page=100&sort=updated";

            ResponseEntity<List> reposResp =
                    restTemplate.exchange(
                            reposUrl, HttpMethod.GET,
                            authEntity(), List.class);

            List<Map<?, ?>> repos = reposResp.getBody();

            int totalStars = 0;
            int totalForks = 0;
            Map<String, Integer> langCount =
                    new java.util.HashMap<>();

            if (repos != null) {
                for (Map<?, ?> repo : repos) {

                    Boolean fork =
                            (Boolean) repo.get("fork");
                    if (Boolean.TRUE.equals(fork))
                        continue;

                    totalStars +=
                            num(repo.get("stargazers_count"));
                    totalForks +=
                            num(repo.get("forks_count"));

                    String lang =
                            str(repo.get("language"));
                    if (!lang.equals("-") &&
                            !lang.isEmpty()) {
                        langCount.merge(lang, 1,
                                Integer::sum);
                    }
                }
            }

            dto.setTotalStars(totalStars);
            dto.setTotalForks(totalForks);

            String topLang = langCount.entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("-");

            dto.setTopLanguage(topLang);

            // ── COMMIT ACTIVITY (last year) ──
            try {
                String eventsUrl =
                        BASE + "/users/" + username +
                                "/events?per_page=100";

                ResponseEntity<List> eventsResp =
                        restTemplate.exchange(
                                eventsUrl, HttpMethod.GET,
                                authEntity(), List.class);

                List<Map<?, ?>> events =
                        eventsResp.getBody();

                int commits = 0;
                if (events != null) {
                    for (Map<?, ?> event : events) {
                        if ("PushEvent".equals(
                                event.get("type"))) {
                            Map<?, ?> payload =
                                    (Map<?, ?>) event
                                            .get("payload");
                            if (payload != null) {
                                Object size =
                                        payload.get("size");
                                commits += num(size);
                            }
                        }
                    }
                }
                dto.setTotalCommits(commits);
            } catch (Exception ignored) {
                dto.setTotalCommits(0);
            }

            return dto;

        } catch (Exception e) {
            System.out.println(
                    "GitHub fetch error for " + username
                            + ": " + e.getMessage());
            return empty(username);
        }
    }

    private GitHubStatsDto empty(String username) {
        GitHubStatsDto dto = new GitHubStatsDto();
        dto.setUsername(username);
        dto.setProfileUrl(
                "https://github.com/" + username
        );
        dto.setTopLanguage("-");
        return dto;
    }

    private String str(Object o) {
        return (o == null) ? "-" :
                o.toString().isBlank() ? "-" :
                        o.toString();
    }

    private int num(Object o) {
        if (o == null) return 0;
        try {
            return ((Number) o).intValue();
        } catch (Exception e) { return 0; }
    }
}