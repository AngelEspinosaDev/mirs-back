package com.example.hack.repository;

import com.example.hack.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialtyName(String specialtyName);
    java.util.Optional<Doctor> findByNameContainingIgnoreCase(String name);
}
