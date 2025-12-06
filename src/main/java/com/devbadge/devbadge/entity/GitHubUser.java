package com.devbadge.devbadge.entity;

import jakarta.persistence.*;
import lombok.*;

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

    private String avatarUrl;
    private String bio;
    private String location;
    private Integer publicRepos;
    private Integer followers;
    private Integer following;

    private String lastSyncTime;

}

