package com.mediverse.dto;

import java.time.LocalDateTime;

import com.mediverse.entity.AppointmentStatus;
import com.mediverse.entity.AppointmentType;

public class AppointmentResponseDto {

    private String appointmentId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private LocalDateTime appointmentDate;
    private String appointmentType;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AppointmentResponseDto() {
    }

    public AppointmentResponseDto(
            String appointmentId,
            String patientId,
            String patientName,
            String doctorId,
            String doctorName,
            LocalDateTime appointmentDate,
            String appointmentType,
            String status,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentType = appointmentType;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public AppointmentResponseDto(
            String appointmentId,
            String patientId,
            String patientName,
            String doctorId,
            String doctorName,
            LocalDateTime appointmentDate,
            AppointmentType appointmentType,
            AppointmentStatus status,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this(
                appointmentId,
                patientId,
                patientName,
                doctorId,
                doctorName,
                appointmentDate,
                appointmentType != null ? appointmentType.name() : null,
                status != null ? status.name() : null,
                reason,
                createdAt,
                updatedAt
        );
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
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

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
