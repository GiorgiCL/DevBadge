package com.devbadge.devbadge.service;

import com.devbadge.devbadge.dto.github.GitHubCommitDTO;
import com.devbadge.devbadge.dto.github.GitHubIssueDTO;
import com.devbadge.devbadge.dto.github.GitHubPullRequestDTO;
import com.devbadge.devbadge.dto.github.GitHubRepoDTO;
import com.devbadge.devbadge.dto.github.GitHubUserDTO;
import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.entity.ScoreHistory;
import com.devbadge.devbadge.entity.UserScore;
import com.devbadge.devbadge.exception.UserNotFoundException;
import com.devbadge.devbadge.repository.GitHubUserRepository;
import com.devbadge.devbadge.repository.ScoreHistoryRepository;
import com.devbadge.devbadge.repository.UserScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock
    private GitHubApiService gitHubApiService;

    @Mock
    private GitHubUserRepository userRepo;

    @Mock
    private ScoreHistoryRepository historyRepo;

    @Mock
    private UserScoreRepository userScoreRepository;

    @Mock
    private AnalysisCacheService cacheService;

    @InjectMocks
    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        forceInjectByType(scoringService, GitHubApiService.class, gitHubApiService);
        forceInjectByType(scoringService, GitHubUserRepository.class, userRepo);
        forceInjectByType(scoringService, ScoreHistoryRepository.class, historyRepo);
        forceInjectByType(scoringService, UserScoreRepository.class, userScoreRepository);
        forceInjectByType(scoringService, AnalysisCacheService.class, cacheService);

        lenient().when(userScoreRepository.findByUser(any())).thenReturn(Optional.empty());
        lenient().when(userScoreRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(historyRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(userRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(cacheService.fromJson(anyString(), eq(UserScore.class))).thenReturn(null);
    }

    @Test
    void cacheHit_returnsCachedScore() {
        UserScore cached = UserScore.builder().overallScore(7.7).build();

        when(cacheService.getCached("testuser", "user_score")).thenReturn(Optional.of("json"));
        when(cacheService.fromJson("json", UserScore.class)).thenReturn(cached);

        UserScore result = scoringService.calculateScores("testuser");

        assertSame(cached, result);
        verifyNoInteractions(gitHubApiService, userRepo, historyRepo, userScoreRepository);
        verify(cacheService, never()).saveCache(anyString(), anyString(), any(), any(Duration.class));
    }

    @Test
    void cacheMiss_callsGitHubProfile() {
        when(cacheService.getCached("testuser", "user_score")).thenReturn(Optional.empty());
        when(gitHubApiService.fetchUserProfile("testuser")).thenReturn(userDto("testuser"));
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(GitHubUser.builder().username("testuser").build()));
        when(gitHubApiService.fetchUserRepositories("testuser")).thenReturn(List.of());

        UserScore result = scoringService.calculateScores("testuser");

        assertNotNull(result);
        verify(gitHubApiService).fetchUserProfile("testuser");
    }

    @Test
    void missingUserInDb_createsUser() {
        when(cacheService.getCached("testuser", "user_score")).thenReturn(Optional.empty());
        when(gitHubApiService.fetchUserProfile("testuser")).thenReturn(userDto("testuser"));
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());
        when(gitHubApiService.fetchUserRepositories("testuser")).thenReturn(List.of());

        scoringService.calculateScores("testuser");

        verify(userRepo).save(any(GitHubUser.class));
    }

    @Test
    void githubUserNotFound_throwsException() {
        when(cacheService.getCached("missing", "user_score")).thenReturn(Optional.empty());
        when(gitHubApiService.fetchUserProfile("missing")).thenThrow(new UserNotFoundException("missing"));

        assertThrows(UserNotFoundException.class, () -> scoringService.calculateScores("missing"));
    }

    @Test
    void emptyRepos_skipsRepoCalls() {
        when(cacheService.getCached("testuser", "user_score")).thenReturn(Optional.empty());
        when(gitHubApiService.fetchUserProfile("testuser")).thenReturn(userDto("testuser"));
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(GitHubUser.builder().username("testuser").build()));
        when(gitHubApiService.fetchUserRepositories("testuser")).thenReturn(List.of());

        scoringService.calculateScores("testuser");

        verify(gitHubApiService, never()).fetchRepositoryCommitsPaginated(anyString(), anyString());
        verify(gitHubApiService, never()).fetchIssuesPaginated(anyString(), anyString());
        verify(gitHubApiService, never()).fetchPullRequestsPaginated(anyString(), anyString());
    }

    @Test
    void oneRepo_triggersRepoFetchers() {
        when(cacheService.getCached("testuser", "user_score")).thenReturn(Optional.empty());
        when(gitHubApiService.fetchUserProfile("testuser")).thenReturn(userDto("testuser"));
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(GitHubUser.builder().username("testuser").build()));
        when(gitHubApiService.fetchUserRepositories("testuser")).thenReturn(List.of(repo("repo1")));

        when(gitHubApiService.fetchRepositoryCommitsPaginated("testuser", "repo1")).thenReturn(List.<GitHubCommitDTO>of());
        when(gitHubApiService.fetchIssuesPaginated("testuser", "repo1")).thenReturn(List.<GitHubIssueDTO>of());
        when(gitHubApiService.fetchPullRequestsPaginated("testuser", "repo1")).thenReturn(List.<GitHubPullRequestDTO>of());

        scoringService.calculateScores("testuser");

        verify(gitHubApiService).fetchRepositoryCommitsPaginated("testuser", "repo1");
        verify(gitHubApiService).fetchIssuesPaginated("testuser", "repo1");
        verify(gitHubApiService).fetchPullRequestsPaginated("testuser", "repo1");
    }

    @Test
    void multipleRepos_triggersMultipleCalls() {
        when(cacheService.getCached("testuser", "user_score")).thenReturn(Optional.empty());
        when(gitHubApiService.fetchUserProfile("testuser")).thenReturn(userDto("testuser"));
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(GitHubUser.builder().username("testuser").build()));
        when(gitHubApiService.fetchUserRepositories("testuser")).thenReturn(List.of(repo("a"), repo("b")));

        when(gitHubApiService.fetchRepositoryCommitsPaginated("testuser", "a")).thenReturn(List.<GitHubCommitDTO>of());
        when(gitHubApiService.fetchIssuesPaginated("testuser", "a")).thenReturn(List.<GitHubIssueDTO>of());
        when(gitHubApiService.fetchPullRequestsPaginated("testuser", "a")).thenReturn(List.<GitHubPullRequestDTO>of());

        when(gitHubApiService.fetchRepositoryCommitsPaginated("testuser", "b")).thenReturn(List.<GitHubCommitDTO>of());
        when(gitHubApiService.fetchIssuesPaginated("testuser", "b")).thenReturn(List.<GitHubIssueDTO>of());
        when(gitHubApiService.fetchPullRequestsPaginated("testuser", "b")).thenReturn(List.<GitHubPullRequestDTO>of());

        scoringService.calculateScores("testuser");

        verify(gitHubApiService).fetchRepositoryCommitsPaginated("testuser", "a");
        verify(gitHubApiService).fetchRepositoryCommitsPaginated("testuser", "b");
        verify(gitHubApiService).fetchIssuesPaginated("testuser", "a");
        verify(gitHubApiService).fetchIssuesPaginated("testuser", "b");
        verify(gitHubApiService).fetchPullRequestsPaginated("testuser", "a");
        verify(gitHubApiService).fetchPullRequestsPaginated("testuser", "b");
    }

    @Test
    void scoreIsSaved() {
        when(cacheService.getCached("testuser", "user_score")).thenReturn(Optional.empty());
        when(gitHubApiService.fetchUserProfile("testuser")).thenReturn(userDto("testuser"));
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(GitHubUser.builder().username("testuser").build()));
        when(gitHubApiService.fetchUserRepositories("testuser")).thenReturn(List.of());

        scoringService.calculateScores("testuser");

        verify(userScoreRepository).save(any(UserScore.class));
        verify(historyRepo).save(any(ScoreHistory.class));
    }

    @Test
    void cacheIsWritten() {
        when(cacheService.getCached("testuser", "user_score")).thenReturn(Optional.empty());
        when(gitHubApiService.fetchUserProfile("testuser")).thenReturn(userDto("testuser"));
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(GitHubUser.builder().username("testuser").build()));
        when(gitHubApiService.fetchUserRepositories("testuser")).thenReturn(List.of());

        scoringService.calculateScores("testuser");

        verify(cacheService).saveCache(eq("testuser"), eq("user_score"), any(UserScore.class), any(Duration.class));
    }

    private static GitHubUserDTO userDto(String name) {
        GitHubUserDTO dto = new GitHubUserDTO();
        dto.setLogin(name);
        dto.setId(1L);
        return dto;
    }

    private static GitHubRepoDTO repo(String name) {
        GitHubRepoDTO dto = new GitHubRepoDTO();
        try {
            dto.getClass().getMethod("setName", String.class).invoke(dto, name);
            return dto;
        } catch (Exception ignored) {
        }
        try {
            Field f = dto.getClass().getDeclaredField("name");
            f.setAccessible(true);
            f.set(dto, name);
        } catch (Exception ignored) {
        }
        return dto;
    }

    private static void forceInjectByType(Object target, Class<?> depType, Object depInstance) {
        if (target == null || depInstance == null) return;
        Class<?> c = target.getClass();
        while (c != null) {
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                if (f.getType().equals(depType)) {
                    try {
                        f.setAccessible(true);
                        Object current = f.get(target);
                        if (current == null) f.set(target, depInstance);
                    } catch (Exception ignored) {
                    }
                }
            }
            c = c.getSuperclass();
        }
    }
}
