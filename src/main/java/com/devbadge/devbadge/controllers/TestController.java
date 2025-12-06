package com.devbadge.devbadge.controllers;

import com.devbadge.devbadge.dto.github.*;
import com.devbadge.devbadge.service.GitHubApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final GitHubApiService gitHubApiService;

    @GetMapping("/user/{username}")
    public GitHubUserDTO getUser(@PathVariable String username) {
        return gitHubApiService.fetchUserProfile(username);
    }
    @GetMapping("/repos/{username}")
    public List<GitHubRepoDTO> testRepos(@PathVariable String username) {
        return gitHubApiService.fetchUserRepositories(username);
    }
    @GetMapping("/commits/{username}/{repo}")
public List<GitHubCommitDTO> testCommits(@PathVariable String username, @PathVariable String repo, @RequestParam(defaultValue = "30") int limit) {
    return gitHubApiService.fetchRepositoryCommits(username, repo, limit);
}
@GetMapping("/prs/{username}/{repo}")
    public List<GitHubPullRequestDTO> testPullRequests(@PathVariable String username, @PathVariable String repo) {
        return gitHubApiService.fetchPullRequests(username, repo);
}
@GetMapping("/issues/{username}/{repo}")
    public List<GitHubIssueDTO> testIssues(@PathVariable String username, @PathVariable String repo) {
        return gitHubApiService.fetchIssues(username, repo);
}
}
