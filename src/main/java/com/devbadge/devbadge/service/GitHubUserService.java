package com.devbadge.devbadge.service;

import com.devbadge.devbadge.dto.user.GitHubUserRequest;
import com.devbadge.devbadge.dto.user.GitHubUserResponse;
import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.entity.ScoreHistory;
import com.devbadge.devbadge.repository.GitHubUserRepository;
import com.devbadge.devbadge.repository.ScoreHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubUserService {

    private final GitHubUserRepository userRepo;
    private final ScoreHistoryRepository historyRepo;

    public GitHubUserResponse create(GitHubUserRequest r) {
        GitHubUser user = GitHubUser.builder()
                .username(r.getUsername())
                .name(r.getName())
                .email(r.getEmail())
                .location(r.getLocation())
                .bio(r.getBio())
                .build();

        userRepo.save(user);

        return toResponse(user);
    }

    public List<GitHubUserResponse> getAll() {
        return userRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GitHubUserResponse getById(Long id) {
        GitHubUser user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toResponse(user);
    }

    public GitHubUserResponse update(Long id, GitHubUserRequest r) {
        GitHubUser user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(r.getUsername());
        user.setName(r.getName());
        user.setEmail(r.getEmail());
        user.setBio(r.getBio());
        user.setLocation(r.getLocation());

        userRepo.save(user);

        return toResponse(user);
    }

    public void delete(Long id) {
        if (!userRepo.existsById(id))
            throw new RuntimeException("User not found");

        userRepo.deleteById(id);
    }

    public List<ScoreHistory> getHistory(Long id) {
        return historyRepo.findByUser_IdOrderByCalculatedAtDesc(id);
    }

    private GitHubUserResponse toResponse(GitHubUser u) {
        return GitHubUserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .name(u.getName())
                .email(u.getEmail())
                .location(u.getLocation())
                .bio(u.getBio())
                .build();
    }
}
