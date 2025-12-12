package com.example.hack.controller;

import com.example.hack.model.ClinicalHistory;
import com.example.hack.model.Patient;
import com.example.hack.repository.ClinicalHistoryRepository;
import com.example.hack.repository.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ClinicalHistoryController {

    private final ClinicalHistoryRepository historyRepository;
    private final PatientRepository patientRepository;

    public ClinicalHistoryController(ClinicalHistoryRepository historyRepository, PatientRepository patientRepository) {
        this.historyRepository = historyRepository;
        this.patientRepository = patientRepository;
    }

    @PostMapping("/clinical-summaries")
    public ResponseEntity<ClinicalHistory> createClinicalSummary(@RequestBody ClinicalSummaryRequest request) {
        ClinicalHistoryDTO dto = request.getClinicalSummary();
        String patientId = dto.getPatientId();
        
        Patient patient = patientRepository.findById(patientId)
                .orElseGet(() -> {
                    Patient newPatient = new Patient();
                    newPatient.setId(patientId);
                    if (dto.getDemographics() != null && dto.getDemographics().getDateOfBirth() != null) {
                        try {
                            newPatient.setDateOfBirth(java.time.LocalDate.parse(dto.getDemographics().getDateOfBirth()));
                        } catch (Exception e) { }
                        newPatient.setSex(dto.getDemographics().getSex());
                    }
                    return patientRepository.save(newPatient);
                });

        ClinicalHistory history = new ClinicalHistory();
        history.setPatient(patient);
        history.setDemographics(dto.getDemographics());
        history.setMetadata(dto.getMetadata());
        history.setTriageHistoryFlags(dto.getTriageHistoryFlags());
        if (dto.getRiskFactors() != null) {
            history.setSpecialConditions(dto.getRiskFactors().getSpecialConditions());
            
            if (dto.getRiskFactors().getChronicConditions() != null) {
                List<com.example.hack.model.ChronicCondition> conditions = dto.getRiskFactors().getChronicConditions().stream()
                    .map(name -> {
                        com.example.hack.model.ChronicCondition c = new com.example.hack.model.ChronicCondition();
                        c.setName(name);
                        c.setClinicalHistory(history);
                        return c;
                    }).collect(java.util.stream.Collectors.toList());
                history.setChronicConditions(conditions);
            }
            if (dto.getRiskFactors().getAllergies() != null) {
                List<com.example.hack.model.Allergy> allergies = dto.getRiskFactors().getAllergies().stream()
                    .map(name -> {
                        com.example.hack.model.Allergy c = new com.example.hack.model.Allergy();
                        c.setName(name);
                        c.setClinicalHistory(history);
                        return c;
                    }).collect(java.util.stream.Collectors.toList());
                history.setAllergies(allergies);
            }
            if (dto.getRiskFactors().getCriticalMedications() != null) {
                List<com.example.hack.model.CriticalMedication> meds = dto.getRiskFactors().getCriticalMedications().stream()
                    .map(name -> {
                        com.example.hack.model.CriticalMedication c = new com.example.hack.model.CriticalMedication();
                        c.setName(name);
                        c.setClinicalHistory(history);
                        return c;
                    }).collect(java.util.stream.Collectors.toList());
                history.setCriticalMedications(meds);
            }
        }
        
        return ResponseEntity.ok(historyRepository.save(history));
    }

    @GetMapping("/clinical-summaries")
    public ResponseEntity<List<ClinicalHistory>> getHistory(@RequestParam(required = false) String patientId) {
        if (patientId != null) {
            return ResponseEntity.ok(historyRepository.findByPatientId(patientId));
        }
        return ResponseEntity.ok(historyRepository.findAll());
    }

    @GetMapping("/patients/{patientId}/history")
    public ResponseEntity<List<ClinicalHistory>> getHistoryByPatientId(@PathVariable String patientId) {
        return ResponseEntity.ok(historyRepository.findByPatientId(patientId));
    }

    @lombok.Data
    static class ClinicalSummaryRequest {
        private ClinicalHistoryDTO clinicalSummary;
    }

    @lombok.Data
    static class ClinicalHistoryDTO {
        @com.fasterxml.jackson.annotation.JsonProperty("patientId")
        private String patientId;
        private ClinicalHistory.Demographics demographics;
        private RiskFactorsDTO riskFactors;
        private ClinicalHistory.TriageHistoryFlags triageHistoryFlags;
        private ClinicalHistory.Metadata metadata;
    }

    @lombok.Data
    static class RiskFactorsDTO {
        private List<String> chronicConditions;
        private List<String> allergies;
        private List<String> criticalMedications;
        private ClinicalHistory.SpecialConditions specialConditions;
    }
}
