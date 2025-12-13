package com.example.hack.repository;

import com.example.hack.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorIdAndDateTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    
    // Retrieve patient appointments
    List<Appointment> findByPatientIdOrderByDateTimeAsc(String patientId);
    
    // Check for duplicate specialty booking in future
    boolean existsByPatientIdAndSpecialtyIdAndDateTimeAfter(String patientId, Long specialtyId, LocalDateTime dateTime);
}
