package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Engine for analyzing learning patterns and generating insights for adaptive learning.
 * Provides analytics, progress tracking, and schedule optimization.
 */
public class LearningAnalyticsEngine {

    private static final Logger logger = LoggerFactory.getLogger(LearningAnalyticsEngine.class);

    /**
     * Analyzes learning progress and updates patterns for future recommendations.
     */
    public void analyzeProgress(LearningProgress progress) {
        logger.debug("Analyzing progress for skill: {}, completion: {:.1f}%", 
                    progress.getSkillDomain(), progress.getCompletionPercentage());

        // Analyze learning velocity
        double learningVelocity = calculateLearningVelocity(progress);
        
        // Identify learning patterns
        LearningPattern pattern = identifyLearningPattern(progress);
        
        // Update recommendations based on analysis
        updateRecommendationWeights(progress, pattern, learningVelocity);

        logger.debug("Learning velocity: {:.3f}, pattern: {}", learningVelocity, pattern);
    }

    /**
     * Optimizes learning schedule based on historical performance and preferences.
     */
    public LearningSchedule optimizeSchedule(String developerId, List<LearningSession> learningHistory) {
        logger.info("Optimizing learning schedule for developer: {} based on {} sessions", 
                   developerId, learningHistory.size());

        // Analyze optimal learning times
        Map<Integer, Double> hourlyPerformance = analyzeHourlyPerformance(learningHistory);
        
        // Analyze session duration preferences
        OptimalSessionMetrics sessionMetrics = analyzeOptimalSessionLength(learningHistory);
        
        // Generate schedule recommendations
        List<ScheduledSession> recommendedSessions = generateOptimalSchedule(
                developerId, hourlyPerformance, sessionMetrics);

        return LearningSchedule.builder()
                .id(UUID.randomUUID().toString())
                .developerId(developerId)
                .scheduledSessions(recommendedSessions)
                .recommendedTimeSlots(optimalHours.stream().map(String::valueOf).collect(Collectors.toList()))
                .optimalSessionDuration(sessionMetrics.optimalDuration)
                .recommendedFrequency(3) // Default to 3 sessions per week
                .createdAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .scheduleRationale("Optimized based on historical performance patterns")
                .build();
    }

    /**
     * Generates comprehensive learning analytics for a developer.
     */
    public LearningAnalytics generateAnalytics(String developerId, String timeRange) {
        logger.info("Generating learning analytics for developer: {}, time range: {}", developerId, timeRange);

        // This would typically fetch data from a repository
        // For now, we'll generate sample analytics
        
        List<LearningTrend> trends = generateLearningTrends(developerId, timeRange);
        Map<String, Double> skillProgression = generateSkillProgression(developerId);
        Map<String, Integer> activityMetrics = generateActivityMetrics(developerId, timeRange);

        return LearningAnalytics.builder()
                .id(UUID.randomUUID().toString())
                .developerId(developerId)
                .timeRange(timeRange)
                .trends(trends)
                .skillProgression(skillProgression)
                .totalLearningHours(activityMetrics.getOrDefault("totalHours", 0))
                .completedSessions(activityMetrics.getOrDefault("completedSessions", 0))
                .averageSessionDuration(activityMetrics.getOrDefault("avgDuration", 30))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private double calculateLearningVelocity(LearningProgress progress) {
        // Calculate velocity based on completion rate and time spent
        double timeSpentHours = progress.getTimeSpentMinutes() / 60.0;
        if (timeSpentHours == 0) return 0.0;
        
        return progress.getCompletionPercentage() / timeSpentHours;
    }

    private LearningPattern identifyLearningPattern(LearningProgress progress) {
        // Analyze progress patterns
        double completionRate = progress.getCompletionPercentage();
        int attempts = progress.getAttempts();
        
        if (completionRate > 90 && attempts <= 2) {
            return LearningPattern.FAST_LEARNER;
        } else if (completionRate > 70 && attempts <= 3) {
            return LearningPattern.STEADY_LEARNER;
        } else if (attempts > 5) {
            return LearningPattern.STRUGGLING_LEARNER;
        } else {
            return LearningPattern.AVERAGE_LEARNER;
        }
    }

    private void updateRecommendationWeights(LearningProgress progress, LearningPattern pattern, 
                                           double learningVelocity) {
        // Update internal recommendation weights based on learning patterns
        // This would typically update a machine learning model or recommendation weights
        
        logger.debug("Updated recommendation weights for pattern: {}, velocity: {:.3f}", 
                    pattern, learningVelocity);
    }

    private Map<Integer, Double> analyzeHourlyPerformance(List<LearningSession> sessions) {
        Map<Integer, List<Double>> hourlyScores = new HashMap<>();
        
        for (LearningSession session : sessions) {
            int hour = session.getStartTime().getHour();
            double avgScore = session.getOutcomes().stream()
                    .mapToDouble(outcome -> outcome.getAchievementScore())
                    .average()
                    .orElse(0.5);
            
            hourlyScores.computeIfAbsent(hour, k -> new ArrayList<>()).add(avgScore);
        }

        Map<Integer, Double> hourlyPerformance = new HashMap<>();
        hourlyScores.forEach((hour, scores) -> {
            double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.5);
            hourlyPerformance.put(hour, avg);
        });

        return hourlyPerformance;
    }

