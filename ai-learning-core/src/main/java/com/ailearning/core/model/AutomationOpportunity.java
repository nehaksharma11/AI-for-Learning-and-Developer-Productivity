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
 * Represents an automation opportunity identified in the codebase.
 * Contains information about repetitive patterns that could be automated.
 */
public class AutomationOpportunity {
    
    public enum OpportunityType {
        CODE_GENERATION, REFACTORING, TEMPLATE_CREATION, SCRIPT_AUTOMATION, BUILD_OPTIMIZATION
    }
    
    public enum Priority {
        LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);
        
        private final int level;
        
        Priority(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
    }

    @NotBlank
    private final String id;
    
    @NotNull
    private final OpportunityType type;
    
    @NotBlank
    private final String title;
    
    @NotBlank
    private final String description;
    
    @NotNull
    private final Priority priority;
    
    @NotNull
    private final List<String> affectedFiles;
    
    @NotNull
    private final List<CodePattern> detectedPatterns;
    
    private final String suggestedSolution;
    
    private final String automationScript;
    
    private final int estimatedTimeSavingsMinutes;
    
    private final double confidenceScore;
    
    @NotNull
    private final LocalDateTime detectedAt;
    
    private final String projectContext;

    @JsonCreator
    public AutomationOpportunity(
            @JsonProperty("id") String id,
            @JsonProperty("type") OpportunityType type,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("priority") Priority priority,
            @JsonProperty("affectedFiles") List<String> affectedFiles,
            @JsonProperty("detectedPatterns") List<CodePattern> detectedPatterns,
            @JsonProperty("suggestedSolution") String suggestedSolution,
            @JsonProperty("automationScript") String automationScript,
            @JsonProperty("estimatedTimeSavingsMinutes") int estimatedTimeSavingsMinutes,
            @JsonProperty("confidenceScore") double confidenceScore,
            @JsonProperty("detectedAt") LocalDateTime detectedAt,
            @JsonProperty("projectContext") String projectContext) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
        this.affectedFiles = affectedFiles != null ? new ArrayList<>(affectedFiles) : new ArrayList<>();
        this.detectedPatterns = detectedPatterns != null ? new ArrayList<>(detectedPatterns) : new ArrayList<>();
        this.suggestedSolution = suggestedSolution;
        this.automationScript = automationScript;
        this.estimatedTimeSavingsMinutes = Math.max(0, estimatedTimeSavingsMinutes);
        this.confidenceScore = Math.max(0.0, Math.min(1.0, confidenceScore));
        this.detectedAt = detectedAt != null ? detectedAt : LocalDateTime.now();
        this.projectContext = projectContext;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getId() { return id; }
    public OpportunityType getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public List<String> getAffectedFiles() { return new ArrayList<>(affectedFiles); }
    public List<CodePattern> getDetectedPatterns() { return new ArrayList<>(detectedPatterns); }
    public String getSuggestedSolution() { return suggestedSolution; }
    public String getAutomationScript() { return automationScript; }
    public int getEstimatedTimeSavingsMinutes() { return estimatedTimeSavingsMinutes; }
    public double getConfidenceScore() { return confidenceScore; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public String getProjectContext() { return projectContext; }

    public static class Builder {
        private String id;
        private OpportunityType type;
        private String title;
        private String description;
        private Priority priority = Priority.MEDIUM;
        private List<String> affectedFiles = new ArrayList<>();
        private List<CodePattern> detectedPatterns = new ArrayList<>();
        private String suggestedSolution;
        private String automationScript;
        private int estimatedTimeSavingsMinutes = 0;
        private double confidenceScore = 0.5;
        private LocalDateTime detectedAt = LocalDateTime.now();
        private String projectContext;

        public Builder id(String id) { this.id = id; return this; }
        public Builder type(OpportunityType type) { this.type = type; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder priority(Priority priority) { this.priority = priority; return this; }
        public Builder affectedFiles(List<String> affectedFiles) { this.affectedFiles = affectedFiles; return this; }
        public Builder detectedPatterns(List<CodePattern> detectedPatterns) { this.detectedPatterns = detectedPatterns; return this; }
        public Builder suggestedSolution(String suggestedSolution) { this.suggestedSolution = suggestedSolution; return this; }
        public Builder automationScript(String automationScript) { this.automationScript = automationScript; return this; }
        public Builder estimatedTimeSavingsMinutes(int estimatedTimeSavingsMinutes) { this.estimatedTimeSavingsMinutes = estimatedTimeSavingsMinutes; return this; }
        public Builder confidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Builder detectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; return this; }
        public Builder projectContext(String projectContext) { this.projectContext = projectContext; return this; }

        public AutomationOpportunity build() {
            return new AutomationOpportunity(id, type, title, description, priority, affectedFiles,
                    detectedPatterns, suggestedSolution, automationScript, estimatedTimeSavingsMinutes,
                    confidenceScore, detectedAt, projectContext);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutomationOpportunity that = (AutomationOpportunity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AutomationOpportunity{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", estimatedTimeSavings=" + estimatedTimeSavingsMinutes + "min" +
                ", confidence=" + String.format("%.2f", confidenceScore) +
                '}';
    }
}