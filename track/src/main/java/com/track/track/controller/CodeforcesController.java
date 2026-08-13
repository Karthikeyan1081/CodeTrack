package com.track.track.controller;

import com.track.track.dto.CodeforcesStatsDto;
import com.track.track.service.CodeforcesService;
import com.track.track.service.LastContestService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/codeforces")
@CrossOrigin(origins = "*")
public class CodeforcesController {

    private final CodeforcesService codeforcesService;
    private final LastContestService lastContestService;

    public CodeforcesController(CodeforcesService codeforcesService,
                                LastContestService lastContestService) {
        this.codeforcesService = codeforcesService;
        this.lastContestService = lastContestService;
    }

    @GetMapping("/{username}")
    public CodeforcesStatsDto getStats(@PathVariable String username) {
        CodeforcesStatsDto dto = codeforcesService.getCodeforcesStats(username);
        lastContestService.fetchCodeforcesLastContest(username, dto);
        return dto;
    }
}