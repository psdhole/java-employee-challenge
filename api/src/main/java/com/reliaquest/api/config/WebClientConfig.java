// WebClientConfig.java
package com.reliaquest.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for setting up WebClient and Resilience4j Retry.
 */
@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${employee.api.base-url}")
    private String employeeApiBaseUrl;

    /**
     * Configures a WebClient bean for interacting with the employee API.
     *
     * @param webClientBuilder the WebClient.Builder to use for building the WebClient
     * @return the configured WebClient
     */
    @Bean
    public WebClient employeeApiClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(employeeApiBaseUrl).build();
    }
}
