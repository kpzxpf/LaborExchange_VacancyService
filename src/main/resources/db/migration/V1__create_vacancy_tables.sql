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

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at_company
    BEFORE UPDATE ON companies
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_updated_at_vacancy
    BEFORE UPDATE ON vacancies
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
