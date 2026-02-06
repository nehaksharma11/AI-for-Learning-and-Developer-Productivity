package com.ailearning.core.model.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an AI-generated code example.
 */
public class AIExample {
    
    private final String title;
    private final String description;
    private final String codeExample;
    private final String language;
    private final List<String> tags;
    private final String difficulty;
    private final boolean isProjectSpecific;
    private final String sourceFile;
    private final double relevanceScore;
    private final LocalDateTime generatedAt;
    private final String serviceProvider;

    private AIExample(String title, String description, String codeExample, String language,
                     List<String> tags, String difficulty, boolean isProjectSpecific,
                     String sourceFile, double relevanceScore, LocalDateTime generatedAt,
                     String serviceProvider) {
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.codeExample = Objects.requireNonNull(codeExample, "Code example cannot be null");
        this.language = language != null ? language : "UNKNOWN";
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.difficulty = difficulty != null ? difficulty : "UNKNOWN";
        this.isProjectSpecific = isProjectSpecific;
        this.sourceFile = sourceFile;
        this.relevanceScore = Math.max(0.0, Math.min(1.0, relevanceScore));
        this.generatedAt = generatedAt != null ? generatedAt : LocalDateTime.now();
        this.serviceProvider = serviceProvider != null ? serviceProvider : "UNKNOWN";
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCodeExample() { return codeExample; }
    public String getLanguage() { return language; }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public String getDifficulty() { return difficulty; }
    public boolean isProjectSpecific() { return isProjectSpecific; }
    public String getSourceFile() { return sourceFile; }
    public double getRelevanceScore() { return relevanceScore; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public String getServiceProvider() { return serviceProvider; }

    public static class Builder {
        private String title;
        private String description;
        private String codeExample;
        private String language = "UNKNOWN";
        private List<String> tags = new ArrayList<>();
        private String difficulty = "UNKNOWN";
        private boolean isProjectSpecific = false;
        private String sourceFile;
        private double relevanceScore = 0.0;
        private LocalDateTime generatedAt;
        private String serviceProvider = "UNKNOWN";

        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder codeExample(String codeExample) { this.codeExample = codeExample; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder difficulty(String difficulty) { this.difficulty = difficulty; return this; }
        public Builder isProjectSpecific(boolean isProjectSpecific) { this.isProjectSpecific = isProjectSpecific; return this; }
        public Builder sourceFile(String sourceFile) { this.sourceFile = sourceFile; return this; }
        public Builder relevanceScore(double relevanceScore) { this.relevanceScore = relevanceScore; return this; }
        public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }
        public Builder serviceProvider(String serviceProvider) { this.serviceProvider = serviceProvider; return this; }

        public AIExample build() {
            return new AIExample(title, description, codeExample, language, tags, difficulty,
                    isProjectSpecific, sourceFile, relevanceScore, generatedAt, serviceProvider);
        }
    }

    @Override
    public String toString() {
        return "AIExample{" +
                "title='" + title + '\'' +
                ", language='" + language + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", isProjectSpecific=" + isProjectSpecific +
                ", relevanceScore=" + relevanceScore +
                '}';
    }
}