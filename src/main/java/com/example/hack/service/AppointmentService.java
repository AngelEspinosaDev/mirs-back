package com.example.hack.service;

import com.example.hack.dto.AppointmentRequest;
import com.example.hack.dto.TimeSlot;
import com.example.hack.model.Appointment;
import com.example.hack.model.Doctor;
import com.example.hack.model.Patient;
import com.example.hack.repository.AppointmentRepository;
import com.example.hack.repository.DoctorRepository;
import com.example.hack.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final com.example.hack.repository.AvailableSlotRepository availableSlotRepository;

    public Appointment scheduleAppointment(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        // Validation: Cannot have another future appointment for the SAME specialty
        boolean alreadyHasAppointment = appointmentRepository.existsByPatientIdAndSpecialtyIdAndDateTimeAfter(
            patient.getId(), 
            doctor.getSpecialty().getId(), 
            LocalDateTime.now()
        );
        
        if (alreadyHasAppointment) {
            throw new RuntimeException("El paciente ya tiene una cita futura para esta especialidad: " + doctor.getSpecialty().getName());
        }
        
        // Validation: Must have an AvailableSlot
        com.example.hack.model.AvailableSlot slot = availableSlotRepository.findByDoctorIdAndDateTime(
                doctor.getId(), request.getDateTime())
                .orElseThrow(() -> new RuntimeException("Espacio no encontrado o tiempo inválido"));
        
        if (!slot.isAvailable()) {
            throw new RuntimeException("El espacio ya está reservado");
        }

        // Mark slot as booked
        slot.setAvailable(false);
        availableSlotRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDateTime(request.getDateTime());
        appointment.setReason(request.getReason());
        // Heredar especialidad del doctor
        appointment.setSpecialty(doctor.getSpecialty());

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        return appointmentRepository.findByPatientIdOrderByDateTimeAsc(patientId);
    }

    public List<TimeSlot> getAvailableSlots(String specialty) {
        List<com.example.hack.model.AvailableSlot> dbSlots;
        LocalDateTime now = LocalDateTime.now();
        
        if (specialty != null && !specialty.isEmpty()) {
            dbSlots = availableSlotRepository.findAvailableSlotsBySpecialty(specialty, now);
        } else {
            dbSlots = availableSlotRepository.findAvailableSlotsAfter(now);
        }

        return dbSlots.stream()
                .map(slot -> new TimeSlot(
                    slot.getDateTime(), 
                    slot.getDoctor().getId(), 
                    slot.getDoctor().getName(),
                    slot.getDoctor().getSpecialty().getName()
                ))
                .limit(5)
                .collect(Collectors.toList());
    }
}
