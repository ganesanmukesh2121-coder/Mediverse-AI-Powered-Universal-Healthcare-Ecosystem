package com.mediverse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.PatientProfile;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, String> {

    Optional<PatientProfile> findByUser_UserId(String userId);

    Optional<PatientProfile> findByHealthId(String healthId);
}
