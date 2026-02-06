package com.ailearning.core.service.impl;

import com.ailearning.core.model.PerformanceAlert;
import com.ailearning.core.model.PerformanceMetrics;
import com.ailearning.core.service.PerformanceMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultPerformanceMonitoringService.
 */
class DefaultPerformanceMonitoringServiceTest {
    
    private PerformanceMonitoringService service;
    
    @BeforeEach
    void setUp() {
        service = new DefaultPerformanceMonitoringService();
    }
    
    @Test
    void testRecordMetric() {
        PerformanceMetrics metric = PerformanceMetrics.builder()
                .metricId("test-1")
                .type(PerformanceMetrics.MetricType.RESPONSE_TIME)
                .operation("testOperation")
                .value(100.0)
                .unit("ms")
                .timestamp(LocalDateTime.now())
                .severity(PerformanceMetrics.Severity.INFO)
                .build();
        
        assertDoesNotThrow(() -> service.recordMetric(metric));
        
        List<PerformanceMetrics> metrics = service.getLatestMetrics("testOperation", 10);
        assertEquals(1, metrics.size());
        assertEquals("testOperation", metrics.get(0).getOperation());
    }
    
    @Test
    void testRecordResponseTime() {
        service.recordResponseTime("contextUpdate", 450L, Map.of("context", "test"));
        
        List<PerformanceMetrics> metrics = service.getLatestMetrics("contextUpdate", 10);
        assertEquals(1, metrics.size());
        assertEquals(450.0, metrics.get(0).getValue());
        assertEquals("ms", metrics.get(0).getUnit());
        assertEquals(PerformanceMetrics.MetricType.RESPONSE_TIME, metrics.get(0).getType());
    }
    
    @Test
    void testRecordResponseTimeExceedsThreshold() {
        // Record response time that exceeds default threshold (500ms)
        service.recordResponseTime("slowOperation", 600L, Map.of());
        
        List<PerformanceMetrics> metrics = service.getLatestMetrics("slowOperation", 10);
        assertEquals(1, metrics.size());
        assertTrue(metrics.get(0).exceedsThreshold());
        assertTrue(metrics.get(0).isAlertTriggered());
        
        // Should trigger an alert
        List<PerformanceAlert> alerts = service.getActiveAlerts();
        assertFalse(alerts.isEmpty());
        assertEquals(PerformanceAlert.AlertType.RESPONSE_TIME_EXCEEDED, alerts.get(0).getType());
    }
    
    @Test
    void testRecordMemoryUsage() {
        service.recordMemoryUsage("analysis", 256.5);
        
        List<PerformanceMetrics> metrics = service.getLatestMetrics("analysis", 10);
        assertEquals(1, metrics.size());
        assertEquals(256.5, metrics.get(0).getValue());
        assertEquals("MB", metrics.get(0).getUnit());
        assertEquals(PerformanceMetrics.MetricType.MEMORY_USAGE, metrics.get(0).getType());
    }
    
    @Test
    void testRecordMemoryUsageExceedsThreshold() {
        // Record memory usage that exceeds default threshold (500MB)
        service.recordMemoryUsage("largeOperation", 600.0);
        
        List<PerformanceMetrics> metrics = service.getLatestMetrics("largeOperation", 10);
        assertEquals(1, metrics.size());
        assertTrue(metrics.get(0).exceedsThreshold());
        
        // Should trigger an alert
        List<PerformanceAlert> alerts = service.getActiveAlerts();
        assertFalse(alerts.isEmpty());
    }
    
    @Test
    void testRecordCpuUsage() {
        service.recordCpuUsage("computation", 45.5);
        
        List<PerformanceMetrics> metrics = service.getLatestMetrics("computation", 10);
        assertEquals(1, metrics.size());
        assertEquals(45.5, metrics.get(0).getValue());
        assertEquals("%", metrics.get(0).getUnit());
        assertEquals(PerformanceMetrics.MetricType.CPU_USAGE, metrics.get(0).getType());
    }
    
    @Test
    void testRecordThroughput() {
        service.recordThroughput("processing", 1000.0);
        
        List<PerformanceMetrics> metrics = service.getLatestMetrics("processing", 10);
        assertEquals(1, metrics.size());
        assertEquals(1000.0, metrics.get(0).getValue());
        assertEquals("ops/sec", metrics.get(0).getUnit());
    }
    
