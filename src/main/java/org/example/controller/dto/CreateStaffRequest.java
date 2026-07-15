package org.example.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.model.CategoryConstants;
import org.example.model.EmploymentStatus;
import org.example.model.PositionConstants;

import java.time.LocalDate;
import java.util.List;

public record CreateStaffRequest(

        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Position is required")
        @Pattern(
                regexp = "^(" + PositionConstants.STYLIST + "|" + PositionConstants.BARBER + "|" + PositionConstants.NAIL_TECH + "|" + PositionConstants.MASSAGE_THERAPIST + "|" + PositionConstants.ESTHETICIAN + "|" + PositionConstants.RECEPTIONIST + "|" + PositionConstants.MANAGER + ")$",
                message = "Position must be one of: STYLIST, BARBER, NAIL_TECH, MASSAGE_THERAPIST, ESTHETICIAN, RECEPTIONIST, MANAGER"
        )
        String position,

        @Pattern(
                regexp = "^(|" + CategoryConstants.MASSAGE + "|" + CategoryConstants.HAIR + "|" + CategoryConstants.FACIAL + "|" + CategoryConstants.NAILS + ")$",
                message = "Specialization must be one of: MASSAGE, HAIR, FACIAL, NAILS"
        )
        String specialization,

        LocalDate hireDate,

        @NotNull(message = "Employment status is required")
        EmploymentStatus employmentStatus,

        @Size(max = 500, message = "Work schedule must not exceed 500 characters")
        String workSchedule,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Phone must be a valid international number")
        String phone,

        @Size(max = 500, message = "Specialty must not exceed 500 characters")
        String specialty,

        List<String> serviceCategories

) {
}
