package com.autocare.workshop.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Talks to customers-service over HTTP. In Kubernetes the base URL is just
 * http://customers-service:8081 - the cluster DNS resolves the service name.
 *
 * Every call is defensive: if customers-service is down the job list must still
 * render, just without vehicle details. That is the whole point of splitting them.
 */
@Component
public class CustomersClient {

    private static final Logger log = LoggerFactory.getLogger(CustomersClient.class);

    private final RestClient restClient;

    public CustomersClient(RestClient customersRestClient) {
        this.restClient = customersRestClient;
    }

    public VehicleRef findVehicle(Long vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        try {
            return restClient.get()
                    .uri("/api/vehicles/{id}", vehicleId)
                    .retrieve()
                    .body(VehicleRef.class);
        } catch (Exception ex) {
            log.warn("Could not reach customers-service for vehicle {}: {}", vehicleId, ex.getMessage());
            return null;
        }
    }

    public boolean isReachable() {
        try {
            restClient.get().uri("/actuator/health").retrieve().toBodilessEntity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
