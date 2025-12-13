package com.example.hack.controller;

import com.example.hack.model.Specialty;
import com.example.hack.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyRepository specialtyRepository;

    @GetMapping
    public List<Specialty> getAll() {
        return specialtyRepository.findAll();
    }

    @PostMapping
    public Specialty create(@RequestBody Specialty specialty) {
        return specialtyRepository.save(specialty);
    }
}
