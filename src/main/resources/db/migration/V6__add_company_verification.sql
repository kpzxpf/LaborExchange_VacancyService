ALTER TABLE companies ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_companies_verified ON companies (is_verified);
