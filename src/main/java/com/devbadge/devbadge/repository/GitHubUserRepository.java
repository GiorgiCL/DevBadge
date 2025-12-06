package com.devbadge.devbadge.repository;

import com.devbadge.devbadge.entity.GitHubUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GitHubUserRepository extends JpaRepository<GitHubUser, Long> {
    Optional<GitHubUser> findByUsername(String username);

    boolean existsByUsername(String username);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM GitHubUser u WHERE u.username = :username")
    Optional<GitHubUser> findByUsernameForUpdate(String username);

}
