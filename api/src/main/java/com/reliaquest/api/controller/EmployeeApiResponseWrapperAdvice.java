package com.reliaquest.api.controller;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.util.CommonUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.*;

/**
 * Advice to wrap API responses in a standard ApiResponse structure.
 * This ensures consistent response formats across the application.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@ControllerAdvice
public class EmployeeApiResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> type = returnType.getParameterType();
        // Only wrap if return type is NOT already ApiResponse or String
        return !ApiResponse.class.isAssignableFrom(type) && !String.class.isAssignableFrom(type);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // Null body -> wrap as empty list
        if (body == null) {
            return new ApiResponse<>(List.of());
        }

        // Already wrapped response -> return as-is
        if (body instanceof ApiResponse) {
            return body;
        }

        // Special handling for String return type
        if (body instanceof String) {
                return CommonUtil.toJson(new ApiResponse<>(body));
        }

        // Wrap normal responses
        return new ApiResponse<>(body);
    }
}