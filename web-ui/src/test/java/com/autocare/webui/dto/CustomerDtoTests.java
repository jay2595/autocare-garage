package com.autocare.webui.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerDtoTests {

    @Test
    void buildsFullNameAndInitials() {
        CustomerDto c = new CustomerDto(1L, "Priya", "Sharma", "123", null, null, "Mumbai", List.of());

        assertThat(c.fullName()).isEqualTo("Priya Sharma");
        assertThat(c.initials()).isEqualTo("PS");
        assertThat(c.vehicleCount()).isZero();
    }

    @Test
    void handlesNullVehicleList() {
        CustomerDto c = new CustomerDto(1L, "A", "B", "1", null, null, null, null);

        assertThat(c.vehicleCount()).isZero();
    }
}
