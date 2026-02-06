package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a background task for processing large operations without blocking the main thread.
 */
public class BackgroundTask<T> {
    
    public enum TaskType {
        CODEBASE_ANALYSIS,
        DOCUMENTATION_GENERATION,
        LEARNING_PATH_CALCULATION,
        PATTERN_DETECTION,
        SECURITY_SCAN,
        PERFORMANCE_OPTIMIZATION
    }
    
    public enum TaskStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED,
        TIMEOUT
    }
    
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }
    
    private final String taskId;
    private final TaskType type;
    private final Priority priority;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final TaskStatus status;
    private final double progressPercentage;
    private final String currentStep;
    private final long estimatedDurationMs;
    private final long actualDurationMs;
    private final CompletableFuture<T> future;
    private final Exception error;
    private final T result;
    
    private BackgroundTask(Builder<T> builder) {
        this.taskId = Objects.requireNonNull(builder.taskId, "Task ID cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Task type cannot be null");
        this.priority = Objects.requireNonNull(builder.priority, "Priority cannot be null");
        this.description = Objects.requireNonNull(builder.description, "Description cannot be null");
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created time cannot be null");
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.status = Objects.requireNonNull(builder.status, "Status cannot be null");
        this.progressPercentage = builder.progressPercentage;
        this.currentStep = builder.currentStep;
        this.estimatedDurationMs = builder.estimatedDurationMs;
        this.actualDurationMs = builder.actualDurationMs;
        this.future = builder.future;
        this.error = builder.error;
        this.result = builder.result;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public TaskType getType() {
        return type;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public double getProgressPercentage() {
        return progressPercentage;
    }
    
    public String getCurrentStep() {
        return currentStep;
    }
    
    public long getEstimatedDurationMs() {
        return estimatedDurationMs;
    }
    
    public long getActualDurationMs() {
        return actualDurationMs;
    }
    
    public CompletableFuture<T> getFuture() {
        return future;
    }
    
    public Exception getError() {
        return error;
    }
    
    public T getResult() {
        return result;
    }
    
    /**
     * Checks if the task is currently running.
     */
    public boolean isRunning() {
        return status == TaskStatus.RUNNING;
    }
    
    /**
     * Checks if the task has completed successfully.
     */
    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }
    
    /**
     * Checks if the task has failed.
     */
    public boolean isFailed() {
        return status == TaskStatus.FAILED;
    }
    
    /**
     * Checks if the task was cancelled.
     */
    public boolean isCancelled() {
        return status == TaskStatus.CANCELLED;
    }
    
    /**
     * Checks if the task has timed out.
     */
    public boolean isTimedOut() {
        return status == TaskStatus.TIMEOUT;
    }
    
    /**
     * Gets the elapsed time since the task started.
     */
    public long getElapsedTimeMs() {
        if (startedAt == null) {
            return 0;
        }
        
        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, endTime).toMillis();
    }
    
    /**
     * Gets the remaining estimated time.
     */
    public long getRemainingTimeMs() {
        if (estimatedDurationMs <= 0 || progressPercentage <= 0) {
            return -1; // Unknown
        }
        
        long elapsedTime = getElapsedTimeMs();
        double remainingProgress = 100.0 - progressPercentage;
        
        if (progressPercentage >= 100.0) {
            return 0;
        }
        
        return (long) ((elapsedTime / progressPercentage) * remainingProgress);
    }
    
    /**
     * Checks if the task is taking longer than estimated.
     */
    public boolean isOverdue() {
        return estimatedDurationMs > 0 && getElapsedTimeMs() > estimatedDurationMs;
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    public static class Builder<T> {
        private String taskId;
        private TaskType type;
        private Priority priority = Priority.NORMAL;
        private String description;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private TaskStatus status = TaskStatus.PENDING;
        private double progressPercentage = 0.0;
        private String currentStep;
        private long estimatedDurationMs = 0;
        private long actualDurationMs = 0;
        private CompletableFuture<T> future;
        private Exception error;
        private T result;
        
        public Builder<T> taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }
        
        public Builder<T> type(TaskType type) {
            this.type = type;
            return this;
        }
        
        public Builder<T> priority(Priority priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder<T> createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder<T> startedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }
        
        public Builder<T> completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }
        
        public Builder<T> status(TaskStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder<T> progressPercentage(double progressPercentage) {
            this.progressPercentage = progressPercentage;
            return this;
        }
        
        public Builder<T> currentStep(String currentStep) {
            this.currentStep = currentStep;
            return this;
        }
        
        public Builder<T> estimatedDurationMs(long estimatedDurationMs) {
            this.estimatedDurationMs = estimatedDurationMs;
            return this;
        }
        
        public Builder<T> actualDurationMs(long actualDurationMs) {
            this.actualDurationMs = actualDurationMs;
            return this;
        }
        
        public Builder<T> future(CompletableFuture<T> future) {
            this.future = future;
            return this;
        }
        
        public Builder<T> error(Exception error) {
            this.error = error;
            return this;
        }
        
        public Builder<T> result(T result) {
            this.result = result;
            return this;
        }
        
        public BackgroundTask<T> build() {
            return new BackgroundTask<>(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackgroundTask<?> that = (BackgroundTask<?>) o;
        return Double.compare(that.progressPercentage, progressPercentage) == 0 &&
               estimatedDurationMs == that.estimatedDurationMs &&
               actualDurationMs == that.actualDurationMs &&
               Objects.equals(taskId, that.taskId) &&
               type == that.type &&
               priority == that.priority &&
               Objects.equals(description, that.description) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(startedAt, that.startedAt) &&
               Objects.equals(completedAt, that.completedAt) &&
               status == that.status &&
               Objects.equals(currentStep, that.currentStep);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(taskId, type, priority, description, createdAt, startedAt, completedAt, status, progressPercentage, currentStep, estimatedDurationMs, actualDurationMs);
    }
    
    @Override
    public String toString() {
        return "BackgroundTask{" +
               "taskId='" + taskId + '\'' +
               ", type=" + type +
               ", priority=" + priority +
               ", description='" + description + '\'' +
               ", status=" + status +
               ", progressPercentage=" + progressPercentage +
               ", currentStep='" + currentStep + '\'' +
               ", elapsedTimeMs=" + getElapsedTimeMs() +
               ", estimatedDurationMs=" + estimatedDurationMs +
               ", isOverdue=" + isOverdue() +
               '}';
    }
}