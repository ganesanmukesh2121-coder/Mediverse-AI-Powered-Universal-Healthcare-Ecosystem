package com.mediverse.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class CreatePrescriptionRequestDto {

    @NotBlank(message = "Patient ID is required")
    @Size(max = 36, message = "Patient ID must not exceed 36 characters")
    private String patientId;

    @Size(max = 36, message = "Medical record ID must not exceed 36 characters")
    private String recordId;

    @Size(max = 2000, message = "General instructions cannot exceed 2000 characters")
    private String generalInstructions;

    @NotEmpty(message = "Prescription must contain at least one prescription item")
    @Valid
    private List<PrescriptionItemRequestDto> items = new ArrayList<>();

    @Size(max = 36, message = "Appointment ID must not exceed 36 characters")
    private String appointmentId;

    private Boolean walkIn = false;

    @Size(max = 2000, message = "Walk-in justification cannot exceed 2000 characters")
    private String walkInJustification;

    public CreatePrescriptionRequestDto() {
    }

    public CreatePrescriptionRequestDto(String patientId, String recordId, String generalInstructions, List<PrescriptionItemRequestDto> items) {
        this.patientId = patientId;
        this.recordId = recordId;
        this.generalInstructions = generalInstructions;
        this.items = items;
    }

    public CreatePrescriptionRequestDto(String patientId, String recordId, String generalInstructions, List<PrescriptionItemRequestDto> items, String appointmentId) {
        this.patientId = patientId;
        this.recordId = recordId;
        this.generalInstructions = generalInstructions;
        this.items = items;
        this.appointmentId = appointmentId;
    }

    public CreatePrescriptionRequestDto(String patientId, String recordId, String generalInstructions, List<PrescriptionItemRequestDto> items, Boolean walkIn, String walkInJustification) {
        this.patientId = patientId;
        this.recordId = recordId;
        this.generalInstructions = generalInstructions;
        this.items = items;
        this.walkIn = walkIn;
        this.walkInJustification = walkInJustification;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getGeneralInstructions() {
        return generalInstructions;
    }

    public void setGeneralInstructions(String generalInstructions) {
        this.generalInstructions = generalInstructions;
    }

    public List<PrescriptionItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemRequestDto> items) {
        this.items = items;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Boolean getWalkIn() {
        return walkIn;
    }

    public void setWalkIn(Boolean walkIn) {
        this.walkIn = walkIn;
    }

    public boolean isWalkIn() {
        return Boolean.TRUE.equals(walkIn);
    }

    public String getWalkInJustification() {
        return walkInJustification;
    }

    public void setWalkInJustification(String walkInJustification) {
        this.walkInJustification = walkInJustification;
    }
}
