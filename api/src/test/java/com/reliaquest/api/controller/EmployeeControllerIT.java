package com.reliaquest.api.controller;

import io.github.resilience4j.retry.RetryRegistry;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for EmployeeController.
 * Uses MockWebServer to simulate the external employee service.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EmployeeControllerIT {
    @Autowired
    private WebTestClient webTestClient;

    private static MockWebServer baseServiceMock;

    @BeforeAll
    static void startMockServer() throws IOException {
        baseServiceMock = new MockWebServer();
        baseServiceMock.start();
    }

    @AfterAll
    static void shutdownMockServer() throws IOException {
        if (baseServiceMock != null) {
            baseServiceMock.shutdown();
        }
    }

    @DynamicPropertySource
    static void dynamicProps(DynamicPropertyRegistry registry) {
        registry.add(
                "employee.api.base-url",
                () -> baseServiceMock.url("/api/v1/employee").toString());
    }

    // Test retrieving all employees
    @Test
    void testGetAllEmployees() throws Exception {
        String responseBody =
                """
                    {
                      "status": "SUCCESS",
                      "data": [
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e1", "employee_name": "John", "employee_salary": 5000, "employee_age": 30, "employee_title": "Developer", "employee_email": "john@example.com" },
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e2", "employee_name": "Jane", "employee_salary": 8000, "employee_age": 35, "employee_title": "Manager", "employee_email": "jane@example.com" },
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e3", "employee_name": "Ray", "employee_salary": 10000, "employee_age": 25, "employee_title": "Tester", "employee_email": "ray@example.com" }
                      ]
                    }
                    """;
        baseServiceMock.enqueue(new MockResponse().setBody(responseBody).addHeader("Content-Type", "application/json"));
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
                .isEqualTo(3)
                .jsonPath("$.data[0].name")
                .isEqualTo("John");
    }

    // Test retrieving an employee by ID
    @Test
    void testGetEmployeeByID() {
        String responseBody =
                """
                    {
                      "status": "SUCCESS",
                      "data":  { "id": "3683dab9-7432-4e24-941b-6b466d8f54e1", "employee_name": "John", "employee_salary": 5000, "employee_age": 30, "employee_title": "Developer", "employee_email": "john@example.com" }
                    }
                    """;
        baseServiceMock.enqueue(new MockResponse().setBody(responseBody).addHeader("Content-Type", "application/json"));
        webTestClient
                .get()
                .uri("/3683dab9-7432-4e24-941b-6b466d8f54e1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isNotEmpty()
                .jsonPath("$.data.name")
                .isEqualTo("John");
    }

    // Test creating a new employee
    @Test
    void testCreateEmployee() throws Exception {
        String requestJson = "{\"name\":\"John\",\"salary\":5000,\"age\":30,\"title\":\"Developer\"}";
        String responseBody =
                """
                    {
                      "status": "SUCCESS",
                      "data": { "id": "3683dab9-7432-4e24-941b-6b466d8f54e1", "employee_name": "John", "employee_salary": 5000, "employee_age": 30, "employee_title": "Developer", "employee_email": "john@example.com" }
                    }
                    """;
        baseServiceMock.enqueue(new MockResponse().setBody(responseBody).addHeader("Content-Type", "application/json"));
        webTestClient
                .post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestJson)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isNotEmpty()
                .jsonPath("$.data.name")
                .isEqualTo("John");
    }

    // Test deleting an employee by ID
    @Test
    void testDeleteEmployeeById() {
        String getResponseBody =
                """
                    {
                      "status": "SUCCESS",
                      "data": { "id": "3683dab9-7432-4e24-941b-6b466d8f54e1", "employee_name": "John", "employee_salary": 5000, "employee_age": 30, "employee_title": "Developer", "employee_email": "john@example.com" }
                    }
                     """;
        String deleteResponseBody =
                """
                    {
                      "status": "SUCCESS",
                      "data": true
                    }
                    """;
        baseServiceMock.enqueue(new MockResponse()
                .setBody(getResponseBody)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        baseServiceMock.enqueue(new MockResponse()
                .setBody(deleteResponseBody)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        webTestClient
                .delete()
                .uri("/3683dab9-7432-4e24-941b-6b466d8f54e1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isNotEmpty()
                .jsonPath("$.data")
                .isEqualTo("John");
    }

    // Test retrieving the highest salary
    @Test
    void testGetHighestSalary() {
        String responseBody =
                """
                    {
                      "status": "SUCCESS",
                      "data": [
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e1", "employee_name": "John", "employee_salary": 5000, "employee_age": 30, "employee_title": "Developer", "employee_email": "john@example.com" },
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e2", "employee_name": "Jane", "employee_salary": 8000, "employee_age": 35, "employee_title": "Manager", "employee_email": "jane@example.com" },
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e3", "employee_name": "Ray", "employee_salary": 10000, "employee_age": 25, "employee_title": "Tester", "employee_email": "ray@example.com" }
                      ]
                    }
                    """;

        baseServiceMock.enqueue(new MockResponse().setBody(responseBody).addHeader("Content-Type", "application/json"));

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
                .isEqualTo(10000);
    }

    // Test retrieving top ten highest earning employee names
    @Test
    void testGetTopTenHighestEarningEmployeeNames_Success() throws Exception {
        String responseBody =
                """
                    {
                      "status": "SUCCESS",
                      "data": [
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e1", "employee_name": "John", "employee_salary": 5000, "employee_age": 30, "employee_title": "Developer", "employee_email": "john@example.com" },
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e2", "employee_name": "Jane", "employee_salary": 8000, "employee_age": 35, "employee_title": "Manager", "employee_email": "jane@example.com" },
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e3", "employee_name": "Ray", "employee_salary": 10000, "employee_age": 25, "employee_title": "Tester", "employee_email": "ray@example.com" }
                      ]
                    }
                    """;
        baseServiceMock.enqueue(new MockResponse().setBody(responseBody).addHeader("Content-Type", "application/json"));
        webTestClient
                .get()
                .uri("/topTenHighestEarningEmployeeNames")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.data")
                .isArray()
                .jsonPath("$.data.length()")
                .isEqualTo(3)
                .jsonPath("$.data[0]")
                .isEqualTo("Ray");
    }

    // Test searching for employees by name
    @Test
    void testGetEmployeesByNameSearch() throws Exception {
        String responseBody =
                """
                    {
                      "status": "SUCCESS",
                      "data": [
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e1", "employee_name": "John", "employee_salary": 5000, "employee_age": 30, "employee_title": "Developer", "employee_email": "john@example.com" },
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e2", "employee_name": "Jane", "employee_salary": 8000, "employee_age": 35, "employee_title": "Manager", "employee_email": "jane@example.com" },
                        { "id": "3683dab9-7432-4e24-941b-6b466d8f54e3", "employee_name": "Ray", "employee_salary": 10000, "employee_age": 25, "employee_title": "Tester", "employee_email": "ray@example.com" }
                      ]
                    }
                    """;
        baseServiceMock.enqueue(new MockResponse().setBody(responseBody).addHeader("Content-Type", "application/json"));
        webTestClient
                .get()
                .uri("/search/john")
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
                .jsonPath("$.data[0].name")
                .isEqualTo("John");
    }

    // Simulate 404 Not Found responses and verify retry ignore logic
    @Test
    void testGetEmployeeByID_NotFound_IgnoreWithRetry() {
        baseServiceMock.enqueue(new MockResponse()
                .setBody("{\"status\":\"FAILURE\",\"error\":\"Employee with given ID not found\"}")
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json"));
        webTestClient
                .get()
                .uri("/3683dab9-7432-4e24-941b-6b466d8f54e1")
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("FAILURE")
                .jsonPath("$.error")
                .isEqualTo("Employee with given ID not found");
    }

    // Simulate 429 Too Many Requests responses and verify retry logic
    @Test
    void testGetEmployeeByID_TooManyRequests_WithRetry() {
        baseServiceMock.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("{\"status\":\"FAILURE\",\"error\":\"Too many requests\"}")
                .addHeader("Content-Type", "application/json"));
        baseServiceMock.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("{\"status\":\"FAILURE\",\"error\":\"Too many requests\"}")
                .addHeader("Content-Type", "application/json"));
        baseServiceMock.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("{\"status\":\"FAILURE\",\"error\":\"Too many requests\"}")
                .addHeader("Content-Type", "application/json"));
        webTestClient
                .get()
                .uri("/3683dab9-7432-4e24-941b-6b466d8f54e1")
                .exchange()
                .expectStatus()
                .isEqualTo(429)
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("FAILURE")
                .jsonPath("$.error")
                .isEqualTo("Too many requests to upstream service");
    }

    // Simulate 500 Internal Server Error responses and verify retry logic
    @Test
    void testGetEmployeeByID_InternalServerError_WithRetry() {
        baseServiceMock.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"status\":\"FAILURE\",\"error\":\"Internal server error\"}")
                .addHeader("Content-Type", "application/json"));
        baseServiceMock.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"status\":\"FAILURE\",\"error\":\"Internal server error\"}")
                .addHeader("Content-Type", "application/json"));
        baseServiceMock.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"status\":\"FAILURE\",\"error\":\"Internal server error\"}")
                .addHeader("Content-Type", "application/json"));
        webTestClient
                .get()
                .uri("/3683dab9-7432-4e24-941b-6b466d8f54e1")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("FAILURE")
                .jsonPath("$.error")
                .isEqualTo("Upstream employee service api unavailable");
    }
}
