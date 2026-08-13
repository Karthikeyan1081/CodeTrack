package com.track.track.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@RestController
@RequestMapping("/contests")
@CrossOrigin(origins = "*")
public class ContestController {

    private final RestTemplate restTemplate = new RestTemplate();

    // ══════════════════════════════════════
    // CODEFORCES — free public API, no key
    // ══════════════════════════════════════
    @GetMapping("/codeforces")
    public ResponseEntity<?> codeforces() {
        try {
            String url = "https://codeforces.com/api/contest.list?gym=false";
            ResponseEntity<Map> r = restTemplate.getForEntity(url, Map.class);
            List<Map> all = (List<Map>) r.getBody().get("result");

            List<Map> upcoming = new ArrayList<>();
            List<Map> past     = new ArrayList<>();

            for (Map c : all) {
                String phase = (String) c.get("phase");
                if ("BEFORE".equals(phase)) upcoming.add(c);
                else if ("FINISHED".equals(phase) && past.size() < 10)
                    past.add(c);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("upcoming", upcoming);
            result.put("past",     past);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        }
    }

    // ══════════════════════════════════════
    // LEETCODE — GraphQL, no key
    // ══════════════════════════════════════
    @GetMapping("/leetcode")
    public ResponseEntity<?> leetcode() {
        try {
            String url   = "https://leetcode.com/graphql";
            String query = "{ allContests { title titleSlug startTime duration isVirtual } }";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer",    "https://leetcode.com");
            headers.set("User-Agent", "Mozilla/5.0");

            Map<String, Object> body = new HashMap<>();
            body.put("query", query);

            ResponseEntity<Map> r = restTemplate.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, headers), Map.class);

            Map    data = (Map)       r.getBody().get("data");
            List<Map> all  = (List<Map>) data.get("allContests");

            long now = System.currentTimeMillis() / 1000;

            List<Map> upcoming = new ArrayList<>();
            List<Map> past     = new ArrayList<>();

            for (Map c : all) {
                if (Boolean.TRUE.equals(c.get("isVirtual"))) continue;
                long start = ((Number) c.get("startTime")).longValue();
                if (start > now)          upcoming.add(c);
                else if (past.size() < 10) past.add(c);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("upcoming", upcoming);
            result.put("past",     past);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        }
    }

    // ══════════════════════════════════════
    // CODECHEF — public API, no key
    // ══════════════════════════════════════
    @GetMapping("/codechef")
    public ResponseEntity<?> codechef() {
        try {
            String url = "https://www.codechef.com/api/list/contests/all" +
                    "?sort_by=START&sorting_order=asc&offset=0&mode=all";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");

            ResponseEntity<Map> r = restTemplate.exchange(
                    url, HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class);

            Map body = r.getBody();
            List<Map> present = (List<Map>) body.getOrDefault(
                    "present_contests", new ArrayList<>());
            List<Map> future  = (List<Map>) body.getOrDefault(
                    "future_contests",  new ArrayList<>());
            List<Map> past    = (List<Map>) body.getOrDefault(
                    "past_contests",    new ArrayList<>());

            if (past.size() > 10) past = past.subList(0, 10);

            List<Map> upcoming = new ArrayList<>();
            upcoming.addAll(present);
            upcoming.addAll(future);

            Map<String, Object> result = new HashMap<>();
            result.put("upcoming", upcoming);
            result.put("past",     past);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        }
    }

    // ══════════════════════════════════════
    // ALL — combined
    // ══════════════════════════════════════
    @GetMapping("/all")
    public ResponseEntity<?> all() {
        Map<String, Object> result = new LinkedHashMap<>();
        try { result.put("codeforces", codeforces().getBody()); }
        catch (Exception e) { result.put("codeforces", Map.of("error", e.getMessage())); }
        try { result.put("leetcode",   leetcode().getBody()); }
        catch (Exception e) { result.put("leetcode",   Map.of("error", e.getMessage())); }
        try { result.put("codechef",   codechef().getBody()); }
        catch (Exception e) { result.put("codechef",   Map.of("error", e.getMessage())); }
        return ResponseEntity.ok(result);
    }
}