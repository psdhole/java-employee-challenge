package com.reliaquest.api.util;

import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.exceptions.ExternalServiceException;
import com.reliaquest.api.exceptions.InvalidInputException;
import com.reliaquest.api.exceptions.TooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Utility class for handling WebClient exceptions and mapping them to custom exceptions.
 */
@Component
@Slf4j
public class WebClientErrorHandler {
    /**
     * Handles WebClientResponseException and maps it to custom exceptions.
     *
     * @param ex the WebClientResponseException to handle
     * @return a RuntimeException representing the mapped exception
     */
    public RuntimeException handleException(WebClientResponseException ex) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        String body = ex.getResponseBodyAsString();
        if (status == HttpStatus.NOT_FOUND) {
            return new EmployeeNotFoundException("Employee with given ID not found");
        } else if (status == HttpStatus.BAD_REQUEST) {
            return new InvalidInputException("Invalid input: " + body);
        } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return new TooManyRequestsException("Too many requests to upstream service");
        } else if (status.is5xxServerError()) {
            return new ExternalServiceException("Upstream employee service api unavailable", ex);
        } else {
            return ex;
        }
    }
}
