package com.vlz.ladorexchange_vacancyservice.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vlz.ladorexchange_vacancyservice.dto.VacancyIndexEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VacancyIndexProducer extends AbstractProducer<VacancyIndexEvent> {

    @Value("${spring.kafka.topics.indexing-vacancy}")
    private String vacancyIndexTopicName;

    public VacancyIndexProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper);
    }

    public void send(VacancyIndexEvent event) {
        super.sendMessage(vacancyIndexTopicName, event);
        log.info("Sending user registration event: {}", event);
    }
}
