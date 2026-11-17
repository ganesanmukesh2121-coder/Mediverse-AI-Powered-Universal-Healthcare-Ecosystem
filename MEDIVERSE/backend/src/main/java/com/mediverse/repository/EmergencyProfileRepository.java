package com.mediverse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.EmergencyProfile;

@Repository
public interface EmergencyProfileRepository extends JpaRepository<EmergencyProfile, Long> {

    Optional<EmergencyProfile> findByPatientProfile_PatientId(String patientId);
    Optional<EmergencyProfile> findByPatientProfile_User_UserId(String userId);
    Optional<EmergencyProfile> findByPatientProfile_HealthId(String healthId);

    boolean existsByPatientProfile_PatientId(String patientId);
    boolean existsByPatientProfile_User_UserId(String userId);
}
