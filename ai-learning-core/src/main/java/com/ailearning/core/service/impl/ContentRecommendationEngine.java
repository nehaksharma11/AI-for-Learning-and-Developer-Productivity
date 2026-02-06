package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Engine for recommending learning content based on skill gaps, preferences, and collaborative filtering.
 * Implements personalized content recommendation algorithms including collaborative filtering,
 * reinforcement learning for content sequencing, and personalized difficulty adjustment.
 */
public class ContentRecommendationEngine {

    private static final Logger logger = LoggerFactory.getLogger(ContentRecommendationEngine.class);

    // Content templates for different skill domains
    private static final Map<String, List<ContentTemplate>> CONTENT_TEMPLATES = initializeContentTemplates();
    
    // Collaborative filtering data structures
    private final Map<String, Map<String, Double>> userContentRatings = new HashMap<>();
    private final Map<String, Set<String>> userSimilarityCache = new HashMap<>();
    
    // Reinforcement learning components
    private final Map<String, Double> contentSequenceRewards = new HashMap<>();
    private final Map<String, Integer> contentSequenceAttempts = new HashMap<>();
    private final double learningRate = 0.1;
    private final double explorationRate = 0.15;
    
    // Difficulty adjustment tracking
    private final Map<String, List<Double>> userPerformanceHistory = new HashMap<>();
    private final Map<String, Double> personalizedDifficultyFactors = new HashMap<>();

    /**
     * Recommends learning content based on skill gaps and preferences using collaborative filtering.
     */
    public List<LearningContent> recommendContent(List<SkillGap> skillGaps, LearningPreferences preferences) {
        logger.info("Recommending content for {} skill gaps using collaborative filtering", skillGaps.size());

        List<LearningContent> recommendations = new ArrayList<>();

        for (SkillGap gap : skillGaps) {
            List<LearningContent> gapContent = generateContentForSkillGap(gap, preferences);
            recommendations.addAll(gapContent);
        }

        // Apply collaborative filtering to enhance recommendations
        recommendations = applyCollaborativeFiltering(recommendations, preferences);
        
        // Apply reinforcement learning for optimal sequencing
        recommendations = optimizeContentSequence(recommendations, preferences);

        // Sort by priority and personalize based on preferences
        return personalizeContent(recommendations, preferences);
    }

    /**
     * Applies collaborative filtering to enhance content recommendations based on similar users.
     */
    public List<LearningContent> applyCollaborativeFiltering(List<LearningContent> baseRecommendations, 
                                                           LearningPreferences preferences) {
        logger.debug("Applying collaborative filtering to {} recommendations", baseRecommendations.size());
        
        String userId = preferences.getDeveloperId();
        if (userId == null) {
            return baseRecommendations; // No user context for collaborative filtering
        }
        
        // Find similar users based on learning preferences and performance
        Set<String> similarUsers = findSimilarUsers(userId, preferences);
        
        if (similarUsers.isEmpty()) {
            logger.debug("No similar users found for collaborative filtering");
            return baseRecommendations;
        }
        
        // Get content recommendations from similar users
        Map<String, Double> collaborativeScores = calculateCollaborativeScores(similarUsers, baseRecommendations);
        
        // Enhance base recommendations with collaborative scores
        List<LearningContent> enhancedRecommendations = new ArrayList<>(baseRecommendations);
        
        // Add highly rated content from similar users that wasn't in base recommendations
        for (Map.Entry<String, Double> entry : collaborativeScores.entrySet()) {
            String contentId = entry.getKey();
            Double score = entry.getValue();
            
            if (score > 0.7 && !containsContentId(baseRecommendations, contentId)) {
                LearningContent collaborativeContent = createCollaborativeContent(contentId, score);
                if (collaborativeContent != null) {
                    enhancedRecommendations.add(collaborativeContent);
                }
            }
        }
        
        logger.debug("Enhanced recommendations from {} to {} items using collaborative filtering", 
                    baseRecommendations.size(), enhancedRecommendations.size());
        
        return enhancedRecommendations;
    }

