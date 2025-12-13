-- Patients Table
CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(50) PRIMARY KEY,
    dni VARCHAR(20) UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    date_of_birth DATE,
    sex VARCHAR(10)
);

-- Clinical Histories
CREATE TABLE IF NOT EXISTS clinical_histories (
    id IDENTITY PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL,
    demographics JSON,
    special_conditions JSON,
    triage_history_flags JSON,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_clinical_histories_patients FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- Related Clinical Tables
CREATE TABLE IF NOT EXISTS chronic_conditions (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    clinical_history_id BIGINT,
    CONSTRAINT fk_chronic_conditions_history FOREIGN KEY (clinical_history_id) REFERENCES clinical_histories(id)
);

CREATE TABLE IF NOT EXISTS allergies (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    clinical_history_id BIGINT,
    CONSTRAINT fk_allergies_history FOREIGN KEY (clinical_history_id) REFERENCES clinical_histories(id)
);

CREATE TABLE IF NOT EXISTS critical_medications (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    clinical_history_id BIGINT,
    CONSTRAINT fk_medications_history FOREIGN KEY (clinical_history_id) REFERENCES clinical_histories(id)
);

-- Triage Sessions
CREATE TABLE IF NOT EXISTS triage_sessions (
    id IDENTITY PRIMARY KEY,
    session_id VARCHAR(255),
    patient_id VARCHAR(50),
    triage_result JSON,
    user_message VARCHAR(2000),
    assistant_response VARCHAR(2000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Specialties
CREATE TABLE IF NOT EXISTS specialties (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255) UNIQUE,
    description VARCHAR(255)
);

-- Doctors
CREATE TABLE IF NOT EXISTS doctors (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    specialty_id BIGINT,
    CONSTRAINT fk_doctors_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id)
);

-- Appointments
CREATE TABLE IF NOT EXISTS appointments (
    id IDENTITY PRIMARY KEY,
    patient_id VARCHAR(50),
    doctor_id BIGINT,
    date_time TIMESTAMP,
    reason VARCHAR(255),
    specialty_id BIGINT,
    CONSTRAINT fk_appointments_patients FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_appointments_doctors FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT fk_appointments_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id)
);

-- Available Slots
CREATE TABLE IF NOT EXISTS available_slots (
    id IDENTITY PRIMARY KEY,
    doctor_id BIGINT,
    date_time TIMESTAMP,
    is_available BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_slots_doctors FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- Available Slots
CREATE TABLE IF NOT EXISTS available_slots (
    id IDENTITY PRIMARY KEY,
    doctor_id BIGINT,
    date_time TIMESTAMP,
    is_available BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_slots_doctors FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
