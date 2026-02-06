package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents a suggestion for code improvement.
 * Suggestions are generated based on analysis results and best practices.
 */
public final class Suggestion {
    
    public enum Type {
        REFACTORING, OPTIMIZATION, BEST_PRACTICE, SECURITY, DOCUMENTATION, 
        TESTING, PERFORMANCE, ARCHITECTURE, LEARNING
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String title;
    
    @NotBlank
    private final String description;
    
    @NotNull
    private final Type type;
    
    @NotNull
    private final Priority priority;
    
    private final String category;
    private final String filePath;
    private final Integer lineNumber;
    private final Integer columnNumber;
    private final String codeExample;
    private final String example;
    private final String rationale;
    private final String learnMoreUrl;
    private final String estimatedImpact;
    private final List<String> tags;
    private final double confidenceScore;

    @JsonCreator
    public Suggestion(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("type") Type type,
            @JsonProperty("priority") Priority priority,
            @JsonProperty("category") String category,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("lineNumber") Integer lineNumber,
            @JsonProperty("columnNumber") Integer columnNumber,
            @JsonProperty("codeExample") String codeExample,
            @JsonProperty("example") String example,
            @JsonProperty("rationale") String rationale,
            @JsonProperty("learnMoreUrl") String learnMoreUrl,
            @JsonProperty("estimatedImpact") String estimatedImpact,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("confidenceScore") double confidenceScore) {
        this.id = Objects.requireNonNull(id, "Suggestion ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.type = Objects.requireNonNull(type, "Suggestion type cannot be null");
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
        this.category = category;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.codeExample = codeExample;
        this.example = example;
        this.rationale = rationale;
        this.learnMoreUrl = learnMoreUrl;
        this.estimatedImpact = estimatedImpact;
        this.tags = tags != null ? List.copyOf(tags) : List.of();
        this.confidenceScore = Math.max(0.0, Math.min(1.0, confidenceScore));
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String title;
        private String description;
        private Type type;
        private Priority priority;
        private String category;
        private String filePath;
        private Integer lineNumber;
        private Integer columnNumber;
        private String codeExample;
        private String example;
        private String rationale;
        private String learnMoreUrl;
        private String estimatedImpact;
        private List<String> tags;
        private double confidenceScore = 0.8; // Default confidence
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder type(Type type) { this.type = type; return this; }
        public Builder priority(Priority priority) { this.priority = priority; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder lineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return this; }
        public Builder columnNumber(Integer columnNumber) { this.columnNumber = columnNumber; return this; }
        public Builder codeExample(String codeExample) { this.codeExample = codeExample; return this; }
        public Builder example(String example) { this.example = example; return this; }
        public Builder rationale(String rationale) { this.rationale = rationale; return this; }
        public Builder learnMoreUrl(String learnMoreUrl) { this.learnMoreUrl = learnMoreUrl; return this; }
        public Builder estimatedImpact(String estimatedImpact) { this.estimatedImpact = estimatedImpact; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder confidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        
        public Suggestion build() {
            if (id == null) id = java.util.UUID.randomUUID().toString();
            return new Suggestion(id, title, description, type, priority, category,
                    filePath, lineNumber, columnNumber, codeExample, example, rationale,
                    learnMoreUrl, estimatedImpact, tags, confidenceScore);
        }
    }

    // Convenience factory methods
    public static Suggestion refactoring(String title, String description, String filePath, 
                                       Integer lineNumber, String example, String rationale) {
        return builder()
                .title(title)
                .description(description)
                .type(Type.REFACTORING)
                .priority(Priority.MEDIUM)
                .category("Refactoring")
                .filePath(filePath)
                .lineNumber(lineNumber)
                .example(example)
                .rationale(rationale)
                .build();
    }

    public static Suggestion performance(String title, String description, Priority priority, 
                                       String category, String example, String estimatedImpact) {
        return builder()
                .title(title)
                .description(description)
                .type(Type.PERFORMANCE)
                .priority(priority)
                .category(category)
                .example(example)
                .estimatedImpact(estimatedImpact)
                .build();
    }

    public static Suggestion bestPractice(String title, String description, String category, 
                                        String example, String learnMoreUrl) {
        return builder()
                .title(title)
                .description(description)
                .type(Type.BEST_PRACTICE)
                .priority(Priority.MEDIUM)
                .category(category)
                .example(example)
                .learnMoreUrl(learnMoreUrl)
                .build();
    }

    public static Suggestion security(String title, String description, String filePath, 
                                    Integer lineNumber, String rationale) {
        return builder()
                .title(title)
                .description(description)
                .type(Type.SECURITY)
                .priority(Priority.CRITICAL)
                .category("Security")
                .filePath(filePath)
                .lineNumber(lineNumber)
                .rationale(rationale)
                .build();
    }

    public static Suggestion architecture(String title, String description, String category, 
                                        String rationale, String learnMoreUrl) {
        return builder()
                .title(title)
                .description(description)
                .type(Type.ARCHITECTURE)
                .priority(Priority.HIGH)
                .category(category)
                .rationale(rationale)
                .learnMoreUrl(learnMoreUrl)
                .build();
    }

    public static Suggestion learning(String title, String description, String category, 
                                    String learnMoreUrl, List<String> tags) {
        return builder()
                .title(title)
                .description(description)
                .type(Type.LEARNING)
                .priority(Priority.LOW)
                .category(category)
                .learnMoreUrl(learnMoreUrl)
                .tags(tags)
                .build();
    }

    /**
     * Checks if this suggestion should be acted upon immediately
     */
    public boolean isUrgent() {
        return priority == Priority.CRITICAL || 
               (priority == Priority.HIGH && type == Type.SECURITY);
    }

    /**
     * Checks if this suggestion is related to learning and improvement
     */
    public boolean isLearningOpportunity() {
        return type == Type.BEST_PRACTICE || 
               type == Type.DOCUMENTATION ||
               type == Type.LEARNING ||
               learnMoreUrl != null;
    }

    /**
     * Gets the location string if available
     */
    public String getLocationString() {
        if (filePath != null && lineNumber != null && lineNumber > 0) {
            return filePath + ":" + lineNumber;
        } else if (filePath != null) {
            return filePath;
        } else {
            return "General";
        }
    }

    /**
     * Checks if this suggestion is actionable (has specific location or example)
     */
    public boolean isActionable() {
        return filePath != null || example != null || codeExample != null;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Type getType() { return type; }
    public Priority getPriority() { return priority; }
    public String getCategory() { return category; }
    public String getFilePath() { return filePath; }
    public Integer getLineNumber() { return lineNumber; }
    public Integer getColumnNumber() { return columnNumber; }
    public String getCodeExample() { return codeExample; }
    public String getExample() { return example; }
    public String getRationale() { return rationale; }
    public String getLearnMoreUrl() { return learnMoreUrl; }
    public String getEstimatedImpact() { return estimatedImpact; }
    public List<String> getTags() { return tags; }
    public double getConfidenceScore() { return confidenceScore; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Suggestion that = (Suggestion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Suggestion{id='%s', type=%s, priority=%s, title='%s', location='%s'}", 
                id, type, priority, title, getLocationString());
    }
}