package com.ailearning.core.service.impl;

import com.ailearning.core.model.PerformanceAlert;
import com.ailearning.core.model.PerformanceMetrics;
import com.ailearning.core.service.PerformanceMonitoringService;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Default implementation of PerformanceMonitoringService that tracks system performance
 * and triggers alerts when thresholds are exceeded.
 */
public class DefaultPerformanceMonitoringService implements PerformanceMonitoringService {
    
    private final List<PerformanceMetrics> metrics = new CopyOnWriteArrayList<>();
    private final List<PerformanceAlert> alerts = new CopyOnWriteArrayList<>();
    private final Map<String, ThresholdConfig> thresholds = new ConcurrentHashMap<>();
    
    // JVM monitoring beans
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    
    // Default thresholds
    private static final long DEFAULT_RESPONSE_TIME_THRESHOLD_MS = 500; // 500ms as per requirement 1.2
    private static final double DEFAULT_MEMORY_THRESHOLD_MB = 500; // 500MB as per requirement 10.3
    private static final double DEFAULT_CPU_THRESHOLD_PERCENT = 80.0;
    private static final double DEFAULT_ERROR_RATE_THRESHOLD = 5.0; // 5%
    
    private static class ThresholdConfig {
        final long responseTimeThresholdMs;
        final double memoryThresholdMB;
        final double cpuThresholdPercent;
        final double errorRateThreshold;
        
        ThresholdConfig(long responseTimeThresholdMs, double memoryThresholdMB, 
                       double cpuThresholdPercent, double errorRateThreshold) {
            this.responseTimeThresholdMs = responseTimeThresholdMs;
            this.memoryThresholdMB = memoryThresholdMB;
            this.cpuThresholdPercent = cpuThresholdPercent;
            this.errorRateThreshold = errorRateThreshold;
        }
    }
    
    @Override
    public void recordMetric(PerformanceMetrics metric) {
        Objects.requireNonNull(metric, "Metric cannot be null");
        
        metrics.add(metric);
        
        // Check if metric exceeds thresholds and trigger alerts
        checkThresholdsAndTriggerAlerts(metric);
        
        // Limit metrics storage to prevent memory issues
        if (metrics.size() > 10000) {
            // Remove oldest 1000 metrics
            for (int i = 0; i < 1000; i++) {
                if (!metrics.isEmpty()) {
                    metrics.remove(0);
                }
            }
        }
    }
    
    @Override
    public void recordResponseTime(String operation, long responseTimeMs, Map<String, Object> metadata) {
        Objects.requireNonNull(operation, "Operation cannot be null");
        
        PerformanceMetrics.Severity severity = determineSeverity(
                PerformanceMetrics.MetricType.RESPONSE_TIME, responseTimeMs, operation);
        
        PerformanceMetrics metric = PerformanceMetrics.builder()
                .metricId(UUID.randomUUID().toString())
                .type(PerformanceMetrics.MetricType.RESPONSE_TIME)
                .operation(operation)
                .value(responseTimeMs)
                .unit("ms")
                .timestamp(LocalDateTime.now())
                .metadata(metadata != null ? metadata : Map.of())
                .severity(severity)
                .threshold(getThreshold(operation).responseTimeThresholdMs)
                .alertTriggered(responseTimeMs > getThreshold(operation).responseTimeThresholdMs)
                .build();
        
        recordMetric(metric);
    }
    
    @Override
    public void recordMemoryUsage(String operation, double memoryUsageMB) {
        Objects.requireNonNull(operation, "Operation cannot be null");
        
        PerformanceMetrics.Severity severity = determineSeverity(
                PerformanceMetrics.MetricType.MEMORY_USAGE, memoryUsageMB, operation);
        
        PerformanceMetrics metric = PerformanceMetrics.builder()
                .metricId(UUID.randomUUID().toString())
                .type(PerformanceMetrics.MetricType.MEMORY_USAGE)
                .operation(operation)
                .value(memoryUsageMB)
                .unit("MB")
                .timestamp(LocalDateTime.now())
                .severity(severity)
                .threshold(getThreshold(operation).memoryThresholdMB)
                .alertTriggered(memoryUsageMB > getThreshold(operation).memoryThresholdMB)
                .build();
        
        recordMetric(metric);
    }
    
