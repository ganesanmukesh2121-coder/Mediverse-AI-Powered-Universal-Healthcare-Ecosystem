package com.mediverse.dto;

import com.mediverse.entity.AppointmentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateAppointmentStatusRequestDto {

    @NotNull(message = "Appointment status is required")
    private AppointmentStatus status;

    @Size(max = 1000, message = "Cancellation reason cannot exceed 1000 characters")
    private String cancellationReason;

    public UpdateAppointmentStatusRequestDto() {
    }

    public UpdateAppointmentStatusRequestDto(AppointmentStatus status) {
        this.status = status;
    }

    public UpdateAppointmentStatusRequestDto(AppointmentStatus status, String cancellationReason) {
        this.status = status;
        this.cancellationReason = cancellationReason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
