package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents productivity metrics for a developer or development session.
 * Tracks various aspects of development productivity and efficiency.
 */
public class ProductivityMetrics {
    
    public enum MetricType {
        LINES_OF_CODE, COMMITS, TESTS_WRITTEN, BUGS_FIXED, CODE_REVIEWS, 
        DOCUMENTATION_WRITTEN, REFACTORING_SESSIONS, LEARNING_TIME
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String developerId;
    
    @NotNull
    private final LocalDateTime periodStart;
    
    @NotNull
    private final LocalDateTime periodEnd;
    
    // Core productivity metrics
    private final int linesOfCodeWritten;
    private final int linesOfCodeDeleted;
    private final int commitsCount;
    private final int testsWritten;
    private final int bugsFixed;
    private final int codeReviewsCompleted;
    private final int documentationPagesWritten;
    private final int refactoringSessions;
    
    // Time-based metrics (in minutes)
    private final int activeCodeTime;
    private final int learningTime;
    private final int debuggingTime;
    private final int meetingTime;
    
    // Quality metrics
    private final double codeQualityScore;
    private final double testCoverageImprovement;
    private final int securityIssuesResolved;
    private final int performanceOptimizations;
    
    // Efficiency metrics
    private final double velocityScore;
    private final double focusScore;
    private final int contextSwitches;
    private final int automationOpportunitiesIdentified;
    
    @NotNull
    private final Map<String, Double> customMetrics;
    
    @NotNull
    private final LocalDateTime calculatedAt;

    @JsonCreator
    public ProductivityMetrics(
            @JsonProperty("id") String id,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("periodStart") LocalDateTime periodStart,
            @JsonProperty("periodEnd") LocalDateTime periodEnd,
            @JsonProperty("linesOfCodeWritten") int linesOfCodeWritten,
            @JsonProperty("linesOfCodeDeleted") int linesOfCodeDeleted,
            @JsonProperty("commitsCount") int commitsCount,
            @JsonProperty("testsWritten") int testsWritten,
            @JsonProperty("bugsFixed") int bugsFixed,
            @JsonProperty("codeReviewsCompleted") int codeReviewsCompleted,
            @JsonProperty("documentationPagesWritten") int documentationPagesWritten,
            @JsonProperty("refactoringSessions") int refactoringSessions,
            @JsonProperty("activeCodeTime") int activeCodeTime,
            @JsonProperty("learningTime") int learningTime,
            @JsonProperty("debuggingTime") int debuggingTime,
            @JsonProperty("meetingTime") int meetingTime,
            @JsonProperty("codeQualityScore") double codeQualityScore,
            @JsonProperty("testCoverageImprovement") double testCoverageImprovement,
            @JsonProperty("securityIssuesResolved") int securityIssuesResolved,
            @JsonProperty("performanceOptimizations") int performanceOptimizations,
            @JsonProperty("velocityScore") double velocityScore,
            @JsonProperty("focusScore") double focusScore,
            @JsonProperty("contextSwitches") int contextSwitches,
            @JsonProperty("automationOpportunitiesIdentified") int automationOpportunitiesIdentified,
            @JsonProperty("customMetrics") Map<String, Double> customMetrics,
            @JsonProperty("calculatedAt") LocalDateTime calculatedAt) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.periodStart = Objects.requireNonNull(periodStart, "Period start cannot be null");
        this.periodEnd = Objects.requireNonNull(periodEnd, "Period end cannot be null");
        this.linesOfCodeWritten = Math.max(0, linesOfCodeWritten);
        this.linesOfCodeDeleted = Math.max(0, linesOfCodeDeleted);
        this.commitsCount = Math.max(0, commitsCount);
        this.testsWritten = Math.max(0, testsWritten);
        this.bugsFixed = Math.max(0, bugsFixed);
        this.codeReviewsCompleted = Math.max(0, codeReviewsCompleted);
        this.documentationPagesWritten = Math.max(0, documentationPagesWritten);
        this.refactoringSessions = Math.max(0, refactoringSessions);
        this.activeCodeTime = Math.max(0, activeCodeTime);
        this.learningTime = Math.max(0, learningTime);
        this.debuggingTime = Math.max(0, debuggingTime);
        this.meetingTime = Math.max(0, meetingTime);
        this.codeQualityScore = Math.max(0.0, Math.min(1.0, codeQualityScore));
        this.testCoverageImprovement = testCoverageImprovement;
        this.securityIssuesResolved = Math.max(0, securityIssuesResolved);
        this.performanceOptimizations = Math.max(0, performanceOptimizations);
        this.velocityScore = Math.max(0.0, velocityScore);
        this.focusScore = Math.max(0.0, Math.min(1.0, focusScore));
        this.contextSwitches = Math.max(0, contextSwitches);
        this.automationOpportunitiesIdentified = Math.max(0, automationOpportunitiesIdentified);
        this.customMetrics = customMetrics != null ? new HashMap<>(customMetrics) : new HashMap<>();
        this.calculatedAt = calculatedAt != null ? calculatedAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Calculates overall productivity score based on various metrics.
     */
    public double getOverallProductivityScore() {
        double codeProductivity = calculateCodeProductivity();
        double qualityScore = calculateQualityScore();
        double efficiencyScore = calculateEfficiencyScore();
        
        // Weighted average: code (40%), quality (35%), efficiency (25%)
        return (codeProductivity * 0.4) + (qualityScore * 0.35) + (efficiencyScore * 0.25);
    }

