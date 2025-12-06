package com.devbadge.devbadge.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
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

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}
