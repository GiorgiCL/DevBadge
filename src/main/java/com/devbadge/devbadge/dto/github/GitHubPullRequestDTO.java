package com.devbadge.devbadge.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GitHubPullRequestDTO {
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

    @JsonProperty("merged_at")
    private LocalDateTime mergedAt;

    private Integer comments;

    @JsonProperty("review_comments")
    private Integer reviewComments;

    private Integer commits;
    private Integer additions;
    private Integer deletions;

    @JsonProperty("changed_files")
    private Integer changedFiles;

}
