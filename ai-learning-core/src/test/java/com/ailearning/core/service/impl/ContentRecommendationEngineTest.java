package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContentRecommendationEngine including collaborative filtering,
 * reinforcement learning, and personalized difficulty adjustment features.
 */
class ContentRecommendationEngineTest {

    private ContentRecommendationEngine engine;
    private LearningPreferences testPreferences;
    private List<SkillGap> testSkillGaps;

    @BeforeEach
    void setUp() {
        engine = new ContentRecommendationEngine();
        
        testPreferences = LearningPreferences.builder()
                .developerId("test-user-1")
                .preferredLearningStyle(LearningPreferences.LearningStyle.VISUAL)
                .difficultyPreference(LearningPreferences.DifficultyPreference.ADAPTIVE)
                .preferredContentTypes(List.of(LearningPreferences.ContentType.EXAMPLES, LearningPreferences.ContentType.EXERCISES))
                .preferredSessionLengthMinutes(30)
                .enableRealTimeHints(true)
                .build();

        testSkillGaps = List.of(
                SkillGap.builder()
                        .skillDomain("java")
                        .currentLevel(0.3)
                        .targetLevel(0.7)
                        .priority(SkillGap.GapPriority.HIGH)
                        .identificationReasons(List.of("Required for current project"))
                        .recommendedActions(List.of("Complete Java fundamentals course"))
                        .estimatedLearningHours(20)
                        .build(),
                SkillGap.builder()
                        .skillDomain("spring-boot")
                        .currentLevel(0.2)
                        .targetLevel(0.6)
                        .priority(SkillGap.GapPriority.MEDIUM)
                        .identificationReasons(List.of("Framework used in project"))
                        .recommendedActions(List.of("Learn Spring Boot basics"))
                        .estimatedLearningHours(15)
                        .build()
        );
    }

    @Test
    @DisplayName("Should recommend content based on skill gaps and preferences")
    void testRecommendContent() {
        // When
        List<LearningContent> recommendations = engine.recommendContent(testSkillGaps, testPreferences);

        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        
        // Should have content for both skill gaps
        assertTrue(recommendations.size() >= 2);
        
        // Content should be personalized based on preferences
        boolean hasPreferredType = recommendations.stream()
                .anyMatch(content -> content.getType() == LearningContent.ContentType.CODE_EXAMPLE ||
                                   content.getType() == LearningContent.ContentType.EXERCISE);
        assertTrue(hasPreferredType, "Should include preferred content types");
    }

    @Test
    @DisplayName("Should generate content for specific skill domain")
    void testGenerateContentForSkill() {
        // When
        List<LearningContent> content = engine.generateContentForSkill("java", 0.4, testPreferences);

        // Then
        assertNotNull(content);
        assertFalse(content.isEmpty());
        assertTrue(content.size() <= 5, "Should limit to 5 content items per skill");
        
        // All content should be relevant to Java
        content.forEach(item -> {
            assertTrue(item.getTitle().toLowerCase().contains("java") || 
                      item.getContent().toLowerCase().contains("java"));
        });
    }

    @Test
    @DisplayName("Should generate session-specific content")
    void testGenerateSessionContent() {
        // Given
        String topic = "Spring Boot";
        SkillLevel skillLevel = SkillLevel.builder()
                .domain("spring-boot")
                .proficiency(0.4)
                .confidence(0.5)
                .lastAssessed(LocalDateTime.now())
                .evidenceCount(3)
                .build();
        
        SessionPreferences sessionPrefs = SessionPreferences.builder()
                .durationMinutes(30)
                .includeExercises(true)
                .includeQuizzes(true)
                .build();

        // When
        List<LearningContent> sessionContent = engine.generateSessionContent(topic, skillLevel, sessionPrefs);

        // Then
        assertNotNull(sessionContent);
        assertFalse(sessionContent.isEmpty());
        
        // Should include explanation content
        assertTrue(sessionContent.stream().anyMatch(c -> c.getType() == LearningContent.ContentType.EXPLANATION));
        
        // Should include exercises if requested
        assertTrue(sessionContent.stream().anyMatch(c -> c.getType() == LearningContent.ContentType.EXERCISE));
        
        // Should include quiz if requested
        assertTrue(sessionContent.stream().anyMatch(c -> c.getType() == LearningContent.ContentType.QUIZ));
    }

    @Test
    @DisplayName("Should apply collaborative filtering to enhance recommendations")
    void testApplyCollaborativeFiltering() {
        // Given
        List<LearningContent> baseRecommendations = List.of(
                createTestContent("content-1", LearningContent.ContentType.EXPLANATION),
                createTestContent("content-2", LearningContent.ContentType.EXERCISE)
        );

        // Simulate user performance data for collaborative filtering
        engine.updateUserPerformance("test-user-1", "content-1", 0.8, null);
        engine.updateUserPerformance("similar-user-1", "content-1", 0.9, null);
        engine.updateUserPerformance("similar-user-1", "content-3", 0.85, "content-1");

        // When
        List<LearningContent> enhancedRecommendations = engine.applyCollaborativeFiltering(baseRecommendations, testPreferences);

        // Then
        assertNotNull(enhancedRecommendations);
        // Should at least contain the base recommendations
        assertTrue(enhancedRecommendations.size() >= baseRecommendations.size());
    }

