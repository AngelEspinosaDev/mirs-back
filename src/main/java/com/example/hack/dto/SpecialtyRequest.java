package com.example.hack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SpecialtyRequest(
    @JsonProperty(required = true)
    @JsonPropertyDescription("The name of the medical specialty (e.g., Cardiologia, Dermatologia)")
    String specialty
) {}
