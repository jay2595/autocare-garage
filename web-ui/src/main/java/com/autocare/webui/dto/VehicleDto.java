package com.autocare.webui.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VehicleDto(
        Long id,
        String make,
        String model,
        Integer year,
        String registrationNumber,
        String vin,
        Integer mileage,
        Long customerId,
        String customerName) {

    public String mileageLabel() {
        return mileage == null ? "-" : String.format("%,d km", mileage);
    }

    public String label() {
        return year + " " + make + " " + model;
    }
}
