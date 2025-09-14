package com.reliaquest.api.controller;

import static org.mockito.Mockito.when;

import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.exceptions.ExternalServiceException;
import com.reliaquest.api.exceptions.TooManyRequestsException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeDto;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test class for EmployeeController.
 * This class uses WebTestClient to perform integration tests on the EmployeeController endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private EmployeeService employeeService;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleEmployee = Employee.builder()
                .id("3683dab9-7432-4e24-941b-6b466d8f54e1")
                .name("John Doe")
                .salary(50000)
                .age(30)
                .build();
    }

    // Test for getting all employees
    @Test
    void testGetAllEmployees() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.singletonList(sampleEmployee));
        webTestClient
                .get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isArray()
                .jsonPath("$.data.length()")
                .isEqualTo(1)
                .jsonPath("$.data[0].id")
                .isEqualTo(sampleEmployee.getId())
                .jsonPath("$.data[0].name")
                .isEqualTo(sampleEmployee.getName())
                .jsonPath("$.data[0].salary")
                .isEqualTo(sampleEmployee.getSalary())
                .jsonPath("$.data[0].age")
                .isEqualTo(sampleEmployee.getAge());
    }

    // Test for getting all employees when the list is empty
    @Test
    void testGetAllEmployees_Empty() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());
        webTestClient
                .get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isArray()
                .jsonPath("$.data.length()")
                .isEqualTo(0);
    }

    // Test for searching employees by name
    @Test
    void testGetEmployeesByNameSearch_Success() {
        String search = "John";
        when(employeeService.searchEmployeesByName(search)).thenReturn(Collections.singletonList(sampleEmployee));
        webTestClient
                .get()
                .uri("/search/{searchString}", search)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isArray()
                .jsonPath("$.data.length()")
                .isEqualTo(1)
                .jsonPath("$.data[0].id")
                .isEqualTo(sampleEmployee.getId())
                .jsonPath("$.data[0].name")
                .isEqualTo(sampleEmployee.getName())
                .jsonPath("$.data[0].salary")
                .isEqualTo(sampleEmployee.getSalary())
                .jsonPath("$.data[0].age")
                .isEqualTo(sampleEmployee.getAge());
    }

    // Test for searching employees by name with validation error
    @Test
    void testGetEmployeesByNameSearch_Validation() throws Exception {
        String search = " ";
        when(employeeService.searchEmployeesByName(search)).thenReturn(Collections.singletonList(sampleEmployee));
        webTestClient
                .get()
                .uri("/search/{searchString}", search)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("FAILURE")
                .jsonPath("$.error")
                .isNotEmpty()
                .jsonPath("$.error")
                .toString()
                .contains("Search string must not be empty");
    }

    // Test for searching employee by ID
    @Test
    void testGetEmployeeById_Success() {
        String id = sampleEmployee.getId();
        when(employeeService.getEmployeeById(id)).thenReturn(sampleEmployee);
        webTestClient
                .get()
                .uri("/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isNotEmpty()
                .jsonPath("$.data.id")
                .isEqualTo(sampleEmployee.getId())
                .jsonPath("$.data.name")
                .isEqualTo(sampleEmployee.getName())
                .jsonPath("$.data.salary")
                .isEqualTo(sampleEmployee.getSalary())
                .jsonPath("$.data.age")
                .isEqualTo(sampleEmployee.getAge());
    }

    // Test for searching employee by ID with validation error
    @Test
    void testGetEmployeeById_Validations() throws Exception {
        String id = "XX";
        webTestClient
                .get()
                .uri("/{id}", id)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("FAILURE")
                .jsonPath("$.error")
                .isNotEmpty()
                .jsonPath("$.error")
                .toString()
                .contains("Employee ID must be a valid UUID");
    }

    // Test for getting the highest salary among employees
    @Test
    void testGetHighestSalaryOfEmployees() {
        when(employeeService.getHighestSalary()).thenReturn(100000);
        webTestClient
                .get()
                .uri("/highestSalary")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isNotEmpty()
                .jsonPath("$.data")
                .isEqualTo(100000);
    }

    // Test for getting the top ten highest earning employee names
    @Test
    void testGetTopTenHighestEarningEmployeeNames_Success() {
        List<String> topEarners = Arrays.asList("John", "Jane", "Bob");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEarners);
        webTestClient
                .get()
                .uri("/topTenHighestEarningEmployeeNames")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.data")
                .isArray()
                .jsonPath("$.data[0]")
                .isEqualTo("John")
                .jsonPath("$.data[1]")
                .isEqualTo("Jane")
                .jsonPath("$.data[2]")
                .isEqualTo("Bob")
                .jsonPath("$.status")
                .isEqualTo("SUCCESS");
    }

    // Test for creating a new employee
    @Test
    void testCreateEmployee_Success() {
        EmployeeDto input = EmployeeDto.builder()
                .name("Alice")
                .age(28)
                .salary(40000)
                .title("Developer")
                .build();
        Employee created = Employee.builder()
                .id("3")
                .name("Alice")
                .age(28)
                .salary(40000)
                .title("Developer")
                .build();
        when(employeeService.createEmployee(input)).thenReturn(created);
        webTestClient
                .post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isNotEmpty()
                .jsonPath("$.data.id")
                .isEqualTo(created.getId())
                .jsonPath("$.data.name")
                .isEqualTo(created.getName())
                .jsonPath("$.data.salary")
                .isEqualTo(created.getSalary())
                .jsonPath("$.data.age")
                .isEqualTo(created.getAge());
    }

    @Test
    void testCreateEmployee_Validation() throws Exception {
        EmployeeDto input =
                EmployeeDto.builder().age(28).salary(40000).title("Developer").build();
        webTestClient
                .post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("FAILURE")
                .jsonPath("$.error")
                .isNotEmpty()
                .jsonPath("$.error")
                .toString()
                .contains("Invalid input");
    }

    // Test for deleting an employee by ID
    @Test
    void testDeleteEmployeeById_Success() {
        String id = sampleEmployee.getId();
        when(employeeService.deleteEmployeeById(id)).thenReturn(sampleEmployee.getName());
        webTestClient
                .delete()
                .uri("/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isNotEmpty()
                .jsonPath("$.data")
                .isEqualTo(sampleEmployee.getName());
    }

    // Test for searching employees by name with invalid input
    @Test
    void testGetEmployeesByNameSearch_InvalidInput() {
        String search = "  ";
        webTestClient
                .get()
                .uri("/search/{searchString}", search)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    // Test for getting an employee by ID when the employee is not found
    @Test
    void testGetEmployeeById_NotFound() {
        String id = sampleEmployee.getId();
        when(employeeService.getEmployeeById(id)).thenThrow(new EmployeeNotFoundException("Employee not found"));
        webTestClient.get().uri("/{id}", id).exchange().expectStatus().isNotFound();
    }

    // Test for deleting an employee by ID when the employee is not found
    @Test
    void testDeleteEmployeeById_NotFound() {
        String id = sampleEmployee.getId();
        when(employeeService.deleteEmployeeById(id)).thenThrow(new EmployeeNotFoundException("Employee not found"));
        webTestClient.delete().uri("/{id}", id).exchange().expectStatus().isNotFound();
    }

    // Test for getting an employee by ID when bad gateway error
    @Test
    void testGetEmployeeById_BadGateway() {
        String id = sampleEmployee.getId();
        when(employeeService.getEmployeeById(id))
                .thenThrow(new ExternalServiceException("502 - Service Unavailable", null));
        webTestClient
                .get()
                .uri("/{id}", id)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectStatus()
                .isEqualTo(502);
    }

    // Test for getting an employee by ID when internal server error
    @Test
    void testGetEmployeeById_InternalServerError() {
        String id = sampleEmployee.getId();
        when(employeeService.getEmployeeById(id)).thenThrow(new RuntimeException("500 - Service Unavailable", null));
        webTestClient
                .get()
                .uri("/{id}", id)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectStatus()
                .isEqualTo(500);
    }

    // Test for getting an employee by ID when too many requests are made
    @Test
    void testGetEmployeeById_TooManyRequests() {
        String id = sampleEmployee.getId();
        when(employeeService.getEmployeeById(id)).thenThrow(new TooManyRequestsException("429 - TooManyRequests"));
        webTestClient
                .get()
                .uri("/{id}", id)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectStatus()
                .isEqualTo(429);
    }
}
