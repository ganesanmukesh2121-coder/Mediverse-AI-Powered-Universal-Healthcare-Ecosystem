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
@Table(name = "laboratory_profiles", uniqueConstraints = {
    @UniqueConstraint(name = "uq_lab_user_id", columnNames = {"user_id"})
})
public class LaboratoryProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "lab_id", length = 36, columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private String labId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lab_user"))
    private User user;

    @Column(name = "lab_name", nullable = false, length = 255)
    private String labName;

    @Column(name = "accreditation_number", nullable = false, length = 100)
    private String accreditationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public LaboratoryProfile() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public String getLabId() {
        return labId;
    }

    public void setLabId(String labId) {
        this.labId = labId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getAccreditationNumber() {
        return accreditationNumber;
    }

    public void setAccreditationNumber(String accreditationNumber) {
        this.accreditationNumber = accreditationNumber;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
