package org.example.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.model.CategoryConstants;
import org.example.model.ServiceStatus;

import java.math.BigDecimal;

public record CreateServiceRequest(

        @NotBlank(message = "Service name is required")
        @Size(min = 2, max = 100, message = "Service name must be between 2 and 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "Category is required")
        @Pattern(
                regexp = "^(" + CategoryConstants.MASSAGE + "|" + CategoryConstants.HAIR + "|" + CategoryConstants.FACIAL + "|" + CategoryConstants.NAILS + ")$",
                message = "Category must be one of: MASSAGE, HAIR, FACIAL, NAILS"
        )
        String category,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Duration is required")
        @Min(value = 5, message = "Duration must be at least 5 minutes")
        Integer durationMinutes,

        @NotNull(message = "Status is required")
        ServiceStatus status

) {
}
