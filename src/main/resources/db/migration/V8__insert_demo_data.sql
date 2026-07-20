-- Demo companies, vacancies, skills and reviews for a full employer/candidate walkthrough.

UPDATE companies
SET employer_id = CASE WHEN id <= 20 THEN id + 20 ELSE ((id - 21) % 20) + 21 END,
    description = COALESCE(description, 'Технологическая компания с продуктовой разработкой и распределенными командами.'),
    email = COALESCE(email, 'hr' || id || '@company.demo'),
    phone_number = COALESCE(phone_number, '+7495' || LPAD(id::text, 7, '0')),
    is_verified = CASE WHEN id <= 15 THEN TRUE ELSE is_verified END,
    updated_at = NOW()
WHERE id BETWEEN 1 AND 40;

UPDATE vacancies
SET employment_type = CASE
        WHEN id % 5 = 0 THEN 'CONTRACT'
        WHEN id % 5 = 1 THEN 'FULL_TIME'
        WHEN id % 5 = 2 THEN 'PART_TIME'
        WHEN id % 5 = 3 THEN 'FREELANCE'
        ELSE 'INTERNSHIP'
    END,
    work_format = CASE
        WHEN id % 3 = 0 THEN 'REMOTE'
        WHEN id % 3 = 1 THEN 'HYBRID'
        ELSE 'OFFICE'
    END,
    updated_at = NOW()
WHERE id BETWEEN 1 AND 40;

INSERT INTO companies (id, name, description, location, email, phone_number, website, employer_id, is_verified, created_at, updated_at)
VALUES
    (100, 'NebulaSoft', 'Разрабатывает B2B-платформу для управления наймом, аналитикой и внутренними сервисами.', 'Екатеринбург', 'hr@nebulasoft.demo', '+73430000100', 'https://nebulasoft.demo', 121, TRUE, NOW() - INTERVAL '70 days', NOW()),
    (101, 'FinPulse', 'Финтех-компания с продуктами для платежей, антифрода и финансовой аналитики.', 'Москва', 'talent@finpulse.demo', '+74950000101', 'https://finpulse.demo', 122, TRUE, NOW() - INTERVAL '68 days', NOW()),
    (102, 'MedCloud', 'Облачная платформа для клиник: расписания, телемедицина, ML-подсказки врачам.', 'Санкт-Петербург', 'people@medcloud.demo', '+78120000102', 'https://medcloud.demo', 123, TRUE, NOW() - INTERVAL '62 days', NOW()),
    (103, 'GameForge Studio', 'Студия мобильных и PC-игр с удаленными командами разработки.', 'Удаленно', 'jobs@gameforge.demo', '+78000000103', 'https://gameforge.demo', 124, FALSE, NOW() - INTERVAL '55 days', NOW()),
    (104, 'RetailCore', 'Платформа для e-commerce, складской логистики и персональных рекомендаций.', 'Казань', 'career@retailcore.demo', '+78430000104', 'https://retailcore.demo', 125, TRUE, NOW() - INTERVAL '50 days', NOW()),
    (105, 'GreenData Hub', 'Команда data engineering для промышленной аналитики и ESG-отчетности.', 'Новосибирск', 'hello@greendata.demo', '+73830000105', 'https://greendata.demo', 123, FALSE, NOW() - INTERVAL '45 days', NOW())
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    location = EXCLUDED.location,
    email = EXCLUDED.email,
    phone_number = EXCLUDED.phone_number,
    website = EXCLUDED.website,
    employer_id = EXCLUDED.employer_id,
    is_verified = EXCLUDED.is_verified,
    updated_at = NOW();

