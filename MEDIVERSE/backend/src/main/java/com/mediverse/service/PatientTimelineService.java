package com.mediverse.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.PatientTimelineEventDto;
import com.mediverse.dto.PatientTimelineResponseDto;
import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.DataCategory;
import com.mediverse.entity.LabOrder;
import com.mediverse.entity.MedicalRecord;
import com.mediverse.entity.MedicalReport;
import com.mediverse.entity.PatientConsent;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.Prescription;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.LabOrderRepository;
import com.mediverse.repository.MedicalRecordRepository;
import com.mediverse.repository.MedicalReportRepository;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.PrescriptionRepository;

@Service
public class PatientTimelineService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabOrderRepository labOrderRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PatientConsentService patientConsentService;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;

    public PatientTimelineService(
            MedicalRecordRepository medicalRecordRepository,
            PrescriptionRepository prescriptionRepository,
            LabOrderRepository labOrderRepository,
            MedicalReportRepository medicalReportRepository,
            PatientProfileRepository patientProfileRepository,
            PatientConsentService patientConsentService,
            AppointmentRepository appointmentRepository,
            AuditLogRepository auditLogRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.labOrderRepository = labOrderRepository;
        this.medicalReportRepository = medicalReportRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.patientConsentService = patientConsentService;
        this.appointmentRepository = appointmentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Dynamically aggregates the full chronological timeline for the authenticated patient (Self-View).
     * Excludes emergency-only triage data and respects patient ownership.
     */
    @Transactional(readOnly = true)
    public PatientTimelineResponseDto getTimelineForPatient(String patientUserId) {
        validateSecurityContext(patientUserId, "PATIENT_VIEW_OWN_PROFILE");

        if (patientUserId == null || patientUserId.isBlank()) {
            throw new IllegalArgumentException("Patient user ID must not be null or blank.");
        }
        PatientProfile patient = patientProfileRepository.findByUser_UserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + patientUserId));

        String patientId = patient.getPatientId();
        List<PatientTimelineEventDto> events = new ArrayList<>();

        // 1. Fetch consultations
        List<MedicalRecord> records = medicalRecordRepository.findAllByPatient_PatientIdOrderByEncounterDateDesc(patientId);
        records.forEach(r -> events.add(mapRecordToEvent(r)));

        // 2. Fetch prescriptions
        List<Prescription> prescriptions = prescriptionRepository.findAllByPatient_PatientIdOrderByPrescriptionDateDesc(patientId);
        prescriptions.forEach(rx -> events.add(mapPrescriptionToEvent(rx)));

        // 3. Fetch lab orders
        List<LabOrder> labOrders = labOrderRepository.findAllByPatient_PatientIdOrderByOrderedAtDesc(patientId);
        labOrders.forEach(lo -> events.add(mapLabOrderToEvent(lo)));

        // 4. Fetch medical reports
        List<MedicalReport> reports = medicalReportRepository.findAllByPatient_PatientIdOrderByCreatedAtDesc(patientId);
        reports.forEach(rep -> events.add(mapReportToEvent(rep)));

        // 5. Sort chronologically descending
        events.sort(Comparator.comparing(PatientTimelineEventDto::getEventDate, Comparator.nullsLast(Comparator.reverseOrder())));

        return new PatientTimelineResponseDto(patientId, patient.getHealthId(), "FULL_TIMELINE", events);
    }

    /**
     * Dynamically aggregates a patient's timeline for an authorized provider.
     * Enforces active consent verification, strictly queries and filters only authorized categories
     * (FULL_TIMELINE, CONSULT_ONLY, or LAB_ONLY), validates care context, and records a mandatory audit read entry.
     */
    @Transactional
    public PatientTimelineResponseDto getTimelineForProvider(
            String providerUserId, String patientId, String appointmentId, String walkInJustification) {

        validateSecurityContext(providerUserId, "DOCTOR_VIEW_AUTHORIZED_RECORD");

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("Provider user ID must not be null or blank.");
        }
        if (patientId == null || patientId.isBlank()) {
            throw new IllegalArgumentException("Patient ID must not be null or blank.");
        }

        PatientProfile patient = patientProfileRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found with ID: " + patientId));

        // 1. Evaluate individual provider consent (any category)
        PatientConsent consent = patientConsentService.validateAndGetActiveConsent(patientId, providerUserId, null)
                .orElseThrow(() -> new AccessDeniedException("Provider does not have active patient consent to view clinical timeline."));

        // 2. Validate care context
        String careContextLog = resolveAndValidateCareContext(providerUserId, patientId, appointmentId, walkInJustification);

        DataCategory authorizedCategory = consent.getDataCategory();
        List<PatientTimelineEventDto> events = new ArrayList<>();

        // 3. Query and aggregate ONLY the permitted categories
        if (authorizedCategory == DataCategory.FULL_TIMELINE || authorizedCategory == DataCategory.CONSULT_ONLY) {
            List<MedicalRecord> records = medicalRecordRepository.findAllByPatient_PatientIdOrderByEncounterDateDesc(patientId);
            records.forEach(r -> events.add(mapRecordToEvent(r)));

            List<Prescription> prescriptions = prescriptionRepository.findAllByPatient_PatientIdOrderByPrescriptionDateDesc(patientId);
            prescriptions.forEach(rx -> events.add(mapPrescriptionToEvent(rx)));
        }

        if (authorizedCategory == DataCategory.FULL_TIMELINE || authorizedCategory == DataCategory.LAB_ONLY) {
            List<LabOrder> labOrders = labOrderRepository.findAllByPatient_PatientIdOrderByOrderedAtDesc(patientId);
            labOrders.forEach(lo -> events.add(mapLabOrderToEvent(lo)));

            List<MedicalReport> reports = medicalReportRepository.findAllByPatient_PatientIdOrderByCreatedAtDesc(patientId);
            reports.forEach(rep -> events.add(mapReportToEvent(rep)));
        }

        // 4. Sort chronologically descending
        events.sort(Comparator.comparing(PatientTimelineEventDto::getEventDate, Comparator.nullsLast(Comparator.reverseOrder())));

        // 5. Mandatory Provider Clinical Read Audit Log with care context
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(providerUserId);
        auditLog.setTargetPatientId(patient.getUser().getUserId());
        auditLog.setAction("PROVIDER_TIMELINE_VIEW");
        String resourceAccessed = "/api/v1/patients/" + patientId + "/timeline - Category: " + authorizedCategory.name() + " - " + careContextLog;
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return new PatientTimelineResponseDto(patientId, patient.getHealthId(), authorizedCategory.name(), events);
    }

    @Transactional
    public PatientTimelineResponseDto getTimelineForProvider(String providerUserId, String patientId) {
        return getTimelineForProvider(providerUserId, patientId, null, null);
    }

    private Appointment validateAppointmentCareContext(String appointmentId, String doctorUserId, String patientId) {
        if (appointmentId == null || appointmentId.isBlank()) {
            throw new IllegalArgumentException("Appointment ID is required for appointment-based care context.");
        }
        Appointment appointment = appointmentRepository.findById(appointmentId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        if (appointment.getDoctor() == null || appointment.getDoctor().getUser() == null
                || !doctorUserId.equals(appointment.getDoctor().getUser().getUserId())) {
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

    private String resolveAndValidateCareContext(String doctorUserId, String patientId, String appointmentId, String walkInJustification) {
        if (appointmentId != null && !appointmentId.isBlank()) {
            Appointment apt = validateAppointmentCareContext(appointmentId, doctorUserId, patientId);
            return "CareContext: APPOINTMENT [ID: " + apt.getAppointmentId() + ", Status: " + apt.getStatus() + "]";
        }
        if (walkInJustification != null && !walkInJustification.isBlank()) {
            validateWalkInCareContext(true, walkInJustification);
            return "CareContext: WALK_IN [Justification: " + walkInJustification.trim() + "]";
        }
        // Check if an applicable active appointment exists between this doctor user and patient
        List<Appointment> doctorAppointments = appointmentRepository.findAllByDoctor_User_UserIdOrderByAppointmentDateDesc(doctorUserId);
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

    private PatientTimelineEventDto mapRecordToEvent(MedicalRecord r) {
        String providerName = r.getDoctor() != null && r.getDoctor().getUser() != null
                ? r.getDoctor().getUser().getFullName() : "Attending Doctor";

        return new PatientTimelineEventDto(
                r.getRecordId(),
                "CONSULTATION",
                r.getEncounterDate(),
                "Consultation (" + (r.getEncounterType() != null ? r.getEncounterType().name() : "GENERAL") + ")",
                r.getChiefComplaint(),
                providerName,
                "CONSULT_ONLY"
        );
    }

    private PatientTimelineEventDto mapPrescriptionToEvent(Prescription rx) {
        String providerName = rx.getDoctor() != null && rx.getDoctor().getUser() != null
                ? rx.getDoctor().getUser().getFullName() : "Prescribing Doctor";

        String summary = rx.getGeneralInstructions() != null && !rx.getGeneralInstructions().isBlank()
                ? rx.getGeneralInstructions()
                : (rx.getPrescriptionItems() != null ? rx.getPrescriptionItems().size() + " medications prescribed" : "Prescription issued");

        return new PatientTimelineEventDto(
                rx.getPrescriptionId(),
                "PRESCRIPTION",
                rx.getPrescriptionDate(),
                "Prescription Issued",
                summary,
                providerName,
                "CONSULT_ONLY"
        );
    }

    private PatientTimelineEventDto mapLabOrderToEvent(LabOrder lo) {
        String labName = lo.getLab() != null ? lo.getLab().getLabName() : "Diagnostic Laboratory";

        return new PatientTimelineEventDto(
                lo.getOrderId(),
                "LAB_ORDER",
                lo.getOrderedAt(),
                "Lab Order: " + lo.getTestName(),
                "Status: " + (lo.getOrderStatus() != null ? lo.getOrderStatus().name() : "ORDERED"),
                labName,
                "LAB_ONLY"
        );
    }

    private PatientTimelineEventDto mapReportToEvent(MedicalReport rep) {
        return new PatientTimelineEventDto(
                rep.getReportId(),
                "MEDICAL_REPORT",
                rep.getCreatedAt(),
                rep.getReportTitle(),
                "Report Type: " + (rep.getReportType() != null ? rep.getReportType().name() : "GENERAL"),
                "Diagnostic Facility",
                "LAB_ONLY"
        );
    }
}
