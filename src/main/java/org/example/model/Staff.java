package org.example.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Staff(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String position,
        String specialization,
        LocalDate hireDate,
        EmploymentStatus employmentStatus,
        String workSchedule,
        String specialty,
        List<String> serviceCategories,
        Long createdBy,
        LocalDateTime createdAt,
        Long updatedBy,
        LocalDateTime updatedAt
) {
}
