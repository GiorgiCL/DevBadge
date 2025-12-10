package com.devbadge.devbadge.service;

import com.devbadge.devbadge.dto.user.GitHubUserRequest;
import com.devbadge.devbadge.dto.user.GitHubUserResponse;
import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.repository.GitHubUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubUserService {

    private final GitHubUserRepository repository;

    public GitHubUserResponse create(GitHubUserRequest request) {
        GitHubUser user = GitHubUser.builder()
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .bio(request.getBio())
                .location(request.getLocation())
                .build();

        repository.save(user);

        return toResponse(user);
    }

    public List<GitHubUserResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public GitHubUserResponse getById(Long id) {
        GitHubUser user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toResponse(user);
    }

    public GitHubUserResponse update(Long id, GitHubUserRequest request) {
        GitHubUser user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        user.setLocation(request.getLocation());

        repository.save(user);

        return toResponse(user);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        repository.deleteById(id);
    }

    private GitHubUserResponse toResponse(GitHubUser user) {
        return GitHubUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .location(user.getLocation())
                .build();
    }
}
