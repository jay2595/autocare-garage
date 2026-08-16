package com.autocare.workshop.web;

import com.autocare.workshop.client.VehicleRef;
import com.autocare.workshop.model.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A service job enriched with data pulled from customers-service.
 * vehicle is null when customers-service is unavailable.
 */
public record JobView(
        Long id,
        String description,
        JobStatus status,
        String statusLabel,
        BigDecimal estimatedCost,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        Long vehicleId,
        VehicleRef vehicle,
        Long mechanicId,
        String mechanicName) {
}
