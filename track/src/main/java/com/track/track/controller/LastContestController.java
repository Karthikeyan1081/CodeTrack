package com.track.track.controller;

import com.track.track.dto.LastContestDto;
import com.track.track.model.Student;
import com.track.track.repository.StudentRepository; // adjust import to your actual repo package/name
import com.track.track.service.CodeChefLastContestService;
import com.track.track.service.CodeforcesLastContestService;
import com.track.track.service.LeetCodeLastContestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GET /students/register/{register}/last-contest
 *
 * Returns a map keyed by platform ("leetcode" | "codechef" | "codeforces"),
 * each value a LastContestDto. Used by all three dashboards (student sees
 * their own, advisor/admin fetch it on demand per student — this hits three
 * external platform APIs, so it is NOT called in bulk for a whole table).
 */
@RestController
@RequestMapping("/students")
public class LastContestController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LeetCodeLastContestService leetCodeService;

    @Autowired
    private CodeforcesLastContestService codeforcesService;

    @Autowired
    private CodeChefLastContestService codeChefService;

    @GetMapping("/register/{register}/last-contest")
    public Map<String, LastContestDto> getLastContestStatus(@PathVariable String register) {
        Student s = studentRepository.findByRegisterNumber(register);
        Map<String, LastContestDto> result = new LinkedHashMap<>();
        if (s == null) return result;

        result.put("leetcode", leetCodeService.getLastContestStatus(s.getLeetcodeUsername()));
        result.put("codeforces", codeforcesService.getLastContestStatus(s.getCodeforcesUsername()));
        result.put("codechef", codeChefService.getLastContestStatus(s.getCodechefUsername()));
        return result;
    }
}
