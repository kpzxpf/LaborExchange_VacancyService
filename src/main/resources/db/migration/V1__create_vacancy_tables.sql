CREATE TABLE companies (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL UNIQUE,
                           description TEXT,
                           location VARCHAR(255),
                           email VARCHAR(255),
                           phone_number VARCHAR(50),
                           website VARCHAR(255),
                           created_at TIMESTAMP DEFAULT NOW(),
                           updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE vacancies (
                           id BIGSERIAL PRIMARY KEY,
                           title VARCHAR(255) NOT NULL,
                           description TEXT,
                           salary NUMERIC(12, 2),
                           employer_id BIGINT NOT NULL,
                           company_id BIGINT NOT NULL,
                           is_published BOOLEAN NOT NULL DEFAULT FALSE,
                           created_at TIMESTAMP DEFAULT NOW(),
                           updated_at TIMESTAMP DEFAULT NOW(),

                           CONSTRAINT fk_vacancy_company
                               FOREIGN KEY (company_id)
                                   REFERENCES companies(id)
                                   ON DELETE CASCADE
);

CREATE INDEX idx_vacancy_company ON vacancies(company_id);
CREATE INDEX idx_company_name ON companies(name);


CREATE INDEX idx_vacancies_published ON vacancies(is_published)
    WHERE is_published = TRUE;

CREATE INDEX idx_vacancies_employer ON vacancies(employer_id);