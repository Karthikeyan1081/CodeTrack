package com.track.track.controller;

import com.track.track.dto.CodeChefStatsDto;
import com.track.track.service.CodeChefService;
import com.track.track.service.LastContestService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/codechef")
@CrossOrigin(origins = "*")
public class CodeChefController {

    private final CodeChefService codeChefService;
    private final LastContestService lastContestService;

    public CodeChefController(CodeChefService codeChefService,
                              LastContestService lastContestService) {
        this.codeChefService = codeChefService;
        this.lastContestService = lastContestService;
    }

    @GetMapping("/{username}")
    public CodeChefStatsDto getCodeChefStats(@PathVariable String username) {
        CodeChefStatsDto dto = codeChefService.getCodeChefStats(username);
        lastContestService.fetchCodeChefLastContest(username, dto);
        return dto;
    }
}