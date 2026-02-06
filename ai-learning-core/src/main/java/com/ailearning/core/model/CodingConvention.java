package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Represents a coding convention detected or enforced in the project.
 * Conventions help maintain consistency and readability across the codebase.
 */
public class CodingConvention {
    
    public enum ConventionType {
        NAMING, FORMATTING, STRUCTURE, DOCUMENTATION, TESTING, SECURITY, PERFORMANCE
    }
    
    public enum ConventionScope {
        PROJECT, MODULE, CLASS, METHOD, VARIABLE, GLOBAL
    }

    @NotBlank
    private final String name;
    
    @NotNull
    private final ConventionType type;
    
    @NotNull
    private final ConventionScope scope;
    
    private final String description;
    private final String rule;
    private final String example;
    private final boolean enforced;
    private final double adherenceScore; // 0.0 to 1.0

    @JsonCreator
    public CodingConvention(
            @JsonProperty("name") String name,
            @JsonProperty("type") ConventionType type,
            @JsonProperty("scope") ConventionScope scope,
            @JsonProperty("description") String description,
            @JsonProperty("rule") String rule,
            @JsonProperty("example") String example,
            @JsonProperty("enforced") boolean enforced,
            @JsonProperty("adherenceScore") double adherenceScore) {
        this.name = Objects.requireNonNull(name, "Convention name cannot be null");
        this.type = Objects.requireNonNull(type, "Convention type cannot be null");
        this.scope = Objects.requireNonNull(scope, "Convention scope cannot be null");
        this.description = description;
        this.rule = rule;
        this.example = example;
        this.enforced = enforced;
        this.adherenceScore = Math.max(0.0, Math.min(1.0, adherenceScore));
    }

    public CodingConvention(String name) {
        this(name, ConventionType.NAMING, ConventionScope.PROJECT, null, null, null, false, 1.0);
    }

    public static CodingConvention namingConvention(String name, String rule, String example) {
        return new CodingConvention(name, ConventionType.NAMING, ConventionScope.PROJECT,
                "Naming convention: " + name, rule, example, true, 0.9);
    }

    public static CodingConvention formattingConvention(String name, String rule) {
        return new CodingConvention(name, ConventionType.FORMATTING, ConventionScope.PROJECT,
                "Formatting convention: " + name, rule, null, true, 0.95);
    }

    public static CodingConvention documentationConvention(String name, String rule) {
        return new CodingConvention(name, ConventionType.DOCUMENTATION, ConventionScope.METHOD,
                "Documentation convention: " + name, rule, null, false, 0.8);
    }

    public static CodingConvention testingConvention(String name, String rule) {
        return new CodingConvention(name, ConventionType.TESTING, ConventionScope.CLASS,
                "Testing convention: " + name, rule, null, true, 0.85);
    }

    /**
     * Checks if the convention adherence is acceptable
     */
    public boolean hasGoodAdherence() {
        return adherenceScore >= 0.8;
    }

    /**
     * Checks if the convention needs attention due to poor adherence
     */
    public boolean needsAttention() {
        return enforced && adherenceScore < 0.7;
    }

    /**
     * Checks if this convention is critical for code quality
     */
    public boolean isCritical() {
        return enforced && (type == ConventionType.SECURITY || type == ConventionType.TESTING);
    }

    // Getters
    public String getName() { return name; }
    public ConventionType getType() { return type; }
    public ConventionScope getScope() { return scope; }
    public String getDescription() { return description; }
    public String getRule() { return rule; }
    public String getExample() { return example; }
    public boolean isEnforced() { return enforced; }
    public double getAdherenceScore() { return adherenceScore; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodingConvention that = (CodingConvention) o;
        return Objects.equals(name, that.name) &&
                type == that.type &&
                scope == that.scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, scope);
    }

    @Override
    public String toString() {
        return "CodingConvention{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", scope=" + scope +
                ", adherence=" + String.format("%.2f", adherenceScore) +
                ", enforced=" + enforced +
                '}';
    }
}