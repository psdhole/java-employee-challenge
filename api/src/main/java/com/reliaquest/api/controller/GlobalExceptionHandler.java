package com.reliaquest.api.controller;

import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.exceptions.ExternalServiceException;
import com.reliaquest.api.exceptions.InvalidInputException;
import com.reliaquest.api.exceptions.TooManyRequestsException;
import com.reliaquest.api.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Advice to handle all/custom type of exceptions.
 * This ensures proper logging and handling based on the exception type.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /** Handles exceptions related to employee not found scenarios.
     *
     * @param ex the exception that was thrown
     * @return a ResponseEntity with a 404 status and error message
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(EmployeeNotFoundException ex) {
        log.error("Request failed with not found error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(ex.getMessage()));
    }

    /** Handles exceptions related to invalid input.
     *
     * @param ex the exception that was thrown
     * @return a ResponseEntity with a 400 status and error message
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidInput(InvalidInputException ex) {
        log.error("Request failed with invalid input error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse<>("Invalid input: " + ex.getMessage()));
    }

    /** Handles exceptions related to rate limiting.
     *
     * @param ex the exception that was thrown
     * @return a ResponseEntity with a 429 status and error message
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse<Object>> handleTooManyRequests(TooManyRequestsException ex) {
        log.error("Request failed with to many requests error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(ex.getMessage()));
    }

    /** Handles exceptions related to failures in external service calls.
     *
     * @param ex the exception that was thrown
     * @return a ResponseEntity with a 502 status and error message
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleExternal(ExternalServiceException ex) {
        log.error("Request failed with external api error", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiResponse<>(ex.getMessage()));
    }

    /**
     * Generic exception handler for any unhandled exceptions.
     *
     * @param ex the exception that was thrown
     * @return a ResponseEntity with a 500 status and error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        log.error("Request failed with unhandled error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Employee service api failed: " + ex.getMessage()));
    }
}
