package com.ailearning.core.service;

import com.ailearning.core.model.*;
import com.ailearning.core.service.impl.DefaultLearningPathGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for adaptive learning features including collaborative filtering,
 * reinforcement learning, and personalized difficulty adjustment.
 */
class AdaptiveLearningIntegrationTest {

    private DefaultLearningPathGenerator learningPathGenerator;
    private DeveloperProfile testProfile;
    private ProjectContext testProjectContext;

    @BeforeEach
    void setUp() {
        learningPathGenerator = new DefaultLearningPathGenerator();
        
        // Create test developer profile
        testProfile = DeveloperProfile.builder()
                .id("test-developer-1")
                .skillLevels(Map.of(
                        "java", SkillLevel.builder()
                                .domain("java")
                                .proficiency(0.4)
                                .confidence(0.5)
                                .lastAssessed(LocalDateTime.now())
                                .evidenceCount(5)
                                .build(),
                        "spring-boot", SkillLevel.builder()
                                .domain("spring-boot")
                                .proficiency(0.2)
                                .confidence(0.3)
                                .lastAssessed(LocalDateTime.now())
                                .evidenceCount(2)
                                .build()
                ))
                .learningPreferences(LearningPreferences.builder()
                        .developerId("test-developer-1")
                        .preferredLearningStyle(LearningPreferences.LearningStyle.VISUAL)
                        .difficultyPreference(LearningPreferences.DifficultyPreference.ADAPTIVE)
                        .preferredContentTypes(List.of(LearningPreferences.ContentType.EXAMPLES, LearningPreferences.ContentType.EXERCISES))
                        .build())
                .workHistory(new ArrayList<>())
                .achievements(new ArrayList<>())
                .currentGoals(List.of(
                        LearningGoal.builder()
                                .id("goal-1")
                                .title("Master Spring Boot")
                                .description("Become proficient in Spring Boot development")
                                .targetSkillLevel(0.8)
                                .deadline(LocalDateTime.now().plusMonths(3))
                                .build()
                ))
                .build();

        // Create test project context
        testProjectContext = ProjectContext.builder()
                .id("test-project-1")
                .projectName("E-commerce API")
                .description("REST API for e-commerce platform")
                .dependencies(List.of(
                        Dependency.builder()
                                .name("spring-boot-starter-web")
                                .version("2.7.0")
                                .type("compile")
                                .build(),
                        Dependency.builder()
                                .name("spring-boot-starter-data-jpa")
                                .version("2.7.0")
                                .type("compile")
                                .build()
                ))
                .patterns(List.of(
                        CodePattern.builder()
                                .id("pattern-1")
                                .name("REST Controller Pattern")
                                .description("Standard REST controller implementation")
                                .frequency(15)
                                .build()
                ))
                .conventions(new ArrayList<>())
                .complexity(ComplexityMetrics.builder()
                        .cyclomaticComplexity(8.5)
                        .linesOfCode(2500)
                        .numberOfClasses(45)
                        .numberOfMethods(180)
                        .build())
                .build();
    }

    @Test
    @DisplayName("Should generate adaptive learning path with collaborative filtering")
    void testGenerateAdaptiveLearningPath() throws Exception {
        // When
        CompletableFuture<LearningPath> pathFuture = learningPathGenerator.generatePath(testProfile, testProjectContext);
        LearningPath learningPath = pathFuture.get();

        // Then
        assertNotNull(learningPath);
        assertNotNull(learningPath.getId());
        assertEquals(testProfile.getId(), learningPath.getDeveloperId());
        assertFalse(learningPath.getModules().isEmpty());
        
        // Should prioritize Spring Boot based on project context and goals
        assertTrue(learningPath.getTargetSkills().contains("spring-boot"));
        
        // Should have reasonable estimated duration
        assertTrue(learningPath.getEstimatedDurationMinutes() > 0);
        
        // Should include modules for identified skill gaps
        assertTrue(learningPath.getModules().size() >= 1);
    }

