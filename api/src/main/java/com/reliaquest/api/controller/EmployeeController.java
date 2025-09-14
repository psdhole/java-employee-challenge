package com.reliaquest.api.controller;

import com.reliaquest.api.exceptions.InvalidInputException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeDto;
import com.reliaquest.api.service.EmployeeService;
import com.reliaquest.api.util.InputValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *  REST controller for managing Employee-related endpoints.
 */
@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, EmployeeDto> {

    private final EmployeeService service;
    private final InputValidator validator;
    /**
     * Retrieves all employees.
     *
     * @return a ResponseEntity containing a list of all employees
     */
    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Request received to fetch all employees");
        List<Employee> employees = service.getAllEmployees();
        log.info("Request processed - returning total {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    /**
     * Searches for employees by name.
     *
     * @param searchString the name or partial name to search for
     * @return a ResponseEntity containing a list of employees matching the search criteria
     * @throws InvalidInputException if the search string is null or empty
     */
    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable("searchString") String searchString) {
        log.info("Request received to search employees by name: {}", searchString);
        validator.validate(searchString);
        List<Employee> employees = service.searchEmployeesByName(searchString);
        log.info("Request processed - returning total {} employees by name: {}", employees.size(), searchString);
        return ResponseEntity.ok(employees);
    }

    /**
     * Retrieves an employee by their ID.
     *
     * @param id the ID of the employee to retrieve
     * @return a ResponseEntity containing the employee with the specified ID
     * @throws InvalidInputException if the ID is null or empty
     */
    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") String id) {
        log.info("Request received to get employee by ID: {}", id);
        validator.validateID(id);
        Employee employee = service.getEmployeeById(id);
        log.info("Request processed - returning employee by ID: {}", id);
        return ResponseEntity.ok(employee);
    }

    /**
     * Retrieves the highest salary among all employees.
     *
     * @return a ResponseEntity containing the highest salary
     */
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Request received to get highest salary of employees");
        Integer highestSalary = service.getHighestSalary();
        log.info("Request processed - returning highest salary of employees: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    /**
     * Retrieves the names of the top ten highest earning employees.
     *
     * @return a ResponseEntity containing a list of names of the top ten highest earning employees
     */
    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Request received to get top ten highest earning employee names");
        List<String> employees = service.getTopTenHighestEarningEmployeeNames();
        log.info("Request processed - returning total {} highest earning employee names", employees.size());
        return ResponseEntity.ok(employees);
    }

    /**
     * Creates a new employee.
     *
     * @param employeeInput the EmployeeDto object containing the details of the employee to create
     * @return a ResponseEntity containing the created employee
     * @throws InvalidInputException if the input data is invalid
     */
    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeDto employeeInput) {
        log.info("Request received to create a new employee: {}", employeeInput);
        validator.validate(employeeInput);
        Employee employee = service.createEmployee(employeeInput);
        log.info("Request processed - returning new employee with name: {}", employee.getName());
        return ResponseEntity.ok(employee);
    }

    /**
     * Deletes an employee by their ID.
     *
     * @param id the ID of the employee to delete
     * @return a ResponseEntity containing the name of the deleted employee
     * @throws InvalidInputException if the ID is null or empty
     */
    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable("id") String id) {
        log.info("Request received to delete employee by ID: {}", id);
        validator.validateID(id);
        String name = service.deleteEmployeeById(id);
        log.info("Request processed - deleted employee by ID: {}, with name {}", id, name);
        return ResponseEntity.ok(name);
    }
}
