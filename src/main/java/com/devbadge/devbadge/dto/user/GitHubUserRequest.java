package com.devbadge.devbadge.dto.user;

import lombok.Data;

@Data
public class GitHubUserRequest {
    private String username;
    private String name;
    private String email;
    private String bio;
    private String location;
}
