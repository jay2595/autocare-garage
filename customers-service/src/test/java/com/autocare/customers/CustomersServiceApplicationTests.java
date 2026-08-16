package com.autocare.customers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CustomersServiceApplicationTests {

    @Test
    void contextLoads() {
        // Fails the build if any bean cannot be wired.
    }
}
