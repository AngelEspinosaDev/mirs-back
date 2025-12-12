package com.example.hack.repository;

import com.example.hack.model.TriageSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TriageSessionRepository extends JpaRepository<TriageSession, Long> {
    Optional<TriageSession> findBySessionId(String sessionId);
}
