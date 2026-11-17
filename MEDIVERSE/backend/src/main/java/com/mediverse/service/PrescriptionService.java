package com.mediverse.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.CreatePrescriptionRequestDto;
import com.mediverse.dto.PrescriptionItemRequestDto;
import com.mediverse.dto.PrescriptionItemResponseDto;
import com.mediverse.dto.PrescriptionResponseDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.DataCategory;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.MedicalRecord;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.Prescription;
import com.mediverse.entity.PrescriptionItem;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.MedicalRecordRepository;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.PrescriptionRepository;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientConsentService patientConsentService;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            MedicalRecordRepository medicalRecordRepository,
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            PatientConsentService patientConsentService,
            AppointmentRepository appointmentRepository,
            AuditLogRepository auditLogRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.patientConsentService = patientConsentService;
        this.appointmentRepository = appointmentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Issues an electronic prescription authored by an authenticated doctor for an authorized patient.
     * Requires active consent and a verified care context (linked consultation record, appointment, or walk-in).
     * Persists items with cascade and enforces append-only immutability.
     */
    @Transactional
    public PrescriptionResponseDto createPrescription(String doctorUserId, CreatePrescriptionRequestDto request) {
        validateSecurityContext(doctorUserId, "DOCTOR_CREATE_PRESCRIPTION");

        if (doctorUserId == null || doctorUserId.isBlank()) {
            throw new IllegalArgumentException("Doctor user ID must not be null or blank.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Prescription request must not be null.");
        }
        if (request.getPatientId() == null || request.getPatientId().isBlank()) {
            throw new IllegalArgumentException("Patient ID is required.");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Prescription must contain at least one prescription item.");
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
                .orElseThrow(() -> new AccessDeniedException("Doctor does not have active patient consent to issue prescriptions."));

        // 4. Care-Context Verification
        MedicalRecord linkedRecord = null;
        String careContextLog;
        if (request.getRecordId() != null && !request.getRecordId().isBlank()) {
            linkedRecord = medicalRecordRepository.findById(request.getRecordId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with ID: " + request.getRecordId()));

            // Consistency checks: same patient and same doctor
            if (!patient.getPatientId().equals(linkedRecord.getPatient().getPatientId())) {
                throw new IllegalArgumentException("Linked medical record does not belong to the target patient.");
            }
            if (!doctor.getDoctorId().equals(linkedRecord.getDoctor().getDoctorId())) {
                throw new IllegalArgumentException("Linked medical record was not authored by the prescribing doctor.");
            }
            careContextLog = "CareContext: CONSULTATION_RECORD [ID: " + linkedRecord.getRecordId() + "]";
        } else if (request.getAppointmentId() != null && !request.getAppointmentId().isBlank()) {
            Appointment apt = validateAppointmentCareContext(request.getAppointmentId(), doctor.getDoctorId(), patient.getPatientId());
            careContextLog = "CareContext: APPOINTMENT [ID: " + apt.getAppointmentId() + ", Status: " + apt.getStatus() + "]";
        } else if (request.isWalkIn() || (request.getWalkInJustification() != null && !request.getWalkInJustification().isBlank())) {
            validateWalkInCareContext(request.getWalkIn(), request.getWalkInJustification());
            careContextLog = "CareContext: WALK_IN [Justification: " + request.getWalkInJustification().trim() + "]";
        } else {
            throw new IllegalArgumentException("Prescription cannot be issued without an authorized care context: provide a linked medical record ID, appointment ID, or documented walk-in justification.");
        }

        // 5. Construct and persist Prescription with child items (CascadeType.ALL)
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setMedicalRecord(linkedRecord);
        prescription.setPrescriptionDate(LocalDateTime.now());
        prescription.setGeneralInstructions(request.getGeneralInstructions() != null ? request.getGeneralInstructions().trim() : null);

        for (PrescriptionItemRequestDto itemDto : request.getItems()) {
            if (itemDto.getMedicineName() == null || itemDto.getMedicineName().isBlank()) {
                throw new IllegalArgumentException("Medicine name is required for all prescription items.");
            }
            if (itemDto.getDosage() == null || itemDto.getDosage().isBlank()) {
                throw new IllegalArgumentException("Dosage is required for all prescription items.");
            }
            if (itemDto.getDurationDays() == null || itemDto.getDurationDays() <= 0) {
                throw new IllegalArgumentException("Duration in days must be greater than zero.");
            }

            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(prescription);
            item.setMedicineName(itemDto.getMedicineName().trim());
            item.setDosage(itemDto.getDosage().trim());
            item.setFrequency(itemDto.getFrequency() != null ? itemDto.getFrequency().trim() : null);
            item.setDurationDays(itemDto.getDurationDays());
            item.setInstructions(itemDto.getInstructions() != null ? itemDto.getInstructions().trim() : null);

            prescription.getPrescriptionItems().add(item);
        }

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        // 6. Mandatory audit logging with care context
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(doctorUserId);
        auditLog.setTargetPatientId(patientUser.getUserId());
        auditLog.setAction("PRESCRIPTION_CREATE");
        String resourceAccessed = "/api/v1/prescriptions/" + savedPrescription.getPrescriptionId() + " - " + careContextLog;
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return mapToDto(savedPrescription);
    }

    /**
     * Retrieves all prescriptions for the authenticated patient (Self-View).
     */
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDto> getPatientPrescriptions(String patientUserId) {
        validateSecurityContext(patientUserId, "PATIENT_VIEW_OWN_PROFILE");

        if (patientUserId == null || patientUserId.isBlank()) {
            throw new IllegalArgumentException("Patient user ID must not be null or blank.");
        }
        PatientProfile patient = patientProfileRepository.findByUser_UserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + patientUserId));

        List<Prescription> list = prescriptionRepository.findAllByPatient_PatientIdOrderByPrescriptionDateDesc(patient.getPatientId());
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Retrieves prescriptions for a specified patient by an authorized doctor under active consent
     * and a verified care context.
     */
    @Transactional
    public List<PrescriptionResponseDto> getPrescriptionsForDoctor(
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
                .orElseThrow(() -> new AccessDeniedException("Doctor does not have active patient consent to view prescriptions."));

        // Validate care context
        String careContextLog = resolveAndValidateCareContext(doctor.getDoctorId(), patientId, appointmentId, walkInJustification);

        List<Prescription> list = prescriptionRepository.findAllByPatient_PatientIdOrderByPrescriptionDateDesc(patientId);

        // Audit clinical read
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(doctorUserId);
        auditLog.setTargetPatientId(patient.getUser().getUserId());
        auditLog.setAction("PRESCRIPTION_VIEW");
        String resourceAccessed = "/api/v1/patients/" + patientId + "/prescriptions - " + careContextLog;
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public List<PrescriptionResponseDto> getPrescriptionsForDoctor(String doctorUserId, String patientId) {
        return getPrescriptionsForDoctor(doctorUserId, patientId, null, null);
    }

    /**
     * Retrieves a single prescription with patient ownership or doctor consent verification and care context.
     */
    @Transactional
    public PrescriptionResponseDto getPrescriptionById(
            String actorUserId, String actorRole, String prescriptionId, String appointmentId, String walkInJustification) {

        if (actorUserId == null || prescriptionId == null) {
            throw new IllegalArgumentException("Actor ID and prescription ID must not be null.");
        }
        Prescription rx = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        String normalizedRole = actorRole != null ? actorRole.toUpperCase().replace("ROLE_", "").trim() : "";

        if ("PATIENT".equals(normalizedRole)) {
            validateSecurityContext(actorUserId, "PATIENT_VIEW_OWN_PROFILE");
            if (rx.getPatient() == null || rx.getPatient().getUser() == null
                    || !actorUserId.equals(rx.getPatient().getUser().getUserId())) {
                throw new AccessDeniedException("User is not authorized to view this prescription.");
            }
        } else if ("DOCTOR".equals(normalizedRole)) {
            validateSecurityContext(actorUserId, "DOCTOR_VIEW_AUTHORIZED_RECORD");

            DoctorProfile doctor = doctorProfileRepository.findByUser_UserId(actorUserId)
                    .orElseThrow(() -> new AccessDeniedException("Doctor profile not found for user ID: " + actorUserId));

            patientConsentService.validateAndGetActiveConsent(rx.getPatient().getPatientId(), actorUserId, DataCategory.CONSULT_ONLY)
                    .orElseThrow(() -> new AccessDeniedException("Doctor does not have active consent to view this prescription."));

            String careContextLog = resolveAndValidateCareContext(
                    doctor.getDoctorId(), rx.getPatient().getPatientId(), appointmentId, walkInJustification);

            AuditLog auditLog = new AuditLog();
            auditLog.setActorUserId(actorUserId);
            auditLog.setTargetPatientId(rx.getPatient().getUser().getUserId());
            auditLog.setAction("PRESCRIPTION_VIEW");
            String resourceAccessed = "/api/v1/prescriptions/" + prescriptionId + " - " + careContextLog;
            if (resourceAccessed.length() > 255) {
                resourceAccessed = resourceAccessed.substring(0, 255);
            }
            auditLog.setResourceAccessed(resourceAccessed);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } else {
            throw new AccessDeniedException("Role not permitted to view prescriptions: " + actorRole);
        }

        return mapToDto(rx);
    }

    @Transactional
    public PrescriptionResponseDto getPrescriptionById(String actorUserId, String actorRole, String prescriptionId) {
        return getPrescriptionById(actorUserId, actorRole, prescriptionId, null, null);
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

    private PrescriptionResponseDto mapToDto(Prescription rx) {
        String patientId = rx.getPatient() != null ? rx.getPatient().getPatientId() : null;
        String patientName = rx.getPatient() != null && rx.getPatient().getUser() != null
                ? rx.getPatient().getUser().getFullName() : null;

        String doctorId = rx.getDoctor() != null ? rx.getDoctor().getDoctorId() : null;
        String doctorName = rx.getDoctor() != null && rx.getDoctor().getUser() != null
                ? rx.getDoctor().getUser().getFullName() : null;

        String recordId = rx.getMedicalRecord() != null ? rx.getMedicalRecord().getRecordId() : null;

        List<PrescriptionItemResponseDto> items = new ArrayList<>();
        if (rx.getPrescriptionItems() != null) {
            items = rx.getPrescriptionItems().stream()
                    .map(i -> new PrescriptionItemResponseDto(
                            i.getItemId(),
                            i.getMedicineName(),
                            i.getDosage(),
                            i.getFrequency(),
                            i.getDurationDays(),
                            i.getInstructions()))
                    .collect(Collectors.toList());
        }

        return new PrescriptionResponseDto(
                rx.getPrescriptionId(),
                patientId,
                patientName,
                doctorId,
                doctorName,
                recordId,
                rx.getPrescriptionDate(),
                rx.getGeneralInstructions(),
                items,
                rx.getCreatedAt()
        );
    }
}
