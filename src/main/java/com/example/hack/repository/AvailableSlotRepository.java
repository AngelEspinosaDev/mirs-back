package com.example.hack.repository;

import com.example.hack.model.AvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailableSlotRepository extends JpaRepository<AvailableSlot, Long> {

    @Query(value = "SELECT * FROM available_slots s " +
                   "WHERE s.is_available = true " +
                   "AND s.date_time > :now " +
                   "ORDER BY s.date_time ASC", 
           nativeQuery = true)
    List<AvailableSlot> findAvailableSlotsAfter(@Param("now") LocalDateTime now);

    @Query(value = "SELECT s.* FROM available_slots s " +
                   "JOIN doctors d ON s.doctor_id = d.id " +
                   "JOIN specialties sp ON d.specialty_id = sp.id " +
                   "WHERE s.is_available = true " +
                   "AND sp.name = :specialty " +
                   "AND s.date_time > :now " +
                   "ORDER BY s.date_time ASC", 
           nativeQuery = true)
    List<AvailableSlot> findAvailableSlotsBySpecialty(@Param("specialty") String specialty, @Param("now") LocalDateTime now);

    Optional<AvailableSlot> findByDoctorIdAndDateTime(Long doctorId, LocalDateTime dateTime);
}
