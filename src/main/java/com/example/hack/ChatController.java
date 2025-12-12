package com.example.hack;

import com.example.hack.dto.ChatTriageRequest;
import com.example.hack.dto.ChatTriageResponse;
import com.example.hack.service.TriageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/triage")
@RequiredArgsConstructor
public class ChatController {

    private final TriageService triageService;

    /**
     * Endpoint principal de triage con LLM
     * POST /api/triage/chat
     * Body: {"patientId": "123", "sessionId": "abc", "content": "Me duele la cabeza", "language": "es"}
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatTriageResponse> processTriage(@Valid @RequestBody ChatTriageRequest request) {
        ChatTriageResponse response = triageService.processTriage(
            request.getPatientId(),
            request.getSessionId(),
            request.getContent(),
            request.getLanguage()
        );
        
        return ResponseEntity.ok(response);
    }
}
