// ApiResponse.java
package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper.
 *
 * @param <T> the type of the data being returned
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T data;
    private String status;
    private String error;

    /** Constructor for successful responses.
     *
     * @param data the data to be returned
     */
    public ApiResponse(T data) {
        this.data = data;
        this.status = "SUCCESS";
        this.error = null;
    }

    /** Constructor for error responses.
     *
     * @param error the error message
     */
    public ApiResponse(String error) {
        this.data = null;
        this.status = "FAILURE";
        this.error = error;
    }
}
