package com.ailearning.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RetentionAssessment model.
 */
class RetentionAssessmentTest {

    @Test
    void testBuilder_CreatesValidAssessment() {
        // Arrange & Act
        RetentionAssessment assessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-123")
                .topic("Java Concurrency")
                .retentionScore(0.75)
                .recallAccuracy(0.8)
                .daysSinceLastReview(7)
                .totalReviewCount(3)
                .retentionLevel(RetentionAssessment.RetentionLevel.GOOD)
                .strengthAreas(List.of("Thread safety", "Synchronization"))
                .weaknessAreas(List.of("Deadlock prevention"))
                .recommendedAction("Continue regular reviews")
                .recommendedNextReviewDays(14)
                .build();

        // Assert
        assertNotNull(assessment);
        assertEquals("dev-123", assessment.getDeveloperId());
        assertEquals("Java Concurrency", assessment.getTopic());
        assertEquals(0.75, assessment.getRetentionScore(), 0.001);
        assertEquals(0.8, assessment.getRecallAccuracy(), 0.001);
        assertEquals(7, assessment.getDaysSinceLastReview());
        assertEquals(3, assessment.getTotalReviewCount());
        assertEquals(RetentionAssessment.RetentionLevel.GOOD, assessment.getRetentionLevel());
        assertEquals(2, assessment.getStrengthAreas().size());
        assertEquals(1, assessment.getWeaknessAreas().size());
        assertEquals(14, assessment.getRecommendedNextReviewDays());
    }

    @Test
    void testRetentionScore_ClampedToValidRange() {
        // Test upper bound
        RetentionAssessment highScore = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-456")
                .topic("Test")
                .retentionScore(1.5) // Above 1.0
                .build();
        assertEquals(1.0, highScore.getRetentionScore(), 0.001);

        // Test lower bound
        RetentionAssessment lowScore = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-789")
                .topic("Test")
                .retentionScore(-0.5) // Below 0.0
                .build();
        assertEquals(0.0, lowScore.getRetentionScore(), 0.001);
    }

    @Test
    void testRecallAccuracy_ClampedToValidRange() {
        // Test upper bound
        RetentionAssessment highAccuracy = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-101")
                .topic("Test")
                .recallAccuracy(2.0) // Above 1.0
                .build();
        assertEquals(1.0, highAccuracy.getRecallAccuracy(), 0.001);

        // Test lower bound
        RetentionAssessment lowAccuracy = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-202")
                .topic("Test")
                .recallAccuracy(-1.0) // Below 0.0
                .build();
        assertEquals(0.0, lowAccuracy.getRecallAccuracy(), 0.001);
    }

    @Test
    void testNeedsImmediateReview_WithPoorRetention() {
        RetentionAssessment poorAssessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-303")
                .topic("Test")
                .retentionScore(0.3)
                .retentionLevel(RetentionAssessment.RetentionLevel.POOR)
                .build();

        assertTrue(poorAssessment.needsImmediateReview());
    }

    @Test
    void testNeedsImmediateReview_WithGoodRetention() {
        RetentionAssessment goodAssessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-404")
                .topic("Test")
                .retentionScore(0.75)
                .retentionLevel(RetentionAssessment.RetentionLevel.GOOD)
                .build();

        assertFalse(goodAssessment.needsImmediateReview());
    }

    @Test
    void testIsRetentionStrong_WithExcellentRetention() {
        RetentionAssessment excellentAssessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-505")
                .topic("Test")
                .retentionScore(0.9)
                .retentionLevel(RetentionAssessment.RetentionLevel.EXCELLENT)
                .build();

        assertTrue(excellentAssessment.isRetentionStrong());
    }

    @Test
    void testIsRetentionStrong_WithFairRetention() {
        RetentionAssessment fairAssessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-606")
                .topic("Test")
                .retentionScore(0.5)
                .retentionLevel(RetentionAssessment.RetentionLevel.FAIR)
                .build();

        assertFalse(fairAssessment.isRetentionStrong());
    }

