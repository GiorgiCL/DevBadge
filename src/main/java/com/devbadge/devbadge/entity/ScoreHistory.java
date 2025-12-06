package com.devbadge.devbadge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "score_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private GitHubUser user;

    @Column(name = "commit_quality_score")
    private Double commitQualityScore;

    @Column(name = "code_review_score")
    private Double codeReviewScore;

    @Column(name = "consistency_score")
    private Double consistencyScore;

    @Column(name = "impact_score")
    private Double impactScore;

    @Column(name = "collaboration_score")
    private Double collaborationScore;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }
}
