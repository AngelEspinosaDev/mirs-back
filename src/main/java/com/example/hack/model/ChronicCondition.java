package com.example.hack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chronic_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChronicCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "clinical_history_id")
    private ClinicalHistory clinicalHistory;
}
