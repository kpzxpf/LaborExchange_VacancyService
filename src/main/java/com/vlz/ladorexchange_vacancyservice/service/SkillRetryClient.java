package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.client.SkillServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillRetryClient {
    private final SkillServiceClient skillServiceClient;

    @CircuitBreaker(name = "skillService", fallbackMethod = "getNameSkillsByIdsFallback")
    @Retryable(
            retryFor = { Exception.class },
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${spring.retry.delay}")
    )
    public Set<String> getNameSkillsByIds(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Collections.emptySet();
        }
        log.info("Fetching skill names for skillIds: {}", skillIds);

        return new HashSet<>(skillServiceClient.findSkillNamesByIds(skillIds));
    }

    public Set<String> getNameSkillsByIdsFallback(List<Long> skillIds, Exception e) {
        log.warn("SkillService circuit breaker open for getNameSkillsByIds: {}", e.getMessage());
        return Collections.emptySet();
    }

    @CircuitBreaker(name = "skillService", fallbackMethod = "getSkillMapByIdsFallback")
    @Retryable(
            retryFor = { Exception.class },
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${spring.retry.delay}")
    )
    public Map<Long, String> getSkillMapByIds(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Collections.emptyMap();
        }
        log.info("Fetching skill map for skillIds: {}", skillIds);
        return skillServiceClient.findSkillMapByIds(skillIds);
    }

    public Map<Long, String> getSkillMapByIdsFallback(List<Long> skillIds, Exception e) {
        log.warn("SkillService circuit breaker open for getSkillMapByIds: {}", e.getMessage());
        return Collections.emptyMap();
    }

    @Recover
    public Set<String> recover(Exception e, List<Long> skillIds) {
        log.error("All retry attempts failed for skillIds: {}. Service is unavailable.", skillIds);

        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Skill service is currently unavailable. Please try again later.",
                e
        );
    }
}
