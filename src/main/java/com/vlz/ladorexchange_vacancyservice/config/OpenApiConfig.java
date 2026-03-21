package com.vlz.ladorexchange_vacancyservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vacancy Service API")
                        .version("1.0.0")
                        .description("""
                                Manages job vacancies and employer company profiles.

                                **Write operations** on `/api/vacancies` (POST, PUT, DELETE, PATCH) \
require the `EMPLOYER` role — enforced by API Gateway.

                                **Headers injected by API Gateway:**
                                - `X-User-Id` — authenticated user ID
                                - `X-User-Role` — `EMPLOYER` or `JOB_SEEKER`

                                **Elasticsearch indexing:** create / update / publish automatically \
publishes a `VacancyIndexEvent` to Kafka topic `indexing-vacancy`.

                                **Caching:** Vacancy and company lookups cached in Redis.

                                **Database:** PostgreSQL (`vacancydb`).
                                """)
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained via POST /api/auth/login")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Direct"),
                        new Server().url("http://localhost:8080").description("Via API Gateway")))
                .tags(List.of(
                        new Tag().name("Vacancies").description("Job vacancy CRUD, skill management, and Elasticsearch indexing"),
                        new Tag().name("Companies").description("Employer company profiles")));
    }
}
