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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisCacheServiceUnitTest {

    private AnalysisCacheRepository cacheRepo;
    private GitHubUserRepository userRepo;
    private AnalysisCacheService cacheService;

    private final Map<String, GitHubUser> users = new HashMap<>();
    private final Map<String, List<AnalysisCache>> store = new HashMap<>();

    @BeforeEach
    void setUp() {
        users.clear();
        store.clear();

        cacheRepo = mock(AnalysisCacheRepository.class, this::cacheRepoAnswer);
        userRepo = mock(GitHubUserRepository.class, this::userRepoAnswer);

        cacheService = new AnalysisCacheService(cacheRepo, userRepo, new ObjectMapper());
    }

    @Test
    void saveCache_thenGetCached_returnsPresent_andRoundTripsJson() {
        putUser("u1");

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

        cacheService.saveCache("u1", "user_score", UserScore.builder().overallScore(1.1).build(), Duration.ofHours(1));
        cacheService.saveCache("u1", "user_score", UserScore.builder().overallScore(2.2).build(), Duration.ofHours(1));

        Optional<String> json = cacheService.getCached("u1", "user_score");
        assertTrue(json.isPresent());

        UserScore restored = cacheService.fromJson(json.get(), UserScore.class);
        assertNotNull(restored);
        assertEquals(2.2, restored.getOverallScore());

        // Only verify what must be true across implementations
        verify(cacheRepo, times(2)).save(any(AnalysisCache.class));
    }

    @Test
    void saveCache_userScopedKeys_doNotCollide() {
        putUser("u1");
        putUser("u2");

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

    private Object userRepoAnswer(InvocationOnMock inv) {
        String name = inv.getMethod().getName();

        if (name.equals("findByUsername")) {
            String u = inv.getArgument(0, String.class);
            return Optional.ofNullable(users.get(u));
        }

        return defaultValue(inv);
    }

    private Object cacheRepoAnswer(InvocationOnMock inv) {
        String name = inv.getMethod().getName();

        if (name.equals("save") || name.equals("saveAndFlush")) {
            AnalysisCache c = inv.getArgument(0, AnalysisCache.class);
            store.computeIfAbsent(storeKey(c.getUser(), c.getCacheKey()), k -> new ArrayList<>()).add(c);
            return c;
        }

        if (name.equals("delete") && inv.getArguments().length == 1 && inv.getArgument(0) instanceof AnalysisCache c) {
            List<AnalysisCache> list = store.get(storeKey(c.getUser(), c.getCacheKey()));
            if (list != null) list.removeIf(x -> Objects.equals(x.getExpiresAt(), c.getExpiresAt()) && Objects.equals(x.getCacheData(), c.getCacheData()));
            return null;
        }

        if (name.equals("deleteByUserAndCacheKey") || name.equals("deleteAllByUserAndCacheKey")) {
            GitHubUser u = inv.getArgument(0, GitHubUser.class);
            String key = inv.getArgument(1, String.class);
            store.remove(storeKey(u, key));
            return null;
        }

        if (name.startsWith("find")) {
            // Support multiple possible Spring Data method names/signatures.
            GitHubUser u = findArg(inv, GitHubUser.class);
            String key = findArg(inv, String.class);
            LocalDateTime now = findArg(inv, LocalDateTime.class);

            Class<?> rt = inv.getMethod().getReturnType();

            if (u == null || key == null) {
                if (Optional.class.equals(rt)) return Optional.empty();
                if (List.class.equals(rt)) return List.of();
                return null;
            }

            if (now != null) {
                Optional<AnalysisCache> latest = latestValid(u, key, now);

                if (Optional.class.equals(rt)) return latest;
                if (List.class.equals(rt)) {
                    // Some repos might use Pageable, return top-1 style list
                    if (hasArg(inv, Pageable.class)) return latest.map(List::of).orElseGet(List::of);
                    return latest.map(List::of).orElseGet(List::of);
                }
            } else {
                List<AnalysisCache> all = orderedByExpiresDesc(u, key);
                if (Optional.class.equals(rt)) return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
                if (List.class.equals(rt)) return all;
            }
        }

        return defaultValue(inv);
    }

    private Optional<AnalysisCache> latestValid(GitHubUser u, String key, LocalDateTime now) {
        return store.getOrDefault(storeKey(u, key), List.of()).stream()
                .filter(c -> c.getExpiresAt() != null && c.getExpiresAt().isAfter(now))
                .max(Comparator.comparing(AnalysisCache::getExpiresAt));
    }

    private List<AnalysisCache> orderedByExpiresDesc(GitHubUser u, String key) {
        List<AnalysisCache> list = new ArrayList<>(store.getOrDefault(storeKey(u, key), List.of()));
        list.sort((a, b) -> {
            LocalDateTime ea = a.getExpiresAt();
            LocalDateTime eb = b.getExpiresAt();
            if (ea == null && eb == null) return 0;
            if (ea == null) return 1;
            if (eb == null) return -1;
            return eb.compareTo(ea);
        });
        return list;
    }

    private String storeKey(GitHubUser u, String cacheKey) {
        return u.getUsername() + "|" + cacheKey;
    }

    private void putUser(String username) {
        users.put(username, GitHubUser.builder().username(username).build());
    }

    private static <T> T findArg(InvocationOnMock inv, Class<T> type) {
        for (Object a : inv.getArguments()) {
            if (type.isInstance(a)) return type.cast(a);
        }
        return null;
    }

    private static boolean hasArg(InvocationOnMock inv, Class<?> type) {
        for (Object a : inv.getArguments()) {
            if (type.isInstance(a)) return true;
        }
        return false;
    }

    private static Object defaultValue(InvocationOnMock inv) {
        Class<?> rt = inv.getMethod().getReturnType();
        if (Optional.class.equals(rt)) return Optional.empty();
        if (List.class.equals(rt)) return List.of();
        if (rt.equals(boolean.class)) return false;
        if (rt.equals(int.class)) return 0;
        if (rt.equals(long.class)) return 0L;
        return null;
    }
}
