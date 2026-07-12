package org.example.model;

public record User(Long id, String username, String email, String mobileNumber, String role, boolean enabled) {
}
