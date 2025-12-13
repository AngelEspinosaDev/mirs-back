package com.example.hack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "special_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialCondition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean pregnant;
    private Boolean postpartum;
    private Boolean immunosuppressed;
    private Boolean recentSurgery;

    @OneToOne(mappedBy = "specialCondition")
    private ClinicalHistory clinicalHistory;
}
