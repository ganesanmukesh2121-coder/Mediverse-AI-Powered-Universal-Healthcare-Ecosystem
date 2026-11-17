package com.mediverse.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionResponseDto {

    private String prescriptionId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String recordId;
    private LocalDateTime prescriptionDate;
    private String generalInstructions;
    private List<PrescriptionItemResponseDto> items = new ArrayList<>();
    private LocalDateTime createdAt;

    public PrescriptionResponseDto() {
    }

    public PrescriptionResponseDto(String prescriptionId, String patientId, String patientName,
                                   String doctorId, String doctorName, String recordId,
                                   LocalDateTime prescriptionDate, String generalInstructions,
                                   List<PrescriptionItemResponseDto> items, LocalDateTime createdAt) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.recordId = recordId;
        this.prescriptionDate = prescriptionDate;
        this.generalInstructions = generalInstructions;
        this.items = items != null ? items : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public LocalDateTime getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDateTime prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getGeneralInstructions() {
        return generalInstructions;
    }

    public void setGeneralInstructions(String generalInstructions) {
        this.generalInstructions = generalInstructions;
    }

    public List<PrescriptionItemResponseDto> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemResponseDto> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
