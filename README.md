# Vacancy Service

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.6-brightgreen?logo=springboot)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Port](https://img.shields.io/badge/port-8083-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-vacancydb-336791?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-cached-DC382D?logo=redis)
![Kafka](https://img.shields.io/badge/Kafka-indexing--vacancy-231F20?logo=apachekafka)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

Microservice for managing job vacancies and employer company profiles, with Elasticsearch indexing via Kafka.

## Table of Contents

- [Overview](#overview)
- [API Endpoints](#api-endpoints)
- [Data Models](#data-models)
- [Kafka Events](#kafka-events)
- [Authorization](#authorization)
- [Configuration](#configuration)
- [Running Locally](#running-locally)

## Overview

| Property | Value |
|---|---|
| Port | **8083** |
| Base paths | `/api/vacancies`, `/api/companies` |
| Database | PostgreSQL — `vacancydb` |
| Cache | Redis |
| Migrations | Flyway |
| Swagger UI | `http://localhost:8083/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8083/v3/api-docs` |
| Prometheus | `http://localhost:8083/actuator/prometheus` |

## API Endpoints

### Vacancies — `/api/vacancies`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/` | No | Get all published vacancies (paginated) |
| `GET` | `/{id}` | No | Get vacancy by ID |
| `GET` | `/employer/{userId}` | No | Get vacancies by employer |
| `GET` | `/{id}/skills` | No | Get skill IDs for vacancy |
| `GET` | `/{id}/company-name` | No | Get company name (internal) |
| `POST` | `/` | `EMPLOYER` | Create vacancy |
| `PUT` | `/{id}` | `EMPLOYER` | Update vacancy |
| `PATCH` | `/{id}/publish` | `EMPLOYER` | Publish vacancy |
| `PATCH` | `/{id}/unpublish` | `EMPLOYER` | Unpublish vacancy |
| `DELETE` | `/{id}` | `EMPLOYER` | Delete vacancy |
| `POST` | `/{id}/skills/{skillId}` | `EMPLOYER` | Add skill to vacancy |
| `DELETE` | `/{id}/skills/{skillId}` | `EMPLOYER` | Remove skill from vacancy |
| `PUT` | `/{id}/skills` | `EMPLOYER` | Replace all vacancy skills |
| `POST` | `/reindex` | No | Reindex all vacancies (maintenance) |

### Companies — `/api/companies`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/` | No | Get all companies |
| `GET` | `/{id}` | No | Get company by ID |
| `GET` | `/my` | JWT | Get authenticated employer's company |
| `GET` | `/employer/{employerId}` | No | Get company by employer ID |
| `POST` | `/` | JWT | Create a company |
| `PUT` | `/{id}` | JWT | Update a company |
| `DELETE` | `/{id}` | JWT | Delete a company |

## Data Models

### VacancyDto

| Field | Type | Constraints |
|---|---|---|
| `id` | Long | Auto-generated |
| `title` | String | 3–255 chars, required |
| `description` | String | Max 5000 chars, required |
| `companyName` | String | Required |
| `employerId` | Long | Required |
| `salary` | Double | ≥ 0 (0 = not specified) |
| `isPublished` | boolean | Default false |
| `createdAt` | LocalDateTime | Auto-set |

### CompanyDto

| Field | Type | Constraints |
|---|---|---|
| `id` | Long | Auto-generated |
| `employerId` | Long | One company per employer |
| `name` | String | 2–100 chars, required |
| `description` | String | Max 2000 chars |
| `location` | String | Required |
| `email` | String | Valid email, required |
| `phoneNumber` | String | Optional |
| `website` | String | Valid URL, optional |

## Kafka Events

| Topic | Event | Trigger |
|---|---|---|
| `indexing-vacancy` | `VacancyIndexEvent` | Create, update, publish vacancy |

`VacancyIndexEvent` carries: id, title, description, companyName, location, salary, skills (names), createdAt.

## Authorization

Write operations require `EMPLOYER` role. The API Gateway:
1. Validates the JWT token.
2. Injects `X-User-Id` and `X-User-Role` headers into the downstream request.
3. Blocks non-EMPLOYER users from POST/PUT/DELETE/PATCH endpoints.

## Configuration

| Property | Default | Description |
|---|---|---|
| `server.port` | `8083` | HTTP port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5435/vacancydb` | Database URL |
| `spring.data.redis.host` | `localhost` | Redis host |
| `spring.kafka.bootstrap-servers` | `localhost:9092` | Kafka brokers |

## Running Locally

```bash
./gradlew bootRun
```

Requires PostgreSQL, Redis, and Kafka running locally.
