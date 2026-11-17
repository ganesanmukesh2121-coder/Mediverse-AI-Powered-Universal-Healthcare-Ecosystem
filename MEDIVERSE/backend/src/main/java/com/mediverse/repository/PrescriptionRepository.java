package com.mediverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

    // 1. Patient-scoped queries
    List<Prescription> findAllByPatient_PatientIdOrderByPrescriptionDateDesc(String patientId);
    List<Prescription> findAllByPatient_User_UserIdOrderByPrescriptionDateDesc(String userId);

    Optional<Prescription> findByPrescriptionIdAndPatient_PatientId(String prescriptionId, String patientId);
    Optional<Prescription> findByPrescriptionIdAndPatient_User_UserId(String prescriptionId, String userId);

    // 2. Doctor-scoped queries
    List<Prescription> findAllByDoctor_DoctorIdOrderByPrescriptionDateDesc(String doctorId);
    List<Prescription> findAllByDoctor_User_UserIdOrderByPrescriptionDateDesc(String userId);

    Optional<Prescription> findByPrescriptionIdAndDoctor_DoctorId(String prescriptionId, String doctorId);
    Optional<Prescription> findByPrescriptionIdAndDoctor_User_UserId(String prescriptionId, String userId);

    // 3. Consultation encounter linking
    List<Prescription> findAllByMedicalRecord_RecordIdOrderByPrescriptionDateDesc(String recordId);
}
