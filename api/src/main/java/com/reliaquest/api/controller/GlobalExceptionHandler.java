package com.reliaquest.api.controller;

import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.exceptions.ExternalServiceException;
import com.reliaquest.api.exceptions.InvalidInputException;
import com.reliaquest.api.exceptions.TooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EmployeeNotFoundException ex) {
        log.error("Request failed with not found error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<String> handleInvalidInput(InvalidInputException ex) {
        log.error("Request failed with invalid input error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Invalid input: " + ex.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<String> handleTooManyRequests(TooManyRequestsException ex) {
        log.error("Request failed with to many requests error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<String> handleExternal(ExternalServiceException ex) {
        log.error("Request failed with external api error: {}", ex.getMessage());
        String message = "External employee service api is currently unavailable..please try again later.";
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        log.error("Request failed with unhandled exception: {}", ex.getMessage());
        String message = "External employee service api is currently unavailable..please try again later.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
}
