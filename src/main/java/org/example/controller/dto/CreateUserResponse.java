package org.example.controller.dto;

import org.example.model.User;

public record CreateUserResponse(User user, String token) {
}
