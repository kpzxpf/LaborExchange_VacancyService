CREATE TABLE vacancies (
                           id BIGSERIAL PRIMARY KEY,
                           title VARCHAR(255) NOT NULL,
                           description TEXT,
                           company_name VARCHAR(255),
                           location VARCHAR(255),
                           salary NUMERIC(12,2),
                           employer_id BIGINT NOT NULL,
                           created_at TIMESTAMP DEFAULT NOW(),
                           updated_at TIMESTAMP DEFAULT NOW()
);