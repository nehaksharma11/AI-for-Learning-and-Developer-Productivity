package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Represents a code pattern detected in the project.
 * Patterns can be design patterns, anti-patterns, or architectural patterns.
 */
public class CodePattern {
    
    public enum PatternType {
        DESIGN_PATTERN, ANTI_PATTERN, ARCHITECTURAL_PATTERN, IDIOM, BEST_PRACTICE, CODE_SMELL
    }
    
    public enum PatternCategory {
        CREATIONAL, STRUCTURAL, BEHAVIORAL, ARCHITECTURAL, PERFORMANCE, SECURITY, MAINTAINABILITY
    }

    @NotBlank
    private final String name;
    
    @NotNull
    private final PatternType type;
    
    @NotNull
    private final PatternCategory category;
    
    private final String description;
    private final String location;
    private final double confidence; // 0.0 to 1.0
    private final String recommendation;

    @JsonCreator
    public CodePattern(
            @JsonProperty("name") String name,
            @JsonProperty("type") PatternType type,
            @JsonProperty("category") PatternCategory category,
            @JsonProperty("description") String description,
            @JsonProperty("location") String location,
            @JsonProperty("confidence") double confidence,
            @JsonProperty("recommendation") String recommendation) {
        this.name = Objects.requireNonNull(name, "Pattern name cannot be null");
        this.type = Objects.requireNonNull(type, "Pattern type cannot be null");
        this.category = Objects.requireNonNull(category, "Pattern category cannot be null");
        this.description = description;
        this.location = location;
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        this.recommendation = recommendation;
    }

    public CodePattern(String name) {
        this(name, PatternType.DESIGN_PATTERN, PatternCategory.STRUCTURAL, null, null, 1.0, null);
    }

    public static CodePattern designPattern(String name, PatternCategory category, String location) {
        return new CodePattern(name, PatternType.DESIGN_PATTERN, category, 
                "Design pattern: " + name, location, 0.9, null);
    }

    public static CodePattern antiPattern(String name, String location, String recommendation) {
        return new CodePattern(name, PatternType.ANTI_PATTERN, PatternCategory.MAINTAINABILITY,
                "Anti-pattern detected: " + name, location, 0.8, recommendation);
    }

    public static CodePattern codeSmell(String name, String location, String recommendation) {
        return new CodePattern(name, PatternType.CODE_SMELL, PatternCategory.MAINTAINABILITY,
                "Code smell: " + name, location, 0.7, recommendation);
    }

    public static CodePattern bestPractice(String name, String location) {
        return new CodePattern(name, PatternType.BEST_PRACTICE, PatternCategory.MAINTAINABILITY,
                "Best practice: " + name, location, 0.9, null);
    }

    /**
     * Checks if this pattern indicates a problem that should be addressed
     */
    public boolean isProblematic() {
        return type == PatternType.ANTI_PATTERN || type == PatternType.CODE_SMELL;
    }

    /**
     * Checks if this pattern is beneficial and should be encouraged
     */
    public boolean isBeneficial() {
        return type == PatternType.DESIGN_PATTERN || type == PatternType.BEST_PRACTICE;
    }

    /**
     * Checks if the pattern detection confidence is high enough to act on
     */
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }

    // Getters
    public String getName() { return name; }
    public PatternType getType() { return type; }
    public PatternCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public double getConfidence() { return confidence; }
    public String getRecommendation() { return recommendation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodePattern that = (CodePattern) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(location, that.location) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location, type);
    }

    @Override
    public String toString() {
        return "CodePattern{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", category=" + category +
                ", confidence=" + String.format("%.2f", confidence) +
                '}';
    }
}