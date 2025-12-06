package com.devbadge.devbadge.service;

import com.devbadge.devbadge.dto.github.*;
import com.devbadge.devbadge.entity.GitHubUser;
import com.devbadge.devbadge.entity.ScoreHistory;
import com.devbadge.devbadge.entity.UserScore;
import com.devbadge.devbadge.repository.GitHubUserRepository;
import com.devbadge.devbadge.repository.ScoreHistoryRepository;
import com.devbadge.devbadge.repository.UserScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScoringService {

    private final AnalysisCacheService cacheService;
    private final GitHubApiService gitHubApiService;
    private final GitHubUserRepository gitHubUserRepository;
    private final UserScoreRepository userScoreRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;

    public UserScore calculateScores(String username) {
        log.info("Calculating scores for {}", username);

        Optional<String> cachedJson = cacheService.getCached(username, "user_score");

        if (cachedJson.isPresent()) {
            log.info("Returning cached score for {}", username);
            UserScore cached = cacheService.fromJson(cachedJson.get(), UserScore.class);
            if (cached != null) return cached;
        }
        GitHubUserDTO userDTO = gitHubApiService.fetchUserProfile(username);

        GitHubUser user = gitHubUserRepository
                .findByUsername(username)
                .orElseGet(() -> saveGitHubUser(userDTO));

        List<GitHubRepoDTO> repos = gitHubApiService.fetchUserRepositories(username);
        List<GitHubCommitDTO> commits = fetchAllCommits(username, repos);
        List<GitHubIssueDTO> issues = fetchAllIssues(username, repos);
        List<GitHubPullRequestDTO> pullRequests = fetchAllPullRequests(username, repos);

        double commitQuality = scoreCommitQuality(commits);
        double consistency = scoreConsistency(commits);
        double collaboration = scoreCollaboration(pullRequests, issues);
        double impact = scoreImpact(commits, pullRequests);
        double codeReview = scoreCodeReview(pullRequests);

        double overall = calculateOverall(
                commitQuality,
                consistency,
                collaboration,
                impact,
                codeReview
        );

        UserScore result = saveScore(
                user,
                commitQuality,
                consistency,
                collaboration,
                impact,
                codeReview,
                overall
        );
        cacheService.saveCache(username, "user_score", result, Duration.ofHours(1));

        return result;
    }



    private GitHubUser saveGitHubUser(GitHubUserDTO dto) {
        log.info("Saving new GitHub user in DB: {}", dto.getLogin());

        GitHubUser user = GitHubUser.builder()
                .username(dto.getLogin())
                .githubUserId(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .location(dto.getLocation())
                .bio(dto.getBio())
                .publicRepos(dto.getPublicRepos())
                .publicGists(dto.getPublicGists())
                .followers(dto.getFollowers())
                .following(dto.getFollowing())
                .accountCreatedAt(dto.getCreatedAt())
                .lastUpdatedAt(dto.getUpdatedAt())
                .build();

        return gitHubUserRepository.save(user);
    }
    private void saveHistory(GitHubUser user, UserScore latestScore) {

        ScoreHistory history = ScoreHistory.builder()
                .user(user)
                .commitQualityScore(latestScore.getCommitQualityScore())
                .consistencyScore(latestScore.getConsistencyScore())
                .collaborationScore(latestScore.getCollaborationScore())
                .impactScore(latestScore.getImpactScore())
                .codeReviewScore(latestScore.getCodeReviewScore())
                .overallScore(latestScore.getOverallScore())
                .build();

        scoreHistoryRepository.save(history);
    }
    private List<GitHubCommitDTO> fetchAllCommits(String username, List<GitHubRepoDTO> repos) {
        log.info("Fetching all commits across {} repositories for {}", repos.size(), username);

    List<GitHubCommitDTO> allCommits = new ArrayList<>();

    for(GitHubRepoDTO repo : repos) {
        String repoName = repo.getName();

        List<GitHubCommitDTO> commits = gitHubApiService.fetchRepositoryCommits(username,repoName,100);

        if(commits != null) {
            allCommits.addAll(commits);
        }
    }
    log.info("Total commits collected for {} : {}", username, allCommits.size());
    return allCommits;
    }
    private List<GitHubIssueDTO> fetchAllIssues(String username, List<GitHubRepoDTO> repos) {
        log.info("Fetching ALL issues across {} repositories for {}", repos.size(), username);

        List<GitHubIssueDTO> allIssues = new java.util.ArrayList<>();

        for (GitHubRepoDTO repo : repos) {
            String repoName = repo.getName();

            List<GitHubIssueDTO> issues = gitHubApiService.fetchIssues(username, repoName);

            if (issues != null) {
                allIssues.addAll(issues);
            }
        }

        log.info("Total issues collected for {}: {}", username, allIssues.size());
        return allIssues;
    }
    private List<GitHubPullRequestDTO> fetchAllPullRequests(String username, List<GitHubRepoDTO> repos) {
        log.info("Fetching ALL pull requests across {} repositories for {}", repos.size(), username);

        List<GitHubPullRequestDTO> allPulls = new java.util.ArrayList<>();

        for (GitHubRepoDTO repo : repos) {
            String repoName = repo.getName();
            List<GitHubPullRequestDTO> prs = gitHubApiService.fetchPullRequests(username, repoName);

            if (prs != null) {
                allPulls.addAll(prs);
            }
        }

        log.info("Total pull requests collected for {}: {}", username, allPulls.size());
        return allPulls;
    }
    private double scoreCommitQuality(List<GitHubCommitDTO> commits) {
        if(commits == null || commits.isEmpty()) {
            return 0.0;
        }
        double totalScore=0.0;

        List<String> bannedWords = List.of("fix","update","temp","wip","test","misc", "change");
        List<String> goodVerbs = List.of("add", "remove", "refactor", "improve", "optimize", "rename");

        for(GitHubCommitDTO commit : commits) {
            if(commit.getCommit() == null || commit.getCommit().getMessage() == null) {
                    continue;
            }
            String message = commit.getCommit().getMessage().toLowerCase();
            double score = 0.0;

            if(message.length() < 10){
                score -= 1;
            }else{
                score += 1;
            }

            if(goodVerbs.stream().anyMatch(message::contains)){
                score +=1;

            }
            if(bannedWords.stream().anyMatch(message::contains)){
                score -=1;
            }
            if(message.startsWith("merge")){
                score -=2;
            }
            score = Math.max(0,Math.min(score, 10));
            totalScore += score;

        }
        return totalScore / commits.size();
    }
    private double scoreConsistency(List<GitHubCommitDTO> commits) {
        if (commits == null || commits.isEmpty()) {
            return 0.0;
        }

        List<java.time.LocalDate> dates = commits.stream()
                .map(c -> c.getCommit().getAuthor().getDate().toLocalDate())
                .toList();

        long activeDays = dates.stream().distinct().count();

        java.time.LocalDate min = dates.stream().min(java.time.LocalDate::compareTo).get();
        java.time.LocalDate max = dates.stream().max(java.time.LocalDate::compareTo).get();

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(min, max) + 1;

        if (totalDays <= 0) {
            return 0.0;
        }

        double ratio = (double) activeDays / totalDays;

        double score = ratio * 10;

        return Math.max(0, Math.min(score, 10));
    }
    private double scoreCollaboration(List<GitHubPullRequestDTO> prs, List<GitHubIssueDTO> issues) {

        if ((prs == null || prs.isEmpty()) && (issues == null || issues.isEmpty())) {
            return 0.0;
        }

        int prCount = (prs == null) ? 0 : prs.size();
        int issueCount = (issues == null) ? 0 : issues.size();

        double score = 0.0;

        if (prCount > 0) {
            score += Math.min(5, prCount * 0.5);
        }

        if (issueCount > 0) {
            score += Math.min(5, issueCount * 0.3);
        }

        return Math.max(0, Math.min(score, 10));
    }
    private double scoreImpact(List<GitHubCommitDTO> commits, List<GitHubPullRequestDTO> prs) {
        if ((commits == null || commits.isEmpty()) && (prs == null || prs.isEmpty())) {
            return 0.0;
        }

        double score = 0.0;

        int highImpactCommits = 0;
        for (GitHubCommitDTO commit : commits) {
            if (commit.getStats() == null) continue;
            int totalChanges = commit.getStats().getTotal();
            if (totalChanges > 50) {
                highImpactCommits++;
            }
        }
        score += Math.min(5, highImpactCommits * 0.5);

        int mergedPRs = 0;
        for (GitHubPullRequestDTO pr : prs) {
            if ("closed".equalsIgnoreCase(pr.getState()) && pr.isMerged()) {
                mergedPRs++;
            }
        }
        score += Math.min(5, mergedPRs * 1.0);

        return Math.max(0, Math.min(score, 10));
    }
    public Long getUserIdByUsername(String username) {
        return gitHubUserRepository.findByUsername(username)
                .map(GitHubUser::getId)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private double scoreCodeReview(List<GitHubPullRequestDTO> prs) {
        if (prs == null || prs.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        int prCount = prs.size();
        int prsWithReviews = 0;
        int totalReviewComments = 0;

        for (GitHubPullRequestDTO pr : prs) {
            int reviewComments = pr.getReviewComments() != null ? pr.getReviewComments() : 0;
            if (reviewComments > 0) {
                prsWithReviews++;
                totalReviewComments += reviewComments;
            }
        }

        double participationRatio = (double) prsWithReviews / prCount;
        score += participationRatio * 5;

        double avgReviewComments = (double) totalReviewComments / prCount;
        if (avgReviewComments >= 5) score += 5;
        else if (avgReviewComments >= 3) score += 3;
        else if (avgReviewComments >= 1) score += 1;

        return Math.min(score, 10);
    }
    private double calculateOverall(
            double commitQuality,
            double consistency,
            double collaboration,
            double impact,
            double codeReview
    ) {
        double sum = commitQuality + consistency + collaboration + impact + codeReview;
        double overall = sum / 5.0;

        return Math.round(overall * 100.0) / 100.0;
    }
    private UserScore saveScore(
            GitHubUser user,
            double commitQuality,
            double consistency,
            double collaboration,
            double impact,
            double codeReview,
            double overall
    ) {
        UserScore userScore = userScoreRepository
                .findByUser(user)
                .orElse(UserScore.builder().user(user).build());

        userScore.setCommitQualityScore(commitQuality);
        userScore.setConsistencyScore(consistency);
        userScore.setCollaborationScore(collaboration);
        userScore.setImpactScore(impact);
        userScore.setCodeReviewScore(codeReview);
        userScore.setOverallScore(overall);

        userScoreRepository.save(userScore);

        scoreHistoryRepository.save(
                ScoreHistory.builder()
                        .user(user)
                        .commitQualityScore(commitQuality)
                        .consistencyScore(consistency)
                        .collaborationScore(collaboration)
                        .impactScore(impact)
                        .codeReviewScore(codeReview)
                        .overallScore(overall)
                        .build()
        );

        return userScore;
    }
}
