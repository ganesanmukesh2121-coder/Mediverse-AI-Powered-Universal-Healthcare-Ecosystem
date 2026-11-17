package com.mediverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.MedicalRecord;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, String> {

    // 1. Patient-scoped queries
    List<MedicalRecord> findAllByPatient_PatientIdOrderByEncounterDateDesc(String patientId);
    List<MedicalRecord> findAllByPatient_User_UserIdOrderByEncounterDateDesc(String userId);

    Optional<MedicalRecord> findByRecordIdAndPatient_PatientId(String recordId, String patientId);
    Optional<MedicalRecord> findByRecordIdAndPatient_User_UserId(String recordId, String userId);

    boolean existsByRecordIdAndPatient_PatientId(String recordId, String patientId);
    boolean existsByRecordIdAndPatient_User_UserId(String recordId, String userId);

    // 2. Doctor-scoped queries (records authored by doctor)
    List<MedicalRecord> findAllByDoctor_DoctorIdOrderByEncounterDateDesc(String doctorId);
    List<MedicalRecord> findAllByDoctor_User_UserIdOrderByEncounterDateDesc(String userId);

    Optional<MedicalRecord> findByRecordIdAndDoctor_DoctorId(String recordId, String doctorId);
    Optional<MedicalRecord> findByRecordIdAndDoctor_User_UserId(String recordId, String userId);
}
