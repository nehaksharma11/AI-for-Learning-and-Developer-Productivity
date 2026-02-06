package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents a code pattern detected in static analysis.
 * Immutable value object with pattern information and occurrences.
 */
public final class Pattern {
    
    public enum Type {
        DESIGN_PATTERN, ANTI_PATTERN, CODE_SMELL, 
        PERFORMANCE_PATTERN, SECURITY_PATTERN, ARCHITECTURAL_PATTERN
    }
    
    public enum Confidence {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String name;
    
    @NotBlank
    private final String description;
    
    @NotNull
    private final Type type;
    
    @NotNull
    private final Confidence confidence;
    
    private final List<String> files;
    private final List<Integer> lines;
    private final String recommendation;
    private final String example;
    private final double impactScore;
    
    @JsonCreator
    public Pattern(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("type") Type type,
            @JsonProperty("confidence") Confidence confidence,
            @JsonProperty("files") List<String> files,
            @JsonProperty("lines") List<Integer> lines,
            @JsonProperty("recommendation") String recommendation,
            @JsonProperty("example") String example,
            @JsonProperty("impactScore") double impactScore) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.confidence = Objects.requireNonNull(confidence, "Confidence cannot be null");
        this.files = files != null ? List.copyOf(files) : List.of();
        this.lines = lines != null ? List.copyOf(lines) : List.of();
        this.recommendation = recommendation;
        this.example = example;
        this.impactScore = impactScore;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Type getType() { return type; }
    public Confidence getConfidence() { return confidence; }
    public List<String> getFiles() { return files; }
    public List<Integer> getLines() { return lines; }
    public String getRecommendation() { return recommendation; }
    public String getExample() { return example; }
    public double getImpactScore() { return impactScore; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String name;
        private String description;
        private Type type;
        private Confidence confidence;
        private List<String> files;
        private List<Integer> lines;
        private String recommendation;
        private String example;
        private double impactScore;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder type(Type type) { this.type = type; return this; }
        public Builder confidence(Confidence confidence) { this.confidence = confidence; return this; }
        public Builder files(List<String> files) { this.files = files; return this; }
        public Builder lines(List<Integer> lines) { this.lines = lines; return this; }
        public Builder recommendation(String recommendation) { this.recommendation = recommendation; return this; }
        public Builder example(String example) { this.example = example; return this; }
        public Builder impactScore(double impactScore) { this.impactScore = impactScore; return this; }
        
        public Pattern build() {
            return new Pattern(id, name, description, type, confidence, 
                    files, lines, recommendation, example, impactScore);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pattern pattern = (Pattern) o;
        return Double.compare(pattern.impactScore, impactScore) == 0 &&
               Objects.equals(id, pattern.id) &&
               Objects.equals(name, pattern.name) &&
               Objects.equals(description, pattern.description) &&
               type == pattern.type &&
               confidence == pattern.confidence;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, type, confidence, impactScore);
    }
    
    @Override
    public String toString() {
        return String.format("Pattern{id='%s', name='%s', type=%s, confidence=%s, impact=%.2f}", 
                id, name, type, confidence, impactScore);
    }
}