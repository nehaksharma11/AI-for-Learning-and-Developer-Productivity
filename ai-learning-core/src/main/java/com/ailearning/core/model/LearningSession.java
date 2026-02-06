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
 * Represents an active learning session for a developer.
 * Contains session metadata, content, and progress tracking.
 */
public class LearningSession {
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String developerId;
    
    @NotBlank
    private final String topic;
    
    @NotNull
    @Valid
    private final List<LearningContent> content;
    
    @NotNull
    private final List<SessionInteraction> interactions;
    
    @NotNull
    private final LocalDateTime startTime;
    
    private final LocalDateTime endTime;
    
    private final int durationMinutes;
    
    @NotNull
    private final List<LearningOutcome> outcomes;
    
    private final SessionStatus status;
    
    private final double difficultyLevel;
    
    private final String sessionType;

    @JsonCreator
    public LearningSession(
            @JsonProperty("id") String id,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("topic") String topic,
            @JsonProperty("content") List<LearningContent> content,
            @JsonProperty("interactions") List<SessionInteraction> interactions,
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("endTime") LocalDateTime endTime,
            @JsonProperty("durationMinutes") int durationMinutes,
            @JsonProperty("outcomes") List<LearningOutcome> outcomes,
            @JsonProperty("status") SessionStatus status,
            @JsonProperty("difficultyLevel") double difficultyLevel,
            @JsonProperty("sessionType") String sessionType) {
        this.id = Objects.requireNonNull(id, "Session ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.topic = Objects.requireNonNull(topic, "Topic cannot be null");
        this.content = content != null ? new ArrayList<>(content) : new ArrayList<>();
        this.interactions = interactions != null ? new ArrayList<>(interactions) : new ArrayList<>();
        this.startTime = startTime != null ? startTime : LocalDateTime.now();
        this.endTime = endTime;
        this.durationMinutes = Math.max(0, durationMinutes);
        this.outcomes = outcomes != null ? new ArrayList<>(outcomes) : new ArrayList<>();
        this.status = status != null ? status : SessionStatus.IN_PROGRESS;
        this.difficultyLevel = Math.max(0.0, Math.min(1.0, difficultyLevel));
        this.sessionType = sessionType != null ? sessionType : "general";
    }

    public static Builder builder() {
        return new Builder();
    }

    public LearningSession complete(List<LearningOutcome> sessionOutcomes) {
        return new LearningSession(id, developerId, topic, content, interactions, startTime,
                LocalDateTime.now(), calculateDuration(), sessionOutcomes, SessionStatus.COMPLETED,
                difficultyLevel, sessionType);
    }

    public LearningSession addInteraction(SessionInteraction interaction) {
        List<SessionInteraction> updatedInteractions = new ArrayList<>(this.interactions);
        updatedInteractions.add(interaction);
        
        return new LearningSession(id, developerId, topic, content, updatedInteractions, startTime,
                endTime, durationMinutes, outcomes, status, difficultyLevel, sessionType);
    }

    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }

    public boolean isActive() {
        return status == SessionStatus.IN_PROGRESS;
    }

    private int calculateDuration() {
        if (endTime != null) {
            return (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return (int) java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
    }

    // Getters
    public String getId() { return id; }
    public String getDeveloperId() { return developerId; }
    public String getTopic() { return topic; }
    public List<LearningContent> getContent() { return new ArrayList<>(content); }
    public List<SessionInteraction> getInteractions() { return new ArrayList<>(interactions); }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public List<LearningOutcome> getOutcomes() { return new ArrayList<>(outcomes); }
    public SessionStatus getStatus() { return status; }
    public double getDifficultyLevel() { return difficultyLevel; }
    public String getSessionType() { return sessionType; }

    public enum SessionStatus {
        SCHEDULED, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED
    }

    public static class Builder {
        private String id;
        private String developerId;
        private String topic;
        private List<LearningContent> content = new ArrayList<>();
        private List<SessionInteraction> interactions = new ArrayList<>();
        private LocalDateTime startTime = LocalDateTime.now();
        private LocalDateTime endTime;
        private int durationMinutes = 0;
        private List<LearningOutcome> outcomes = new ArrayList<>();
        private SessionStatus status = SessionStatus.IN_PROGRESS;
        private double difficultyLevel = 0.5;
        private String sessionType = "general";

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder content(List<LearningContent> content) { this.content = content; return this; }
        public Builder interactions(List<SessionInteraction> interactions) { this.interactions = interactions; return this; }
        public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public Builder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public Builder durationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder outcomes(List<LearningOutcome> outcomes) { this.outcomes = outcomes; return this; }
        public Builder status(SessionStatus status) { this.status = status; return this; }
        public Builder difficultyLevel(double difficultyLevel) { this.difficultyLevel = difficultyLevel; return this; }
        public Builder sessionType(String sessionType) { this.sessionType = sessionType; return this; }

        public LearningSession build() {
            return new LearningSession(id, developerId, topic, content, interactions, startTime,
                    endTime, durationMinutes, outcomes, status, difficultyLevel, sessionType);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningSession that = (LearningSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LearningSession{" +
                "id='" + id + '\'' +
                ", topic='" + topic + '\'' +
                ", status=" + status +
                ", durationMinutes=" + durationMinutes +
                ", difficultyLevel=" + String.format("%.2f", difficultyLevel) +
                '}';
    }
}