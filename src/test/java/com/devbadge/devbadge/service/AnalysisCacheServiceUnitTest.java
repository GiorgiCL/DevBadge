package com.devbadge.devbadge.service;

import com.devbadge.devbadge.entity.AnalysisCache;
import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.entity.UserScore;
import com.devbadge.devbadge.repository.AnalysisCacheRepository;
import com.devbadge.devbadge.repository.GitHubUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisCacheServiceUnitTest {

    private AnalysisCacheRepository cacheRepo;
    private GitHubUserRepository userRepo;
    private AnalysisCacheService cacheService;

    private final Map<String, GitHubUser> users = new HashMap<>();
    private final Map<String, AnalysisCache> store = new HashMap<>();

    @BeforeEach
    void setUp() {
        users.clear();
        store.clear();

        cacheRepo = mock(AnalysisCacheRepository.class);
        userRepo = mock(GitHubUserRepository.class);
        cacheService = new AnalysisCacheService(cacheRepo, userRepo, new ObjectMapper());
    }

    @Test
    void saveCache_thenGetCached_returnsPresent_andRoundTripsJson() {
        putUser("u1");
        stubUserLookup();
        stubCacheDeleteSaveFind();

        UserScore score = UserScore.builder().overallScore(9.9).build();
        cacheService.saveCache("u1", "user_score", score, Duration.ofHours(1));

        Optional<String> json = cacheService.getCached("u1", "user_score");
        assertTrue(json.isPresent());

        UserScore restored = cacheService.fromJson(json.get(), UserScore.class);
        assertNotNull(restored);
        assertEquals(9.9, restored.getOverallScore());
    }

    @Test
    void getCached_missingKey_returnsEmpty() {
        putUser("u1");
        stubUserLookup();
        stubCacheFindOnly();

        Optional<String> jsonMissingKey = cacheService.getCached("u1", "missing_key");
        assertTrue(jsonMissingKey.isEmpty());

        Optional<String> jsonUnknownUser = cacheService.getCached("unknown", "user_score");
        assertTrue(jsonUnknownUser.isEmpty());
    }

    @Test
    void fromJson_invalidJson_returnsNull() {
        UserScore restored = cacheService.fromJson("not-json", UserScore.class);
        assertNull(restored);
    }

    @Test
    void saveCache_overwritesExistingValue() {
        putUser("u1");
        stubUserLookup();
        stubCacheDeleteSaveFind();

        cacheService.saveCache("u1", "user_score", UserScore.builder().overallScore(1.1).build(), Duration.ofHours(1));
        cacheService.saveCache("u1", "user_score", UserScore.builder().overallScore(2.2).build(), Duration.ofHours(1));

        Optional<String> json = cacheService.getCached("u1", "user_score");
        assertTrue(json.isPresent());

        UserScore restored = cacheService.fromJson(json.get(), UserScore.class);
        assertNotNull(restored);
        assertEquals(2.2, restored.getOverallScore());

        verify(cacheRepo, times(2)).deleteByUserAndCacheKey(any(GitHubUser.class), eq("user_score"));
        verify(cacheRepo, times(2)).save(any(AnalysisCache.class));
    }

    @Test
    void saveCache_userScopedKeys_doNotCollide() {
        putUser("u1");
        putUser("u2");
        stubUserLookup();
        stubCacheDeleteSaveFind();

        cacheService.saveCache("u1", "user_score", UserScore.builder().overallScore(3.3).build(), Duration.ofHours(1));
        cacheService.saveCache("u2", "user_score", UserScore.builder().overallScore(4.4).build(), Duration.ofHours(1));

        Optional<String> json1 = cacheService.getCached("u1", "user_score");
        Optional<String> json2 = cacheService.getCached("u2", "user_score");

        assertTrue(json1.isPresent());
        assertTrue(json2.isPresent());

        UserScore s1 = cacheService.fromJson(json1.get(), UserScore.class);
        UserScore s2 = cacheService.fromJson(json2.get(), UserScore.class);

        assertNotNull(s1);
        assertNotNull(s2);
        assertEquals(3.3, s1.getOverallScore());
        assertEquals(4.4, s2.getOverallScore());
    }

    private void stubUserLookup() {
        when(userRepo.findByUsername(anyString())).thenAnswer(inv -> {
            String u = inv.getArgument(0, String.class);
            return Optional.ofNullable(users.get(u));
        });
    }

    private void stubCacheFindOnly() {
        when(cacheRepo.findByUserAndCacheKeyAndExpiresAtAfter(any(GitHubUser.class), anyString(), any(LocalDateTime.class)))
                .thenAnswer(inv -> {
                    GitHubUser u = inv.getArgument(0, GitHubUser.class);
                    String key = inv.getArgument(1, String.class);
                    LocalDateTime now = inv.getArgument(2, LocalDateTime.class);

                    AnalysisCache c = store.get(u.getUsername() + "|" + key);
                    if (c == null) return Optional.empty();
                    if (c.getExpiresAt() == null) return Optional.empty();
                    if (!c.getExpiresAt().isAfter(now)) return Optional.empty();
                    return Optional.of(c);
                });
    }

    private void stubCacheDeleteSaveFind() {
        doAnswer(inv -> {
            GitHubUser u = inv.getArgument(0, GitHubUser.class);
            String key = inv.getArgument(1, String.class);
            store.remove(u.getUsername() + "|" + key);
            return null;
        }).when(cacheRepo).deleteByUserAndCacheKey(any(GitHubUser.class), anyString());

        when(cacheRepo.save(any(AnalysisCache.class))).thenAnswer(inv -> {
            AnalysisCache c = inv.getArgument(0, AnalysisCache.class);
            store.put(c.getUser().getUsername() + "|" + c.getCacheKey(), c);
            return c;
        });

        stubCacheFindOnly();
    }

    private void putUser(String username) {
        users.put(username, GitHubUser.builder().username(username).build());
    }
}
