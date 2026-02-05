# 💼 Vacancy Service

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Feign](https://img.shields.io/badge/OpenFeign-Client-orange?style=flat-square)

**Job vacancy management service for employers**

</div>

---

## 📋 Overview

The Vacancy Service manages job postings, company profiles, and vacancy-related operations. It provides CRUD operations for vacancies and companies, with role-based access control.

### Key Features

- 📝 Vacancy creation and management
- 🏢 Company profile management
- 🔐 Employer-only vacancy creation
- 📊 Pagination support
- 🚀 Publish/unpublish functionality
- 🔄 Retry mechanism for role validation

## 🏗️ Architecture

```
┌──────────────┐      ┌──────────────────┐      ┌─────────────┐
│ Role Service │◀────│  Vacancy Service  │─────▶│ PostgreSQL  │
│   (Feign)    │      │   (Port 8083)     │      │(vacancydb)  │
└──────────────┘      └──────────────────┘      └─────────────┘
```

## 🚀 API Endpoints

### Vacancy Management

#### Get All Vacancies (Published)
```http
GET /api/vacancies?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Senior Java Developer",
      "description": "We are looking for an experienced Java developer...",
      "companyName": "Tech Corp",
      "employerId": 5,
      "salary": 150000.00,
      "isPublished": true
    }
  ],
  "totalPages": 5,
  "totalElements": 50,
  "size": 10,
  "number": 0
}
```

#### Get Vacancy by ID
```http
GET /api/vacancies/1
```

#### Create Vacancy (Employer Only)
```http
POST /api/vacancies
Authorization: Bearer {token}
X-User-Id: 5
Content-Type: application/json

{
  "title": "Senior Java Developer",
  "description": "We are looking for...",
  "companyName": "Tech Corp",
  "employerId": 5,
  "salary": 150000.00,
  "isPublished": false
}
```

**Validation:**
- Title: 3-255 characters, required
- Description: max 5000 characters, required
- Company name: required
- Employer ID: positive number, required
- Salary: zero or positive
- Only users with `EMPLOYER` role can create

#### Update Vacancy
```http
POST /api/vacancies/update
Authorization: Bearer {token}
X-User-Id: 5
Content-Type: application/json

{
  "id": 1,
  "title": "Lead Java Developer",
  "description": "Updated description...",
  "companyName": "Tech Corp",
  "employerId": 5,
  "salary": 160000.00
}
```

#### Publish Vacancy
```http
PATCH /api/vacancies/1/publish
Authorization: Bearer {token}
X-User-Id: 5
```

#### Unpublish Vacancy
```http
PATCH /api/vacancies/1/unpublish
Authorization: Bearer {token}
X-User-Id: 5
```

#### Delete Vacancy
```http
DELETE /api/vacancies/1
Authorization: Bearer {token}
```

### Company Management

#### Get All Companies
```http
GET /api/companies
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Tech Corp",
    "description": "Leading technology company...",
    "location": "San Francisco, CA",
    "email": "hr@techcorp.com",
    "phoneNumber": "+14155551234",
    "website": "https://techcorp.com"
  }
]
```

#### Get Company by ID
```http
GET /api/companies/1
```

#### Create Company
```http
POST /api/companies
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Tech Corp",
  "description": "Leading technology company",
  "location": "San Francisco, CA",
  "email": "hr@techcorp.com",
  "phoneNumber": "+14155551234",
  "website": "https://techcorp.com"
}
```

**Validation:**
- Name: 2-100 characters, required
- Description: max 2000 characters
- Location: required
- Email: valid email format, required
- Phone: pattern `^(\+7|8|\+380|\+375)\d{9,11}$`
- Website: valid URL

#### Delete Company
```http
DELETE /api/companies/1
Authorization: Bearer {token}
```

## 📊 Database Schema

### Vacancies Table
```sql
CREATE TABLE vacancies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    salary NUMERIC(12,2),
    employer_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vacancies_employer ON vacancies(employer_id);
CREATE INDEX idx_vacancies_company ON vacancies(company_id);
CREATE INDEX idx_vacancies_published ON vacancies(is_published);
```

### Companies Table
```sql
CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    location VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔐 Access Control

### Role Validation

Before creating a vacancy, the service validates the user's role:

```java
private void checkForRequiredRole(Long userId) {
    String userRole = roleRetryClient.getUserRoleById(userId);
    
    if (!needRoleForCreate.equals(userRole)) {
        throw new InsufficientPermissionsException(
            "Only users with EMPLOYER role can create vacancies. Current role: " + userRole
        );
    }
}
```

**Configuration:**
```yaml
spring:
  vacancy-create:
    role: EMPLOYER
```

### Ownership Validation

Users can only update/publish their own vacancies:

```java
private void validateOwnership(Long vacancyUserId, Long userId) {
    if (!vacancyUserId.equals(userId)) {
        throw new InsufficientPermissionsException(
            "You can only edit your own vacancies"
        );
    }
}
```

## 🔄 Retry Mechanism

### Role Service Client with Retry

```java
@Component
@RequiredArgsConstructor
public class RoleRetryClient {
    private final RoleServiceClient roleServiceClient;
    
    @Retryable(
        retryFor = { Exception.class },
        maxAttemptsExpression = "${spring.retry.max-attempts}",
        backoff = @Backoff(delayExpression = "${spring.retry.delay}")
    )
    public String getUserRoleById(Long id) {
        log.info("Attempting to fetch role for user id: {}", id);
        return roleServiceClient.getUserRoleById(id);
    }
    
    @Recover
    public String recover(Exception e, Long id) {
        log.error("All retry attempts failed for user id: {}", id);
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Role service is currently unavailable"
        );
    }
}
```

**Configuration:**
```yaml
spring:
  retry:
    max-attempts: 3
    delay: 2000  # milliseconds
