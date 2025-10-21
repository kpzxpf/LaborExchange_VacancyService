INSERT INTO companies (name, description, location, email, phone_number, website)
VALUES
    ('TechCorp', 'Leading software development company', 'Moscow', 'hr@techcorp.com', '+79998887766', 'https://techcorp.com'),
    ('FinWise', 'Innovative fintech solutions', 'Saint Petersburg', 'info@finwise.io', '+79997776655', 'https://finwise.io'),
    ('DataLab', 'Data analytics and AI solutions', 'Remote', 'contact@datalab.ai', '+79996665544', 'https://datalab.ai');

INSERT INTO vacancies (title, description, salary, employer_id, company_id)
VALUES
    ('Java Developer', 'Develop microservices using Spring Boot and Kafka.', 180000, 1, 1),
    ('Frontend Engineer', 'React/TypeScript developer for fintech platform.', 160000, 2, 2),
    ('Data Analyst', 'Analyze data for business insights.', 140000, 3, 3);