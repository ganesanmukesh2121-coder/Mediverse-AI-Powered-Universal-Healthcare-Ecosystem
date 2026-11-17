package com.mediverse.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.ConsentStatus;
import com.mediverse.entity.DataCategory;
import com.mediverse.entity.PatientConsent;

@Repository
public interface PatientConsentRepository extends JpaRepository<PatientConsent, String> {

    // 1. Patient consent management queries
    List<PatientConsent> findAllByPatient_PatientIdOrderByGrantedAtDesc(String patientId);
    List<PatientConsent> findAllByPatient_User_UserIdOrderByGrantedAtDesc(String userId);

    Optional<PatientConsent> findByConsentIdAndPatient_PatientId(String consentId, String patientId);
    Optional<PatientConsent> findByConsentIdAndPatient_User_UserId(String consentId, String userId);

    // 2. Grantee provider queries
    List<PatientConsent> findAllByGrantedToUser_UserIdOrderByGrantedAtDesc(String userId);

    // 3. Active, unexpired, unrevoked consent verification for care context
    Optional<PatientConsent> findFirstByPatient_PatientIdAndGrantedToUser_UserIdAndStatusAndExpiresAtAfterAndRevokedAtIsNullOrderByGrantedAtDesc(
            String patientId, String grantedToUserId, ConsentStatus status, LocalDateTime now);

    Optional<PatientConsent> findFirstByPatient_User_UserIdAndGrantedToUser_UserIdAndStatusAndExpiresAtAfterAndRevokedAtIsNullOrderByGrantedAtDesc(
            String patientUserId, String grantedToUserId, ConsentStatus status, LocalDateTime now);

    boolean existsByPatient_PatientIdAndGrantedToUser_UserIdAndStatusAndExpiresAtAfterAndRevokedAtIsNull(
            String patientId, String grantedToUserId, ConsentStatus status, LocalDateTime now);

    boolean existsByPatient_User_UserIdAndGrantedToUser_UserIdAndStatusAndExpiresAtAfterAndRevokedAtIsNull(
            String patientUserId, String grantedToUserId, ConsentStatus status, LocalDateTime now);

    // 4. Category-scoped consent verification
    Optional<PatientConsent> findFirstByPatient_PatientIdAndGrantedToUser_UserIdAndDataCategoryAndStatusAndExpiresAtAfterAndRevokedAtIsNullOrderByGrantedAtDesc(
            String patientId, String grantedToUserId, DataCategory dataCategory, ConsentStatus status, LocalDateTime now);

    boolean existsByPatient_PatientIdAndGrantedToUser_UserIdAndDataCategoryAndStatusAndExpiresAtAfterAndRevokedAtIsNull(
            String patientId, String grantedToUserId, DataCategory dataCategory, ConsentStatus status, LocalDateTime now);
}
