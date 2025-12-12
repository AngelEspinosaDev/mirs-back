package com.example.hack.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatTriageResponse {
    private String sessionId;
    private String urgencyLevel; // ALTO, MEDIO, BAJO
    private String recommendation; // EMERGENCIAS, CITA_PRIORITARIA, CITA_GENERAL, AUTOCUIDADO
    private List<String> warningSigns;
    private String epsBrief;
    private List<String> followUpQuestions;
}
