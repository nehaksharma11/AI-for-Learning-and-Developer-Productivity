package com.ailearning.core.service;

import com.ailearning.core.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing learning retention and follow-up scheduling.
 * Implements spaced repetition algorithms to reinforce learning and check retention.
 */
public interface RetentionSchedulingService {
    
    /**
     * Schedules follow-up sessions based on completed learning sessions.
     * Uses spaced repetition algorithms to optimize retention.
     *
     * @param developerId the developer's ID
     * @param completedSession the completed learning session
     * @return future containing scheduled follow-up sessions
     */
    CompletableFuture<List<ScheduledSession>> scheduleFollowUp(String developerId, LearningSession completedSession);
    
    /**
     * Assesses retention for a specific topic based on follow-up session performance.
     *
     * @param developerId the developer's ID
     * @param topic the learning topic
     * @param followUpSession the follow-up session with performance data
     * @return future containing retention assessment
     */
    CompletableFuture<RetentionAssessment> assessRetention(String developerId, String topic, LearningSession followUpSession);
    
    /**
     * Adjusts the follow-up schedule based on retention assessment results.
     * Implements adaptive scheduling based on performance.
     *
     * @param developerId the developer's ID
     * @param retentionAssessment the retention assessment results
     * @return future containing updated schedule
     */
    CompletableFuture<LearningSchedule> adjustSchedule(String developerId, RetentionAssessment retentionAssessment);
    
    /**
     * Gets the next recommended review time for a topic using spaced repetition.
     *
     * @param topic the learning topic
     * @param lastReviewTime the last review timestamp
     * @param performanceScore the performance score from last review (0.0-1.0)
     * @param reviewCount the number of times reviewed
     * @return future containing next review time
     */
    CompletableFuture<java.time.LocalDateTime> getNextReviewTime(
            String topic, 
            java.time.LocalDateTime lastReviewTime, 
            double performanceScore, 
            int reviewCount);
    
    /**
     * Calculates the optimal interval for the next review using SM-2 algorithm.
     *
     * @param previousInterval the previous interval in days
     * @param easinessFactor the easiness factor (quality of recall)
     * @param performanceScore the performance score (0.0-1.0)
     * @return the next interval in days
     */
    int calculateNextInterval(int previousInterval, double easinessFactor, double performanceScore);
    
    /**
     * Gets all pending follow-up sessions for a developer.
     *
     * @param developerId the developer's ID
     * @return future containing list of pending follow-up sessions
     */
    CompletableFuture<List<ScheduledSession>> getPendingFollowUps(String developerId);
}
