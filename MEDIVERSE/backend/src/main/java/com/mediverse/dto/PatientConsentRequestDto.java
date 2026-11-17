package com.mediverse.dto;

import java.time.LocalDateTime;

import com.mediverse.entity.DataCategory;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PatientConsentRequestDto {

    @NotBlank(message = "Grantee provider user ID is required")
    @Size(max = 36, message = "Grantee user ID cannot exceed 36 characters")
    private String grantedToUserId;

    @NotNull(message = "Data category is required")
    private DataCategory dataCategory = DataCategory.FULL_TIMELINE;

    @NotNull(message = "Consent expiration date is required")
    @Future(message = "Consent expiration date must be in the future")
    private LocalDateTime expiresAt;

    public PatientConsentRequestDto() {
    }

    public PatientConsentRequestDto(String grantedToUserId, DataCategory dataCategory, LocalDateTime expiresAt) {
        this.grantedToUserId = grantedToUserId;
        this.dataCategory = dataCategory;
        this.expiresAt = expiresAt;
    }

    public String getGrantedToUserId() {
        return grantedToUserId;
    }

    public void setGrantedToUserId(String grantedToUserId) {
        this.grantedToUserId = grantedToUserId;
    }

    public DataCategory getDataCategory() {
        return dataCategory;
    }

    public void setDataCategory(DataCategory dataCategory) {
        this.dataCategory = dataCategory;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
