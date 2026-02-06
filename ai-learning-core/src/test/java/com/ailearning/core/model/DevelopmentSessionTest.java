package com.ailearning.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DevelopmentSessionTest {

    @Test
    void testBuilderAndGetters() {
        // Given
        String id = "session123";
        String developerId = "dev456";
        String projectId = "project789";
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        LocalDateTime endTime = LocalDateTime.now();
        DevelopmentSession.SessionState state = DevelopmentSession.SessionState.COMPLETED;
        
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("key1", "value1");
        sessionData.put("key2", 42);

        // When
        DevelopmentSession session = DevelopmentSession.builder()
                .id(id)
                .developerId(developerId)
                .projectId(projectId)
                .startTime(startTime)
                .endTime(endTime)
                .state(state)
                .sessionData(sessionData)
                .totalKeystrokes(1000)
                .totalMouseClicks(200)
                .filesModified(5)
                .linesAdded(100)
                .linesDeleted(20)
                .contextSwitches(3)
                .focusScore(0.85)
                .interruptionCount(2)
                .idleTimeMinutes(15)
                .build();

        // Then
        assertEquals(id, session.getId());
        assertEquals(developerId, session.getDeveloperId());
        assertEquals(projectId, session.getProjectId());
        assertEquals(startTime, session.getStartTime());
        assertEquals(endTime, session.getEndTime());
        assertEquals(state, session.getState());
        assertEquals(1000, session.getTotalKeystrokes());
        assertEquals(200, session.getTotalMouseClicks());
        assertEquals(5, session.getFilesModified());
        assertEquals(100, session.getLinesAdded());
        assertEquals(20, session.getLinesDeleted());
        assertEquals(3, session.getContextSwitches());
        assertEquals(0.85, session.getFocusScore(), 0.001);
        assertEquals(2, session.getInterruptionCount());
        assertEquals(15, session.getIdleTimeMinutes());
        assertNotNull(session.getSessionData());
        assertEquals("value1", session.getSessionData().get("key1"));
        assertEquals(42, session.getSessionData().get("key2"));
    }

    @Test
    void testBuilderWithDefaults() {
        // Given
        String id = "session123";
        String developerId = "dev456";
        String projectId = "project789";
        LocalDateTime startTime = LocalDateTime.now();

        // When
        DevelopmentSession session = DevelopmentSession.builder()
                .id(id)
                .developerId(developerId)
                .projectId(projectId)
                .startTime(startTime)
                .build();

        // Then
        assertEquals(id, session.getId());
        assertEquals(developerId, session.getDeveloperId());
        assertEquals(projectId, session.getProjectId());
        assertEquals(startTime, session.getStartTime());
        assertNull(session.getEndTime());
        assertEquals(DevelopmentSession.SessionState.ACTIVE, session.getState());
        assertEquals(0, session.getTotalKeystrokes());
        assertEquals(0, session.getTotalMouseClicks());
        assertEquals(0, session.getFilesModified());
        assertEquals(0, session.getLinesAdded());
        assertEquals(0, session.getLinesDeleted());
        assertEquals(0, session.getContextSwitches());
        assertEquals(0.0, session.getFocusScore(), 0.001);
        assertEquals(0, session.getInterruptionCount());
        assertEquals(0, session.getIdleTimeMinutes());
        assertTrue(session.getActivities().isEmpty());
        assertTrue(session.getSessionData().isEmpty());
    }

    @Test
    void testValidation() {
        // Test null ID
        assertThrows(NullPointerException.class, () ->
                DevelopmentSession.builder()
                        .id(null)
                        .developerId("dev123")
                        .projectId("project123")
                        .startTime(LocalDateTime.now())
                        .build());

        // Test null developer ID
        assertThrows(NullPointerException.class, () ->
                DevelopmentSession.builder()
                        .id("session123")
                        .developerId(null)
                        .projectId("project123")
                        .startTime(LocalDateTime.now())
                        .build());

        // Test null project ID
        assertThrows(NullPointerException.class, () ->
                DevelopmentSession.builder()
                        .id("session123")
                        .developerId("dev123")
                        .projectId(null)
                        .startTime(LocalDateTime.now())
                        .build());

        // Test null start time
        assertThrows(NullPointerException.class, () ->
                DevelopmentSession.builder()
                        .id("session123")
                        .developerId("dev123")
                        .projectId("project123")
                        .startTime(null)
                        .build());
    }

    @Test
    void testNegativeValueHandling() {
        // When
        DevelopmentSession session = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now())
                .totalKeystrokes(-100) // Negative value
                .totalMouseClicks(-50) // Negative value
                .filesModified(-5) // Negative value
                .linesAdded(-10) // Negative value
                .linesDeleted(-5) // Negative value
                .contextSwitches(-3) // Negative value
                .focusScore(-0.5) // Negative value
                .interruptionCount(-2) // Negative value
                .idleTimeMinutes(-15) // Negative value
                .build();

        // Then - negative values should be converted to 0
        assertEquals(0, session.getTotalKeystrokes());
        assertEquals(0, session.getTotalMouseClicks());
        assertEquals(0, session.getFilesModified());
        assertEquals(0, session.getLinesAdded());
        assertEquals(0, session.getLinesDeleted());
        assertEquals(0, session.getContextSwitches());
        assertEquals(0.0, session.getFocusScore(), 0.001);
        assertEquals(0, session.getInterruptionCount());
        assertEquals(0, session.getIdleTimeMinutes());
    }

    @Test
    void testFocusScoreBounds() {
        // Test focus score > 1.0
        DevelopmentSession session1 = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now())
                .focusScore(1.5) // > 1.0
                .build();

        assertEquals(1.0, session1.getFocusScore(), 0.001);

        // Test focus score < 0.0
        DevelopmentSession session2 = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now())
                .focusScore(-0.5) // < 0.0
                .build();

        assertEquals(0.0, session2.getFocusScore(), 0.001);
    }

    @Test
    void testGetDurationMinutes() {
        // Test with end time
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();
        
        DevelopmentSession completedSession = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(start)
                .endTime(end)
                .build();

        long duration = completedSession.getDurationMinutes();
        assertTrue(duration >= 119 && duration <= 121); // Approximately 2 hours

        // Test without end time (active session)
        DevelopmentSession activeSession = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now().minusMinutes(30))
                .build();

        long activeDuration = activeSession.getDurationMinutes();
        assertTrue(activeDuration >= 29 && activeDuration <= 31); // Approximately 30 minutes
    }

    @Test
    void testGetActiveCodingMinutes() {
        // Given
        DevelopmentSession session = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now().minusHours(2)) // 120 minutes total
                .endTime(LocalDateTime.now())
                .idleTimeMinutes(20) // 20 minutes idle
                .activities(Arrays.asList(
                        new DevelopmentSession.SessionActivity(
                                DevelopmentSession.ActivityType.BREAK,
                                LocalDateTime.now().minusMinutes(30),
                                LocalDateTime.now().minusMinutes(20),
                                "Coffee break",
                                null
                        )
                )) // 10 minutes break
                .build();

        // When
        long activeCoding = session.getActiveCodingMinutes();

        // Then
        // Total (120) - Idle (20) - Break (10) = 90 minutes
        assertEquals(90, activeCoding);
    }

    @Test
    void testGetBreakTimeMinutes() {
        // Given
        DevelopmentSession.SessionActivity break1 = new DevelopmentSession.SessionActivity(
                DevelopmentSession.ActivityType.BREAK,
                LocalDateTime.now().minusMinutes(60),
                LocalDateTime.now().minusMinutes(50),
                "Coffee break",
                null
        );

        DevelopmentSession.SessionActivity break2 = new DevelopmentSession.SessionActivity(
                DevelopmentSession.ActivityType.BREAK,
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().minusMinutes(25),
                "Lunch break",
                null
        );

        DevelopmentSession.SessionActivity coding = new DevelopmentSession.SessionActivity(
                DevelopmentSession.ActivityType.CODING,
                LocalDateTime.now().minusMinutes(25),
                LocalDateTime.now(),
                "Coding session",
                null
        );

        DevelopmentSession session = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now().minusHours(1))
                .activities(Arrays.asList(break1, break2, coding))
                .build();

        // When
        long breakTime = session.getBreakTimeMinutes();

        // Then
        assertEquals(15, breakTime); // 10 + 5 minutes of breaks
    }

    @Test
    void testGetCurrentActivity() {
        // Test active session with activities
        DevelopmentSession.SessionActivity currentActivity = new DevelopmentSession.SessionActivity(
                DevelopmentSession.ActivityType.CODING,
                LocalDateTime.now().minusMinutes(30),
                null, // Ongoing activity
                "Current coding session",
                null
        );

        DevelopmentSession activeSession = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now().minusHours(1))
                .state(DevelopmentSession.SessionState.ACTIVE)
                .activities(Arrays.asList(currentActivity))
                .build();

        assertEquals(currentActivity, activeSession.getCurrentActivity());

        // Test completed session
        DevelopmentSession completedSession = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now().minusHours(1))
                .state(DevelopmentSession.SessionState.COMPLETED)
                .activities(Arrays.asList(currentActivity))
                .build();

        assertNull(completedSession.getCurrentActivity());

        // Test active session without activities
        DevelopmentSession emptyActiveSession = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now().minusHours(1))
                .state(DevelopmentSession.SessionState.ACTIVE)
                .build();

        assertNull(emptyActiveSession.getCurrentActivity());
    }

    @Test
    void testGetProductivityScore() {
        // Test session with good metrics
        DevelopmentSession productiveSession = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now().minusHours(2)) // 120 minutes
                .endTime(LocalDateTime.now())
                .linesAdded(100)
                .linesDeleted(10)
                .filesModified(5)
                .focusScore(0.9)
                .idleTimeMinutes(10) // Low idle time
                .build();

        double productivityScore = productiveSession.getProductivityScore();
        assertTrue(productivityScore > 0.0);
        assertTrue(productivityScore <= 1.0);

        // Test session with zero duration
        DevelopmentSession zeroSession = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now()) // Same time = 0 duration
                .build();

        assertEquals(0.0, zeroSession.getProductivityScore(), 0.001);
    }

    @Test
    void testSessionActivity() {
        // Given
        DevelopmentSession.ActivityType type = DevelopmentSession.ActivityType.CODING;
        LocalDateTime start = LocalDateTime.now().minusMinutes(30);
        LocalDateTime end = LocalDateTime.now();
        String description = "Working on feature X";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("file", "test.java");
        metadata.put("lines", 50);

        // When
        DevelopmentSession.SessionActivity activity = new DevelopmentSession.SessionActivity(
                type, start, end, description, metadata);

        // Then
        assertEquals(type, activity.getType());
        assertEquals(start, activity.getStartTime());
        assertEquals(end, activity.getEndTime());
        assertEquals(description, activity.getDescription());
        assertEquals(30, activity.getDurationMinutes());
        assertEquals("test.java", activity.getMetadata().get("file"));
        assertEquals(50, activity.getMetadata().get("lines"));
    }

    @Test
    void testSessionActivityWithoutEndTime() {
        // Given
        DevelopmentSession.ActivityType type = DevelopmentSession.ActivityType.CODING;
        LocalDateTime start = LocalDateTime.now().minusMinutes(15);

        // When
        DevelopmentSession.SessionActivity activity = new DevelopmentSession.SessionActivity(
                type, start, null, "Ongoing activity", null);

        // Then
        assertEquals(type, activity.getType());
        assertEquals(start, activity.getStartTime());
        assertNull(activity.getEndTime());
        long duration = activity.getDurationMinutes();
        assertTrue(duration >= 14 && duration <= 16); // Approximately 15 minutes
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        String id = "session123";
        DevelopmentSession session1 = DevelopmentSession.builder()
                .id(id)
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now())
                .build();

        DevelopmentSession session2 = DevelopmentSession.builder()
                .id(id)
                .developerId("dev456") // Different developer
                .projectId("project456") // Different project
                .startTime(LocalDateTime.now().minusHours(1)) // Different time
                .build();

        DevelopmentSession session3 = DevelopmentSession.builder()
                .id("different123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now())
                .build();

        // Then
        assertEquals(session1, session2); // Same ID
        assertNotEquals(session1, session3); // Different ID
        assertEquals(session1.hashCode(), session2.hashCode()); // Same ID
        assertNotEquals(session1.hashCode(), session3.hashCode()); // Different ID
    }

    @Test
    void testToString() {
        // Given
        DevelopmentSession session = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev456")
                .projectId("project789")
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .state(DevelopmentSession.SessionState.COMPLETED)
                .build();

        // When
        String toString = session.toString();

        // Then
        assertTrue(toString.contains("session123"));
        assertTrue(toString.contains("dev456"));
        assertTrue(toString.contains("project789"));
        assertTrue(toString.contains("COMPLETED"));
        assertTrue(toString.contains("60min")); // Duration
    }

    @Test
    void testDataImmutability() {
        // Given
        Map<String, Object> originalSessionData = new HashMap<>();
        originalSessionData.put("key1", "value1");

        DevelopmentSession session = DevelopmentSession.builder()
                .id("session123")
                .developerId("dev123")
                .projectId("project123")
                .startTime(LocalDateTime.now())
                .sessionData(originalSessionData)
                .build();

        // When - modify original data
        originalSessionData.put("key2", "value2");

        // Then - session data should not be affected
        assertFalse(session.getSessionData().containsKey("key2"));

        // When - modify returned data
        Map<String, Object> returnedData = session.getSessionData();
        returnedData.put("key3", "value3");

        // Then - session internal data should not be affected
        assertFalse(session.getSessionData().containsKey("key3"));
    }
}