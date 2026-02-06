package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.RetentionSchedulingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultRetentionSchedulingService.
 */
class DefaultRetentionSchedulingServiceTest {

    private RetentionSchedulingService retentionSchedulingService;

    @BeforeEach
    void setUp() {
        retentionSchedulingService = new DefaultRetentionSchedulingService();
    }

    @Test
    void testScheduleFollowUp_CreatesMultipleFollowUpSessions() throws Exception {
        // Arrange
        String developerId = "dev-123";
        LearningSession completedSession = createCompletedSession(developerId, "Java Streams", 0.8);

        // Act
        CompletableFuture<List<ScheduledSession>> future = retentionSchedulingService.scheduleFollowUp(developerId, completedSession);
        List<ScheduledSession> followUps = future.get();

        // Assert
        assertNotNull(followUps);
        assertFalse(followUps.isEmpty());
        assertTrue(followUps.size() >= 1, "Should schedule at least one follow-up session");

        // Verify sessions are scheduled in the future
        LocalDateTime now = LocalDateTime.now();
        for (ScheduledSession session : followUps) {
            assertTrue(session.getScheduledTime().isAfter(now));
            assertEquals(developerId, session.getDeveloperId());
            assertEquals("Java Streams", session.getTopic());
            assertEquals("retention-review", session.getSessionType());
        }
    }

    @Test
    void testScheduleFollowUp_WithPoorPerformance_SchedulesEarlierReviews() throws Exception {
        // Arrange
        String developerId = "dev-456";
        LearningSession poorSession = createCompletedSession(developerId, "Algorithms", 0.3);

        // Act
        CompletableFuture<List<ScheduledSession>> future = retentionSchedulingService.scheduleFollowUp(developerId, poorSession);
        List<ScheduledSession>> followUps = future.get();

        // Assert
        assertNotNull(followUps);
        assertFalse(followUps.isEmpty());

        // First follow-up should be high priority for poor performance
        ScheduledSession firstFollowUp = followUps.get(0);
        assertEquals(ScheduledSession.Priority.HIGH, firstFollowUp.getPriority());
    }

    @Test
    void testScheduleFollowUp_WithExcellentPerformance_SchedulesLaterReviews() throws Exception {
        // Arrange
        String developerId = "dev-789";
        LearningSession excellentSession = createCompletedSession(developerId, "Design Patterns", 0.95);

        // Act
        CompletableFuture<List<ScheduledSession>> future = retentionSchedulingService.scheduleFollowUp(developerId, excellentSession);
        List<ScheduledSession> followUps = future.get();

        // Assert
        assertNotNull(followUps);
        assertFalse(followUps.isEmpty());

        // Verify sessions are scheduled with appropriate intervals
        for (int i = 0; i < followUps.size() - 1; i++) {
            LocalDateTime current = followUps.get(i).getScheduledTime();
            LocalDateTime next = followUps.get(i + 1).getScheduledTime();
            assertTrue(next.isAfter(current), "Follow-ups should be in chronological order");
        }
    }

    @Test
    void testAssessRetention_CalculatesRetentionScore() throws Exception {
        // Arrange
        String developerId = "dev-101";
        String topic = "Spring Boot";
        LearningSession followUpSession = createCompletedSession(developerId, topic, 0.75);

        // Act
        CompletableFuture<RetentionAssessment> future = retentionSchedulingService.assessRetention(developerId, topic, followUpSession);
        RetentionAssessment assessment = future.get();

        // Assert
        assertNotNull(assessment);
        assertEquals(developerId, assessment.getDeveloperId());
        assertEquals(topic, assessment.getTopic());
        assertTrue(assessment.getRetentionScore() >= 0.0 && assessment.getRetentionScore() <= 1.0);
        assertTrue(assessment.getRecallAccuracy() >= 0.0 && assessment.getRecallAccuracy() <= 1.0);
        assertNotNull(assessment.getRetentionLevel());
        assertNotNull(assessment.getRecommendedAction());
        assertTrue(assessment.getRecommendedNextReviewDays() > 0);
    }

    @Test
    void testAssessRetention_WithPoorPerformance_RecommendsShorterInterval() throws Exception {
        // Arrange
        String developerId = "dev-202";
        String topic = "Microservices";
        LearningSession poorFollowUp = createCompletedSession(developerId, topic, 0.3);

        // Act
        CompletableFuture<RetentionAssessment> future = retentionSchedulingService.assessRetention(developerId, topic, poorFollowUp);
        RetentionAssessment assessment = future.get();

        // Assert
        assertNotNull(assessment);
        assertTrue(assessment.needsImmediateReview() || assessment.getRecommendedNextReviewDays() <= 3,
                "Poor performance should recommend short review interval");
        assertNotNull(assessment.getWeaknessAreas());
    }

    @Test
    void testAssessRetention_WithExcellentPerformance_RecommendsLongerInterval() throws Exception {
        // Arrange
        String developerId = "dev-303";
        String topic = "Docker";
        LearningSession excellentFollowUp = createCompletedSession(developerId, topic, 0.95);

        // Act
        CompletableFuture<RetentionAssessment> future = retentionSchedulingService.assessRetention(developerId, topic, excellentFollowUp);
        RetentionAssessment assessment = future.get();

        // Assert
        assertNotNull(assessment);
        assertTrue(assessment.isRetentionStrong());
        assertTrue(assessment.getRecommendedNextReviewDays() >= 7,
                "Excellent performance should recommend longer review interval");
        assertNotNull(assessment.getStrengthAreas());
    }

