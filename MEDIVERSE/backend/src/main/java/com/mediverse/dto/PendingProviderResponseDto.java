package com.mediverse.dto;

import java.time.LocalDateTime;

public class PendingProviderResponseDto {

    private String userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String accountStatus;
    private String verificationStatus;
    private String providerName;
    private String organizationName;
    private String licenseNumber;
    private String registrationNumber;
    private String specialization;
    private String hospitalAffiliation;
    private String facilityType;
    private String accreditationNumber;
    private LocalDateTime createdAt;

    public PendingProviderResponseDto() {
    }

    public PendingProviderResponseDto(
            String userId,
            String fullName,
            String email,
            String phoneNumber,
            String role,
            String accountStatus,
            String verificationStatus,
            String providerName,
            String organizationName,
            String licenseNumber,
            String registrationNumber,
            String specialization,
            String hospitalAffiliation,
            String facilityType,
            String accreditationNumber,
            LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.accountStatus = accountStatus;
        this.verificationStatus = verificationStatus;
        this.providerName = providerName;
        this.organizationName = organizationName;
        this.licenseNumber = licenseNumber;
        this.registrationNumber = registrationNumber;
        this.specialization = specialization;
        this.hospitalAffiliation = hospitalAffiliation;
        this.facilityType = facilityType;
        this.accreditationNumber = accreditationNumber;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getHospitalAffiliation() {
        return hospitalAffiliation;
    }

    public void setHospitalAffiliation(String hospitalAffiliation) {
        this.hospitalAffiliation = hospitalAffiliation;
    }

    public String getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }

    public String getAccreditationNumber() {
        return accreditationNumber;
    }

    public void setAccreditationNumber(String accreditationNumber) {
        this.accreditationNumber = accreditationNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
