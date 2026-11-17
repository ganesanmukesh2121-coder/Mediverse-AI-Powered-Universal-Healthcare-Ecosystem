package com.mediverse.dto;

import java.time.LocalDateTime;

public class MedicalReportResponseDto {

    private String reportId;
    private String patientId;
    private String patientName;
    private String fileId;
    private String reportTitle;
    private String reportType;
    private String structuredMetricsJson;
    private LocalDateTime createdAt;

    public MedicalReportResponseDto() {
    }

    public MedicalReportResponseDto(String reportId, String patientId, String patientName,
                                   String fileId, String reportTitle, String reportType,
                                   String structuredMetricsJson, LocalDateTime createdAt) {
        this.reportId = reportId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.fileId = fileId;
        this.reportTitle = reportTitle;
        this.reportType = reportType;
        this.structuredMetricsJson = structuredMetricsJson;
        this.createdAt = createdAt;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
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

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getStructuredMetricsJson() {
        return structuredMetricsJson;
    }

    public void setStructuredMetricsJson(String structuredMetricsJson) {
        this.structuredMetricsJson = structuredMetricsJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
