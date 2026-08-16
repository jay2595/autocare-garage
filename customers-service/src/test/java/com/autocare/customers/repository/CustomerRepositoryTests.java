package com.autocare.customers.repository;

import com.autocare.customers.model.Customer;
import com.autocare.customers.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTests {

    @Autowired
    private CustomerRepository customers;

    private Customer sampleCustomer() {
        Customer c = new Customer();
        c.setFirstName("Ravi");
        c.setLastName("Krishnan");
        c.setPhone("+91 90000 11111");
        c.setEmail("ravi@example.com");
        c.setCity("Chennai");

        Vehicle v = new Vehicle();
        v.setMake("Hyundai");
        v.setModel("i20");
        v.setYear(2020);
        v.setRegistrationNumber("TN09ZZ0001");
        v.setMileage(41000);
        c.addVehicle(v);
        return c;
    }

    @Test
    void savesCustomerWithCascadedVehicle() {
        Customer saved = customers.save(sampleCustomer());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVehicles()).hasSize(1);
        assertThat(saved.getVehicles().get(0).getId()).isNotNull();
    }

    @Test
    void findsByPartialLastNameIgnoringCase() {
        customers.save(sampleCustomer());

        List<Customer> found = customers.findByLastNameContainingIgnoreCase("krish");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getFullName()).isEqualTo("Ravi Krishnan");
    }

    @Test
    void returnsEmptyListWhenNoMatch() {
        customers.save(sampleCustomer());

        assertThat(customers.findByLastNameContainingIgnoreCase("nobody")).isEmpty();
    }
}
