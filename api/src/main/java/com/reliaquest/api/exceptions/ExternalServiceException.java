package com.reliaquest.api.exceptions;

/**
 * Custom exception for errors related to external service calls.
 */
public class ExternalServiceException extends RuntimeException {
    /**
     * Constructs a new ExternalServiceException with the specified detail message.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
