package com.example.hack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PatientIdRequest(
    @JsonProperty(required = true) 
    @JsonPropertyDescription("The ID of the patient") 
    String patientId
) {}
