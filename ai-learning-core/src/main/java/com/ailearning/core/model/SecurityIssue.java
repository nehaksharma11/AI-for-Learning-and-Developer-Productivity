package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Represents a security issue found in code analysis.
 * Immutable value object with comprehensive security information.
 */
public final class SecurityIssue {
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum Category {
        INJECTION, XSS, AUTHENTICATION, AUTHORIZATION, 
        CRYPTOGRAPHY, CONFIGURATION, DEPENDENCY_VULNERABILITY,
        INPUT_VALIDATION, OUTPUT_ENCODING, SESSION_MANAGEMENT
    }
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String title;
    
    @NotBlank
    private final String description;
    
    @NotNull
    private final Severity severity;
    
    @NotNull
    private final Category category;
    
    private final String file;
    private final Integer line;
    private final Integer column;
    private final String cweId;
    private final String recommendation;
    private final String codeSnippet;
    
    @JsonCreator
    public SecurityIssue(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("severity") Severity severity,
            @JsonProperty("category") Category category,
            @JsonProperty("file") String file,
            @JsonProperty("line") Integer line,
            @JsonProperty("column") Integer column,
            @JsonProperty("cweId") String cweId,
            @JsonProperty("recommendation") String recommendation,
            @JsonProperty("codeSnippet") String codeSnippet) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.file = file;
        this.line = line;
        this.column = column;
        this.cweId = cweId;
        this.recommendation = recommendation;
        this.codeSnippet = codeSnippet;
    }
    
    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Severity getSeverity() { return severity; }
    public Category getCategory() { return category; }
    public String getFile() { return file; }
    public Integer getLine() { return line; }
    public Integer getColumn() { return column; }
    public String getCweId() { return cweId; }
    public String getRecommendation() { return recommendation; }
    public String getCodeSnippet() { return codeSnippet; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String title;
        private String description;
        private Severity severity;
        private Category category;
        private String file;
        private Integer line;
        private Integer column;
        private String cweId;
        private String recommendation;
        private String codeSnippet;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder severity(Severity severity) { this.severity = severity; return this; }
        public Builder category(Category category) { this.category = category; return this; }
        public Builder file(String file) { this.file = file; return this; }
        public Builder line(Integer line) { this.line = line; return this; }
        public Builder column(Integer column) { this.column = column; return this; }
        public Builder cweId(String cweId) { this.cweId = cweId; return this; }
        public Builder recommendation(String recommendation) { this.recommendation = recommendation; return this; }
        public Builder codeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; return this; }
        
        public SecurityIssue build() {
            return new SecurityIssue(id, title, description, severity, category, 
                    file, line, column, cweId, recommendation, codeSnippet);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityIssue that = (SecurityIssue) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               severity == that.severity &&
               category == that.category &&
               Objects.equals(file, that.file) &&
               Objects.equals(line, that.line) &&
               Objects.equals(column, that.column);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, severity, category, file, line, column);
    }
    
    @Override
    public String toString() {
        return String.format("SecurityIssue{id='%s', title='%s', severity=%s, category=%s, file='%s', line=%d}", 
                id, title, severity, category, file, line);
    }
}