    @Test
    @DisplayName("Should optimize content sequence using reinforcement learning")
    void testOptimizeContentSequence() {
        // Given
        List<LearningContent> content = List.of(
                createTestContent("content-1", LearningContent.ContentType.EXPLANATION),
                createTestContent("content-2", LearningContent.ContentType.CODE_EXAMPLE),
                createTestContent("content-3", LearningContent.ContentType.EXERCISE)
        );

        // When
        List<LearningContent> optimizedSequence = engine.optimizeContentSequence(content, testPreferences);

        // Then
        assertNotNull(optimizedSequence);
        assertEquals(content.size(), optimizedSequence.size());
        
        // Should contain all original content items
        assertTrue(optimizedSequence.containsAll(content));
    }

    @Test
    @DisplayName("Should adjust personalized difficulty based on performance history")
    void testAdjustPersonalizedDifficulty() {
        // Given
        String userId = "test-user-1";
        List<LearningContent> content = List.of(
                createTestContent("content-1", LearningContent.ContentType.EXPLANATION)
        );

        // Simulate performance history (high performance should increase difficulty)
        engine.updateUserPerformance(userId, "prev-content-1", 0.9, null);
        engine.updateUserPerformance(userId, "prev-content-2", 0.85, "prev-content-1");
        engine.updateUserPerformance(userId, "prev-content-3", 0.88, "prev-content-2");

        // When
        List<LearningContent> adjustedContent = engine.adjustPersonalizedDifficulty(content, userId, testPreferences);

        // Then
        assertNotNull(adjustedContent);
        assertEquals(content.size(), adjustedContent.size());
    }

    @Test
    @DisplayName("Should update user performance data correctly")
    void testUpdateUserPerformance() {
        // Given
        String userId = "test-user-1";
        String contentId = "content-1";
        String previousContentId = "content-0";
        double performanceScore = 0.75;

        // When
        engine.updateUserPerformance(userId, contentId, performanceScore, previousContentId);

        // Then - No exceptions should be thrown
        // The internal state should be updated (tested indirectly through other methods)
        assertDoesNotThrow(() -> engine.updateUserPerformance(userId, "content-2", 0.8, contentId));
    }

    @Test
    @DisplayName("Should generate follow-up content based on completed sessions")
    void testGenerateFollowUpContent() {
        // Given
        List<LearningSession> completedSessions = List.of(
                createTestSession("session-1", "Java Basics", 0.6),
                createTestSession("session-2", "Spring Boot", 0.8),
                createTestSession("session-3", "Testing", 0.4) // Low performance - needs reinforcement
        );

        // When
        List<LearningContent> followUpContent = engine.generateFollowUpContent(completedSessions);

        // Then
        assertNotNull(followUpContent);
        assertTrue(followUpContent.size() <= 3, "Should limit follow-up recommendations");
        
        // Should include reinforcement content for low-performing topics
        boolean hasReinforcementContent = followUpContent.stream()
                .anyMatch(content -> content.getTitle().toLowerCase().contains("reinforce") ||
                                   content.getTitle().toLowerCase().contains("testing"));
        assertTrue(hasReinforcementContent, "Should include reinforcement content for struggling areas");
    }

    @Test
    @DisplayName("Should handle empty skill gaps gracefully")
    void testRecommendContentWithEmptySkillGaps() {
        // When
        List<LearningContent> recommendations = engine.recommendContent(Collections.emptyList(), testPreferences);

        // Then
        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    @DisplayName("Should handle null preferences gracefully")
    void testRecommendContentWithNullPreferences() {
        // When & Then
        assertDoesNotThrow(() -> {
            List<LearningContent> recommendations = engine.recommendContent(testSkillGaps, null);
            assertNotNull(recommendations);
        });
    }

    // Helper methods

    private LearningContent createTestContent(String id, LearningContent.ContentType type) {
        return LearningContent.builder()
                .id(id)
                .type(type)
                .title("Test Content " + id)
                .content("Test content description")
                .difficulty(LearningContent.DifficultyLevel.INTERMEDIATE)
                .estimatedMinutes(20)
                .prerequisites(List.of())
                .build();
    }

    private LearningSession createTestSession(String sessionId, String topic, double avgScore) {
        List<LearningOutcome> outcomes = List.of(
                LearningOutcome.builder()
                        .outcomeId(UUID.randomUUID().toString())
                        .achievementScore(avgScore)
                        .completionTime(LocalDateTime.now())
                        .build()
        );

        return LearningSession.builder()
                .id(sessionId)
                .developerId("test-user-1")
                .topic(topic)
                .content(List.of())
                .outcomes(outcomes)
                .difficultyLevel(0.5)
                .sessionType("tutorial")
                .build();
    }
}