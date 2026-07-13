package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceItem(
        Long id,
        String name,
        String description,
        String category,
        BigDecimal price,
        Integer durationMinutes,
        ServiceStatus status,
        Long createdBy,
        LocalDateTime createdAt,
        Long updatedBy,
        LocalDateTime updatedAt
) {
}
