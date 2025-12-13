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
}
