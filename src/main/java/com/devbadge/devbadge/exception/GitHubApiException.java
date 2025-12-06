package com.devbadge.devbadge.exception;

public class GitHubApiException extends RuntimeException {
    public GitHubApiException(String msg) {
        super(msg);
    }
}
