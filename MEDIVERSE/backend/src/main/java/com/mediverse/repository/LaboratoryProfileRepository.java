package com.mediverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.LaboratoryProfile;
import com.mediverse.entity.VerificationStatus;

@Repository
public interface LaboratoryProfileRepository extends JpaRepository<LaboratoryProfile, String> {

    Optional<LaboratoryProfile> findByVerificationStatus(VerificationStatus status);

    List<LaboratoryProfile> findAllByVerificationStatus(VerificationStatus status);

    Optional<LaboratoryProfile> findByUser_UserId(String userId);
}