    private OptimalSessionMetrics analyzeOptimalSessionLength(List<LearningSession> sessions) {
        if (sessions.isEmpty()) {
            return new OptimalSessionMetrics(30, 0.5); // Default values
        }

        // Analyze correlation between session length and performance
        Map<Integer, List<Double>> durationPerformance = new HashMap<>();
        
        for (LearningSession session : sessions) {
            int duration = session.getDurationMinutes();
            double avgScore = session.getOutcomes().stream()
                    .mapToDouble(outcome -> outcome.getAchievementScore())
                    .average()
                    .orElse(0.5);
            
            // Group by duration ranges
            int durationRange = (duration / 15) * 15; // Round to nearest 15 minutes
            durationPerformance.computeIfAbsent(durationRange, k -> new ArrayList<>()).add(avgScore);
        }

        // Find optimal duration
        int optimalDuration = 30;
        double bestPerformance = 0.0;
        
        for (Map.Entry<Integer, List<Double>> entry : durationPerformance.entrySet()) {
            double avgPerformance = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            
            if (avgPerformance > bestPerformance) {
                bestPerformance = avgPerformance;
                optimalDuration = entry.getKey();
            }
        }

        return new OptimalSessionMetrics(optimalDuration, bestPerformance);
    }

    private List<ScheduledSession> generateOptimalSchedule(String developerId, 
                                                          Map<Integer, Double> hourlyPerformance,
                                                          OptimalSessionMetrics sessionMetrics) {
        List<ScheduledSession> sessions = new ArrayList<>();
        
        // Find top 3 performing hours
        List<Integer> optimalHours = hourlyPerformance.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Generate sessions for the next week
        LocalDateTime now = LocalDateTime.now();
        for (int day = 1; day <= 7; day++) {
            for (Integer hour : optimalHours) {
                LocalDateTime sessionTime = now.plusDays(day).withHour(hour).withMinute(0).withSecond(0);
                
                ScheduledSession session = ScheduledSession.builder()
                        .id(UUID.randomUUID().toString())
                        .developerId(developerId)
                        .scheduledTime(sessionTime)
                        .estimatedDuration(sessionMetrics.optimalDuration)
                        .priority(calculateSessionPriority(hour, hourlyPerformance))
                        .sessionType("optimized")
                        .build();
                
                sessions.add(session);
                
                if (sessions.size() >= 5) break; // Limit to 5 sessions per week
            }
            if (sessions.size() >= 5) break;
        }

        return sessions;
    }

    private String calculateSessionPriority(Integer hour, Map<Integer, Double> hourlyPerformance) {
        double performance = hourlyPerformance.getOrDefault(hour, 0.5);
        if (performance > 0.8) return "high";
        if (performance > 0.6) return "medium";
        return "low";
    }

    private List<LearningTrend> generateLearningTrends(String developerId, String timeRange) {
        // Generate sample trends - in a real implementation, this would analyze historical data
        List<LearningTrend> trends = new ArrayList<>();
        
        trends.add(LearningTrend.builder()
                .id(UUID.randomUUID().toString())
                .skillDomain("java")
                .trendType("skill_improvement")
                .value(0.15) // 15% improvement
                .timeRange(timeRange)
                .description("Steady improvement in Java programming skills")
                .build());
        
        trends.add(LearningTrend.builder()
                .id(UUID.randomUUID().toString())
                .skillDomain("testing")
                .trendType("learning_velocity")
                .value(1.2) // 20% faster learning
                .timeRange(timeRange)
                .description("Increased learning velocity in testing practices")
                .build());

        return trends;
    }

    private Map<String, Double> generateSkillProgression(String developerId) {
        // Generate sample skill progression data
        Map<String, Double> progression = new HashMap<>();
        progression.put("java", 0.75);
        progression.put("spring-boot", 0.60);
        progression.put("testing", 0.45);
        progression.put("database", 0.30);
        
        return progression;
    }

    private Map<String, Integer> generateActivityMetrics(String developerId, String timeRange) {
        // Generate sample activity metrics
        Map<String, Integer> metrics = new HashMap<>();
        metrics.put("totalHours", 25);
        metrics.put("completedSessions", 12);
        metrics.put("avgDuration", 35);
        
        return metrics;
    }

    /**
     * Enum representing different learning patterns.
     */
    private enum LearningPattern {
        FAST_LEARNER,
        STEADY_LEARNER,
        AVERAGE_LEARNER,
        STRUGGLING_LEARNER
    }

    /**
     * Internal class for optimal session metrics.
     */
    private static class OptimalSessionMetrics {
        final int optimalDuration;
        final double performance;

        OptimalSessionMetrics(int optimalDuration, double performance) {
            this.optimalDuration = optimalDuration;
            this.performance = performance;
        }
    }
}