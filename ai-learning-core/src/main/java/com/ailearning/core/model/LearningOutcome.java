package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a learning outcome achieved during a session.
 * Tracks specific skills or concepts learned and mastery level.
 */
public class LearningOutcome {
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String concept;
    
    @NotBlank
    private final String skillDomain;
    
    @DecimalMin(value = "0.0", message = "Mastery level must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Mastery level must be between 0.0 and 1.0")
    private final double masteryLevel;
    
    @NotNull
    private final OutcomeType type;
    
    private final String evidence;
    
    @NotNull
    private final LocalDateTime achievedAt;
    
    private final String feedback;
    
    private final boolean verified;

    @JsonCreator
    public LearningOutcome(
            @JsonProperty("id") String id,
            @JsonProperty("concept") String concept,
            @JsonProperty("skillDomain") String skillDomain,
            @JsonProperty("masteryLevel") double masteryLevel,
            @JsonProperty("type") OutcomeType type,
            @JsonProperty("evidence") String evidence,
            @JsonProperty("achievedAt") LocalDateTime achievedAt,
            @JsonProperty("feedback") String feedback,
            @JsonProperty("verified") boolean verified) {
        this.id = Objects.requireNonNull(id, "Outcome ID cannot be null");
        this.concept = Objects.requireNonNull(concept, "Concept cannot be null");
        this.skillDomain = Objects.requireNonNull(skillDomain, "Skill domain cannot be null");
        this.masteryLevel = validateRange(masteryLevel, "masteryLevel");
        this.type = Objects.requireNonNull(type, "Outcome type cannot be null");
        this.evidence = evidence;
        this.achievedAt = achievedAt != null ? achievedAt : LocalDateTime.now();
        this.feedback = feedback;
        this.verified = verified;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LearningOutcome conceptMastered(String id, String concept, String skillDomain, double masteryLevel) {
        return new LearningOutcome(id, concept, skillDomain, masteryLevel, OutcomeType.CONCEPT_MASTERED,
                null, LocalDateTime.now(), null, false);
    }

    public static LearningOutcome skillImproved(String id, String concept, String skillDomain, double masteryLevel) {
        return new LearningOutcome(id, concept, skillDomain, masteryLevel, OutcomeType.SKILL_IMPROVED,
                null, LocalDateTime.now(), null, false);
    }

    public boolean isHighMastery() {
        return masteryLevel >= 0.8;
    }

    public boolean needsReinforcement() {
        return masteryLevel < 0.6;
    }

    public double getAchievementScore() {
        return masteryLevel;
    }

    private static double validateRange(double value, String fieldName) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(fieldName + " must be between 0.0 and 1.0, got: " + value);
        }
        return value;
    }

    // Getters
    public String getId() { return id; }
    public String getConcept() { return concept; }
    public String getSkillDomain() { return skillDomain; }
    public double getMasteryLevel() { return masteryLevel; }
    public OutcomeType getType() { return type; }
    public String getEvidence() { return evidence; }
    public LocalDateTime getAchievedAt() { return achievedAt; }
    public String getFeedback() { return feedback; }
    public boolean isVerified() { return verified; }

    public enum OutcomeType {
        CONCEPT_MASTERED,
        SKILL_IMPROVED,
        KNOWLEDGE_GAINED,
        PROBLEM_SOLVED,
        PATTERN_RECOGNIZED,
        BEST_PRACTICE_LEARNED
    }

    public static class Builder {
        private String id;
        private String concept;
        private String skillDomain;
        private double masteryLevel;
        private OutcomeType type;
        private String evidence;
        private LocalDateTime achievedAt = LocalDateTime.now();
        private String feedback;
        private boolean verified = false;

        public Builder id(String id) { this.id = id; return this; }
        public Builder concept(String concept) { this.concept = concept; return this; }
        public Builder skillDomain(String skillDomain) { this.skillDomain = skillDomain; return this; }
        public Builder masteryLevel(double masteryLevel) { this.masteryLevel = masteryLevel; return this; }
        public Builder type(OutcomeType type) { this.type = type; return this; }
        public Builder evidence(String evidence) { this.evidence = evidence; return this; }
        public Builder achievedAt(LocalDateTime achievedAt) { this.achievedAt = achievedAt; return this; }
        public Builder feedback(String feedback) { this.feedback = feedback; return this; }
        public Builder verified(boolean verified) { this.verified = verified; return this; }

        public LearningOutcome build() {
            return new LearningOutcome(id, concept, skillDomain, masteryLevel, type, evidence,
                    achievedAt, feedback, verified);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningOutcome that = (LearningOutcome) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LearningOutcome{" +
                "id='" + id + '\'' +
                ", concept='" + concept + '\'' +
                ", skillDomain='" + skillDomain + '\'' +
                ", masteryLevel=" + String.format("%.2f", masteryLevel) +
                ", type=" + type +
                '}';
    }
}