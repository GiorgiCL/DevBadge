package com.devbadge.devbadge.exception;

public class UserNotInDatabaseException extends RuntimeException {
    private final String username;

    public UserNotInDatabaseException(String username) {
        super("User not found in DB: " + username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
