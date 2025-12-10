package com.devbadge.devbadge.repository;

import com.devbadge.devbadge.entity.GitHubUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GitHubUserRepositoryTest {

    @Autowired
    private GitHubUserRepository repo;

    @Test
    void testSaveAndFindByUsername() {
        GitHubUser user = GitHubUser.builder()
                .username("giorgi")
                .build();

        repo.save(user);

        Optional<GitHubUser> found = repo.findByUsername("giorgi");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("giorgi");
    }

    @Test
    void testExistsByUsername() {
        GitHubUser user = GitHubUser.builder()
                .username("tester")
                .build();

        repo.save(user);

        assertThat(repo.existsByUsername("tester")).isTrue();
        assertThat(repo.existsByUsername("unknown")).isFalse();
    }
}
