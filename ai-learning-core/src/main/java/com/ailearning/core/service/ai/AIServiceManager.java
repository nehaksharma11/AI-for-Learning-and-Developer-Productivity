package com.ailearning.core.service.ai;

import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ai.AIBreakdown;
import com.ailearning.core.model.ai.AIExample;
import com.ailearning.core.model.ai.AIExplanation;
import com.ailearning.core.service.ai.impl.FallbackAIService;
import com.ailearning.core.service.ai.impl.HuggingFaceService;
import com.ailearning.core.service.ai.impl.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * Manages multiple AI services and provides fallback mechanisms for robust AI integration.
 * This service coordinates between OpenAI, Hugging Face, and fallback services to ensure
 * the system remains functional even when some services are unavailable.
 */
public class AIServiceManager implements AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIServiceManager.class);
    
    private final List<AIService> aiServices;
    private final long timeoutSeconds;
    private final boolean enableFallback;
    
    public AIServiceManager(List<AIService> aiServices, long timeoutSeconds, boolean enableFallback) {
        this.aiServices = new ArrayList<>(aiServices);
        this.timeoutSeconds = Math.max(5, timeoutSeconds);
        this.enableFallback = enableFallback;
        
        // Sort services by priority (highest first)
        this.aiServices.sort(Comparator.comparingInt(AIService::getPriority).reversed());
        
        // Ensure fallback service is available if enabled
        if (enableFallback && this.aiServices.stream().noneMatch(s -> s instanceof FallbackAIService)) {
            this.aiServices.add(new FallbackAIService(true));
            this.aiServices.sort(Comparator.comparingInt(AIService::getPriority).reversed());
        }
        
        logger.info("AI Service Manager initialized with {} services, timeout={}s, fallback={}",
                this.aiServices.size(), timeoutSeconds, enableFallback);
    }
    
    /**
     * Creates a default AI service manager with standard configuration.
     */
    public static AIServiceManager createDefault() {
        List<AIService> services = new ArrayList<>();
        
        // Add OpenAI service (would be configured via properties in real implementation)
        services.add(new OpenAIService(null, "gpt-3.5-turbo", false, 1000, 0.7));
        
        // Add Hugging Face service
        services.add(new HuggingFaceService("microsoft/codebert-base", false, 500, "text-generation"));
        
        // Fallback service will be added automatically
        return new AIServiceManager(services, 30, true);
    }

    @Override
    public CompletableFuture<AIExplanation> explainCode(String codeSnippet, CodeContext codeContext, ProjectContext projectContext) {
        return executeWithFallback(
            service -> service.explainCode(codeSnippet, codeContext, projectContext),
            "explainCode"
        );
    }

    @Override
    public CompletableFuture<List<AIExample>> generateExamples(String codePattern, ProjectContext projectContext) {
        return executeWithFallback(
            service -> service.generateExamples(codePattern, projectContext),
            "generateExamples"
        );
    }

    @Override
    public CompletableFuture<AIBreakdown> createBreakdown(String complexCode, CodeContext codeContext) {
        return executeWithFallback(
            service -> service.createBreakdown(complexCode, codeContext),
            "createBreakdown"
        );
    }

    @Override
    public boolean isAvailable() {
        return aiServices.stream().anyMatch(AIService::isAvailable);
    }

    @Override
    public String getServiceName() {
        return "AIServiceManager";
    }

    @Override
    public int getPriority() {
        return 1000; // Highest priority as it manages other services
    }
    
    /**
     * Gets the list of available AI services.
     */
    public List<AIService> getAvailableServices() {
        return aiServices.stream()
                .filter(AIService::isAvailable)
                .toList();
    }
    
    /**
     * Gets the currently preferred AI service (highest priority available service).
     */
    public AIService getPreferredService() {
        return aiServices.stream()
                .filter(AIService::isAvailable)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Adds a new AI service to the manager.
     */
    public void addService(AIService service) {
        if (service != null && !aiServices.contains(service)) {
            aiServices.add(service);
            aiServices.sort(Comparator.comparingInt(AIService::getPriority).reversed());
            logger.info("Added AI service: {} with priority {}", service.getServiceName(), service.getPriority());
        }
    }
    
    /**
     * Removes an AI service from the manager.
     */
    public void removeService(AIService service) {
        if (aiServices.remove(service)) {
            logger.info("Removed AI service: {}", service.getServiceName());
        }
    }
    
    // Generic method to execute AI operations with fallback
    private <T> CompletableFuture<T> executeWithFallback(
            java.util.function.Function<AIService, CompletableFuture<T>> operation,
            String operationName) {
        
        return executeWithFallbackRecursive(operation, operationName, 0);
    }
    
    private <T> CompletableFuture<T> executeWithFallbackRecursive(
            java.util.function.Function<AIService, CompletableFuture<T>> operation,
            String operationName,
            int serviceIndex) {
        
        if (serviceIndex >= aiServices.size()) {
            return CompletableFuture.failedFuture(
                new RuntimeException("All AI services failed for operation: " + operationName)
            );
        }
        
        AIService currentService = aiServices.get(serviceIndex);
        
        if (!currentService.isAvailable()) {
            logger.debug("Service {} is not available, trying next service", currentService.getServiceName());
            return executeWithFallbackRecursive(operation, operationName, serviceIndex + 1);
        }
        
        logger.debug("Attempting {} with service: {}", operationName, currentService.getServiceName());
        
        return operation.apply(currentService)
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        logger.warn("Service {} failed for {}: {}", 
                                currentService.getServiceName(), operationName, throwable.getMessage());
                        
                        // Try next service
                        return executeWithFallbackRecursive(operation, operationName, serviceIndex + 1);
                    } else {
                        logger.debug("Service {} succeeded for {}", currentService.getServiceName(), operationName);
                        return CompletableFuture.completedFuture(result);
                    }
                })
                .thenCompose(future -> future);
    }
    
    /**
     * Checks the health of all AI services.
     */
    public CompletableFuture<AIServiceHealthReport> checkHealth() {
        return CompletableFuture.supplyAsync(() -> {
            AIServiceHealthReport.Builder reportBuilder = AIServiceHealthReport.builder();
            
            for (AIService service : aiServices) {
                AIServiceHealth health = AIServiceHealth.builder()
                        .serviceName(service.getServiceName())
                        .isAvailable(service.isAvailable())
                        .priority(service.getPriority())
                        .lastChecked(java.time.LocalDateTime.now())
                        .build();
                
                reportBuilder.addServiceHealth(health);
            }
            
            return reportBuilder.build();
        });
    }
}

