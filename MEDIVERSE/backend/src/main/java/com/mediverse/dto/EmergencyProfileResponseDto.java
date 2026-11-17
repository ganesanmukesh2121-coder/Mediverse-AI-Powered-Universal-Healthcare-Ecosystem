package com.mediverse.dto;

import java.time.LocalDateTime;

/**
 * Minimal emergency profile DTO for emergency responders and patient emergency view.
 *
 * <p>Strict Privacy Boundary: This DTO contains ONLY non-confidential triage data
 * (allergies, critical medications, blood group, chronic conditions, emergency contact).
 * It strictly excludes consultation history, prescriptions, lab reports, radiology scans,
 * uploaded files, or general timeline information.
 */
public class EmergencyProfileResponseDto {

    private Long emergencyProfileId;
    private String patientId;
    private String healthId;
    private String patientName;
    private String bloodGroup;
    private String emergencyContactPhone;
    private String severeAllergies;
    private String activeCriticalMedications;
    private String chronicConditions;
    private String emergencyNotes;
    private LocalDateTime updatedAt;

    public EmergencyProfileResponseDto() {
    }

    public EmergencyProfileResponseDto(Long emergencyProfileId, String patientId, String healthId,
                                      String patientName, String bloodGroup, String emergencyContactPhone,
                                      String severeAllergies, String activeCriticalMedications,
                                      String chronicConditions, String emergencyNotes, LocalDateTime updatedAt) {
        this.emergencyProfileId = emergencyProfileId;
        this.patientId = patientId;
        this.healthId = healthId;
        this.patientName = patientName;
        this.bloodGroup = bloodGroup;
        this.emergencyContactPhone = emergencyContactPhone;
        this.severeAllergies = severeAllergies;
        this.activeCriticalMedications = activeCriticalMedications;
        this.chronicConditions = chronicConditions;
        this.emergencyNotes = emergencyNotes;
        this.updatedAt = updatedAt;
    }

    public Long getEmergencyProfileId() {
        return emergencyProfileId;
    }

    public void setEmergencyProfileId(Long emergencyProfileId) {
        this.emergencyProfileId = emergencyProfileId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getSevereAllergies() {
        return severeAllergies;
    }

    public void setSevereAllergies(String severeAllergies) {
        this.severeAllergies = severeAllergies;
    }

    public String getActiveCriticalMedications() {
        return activeCriticalMedications;
    }

    public void setActiveCriticalMedications(String activeCriticalMedications) {
        this.activeCriticalMedications = activeCriticalMedications;
    }

    public String getChronicConditions() {
        return chronicConditions;
    }

    public void setChronicConditions(String chronicConditions) {
        this.chronicConditions = chronicConditions;
    }

    public String getEmergencyNotes() {
        return emergencyNotes;
    }

    public void setEmergencyNotes(String emergencyNotes) {
        this.emergencyNotes = emergencyNotes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
