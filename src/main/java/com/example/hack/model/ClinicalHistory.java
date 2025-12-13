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
import jakarta.persistence.OneToOne;
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



    @Column(name = "last_update")
    private String lastUpdate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "special_condition_id", referencedColumnName = "id")
    private SpecialCondition specialCondition;

    @OneToMany(mappedBy = "clinicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChronicCondition> chronicConditions;

    @OneToMany(mappedBy = "clinicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Allergy> allergies;

    @OneToMany(mappedBy = "clinicalHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CriticalMedication> criticalMedications;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
