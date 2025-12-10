package com.devbadge.devbadge.service;

import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.entity.UserScore;
import com.devbadge.devbadge.repository.GitHubUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AnalysisCacheServiceTest {

    @Autowired
    private AnalysisCacheService cache;

    @Autowired
    private GitHubUserRepository userRepo;

    @Test
    void testSaveAndLoadCache() {

        GitHubUser user = userRepo.save(
                GitHubUser.builder()
                        .username("giorgi")
                        .build()
        );

        UserScore s = UserScore.builder()
                .overallScore(9.9)
                .build();

        cache.saveCache("giorgi", "score", s, Duration.ofHours(1));

        var loaded = cache.getCached("giorgi", "score");
        assertThat(loaded).isPresent();

        UserScore restored = cache.fromJson(loaded.get(), UserScore.class);
        assertThat(restored.getOverallScore()).isEqualTo(9.9);
    }
}
