package com.autocare.customers.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A vehicle belonging to a customer. The workshop service refers to these by id.
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Make is required")
    @Size(max = 40)
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 40)
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1950, message = "Year must be 1950 or later")
    @Max(value = 2035, message = "Year looks wrong")
    @Column(name = "model_year")
    private Integer year;

    @NotBlank(message = "Registration number is required")
    @Size(max = 15)
    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Size(max = 17)
    private String vin;

    @Min(value = 0, message = "Mileage cannot be negative")
    private Integer mileage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;

    @JsonProperty("customerId")
    public Long getCustomerId() {
        return customer != null ? customer.getId() : null;
    }

    @JsonProperty("customerName")
    public String getCustomerName() {
        return customer != null ? customer.getFullName() : null;
    }

    @JsonProperty("displayName")
    public String getDisplayName() {
        return year + " " + make + " " + model;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }

    @JsonIgnore
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}
