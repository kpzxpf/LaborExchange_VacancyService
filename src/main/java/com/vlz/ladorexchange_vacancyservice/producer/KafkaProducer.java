package com.vlz.ladorexchange_vacancyservice.producer;

public interface KafkaProducer<T> {
    void send(T event);
}