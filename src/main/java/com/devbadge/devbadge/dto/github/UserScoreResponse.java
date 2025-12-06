package com.devbadge.devbadge.dto.github;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserScoreResponse {

    private String username;

    private double commitQuality;
    private double consistency;
    private double collaboration;
    private double impact;
    private double codeReview;

    private double overallScore;
}
