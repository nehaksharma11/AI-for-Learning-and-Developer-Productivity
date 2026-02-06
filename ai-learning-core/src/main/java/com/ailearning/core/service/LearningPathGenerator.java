package com.ailearning.core.service;

import com.ailearning.core.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for generating personalized learning paths and managing skill assessments.
 * Implements adaptive learning algorithms to create tailored educational experiences.
 */
public interface LearningPathGenerator {

    /**
     * Generates a personalized learning path for a developer based on their profile and project context.
     *
     * @param profile the developer's current profile including skills and preferences
     * @param context the current project context for relevant learning recommendations
     * @return a future containing the generated learning path
     */
    CompletableFuture<LearningPath> generatePath(DeveloperProfile profile, ProjectContext context);

    /**
     * Updates learning progress for a specific session and adjusts future recommendations.
     *
     * @param sessionId the unique identifier of the learning session
     * @param progress the progress information including completion status and scores
     * @return a future that completes when progress is updated
     */
    CompletableFuture<Void> updateProgress(String sessionId, LearningProgress progress);

    /**
     * Recommends learning content based on identified skill gaps and learning preferences.
     *
     * @param skillGaps the list of identified skill gaps to address
     * @param preferences the developer's learning preferences and constraints
     * @return a future containing recommended learning content
     */
    CompletableFuture<List<LearningContent>> recommendContent(List<SkillGap> skillGaps, LearningPreferences preferences);

    /**
     * Assesses skill level based on code samples and project contributions.
     *
     * @param codeSamples the code samples to analyze for skill assessment
     * @param domain the skill domain to assess (e.g., "java", "spring-boot", "testing")
     * @return a future containing the skill assessment results
     */
    CompletableFuture<SkillAssessment> assessSkillLevel(List<CodeSample> codeSamples, String domain);

    /**
     * Identifies skill gaps by analyzing current skills against project requirements and career goals.
     *
     * @param profile the developer's current profile
     * @param projectContext the current project context
     * @param careerGoals optional career goals to consider in gap analysis
     * @return a future containing identified skill gaps
     */
    CompletableFuture<List<SkillGap>> identifySkillGaps(DeveloperProfile profile, ProjectContext projectContext, List<String> careerGoals);

    /**
     * Creates an adaptive learning session based on current context and skill level.
     *
     * @param developerId the developer's unique identifier
     * @param topic the learning topic or skill domain
     * @param currentSkillLevel the developer's current skill level in the topic
     * @param sessionPreferences preferences for the learning session
     * @return a future containing the created learning session
     */
    CompletableFuture<LearningSession> createLearningSession(String developerId, String topic, SkillLevel currentSkillLevel, SessionPreferences sessionPreferences);

    /**
     * Updates a developer's skill profile based on assessment results.
     *
     * @param developerId the developer's unique identifier
     * @param assessment the skill assessment results
     * @return a future containing the updated developer profile
     */
    CompletableFuture<DeveloperProfile> updateSkillProfile(String developerId, SkillAssessment assessment);

    /**
     * Generates follow-up learning recommendations based on completed sessions and retention analysis.
     *
     * @param developerId the developer's unique identifier
     * @param completedSessions the list of completed learning sessions
     * @return a future containing follow-up recommendations
     */
    CompletableFuture<List<LearningContent>> generateFollowUpRecommendations(String developerId, List<LearningSession> completedSessions);

    /**
     * Analyzes learning patterns and suggests optimal learning schedules.
     *
     * @param developerId the developer's unique identifier
     * @param learningHistory the developer's learning history
     * @return a future containing schedule recommendations
     */
    CompletableFuture<LearningSchedule> optimizeLearningSchedule(String developerId, List<LearningSession> learningHistory);

    /**
     * Provides personalized learning analytics and progress insights.
     *
     * @param developerId the developer's unique identifier
     * @param timeRange the time range for analytics (e.g., last 30 days)
     * @return a future containing learning analytics
     */
    CompletableFuture<LearningAnalytics> getLearningAnalytics(String developerId, String timeRange);
}