package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a performance alert triggered when metrics exceed thresholds.
 */
public class PerformanceAlert {
    
    public enum AlertType {
        RESPONSE_TIME_EXCEEDED,
        MEMORY_THRESHOLD_EXCEEDED,
        CPU_THRESHOLD_EXCEEDED,
        ERROR_RATE_HIGH,
        SYSTEM_OVERLOAD,
        BACKGROUND_TASK_TIMEOUT
    }
    
    public enum AlertStatus {
        ACTIVE,
        ACKNOWLEDGED,
        RESOLVED,
        SUPPRESSED
    }
    
    private final String alertId;
    private final AlertType type;
    private final AlertStatus status;
    private final String title;
    private final String message;
    private final PerformanceMetrics.Severity severity;
    private final LocalDateTime triggeredAt;
    private final LocalDateTime acknowledgedAt;
    private final LocalDateTime resolvedAt;
    private final String operation;
    private final double currentValue;
    private final double threshold;
    private final String unit;
    private final String recommendedAction;
    
    private PerformanceAlert(Builder builder) {
        this.alertId = Objects.requireNonNull(builder.alertId, "Alert ID cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Alert type cannot be null");
        this.status = Objects.requireNonNull(builder.status, "Alert status cannot be null");
        this.title = Objects.requireNonNull(builder.title, "Title cannot be null");
        this.message = Objects.requireNonNull(builder.message, "Message cannot be null");
        this.severity = Objects.requireNonNull(builder.severity, "Severity cannot be null");
        this.triggeredAt = Objects.requireNonNull(builder.triggeredAt, "Triggered time cannot be null");
        this.acknowledgedAt = builder.acknowledgedAt;
        this.resolvedAt = builder.resolvedAt;
        this.operation = builder.operation;
        this.currentValue = builder.currentValue;
        this.threshold = builder.threshold;
        this.unit = builder.unit;
        this.recommendedAction = builder.recommendedAction;
    }
    
    public String getAlertId() {
        return alertId;
    }
    
    public AlertType getType() {
        return type;
    }
    
    public AlertStatus getStatus() {
        return status;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public PerformanceMetrics.Severity getSeverity() {
        return severity;
    }
    
    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }
    
    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public double getCurrentValue() {
        return currentValue;
    }
    
    public double getThreshold() {
        return threshold;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public String getRecommendedAction() {
        return recommendedAction;
    }
    
    /**
     * Checks if this alert is currently active.
     */
    public boolean isActive() {
        return status == AlertStatus.ACTIVE;
    }
    
    /**
     * Checks if this alert has been acknowledged.
     */
    public boolean isAcknowledged() {
        return acknowledgedAt != null;
    }
    
    /**
     * Checks if this alert has been resolved.
     */
    public boolean isResolved() {
        return status == AlertStatus.RESOLVED && resolvedAt != null;
    }
    
    /**
     * Gets the duration since the alert was triggered.
     */
    public long getDurationMinutes() {
        LocalDateTime endTime = resolvedAt != null ? resolvedAt : LocalDateTime.now();
        return java.time.Duration.between(triggeredAt, endTime).toMinutes();
    }
    
    /**
     * Gets the threshold exceedance percentage.
     */
    public double getExceedancePercentage() {
        if (threshold <= 0) {
            return 0.0;
        }
        return ((currentValue - threshold) / threshold) * 100.0;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String alertId;
        private AlertType type;
        private AlertStatus status = AlertStatus.ACTIVE;
        private String title;
        private String message;
        private PerformanceMetrics.Severity severity;
        private LocalDateTime triggeredAt = LocalDateTime.now();
        private LocalDateTime acknowledgedAt;
        private LocalDateTime resolvedAt;
        private String operation;
        private double currentValue;
        private double threshold;
        private String unit;
        private String recommendedAction;
        
        public Builder alertId(String alertId) {
            this.alertId = alertId;
            return this;
        }
        
        public Builder type(AlertType type) {
            this.type = type;
            return this;
        }
        
        public Builder status(AlertStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder severity(PerformanceMetrics.Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder triggeredAt(LocalDateTime triggeredAt) {
            this.triggeredAt = triggeredAt;
            return this;
        }
        
        public Builder acknowledgedAt(LocalDateTime acknowledgedAt) {
            this.acknowledgedAt = acknowledgedAt;
            return this;
        }
        
        public Builder resolvedAt(LocalDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }
        
        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }
        
        public Builder currentValue(double currentValue) {
            this.currentValue = currentValue;
            return this;
        }
        
        public Builder threshold(double threshold) {
            this.threshold = threshold;
            return this;
        }
        
        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }
        
        public Builder recommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
            return this;
        }
        
        public PerformanceAlert build() {
            return new PerformanceAlert(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceAlert that = (PerformanceAlert) o;
        return Double.compare(that.currentValue, currentValue) == 0 &&
               Double.compare(that.threshold, threshold) == 0 &&
               Objects.equals(alertId, that.alertId) &&
               type == that.type &&
               status == that.status &&
               Objects.equals(title, that.title) &&
               Objects.equals(message, that.message) &&
               severity == that.severity &&
               Objects.equals(triggeredAt, that.triggeredAt) &&
               Objects.equals(acknowledgedAt, that.acknowledgedAt) &&
               Objects.equals(resolvedAt, that.resolvedAt) &&
               Objects.equals(operation, that.operation) &&
               Objects.equals(unit, that.unit) &&
               Objects.equals(recommendedAction, that.recommendedAction);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(alertId, type, status, title, message, severity, triggeredAt, acknowledgedAt, resolvedAt, operation, currentValue, threshold, unit, recommendedAction);
    }
    
    @Override
    public String toString() {
        return "PerformanceAlert{" +
               "alertId='" + alertId + '\'' +
               ", type=" + type +
               ", status=" + status +
               ", title='" + title + '\'' +
               ", severity=" + severity +
               ", triggeredAt=" + triggeredAt +
               ", operation='" + operation + '\'' +
               ", currentValue=" + currentValue +
               ", threshold=" + threshold +
               ", unit='" + unit + '\'' +
               ", durationMinutes=" + getDurationMinutes() +
               ", exceedancePercentage=" + String.format("%.1f%%", getExceedancePercentage()) +
               '}';
    }
}