    @Test
    void testRetentionLevel_AutomaticallyDetermined() {
        // Test EXCELLENT level
        RetentionAssessment excellent = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-707")
                .topic("Test")
                .retentionScore(0.85)
                .build();
        assertEquals(RetentionAssessment.RetentionLevel.EXCELLENT, excellent.getRetentionLevel());

        // Test GOOD level
        RetentionAssessment good = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-808")
                .topic("Test")
                .retentionScore(0.7)
                .build();
        assertEquals(RetentionAssessment.RetentionLevel.GOOD, good.getRetentionLevel());

        // Test FAIR level
        RetentionAssessment fair = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-909")
                .topic("Test")
                .retentionScore(0.5)
                .build();
        assertEquals(RetentionAssessment.RetentionLevel.FAIR, fair.getRetentionLevel());

        // Test POOR level
        RetentionAssessment poor = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-1010")
                .topic("Test")
                .retentionScore(0.3)
                .build();
        assertEquals(RetentionAssessment.RetentionLevel.POOR, poor.getRetentionLevel());
    }

    @Test
    void testDefaultValues() {
        RetentionAssessment assessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-1111")
                .topic("Test")
                .build();

        assertNotNull(assessment.getAssessedAt());
        assertNotNull(assessment.getStrengthAreas());
        assertNotNull(assessment.getWeaknessAreas());
        assertTrue(assessment.getRecommendedNextReviewDays() >= 1);
    }

    @Test
    void testNegativeValues_ClampedToZero() {
        RetentionAssessment assessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-1212")
                .topic("Test")
                .daysSinceLastReview(-5)
                .totalReviewCount(-3)
                .recommendedNextReviewDays(-7)
                .build();

        assertEquals(0, assessment.getDaysSinceLastReview());
        assertEquals(0, assessment.getTotalReviewCount());
        assertEquals(1, assessment.getRecommendedNextReviewDays()); // Minimum 1 day
    }

    @Test
    void testEquality() {
        String id = UUID.randomUUID().toString();

        RetentionAssessment assessment1 = RetentionAssessment.builder()
                .id(id)
                .developerId("dev-1313")
                .topic("Test")
                .retentionScore(0.7)
                .build();

        RetentionAssessment assessment2 = RetentionAssessment.builder()
                .id(id)
                .developerId("dev-1313")
                .topic("Test")
                .retentionScore(0.8) // Different score
                .build();

        assertEquals(assessment1, assessment2, "Assessments with same ID should be equal");
        assertEquals(assessment1.hashCode(), assessment2.hashCode());
    }

    @Test
    void testToString() {
        RetentionAssessment assessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-1414")
                .topic("Spring Security")
                .retentionScore(0.75)
                .retentionLevel(RetentionAssessment.RetentionLevel.GOOD)
                .recommendedNextReviewDays(14)
                .build();

        String toString = assessment.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Spring Security"));
        assertTrue(toString.contains("GOOD"));
        assertTrue(toString.contains("14"));
    }

    @Test
    void testRetentionLevelEnum() {
        assertEquals(4, RetentionAssessment.RetentionLevel.EXCELLENT.getLevel());
        assertEquals(3, RetentionAssessment.RetentionLevel.GOOD.getLevel());
        assertEquals(2, RetentionAssessment.RetentionLevel.FAIR.getLevel());
        assertEquals(1, RetentionAssessment.RetentionLevel.POOR.getLevel());
    }

    @Test
    void testImmutability_StrengthAreas() {
        List<String> originalStrengths = List.of("Area 1", "Area 2");
        RetentionAssessment assessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-1515")
                .topic("Test")
                .strengthAreas(originalStrengths)
                .build();

        List<String> retrievedStrengths = assessment.getStrengthAreas();
        assertNotSame(originalStrengths, retrievedStrengths, "Should return a copy");
    }

    @Test
    void testImmutability_WeaknessAreas() {
        List<String> originalWeaknesses = List.of("Weakness 1");
        RetentionAssessment assessment = RetentionAssessment.builder()
                .id(UUID.randomUUID().toString())
                .developerId("dev-1616")
                .topic("Test")
                .weaknessAreas(originalWeaknesses)
                .build();

        List<String> retrievedWeaknesses = assessment.getWeaknessAreas();
        assertNotSame(originalWeaknesses, retrievedWeaknesses, "Should return a copy");
    }
}
