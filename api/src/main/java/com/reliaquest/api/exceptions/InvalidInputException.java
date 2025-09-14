package com.reliaquest.api.exceptions;
/**
 * Custom exception for invalid input scenarios.
 */
public class InvalidInputException extends RuntimeException {
    /**
     * Constructs a new InvalidInputException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
