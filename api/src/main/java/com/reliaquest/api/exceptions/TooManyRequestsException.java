package com.reliaquest.api.exceptions;
/**
 * Custom exception for handling too many requests scenarios.
 */
public class TooManyRequestsException extends RuntimeException {
    /**
     * Constructs a new TooManyRequestsException with the specified detail message.
     *
     * @param message the detail message
     */
    public TooManyRequestsException(String message) {
        super(message);
    }
}
