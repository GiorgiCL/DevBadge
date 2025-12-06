package com.devbadge.devbadge.service;

import com.devbadge.devbadge.entity.AnalysisCache;
import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.repository.AnalysisCacheRepository;
import com.devbadge.devbadge.repository.GitHubUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisCacheService {
    private final AnalysisCacheRepository analysisCacheRepository;
    private final GitHubUserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<String> getCached(String username, String cacheKey){
        GitHubUser user = userRepository.findByUsername(username).orElse(null);
        if(user == null){
            return Optional.empty();
        }
        LocalDateTime now = LocalDateTime.now();
        return analysisCacheRepository.findByUserAndCacheKeyAndExpiresAtAfter(user,cacheKey,now)
                .map(AnalysisCache::getCacheData);
    }
    public void saveCache(String username, String cacheKey, Object data, Duration ttl) {

        GitHubUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Cannot cache: user not found: " + username));

        LocalDateTime expiresAt = LocalDateTime.now().plus(ttl);

        try {
            String json = objectMapper.writeValueAsString(data);

            AnalysisCache cache = AnalysisCache.builder()
                    .user(user)
                    .cacheKey(cacheKey)
                    .cacheData(json)
                    .expiresAt(expiresAt)
                    .build();

            analysisCacheRepository.save(cache);
            log.info("Saved cache for {} / {} (expires at {})", username, cacheKey, expiresAt);

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