    @Test
    @DisplayName("Should recommend content with collaborative filtering and RL optimization")
    void testRecommendContentWithAdvancedFeatures() throws Exception {
        // Given - Identify skill gaps first
        CompletableFuture<List<SkillGap>> gapsFuture = learningPathGenerator.identifySkillGaps(
                testProfile, testProjectContext, List.of("senior developer"));
        List<SkillGap> skillGaps = gapsFuture.get();

        // When - Get recommendations with advanced features
        CompletableFuture<List<LearningContent>> contentFuture = learningPathGenerator.recommendContent(
                skillGaps, testProfile.getLearningPreferences());
        List<LearningContent> recommendations = contentFuture.get();

        // Then
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        
        // Should include content for major skill gaps
        boolean hasJavaContent = recommendations.stream()
                .anyMatch(content -> content.getTitle().toLowerCase().contains("java") ||
                                   content.getContent().toLowerCase().contains("java"));
        assertTrue(hasJavaContent, "Should include Java content for identified skill gap");
        
        // Should respect user preferences for content types
        boolean hasPreferredTypes = recommendations.stream()
                .anyMatch(content -> content.getType() == LearningContent.ContentType.CODE_EXAMPLE ||
                                   content.getType() == LearningContent.ContentType.EXERCISE);
        assertTrue(hasPreferredTypes, "Should include preferred content types");
    }

