package com.devbadge.devbadge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "analysis_cache",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_analysis_cache_user_key", columnNames = {"user_id", "cache_key"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private GitHubUser user;

    @Column(name = "cache_key", nullable = false)
    private String cacheKey;

    @Column(name = "cache_data", columnDefinition = "TEXT")
    private String cacheData;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
