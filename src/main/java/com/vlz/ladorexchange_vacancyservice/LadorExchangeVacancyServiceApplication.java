package com.vlz.ladorexchange_vacancyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class LadorExchangeVacancyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LadorExchangeVacancyServiceApplication.class, args);
    }

}