    /**
     * Optimizes content sequence using reinforcement learning based on historical success rates.
     */
    public List<LearningContent> optimizeContentSequence(List<LearningContent> content, 
                                                        LearningPreferences preferences) {
        logger.debug("Optimizing content sequence using reinforcement learning for {} items", content.size());
        
        if (content.size() <= 1) {
            return content; // No sequencing needed
        }
        
        List<LearningContent> optimizedSequence = new ArrayList<>();
        Set<LearningContent> remaining = new HashSet<>(content);
        
        // Start with the content that has the highest expected reward
        LearningContent current = selectNextContentWithRL(null, remaining, preferences);
        optimizedSequence.add(current);
        remaining.remove(current);
        
        // Build sequence using reinforcement learning
        while (!remaining.isEmpty()) {
            LearningContent next = selectNextContentWithRL(current, remaining, preferences);
            optimizedSequence.add(next);
            remaining.remove(next);
            current = next;
        }
        
        logger.debug("Optimized content sequence using RL-based selection");
        return optimizedSequence;
    }

    /**
     * Adjusts content difficulty based on personalized performance history.
     */
    public List<LearningContent> adjustPersonalizedDifficulty(List<LearningContent> content, 
                                                             String userId, 
                                                             LearningPreferences preferences) {
        logger.debug("Adjusting personalized difficulty for user: {} with {} content items", userId, content.size());
        
        if (userId == null) {
            return content; // No personalization without user context
        }
        
        double personalizedFactor = getPersonalizedDifficultyFactor(userId);
        List<Double> performanceHistory = userPerformanceHistory.getOrDefault(userId, new ArrayList<>());
        
        return content.stream()
                .map(item -> adjustContentDifficulty(item, personalizedFactor, performanceHistory, preferences))
                .collect(Collectors.toList());
    }

    /**
     * Updates user performance data for reinforcement learning and difficulty adjustment.
     */
    public void updateUserPerformance(String userId, String contentId, double performanceScore, 
                                    String previousContentId) {
        logger.debug("Updating user performance: user={}, content={}, score={:.2f}", 
                    userId, contentId, performanceScore);
        
        // Update performance history for difficulty adjustment
        userPerformanceHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(performanceScore);
        
        // Update reinforcement learning rewards for content sequencing
        if (previousContentId != null) {
            String sequenceKey = previousContentId + "->" + contentId;
            updateSequenceReward(sequenceKey, performanceScore);
        }
        
        // Update content ratings for collaborative filtering
        userContentRatings.computeIfAbsent(userId, k -> new HashMap<>()).put(contentId, performanceScore);
        
        // Update personalized difficulty factor
        updatePersonalizedDifficultyFactor(userId, performanceScore);
        
        // Clear similarity cache as user data has changed
        userSimilarityCache.remove(userId);
    }

    /**
     * Generates content for a specific skill domain and gap size.
     */
    public List<LearningContent> generateContentForSkill(String skillDomain, double gapSize, LearningPreferences preferences) {
        logger.debug("Generating content for skill: {}, gap size: {:.2f}", skillDomain, gapSize);

        List<ContentTemplate> templates = CONTENT_TEMPLATES.getOrDefault(skillDomain, 
                CONTENT_TEMPLATES.get("general"));

        List<LearningContent> content = new ArrayList<>();

        // Select appropriate content based on gap size
        for (ContentTemplate template : templates) {
            if (isTemplateAppropriate(template, gapSize, preferences)) {
                LearningContent learningContent = createContentFromTemplate(template, skillDomain, preferences);
                content.add(learningContent);
            }
        }

        return content.stream()
                .limit(5) // Limit to top 5 content items per skill
                .collect(Collectors.toList());
    }

