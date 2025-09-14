package com.reliaquest.api.exceptions;
/**
 * Custom exception for handling too many requests scenarios.
 */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
