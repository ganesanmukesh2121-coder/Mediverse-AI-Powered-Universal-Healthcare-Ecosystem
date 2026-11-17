package com.mediverse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.FileMetadata;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {

    // 1. Patient-scoped queries
    List<FileMetadata> findAllByPatient_PatientIdOrderByCreatedAtDesc(String patientId);
    List<FileMetadata> findAllByPatient_User_UserIdOrderByCreatedAtDesc(String userId);

    Optional<FileMetadata> findByFileIdAndPatient_PatientId(String fileId, String patientId);
    Optional<FileMetadata> findByFileIdAndPatient_User_UserId(String fileId, String userId);

    // 2. Uploader queries
    List<FileMetadata> findAllByUploadedByUser_UserIdOrderByCreatedAtDesc(String userId);
}
