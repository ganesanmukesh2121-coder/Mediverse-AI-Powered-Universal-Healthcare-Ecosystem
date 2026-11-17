package com.mediverse.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "health_id_registry", uniqueConstraints = {
    @UniqueConstraint(name = "uq_hir_health_id", columnNames = {"health_id"}),
    @UniqueConstraint(name = "uq_hir_patient_user", columnNames = {"patient_user_id"})
})
public class HealthIdRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "health_id_registry_id", nullable = false, updatable = false)
    private Long healthIdRegistryId;

    @Column(name = "health_id", nullable = false, length = 30)
    private String healthId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_hir_patient_user"))
    private User patientUser;

    @Column(name = "qr_reference_token", length = 255)
    private String qrReferenceToken;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HealthIdStatus status;

    public HealthIdRegistry() {
    }

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
    }

    public Long getHealthIdRegistryId() {
        return healthIdRegistryId;
    }

    public void setHealthIdRegistryId(Long healthIdRegistryId) {
        this.healthIdRegistryId = healthIdRegistryId;
    }

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public User getPatientUser() {
        return patientUser;
    }

    public void setPatientUser(User patientUser) {
        this.patientUser = patientUser;
    }

    public String getQrReferenceToken() {
        return qrReferenceToken;
    }

    public void setQrReferenceToken(String qrReferenceToken) {
        this.qrReferenceToken = qrReferenceToken;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public HealthIdStatus getStatus() {
        return status;
    }

    public void setStatus(HealthIdStatus status) {
        this.status = status;
    }
}
