package com.mediverse.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.AppointmentResponseDto;
import com.mediverse.dto.CreateAppointmentRequestDto;
import com.mediverse.dto.UpdateAppointmentStatusRequestDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Appointment;
import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.AuditLog;
import com.mediverse.entity.DoctorProfile;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.User;
import com.mediverse.entity.VerificationStatus;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.AppointmentRepository;
import com.mediverse.repository.AuditLogRepository;
import com.mediverse.repository.DoctorProfileRepository;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.UserRepository;

/**
 * Service managing appointment booking, listing, and lifecycle state transitions.
 *
 * <p>Concurrency note: Service-level slot conflict checks ensure validation under
 * normal application workflows. However, because the underlying database schema currently lacks a
 * composite unique constraint on (doctor_id, appointment_date) or (patient_id, appointment_date),
 * simultaneous concurrent booking races can theoretically lead to duplicates without database-level
 * locks or constraints.
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Schedules a new consultation appointment for an authenticated patient.
     *
     * @param patientUserId Authenticated patient user ID from SecurityContext
     * @param request       Appointment creation request DTO
     * @return Created appointment response DTO
     */
    @Transactional
    public AppointmentResponseDto createAppointment(String patientUserId, CreateAppointmentRequestDto request) {
        if (patientUserId == null || patientUserId.isBlank()) {
            throw new IllegalArgumentException("Patient user ID must not be null or blank.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Appointment creation request must not be null.");
        }
        if (request.getDoctorId() == null || request.getDoctorId().isBlank()) {
            throw new IllegalArgumentException("Doctor ID is required.");
        }
        if (request.getAppointmentDate() == null) {
            throw new IllegalArgumentException("Appointment date is required.");
        }
        if (!request.getAppointmentDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment date must be in the future.");
        }
        if (request.getAppointmentType() == null) {
            throw new IllegalArgumentException("Appointment type is required.");
        }

        // 1. Identify patient using authenticated user ID and verify active status
        PatientProfile patient = patientProfileRepository.findByUser_UserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + patientUserId));

        User patientUser = patient.getUser();
        if (patientUser == null || patientUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Patient account is not active. Current status: "
                    + (patientUser != null ? patientUser.getAccountStatus() : "UNKNOWN"));
        }

        // 2. Identify requested doctor and verify active/verified status
        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with ID: " + request.getDoctorId()));

        User doctorUser = doctor.getUser();
        if (doctorUser == null || doctorUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Doctor account is not active. Current status: "
                    + (doctorUser != null ? doctorUser.getAccountStatus() : "UNKNOWN"));
        }
        if (doctor.getVerificationStatus() != VerificationStatus.ACTIVE) {
            throw new IllegalStateException("Doctor profile is not verified. Current status: " + doctor.getVerificationStatus());
        }

        // 3. Slot conflict checks (SCHEDULED appointments only)
        if (appointmentRepository.existsByDoctor_DoctorIdAndAppointmentDateAndStatus(
                doctor.getDoctorId(), request.getAppointmentDate(), AppointmentStatus.SCHEDULED)) {
            throw new IllegalStateException("Doctor already has a scheduled appointment at " + request.getAppointmentDate());
        }

        if (appointmentRepository.existsByPatient_PatientIdAndAppointmentDateAndStatus(
                patient.getPatientId(), request.getAppointmentDate(), AppointmentStatus.SCHEDULED)) {
            throw new IllegalStateException("Patient already has a scheduled appointment at " + request.getAppointmentDate());
        }

        // 4. Create and persist appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason(request.getReason() != null ? request.getReason().trim() : null);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 5. Mandatory audit log in the same transaction
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(patientUserId);
        auditLog.setTargetPatientId(patientUser.getUserId());
        auditLog.setAction("APPOINTMENT_SCHEDULE");
        String resourceAccessed = "/api/v1/appointments/" + savedAppointment.getAppointmentId();
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return mapToDto(savedAppointment);
    }

    /**
     * Retrieves appointments for the authenticated user based on role scoping.
     *
     * @param userId Authenticated user ID
     * @param role   Role name (e.g. "PATIENT" or "DOCTOR")
     * @return List of user-scoped appointment DTOs
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getMyAppointments(String userId, String role) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be null or blank.");
        }
        String normalizedRole = role != null ? role.toUpperCase().replace("ROLE_", "").trim() : "";

        List<Appointment> appointments;
        if ("PATIENT".equals(normalizedRole)) {
            appointments = appointmentRepository.findAllByPatient_User_UserIdOrderByAppointmentDateDesc(userId);
        } else if ("DOCTOR".equals(normalizedRole)) {
            appointments = appointmentRepository.findAllByDoctor_User_UserIdOrderByAppointmentDateDesc(userId);
        } else {
            throw new AccessDeniedException("Access denied: Unsupported role for appointment listing: " + role);
        }

        return appointments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Convenience method to retrieve appointments resolving role from user entity.
     *
     * @param userId Authenticated user ID
     * @return List of user-scoped appointment DTOs
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getMyAppointments(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be null or blank.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getRole() == null || user.getRole().getRoleName() == null) {
            throw new AccessDeniedException("Access denied: User does not have an assigned role.");
        }
        return getMyAppointments(userId, user.getRole().getRoleName());
    }

    /**
     * Updates the status of an existing appointment adhering strictly to state machine and ownership rules.
     *
     * @param appointmentId Target appointment ID
     * @param actorUserId   Authenticated user ID of the caller
     * @param request       Status update request DTO
     * @return Updated appointment response DTO
     */
    @Transactional
    public AppointmentResponseDto updateAppointmentStatus(String appointmentId, String actorUserId, UpdateAppointmentStatusRequestDto request) {
        if (appointmentId == null || appointmentId.isBlank()) {
            throw new IllegalArgumentException("Appointment ID must not be null or blank.");
        }
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new IllegalArgumentException("Actor user ID must not be null or blank.");
        }
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("Status update request and target status are required.");
        }

        // 1. Find appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // 2. Identify actor relationship to appointment
        boolean isOwningPatient = appointment.getPatient() != null
                && appointment.getPatient().getUser() != null
                && actorUserId.equals(appointment.getPatient().getUser().getUserId());

        boolean isAssignedDoctor = appointment.getDoctor() != null
                && appointment.getDoctor().getUser() != null
                && actorUserId.equals(appointment.getDoctor().getUser().getUserId());

        if (!isOwningPatient && !isAssignedDoctor) {
            throw new AccessDeniedException("User is not authorized to modify this appointment.");
        }

        // 3. State machine validation
        AppointmentStatus currentStatus = appointment.getStatus();
        AppointmentStatus newStatus = request.getStatus();

        if (currentStatus != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot update an appointment in terminal status: " + currentStatus);
        }

        if (newStatus == AppointmentStatus.SCHEDULED) {
            throw new IllegalArgumentException("Invalid status transition: Appointment is already in SCHEDULED status.");
        }

        if (newStatus == AppointmentStatus.COMPLETED) {
            if (!isAssignedDoctor) {
                throw new AccessDeniedException("Only the assigned doctor can mark an appointment as COMPLETED.");
            }
        } else if (newStatus == AppointmentStatus.NO_SHOW) {
            if (!isAssignedDoctor) {
                throw new AccessDeniedException("Only the assigned doctor can mark an appointment as NO_SHOW.");
            }
        } else if (newStatus == AppointmentStatus.CANCELLED) {
            // Both owning patient and assigned doctor are permitted to cancel
            if (!isOwningPatient && !isAssignedDoctor) {
                throw new AccessDeniedException("Only the owning patient or assigned doctor can cancel an appointment.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported appointment status: " + newStatus);
        }

        // 4. Update and persist
        appointment.setStatus(newStatus);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 5. Mandatory audit log in the same transaction
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(actorUserId);
        auditLog.setTargetPatientId(appointment.getPatient().getUser().getUserId());
        auditLog.setAction("APPOINTMENT_STATUS_UPDATE");

        String resourceAccessed = "/api/v1/appointments/" + appointmentId + "/status - Status: " + newStatus;
        if (request.getCancellationReason() != null && !request.getCancellationReason().isBlank()) {
            resourceAccessed += " - Reason: " + request.getCancellationReason().trim();
        }
        if (resourceAccessed.length() > 255) {
            resourceAccessed = resourceAccessed.substring(0, 255);
        }
        auditLog.setResourceAccessed(resourceAccessed);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        return mapToDto(savedAppointment);
    }

    private AppointmentResponseDto mapToDto(Appointment appointment) {
        String patientId = appointment.getPatient() != null ? appointment.getPatient().getPatientId() : null;
        String patientName = appointment.getPatient() != null && appointment.getPatient().getUser() != null
                ? appointment.getPatient().getUser().getFullName() : null;

        String doctorId = appointment.getDoctor() != null ? appointment.getDoctor().getDoctorId() : null;
        String doctorName = appointment.getDoctor() != null && appointment.getDoctor().getUser() != null
                ? appointment.getDoctor().getUser().getFullName() : null;

        return new AppointmentResponseDto(
                appointment.getAppointmentId(),
                patientId,
                patientName,
                doctorId,
                doctorName,
                appointment.getAppointmentDate(),
                appointment.getAppointmentType() != null ? appointment.getAppointmentType().name() : null,
                appointment.getStatus() != null ? appointment.getStatus().name() : null,
                appointment.getReason(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
