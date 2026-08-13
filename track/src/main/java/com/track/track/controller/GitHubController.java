package com.track.track.controller;

import com.track.track.dto.GitHubStatsDto;
import com.track.track.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/github")
@CrossOrigin(origins = "*")
public class GitHubController {

    @Autowired
    private GitHubService gitHubService;

    @GetMapping("/{username}")
    public GitHubStatsDto getStats(
            @PathVariable String username
    ) {
        return gitHubService.getGitHubStats(username);
    }
}