/**
 * Represents the health status of an AI service.
 */
class AIServiceHealth {
    private final String serviceName;
    private final boolean isAvailable;
    private final int priority;
    private final java.time.LocalDateTime lastChecked;
    private final String errorMessage;
    
    private AIServiceHealth(String serviceName, boolean isAvailable, int priority,
                           java.time.LocalDateTime lastChecked, String errorMessage) {
        this.serviceName = serviceName;
        this.isAvailable = isAvailable;
        this.priority = priority;
        this.lastChecked = lastChecked;
        this.errorMessage = errorMessage;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getServiceName() { return serviceName; }
    public boolean isAvailable() { return isAvailable; }
    public int getPriority() { return priority; }
    public java.time.LocalDateTime getLastChecked() { return lastChecked; }
    public String getErrorMessage() { return errorMessage; }
    
    public static class Builder {
        private String serviceName;
        private boolean isAvailable;
        private int priority;
        private java.time.LocalDateTime lastChecked;
        private String errorMessage;
        
        public Builder serviceName(String serviceName) { this.serviceName = serviceName; return this; }
        public Builder isAvailable(boolean isAvailable) { this.isAvailable = isAvailable; return this; }
        public Builder priority(int priority) { this.priority = priority; return this; }
        public Builder lastChecked(java.time.LocalDateTime lastChecked) { this.lastChecked = lastChecked; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        
        public AIServiceHealth build() {
            return new AIServiceHealth(serviceName, isAvailable, priority, lastChecked, errorMessage);
        }
    }
}

/**
 * Represents a health report for all AI services.
 */
class AIServiceHealthReport {
    private final List<AIServiceHealth> serviceHealths;
    private final java.time.LocalDateTime reportTime;
    private final int totalServices;
    private final int availableServices;
    
    private AIServiceHealthReport(List<AIServiceHealth> serviceHealths, java.time.LocalDateTime reportTime) {
        this.serviceHealths = new ArrayList<>(serviceHealths);
        this.reportTime = reportTime;
        this.totalServices = serviceHealths.size();
        this.availableServices = (int) serviceHealths.stream().filter(AIServiceHealth::isAvailable).count();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public List<AIServiceHealth> getServiceHealths() { return new ArrayList<>(serviceHealths); }
    public java.time.LocalDateTime getReportTime() { return reportTime; }
    public int getTotalServices() { return totalServices; }
    public int getAvailableServices() { return availableServices; }
    public boolean hasAvailableServices() { return availableServices > 0; }
    
    public static class Builder {
        private final List<AIServiceHealth> serviceHealths = new ArrayList<>();
        
        public Builder addServiceHealth(AIServiceHealth health) {
            this.serviceHealths.add(health);
            return this;
        }
        
        public AIServiceHealthReport build() {
            return new AIServiceHealthReport(serviceHealths, java.time.LocalDateTime.now());
        }
    }
}