    /**
     * Generates session-specific content based on topic and skill level.
     */
    public List<LearningContent> generateSessionContent(String topic, SkillLevel currentSkillLevel, 
                                                       SessionPreferences preferences) {
        logger.debug("Generating session content for topic: {}, skill level: {:.2f}", 
                    topic, currentSkillLevel.getProficiency());

        List<LearningContent> sessionContent = new ArrayList<>();

        // Start with explanation content
        sessionContent.add(createExplanationContent(topic, currentSkillLevel));

        // Add examples based on skill level
        if (currentSkillLevel.getProficiency() < 0.5) {
            sessionContent.add(createBasicExampleContent(topic));
        } else {
            sessionContent.add(createAdvancedExampleContent(topic));
        }

        // Add interactive content if preferred
        if (preferences.isIncludeExercises()) {
            sessionContent.add(createExerciseContent(topic, currentSkillLevel));
        }

        // Add quiz for assessment
        if (preferences.isIncludeQuizzes()) {
            sessionContent.add(createQuizContent(topic, currentSkillLevel));
        }

        return sessionContent;
    }

    /**
     * Generates follow-up content based on completed sessions.
     */
    public List<LearningContent> generateFollowUpContent(List<LearningSession> completedSessions) {
        logger.info("Generating follow-up content based on {} completed sessions", completedSessions.size());

        List<LearningContent> followUpContent = new ArrayList<>();

        // Analyze session outcomes to identify areas needing reinforcement
        Map<String, Double> topicPerformance = analyzeSessionPerformance(completedSessions);

        for (Map.Entry<String, Double> entry : topicPerformance.entrySet()) {
            String topic = entry.getKey();
            Double performance = entry.getValue();

            if (performance < 0.7) { // Needs reinforcement
                followUpContent.add(createReinforcementContent(topic, performance));
            } else if (performance > 0.9) { // Ready for advanced content
                followUpContent.add(createAdvancedFollowUpContent(topic));
            }
        }

        return followUpContent.stream()
                .limit(3) // Limit follow-up recommendations
                .collect(Collectors.toList());
    }

    private List<LearningContent> generateContentForSkillGap(SkillGap gap, LearningPreferences preferences) {
        String skillDomain = gap.getSkillDomain();
        double gapSize = gap.getGapSize();

        List<ContentTemplate> templates = CONTENT_TEMPLATES.getOrDefault(skillDomain, 
                CONTENT_TEMPLATES.get("general"));

        return templates.stream()
                .filter(template -> isTemplateAppropriate(template, gapSize, preferences))
                .map(template -> createContentFromTemplate(template, skillDomain, preferences))
                .limit(3) // Limit content per gap
                .collect(Collectors.toList());
    }

    private boolean isTemplateAppropriate(ContentTemplate template, double gapSize, LearningPreferences preferences) {
        // Check difficulty match
        if (gapSize > 0.6 && template.difficulty < 0.5) return false; // Large gap needs advanced content
        if (gapSize < 0.3 && template.difficulty > 0.7) return false; // Small gap doesn't need advanced content

        // Check content type preferences
        if (preferences.getPreferredContentTypesAsStrings().contains("video") && template.type != LearningContent.ContentType.VIDEO) {
            return template.type == LearningContent.ContentType.EXPLANATION; // Allow explanations as fallback
        }

        return true;
    }

    private LearningContent createContentFromTemplate(ContentTemplate template, String skillDomain, 
                                                     LearningPreferences preferences) {
        String contentText = template.content.replace("{skill}", skillDomain)
                .replace("{detail_level}", preferences.getDetailLevel());

        return LearningContent.builder()
                .id(UUID.randomUUID().toString())
                .type(template.type)
                .title(template.title.replace("{skill}", skillDomain))
                .content(contentText)
                .difficulty(mapDifficultyToEnum(template.difficulty))
                .estimatedMinutes(template.estimatedMinutes)
                .prerequisites(template.prerequisites)
                .build();
    }

