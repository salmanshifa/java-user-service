package org.example.controller.dto;

public record LoginResponse(String token, String message) {
    public static LoginResponse of(String token, String message) {
        return new LoginResponse(token, message);
    }
}