    /**
     * Calculates net lines of code (written - deleted).
     */
    public int getNetLinesOfCode() {
        return linesOfCodeWritten - linesOfCodeDeleted;
    }

    /**
     * Calculates total productive time in minutes.
     */
    public int getTotalProductiveTime() {
        return activeCodeTime + learningTime;
    }

    /**
     * Calculates code velocity (lines per hour of active coding).
     */
    public double getCodeVelocity() {
        if (activeCodeTime == 0) return 0.0;
        return (double) getNetLinesOfCode() / (activeCodeTime / 60.0);
    }

    private double calculateCodeProductivity() {
        // Normalize based on typical values
        double locScore = Math.min(1.0, getNetLinesOfCode() / 500.0); // 500 LOC as baseline
        double commitScore = Math.min(1.0, commitsCount / 20.0); // 20 commits as baseline
        double testScore = Math.min(1.0, testsWritten / 10.0); // 10 tests as baseline
        
        return (locScore + commitScore + testScore) / 3.0;
    }

    private double calculateQualityScore() {
        double qualityWeight = codeQualityScore * 0.4;
        double testWeight = Math.min(1.0, testCoverageImprovement / 10.0) * 0.3; // 10% improvement baseline
        double securityWeight = Math.min(1.0, securityIssuesResolved / 5.0) * 0.3; // 5 issues baseline
        
        return qualityWeight + testWeight + securityWeight;
    }

    private double calculateEfficiencyScore() {
        double focusWeight = focusScore * 0.4;
        double velocityWeight = Math.min(1.0, velocityScore / 100.0) * 0.3; // 100 as baseline velocity
        double contextWeight = Math.max(0.0, 1.0 - (contextSwitches / 50.0)) * 0.3; // Penalty for context switches
        
        return focusWeight + velocityWeight + contextWeight;
    }

    // Getters
    public String getId() { return id; }
    public String getDeveloperId() { return developerId; }
    public LocalDateTime getPeriodStart() { return periodStart; }
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public int getLinesOfCodeWritten() { return linesOfCodeWritten; }
    public int getLinesOfCodeDeleted() { return linesOfCodeDeleted; }
    public int getCommitsCount() { return commitsCount; }
    public int getTestsWritten() { return testsWritten; }
    public int getBugsFixed() { return bugsFixed; }
    public int getCodeReviewsCompleted() { return codeReviewsCompleted; }
    public int getDocumentationPagesWritten() { return documentationPagesWritten; }
    public int getRefactoringSessions() { return refactoringSessions; }
    public int getActiveCodeTime() { return activeCodeTime; }
    public int getLearningTime() { return learningTime; }
    public int getDebuggingTime() { return debuggingTime; }
    public int getMeetingTime() { return meetingTime; }
    public double getCodeQualityScore() { return codeQualityScore; }
    public double getTestCoverageImprovement() { return testCoverageImprovement; }
    public int getSecurityIssuesResolved() { return securityIssuesResolved; }
    public int getPerformanceOptimizations() { return performanceOptimizations; }
    public double getVelocityScore() { return velocityScore; }
    public double getFocusScore() { return focusScore; }
    public int getContextSwitches() { return contextSwitches; }
    public int getAutomationOpportunitiesIdentified() { return automationOpportunitiesIdentified; }
    public Map<String, Double> getCustomMetrics() { return new HashMap<>(customMetrics); }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }

