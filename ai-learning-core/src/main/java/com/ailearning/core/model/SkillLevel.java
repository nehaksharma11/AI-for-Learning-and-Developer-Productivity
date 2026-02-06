package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a developer's skill level in a specific domain.
 * Tracks proficiency, confidence, and evidence of competency.
 */
public class SkillLevel {
    
    @NotBlank
    private final String domain;
    
    @DecimalMin(value = "0.0", message = "Proficiency must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Proficiency must be between 0.0 and 1.0")
    private final double proficiency;
    
    @DecimalMin(value = "0.0", message = "Confidence must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Confidence must be between 0.0 and 1.0")
    private final double confidence;
    
    @NotNull
    private final LocalDateTime lastAssessed;
    
    @Min(value = 0, message = "Evidence count cannot be negative")
    private final int evidenceCount;

    @JsonCreator
    public SkillLevel(
            @JsonProperty("domain") String domain,
            @JsonProperty("proficiency") double proficiency,
            @JsonProperty("confidence") double confidence,
            @JsonProperty("lastAssessed") LocalDateTime lastAssessed,
            @JsonProperty("evidenceCount") int evidenceCount) {
        this.domain = Objects.requireNonNull(domain, "Domain cannot be null");
        this.proficiency = validateRange(proficiency, "proficiency");
        this.confidence = validateRange(confidence, "confidence");
        this.lastAssessed = Objects.requireNonNull(lastAssessed, "Last assessed date cannot be null");
        this.evidenceCount = Math.max(0, evidenceCount);
    }

    public static SkillLevel beginner(String domain) {
        return new SkillLevel(domain, 0.1, 0.1, LocalDateTime.now(), 0);
    }

    public static SkillLevel intermediate(String domain) {
        return new SkillLevel(domain, 0.5, 0.6, LocalDateTime.now(), 5);
    }

    public static SkillLevel advanced(String domain) {
        return new SkillLevel(domain, 0.8, 0.9, LocalDateTime.now(), 15);
    }

    public SkillLevel updateProficiency(double newProficiency, int additionalEvidence) {
        return new SkillLevel(domain, newProficiency, confidence, 
                LocalDateTime.now(), evidenceCount + additionalEvidence);
    }

    public SkillLevel updateConfidence(double newConfidence) {
        return new SkillLevel(domain, proficiency, newConfidence, 
                LocalDateTime.now(), evidenceCount);
    }

    public SkillLevel addEvidence(int evidenceToAdd) {
        return new SkillLevel(domain, proficiency, confidence, 
                LocalDateTime.now(), evidenceCount + evidenceToAdd);
    }

    /**
     * Calculates overall skill score combining proficiency and confidence
     */
    public double getOverallScore() {
        // Weight proficiency more heavily than confidence
        return (proficiency * 0.7) + (confidence * 0.3);
    }

    /**
     * Determines if this skill level indicates expertise (high proficiency and confidence)
     */
    public boolean isExpert() {
        return proficiency >= 0.8 && confidence >= 0.8 && evidenceCount >= 10;
    }

    /**
     * Determines if this skill needs improvement (low proficiency or confidence)
     */
    public boolean needsImprovement() {
        return proficiency < 0.4 || confidence < 0.4;
    }

    public SkillCategory getCategory() {
        double overall = getOverallScore();
        if (overall >= 0.8) return SkillCategory.EXPERT;
        if (overall >= 0.6) return SkillCategory.ADVANCED;
        if (overall >= 0.4) return SkillCategory.INTERMEDIATE;
        return SkillCategory.BEGINNER;
    }

    public enum SkillCategory {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    private static double validateRange(double value, String fieldName) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(fieldName + " must be between 0.0 and 1.0, got: " + value);
        }
        return value;
    }

    // Getters
    public String getDomain() { return domain; }
    public double getProficiency() { return proficiency; }
    public double getConfidence() { return confidence; }
    public LocalDateTime getLastAssessed() { return lastAssessed; }
    public int getEvidenceCount() { return evidenceCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillLevel that = (SkillLevel) o;
        return Double.compare(that.proficiency, proficiency) == 0 &&
                Double.compare(that.confidence, confidence) == 0 &&
                evidenceCount == that.evidenceCount &&
                Objects.equals(domain, that.domain) &&
                Objects.equals(lastAssessed, that.lastAssessed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, proficiency, confidence, lastAssessed, evidenceCount);
    }

    @Override
    public String toString() {
        return "SkillLevel{" +
                "domain='" + domain + '\'' +
                ", proficiency=" + String.format("%.2f", proficiency) +
                ", confidence=" + String.format("%.2f", confidence) +
                ", evidenceCount=" + evidenceCount +
                ", overallScore=" + String.format("%.2f", getOverallScore()) +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String domain;
        private double proficiency;
        private double confidence;
        private LocalDateTime lastAssessed;
        private int evidenceCount;

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder proficiency(double proficiency) {
            this.proficiency = proficiency;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder lastAssessed(LocalDateTime lastAssessed) {
            this.lastAssessed = lastAssessed;
            return this;
        }

        public Builder evidenceCount(int evidenceCount) {
            this.evidenceCount = evidenceCount;
            return this;
        }

        public SkillLevel build() {
            return new SkillLevel(domain, proficiency, confidence, lastAssessed, evidenceCount);
        }
    }
}