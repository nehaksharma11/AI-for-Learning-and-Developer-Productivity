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
 * Represents a context switching event that occurs when a developer
 * changes their focus from one task or activity to another.
 */
public class ContextSwitchEvent {
    
    public enum SwitchType {
        TASK_CHANGE,        // Switching between different tasks
        PROJECT_CHANGE,     // Switching between different projects
        ACTIVITY_CHANGE,    // Switching between different activities (coding, debugging, etc.)
        FILE_CHANGE,        // Switching between different files
        INTERRUPTION,       // External interruption (meeting, message, etc.)
        BREAK,             // Taking a break
        RETURN_FROM_BREAK  // Returning from a break
    }
    
    public enum SwitchReason {
        PLANNED,           // Intentional, planned switch
        INTERRUPTION,      // External interruption
        DISTRACTION,       // Internal distraction
        COMPLETION,        // Previous task completed
        BLOCKED,           // Previous task blocked
        PRIORITY_CHANGE,   // Priority change required switch
        UNKNOWN            // Reason not determined
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String developerId;
    
    @NotNull
    private final LocalDateTime timestamp;
    
    @NotNull
    private final SwitchType switchType;
    
    @NotNull
    private final SwitchReason switchReason;
    
    // Previous context
    private final String previousProjectId;
    private final String previousTask;
    private final String previousActivity;
    private final String previousFile;
    private final String previousWorkStateId;
    
    // New context
    private final String newProjectId;
    private final String newTask;
    private final String newActivity;
    private final String newFile;
    private final String newWorkStateId;
    
    // Switch metadata
    private final long previousContextDurationMinutes;
    private final int interruptionCount;
    private final String interruptionSource;
    
    @NotNull
    private final Map<String, Object> metadata;
    
    // Productivity impact
    private final double estimatedProductivityImpact; // -1.0 to 1.0
    private final long estimatedRecoveryTimeMinutes;

    @JsonCreator
    public ContextSwitchEvent(
            @JsonProperty("id") String id,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("switchType") SwitchType switchType,
            @JsonProperty("switchReason") SwitchReason switchReason,
            @JsonProperty("previousProjectId") String previousProjectId,
            @JsonProperty("previousTask") String previousTask,
            @JsonProperty("previousActivity") String previousActivity,
            @JsonProperty("previousFile") String previousFile,
            @JsonProperty("previousWorkStateId") String previousWorkStateId,
            @JsonProperty("newProjectId") String newProjectId,
            @JsonProperty("newTask") String newTask,
            @JsonProperty("newActivity") String newActivity,
            @JsonProperty("newFile") String newFile,
            @JsonProperty("newWorkStateId") String newWorkStateId,
            @JsonProperty("previousContextDurationMinutes") long previousContextDurationMinutes,
            @JsonProperty("interruptionCount") int interruptionCount,
            @JsonProperty("interruptionSource") String interruptionSource,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("estimatedProductivityImpact") double estimatedProductivityImpact,
            @JsonProperty("estimatedRecoveryTimeMinutes") long estimatedRecoveryTimeMinutes) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.switchType = Objects.requireNonNull(switchType, "Switch type cannot be null");
        this.switchReason = Objects.requireNonNull(switchReason, "Switch reason cannot be null");
        this.previousProjectId = previousProjectId;
        this.previousTask = previousTask;
        this.previousActivity = previousActivity;
        this.previousFile = previousFile;
        this.previousWorkStateId = previousWorkStateId;
        this.newProjectId = newProjectId;
        this.newTask = newTask;
        this.newActivity = newActivity;
        this.newFile = newFile;
        this.newWorkStateId = newWorkStateId;
        this.previousContextDurationMinutes = Math.max(0, previousContextDurationMinutes);
        this.interruptionCount = Math.max(0, interruptionCount);
        this.interruptionSource = interruptionSource;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.estimatedProductivityImpact = Math.max(-1.0, Math.min(1.0, estimatedProductivityImpact));
        this.estimatedRecoveryTimeMinutes = Math.max(0, estimatedRecoveryTimeMinutes);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Determines if this context switch represents a significant disruption.
     */
    public boolean isSignificantDisruption() {
        // Consider it significant if:
        // 1. It's an interruption with negative productivity impact
        // 2. Recovery time is substantial (> 5 minutes)
        // 3. Previous context was active for a reasonable time (> 10 minutes)
        return (switchReason == SwitchReason.INTERRUPTION && estimatedProductivityImpact < -0.3) ||
               estimatedRecoveryTimeMinutes > 5 ||
               (previousContextDurationMinutes > 10 && estimatedProductivityImpact < -0.2);
    }

