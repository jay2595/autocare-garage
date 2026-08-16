package com.autocare.customers.web;

import com.autocare.customers.model.Customer;
import com.autocare.customers.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@ActiveProfiles("test")
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerRepository customers;

    private Customer sample() {
        Customer c = new Customer();
        c.setId(1L);
        c.setFirstName("Priya");
        c.setLastName("Sharma");
        c.setPhone("+91 98200 11223");
        c.setEmail("priya@example.com");
        c.setCity("Mumbai");
        return c;
    }

    @Test
    void listsAllCustomers() throws Exception {
        given(customers.findAll()).willReturn(List.of(sample()));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName").value("Sharma"));
    }

    @Test
    void returns404WhenCustomerMissing() throws Exception {
        given(customers.findById(anyLong())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer 99 not found"));
    }

    @Test
    void rejectsCustomerWithBlankLastName() throws Exception {
        Customer bad = sample();
        bad.setLastName("");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lastName").exists());
    }

    @Test
    void createsCustomer() throws Exception {
        given(customers.save(any(Customer.class))).willReturn(sample());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}
