package com.devbadge.devbadge.repository;

import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.entity.ScoreHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ScoreHistoryRepositoryTest {

    @Autowired
    private GitHubUserRepository userRepo;

    @Autowired
    private ScoreHistoryRepository historyRepo;

    @Test
    void testFindByUserOrderByCalculatedAt() {
        GitHubUser user = userRepo.save(GitHubUser.builder()
                .username("giorgi")
                .build());

        ScoreHistory h1 = ScoreHistory.builder()
                .user(user)
                .overallScore(5.0)
                .build();

        ScoreHistory h2 = ScoreHistory.builder()
                .user(user)
                .overallScore(7.0)
                .build();

        historyRepo.save(h1);
        historyRepo.save(h2);

        List<ScoreHistory> result = historyRepo.findByUserOrderByCalculatedAtDesc(user);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOverallScore()).isEqualTo(7.0);
    }
}
