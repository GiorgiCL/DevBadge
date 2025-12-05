package com.devbadge.devbadge.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GitHubUserDTO {
        private String login;
        private Long id;
        private String name;
        private String bio;
        private String location;
        private String email;


        @JsonProperty("public_repos")
        private Integer publicRepos;

        @JsonProperty("public_gists")
        private Integer publicGists;

        private Integer followers;
        private Integer following;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;
    }

