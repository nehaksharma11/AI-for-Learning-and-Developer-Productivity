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
 * Represents evidence used in skill assessment.
 * Contains information about code samples, projects, or other artifacts that demonstrate skill.
 */
public class AssessmentEvidence {
    
    @NotBlank
    private final String id;
    
    @NotNull
    private final EvidenceType type;
    
    @NotBlank
    private final String description;
    
    private final String sourceCode;
    
    private final String filePath;
    
    private final String projectContext;
    
    @DecimalMin(value = "0.0", message = "Confidence must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Confidence must be between 0.0 and 1.0")
    private final double confidence;
    
    @DecimalMin(value = "0.0", message = "Quality score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Quality score must be between 0.0 and 1.0")
    private final double qualityScore;
    
    @NotNull
    private final LocalDateTime collectedAt;
    
    private final String analysisNotes;

    @JsonCreator
    public AssessmentEvidence(
            @JsonProperty("id") String id,
            @JsonProperty("type") EvidenceType type,
            @JsonProperty("description") String description,
            @JsonProperty("sourceCode") String sourceCode,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("projectContext") String projectContext,
            @JsonProperty("confidence") double confidence,
            @JsonProperty("qualityScore") double qualityScore,
            @JsonProperty("collectedAt") LocalDateTime collectedAt,
            @JsonProperty("analysisNotes") String analysisNotes) {
        this.id = Objects.requireNonNull(id, "Evidence ID cannot be null");
        this.type = Objects.requireNonNull(type, "Evidence type cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.sourceCode = sourceCode;
        this.filePath = filePath;
        this.projectContext = projectContext;
        this.confidence = validateRange(confidence, "confidence");
        this.qualityScore = validateRange(qualityScore, "qualityScore");
        this.collectedAt = collectedAt != null ? collectedAt : LocalDateTime.now();
        this.analysisNotes = analysisNotes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static AssessmentEvidence codeEvidence(String id, String description, String sourceCode, String filePath, double qualityScore) {
        return new AssessmentEvidence(id, EvidenceType.CODE_SAMPLE, description, sourceCode, filePath, null, 
                qualityScore, qualityScore, LocalDateTime.now(), null);
    }

    public static AssessmentEvidence projectEvidence(String id, String description, String projectContext, double qualityScore) {
        return new AssessmentEvidence(id, EvidenceType.PROJECT_CONTRIBUTION, description, null, null, projectContext, 
                qualityScore, qualityScore, LocalDateTime.now(), null);
    }

    public boolean hasSourceCode() {
        return sourceCode != null && !sourceCode.trim().isEmpty();
    }

    public boolean isHighQuality() {
        return qualityScore >= 0.7 && confidence >= 0.7;
    }

    public boolean isRecentEvidence() {
        return collectedAt.isAfter(LocalDateTime.now().minusDays(30));
    }

    private static double validateRange(double value, String fieldName) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(fieldName + " must be between 0.0 and 1.0, got: " + value);
        }
        return value;
    }

    // Getters
    public String getId() { return id; }
    public EvidenceType getType() { return type; }
    public String getDescription() { return description; }
    public String getSourceCode() { return sourceCode; }
    public String getFilePath() { return filePath; }
    public String getProjectContext() { return projectContext; }
    public double getConfidence() { return confidence; }
    public double getQualityScore() { return qualityScore; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public String getAnalysisNotes() { return analysisNotes; }

    public enum EvidenceType {
        CODE_SAMPLE,
        PROJECT_CONTRIBUTION,
        QUIZ_RESULT,
        PEER_REVIEW,
        SELF_ASSESSMENT,
        DOCUMENTATION,
        TEST_COVERAGE,
        PERFORMANCE_METRIC
    }

    public static class Builder {
        private String id;
        private EvidenceType type;
        private String description;
        private String sourceCode;
        private String filePath;
        private String projectContext;
        private double confidence = 0.5;
        private double qualityScore = 0.5;
        private LocalDateTime collectedAt = LocalDateTime.now();
        private String analysisNotes;

        public Builder id(String id) { this.id = id; return this; }
        public Builder type(EvidenceType type) { this.type = type; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder sourceCode(String sourceCode) { this.sourceCode = sourceCode; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder projectContext(String projectContext) { this.projectContext = projectContext; return this; }
        public Builder confidence(double confidence) { this.confidence = confidence; return this; }
        public Builder qualityScore(double qualityScore) { this.qualityScore = qualityScore; return this; }
        public Builder collectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; return this; }
        public Builder analysisNotes(String analysisNotes) { this.analysisNotes = analysisNotes; return this; }

        public AssessmentEvidence build() {
            return new AssessmentEvidence(id, type, description, sourceCode, filePath, projectContext,
                    confidence, qualityScore, collectedAt, analysisNotes);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssessmentEvidence that = (AssessmentEvidence) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AssessmentEvidence{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", confidence=" + String.format("%.2f", confidence) +
                ", qualityScore=" + String.format("%.2f", qualityScore) +
                ", collectedAt=" + collectedAt +
                '}';
    }
}