package com.autocare.workshop.client;

/**
 * The slice of a vehicle that workshop-service cares about.
 * Deserialised from customers-service GET /api/vehicles/{id}.
 */
public record VehicleRef(
        Long id,
        String make,
        String model,
        Integer year,
        String registrationNumber,
        Integer mileage,
        Long customerId,
        String customerName) {

    public String displayName() {
        return year + " " + make + " " + model;
    }
}
