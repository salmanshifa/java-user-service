package org.example.controller.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import org.example.model.BookingStatus;

import java.time.LocalDateTime;

public record UpdateBookingRequest(

        Long customerUserId,

        Long staffId,

        Long serviceId,

        @Future(message = "Appointment time must be in the future")
        LocalDateTime appointmentTime,

        @Future(message = "End time must be in the future")
        LocalDateTime endTime,

        BookingStatus status,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes

) {
}