    @Override
    public void recordCpuUsage(String operation, double cpuUsagePercent) {
        Objects.requireNonNull(operation, "Operation cannot be null");
        
        PerformanceMetrics.Severity severity = determineSeverity(
                PerformanceMetrics.MetricType.CPU_USAGE, cpuUsagePercent, operation);
        
        PerformanceMetrics metric = PerformanceMetrics.builder()
                .metricId(UUID.randomUUID().toString())
                .type(PerformanceMetrics.MetricType.CPU_USAGE)
                .operation(operation)
                .value(cpuUsagePercent)
                .unit("%")
                .timestamp(LocalDateTime.now())
                .severity(severity)
                .threshold(getThreshold(operation).cpuThresholdPercent)
                .alertTriggered(cpuUsagePercent > getThreshold(operation).cpuThresholdPercent)
                .build();
        
        recordMetric(metric);
    }
    
    @Override
    public void recordThroughput(String operation, double throughputPerSecond) {
        Objects.requireNonNull(operation, "Operation cannot be null");
        
        PerformanceMetrics metric = PerformanceMetrics.builder()
                .metricId(UUID.randomUUID().toString())
                .type(PerformanceMetrics.MetricType.THROUGHPUT)
                .operation(operation)
                .value(throughputPerSecond)
                .unit("ops/sec")
                .timestamp(LocalDateTime.now())
                .severity(PerformanceMetrics.Severity.INFO)
                .build();
        
        recordMetric(metric);
    }
    
    @Override
    public void recordErrorRate(String operation, double errorRate) {
        Objects.requireNonNull(operation, "Operation cannot be null");
        
        PerformanceMetrics.Severity severity = determineSeverity(
                PerformanceMetrics.MetricType.ERROR_RATE, errorRate, operation);
        
        PerformanceMetrics metric = PerformanceMetrics.builder()
                .metricId(UUID.randomUUID().toString())
                .type(PerformanceMetrics.MetricType.ERROR_RATE)
                .operation(operation)
                .value(errorRate)
                .unit("%")
                .timestamp(LocalDateTime.now())
                .severity(severity)
                .threshold(getThreshold(operation).errorRateThreshold)
                .alertTriggered(errorRate > getThreshold(operation).errorRateThreshold)
                .build();
        
        recordMetric(metric);
    }
    
