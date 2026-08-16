package com.autocare.webui.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerDto(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String email,
        String address,
        String city,
        List<VehicleDto> vehicles) {

    public String fullName() {
        return firstName + " " + lastName;
    }

    public String initials() {
        String a = firstName == null || firstName.isEmpty() ? "?" : firstName.substring(0, 1);
        String b = lastName == null || lastName.isEmpty() ? "" : lastName.substring(0, 1);
        return (a + b).toUpperCase();
    }

    public int vehicleCount() {
        return vehicles == null ? 0 : vehicles.size();
    }
}
