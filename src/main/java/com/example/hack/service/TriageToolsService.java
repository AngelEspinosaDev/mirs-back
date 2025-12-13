package com.example.hack.service;

import com.example.hack.dto.PatientIdRequest;
import com.example.hack.dto.SpecialtyRequest;
import com.example.hack.dto.TimeSlot;
import com.example.hack.model.ClinicalHistory;
import com.example.hack.repository.ClinicalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TriageToolsService {


    private final ClinicalHistoryRepository clinicalHistoryRepository;
    private final AppointmentService appointmentService;
    private final com.example.hack.repository.SpecialtyRepository specialtyRepository;
    private final com.example.hack.repository.DoctorRepository doctorRepository;

    public String getClinicalHistory(PatientIdRequest request) {
        if (request.patientId() == null) return "No patient ID provided.";
        
        List<ClinicalHistory> history = clinicalHistoryRepository.findByPatientId(request.patientId());
        if (history.isEmpty()) return "No clinical history found for patient " + request.patientId();

        ClinicalHistory latest = history.get(0);
        StringBuilder sb = new StringBuilder();
        
        if (latest.getPatient().getFullName() != null) {
            sb.append("Paciente: ").append(latest.getPatient().getFullName()).append(". ");
        }
        sb.append("Edad: ").append(latest.getPatient().getDateOfBirth()).append(". ");
        sb.append("Sexo: ").append(latest.getPatient().getSex()).append(". ");
        
        if (latest.getChronicConditions() != null && !latest.getChronicConditions().isEmpty()) {
            sb.append("Condiciones Crónicas: ").append(
                latest.getChronicConditions().stream().map(c -> c.getName()).collect(Collectors.joining(", "))
            ).append(". ");
        }
        if (latest.getAllergies() != null && !latest.getAllergies().isEmpty()) {
            sb.append("Alergias: ").append(
                latest.getAllergies().stream().map(a -> a.getName()).collect(Collectors.joining(", "))
            ).append(". ");
        }
        
        return sb.toString();
    }

    public List<TimeSlot> getAvailableSlots(SpecialtyRequest request) {
        return appointmentService.getAvailableSlots(request.specialty());
    }

    public List<String> getAllSpecialties() {
        return specialtyRepository.findAll().stream()
                .map(com.example.hack.model.Specialty::getName)
                .collect(Collectors.toList());
    }

    public com.example.hack.model.Doctor getDoctorByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name).orElse(null);
    }
}
