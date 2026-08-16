package com.autocare.customers.web;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String what, Long id) {
        super(what + " " + id + " not found");
    }
}
