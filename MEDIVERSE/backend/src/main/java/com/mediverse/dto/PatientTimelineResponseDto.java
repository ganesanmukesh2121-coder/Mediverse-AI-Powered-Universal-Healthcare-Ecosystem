package com.mediverse.dto;

import java.util.ArrayList;
import java.util.List;

public class PatientTimelineResponseDto {

    private String patientId;
    private String healthId;
    private String authorizedCategory; // "FULL_TIMELINE", "LAB_ONLY", "CONSULT_ONLY"
    private List<PatientTimelineEventDto> events = new ArrayList<>();

    public PatientTimelineResponseDto() {
    }

    public PatientTimelineResponseDto(String patientId, String healthId, String authorizedCategory, List<PatientTimelineEventDto> events) {
        this.patientId = patientId;
        this.healthId = healthId;
        this.authorizedCategory = authorizedCategory;
        this.events = events != null ? events : new ArrayList<>();
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getAuthorizedCategory() {
        return authorizedCategory;
    }

    public void setAuthorizedCategory(String authorizedCategory) {
        this.authorizedCategory = authorizedCategory;
    }

    public List<PatientTimelineEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<PatientTimelineEventDto> events) {
        this.events = events;
    }
}
