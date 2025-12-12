package com.example.hack.repository;

import com.example.hack.model.ClinicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClinicalHistoryRepository extends JpaRepository<ClinicalHistory, Long> {
    List<ClinicalHistory> findByPatientId(String patientId);
}
