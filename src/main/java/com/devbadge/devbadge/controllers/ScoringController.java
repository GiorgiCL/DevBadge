package com.devbadge.devbadge.controllers;

import com.devbadge.devbadge.dto.github.ScoreHistoryResponse;
import com.devbadge.devbadge.dto.github.UserScoreResponse;
import com.devbadge.devbadge.entity.ScoreHistory;
import com.devbadge.devbadge.entity.UserScore;
import com.devbadge.devbadge.repository.ScoreHistoryRepository;
import com.devbadge.devbadge.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoringController {

    private final ScoringService scoringService;
    private final ScoreHistoryRepository scoreHistoryRepository;

    @GetMapping("/{username}")
    public UserScoreResponse calculateAndReturnScore(@PathVariable String username) {
        UserScore score = scoringService.calculateScores(username);

        return UserScoreResponse.builder()
                .username(score.getUser().getUsername())
                .commitQuality(score.getCommitQualityScore())
                .consistency(score.getConsistencyScore())
                .collaboration(score.getCollaborationScore())
                .impact(score.getImpactScore())
                .codeReview(score.getCodeReviewScore())
                .overallScore(score.getOverallScore())
                .build();
    }

    @GetMapping("/{username}/history")
    public List<ScoreHistoryResponse> getScoreHistory(@PathVariable String username) {
        Long userId = scoringService.getUserIdByUsername(username);

        return scoreHistoryRepository.findByUser_IdOrderByCalculatedAtDesc(userId)
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
    }


}
