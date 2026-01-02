package com.devbadge.devbadge.controllers;

import com.devbadge.devbadge.dto.github.ScoreHistoryResponse;
import com.devbadge.devbadge.dto.github.UserScoreResponse;
import com.devbadge.devbadge.entity.UserScore;
import com.devbadge.devbadge.exception.UserNotInDatabaseException;
import com.devbadge.devbadge.repository.ScoreHistoryRepository;
import com.devbadge.devbadge.service.ScoringService;
import com.devbadge.devbadge.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoringController {

    private final ScoringService scoringService;
    private final ScoreHistoryRepository scoreHistoryRepository;
    private final UserSyncService userSyncService;

    @GetMapping("/{username}")
    public ResponseEntity<?> calculateAndReturnScore(@PathVariable String username) {
        try {
            UserScore score = scoringService.calculateScores(username);

            return ResponseEntity.ok(
                    UserScoreResponse.builder()
                            .username(score.getUser().getUsername())
                            .commitQuality(score.getCommitQualityScore())
                            .consistency(score.getConsistencyScore())
                            .collaboration(score.getCollaborationScore())
                            .impact(score.getImpactScore())
                            .codeReview(score.getCodeReviewScore())
                            .overallScore(score.getOverallScore())
                            .build()
            );

        } catch (UserNotInDatabaseException ex) {

            // Start async warmup: fetch from GitHub + persist + compute + cache
            userSyncService.warmupUserIfNeeded(username);

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .header(HttpHeaders.RETRY_AFTER, "60")
                    .body(Map.of(
                            "code", "USER_NOT_IN_DB",
                            "message", "User not found in database. Fetching from GitHub now. Please retry in about 1 minute."
                    ));
        }
    }

    @GetMapping("/{username}/history")
    public ResponseEntity<?> getScoreHistory(@PathVariable String username) {
        try {
            Long userId = scoringService.getUserIdByUsername(username);

            List<ScoreHistoryResponse> history = scoreHistoryRepository
                    .findByUser_IdOrderByCalculatedAtDesc(userId)
                    .stream()
                    .map(h -> ScoreHistoryResponse.builder()
                            .commitQuality(h.getCommitQualityScore())
                            .consistency(h.getConsistencyScore())
                            .collaboration(h.getCollaborationScore())
                            .impact(h.getImpactScore())
                            .codeReview(h.getCodeReviewScore())
                            .overall(h.getOverallScore())
                            .calculatedAt(h.getCalculatedAt())
                            .build()
                    )
                    .toList();

            return ResponseEntity.ok(history);

        } catch (UserNotInDatabaseException ex) {

            userSyncService.warmupUserIfNeeded(username);

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .header(HttpHeaders.RETRY_AFTER, "60")
                    .body(Map.of(
                            "code", "USER_NOT_IN_DB",
                            "message", "User not found in database, so history is not available yet. Fetching from GitHub now. Please retry in about 1 minute."
                    ));
        }
    }
}
