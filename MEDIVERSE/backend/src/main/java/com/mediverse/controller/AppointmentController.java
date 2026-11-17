package com.mediverse.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediverse.dto.AppointmentResponseDto;
import com.mediverse.dto.CreateAppointmentRequestDto;
import com.mediverse.dto.UpdateAppointmentStatusRequestDto;
import com.mediverse.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Schedules a new consultation appointment. Accessible to patients only.
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponseDto> scheduleAppointment(
            @Valid @RequestBody CreateAppointmentRequestDto request,
            Authentication authentication) {
        String patientUserId = authentication.getName();
        AppointmentResponseDto response = appointmentService.createAppointment(patientUserId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Lists appointments for the authenticated caller (scoped to patient or doctor).
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(Authentication authentication) {
        String userId = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst()
                .orElse(null);

        List<AppointmentResponseDto> appointments = appointmentService.getMyAppointments(userId, role);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Updates the status of an appointment (cancel, complete, no-show).
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<AppointmentResponseDto> updateAppointmentStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateAppointmentStatusRequestDto request,
            Authentication authentication) {
        String actorUserId = authentication.getName();
        AppointmentResponseDto response = appointmentService.updateAppointmentStatus(id, actorUserId, request);
        return ResponseEntity.ok(response);
    }
}
