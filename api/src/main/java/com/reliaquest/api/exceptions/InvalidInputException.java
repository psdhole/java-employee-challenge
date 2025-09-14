package com.reliaquest.api.exceptions;
/**
 * Custom exception for invalid input scenarios.
 */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
