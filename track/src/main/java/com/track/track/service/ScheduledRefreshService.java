package com.track.track.service;

import com.track.track.model.Student;
import com.track.track.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ScheduledRefreshService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StatsRefreshService statsRefreshService;

    @Autowired
    private RankService rankService;

    private static final ZoneId IST =
            ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy hh:mm:ssa");

    // ══════════════════════════════════════════════
    // CORE METHOD
    // Checks each student's rating before and after
    // If rating changed → records exact timestamp
    // ══════════════════════════════════════════════
    private void detectChanges() {

        String now = LocalDateTime.now(IST)
                .format(FMT);

        List<Student> all =
                studentRepository.findAll();

        boolean anyChanged = false;

        for (Student s : all) {
            try {
                // Save old ratings
                int oldLc = safe(
                        s.getLeetcodeContestRating());
                int oldCc = safe(
                        s.getCodechefCurrentRating());
                int oldCf = safe(
                        s.getCodeforcesCurrentRating());

                // Fetch latest from platforms
                statsRefreshService
                        .refreshStudent(s.getId());

                // Reload from DB
                Student u = studentRepository
                        .findById(s.getId())
                        .orElse(null);
                if (u == null) continue;

                boolean changed = false;

                // LeetCode changed?
                if (safe(u.getLeetcodeContestRating())
                        != oldLc &&
                        safe(u.getLeetcodeContestRating())
                                > 0) {
                    u.setLeetcodeLastUpdated(now);
                    changed = true;
                    System.out.println(
                            "🟡 LeetCode rating updated: "
                                    + s.getName()
                                    + " | " + oldLc
                                    + " → "
                                    + u.getLeetcodeContestRating()
                                    + " at " + now);
                }

                // CodeChef changed?
                if (safe(u.getCodechefCurrentRating())
                        != oldCc &&
                        safe(u.getCodechefCurrentRating())
                                > 0) {
                    u.setCodechefLastUpdated(now);
                    changed = true;
                    System.out.println(
                            "⭐ CodeChef rating updated: "
                                    + s.getName()
                                    + " | " + oldCc
                                    + " → "
                                    + u.getCodechefCurrentRating()
                                    + " at " + now);
                }

                // Codeforces changed?
                if (safe(u.getCodeforcesCurrentRating())
                        != oldCf &&
                        safe(u.getCodeforcesCurrentRating())
                                > 0) {
                    u.setCodeforcesLastUpdated(now);
                    changed = true;
                    System.out.println(
                            "🔵 Codeforces rating updated: "
                                    + s.getName()
                                    + " | " + oldCf
                                    + " → "
                                    + u.getCodeforcesCurrentRating()
                                    + " at " + now);
                }

                if (changed) {
                    studentRepository.save(u);
                    anyChanged = true;
                }

            } catch (Exception e) {
                System.out.println(
                        "❌ " + s.getName()
                                + ": " + e.getMessage());
            }
        }

        if (anyChanged) {
            rankService.recalculateAllRanks();
            System.out.println(
                    "🏆 Ranks updated at " + now);
        }
    }

    // ══════════════════════════════════════════════
    // CODECHEF — predictable schedule
    // Contest: Every Wednesday 8pm-10pm IST
    // Rating update window: 10pm-11pm IST
    // Poll every 1 MINUTE during that window
    // ══════════════════════════════════════════════
    @Scheduled(
            cron = "0 * 22 * * WED",
            zone = "Asia/Kolkata"
    )
    public void codechefWindow() {
        System.out.println(
                "⭐ CodeChef 1-min check: "
                        + LocalDateTime.now(IST).format(FMT));
        detectChanges();
    }

    // Also check Wednesday 11pm-midnight
    // in case it's delayed
    @Scheduled(
            cron = "0 * 23 * * WED",
            zone = "Asia/Kolkata"
    )
    public void codechefDelayedWindow() {
        System.out.println(
                "⭐ CodeChef delayed check: "
                        + LocalDateTime.now(IST).format(FMT));
        detectChanges();
    }

    // Also check Thursday 12am-1am
    // in case it's very delayed
    @Scheduled(
            cron = "0 * 0 * * THU",
            zone = "Asia/Kolkata"
    )
    public void codechefOvernightWindow() {
        detectChanges();
    }

    // ══════════════════════════════════════════════
    // LEETCODE — unpredictable
    // Poll every 5 MINUTES all day
    // ══════════════════════════════════════════════
    @Scheduled(
            cron = "0 */5 * * * *",
            zone = "Asia/Kolkata"
    )
    public void leetcodeAndCodeforcesRefresh() {
        detectChanges();
    }

    // ══════════════════════════════════════════════
    // STARTUP — run once after 1 minute of boot
    // ══════════════════════════════════════════════
    @Scheduled(
            initialDelay = 60 * 1000,
            fixedDelay = Long.MAX_VALUE
    )
    public void startupRefresh() {
        System.out.println("🚀 Startup refresh");
        detectChanges();
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}