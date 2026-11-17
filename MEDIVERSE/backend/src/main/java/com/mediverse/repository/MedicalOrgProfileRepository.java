package com.mediverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.MedicalOrgProfile;
import com.mediverse.entity.VerificationStatus;

@Repository
public interface MedicalOrgProfileRepository extends JpaRepository<MedicalOrgProfile, String> {

    Optional<MedicalOrgProfile> findByVerificationStatus(VerificationStatus status);

    List<MedicalOrgProfile> findAllByVerificationStatus(VerificationStatus status);

    Optional<MedicalOrgProfile> findByUser_UserId(String userId);
}
