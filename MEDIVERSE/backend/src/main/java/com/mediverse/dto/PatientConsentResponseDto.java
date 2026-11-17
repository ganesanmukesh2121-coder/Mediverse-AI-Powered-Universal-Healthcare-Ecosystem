package com.mediverse.dto;

import java.time.LocalDateTime;

public class PatientConsentResponseDto {

    private String consentId;
    private String patientId;
    private String patientName;
    private String grantedToUserId;
    private String grantedToUserName;
    private String grantedToUserRole;
    private String dataCategory;
    private String status;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    public PatientConsentResponseDto() {
    }

    public PatientConsentResponseDto(String consentId, String patientId, String patientName,
                                     String grantedToUserId, String grantedToUserName, String grantedToUserRole,
                                     String dataCategory, String status, LocalDateTime grantedAt,
                                     LocalDateTime expiresAt, LocalDateTime revokedAt) {
        this.consentId = consentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.grantedToUserId = grantedToUserId;
        this.grantedToUserName = grantedToUserName;
        this.grantedToUserRole = grantedToUserRole;
        this.dataCategory = dataCategory;
        this.status = status;
        this.grantedAt = grantedAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
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

    public String getGrantedToUserId() {
        return grantedToUserId;
    }

    public void setGrantedToUserId(String grantedToUserId) {
        this.grantedToUserId = grantedToUserId;
    }

    public String getGrantedToUserName() {
        return grantedToUserName;
    }

    public void setGrantedToUserName(String grantedToUserName) {
        this.grantedToUserName = grantedToUserName;
    }

    public String getGrantedToUserRole() {
        return grantedToUserRole;
    }

    public void setGrantedToUserRole(String grantedToUserRole) {
        this.grantedToUserRole = grantedToUserRole;
    }

    public String getDataCategory() {
        return dataCategory;
    }

    public void setDataCategory(String dataCategory) {
        this.dataCategory = dataCategory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
