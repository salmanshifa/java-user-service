package org.example.model;

import java.time.LocalDateTime;

public record Booking(
        Long id,
        Long customerUserId,
        Long staffId,
        Long serviceId,
        LocalDateTime appointmentTime,
        LocalDateTime endTime,
        BookingStatus status,
        String notes,
        Long createdBy,
        LocalDateTime createdAt,
        Long updatedBy,
        LocalDateTime updatedAt
) {
}
