package com.reliaquest.api.service;

import com.reliaquest.api.exceptions.ExternalServiceException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeDto;
import com.reliaquest.api.util.WebClientErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService using mocked WebClient.
 * This class uses Mockito to simulate WebClient interactions and test service logic.
 */
@SpringBootTest
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @MockBean
    private WebClient employeeApiClient;

    @MockBean
    private WebClientErrorHandler errorHandler;

    private Employee sampleEmployee;

    private final WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    private final WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    private final WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    private final WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);


    @BeforeEach
    void setUp() {
        sampleEmployee = Employee.builder()
                .id("95f2e8a4-49e9-4e21-b1e2-10075394e1bb")
                .name("John Doe")
                .age(30)
                .salary(50000)
                .build();
    }


    // Helper to mock WebClient GET calls returning ApiResponse
    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> void mockWebClientGet(T body) {
        when(employeeApiClient.get()).thenReturn(requestHeadersUriSpec);

        // Add URI mock
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
                .thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(new ApiResponse<>(body)));
    }

    // Helper to mock WebClient DELETE calls returning Void
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void mockWebClientDelete() {
        // Cast to RequestBodyUriSpec for bodyValue()
        when(employeeApiClient.method(any(HttpMethod.class)))
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
    }

    // Helper to mock WebClient POST calls returning ApiResponse
    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> void mockWebClientPost(T body) {
        when(employeeApiClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(new ApiResponse<>(body)));
    }



    // Test fetching all employees
    @Test
    void testGetAllEmployees() {
        mockWebClientGet(List.of(sampleEmployee));
        List<Employee> employees = employeeService.getAllEmployees();
        assertEquals(1, employees.size());
        assertEquals("John Doe", employees.get(0).getName());
    }

    // Test searching employees by name
    @Test
    void testGetEmployeeById() {
        mockWebClientGet(sampleEmployee);
        Employee emp = employeeService.getEmployeeById("1");
        assertEquals("John Doe", emp.getName());
    }


    // Test getting highest salary
    @Test
    void testGetHighestSalary() {
        mockWebClientGet(List.of(
                sampleEmployee,
                Employee.builder().id("2").name("Jane").salary(60000).age(28).build()
        ));
        Integer highest = employeeService.getHighestSalary();
        assertEquals(60000, highest);
    }

    // Test getting top ten highest earning employee names
    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        mockWebClientGet(List.of(
                sampleEmployee,
                Employee.builder().id("2").name("Jane").salary(60000).age(28).build(),
                Employee.builder().id("3").name("Bob").salary(55000).age(25).build()
        ));
        List<String> topEarners = employeeService.getTopTenHighestEarningEmployeeNames();
        assertEquals(List.of("Jane", "Bob", "John Doe"), topEarners);
    }


    // Test creating a new employee
    @Test
    void testCreateEmployee() {
        mockWebClientPost(Employee.builder().id("4").name("Alice").salary(45000).age(27).build());
        Employee emp = employeeService.createEmployee(
                new EmployeeDto("1","Alice", 45000, 27, null, null)
        );
        assertEquals("Alice", emp.getName());
    }


    // Test creating employee failure handling
    @Test
    void testCreateEmployee_Failure() {
        //simulate WebClient POST failure
        when(employeeApiClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // WebClient call throws a 500
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenThrow(new WebClientResponseException(500, "Create failed", null, null, null));

        // Error handler maps it to ExternalServiceException
        when(errorHandler.handleException(any(WebClientResponseException.class)))
                .thenThrow(new ExternalServiceException("Create failed",null));


        ExternalServiceException ex = assertThrows(
                ExternalServiceException.class,
                () -> employeeService.createEmployee(new EmployeeDto("1", "Alice", 45000, 27, null, null))
        );

        assertEquals("Create failed", ex.getMessage());

        // Verify error handler was called
        verify(errorHandler, atLeastOnce()).handleException(any(WebClientResponseException.class));
    }

    // Test deleting an employee by ID
    @Test
    void testDeleteEmployeeById() {
        mockWebClientGet(sampleEmployee);
        mockWebClientDelete();
        String name = employeeService.deleteEmployeeById("1");
        assertEquals("John Doe", name);
    }


    // Test deleting an employee by ID failure
    @Test
    void testDeleteEmployeeById_Failure() {
        //  successful lookup first
        mockWebClientGet(sampleEmployee);

        // simulate delete failure
        when(employeeApiClient.method(any(HttpMethod.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class))
                .thenThrow(new WebClientResponseException(500, "Delete failed", null, null, null));

        when(errorHandler.handleException(any(WebClientResponseException.class)))
                .thenThrow(new ExternalServiceException("Delete failed",null));

        ExternalServiceException ex = assertThrows(
                ExternalServiceException.class,
                () -> employeeService.deleteEmployeeById("1")
        );

        assertEquals("Delete failed", ex.getMessage());

        //  verify error handler was called
        verify(errorHandler, atLeastOnce()).handleException(any(WebClientResponseException.class));
    }


    // Test retry logic on getAllEmployees
    @Test
    void testGetAllEmployees_RetryOnFailure() {
        ApiResponse<List<Employee>> response = new ApiResponse<>(List.of(sampleEmployee));

        when(employeeApiClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenThrow(new WebClientResponseException(500, "Internal Error", null, null, null))
                .thenThrow(new WebClientResponseException(500, "Internal Error", null, null, null))
                .thenReturn(Mono.just(response));

        when(errorHandler.handleException(any()))
                .thenAnswer(invocation -> {
                    throw (Throwable) invocation.getArgument(0);
                });

        List<Employee> employees = employeeService.getAllEmployees();
        assertEquals(1, employees.size());

        //  verify error handler was called
        verify(responseSpec, times(3)).bodyToMono(any(ParameterizedTypeReference.class));
    }

    // Test retry logic on getEmployeeById with all failures
    @Test
    void testGetEmployeeById_RetryAndFail() {
        WebClientResponseException ex = new WebClientResponseException(500, "Internal Error", null, null, null);

        // Simulate 3 failures
        when(employeeApiClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{id}", "1")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenThrow(ex);
        when(errorHandler.handleException(ex)).thenThrow(new RuntimeException("Upstream service failed"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> employeeService.getEmployeeById("1"));
        assertEquals("Upstream service failed", thrown.getMessage());

        //  verify error handler was called
        verify(responseSpec, times(3)).bodyToMono(any(ParameterizedTypeReference.class));
    }

    // Edge case: empty employee list
    @Test
    void testGetAllEmployees_EmptyList() {
        mockWebClientGet(Collections.emptyList());
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.isEmpty());
    }

    // Edge case: search with no matches
    @Test
    void testSearchEmployeesByName_NoMatch() {
        mockWebClientGet(List.of(sampleEmployee));
        List<Employee> result = employeeService.searchEmployeesByName("XYZ");
        assertTrue(result.isEmpty());
    }
}