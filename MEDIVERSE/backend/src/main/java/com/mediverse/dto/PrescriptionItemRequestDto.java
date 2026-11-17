package com.mediverse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PrescriptionItemRequestDto {

    @NotBlank(message = "Medicine name is required")
    @Size(max = 150, message = "Medicine name cannot exceed 150 characters")
    private String medicineName;

    @NotBlank(message = "Dosage is required")
    @Size(max = 50, message = "Dosage cannot exceed 50 characters")
    private String dosage;

    @Size(max = 50, message = "Frequency cannot exceed 50 characters")
    private String frequency;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    private Integer durationDays;

    @Size(max = 255, message = "Instructions cannot exceed 255 characters")
    private String instructions;

    public PrescriptionItemRequestDto() {
    }

    public PrescriptionItemRequestDto(String medicineName, String dosage, String frequency, Integer durationDays, String instructions) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.instructions = instructions;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
