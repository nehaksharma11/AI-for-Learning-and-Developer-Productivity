package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents complexity metrics for code analysis.
 * Tracks various measures of code complexity to assess maintainability and quality.
 */
public class ComplexityMetrics {
    
    @Min(0)
    private final int cyclomaticComplexity;
    
    @Min(0)
    private final int linesOfCode;
    
    @Min(0)
    private final int cognitiveComplexity;
    
    @Min(0)
    private final int nestingDepth;
    
    @Min(0)
    private final int numberOfMethods;
    
    @Min(0)
    private final int numberOfClasses;
    
    @NotNull
    private final Map<String, Double> additionalMetrics;

    @JsonCreator
    public ComplexityMetrics(
            @JsonProperty("cyclomaticComplexity") int cyclomaticComplexity,
            @JsonProperty("linesOfCode") int linesOfCode,
            @JsonProperty("cognitiveComplexity") int cognitiveComplexity,
            @JsonProperty("nestingDepth") int nestingDepth,
            @JsonProperty("numberOfMethods") int numberOfMethods,
            @JsonProperty("numberOfClasses") int numberOfClasses,
            @JsonProperty("additionalMetrics") Map<String, Double> additionalMetrics) {
        this.cyclomaticComplexity = Math.max(0, cyclomaticComplexity);
        this.linesOfCode = Math.max(0, linesOfCode);
        this.cognitiveComplexity = Math.max(0, cognitiveComplexity);
        this.nestingDepth = Math.max(0, nestingDepth);
        this.numberOfMethods = Math.max(0, numberOfMethods);
        this.numberOfClasses = Math.max(0, numberOfClasses);
        this.additionalMetrics = additionalMetrics != null ? 
                new HashMap<>(additionalMetrics) : new HashMap<>();
    }

    public static ComplexityMetrics empty() {
        return new ComplexityMetrics(0, 0, 0, 0, 0, 0, new HashMap<>());
    }

    public static ComplexityMetrics simple(int linesOfCode, int numberOfMethods) {
        return new ComplexityMetrics(numberOfMethods, linesOfCode, numberOfMethods, 
                2, numberOfMethods, 1, new HashMap<>());
    }

    public static ComplexityMetrics complex(int cyclomaticComplexity, int linesOfCode, 
                                          int cognitiveComplexity, int nestingDepth) {
        return new ComplexityMetrics(cyclomaticComplexity, linesOfCode, cognitiveComplexity, 
                nestingDepth, cyclomaticComplexity, linesOfCode / 50, new HashMap<>());
    }

    /**
     * Calculates overall complexity score (0.0 to 1.0, higher = more complex)
     */
    public double getOverallComplexityScore() {
        if (linesOfCode == 0) return 0.0;
        
        // Normalize metrics and combine with weights
        double cyclomaticScore = Math.min(1.0, cyclomaticComplexity / 20.0);
        double cognitiveScore = Math.min(1.0, cognitiveComplexity / 30.0);
        double nestingScore = Math.min(1.0, nestingDepth / 10.0);
        double sizeScore = Math.min(1.0, linesOfCode / 1000.0);
        
        return (cyclomaticScore * 0.3) + (cognitiveScore * 0.3) + 
               (nestingScore * 0.2) + (sizeScore * 0.2);
    }

    /**
     * Determines if the code is considered highly complex
     */
    public boolean isHighlyComplex() {
        return getOverallComplexityScore() > 0.7 || 
               cyclomaticComplexity > 15 || 
               cognitiveComplexity > 25 ||
               nestingDepth > 6;
    }

    /**
     * Determines if the code needs refactoring based on complexity
     */
    public boolean needsRefactoring() {
        return isHighlyComplex() || 
               (linesOfCode > 500 && numberOfMethods > 20) ||
               (cyclomaticComplexity > 10 && cognitiveComplexity > 15);
    }

    public ComplexityMetrics withAdditionalMetric(String name, double value) {
        Map<String, Double> newMetrics = new HashMap<>(this.additionalMetrics);
        newMetrics.put(name, value);
        return new ComplexityMetrics(cyclomaticComplexity, linesOfCode, cognitiveComplexity,
                nestingDepth, numberOfMethods, numberOfClasses, newMetrics);
    }

    // Getters
    public int getCyclomaticComplexity() { return cyclomaticComplexity; }
    public int getLinesOfCode() { return linesOfCode; }
    public int getCognitiveComplexity() { return cognitiveComplexity; }
    public int getNestingDepth() { return nestingDepth; }
    public int getNumberOfMethods() { return numberOfMethods; }
    public int getNumberOfClasses() { return numberOfClasses; }
    public Map<String, Double> getAdditionalMetrics() { return new HashMap<>(additionalMetrics); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexityMetrics that = (ComplexityMetrics) o;
        return cyclomaticComplexity == that.cyclomaticComplexity &&
                linesOfCode == that.linesOfCode &&
                cognitiveComplexity == that.cognitiveComplexity &&
                nestingDepth == that.nestingDepth &&
                numberOfMethods == that.numberOfMethods &&
                numberOfClasses == that.numberOfClasses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cyclomaticComplexity, linesOfCode, cognitiveComplexity, 
                nestingDepth, numberOfMethods, numberOfClasses);
    }

    @Override
    public String toString() {
        return "ComplexityMetrics{" +
                "cyclomatic=" + cyclomaticComplexity +
                ", loc=" + linesOfCode +
                ", cognitive=" + cognitiveComplexity +
                ", nesting=" + nestingDepth +
                ", overallScore=" + String.format("%.2f", getOverallComplexityScore()) +
                '}';
    }
}