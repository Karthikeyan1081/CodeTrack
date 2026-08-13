package com.track.track.service;

import com.track.track.dto.CodeforcesStatsDto;
import com.track.track.dto.LeetCodeStatsDto;
import com.track.track.dto.CodeChefStatsDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LastContestService {

    @Autowired
    private RestTemplate restTemplate;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.of("Asia/Kolkata"));

    // =====================================================
    // LEETCODE — Last Contest
    // Uses GraphQL userContestRanking query
    // =====================================================

    public void fetchLeetCodeLastContest(
            String username,
            LeetCodeStatsDto dto
    ) {
        try {
            String url = "https://leetcode.com/graphql";

            String query =
                    "{\"query\":\"query($username:String!)" +
                            "{userContestRanking(username:$username)" +
                            "{attended trendDirection problemsSolved " +
                            "finishTimeInSeconds rating ranking " +
                            "contest{title startTime}}}\"," +
                            "\"variables\":{\"username\":\"" +
                            username + "\"}}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer",
                    "https://leetcode.com/" + username + "/");

            HttpEntity<String> entity =
                    new HttpEntity<>(query, headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url, HttpMethod.POST,
                            entity, Map.class
                    );

            Map data = (Map) response.getBody().get("data");
            if (data == null) return;

            List<Map<?, ?>> history =
                    (List<Map<?, ?>>) data
                            .get("userContestRanking");

            if (history == null || history.isEmpty()) {
                dto.setLastContestFinished("No");
                dto.setLastContestName("No contests attended");
                return;
            }

            // First entry = most recent contest
            Map<?, ?> last = history.get(0);

            Boolean attended =
                    (Boolean) last.get("attended");

            Map<?, ?> contest =
                    (Map<?, ?>) last.get("contest");

            String title = contest != null ?
                    (String) contest.get("title") : "N/A";

            Object startTimeObj = contest != null ?
                    contest.get("startTime") : null;

            String date = "N/A";
            if (startTimeObj != null) {
                long ts = ((Number) startTimeObj)
                        .longValue();
                date = FMT.format(
                        Instant.ofEpochSecond(ts)
                );
            }

            Object rank    = last.get("ranking");
            Object solved  = last.get("problemsSolved");

            dto.setLastContestName(title);
            dto.setLastContestDate(date);
            dto.setLastContestRank(
                    rank != null ?
                            String.valueOf(rank) : "N/A"
            );
            dto.setLastContestSolved(
                    solved != null ?
                            String.valueOf(solved) : "0"
            );
            dto.setLastContestFinished(
                    Boolean.TRUE.equals(attended) ?
                            "Yes" : "No"
            );

        } catch (Exception e) {
            dto.setLastContestFinished("N/A");
            dto.setLastContestName("N/A");
            dto.setLastContestSolved("N/A");
        }
    }

    // =====================================================
    // CODECHEF — Last Contest
    // Scrapes contest history from profile page
    // =====================================================

    public void fetchCodeChefLastContest(
            String username,
            CodeChefStatsDto dto
    ) {
        try {
            String url =
                    "https://www.codechef.com/users/" +
                            username;

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0;" +
                            " Win64; x64) AppleWebKit/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(15000)
                    .get();

            // Contest history table
            Element contestTable = doc.selectFirst(
                    "table.dataTable"
            );

            if (contestTable == null) {
                // Try alternative selector
                contestTable = doc.selectFirst(
                        ".contest-participated-count"
                );
                if (contestTable == null) {
                    dto.setLastContestFinished("N/A");
                    dto.setLastContestName("N/A");
                    return;
                }
            }

            // Get first row of contest history
            Element firstRow = contestTable
                    .select("tbody tr").first();

            if (firstRow == null) {
                dto.setLastContestFinished("No");
                dto.setLastContestName(
                        "No contests attended"
                );
                return;
            }

            List<Element> cols =
                    firstRow.select("td");

            String contestName = cols.size() > 0 ?
                    cols.get(0).text() : "N/A";
            String rank = cols.size() > 1 ?
                    cols.get(1).text() : "N/A";
            String solved = cols.size() > 2 ?
                    cols.get(2).text() : "N/A";
            String date = cols.size() > 3 ?
                    cols.get(3).text() : "N/A";

            dto.setLastContestName(contestName);
            dto.setLastContestDate(date);
            dto.setLastContestRank(rank);
            dto.setLastContestSolved(solved);
            dto.setLastContestFinished("Yes");

        } catch (Exception e) {
            dto.setLastContestFinished("N/A");
            dto.setLastContestName("N/A");
            dto.setLastContestSolved("N/A");
        }
    }

    // =====================================================
    // CODEFORCES — Last Contest
    // Uses official API: user.rating + contest.standings
    // =====================================================

    public void fetchCodeforcesLastContest(
            String username,
            CodeforcesStatsDto dto
    ) {
        try {
            // Get contest rating history
            String url =
                    "https://codeforces.com/api/user.rating" +
                            "?handle=" + username;

            ResponseEntity<Map> response =
                    restTemplate.getForEntity(url, Map.class);

            Map body = response.getBody();
            if (body == null ||
                    !"OK".equals(body.get("status"))) {
                dto.setLastContestFinished("N/A");
                return;
            }

            List<Map<?, ?>> result =
                    (List<Map<?, ?>>) body.get("result");

            if (result == null || result.isEmpty()) {
                dto.setLastContestFinished("No");
                dto.setLastContestName(
                        "No contests attended"
                );
                dto.setLastContestSolved("0");
                return;
            }

            // Last entry = most recent contest
            Map<?, ?> last =
                    result.get(result.size() - 1);

            String contestName =
                    (String) last.get("contestName");
            Object rank = last.get("rank");
            Object ts   = last.get("ratingUpdateTimeSeconds");

            String date = "N/A";
            if (ts != null) {
                date = FMT.format(
                        Instant.ofEpochSecond(
                                ((Number) ts).longValue()
                        )
                );
            }

            dto.setLastContestName(
                    contestName != null ?
                            contestName : "N/A"
            );
            dto.setLastContestDate(date);
            dto.setLastContestRank(
                    rank != null ?
                            String.valueOf(rank) : "N/A"
            );
            dto.setLastContestFinished("Yes");

            // Get problems solved in last contest
            // via user.status API
            try {
                Object contestId =
                        last.get("contestId");

                if (contestId != null) {
                    String statusUrl =
                            "https://codeforces.com/api/" +
                                    "contest.standings?contestId=" +
                                    contestId +
                                    "&handles=" + username +
                                    "&showUnofficial=false";

                    ResponseEntity<Map> statusResp =
                            restTemplate.getForEntity(
                                    statusUrl, Map.class
                            );

                    Map statusBody =
                            statusResp.getBody();

                    if (statusBody != null &&
                            "OK".equals(
                                    statusBody.get("status")
                            )) {

                        Map resultMap =
                                (Map) statusBody
                                        .get("result");

                        List<Map<?, ?>> rows =
                                (List<Map<?, ?>>) resultMap
                                        .get("rows");

                        if (rows != null &&
                                !rows.isEmpty()) {

                            Map<?, ?> row =
                                    rows.get(0);
                            List<?> problemResults =
                                    (List<?>) row
                                            .get("problemResults");

                            int solved = 0;
                            if (problemResults != null) {
                                for (Object pr :
                                        problemResults) {
                                    Map<?, ?> prMap =
                                            (Map<?, ?>) pr;
                                    Object pts =
                                            prMap.get(
                                                    "points"
                                            );
                                    if (pts != null &&
                                            ((Number) pts)
                                                    .doubleValue()
                                                    > 0) {
                                        solved++;
                                    }
                                }
                            }
                            dto.setLastContestSolved(
                                    String.valueOf(solved)
                            );
                        }
                    }
                }
            } catch (Exception ignored) {
                dto.setLastContestSolved("N/A");
            }

        } catch (Exception e) {
            dto.setLastContestFinished("N/A");
            dto.setLastContestName("N/A");
            dto.setLastContestSolved("N/A");
        }
    }
}