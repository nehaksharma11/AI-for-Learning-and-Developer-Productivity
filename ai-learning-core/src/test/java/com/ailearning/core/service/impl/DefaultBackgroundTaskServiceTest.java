package com.ailearning.core.service.impl;

import com.ailearning.core.model.BackgroundTask;
import com.ailearning.core.service.BackgroundTaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultBackgroundTaskService.
 */
class DefaultBackgroundTaskServiceTest {
    
    private BackgroundTaskService service;
    
    @BeforeEach
    void setUp() {
        service = new DefaultBackgroundTaskService(2, 4);
    }
    
    @AfterEach
    void tearDown() {
        service.shutdown(5000);
    }
    
    @Test
    void testSubmitTask() throws Exception {
        BackgroundTask<String> task = service.submitTask(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS,
                "Test analysis",
                BackgroundTask.Priority.NORMAL,
                1000L,
                () -> "Result"
        );
        
        assertNotNull(task);
        assertNotNull(task.getTaskId());
        assertEquals(BackgroundTask.TaskType.CODEBASE_ANALYSIS, task.getType());
        assertEquals("Test analysis", task.getDescription());
        assertEquals(BackgroundTask.Priority.NORMAL, task.getPriority());
        
        // Wait for completion
        String result = task.getFuture().get(5, TimeUnit.SECONDS);
        assertEquals("Result", result);
        
        // Verify task is completed
        Optional<BackgroundTask<?>> completedTask = service.getTask(task.getTaskId());
        assertTrue(completedTask.isPresent());
        assertTrue(completedTask.get().isCompleted());
    }
    
    @Test
    void testSubmitTaskWithProgress() throws Exception {
        BackgroundTask<Integer> task = service.submitTaskWithProgress(
                BackgroundTask.TaskType.DOCUMENTATION_GENERATION,
                "Generate docs",
                BackgroundTask.Priority.HIGH,
                2000L,
                (callback) -> {
                    callback.updateProgress(25.0, "Step 1");
                    Thread.sleep(100);
                    callback.updateProgress(50.0, "Step 2");
                    Thread.sleep(100);
                    callback.updateProgress(75.0, "Step 3");
                    Thread.sleep(100);
                    callback.updateProgress(100.0, "Complete");
                    return 42;
                }
        );
        
        assertNotNull(task);
        
        // Wait for completion
        Integer result = task.getFuture().get(5, TimeUnit.SECONDS);
        assertEquals(42, result);
        
        // Verify task completed with progress
        Optional<BackgroundTask<?>> completedTask = service.getTask(task.getTaskId());
        assertTrue(completedTask.isPresent());
        assertTrue(completedTask.get().isCompleted());
        assertEquals(100.0, completedTask.get().getProgressPercentage());
    }
    
    @Test
    void testGetTask() {
        BackgroundTask<String> task = service.submitTask(
                BackgroundTask.TaskType.PATTERN_DETECTION,
                "Detect patterns",
                BackgroundTask.Priority.NORMAL,
                1000L,
                () -> "Patterns found"
        );
        
        Optional<BackgroundTask<?>> retrieved = service.getTask(task.getTaskId());
        assertTrue(retrieved.isPresent());
        assertEquals(task.getTaskId(), retrieved.get().getTaskId());
    }
    
    @Test
    void testGetAllTasks() {
        service.submitTask(BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Task 1", 
                BackgroundTask.Priority.NORMAL, 1000L, () -> "Result 1");
        service.submitTask(BackgroundTask.TaskType.SECURITY_SCAN, "Task 2", 
                BackgroundTask.Priority.HIGH, 1000L, () -> "Result 2");
        
        List<BackgroundTask<?>> allTasks = service.getAllTasks();
        assertEquals(2, allTasks.size());
    }
    
    @Test
    void testGetTasksByStatus() throws Exception {
        BackgroundTask<String> task1 = service.submitTask(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Task 1", 
                BackgroundTask.Priority.NORMAL, 1000L, () -> {
                    Thread.sleep(2000);
                    return "Result 1";
                });
        
        BackgroundTask<String> task2 = service.submitTask(
                BackgroundTask.TaskType.SECURITY_SCAN, "Task 2", 
                BackgroundTask.Priority.NORMAL, 1000L, () -> "Result 2");
        
        // Give tasks time to start
        Thread.sleep(500);
        
        List<BackgroundTask<?>> runningTasks = service.getTasksByStatus(BackgroundTask.TaskStatus.RUNNING);
        assertFalse(runningTasks.isEmpty());
    }
    
