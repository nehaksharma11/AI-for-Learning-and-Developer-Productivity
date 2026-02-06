package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of documentation validation.
 */
public final class ValidationResult {
    
    public enum Status {
        VALID, INVALID, WARNING, NEEDS_UPDATE
    }
    
    public enum IssueType {
        ACCURACY, COMPLETENESS, STYLE, CONSISTENCY, OUTDATED
    }

    @NotNull
    private final Status status;
    
    private final double accuracyScore;
    private final List<ValidationIssue> issues;
    private final List<String> suggestions;
    private final Instant validatedAt;
    private final String validatorVersion;

    @JsonCreator
    public ValidationResult(
            @JsonProperty("status") Status status,
            @JsonProperty("accuracyScore") double accuracyScore,
            @JsonProperty("issues") List<ValidationIssue> issues,
            @JsonProperty("suggestions") List<String> suggestions,
            @JsonProperty("validatedAt") Instant validatedAt,
            @JsonProperty("validatorVersion") String validatorVersion) {
        this.status = Objects.requireNonNull(status, "Validation status cannot be null");
        this.accuracyScore = Math.max(0.0, Math.min(1.0, accuracyScore));
        this.issues = issues != null ? List.copyOf(issues) : List.of();
        this.suggestions = suggestions != null ? List.copyOf(suggestions) : List.of();
        this.validatedAt = validatedAt != null ? validatedAt : Instant.now();
        this.validatorVersion = validatorVersion;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Status status;
        private double accuracyScore = 1.0;
        private List<ValidationIssue> issues;
        private List<String> suggestions;
        private Instant validatedAt;
        private String validatorVersion;
        
        public Builder status(Status status) { this.status = status; return this; }
        public Builder accuracyScore(double accuracyScore) { this.accuracyScore = accuracyScore; return this; }
        public Builder issues(List<ValidationIssue> issues) { this.issues = issues; return this; }
        public Builder suggestions(List<String> suggestions) { this.suggestions = suggestions; return this; }
        public Builder validatedAt(Instant validatedAt) { this.validatedAt = validatedAt; return this; }
        public Builder validatorVersion(String validatorVersion) { this.validatorVersion = validatorVersion; return this; }
        
        public ValidationResult build() {
            return new ValidationResult(status, accuracyScore, issues, suggestions, 
                    validatedAt, validatorVersion);
        }
    }

    // Convenience factory methods
    public static ValidationResult valid(double accuracyScore) {
        return builder()
                .status(Status.VALID)
                .accuracyScore(accuracyScore)
                .build();
    }

    public static ValidationResult invalid(List<ValidationIssue> issues, List<String> suggestions) {
        return builder()
                .status(Status.INVALID)
                .accuracyScore(0.0)
                .issues(issues)
                .suggestions(suggestions)
                .build();
    }

    public static ValidationResult warning(List<ValidationIssue> issues, double accuracyScore) {
        return builder()
                .status(Status.WARNING)
                .accuracyScore(accuracyScore)
                .issues(issues)
                .build();
    }

    public static ValidationResult needsUpdate(List<String> suggestions, double accuracyScore) {
        return builder()
                .status(Status.NEEDS_UPDATE)
                .accuracyScore(accuracyScore)
                .suggestions(suggestions)
                .build();
    }

    /**
     * Checks if the validation passed without issues
     */
    public boolean isValid() {
        return status == Status.VALID;
    }

    /**
     * Checks if there are critical issues that need immediate attention
     */
    public boolean hasCriticalIssues() {
        return issues.stream().anyMatch(issue -> 
                issue.getSeverity() == ValidationIssue.Severity.HIGH);
    }

    /**
     * Gets the count of issues by type
     */
    public long getIssueCount(IssueType type) {
        return issues.stream().filter(issue -> issue.getType() == type).count();
    }

    // Getters
    public Status getStatus() { return status; }
    public double getAccuracyScore() { return accuracyScore; }
    public List<ValidationIssue> getIssues() { return issues; }
    public List<String> getSuggestions() { return suggestions; }
    public Instant getValidatedAt() { return validatedAt; }
    public String getValidatorVersion() { return validatorVersion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return Double.compare(that.accuracyScore, accuracyScore) == 0 &&
                status == that.status &&
                Objects.equals(issues, that.issues) &&
                Objects.equals(suggestions, that.suggestions) &&
                Objects.equals(validatedAt, that.validatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, accuracyScore, issues, suggestions, validatedAt);
    }

    @Override
    public String toString() {
        return String.format("ValidationResult{status=%s, accuracy=%.2f, issues=%d, suggestions=%d}", 
                status, accuracyScore, issues.size(), suggestions.size());
    }

    /**
     * Represents a specific validation issue.
     */
    public static final class ValidationIssue {
        
        public enum Severity {
            LOW, MEDIUM, HIGH
        }

        private final IssueType type;
        private final Severity severity;
        private final String description;
        private final String location;
        private final String suggestion;

        @JsonCreator
        public ValidationIssue(
                @JsonProperty("type") IssueType type,
                @JsonProperty("severity") Severity severity,
                @JsonProperty("description") String description,
                @JsonProperty("location") String location,
                @JsonProperty("suggestion") String suggestion) {
            this.type = Objects.requireNonNull(type, "Issue type cannot be null");
            this.severity = Objects.requireNonNull(severity, "Issue severity cannot be null");
            this.description = Objects.requireNonNull(description, "Issue description cannot be null");
            this.location = location;
            this.suggestion = suggestion;
        }

        public static ValidationIssue create(IssueType type, Severity severity, String description) {
            return new ValidationIssue(type, severity, description, null, null);
        }

        public static ValidationIssue create(IssueType type, Severity severity, String description, 
                                           String location, String suggestion) {
            return new ValidationIssue(type, severity, description, location, suggestion);
        }

        // Getters
        public IssueType getType() { return type; }
        public Severity getSeverity() { return severity; }
        public String getDescription() { return description; }
        public String getLocation() { return location; }
        public String getSuggestion() { return suggestion; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValidationIssue that = (ValidationIssue) o;
            return type == that.type &&
                    severity == that.severity &&
                    Objects.equals(description, that.description) &&
                    Objects.equals(location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, severity, description, location);
        }

        @Override
        public String toString() {
            return String.format("ValidationIssue{type=%s, severity=%s, description='%s'}", 
                    type, severity, description);
        }
    }
}