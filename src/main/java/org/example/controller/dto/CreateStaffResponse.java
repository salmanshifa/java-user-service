package org.example.controller.dto;

import org.example.model.Staff;
import org.example.model.User;

public record CreateStaffResponse(
        Staff staff,
        User user,
        String token
) {
    public static CreateStaffResponse of(Staff staff, User user, String token) {
        return new CreateStaffResponse(staff, user, token);
    }
}
