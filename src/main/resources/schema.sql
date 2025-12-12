CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(50) PRIMARY KEY,
    date_of_birth DATE NOT NULL,
    sex VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS clinical_histories (
    id SERIAL PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL REFERENCES patients(id),
    demographics JSON,
    special_conditions JSON,
    triage_history_flags JSON,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chronic_conditions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    clinical_history_id BIGINT REFERENCES clinical_histories(id)
);

CREATE TABLE IF NOT EXISTS allergies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    clinical_history_id BIGINT REFERENCES clinical_histories(id)
);

CREATE TABLE IF NOT EXISTS critical_medications (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    clinical_history_id BIGINT REFERENCES clinical_histories(id)
);

CREATE TABLE IF NOT EXISTS triage_sessions (
    id SERIAL PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE,
    patient_id VARCHAR(50),
    triage_result JSON,
    user_message VARCHAR(2000),
    assistant_response VARCHAR(2000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
