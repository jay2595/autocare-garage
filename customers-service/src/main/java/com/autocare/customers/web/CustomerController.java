package com.autocare.customers.web;

import com.autocare.customers.model.Customer;
import com.autocare.customers.model.Vehicle;
import com.autocare.customers.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customers;

    public CustomerController(CustomerRepository customers) {
        this.customers = customers;
    }

    @GetMapping
    public List<Customer> list(@RequestParam(required = false) String lastName) {
        if (lastName != null && !lastName.isBlank()) {
            return customers.findByLastNameContainingIgnoreCase(lastName);
        }
        return customers.findAll();
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@Valid @RequestBody Customer customer) {
        customer.setId(null);
        for (Vehicle vehicle : customer.getVehicles()) {
            vehicle.setCustomer(customer);
        }
        return customers.save(customer);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @Valid @RequestBody Customer incoming) {
        Customer existing = customers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        existing.setFirstName(incoming.getFirstName());
        existing.setLastName(incoming.getLastName());
        existing.setPhone(incoming.getPhone());
        existing.setEmail(incoming.getEmail());
        existing.setAddress(incoming.getAddress());
        existing.setCity(incoming.getCity());
        return customers.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!customers.existsById(id)) {
            throw new ResourceNotFoundException("Customer", id);
        }
        customers.deleteById(id);
    }

    @GetMapping("/{id}/vehicles")
    public List<Vehicle> vehicles(@PathVariable Long id) {
        return get(id).getVehicles();
    }

    @PostMapping("/{id}/vehicles")
    public ResponseEntity<Vehicle> addVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicle) {
        Customer customer = customers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        vehicle.setId(null);
        customer.addVehicle(vehicle);
        Customer saved = customers.save(customer);
        Vehicle created = saved.getVehicles().get(saved.getVehicles().size() - 1);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
