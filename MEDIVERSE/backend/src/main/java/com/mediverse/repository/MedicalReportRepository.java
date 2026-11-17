package com.mediverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.MedicalReport;
import com.mediverse.entity.ReportType;

@Repository
public interface MedicalReportRepository extends JpaRepository<MedicalReport, String> {

    // 1. Patient-scoped queries
    List<MedicalReport> findAllByPatient_PatientIdOrderByCreatedAtDesc(String patientId);
    List<MedicalReport> findAllByPatient_User_UserIdOrderByCreatedAtDesc(String userId);

    List<MedicalReport> findAllByPatient_PatientIdAndReportTypeOrderByCreatedAtDesc(String patientId, ReportType reportType);
    List<MedicalReport> findAllByPatient_User_UserIdAndReportTypeOrderByCreatedAtDesc(String userId, ReportType reportType);

    Optional<MedicalReport> findByReportIdAndPatient_PatientId(String reportId, String patientId);
    Optional<MedicalReport> findByReportIdAndPatient_User_UserId(String reportId, String userId);

    // 2. File-linked queries
    Optional<MedicalReport> findByFile_FileId(String fileId);
}
