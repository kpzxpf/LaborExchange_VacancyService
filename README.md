# Vacancy Service

Manages job vacancies and employer company profiles for LaborExchange.

## Overview

| Property | Value |
|---|---|
| Port | **8083** |
| Base paths | `/api/vacancies`, `/api/companies` |
| Database | PostgreSQL (`vacancydb`) |
| Cache | Redis (vacancies, companies) |
| Swagger UI | http://localhost:8083/swagger-ui.html |
| Prometheus metrics | http://localhost:8083/actuator/prometheus |

## API Endpoints

### Vacancies (`/api/vacancies`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/vacancies` | No | Get all published vacancies (paginated) |
| `GET` | `/api/vacancies/{id}` | No | Get vacancy by ID |
| `POST` | `/api/vacancies` | EMPLOYER only | Create vacancy |
| `PUT` | `/api/vacancies/{id}` | EMPLOYER owner | Update vacancy |
| `PATCH` | `/api/vacancies/{id}/publish` | EMPLOYER owner | Publish vacancy |
| `PATCH` | `/api/vacancies/{id}/unpublish` | EMPLOYER owner | Unpublish vacancy |
| `GET` | `/api/vacancies/employer/{userId}` | No | Get vacancies by employer |
| `DELETE` | `/api/vacancies/{id}` | EMPLOYER owner | Delete vacancy |
| `GET` | `/api/vacancies/{id}/skills` | No | Get skill IDs |
| `POST` | `/api/vacancies/{id}/skills/{skillId}` | EMPLOYER owner | Add skill |
| `DELETE` | `/api/vacancies/{id}/skills/{skillId}` | EMPLOYER owner | Remove skill |
| `PUT` | `/api/vacancies/{id}/skills` | EMPLOYER owner | Replace all skills |
| `GET` | `/api/vacancies/{id}/company-name` | No | Get company name |
| `POST` | `/api/vacancies/reindex` | No | Reindex all to Elasticsearch |

### Companies (`/api/companies`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/companies` | No | Get all companies |
| `GET` | `/api/companies/{id}` | No | Get company by ID |
| `GET` | `/api/companies/my` | X-User-Id | Get my company |
| `GET` | `/api/companies/employer/{employerId}` | No | Get company by employer ID |
| `POST` | `/api/companies` | X-User-Id | Create company |
| `PUT` | `/api/companies/{id}` | X-User-Id + owner | Update company |
| `GET` | `/api/companies/{id}/company` | No | Get company name by vacancy ID |
| `DELETE` | `/api/companies/{id}` | No | Delete company |

## Kafka Events

| Topic | Trigger |
|---|---|
| `indexing-vacancy` | Vacancy created, updated, published, or skills changed |

Event schema:
```json
{
  "id": 7,
  "title": "Senior Java Developer",
  "description": "...",
  "companyName": "Acme Corp",
  "location": "Moscow",
  "skills": ["Java", "Spring Boot"],
  "salary": 250000.0,
  "createdAt": "2026-03-20T12:00:00"
}
```

## Access Control

Write operations on vacancies require `EMPLOYER` role — enforced at the **API Gateway** level. Service-level checks also validate ownership (only the vacancy's employer can modify it).

## Running locally

```bash
./gradlew bootRun
```

Requires: PostgreSQL, Redis, Kafka.
