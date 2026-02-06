package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.ContextEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextPreservationServiceTest {

    @Mock
    private ContextEngine contextEngine;

    @Mock
    private ProductivityTracker productivityTracker;

    private ContextPreservationService contextPreservationService;

    @BeforeEach
    void setUp() {
        contextPreservationService = new ContextPreservationService(contextEngine, productivityTracker);
    }

    @Test
    void testCaptureWorkState() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        
        ContextPreservationService.CaptureRequest request = new ContextPreservationService.CaptureRequest();
        request.setCurrentActivity(WorkState.StateType.CODING);
        request.setOpenFiles(Arrays.asList("file1.java", "file2.java"));
        request.setActiveFile("file1.java");
        request.setCursorPosition(100);
        request.setCurrentTask("Implement user authentication");
        request.setCurrentGoal("Complete login functionality");

        // When
        WorkState workState = contextPreservationService.captureWorkState(developerId, projectId, request);

        // Then
        assertNotNull(workState);
        assertEquals(developerId, workState.getDeveloperId());
        assertEquals(projectId, workState.getProjectId());
        assertEquals(WorkState.StateType.CODING, workState.getCurrentActivity());
        assertEquals(Arrays.asList("file1.java", "file2.java"), workState.getOpenFiles());
        assertEquals("file1.java", workState.getActiveFile());
        assertEquals(100, workState.getCursorPosition());
        assertEquals("Implement user authentication", workState.getCurrentTask());
        assertEquals("Complete login functionality", workState.getCurrentGoal());
        assertNotNull(workState.getId());
        assertTrue(workState.isValid());
    }

    @Test
    void testRestoreWorkStateSuccess() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        
        // First capture a work state
        ContextPreservationService.CaptureRequest request = new ContextPreservationService.CaptureRequest();
        request.setCurrentActivity(WorkState.StateType.CODING);
        request.setActiveFile("test.java");
        request.setCurrentTask("Test task");
        
        WorkState capturedState = contextPreservationService.captureWorkState(developerId, projectId, request);

        // When
        RestorationResult result = contextPreservationService.restoreWorkState(capturedState.getId(), developerId);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getWorkState());
        assertEquals(capturedState.getId(), result.getWorkState().getId());
        assertFalse(result.getInstructions().isEmpty());
        assertEquals("Work state restored successfully", result.getMessage());
    }

    @Test
    void testRestoreWorkStateNotFound() {
        // Given
        String developerId = "dev123";
        String nonExistentWorkStateId = "nonexistent123";

        // When
        RestorationResult result = contextPreservationService.restoreWorkState(nonExistentWorkStateId, developerId);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getWorkState());
        assertTrue(result.getMessage().contains("Work state not found"));
    }

    @Test
    void testRestoreWorkStateWrongDeveloper() {
        // Given
        String developerId1 = "dev123";
        String developerId2 = "dev456";
        String projectId = "project789";
        
        // Capture work state for developer 1
        ContextPreservationService.CaptureRequest request = new ContextPreservationService.CaptureRequest();
        request.setCurrentActivity(WorkState.StateType.CODING);
        WorkState capturedState = contextPreservationService.captureWorkState(developerId1, projectId, request);

        // When - try to restore with developer 2
        RestorationResult result = contextPreservationService.restoreWorkState(capturedState.getId(), developerId2);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("does not belong to developer"));
    }

    @Test
    void testRecordContextSwitch() {
        // Given
        String developerId = "dev123";
        
        ContextPreservationService.ContextSwitchRequest request = new ContextPreservationService.ContextSwitchRequest();
        request.setSwitchType(ContextSwitchEvent.SwitchType.TASK_CHANGE);
        request.setSwitchReason(ContextSwitchEvent.SwitchReason.INTERRUPTION);
        request.setNewTask("New urgent task");
        request.setInterruptionCount(1);
        request.setInterruptionSource("Slack message");

        // Mock productivity tracker
        when(productivityTracker.getActiveSession(developerId)).thenReturn(Optional.empty());

        // When
        ContextSwitchEvent event = contextPreservationService.recordContextSwitch(developerId, request);

        // Then
        assertNotNull(event);
        assertEquals(developerId, event.getDeveloperId());
        assertEquals(ContextSwitchEvent.SwitchType.TASK_CHANGE, event.getSwitchType());
        assertEquals(ContextSwitchEvent.SwitchReason.INTERRUPTION, event.getSwitchReason());
        assertEquals("New urgent task", event.getNewTask());
        assertEquals(1, event.getInterruptionCount());
        assertEquals("Slack message", event.getInterruptionSource());
        assertTrue(event.getEstimatedProductivityImpact() < 0); // Should be negative for interruption
        assertTrue(event.getEstimatedRecoveryTimeMinutes() > 0);
    }

    @Test
    void testGetAvailableWorkStates() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        
        // Capture multiple work states
        for (int i = 0; i < 3; i++) {
            ContextPreservationService.CaptureRequest request = new ContextPreservationService.CaptureRequest();
            request.setCurrentActivity(WorkState.StateType.CODING);
            request.setCurrentTask("Task " + i);
            contextPreservationService.captureWorkState(developerId, projectId, request);
        }

        // When
        List<WorkState.WorkStateSummary> summaries = contextPreservationService.getAvailableWorkStates(developerId);

        // Then
        assertEquals(3, summaries.size());
        // Should be sorted by priority score (descending)
        for (int i = 0; i < summaries.size() - 1; i++) {
            assertTrue(summaries.get(i).getPriorityScore() >= summaries.get(i + 1).getPriorityScore());
        }
    }

    @Test
    void testGetContextSwitchHistory() {
        // Given
        String developerId = "dev123";
        
        // Record multiple context switches
        for (int i = 0; i < 5; i++) {
            ContextPreservationService.ContextSwitchRequest request = new ContextPreservationService.ContextSwitchRequest();
            request.setSwitchType(ContextSwitchEvent.SwitchType.TASK_CHANGE);
            request.setSwitchReason(ContextSwitchEvent.SwitchReason.PLANNED);
            request.setNewTask("Task " + i);
            
            when(productivityTracker.getActiveSession(developerId)).thenReturn(Optional.empty());
            contextPreservationService.recordContextSwitch(developerId, request);
        }

        // When
        List<ContextSwitchEvent> history = contextPreservationService.getContextSwitchHistory(developerId, 3);

        // Then
        assertEquals(3, history.size());
        // Should be sorted by timestamp (descending - most recent first)
        for (int i = 0; i < history.size() - 1; i++) {
            assertTrue(history.get(i).getTimestamp().isAfter(history.get(i + 1).getTimestamp()) ||
                      history.get(i).getTimestamp().equals(history.get(i + 1).getTimestamp()));
        }
    }

    @Test
    void testAnalyzeContextSwitchingPatterns() {
        // Given
        String developerId = "dev123";
        LocalDateTime since = LocalDateTime.now().minusHours(2);
        
        // Record some context switches
        for (int i = 0; i < 3; i++) {
            ContextPreservationService.ContextSwitchRequest request = new ContextPreservationService.ContextSwitchRequest();
            request.setSwitchType(ContextSwitchEvent.SwitchType.INTERRUPTION);
            request.setSwitchReason(ContextSwitchEvent.SwitchReason.INTERRUPTION);
            request.setInterruptionCount(1);
            
            when(productivityTracker.getActiveSession(developerId)).thenReturn(Optional.empty());
            contextPreservationService.recordContextSwitch(developerId, request);
        }

        // When
        ContextSwitchAnalysis analysis = contextPreservationService.analyzeContextSwitchingPatterns(developerId, since);

        // Then
        assertNotNull(analysis);
        assertEquals(developerId, analysis.getDeveloperId());
        assertEquals(3, analysis.getTotalSwitches());
        assertTrue(analysis.getAverageSwitchesPerHour() > 0);
        assertEquals(ContextSwitchEvent.SwitchType.INTERRUPTION, analysis.getMostCommonSwitchType());
        assertEquals(ContextSwitchEvent.SwitchReason.INTERRUPTION, analysis.getMostCommonSwitchReason());
        assertTrue(analysis.getSignificantDisruptions() > 0);
        assertFalse(analysis.getRecommendations().isEmpty());
    }

    @Test
    void testAnalyzeContextSwitchingPatternsEmpty() {
        // Given
        String developerId = "dev123";
        LocalDateTime since = LocalDateTime.now().minusHours(2);

        // When - no context switches recorded
        ContextSwitchAnalysis analysis = contextPreservationService.analyzeContextSwitchingPatterns(developerId, since);

        // Then
        assertNotNull(analysis);
        assertEquals(developerId, analysis.getDeveloperId());
        assertEquals(0, analysis.getTotalSwitches());
        assertEquals(0.0, analysis.getAverageSwitchesPerHour());
        assertEquals(0.0, analysis.getTotalProductivityLoss());
        assertEquals(0, analysis.getSignificantDisruptions());
    }

    @Test
    void testSuggestWorkStateToRestore() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        String currentTask = "authentication";
        
        // Capture work states with different relevance
        ContextPreservationService.CaptureRequest request1 = new ContextPreservationService.CaptureRequest();
        request1.setCurrentTask("Implement user authentication system");
        request1.setCurrentActivity(WorkState.StateType.CODING);
        WorkState relevantState = contextPreservationService.captureWorkState(developerId, projectId, request1);
        
        ContextPreservationService.CaptureRequest request2 = new ContextPreservationService.CaptureRequest();
        request2.setCurrentTask("Fix database connection");
        request2.setCurrentActivity(WorkState.StateType.DEBUGGING);
        contextPreservationService.captureWorkState(developerId, projectId, request2);

        // When
        Optional<WorkState.WorkStateSummary> suggestion = contextPreservationService
                .suggestWorkStateToRestore(developerId, projectId, currentTask);

        // Then
        assertTrue(suggestion.isPresent());
        assertEquals(relevantState.getId(), suggestion.get().getId());
        assertTrue(suggestion.get().getCurrentTask().toLowerCase().contains("authentication"));
    }

    @Test
    void testCreateRestorationPlan() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        
        ContextPreservationService.CaptureRequest request = new ContextPreservationService.CaptureRequest();
        request.setCurrentActivity(WorkState.StateType.CODING);
        request.setOpenFiles(Arrays.asList("file1.java", "file2.java"));
        request.setActiveFile("file1.java");
        request.setCurrentTask("Implement feature X");
        request.setMentalModel("Working on user service layer");
        
        WorkState workState = contextPreservationService.captureWorkState(developerId, projectId, request);

        // When
        ContextRestorationPlan plan = contextPreservationService.createRestorationPlan(developerId, workState.getId());

        // Then
        assertNotNull(plan);
        assertEquals(developerId, plan.getDeveloperId());
        assertEquals(workState.getId(), plan.getWorkStateId());
        assertEquals(workState, plan.getWorkState());
        assertFalse(plan.getSteps().isEmpty());
        assertTrue(plan.getTotalEstimatedTimeMinutes() > 0);
        
        // Verify steps are in order
        List<RestorationStep> steps = plan.getSteps();
        for (int i = 0; i < steps.size() - 1; i++) {
            assertTrue(steps.get(i).getStepNumber() <= steps.get(i + 1).getStepNumber());
        }
    }

    @Test
    void testCreateRestorationPlanNonExistentWorkState() {
        // Given
        String developerId = "dev123";
        String nonExistentWorkStateId = "nonexistent123";

        // When
        ContextRestorationPlan plan = contextPreservationService.createRestorationPlan(developerId, nonExistentWorkStateId);

        // Then
        assertNotNull(plan);
        assertTrue(plan.getSteps().isEmpty());
        assertEquals(0, plan.getTotalEstimatedTimeMinutes());
    }

    @Test
    void testWorkStateExpiration() throws InterruptedException {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        
        ContextPreservationService.CaptureRequest request = new ContextPreservationService.CaptureRequest();
        request.setCurrentActivity(WorkState.StateType.CODING);
        
        WorkState workState = contextPreservationService.captureWorkState(developerId, projectId, request);
        
        // Verify it's initially valid
        assertTrue(workState.isValid());
        
        // Create an expired work state by manipulating the expiration time
        // (In a real scenario, this would happen after 7 days)
        WorkState expiredState = WorkState.builder()
                .id("expired123")
                .developerId(developerId)
                .projectId(projectId)
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now().minusDays(8))
                .expiresAt(LocalDateTime.now().minusDays(1)) // Expired yesterday
                .build();
        
        assertFalse(expiredState.isValid());
    }

    @Test
    void testContextSwitchProductivityImpact() {
        // Given
        String developerId = "dev123";
        
        // Test different types of context switches
        ContextPreservationService.ContextSwitchRequest interruptionRequest = new ContextPreservationService.ContextSwitchRequest();
        interruptionRequest.setSwitchType(ContextSwitchEvent.SwitchType.INTERRUPTION);
        interruptionRequest.setSwitchReason(ContextSwitchEvent.SwitchReason.INTERRUPTION);
        
        ContextPreservationService.ContextSwitchRequest plannedRequest = new ContextPreservationService.ContextSwitchRequest();
        plannedRequest.setSwitchType(ContextSwitchEvent.SwitchType.TASK_CHANGE);
        plannedRequest.setSwitchReason(ContextSwitchEvent.SwitchReason.PLANNED);
        
        when(productivityTracker.getActiveSession(developerId)).thenReturn(Optional.empty());

        // When
        ContextSwitchEvent interruptionEvent = contextPreservationService.recordContextSwitch(developerId, interruptionRequest);
        ContextSwitchEvent plannedEvent = contextPreservationService.recordContextSwitch(developerId, plannedRequest);

        // Then
        // Interruption should have more negative impact than planned switch
        assertTrue(interruptionEvent.getEstimatedProductivityImpact() < plannedEvent.getEstimatedProductivityImpact());
        assertTrue(interruptionEvent.getEstimatedRecoveryTimeMinutes() >= plannedEvent.getEstimatedRecoveryTimeMinutes());
        assertTrue(interruptionEvent.isSignificantDisruption());
    }

    @Test
    void testWorkStatePriorityScoring() {
        // Given
        String developerId = "dev123";
        String projectId = "project456";
        
        // Create work state with rich context (should have higher priority)
        ContextPreservationService.CaptureRequest richRequest = new ContextPreservationService.CaptureRequest();
        richRequest.setCurrentActivity(WorkState.StateType.CODING);
        richRequest.setOpenFiles(Arrays.asList("file1.java", "file2.java", "file3.java"));
        richRequest.setActiveFile("file1.java");
        richRequest.setCurrentTask("Complex feature implementation");
        richRequest.setCurrentGoal("Complete by end of sprint");
        richRequest.setMentalModel("Working on service layer integration");
        richRequest.setRecentActions(Arrays.asList("Added method", "Fixed bug", "Wrote test"));
        
        // Create work state with minimal context (should have lower priority)
        ContextPreservationService.CaptureRequest minimalRequest = new ContextPreservationService.CaptureRequest();
        minimalRequest.setCurrentActivity(WorkState.StateType.CODING);
        
        WorkState richState = contextPreservationService.captureWorkState(developerId, projectId, richRequest);
        WorkState minimalState = contextPreservationService.captureWorkState(developerId, projectId, minimalRequest);

        // Then
        assertTrue(richState.getPriorityScore() > minimalState.getPriorityScore());
    }

    @Test
    void testConstructorWithNullDependencies() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new ContextPreservationService(null, productivityTracker));
        assertThrows(NullPointerException.class, () -> new ContextPreservationService(contextEngine, null));
    }
}