package com.ailearning.core;

import com.ailearning.core.config.AILearningProperties;
import com.ailearning.core.orchestration.AILearningOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;

/**
 * Main Spring Boot application for AI Learning Companion.
 * Initializes all services and manages application lifecycle.
 */
@SpringBootApplication
@EnableConfigurationProperties(AILearningProperties.class)
public class AILearningCompanionApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(AILearningCompanionApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting AI Learning Companion Application...");
        
        try {
            SpringApplication app = new SpringApplication(AILearningCompanionApplication.class);
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown hook triggered - initiating graceful shutdown");
            }));
            
            app.run(args);
            
        } catch (Exception e) {
            logger.error("Failed to start AI Learning Companion Application", e);
            System.exit(1);
        }
    }
    
    /**
     * Application ready listener that performs post-startup initialization.
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyListener(
            AILearningOrchestrator orchestrator,
            AILearningProperties properties) {
        return event -> {
            logger.info("=".repeat(80));
            logger.info("AI Learning Companion Application Started Successfully");
            logger.info("=".repeat(80));
            logger.info("Performance Settings:");
            logger.info("  - Context Update Threshold: {}ms", properties.getPerformance().getContextUpdateThresholdMs());
            logger.info("  - Typical Operation Threshold: {}ms", properties.getPerformance().getTypicalOperationThresholdMs());
            logger.info("  - Memory Threshold: {}MB", properties.getPerformance().getMemoryThresholdMb());
            logger.info("  - CPU Threshold: {}%", properties.getPerformance().getCpuThresholdPercent());
            logger.info("");
            logger.info("Security Settings:");
            logger.info("  - Encryption Enabled: {}", properties.getSecurity().isEncryptionEnabled());
            logger.info("  - Encryption Algorithm: {}", properties.getSecurity().getEncryptionAlgorithm());
            logger.info("  - TLS Enabled: {}", properties.getSecurity().isTlsEnabled());
            logger.info("  - TLS Version: {}", properties.getSecurity().getTlsVersion());
            logger.info("");
            logger.info("AI Services:");
            logger.info("  - OpenAI Enabled: {}", properties.getAi().isOpenaiEnabled());
            logger.info("  - HuggingFace Enabled: {}", properties.getAi().isHuggingfaceEnabled());
            logger.info("  - Fallback Enabled: {}", properties.getAi().isFallbackEnabled());
            logger.info("");
            logger.info("Cache Settings:");
            logger.info("  - Max Size: {}MB", properties.getCache().getMaxSizeMb());
            logger.info("  - Default TTL: {} minutes", properties.getCache().getDefaultTtlMinutes());
            logger.info("  - Eviction Policy: {}", properties.getCache().getEvictionPolicy());
            logger.info("");
            logger.info("Learning Settings:");
            logger.info("  - Adaptive Difficulty: {}", properties.getLearning().isAdaptiveDifficultyEnabled());
            logger.info("  - Session Duration: {}-{} minutes", 
                    properties.getLearning().getMinSessionDurationMinutes(),
                    properties.getLearning().getMaxSessionDurationMinutes());
            logger.info("  - Retention Check: {} days", properties.getLearning().getRetentionCheckDays());
            logger.info("=".repeat(80));
            
            // Get system health status
            var healthStatus = orchestrator.getSystemHealth();
            logger.info("System Health: {}", healthStatus.getStatus());
            logger.info("Memory Usage: {:.2f}MB", healthStatus.getMemoryUsageMb());
            logger.info("CPU Usage: {:.2f}%", healthStatus.getCpuUsagePercent());
            logger.info("=".repeat(80));
            
            logger.info("Application is ready to accept requests");
        };
    }
    
    /**
     * Graceful shutdown handler.
     */
    @PreDestroy
    public void onShutdown() {
        logger.info("=".repeat(80));
        logger.info("AI Learning Companion Application Shutting Down");
        logger.info("=".repeat(80));
        logger.info("Performing cleanup operations...");
        
        try {
            // Give time for in-flight requests to complete
            Thread.sleep(2000);
            logger.info("Cleanup completed successfully");
        } catch (InterruptedException e) {
            logger.warn("Shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
        
        logger.info("Application shutdown complete");
        logger.info("=".repeat(80));
    }
}
