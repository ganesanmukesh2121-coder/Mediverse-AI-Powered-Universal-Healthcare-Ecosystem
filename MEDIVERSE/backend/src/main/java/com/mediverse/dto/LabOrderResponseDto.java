package com.mediverse.dto;

import java.time.LocalDateTime;

public class LabOrderResponseDto {

    private String orderId;
    private String patientId;
    private String patientName;
    private String labId;
    private String labName;
    private String requestingDoctorId;
    private String requestingDoctorName;
    private String testName;
    private String orderStatus;
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;

    public LabOrderResponseDto() {
    }

    public LabOrderResponseDto(String orderId, String patientId, String patientName,
                              String labId, String labName, String requestingDoctorId,
                              String requestingDoctorName, String testName, String orderStatus,
                              LocalDateTime orderedAt, LocalDateTime completedAt) {
        this.orderId = orderId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.labId = labId;
        this.labName = labName;
        this.requestingDoctorId = requestingDoctorId;
        this.requestingDoctorName = requestingDoctorName;
        this.testName = testName;
        this.orderStatus = orderStatus;
        this.orderedAt = orderedAt;
        this.completedAt = completedAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public String getLabId() {
        return labId;
    }

    public void setLabId(String labId) {
        this.labId = labId;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getRequestingDoctorId() {
        return requestingDoctorId;
    }

    public void setRequestingDoctorId(String requestingDoctorId) {
        this.requestingDoctorId = requestingDoctorId;
    }

    public String getRequestingDoctorName() {
        return requestingDoctorName;
    }

    public void setRequestingDoctorName(String requestingDoctorName) {
        this.requestingDoctorName = requestingDoctorName;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
