package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Represents a code issue found during analysis.
 * Issues can be bugs, security vulnerabilities, code smells, or style violations.
 */
public class CodeIssue {
    
    public enum Severity {
        ERROR, WARNING, INFO
    }
    
    public enum Category {
        BUG, SECURITY, PERFORMANCE, MAINTAINABILITY, STYLE, DOCUMENTATION, TESTING
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String message;
    
    @NotNull
    private final Severity severity;
    
    @NotNull
    private final Category category;
    
    @NotBlank
    private final String filePath;
    
    @Min(1)
    private final int lineNumber;
    
    private final int columnNumber;
    private final String ruleId;
    private final String suggestion;
    private final String codeSnippet;

    @JsonCreator
    public CodeIssue(
            @JsonProperty("id") String id,
            @JsonProperty("message") String message,
            @JsonProperty("severity") Severity severity,
            @JsonProperty("category") Category category,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("lineNumber") int lineNumber,
            @JsonProperty("columnNumber") int columnNumber,
            @JsonProperty("ruleId") String ruleId,
            @JsonProperty("suggestion") String suggestion,
            @JsonProperty("codeSnippet") String codeSnippet) {
        this.id = Objects.requireNonNull(id, "Issue ID cannot be null");
        this.message = Objects.requireNonNull(message, "Issue message cannot be null");
        this.severity = Objects.requireNonNull(severity, "Issue severity cannot be null");
        this.category = Objects.requireNonNull(category, "Issue category cannot be null");
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null");
        this.lineNumber = Math.max(1, lineNumber);
        this.columnNumber = Math.max(0, columnNumber);
        this.ruleId = ruleId;
        this.suggestion = suggestion;
        this.codeSnippet = codeSnippet;
    }

    public static CodeIssue bug(String message, String filePath, int lineNumber, String suggestion) {
        return new CodeIssue(
                java.util.UUID.randomUUID().toString(),
                message,
                Severity.ERROR,
                Category.BUG,
                filePath,
                lineNumber,
                0,
                "BUG_DETECTION",
                suggestion,
                null
        );
    }

    public static CodeIssue securityIssue(String message, String filePath, int lineNumber, String ruleId) {
        return new CodeIssue(
                java.util.UUID.randomUUID().toString(),
                message,
                Severity.ERROR,
                Category.SECURITY,
                filePath,
                lineNumber,
                0,
                ruleId,
                "Review security implications and apply appropriate fixes",
                null
        );
    }

    public static CodeIssue performanceWarning(String message, String filePath, int lineNumber, String suggestion) {
        return new CodeIssue(
                java.util.UUID.randomUUID().toString(),
                message,
                Severity.WARNING,
                Category.PERFORMANCE,
                filePath,
                lineNumber,
                0,
                "PERFORMANCE_CHECK",
                suggestion,
                null
        );
    }

    public static CodeIssue codeSmell(String message, String filePath, int lineNumber, String suggestion) {
        return new CodeIssue(
                java.util.UUID.randomUUID().toString(),
                message,
                Severity.WARNING,
                Category.MAINTAINABILITY,
                filePath,
                lineNumber,
                0,
                "CODE_SMELL",
                suggestion,
                null
        );
    }

    public static CodeIssue styleViolation(String message, String filePath, int lineNumber, String ruleId) {
        return new CodeIssue(
                java.util.UUID.randomUUID().toString(),
                message,
                Severity.INFO,
                Category.STYLE,
                filePath,
                lineNumber,
                0,
                ruleId,
                "Follow project coding standards",
                null
        );
    }

    /**
     * Checks if this issue is critical and should be addressed immediately
     */
    public boolean isCritical() {
        return severity == Severity.ERROR && 
               (category == Category.BUG || category == Category.SECURITY);
    }

    /**
     * Checks if this issue affects code maintainability
     */
    public boolean affectsMaintainability() {
        return category == Category.MAINTAINABILITY || 
               category == Category.STYLE || 
               category == Category.DOCUMENTATION;
    }

    /**
     * Gets the location string for this issue
     */
    public String getLocationString() {
        if (columnNumber > 0) {
            return filePath + ":" + lineNumber + ":" + columnNumber;
        } else {
            return filePath + ":" + lineNumber;
        }
    }

    // Getters
    public String getId() { return id; }
    public String getMessage() { return message; }
    public Severity getSeverity() { return severity; }
    public Category getCategory() { return category; }
    public String getFilePath() { return filePath; }
    public int getLineNumber() { return lineNumber; }
    public int getColumnNumber() { return columnNumber; }
    public String getRuleId() { return ruleId; }
    public String getSuggestion() { return suggestion; }
    public String getCodeSnippet() { return codeSnippet; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeIssue codeIssue = (CodeIssue) o;
        return Objects.equals(id, codeIssue.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CodeIssue{" +
                "severity=" + severity +
                ", category=" + category +
                ", message='" + message + '\'' +
                ", location=" + getLocationString() +
                '}';
    }
}