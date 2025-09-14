package com.reliaquest.api.service;

import static com.reliaquest.api.util.CommonUtil.toJson;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeDto;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Service class for managing Employee operations via an external API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final WebClient employeeApiClient;
    private final WebClientErrorHandler errorHandler;

    /**
     * Fetches all employees from the external API.
     *
     * @return a list of all employees
     */
    @Retry(name = "employeeApiRetry")
    public List<Employee> getAllEmployees() {
        log.debug("Fetching all employees");
        List<Employee> employees;
        try {
            employees = employeeApiClient
                    .get()
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {})
                    .map(ApiResponse::getData)
                    .defaultIfEmpty(Collections.emptyList())
                    .block();
        } catch (WebClientResponseException ex) {
            throw errorHandler.handleException(ex);
        }
        log.debug("Successfully fetched all employees: {}", toJson(employees));
        return employees;
    }

    /**
     * Searches for employees whose names contain the specified search string (case-insensitive).
     *
     * @param searchName the name or partial name to search for
     * @return a list of employees matching the search criteria
     */
    @Retry(name = "employeeApiRetry")
    public List<Employee> searchEmployeesByName(String searchName) {
        log.info("Searching employees with name: {}", searchName);
        List<Employee> matchedEmployees = getAllEmployees().stream()
                .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(searchName.toLowerCase()))
                .collect(Collectors.toList());

        log.debug("Search successful for: '{}'. Matches found: {}", searchName, toJson(matchedEmployees));
        return matchedEmployees;
    }

    /**
     * Fetches an employee by their unique ID from the external API.
     *
     * @param id the unique ID of the employee
     * @return the employee with the specified ID
     */
    @Retry(name = "employeeApiRetry")
    public Employee getEmployeeById(String id) {
        log.info("Fetching employee by ID: {}", id);
        Employee employee;
        try {
            employee = employeeApiClient
                    .get()
                    .uri("/{id}", id)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Employee>>() {})
                    .map(ApiResponse::getData)
                    .block();
        } catch (WebClientResponseException ex) {
            throw errorHandler.handleException(ex);
        }
        log.debug("Successfully fetched employee: {}", toJson(employee));
        return employee;
    }

    /**
     * Calculates the highest salary among all employees.
     *
     * @return the highest salary, or 0 if no employees exist
     */
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

    /**
     * Retrieves the names of the top 10 highest earning employees.
     *
     * @return a list of names of the top 10 highest earning employees
     */
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

    /**
     * Creates a new employee via the external API.
     *
     * @param request the employee data to create
     * @return the created employee
     */
    @Retry(name = "employeeApiRetry")
    public Employee createEmployee(EmployeeDto request) {
        log.info("Creating new employee with name: {}", request.getName());
        Employee employee;
        try {
            employee = employeeApiClient
                    .post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Employee>>() {})
                    .map(ApiResponse::getData)
                    .block();
        } catch (WebClientResponseException ex) {
            throw errorHandler.handleException(ex);
        }
        log.debug("Successfully created employee: {}", toJson(employee));
        return employee;
    }

    /**
     * Deletes an employee by their unique ID via the external API.
     *
     * @param id the unique ID of the employee to delete
     * @return the name of the deleted employee
     */
    @Retry(name = "employeeApiRetry")
    public String deleteEmployeeById(String id) {
        log.info("Deleting employee with ID: {}", id);

        // Find employee name to delete
        Employee emp = getEmployeeById(id); // will throw if not found

        // Build delete request
        EmployeeDto input = new EmployeeDto();
        input.setId(id);
        input.setName(emp.getName());

        try {
            employeeApiClient
                    .method(HttpMethod.DELETE)
                    .bodyValue(input)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw errorHandler.handleException(ex);
        }
        log.debug("Successfully deleted employee with ID: {}", id);
        return emp.getName();
    }
}
