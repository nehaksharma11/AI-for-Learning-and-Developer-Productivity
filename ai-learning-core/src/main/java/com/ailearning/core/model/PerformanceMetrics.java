package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents performance metrics for monitoring system performance and resource usage.
 */
public class PerformanceMetrics {
    
    public enum MetricType {
        RESPONSE_TIME,
        MEMORY_USAGE,
        CPU_USAGE,
        THROUGHPUT,
        ERROR_RATE,
        CACHE_HIT_RATE,
        BACKGROUND_TASK_DURATION
    }
    
    public enum Severity {
        INFO,
        WARNING,
        CRITICAL,
        ALERT
    }
    
    private final String metricId;
    private final MetricType type;
    private final String operation;
    private final double value;
    private final String unit;
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata;
    private final Severity severity;
    private final String description;
    private final double threshold;
    private final boolean alertTriggered;
    
    private PerformanceMetrics(Builder builder) {
        this.metricId = Objects.requireNonNull(builder.metricId, "Metric ID cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Metric type cannot be null");
        this.operation = Objects.requireNonNull(builder.operation, "Operation cannot be null");
        this.value = builder.value;
        this.unit = Objects.requireNonNull(builder.unit, "Unit cannot be null");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "Timestamp cannot be null");
        this.metadata = Map.copyOf(Objects.requireNonNull(builder.metadata, "Metadata cannot be null"));
        this.severity = Objects.requireNonNull(builder.severity, "Severity cannot be null");
        this.description = builder.description;
        this.threshold = builder.threshold;
        this.alertTriggered = builder.alertTriggered;
    }
    
    public String getMetricId() {
        return metricId;
    }
    
    public MetricType getType() {
        return type;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public double getValue() {
        return value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public double getThreshold() {
        return threshold;
    }
    
    public boolean isAlertTriggered() {
        return alertTriggered;
    }
    
    /**
     * Checks if this metric exceeds its threshold.
     */
    public boolean exceedsThreshold() {
        return threshold > 0 && value > threshold;
    }
    
    /**
     * Checks if this metric indicates a performance issue.
     */
    public boolean isPerformanceIssue() {
        return severity == Severity.WARNING || severity == Severity.CRITICAL || severity == Severity.ALERT;
    }
    
    /**
     * Gets the performance impact level based on severity and threshold.
     */
    public String getImpactLevel() {
        if (severity == Severity.CRITICAL || severity == Severity.ALERT) {
            return "HIGH";
        } else if (severity == Severity.WARNING || exceedsThreshold()) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String metricId;
        private MetricType type;
        private String operation;
        private double value;
        private String unit;
        private LocalDateTime timestamp = LocalDateTime.now();
        private Map<String, Object> metadata = Map.of();
        private Severity severity = Severity.INFO;
        private String description;
        private double threshold = 0.0;
        private boolean alertTriggered = false;
        
        public Builder metricId(String metricId) {
            this.metricId = metricId;
            return this;
        }
        
        public Builder type(MetricType type) {
            this.type = type;
            return this;
        }
        
        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }
        
        public Builder value(double value) {
            this.value = value;
            return this;
        }
        
        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }
        
        public Builder alertTriggered(boolean alertTriggered) {
            this.alertTriggered = alertTriggered;
            return this;
        }
        
        public PerformanceMetrics build() {
            return new PerformanceMetrics(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceMetrics that = (PerformanceMetrics) o;
        return Double.compare(that.value, value) == 0 &&
               Double.compare(that.threshold, threshold) == 0 &&
               alertTriggered == that.alertTriggered &&
               Objects.equals(metricId, that.metricId) &&
               type == that.type &&
               Objects.equals(operation, that.operation) &&
               Objects.equals(unit, that.unit) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(metadata, that.metadata) &&
               severity == that.severity &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(metricId, type, operation, value, unit, timestamp, metadata, severity, description, threshold, alertTriggered);
    }
    
    @Override
    public String toString() {
        return "PerformanceMetrics{" +
               "metricId='" + metricId + '\'' +
               ", type=" + type +
               ", operation='" + operation + '\'' +
               ", value=" + value +
               ", unit='" + unit + '\'' +
               ", timestamp=" + timestamp +
               ", severity=" + severity +
               ", description='" + description + '\'' +
               ", threshold=" + threshold +
               ", alertTriggered=" + alertTriggered +
               ", impactLevel='" + getImpactLevel() + '\'' +
               '}';
    }
}