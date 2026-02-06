package com.ailearning.core.health;

import com.ailearning.core.orchestration.AILearningOrchestrator;
import com.ailearning.core.service.PerformanceMonitoringService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for AI Learning Companion.
 * Provides detailed health information about the system.
 */
@Component
public class AILearningHealthIndicator implements HealthIndicator {
    
    private final AILearningOrchestrator orchestrator;
    private final PerformanceMonitoringService performanceMonitoring;
    
    public AILearningHealthIndicator(
            AILearningOrchestrator orchestrator,
            PerformanceMonitoringService performanceMonitoring) {
        this.orchestrator = orchestrator;
        this.performanceMonitoring = performanceMonitoring;
    }
    
    @Override
    public Health health() {
        try {
            var systemHealth = orchestrator.getSystemHealth();
            
            Health.Builder builder;
            
            // Determine overall health status
            switch (systemHealth.getStatus()) {
                case "HEALTHY":
                    builder = Health.up();
                    break;
                case "DEGRADED":
                    builder = Health.status("DEGRADED");
                    break;
                case "CRITICAL":
                    builder = Health.down();
                    break;
                default:
                    builder = Health.unknown();
            }
            
            // Add detailed health information
            builder.withDetail("status", systemHealth.getStatus())
                   .withDetail("memoryUsageMb", String.format("%.2f", systemHealth.getMemoryUsageMb()))
                   .withDetail("cpuUsagePercent", String.format("%.2f", systemHealth.getCpuUsagePercent()))
                   .withDetail("hasIssues", systemHealth.isHasIssues())
                   .withDetail("activeAlerts", performanceMonitoring.getActiveAlerts().size());
            
            // Add performance issues if any
            if (systemHealth.isHasIssues()) {
                var activeAlerts = performanceMonitoring.getActiveAlerts();
                if (!activeAlerts.isEmpty()) {
                    builder.withDetail("alerts", activeAlerts.stream()
                            .map(alert -> alert.getType() + ": " + alert.getMessage())
                            .toArray());
                }
            }
            
            return builder.build();
            
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
