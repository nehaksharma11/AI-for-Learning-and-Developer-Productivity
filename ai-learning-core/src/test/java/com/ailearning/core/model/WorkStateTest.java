package com.ailearning.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkStateTest {

    @Test
    void testBuilderAndGetters() {
        // Given
        String id = "ws123";
        String developerId = "dev456";
        String projectId = "project789";
        WorkState.StateType currentActivity = WorkState.StateType.CODING;
        LocalDateTime capturedAt = LocalDateTime.now().minusMinutes(30);
        
        Map<String, Object> ideState = new HashMap<>();
        ideState.put("theme", "dark");
        ideState.put("fontSize", 14);

        // When
        WorkState workState = WorkState.builder()
                .id(id)
                .developerId(developerId)
                .projectId(projectId)
                .currentActivity(currentActivity)
                .capturedAt(capturedAt)
                .openFiles(Arrays.asList("file1.java", "file2.java"))
                .activeFile("file1.java")
                .cursorPosition(150)
                .selectedText("public class Test")
                .ideState(ideState)
                .currentTask("Implement authentication")
                .currentGoal("Complete login feature")
                .mentalModel("Working on user service layer")
                .build();

        // Then
        assertEquals(id, workState.getId());
        assertEquals(developerId, workState.getDeveloperId());
        assertEquals(projectId, workState.getProjectId());
        assertEquals(currentActivity, workState.getCurrentActivity());
        assertEquals(capturedAt, workState.getCapturedAt());
        assertEquals(Arrays.asList("file1.java", "file2.java"), workState.getOpenFiles());
        assertEquals("file1.java", workState.getActiveFile());
        assertEquals(150, workState.getCursorPosition());
        assertEquals("public class Test", workState.getSelectedText());
        assertEquals("Implement authentication", workState.getCurrentTask());
        assertEquals("Complete login feature", workState.getCurrentGoal());
        assertEquals("Working on user service layer", workState.getMentalModel());
        assertNotNull(workState.getIdeState());
        assertEquals("dark", workState.getIdeState().get("theme"));
        assertEquals(14, workState.getIdeState().get("fontSize"));
    }

    @Test
    void testBuilderWithDefaults() {
        // Given
        String id = "ws123";
        String developerId = "dev456";
        String projectId = "project789";
        LocalDateTime capturedAt = LocalDateTime.now();

        // When
        WorkState workState = WorkState.builder()
                .id(id)
                .developerId(developerId)
                .projectId(projectId)
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(capturedAt)
                .build();

        // Then
        assertEquals(id, workState.getId());
        assertEquals(developerId, workState.getDeveloperId());
        assertEquals(projectId, workState.getProjectId());
        assertEquals(WorkState.StateType.CODING, workState.getCurrentActivity());
        assertEquals(capturedAt, workState.getCapturedAt());
        assertTrue(workState.getOpenFiles().isEmpty());
        assertNull(workState.getActiveFile());
        assertEquals(0, workState.getCursorPosition());
        assertNull(workState.getSelectedText());
        assertTrue(workState.getIdeState().isEmpty());
        assertNull(workState.getCurrentTask());
        assertNull(workState.getCurrentGoal());
        assertTrue(workState.getRecentActions().isEmpty());
        assertTrue(workState.getRelevantCodeReferences().isEmpty());
        assertTrue(workState.getRecentSearchQueries().isEmpty());
        assertNull(workState.getActiveLearningSession());
        assertTrue(workState.getRecentLearningTopics().isEmpty());
        assertNull(workState.getActiveProductivitySession());
        assertTrue(workState.getSessionMetrics().isEmpty());
        assertNull(workState.getMentalModel());
        assertTrue(workState.getDeveloperNotes().isEmpty());
        assertTrue(workState.getRestorationHints().isEmpty());
        assertNotNull(workState.getExpiresAt());
    }

    @Test
    void testValidation() {
        // Test null ID
        assertThrows(NullPointerException.class, () ->
                WorkState.builder()
                        .id(null)
                        .developerId("dev123")
                        .projectId("project123")
                        .currentActivity(WorkState.StateType.CODING)
                        .capturedAt(LocalDateTime.now())
                        .build());

        // Test null developer ID
        assertThrows(NullPointerException.class, () ->
                WorkState.builder()
                        .id("ws123")
                        .developerId(null)
                        .projectId("project123")
                        .currentActivity(WorkState.StateType.CODING)
                        .capturedAt(LocalDateTime.now())
                        .build());

        // Test null project ID
        assertThrows(NullPointerException.class, () ->
                WorkState.builder()
                        .id("ws123")
                        .developerId("dev123")
                        .projectId(null)
                        .currentActivity(WorkState.StateType.CODING)
                        .capturedAt(LocalDateTime.now())
                        .build());

        // Test null current activity
        assertThrows(NullPointerException.class, () ->
                WorkState.builder()
                        .id("ws123")
                        .developerId("dev123")
                        .projectId("project123")
                        .currentActivity(null)
                        .capturedAt(LocalDateTime.now())
                        .build());

        // Test null captured at
        assertThrows(NullPointerException.class, () ->
                WorkState.builder()
                        .id("ws123")
                        .developerId("dev123")
                        .projectId("project123")
                        .currentActivity(WorkState.StateType.CODING)
                        .capturedAt(null)
                        .build());
    }

    @Test
    void testNegativeValueHandling() {
        // When
        WorkState workState = WorkState.builder()
                .id("ws123")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now())
                .cursorPosition(-100) // Negative value
                .build();

        // Then - negative values should be converted to 0
        assertEquals(0, workState.getCursorPosition());
    }

    @Test
    void testIsValid() {
        // Test valid work state (not expired)
        WorkState validState = WorkState.builder()
                .id("ws123")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1)) // Expires tomorrow
                .build();

        assertTrue(validState.isValid());

        // Test expired work state
        WorkState expiredState = WorkState.builder()
                .id("ws123")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now().minusDays(8))
                .expiresAt(LocalDateTime.now().minusDays(1)) // Expired yesterday
                .build();

        assertFalse(expiredState.isValid());
    }

    @Test
    void testGetAgeMinutes() {
        // Given
        LocalDateTime capturedAt = LocalDateTime.now().minusMinutes(45);
        
        WorkState workState = WorkState.builder()
                .id("ws123")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(capturedAt)
                .build();

        // When
        long ageMinutes = workState.getAgeMinutes();

        // Then
        assertTrue(ageMinutes >= 44 && ageMinutes <= 46); // Approximately 45 minutes
    }

    @Test
    void testGetPriorityScore() {
        // Test recent work state with rich context (should have high priority)
        WorkState recentRichState = WorkState.builder()
                .id("ws123")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING) // High importance activity
                .capturedAt(LocalDateTime.now().minusMinutes(5)) // Very recent
                .openFiles(Arrays.asList("file1.java", "file2.java", "file3.java"))
                .activeFile("file1.java")
                .selectedText("some selected code")
                .currentTask("Important task")
                .currentGoal("Critical goal")
                .recentActions(Arrays.asList("action1", "action2", "action3"))
                .build();

        // Test old work state with minimal context (should have low priority)
        WorkState oldMinimalState = WorkState.builder()
                .id("ws456")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.MEETING) // Lower importance activity
                .capturedAt(LocalDateTime.now().minusHours(5)) // Older
                .build();

        // Then
        assertTrue(recentRichState.getPriorityScore() > oldMinimalState.getPriorityScore());
        assertTrue(recentRichState.getPriorityScore() >= 0.0);
        assertTrue(recentRichState.getPriorityScore() <= 1.0);
        assertTrue(oldMinimalState.getPriorityScore() >= 0.0);
        assertTrue(oldMinimalState.getPriorityScore() <= 1.0);
    }

    @Test
    void testCreateSummary() {
        // Given
        WorkState workState = WorkState.builder()
                .id("ws123")
                .developerId("dev456")
                .projectId("project789")
                .currentActivity(WorkState.StateType.DEBUGGING)
                .capturedAt(LocalDateTime.now().minusMinutes(30))
                .activeFile("test.java")
                .currentTask("Fix authentication bug")
                .openFiles(Arrays.asList("file1.java", "file2.java"))
                .build();

        // When
        WorkState.WorkStateSummary summary = workState.createSummary();

        // Then
        assertNotNull(summary);
        assertEquals(workState.getId(), summary.getId());
        assertEquals(workState.getDeveloperId(), summary.getDeveloperId());
        assertEquals(workState.getProjectId(), summary.getProjectId());
        assertEquals(workState.getCurrentActivity(), summary.getCurrentActivity());
        assertEquals(workState.getCapturedAt(), summary.getCapturedAt());
        assertEquals(workState.getActiveFile(), summary.getActiveFile());
        assertEquals(workState.getCurrentTask(), summary.getCurrentTask());
        assertEquals(2, summary.getOpenFileCount());
        assertTrue(summary.getAgeMinutes() >= 29 && summary.getAgeMinutes() <= 31);
        assertEquals(workState.getPriorityScore(), summary.getPriorityScore());
        assertEquals(workState.isValid(), summary.isValid());
    }

    @Test
    void testWorkStateSummaryToString() {
        // Given
        WorkState workState = WorkState.builder()
                .id("ws123")
                .developerId("dev456")
                .projectId("project789")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now().minusMinutes(15))
                .activeFile("main.java")
                .currentTask("Implement feature")
                .build();

        WorkState.WorkStateSummary summary = workState.createSummary();

        // When
        String toString = summary.toString();

        // Then
        assertTrue(toString.contains("ws123"));
        assertTrue(toString.contains("CODING"));
        assertTrue(toString.contains("Implement feature"));
        assertTrue(toString.contains("main.java"));
        assertTrue(toString.contains("15min"));
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        String id = "ws123";
        WorkState workState1 = WorkState.builder()
                .id(id)
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now())
                .build();

        WorkState workState2 = WorkState.builder()
                .id(id)
                .developerId("dev456") // Different developer
                .projectId("project456") // Different project
                .currentActivity(WorkState.StateType.DEBUGGING) // Different activity
                .capturedAt(LocalDateTime.now().minusHours(1)) // Different time
                .build();

        WorkState workState3 = WorkState.builder()
                .id("different123")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now())
                .build();

        // Then
        assertEquals(workState1, workState2); // Same ID
        assertNotEquals(workState1, workState3); // Different ID
        assertEquals(workState1.hashCode(), workState2.hashCode()); // Same ID
        assertNotEquals(workState1.hashCode(), workState3.hashCode()); // Different ID
    }

    @Test
    void testToString() {
        // Given
        WorkState workState = WorkState.builder()
                .id("ws123")
                .developerId("dev456")
                .projectId("project789")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now().minusMinutes(20))
                .activeFile("test.java")
                .build();

        // When
        String toString = workState.toString();

        // Then
        assertTrue(toString.contains("ws123"));
        assertTrue(toString.contains("dev456"));
        assertTrue(toString.contains("project789"));
        assertTrue(toString.contains("CODING"));
        assertTrue(toString.contains("test.java"));
        assertTrue(toString.contains("20min"));
    }

    @Test
    void testDataImmutability() {
        // Given
        Map<String, Object> originalIdeState = new HashMap<>();
        originalIdeState.put("key1", "value1");

        WorkState workState = WorkState.builder()
                .id("ws123")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now())
                .ideState(originalIdeState)
                .build();

        // When - modify original data
        originalIdeState.put("key2", "value2");

        // Then - work state data should not be affected
        assertFalse(workState.getIdeState().containsKey("key2"));

        // When - modify returned data
        Map<String, Object> returnedIdeState = workState.getIdeState();
        returnedIdeState.put("key3", "value3");

        // Then - work state internal data should not be affected
        assertFalse(workState.getIdeState().containsKey("key3"));
    }

    @Test
    void testDefaultExpirationTime() {
        // Given
        WorkState workState = WorkState.builder()
                .id("ws123")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(LocalDateTime.now())
                .build();

        // Then - should expire in 7 days by default
        LocalDateTime expectedExpiration = LocalDateTime.now().plusDays(7);
        assertTrue(workState.getExpiresAt().isAfter(expectedExpiration.minusMinutes(1)));
        assertTrue(workState.getExpiresAt().isBefore(expectedExpiration.plusMinutes(1)));
    }

    @Test
    void testActivityImportanceInPriorityScore() {
        LocalDateTime now = LocalDateTime.now();
        
        // Create work states with different activities but same other conditions
        WorkState codingState = WorkState.builder()
                .id("ws1")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.CODING)
                .capturedAt(now)
                .build();

        WorkState meetingState = WorkState.builder()
                .id("ws2")
                .developerId("dev123")
                .projectId("project123")
                .currentActivity(WorkState.StateType.MEETING)
                .capturedAt(now)
                .build();

        // Then - coding should have higher priority than meeting
        assertTrue(codingState.getPriorityScore() > meetingState.getPriorityScore());
    }
}