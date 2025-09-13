package com.reliaquest.api.service;

import static com.reliaquest.api.util.CommonUtil.toJson;

import com.reliaquest.api.exceptions.ExternalServiceException;
import com.reliaquest.api.exceptions.InvalidInputException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.DeleteEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.util.WebClientErrorHandler;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final WebClient employeeApiClient;
    private final WebClientErrorHandler errorHandler;

    @Retry(name = "employeeApiRetry")
    public List<Employee> getAllEmployees() {
        log.debug("Fetching all employees");
        List<Employee> employees = employeeApiClient
                .get()
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.warn("Employee API failed with status: {}, body: {}", response.statusCode(), body);
                            return Mono.error(errorHandler.handleResponse(response, body, "getAllEmployees"));
                        }))
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {})
                .map(ApiResponse::getData)
                .defaultIfEmpty(Collections.emptyList())
                .block();
        log.debug("Successfully fetched all employees: {}", toJson(employees));
        return employees;
    }

    @Retry(name = "employeeApiRetry")
    public List<Employee> searchEmployeesByName(String searchName) {
        log.info("Searching employees with name: {}", searchName);
        if (searchName == null || searchName.isBlank()) {
            throw new InvalidInputException("Search string must not be empty");
        }
        List<Employee> matchedEmployees = getAllEmployees().stream()
                .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(searchName.toLowerCase()))
                .collect(Collectors.toList());

        log.debug("Search successful for: '{}'. Matches found: {}", searchName, toJson(matchedEmployees));
        return matchedEmployees;
    }

    @Retry(name = "employeeApiRetry")
    public Employee getEmployeeById(String id) {
        log.info("Fetching employee by ID: {}", id);
        Employee employee = employeeApiClient
                .get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.warn(
                                    "Failed to fetch employee with ID: {}, status: {}, body: {}",
                                    id,
                                    response.statusCode(),
                                    body);
                            return Mono.error(errorHandler.handleResponse(response, body, id));
                        }))
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Employee>>() {})
                .map(ApiResponse::getData)
                .block();

        if (employee == null) {
            throw new ExternalServiceException("failed to fetch employee with ID: " + id, null);
        }
        log.debug("Successfully fetched employee: {}", toJson(employee));
        return employee;
    }

    @Retry(name = "employeeApiRetry")
    public Integer getHighestSalary() {
        log.info("Calculating highest employee salary");
        Integer highestSalary = getAllEmployees().stream()
                .map(Employee::getSalary)
                .max(Comparator.naturalOrder())
                .orElse(0);
        log.debug("Highest salary found: {}", highestSalary);
        return highestSalary;
    }

    @Retry(name = "employeeApiRetry")
    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.info("Fetching top 10 highest earning employee names");
        List<String> topEarners = getAllEmployees().stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());
        log.debug("Fetched top 10 earning employee names: {}", toJson(topEarners));
        return topEarners;
    }

    @Retry(name = "employeeApiRetry")
    public Employee createEmployee(CreateEmployeeRequest request) {
        log.info("Creating new employee with name: {}", request.getName());

        Employee employee = employeeApiClient
                .post()
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.warn(
                                    "Failed to create employee '{}': status={}, body={}",
                                    request.getName(),
                                    response.statusCode(),
                                    body);
                            return Mono.error(errorHandler.handleResponse(response, body, request.getName()));
                        }))
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Employee>>() {})
                .map(ApiResponse::getData)
                .block();

        if (employee == null) {
            throw new ExternalServiceException("failed to create employee with name: " + request.getName(), null);
        }

        log.debug("Successfully created employee: {}", toJson(employee));
        return employee;
    }

    @Retry(name = "employeeApiRetry")
    public String deleteEmployeeById(String id) {
        log.info("Deleting employee with ID: {}", id);

        // Find employee name to delete
        Employee emp = getEmployeeById(id); // will throw if not found

        // Build delete request
        DeleteEmployeeRequest input = new DeleteEmployeeRequest();
        input.setId(id);
        input.setName(emp.getName());

        employeeApiClient
                .method(HttpMethod.DELETE)
                .bodyValue(input)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.warn(
                                    "Failed to delete employee with ID {}, status: {}, body: {}",
                                    id,
                                    response.statusCode(),
                                    body);
                            return Mono.error(errorHandler.handleResponse(response, body, id));
                        }))
                .bodyToMono(Void.class)
                .block();

        log.debug("Successfully deleted employee with ID: {}", id);
        return emp.getName();
    }
}