    /**
     * Calculates the context switching cost based on various factors.
     */
    public double getContextSwitchingCost() {
        double baseCost = calculateBaseCost();
        double durationMultiplier = calculateDurationMultiplier();
        double reasonMultiplier = calculateReasonMultiplier();
        double typeMultiplier = calculateTypeMultiplier();
        
        return baseCost * durationMultiplier * reasonMultiplier * typeMultiplier;
    }

    /**
     * Gets a human-readable description of this context switch.
     */
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        
        description.append("Switched from ");
        if (previousTask != null) {
            description.append("task '").append(previousTask).append("'");
        } else if (previousActivity != null) {
            description.append("activity '").append(previousActivity).append("'");
        } else if (previousFile != null) {
            description.append("file '").append(previousFile).append("'");
        } else {
            description.append("previous context");
        }
        
        description.append(" to ");
        if (newTask != null) {
            description.append("task '").append(newTask).append("'");
        } else if (newActivity != null) {
            description.append("activity '").append(newActivity).append("'");
        } else if (newFile != null) {
            description.append("file '").append(newFile).append("'");
        } else {
            description.append("new context");
        }
        
        if (switchReason != SwitchReason.UNKNOWN) {
            description.append(" (").append(switchReason.name().toLowerCase().replace('_', ' ')).append(")");
        }
        
        return description.toString();
    }

    private double calculateBaseCost() {
        // Base cost in minutes of lost productivity
        return estimatedRecoveryTimeMinutes + (Math.abs(estimatedProductivityImpact) * 10);
    }

    private double calculateDurationMultiplier() {
        // Longer previous context duration = higher switching cost
        if (previousContextDurationMinutes <= 5) return 0.5;
        if (previousContextDurationMinutes <= 15) return 0.8;
        if (previousContextDurationMinutes <= 30) return 1.0;
        if (previousContextDurationMinutes <= 60) return 1.2;
        return 1.5;
    }

    private double calculateReasonMultiplier() {
        switch (switchReason) {
            case INTERRUPTION: return 1.5;
            case DISTRACTION: return 1.3;
            case BLOCKED: return 0.8;
            case COMPLETION: return 0.5;
            case PLANNED: return 0.7;
            case PRIORITY_CHANGE: return 1.0;
            default: return 1.0;
        }
    }

    private double calculateTypeMultiplier() {
        switch (switchType) {
            case PROJECT_CHANGE: return 1.4;
            case TASK_CHANGE: return 1.2;
            case ACTIVITY_CHANGE: return 1.0;
            case FILE_CHANGE: return 0.6;
            case INTERRUPTION: return 1.3;
            case BREAK: return 0.3;
            case RETURN_FROM_BREAK: return 0.8;
            default: return 1.0;
        }
    }

