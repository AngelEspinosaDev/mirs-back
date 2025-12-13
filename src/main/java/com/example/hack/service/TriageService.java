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
    private final com.example.hack.repository.ChatMessageRepository chatMessageRepository;
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
            2. **getAllSpecialties**: 
               - Llama a esta herramienta para obtener la lista completa de especialidades médicas disponibles en el sistema.
               - **OBLIGATORIO para urgencia MEDIO**: Usa esta lista para seleccionar la especialidad más apropiada según los síntomas del paciente.
            3. **getAvailableSlots**: 
               - Usa esta herramienta si el usuario pregunta por disponibilidad o quiere agendar.
               - **OBLIGATORIO para urgencia MEDIO**: Después de seleccionar la especialidad con getAllSpecialties, llama a esta herramienta para mostrar citas disponibles.
               - **FORMATO DE RESPUESTA PARA MEDIO**: En el campo "recommendation", NO uses solo "CITA_PRIORITARIA". En su lugar, escribe un mensaje detallado con emojis como: "Debe agendar una cita con [ESPECIALIDAD]. 📅 Horarios disponibles: 🩺 [Doctor X - Fecha/Hora], 🩺 [Doctor Y - Fecha/Hora]... Mientras espera su cita: [recomendaciones de autocuidado específicas para los síntomas, ej: evitar alimentos irritantes, mantener hidratación, etc.]"
               - **IMPORTANTE**: Si esta herramienta devuelve una lista vacía o no hay doctores con la especialidad seleccionada, en el campo "recommendation" escribe: "❌ Lo sentimos, no hay disponibilidad para [ESPECIALIDAD] en este momento. Por favor, intente más tarde o contacte directamente con la EPS."
            4. **scheduleAppointment**: 
               - Usa esta herramienta cuando el usuario CONFIRME que quiere agendar una cita específica (debe indicar fecha, hora y doctor).
               - Parámetros requeridos: patientId, doctorId, dateTime (formato: YYYY-MM-DDTHH:mm:ss), reason, specialty
               - **FORMATO DE RESPUESTA DESPUÉS DE AGENDAR**: En el campo "recommendation", escribe: "✅ ¡Cita asignada exitosamente! 🎉 Su cita con [Doctor] en [Especialidad] está confirmada para el [Fecha y Hora]. Mientras espera su cita: [recomendaciones específicas de autocuidado según síntomas]"

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
            - IMPORTANTE: SOLO SERAS UN ASISTENTE DE TRIAGE, NO UN MEDICO.
            - IMPORTANTE: un asistente de triage clínico para una EPS que debe clasificar y orientar pacientes en menos de 2 minutos si el usuario te pregunta algo no relacionado con el triage, responde con "Lo siento, pero no puedo ayudarte con eso."

            **Formato de salida (JSON estricto)**
            Responde únicamente en este formato JSON:
            {
              "urgencyLevel": "ALTO | MEDIO | BAJO",
              "recommendation": "...",
              "warningSigns": ["...", "..."],
              "epsBrief": "...",
              "followUpQuestions": []
            }
            
            **REGLAS CRÍTICAS - NO NEGOCIABLES:**
            1. Los campos "urgencyLevel" y "recommendation" son OBLIGATORIOS y NUNCA pueden ser null, vacíos o undefined.
            2. SIEMPRE debes proporcionar un valor válido para ambos campos en CADA respuesta.
            3. Si la información del usuario es insuficiente o vaga:
               - urgencyLevel: "BAJO"
               - recommendation: "Para poder ayudarte mejor, necesito que me des más detalles. Por favor, describe con más detalle cómo te sientes, cuándo comenzaron los síntomas y qué tan intensos son."
               - followUpQuestions: ["¿Cuándo comenzaron los síntomas?", "¿Qué tan intenso es el dolor del 1 al 10?", "¿Has notado otros síntomas?"]
            4. Ejemplo de respuesta válida cuando falta información:
               {
                 "urgencyLevel": "BAJO",
                 "recommendation": "Necesito más información para clasificar correctamente tu caso. Por favor, explícame con más detalle qué síntomas tienes y desde cuándo.",
                 "warningSigns": [],
                 "epsBrief": "Información insuficiente para clasificación inicial.",
                 "followUpQuestions": ["¿Qué síntomas específicos tienes?", "¿Desde cuándo?", "¿Qué tan intensos son?"]
               }
            """;


    public ChatTriageResponse processTriage(String patientId, String sessionId, String userContent, String language) {
        // 1. Session Management
        String currentSessionId = (sessionId == null || sessionId.isEmpty() || "null".equals(sessionId)) 
                ? UUID.randomUUID().toString() 
                : sessionId;

        // 2. Retrieve conversation history
        List<com.example.hack.model.ChatMessage> history = chatMessageRepository.findBySessionIdOrderByTimestampAsc(currentSessionId);
        
        // 3. Build message list with history
        String formattedSystemPrompt = SYSTEM_PROMPT.replace("{patientId}", patientId);
        List<Message> messages = new java.util.ArrayList<>();
        messages.add(new org.springframework.ai.chat.messages.SystemMessage(formattedSystemPrompt));
        
        // Add conversation history
        for (com.example.hack.model.ChatMessage msg : history) {
            if ("USER".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("ASSISTANT".equals(msg.getRole())) {
                messages.add(new org.springframework.ai.chat.messages.AssistantMessage(msg.getContent()));
            }
        }
        
        // Add current user message
        messages.add(new UserMessage(userContent));
        
        Prompt prompt = new Prompt(messages, 
            OpenAiChatOptions.builder()
                .withFunction("getClinicalHistory")
                .withFunction("getAllSpecialties")
                .withFunction("getAvailableSlots")
                .withFunction("scheduleAppointment")
                .build());

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

            // Save user message to history
            com.example.hack.model.ChatMessage userMsg = new com.example.hack.model.ChatMessage();
            userMsg.setSessionId(currentSessionId);
            userMsg.setPatientId(patientId);
            userMsg.setRole("USER");
            userMsg.setContent(userContent);
            userMsg.setTimestamp(java.time.LocalDateTime.now());
            chatMessageRepository.save(userMsg);

            // Save assistant message to history
            com.example.hack.model.ChatMessage assistantMsg = new com.example.hack.model.ChatMessage();
            assistantMsg.setSessionId(currentSessionId);
            assistantMsg.setPatientId(patientId);
            assistantMsg.setRole("ASSISTANT");
            assistantMsg.setContent(rawResponse);
            assistantMsg.setTimestamp(java.time.LocalDateTime.now());
            chatMessageRepository.save(assistantMsg);

            // VALIDATION: Ensure urgencyLevel and recommendation are never null
            if (result.getUrgencyLevel() == null || result.getUrgencyLevel().isEmpty()) {
                result.setUrgencyLevel("BAJO");
            }
            if (result.getRecommendation() == null || result.getRecommendation().isEmpty()) {
                result.setRecommendation("Necesito más información para poder ayudarte mejor. Por favor, describe con más detalle cómo te sientes, cuándo comenzaron los síntomas y qué tan intensos son.");
            }

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
