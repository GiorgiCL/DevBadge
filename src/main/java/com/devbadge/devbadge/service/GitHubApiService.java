package com.devbadge.devbadge.service;

import com.devbadge.devbadge.dto.github.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubApiService {

    @Qualifier("githubRestTemplate")
    private final RestTemplate restTemplate;

    public GitHubUserDTO fetchUserProfile(String username) {
        try{
            log.info("Fetching user profile from GitHub, username: {}", username);

            return restTemplate.getForObject(
                    "/users/" + username,
                    GitHubUserDTO.class
            );
        }catch(HttpClientErrorException e){
            log.error("Failed to fetch user profile from GitHub, username{}: {}", username, e.getMessage());
            throw new RuntimeException("GitHub user not found: " + username);
        }
    }

    public List<GitHubRepoDTO> fetchUserRepositories(String username) {
        try{
            log.info("Fetching user repositories from GitHub, username: {}", username);

            ResponseEntity<List<GitHubRepoDTO>> response = restTemplate.exchange(
                    "/users/" + username + "/repos?per_page=100&sort=updated",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<GitHubRepoDTO>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        }catch(HttpClientErrorException e){
            log.error("Failed to fetch user repositories from GitHub, username{}: {}", username, e.getMessage());
            return Collections.emptyList();
        }
    }
     public List<GitHubIssueDTO> fetchIssues(String username,String repo) {
         try {
             log.info("Fetching issues for {}/{}", username, repo);

             ResponseEntity<List<GitHubIssueDTO>> response = restTemplate.exchange(
                     "/repos/" + username + "/" + repo + "/issues?creator=" + username + "&state=all&per_page=100",
                     HttpMethod.GET,
                     null,
                     new ParameterizedTypeReference<List<GitHubIssueDTO>>() {
                     }
             );
             return response.getBody() != null ? response.getBody() : Collections.emptyList();
         } catch (HttpClientErrorException e) {
             log.error("Failed to fetch issues for {}/{}", username, repo);
             return Collections.emptyList();
         }
     }
    public List<GitHubCommitDTO> fetchRepositoryCommits(String username, String repo, int limit) {
        try {
            log.info("Fetching commits for repo {} / {}", username, repo);

            ResponseEntity<List<GitHubCommitDTO>> response = restTemplate.exchange(
                    "/repos/" + username + "/" + repo + "/commits?per_page=" + limit + "&author=" + username,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<GitHubCommitDTO>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch commits for {}/{}: {}", username, repo, e.getMessage());
            return Collections.emptyList();
        }
    }
    public List<GitHubPullRequestDTO> fetchPullRequests(String username, String repo) {
        try {
            log.info("Fetching pull requests for {}/{}", username, repo);

            ResponseEntity<List<GitHubPullRequestDTO>> response = restTemplate.exchange(
                    "/repos/" + username + "/" + repo + "/pulls?state=all&per_page=100",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<GitHubPullRequestDTO>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch PRs for {}/{}: {}", username, repo, e.getMessage());
            return Collections.emptyList();
        }
    }
}
