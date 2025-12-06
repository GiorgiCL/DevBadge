package com.devbadge.devbadge.repository;

import com.devbadge.devbadge.entity.AnalysisCache;
import com.devbadge.devbadge.entity.GitHubUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface  AnalysisCacheRepository extends JpaRepository<AnalysisCache, Long> {
    Optional<AnalysisCache> findByUserAndCacheKeyAndExpiresAtAfter(GitHubUser user,String cacheKey, LocalDateTime currentTime);
    void deleteByExpiresAtBefore(LocalDateTime currentTime);
}
