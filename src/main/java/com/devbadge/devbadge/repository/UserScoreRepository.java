package com.devbadge.devbadge.repository;

import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.entity.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserScoreRepository extends JpaRepository<UserScore, Long> {
    Optional<UserScore> findByUser(GitHubUser user);
    Optional<UserScore> findByUserId(Long userId);
}