    @Test
    void testRecordErrorRate() {
        service.recordErrorRate("apiCall", 2.5);
        
        List<PerformanceMetrics> metrics = service.getLatestMetrics("apiCall", 10);
        assertEquals(1, metrics.size());
        assertEquals(2.5, metrics.get(0).getValue());
        assertEquals("%", metrics.get(0).getUnit());
    }
    
    @Test
    void testGetMetricsByTimeRange() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        
        service.recordResponseTime("operation1", 100L, Map.of());
        service.recordResponseTime("operation1", 200L, Map.of());
        service.recordResponseTime("operation2", 150L, Map.of());
        
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);
        
        List<PerformanceMetrics> metrics = service.getMetrics("operation1", start, end);
        assertEquals(2, metrics.size());
        
        List<PerformanceMetrics> operation2Metrics = service.getMetrics("operation2", start, end);
        assertEquals(1, metrics.size());
    }
    
    @Test
    void testGetMetricsByType() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        
        service.recordResponseTime("op1", 100L, Map.of());
        service.recordMemoryUsage("op2", 200.0);
        service.recordCpuUsage("op3", 50.0);
        service.recordResponseTime("op4", 150L, Map.of());
        
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);
        
        List<PerformanceMetrics> responseTimeMetrics = service.getMetricsByType(
                PerformanceMetrics.MetricType.RESPONSE_TIME, start, end);
        assertEquals(2, responseTimeMetrics.size());
        
        List<PerformanceMetrics> memoryMetrics = service.getMetricsByType(
                PerformanceMetrics.MetricType.MEMORY_USAGE, start, end);
        assertEquals(1, memoryMetrics.size());
    }
    
    @Test
    void testGetAverageResponseTime() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        
        service.recordResponseTime("operation", 100L, Map.of());
        service.recordResponseTime("operation", 200L, Map.of());
        service.recordResponseTime("operation", 300L, Map.of());
        
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);
        
        double average = service.getAverageResponseTime("operation", start, end);
        assertEquals(200.0, average, 0.01);
    }
    
    @Test
    void testGetCurrentMemoryUsage() {
        double memoryUsage = service.getCurrentMemoryUsage();
        assertTrue(memoryUsage >= 0);
    }
    
    @Test
    void testGetCurrentCpuUsage() {
        double cpuUsage = service.getCurrentCpuUsage();
        // CPU usage might be -1 if not available on the platform
        assertTrue(cpuUsage >= -1.0 && cpuUsage <= 100.0);
    }
    
    @Test
    void testAlertTriggering() {
        // Trigger an alert by exceeding threshold
        service.recordResponseTime("slowOp", 1000L, Map.of());
        
        List<PerformanceAlert> alerts = service.getActiveAlerts();
        assertFalse(alerts.isEmpty());
        
        PerformanceAlert alert = alerts.get(0);
        assertTrue(alert.isActive());
        assertFalse(alert.isAcknowledged());
        assertFalse(alert.isResolved());
        assertEquals(PerformanceAlert.AlertType.RESPONSE_TIME_EXCEEDED, alert.getType());
    }
    
    @Test
    void testAcknowledgeAlert() {
        // Trigger an alert
        service.recordResponseTime("slowOp", 1000L, Map.of());
        
        List<PerformanceAlert> alerts = service.getActiveAlerts();
        assertFalse(alerts.isEmpty());
        
        String alertId = alerts.get(0).getAlertId();
        
        Optional<PerformanceAlert> acknowledged = service.acknowledgeAlert(alertId);
        assertTrue(acknowledged.isPresent());
        assertTrue(acknowledged.get().isAcknowledged());
        assertEquals(PerformanceAlert.AlertStatus.ACKNOWLEDGED, acknowledged.get().getStatus());
    }
    
    @Test
    void testResolveAlert() {
        // Trigger an alert
        service.recordResponseTime("slowOp", 1000L, Map.of());
        
        List<PerformanceAlert> alerts = service.getActiveAlerts();
        assertFalse(alerts.isEmpty());
        
        String alertId = alerts.get(0).getAlertId();
        
        Optional<PerformanceAlert> resolved = service.resolveAlert(alertId, "Fixed performance issue");
        assertTrue(resolved.isPresent());
        assertTrue(resolved.get().isResolved());
        assertEquals(PerformanceAlert.AlertStatus.RESOLVED, resolved.get().getStatus());
    }
    
    @Test
    void testSetThresholds() {
        service.setThresholds("customOp", 200L, 300.0, 70.0);
        
        // Record metric that would exceed default but not custom threshold
        service.recordResponseTime("customOp", 250L, Map.of());
        
        List<PerformanceMetrics> metrics = service.getLatestMetrics("customOp", 10);
        assertFalse(metrics.isEmpty());
        assertFalse(metrics.get(0).exceedsThreshold());
    }
    
    @Test
    void testGetPerformanceSummary() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        
        service.recordResponseTime("op1", 100L, Map.of());
        service.recordResponseTime("op1", 200L, Map.of());
        service.recordMemoryUsage("op2", 300.0);
        
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);
        
        Map<String, Object> summary = service.getPerformanceSummary(start, end);
        
        assertNotNull(summary);
        assertTrue(summary.containsKey("totalMetrics"));
        assertTrue(summary.containsKey("averageResponseTime"));
        assertTrue(summary.containsKey("averageMemoryUsage"));
        assertTrue(summary.containsKey("activeAlerts"));
        
        assertEquals(3, summary.get("totalMetrics"));
    }
    
    @Test
    void testHasPerformanceIssues() {
        // Initially should have no issues
        assertFalse(service.hasPerformanceIssues());
        
        // Trigger an alert
        service.recordResponseTime("slowOp", 1000L, Map.of());
        
        // Now should have issues
        assertTrue(service.hasPerformanceIssues());
    }
    
    @Test
    void testGetSystemHealthStatus() {
        // Initially should be healthy
        String health = service.getSystemHealthStatus();
        assertEquals("HEALTHY", health);
        
        // Trigger a warning
        service.recordResponseTime("slowOp", 600L, Map.of());
        
        health = service.getSystemHealthStatus();
        assertTrue(health.equals("DEGRADED") || health.equals("HEALTHY"));
        
        // Trigger a critical alert
        service.recordResponseTime("criticalOp", 1500L, Map.of());
        
        health = service.getSystemHealthStatus();
        assertEquals("CRITICAL", health);
    }
    
    @Test
    void testCleanupOldData() {
        service.recordResponseTime("op1", 100L, Map.of());
        service.recordResponseTime("op2", 200L, Map.of());
        
        // Cleanup data older than 30 days (should not remove recent data)
        int cleaned = service.cleanupOldData(30);
        
        // Recent data should still be there
        List<PerformanceMetrics> metrics = service.getLatestMetrics("op1", 10);
        assertFalse(metrics.isEmpty());
    }
    
    @Test
    void testNoDuplicateAlerts() {
        // Trigger multiple threshold violations for same operation
        service.recordResponseTime("slowOp", 1000L, Map.of());
        service.recordResponseTime("slowOp", 1100L, Map.of());
        service.recordResponseTime("slowOp", 1200L, Map.of());
        
        // Should only create one alert
        List<PerformanceAlert> alerts = service.getActiveAlerts();
        long slowOpAlerts = alerts.stream()
                .filter(a -> "slowOp".equals(a.getOperation()))
                .count();
        
        assertEquals(1, slowOpAlerts);
    }
    
    @Test
    void testGetAlertsByType() {
        service.recordResponseTime("slowOp", 1000L, Map.of());
        service.recordMemoryUsage("memoryOp", 600.0);
        
        List<PerformanceAlert> responseTimeAlerts = service.getAlertsByType(
                PerformanceAlert.AlertType.RESPONSE_TIME_EXCEEDED);
        assertEquals(1, responseTimeAlerts.size());
        
        List<PerformanceAlert> memoryAlerts = service.getAlertsByType(
                PerformanceAlert.AlertType.MEMORY_THRESHOLD_EXCEEDED);
        assertEquals(1, memoryAlerts.size());
    }
}
