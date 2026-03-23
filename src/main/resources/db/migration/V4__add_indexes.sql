-- Vacancy + employer + published status combined (employer viewing their published vacancies)
CREATE INDEX IF NOT EXISTS idx_vacancies_employer_status ON vacancies(employer_id, is_published);

-- Vacancy creation time (sorting by newest)
CREATE INDEX IF NOT EXISTS idx_vacancies_created_at ON vacancies(created_at DESC);

-- Vacancy-skill join table indexes
CREATE INDEX IF NOT EXISTS idx_vacancy_skills_vacancy_id ON vacancy_skills(vacancy_id);
CREATE INDEX IF NOT EXISTS idx_vacancy_skills_skill_id ON vacancy_skills(skill_id);
