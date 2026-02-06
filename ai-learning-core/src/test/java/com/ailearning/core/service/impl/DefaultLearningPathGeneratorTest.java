package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.LearningPathGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultLearningPathGenerator.
 * Tests skill assessment algorithms and learning path generation.
 */
class DefaultLearningPathGeneratorTest {

    private LearningPathGenerator learningPathGenerator;
    private DeveloperProfile testProfile;
    private ProjectContext testProjectContext;

    @BeforeEach
    void setUp() {
        learningPathGenerator = new DefaultLearningPathGenerator();
        
        // Create test developer profile
        testProfile = DeveloperProfile.builder()
                .id("test-developer")
                .skillLevels(Map.of(
                        "java", SkillLevel.builder()
                                .domain("java")
                                .proficiency(0.6)
                                .confidence(0.7)
                                .lastAssessed(LocalDateTime.now())
                                .evidenceCount(5)
                                .build(),
                        "testing", SkillLevel.builder()
                                .domain("testing")
                                .proficiency(0.3)
                                .confidence(0.4)
                                .lastAssessed(LocalDateTime.now())
                                .evidenceCount(2)
                                .build()
                ))
                .learningPreferences(LearningPreferences.builder()
                        .detailLevel("intermediate")
                        .build())
                .workHistory(List.of())
                .achievements(List.of())
                .currentGoals(List.of(
                        LearningGoal.builder()
                                .id("goal-1")
                                .title("Improve Testing Skills")
                                .description("Learn advanced testing techniques")
                                .targetSkillLevel(0.8)
                                .build()
                ))
                .build();

        // Create test project context
        testProjectContext = ProjectContext.create("test-project", 
                ProjectStructure.builder()
                        .files(List.of())
                        .modules(List.of())
                        .relationships(List.of())
                        .entryPoints(List.of())
                        .build());
    }

    @Test
    void testGeneratePath() throws Exception {
        // When
        CompletableFuture<LearningPath> pathFuture = learningPathGenerator.generatePath(testProfile, testProjectContext);
        LearningPath path = pathFuture.get();

        // Then
        assertNotNull(path);
        assertEquals("test-developer", path.getDeveloperId());
        assertFalse(path.getModules().isEmpty());
        assertTrue(path.getEstimatedDurationMinutes() > 0);
        assertNotNull(path.getDifficulty());
    }

    @Test
    void testIdentifySkillGaps() throws Exception {
        // When
        CompletableFuture<List<SkillGap>> gapsFuture = learningPathGenerator.identifySkillGaps(
                testProfile, testProjectContext, List.of("senior developer"));
        List<SkillGap> gaps = gapsFuture.get();

        // Then
        assertNotNull(gaps);
        assertFalse(gaps.isEmpty());
        
        // Should identify testing as a skill gap (proficiency 0.3 < target)
        assertTrue(gaps.stream().anyMatch(gap -> gap.getSkillDomain().equals("testing")));
    }

    @Test
    void testAssessSkillLevel() throws Exception {
        // Given
        List<CodeSample> codeSamples = List.of(
                CodeSample.builder()
                        .id("sample-1")
                        .code("public class TestClass { @Test public void testMethod() { assertEquals(1, 1); } }")
                        .language("java")
                        .context("unit test")
                        .build()
        );

        // When
        CompletableFuture<SkillAssessment> assessmentFuture = learningPathGenerator.assessSkillLevel(codeSamples, "testing");
        SkillAssessment assessment = assessmentFuture.get();

        // Then
        assertNotNull(assessment);
        assertEquals("testing", assessment.getSkillDomain());
        assertTrue(assessment.getProficiencyScore() >= 0.0 && assessment.getProficiencyScore() <= 1.0);
        assertTrue(assessment.getConfidenceScore() >= 0.0 && assessment.getConfidenceScore() <= 1.0);
    }