    private LearningContent.DifficultyLevel mapDifficultyToEnum(double difficulty) {
        if (difficulty >= 0.8) return LearningContent.DifficultyLevel.EXPERT;
        if (difficulty >= 0.6) return LearningContent.DifficultyLevel.ADVANCED;
        if (difficulty >= 0.4) return LearningContent.DifficultyLevel.INTERMEDIATE;
        return LearningContent.DifficultyLevel.BEGINNER;
    }

    private List<LearningContent> personalizeContent(List<LearningContent> content, LearningPreferences preferences) {
        return content.stream()
                .sorted((c1, c2) -> {
                    // Prioritize preferred content types
                    boolean c1Preferred = preferences.getPreferredContentTypesAsStrings().contains(c1.getType().name().toLowerCase());
                    boolean c2Preferred = preferences.getPreferredContentTypesAsStrings().contains(c2.getType().name().toLowerCase());
                    
                    if (c1Preferred && !c2Preferred) return -1;
                    if (!c1Preferred && c2Preferred) return 1;
                    
                    // Then sort by difficulty preference
                    double c1DiffMatch = Math.abs(c1.getDifficulty() - preferences.getPreferredDifficulty());
                    double c2DiffMatch = Math.abs(c2.getDifficulty() - preferences.getPreferredDifficulty());
                    
                    return Double.compare(c1DiffMatch, c2DiffMatch);
                })
                .collect(Collectors.toList());
    }

    private LearningContent createExplanationContent(String topic, SkillLevel skillLevel) {
        String difficulty = skillLevel.getProficiency() > 0.6 ? "advanced" : "beginner";
        
        return LearningContent.builder()
                .id(UUID.randomUUID().toString())
                .type(LearningContent.ContentType.EXPLANATION)
                .title("Understanding " + topic)
                .content(String.format("Comprehensive %s explanation of %s concepts and principles", difficulty, topic))
                .difficulty(mapDifficultyToEnum(skillLevel.getProficiency()))
                .estimatedMinutes(15)
                .prerequisites(List.of())
                .build();
    }

    private LearningContent createBasicExampleContent(String topic) {
        return LearningContent.builder()
                .id(UUID.randomUUID().toString())
                .type(LearningContent.ContentType.CODE_EXAMPLE)
                .title("Basic " + topic + " Examples")
                .content("Step-by-step examples demonstrating fundamental " + topic + " patterns")
                .difficulty(LearningContent.DifficultyLevel.BEGINNER)
                .estimatedMinutes(20)
                .prerequisites(List.of())
                .build();
    }

    private LearningContent createAdvancedExampleContent(String topic) {
        return LearningContent.builder()
                .id(UUID.randomUUID().toString())
                .type(LearningContent.ContentType.CODE_EXAMPLE)
                .title("Advanced " + topic + " Patterns")
                .content("Complex real-world examples showcasing advanced " + topic + " techniques")
                .difficulty(LearningContent.DifficultyLevel.ADVANCED)
                .estimatedMinutes(30)
                .prerequisites(List.of("basic-" + topic.toLowerCase()))
                .build();
    }

    private LearningContent createExerciseContent(String topic, SkillLevel skillLevel) {
        return LearningContent.builder()
                .id(UUID.randomUUID().toString())
                .type(LearningContent.ContentType.EXERCISE)
                .title("Practice " + topic + " Exercise")
                .content("Hands-on coding exercise to practice " + topic + " skills")
                .difficulty(mapDifficultyToEnum(Math.min(1.0, skillLevel.getProficiency() + 0.1))) // Slightly above current level
                .estimatedMinutes(25)
                .prerequisites(List.of())
                .build();
    }

    private LearningContent createQuizContent(String topic, SkillLevel skillLevel) {
        return LearningContent.builder()
                .id(UUID.randomUUID().toString())
                .type(LearningContent.ContentType.QUIZ)
                .title(topic + " Knowledge Check")
                .content("Interactive quiz to assess understanding of " + topic + " concepts")
                .difficulty(mapDifficultyToEnum(skillLevel.getProficiency()))
                .estimatedMinutes(10)
                .prerequisites(List.of())
                .build();
    }

