package com.reliaquest.api.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Employee.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto {
    private String id;

    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotNull(message = "Salary is required") private Integer salary;

    @NotNull(message = "Age is required")
    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    private Integer age;

    @NotBlank(message = "Title must not be empty")
    private String title;

    private String email;
}