    @Test
    void testCreateLearningSession() throws Exception {
        // Given
        SkillLevel currentSkillLevel = SkillLevel.builder()
                .domain("java")
                .proficiency(0.5)
                .confidence(0.6)
                .lastAssessed(LocalDateTime.now())
                .evidenceCount(3)
                .build();
        
        SessionPreferences preferences = SessionPreferences.builder()
                .durationMinutes(30)
                .includeExercises(true)
                .includeQuizzes(true)
                .build();

        // When
        CompletableFuture<LearningSession> sessionFuture = learningPathGenerator.createLearningSession(
                "test-developer", "java", currentSkillLevel, preferences);
        LearningSession session = sessionFuture.get();

        // Then
        assertNotNull(session);
        assertEquals("test-developer", session.getDeveloperId());
        assertEquals("java", session.getTopic());
        assertFalse(session.getContent().isEmpty());
        assertTrue(session.getDifficultyLevel() >= 0.0 && session.getDifficultyLevel() <= 1.0);
    }

    @Test
    void testUpdateSkillProfile() throws Exception {
        // Given
        SkillAssessment assessment = SkillAssessment.builder()
                .assessmentId("assessment-1")
                .skillDomain("java")
                .proficiencyScore(0.8)
                .confidenceScore(0.9)
                .evidence(List.of())
                .method(SkillAssessment.AssessmentMethod.CODE_ANALYSIS)
                .build();

        // When
        CompletableFuture<DeveloperProfile> profileFuture = learningPathGenerator.updateSkillProfile("test-developer", assessment);
        DeveloperProfile updatedProfile = profileFuture.get();

        // Then
        assertNotNull(updatedProfile);
        assertEquals("test-developer", updatedProfile.getId());
        assertTrue(updatedProfile.getSkillLevels().containsKey("java"));
        assertEquals(0.8, updatedProfile.getSkillLevels().get("java").getProficiency(), 0.01);
    }

    @Test
    void testRecommendContent() throws Exception {
        // Given
        List<SkillGap> skillGaps = List.of(
                SkillGap.builder()
                        .skillDomain("testing")
                        .currentLevel(0.3)
                        .targetLevel(0.7)
                        .priority(SkillGap.GapPriority.HIGH)
                        .identificationReasons(List.of("Low proficiency"))
                        .recommendedActions(List.of("Practice unit testing"))
                        .estimatedLearningHours(10)
                        .build()
        );

        LearningPreferences preferences = LearningPreferences.builder()
                .detailLevel("intermediate")
                .build();

        // When
        CompletableFuture<List<LearningContent>> contentFuture = learningPathGenerator.recommendContent(skillGaps, preferences);
        List<LearningContent> content = contentFuture.get();

        // Then
        assertNotNull(content);
        assertFalse(content.isEmpty());
        assertTrue(content.stream().anyMatch(c -> c.getTitle().toLowerCase().contains("testing")));
    }

    @Test
    void testOptimizeLearningSchedule() throws Exception {
        // Given
        List<LearningSession> learningHistory = List.of(
                LearningSession.builder()
                        .id("session-1")
                        .developerId("test-developer")
                        .topic("java")
                        .content(List.of())
                        .startTime(LocalDateTime.now().minusDays(1))
                        .durationMinutes(30)
                        .outcomes(List.of(
                                LearningOutcome.builder()
                                        .id("outcome-1")
                                        .skillDomain("java")
                                        .achievementScore(0.8)
                                        .build()
                        ))
                        .build()
        );

        // When
        CompletableFuture<LearningSchedule> scheduleFuture = learningPathGenerator.optimizeLearningSchedule("test-developer", learningHistory);
        LearningSchedule schedule = scheduleFuture.get();

        // Then
        assertNotNull(schedule);
        assertEquals("test-developer", schedule.getDeveloperId());
        assertFalse(schedule.getScheduledSessions().isEmpty());
        assertTrue(schedule.getOptimalSessionDuration() > 0);
    }

    @Test
    void testGetLearningAnalytics() throws Exception {
        // When
        CompletableFuture<LearningAnalytics> analyticsFuture = learningPathGenerator.getLearningAnalytics("test-developer", "last_30_days");
        LearningAnalytics analytics = analyticsFuture.get();

        // Then
        assertNotNull(analytics);
        assertEquals("test-developer", analytics.getDeveloperId());
        assertEquals("last_30_days", analytics.getTimeRange());
        assertFalse(analytics.getTrends().isEmpty());
        assertFalse(analytics.getSkillProgression().isEmpty());
    }
}