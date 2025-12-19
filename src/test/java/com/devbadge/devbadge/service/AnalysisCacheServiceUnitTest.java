package com.devbadge.devbadge.service;

import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.entity.UserScore;
import com.devbadge.devbadge.repository.GitHubUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AnalysisCacheServiceUnitTest {

    private AnalysisCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = newService();
    }

    @Test
    void saveCache_thenGetCached_returnsPresent_andRoundTripsJson() {
        UserScore score = UserScore.builder().overallScore(9.9).build();

        cacheService.saveCache("u1", "user_score", score, Duration.ofHours(1));

        Optional<String> json = cacheService.getCached("u1", "user_score");
        assertTrue(json.isPresent(), "Expected cached JSON to be present but it was empty");

        UserScore restored = cacheService.fromJson(json.get(), UserScore.class);
        assertNotNull(restored);
        assertEquals(9.9, restored.getOverallScore());
    }

    @Test
    void getCached_missingKey_returnsEmpty() {
        Optional<String> json = cacheService.getCached("u1", "missing_key");
        assertTrue(json.isEmpty());
    }

    @Test
    void fromJson_invalidJson_returnsNull_orThrows() {
        try {
            UserScore restored = cacheService.fromJson("not-json", UserScore.class);
            assertNull(restored);
        } catch (Exception ignored) {
            assertTrue(true);
        }
    }

    @Test
    void saveCache_overwritesExistingValue() {
        cacheService.saveCache("u1", "user_score", UserScore.builder().overallScore(1.1).build(), Duration.ofHours(1));
        cacheService.saveCache("u1", "user_score", UserScore.builder().overallScore(2.2).build(), Duration.ofHours(1));

        Optional<String> json = cacheService.getCached("u1", "user_score");
        assertTrue(json.isPresent(), "Expected cached JSON to be present but it was empty");

        UserScore restored = cacheService.fromJson(json.get(), UserScore.class);
        assertNotNull(restored);
        assertEquals(2.2, restored.getOverallScore());
    }

    @Test
    void saveCache_userScopedKeys_doNotCollide() {
        cacheService.saveCache("u1", "user_score", UserScore.builder().overallScore(3.3).build(), Duration.ofHours(1));
        cacheService.saveCache("u2", "user_score", UserScore.builder().overallScore(4.4).build(), Duration.ofHours(1));

        Optional<String> json1 = cacheService.getCached("u1", "user_score");
        Optional<String> json2 = cacheService.getCached("u2", "user_score");

        assertTrue(json1.isPresent(), "Expected u1 cache to be present but it was empty");
        assertTrue(json2.isPresent(), "Expected u2 cache to be present but it was empty");

        UserScore s1 = cacheService.fromJson(json1.get(), UserScore.class);
        UserScore s2 = cacheService.fromJson(json2.get(), UserScore.class);

        assertNotNull(s1);
        assertNotNull(s2);
        assertEquals(3.3, s1.getOverallScore());
        assertEquals(4.4, s2.getOverallScore());
    }

    private static AnalysisCacheService newService() {
        ObjectMapper mapper = new ObjectMapper();

        GitHubUserRepository userRepo = Mockito.mock(GitHubUserRepository.class);
        when(userRepo.findByUsername(anyString())).thenAnswer(inv -> {
            String u = inv.getArgument(0, String.class);
            return Optional.of(GitHubUser.builder().username(u).build());
        });
        when(userRepo.existsByUsername(anyString())).thenReturn(true);
        when(userRepo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        Class<?> cacheRepoType = findExtraRepositoryType();
        Object cacheRepo = null;

        if (cacheRepoType != null) {
            cacheRepo = Mockito.mock(cacheRepoType, new InMemoryRepoAnswer());
        }

        AnalysisCacheService instance = tryConstruct(mapper, userRepo, cacheRepoType, cacheRepo);
        if (instance == null) {
            throw new IllegalStateException("Failed to instantiate AnalysisCacheService for unit testing");
        }

        wireAllByType(instance, ObjectMapper.class, mapper);
        wireAllByType(instance, GitHubUserRepository.class, userRepo);

        if (cacheRepoType != null && cacheRepo != null) {
            wireAllByType(instance, cacheRepoType, cacheRepo);
        }

        return instance;
    }

    private static Class<?> findExtraRepositoryType() {
        Set<Class<?>> candidates = new LinkedHashSet<>();

        for (Class<?> c = AnalysisCacheService.class; c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                Class<?> t = f.getType();
                if (t == null) continue;

                String name = t.getSimpleName();
                if (!name.endsWith("Repository")) continue;
                if (GitHubUserRepository.class.isAssignableFrom(t)) continue;

                candidates.add(t);
            }
        }

        return candidates.stream().findFirst().orElse(null);
    }

    private static AnalysisCacheService tryConstruct(
            ObjectMapper mapper,
            GitHubUserRepository userRepo,
            Class<?> cacheRepoType,
            Object cacheRepo
    ) {
        Constructor<?>[] ctors = AnalysisCacheService.class.getDeclaredConstructors();
        Constructor<?> best = null;
        int bestKnown = -1;
        int bestParams = Integer.MAX_VALUE;

        for (Constructor<?> c : ctors) {
            int known = 0;
            for (Class<?> t : c.getParameterTypes()) {
                if (ObjectMapper.class.isAssignableFrom(t)) known++;
                else if (GitHubUserRepository.class.isAssignableFrom(t)) known++;
                else if (cacheRepoType != null && cacheRepoType.isAssignableFrom(t)) known++;
            }

            int params = c.getParameterCount();
            if (known > bestKnown || (known == bestKnown && params < bestParams)) {
                bestKnown = known;
                bestParams = params;
                best = c;
            }
        }

        if (best == null) return null;

        try {
            best.setAccessible(true);
            Class<?>[] types = best.getParameterTypes();
            Object[] args = new Object[types.length];

            for (int i = 0; i < types.length; i++) {
                Class<?> t = types[i];

                if (ObjectMapper.class.isAssignableFrom(t)) args[i] = mapper;
                else if (GitHubUserRepository.class.isAssignableFrom(t)) args[i] = userRepo;
                else if (cacheRepoType != null && cacheRepoType.isAssignableFrom(t)) args[i] = cacheRepo;
                else if (Duration.class.isAssignableFrom(t)) args[i] = Duration.ZERO;
                else if (String.class.isAssignableFrom(t)) args[i] = "";
                else if (t.isPrimitive()) args[i] = defaultPrimitive(t);
                else args[i] = Mockito.mock(t);
            }

            return (AnalysisCacheService) best.newInstance(args);
        } catch (Exception e) {
            return null;
        }
    }

    private static void wireAllByType(Object target, Class<?> wantedType, Object value) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (wantedType.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        f.set(target, value);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private static Object defaultPrimitive(Class<?> t) {
        if (t == boolean.class) return false;
        if (t == byte.class) return (byte) 0;
        if (t == short.class) return (short) 0;
        if (t == int.class) return 0;
        if (t == long.class) return 0L;
        if (t == float.class) return 0f;
        if (t == double.class) return 0d;
        if (t == char.class) return '\0';
        return 0;
    }


    private static final class InMemoryRepoAnswer implements Answer<Object> {

        private final Map<String, Object> byUserAndKey = new ConcurrentHashMap<>();

        @Override
        public Object answer(InvocationOnMock inv) {
            String method = inv.getMethod().getName();
            Object[] args = inv.getArguments();
            Class<?> returnType = inv.getMethod().getReturnType();

            if ("save".equals(method) && args != null && args.length == 1) {
                Object entity = args[0];
                String composite = compositeFromEntity(entity);
                if (composite != null) {
                    byUserAndKey.put(composite, entity);
                }
                return entity;
            }

            if (method.startsWith("delete") && args != null && args.length == 1) {
                Object entity = args[0];
                String composite = compositeFromEntity(entity);
                if (composite != null) {
                    byUserAndKey.remove(composite);
                }
                return null;
            }

            if (method.startsWith("find") || method.startsWith("get")) {
                Object found = findByArgs(args);

                if (Optional.class.isAssignableFrom(returnType)) {
                    return Optional.ofNullable(found);
                }
                if (List.class.isAssignableFrom(returnType)) {
                    return found == null ? List.of() : List.of(found);
                }
                if (Collection.class.isAssignableFrom(returnType)) {
                    return found == null ? List.of() : List.of(found);
                }
                return found;
            }

            if (returnType == boolean.class || returnType == Boolean.class) {
                Object found = findByArgs(args);
                return found != null;
            }

            return defaultValue(returnType);
        }

        private Object findByArgs(Object[] args) {
            String userPart = null;
            String keyPart = null;

            if (args != null) {
                for (Object a : args) {
                    if (a instanceof GitHubUser gu) {
                        userPart = safeString(readField(gu, "username"));
                        if (userPart == null) userPart = safeString(readField(gu, "login"));
                        if (userPart == null) userPart = safeString(readField(gu, "id"));
                    }
                }

                List<String> strings = new ArrayList<>();
                for (Object a : args) {
                    if (a instanceof String s && !s.isBlank()) strings.add(s);
                }

                if (userPart != null) {
                    if (!strings.isEmpty()) keyPart = strings.get(0);
                } else {

                    if (strings.size() >= 2) {
                        userPart = strings.get(0);
                        keyPart = strings.get(1);
                    } else if (strings.size() == 1) {
                        keyPart = strings.get(0);
                    }
                }
            }

            if (userPart != null && keyPart != null) {
                return byUserAndKey.get(userPart + "|" + keyPart);
            }

            if (keyPart != null) {
                for (Object e : byUserAndKey.values()) {
                    String entityKey = extractCacheKeyFromEntity(e);
                    if (keyPart.equals(entityKey)) return e;
                }
            }

            return null;
        }

        private static String compositeFromEntity(Object entity) {
            if (entity == null) return null;

            String userPart = extractUserFromEntity(entity);
            String keyPart = extractCacheKeyFromEntity(entity);

            if (userPart == null || keyPart == null) return null;
            return userPart + "|" + keyPart;
        }

        private static String extractUserFromEntity(Object entity) {
            Object userObj = firstFieldAssignable(entity, GitHubUser.class);
            if (userObj instanceof GitHubUser gu) {
                String u = safeString(readField(gu, "username"));
                if (u == null) u = safeString(readField(gu, "login"));
                if (u != null) return u;
            }

            String u = safeString(readAnyNamed(entity, "username", "userName", "login", "user"));
            if (u != null) return u;

            Object id = readAnyNamed(entity, "userId", "user_id", "githubUserId", "gitHubUserId");
            if (id != null) return String.valueOf(id);

            return null;
        }

        private static String extractCacheKeyFromEntity(Object entity) {
            Object k = readAnyNamed(entity, "cacheKey", "cache_key", "key", "analysisKey");
            return safeString(k);
        }

        private static Object defaultValue(Class<?> returnType) {
            if (returnType == void.class) return null;
            if (!returnType.isPrimitive()) return null;
            if (returnType == boolean.class) return false;
            if (returnType == byte.class) return (byte) 0;
            if (returnType == short.class) return (short) 0;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            if (returnType == float.class) return 0f;
            if (returnType == double.class) return 0d;
            if (returnType == char.class) return '\0';
            return null;
        }

        private static Object firstFieldAssignable(Object target, Class<?> type) {
            for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (type.isAssignableFrom(f.getType())) {
                        try {
                            f.setAccessible(true);
                            return f.get(target);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            return null;
        }

        private static Object readAnyNamed(Object target, String... names) {
            for (String n : names) {
                Object v = readField(target, n);
                if (v != null) return v;
            }
            return null;
        }

        private static Object readField(Object target, String name) {
            if (target == null || name == null) return null;
            for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
                try {
                    Field f = c.getDeclaredField(name);
                    f.setAccessible(true);
                    return f.get(target);
                } catch (Exception ignored) {
                }
            }
            return null;
        }

        private static String safeString(Object o) {
            if (o == null) return null;
            String s = String.valueOf(o);
            return s.isBlank() ? null : s;
        }
    }
}
