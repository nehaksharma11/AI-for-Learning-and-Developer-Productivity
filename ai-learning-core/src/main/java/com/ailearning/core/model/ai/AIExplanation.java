package com.ailearning.core.model.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an AI-generated explanation of code.
 */
public class AIExplanation {
    
    private final String codeSnippet;
    private final String explanation;
    private final String summary;
    private final List<String> keyPoints;
    private final List<String> relatedConcepts;
    private final String difficulty;
    private final double confidenceScore;
    private final String language;
    private final LocalDateTime generatedAt;
    private final String serviceProvider;

    private AIExplanation(String codeSnippet, String explanation, String summary, 
                         List<String> keyPoints, List<String> relatedConcepts, 
                         String difficulty, double confidenceScore, String language,
                         LocalDateTime generatedAt, String serviceProvider) {
        this.codeSnippet = Objects.requireNonNull(codeSnippet, "Code snippet cannot be null");
        this.explanation = Objects.requireNonNull(explanation, "Explanation cannot be null");
        this.summary = summary != null ? summary : "";
        this.keyPoints = keyPoints != null ? new ArrayList<>(keyPoints) : new ArrayList<>();
        this.relatedConcepts = relatedConcepts != null ? new ArrayList<>(relatedConcepts) : new ArrayList<>();
        this.difficulty = difficulty != null ? difficulty : "UNKNOWN";
        this.confidenceScore = Math.max(0.0, Math.min(1.0, confidenceScore));
        this.language = language != null ? language : "UNKNOWN";
        this.generatedAt = generatedAt != null ? generatedAt : LocalDateTime.now();
        this.serviceProvider = serviceProvider != null ? serviceProvider : "UNKNOWN";
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getCodeSnippet() { return codeSnippet; }
    public String getExplanation() { return explanation; }
    public String getSummary() { return summary; }
    public List<String> getKeyPoints() { return new ArrayList<>(keyPoints); }
    public List<String> getRelatedConcepts() { return new ArrayList<>(relatedConcepts); }
    public String getDifficulty() { return difficulty; }
    public double getConfidenceScore() { return confidenceScore; }
    public String getLanguage() { return language; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public String getServiceProvider() { return serviceProvider; }

    public static class Builder {
        private String codeSnippet;
        private String explanation;
        private String summary;
        private List<String> keyPoints = new ArrayList<>();
        private List<String> relatedConcepts = new ArrayList<>();
        private String difficulty = "UNKNOWN";
        private double confidenceScore = 0.0;
        private String language = "UNKNOWN";
        private LocalDateTime generatedAt;
        private String serviceProvider = "UNKNOWN";

        public Builder codeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }
        public Builder keyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; return this; }
        public Builder relatedConcepts(List<String> relatedConcepts) { this.relatedConcepts = relatedConcepts; return this; }
        public Builder difficulty(String difficulty) { this.difficulty = difficulty; return this; }
        public Builder confidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }
        public Builder serviceProvider(String serviceProvider) { this.serviceProvider = serviceProvider; return this; }

        public AIExplanation build() {
            return new AIExplanation(codeSnippet, explanation, summary, keyPoints, relatedConcepts,
                    difficulty, confidenceScore, language, generatedAt, serviceProvider);
        }
    }

    @Override
    public String toString() {
        return "AIExplanation{" +
                "language='" + language + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", serviceProvider='" + serviceProvider + '\'' +
                '}';
    }
}