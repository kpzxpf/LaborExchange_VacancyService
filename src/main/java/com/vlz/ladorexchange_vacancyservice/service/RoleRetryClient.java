package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.client.RoleServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleRetryClient {

    private final RoleServiceClient roleServiceClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserRoleByIdFallback")
    @Retryable(
            retryFor = { Exception.class },
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${spring.retry.delay}")
    )
    public String getUserRoleById(Long id) {
        log.info("Attempting to fetch role for user id: {}", id);
        return roleServiceClient.getUserRoleById(id);
    }

    public String getUserRoleByIdFallback(Long id, Exception e) {
        log.warn("UserService circuit breaker open for getUserRoleById, userId={}: {}", id, e.getMessage());
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Role service is currently unavailable. Please try again later.",
                e
        );
    }

    @Recover
    public String recover(Exception e, Long id) {
        log.error("All retry attempts failed for user id: {}. Service is unavailable.", id);

        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Role service is currently unavailable. Please try again later.",
                e
        );
    }
}