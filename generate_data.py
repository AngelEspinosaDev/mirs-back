import random
from datetime import datetime, timedelta

# Listas de condiciones médicas realistas
chronic_conditions = [
    "Diabetes Mellitus Tipo 2", "Hipertensión Arterial", "Asma Bronquial", 
    "EPOC", "Artritis Reumatoide", "Hipotiroidismo", "Insuficiencia Renal Crónica",
    "Enfermedad Coronaria", "Fibrilación Auricular", "Osteoporosis",
    "Migraña Crónica", "Epilepsia", "Enfermedad de Crohn", "Colitis Ulcerosa",
    "Lupus Eritematoso Sistémico", "Psoriasis", "Anemia Crónica"
]

allergies = [
    "Penicilina", "Sulfonamidas", "AINEs", "Aspirina", "Látex",
    "Mariscos", "Frutos secos", "Polen", "Ácaros", "Contraste yodado",
    "Codeína", "Morfina", "Amoxicilina", "Diclofenaco", "Ibuprofeno"
]

medications = [
    "Metformina 850mg", "Losartán 50mg", "Atorvastatina 20mg", "Omeprazol 20mg",
    "Levotiroxina 100mcg", "Salbutamol inhalador", "Warfarina 5mg", "Insulina NPH",
    "Enalapril 10mg", "Amlodipino 5mg", "Aspirina 100mg", "Prednisona 5mg",
    "Metotrexato 15mg", "Gabapentina 300mg", "Carbamazepina 200mg"
]

# Generar SQL para historias clínicas
sql_lines = []
sql_lines.append("-- Insertar historias clínicas con datos realistas")
sql_lines.append("INSERT INTO clinical_histories (patient_id, demographics, special_conditions, triage_history_flags, metadata) VALUES")

history_values = []
for i in range(1, 101):
    patient_id = f"USR-{i:06d}"
    # Determinar sexo y fecha de nacimiento del paciente
    sex = "M" if i % 2 == 1 else "F"
    age = random.randint(20, 80)
    dob = (datetime.now() - timedelta(days=age*365)).strftime("%Y-%m-%d")
    
    demographics = f'{{\\"dateOfBirth\\":\\"{dob}\\",\\"sex\\":\\"{sex}\\"}}'
    
    # Condiciones especiales
    pregnancy = "true" if (sex == "F" and age < 45 and random.random() < 0.1) else "false"
    immunosuppression = "true" if random.random() < 0.05 else "false"
    special_conditions = f'{{\\"pregnancy\\":{pregnancy},\\"immunosuppression\\":{immunosuppression}}}'
    
    history_values.append(f"('{patient_id}', '{demographics}', '{special_conditions}', '{{}}', '{{}}')")

sql_lines.append(",\n".join(history_values) + ";")

# Generar condiciones crónicas
sql_lines.append("\n-- Insertar condiciones crónicas")
sql_lines.append("INSERT INTO chronic_conditions (name, clinical_history_id) VALUES")
condition_values = []
for i in range(1, 101):
    # 60% de pacientes tienen al menos una condición
    if random.random() < 0.6:
        num_conditions = random.randint(1, 3)
        selected_conditions = random.sample(chronic_conditions, min(num_conditions, len(chronic_conditions)))
        for condition in selected_conditions:
            condition_values.append(f"('{condition}', {i})")

sql_lines.append(",\n".join(condition_values) + ";")

# Generar alergias
sql_lines.append("\n-- Insertar alergias")
sql_lines.append("INSERT INTO allergies (name, clinical_history_id) VALUES")
allergy_values = []
for i in range(1, 101):
    # 40% de pacientes tienen alergias
    if random.random() < 0.4:
        num_allergies = random.randint(1, 2)
        selected_allergies = random.sample(allergies, min(num_allergies, len(allergies)))
        for allergy in selected_allergies:
            allergy_values.append(f"('{allergy}', {i})")

sql_lines.append(",\n".join(allergy_values) + ";")

# Generar medicamentos críticos
sql_lines.append("\n-- Insertar medicamentos críticos")
sql_lines.append("INSERT INTO critical_medications (name, clinical_history_id) VALUES")
medication_values = []
for i in range(1, 101):
    # 50% de pacientes toman medicamentos
    if random.random() < 0.5:
        num_meds = random.randint(1, 4)
        selected_meds = random.sample(medications, min(num_meds, len(medications)))
        for med in selected_meds:
            medication_values.append(f"('{med}', {i})")

sql_lines.append(",\n".join(medication_values) + ";")

# Escribir archivo
with open("clinical_data.sql", "w", encoding="utf-8") as f:
    f.write("\n".join(sql_lines))

print("Archivo clinical_data.sql generado exitosamente")
