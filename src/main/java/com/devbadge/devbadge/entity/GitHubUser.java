package com.devbadge.devbadge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private Long githubUserId;


    private String avatarUrl;
    private String bio;
    private String location;
    private Integer publicRepos;
    private Integer followers;
    private Integer following;
    private String name;
    private String email;
    private Integer publicGists;
    private LocalDateTime accountCreatedAt;
    private LocalDateTime lastUpdatedAt;


    private String lastSyncTime;

}