INSERT INTO vacancies (id, title, description, salary, employer_id, company_id, is_published, employment_type, work_format, created_at, updated_at)
VALUES
    (100, 'Senior Java/Kafka Backend Developer', 'Разработка микросервисов на Spring Boot, интеграция Kafka, Redis и PostgreSQL. Нужен опыт проектирования API, оптимизации запросов и production-monitoring.', 320000, 121, 100, TRUE, 'FULL_TIME', 'HYBRID', NOW() - INTERVAL '21 days', NOW()),
    (101, 'Middle React/Next.js Frontend Engineer', 'Развитие личного кабинета, дизайн-системы и сложных форм на React, TypeScript и Next.js. Важны аккуратная верстка, тесты и работа с REST API.', 210000, 121, 100, TRUE, 'FULL_TIME', 'REMOTE', NOW() - INTERVAL '20 days', NOW()),
    (102, 'DevOps/SRE Engineer', 'Поддержка Kubernetes-кластеров, CI/CD, мониторинг Prometheus/Grafana, автоматизация инфраструктуры Terraform и Ansible.', 260000, 121, 100, TRUE, 'CONTRACT', 'HYBRID', NOW() - INTERVAL '18 days', NOW()),
    (103, 'Product Analyst FinTech', 'Аналитика платежных сценариев, продуктовые метрики, SQL, Python, Tableau и A/B эксперименты для роста конверсии.', 190000, 122, 101, TRUE, 'FULL_TIME', 'OFFICE', NOW() - INTERVAL '17 days', NOW()),
    (104, 'Data Engineer ClickHouse/Airflow', 'Проектирование витрин данных, пайплайны Airflow, Kafka, ClickHouse и контроль качества данных.', 250000, 122, 101, TRUE, 'FULL_TIME', 'HYBRID', NOW() - INTERVAL '16 days', NOW()),
    (105, 'QA Automation Engineer', 'Автоматизация UI и API тестов, Playwright/Selenium, интеграция проверок в CI, тест-дизайн и регрессионные наборы.', 170000, 123, 102, TRUE, 'FULL_TIME', 'REMOTE', NOW() - INTERVAL '15 days', NOW()),
    (106, 'Python ML Engineer', 'ML-сервисы для медицинских подсказок: подготовка данных, обучение моделей, FastAPI, Docker и мониторинг качества.', 280000, 123, 102, TRUE, 'FULL_TIME', 'HYBRID', NOW() - INTERVAL '13 days', NOW()),
    (107, 'Unity Gameplay Developer', 'Разработка игровых механик, UI-сцен, интеграция аналитики и оптимизация производительности мобильной игры.', 220000, 124, 103, TRUE, 'CONTRACT', 'REMOTE', NOW() - INTERVAL '12 days', NOW()),
    (108, 'UI/UX Product Designer', 'Исследования пользователей, прототипы в Figma, поддержка дизайн-системы и улучшение сценариев онбординга.', 160000, 124, 103, TRUE, 'PART_TIME', 'REMOTE', NOW() - INTERVAL '11 days', NOW()),
    (109, 'Kotlin Backend Intern', 'Стажировка в backend-команде: Kotlin, Spring Boot, PostgreSQL, code review и парное программирование с наставником.', 80000, 125, 104, TRUE, 'INTERNSHIP', 'OFFICE', NOW() - INTERVAL '10 days', NOW()),
    (110, 'Customer Support Engineer', 'Техническая поддержка B2B-клиентов, диагностика интеграций, Linux, логи, SQL-запросы и передача задач разработке.', 90000, 125, 104, TRUE, 'FULL_TIME', 'HYBRID', NOW() - INTERVAL '9 days', NOW()),
    (111, 'Draft Java Solution Architect', 'Черновик вакансии для демонстрации публикации: архитектура enterprise-систем, интеграции и техническое лидерство.', 400000, 121, 100, FALSE, 'FULL_TIME', 'HYBRID', NOW() - INTERVAL '8 days', NOW()),
    (112, 'Freelance Cybersecurity Consultant', 'Пентест веб-приложений, threat modeling, проверка инфраструктуры и рекомендации по устранению уязвимостей.', 300000, 122, 101, TRUE, 'FREELANCE', 'REMOTE', NOW() - INTERVAL '7 days', NOW()),
    (113, 'HR Tech Project Manager', 'Ведение roadmap, синхронизация команд разработки, контроль сроков, Scrum-процессы и коммуникация со стейкхолдерами.', 180000, 125, 104, TRUE, 'FULL_TIME', 'OFFICE', NOW() - INTERVAL '6 days', NOW()),
    (114, 'Junior Data Analyst', 'Помощь аналитикам: SQL-выгрузки, Python/Pandas, визуализации и подготовка отчетов по продуктовым метрикам.', 120000, 123, 105, TRUE, 'INTERNSHIP', 'HYBRID', NOW() - INTERVAL '5 days', NOW())
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    salary = EXCLUDED.salary,
    employer_id = EXCLUDED.employer_id,
    company_id = EXCLUDED.company_id,
    is_published = EXCLUDED.is_published,
    employment_type = EXCLUDED.employment_type,
    work_format = EXCLUDED.work_format,
    updated_at = NOW();

