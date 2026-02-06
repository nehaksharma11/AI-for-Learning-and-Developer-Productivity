package com.ailearning.core.service.impl;

import com.ailearning.core.model.DevelopmentSession;
import com.ailearning.core.model.ProductivityMetrics;
import com.ailearning.core.model.CodeChange;
import com.ailearning.core.model.AnalysisResult;
import com.ailearning.core.model.ComplexityMetrics;
import com.ailearning.core.service.CodeAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductivityTrackerTest {

    @Mock
    private CodeAnalyzer codeAnalyzer;

    private ProductivityTracker productivityTracker;

    @BeforeEach
    void setUp() {
        productivityTracker = new ProductivityTracker(codeAnalyzer);
    }

    @Test
    void testStartSession() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";

        // When
        DevelopmentSession session = productivityTracker.startSession(developerId, projectId);

        // Then
        assertNotNull(session);
        assertEquals(developerId, session.getDeveloperId());
        assertEquals(projectId, session.getProjectId());
        assertEquals(DevelopmentSession.SessionState.ACTIVE, session.getState());
        assertNotNull(session.getId());
        assertNotNull(session.getStartTime());
    }

    @Test
    void testUpdateSession() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        DevelopmentSession session = productivityTracker.startSession(developerId, projectId);
        
        ProductivityTracker.SessionUpdateData updateData = new ProductivityTracker.SessionUpdateData();
        updateData.keystrokesDelta = 100;
        updateData.linesAdded = 10;
        updateData.linesDeleted = 2;
        updateData.filesModified = 1;

        // When
        DevelopmentSession updatedSession = productivityTracker.updateSession(session.getId(), updateData);

        // Then
        assertNotNull(updatedSession);
        assertEquals(100, updatedSession.getTotalKeystrokes());
        assertEquals(10, updatedSession.getLinesAdded());
        assertEquals(2, updatedSession.getLinesDeleted());
        assertEquals(1, updatedSession.getFilesModified());
    }

    @Test
    void testUpdateSessionWithInvalidId() {
        // Given
        String invalidSessionId = "invalid123";
        ProductivityTracker.SessionUpdateData updateData = new ProductivityTracker.SessionUpdateData();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            productivityTracker.updateSession(invalidSessionId, updateData));
    }

    @Test
    void testEndSession() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        DevelopmentSession session = productivityTracker.startSession(developerId, projectId);

        // When
        DevelopmentSession endedSession = productivityTracker.endSession(session.getId());

        // Then
        assertNotNull(endedSession);
        assertEquals(DevelopmentSession.SessionState.COMPLETED, endedSession.getState());
        assertNotNull(endedSession.getEndTime());
        
        // Session should no longer be active
        Optional<DevelopmentSession> activeSession = productivityTracker.getActiveSession(developerId);
        assertFalse(activeSession.isPresent());
    }

    @Test
    void testEndSessionWithInvalidId() {
        // Given
        String invalidSessionId = "invalid123";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            productivityTracker.endSession(invalidSessionId));
    }

    @Test
    void testCalculateMetrics() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();

        // Create and end a session to have data for metrics
        DevelopmentSession session = productivityTracker.startSession(developerId, projectId);
        ProductivityTracker.SessionUpdateData updateData = new ProductivityTracker.SessionUpdateData();
        updateData.linesAdded = 50;
        updateData.linesDeleted = 10;
        updateData.filesModified = 3;
        productivityTracker.updateSession(session.getId(), updateData);
        productivityTracker.endSession(session.getId());

        // When
        ProductivityMetrics metrics = productivityTracker.calculateMetrics(developerId, start, end);

        // Then
        assertNotNull(metrics);
        assertEquals(developerId, metrics.getDeveloperId());
        assertEquals(50, metrics.getLinesOfCodeWritten());
        assertEquals(10, metrics.getLinesOfCodeDeleted());
        assertEquals(40, metrics.getNetLinesOfCode());
        assertTrue(metrics.getOverallProductivityScore() >= 0.0);
        assertTrue(metrics.getOverallProductivityScore() <= 1.0);
    }

    @Test
    void testCalculateMetricsWithNoSessions() {
        // Given
        String developerId = "dev123";
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();

        // When
        ProductivityMetrics metrics = productivityTracker.calculateMetrics(developerId, start, end);

        // Then
        assertNotNull(metrics);
        assertEquals(developerId, metrics.getDeveloperId());
        assertEquals(0, metrics.getLinesOfCodeWritten());
        assertEquals(0, metrics.getLinesOfCodeDeleted());
        assertEquals(0, metrics.getNetLinesOfCode());
    }

    @Test
    void testGetDailyMetrics() {
        // Given
        String developerId = "dev123";

        // When
        ProductivityMetrics dailyMetrics = productivityTracker.getDailyMetrics(developerId);

        // Then
        assertNotNull(dailyMetrics);
        assertEquals(developerId, dailyMetrics.getDeveloperId());
        assertTrue(dailyMetrics.getPeriodStart().toLocalDate().equals(LocalDateTime.now().toLocalDate()));
    }

    @Test
    void testGetWeeklyMetrics() {
        // Given
        String developerId = "dev123";

        // When
        ProductivityMetrics weeklyMetrics = productivityTracker.getWeeklyMetrics(developerId);

        // Then
        assertNotNull(weeklyMetrics);
        assertEquals(developerId, weeklyMetrics.getDeveloperId());
        // Should start from beginning of current week
        assertTrue(weeklyMetrics.getPeriodStart().getDayOfWeek().getValue() == 1); // Monday
    }

    @Test
    void testRecordCodeChange() throws Exception {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        DevelopmentSession session = productivityTracker.startSession(developerId, projectId);
        
        CodeChange codeChange = CodeChange.builder()
                .id("change123")
                .filePath("test.java")
                .language("java")
                .oldContent("old code")
                .newContent("new code")
                .linesAdded(5)
                .linesDeleted(2)
                .timestamp(LocalDateTime.now())
                .build();

        // Mock code analyzer
        AnalysisResult analysisResult = AnalysisResult.builder()
                .id("analysis123")
                .filePath("test.java")
                .language("java")
                .qualityScore(0.8)
                .complexityMetrics(ComplexityMetrics.builder()
                        .cyclomaticComplexity(3)
                        .cognitiveComplexity(2)
                        .linesOfCode(100)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        when(codeAnalyzer.analyzeCode(anyString(), anyString())).thenReturn(analysisResult);

        // When
        productivityTracker.recordCodeChange(session.getId(), codeChange);

        // Then
        Optional<DevelopmentSession> activeSession = productivityTracker.getActiveSession(developerId);
        assertTrue(activeSession.isPresent());
        assertEquals(5, activeSession.get().getLinesAdded());
        assertEquals(2, activeSession.get().getLinesDeleted());
        assertEquals(1, activeSession.get().getFilesModified());
    }

    @Test
    void testRecordCodeChangeWithAnalysisFailure() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        DevelopmentSession session = productivityTracker.startSession(developerId, projectId);
        
        CodeChange codeChange = CodeChange.builder()
                .id("change123")
                .filePath("test.java")
                .language("java")
                .oldContent("old code")
                .newContent("new code")
                .linesAdded(5)
                .linesDeleted(2)
                .timestamp(LocalDateTime.now())
                .build();

        // Mock code analyzer to throw exception
        when(codeAnalyzer.analyzeCode(anyString(), anyString())).thenThrow(new RuntimeException("Analysis failed"));

        // When - should not throw exception
        assertDoesNotThrow(() -> productivityTracker.recordCodeChange(session.getId(), codeChange));

        // Then - session should still be updated with basic metrics
        Optional<DevelopmentSession> activeSession = productivityTracker.getActiveSession(developerId);
        assertTrue(activeSession.isPresent());
        assertEquals(5, activeSession.get().getLinesAdded());
        assertEquals(2, activeSession.get().getLinesDeleted());
    }

    @Test
    void testGetActiveSession() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";

        // When - no active session initially
        Optional<DevelopmentSession> noActiveSession = productivityTracker.getActiveSession(developerId);

        // Then
        assertFalse(noActiveSession.isPresent());

        // When - start a session
        DevelopmentSession session = productivityTracker.startSession(developerId, projectId);
        Optional<DevelopmentSession> activeSession = productivityTracker.getActiveSession(developerId);

        // Then
        assertTrue(activeSession.isPresent());
        assertEquals(session.getId(), activeSession.get().getId());
    }

    @Test
    void testGetSessionHistory() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";

        // Create and end multiple sessions
        for (int i = 0; i < 3; i++) {
            DevelopmentSession session = productivityTracker.startSession(developerId, projectId);
            productivityTracker.endSession(session.getId());
        }

        // When
        var history = productivityTracker.getSessionHistory(developerId, 10);

        // Then
        assertEquals(3, history.size());
        // Should be sorted by start time descending (most recent first)
        for (int i = 0; i < history.size() - 1; i++) {
            assertTrue(history.get(i).getStartTime().isAfter(history.get(i + 1).getStartTime()) ||
                      history.get(i).getStartTime().equals(history.get(i + 1).getStartTime()));
        }
    }

    @Test
    void testGetSessionHistoryWithLimit() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";

        // Create and end multiple sessions
        for (int i = 0; i < 5; i++) {
            DevelopmentSession session = productivityTracker.startSession(developerId, projectId);
            productivityTracker.endSession(session.getId());
        }

        // When
        var history = productivityTracker.getSessionHistory(developerId, 3);

        // Then
        assertEquals(3, history.size());
    }

    @Test
    void testFocusScoreCalculation() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        DevelopmentSession session = productivityTracker.startSession(developerId, projectId);

        // When - update with interruptions and context switches
        ProductivityTracker.SessionUpdateData updateData = new ProductivityTracker.SessionUpdateData();
        updateData.interruptions = 5; // High interruptions should lower focus score
        updateData.contextSwitches = 10; // High context switches should lower focus score
        
        DevelopmentSession updatedSession = productivityTracker.updateSession(session.getId(), updateData);

        // Then
        assertTrue(updatedSession.getFocusScore() < 1.0); // Should be penalized
        assertTrue(updatedSession.getFocusScore() >= 0.0); // Should not go negative
    }

    @Test
    void testMetricsCaching() {
        // Given
        String developerId = "dev123";
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();

        // When - calculate metrics twice
        ProductivityMetrics metrics1 = productivityTracker.calculateMetrics(developerId, start, end);
        ProductivityMetrics metrics2 = productivityTracker.calculateMetrics(developerId, start, end);

        // Then - should return same instance (cached)
        assertEquals(metrics1.getCalculatedAt(), metrics2.getCalculatedAt());
    }

    @Test
    void testConstructorWithNullCodeAnalyzer() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new ProductivityTracker(null));
    }
}