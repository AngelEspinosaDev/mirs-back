package com.example.hack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TimeSlot {
    private LocalDateTime dateTime;
    private Long doctorId;
    private String doctorName;
}
