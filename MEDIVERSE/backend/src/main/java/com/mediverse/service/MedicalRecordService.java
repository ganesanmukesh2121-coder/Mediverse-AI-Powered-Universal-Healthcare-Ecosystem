package com.mediverse.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.CreateConsultationRequestDto;
import com.mediverse.dto.MedicalRecordResponseDto;
import com.mediverse.dto.PrescriptionResponseDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.DataCategory;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.EncounterType;
import com.mediverse.entity.MedicalRecord;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.MedicalRecordRepository;
import com.mediverse.repository.PatientProfileRepository;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientConsentService patientConsentService;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;

    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            PatientConsentService patientConsentService,
            AppointmentRepository appointmentRepository,
            AuditLogRepository auditLogRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.patientConsentService = patientConsentService;
        this.appointmentRepository = appointmentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Creates a new consultation encounter record authored by an authenticated doctor.
     * Requires active patient consent for consultation or full timeline, and a valid care context
     * (either an active, non-cancelled appointment belonging to doctor and patient, or documented walk-in).
     */
    @Transactional
    public MedicalRecordResponseDto createConsultation(String doctorUserId, CreateConsultationRequestDto request) {
        validateSecurityContext(doctorUserId, "DOCTOR_CREATE_CONSULTATION");

        if (doctorUserId == null || doctorUserId.isBlank()) {
            throw new IllegalArgumentException("Doctor user ID must not be null or blank.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Consultation creation request must not be null.");
        }
        if (request.getPatientId() == null || request.getPatientId().isBlank()) {
            throw new IllegalArgumentException("Patient ID is required.");
        }
        if (request.getChiefComplaint() == null || request.getChiefComplaint().isBlank()) {
            throw new IllegalArgumentException("Chief complaint is required.");
        }
        if (request.getDiagnosisNotes() == null || request.getDiagnosisNotes().isBlank()) {
            throw new IllegalArgumentException("Diagnosis notes are required.");
        }

        // 1. Resolve authoring Doctor
        DoctorProfile doctor = doctorProfileRepository.findByUser_UserId(doctorUserId)
                .orElseThrow(() -> new AccessDeniedException("Doctor profile not found for user ID: " + doctorUserId));

        User doctorUser = doctor.getUser();
        if (doctorUser == null || doctorUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Doctor user account is not active.");
        }
        if (doctor.getVerificationStatus() != VerificationStatus.ACTIVE) {
            throw new IllegalStateException("Doctor profile is not verified.");
        }

        // 2. Resolve Target Patient
        PatientProfile patient = patientProfileRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found with ID: " + request.getPatientId()));

        User patientUser = patient.getUser();
        if (patientUser == null || patientUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Patient account is not active.");
        }

        // 3. Authorization & Consent Verification (CONSULT_ONLY or FULL_TIMELINE)
        patientConsentService.validateAndGetActiveConsent(patient.getPatientId(), doctorUserId, DataCategory.CONSULT_ONLY)
                .orElseThrow(() -> new AccessDeniedException("Doctor does not have active patient consent for consultation."));

        // 4. Care-Context Verification: Appointment or Documented Walk-in
        String careContextLog;
        if (request.getAppointmentId() != null && !request.getAppointmentId().isBlank()) {
            Appointment apt = validateAppointmentCareContext(request.getAppointmentId(), doctor.getDoctorId(), patient.getPatientId());
            careContextLog = "CareContext: APPOINTMENT [ID: " + apt.getAppointmentId() + ", Status: " + apt.getStatus() + "]";
        } else if (request.isWalkIn() || (request.getWalkInJustification() != null && !request.getWalkInJustification().isBlank())) {
            validateWalkInCareContext(request.getWalkIn(), request.getWalkInJustification());
            careContextLog = "CareContext: WALK_IN [Justification: " + request.getWalkInJustification().trim() + "]";
        } else {
            throw new IllegalArgumentException("A valid care context is required: provide either a valid appointment ID or documented walk-in justification.");
        }

        // 5. Construct and persist MedicalRecord (Append-Only)
        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setEncounterType(EncounterType.CONSULTATION);
        record.setChiefComplaint(request.getChiefComplaint().trim());
        record.setDiagnosisNotes(request.getDiagnosisNotes().trim());
        record.setEncounterDate(request.getEncounterDate() != null ? request.getEncounterDate() : LocalDateTime.now());

        MedicalRecord savedRecord = medicalRecordRepository.save(record);

        // 6. Mandatory audit logging with care context
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(doctorUserId);
        auditLog.setTargetPatientId(patientUser.getUserId());
        auditLog.setAction("CONSULTATION_CREATE");
        String resourceAccessed = "/api/v1/medical-records/" + savedRecord.getRecordId() + " - " + careContextLog;
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return mapToDto(savedRecord);
    }

    /**
     * Retrieves all medical records for the authenticated patient (Self-View).
     */
    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDto> getPatientMedicalRecords(String patientUserId) {
        validateSecurityContext(patientUserId, "PATIENT_VIEW_OWN_PROFILE");

        if (patientUserId == null || patientUserId.isBlank()) {
            throw new IllegalArgumentException("Patient user ID must not be null or blank.");
        }
        PatientProfile patient = patientProfileRepository.findByUser_UserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + patientUserId));

        List<MedicalRecord> records = medicalRecordRepository.findAllByPatient_PatientIdOrderByEncounterDateDesc(patient.getPatientId());
        return records.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Retrieves medical records for a specified patient by an authorized doctor under active consent
     * and a verified care context (specific appointment ID or walk-in justification).
     */
    @Transactional
    public List<MedicalRecordResponseDto> getMedicalRecordsForDoctor(
            String doctorUserId, String patientId, String appointmentId, String walkInJustification) {

        validateSecurityContext(doctorUserId, "DOCTOR_VIEW_AUTHORIZED_RECORD");

        if (doctorUserId == null || doctorUserId.isBlank()) {
            throw new IllegalArgumentException("Doctor user ID must not be null or blank.");
        }
        if (patientId == null || patientId.isBlank()) {
            throw new IllegalArgumentException("Patient ID must not be null or blank.");
        }

        DoctorProfile doctor = doctorProfileRepository.findByUser_UserId(doctorUserId)
                .orElseThrow(() -> new AccessDeniedException("Doctor profile not found for user ID: " + doctorUserId));

        if (doctor.getUser() == null || doctor.getUser().getAccountStatus() != AccountStatus.ACTIVE
                || doctor.getVerificationStatus() != VerificationStatus.ACTIVE) {
            throw new AccessDeniedException("Doctor account is not active and verified.");
        }

        PatientProfile patient = patientProfileRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        // Evaluate active consent
        patientConsentService.validateAndGetActiveConsent(patientId, doctorUserId, DataCategory.CONSULT_ONLY)
                .orElseThrow(() -> new AccessDeniedException("Doctor does not have active patient consent to view clinical records."));

        // Validate care context
        String careContextLog = resolveAndValidateCareContext(doctor.getDoctorId(), patientId, appointmentId, walkInJustification);

        List<MedicalRecord> records = medicalRecordRepository.findAllByPatient_PatientIdOrderByEncounterDateDesc(patientId);

        // Audit clinical read
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(doctorUserId);
        auditLog.setTargetPatientId(patient.getUser().getUserId());
        auditLog.setAction("CLINICAL_RECORD_VIEW");
        String resourceAccessed = "/api/v1/patients/" + patientId + "/medical-records - " + careContextLog;
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return records.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Overloaded method defaulting to automatic appointment lookup for care context.
     */
    @Transactional
    public List<MedicalRecordResponseDto> getMedicalRecordsForDoctor(String doctorUserId, String patientId) {
        return getMedicalRecordsForDoctor(doctorUserId, patientId, null, null);
    }

    /**
     * Retrieves a single medical record by record ID with strict role-based access validation and care context.
     */
    @Transactional
    public MedicalRecordResponseDto getMedicalRecordById(
            String actorUserId, String actorRole, String recordId, String appointmentId, String walkInJustification) {

        if (actorUserId == null || recordId == null) {
            throw new IllegalArgumentException("Actor ID and record ID must not be null.");
        }
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with ID: " + recordId));

        String normalizedRole = actorRole != null ? actorRole.toUpperCase().replace("ROLE_", "").trim() : "";

        if ("PATIENT".equals(normalizedRole)) {
            validateSecurityContext(actorUserId, "PATIENT_VIEW_OWN_PROFILE");
            // Patient ownership check
            if (record.getPatient() == null || record.getPatient().getUser() == null
                    || !actorUserId.equals(record.getPatient().getUser().getUserId())) {
                throw new AccessDeniedException("User is not authorized to view this medical record.");
            }
        } else if ("DOCTOR".equals(normalizedRole)) {
            validateSecurityContext(actorUserId, "DOCTOR_VIEW_AUTHORIZED_RECORD");

            DoctorProfile doctor = doctorProfileRepository.findByUser_UserId(actorUserId)
                    .orElseThrow(() -> new AccessDeniedException("Doctor profile not found for user ID: " + actorUserId));

            // Doctor consent check
            patientConsentService.validateAndGetActiveConsent(record.getPatient().getPatientId(), actorUserId, DataCategory.CONSULT_ONLY)
                    .orElseThrow(() -> new AccessDeniedException("Doctor does not have active consent to view this record."));

            // Care context check
            String careContextLog = resolveAndValidateCareContext(
                    doctor.getDoctorId(), record.getPatient().getPatientId(), appointmentId, walkInJustification);

            // Audit read
            AuditLog auditLog = new AuditLog();
            auditLog.setActorUserId(actorUserId);
            auditLog.setTargetPatientId(record.getPatient().getUser().getUserId());
            auditLog.setAction("CLINICAL_RECORD_VIEW");
            String resourceAccessed = "/api/v1/medical-records/" + recordId + " - " + careContextLog;
            if (resourceAccessed.length() > 255) {
                resourceAccessed = resourceAccessed.substring(0, 255);
            }
            auditLog.setResourceAccessed(resourceAccessed);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } else {
            throw new AccessDeniedException("Role not permitted to view medical records: " + actorRole);
        }

        return mapToDto(record);
    }

    @Transactional
    public MedicalRecordResponseDto getMedicalRecordById(String actorUserId, String actorRole, String recordId) {
        return getMedicalRecordById(actorUserId, actorRole, recordId, null, null);
    }

    private Appointment validateAppointmentCareContext(String appointmentId, String doctorId, String patientId) {
        if (appointmentId == null || appointmentId.isBlank()) {
            throw new IllegalArgumentException("Appointment ID is required for appointment-based care context.");
        }
        Appointment appointment = appointmentRepository.findById(appointmentId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        if (appointment.getDoctor() == null || !doctorId.equals(appointment.getDoctor().getDoctorId())) {
            throw new AccessDeniedException("Appointment does not belong to the authenticated doctor.");
        }
        if (appointment.getPatient() == null || !patientId.equals(appointment.getPatient().getPatientId())) {
            throw new AccessDeniedException("Appointment does not belong to the target patient.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment is cancelled and cannot authorize clinical care context.");
        }
        if (appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new IllegalStateException("Appointment is marked as NO_SHOW and cannot authorize clinical care context.");
        }
        return appointment;
    }

    private void validateWalkInCareContext(Boolean walkIn, String walkInJustification) {
        boolean isWalkIn = Boolean.TRUE.equals(walkIn) || (walkInJustification != null && !walkInJustification.isBlank());
        if (!isWalkIn) {
            throw new IllegalArgumentException("A valid care context is required: provide either a valid appointment ID or documented walk-in justification.");
        }
        if (walkInJustification == null || walkInJustification.trim().length() < 5) {
            throw new IllegalArgumentException("Meaningful clinical justification is required for walk-in care context (minimum 5 characters).");
        }
    }

    private String resolveAndValidateCareContext(String doctorId, String patientId, String appointmentId, String walkInJustification) {
        if (appointmentId != null && !appointmentId.isBlank()) {
            Appointment apt = validateAppointmentCareContext(appointmentId, doctorId, patientId);
            return "CareContext: APPOINTMENT [ID: " + apt.getAppointmentId() + ", Status: " + apt.getStatus() + "]";
        }
        if (walkInJustification != null && !walkInJustification.isBlank()) {
            validateWalkInCareContext(true, walkInJustification);
            return "CareContext: WALK_IN [Justification: " + walkInJustification.trim() + "]";
        }
        // Check if an applicable active appointment exists between this doctor and patient
        List<Appointment> doctorAppointments = appointmentRepository.findAllByDoctor_DoctorIdOrderByAppointmentDateDesc(doctorId);
        Optional<Appointment> applicableApt = doctorAppointments.stream()
                .filter(a -> a.getPatient() != null && patientId.equals(a.getPatient().getPatientId()))
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.NO_SHOW)
                .findFirst();

        if (applicableApt.isPresent()) {
            return "CareContext: APPOINTMENT [ID: " + applicableApt.get().getAppointmentId() + ", Status: " + applicableApt.get().getStatus() + "]";
        }

        throw new AccessDeniedException("Access denied: Care context required. Doctor must have an applicable appointment or documented walk-in context.");
    }

    private void validateSecurityContext(String expectedUserId, String requiredPermission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated.");
        }
        String authenticatedUserId = auth.getName();
        if (expectedUserId != null && !expectedUserId.isBlank() && !expectedUserId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("Authenticated user does not match requested actor ID.");
        }
        if (requiredPermission != null) {
            boolean hasPerm = auth.getAuthorities().stream()
                    .anyMatch(a -> requiredPermission.equals(a.getAuthority()));
            if (!hasPerm) {
                throw new AccessDeniedException("Access denied: Missing required permission " + requiredPermission);
            }
        }
    }

    private MedicalRecordResponseDto mapToDto(MedicalRecord record) {
        String patientId = record.getPatient() != null ? record.getPatient().getPatientId() : null;
        String patientName = record.getPatient() != null && record.getPatient().getUser() != null
                ? record.getPatient().getUser().getFullName() : null;

        String doctorId = record.getDoctor() != null ? record.getDoctor().getDoctorId() : null;
        String doctorName = record.getDoctor() != null && record.getDoctor().getUser() != null
                ? record.getDoctor().getUser().getFullName() : null;
        String doctorSpec = record.getDoctor() != null ? record.getDoctor().getSpecialization() : null;

        return new MedicalRecordResponseDto(
                record.getRecordId(),
                patientId,
                patientName,
                doctorId,
                doctorName,
                doctorSpec,
                record.getEncounterType() != null ? record.getEncounterType().name() : null,
                record.getChiefComplaint(),
                record.getDiagnosisNotes(),
                record.getEncounterDate(),
                record.getCreatedAt()
        );
    }
}
