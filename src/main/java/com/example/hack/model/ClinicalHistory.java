package com.example.hack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "clinical_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;



    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Demographics demographics;

    @OneToMany(mappedBy = "clinicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChronicCondition> chronicConditions;

    @OneToMany(mappedBy = "clinicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Allergy> allergies;

    @OneToMany(mappedBy = "clinicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CriticalMedication> criticalMedications;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private SpecialConditions specialConditions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "triage_history_flags", columnDefinition = "json")
    private TriageHistoryFlags triageHistoryFlags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Metadata metadata;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialConditions {
        private Boolean pregnancy;
        private Boolean immunosuppression;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriageHistoryFlags {
        private Boolean previousEmergencyVisits;
        private Boolean recurrentSymptoms;
        private Boolean highRiskAlerts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Demographics {
        private String dateOfBirth;
        private String sex;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private String source;
        private String lastUpdated;
        private Boolean validated;
    }
}
