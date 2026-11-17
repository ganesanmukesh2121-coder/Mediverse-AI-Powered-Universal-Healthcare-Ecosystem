package com.mediverse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediverse.dto.PatientProfileResponseDto;
import com.mediverse.dto.PatientProfileUpdateRequestDto;
import com.mediverse.service.PatientProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientProfileService patientProfileService;

    public PatientController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT_VIEW_OWN_PROFILE')")
    public ResponseEntity<PatientProfileResponseDto> getMyProfile(Authentication authentication) {
        String userId = authentication.getName();
        PatientProfileResponseDto response = patientProfileService.getPatientProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT_UPDATE_OWN_PROFILE')")
    public ResponseEntity<PatientProfileResponseDto> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody PatientProfileUpdateRequestDto request) {
        String userId = authentication.getName();
        PatientProfileResponseDto response = patientProfileService.updatePatientProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}
