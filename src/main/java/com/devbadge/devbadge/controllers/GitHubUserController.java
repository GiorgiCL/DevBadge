package com.devbadge.devbadge.controllers;

import com.devbadge.devbadge.dto.user.GitHubUserRequest;
import com.devbadge.devbadge.dto.user.GitHubUserResponse;
import com.devbadge.devbadge.entity.ScoreHistory;
import com.devbadge.devbadge.service.GitHubUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class GitHubUserController {

    private final GitHubUserService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public GitHubUserResponse create(@RequestBody GitHubUserRequest request) {
        return service.create(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<GitHubUserResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public GitHubUserResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR')")
    public GitHubUserResponse update(@PathVariable Long id, @RequestBody GitHubUserRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('USER')")
    public List<ScoreHistory> history(@PathVariable Long id) {
        return service.getHistory(id);
    }
}
