package com.ailearning.core.lifecycle;

import com.ailearning.core.service.BackgroundTaskService;
import com.ailearning.core.service.CacheService;
import com.ailearning.core.service.PerformanceMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Manages the application lifecycle including startup and shutdown procedures.
 */
@Component
public class ApplicationLifecycleManager implements SmartLifecycle {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationLifecycleManager.class);
    
    private final PerformanceMonitoringService performanceMonitoring;
    private final BackgroundTaskService backgroundTaskService;
    private final CacheService cacheService;
    
    private volatile boolean running = false;
    
    public ApplicationLifecycleManager(
            PerformanceMonitoringService performanceMonitoring,
            BackgroundTaskService backgroundTaskService,
            CacheService cacheService) {
        this.performanceMonitoring = performanceMonitoring;
        this.backgroundTaskService = backgroundTaskService;
        this.cacheService = cacheService;
    }
    
    @Override
    public void start() {
        if (!running) {
            logger.info("Starting Application Lifecycle Manager...");
            
            try {
                // Initialize services
                initializeServices();
                
                // Start background monitoring
                startBackgroundMonitoring();
                
                running = true;
                logger.info("Application Lifecycle Manager started successfully");
                
            } catch (Exception e) {
                logger.error("Failed to start Application Lifecycle Manager", e);
                throw new RuntimeException("Lifecycle manager startup failed", e);
            }
        }
    }
    
    @Override
    public void stop() {
        if (running) {
            logger.info("Stopping Application Lifecycle Manager...");
            
            try {
                // Stop background monitoring
                stopBackgroundMonitoring();
                
                // Cleanup services
                cleanupServices();
                
                running = false;
                logger.info("Application Lifecycle Manager stopped successfully");
                
            } catch (Exception e) {
                logger.error("Error during Application Lifecycle Manager shutdown", e);
            }
        }
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public int getPhase() {
        // Start early in the lifecycle
        return Integer.MIN_VALUE + 1000;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true;
    }
    
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
    
    private void initializeServices() {
        logger.debug("Initializing services...");
        
        // Services are already initialized by Spring
        // This method can be used for additional initialization if needed
        
        logger.debug("Services initialized");
    }
    
    private void startBackgroundMonitoring() {
        logger.debug("Starting background monitoring...");
        
        // Background monitoring is handled by the services themselves
        // This method can be used to start additional monitoring tasks
        
        logger.debug("Background monitoring started");
    }
    
    private void stopBackgroundMonitoring() {
        logger.debug("Stopping background monitoring...");
        
        // Stop any background monitoring tasks
        
        logger.debug("Background monitoring stopped");
    }
    
    private void cleanupServices() {
        logger.debug("Cleaning up services...");
        
        try {
            // Cancel all pending background tasks
            logger.debug("Cancelling pending background tasks...");
            backgroundTaskService.cancelAllTasks();
            
            // Clear cache
            logger.debug("Clearing cache...");
            cacheService.clear();
            
            // Cleanup old performance data
            logger.debug("Cleaning up old performance data...");
            int cleanedUp = performanceMonitoring.cleanupOldData(30); // Keep last 30 days
            logger.debug("Cleaned up {} old performance records", cleanedUp);
            
        } catch (Exception e) {
            logger.warn("Error during service cleanup", e);
        }
        
        logger.debug("Services cleaned up");
    }
}
