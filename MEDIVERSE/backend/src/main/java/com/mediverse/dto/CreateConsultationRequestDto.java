package com.mediverse.dto;

import java.time.LocalDateTime;

import com.mediverse.entity.EncounterType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateConsultationRequestDto {

    @NotBlank(message = "Patient ID is required")
    @Size(max = 36, message = "Patient ID must not exceed 36 characters")
    private String patientId;

    @NotNull(message = "Encounter type is required")
    private EncounterType encounterType = EncounterType.CONSULTATION;

    @NotBlank(message = "Chief complaint is required")
    @Size(max = 5000, message = "Chief complaint cannot exceed 5000 characters")
    private String chiefComplaint;

    @NotBlank(message = "Diagnosis notes are required")
    @Size(max = 10000, message = "Diagnosis notes cannot exceed 10000 characters")
    private String diagnosisNotes;

    private LocalDateTime encounterDate;

    @Size(max = 36, message = "Appointment ID must not exceed 36 characters")
    private String appointmentId;

    private Boolean walkIn = false;

    @Size(max = 2000, message = "Walk-in justification cannot exceed 2000 characters")
    private String walkInJustification;

    public CreateConsultationRequestDto() {
    }

    public CreateConsultationRequestDto(String patientId, EncounterType encounterType, String chiefComplaint, String diagnosisNotes) {
        this.patientId = patientId;
        this.encounterType = encounterType;
        this.chiefComplaint = chiefComplaint;
        this.diagnosisNotes = diagnosisNotes;
        this.encounterDate = LocalDateTime.now();
    }

    public CreateConsultationRequestDto(String patientId, EncounterType encounterType, String chiefComplaint, String diagnosisNotes, LocalDateTime encounterDate) {
        this.patientId = patientId;
        this.encounterType = encounterType;
        this.chiefComplaint = chiefComplaint;
        this.diagnosisNotes = diagnosisNotes;
        this.encounterDate = encounterDate;
    }

    public CreateConsultationRequestDto(String patientId, EncounterType encounterType, String chiefComplaint, String diagnosisNotes, String appointmentId) {
        this.patientId = patientId;
        this.encounterType = encounterType;
        this.chiefComplaint = chiefComplaint;
        this.diagnosisNotes = diagnosisNotes;
        this.encounterDate = LocalDateTime.now();
        this.appointmentId = appointmentId;
    }

    public CreateConsultationRequestDto(String patientId, EncounterType encounterType, String chiefComplaint, String diagnosisNotes, LocalDateTime encounterDate, String appointmentId) {
        this.patientId = patientId;
        this.encounterType = encounterType;
        this.chiefComplaint = chiefComplaint;
        this.diagnosisNotes = diagnosisNotes;
        this.encounterDate = encounterDate;
        this.appointmentId = appointmentId;
    }

    public CreateConsultationRequestDto(String patientId, EncounterType encounterType, String chiefComplaint, String diagnosisNotes, Boolean walkIn, String walkInJustification) {
        this.patientId = patientId;
        this.encounterType = encounterType;
        this.chiefComplaint = chiefComplaint;
        this.diagnosisNotes = diagnosisNotes;
        this.encounterDate = LocalDateTime.now();
        this.walkIn = walkIn;
        this.walkInJustification = walkInJustification;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getDiagnosisNotes() {
        return diagnosisNotes;
    }

    public void setDiagnosisNotes(String diagnosisNotes) {
        this.diagnosisNotes = diagnosisNotes;
    }

    public LocalDateTime getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(LocalDateTime encounterDate) {
        this.encounterDate = encounterDate;
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
