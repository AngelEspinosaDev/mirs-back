package com.example.hack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "triage_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriageSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String sessionId;
    
    private String patientId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private TriageResult triageResult;

    @Column(length = 2000)
    private String userMessage;

    @Column(length = 2000)
    private String assistantResponse;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    public static class TriageResult {
        private String urgencyLevel;
        private String recommendation;
        private java.util.List<String> warningSigns;
        private String epsBrief;
        private java.util.List<String> followUpQuestions;
    }
}
