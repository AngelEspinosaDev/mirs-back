package com.example.hack.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    private String patientId;
    private Long doctorId;
    private LocalDateTime dateTime;
    private String reason;
    private String specialty;
}