    // Getters
    public String getId() { return id; }
    public String getDeveloperId() { return developerId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public SwitchType getSwitchType() { return switchType; }
    public SwitchReason getSwitchReason() { return switchReason; }
    public String getPreviousProjectId() { return previousProjectId; }
    public String getPreviousTask() { return previousTask; }
    public String getPreviousActivity() { return previousActivity; }
    public String getPreviousFile() { return previousFile; }
    public String getPreviousWorkStateId() { return previousWorkStateId; }
    public String getNewProjectId() { return newProjectId; }
    public String getNewTask() { return newTask; }
    public String getNewActivity() { return newActivity; }
    public String getNewFile() { return newFile; }
    public String getNewWorkStateId() { return newWorkStateId; }
    public long getPreviousContextDurationMinutes() { return previousContextDurationMinutes; }
    public int getInterruptionCount() { return interruptionCount; }
    public String getInterruptionSource() { return interruptionSource; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public double getEstimatedProductivityImpact() { return estimatedProductivityImpact; }
    public long getEstimatedRecoveryTimeMinutes() { return estimatedRecoveryTimeMinutes; }

    public static class Builder {
        private String id;
        private String developerId;
        private LocalDateTime timestamp = LocalDateTime.now();
        private SwitchType switchType;
        private SwitchReason switchReason = SwitchReason.UNKNOWN;
        private String previousProjectId;
        private String previousTask;
        private String previousActivity;
        private String previousFile;
        private String previousWorkStateId;
        private String newProjectId;
        private String newTask;
        private String newActivity;
        private String newFile;
        private String newWorkStateId;
        private long previousContextDurationMinutes = 0;
        private int interruptionCount = 0;
        private String interruptionSource;
        private Map<String, Object> metadata = new HashMap<>();
        private double estimatedProductivityImpact = 0.0;
        private long estimatedRecoveryTimeMinutes = 0;

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder switchType(SwitchType switchType) { this.switchType = switchType; return this; }
        public Builder switchReason(SwitchReason switchReason) { this.switchReason = switchReason; return this; }
        public Builder previousProjectId(String previousProjectId) { this.previousProjectId = previousProjectId; return this; }
        public Builder previousTask(String previousTask) { this.previousTask = previousTask; return this; }
        public Builder previousActivity(String previousActivity) { this.previousActivity = previousActivity; return this; }
        public Builder previousFile(String previousFile) { this.previousFile = previousFile; return this; }
        public Builder previousWorkStateId(String previousWorkStateId) { this.previousWorkStateId = previousWorkStateId; return this; }
        public Builder newProjectId(String newProjectId) { this.newProjectId = newProjectId; return this; }
        public Builder newTask(String newTask) { this.newTask = newTask; return this; }
        public Builder newActivity(String newActivity) { this.newActivity = newActivity; return this; }
        public Builder newFile(String newFile) { this.newFile = newFile; return this; }
        public Builder newWorkStateId(String newWorkStateId) { this.newWorkStateId = newWorkStateId; return this; }
        public Builder previousContextDurationMinutes(long previousContextDurationMinutes) { this.previousContextDurationMinutes = previousContextDurationMinutes; return this; }
        public Builder interruptionCount(int interruptionCount) { this.interruptionCount = interruptionCount; return this; }
        public Builder interruptionSource(String interruptionSource) { this.interruptionSource = interruptionSource; return this; }
        public Builder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
        public Builder estimatedProductivityImpact(double estimatedProductivityImpact) { this.estimatedProductivityImpact = estimatedProductivityImpact; return this; }
        public Builder estimatedRecoveryTimeMinutes(long estimatedRecoveryTimeMinutes) { this.estimatedRecoveryTimeMinutes = estimatedRecoveryTimeMinutes; return this; }

        public ContextSwitchEvent build() {
            return new ContextSwitchEvent(id, developerId, timestamp, switchType, switchReason,
                    previousProjectId, previousTask, previousActivity, previousFile, previousWorkStateId,
                    newProjectId, newTask, newActivity, newFile, newWorkStateId,
                    previousContextDurationMinutes, interruptionCount, interruptionSource, metadata,
                    estimatedProductivityImpact, estimatedRecoveryTimeMinutes);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextSwitchEvent that = (ContextSwitchEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ContextSwitchEvent{" +
                "id='" + id + '\'' +
                ", developerId='" + developerId + '\'' +
                ", switchType=" + switchType +
                ", switchReason=" + switchReason +
                ", cost=" + String.format("%.1f", getContextSwitchingCost()) +
                ", recovery=" + estimatedRecoveryTimeMinutes + "min" +
                '}';
    }
}