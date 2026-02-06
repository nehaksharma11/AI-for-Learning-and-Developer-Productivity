package com.ailearning.core.model.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an AI-generated step-by-step breakdown of complex code.
 */
public class AIBreakdown {
    
    private final String originalCode;
    private final String overview;
    private final List<BreakdownStep> steps;
    private final List<String> prerequisites;
    private final List<String> learningObjectives;
    private final String complexity;
    private final double confidenceScore;
    private final String language;
    private final LocalDateTime generatedAt;
    private final String serviceProvider;

    private AIBreakdown(String originalCode, String overview, List<BreakdownStep> steps,
                       List<String> prerequisites, List<String> learningObjectives,
                       String complexity, double confidenceScore, String language,
                       LocalDateTime generatedAt, String serviceProvider) {
        this.originalCode = Objects.requireNonNull(originalCode, "Original code cannot be null");
        this.overview = Objects.requireNonNull(overview, "Overview cannot be null");
        this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
        this.prerequisites = prerequisites != null ? new ArrayList<>(prerequisites) : new ArrayList<>();
        this.learningObjectives = learningObjectives != null ? new ArrayList<>(learningObjectives) : new ArrayList<>();
        this.complexity = complexity != null ? complexity : "UNKNOWN";
        this.confidenceScore = Math.max(0.0, Math.min(1.0, confidenceScore));
        this.language = language != null ? language : "UNKNOWN";
        this.generatedAt = generatedAt != null ? generatedAt : LocalDateTime.now();
        this.serviceProvider = serviceProvider != null ? serviceProvider : "UNKNOWN";
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getOriginalCode() { return originalCode; }
    public String getOverview() { return overview; }
    public List<BreakdownStep> getSteps() { return new ArrayList<>(steps); }
    public List<String> getPrerequisites() { return new ArrayList<>(prerequisites); }
    public List<String> getLearningObjectives() { return new ArrayList<>(learningObjectives); }
    public String getComplexity() { return complexity; }
    public double getConfidenceScore() { return confidenceScore; }
    public String getLanguage() { return language; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public String getServiceProvider() { return serviceProvider; }

    public static class Builder {
        private String originalCode;
        private String overview;
        private List<BreakdownStep> steps = new ArrayList<>();
        private List<String> prerequisites = new ArrayList<>();
        private List<String> learningObjectives = new ArrayList<>();
        private String complexity = "UNKNOWN";
        private double confidenceScore = 0.0;
        private String language = "UNKNOWN";
        private LocalDateTime generatedAt;
        private String serviceProvider = "UNKNOWN";

        public Builder originalCode(String originalCode) { this.originalCode = originalCode; return this; }
        public Builder overview(String overview) { this.overview = overview; return this; }
        public Builder steps(List<BreakdownStep> steps) { this.steps = steps; return this; }
        public Builder prerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; return this; }
        public Builder learningObjectives(List<String> learningObjectives) { this.learningObjectives = learningObjectives; return this; }
        public Builder complexity(String complexity) { this.complexity = complexity; return this; }
        public Builder confidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }
        public Builder serviceProvider(String serviceProvider) { this.serviceProvider = serviceProvider; return this; }

        public AIBreakdown build() {
            return new AIBreakdown(originalCode, overview, steps, prerequisites, learningObjectives,
                    complexity, confidenceScore, language, generatedAt, serviceProvider);
        }
    }

    @Override
    public String toString() {
        return "AIBreakdown{" +
                "complexity='" + complexity + '\'' +
                ", stepCount=" + steps.size() +
                ", language='" + language + '\'' +
                ", confidenceScore=" + confidenceScore +
                '}';
    }
}

/**
 * Represents a single step in a code breakdown.
 */
class BreakdownStep {
    
    private final int stepNumber;
    private final String title;
    private final String description;
    private final String codeFragment;
    private final List<String> keyPoints;
    private final String difficulty;

    private BreakdownStep(int stepNumber, String title, String description, String codeFragment,
                         List<String> keyPoints, String difficulty) {
        this.stepNumber = stepNumber;
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.codeFragment = codeFragment;
        this.keyPoints = keyPoints != null ? new ArrayList<>(keyPoints) : new ArrayList<>();
        this.difficulty = difficulty != null ? difficulty : "UNKNOWN";
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public int getStepNumber() { return stepNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCodeFragment() { return codeFragment; }
    public List<String> getKeyPoints() { return new ArrayList<>(keyPoints); }
    public String getDifficulty() { return difficulty; }

    public static class Builder {
        private int stepNumber;
        private String title;
        private String description;
        private String codeFragment;
        private List<String> keyPoints = new ArrayList<>();
        private String difficulty = "UNKNOWN";

        public Builder stepNumber(int stepNumber) { this.stepNumber = stepNumber; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder codeFragment(String codeFragment) { this.codeFragment = codeFragment; return this; }
        public Builder keyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; return this; }
        public Builder difficulty(String difficulty) { this.difficulty = difficulty; return this; }

        public BreakdownStep build() {
            return new BreakdownStep(stepNumber, title, description, codeFragment, keyPoints, difficulty);
        }
    }

    @Override
    public String toString() {
        return "BreakdownStep{" +
                "stepNumber=" + stepNumber +
                ", title='" + title + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}