    @Test
    void testAdjustSchedule_WithPoorRetention_SchedulesImmediateReview() throws Exception {
        // Arrange
        String developerId = "dev-404";
        RetentionAssessment poorAssessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId(developerId)
                .topic("Kubernetes")
                .retentionScore(0.3)
                .recallAccuracy(0.35)
                .daysSinceLastReview(7)
                .totalReviewCount(2)
                .retentionLevel(RetentionAssessment.RetentionLevel.POOR)
                .recommendedNextReviewDays(1)
                .build();

        // Act
        CompletableFuture<LearningSchedule> future = retentionSchedulingService.adjustSchedule(developerId, poorAssessment);
        LearningSchedule schedule = future.get();

        // Assert
        assertNotNull(schedule);
        assertEquals(developerId, schedule.getDeveloperId());
        assertFalse(schedule.getScheduledSessions().isEmpty());

        // Should have immediate review session
        boolean hasImmediateReview = schedule.getScheduledSessions().stream()
                .anyMatch(s -> s.getSessionType().equals("immediate-review"));
        assertTrue(hasImmediateReview, "Poor retention should schedule immediate review");
    }

    @Test
    void testAdjustSchedule_WithGoodRetention_SchedulesRegularReview() throws Exception {
        // Arrange
        String developerId = "dev-505";
        RetentionAssessment goodAssessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId(developerId)
                .topic("REST APIs")
                .retentionScore(0.75)
                .recallAccuracy(0.8)
                .daysSinceLastReview(14)
                .totalReviewCount(3)
                .retentionLevel(RetentionAssessment.RetentionLevel.GOOD)
                .recommendedNextReviewDays(14)
                .build();

        // Act
        CompletableFuture<LearningSchedule> future = retentionSchedulingService.adjustSchedule(developerId, goodAssessment);
        LearningSchedule schedule = future.get();

        // Assert
        assertNotNull(schedule);
        assertFalse(schedule.getScheduledSessions().isEmpty());

        // Should have scheduled review
        boolean hasScheduledReview = schedule.getScheduledSessions().stream()
                .anyMatch(s -> s.getSessionType().equals("scheduled-review"));
        assertTrue(hasScheduledReview, "Good retention should schedule regular review");
    }

    @Test
    void testGetNextReviewTime_CalculatesCorrectInterval() throws Exception {
        // Arrange
        String topic = "GraphQL";
        LocalDateTime lastReview = LocalDateTime.now().minusDays(7);
        double performanceScore = 0.8;
        int reviewCount = 2;

        // Act
        CompletableFuture<LocalDateTime> future = retentionSchedulingService.getNextReviewTime(
                topic, lastReview, performanceScore, reviewCount);
        LocalDateTime nextReview = future.get();

        // Assert
        assertNotNull(nextReview);
        assertTrue(nextReview.isAfter(lastReview), "Next review should be after last review");
    }

    @Test
    void testCalculateNextInterval_SM2Algorithm() {
        // Test initial interval
        int interval1 = retentionSchedulingService.calculateNextInterval(0, 2.5, 0.8);
        assertEquals(1, interval1, "First interval should be 1 day");

        // Test second interval
        int interval2 = retentionSchedulingService.calculateNextInterval(1, 2.5, 0.8);
        assertEquals(6, interval2, "Second interval should be 6 days");

        // Test subsequent intervals
        int interval3 = retentionSchedulingService.calculateNextInterval(6, 2.5, 0.8);
        assertTrue(interval3 > 6, "Subsequent intervals should increase");

        // Test poor performance resets interval
        int intervalReset = retentionSchedulingService.calculateNextInterval(30, 2.5, 0.4);
        assertEquals(1, intervalReset, "Poor performance should reset to initial interval");
    }

    @Test
    void testCalculateNextInterval_WithDifferentEasinessFactors() {
        int previousInterval = 10;
        double performanceScore = 0.8;

        // Higher easiness factor should result in longer interval
        int interval1 = retentionSchedulingService.calculateNextInterval(previousInterval, 2.0, performanceScore);
        int interval2 = retentionSchedulingService.calculateNextInterval(previousInterval, 2.5, performanceScore);
        int interval3 = retentionSchedulingService.calculateNextInterval(previousInterval, 3.0, performanceScore);

        assertTrue(interval2 > interval1, "Higher EF should give longer interval");
        assertTrue(interval3 > interval2, "Higher EF should give longer interval");
    }

    @Test
    void testCalculateNextInterval_CapsAtMaximum() {
        // Test that interval doesn't exceed maximum (180 days)
        int largeInterval = retentionSchedulingService.calculateNextInterval(100, 3.0, 0.95);
        assertTrue(largeInterval <= 180, "Interval should be capped at 180 days");
    }

    @Test
    void testGetPendingFollowUps_ReturnsEmptyList() throws Exception {
        // Arrange
        String developerId = "dev-606";

        // Act
        CompletableFuture<List<ScheduledSession>> future = retentionSchedulingService.getPendingFollowUps(developerId);
        List<ScheduledSession> pending = future.get();

        // Assert
        assertNotNull(pending);
        // Current implementation returns empty list
        assertTrue(pending.isEmpty());
    }

    // Helper methods

    private LearningSession createCompletedSession(String developerId, String topic, double performanceScore) {
        List<LearningOutcome> outcomes = List.of(
                LearningOutcome.builder()
                        .objective("Understand " + topic)
                        .achieved(performanceScore >= 0.6)
                        .achievementScore(performanceScore)
                        .build()
        );

        return LearningSession.builder()
                .id(UUID.randomUUID().toString())
                .developerId(developerId)
                .topic(topic)
                .content(List.of())
                .outcomes(outcomes)
                .status(LearningSession.SessionStatus.COMPLETED)
                .difficultyLevel(0.5)
                .build();
    }
}
