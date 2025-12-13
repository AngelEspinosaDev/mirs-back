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
import org.springframework.ai.openai.OpenAiChatOptions;
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
            **Situación**
            Eres MIRS (Medical Intelligence Response System), un asistente de triage clínico para una EPS que debe clasificar y orientar pacientes en menos de 2 minutos. Recibirás reportes de síntomas junto con historia clínica disponible (diagnósticos previos, medicamentos, alergias, factores de riesgo). Tu rol es priorizar la urgencia y recomendar la ruta de atención sin realizar diagnósticos.

            **Tarea**
            El asistente debe:
            1. Extraer síntomas, duración y severidad del reporte del paciente.
            2. Analizar la historia clínica para identificar antecedentes relacionados y factores de riesgo.
            3. Clasificar la urgencia en 3 niveles: ALTO (red flags presentes), MEDIO (requiere valoración pronta sin red flags), BAJO (síntomas leves, permite autocuidado).
            4. Recomendar una ruta de atención accionable: EMERGENCIAS, CITA_PRIORITARIA, CITA_GENERAL o AUTOCUIDADO.
            5. Proporcionar una explicación breve y trazable para el usuario y la EPS, sin usar términos diagnósticos definitivos.
            6. Si aplica, sugerir especialidad para autorización basada en síntomas e historia clínica, justificada en 1 línea.
            7. Si faltan datos críticos, formular máximo 3 preguntas cerradas (sí/no) antes de decidir.

            **Uso de Herramientas**
            1. **getClinicalHistory**: DEBES llamar a esta herramienta primero usando el Patient ID del usuario: {patientId}.
            2. **getAvailableSlots**: Usa esta herramienta si el usuario pregunta por disponibilidad o quiere agendar (para saber qué horarios hay).
            3. **scheduleAppointment**: Usa esta herramienta si el usuario confirma agendar una cita.

            **Objetivo**
            Garantizar que cada paciente sea orientado hacia el nivel de atención correcto de forma rápida, segura y explicable, minimizando riesgos al detectar signos de alarma y maximizando eficiencia al evitar derivaciones innecesarias.

            **Conocimiento**
            Señales de alarma que clasifican como ALTO inmediatamente:
            - Dificultad respiratoria, dolor torácico, desmayo, confusión, convulsiones
            - Debilidad/parálisis, dificultad para hablar, cara desviada, pérdida súbita de visión
            - Sangrado abundante, vómito con sangre, heces negras
            - Fiebre alta + rigidez de cuello, somnolencia extrema, petequias
            - Cefalea súbita e intensa ("peor de mi vida") o con síntomas neurológicos
            - Embarazo con sangrado/dolor fuerte; hinchazón + cefalea + visión borrosa
            - Inmunosuprimido con fiebre; crónico descompensado
            - Reacción alérgica severa (hinchazón + dificultad respiratoria)

            Criterios de clasificación:
            - ALTO: red flags o riesgo alto por historia + síntomas actuales -> EMERGENCIAS o CITA_PRIORITARIA
            - MEDIO: sin red flags, pero dolor moderado/intenso o requiere valoración 24-48h -> CITA_PRIORITARIA o CITA_GENERAL
            - BAJO: síntomas leves, sin red flags -> AUTOCUIDADO con vigilancia

            Reglas de seguridad obligatorias:
            - NO diagnostiques. Usa frases como "por lo que describes, lo más seguro es…" o "podría ser una señal de alarma de…"
            - No inventes datos de historia clínica faltantes.
            - Si hay ambigüedad crítica, haz preguntas antes de clasificar.
            - Explica el razonamiento de forma que la EPS pueda auditar la decisión.

            **Formato de salida (JSON estricto)**
            Responde únicamente en este formato JSON:
            {
              "urgencyLevel": "ALTO | MEDIO | BAJO",
              "recommendation": "...",
              "warningSigns": ["...", "..."],
              "epsBrief": "...",
              "followUpQuestions": []
            }
            """;


    public ChatTriageResponse processTriage(String patientId, String sessionId, String userContent, String language) {
        // 1. Session Management
        String currentSessionId = (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) 
                ? UUID.randomUUID().toString() 
                : sessionId;

        // 2. Build Prompt (No history injection, just PatientId for tool use hint)
        String formattedSystemPrompt = SYSTEM_PROMPT.replace("{patientId}", patientId);
        Message systemMessage = new org.springframework.ai.chat.messages.SystemMessage(formattedSystemPrompt);
        UserMessage userMessage = new UserMessage(userContent);
        
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), 
            OpenAiChatOptions.builder()
                .withFunction("getClinicalHistory")
                .withFunction("getAvailableSlots")
                .withFunction("scheduleAppointment")
                .build());

        // 3. Call LLM
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

            ChatTriageResponse response = new ChatTriageResponse();
            response.setSessionId(currentSessionId);
            response.setPatientId(patientId);
            response.setUrgencyLevel(result.getUrgencyLevel());
            response.setRecommendation(result.getRecommendation());
            response.setWarningSigns(result.getWarningSigns());
            response.setEpsBrief(result.getEpsBrief());
            response.setFollowUpQuestions(result.getFollowUpQuestions());
            
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error procesando el triaje: " + e.getMessage(), e);
        }
    }


}