    public static class Builder {
        private String id;
        private String developerId;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        private int linesOfCodeWritten = 0;
        private int linesOfCodeDeleted = 0;
        private int commitsCount = 0;
        private int testsWritten = 0;
        private int bugsFixed = 0;
        private int codeReviewsCompleted = 0;
        private int documentationPagesWritten = 0;
        private int refactoringSessions = 0;
        private int activeCodeTime = 0;
        private int learningTime = 0;
        private int debuggingTime = 0;
        private int meetingTime = 0;
        private double codeQualityScore = 0.0;
        private double testCoverageImprovement = 0.0;
        private int securityIssuesResolved = 0;
        private int performanceOptimizations = 0;
        private double velocityScore = 0.0;
        private double focusScore = 0.0;
        private int contextSwitches = 0;
        private int automationOpportunitiesIdentified = 0;
        private Map<String, Double> customMetrics = new HashMap<>();
        private LocalDateTime calculatedAt = LocalDateTime.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder periodStart(LocalDateTime periodStart) { this.periodStart = periodStart; return this; }
        public Builder periodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; return this; }
        public Builder linesOfCodeWritten(int linesOfCodeWritten) { this.linesOfCodeWritten = linesOfCodeWritten; return this; }
        public Builder linesOfCodeDeleted(int linesOfCodeDeleted) { this.linesOfCodeDeleted = linesOfCodeDeleted; return this; }
        public Builder commitsCount(int commitsCount) { this.commitsCount = commitsCount; return this; }
        public Builder testsWritten(int testsWritten) { this.testsWritten = testsWritten; return this; }
        public Builder bugsFixed(int bugsFixed) { this.bugsFixed = bugsFixed; return this; }
        public Builder codeReviewsCompleted(int codeReviewsCompleted) { this.codeReviewsCompleted = codeReviewsCompleted; return this; }
        public Builder documentationPagesWritten(int documentationPagesWritten) { this.documentationPagesWritten = documentationPagesWritten; return this; }
        public Builder refactoringSessions(int refactoringSessions) { this.refactoringSessions = refactoringSessions; return this; }
        public Builder activeCodeTime(int activeCodeTime) { this.activeCodeTime = activeCodeTime; return this; }
        public Builder learningTime(int learningTime) { this.learningTime = learningTime; return this; }
        public Builder debuggingTime(int debuggingTime) { this.debuggingTime = debuggingTime; return this; }
        public Builder meetingTime(int meetingTime) { this.meetingTime = meetingTime; return this; }
        public Builder codeQualityScore(double codeQualityScore) { this.codeQualityScore = codeQualityScore; return this; }
        public Builder testCoverageImprovement(double testCoverageImprovement) { this.testCoverageImprovement = testCoverageImprovement; return this; }
        public Builder securityIssuesResolved(int securityIssuesResolved) { this.securityIssuesResolved = securityIssuesResolved; return this; }
        public Builder performanceOptimizations(int performanceOptimizations) { this.performanceOptimizations = performanceOptimizations; return this; }
        public Builder velocityScore(double velocityScore) { this.velocityScore = velocityScore; return this; }
        public Builder focusScore(double focusScore) { this.focusScore = focusScore; return this; }
        public Builder contextSwitches(int contextSwitches) { this.contextSwitches = contextSwitches; return this; }
        public Builder automationOpportunitiesIdentified(int automationOpportunitiesIdentified) { this.automationOpportunitiesIdentified = automationOpportunitiesIdentified; return this; }
        public Builder customMetrics(Map<String, Double> customMetrics) { this.customMetrics = customMetrics; return this; }
        public Builder calculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; return this; }

        public ProductivityMetrics build() {
            return new ProductivityMetrics(id, developerId, periodStart, periodEnd, linesOfCodeWritten,
                    linesOfCodeDeleted, commitsCount, testsWritten, bugsFixed, codeReviewsCompleted,
                    documentationPagesWritten, refactoringSessions, activeCodeTime, learningTime,
                    debuggingTime, meetingTime, codeQualityScore, testCoverageImprovement,
                    securityIssuesResolved, performanceOptimizations, velocityScore, focusScore,
                    contextSwitches, automationOpportunitiesIdentified, customMetrics, calculatedAt);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductivityMetrics that = (ProductivityMetrics) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductivityMetrics{" +
                "id='" + id + '\'' +
                ", developerId='" + developerId + '\'' +
                ", overallScore=" + String.format("%.2f", getOverallProductivityScore()) +
                ", netLOC=" + getNetLinesOfCode() +
                ", velocity=" + String.format("%.1f", getCodeVelocity()) +
                ", focusScore=" + String.format("%.2f", focusScore) +
                '}';
    }
}