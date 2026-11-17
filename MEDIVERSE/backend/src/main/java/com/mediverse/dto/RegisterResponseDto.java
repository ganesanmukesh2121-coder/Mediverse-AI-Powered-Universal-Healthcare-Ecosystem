package com.mediverse.dto;

public class RegisterResponseDto {

    private String userId;
    private String email;
    private String fullName;
    private String role;
    private String accountStatus;
    private String healthId;
    private String message;

    public RegisterResponseDto() {
    }

    public RegisterResponseDto(String userId, String email, String fullName, String role, String accountStatus, String healthId, String message) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.accountStatus = accountStatus;
        this.healthId = healthId;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