    private LearningContent createReinforcementContent(String topic, double performance) {
        return LearningContent.builder()
                .id(UUID.randomUUID().toString())
                .type(LearningContent.ContentType.EXERCISE)
                .title("Reinforce " + topic + " Skills")
                .content("Additional practice to strengthen understanding of " + topic)
                .difficulty(mapDifficultyToEnum(Math.max(0.3, performance - 0.1))) // Slightly easier than previous performance
                .estimatedMinutes(20)
                .prerequisites(List.of())
                .build();
    }

    private LearningContent createAdvancedFollowUpContent(String topic) {
        return LearningContent.builder()
                .id(UUID.randomUUID().toString())
                .type(LearningContent.ContentType.CODE_EXAMPLE)
                .title("Advanced " + topic + " Techniques")
                .content("Explore advanced patterns and best practices in " + topic)
                .difficulty(LearningContent.DifficultyLevel.EXPERT)
                .estimatedMinutes(35)
                .prerequisites(List.of("intermediate-" + topic.toLowerCase()))
                .build();
    }

    private Map<String, Double> analyzeSessionPerformance(List<LearningSession> sessions) {
        Map<String, List<Double>> topicScores = new HashMap<>();

        for (LearningSession session : sessions) {
            String topic = session.getTopic();
            double avgScore = session.getOutcomes().stream()
                    .mapToDouble(outcome -> outcome.getAchievementScore())
                    .average()
                    .orElse(0.5);
            
            topicScores.computeIfAbsent(topic, k -> new ArrayList<>()).add(avgScore);
        }

        Map<String, Double> avgPerformance = new HashMap<>();
        topicScores.forEach((topic, scores) -> {
            double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.5);
            avgPerformance.put(topic, avg);
        });

