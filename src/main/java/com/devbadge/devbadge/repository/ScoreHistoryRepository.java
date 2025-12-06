package com.devbadge.devbadge.repository;

import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.entity.ScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, Long> {
    List<ScoreHistory> findByUserOrderByCalculatedAt(GitHubUser user);
    List<ScoreHistory> findByUserIdOrderByCalculatedAt(Long userId);
}
