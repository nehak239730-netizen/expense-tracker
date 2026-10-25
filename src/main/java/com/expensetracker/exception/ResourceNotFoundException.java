package com.expensetracker.exception;

/**
 * Thrown when an expense with the requested id does not exist (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
