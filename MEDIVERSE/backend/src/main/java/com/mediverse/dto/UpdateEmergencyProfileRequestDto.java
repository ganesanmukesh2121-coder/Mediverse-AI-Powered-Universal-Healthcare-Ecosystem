package com.mediverse.dto;

import jakarta.validation.constraints.Size;

public class UpdateEmergencyProfileRequestDto {

    @Size(max = 2000, message = "Severe allergies description cannot exceed 2000 characters")
    private String severeAllergies;

    @Size(max = 2000, message = "Active critical medications cannot exceed 2000 characters")
    private String activeCriticalMedications;

    @Size(max = 2000, message = "Chronic conditions description cannot exceed 2000 characters")
    private String chronicConditions;

    @Size(max = 2000, message = "Emergency notes cannot exceed 2000 characters")
    private String emergencyNotes;

    public UpdateEmergencyProfileRequestDto() {
    }

    public UpdateEmergencyProfileRequestDto(String severeAllergies, String activeCriticalMedications, String chronicConditions, String emergencyNotes) {
        this.severeAllergies = severeAllergies;
        this.activeCriticalMedications = activeCriticalMedications;
        this.chronicConditions = chronicConditions;
        this.emergencyNotes = emergencyNotes;
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
}
