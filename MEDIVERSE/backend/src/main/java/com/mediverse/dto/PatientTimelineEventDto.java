package com.mediverse.dto;

import java.time.LocalDateTime;

public class PatientTimelineEventDto {

    private String eventId;
    private String eventType; // "CONSULTATION", "PRESCRIPTION", "LAB_ORDER", "MEDICAL_REPORT"
    private LocalDateTime eventDate;
    private String title;
    private String summary;
    private String providerName;
    private String category; // "CONSULT_ONLY", "LAB_ONLY"

    public PatientTimelineEventDto() {
    }

    public PatientTimelineEventDto(String eventId, String eventType, LocalDateTime eventDate,
                                  String title, String summary, String providerName, String category) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.title = title;
        this.summary = summary;
        this.providerName = providerName;
        this.category = category;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
