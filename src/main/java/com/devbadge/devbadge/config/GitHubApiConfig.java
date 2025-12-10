package com.devbadge.devbadge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GitHubApiConfig {

    @Bean
    public RestTemplate githubRestTemplate() {
        return new RestTemplate();  // SIMPLE, SAFE, WORKS
    }
}