    @Test
    void testGetTasksByType() {
        service.submitTask(BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Task 1", 
                BackgroundTask.Priority.NORMAL, 1000L, () -> "Result 1");
        service.submitTask(BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Task 2", 
                BackgroundTask.Priority.NORMAL, 1000L, () -> "Result 2");
        service.submitTask(BackgroundTask.TaskType.SECURITY_SCAN, "Task 3", 
                BackgroundTask.Priority.NORMAL, 1000L, () -> "Result 3");
        
        List<BackgroundTask<?>> analysisTasks = service.getTasksByType(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS);
        assertEquals(2, analysisTasks.size());
        
        List<BackgroundTask<?>> securityTasks = service.getTasksByType(
                BackgroundTask.TaskType.SECURITY_SCAN);
        assertEquals(1, securityTasks.size());
    }
    
    @Test
    void testGetRunningTasks() throws Exception {
        BackgroundTask<String> task = service.submitTask(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Long task", 
                BackgroundTask.Priority.NORMAL, 2000L, () -> {
                    Thread.sleep(2000);
                    return "Result";
                });
        
        // Give task time to start
        Thread.sleep(500);
        
        List<BackgroundTask<?>> runningTasks = service.getRunningTasks();
        assertFalse(runningTasks.isEmpty());
        assertTrue(runningTasks.stream().anyMatch(t -> t.getTaskId().equals(task.getTaskId())));
    }
    
    @Test
    void testGetPendingTasks() {
        // Submit more tasks than the pool can handle immediately
        for (int i = 0; i < 10; i++) {
            service.submitTask(BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Task " + i, 
                    BackgroundTask.Priority.NORMAL, 1000L, () -> {
                        Thread.sleep(1000);
                        return "Result";
                    });
        }
        
        List<BackgroundTask<?>> pendingTasks = service.getPendingTasks();
        // Some tasks should be pending since we exceeded pool capacity
        assertFalse(pendingTasks.isEmpty());
    }
    
    @Test
    void testCancelTask() throws Exception {
        BackgroundTask<String> task = service.submitTask(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Cancellable task", 
                BackgroundTask.Priority.NORMAL, 5000L, () -> {
                    Thread.sleep(5000);
                    return "Result";
                });
        
        // Give task time to start
        Thread.sleep(500);
        
        boolean cancelled = service.cancelTask(task.getTaskId());
        assertTrue(cancelled);
        
        Optional<BackgroundTask<?>> cancelledTask = service.getTask(task.getTaskId());
        assertTrue(cancelledTask.isPresent());
        assertTrue(cancelledTask.get().isCancelled());
    }
    
    @Test
    void testCancelTasksByType() {
        // Submit multiple tasks of same type
        for (int i = 0; i < 5; i++) {
            service.submitTask(BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Task " + i, 
                    BackgroundTask.Priority.NORMAL, 5000L, () -> {
                        Thread.sleep(5000);
                        return "Result";
                    });
        }
        
        int cancelled = service.cancelTasksByType(BackgroundTask.TaskType.CODEBASE_ANALYSIS);
        assertTrue(cancelled > 0);
    }
    
    @Test
    void testUpdateTaskProgress() throws Exception {
        BackgroundTask<String> task = service.submitTask(
                BackgroundTask.TaskType.DOCUMENTATION_GENERATION, "Task with progress", 
                BackgroundTask.Priority.NORMAL, 2000L, () -> {
                    Thread.sleep(2000);
                    return "Result";
                });
        
        // Give task time to start
        Thread.sleep(500);
        
        service.updateTaskProgress(task.getTaskId(), 50.0, "Halfway done");
        
        Optional<BackgroundTask<?>> updatedTask = service.getTask(task.getTaskId());
        assertTrue(updatedTask.isPresent());
        assertEquals(50.0, updatedTask.get().getProgressPercentage());
        assertEquals("Halfway done", updatedTask.get().getCurrentStep());
    }
    
    @Test
    void testGetSystemLoad() {
        double load = service.getSystemLoad();
        assertTrue(load >= 0.0 && load <= 100.0);
    }
    
    @Test
    void testGetAvailableWorkers() {
        int available = service.getAvailableWorkers();
        assertTrue(available >= 0);
        assertTrue(available <= service.getMaxConcurrentTasks());
    }
    
    @Test
    void testGetMaxConcurrentTasks() {
        int maxTasks = service.getMaxConcurrentTasks();
        assertEquals(4, maxTasks); // We initialized with max pool size of 4
    }
    
