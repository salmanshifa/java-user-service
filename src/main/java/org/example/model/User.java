package org.example.model;

public record User(Long id, String username, String email, String phone, String role, boolean enabled) {
}