INSERT INTO vacancy_skills (vacancy_id, skill_id)
VALUES
    (100, 1), (100, 2), (100, 3), (100, 18), (100, 100), (100, 101), (100, 109),
    (101, 11), (101, 12), (101, 13), (101, 106), (101, 108), (101, 117),
    (102, 18), (102, 23), (102, 38), (102, 39), (102, 40), (102, 116),
    (103, 16), (103, 33), (103, 34), (103, 120),
    (104, 16), (104, 18), (104, 33), (104, 101), (104, 111), (104, 112),
    (105, 42), (105, 43), (105, 45), (105, 118),
    (106, 16), (106, 18), (106, 20), (106, 113), (106, 114),
    (107, 123), (107, 124), (107, 46), (107, 49),
    (108, 50), (108, 52), (108, 119),
    (109, 2), (109, 4), (109, 5), (109, 28),
    (110, 29), (110, 33), (110, 115),
    (112, 29), (112, 30), (112, 32),
    (113, 35), (113, 46), (113, 47), (113, 49),
    (114, 16), (114, 19), (114, 33), (114, 34)
ON CONFLICT DO NOTHING;

INSERT INTO company_reviews (company_id, author_id, rating, title, text, created_at, updated_at)
VALUES
    (100, 101, 5, 'Сильная инженерная культура', 'Быстрый найм, понятные этапы собеседования и живые технические задачи без лишней бюрократии.', NOW() - INTERVAL '14 days', NOW()),
    (100, 102, 4, 'Хороший продуктовый фокус', 'Команда внимательно относится к UX и дает кандидатам развернутую обратную связь.', NOW() - INTERVAL '12 days', NOW()),
    (101, 103, 5, 'Интересные данные', 'Много задач на стыке финтеха, аналитики и платформенной разработки.', NOW() - INTERVAL '10 days', NOW()),
    (102, 104, 4, 'Зрелый процесс QA', 'В собеседовании обсуждали реальные кейсы автоматизации и качество релизного процесса.', NOW() - INTERVAL '8 days', NOW()),
    (103, 102, 5, 'Гибкая удаленка', 'Комфортное общение, четко описанные ожидания и сильная арт-команда.', NOW() - INTERVAL '6 days', NOW()),
    (104, 105, 4, 'Практичные задачи', 'Много инфраструктуры, метрик и понятное влияние на бизнес.', NOW() - INTERVAL '5 days', NOW())
ON CONFLICT (company_id, author_id) DO UPDATE SET
    rating = EXCLUDED.rating,
    title = EXCLUDED.title,
    text = EXCLUDED.text,
    updated_at = NOW();

SELECT setval(pg_get_serial_sequence('companies', 'id'), COALESCE((SELECT MAX(id) FROM companies), 1), TRUE);
SELECT setval(pg_get_serial_sequence('vacancies', 'id'), COALESCE((SELECT MAX(id) FROM vacancies), 1), TRUE);
SELECT setval(pg_get_serial_sequence('company_reviews', 'id'), COALESCE((SELECT MAX(id) FROM company_reviews), 1), TRUE);