    @Override
    public List<PerformanceMetrics> getMetrics(String operation, LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(operation, "Operation cannot be null");
        Objects.requireNonNull(startTime, "Start time cannot be null");
        Objects.requireNonNull(endTime, "End time cannot be null");
        
        return metrics.stream()
                .filter(m -> operation.equals(m.getOperation()))
                .filter(m -> !m.getTimestamp().isBefore(startTime) && !m.getTimestamp().isAfter(endTime))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PerformanceMetrics> getMetricsByType(PerformanceMetrics.MetricType type, LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(startTime, "Start time cannot be null");
        Objects.requireNonNull(endTime, "End time cannot be null");
        
        return metrics.stream()
                .filter(m -> type.equals(m.getType()))
                .filter(m -> !m.getTimestamp().isBefore(startTime) && !m.getTimestamp().isAfter(endTime))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PerformanceMetrics> getLatestMetrics(String operation, int limit) {
        Objects.requireNonNull(operation, "Operation cannot be null");
        
        return metrics.stream()
                .filter(m -> operation.equals(m.getOperation()))
                .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public double getAverageResponseTime(String operation, LocalDateTime startTime, LocalDateTime endTime) {
        List<PerformanceMetrics> responseTimeMetrics = getMetrics(operation, startTime, endTime).stream()
                .filter(m -> m.getType() == PerformanceMetrics.MetricType.RESPONSE_TIME)
                .collect(Collectors.toList());
        
        if (responseTimeMetrics.isEmpty()) {
            return 0.0;
        }
        
        return responseTimeMetrics.stream()
                .mapToDouble(PerformanceMetrics::getValue)
                .average()
                .orElse(0.0);
    }
    
    @Override
    public double getCurrentMemoryUsage() {
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        return usedMemory / (1024.0 * 1024.0); // Convert to MB
    }
    
    @Override
    public double getCurrentCpuUsage() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
            return sunOsBean.getProcessCpuLoad() * 100.0;
        }
        return -1.0; // Unable to determine
    }
    
    @Override
    public List<PerformanceAlert> getActiveAlerts() {
        return alerts.stream()
                .filter(PerformanceAlert::isActive)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PerformanceAlert> getAlertsByType(PerformanceAlert.AlertType type) {
        Objects.requireNonNull(type, "Alert type cannot be null");
        
        return alerts.stream()
                .filter(a -> type.equals(a.getType()))
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<PerformanceAlert> acknowledgeAlert(String alertId) {
        Objects.requireNonNull(alertId, "Alert ID cannot be null");
        
        for (int i = 0; i < alerts.size(); i++) {
            PerformanceAlert alert = alerts.get(i);
            if (alertId.equals(alert.getAlertId())) {
                PerformanceAlert updatedAlert = PerformanceAlert.builder()
                        .alertId(alert.getAlertId())
                        .type(alert.getType())
                        .status(PerformanceAlert.AlertStatus.ACKNOWLEDGED)
                        .title(alert.getTitle())
                        .message(alert.getMessage())
                        .severity(alert.getSeverity())
                        .triggeredAt(alert.getTriggeredAt())
                        .acknowledgedAt(LocalDateTime.now())
                        .resolvedAt(alert.getResolvedAt())
                        .operation(alert.getOperation())
                        .currentValue(alert.getCurrentValue())
                        .threshold(alert.getThreshold())
                        .unit(alert.getUnit())
                        .recommendedAction(alert.getRecommendedAction())
                        .build();
                
                alerts.set(i, updatedAlert);
                return Optional.of(updatedAlert);
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<PerformanceAlert> resolveAlert(String alertId, String resolution) {
        Objects.requireNonNull(alertId, "Alert ID cannot be null");
        
        for (int i = 0; i < alerts.size(); i++) {
            PerformanceAlert alert = alerts.get(i);
            if (alertId.equals(alert.getAlertId())) {
                PerformanceAlert updatedAlert = PerformanceAlert.builder()
                        .alertId(alert.getAlertId())
                        .type(alert.getType())
                        .status(PerformanceAlert.AlertStatus.RESOLVED)
                        .title(alert.getTitle())
                        .message(alert.getMessage() + " Resolution: " + (resolution != null ? resolution : "Resolved"))
                        .severity(alert.getSeverity())
                        .triggeredAt(alert.getTriggeredAt())
                        .acknowledgedAt(alert.getAcknowledgedAt())
                        .resolvedAt(LocalDateTime.now())
                        .operation(alert.getOperation())
                        .currentValue(alert.getCurrentValue())
                        .threshold(alert.getThreshold())
                        .unit(alert.getUnit())
                        .recommendedAction(alert.getRecommendedAction())
                        .build();
                
                alerts.set(i, updatedAlert);
                return Optional.of(updatedAlert);
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public void setThresholds(String operation, long responseTimeThresholdMs, double memoryThresholdMB, double cpuThresholdPercent) {
        Objects.requireNonNull(operation, "Operation cannot be null");
        
        thresholds.put(operation, new ThresholdConfig(
                responseTimeThresholdMs, memoryThresholdMB, cpuThresholdPercent, DEFAULT_ERROR_RATE_THRESHOLD));
    }
    
    @Override
    public Map<String, Object> getPerformanceSummary(LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(startTime, "Start time cannot be null");
        Objects.requireNonNull(endTime, "End time cannot be null");
        
        List<PerformanceMetrics> periodMetrics = metrics.stream()
                .filter(m -> !m.getTimestamp().isBefore(startTime) && !m.getTimestamp().isAfter(endTime))
                .collect(Collectors.toList());
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMetrics", periodMetrics.size());
        summary.put("timeRange", startTime + " to " + endTime);
        
        // Response time statistics
        OptionalDouble avgResponseTime = periodMetrics.stream()
                .filter(m -> m.getType() == PerformanceMetrics.MetricType.RESPONSE_TIME)
                .mapToDouble(PerformanceMetrics::getValue)
                .average();
        summary.put("averageResponseTime", avgResponseTime.orElse(0.0));
        
        // Memory usage statistics
        OptionalDouble avgMemoryUsage = periodMetrics.stream()
                .filter(m -> m.getType() == PerformanceMetrics.MetricType.MEMORY_USAGE)
                .mapToDouble(PerformanceMetrics::getValue)
                .average();
        summary.put("averageMemoryUsage", avgMemoryUsage.orElse(0.0));
        
        // Alert statistics
        long activeAlerts = getActiveAlerts().size();
        summary.put("activeAlerts", activeAlerts);
        
        // Performance issues
        long performanceIssues = periodMetrics.stream()
                .filter(PerformanceMetrics::isPerformanceIssue)
                .count();
        summary.put("performanceIssues", performanceIssues);
        
        return summary;
    }
    
    @Override
    public boolean hasPerformanceIssues() {
        return !getActiveAlerts().isEmpty() || 
               getCurrentMemoryUsage() > DEFAULT_MEMORY_THRESHOLD_MB ||
               getCurrentCpuUsage() > DEFAULT_CPU_THRESHOLD_PERCENT;
    }
    
    @Override
    public String getSystemHealthStatus() {
        List<PerformanceAlert> activeAlerts = getActiveAlerts();
        
        boolean hasCriticalAlerts = activeAlerts.stream()
                .anyMatch(a -> a.getSeverity() == PerformanceMetrics.Severity.CRITICAL || 
                              a.getSeverity() == PerformanceMetrics.Severity.ALERT);
        
        if (hasCriticalAlerts) {
            return "CRITICAL";
        }
        
        boolean hasWarningAlerts = activeAlerts.stream()
                .anyMatch(a -> a.getSeverity() == PerformanceMetrics.Severity.WARNING);
        
        if (hasWarningAlerts || getCurrentMemoryUsage() > DEFAULT_MEMORY_THRESHOLD_MB * 0.8) {
            return "DEGRADED";
        }
        
        return "HEALTHY";
    }
    
    @Override
    public int cleanupOldData(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        int cleanedUp = 0;
        
        // Clean up old metrics
        cleanedUp += metrics.removeIf(m -> m.getTimestamp().isBefore(cutoffTime)) ? 1 : 0;
        
        // Clean up resolved alerts
        cleanedUp += alerts.removeIf(a -> a.isResolved() && a.getResolvedAt().isBefore(cutoffTime)) ? 1 : 0;
        
        return cleanedUp;
    }
    
    private ThresholdConfig getThreshold(String operation) {
        return thresholds.getOrDefault(operation, new ThresholdConfig(
                DEFAULT_RESPONSE_TIME_THRESHOLD_MS, DEFAULT_MEMORY_THRESHOLD_MB, 
                DEFAULT_CPU_THRESHOLD_PERCENT, DEFAULT_ERROR_RATE_THRESHOLD));
    }
    
    private PerformanceMetrics.Severity determineSeverity(PerformanceMetrics.MetricType type, double value, String operation) {
        ThresholdConfig threshold = getThreshold(operation);
        
        switch (type) {
            case RESPONSE_TIME:
                if (value > threshold.responseTimeThresholdMs * 2) {
                    return PerformanceMetrics.Severity.CRITICAL;
                } else if (value > threshold.responseTimeThresholdMs) {
                    return PerformanceMetrics.Severity.WARNING;
                }
                break;
            case MEMORY_USAGE:
                if (value > threshold.memoryThresholdMB * 1.5) {
                    return PerformanceMetrics.Severity.CRITICAL;
                } else if (value > threshold.memoryThresholdMB) {
                    return PerformanceMetrics.Severity.WARNING;
                }
                break;
            case CPU_USAGE:
                if (value > threshold.cpuThresholdPercent * 1.2) {
                    return PerformanceMetrics.Severity.CRITICAL;
                } else if (value > threshold.cpuThresholdPercent) {
                    return PerformanceMetrics.Severity.WARNING;
                }
                break;
            case ERROR_RATE:
                if (value > threshold.errorRateThreshold * 2) {
                    return PerformanceMetrics.Severity.CRITICAL;
                } else if (value > threshold.errorRateThreshold) {
                    return PerformanceMetrics.Severity.WARNING;
                }
                break;
        }
        
        return PerformanceMetrics.Severity.INFO;
    }
    
    private void checkThresholdsAndTriggerAlerts(PerformanceMetrics metric) {
        if (!metric.exceedsThreshold()) {
            return;
        }
        
        // Check if there's already an active alert for this operation and type
        boolean hasActiveAlert = alerts.stream()
                .anyMatch(a -> a.isActive() && 
                              a.getOperation() != null && 
                              a.getOperation().equals(metric.getOperation()) &&
                              getAlertTypeForMetric(metric.getType()) == a.getType());
        
        if (hasActiveAlert) {
            return; // Don't create duplicate alerts
        }
        
        // Create new alert
        PerformanceAlert alert = createAlertFromMetric(metric);
        alerts.add(alert);
    }
    
    private PerformanceAlert createAlertFromMetric(PerformanceMetrics metric) {
        PerformanceAlert.AlertType alertType = getAlertTypeForMetric(metric.getType());
        String title = generateAlertTitle(metric);
        String message = generateAlertMessage(metric);
        String recommendedAction = generateRecommendedAction(metric);
        
        return PerformanceAlert.builder()
                .alertId(UUID.randomUUID().toString())
                .type(alertType)
                .status(PerformanceAlert.AlertStatus.ACTIVE)
                .title(title)
                .message(message)
                .severity(metric.getSeverity())
                .triggeredAt(metric.getTimestamp())
                .operation(metric.getOperation())
                .currentValue(metric.getValue())
                .threshold(metric.getThreshold())
                .unit(metric.getUnit())
                .recommendedAction(recommendedAction)
                .build();
    }
    
    private PerformanceAlert.AlertType getAlertTypeForMetric(PerformanceMetrics.MetricType metricType) {
        switch (metricType) {
            case RESPONSE_TIME:
                return PerformanceAlert.AlertType.RESPONSE_TIME_EXCEEDED;
            case MEMORY_USAGE:
                return PerformanceAlert.AlertType.MEMORY_THRESHOLD_EXCEEDED;
            case CPU_USAGE:
                return PerformanceAlert.AlertType.CPU_THRESHOLD_EXCEEDED;
            case ERROR_RATE:
                return PerformanceAlert.AlertType.ERROR_RATE_HIGH;
            default:
                return PerformanceAlert.AlertType.SYSTEM_OVERLOAD;
        }
    }
    
    private String generateAlertTitle(PerformanceMetrics metric) {
        return String.format("%s threshold exceeded for %s", 
                metric.getType().name().replace("_", " ").toLowerCase(), 
                metric.getOperation());
    }
    
    private String generateAlertMessage(PerformanceMetrics metric) {
        return String.format("Operation '%s' %s is %.1f %s, exceeding threshold of %.1f %s",
                metric.getOperation(),
                metric.getType().name().toLowerCase().replace("_", " "),
                metric.getValue(),
                metric.getUnit(),
                metric.getThreshold(),
                metric.getUnit());
    }
    
    private String generateRecommendedAction(PerformanceMetrics metric) {
        switch (metric.getType()) {
            case RESPONSE_TIME:
                return "Consider optimizing the operation, adding caching, or scaling resources";
            case MEMORY_USAGE:
                return "Check for memory leaks, optimize data structures, or increase heap size";
            case CPU_USAGE:
                return "Optimize algorithms, reduce computational complexity, or scale horizontally";
            case ERROR_RATE:
                return "Investigate error causes, improve error handling, or check system dependencies";
            default:
                return "Monitor system resources and consider scaling if issues persist";
        }
    }
}