package com.autocare.webui.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobDtoTests {

    private JobDto jobWithStatus(String status, VehicleDto vehicle) {
        return new JobDto(1L, "Brake pads", status, status, null, null, null,
                5L, vehicle, null, null);
    }

    @Test
    void mapsStatusToAccentColour() {
        assertThat(jobWithStatus("IN_PROGRESS", null).accentClass()).contains("amber");
        assertThat(jobWithStatus("COMPLETED", null).accentClass()).contains("emerald");
        assertThat(jobWithStatus("DIAGNOSING", null).accentClass()).contains("sky");
    }

    @Test
    void fallsBackToNeutralColourForUnknownStatus() {
        assertThat(jobWithStatus("SOMETHING_ELSE", null).accentClass()).contains("zinc");
        assertThat(jobWithStatus(null, null).badgeClass()).contains("zinc");
    }

    @Test
    void showsPlaceholderLabelWhenVehicleCannotBeResolved() {
        JobDto job = jobWithStatus("RECEIVED", null);

        assertThat(job.hasVehicle()).isFalse();
        assertThat(job.vehicleLabel()).isEqualTo("Vehicle #5 (unavailable)");
        assertThat(job.registration()).isEqualTo("-");
    }

    @Test
    void usesVehicleLabelWhenResolved() {
        VehicleDto vehicle = new VehicleDto(5L, "Toyota", "Corolla", 2018,
                "OSK4821", "JTDBR32E830123456", 87650, 3L, "Mei Tanaka");

        JobDto job = jobWithStatus("RECEIVED", vehicle);

        assertThat(job.hasVehicle()).isTrue();
        assertThat(job.vehicleLabel()).isEqualTo("2018 Toyota Corolla");
        assertThat(job.registration()).isEqualTo("OSK4821");
    }
}
