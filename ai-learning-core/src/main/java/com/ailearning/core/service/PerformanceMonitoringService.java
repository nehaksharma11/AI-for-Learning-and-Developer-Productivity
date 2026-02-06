package com.ailearning.core.service;

import com.ailearning.core.model.PerformanceAlert;
import com.ailearning.core.model.PerformanceMetrics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for monitoring system performance and triggering alerts when thresholds are exceeded.
 */
public interface PerformanceMonitoringService {
    
    /**
     * Records a performance metric.
     * 
     * @param metric The performance metric to record
     */
    void recordMetric(PerformanceMetrics metric);
    
    /**
     * Records response time for an operation.
     * 
     * @param operation The operation name
     * @param responseTimeMs The response time in milliseconds
     * @param metadata Additional metadata about the operation
     */
    void recordResponseTime(String operation, long responseTimeMs, Map<String, Object> metadata);
    
    /**
     * Records memory usage.
     * 
     * @param operation The operation that triggered memory measurement
     * @param memoryUsageMB The memory usage in megabytes
     */
    void recordMemoryUsage(String operation, double memoryUsageMB);
    
    /**
     * Records CPU usage.
     * 
     * @param operation The operation that triggered CPU measurement
     * @param cpuUsagePercent The CPU usage percentage
     */
    void recordCpuUsage(String operation, double cpuUsagePercent);
    
    /**
     * Records throughput metric.
     * 
     * @param operation The operation name
     * @param throughputPerSecond The throughput in operations per second
     */
    void recordThroughput(String operation, double throughputPerSecond);
    
    /**
     * Records error rate.
     * 
     * @param operation The operation name
     * @param errorRate The error rate as a percentage
     */
    void recordErrorRate(String operation, double errorRate);
    
    /**
     * Gets performance metrics for a specific operation within a time range.
     * 
     * @param operation The operation name
     * @param startTime The start time for the query
     * @param endTime The end time for the query
     * @return List of performance metrics
     */
    List<PerformanceMetrics> getMetrics(String operation, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Gets performance metrics by type within a time range.
     * 
     * @param type The metric type
     * @param startTime The start time for the query
     * @param endTime The end time for the query
     * @return List of performance metrics
     */
    List<PerformanceMetrics> getMetricsByType(PerformanceMetrics.MetricType type, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Gets the latest performance metrics for an operation.
     * 
     * @param operation The operation name
     * @param limit The maximum number of metrics to return
     * @return List of latest performance metrics
     */
    List<PerformanceMetrics> getLatestMetrics(String operation, int limit);
    
    /**
     * Gets average response time for an operation over a time period.
     * 
     * @param operation The operation name
     * @param startTime The start time for the calculation
     * @param endTime The end time for the calculation
     * @return The average response time in milliseconds
     */
    double getAverageResponseTime(String operation, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Gets current memory usage.
     * 
     * @return Current memory usage in megabytes
     */
    double getCurrentMemoryUsage();
    
    /**
     * Gets current CPU usage.
     * 
     * @return Current CPU usage percentage
     */
    double getCurrentCpuUsage();
    
    /**
     * Gets all active performance alerts.
     * 
     * @return List of active performance alerts
     */
    List<PerformanceAlert> getActiveAlerts();
    
    /**
     * Gets performance alerts by type.
     * 
     * @param type The alert type
     * @return List of performance alerts
     */
    List<PerformanceAlert> getAlertsByType(PerformanceAlert.AlertType type);
    
    /**
     * Acknowledges a performance alert.
     * 
     * @param alertId The alert ID
     * @return The updated alert, if found
     */
    Optional<PerformanceAlert> acknowledgeAlert(String alertId);
    
    /**
     * Resolves a performance alert.
     * 
     * @param alertId The alert ID
     * @param resolution The resolution description
     * @return The updated alert, if found
     */
    Optional<PerformanceAlert> resolveAlert(String alertId, String resolution);
    
    /**
     * Sets performance thresholds for monitoring.
     * 
     * @param operation The operation name
     * @param responseTimeThresholdMs Response time threshold in milliseconds
     * @param memoryThresholdMB Memory usage threshold in megabytes
     * @param cpuThresholdPercent CPU usage threshold percentage
     */
    void setThresholds(String operation, long responseTimeThresholdMs, double memoryThresholdMB, double cpuThresholdPercent);
    
    /**
     * Gets performance summary for a time period.
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return Map containing performance summary statistics
     */
    Map<String, Object> getPerformanceSummary(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Checks if the system is currently experiencing performance issues.
     * 
     * @return True if there are active critical alerts or performance degradation
     */
    boolean hasPerformanceIssues();
    
    /**
     * Gets system health status based on current metrics.
     * 
     * @return Health status ("HEALTHY", "DEGRADED", "CRITICAL")
     */
    String getSystemHealthStatus();
    
    /**
     * Clears old metrics and alerts based on retention policy.
     * 
     * @param retentionDays Number of days to retain data
     * @return Number of records cleaned up
     */
    int cleanupOldData(int retentionDays);
}