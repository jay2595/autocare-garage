package com.autocare.customers.web;

import com.autocare.customers.model.Customer;
import com.autocare.customers.model.Vehicle;
import com.autocare.customers.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test: real repositories, real JSON serialisation, no mocks.
 *
 * The mocked controller tests cannot catch lazy-loading problems because they never
 * touch Hibernate. This one does - it is the test that fails if Vehicle.customer ever
 * goes back to FetchType.LAZY while open-in-view is disabled.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customers;

    @BeforeEach
    void seed() {
        customers.deleteAll();

        Customer customer = new Customer();
        customer.setFirstName("Priya");
        customer.setLastName("Sharma");
        customer.setPhone("+91 98200 11223");
        customer.setEmail("priya@example.com");
        customer.setCity("Mumbai");

        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Honda");
        vehicle.setModel("City");
        vehicle.setYear(2019);
        vehicle.setRegistrationNumber("MH01AB1234");
        vehicle.setMileage(64200);
        customer.addVehicle(vehicle);

        customers.save(customer);
    }

    @Test
    void listsVehiclesWithOwnerDetailsResolved() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Honda"))
                .andExpect(jsonPath("$[0].customerName").value("Priya Sharma"))
                .andExpect(jsonPath("$[0].customerId").exists())
                .andExpect(jsonPath("$[0].displayName").value("2019 Honda City"));
    }

    @Test
    void singleVehicleLookupAlsoResolvesOwner() throws Exception {
        Long vehicleId = customers.findAll().get(0).getVehicles().get(0).getId();

        mockMvc.perform(get("/api/vehicles/{id}", vehicleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("MH01AB1234"))
                .andExpect(jsonPath("$.customerName").value("Priya Sharma"));
    }

    @Test
    void unknownVehicleReturns404() throws Exception {
        mockMvc.perform(get("/api/vehicles/9999"))
                .andExpect(status().isNotFound());
    }
}
