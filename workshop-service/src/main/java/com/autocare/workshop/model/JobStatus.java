package com.autocare.workshop.model;

/**
 * Lifecycle of a service job. The UI colours each stage differently.
 */
public enum JobStatus {

    RECEIVED("Received"),
    DIAGNOSING("Diagnosing"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    DELIVERED("Delivered");

    private final String label;

    JobStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isOpen() {
        return this != COMPLETED && this != DELIVERED;
    }
}
