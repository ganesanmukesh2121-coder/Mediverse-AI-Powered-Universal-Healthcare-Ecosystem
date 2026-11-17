package com.mediverse.dto;

import java.time.LocalDateTime;

import com.mediverse.entity.AppointmentType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateAppointmentRequestDto {

    @NotBlank(message = "Doctor ID is required")
    @Size(max = 36, message = "Doctor ID cannot exceed 36 characters")
    private String doctorId;

    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDate;

    @NotNull(message = "Appointment type is required")
    private AppointmentType appointmentType;

    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason;

    public CreateAppointmentRequestDto() {
    }

    public CreateAppointmentRequestDto(String doctorId, LocalDateTime appointmentDate, AppointmentType appointmentType) {
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentType = appointmentType;
    }

    public CreateAppointmentRequestDto(String doctorId, LocalDateTime appointmentDate, AppointmentType appointmentType, String reason) {
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentType = appointmentType;
        this.reason = reason;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
