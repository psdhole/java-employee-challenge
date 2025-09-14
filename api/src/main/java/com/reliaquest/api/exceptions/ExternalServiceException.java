package com.reliaquest.api.exceptions;

/**
 * Custom exception for errors related to external service calls.
 */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
