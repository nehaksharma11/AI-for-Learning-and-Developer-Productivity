package com.ailearning.core.service.impl;

import com.ailearning.core.model.DevelopmentSession;
import com.ailearning.core.model.ProductivityMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductivityReportGeneratorTest {

    @Mock
    private ProductivityTracker productivityTracker;

    private ProductivityReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        reportGenerator = new ProductivityReportGenerator(productivityTracker);
    }

    @Test
    void testGenerateReport() {
        // Given
        String developerId = "dev123";
        ProductivityReportGenerator.ReportPeriod period = ProductivityReportGenerator.ReportPeriod.DAILY;
        
        ProductivityMetrics mockMetrics = createMockMetrics(developerId);
        List<DevelopmentSession> mockSessions = createMockSessions(developerId);

        when(productivityTracker.calculateMetrics(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockMetrics);
        when(productivityTracker.getSessionHistory(anyString(), anyInt()))
                .thenReturn(mockSessions);

        // When
        ProductivityReportGenerator.ProductivityReport report = reportGenerator.generateReport(developerId, period);

        // Then
        assertNotNull(report);
        assertEquals(developerId, report.getDeveloperId());
        assertEquals(period, report.getPeriod());
        assertNotNull(report.getMetrics());
        assertNotNull(report.getSessions());
        assertNotNull(report.getSummary());
        assertNotNull(report.getTrends());
        assertNotNull(report.getRecommendations());
        assertNotNull(report.getCharts());
        assertNotNull(report.getGeneratedAt());
    }

    @Test
    void testGenerateDailySummary() {
        // Given
        String developerId = "dev123";
        ProductivityMetrics mockMetrics = createMockMetrics(developerId);
        DevelopmentSession mockActiveSession = createMockSession(developerId, "project123");

        when(productivityTracker.getDailyMetrics(developerId)).thenReturn(mockMetrics);
        when(productivityTracker.getActiveSession(developerId)).thenReturn(Optional.of(mockActiveSession));

        // When
        ProductivityReportGenerator.DailySummary summary = reportGenerator.generateDailySummary(developerId);

        // Then
        assertNotNull(summary);
        assertEquals(developerId, summary.getDeveloperId());
        assertNotNull(summary.getDate());
        assertEquals(mockMetrics, summary.getMetrics());
        assertEquals(mockActiveSession, summary.getActiveSession());
        assertTrue(summary.getTotalActiveTime() >= 0);
        assertTrue(summary.getProductivityScore() >= 0.0);
        assertTrue(summary.getProductivityScore() <= 1.0);
        assertNotNull(summary.getKeyAchievements());
        assertNotNull(summary.getAreasForImprovement());
    }

    @Test
    void testGenerateDailySummaryWithoutActiveSession() {
        // Given
        String developerId = "dev123";
        ProductivityMetrics mockMetrics = createMockMetrics(developerId);

        when(productivityTracker.getDailyMetrics(developerId)).thenReturn(mockMetrics);
        when(productivityTracker.getActiveSession(developerId)).thenReturn(Optional.empty());

        // When
        ProductivityReportGenerator.DailySummary summary = reportGenerator.generateDailySummary(developerId);

        // Then
        assertNotNull(summary);
        assertNull(summary.getActiveSession());
    }

    @Test
    void testGenerateTrendAnalysis() {
        // Given
        String developerId = "dev123";
        ProductivityReportGenerator.ReportPeriod period = ProductivityReportGenerator.ReportPeriod.WEEKLY;
        int periodsToCompare = 3;

        // Mock metrics for different periods
        ProductivityMetrics currentMetrics = createMockMetrics(developerId, 0.8, 100, 0.9);
        ProductivityMetrics previousMetrics1 = createMockMetrics(developerId, 0.7, 80, 0.8);
        ProductivityMetrics previousMetrics2 = createMockMetrics(developerId, 0.6, 60, 0.7);

        when(productivityTracker.calculateMetrics(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(currentMetrics, previousMetrics1, previousMetrics2);

        // When
        ProductivityReportGenerator.TrendAnalysis trendAnalysis = reportGenerator.generateTrendAnalysis(
                developerId, period, periodsToCompare);

        // Then
        assertNotNull(trendAnalysis);
        assertEquals(developerId, trendAnalysis.getDeveloperId());
        assertEquals(period, trendAnalysis.getPeriod());
        assertEquals(periodsToCompare, trendAnalysis.getPeriodsAnalyzed());
        assertNotNull(trendAnalysis.getMetrics());
        assertEquals(periodsToCompare, trendAnalysis.getMetrics().size());
        assertNotNull(trendAnalysis.getTrends());
        assertNotNull(trendAnalysis.getInsights());
    }

    @Test
    void testExportReportAsJson() {
        // Given
        String developerId = "dev123";
        ProductivityReportGenerator.ReportPeriod period = ProductivityReportGenerator.ReportPeriod.DAILY;
        
        ProductivityMetrics mockMetrics = createMockMetrics(developerId);
        List<DevelopmentSession> mockSessions = createMockSessions(developerId);

        when(productivityTracker.calculateMetrics(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockMetrics);
        when(productivityTracker.getSessionHistory(anyString(), anyInt()))
                .thenReturn(mockSessions);

        ProductivityReportGenerator.ProductivityReport report = reportGenerator.generateReport(developerId, period);

        // When
        String jsonExport = reportGenerator.exportReport(report, ProductivityReportGenerator.ExportFormat.JSON);

        // Then
        assertNotNull(jsonExport);
        assertTrue(jsonExport.contains("json_export_placeholder")); // Simplified implementation
    }

    @Test
    void testExportReportAsCsv() {
        // Given
        String developerId = "dev123";
        ProductivityReportGenerator.ReportPeriod period = ProductivityReportGenerator.ReportPeriod.DAILY;
        
        ProductivityMetrics mockMetrics = createMockMetrics(developerId);
        List<DevelopmentSession> mockSessions = createMockSessions(developerId);

        when(productivityTracker.calculateMetrics(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockMetrics);
        when(productivityTracker.getSessionHistory(anyString(), anyInt()))
                .thenReturn(mockSessions);

        ProductivityReportGenerator.ProductivityReport report = reportGenerator.generateReport(developerId, period);

        // When
        String csvExport = reportGenerator.exportReport(report, ProductivityReportGenerator.ExportFormat.CSV);

        // Then
        assertNotNull(csvExport);
        assertTrue(csvExport.contains("Metric,Value"));
        assertTrue(csvExport.contains("Developer ID," + developerId));
        assertTrue(csvExport.contains("Period," + period));
    }

    @Test
    void testExportReportAsMarkdown() {
        // Given
        String developerId = "dev123";
        ProductivityReportGenerator.ReportPeriod period = ProductivityReportGenerator.ReportPeriod.DAILY;
        
        ProductivityMetrics mockMetrics = createMockMetrics(developerId);
        List<DevelopmentSession> mockSessions = createMockSessions(developerId);

        when(productivityTracker.calculateMetrics(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockMetrics);
        when(productivityTracker.getSessionHistory(anyString(), anyInt()))
                .thenReturn(mockSessions);

        ProductivityReportGenerator.ProductivityReport report = reportGenerator.generateReport(developerId, period);

        // When
        String markdownExport = reportGenerator.exportReport(report, ProductivityReportGenerator.ExportFormat.MARKDOWN);

        // Then
        assertNotNull(markdownExport);
        assertTrue(markdownExport.contains("# Productivity Report"));
        assertTrue(markdownExport.contains("**Developer:** " + developerId));
        assertTrue(markdownExport.contains("**Period:** " + period));
        assertTrue(markdownExport.contains("## Summary"));
    }

    @Test
    void testExportReportWithUnsupportedFormat() {
        // Given
        String developerId = "dev123";
        ProductivityReportGenerator.ReportPeriod period = ProductivityReportGenerator.ReportPeriod.DAILY;
        
        ProductivityMetrics mockMetrics = createMockMetrics(developerId);
        List<DevelopmentSession> mockSessions = createMockSessions(developerId);

        when(productivityTracker.calculateMetrics(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockMetrics);
        when(productivityTracker.getSessionHistory(anyString(), anyInt()))
                .thenReturn(mockSessions);

        ProductivityReportGenerator.ProductivityReport report = reportGenerator.generateReport(developerId, period);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            reportGenerator.exportReport(report, null));
    }

    @Test
    void testRecommendationsForLowFocusScore() {
        // Given
        String developerId = "dev123";
        ProductivityReportGenerator.ReportPeriod period = ProductivityReportGenerator.ReportPeriod.DAILY;
        
        // Create metrics with low focus score
        ProductivityMetrics lowFocusMetrics = createMockMetrics(developerId, 0.8, 100, 0.5); // Low focus
        List<DevelopmentSession> mockSessions = createMockSessions(developerId);

        when(productivityTracker.calculateMetrics(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(lowFocusMetrics);
        when(productivityTracker.getSessionHistory(anyString(), anyInt()))
                .thenReturn(mockSessions);

        // When
        ProductivityReportGenerator.ProductivityReport report = reportGenerator.generateReport(developerId, period);

        // Then
        List<String> recommendations = report.getRecommendations();
        assertTrue(recommendations.stream().anyMatch(rec -> rec.contains("context switches")));
    }

    @Test
    void testRecommendationsForLowProductivity() {
        // Given
        String developerId = "dev123";
        ProductivityReportGenerator.ReportPeriod period = ProductivityReportGenerator.ReportPeriod.DAILY;
        
        // Create metrics with low productivity score
        ProductivityMetrics lowProductivityMetrics = createMockMetrics(developerId, 0.4, 100, 0.8); // Low productivity
        List<DevelopmentSession> mockSessions = createMockSessions(developerId);

        when(productivityTracker.calculateMetrics(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(lowProductivityMetrics);
        when(productivityTracker.getSessionHistory(anyString(), anyInt()))
                .thenReturn(mockSessions);

        // When
        ProductivityReportGenerator.ProductivityReport report = reportGenerator.generateReport(developerId, period);

        // Then
        List<String> recommendations = report.getRecommendations();
        assertTrue(recommendations.stream().anyMatch(rec -> rec.contains("productivity score")));
    }

    @Test
    void testKeyAchievementsIdentification() {
        // Given
        String developerId = "dev123";
        
        // Create metrics with high scores
        ProductivityMetrics highPerformanceMetrics = ProductivityMetrics.builder()
                .id("metrics123")
                .developerId(developerId)
                .periodStart(LocalDateTime.now().minusHours(8))
                .periodEnd(LocalDateTime.now())
                .linesOfCodeWritten(300) // High output
                .linesOfCodeDeleted(50)
                .activeCodeTime(480) // 8 hours
                .focusScore(0.9) // High focus
                .codeQualityScore(0.9) // High quality
                .velocityScore(120)
                .build();

        when(productivityTracker.getDailyMetrics(developerId)).thenReturn(highPerformanceMetrics);
        when(productivityTracker.getActiveSession(developerId)).thenReturn(Optional.empty());

        // When
        ProductivityReportGenerator.DailySummary summary = reportGenerator.generateDailySummary(developerId);

        // Then
        List<String> achievements = summary.getKeyAchievements();
        assertFalse(achievements.isEmpty());
        assertTrue(achievements.stream().anyMatch(achievement -> achievement.contains("productivity score")));
        assertTrue(achievements.stream().anyMatch(achievement -> achievement.contains("code output")));
        assertTrue(achievements.stream().anyMatch(achievement -> achievement.contains("focus")));
        assertTrue(achievements.stream().anyMatch(achievement -> achievement.contains("quality")));
    }

    @Test
    void testImprovementAreasIdentification() {
        // Given
        String developerId = "dev123";
        
        // Create metrics with low scores
        ProductivityMetrics lowPerformanceMetrics = ProductivityMetrics.builder()
                .id("metrics123")
                .developerId(developerId)
                .periodStart(LocalDateTime.now().minusHours(8))
                .periodEnd(LocalDateTime.now())
                .linesOfCodeWritten(50)
                .linesOfCodeDeleted(10)
                .activeCodeTime(120) // Only 2 hours
                .focusScore(0.4) // Low focus
                .codeQualityScore(0.4) // Low quality
                .velocityScore(30)
                .build();

        when(productivityTracker.getDailyMetrics(developerId)).thenReturn(lowPerformanceMetrics);
        when(productivityTracker.getActiveSession(developerId)).thenReturn(Optional.empty());

        // When
        ProductivityReportGenerator.DailySummary summary = reportGenerator.generateDailySummary(developerId);

        // Then
        List<String> improvements = summary.getAreasForImprovement();
        assertFalse(improvements.isEmpty());
        assertTrue(improvements.stream().anyMatch(improvement -> improvement.contains("Focus")));
        assertTrue(improvements.stream().anyMatch(improvement -> improvement.contains("Code Quality")));
        assertTrue(improvements.stream().anyMatch(improvement -> improvement.contains("Active Time")));
    }

    @Test
    void testConstructorWithNullProductivityTracker() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new ProductivityReportGenerator(null));
    }

    // Helper methods

    private ProductivityMetrics createMockMetrics(String developerId) {
        return createMockMetrics(developerId, 0.75, 100, 0.8);
    }

    private ProductivityMetrics createMockMetrics(String developerId, double productivityScore, 
                                                int netLinesOfCode, double focusScore) {
        return ProductivityMetrics.builder()
                .id("metrics123")
                .developerId(developerId)
                .periodStart(LocalDateTime.now().minusHours(8))
                .periodEnd(LocalDateTime.now())
                .linesOfCodeWritten(netLinesOfCode + 20)
                .linesOfCodeDeleted(20)
                .commitsCount(5)
                .testsWritten(3)
                .activeCodeTime(300)
                .learningTime(60)
                .focusScore(focusScore)
                .codeQualityScore(0.7)
                .velocityScore(productivityScore * 100)
                .build();
    }

    private List<DevelopmentSession> createMockSessions(String developerId) {
        DevelopmentSession session1 = createMockSession(developerId, "project123");
        DevelopmentSession session2 = createMockSession(developerId, "project456");
        return Arrays.asList(session1, session2);
    }

    private DevelopmentSession createMockSession(String developerId, String projectId) {
        return DevelopmentSession.builder()
                .id("session123")
                .developerId(developerId)
                .projectId(projectId)
                .startTime(LocalDateTime.now().minusHours(2))
                .endTime(LocalDateTime.now().minusHours(1))
                .state(DevelopmentSession.SessionState.COMPLETED)
                .linesAdded(50)
                .linesDeleted(10)
                .filesModified(3)
                .focusScore(0.8)
                .build();
    }
}