package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.RetentionSchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of RetentionSchedulingService.
 * Implements spaced repetition algorithms (SM-2) for optimal learning retention.
 */
public class DefaultRetentionSchedulingService implements RetentionSchedulingService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRetentionSchedulingService.class);

    // SM-2 algorithm constants
    private static final double MIN_EASINESS_FACTOR = 1.3;
    private static final double DEFAULT_EASINESS_FACTOR = 2.5;
    private static final int INITIAL_INTERVAL_DAYS = 1;
    private static final int SECOND_INTERVAL_DAYS = 6;

    // Storage for topic review data (in production, this would be a database)
    private final Map<String, TopicReviewData> topicReviewDataStore = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<List<ScheduledSession>> scheduleFollowUp(String developerId, LearningSession completedSession) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Scheduling follow-up for developer: {}, session: {}", developerId, completedSession.getId());

            try {
                List<ScheduledSession> followUpSessions = new ArrayList<>();

                // Calculate performance score from session outcomes
                double performanceScore = calculateSessionPerformanceScore(completedSession);

                // Get or create topic review data
                String topicKey = developerId + ":" + completedSession.getTopic();
                TopicReviewData reviewData = topicReviewDataStore.computeIfAbsent(topicKey,
                        k -> new TopicReviewData(completedSession.getTopic(), DEFAULT_EASINESS_FACTOR));

                // Update review data
                reviewData.incrementReviewCount();
                reviewData.setLastReviewTime(LocalDateTime.now());
                reviewData.setLastPerformanceScore(performanceScore);

                // Calculate next review intervals using SM-2 algorithm
                List<Integer> intervals = calculateFollowUpIntervals(reviewData, performanceScore);

                // Create scheduled sessions for each interval
                LocalDateTime baseTime = LocalDateTime.now();
                for (int i = 0; i < intervals.size(); i++) {
                    int intervalDays = intervals.get(i);
                    LocalDateTime scheduledTime = baseTime.plusDays(intervalDays);

                    ScheduledSession followUp = ScheduledSession.builder()
                            .id(UUID.randomUUID().toString())
                            .developerId(developerId)
                            .topic(completedSession.getTopic())
                            .sessionType("retention-review")
                            .scheduledTime(scheduledTime)
                            .estimatedDuration(15 + (i * 5)) // Increasing duration for later reviews
                            .priority(determinePriority(performanceScore, i))
                            .description(String.format("Follow-up review #%d for %s", i + 1, completedSession.getTopic()))
                            .build();

                    followUpSessions.add(followUp);
                }

                // Update easiness factor based on performance
                double newEasinessFactor = updateEasinessFactor(reviewData.getEasinessFactor(), performanceScore);
                reviewData.setEasinessFactor(newEasinessFactor);

                logger.info("Scheduled {} follow-up sessions for topic: {}", followUpSessions.size(), completedSession.getTopic());

                return followUpSessions;

            } catch (Exception e) {
                logger.error("Error scheduling follow-up for developer: {}", developerId, e);
                throw new RuntimeException("Failed to schedule follow-up", e);
            }
        });
    }

    @Override
    public CompletableFuture<RetentionAssessment> assessRetention(String developerId, String topic, LearningSession followUpSession) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Assessing retention for developer: {}, topic: {}", developerId, topic);

            try {
                String topicKey = developerId + ":" + topic;
                TopicReviewData reviewData = topicReviewDataStore.get(topicKey);

                if (reviewData == null) {
                    logger.warn("No review data found for topic: {}", topic);
                    reviewData = new TopicReviewData(topic, DEFAULT_EASINESS_FACTOR);
                }

                // Calculate retention metrics
                double performanceScore = calculateSessionPerformanceScore(followUpSession);
                double recallAccuracy = calculateRecallAccuracy(followUpSession);
                int daysSinceLastReview = calculateDaysSinceLastReview(reviewData.getLastReviewTime());

                // Determine retention score (combines performance and time decay)
                double retentionScore = calculateRetentionScore(performanceScore, daysSinceLastReview);

                // Analyze strengths and weaknesses
                List<String> strengthAreas = identifyStrengthAreas(followUpSession);
                List<String> weaknessAreas = identifyWeaknessAreas(followUpSession);

                // Determine recommended action
                String recommendedAction = determineRecommendedAction(retentionScore, performanceScore);

                // Calculate next review interval
                int nextReviewDays = calculateNextInterval(
                        reviewData.getLastIntervalDays(),
                        reviewData.getEasinessFactor(),
                        performanceScore
                );

                RetentionAssessment assessment = RetentionAssessment.builder()
                        .id(UUID.randomUUID().toString())
                        .developerId(developerId)
                        .topic(topic)
                        .retentionScore(retentionScore)
                        .recallAccuracy(recallAccuracy)
                        .daysSinceLastReview(daysSinceLastReview)
                        .totalReviewCount(reviewData.getReviewCount())
                        .assessedAt(LocalDateTime.now())
                        .strengthAreas(strengthAreas)
                        .weaknessAreas(weaknessAreas)
                        .recommendedAction(recommendedAction)
                        .recommendedNextReviewDays(nextReviewDays)
                        .build();

                // Update review data
                reviewData.setLastIntervalDays(nextReviewDays);

                logger.info("Retention assessment completed: score={}, level={}", 
                           retentionScore, assessment.getRetentionLevel());

                return assessment;

            } catch (Exception e) {
                logger.error("Error assessing retention for developer: {}, topic: {}", developerId, topic, e);
                throw new RuntimeException("Failed to assess retention", e);
            }
        });
    }

    @Override
    public CompletableFuture<LearningSchedule> adjustSchedule(String developerId, RetentionAssessment retentionAssessment) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Adjusting schedule for developer: {} based on retention assessment", developerId);

            try {
                List<ScheduledSession> adjustedSessions = new ArrayList<>();

                // If retention is poor, schedule immediate review
                if (retentionAssessment.needsImmediateReview()) {
                    ScheduledSession immediateReview = ScheduledSession.builder()
                            .id(UUID.randomUUID().toString())
                            .developerId(developerId)
                            .topic(retentionAssessment.getTopic())
                            .sessionType("immediate-review")
                            .scheduledTime(LocalDateTime.now().plusHours(2))
                            .estimatedDuration(30)
                            .priority(ScheduledSession.Priority.HIGH)
                            .description("Immediate review needed due to low retention")
                            .build();
                    adjustedSessions.add(immediateReview);
                }

                // Schedule next review based on assessment
                LocalDateTime nextReviewTime = LocalDateTime.now().plusDays(retentionAssessment.getRecommendedNextReviewDays());
                ScheduledSession nextReview = ScheduledSession.builder()
                        .id(UUID.randomUUID().toString())
                        .developerId(developerId)
                        .topic(retentionAssessment.getTopic())
                        .sessionType("scheduled-review")
                        .scheduledTime(nextReviewTime)
                        .estimatedDuration(20)
                        .priority(determinePriorityFromRetention(retentionAssessment.getRetentionLevel()))
                        .description(String.format("Scheduled review for %s", retentionAssessment.getTopic()))
                        .build();
                adjustedSessions.add(nextReview);

                // Create updated schedule
                LearningSchedule schedule = LearningSchedule.builder()
                        .id(UUID.randomUUID().toString())
                        .developerId(developerId)
                        .scheduledSessions(adjustedSessions)
                        .recommendedTimeSlots(List.of("morning", "afternoon"))
                        .optimalSessionDuration(20)
                        .recommendedFrequency(calculateRecommendedFrequency(retentionAssessment))
                        .scheduleRationale(String.format("Adjusted based on %s retention level", 
                                retentionAssessment.getRetentionLevel()))
                        .build();

                logger.info("Schedule adjusted with {} sessions", adjustedSessions.size());

                return schedule;

            } catch (Exception e) {
                logger.error("Error adjusting schedule for developer: {}", developerId, e);
                throw new RuntimeException("Failed to adjust schedule", e);
            }
        });
    }

    @Override
    public CompletableFuture<LocalDateTime> getNextReviewTime(String topic, LocalDateTime lastReviewTime, 
                                                               double performanceScore, int reviewCount) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Calculating next review time for topic: {}", topic);

            try {
                // Get or create review data
                TopicReviewData reviewData = new TopicReviewData(topic, DEFAULT_EASINESS_FACTOR);
                reviewData.setReviewCount(reviewCount);
                reviewData.setLastReviewTime(lastReviewTime);

                // Calculate interval
                int intervalDays = calculateNextInterval(
                        reviewData.getLastIntervalDays(),
                        reviewData.getEasinessFactor(),
                        performanceScore
                );

                return lastReviewTime.plusDays(intervalDays);

            } catch (Exception e) {
                logger.error("Error calculating next review time for topic: {}", topic, e);
                throw new RuntimeException("Failed to calculate next review time", e);
            }
        });
    }

    @Override
    public int calculateNextInterval(int previousInterval, double easinessFactor, double performanceScore) {
        // SM-2 algorithm implementation
        if (performanceScore < 0.6) {
            // Poor performance - reset to initial interval
            return INITIAL_INTERVAL_DAYS;
        }

        if (previousInterval == 0) {
            return INITIAL_INTERVAL_DAYS;
        } else if (previousInterval == INITIAL_INTERVAL_DAYS) {
            return SECOND_INTERVAL_DAYS;
        } else {
            // Calculate next interval: I(n) = I(n-1) * EF
            int nextInterval = (int) Math.ceil(previousInterval * easinessFactor);
            return Math.min(nextInterval, 180); // Cap at 6 months
        }
    }

    @Override
    public CompletableFuture<List<ScheduledSession>> getPendingFollowUps(String developerId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Getting pending follow-ups for developer: {}", developerId);

            try {
                // In production, this would query a database
                // For now, return empty list as this is a simplified implementation
                return new ArrayList<>();

            } catch (Exception e) {
                logger.error("Error getting pending follow-ups for developer: {}", developerId, e);
                throw new RuntimeException("Failed to get pending follow-ups", e);
            }
        });
    }

    // Private helper methods

    private double calculateSessionPerformanceScore(LearningSession session) {
        if (session.getOutcomes().isEmpty()) {
            return 0.7; // Default moderate performance
        }

        double totalScore = session.getOutcomes().stream()
                .mapToDouble(outcome -> outcome.getAchievementScore())
                .average()
                .orElse(0.7);

        return Math.max(0.0, Math.min(1.0, totalScore));
    }

    private List<Integer> calculateFollowUpIntervals(TopicReviewData reviewData, double performanceScore) {
        List<Integer> intervals = new ArrayList<>();

        // Calculate 3 follow-up intervals
        int currentInterval = reviewData.getLastIntervalDays();
        double easinessFactor = reviewData.getEasinessFactor();

        for (int i = 0; i < 3; i++) {
            int nextInterval = calculateNextInterval(currentInterval, easinessFactor, performanceScore);
            intervals.add(nextInterval);
            currentInterval = nextInterval;
        }

        return intervals;
    }

    private double updateEasinessFactor(double currentEF, double performanceScore) {
        // SM-2 formula: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        // where q is quality of recall (0-5), we map performanceScore (0-1) to q
        double q = performanceScore * 5.0;

        double newEF = currentEF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02));

        return Math.max(MIN_EASINESS_FACTOR, newEF);
    }

    private ScheduledSession.Priority determinePriority(double performanceScore, int reviewIndex) {
        if (performanceScore < 0.5) {
            return ScheduledSession.Priority.HIGH;
        } else if (performanceScore < 0.7 || reviewIndex == 0) {
            return ScheduledSession.Priority.MEDIUM;
        } else {
            return ScheduledSession.Priority.LOW;
        }
    }

    private double calculateRecallAccuracy(LearningSession session) {
        // Calculate based on interactions and outcomes
        if (session.getInteractions().isEmpty()) {
            return calculateSessionPerformanceScore(session);
        }

        // Simplified calculation - in production, would analyze interaction quality
        return calculateSessionPerformanceScore(session);
    }

    private int calculateDaysSinceLastReview(LocalDateTime lastReviewTime) {
        if (lastReviewTime == null) {
            return 0;
        }
        return (int) java.time.Duration.between(lastReviewTime, LocalDateTime.now()).toDays();
    }

    private double calculateRetentionScore(double performanceScore, int daysSinceLastReview) {
        // Apply forgetting curve: R(t) = e^(-t/S) where S is strength
        // Simplified model: retention decreases with time, modulated by performance
        double timeDecay = Math.exp(-daysSinceLastReview / 30.0); // 30-day half-life
        return performanceScore * (0.5 + 0.5 * timeDecay);
    }

    private List<String> identifyStrengthAreas(LearningSession session) {
        List<String> strengths = new ArrayList<>();

        // Analyze outcomes to identify strengths
        session.getOutcomes().forEach(outcome -> {
            if (outcome.getAchievementScore() >= 0.8) {
                strengths.add(outcome.getObjective());
            }
        });

        if (strengths.isEmpty()) {
            strengths.add("Completed session");
        }

        return strengths;
    }

    private List<String> identifyWeaknessAreas(LearningSession session) {
        List<String> weaknesses = new ArrayList<>();

        // Analyze outcomes to identify weaknesses
        session.getOutcomes().forEach(outcome -> {
            if (outcome.getAchievementScore() < 0.6) {
                weaknesses.add(outcome.getObjective());
            }
        });

        return weaknesses;
    }

    private String determineRecommendedAction(double retentionScore, double performanceScore) {
        if (retentionScore < 0.4) {
            return "Immediate review recommended - significant knowledge loss detected";
        } else if (retentionScore < 0.6) {
            return "Schedule review within 2-3 days to reinforce learning";
        } else if (retentionScore < 0.8) {
            return "Continue with scheduled reviews to maintain retention";
        } else {
            return "Excellent retention - extend review intervals";
        }
    }

    private ScheduledSession.Priority determinePriorityFromRetention(RetentionAssessment.RetentionLevel level) {
        return switch (level) {
            case POOR -> ScheduledSession.Priority.HIGH;
            case FAIR -> ScheduledSession.Priority.MEDIUM;
            case GOOD, EXCELLENT -> ScheduledSession.Priority.LOW;
        };
    }

    private int calculateRecommendedFrequency(RetentionAssessment assessment) {
        return switch (assessment.getRetentionLevel()) {
            case POOR -> 5; // 5 times per week
            case FAIR -> 3; // 3 times per week
            case GOOD -> 2; // 2 times per week
            case EXCELLENT -> 1; // Once per week
        };
    }

    /**
     * Internal class to track review data for a topic.
     */
    private static class TopicReviewData {
        private final String topic;
        private double easinessFactor;
        private int reviewCount;
        private LocalDateTime lastReviewTime;
        private double lastPerformanceScore;
        private int lastIntervalDays;

        public TopicReviewData(String topic, double easinessFactor) {
            this.topic = topic;
            this.easinessFactor = easinessFactor;
            this.reviewCount = 0;
            this.lastIntervalDays = 0;
        }

        public String getTopic() { return topic; }
        public double getEasinessFactor() { return easinessFactor; }
        public void setEasinessFactor(double easinessFactor) { this.easinessFactor = easinessFactor; }
        public int getReviewCount() { return reviewCount; }
        public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
        public void incrementReviewCount() { this.reviewCount++; }
        public LocalDateTime getLastReviewTime() { return lastReviewTime; }
        public void setLastReviewTime(LocalDateTime lastReviewTime) { this.lastReviewTime = lastReviewTime; }
        public double getLastPerformanceScore() { return lastPerformanceScore; }
        public void setLastPerformanceScore(double lastPerformanceScore) { this.lastPerformanceScore = lastPerformanceScore; }
        public int getLastIntervalDays() { return lastIntervalDays; }
        public void setLastIntervalDays(int lastIntervalDays) { this.lastIntervalDays = lastIntervalDays; }
    }
}
