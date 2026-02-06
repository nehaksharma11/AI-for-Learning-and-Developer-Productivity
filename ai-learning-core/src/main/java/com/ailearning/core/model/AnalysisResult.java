package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of code analysis operations.
 * Contains findings, metrics, and recommendations from static analysis.
 */
public class AnalysisResult {
    
    public enum AnalysisStatus {
        SUCCESS, WARNING, ERROR, PARTIAL
    }

    @NotBlank
    private final String analysisId;
    
    @NotBlank
    private final String filePath;
    
    @NotNull
    private final AnalysisStatus status;
    
    private final String summary;
    
    @NotNull
    private final List<CodeIssue> issues;
    
    @NotNull
    private final List<Suggestion> suggestions;
    
    @NotNull
    private final ComplexityMetrics complexityMetrics;
    
    @NotNull
    private final LocalDateTime analysisTime;
    
    private final long analysisTimeMs;

    @JsonCreator
    public AnalysisResult(
            @JsonProperty("analysisId") String analysisId,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("status") AnalysisStatus status,
            @JsonProperty("summary") String summary,
            @JsonProperty("issues") List<CodeIssue> issues,
            @JsonProperty("suggestions") List<Suggestion> suggestions,
            @JsonProperty("complexityMetrics") ComplexityMetrics complexityMetrics,
            @JsonProperty("analysisTime") LocalDateTime analysisTime,
            @JsonProperty("analysisTimeMs") long analysisTimeMs) {
        this.analysisId = Objects.requireNonNull(analysisId, "Analysis ID cannot be null");
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null");
        this.status = Objects.requireNonNull(status, "Analysis status cannot be null");
        this.summary = summary;
        this.issues = issues != null ? new ArrayList<>(issues) : new ArrayList<>();
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : new ArrayList<>();
        this.complexityMetrics = Objects.requireNonNull(complexityMetrics, "Complexity metrics cannot be null");
        this.analysisTime = Objects.requireNonNull(analysisTime, "Analysis time cannot be null");
        this.analysisTimeMs = Math.max(0, analysisTimeMs);
    }

    public AnalysisResult(String summary) {
        this(java.util.UUID.randomUUID().toString(), "unknown", AnalysisStatus.SUCCESS, 
             summary, new ArrayList<>(), new ArrayList<>(), ComplexityMetrics.empty(), 
             LocalDateTime.now(), 0);
    }

    public static AnalysisResult success(String filePath, ComplexityMetrics metrics, 
                                       List<Suggestion> suggestions, long analysisTimeMs) {
        return new AnalysisResult(
                java.util.UUID.randomUUID().toString(),
                filePath,
                AnalysisStatus.SUCCESS,
                "Analysis completed successfully",
                new ArrayList<>(),
                suggestions,
                metrics,
                LocalDateTime.now(),
                analysisTimeMs
        );
    }

    public static AnalysisResult withIssues(String filePath, List<CodeIssue> issues, 
                                          ComplexityMetrics metrics, long analysisTimeMs) {
        AnalysisStatus status = issues.stream().anyMatch(issue -> issue.getSeverity() == CodeIssue.Severity.ERROR) 
                ? AnalysisStatus.ERROR : AnalysisStatus.WARNING;
        
        return new AnalysisResult(
                java.util.UUID.randomUUID().toString(),
                filePath,
                status,
                "Analysis found " + issues.size() + " issues",
                issues,
                new ArrayList<>(),
                metrics,
                LocalDateTime.now(),
                analysisTimeMs
        );
    }

    public static AnalysisResult error(String filePath, String errorMessage) {
        return new AnalysisResult(
                java.util.UUID.randomUUID().toString(),
                filePath,
                AnalysisStatus.ERROR,
                "Analysis failed: " + errorMessage,
                new ArrayList<>(),
                new ArrayList<>(),
                ComplexityMetrics.empty(),
                LocalDateTime.now(),
                0
        );
    }

    /**
     * Gets the count of issues by severity level
     */
    public long getIssueCountBySeverity(CodeIssue.Severity severity) {
        return issues.stream()
                .filter(issue -> issue.getSeverity() == severity)
                .count();
    }

    /**
     * Checks if the analysis found any critical issues
     */
    public boolean hasCriticalIssues() {
        return getIssueCountBySeverity(CodeIssue.Severity.ERROR) > 0;
    }

    /**
     * Checks if the analysis was successful without major issues
     */
    public boolean isSuccessful() {
        return status == AnalysisStatus.SUCCESS && !hasCriticalIssues();
    }

    /**
     * Gets the overall quality score based on issues and complexity
     */
    public double getQualityScore() {
        if (status == AnalysisStatus.ERROR) return 0.0;
        
        // Start with base score
        double score = 1.0;
        
        // Deduct for issues
        score -= getIssueCountBySeverity(CodeIssue.Severity.ERROR) * 0.2;
        score -= getIssueCountBySeverity(CodeIssue.Severity.WARNING) * 0.1;
        score -= getIssueCountBySeverity(CodeIssue.Severity.INFO) * 0.05;
        
        // Deduct for complexity
        score -= complexityMetrics.getOverallComplexityScore() * 0.3;
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    // Getters
    public String getAnalysisId() { return analysisId; }
    public String getFilePath() { return filePath; }
    public AnalysisStatus getStatus() { return status; }
    public String getSummary() { return summary; }
    public List<CodeIssue> getIssues() { return new ArrayList<>(issues); }
    public List<Suggestion> getSuggestions() { return new ArrayList<>(suggestions); }
    public ComplexityMetrics getComplexityMetrics() { return complexityMetrics; }
    public LocalDateTime getAnalysisTime() { return analysisTime; }
    public long getAnalysisTimeMs() { return analysisTimeMs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResult that = (AnalysisResult) o;
        return Objects.equals(analysisId, that.analysisId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(analysisId);
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "filePath='" + filePath + '\'' +
                ", status=" + status +
                ", issues=" + issues.size() +
                ", suggestions=" + suggestions.size() +
                ", qualityScore=" + String.format("%.2f", getQualityScore()) +
                ", analysisTime=" + analysisTimeMs + "ms" +
                '}';
    }
}