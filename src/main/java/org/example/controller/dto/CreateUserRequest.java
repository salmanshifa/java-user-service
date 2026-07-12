package org.example.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.model.RoleConstants;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Mobile number must be a valid international number")
        String mobileNumber,

        @NotBlank(message = "Role is required")
        @Pattern(
                regexp = "^(" + RoleConstants.CUSTOMER + "|" + RoleConstants.SHOP_OWNER + ")$",
                message = "Role must be CUSTOMER or SHOP_OWNER"
        )
        String role,

        @NotNull(message = "Enabled flag is required")
        boolean enabled,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}
