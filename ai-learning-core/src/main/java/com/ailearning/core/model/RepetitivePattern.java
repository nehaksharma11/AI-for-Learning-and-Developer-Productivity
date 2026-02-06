package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a repetitive code pattern detected in the codebase.
 * Used to identify automation opportunities and suggest improvements.
 */
public class RepetitivePattern {
    
    public enum PatternType {
        BOILERPLATE_CODE, SIMILAR_METHODS, DUPLICATE_LOGIC, COPY_PASTE, CONFIGURATION_REPETITION
    }

    @NotBlank
    private final String id;
    
    @NotNull
    private final PatternType type;
    
    @NotBlank
    private final String description;
    
    @NotNull
    private final List<String> occurrences;
    
    @NotNull
    private final List<String> affectedFiles;
    
    private final String codeSnippet;
    
    private final int frequency;
    
    private final double similarity;
    
    private final String suggestedRefactoring;
    
    @NotNull
    private final LocalDateTime detectedAt;
    
    private final String projectContext;

    @JsonCreator
    public RepetitivePattern(
            @JsonProperty("id") String id,
            @JsonProperty("type") PatternType type,
            @JsonProperty("description") String description,
            @JsonProperty("occurrences") List<String> occurrences,
            @JsonProperty("affectedFiles") List<String> affectedFiles,
            @JsonProperty("codeSnippet") String codeSnippet,
            @JsonProperty("frequency") int frequency,
            @JsonProperty("similarity") double similarity,
            @JsonProperty("suggestedRefactoring") String suggestedRefactoring,
            @JsonProperty("detectedAt") LocalDateTime detectedAt,
            @JsonProperty("projectContext") String projectContext) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.occurrences = occurrences != null ? new ArrayList<>(occurrences) : new ArrayList<>();
        this.affectedFiles = affectedFiles != null ? new ArrayList<>(affectedFiles) : new ArrayList<>();
        this.codeSnippet = codeSnippet;
        this.frequency = Math.max(0, frequency);
        this.similarity = Math.max(0.0, Math.min(1.0, similarity));
        this.suggestedRefactoring = suggestedRefactoring;
        this.detectedAt = detectedAt != null ? detectedAt : LocalDateTime.now();
        this.projectContext = projectContext;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Determines if this pattern is significant enough to warrant automation.
     */
    public boolean isSignificant() {
        return frequency >= 3 && similarity >= 0.7;
    }

    /**
     * Calculates the automation potential score based on frequency and similarity.
     */
    public double getAutomationPotential() {
        return (frequency * similarity) / 10.0; // Normalized score
    }

    // Getters
    public String getId() { return id; }
    public PatternType getType() { return type; }
    public String getDescription() { return description; }
    public List<String> getOccurrences() { return new ArrayList<>(occurrences); }
    public List<String> getAffectedFiles() { return new ArrayList<>(affectedFiles); }
    public String getCodeSnippet() { return codeSnippet; }
    public int getFrequency() { return frequency; }
    public double getSimilarity() { return similarity; }
    public String getSuggestedRefactoring() { return suggestedRefactoring; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public String getProjectContext() { return projectContext; }

    public static class Builder {
        private String id;
        private PatternType type;
        private String description;
        private List<String> occurrences = new ArrayList<>();
        private List<String> affectedFiles = new ArrayList<>();
        private String codeSnippet;
        private int frequency = 0;
        private double similarity = 0.0;
        private String suggestedRefactoring;
        private LocalDateTime detectedAt = LocalDateTime.now();
        private String projectContext;

        public Builder id(String id) { this.id = id; return this; }
        public Builder type(PatternType type) { this.type = type; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder occurrences(List<String> occurrences) { this.occurrences = occurrences; return this; }
        public Builder affectedFiles(List<String> affectedFiles) { this.affectedFiles = affectedFiles; return this; }
        public Builder codeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; return this; }
        public Builder frequency(int frequency) { this.frequency = frequency; return this; }
        public Builder similarity(double similarity) { this.similarity = similarity; return this; }
        public Builder suggestedRefactoring(String suggestedRefactoring) { this.suggestedRefactoring = suggestedRefactoring; return this; }
        public Builder detectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; return this; }
        public Builder projectContext(String projectContext) { this.projectContext = projectContext; return this; }

        public RepetitivePattern build() {
            return new RepetitivePattern(id, type, description, occurrences, affectedFiles,
                    codeSnippet, frequency, similarity, suggestedRefactoring, detectedAt, projectContext);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepetitivePattern that = (RepetitivePattern) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RepetitivePattern{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", frequency=" + frequency +
                ", similarity=" + String.format("%.2f", similarity) +
                ", automationPotential=" + String.format("%.2f", getAutomationPotential()) +
                '}';
    }
}