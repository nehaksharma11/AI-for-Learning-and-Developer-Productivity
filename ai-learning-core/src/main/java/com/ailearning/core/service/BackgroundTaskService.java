package com.ailearning.core.service;

import com.ailearning.core.model.BackgroundTask;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Service for managing background tasks to handle large operations without blocking the main thread.
 */
public interface BackgroundTaskService {
    
    /**
     * Submits a background task for execution.
     * 
     * @param type The type of task
     * @param description Description of the task
     * @param priority Task priority
     * @param estimatedDurationMs Estimated duration in milliseconds
     * @param taskSupplier The task to execute
     * @param <T> The result type
     * @return The background task with future
     */
    <T> BackgroundTask<T> submitTask(BackgroundTask.TaskType type, 
                                   String description, 
                                   BackgroundTask.Priority priority,
                                   long estimatedDurationMs,
                                   Supplier<T> taskSupplier);
    
    /**
     * Submits a background task with progress tracking.
     * 
     * @param type The type of task
     * @param description Description of the task
     * @param priority Task priority
     * @param estimatedDurationMs Estimated duration in milliseconds
     * @param progressTrackingTask The task with progress tracking
     * @param <T> The result type
     * @return The background task with future
     */
    <T> BackgroundTask<T> submitTaskWithProgress(BackgroundTask.TaskType type,
                                               String description,
                                               BackgroundTask.Priority priority,
                                               long estimatedDurationMs,
                                               ProgressTrackingTask<T> progressTrackingTask);
    
    /**
     * Gets a background task by ID.
     * 
     * @param taskId The task ID
     * @return The background task, if found
     */
    Optional<BackgroundTask<?>> getTask(String taskId);
    
    /**
     * Gets all background tasks.
     * 
     * @return List of all background tasks
     */
    List<BackgroundTask<?>> getAllTasks();
    
    /**
     * Gets background tasks by status.
     * 
     * @param status The task status
     * @return List of background tasks with the specified status
     */
    List<BackgroundTask<?>> getTasksByStatus(BackgroundTask.TaskStatus status);
    
    /**
     * Gets background tasks by type.
     * 
     * @param type The task type
     * @return List of background tasks of the specified type
     */
    List<BackgroundTask<?>> getTasksByType(BackgroundTask.TaskType type);
    
    /**
     * Gets currently running background tasks.
     * 
     * @return List of running background tasks
     */
    List<BackgroundTask<?>> getRunningTasks();
    
    /**
     * Gets pending background tasks ordered by priority.
     * 
     * @return List of pending background tasks
     */
    List<BackgroundTask<?>> getPendingTasks();
    
    /**
     * Cancels a background task.
     * 
     * @param taskId The task ID
     * @return True if the task was cancelled successfully
     */
    boolean cancelTask(String taskId);
    
    /**
     * Cancels all pending tasks of a specific type.
     * 
     * @param type The task type
     * @return Number of tasks cancelled
     */
    int cancelTasksByType(BackgroundTask.TaskType type);
    
    /**
     * Updates the progress of a running task.
     * 
     * @param taskId The task ID
     * @param progressPercentage The progress percentage (0-100)
     * @param currentStep Description of the current step
     */
    void updateTaskProgress(String taskId, double progressPercentage, String currentStep);
    
    /**
     * Gets the current system load (number of running tasks vs capacity).
     * 
     * @return System load as a percentage (0-100)
     */
    double getSystemLoad();
    
    /**
     * Gets the number of available worker threads.
     * 
     * @return Number of available worker threads
     */
    int getAvailableWorkers();
    
    /**
     * Gets the maximum number of concurrent tasks.
     * 
     * @return Maximum concurrent tasks
     */
    int getMaxConcurrentTasks();
    
    /**
     * Sets the maximum number of concurrent tasks.
     * 
     * @param maxConcurrentTasks Maximum concurrent tasks
     */
    void setMaxConcurrentTasks(int maxConcurrentTasks);
    
    /**
     * Gets task execution statistics.
     * 
     * @return Map containing execution statistics
     */
    java.util.Map<String, Object> getExecutionStatistics();
    
    /**
     * Cleans up completed and failed tasks older than the specified age.
     * 
     * @param maxAgeHours Maximum age in hours
     * @return Number of tasks cleaned up
     */
    int cleanupOldTasks(int maxAgeHours);
    
    /**
     * Shuts down the background task service gracefully.
     * 
     * @param timeoutMs Timeout in milliseconds to wait for running tasks
     * @return True if shutdown completed within timeout
     */
    boolean shutdown(long timeoutMs);
    
    /**
     * Interface for tasks that can report progress.
     */
    @FunctionalInterface
    interface ProgressTrackingTask<T> {
        T execute(ProgressCallback callback) throws Exception;
    }
    
    /**
     * Callback interface for reporting task progress.
     */
    interface ProgressCallback {
        void updateProgress(double percentage, String currentStep);
        boolean isCancelled();
    }
}