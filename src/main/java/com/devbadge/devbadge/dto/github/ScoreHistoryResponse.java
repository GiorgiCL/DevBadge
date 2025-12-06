package com.devbadge.devbadge.dto.github;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScoreHistoryResponse {

    private double commitQuality;
    private double consistency;
    private double collaboration;
    private double impact;
    private double codeReview;

    private double overall;

    private LocalDateTime calculatedAt;
}
