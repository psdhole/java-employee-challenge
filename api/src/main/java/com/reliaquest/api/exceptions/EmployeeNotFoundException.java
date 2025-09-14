package com.reliaquest.api.exceptions;

/**
 * Custom exception thrown when an employee is not found.
 */
public class EmployeeNotFoundException extends RuntimeException {
    /**
     * Constructs a new EmployeeNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
