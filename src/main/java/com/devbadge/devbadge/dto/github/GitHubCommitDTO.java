package com.devbadge.devbadge.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GitHubCommitDTO {
    private String sha;
    private CommitDetails commit;
    private Stats stats;

    @Data
    public static class CommitDetails {
        private Author author;
        private String message;

        @JsonProperty("comment_count")
        private Integer commentCount;

        @Data
        public static class Author {
            private String name;
            private String email;
            private LocalDateTime date;

        }
    }
    @Data
    public static class Stats {
        private Integer additions;
        private Integer deletions;
        private Integer total;
    }
}
