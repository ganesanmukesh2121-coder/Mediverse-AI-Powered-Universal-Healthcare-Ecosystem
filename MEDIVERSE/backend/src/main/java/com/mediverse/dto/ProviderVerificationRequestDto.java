package com.mediverse.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProviderVerificationRequestDto {

    @NotBlank(message = "Decision is required")
    @Size(max = 20, message = "Decision cannot exceed 20 characters")
    private String decision;

    @Size(max = 1000, message = "Rejection reason cannot exceed 1000 characters")
    private String rejectionReason;

    public ProviderVerificationRequestDto() {
    }

    public ProviderVerificationRequestDto(String decision) {
        this.decision = decision;
    }

    public ProviderVerificationRequestDto(String decision, String rejectionReason) {
        this.decision = decision;
        this.rejectionReason = rejectionReason;
    }

    @AssertTrue(message = "Decision must be either ACTIVE or REJECTED")
    public boolean isDecisionValid() {
        if (decision == null || decision.isBlank()) {
            return true;
        }
        String normalized = decision.trim().toUpperCase();
        return "ACTIVE".equals(normalized) || "REJECTED".equals(normalized);
    }

    @AssertTrue(message = "Rejection reason is required when decision is REJECTED")
    public boolean isRejectionReasonValid() {
        if (decision != null && "REJECTED".equalsIgnoreCase(decision.trim())) {
            return rejectionReason != null && !rejectionReason.isBlank();
        }
        return true;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
