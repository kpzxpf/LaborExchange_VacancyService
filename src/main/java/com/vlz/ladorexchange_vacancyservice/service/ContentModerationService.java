package com.vlz.ladorexchange_vacancyservice.service;

import com.vlz.ladorexchange_vacancyservice.exception.ContentViolationException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ContentModerationService {

    private final List<String> bannedPhrases = new ArrayList<>();

    @PostConstruct
    void loadBannedWords() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("content/banned-words.txt").getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(String::toLowerCase)
                    .forEach(bannedPhrases::add);
            log.info("Loaded {} banned phrases", bannedPhrases.size());
        } catch (Exception e) {
            log.error("Failed to load banned-words.txt", e);
        }
    }

    public void check(String... texts) {
        for (String text : texts) {
            if (text == null) continue;
            String lower = text.toLowerCase();
            for (String banned : bannedPhrases) {
                if (lower.contains(banned)) {
                    log.warn("Content violation: banned phrase '{}' detected", banned);
                    throw new ContentViolationException(
                            "Content contains prohibited material. Please review and resubmit.");
                }
            }
        }
    }
}
