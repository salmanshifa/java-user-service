package org.example.controller.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.model.BookingStatus;

import java.time.LocalDateTime;

public record CreateBookingRequest(

        Long customerUserId,

        Long staffId,

        @NotNull(message = "Service ID is required")
        Long serviceId,

        @NotNull(message = "Appointment time is required")
        @Future(message = "Appointment time must be in the future")
        LocalDateTime appointmentTime,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        LocalDateTime endTime,

        BookingStatus status,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes

) {
}
