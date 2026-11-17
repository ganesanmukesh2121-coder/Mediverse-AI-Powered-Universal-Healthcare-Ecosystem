package com.mediverse.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediverse.dto.PatientProfileResponseDto;
import com.mediverse.dto.PatientProfileUpdateRequestDto;
import com.mediverse.entity.Gender;
import com.mediverse.entity.PatientProfile;
import com.mediverse.entity.User;
import com.mediverse.exception.ResourceNotFoundException;
import com.mediverse.repository.PatientProfileRepository;
import com.mediverse.repository.UserRepository;

@Service
public class PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    public PatientProfileService(
            PatientProfileRepository patientProfileRepository,
            UserRepository userRepository) {
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PatientProfileResponseDto getPatientProfile(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be null or blank.");
        }

        PatientProfile profile = patientProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));

        return mapToResponseDto(profile);
    }

    @Transactional
    public PatientProfileResponseDto updatePatientProfile(String userId, PatientProfileUpdateRequestDto request) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be null or blank.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Update request must not be null.");
        }

        PatientProfile profile = patientProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));

        User user = profile.getUser();
        if (user == null) {
            throw new ResourceNotFoundException("Associated user account not found for patient profile.");
        }

        // Full name is mandatory and cannot be blank
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        user.setFullName(request.getFullName().trim());

        // Optional phone number
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().isBlank() ? null : request.getPhoneNumber().trim());
        }

        // Optional date of birth - must not be in the future
        if (request.getDateOfBirth() != null) {
            if (request.getDateOfBirth().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Date of birth cannot be in the future.");
            }
            profile.setDateOfBirth(request.getDateOfBirth());
        }

        // Optional gender - validate against Gender enum
        if (request.getGender() != null) {
            if (request.getGender().isBlank()) {
                profile.setGender(null);
            } else {
                try {
                    profile.setGender(Gender.valueOf(request.getGender().trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid gender: " + request.getGender() + ". Allowed values are MALE, FEMALE, OTHER.");
                }
            }
        }

        // Optional blood group
        if (request.getBloodGroup() != null) {
            profile.setBloodGroup(request.getBloodGroup().isBlank() ? null : request.getBloodGroup().trim());
        }

        // Optional emergency contact phone
        if (request.getEmergencyContactPhone() != null) {
            profile.setEmergencyContactPhone(request.getEmergencyContactPhone().isBlank() ? null : request.getEmergencyContactPhone().trim());
        }

        // Optional address
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress().isBlank() ? null : request.getAddress().trim());
        }

        // Persist updates to both entities
        userRepository.save(user);
        PatientProfile savedProfile = patientProfileRepository.save(profile);

        return mapToResponseDto(savedProfile);
    }

    private PatientProfileResponseDto mapToResponseDto(PatientProfile profile) {
        User user = profile.getUser();
        return new PatientProfileResponseDto(
                user != null ? user.getFullName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhoneNumber() : null,
                user != null && user.getAccountStatus() != null ? user.getAccountStatus().name() : null,
                profile.getHealthId(),
                profile.getDateOfBirth(),
                profile.getGender() != null ? profile.getGender().name() : null,
                profile.getBloodGroup(),
                profile.getEmergencyContactPhone(),
                profile.getAddress()
        );
    }
}
