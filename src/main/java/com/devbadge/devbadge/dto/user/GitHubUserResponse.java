package com.devbadge.devbadge.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitHubUserResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String bio;
    private String location;
}
