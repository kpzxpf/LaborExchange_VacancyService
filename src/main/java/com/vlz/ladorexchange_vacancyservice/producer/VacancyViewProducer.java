package com.vlz.ladorexchange_vacancyservice.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vlz.ladorexchange_vacancyservice.dto.VacancyViewEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VacancyViewProducer extends AbstractProducer<VacancyViewEvent> {

    @Value("${spring.kafka.topics.vacancy-views}")
    private String topic;

    public VacancyViewProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper);
    }

    public void send(VacancyViewEvent event) {
        super.sendMessage(topic, event);
        log.debug("Sent vacancy view event: vacancyId={}, viewerId={}", event.getVacancyId(), event.getViewerId());
    }
}
