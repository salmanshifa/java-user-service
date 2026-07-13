package org.example.security;

/**
 * Custom principal that holds both username and userId from the JWT token.
 * <code>toString()</code> returns just the username so that
 * {@code Authentication.getName()} continues to work correctly.
 */
public class AuthenticatedUser {

    private final String username;
    private final Long userId;

    public AuthenticatedUser(String username, Long userId) {
        this.username = username;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return username;
    }
}
