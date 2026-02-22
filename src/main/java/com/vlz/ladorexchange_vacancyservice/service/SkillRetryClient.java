package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.client.SkillServiceClient;
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
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillRetryClient {
    private final SkillServiceClient skillServiceClient;

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

    @Recover
    public String recover(Exception e, Long id) {
        log.error("All retry attempts failed for user id: {}. Service is unavailable.", id);

        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Skill service is currently unavailable. Please try again later.",
                e
        );
    }
}
