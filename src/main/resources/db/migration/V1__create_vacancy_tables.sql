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
                           salary NUMERIC(12,2),
                           employer_id BIGINT NOT NULL,
                           company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
                           created_at TIMESTAMP DEFAULT NOW(),
                           updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_vacancy_company ON vacancies(company_id);
CREATE INDEX IF NOT EXISTS idx_company_name ON companies(name);
