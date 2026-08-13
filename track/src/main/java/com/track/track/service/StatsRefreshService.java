package com.track.track.service;

import com.track.track.dto.CodeChefStatsDto;
import com.track.track.dto.CodeforcesStatsDto;
import com.track.track.dto.LeetCodeStatsDto;
import com.track.track.model.Student;
import com.track.track.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatsRefreshService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LeetCodeService leetCodeService;

    @Autowired
    private CodeChefService codeChefService;

    @Autowired
    private CodeforcesService codeforcesService;

    // =====================================================
    // REFRESH ONE STUDENT
    // =====================================================

    public Student refreshStudent(Long id) {

        Student s = studentRepository
                .findById(id)
                .orElse(null);

        if (s == null) return null;

        fetchAndSave(s);

        return studentRepository.save(s);
    }

    // =====================================================
    // REFRESH ALL STUDENTS
    // =====================================================

    public void refreshAll() {

        List<Student> all =
                studentRepository.findAll();

        for (Student s : all) {
            try {
                fetchAndSave(s);
                studentRepository.save(s);
            } catch (Exception e) {
                // skip failed student, continue
            }
        }
    }

    // =====================================================
    // REFRESH ALL IN A DEPARTMENT
    // =====================================================

    public void refreshDepartment(String department) {

        List<Student> students =
                studentRepository
                        .findByDepartment(department);

        for (Student s : students) {
            try {
                fetchAndSave(s);
                studentRepository.save(s);
            } catch (Exception e) {
                // skip
            }
        }
    }

    // =====================================================
    // FETCH FROM ALL 3 PLATFORMS AND UPDATE STUDENT
    // =====================================================

    private void fetchAndSave(Student s) {

        String now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter
                        .ofPattern("dd/MM/yyyy HH:mm:ss"));

        // ── LEETCODE ──
        if (s.getLeetcodeUsername() != null &&
                !s.getLeetcodeUsername().isBlank()) {
            try {
                LeetCodeStatsDto lc =
                        leetCodeService.getLeetCodeStats(
                                s.getLeetcodeUsername());

                // Track if rating changed
                int oldRating = safe(s.getLeetcodeContestRating());

                if (!"N/A".equals(lc.getTotalSolved()))
                    s.setLeetcodeTotalSolved(
                            parseIntSafe(lc.getTotalSolved()));
                if (!"N/A".equals(lc.getCurrentRating()))
                    s.setLeetcodeContestRating(
                            parseIntSafe(lc.getCurrentRating()));
                if (!"N/A".equals(lc.getGlobalRank()))
                    s.setLeetcodeGlobalRank(lc.getGlobalRank());
                if (!"N/A".equals(lc.getContestsAttended()))
                    s.setLeetcodeContestCount(
                            parseIntSafe(lc.getContestsAttended()));
                if (!"N/A".equals(lc.getLastActive()))
                    s.setLeetcodeLastActive(lc.getLastActive());
                if (!"N/A".equals(lc.getEasySolved()))
                    s.setLeetcodeEasySolved(
                            parseIntSafe(lc.getEasySolved()));
                if (!"N/A".equals(lc.getMediumSolved()))
                    s.setLeetcodeMediumSolved(
                            parseIntSafe(lc.getMediumSolved()));
                if (!"N/A".equals(lc.getHardSolved()))
                    s.setLeetcodeHardSolved(
                            parseIntSafe(lc.getHardSolved()));

                // ✅ Save timestamp ONLY when rating changed
                int newRating = safe(s.getLeetcodeContestRating());
                if (newRating != oldRating && newRating > 0) {
                    s.setLastUpdated(now);
                }

            } catch (Exception ignored) {}
        }

        // ── CODECHEF ──
        if (s.getCodechefUsername() != null &&
                !s.getCodechefUsername().isBlank()) {
            try {
                CodeChefStatsDto cc =
                        codeChefService.getCodeChefStats(
                                s.getCodechefUsername());

                // Track if rating changed
                int oldRating = safe(s.getCodechefCurrentRating());

                if (!"N/A".equals(cc.getCurrentRating()))
                    s.setCodechefCurrentRating(
                            parseIntSafe(cc.getCurrentRating()));
                if (!"N/A".equals(cc.getHighestRating()))
                    s.setCodechefHighestRating(
                            parseIntSafe(cc.getHighestRating()));
                if (!"N/A".equals(cc.getGlobalRank()))
                    s.setCodechefGlobalRank(cc.getGlobalRank());
                if (!"N/A".equals(cc.getTotalSolved()))
                    s.setCodechefProblemsSolved(
                            parseIntSafe(cc.getTotalSolved()));
                if (!"N/A".equals(cc.getContestsAttended()))
                    s.setCodechefContests(
                            parseIntSafe(cc.getContestsAttended()));
                if (!"N/A".equals(cc.getLastActive()))
                    s.setCodechefLastActive(cc.getLastActive());
                if (!"N/A".equals(cc.getStars()))
                    s.setCodechefStars(cc.getStars());
                if (!"N/A".equals(cc.getCountryRank()))
                    s.setCodechefCountryRank(cc.getCountryRank());
                if (!"N/A".equals(cc.getDivision()))
                    s.setCodechefDivision(cc.getDivision());

                // ✅ Save timestamp ONLY when rating changed
                int newRating = safe(s.getCodechefCurrentRating());
                if (newRating != oldRating && newRating > 0) {
                    s.setLastUpdated(now);
                }

            } catch (Exception ignored) {}
        }

        // ── CODEFORCES ──
        if (s.getCodeforcesUsername() != null &&
                !s.getCodeforcesUsername().isBlank()) {
            try {
                CodeforcesStatsDto cf =
                        codeforcesService.getCodeforcesStats(
                                s.getCodeforcesUsername());

                // Track if rating changed
                int oldRating = safe(s.getCodeforcesCurrentRating());

                if (!"N/A".equals(cf.getCurrentRating()))
                    s.setCodeforcesCurrentRating(
                            parseIntSafe(cf.getCurrentRating()));
                if (!"N/A".equals(cf.getHighestRating()))
                    s.setCodeforcesMaxRating(
                            parseIntSafe(cf.getHighestRating()));
                if (!"N/A".equals(cf.getGlobalRank()))
                    s.setCodeforcesCurrentRank(cf.getGlobalRank());
                if (!"N/A".equals(cf.getTotalSolved()))
                    s.setCodeforcesProblemsCount(
                            parseIntSafe(cf.getTotalSolved()));
                if (!"N/A".equals(cf.getContestsAttended()))
                    s.setCodeforcesContests(
                            parseIntSafe(cf.getContestsAttended()));
                if (!"N/A".equals(cf.getLastActive()))
                    s.setCodeforcesLastActive(cf.getLastActive());

                // ✅ Save timestamp ONLY when rating changed
                int newRating = safe(s.getCodeforcesCurrentRating());
                if (newRating != oldRating && newRating > 0) {
                    s.setLastUpdated(now);
                }

            } catch (Exception ignored) {}
        }

        // ── RECALCULATE TOTALS ──
        int totalSolved =
                safe(s.getLeetcodeTotalSolved()) +
                        safe(s.getCodechefProblemsSolved()) +
                        safe(s.getCodeforcesProblemsCount());

        int totalContests =
                safe(s.getLeetcodeContestCount()) +
                        safe(s.getCodechefContests()) +
                        safe(s.getCodeforcesContests());

        int highest = Math.max(
                Math.max(
                        safe(s.getLeetcodeContestRating()),
                        safe(s.getCodechefHighestRating())
                ),
                safe(s.getCodeforcesMaxRating())
        );

        s.setTotalSolved(totalSolved);
        s.setTotalContests(totalContests);
        s.setHighestRating(highest);

        // ✅ Overall last updated with date+time
        s.setLastUpdated(now);
    }

    // =====================================================
    // SAFE PARSE
    // =====================================================

    private int parseIntSafe(String v) {
        try {
            return Integer.parseInt(
                    v.replaceAll("[^0-9]", "")
            );
        } catch (Exception e) {
            return 0;
        }
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}