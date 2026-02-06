package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a learning trend or pattern identified in a developer's learning history.
 * Contains trend information, direction, and significance metrics.
 */
public class LearningTrend {
    
    @NotBlank
    private final String metric;
    
    @NotNull
    private final TrendDirection direction;
    
    private final double changePercentage;
    
    private final double significance;
    
    @NotBlank
    private final String description;
    
    @NotNull
    private final LocalDateTime periodStart;
    
    @NotNull
    private final LocalDateTime periodEnd;
    
    private final String recommendation;

    @JsonCreator
    public LearningTrend(
            @JsonProperty("metric") String metric,
            @JsonProperty("direction") TrendDirection direction,
            @JsonProperty("changePercentage") double changePercentage,
            @JsonProperty("significance") double significance,
            @JsonProperty("description") String description,
            @JsonProperty("periodStart") LocalDateTime periodStart,
            @JsonProperty("periodEnd") LocalDateTime periodEnd,
            @JsonProperty("recommendation") String recommendation) {
        this.metric = Objects.requireNonNull(metric, "Metric cannot be null");
        this.direction = Objects.requireNonNull(direction, "Direction cannot be null");
        this.changePercentage = changePercentage;
        this.significance = Math.max(0.0, Math.min(1.0, significance));
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.periodStart = Objects.requireNonNull(periodStart, "Period start cannot be null");
        this.periodEnd = Objects.requireNonNull(periodEnd, "Period end cannot be null");
        this.recommendation = recommendation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LearningTrend improving(String metric, double changePercentage, String description) {
        return new LearningTrend(metric, TrendDirection.IMPROVING, changePercentage, 0.8, description,
                LocalDateTime.now().minusDays(30), LocalDateTime.now(), null);
    }

    public static LearningTrend declining(String metric, double changePercentage, String description) {
        return new LearningTrend(metric, TrendDirection.DECLINING, changePercentage, 0.8, description,
                LocalDateTime.now().minusDays(30), LocalDateTime.now(), null);
    }

    public boolean isSignificant() {
        return significance >= 0.7;
    }

    public boolean isPositive() {
        return direction == TrendDirection.IMPROVING;
    }

    public boolean requiresAttention() {
        return direction == TrendDirection.DECLINING && significance >= 0.6;
    }

    // Getters
    public String getMetric() { return metric; }
    public TrendDirection getDirection() { return direction; }
    public double getChangePercentage() { return changePercentage; }
    public double getSignificance() { return significance; }
    public String getDescription() { return description; }
    public LocalDateTime getPeriodStart() { return periodStart; }
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public String getRecommendation() { return recommendation; }

    public enum TrendDirection {
        IMPROVING, DECLINING, STABLE, VOLATILE
    }

    public static class Builder {
        private String id;
        private String metric;
        private String skillDomain;
        private String trendType;
        private double value;
        private String timeRange;
        private TrendDirection direction;
        private double changePercentage;
        private double significance;
        private String description;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        private String recommendation;

        public Builder id(String id) { this.id = id; return this; }
        public Builder metric(String metric) { this.metric = metric; return this; }
        public Builder skillDomain(String skillDomain) { this.skillDomain = skillDomain; return this; }
        public Builder trendType(String trendType) { this.trendType = trendType; return this; }
        public Builder value(double value) { this.value = value; this.changePercentage = value; return this; }
        public Builder timeRange(String timeRange) { this.timeRange = timeRange; return this; }
        public Builder direction(TrendDirection direction) { this.direction = direction; return this; }
        public Builder changePercentage(double changePercentage) { this.changePercentage = changePercentage; return this; }
        public Builder significance(double significance) { this.significance = significance; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder periodStart(LocalDateTime periodStart) { this.periodStart = periodStart; return this; }
        public Builder periodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; return this; }
        public Builder recommendation(String recommendation) { this.recommendation = recommendation; return this; }

        public LearningTrend build() {
            if (metric == null) metric = skillDomain != null ? skillDomain : "general";
            return new LearningTrend(metric, direction, changePercentage, significance, description,
                    periodStart, periodEnd, recommendation);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningTrend that = (LearningTrend) o;
        return Objects.equals(metric, that.metric) &&
                Objects.equals(periodStart, that.periodStart) &&
                Objects.equals(periodEnd, that.periodEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metric, periodStart, periodEnd);
    }

    @Override
    public String toString() {
        return "LearningTrend{" +
                "metric='" + metric + '\'' +
                ", direction=" + direction +
                ", changePercentage=" + String.format("%.1f", changePercentage) + "%" +
                ", significance=" + String.format("%.2f", significance) +
                '}';
    }
}