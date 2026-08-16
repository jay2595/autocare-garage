package com.autocare.customers.repository;

import com.autocare.customers.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
}
