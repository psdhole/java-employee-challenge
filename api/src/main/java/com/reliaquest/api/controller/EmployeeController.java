package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import com.reliaquest.api.util.CommonUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeRequest> {

    private final EmployeeService service;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Request received to fetch all employees");
        List<Employee> employees = service.getAllEmployees();
        log.info("Request processed - returning total {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("Request received to search employees by name: {}", searchString);
        List<Employee> employees = service.searchEmployeesByName(searchString);
        log.info("Request processed - returning total {} employees by name: {}", employees.size(), searchString);
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("Request received to get employee by ID: {}", id);
        CommonUtil.validateID(id);
        Employee employee = service.getEmployeeById(id);
        log.info("Request processed - returning employee by ID: {}", id);
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Request received to get highest salary of employees");
        Integer highestSalary = service.getHighestSalary();
        log.info("Request processed - returning highest salary of employees: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Request received to get top ten highest earning employee names");
        List<String> employees = service.getTopTenHighestEarningEmployeeNames();
        log.info("Request processed - returning total {} highest earning employee names", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeRequest employeeInput) {
        log.info("Request received to create a new employee: {}", employeeInput);
        Employee employee = service.createEmployee(employeeInput);
        log.debug("Request processed - returning new employee with name: {}", employee.getName());
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("Request received to delete employee by ID: {}", id);
        CommonUtil.validateID(id);
        String name = service.deleteEmployeeById(id);
        log.debug("Request processed - deleted employee by ID: {}, with name {}", id, name);
        return ResponseEntity.ok(name);
    }
}
