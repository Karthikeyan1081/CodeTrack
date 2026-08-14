package com.track.track.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents whether a student attended the most recent (finished) contest
 * on a given platform, and — where the platform's API allows it — exactly
 * which questions they solved.
 *
 * attended:
 *   TRUE   -> student took part in the latest finished contest
 *   FALSE  -> student did NOT take part (contestName/contestDate still filled
 *             in, so the UI can show "Not attended — last contest was X on dd/mm/yyyy")
 *   null   -> platform username not set for this student, or the lookup failed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastContestDto {

    private String platform;              // "leetcode" | "codechef" | "codeforces"
    private Boolean attended;
    private String contestName;
    private String contestDate;           // ISO yyyy-MM-dd (IST)
    private Integer solvedCount;          // null when the platform can't tell us
    private Integer totalProblems;        // null when unknown
    private List<String> solvedProblemNames; // null when unavailable (e.g. CodeChef)
}
