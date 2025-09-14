// WebClientConfig.java
package com.reliaquest.api.config;

import io.github.resilience4j.retry.RetryRegistry;
import java.util.Objects;
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
        return webClientBuilder.filter((request, next) -> {
                    log.debug("Request: {} {}", request.method(), request.url());
                    return next.exchange(request).doOnNext(response -> {
                        log.debug("Response Status: {}", response.statusCode());
                    });
                })
                .baseUrl(employeeApiBaseUrl)
                .build();
    }

    /**
     * Configures a RetryRegistry with a retry instance for the employee API.
     *
     * @return the configured RetryRegistry
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry registry = RetryRegistry.ofDefaults();
        registry.retry("employeeApiRetry")
                .getEventPublisher()
                .onRetry(event -> log.debug(
                        "Retrying attempt #{} due to {}",
                        event.getNumberOfRetryAttempts(),
                        Objects.requireNonNull(event.getLastThrowable()).getMessage()));
        return registry;
    }
}
