package com.devbadge.devbadge.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.awt.desktop.ScreenSleepEvent;
import java.time.LocalDateTime;

@Data
public class GitHubIssueDTO {
    private Long id;
    private Integer number;
    private String title;
    private String state;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("closed_at")
    private LocalDateTime closedAt;

    private Integer comments;

    @JsonProperty("pull_request")
    private Object pullRequest;

}
