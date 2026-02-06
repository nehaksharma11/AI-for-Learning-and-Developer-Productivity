package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the result of a skill assessment for a developer.
 * Contains detailed analysis of competency levels and evidence.
 */
public class SkillAssessment {
    
    @NotBlank
    private final String assessmentId;
    
    @NotBlank
    private final String developerId;
    
    @NotBlank
    private final String skillDomain;
    
    @DecimalMin(value = "0.0", message = "Proficiency score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Proficiency score must be between 0.0 and 1.0")
    private final double proficiencyScore;
    
    @DecimalMin(value = "0.0", message = "Confidence score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Confidence score must be between 0.0 and 1.0")
    private final double confidenceScore;
    
    @NotNull
    @Valid
    private final List<AssessmentEvidence> evidence;
    
    @NotNull
    private final Map<String, Double> subSkillScores;
    
    @NotNull
    private final AssessmentMethod method;
    
    @NotNull
    private final LocalDateTime assessedAt;
    
    private final String assessmentContext;
    
    @NotNull
    private final List<String> strengths;
    
    @NotNull
    private final List<String> weaknesses;
    
    @NotNull
    private final List<String> recommendations;

    @JsonCreator
    public SkillAssessment(
            @JsonProperty("assessmentId") String assessmentId,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("skillDomain") String skillDomain,
            @JsonProperty("proficiencyScore") double proficiencyScore,
            @JsonProperty("confidenceScore") double confidenceScore,
            @JsonProperty("evidence") List<AssessmentEvidence> evidence,
            @JsonProperty("subSkillScores") Map<String, Double> subSkillScores,
            @JsonProperty("method") AssessmentMethod method,
            @JsonProperty("assessedAt") LocalDateTime assessedAt,
            @JsonProperty("assessmentContext") String assessmentContext,
            @JsonProperty("strengths") List<String> strengths,
            @JsonProperty("weaknesses") List<String> weaknesses,
            @JsonProperty("recommendations") List<String> recommendations) {
        this.assessmentId = Objects.requireNonNull(assessmentId, "Assessment ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.skillDomain = Objects.requireNonNull(skillDomain, "Skill domain cannot be null");
        this.proficiencyScore = validateRange(proficiencyScore, "proficiencyScore");
        this.confidenceScore = validateRange(confidenceScore, "confidenceScore");
        this.evidence = evidence != null ? new ArrayList<>(evidence) : new ArrayList<>();
        this.subSkillScores = subSkillScores != null ? new HashMap<>(subSkillScores) : new HashMap<>();
        this.method = Objects.requireNonNull(method, "Assessment method cannot be null");
        this.assessedAt = assessedAt != null ? assessedAt : LocalDateTime.now();
        this.assessmentContext = assessmentContext;
        this.strengths = strengths != null ? new ArrayList<>(strengths) : new ArrayList<>();
        this.weaknesses = weaknesses != null ? new ArrayList<>(weaknesses) : new ArrayList<>();
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getOverallScore() {
        // Weight proficiency more heavily than confidence
        return (proficiencyScore * 0.7) + (confidenceScore * 0.3);
    }

    public SkillLevel.SkillCategory getSkillCategory() {
        double overall = getOverallScore();
        if (overall >= 0.8) return SkillLevel.SkillCategory.EXPERT;
        if (overall >= 0.6) return SkillLevel.SkillCategory.ADVANCED;
        if (overall >= 0.4) return SkillLevel.SkillCategory.INTERMEDIATE;
        return SkillLevel.SkillCategory.BEGINNER;
    }

    public boolean hasStrongEvidence() {
        return evidence.size() >= 3 && evidence.stream()
                .anyMatch(e -> e.getConfidence() >= 0.8);
    }

    public List<String> getTopSubSkills() {
        return subSkillScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getWeakSubSkills() {
        return subSkillScores.entrySet().stream()
                .filter(entry -> entry.getValue() < 0.4)
                .map(Map.Entry::getKey)
                .toList();
    }

    private static double validateRange(double value, String fieldName) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(fieldName + " must be between 0.0 and 1.0, got: " + value);
        }
        return value;
    }

    // Getters
    public String getAssessmentId() { return assessmentId; }
    public String getDeveloperId() { return developerId; }
    public String getSkillDomain() { return skillDomain; }
    public double getProficiencyScore() { return proficiencyScore; }
    public double getConfidenceScore() { return confidenceScore; }
    public List<AssessmentEvidence> getEvidence() { return new ArrayList<>(evidence); }
    public Map<String, Double> getSubSkillScores() { return new HashMap<>(subSkillScores); }
    public AssessmentMethod getMethod() { return method; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
    public String getAssessmentContext() { return assessmentContext; }
    public List<String> getStrengths() { return new ArrayList<>(strengths); }
    public List<String> getWeaknesses() { return new ArrayList<>(weaknesses); }
    public List<String> getRecommendations() { return new ArrayList<>(recommendations); }

    public enum AssessmentMethod {
        CODE_ANALYSIS,
        BAYESIAN_KNOWLEDGE_TRACING,
        PEER_COMPARISON,
        SELF_ASSESSMENT,
        PROJECT_ANALYSIS,
        QUIZ_RESULTS,
        COMBINED
    }

    public static class Builder {
        private String assessmentId;
        private String developerId;
        private String skillDomain;
        private double proficiencyScore;
        private double confidenceScore;
        private List<AssessmentEvidence> evidence = new ArrayList<>();
        private Map<String, Double> subSkillScores = new HashMap<>();
        private AssessmentMethod method = AssessmentMethod.CODE_ANALYSIS;
        private LocalDateTime assessedAt = LocalDateTime.now();
        private String assessmentContext;
        private List<String> strengths = new ArrayList<>();
        private List<String> weaknesses = new ArrayList<>();
        private List<String> recommendations = new ArrayList<>();

        public Builder assessmentId(String assessmentId) { this.assessmentId = assessmentId; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder skillDomain(String skillDomain) { this.skillDomain = skillDomain; return this; }
        public Builder proficiencyScore(double proficiencyScore) { this.proficiencyScore = proficiencyScore; return this; }
        public Builder confidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Builder evidence(List<AssessmentEvidence> evidence) { this.evidence = evidence; return this; }
        public Builder subSkillScores(Map<String, Double> subSkillScores) { this.subSkillScores = subSkillScores; return this; }
        public Builder method(AssessmentMethod method) { this.method = method; return this; }
        public Builder assessedAt(LocalDateTime assessedAt) { this.assessedAt = assessedAt; return this; }
        public Builder assessmentContext(String assessmentContext) { this.assessmentContext = assessmentContext; return this; }
        public Builder strengths(List<String> strengths) { this.strengths = strengths; return this; }
        public Builder weaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; return this; }
        public Builder recommendations(List<String> recommendations) { this.recommendations = recommendations; return this; }

        public SkillAssessment build() {
            return new SkillAssessment(assessmentId, developerId, skillDomain, proficiencyScore, confidenceScore,
                    evidence, subSkillScores, method, assessedAt, assessmentContext, strengths, weaknesses, recommendations);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillAssessment that = (SkillAssessment) o;
        return Objects.equals(assessmentId, that.assessmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assessmentId);
    }

    @Override
    public String toString() {
        return "SkillAssessment{" +
                "assessmentId='" + assessmentId + '\'' +
                ", skillDomain='" + skillDomain + '\'' +
                ", proficiencyScore=" + String.format("%.2f", proficiencyScore) +
                ", confidenceScore=" + String.format("%.2f", confidenceScore) +
                ", overallScore=" + String.format("%.2f", getOverallScore()) +
                ", category=" + getSkillCategory() +
                ", method=" + method +
                '}';
    }
}