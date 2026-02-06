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
 * Represents an assessment of learning retention for a specific topic.
 * Used to track how well knowledge is retained over time and adjust follow-up schedules.
 */
public class RetentionAssessment {
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String developerId;
    
    @NotBlank
    private final String topic;
    
    private final double retentionScore;
    
    private final double recallAccuracy;
    
    private final int daysSinceLastReview;
    
    private final int totalReviewCount;
    
    @NotNull
    private final LocalDateTime assessedAt;
    
    private final RetentionLevel retentionLevel;
    
    @NotNull
    private final List<String> strengthAreas;
    
    @NotNull
    private final List<String> weaknessAreas;
    
    private final String recommendedAction;
    
    private final int recommendedNextReviewDays;

    @JsonCreator
    public RetentionAssessment(
            @JsonProperty("id") String id,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("topic") String topic,
            @JsonProperty("retentionScore") double retentionScore,
            @JsonProperty("recallAccuracy") double recallAccuracy,
            @JsonProperty("daysSinceLastReview") int daysSinceLastReview,
            @JsonProperty("totalReviewCount") int totalReviewCount,
            @JsonProperty("assessedAt") LocalDateTime assessedAt,
            @JsonProperty("retentionLevel") RetentionLevel retentionLevel,
            @JsonProperty("strengthAreas") List<String> strengthAreas,
            @JsonProperty("weaknessAreas") List<String> weaknessAreas,
            @JsonProperty("recommendedAction") String recommendedAction,
            @JsonProperty("recommendedNextReviewDays") int recommendedNextReviewDays) {
        this.id = Objects.requireNonNull(id, "Assessment ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.topic = Objects.requireNonNull(topic, "Topic cannot be null");
        this.retentionScore = Math.max(0.0, Math.min(1.0, retentionScore));
        this.recallAccuracy = Math.max(0.0, Math.min(1.0, recallAccuracy));
        this.daysSinceLastReview = Math.max(0, daysSinceLastReview);
        this.totalReviewCount = Math.max(0, totalReviewCount);
        this.assessedAt = assessedAt != null ? assessedAt : LocalDateTime.now();
        this.retentionLevel = retentionLevel != null ? retentionLevel : determineRetentionLevel(retentionScore);
        this.strengthAreas = strengthAreas != null ? new ArrayList<>(strengthAreas) : new ArrayList<>();
        this.weaknessAreas = weaknessAreas != null ? new ArrayList<>(weaknessAreas) : new ArrayList<>();
        this.recommendedAction = recommendedAction;
        this.recommendedNextReviewDays = Math.max(1, recommendedNextReviewDays);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static RetentionLevel determineRetentionLevel(double score) {
        if (score >= 0.8) return RetentionLevel.EXCELLENT;
        if (score >= 0.6) return RetentionLevel.GOOD;
        if (score >= 0.4) return RetentionLevel.FAIR;
        return RetentionLevel.POOR;
    }

    public boolean needsImmediateReview() {
        return retentionLevel == RetentionLevel.POOR || retentionScore < 0.4;
    }

    public boolean isRetentionStrong() {
        return retentionLevel == RetentionLevel.EXCELLENT || retentionScore >= 0.8;
    }

    // Getters
    public String getId() { return id; }
    public String getDeveloperId() { return developerId; }
    public String getTopic() { return topic; }
    public double getRetentionScore() { return retentionScore; }
    public double getRecallAccuracy() { return recallAccuracy; }
    public int getDaysSinceLastReview() { return daysSinceLastReview; }
    public int getTotalReviewCount() { return totalReviewCount; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
    public RetentionLevel getRetentionLevel() { return retentionLevel; }
    public List<String> getStrengthAreas() { return new ArrayList<>(strengthAreas); }
    public List<String> getWeaknessAreas() { return new ArrayList<>(weaknessAreas); }
    public String getRecommendedAction() { return recommendedAction; }
    public int getRecommendedNextReviewDays() { return recommendedNextReviewDays; }

    public enum RetentionLevel {
        EXCELLENT(4),
        GOOD(3),
        FAIR(2),
        POOR(1);

        private final int level;

        RetentionLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    public static class Builder {
        private String id;
        private String developerId;
        private String topic;
        private double retentionScore = 0.5;
        private double recallAccuracy = 0.5;
        private int daysSinceLastReview = 0;
        private int totalReviewCount = 0;
        private LocalDateTime assessedAt = LocalDateTime.now();
        private RetentionLevel retentionLevel;
        private List<String> strengthAreas = new ArrayList<>();
        private List<String> weaknessAreas = new ArrayList<>();
        private String recommendedAction;
        private int recommendedNextReviewDays = 7;

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder retentionScore(double retentionScore) { this.retentionScore = retentionScore; return this; }
        public Builder recallAccuracy(double recallAccuracy) { this.recallAccuracy = recallAccuracy; return this; }
        public Builder daysSinceLastReview(int daysSinceLastReview) { this.daysSinceLastReview = daysSinceLastReview; return this; }
        public Builder totalReviewCount(int totalReviewCount) { this.totalReviewCount = totalReviewCount; return this; }
        public Builder assessedAt(LocalDateTime assessedAt) { this.assessedAt = assessedAt; return this; }
        public Builder retentionLevel(RetentionLevel retentionLevel) { this.retentionLevel = retentionLevel; return this; }
        public Builder strengthAreas(List<String> strengthAreas) { this.strengthAreas = strengthAreas; return this; }
        public Builder weaknessAreas(List<String> weaknessAreas) { this.weaknessAreas = weaknessAreas; return this; }
        public Builder recommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; return this; }
        public Builder recommendedNextReviewDays(int recommendedNextReviewDays) { this.recommendedNextReviewDays = recommendedNextReviewDays; return this; }

        public RetentionAssessment build() {
            return new RetentionAssessment(id, developerId, topic, retentionScore, recallAccuracy,
                    daysSinceLastReview, totalReviewCount, assessedAt, retentionLevel, strengthAreas,
                    weaknessAreas, recommendedAction, recommendedNextReviewDays);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetentionAssessment that = (RetentionAssessment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RetentionAssessment{" +
                "id='" + id + '\'' +
                ", topic='" + topic + '\'' +
                ", retentionScore=" + String.format("%.2f", retentionScore) +
                ", retentionLevel=" + retentionLevel +
                ", recommendedNextReviewDays=" + recommendedNextReviewDays +
                '}';
    }
}
