package com.devbadge.devbadge.service;

import com.devbadge.devbadge.dto.github.*;
import com.devbadge.devbadge.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubApiService {

    @Qualifier("githubRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${github.api.base-url}")
    private String baseUrl;

    @Value("${github.api.token}")
    private String token;

    private HttpEntity<?> authHeaders() {
        HttpHeaders h = new HttpHeaders();
        // Keep your existing style. (If token is empty, GitHub just treats it as unauth.)
        h.set("Authorization", "token " + token);
        return new HttpEntity<>(h);
    }

    // ---------------------------------------------------------
    // USER PROFILE
    // ---------------------------------------------------------
    public GitHubUserDTO fetchUserProfile(String username) {
        try {
            log.info("Fetching user profile: {}", username);

            String url = baseUrl + "/users/" + username;

            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authHeaders(),
                    GitHubUserDTO.class
            ).getBody();

        } catch (HttpClientErrorException e) {
            log.error("Profile fetch FAILED for {}: {} {}", username, e.getStatusCode(), e.getMessage());
            throw new UserNotFoundException(username);
        }
    }

    // ---------------------------------------------------------
    // USER REPOS
    // ---------------------------------------------------------
    public List<GitHubRepoDTO> fetchUserRepositories(String username) {
        try {
            log.info("Fetching repos for {}", username);

            String url = baseUrl + "/users/" + username + "/repos?per_page=100&sort=updated";

            ResponseEntity<List<GitHubRepoDTO>> response =
                    restTemplate.exchange(url, HttpMethod.GET, authHeaders(),
                            new ParameterizedTypeReference<List<GitHubRepoDTO>>() {});

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (HttpClientErrorException e) {
            log.error("Repo fetch FAILED for {}: {} {}", username, e.getStatusCode(), e.getMessage());
            return Collections.emptyList();
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.error("Repo fetch FAILED (server/network) for {}: {}", username, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ---------------------------------------------------------
    // PAGINATED COMMITS
    // ---------------------------------------------------------
    public List<GitHubCommitDTO> fetchRepositoryCommitsPaginated(String username, String repo) {
        List<GitHubCommitDTO> all = new ArrayList<>();
        int page = 1;

        while (true) {
            String url = baseUrl + "/repos/" + username + "/" + repo +
                    "/commits?author=" + username + "&per_page=100&page=" + page;

            try {
                ResponseEntity<List<GitHubCommitDTO>> response =
                        restTemplate.exchange(url, HttpMethod.GET, authHeaders(),
                                new ParameterizedTypeReference<List<GitHubCommitDTO>>() {});

                List<GitHubCommitDTO> pageData = response.getBody();
                if (pageData == null || pageData.isEmpty()) break;

                all.addAll(pageData);
                page++;

            } catch (HttpClientErrorException e) {
                // Common: 409 CONFLICT for empty repos (no commits yet)
                // Also possible: 404 not found, 403 forbidden, etc.
                log.warn("Commits fetch skipped for {}/{}: {} {}",
                        username, repo, e.getStatusCode(), e.getMessage());
                break; // Treat as "no commits" for this repo
            } catch (HttpServerErrorException | ResourceAccessException e) {
                log.warn("Commits fetch failed for {}/{} (server/network): {}",
                        username, repo, e.getMessage());
                break; // Don't crash whole scoring because one repo endpoint is unstable
            }
        }

        return all;
    }

    // ---------------------------------------------------------
    // PAGINATED ISSUES
    // ---------------------------------------------------------
    public List<GitHubIssueDTO> fetchIssuesPaginated(String username, String repo) {
        List<GitHubIssueDTO> all = new ArrayList<>();
        int page = 1;

        while (true) {
            String url = baseUrl + "/repos/" + username + "/" + repo +
                    "/issues?creator=" + username + "&state=all&per_page=100&page=" + page;

            try {
                ResponseEntity<List<GitHubIssueDTO>> response =
                        restTemplate.exchange(url, HttpMethod.GET, authHeaders(),
                                new ParameterizedTypeReference<List<GitHubIssueDTO>>() {});

                List<GitHubIssueDTO> pageData = response.getBody();
                if (pageData == null || pageData.isEmpty()) break;

                all.addAll(pageData);
                page++;

            } catch (HttpClientErrorException e) {
                log.warn("Issues fetch skipped for {}/{}: {} {}",
                        username, repo, e.getStatusCode(), e.getMessage());
                break;
            } catch (HttpServerErrorException | ResourceAccessException e) {
                log.warn("Issues fetch failed for {}/{} (server/network): {}",
                        username, repo, e.getMessage());
                break;
            }
        }

        return all;
    }

    // ---------------------------------------------------------
    // PAGINATED PULL REQUESTS
    // ---------------------------------------------------------
    public List<GitHubPullRequestDTO> fetchPullRequestsPaginated(String username, String repo) {
        List<GitHubPullRequestDTO> all = new ArrayList<>();
        int page = 1;

        while (true) {
            String url = baseUrl + "/repos/" + username + "/" + repo +
                    "/pulls?state=all&per_page=100&page=" + page;

            try {
                ResponseEntity<List<GitHubPullRequestDTO>> response =
                        restTemplate.exchange(url, HttpMethod.GET, authHeaders(),
                                new ParameterizedTypeReference<List<GitHubPullRequestDTO>>() {});

                List<GitHubPullRequestDTO> pageData = response.getBody();
                if (pageData == null || pageData.isEmpty()) break;

                all.addAll(pageData);
                page++;

            } catch (HttpClientErrorException e) {
                log.warn("PR fetch skipped for {}/{}: {} {}",
                        username, repo, e.getStatusCode(), e.getMessage());
                break;
            } catch (HttpServerErrorException | ResourceAccessException e) {
                log.warn("PR fetch failed for {}/{} (server/network): {}",
                        username, repo, e.getMessage());
                break;
            }
        }

        return all;
    }
}
