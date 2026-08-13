package com.track.track.controller;

import com.track.track.dto.LeetCodeStatsDto;
import com.track.track.service.LeetCodeService;
import com.track.track.service.LastContestService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leetcode")
@CrossOrigin(origins = "*")
public class LeetCodeController {

    private final LeetCodeService leetCodeService;
    private final LastContestService lastContestService;

    public LeetCodeController(LeetCodeService leetCodeService,
                              LastContestService lastContestService) {
        this.leetCodeService = leetCodeService;
        this.lastContestService = lastContestService;
    }

    @GetMapping("/{username}")
    public LeetCodeStatsDto getLeetCodeStats(@PathVariable String username) {
        LeetCodeStatsDto dto = leetCodeService.getLeetCodeStats(username);
        lastContestService.fetchLeetCodeLastContest(username, dto);
        return dto;
    }
}