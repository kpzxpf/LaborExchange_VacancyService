package com.vlz.ladorexchange_vacancyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCaching
@EnableFeignClients
@SpringBootApplication
public class LaborExchangeVacancyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaborExchangeVacancyServiceApplication.class, args);
    }

}
