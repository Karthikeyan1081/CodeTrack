package com.track.track.model;

import com.track.track.entity.Advisor;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================
    // BASIC STUDENT DETAILS
    // =====================================

    private String name;

    @Column(unique = true)
    private String registerNumber;

    private String year;
    private String department;
    private String section;
    private String batch;

    // =====================================
    // LOGIN DETAILS
    // =====================================

    @Column(unique = true)
    private String email;

    private String password;

    // =====================================
    // ADVISOR MAPPING
    // =====================================

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private Advisor advisor;

    // =====================================
    // PLATFORM USERNAMES
    // =====================================

    private String leetcodeUsername;
    private String codechefUsername;
    private String codeforcesUsername;
    private String githubUsername;
    private String hackerrankUsername;
    private String geeksforgeeksUsername;

    // =====================================
    // OPTIONAL PROFILE
    // =====================================

    private String phoneNumber;
    private String linkedinUrl;
    private String portfolioUrl;
    private String profileImage;

    // =====================================
    // LEETCODE STATS
    // =====================================

    private Integer leetcodeEasySolved   = 0;
    private Integer leetcodeMediumSolved = 0;
    private Integer leetcodeHardSolved   = 0;
    private Integer leetcodeTotalSolved  = 0;
    private Integer leetcodeContestCount = 0;
    private Integer leetcodeContestRating = 0;
    private String  leetcodeGlobalRank;
    private String  leetcodeTopPercentage;
    private String  leetcodeLastActive;

    // =====================================
    // CODECHEF STATS
    // =====================================

    private Integer codechefCurrentRating  = 0;
    private Integer codechefHighestRating  = 0;
    private String  codechefDivision;
    private String  codechefStars;
    private String  codechefGlobalRank;
    private String  codechefCountryRank;
    private Integer codechefContests       = 0;
    private Integer codechefProblemsSolved = 0;
    private String  codechefLastActive;

    // =====================================
    // CODEFORCES STATS
    // =====================================

    private Integer codeforcesCurrentRating = 0;
    private Integer codeforcesMaxRating     = 0;
    private String  codeforcesCurrentRank;
    private String  codeforcesMaxRank;
    private Integer codeforcesProblemsCount = 0;
    private Integer codeforcesContests      = 0;
    private String  codeforcesLevel;
    private String  codeforcesLastActive;

    // =====================================
    // OVERALL SUMMARY
    // =====================================

    private Integer totalSolved    = 0;
    private Integer totalContests  = 0;
    private Integer highestRating  = 0;

    // =====================================
    // RANKINGS
    // =====================================

    private Integer departmentRank;
    private Integer collegeRank;

    // =====================================
    // ACTIVITY
    // =====================================

    // =====================================
// ACTIVITY
// =====================================

    private String lastUpdated;
    private String leetcodeLastUpdated;
    private String codechefLastUpdated;
    private String codeforcesLastUpdated;
}