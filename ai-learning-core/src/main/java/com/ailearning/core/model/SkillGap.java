package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a skill gap identified for a developer.
 * Contains information about the gap, its priority, and recommended learning actions.
 */
public class SkillGap {
    
    @NotBlank
    private final String skillDomain;
    
    @DecimalMin(value = "0.0", message = "Current level must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Current level must be between 0.0 and 1.0")
    private final double currentLevel;
    
    @DecimalMin(value = "0.0", message = "Target level must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Target level must be between 0.0 and 1.0")
    private final double targetLevel;
    
    @NotNull
    private final GapPriority priority;
    
    @NotNull
    private final List<String> identificationReasons;
    
    @NotNull
    private final List<String> recommendedActions;
    
    private final String projectContext;
    
    @NotNull
    private final LocalDateTime identifiedAt;
    
    private final int estimatedLearningHours;

    @JsonCreator
    public SkillGap(
            @JsonProperty("skillDomain") String skillDomain,
            @JsonProperty("currentLevel") double currentLevel,
            @JsonProperty("targetLevel") double targetLevel,
            @JsonProperty("priority") GapPriority priority,
            @JsonProperty("identificationReasons") List<String> identificationReasons,
            @JsonProperty("recommendedActions") List<String> recommendedActions,
            @JsonProperty("projectContext") String projectContext,
            @JsonProperty("identifiedAt") LocalDateTime identifiedAt,
            @JsonProperty("estimatedLearningHours") int estimatedLearningHours) {
        this.skillDomain = Objects.requireNonNull(skillDomain, "Skill domain cannot be null");
        this.currentLevel = validateRange(currentLevel, "currentLevel");
        this.targetLevel = validateRange(targetLevel, "targetLevel");
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
        this.identificationReasons = identificationReasons != null ? new ArrayList<>(identificationReasons) : new ArrayList<>();
        this.recommendedActions = recommendedActions != null ? new ArrayList<>(recommendedActions) : new ArrayList<>();
        this.projectContext = projectContext;
        this.identifiedAt = identifiedAt != null ? identifiedAt : LocalDateTime.now();
        this.estimatedLearningHours = Math.max(0, estimatedLearningHours);
        
        if (targetLevel <= currentLevel) {
            throw new IllegalArgumentException("Target level must be higher than current level");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getGapSize() {
        return targetLevel - currentLevel;
    }

    public boolean isCritical() {
        return priority == GapPriority.CRITICAL || getGapSize() > 0.5;
    }

    public boolean isProjectRelated() {
        return projectContext != null && !projectContext.trim().isEmpty();
    }

    public SkillGap updatePriority(GapPriority newPriority) {
        return new SkillGap(skillDomain, currentLevel, targetLevel, newPriority, identificationReasons,
                recommendedActions, projectContext, identifiedAt, estimatedLearningHours);
    }

    public SkillGap addRecommendedAction(String action) {
        List<String> updatedActions = new ArrayList<>(this.recommendedActions);
        updatedActions.add(action);
        
        return new SkillGap(skillDomain, currentLevel, targetLevel, priority, identificationReasons,
                updatedActions, projectContext, identifiedAt, estimatedLearningHours);
    }

    private static double validateRange(double value, String fieldName) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(fieldName + " must be between 0.0 and 1.0, got: " + value);
        }
        return value;
    }

    // Getters
    public String getSkillDomain() { return skillDomain; }
    public double getCurrentLevel() { return currentLevel; }
    public double getTargetLevel() { return targetLevel; }
    public GapPriority getPriority() { return priority; }
    public List<String> getIdentificationReasons() { return new ArrayList<>(identificationReasons); }
    public List<String> getRecommendedActions() { return new ArrayList<>(recommendedActions); }
    public String getProjectContext() { return projectContext; }
    public LocalDateTime getIdentifiedAt() { return identifiedAt; }
    public int getEstimatedLearningHours() { return estimatedLearningHours; }

    public enum GapPriority {
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        CRITICAL(4);

        private final int level;

        GapPriority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    public static class Builder {
        private String skillDomain;
        private double currentLevel;
        private double targetLevel;
        private GapPriority priority = GapPriority.MEDIUM;
        private List<String> identificationReasons = new ArrayList<>();
        private List<String> recommendedActions = new ArrayList<>();
        private String projectContext;
        private LocalDateTime identifiedAt = LocalDateTime.now();
        private int estimatedLearningHours;

        public Builder skillDomain(String skillDomain) { this.skillDomain = skillDomain; return this; }
        public Builder currentLevel(double currentLevel) { this.currentLevel = currentLevel; return this; }
        public Builder targetLevel(double targetLevel) { this.targetLevel = targetLevel; return this; }
        public Builder priority(GapPriority priority) { this.priority = priority; return this; }
        public Builder identificationReasons(List<String> identificationReasons) { this.identificationReasons = identificationReasons; return this; }
        public Builder recommendedActions(List<String> recommendedActions) { this.recommendedActions = recommendedActions; return this; }
        public Builder projectContext(String projectContext) { this.projectContext = projectContext; return this; }
        public Builder identifiedAt(LocalDateTime identifiedAt) { this.identifiedAt = identifiedAt; return this; }
        public Builder estimatedLearningHours(int estimatedLearningHours) { this.estimatedLearningHours = estimatedLearningHours; return this; }

        public SkillGap build() {
            return new SkillGap(skillDomain, currentLevel, targetLevel, priority, identificationReasons,
                    recommendedActions, projectContext, identifiedAt, estimatedLearningHours);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillGap skillGap = (SkillGap) o;
        return Objects.equals(skillDomain, skillGap.skillDomain) &&
                Objects.equals(identifiedAt, skillGap.identifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillDomain, identifiedAt);
    }

    @Override
    public String toString() {
        return "SkillGap{" +
                "skillDomain='" + skillDomain + '\'' +
                ", gapSize=" + String.format("%.2f", getGapSize()) +
                ", priority=" + priority +
                ", estimatedHours=" + estimatedLearningHours +
                '}';
    }
}