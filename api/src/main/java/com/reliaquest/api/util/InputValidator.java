package com.reliaquest.api.util;

import com.reliaquest.api.exceptions.InvalidInputException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Utility class for validating input data.
 *
 * <p>Cannot use @Valid at the controller layer due to the
 * {@link com.reliaquest.api.controller.IEmployeeController} interface contract.
 * This helper provides programmatic validation for request parameters or DTOs
 * before they are processed by the service layer.
 */
@Component
public class InputValidator {
    private final jakarta.validation.Validator validator;

    // Precompiled regex for UUID validation
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");

    /**
     * Constructs an InputValidator with the given Jakarta Validator.
     *
     * @param validator the Jakarta {@link Validator} used to perform
     *                  constraint validations on input objects
     */
    public InputValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validates that the given ID is a non-blank string and matches the UUID format.
     *
     * @param id the ID to validate
     * @param <T> the type of the ID (should be String)
     * @throws InvalidInputException if the ID is blank or not a valid UUID
     */
    public <T> void validateID(String id) {
        if (id.isBlank()) {
            throw new InvalidInputException("Employee ID must be empty");
        }
        if (!UUID_PATTERN.matcher(id).matches()) {
            throw new InvalidInputException("Employee ID must be a valid UUID");
        }
    }

    /**
     * Validates the given target object.
     *
     * <p>If the target is null, an {@link InvalidInputException} is thrown.
     * If the target is a String and is blank, an {@link InvalidInputException} is thrown.
     * If the target has any constraint violations, an {@link InvalidInputException} is thrown
     * with details of the violations.
     *
     * @param target the object to validate
     * @param <T>    the type of the object to validate
     * @throws InvalidInputException if the target is null, a blank String, or has constraint violations
     */
    public <T> void validate(T target) {
        if (target == null) {
            throw new InvalidInputException("Input cannot be null");
        }

        if (target instanceof String str) {
            if (str.isBlank()) {
                throw new InvalidInputException("Search string not be empty");
            }
        }

        Set<ConstraintViolation<T>> violations = validator.validate(target);
        if (!violations.isEmpty()) {
            String errorDetails =
                    violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
            throw new InvalidInputException(errorDetails);
        }
    }
}
