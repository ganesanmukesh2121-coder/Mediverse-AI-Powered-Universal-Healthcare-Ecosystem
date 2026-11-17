package com.mediverse.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordResponseDto {

    private String recordId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String encounterType;
    private String chiefComplaint;
    private String diagnosisNotes;
    private LocalDateTime encounterDate;
    private LocalDateTime createdAt;
    private List<PrescriptionResponseDto> prescriptions = new ArrayList<>();

    public MedicalRecordResponseDto() {
    }

    public MedicalRecordResponseDto(String recordId, String patientId, String patientName,
                                   String doctorId, String doctorName, String doctorSpecialization,
                                   String encounterType, String chiefComplaint, String diagnosisNotes,
                                   LocalDateTime encounterDate, LocalDateTime createdAt) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.encounterType = encounterType;
        this.chiefComplaint = chiefComplaint;
        this.diagnosisNotes = diagnosisNotes;
        this.encounterDate = encounterDate;
        this.createdAt = createdAt;
        this.prescriptions = new ArrayList<>();
    }

    public MedicalRecordResponseDto(String recordId, String patientId, String patientName,
                                   String doctorId, String doctorName, String doctorSpecialization,
                                   String encounterType, String chiefComplaint, String diagnosisNotes,
                                   LocalDateTime encounterDate, LocalDateTime createdAt,
                                   List<PrescriptionResponseDto> prescriptions) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialization = doctorSpecialization;
        this.encounterType = encounterType;
        this.chiefComplaint = chiefComplaint;
        this.diagnosisNotes = diagnosisNotes;
        this.encounterDate = encounterDate;
        this.createdAt = createdAt;
        this.prescriptions = prescriptions != null ? prescriptions : new ArrayList<>();
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
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

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PrescriptionResponseDto> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<PrescriptionResponseDto> prescriptions) {
        this.prescriptions = prescriptions;
    }
}