    @Test
    void testSetMaxConcurrentTasks() {
        service.setMaxConcurrentTasks(6);
        assertEquals(6, service.getMaxConcurrentTasks());
    }
    
    @Test
    void testGetExecutionStatistics() {
        service.submitTask(BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Task 1", 
                BackgroundTask.Priority.NORMAL, 1000L, () -> "Result");
        
        Map<String, Object> stats = service.getExecutionStatistics();
        
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalTasks"));
        assertTrue(stats.containsKey("runningTasks"));
        assertTrue(stats.containsKey("pendingTasks"));
        assertTrue(stats.containsKey("completedTasks"));
        assertTrue(stats.containsKey("systemLoad"));
        
        assertEquals(1, stats.get("totalTasks"));
    }
    
    @Test
    void testCleanupOldTasks() throws Exception {
        BackgroundTask<String> task = service.submitTask(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Quick task", 
                BackgroundTask.Priority.NORMAL, 100L, () -> "Result");
        
        // Wait for completion
        task.getFuture().get(5, TimeUnit.SECONDS);
        
        // Cleanup tasks older than 1 hour (should not remove recent task)
        int cleaned = service.cleanupOldTasks(1);
        assertEquals(0, cleaned);
        
        // Task should still be there
        Optional<BackgroundTask<?>> retrieved = service.getTask(task.getTaskId());
        assertTrue(retrieved.isPresent());
    }
    
    @Test
    void testTaskFailure() throws Exception {
        BackgroundTask<String> task = service.submitTask(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Failing task", 
                BackgroundTask.Priority.NORMAL, 1000L, () -> {
                    throw new RuntimeException("Task failed");
                });
        
        // Wait for task to fail
        Thread.sleep(1000);
        
        Optional<BackgroundTask<?>> failedTask = service.getTask(task.getTaskId());
        assertTrue(failedTask.isPresent());
        assertTrue(failedTask.get().isFailed());
        assertNotNull(failedTask.get().getError());
    }
    
    @Test
    void testPriorityOrdering() throws Exception {
        // Submit tasks with different priorities
        BackgroundTask<String> lowPriority = service.submitTask(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Low priority", 
                BackgroundTask.Priority.LOW, 1000L, () -> {
                    Thread.sleep(2000);
                    return "Low";
                });
        
        BackgroundTask<String> highPriority = service.submitTask(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS, "High priority", 
                BackgroundTask.Priority.HIGH, 1000L, () -> {
                    Thread.sleep(2000);
                    return "High";
                });
        
        BackgroundTask<String> criticalPriority = service.submitTask(
                BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Critical priority", 
                BackgroundTask.Priority.CRITICAL, 1000L, () -> {
                    Thread.sleep(2000);
                    return "Critical";
                });
        
        // Give tasks time to queue
        Thread.sleep(500);
        
        List<BackgroundTask<?>> pendingTasks = service.getPendingTasks();
        
        // Verify tasks are ordered by priority (highest first)
        if (!pendingTasks.isEmpty()) {
            BackgroundTask.Priority firstPriority = pendingTasks.get(0).getPriority();
            assertTrue(firstPriority == BackgroundTask.Priority.CRITICAL || 
                      firstPriority == BackgroundTask.Priority.HIGH);
        }
    }
    
    @Test
    void testShutdown() {
        service.submitTask(BackgroundTask.TaskType.CODEBASE_ANALYSIS, "Task", 
                BackgroundTask.Priority.NORMAL, 100L, () -> "Result");
        
        boolean shutdown = service.shutdown(5000);
        assertTrue(shutdown);
    }
    
    @Test
    void testProgressCallback() throws Exception {
        BackgroundTask<String> task = service.submitTaskWithProgress(
                BackgroundTask.TaskType.LEARNING_PATH_CALCULATION,
                "Calculate learning path",
                BackgroundTask.Priority.NORMAL,
                1000L,
                (callback) -> {
                    for (int i = 0; i <= 100; i += 25) {
                        if (callback.isCancelled()) {
                            return "Cancelled";
                        }
                        callback.updateProgress(i, "Step " + (i / 25));
                        Thread.sleep(100);
                    }
                    return "Completed";
                }
        );
        
        String result = task.getFuture().get(5, TimeUnit.SECONDS);
        assertEquals("Completed", result);
        
        Optional<BackgroundTask<?>> completedTask = service.getTask(task.getTaskId());
        assertTrue(completedTask.isPresent());
        assertEquals(100.0, completedTask.get().getProgressPercentage());
    }
}
