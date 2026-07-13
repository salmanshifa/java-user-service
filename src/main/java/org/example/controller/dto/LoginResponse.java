package org.example.controller.dto;

import org.example.model.User;

public record LoginResponse(String token, String message, User user) {
    public static LoginResponse of(String token, String message, User user) {
        return new LoginResponse(token, message, user);
    }
}