        return avgPerformance;
    }

    // Collaborative Filtering Helper Methods

    private Set<String> findSimilarUsers(String userId, LearningPreferences preferences) {
        if (userSimilarityCache.containsKey(userId)) {
            return userSimilarityCache.get(userId);
        }
        
        Set<String> similarUsers = new HashSet<>();
        Map<String, Double> userRatings = userContentRatings.get(userId);
        
        if (userRatings == null || userRatings.isEmpty()) {
            return similarUsers; // No data for similarity calculation
        }
        
        // Calculate similarity with other users using Pearson correlation
        for (Map.Entry<String, Map<String, Double>> otherUserEntry : userContentRatings.entrySet()) {
            String otherUserId = otherUserEntry.getKey();
            if (otherUserId.equals(userId)) continue;
            
            Map<String, Double> otherUserRatings = otherUserEntry.getValue();
            double similarity = calculateUserSimilarity(userRatings, otherUserRatings);
            
            if (similarity > 0.6) { // Similarity threshold
                similarUsers.add(otherUserId);
            }
        }
        
        // Cache the result
        userSimilarityCache.put(userId, similarUsers);
        return similarUsers;
    }

    private double calculateUserSimilarity(Map<String, Double> user1Ratings, Map<String, Double> user2Ratings) {
        Set<String> commonContent = new HashSet<>(user1Ratings.keySet());
        commonContent.retainAll(user2Ratings.keySet());
        
        if (commonContent.size() < 2) {
            return 0.0; // Need at least 2 common items for meaningful similarity
        }
        
        // Calculate Pearson correlation coefficient
        double sum1 = 0, sum2 = 0, sum1Sq = 0, sum2Sq = 0, pSum = 0;
        int n = commonContent.size();
        
        for (String contentId : commonContent) {
            double rating1 = user1Ratings.get(contentId);
            double rating2 = user2Ratings.get(contentId);
            
            sum1 += rating1;
            sum2 += rating2;
            sum1Sq += rating1 * rating1;
            sum2Sq += rating2 * rating2;
            pSum += rating1 * rating2;
        }
        
        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - sum1 * sum1 / n) * (sum2Sq - sum2 * sum2 / n));
        
        return den == 0 ? 0 : num / den;
    }

    private Map<String, Double> calculateCollaborativeScores(Set<String> similarUsers, 
                                                           List<LearningContent> baseRecommendations) {
        Map<String, Double> collaborativeScores = new HashMap<>();
        Map<String, List<Double>> contentScores = new HashMap<>();
        
        // Collect ratings from similar users
        for (String similarUserId : similarUsers) {
            Map<String, Double> userRatings = userContentRatings.get(similarUserId);
            if (userRatings != null) {
                for (Map.Entry<String, Double> entry : userRatings.entrySet()) {
                    String contentId = entry.getKey();
                    Double rating = entry.getValue();
                    
                    contentScores.computeIfAbsent(contentId, k -> new ArrayList<>()).add(rating);
                }
            }
        }
        
        // Calculate average scores for each content item
        for (Map.Entry<String, List<Double>> entry : contentScores.entrySet()) {
            String contentId = entry.getKey();
            List<Double> scores = entry.getValue();
            
            double avgScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            collaborativeScores.put(contentId, avgScore);
        }
        
        return collaborativeScores;
    }

    private boolean containsContentId(List<LearningContent> content, String contentId) {
        return content.stream().anyMatch(item -> item.getId().equals(contentId));
    }

    private LearningContent createCollaborativeContent(String contentId, double score) {
        // In a real implementation, this would fetch content from a repository
        // For now, create a placeholder content item
        return LearningContent.builder()
                .id(contentId)
                .type(LearningContent.ContentType.EXPLANATION)
                .title("Recommended Content")
                .content("Content recommended by similar learners")
                .difficulty(LearningContent.DifficultyLevel.INTERMEDIATE)
                .estimatedMinutes(20)
                .prerequisites(List.of())
                .build();
    }

    // Reinforcement Learning Helper Methods

    private LearningContent selectNextContentWithRL(LearningContent current, Set<LearningContent> remaining, 
                                                   LearningPreferences preferences) {
        if (remaining.isEmpty()) return null;
        
        // Epsilon-greedy strategy: explore vs exploit
        if (Math.random() < explorationRate) {
            // Exploration: random selection
            return remaining.iterator().next();
        }
        
        // Exploitation: select content with highest expected reward
        LearningContent bestContent = null;
        double bestReward = Double.NEGATIVE_INFINITY;
        
        for (LearningContent candidate : remaining) {
            double expectedReward = calculateExpectedReward(current, candidate, preferences);
            if (expectedReward > bestReward) {
                bestReward = expectedReward;
                bestContent = candidate;
            }
        }
        
        return bestContent != null ? bestContent : remaining.iterator().next();
    }

    private double calculateExpectedReward(LearningContent current, LearningContent candidate, 
                                         LearningPreferences preferences) {
        String sequenceKey = (current != null ? current.getId() : "START") + "->" + candidate.getId();
        
        // Get historical reward for this sequence
        double historicalReward = contentSequenceRewards.getOrDefault(sequenceKey, 0.5); // Default neutral reward
        
        // Adjust based on user preferences
        double preferenceBonus = calculatePreferenceBonus(candidate, preferences);
        
        // Adjust based on difficulty progression
        double difficultyBonus = calculateDifficultyProgressionBonus(current, candidate);
        
        return historicalReward + preferenceBonus + difficultyBonus;
    }

    private double calculatePreferenceBonus(LearningContent content, LearningPreferences preferences) {
        double bonus = 0.0;
        
        // Bonus for preferred content types
        if (preferences.getPreferredContentTypesAsStrings().contains(content.getType().name().toLowerCase())) {
            bonus += 0.1;
        }
        
        // Bonus for preferred difficulty
        double difficultyMatch = 1.0 - Math.abs(content.getDifficulty() - preferences.getPreferredDifficulty());
        bonus += difficultyMatch * 0.1;
        
        return bonus;
    }

    private double calculateDifficultyProgressionBonus(LearningContent current, LearningContent candidate) {
        if (current == null) return 0.0;
        
        double currentDifficulty = current.getDifficulty();
        double candidateDifficulty = candidate.getDifficulty();
        
        // Prefer gradual difficulty increase
        double difficultyDelta = candidateDifficulty - currentDifficulty;
        
        if (difficultyDelta >= 0 && difficultyDelta <= 0.2) {
            return 0.1; // Good progression
        } else if (difficultyDelta > 0.2) {
            return -0.1; // Too big jump
        } else {
            return 0.0; // Neutral for difficulty decrease
        }
    }

    private void updateSequenceReward(String sequenceKey, double performanceScore) {
        // Update reward using exponential moving average
        double currentReward = contentSequenceRewards.getOrDefault(sequenceKey, 0.5);
        double newReward = currentReward + learningRate * (performanceScore - currentReward);
        
        contentSequenceRewards.put(sequenceKey, newReward);
        contentSequenceAttempts.put(sequenceKey, contentSequenceAttempts.getOrDefault(sequenceKey, 0) + 1);
    }

    // Personalized Difficulty Adjustment Helper Methods

    private double getPersonalizedDifficultyFactor(String userId) {
        return personalizedDifficultyFactors.getOrDefault(userId, 1.0); // Default factor
    }

    private LearningContent adjustContentDifficulty(LearningContent content, double personalizedFactor, 
                                                  List<Double> performanceHistory, LearningPreferences preferences) {
        if (performanceHistory.isEmpty()) {
            return content; // No adjustment without performance data
        }
        
        // Calculate recent performance trend
        double recentPerformance = calculateRecentPerformanceTrend(performanceHistory);
        
        // Determine difficulty adjustment
        double adjustmentFactor = calculateDifficultyAdjustment(recentPerformance, personalizedFactor);
        
        // Apply adjustment to content difficulty
        double originalDifficulty = content.getDifficulty();
        double adjustedDifficulty = Math.max(0.1, Math.min(1.0, originalDifficulty * adjustmentFactor));
        
        // Create adjusted content if significant change
        if (Math.abs(adjustedDifficulty - originalDifficulty) > 0.1) {
            return content.toBuilder()
                    .difficulty(mapDifficultyToEnum(adjustedDifficulty))
                    .build();
        }
        
        return content;
    }

    private double calculateRecentPerformanceTrend(List<Double> performanceHistory) {
        int historySize = performanceHistory.size();
        int recentWindow = Math.min(5, historySize); // Look at last 5 performances
        
        if (recentWindow < 2) {
            return performanceHistory.get(historySize - 1); // Single recent performance
        }
        
        // Calculate weighted average with more weight on recent performances
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (int i = 0; i < recentWindow; i++) {
            int index = historySize - recentWindow + i;
            double weight = i + 1; // More weight for recent items
            weightedSum += performanceHistory.get(index) * weight;
            totalWeight += weight;
        }
        
        return weightedSum / totalWeight;
    }

    private double calculateDifficultyAdjustment(double recentPerformance, double personalizedFactor) {
        // Base adjustment on performance
        double performanceAdjustment;
        if (recentPerformance > 0.8) {
            performanceAdjustment = 1.2; // Increase difficulty for high performers
        } else if (recentPerformance < 0.5) {
            performanceAdjustment = 0.8; // Decrease difficulty for struggling learners
        } else {
            performanceAdjustment = 1.0; // No adjustment for average performance
        }
        
        // Combine with personalized factor
        return performanceAdjustment * personalizedFactor;
    }

    private void updatePersonalizedDifficultyFactor(String userId, double performanceScore) {
        double currentFactor = personalizedDifficultyFactors.getOrDefault(userId, 1.0);
        
        // Adjust factor based on performance
        double adjustment = 0.0;
        if (performanceScore > 0.8) {
            adjustment = 0.05; // Increase factor for high performance
        } else if (performanceScore < 0.5) {
            adjustment = -0.05; // Decrease factor for low performance
        }
        
        double newFactor = Math.max(0.5, Math.min(2.0, currentFactor + adjustment));
        personalizedDifficultyFactors.put(userId, newFactor);
    }

    private static Map<String, List<ContentTemplate>> initializeContentTemplates() {
        Map<String, List<ContentTemplate>> templates = new HashMap<>();

        // Java content templates
        templates.put("java", List.of(
                new ContentTemplate("Java Fundamentals", LearningContent.ContentType.EXPLANATION,
                        "Learn core {skill} concepts including OOP principles", 0.3, 20, List.of()),
                new ContentTemplate("Advanced {skill} Patterns", LearningContent.ContentType.EXAMPLE,
                        "Explore design patterns and best practices in {skill}", 0.7, 30, List.of("java-basics")),
                new ContentTemplate("{skill} Coding Exercise", LearningContent.ContentType.EXERCISE,
                        "Practice {skill} programming with hands-on exercises", 0.5, 25, List.of())
        ));

        // Spring Boot content templates
        templates.put("spring-boot", List.of(
                new ContentTemplate("Spring Boot Basics", LearningContent.ContentType.EXPLANATION,
                        "Introduction to {skill} framework and dependency injection", 0.4, 25, List.of("java")),
                new ContentTemplate("REST API with {skill}", LearningContent.ContentType.EXAMPLE,
                        "Build RESTful services using {skill}", 0.6, 35, List.of("spring-boot-basics")),
                new ContentTemplate("{skill} Project", LearningContent.ContentType.EXERCISE,
                        "Create a complete application using {skill}", 0.8, 60, List.of("rest-api"))
        ));

        // Testing content templates
        templates.put("testing", List.of(
                new ContentTemplate("Unit Testing Fundamentals", LearningContent.ContentType.EXPLANATION,
                        "Learn {skill} principles and best practices", 0.3, 20, List.of()),
                new ContentTemplate("Test-Driven Development", LearningContent.ContentType.EXAMPLE,
                        "Practice TDD methodology with {skill}", 0.6, 30, List.of("unit-testing")),
                new ContentTemplate("Advanced {skill} Techniques", LearningContent.ContentType.EXERCISE,
                        "Master mocking, integration testing, and {skill} strategies", 0.8, 40, List.of("tdd"))
        ));

        // General content templates (fallback)
        templates.put("general", List.of(
                new ContentTemplate("Introduction to {skill}", LearningContent.ContentType.EXPLANATION,
                        "Basic concepts and principles of {skill}", 0.3, 15, List.of()),
                new ContentTemplate("{skill} Examples", LearningContent.ContentType.EXAMPLE,
                        "Practical examples demonstrating {skill} usage", 0.5, 20, List.of()),
                new ContentTemplate("{skill} Practice", LearningContent.ContentType.EXERCISE,
                        "Hands-on practice with {skill}", 0.6, 25, List.of())
        ));

        return templates;
    }

    /**
     * Internal class representing a content template.
     */
    private static class ContentTemplate {
        final String title;
        final LearningContent.ContentType type;
        final String content;
        final double difficulty;
        final int estimatedMinutes;
        final List<String> prerequisites;

        ContentTemplate(String title, LearningContent.ContentType type, String content,
                       double difficulty, int estimatedMinutes, List<String> prerequisites) {
            this.title = title;
            this.type = type;
            this.content = content;
            this.difficulty = difficulty;
            this.estimatedMinutes = estimatedMinutes;
            this.prerequisites = prerequisites != null ? prerequisites : List.of();
        }
    }
}