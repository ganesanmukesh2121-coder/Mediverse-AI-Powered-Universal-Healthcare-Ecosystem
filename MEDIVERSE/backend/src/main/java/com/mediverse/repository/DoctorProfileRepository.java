package com.mediverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.VerificationStatus;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, String> {

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<DoctorProfile> findByVerificationStatus(VerificationStatus status);

    List<DoctorProfile> findAllByVerificationStatus(VerificationStatus status);

    Optional<DoctorProfile> findByUser_UserId(String userId);
}
