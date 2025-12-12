package com.example.hack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "critical_medications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriticalMedication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "clinical_history_id")
    private ClinicalHistory clinicalHistory;
}
