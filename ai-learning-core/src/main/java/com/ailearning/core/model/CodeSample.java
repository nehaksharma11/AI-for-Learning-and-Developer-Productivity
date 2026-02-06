package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a code sample used for skill assessment.
 * Contains code content and metadata for analysis.
 */
public class CodeSample {
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String content;
    
    @NotBlank
    private final String language;
    
    private final String filePath;
    
    private final String projectContext;
    
    private final String description;
    
    @NotNull
    private final LocalDateTime createdAt;
    
    private final int linesOfCode;
    
    private final ComplexityMetrics complexity;

    @JsonCreator
    public CodeSample(
            @JsonProperty("id") String id,
            @JsonProperty("content") String content,
            @JsonProperty("language") String language,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("projectContext") String projectContext,
            @JsonProperty("description") String description,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("linesOfCode") int linesOfCode,
            @JsonProperty("complexity") ComplexityMetrics complexity) {
        this.id = Objects.requireNonNull(id, "Code sample ID cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.language = Objects.requireNonNull(language, "Language cannot be null");
        this.filePath = filePath;
        this.projectContext = projectContext;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.linesOfCode = Math.max(0, linesOfCode);
        this.complexity = complexity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static CodeSample create(String id, String content, String language) {
        return new CodeSample(id, content, language, null, null, null, LocalDateTime.now(), 
                content.split("\n").length, null);
    }

    public boolean hasComplexityMetrics() {
        return complexity != null;
    }

    public boolean isSubstantial() {
        return linesOfCode >= 10;
    }

    // Getters
    public String getId() { return id; }
    public String getContent() { return content; }
    public String getLanguage() { return language; }
    public String getFilePath() { return filePath; }
    public String getProjectContext() { return projectContext; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getLinesOfCode() { return linesOfCode; }
    public ComplexityMetrics getComplexity() { return complexity; }

    public static class Builder {
        private String id;
        private String content;
        private String language;
        private String filePath;
        private String projectContext;
        private String description;
        private LocalDateTime createdAt = LocalDateTime.now();
        private int linesOfCode;
        private ComplexityMetrics complexity;

        public Builder id(String id) { this.id = id; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder projectContext(String projectContext) { this.projectContext = projectContext; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder linesOfCode(int linesOfCode) { this.linesOfCode = linesOfCode; return this; }
        public Builder complexity(ComplexityMetrics complexity) { this.complexity = complexity; return this; }

        public CodeSample build() {
            return new CodeSample(id, content, language, filePath, projectContext, description,
                    createdAt, linesOfCode, complexity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeSample that = (CodeSample) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CodeSample{" +
                "id='" + id + '\'' +
                ", language='" + language + '\'' +
                ", linesOfCode=" + linesOfCode +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}