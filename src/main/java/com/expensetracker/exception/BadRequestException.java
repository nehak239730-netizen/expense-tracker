package com.expensetracker.exception;

/**
 * Thrown when the request is well-formed but logically wrong,
 * for example start date is after end date (HTTP 400).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
