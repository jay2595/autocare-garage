package com.autocare.webui.client;

import com.autocare.webui.dto.CustomerDto;
import com.autocare.webui.dto.VehicleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CustomersClient {

    private static final Logger log = LoggerFactory.getLogger(CustomersClient.class);

    private final RestClient client;

    public CustomersClient(RestClient customersRestClient) {
        this.client = customersRestClient;
    }

    public List<CustomerDto> findAll(String lastName) {
        try {
            String uri = (lastName == null || lastName.isBlank())
                    ? "/api/customers"
                    : "/api/customers?lastName=" + lastName;
            List<CustomerDto> result = client.get().uri(uri).retrieve()
                    .body(new ParameterizedTypeReference<List<CustomerDto>>() { });
            return result == null ? List.of() : result;
        } catch (Exception ex) {
            log.warn("customers-service unavailable: {}", ex.getMessage());
            return List.of();
        }
    }

    public CustomerDto findById(Long id) {
        try {
            return client.get().uri("/api/customers/{id}", id).retrieve().body(CustomerDto.class);
        } catch (Exception ex) {
            log.warn("Could not load customer {}: {}", id, ex.getMessage());
            return null;
        }
    }

    public List<VehicleDto> findAllVehicles() {
        try {
            List<VehicleDto> result = client.get().uri("/api/vehicles").retrieve()
                    .body(new ParameterizedTypeReference<List<VehicleDto>>() { });
            return result == null ? List.of() : result;
        } catch (Exception ex) {
            log.warn("customers-service unavailable: {}", ex.getMessage());
            return List.of();
        }
    }

    public void create(String firstName, String lastName, String phone,
                       String email, String address, String city) {
        client.post().uri("/api/customers")
                .body(new CustomerDto(null, firstName, lastName, phone, email, address, city, List.of()))
                .retrieve()
                .toBodilessEntity();
    }

    public void addVehicle(Long customerId, VehicleDto vehicle) {
        client.post().uri("/api/customers/{id}/vehicles", customerId)
                .body(vehicle)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean isUp() {
        try {
            client.get().uri("/actuator/health").retrieve().toBodilessEntity();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
