package com.example.hack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatTriageRequest {
    @NotNull(message = "patientId es requerido")
    private String patientId;
    
    private String sessionId; // Opcional - se crea si viene null/vacío
    
    @NotBlank(message = "content es requerido")
    private String content;
    
    @NotBlank(message = "language es requerido")
    private String language;
}
