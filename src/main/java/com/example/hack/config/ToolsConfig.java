package com.example.hack.config;

import com.example.hack.dto.AppointmentRequest;
import com.example.hack.model.Appointment;
import com.example.hack.service.AppointmentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class ToolsConfig {

    @Bean
    @Description("Schedule a medical appointment for a patient")
    public Function<AppointmentRequest, Appointment> scheduleAppointment(AppointmentService appointmentService) {
        return appointmentService::scheduleAppointment;
    }

    @Bean
    @Description("Get clinical history summary for a patient")
    public Function<com.example.hack.dto.PatientIdRequest, String> getClinicalHistory(com.example.hack.service.TriageToolsService toolsService) {
        return toolsService::getClinicalHistory;
    }

    @Bean
    @Description("Get available appointment slots for a specialty")
    public Function<com.example.hack.dto.SpecialtyRequest, java.util.List<com.example.hack.dto.TimeSlot>> getAvailableSlots(com.example.hack.service.TriageToolsService toolsService) {
        return toolsService::getAvailableSlots;
    }

    @Bean
    @Description("Get all available medical specialties from the system")
    public Function<Void, java.util.List<String>> getAllSpecialties(com.example.hack.service.TriageToolsService toolsService) {
        return (input) -> toolsService.getAllSpecialties();
    }

    @Bean
    @Description("Find a doctor by name")
    public Function<String, com.example.hack.model.Doctor> getDoctorByName(com.example.hack.service.TriageToolsService toolsService) {
        return (name) -> toolsService.getDoctorByName(name);
    }
}
