package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a learning goal set by or for a developer.
 */
public class LearningGoal {
    
    public enum GoalStatus {
        ACTIVE, COMPLETED, PAUSED, CANCELLED
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String title;
    
    @NotBlank
    private final String description;
    
    @NotBlank
    private final String skillDomain;
    
    private final double targetProficiency;
    
    @NotNull
    private final LocalDateTime createdAt;
    
    private final LocalDateTime targetDate;
    
    @NotNull
    private final GoalStatus status;

    @JsonCreator
    public LearningGoal(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("skillDomain") String skillDomain,
            @JsonProperty("targetProficiency") double targetProficiency,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("targetDate") LocalDateTime targetDate,
            @JsonProperty("status") GoalStatus status) {
        this.id = Objects.requireNonNull(id, "Goal ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.skillDomain = Objects.requireNonNull(skillDomain, "Skill domain cannot be null");
        this.targetProficiency = Math.max(0.0, Math.min(1.0, targetProficiency));
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.targetDate = targetDate;
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public static LearningGoal create(String title, String description, String skillDomain, 
                                    double targetProficiency, LocalDateTime targetDate) {
        return new LearningGoal(
                java.util.UUID.randomUUID().toString(),
                title,
                description,
                skillDomain,
                targetProficiency,
                LocalDateTime.now(),
                targetDate,
                GoalStatus.ACTIVE
        );
    }

    public LearningGoal complete() {
        return new LearningGoal(id, title, description, skillDomain, targetProficiency,
                createdAt, targetDate, GoalStatus.COMPLETED);
    }

    public LearningGoal pause() {
        return new LearningGoal(id, title, description, skillDomain, targetProficiency,
                createdAt, targetDate, GoalStatus.PAUSED);
    }

    public boolean isOverdue() {
        return targetDate != null && LocalDateTime.now().isAfter(targetDate) && status == GoalStatus.ACTIVE;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSkillDomain() { return skillDomain; }
    public double getTargetProficiency() { return targetProficiency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getTargetDate() { return targetDate; }
    public GoalStatus getStatus() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningGoal that = (LearningGoal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LearningGoal{" +
                "title='" + title + '\'' +
                ", skillDomain='" + skillDomain + '\'' +
                ", targetProficiency=" + targetProficiency +
                ", status=" + status +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String title;
        private String description;
        private String skillDomain;
        private double targetProficiency = 0.7;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime targetDate;
        private GoalStatus status = GoalStatus.ACTIVE;

        public Builder id(String id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder skillDomain(String skillDomain) { this.skillDomain = skillDomain; return this; }
        public Builder targetProficiency(double targetProficiency) { this.targetProficiency = targetProficiency; return this; }
        public Builder targetSkillLevel(double targetSkillLevel) { this.targetProficiency = targetSkillLevel; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder targetDate(LocalDateTime targetDate) { this.targetDate = targetDate; return this; }
        public Builder status(GoalStatus status) { this.status = status; return this; }

        public LearningGoal build() {
            if (id == null) id = java.util.UUID.randomUUID().toString();
            return new LearningGoal(id, title, description, skillDomain, targetProficiency,
                    createdAt, targetDate, status);
        }
    }
}