package com.devbadge.devbadge.service;

import com.devbadge.devbadge.dto.github.GitHubCommitDTO;
import com.devbadge.devbadge.dto.github.GitHubIssueDTO;
import com.devbadge.devbadge.dto.github.GitHubPullRequestDTO;
import com.devbadge.devbadge.dto.github.GitHubRepoDTO;
import com.devbadge.devbadge.dto.github.GitHubUserDTO;
import com.devbadge.devbadge.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GitHubApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GitHubApiService gitHubApiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TestUtils.setField(gitHubApiService, "baseUrl", "https://api.github.com");
        TestUtils.setField(gitHubApiService, "token", "test-token");
    }

    @Test
    void fetchUserProfile_success_returnsUserDto_andSendsAuthHeader() {
        GitHubUserDTO dto = new GitHubUserDTO();
        dto.setLogin("testuser");
        dto.setId(123L);

        when(restTemplate.exchange(
                eq("https://api.github.com/users/testuser"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubUserDTO.class)
        )).thenReturn(ResponseEntity.ok(dto));

        GitHubUserDTO result = gitHubApiService.fetchUserProfile("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getLogin());
        assertEquals(123L, result.getId());

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(1)).exchange(
                eq("https://api.github.com/users/testuser"),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(GitHubUserDTO.class)
        );

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertEquals("token test-token", headers.getFirst("Authorization"));
    }

    @Test
    void fetchUserProfile_notFound_throwsUserNotFoundException() {
        when(restTemplate.exchange(
                eq("https://api.github.com/users/missing"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GitHubUserDTO.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(UserNotFoundException.class, () -> gitHubApiService.fetchUserProfile("missing"));
    }

    @Test
    void fetchUserRepositories_success_returnsList() {
        GitHubRepoDTO r1 = new GitHubRepoDTO();
        GitHubRepoDTO r2 = new GitHubRepoDTO();

        when(restTemplate.exchange(
                eq("https://api.github.com/users/testuser/repos?per_page=100&sort=updated"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(r1, r2)));

        List<GitHubRepoDTO> result = gitHubApiService.fetchUserRepositories("testuser");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(r1, result.get(0));
        assertSame(r2, result.get(1));
    }

    @Test
    void fetchUserRepositories_nullBody_returnsEmptyList() {
        when(restTemplate.exchange(
                eq("https://api.github.com/users/testuser/repos?per_page=100&sort=updated"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(null));

        List<GitHubRepoDTO> result = gitHubApiService.fetchUserRepositories("testuser");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchUserRepositories_httpError_returnsEmptyList() {
        when(restTemplate.exchange(
                eq("https://api.github.com/users/testuser/repos?per_page=100&sort=updated"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        List<GitHubRepoDTO> result = gitHubApiService.fetchUserRepositories("testuser");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchRepositoryCommitsPaginated_twoPages_returnsCombinedList() {
        GitHubCommitDTO c1 = new GitHubCommitDTO();
        GitHubCommitDTO c2 = new GitHubCommitDTO();

        when(restTemplate.exchange(
                contains("page=1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(c1)));

        when(restTemplate.exchange(
                contains("page=2"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(c2)));

        when(restTemplate.exchange(
                contains("page=3"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of()));

        List<GitHubCommitDTO> result =
                gitHubApiService.fetchRepositoryCommitsPaginated("testuser", "repo1");

        assertEquals(2, result.size());
        assertSame(c1, result.get(0));
        assertSame(c2, result.get(1));
    }


    @Test
    void fetchRepositoryCommitsPaginated_firstPageEmpty_returnsEmptyList() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of()));

        List<GitHubCommitDTO> result = gitHubApiService.fetchRepositoryCommitsPaginated("testuser", "repo1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchIssuesPaginated_twoPages_returnsCombinedList() {
        GitHubIssueDTO i1 = new GitHubIssueDTO();
        GitHubIssueDTO i2 = new GitHubIssueDTO();

        when(restTemplate.exchange(
                contains("page=1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(i1)));

        when(restTemplate.exchange(
                contains("page=2"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(i2)));

        when(restTemplate.exchange(
                contains("page=3"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of()));

        List<GitHubIssueDTO> result =
                gitHubApiService.fetchIssuesPaginated("testuser", "repo1");

        assertEquals(2, result.size());
        assertSame(i1, result.get(0));
        assertSame(i2, result.get(1));
    }

    @Test
    void fetchPullRequestsPaginated_twoPages_returnsCombinedList() {
        GitHubPullRequestDTO p1 = new GitHubPullRequestDTO();
        GitHubPullRequestDTO p2 = new GitHubPullRequestDTO();

        when(restTemplate.exchange(
                contains("page=1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(p1)));

        when(restTemplate.exchange(
                contains("page=2"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(p2)));

        when(restTemplate.exchange(
                contains("page=3"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of()));

        List<GitHubPullRequestDTO> result =
                gitHubApiService.fetchPullRequestsPaginated("testuser", "repo1");

        assertEquals(2, result.size());
        assertSame(p1, result.get(0));
        assertSame(p2, result.get(1));
    }
}