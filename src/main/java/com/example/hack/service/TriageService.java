package com.example.hack.service;

import com.example.hack.dto.ChatTriageResponse;
import com.example.hack.model.ClinicalHistory;
import com.example.hack.model.TriageSession;
import com.example.hack.repository.ClinicalHistoryRepository;
import com.example.hack.repository.TriageSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TriageService {

    private final ClinicalHistoryRepository clinicalHistoryRepository;
    private final TriageSessionRepository triageSessionRepository;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            Eres un asistente de salud empático y profesional. Tu rol es orientar al paciente sobre qué hacer, NO diagnosticar.
            
            IMPORTANTE: El campo "recommendation" es TU RESPUESTA DIRECTA AL USUARIO. Escríbela como si estuvieras conversando con el paciente de forma cálida y clara.
            
            Reglas:
            1. NUNCA diagnostiques. Solo orienta y recomienda acciones.
            2. Clasifica urgencia: ALTO, MEDIO, BAJO
            3. "recommendation" = tu mensaje conversacional al paciente (2-4 oraciones naturales y empáticas)
            4. "warningSigns" = señales de alarma que debe vigilar (en lenguaje simple)
            5. "epsBrief" = resumen técnico breve para la aseguradora
            6. "followUpQuestions" = preguntas amables si necesitas más información (máximo 3)
            7. Responde SOLO en JSON válido, sin markdown ni texto extra.
            
            Tu respuesta en "recommendation" debe:
            - Saludar o mostrar empatía ("Entiendo que te sientes mal...")
            - Dar una orientación clara de qué hacer
            - Ser reconfortante pero honesta
            - NO usar términos médicos complicados
            
            Ejemplo:
            {
              "urgencyLevel": "MEDIO",
              "recommendation": "Entiendo que llevas dos días con dolor de cabeza y fiebre, eso puede ser muy incómodo. Te sugiero que solicites una cita médica en las próximas 24 horas para que un profesional pueda evaluarte. Mientras tanto, descansa, mantente hidratado y puedes tomar un analgésico común si lo necesitas. Si notas que la fiebre sube mucho o aparece rigidez en el cuello, acude a urgencias.",
              "warningSigns": ["Fiebre que supera 39°C y no baja", "Rigidez o dolor intenso al mover el cuello", "Confusión o somnolencia excesiva", "Manchas rojas en la piel"],
              "epsBrief": "Síndrome febril con cefalea de 48h. Requiere valoración médica prioritaria para descartar proceso infeccioso. Sin signos de alarma al momento.",
              "followUpQuestions": []
            }
            
            Contexto del Paciente (Historia Clínica):
            {historyContext}
            """;

    public ChatTriageResponse processTriage(String patientId, String sessionId, String userContent, String language) {
        // 1. Session Management
        String currentSessionId = (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) 
                ? UUID.randomUUID().toString() 
                : sessionId;

        // 2. Retrieve History Context
        String historyContext = getPatientHistoryContext(patientId);

        // 3. Build Prompt
        String formattedSystemPrompt = SYSTEM_PROMPT.replace("{historyContext}", historyContext);
        Message systemMessage = new org.springframework.ai.chat.messages.SystemMessage(formattedSystemPrompt);
        UserMessage userMessage = new UserMessage(userContent);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // 4. Call LLM
        String rawResponse = chatModel.call(prompt).getResult().getOutput().getContent();

        // 5. Parse Response & Persist
        try {
            // Clean markdown code blocks if present
            rawResponse = rawResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            
            TriageSession.TriageResult result = objectMapper.readValue(rawResponse, TriageSession.TriageResult.class);
            
            TriageSession session = new TriageSession();
            session.setSessionId(currentSessionId);
            session.setPatientId(patientId);
            session.setUserMessage(userContent);
            session.setAssistantResponse(rawResponse); // Store raw JSON response
            session.setTriageResult(result);
            triageSessionRepository.save(session);

            // 6. Map to DTO
            ChatTriageResponse response = new ChatTriageResponse();
            response.setSessionId(currentSessionId);
            response.setUrgencyLevel(result.getUrgencyLevel());
            response.setRecommendation(result.getRecommendation());
            response.setWarningSigns(result.getWarningSigns());
            response.setEpsBrief(result.getEpsBrief());
            response.setFollowUpQuestions(result.getFollowUpQuestions());
            
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error processing triage: " + e.getMessage(), e);
        }
    }

    private String getPatientHistoryContext(String patientId) {
        if (patientId == null) return "No patient ID provided.";
        
        List<ClinicalHistory> history = clinicalHistoryRepository.findByPatientId(patientId);
        if (history.isEmpty()) return "No clinical history found for patient " + patientId;

        // Summarize history (taking the latest entry or aggregating)
        ClinicalHistory latest = history.get(0); // Assuming order or just taking first for now
        StringBuilder sb = new StringBuilder();
        
        // Datos de identificación
        if (latest.getPatient().getFullName() != null && !latest.getPatient().getFullName().isBlank()) {
            sb.append("Paciente: ").append(latest.getPatient().getFullName().trim()).append(". ");
        }
        if (latest.getPatient().getDni() != null) {
            sb.append("DNI: ").append(latest.getPatient().getDni()).append(". ");
        }
        
        sb.append("Edad: ").append(latest.getPatient().getDateOfBirth() != null ? latest.getPatient().getDateOfBirth() : "Desc").append(". ");
        sb.append("Sexo: ").append(latest.getPatient().getSex()).append(". ");
        
        if (latest.getChronicConditions() != null && !latest.getChronicConditions().isEmpty()) {
            sb.append("Condiciones Crónicas: ").append(
                latest.getChronicConditions().stream().map(com.example.hack.model.ChronicCondition::getName).collect(Collectors.joining(", "))
            ).append(". ");
        }
        if (latest.getAllergies() != null && !latest.getAllergies().isEmpty()) {
            sb.append("Alergias: ").append(
                latest.getAllergies().stream().map(com.example.hack.model.Allergy::getName).collect(Collectors.joining(", "))
            ).append(". ");
        }
        if (latest.getCriticalMedications() != null && !latest.getCriticalMedications().isEmpty()) {
            sb.append("Medicamentos Críticos: ").append(
                latest.getCriticalMedications().stream().map(com.example.hack.model.CriticalMedication::getName).collect(Collectors.joining(", "))
            ).append(".");
        }
        
        return sb.toString();
    }
}
