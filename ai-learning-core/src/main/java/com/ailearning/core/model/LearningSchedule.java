package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an optimized learning schedule for a developer.
 * Contains scheduled sessions, reminders, and adaptive timing recommendations.
 */
public class LearningSchedule {
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String developerId;
    
    @NotNull
    @Valid
    private final List<ScheduledSession> scheduledSessions;
    
    @NotNull
    private final List<String> recommendedTimeSlots;
    
    private final int optimalSessionDuration;
    
    private final int recommendedFrequency;
    
    @NotNull
    private final LocalDateTime createdAt;
    
    @NotNull
    private final LocalDateTime validUntil;
    
    private final String scheduleRationale;

    @JsonCreator
    public LearningSchedule(
            @JsonProperty("id") String id,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("scheduledSessions") List<ScheduledSession> scheduledSessions,
            @JsonProperty("recommendedTimeSlots") List<String> recommendedTimeSlots,
            @JsonProperty("optimalSessionDuration") int optimalSessionDuration,
            @JsonProperty("recommendedFrequency") int recommendedFrequency,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("validUntil") LocalDateTime validUntil,
            @JsonProperty("scheduleRationale") String scheduleRationale) {
        this.id = Objects.requireNonNull(id, "Schedule ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.scheduledSessions = scheduledSessions != null ? new ArrayList<>(scheduledSessions) : new ArrayList<>();
        this.recommendedTimeSlots = recommendedTimeSlots != null ? new ArrayList<>(recommendedTimeSlots) : new ArrayList<>();
        this.optimalSessionDuration = Math.max(5, optimalSessionDuration);
        this.recommendedFrequency = Math.max(1, recommendedFrequency);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.validUntil = validUntil != null ? validUntil : LocalDateTime.now().plusDays(30);
        this.scheduleRationale = scheduleRationale;
    }

    public static Builder builder() {
        return new Builder();
    }

    public LearningSchedule addSession(ScheduledSession session) {
        List<ScheduledSession> updatedSessions = new ArrayList<>(this.scheduledSessions);
        updatedSessions.add(session);
        
        return new LearningSchedule(id, developerId, updatedSessions, recommendedTimeSlots,
                optimalSessionDuration, recommendedFrequency, createdAt, validUntil, scheduleRationale);
    }

    public boolean isValid() {
        return LocalDateTime.now().isBefore(validUntil);
    }

    public List<ScheduledSession> getUpcomingSessions() {
        LocalDateTime now = LocalDateTime.now();
        return scheduledSessions.stream()
                .filter(session -> session.getScheduledTime().isAfter(now))
                .toList();
    }

    // Getters
    public String getId() { return id; }
    public String getDeveloperId() { return developerId; }
    public List<ScheduledSession> getScheduledSessions() { return new ArrayList<>(scheduledSessions); }
    public List<String> getRecommendedTimeSlots() { return new ArrayList<>(recommendedTimeSlots); }
    public int getOptimalSessionDuration() { return optimalSessionDuration; }
    public int getRecommendedFrequency() { return recommendedFrequency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public String getScheduleRationale() { return scheduleRationale; }

    public static class Builder {
        private String id;
        private String developerId;
        private List<ScheduledSession> scheduledSessions = new ArrayList<>();
        private List<String> recommendedTimeSlots = new ArrayList<>();
        private int optimalSessionDuration = 30;
        private int recommendedFrequency = 3;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime validUntil = LocalDateTime.now().plusDays(30);
        private String scheduleRationale;

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder scheduledSessions(List<ScheduledSession> scheduledSessions) { this.scheduledSessions = scheduledSessions; return this; }
        public Builder recommendedTimeSlots(List<String> recommendedTimeSlots) { this.recommendedTimeSlots = recommendedTimeSlots; return this; }
        public Builder optimalSessionDuration(int optimalSessionDuration) { this.optimalSessionDuration = optimalSessionDuration; return this; }
        public Builder recommendedFrequency(int recommendedFrequency) { this.recommendedFrequency = recommendedFrequency; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder validUntil(LocalDateTime validUntil) { this.validUntil = validUntil; return this; }
        public Builder scheduleRationale(String scheduleRationale) { this.scheduleRationale = scheduleRationale; return this; }

        public LearningSchedule build() {
            return new LearningSchedule(id, developerId, scheduledSessions, recommendedTimeSlots,
                    optimalSessionDuration, recommendedFrequency, createdAt, validUntil, scheduleRationale);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningSchedule that = (LearningSchedule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LearningSchedule{" +
                "id='" + id + '\'' +
                ", sessionCount=" + scheduledSessions.size() +
                ", optimalDuration=" + optimalSessionDuration +
                ", frequency=" + recommendedFrequency +
                ", validUntil=" + validUntil +
                '}';
    }
}