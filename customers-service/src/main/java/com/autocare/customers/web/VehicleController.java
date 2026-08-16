package com.autocare.customers.web;

import com.autocare.customers.model.Vehicle;
import com.autocare.customers.repository.VehicleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only vehicle lookup. The workshop service calls GET /api/vehicles/{id}
 * to resolve the vehicle attached to a service job.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository vehicles;

    public VehicleController(VehicleRepository vehicles) {
        this.vehicles = vehicles;
    }

    @GetMapping
    public List<Vehicle> list(@RequestParam(required = false) String registration) {
        if (registration != null && !registration.isBlank()) {
            return vehicles.findByRegistrationNumberIgnoreCase(registration)
                    .map(List::of)
                    .orElseGet(List::of);
        }
        return vehicles.findAll();
    }

    @GetMapping("/{id}")
    public Vehicle get(@PathVariable Long id) {
        return vehicles.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }
}
