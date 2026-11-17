package com.mediverse.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "emergency_profiles", uniqueConstraints = {
    @UniqueConstraint(name = "uq_emergency_patient_id", columnNames = {"patient_id"})
})
public class EmergencyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emergency_profile_id", nullable = false, updatable = false)
    private Long emergencyProfileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_emergency_patient"))
    private PatientProfile patientProfile;

    @Column(name = "severe_allergies", columnDefinition = "TEXT")
    private String severeAllergies;

    @Column(name = "active_critical_medications", columnDefinition = "TEXT")
    private String activeCriticalMedications;

    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions;

    @Column(name = "emergency_notes", columnDefinition = "TEXT")
    private String emergencyNotes;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public EmergencyProfile() {
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
    }

    public Long getEmergencyProfileId() {
        return emergencyProfileId;
    }

    public void setEmergencyProfileId(Long emergencyProfileId) {
        this.emergencyProfileId = emergencyProfileId;
    }

    public PatientProfile getPatientProfile() {
        return patientProfile;
    }

    public void setPatientProfile(PatientProfile patientProfile) {
        this.patientProfile = patientProfile;
    }

    public String getSevereAllergies() {
        return severeAllergies;
    }

    public void setSevereAllergies(String severeAllergies) {
        this.severeAllergies = severeAllergies;
    }

    public String getActiveCriticalMedications() {
        return activeCriticalMedications;
    }

    public void setActiveCriticalMedications(String activeCriticalMedications) {
        this.activeCriticalMedications = activeCriticalMedications;
    }

    public String getChronicConditions() {
        return chronicConditions;
    }

    public void setChronicConditions(String chronicConditions) {
        this.chronicConditions = chronicConditions;
    }

    public String getEmergencyNotes() {
        return emergencyNotes;
    }

    public void setEmergencyNotes(String emergencyNotes) {
        this.emergencyNotes = emergencyNotes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
