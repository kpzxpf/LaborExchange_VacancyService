package com.vlz.ladorexchange_vacancyservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootTest
class LaborExchangeVacancyServiceApplicationTests {

    @MockBean
    RedisConnectionFactory redisConnectionFactory;

    @MockBean
    ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @Test
    void contextLoads() {
    }

}