```

## ⚙️ Configuration

### application.yaml
```yaml
server:
  port: 8083

spring:
  application:
    name: vacancy-service
  
  datasource:
    url: jdbc:postgresql://localhost:5435/vacancydb
    username: vacancyuser
    password: vacancypass
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  flyway:
    enabled: true
    locations: classpath:db/migration
  
  clients:
    user-service:
      name: user-service
      url: http://localhost:8082
  
  vacancy-create:
    role: EMPLOYER
  
  retry:
    max-attempts: 3
    delay: 2000
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 16
- User Service running (for role validation)

### Run Service
```bash
./gradlew bootRun
```

### Run with Docker
```bash
docker-compose up vacancy-service postgres-vacancy
```

## 🧪 Testing

```bash
./gradlew test
```

**Example Test:**
```java
@Test
@DisplayName("Create vacancy: Success - role matches")
void create_Success() {
    VacancyDto dto = VacancyDto.builder()
        .title("Java Developer")
        .employerId(USER_ID)
        .companyName("Best Company")
        .build();
    
    when(roleRetryClient.getUserRoleById(USER_ID))
        .thenReturn(REQUIRED_ROLE);
    when(repository.save(any(Vacancy.class)))
        .thenAnswer(i -> i.getArgument(0));
    
    Vacancy result = vacancyService.create(dto);
    
    assertNotNull(result);
    assertEquals("Java Developer", result.getTitle());
    verify(repository).save(any(Vacancy.class));
}
```

## 📈 Performance

- Vacancy list retrieval: ~20ms (paginated)
- Vacancy creation: ~80ms (with role validation)
- Company lookup: ~10ms
- Publish/unpublish: ~15ms

## 🔮 Future Enhancements

- [ ] Full-text search for vacancies
- [ ] Vacancy expiration dates
- [ ] Application count tracking
- [ ] Salary range filters
- [ ] Location-based search
- [ ] Skills matching
- [ ] Vacancy analytics dashboard

## 📄 License

Part of LaborExchange Platform - MIT License

---

<div align="center">

**[Back to Main Documentation](../README.md)** | **[User Service](../laborexchange-userservice/README.md)** | **[Resume Service](../laborexchange-resumeservice/README.md)**

</div>
