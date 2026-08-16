package com.autocare.webui.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JobDto(
        Long id,
        String description,
        String status,
        String statusLabel,
        BigDecimal estimatedCost,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        Long vehicleId,
        VehicleDto vehicle,
        Long mechanicId,
        String mechanicName) {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public String createdAtLabel() {
        return createdAt == null ? "-" : DAY.format(createdAt);
    }

    public String completedAtLabel() {
        return completedAt == null ? "open" : DAY.format(completedAt);
    }

    public String costLabel() {
        return estimatedCost == null ? "-" : "$" + estimatedCost.toPlainString();
    }

    public String mileageLabel() {
        if (vehicle == null || vehicle.mileage() == null) {
            return "-";
        }
        return String.format("%,d km", vehicle.mileage());
    }

    /** Left border stripe colour for the job card. */
    public String accentClass() {
        if (status == null) {
            return "border-l-zinc-600";
        }
        return switch (status) {
            case "RECEIVED" -> "border-l-zinc-500";
            case "DIAGNOSING" -> "border-l-sky-500";
            case "IN_PROGRESS" -> "border-l-amber-500";
            case "COMPLETED" -> "border-l-emerald-500";
            case "DELIVERED" -> "border-l-emerald-700";
            default -> "border-l-zinc-600";
        };
    }

    /** Pill colours for the status chip. */
    public String badgeClass() {
        if (status == null) {
            return "bg-zinc-800 text-zinc-300 ring-zinc-700";
        }
        return switch (status) {
            case "RECEIVED" -> "bg-zinc-800 text-zinc-300 ring-zinc-600";
            case "DIAGNOSING" -> "bg-sky-950 text-sky-300 ring-sky-800";
            case "IN_PROGRESS" -> "bg-amber-950 text-amber-300 ring-amber-800";
            case "COMPLETED" -> "bg-emerald-950 text-emerald-300 ring-emerald-800";
            case "DELIVERED" -> "bg-emerald-950 text-emerald-500 ring-emerald-900";
            default -> "bg-zinc-800 text-zinc-300 ring-zinc-700";
        };
    }

    public boolean hasVehicle() {
        return vehicle != null;
    }

    public String vehicleLabel() {
        return vehicle == null ? "Vehicle #" + vehicleId + " (unavailable)" : vehicle.label();
    }

    public String registration() {
        return vehicle == null ? "-" : vehicle.registrationNumber();
    }
}
