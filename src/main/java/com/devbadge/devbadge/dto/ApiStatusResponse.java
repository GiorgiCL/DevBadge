package com.devbadge.devbadge.dto;

import java.time.Instant;

public record ApiStatusResponse(
        String status,
        String message,
        String username,
        int retryAfterSeconds,
        Instant timestamp
) {
    public static ApiStatusResponse accepted(String message, String username, int retryAfterSeconds) {
        return new ApiStatusResponse("ACCEPTED", message, username, retryAfterSeconds, Instant.now());
    }
}
