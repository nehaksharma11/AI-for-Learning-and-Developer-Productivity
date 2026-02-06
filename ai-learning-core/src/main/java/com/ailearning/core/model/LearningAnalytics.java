package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents learning analytics and progress insights for a developer.
 * Contains metrics, trends, and recommendations based on learning history.
 */
public class LearningAnalytics {
    
    @NotBlank
    private final String developerId;
    
    @NotBlank
    private final String timeRange;
    
    private final int totalSessionsCompleted;
    
    private final int totalLearningMinutes;
    
    private final double averageSessionScore;
    
    @NotNull
    private final Map<String, Double> skillProgressMap;
    
    @NotNull
    private final List<String> topPerformingAreas;
    
    @NotNull
    private final List<String> improvementAreas;
    
    @NotNull
    private final List<LearningTrend> trends;
    
    private final double overallProgress;
    
    private final int streakDays;
    
    @NotNull
    private final LocalDateTime generatedAt;
    
    @NotNull
    private final List<String> recommendations;

    @JsonCreator
    public LearningAnalytics(
            @JsonProperty("developerId") String developerId,
            @JsonProperty("timeRange") String timeRange,
            @JsonProperty("totalSessionsCompleted") int totalSessionsCompleted,
            @JsonProperty("totalLearningMinutes") int totalLearningMinutes,
            @JsonProperty("averageSessionScore") double averageSessionScore,
            @JsonProperty("skillProgressMap") Map<String, Double> skillProgressMap,
            @JsonProperty("topPerformingAreas") List<String> topPerformingAreas,
            @JsonProperty("improvementAreas") List<String> improvementAreas,
            @JsonProperty("trends") List<LearningTrend> trends,
            @JsonProperty("overallProgress") double overallProgress,
            @JsonProperty("streakDays") int streakDays,
            @JsonProperty("generatedAt") LocalDateTime generatedAt,
            @JsonProperty("recommendations") List<String> recommendations) {
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.timeRange = Objects.requireNonNull(timeRange, "Time range cannot be null");
        this.totalSessionsCompleted = Math.max(0, totalSessionsCompleted);
        this.totalLearningMinutes = Math.max(0, totalLearningMinutes);
        this.averageSessionScore = Math.max(0.0, Math.min(100.0, averageSessionScore));
        this.skillProgressMap = skillProgressMap != null ? new HashMap<>(skillProgressMap) : new HashMap<>();
        this.topPerformingAreas = topPerformingAreas != null ? new ArrayList<>(topPerformingAreas) : new ArrayList<>();
        this.improvementAreas = improvementAreas != null ? new ArrayList<>(improvementAreas) : new ArrayList<>();
        this.trends = trends != null ? new ArrayList<>(trends) : new ArrayList<>();
        this.overallProgress = Math.max(0.0, Math.min(100.0, overallProgress));
        this.streakDays = Math.max(0, streakDays);
        this.generatedAt = generatedAt != null ? generatedAt : LocalDateTime.now();
        this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getLearningVelocity() {
        if (totalLearningMinutes == 0) return 0.0;
        return (double) totalSessionsCompleted / (totalLearningMinutes / 60.0); // sessions per hour
    }

    public boolean isActiveLearner() {
        return totalSessionsCompleted >= 5 && streakDays >= 3;
    }

    public boolean needsMotivation() {
        return averageSessionScore < 70.0 || streakDays == 0;
    }

    public String getPerformanceLevel() {
        if (averageSessionScore >= 90.0) return "Excellent";
        if (averageSessionScore >= 80.0) return "Good";
        if (averageSessionScore >= 70.0) return "Average";
        if (averageSessionScore >= 60.0) return "Below Average";
        return "Needs Improvement";
    }

    // Getters
    public String getDeveloperId() { return developerId; }
    public String getTimeRange() { return timeRange; }
    public int getTotalSessionsCompleted() { return totalSessionsCompleted; }
    public int getTotalLearningMinutes() { return totalLearningMinutes; }
    public double getAverageSessionScore() { return averageSessionScore; }
    public Map<String, Double> getSkillProgressMap() { return new HashMap<>(skillProgressMap); }
    public List<String> getTopPerformingAreas() { return new ArrayList<>(topPerformingAreas); }
    public List<String> getImprovementAreas() { return new ArrayList<>(improvementAreas); }
    public List<LearningTrend> getTrends() { return new ArrayList<>(trends); }
    public double getOverallProgress() { return overallProgress; }
    public int getStreakDays() { return streakDays; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public List<String> getRecommendations() { return new ArrayList<>(recommendations); }

    public static class Builder {
        private String id;
        private String developerId;
        private String timeRange;
        private int totalSessionsCompleted = 0;
        private int completedSessions = 0;
        private int totalLearningHours = 0;
        private int totalLearningMinutes = 0;
        private int averageSessionDuration = 30;
        private double averageSessionScore = 0.0;
        private Map<String, Double> skillProgressMap = new HashMap<>();
        private Map<String, Double> skillProgression = new HashMap<>();
        private List<String> topPerformingAreas = new ArrayList<>();
        private List<String> improvementAreas = new ArrayList<>();
        private List<LearningTrend> trends = new ArrayList<>();
        private double overallProgress = 0.0;
        private int streakDays = 0;
        private LocalDateTime generatedAt = LocalDateTime.now();
        private List<String> recommendations = new ArrayList<>();

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder timeRange(String timeRange) { this.timeRange = timeRange; return this; }
        public Builder totalSessionsCompleted(int totalSessionsCompleted) { this.totalSessionsCompleted = totalSessionsCompleted; return this; }
        public Builder completedSessions(int completedSessions) { this.completedSessions = completedSessions; this.totalSessionsCompleted = completedSessions; return this; }
        public Builder totalLearningHours(int totalLearningHours) { this.totalLearningHours = totalLearningHours; this.totalLearningMinutes = totalLearningHours * 60; return this; }
        public Builder totalLearningMinutes(int totalLearningMinutes) { this.totalLearningMinutes = totalLearningMinutes; return this; }
        public Builder averageSessionDuration(int averageSessionDuration) { this.averageSessionDuration = averageSessionDuration; return this; }
        public Builder averageSessionScore(double averageSessionScore) { this.averageSessionScore = averageSessionScore; return this; }
        public Builder skillProgressMap(Map<String, Double> skillProgressMap) { this.skillProgressMap = skillProgressMap; return this; }
        public Builder skillProgression(Map<String, Double> skillProgression) { this.skillProgression = skillProgression; this.skillProgressMap = skillProgression; return this; }
        public Builder topPerformingAreas(List<String> topPerformingAreas) { this.topPerformingAreas = topPerformingAreas; return this; }
        public Builder improvementAreas(List<String> improvementAreas) { this.improvementAreas = improvementAreas; return this; }
        public Builder trends(List<LearningTrend> trends) { this.trends = trends; return this; }
        public Builder overallProgress(double overallProgress) { this.overallProgress = overallProgress; return this; }
        public Builder streakDays(int streakDays) { this.streakDays = streakDays; return this; }
        public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }
        public Builder recommendations(List<String> recommendations) { this.recommendations = recommendations; return this; }

        public LearningAnalytics build() {
            return new LearningAnalytics(developerId, timeRange, totalSessionsCompleted, totalLearningMinutes,
                    averageSessionScore, skillProgressMap, topPerformingAreas, improvementAreas, trends,
                    overallProgress, streakDays, generatedAt, recommendations);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningAnalytics that = (LearningAnalytics) o;
        return Objects.equals(developerId, that.developerId) &&
                Objects.equals(timeRange, that.timeRange) &&
                Objects.equals(generatedAt, that.generatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(developerId, timeRange, generatedAt);
    }

    @Override
    public String toString() {
        return "LearningAnalytics{" +
                "developerId='" + developerId + '\'' +
                ", timeRange='" + timeRange + '\'' +
                ", totalSessions=" + totalSessionsCompleted +
                ", totalMinutes=" + totalLearningMinutes +
                ", averageScore=" + String.format("%.1f", averageSessionScore) +
                ", overallProgress=" + String.format("%.1f", overallProgress) + "%" +
                ", streakDays=" + streakDays +
                '}';
    }
}