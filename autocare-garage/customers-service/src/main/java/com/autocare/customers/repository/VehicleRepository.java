package com.autocare.customers.repository;

import com.autocare.customers.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByRegistrationNumberIgnoreCase(String registrationNumber);
}
