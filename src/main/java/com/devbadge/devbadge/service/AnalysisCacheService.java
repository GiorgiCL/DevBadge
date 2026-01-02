package com.devbadge.devbadge.service;

import com.devbadge.devbadge.entity.AnalysisCache;
import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.repository.AnalysisCacheRepository;
import com.devbadge.devbadge.repository.GitHubUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisCacheService {
    private final AnalysisCacheRepository analysisCacheRepository;
    private final GitHubUserRepository userRepository;
    private final ObjectMapper objectMapper;

    public Optional<String> getCached(String username, String cacheKey) {
        GitHubUser user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Optional.empty();

        LocalDateTime now = LocalDateTime.now();
        return analysisCacheRepository
                .findTopByUserAndCacheKeyAndExpiresAtAfterOrderByExpiresAtDesc(user, cacheKey, now)
                .map(AnalysisCache::getCacheData);
    }

    @Transactional
    public void saveCache(String username, String cacheKey, Object data, Duration ttl) {
        GitHubUser user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            log.warn("Cannot cache: user not found: {}", username);
            return;
        }

        LocalDateTime expiresAt = LocalDateTime.now().plus(ttl);

        try {
            String json = objectMapper.writeValueAsString(data);

            List<AnalysisCache> existing = analysisCacheRepository.findAllByUserAndCacheKeyOrderByExpiresAtDesc(user, cacheKey);

            if (!existing.isEmpty()) {
                AnalysisCache primary = existing.get(0);
                primary.setCacheData(json);
                primary.setExpiresAt(expiresAt);
                analysisCacheRepository.save(primary);

                if (existing.size() > 1) {
                    analysisCacheRepository.deleteAll(existing.subList(1, existing.size()));
                }

                log.info("Updated cache for {} / {} (expires at {})", username, cacheKey, expiresAt);
                return;
            }

            try {
                AnalysisCache cache = AnalysisCache.builder()
                        .user(user)
                        .cacheKey(cacheKey)
                        .cacheData(json)
                        .expiresAt(expiresAt)
                        .build();

                analysisCacheRepository.save(cache);
                log.info("Saved cache for {} / {} (expires at {})", username, cacheKey, expiresAt);
            } catch (DataIntegrityViolationException e) {
                List<AnalysisCache> again = analysisCacheRepository.findAllByUserAndCacheKeyOrderByExpiresAtDesc(user, cacheKey);
                if (!again.isEmpty()) {
                    AnalysisCache primary = again.get(0);
                    primary.setCacheData(json);
                    primary.setExpiresAt(expiresAt);
                    analysisCacheRepository.save(primary);

                    if (again.size() > 1) {
                        analysisCacheRepository.deleteAll(again.subList(1, again.size()));
                    }

                    log.info("Updated cache after race for {} / {} (expires at {})", username, cacheKey, expiresAt);
                } else {
                    throw e;
                }
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cache data: {}", e.getMessage());
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("Cache JSON parse error: {}", e.getMessage());
            return null;
        }
    }
}
