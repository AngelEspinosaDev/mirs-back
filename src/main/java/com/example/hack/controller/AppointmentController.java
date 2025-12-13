package com.example.hack.controller;

import com.example.hack.dto.AppointmentRequest;
import com.example.hack.dto.TimeSlot;
import com.example.hack.model.Appointment;
import com.example.hack.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> scheduleAppointment(@RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.scheduleAppointment(request));
    }

    @GetMapping("/available")
    public ResponseEntity<List<TimeSlot>> getAvailableSlots(@RequestParam(required = false) String specialty) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(specialty));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }
}
