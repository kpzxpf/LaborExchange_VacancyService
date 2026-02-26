ALTER TABLE companies ADD COLUMN IF NOT EXISTS employer_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_companies_employer ON companies (employer_id);
