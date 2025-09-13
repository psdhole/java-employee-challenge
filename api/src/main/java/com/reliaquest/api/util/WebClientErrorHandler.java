package com.reliaquest.api.util;

import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.exceptions.ExternalServiceException;
import com.reliaquest.api.exceptions.TooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;

@Component
@Slf4j
public class WebClientErrorHandler {
    public Throwable handleResponse(ClientResponse response, String body, String id) {
        HttpStatus status = (HttpStatus) response.statusCode();
        if (status == HttpStatus.NOT_FOUND) {
            return new EmployeeNotFoundException(id);
        } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return new TooManyRequestsException("Rate limit exceeded. Please try again later.");
        } else {
            return new ExternalServiceException("Service error: " + status + " - " + body, null);
        }
    }
}
