package com.autocare.webui.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MechanicDto(
        Long id,
        String firstName,
        String lastName,
        LocalDate hireDate,
        List<SpecialtyDto> specialties) {

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMM yyyy");

    public String hiredLabel() {
        return hireDate == null ? "" : "since " + MONTH.format(hireDate);
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public String initials() {
        String a = firstName == null || firstName.isEmpty() ? "?" : firstName.substring(0, 1);
        String b = lastName == null || lastName.isEmpty() ? "" : lastName.substring(0, 1);
        return (a + b).toUpperCase();
    }
}