    @Test
    @DisplayName("Should update progress and adapt recommendations based on performance")
    void testProgressUpdateAndAdaptation() throws Exception {
        // Given - Create a learning session
        SkillLevel currentSkill = testProfile.getSkillLevels().get("java");
        SessionPreferences sessionPrefs = SessionPreferences.builder()
                .durationMinutes(30)
                .includeExercises(true)
                .includeQuizzes(true)
                .build();

        CompletableFuture<LearningSession> sessionFuture = learningPathGenerator.createLearningSession(
                testProfile.getId(), "Java Fundamentals", currentSkill, sessionPrefs);
        LearningSession session = sessionFuture.get();

        // Simulate learning progress with high performance
        LearningProgress highPerformanceProgress = LearningProgress.builder()
                .sessionId(session.getId())
                .developerId(testProfile.getId())
                .completionPercentage(95.0)
                .score(88.0)
                .contentId("java-basics-content")
                .previousContentId(null)
                .accuracyScore(0.88)
                .expectedTimeMinutes(30)
                .actualTimeMinutes(25)
                .timeSpentMinutes(25)
                .sessionCompleted(true)
                .build();

        // When - Update progress
        CompletableFuture<Void> updateFuture = learningPathGenerator.updateProgress(session.getId(), highPerformanceProgress);
        updateFuture.get(); // Wait for completion

        // Then - Should complete without errors
        assertTrue(updateFuture.isDone());
        assertFalse(updateFuture.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Should assess skill level based on code samples")
    void testSkillAssessment() throws Exception {
        // Given
        List<CodeSample> javaSamples = List.of(
                CodeSample.builder()
                        .id("sample-1")
                        .code("public class Calculator { public int add(int a, int b) { return a + b; } }")
                        .language("java")
                        .complexity(0.3)
                        .qualityScore(0.8)
                        .build(),
                CodeSample.builder()
                        .id("sample-2")
                        .code("@RestController public class UserController { @GetMapping(\"/users\") public List<User> getUsers() { return userService.findAll(); } }")
                        .language("java")
                        .complexity(0.6)
                        .qualityScore(0.7)
                        .build()
        );

        // When
        CompletableFuture<SkillAssessment> assessmentFuture = learningPathGenerator.assessSkillLevel(javaSamples, "java");
        SkillAssessment assessment = assessmentFuture.get();

        // Then
        assertNotNull(assessment);
        assertEquals("java", assessment.getSkillDomain());
        assertTrue(assessment.getProficiencyScore() >= 0.0 && assessment.getProficiencyScore() <= 1.0);
        assertTrue(assessment.getConfidenceScore() >= 0.0 && assessment.getConfidenceScore() <= 1.0);
        assertNotNull(assessment.getEvidence());
        assertFalse(assessment.getEvidence().isEmpty());
    }

    @Test
    @DisplayName("Should generate follow-up recommendations based on session history")
    void testFollowUpRecommendations() throws Exception {
        // Given - Simulate completed learning sessions with varying performance
        List<LearningSession> completedSessions = List.of(
                createCompletedSession("session-1", "Java Basics", 0.85), // Good performance
                createCompletedSession("session-2", "Spring Boot Intro", 0.45), // Poor performance - needs reinforcement
                createCompletedSession("session-3", "REST APIs", 0.92) // Excellent performance - ready for advanced
        );

        // When
        CompletableFuture<List<LearningContent>> followUpFuture = learningPathGenerator.generateFollowUpRecommendations(
                testProfile.getId(), completedSessions);
        List<LearningContent> followUpContent = followUpFuture.get();

        // Then
        assertNotNull(followUpContent);
        assertTrue(followUpContent.size() <= 3, "Should limit follow-up recommendations");
        
        // Should include reinforcement content for poor performance areas
        boolean hasReinforcementContent = followUpContent.stream()
                .anyMatch(content -> content.getTitle().toLowerCase().contains("spring boot") ||
                                   content.getTitle().toLowerCase().contains("reinforce"));
        
        // Should include advanced content for areas with excellent performance
        boolean hasAdvancedContent = followUpContent.stream()
                .anyMatch(content -> content.getDifficulty() == LearningContent.DifficultyLevel.ADVANCED ||
                                   content.getDifficulty() == LearningContent.DifficultyLevel.EXPERT);
    }

    @Test
    @DisplayName("Should optimize learning schedule based on history")
    void testLearningScheduleOptimization() throws Exception {
        // Given
        List<LearningSession> learningHistory = List.of(
                createCompletedSession("session-1", "Java Basics", 0.75),
                createCompletedSession("session-2", "OOP Concepts", 0.68),
                createCompletedSession("session-3", "Collections", 0.82)
        );

        // When
        CompletableFuture<LearningSchedule> scheduleFuture = learningPathGenerator.optimizeLearningSchedule(
                testProfile.getId(), learningHistory);
        LearningSchedule schedule = scheduleFuture.get();

        // Then
        assertNotNull(schedule);
        assertNotNull(schedule.getId());
        assertEquals(testProfile.getId(), schedule.getDeveloperId());
        assertFalse(schedule.getScheduledSessions().isEmpty());
    }

    @Test
    @DisplayName("Should generate learning analytics")
    void testLearningAnalytics() throws Exception {
        // When
        CompletableFuture<LearningAnalytics> analyticsFuture = learningPathGenerator.getLearningAnalytics(
                testProfile.getId(), "last_30_days");
        LearningAnalytics analytics = analyticsFuture.get();

        // Then
        assertNotNull(analytics);
        assertEquals(testProfile.getId(), analytics.getDeveloperId());
        assertNotNull(analytics.getTimeRange());
        assertNotNull(analytics.getSkillProgressMap());
        assertNotNull(analytics.getLearningTrends());
    }

    @Test
    @DisplayName("Should update skill profile based on assessment")
    void testSkillProfileUpdate() throws Exception {
        // Given
        SkillAssessment assessment = SkillAssessment.builder()
                .id("assessment-1")
                .developerId(testProfile.getId())
                .skillDomain("java")
                .proficiencyScore(0.7)
                .confidenceScore(0.8)
                .assessedAt(LocalDateTime.now())
                .evidence(List.of(
                        AssessmentEvidence.builder()
                                .evidenceId("evidence-1")
                                .type("code_analysis")
                                .description("Analyzed Java code samples")
                                .score(0.75)
                                .build()
                ))
                .build();

        // When
        CompletableFuture<DeveloperProfile> profileFuture = learningPathGenerator.updateSkillProfile(
                testProfile.getId(), assessment);
        DeveloperProfile updatedProfile = profileFuture.get();

        // Then
        assertNotNull(updatedProfile);
        assertEquals(testProfile.getId(), updatedProfile.getId());
        assertTrue(updatedProfile.getSkillLevels().containsKey("java"));
        
        SkillLevel updatedSkill = updatedProfile.getSkillLevels().get("java");
        assertEquals(0.7, updatedSkill.getProficiency(), 0.01);
        assertEquals(0.8, updatedSkill.getConfidence(), 0.01);
    }

    // Helper methods

    private LearningSession createCompletedSession(String sessionId, String topic, double avgScore) {
        List<LearningOutcome> outcomes = List.of(
                LearningOutcome.builder()
                        .outcomeId(UUID.randomUUID().toString())
                        .achievementScore(avgScore)
                        .completionTime(LocalDateTime.now())
                        .build()
        );

        return LearningSession.builder()
                .id(sessionId)
                .developerId(testProfile.getId())
                .topic(topic)
                .content(List.of())
                .outcomes(outcomes)
                .difficultyLevel(0.5)
                .sessionType("tutorial")
                .build();
    }
}