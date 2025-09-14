package com.reliaquest.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exceptions.InvalidInputException;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
/**
 * Utility class for common operations.
 */
@Component
@Slf4j
public class CommonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Validates that the provided ID is a non-empty valid UUID.
     *
     * @param id the ID to validate
     * @throws InvalidInputException if the ID is empty or not a valid UUID
     */
    public static void validateID(String id) {
        try {
            UUID.fromString(Optional.ofNullable(id)
                    .orElseThrow(() -> new InvalidInputException("Employee ID must not be empty")));
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Employee ID must be a valid UUID");
        }
    }

    /**
     * Converts an object to its JSON string representation.
     *
     * @param obj the object to convert
     * @return the JSON string representation of the object, or the object's toString() if serialization fails
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON serialization failed for: {}", obj, e);
            return String.valueOf(obj);
        }
    }
}
