package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a scheduled learning session.
 * Contains timing, topic, and session configuration information.
 */
public class ScheduledSession {
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String topic;
    
    @NotNull
    private final LocalDateTime scheduledTime;
    
    private final int durationMinutes;
    
    private final SessionType type;
    
    private final double difficulty;
    
    private final String description;
    
    private final boolean reminderSent;
    
    private final String learningPathId;

    @JsonCreator
    public ScheduledSession(
            @JsonProperty("id") String id,
            @JsonProperty("topic") String topic,
            @JsonProperty("scheduledTime") LocalDateTime scheduledTime,
            @JsonProperty("durationMinutes") int durationMinutes,
            @JsonProperty("type") SessionType type,
            @JsonProperty("difficulty") double difficulty,
            @JsonProperty("description") String description,
            @JsonProperty("reminderSent") boolean reminderSent,
            @JsonProperty("learningPathId") String learningPathId) {
        this.id = Objects.requireNonNull(id, "Session ID cannot be null");
        this.topic = Objects.requireNonNull(topic, "Topic cannot be null");
        this.scheduledTime = Objects.requireNonNull(scheduledTime, "Scheduled time cannot be null");
        this.durationMinutes = Math.max(5, durationMinutes);
        this.type = type != null ? type : SessionType.REGULAR;
        this.difficulty = Math.max(0.0, Math.min(1.0, difficulty));
        this.description = description;
        this.reminderSent = reminderSent;
        this.learningPathId = learningPathId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isUpcoming() {
        return scheduledTime.isAfter(LocalDateTime.now());
    }

    public boolean isDue() {
        LocalDateTime now = LocalDateTime.now();
        return scheduledTime.isBefore(now) && scheduledTime.plusMinutes(durationMinutes).isAfter(now);
    }

    public boolean isOverdue() {
        return scheduledTime.plusMinutes(durationMinutes).isBefore(LocalDateTime.now());
    }

    // Getters
    public String getId() { return id; }
    public String getTopic() { return topic; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public SessionType getType() { return type; }
    public double getDifficulty() { return difficulty; }
    public String getDescription() { return description; }
    public boolean isReminderSent() { return reminderSent; }
    public String getLearningPathId() { return learningPathId; }

    public enum SessionType {
        REGULAR,
        REVIEW,
        REINFORCEMENT,
        ASSESSMENT,
        PROJECT_BASED
    }

    public static class Builder {
        private String id;
        private String developerId;
        private String topic;
        private LocalDateTime scheduledTime;
        private int durationMinutes = 30;
        private int estimatedDuration = 30;
        private SessionType type = SessionType.REGULAR;
        private double difficulty = 0.5;
        private String priority = "medium";
        private String sessionType = "regular";
        private String description;
        private boolean reminderSent = false;
        private String learningPathId;

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder scheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; return this; }
        public Builder durationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder estimatedDuration(int estimatedDuration) { this.estimatedDuration = estimatedDuration; this.durationMinutes = estimatedDuration; return this; }
        public Builder type(SessionType type) { this.type = type; return this; }
        public Builder difficulty(double difficulty) { this.difficulty = difficulty; return this; }
        public Builder priority(String priority) { this.priority = priority; return this; }
        public Builder sessionType(String sessionType) { this.sessionType = sessionType; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder reminderSent(boolean reminderSent) { this.reminderSent = reminderSent; return this; }
        public Builder learningPathId(String learningPathId) { this.learningPathId = learningPathId; return this; }

        public ScheduledSession build() {
            if (topic == null) topic = "General Learning";
            return new ScheduledSession(id, topic, scheduledTime, durationMinutes, type, difficulty,
                    description, reminderSent, learningPathId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledSession that = (ScheduledSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ScheduledSession{" +
                "id='" + id + '\'' +
                ", topic='" + topic + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", durationMinutes=" + durationMinutes +
                ", type=" + type +
                '}';
    }
}