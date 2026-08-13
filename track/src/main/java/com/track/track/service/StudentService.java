package com.track.track.service;

import com.track.track.model.Student;
import com.track.track.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StatsRefreshService statsRefreshService;

    @Autowired
    private RankService rankService;

    // ---------------- SAVE ----------------
    // Auto-fetches platform stats + recalculates ranks
    // asynchronously after saving

    public Student saveStudent(Student student) {
        Student saved = studentRepository.save(student);
        triggerStatsRefresh(saved.getId());
        return saved;
    }

    // Runs in background thread — does NOT block the
    // HTTP response. Student gets saved immediately,
    // stats arrive within a few seconds.
    @Async
    public void triggerStatsRefresh(Long id) {
        try {
            statsRefreshService.refreshStudent(id);
            rankService.recalculateAllRanks();
        } catch (Exception e) {
            System.out.println(
                    "❌ Auto-refresh failed for id "
                            + id + ": " + e.getMessage()
            );
        }
    }

    // ---------------- GET ALL ----------------

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ---------------- GET BY DEPARTMENT ----------------

    public List<Student> getByDepartment(
            String department
    ) {
        return studentRepository
                .findByDepartment(department);
    }

    // ---------------- GET BY ID ----------------

    public Student getStudentById(Long id) {
        return studentRepository
                .findById(id)
                .orElse(null);
    }

    // ---------------- GET BY REGISTER ----------------

    public Student getByRegisterNumber(
            String registerNumber
    ) {
        return studentRepository
                .findByRegisterNumber(registerNumber);
    }

    // ---------------- UPDATE ----------------

    public Student updateStudent(
            Long id,
            Student u
    ) {
        Student s = studentRepository
                .findById(id)
                .orElse(null);

        if (s == null) return null;

        // BASIC
        s.setName(u.getName());
        s.setRegisterNumber(u.getRegisterNumber());
        s.setYear(u.getYear());
        s.setDepartment(u.getDepartment());
        s.setSection(u.getSection());
        s.setBatch(u.getBatch());
        s.setEmail(u.getEmail());
        s.setPhoneNumber(u.getPhoneNumber());
        s.setLinkedinUrl(u.getLinkedinUrl());
        s.setPortfolioUrl(u.getPortfolioUrl());

        // USERNAMES
        s.setLeetcodeUsername(u.getLeetcodeUsername());
        s.setCodechefUsername(u.getCodechefUsername());
        s.setCodeforcesUsername(u.getCodeforcesUsername());
        s.setGithubUsername(u.getGithubUsername());
        s.setHackerrankUsername(u.getHackerrankUsername());
        s.setGeeksforgeeksUsername(
                u.getGeeksforgeeksUsername());

        // LEETCODE STATS
        s.setLeetcodeEasySolved(u.getLeetcodeEasySolved());
        s.setLeetcodeMediumSolved(u.getLeetcodeMediumSolved());
        s.setLeetcodeHardSolved(u.getLeetcodeHardSolved());
        s.setLeetcodeTotalSolved(u.getLeetcodeTotalSolved());
        s.setLeetcodeContestCount(u.getLeetcodeContestCount());
        s.setLeetcodeContestRating(u.getLeetcodeContestRating());
        s.setLeetcodeGlobalRank(u.getLeetcodeGlobalRank());
        s.setLeetcodeLastActive(u.getLeetcodeLastActive());

        // CODECHEF STATS
        s.setCodechefCurrentRating(u.getCodechefCurrentRating());
        s.setCodechefHighestRating(u.getCodechefHighestRating());
        s.setCodechefDivision(u.getCodechefDivision());
        s.setCodechefStars(u.getCodechefStars());
        s.setCodechefGlobalRank(u.getCodechefGlobalRank());
        s.setCodechefCountryRank(u.getCodechefCountryRank());
        s.setCodechefContests(u.getCodechefContests());
        s.setCodechefProblemsSolved(u.getCodechefProblemsSolved());
        s.setCodechefLastActive(u.getCodechefLastActive());

        // CODEFORCES STATS
        s.setCodeforcesCurrentRating(u.getCodeforcesCurrentRating());
        s.setCodeforcesMaxRating(u.getCodeforcesMaxRating());
        s.setCodeforcesCurrentRank(u.getCodeforcesCurrentRank());
        s.setCodeforcesProblemsCount(u.getCodeforcesProblemsCount());
        s.setCodeforcesContests(u.getCodeforcesContests());
        s.setCodeforcesLastActive(u.getCodeforcesLastActive());

        // OVERALL — auto-calculate
        int total =
                safe(u.getLeetcodeTotalSolved()) +
                        safe(u.getCodechefProblemsSolved()) +
                        safe(u.getCodeforcesProblemsCount());

        int contests =
                safe(u.getLeetcodeContestCount()) +
                        safe(u.getCodechefContests()) +
                        safe(u.getCodeforcesContests());

        int highest = Math.max(
                Math.max(
                        safe(u.getLeetcodeContestRating()),
                        safe(u.getCodechefHighestRating())
                ),
                safe(u.getCodeforcesMaxRating())
        );

        s.setTotalSolved(total);
        s.setTotalContests(contests);
        s.setHighestRating(highest);
        s.setLastUpdated(u.getLastUpdated());
        s.setDepartmentRank(u.getDepartmentRank());
        s.setCollegeRank(u.getCollegeRank());

        return studentRepository.save(s);
    }

    private int safe(Integer v) {
        return (v == null) ? 0 : v;
    }

    // ---------------- DELETE ----------------

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}