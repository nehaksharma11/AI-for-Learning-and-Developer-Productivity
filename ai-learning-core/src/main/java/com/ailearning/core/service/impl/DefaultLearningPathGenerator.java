package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.LearningPathGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Default implementation of LearningPathGenerator.
 * Implements adaptive learning algorithms including Bayesian Knowledge Tracing,
 * collaborative filtering, and personalized content recommendation.
 */
public class DefaultLearningPathGenerator implements LearningPathGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLearningPathGenerator.class);

    private final SkillAssessmentEngine skillAssessmentEngine;
    private final BayesianKnowledgeTracer knowledgeTracer;
    private final ContentRecommendationEngine contentEngine;
    private final LearningAnalyticsEngine analyticsEngine;

    public DefaultLearningPathGenerator() {
        this.skillAssessmentEngine = new SkillAssessmentEngine();
        this.knowledgeTracer = new BayesianKnowledgeTracer();
        this.contentEngine = new ContentRecommendationEngine();
        this.analyticsEngine = new LearningAnalyticsEngine();
    }

    @Override
    public CompletableFuture<LearningPath> generatePath(DeveloperProfile profile, ProjectContext context) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Generating learning path for developer: {}", profile.getId());
            
            try {
                // 1. Identify skill gaps based on profile and project context
                List<SkillGap> skillGaps = identifySkillGapsSync(profile, context, profile.getCurrentGoals().stream()
                        .map(LearningGoal::getTitle)
                        .collect(Collectors.toList()));

                // 2. Prioritize skill gaps based on project needs and career goals
                List<SkillGap> prioritizedGaps = prioritizeSkillGaps(skillGaps, context);

                // 3. Generate learning modules for each priority skill gap
                List<LearningModule> modules = generateLearningModules(prioritizedGaps, profile.getLearningPreferences());

                // 4. Create the learning path
                String pathId = UUID.randomUUID().toString();
                String title = generatePathTitle(prioritizedGaps, context);
                String description = generatePathDescription(prioritizedGaps, context);
                
                List<String> targetSkills = prioritizedGaps.stream()
                        .map(SkillGap::getSkillDomain)
                        .collect(Collectors.toList());

                int estimatedDuration = modules.stream()
                        .mapToInt(LearningModule::getEstimatedMinutes)
                        .sum();

                LearningPath.DifficultyLevel difficulty = determineDifficulty(prioritizedGaps, profile);

                LearningPath path = LearningPath.builder()
                        .id(pathId)
                        .developerId(profile.getId())
                        .title(title)
                        .description(description)
                        .modules(modules)
                        .targetSkills(targetSkills)
                        .estimatedDurationMinutes(estimatedDuration)
                        .difficulty(difficulty)
                        .build();

                logger.info("Generated learning path with {} modules, estimated duration: {} minutes", 
                           modules.size(), estimatedDuration);
                
                return path;
                
            } catch (Exception e) {
                logger.error("Error generating learning path for developer: {}", profile.getId(), e);
                throw new RuntimeException("Failed to generate learning path", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateProgress(String sessionId, LearningProgress progress) {
        return CompletableFuture.runAsync(() -> {
            logger.info("Updating progress for session: {}", sessionId);
            
            try {
                // Update Bayesian Knowledge Tracing model with new evidence
                knowledgeTracer.updateKnowledge(progress);
                
                // Analyze progress patterns for adaptive recommendations
                analyticsEngine.analyzeProgress(progress);
                
                // Update content recommendation engine with performance data
                if (progress.getDeveloperId() != null && progress.getContentId() != null) {
                    double performanceScore = calculatePerformanceScore(progress);
                    contentEngine.updateUserPerformance(
                            progress.getDeveloperId(), 
                            progress.getContentId(), 
                            performanceScore, 
                            progress.getPreviousContentId()
                    );
                }
                
                logger.info("Progress updated successfully for session: {}", sessionId);
                
            } catch (Exception e) {
                logger.error("Error updating progress for session: {}", sessionId, e);
                throw new RuntimeException("Failed to update progress", e);
            }
        });
    }

    /**
     * Calculates a performance score from learning progress data.
     */
    private double calculatePerformanceScore(LearningProgress progress) {
        double completionScore = progress.getCompletionPercentage() / 100.0;
        double accuracyScore = progress.getAccuracyScore();
        double timeEfficiencyScore = Math.min(1.0, progress.getExpectedTimeMinutes() / (double) progress.getActualTimeMinutes());
        
        // Weighted average: completion (40%), accuracy (40%), time efficiency (20%)
        return (completionScore * 0.4) + (accuracyScore * 0.4) + (timeEfficiencyScore * 0.2);
    }

    @Override
    public CompletableFuture<List<LearningContent>> recommendContent(List<SkillGap> skillGaps, LearningPreferences preferences) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Recommending content for {} skill gaps using advanced algorithms", skillGaps.size());
            
            try {
                // Get base recommendations
                List<LearningContent> baseRecommendations = contentEngine.recommendContent(skillGaps, preferences);
                
                // Apply collaborative filtering if user ID is available
                if (preferences.getDeveloperId() != null) {
                    baseRecommendations = contentEngine.applyCollaborativeFiltering(baseRecommendations, preferences);
                }
                
                // Optimize content sequence using reinforcement learning
                baseRecommendations = contentEngine.optimizeContentSequence(baseRecommendations, preferences);
                
                // Apply personalized difficulty adjustment
                if (preferences.getDeveloperId() != null) {
                    baseRecommendations = contentEngine.adjustPersonalizedDifficulty(
                            baseRecommendations, preferences.getDeveloperId(), preferences);
                }
                
                return baseRecommendations;
                
            } catch (Exception e) {
                logger.error("Error recommending content", e);
                throw new RuntimeException("Failed to recommend content", e);
            }
        });
    }

    @Override
    public CompletableFuture<SkillAssessment> assessSkillLevel(List<CodeSample> codeSamples, String domain) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Assessing skill level for domain: {} with {} code samples", domain, codeSamples.size());
            
            try {
                return skillAssessmentEngine.assessSkill(codeSamples, domain);
            } catch (Exception e) {
                logger.error("Error assessing skill level for domain: {}", domain, e);
                throw new RuntimeException("Failed to assess skill level", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<SkillGap>> identifySkillGaps(DeveloperProfile profile, ProjectContext projectContext, List<String> careerGoals) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Identifying skill gaps for developer: {}", profile.getId());
            
            try {
                return identifySkillGapsSync(profile, projectContext, careerGoals);
            } catch (Exception e) {
                logger.error("Error identifying skill gaps for developer: {}", profile.getId(), e);
                throw new RuntimeException("Failed to identify skill gaps", e);
            }
        });
    }

    @Override
    public CompletableFuture<LearningSession> createLearningSession(String developerId, String topic, SkillLevel currentSkillLevel, SessionPreferences sessionPreferences) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Creating learning session for developer: {}, topic: {}", developerId, topic);
            
            try {
                String sessionId = UUID.randomUUID().toString();
                
                // Generate content based on current skill level and preferences
                List<LearningContent> content = generateSessionContent(topic, currentSkillLevel, sessionPreferences);
                
                // Determine difficulty level based on current skill and preferences
                double difficultyLevel = calculateSessionDifficulty(currentSkillLevel, sessionPreferences);
                
                LearningSession session = LearningSession.builder()
                        .id(sessionId)
                        .developerId(developerId)
                        .topic(topic)
                        .content(content)
                        .difficultyLevel(difficultyLevel)
                        .sessionType(determineSessionType(currentSkillLevel, sessionPreferences))
                        .build();

                logger.info("Created learning session: {} with {} content items", sessionId, content.size());
                
                return session;
                
            } catch (Exception e) {
                logger.error("Error creating learning session for developer: {}, topic: {}", developerId, topic, e);
                throw new RuntimeException("Failed to create learning session", e);
            }
        });
    }

    @Override
    public CompletableFuture<DeveloperProfile> updateSkillProfile(String developerId, SkillAssessment assessment) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating skill profile for developer: {}, domain: {}", developerId, assessment.getSkillDomain());
            
            try {
                // This would typically load the existing profile from a repository
                // For now, we'll create a basic updated profile
                SkillLevel updatedSkill = SkillLevel.builder()
                        .domain(assessment.getSkillDomain())
                        .proficiency(assessment.getProficiencyScore())
                        .confidence(assessment.getConfidenceScore())
                        .lastAssessed(assessment.getAssessedAt())
                        .evidenceCount(assessment.getEvidence().size())
                        .build();

                // Create a basic profile with the updated skill
                // In a real implementation, this would merge with existing profile
                DeveloperProfile updatedProfile = DeveloperProfile.builder()
                        .id(developerId)
                        .skillLevels(Map.of(assessment.getSkillDomain(), updatedSkill))
                        .learningPreferences(LearningPreferences.builder()
                                .detailLevel("intermediate")
                                .build())
                        .workHistory(new ArrayList<>())
                        .achievements(new ArrayList<>())
                        .currentGoals(new ArrayList<>())
                        .build();

                logger.info("Updated skill profile for developer: {}", developerId);
                
                return updatedProfile;
                
            } catch (Exception e) {
                logger.error("Error updating skill profile for developer: {}", developerId, e);
                throw new RuntimeException("Failed to update skill profile", e);
            }
        });
    }

    @Override
    public CompletableFuture<List<LearningContent>> generateFollowUpRecommendations(String developerId, List<LearningSession> completedSessions) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Generating follow-up recommendations for developer: {} based on {} completed sessions", 
                       developerId, completedSessions.size());
            
            try {
                return contentEngine.generateFollowUpContent(completedSessions);
            } catch (Exception e) {
                logger.error("Error generating follow-up recommendations for developer: {}", developerId, e);
                throw new RuntimeException("Failed to generate follow-up recommendations", e);
            }
        });
    }

    @Override
    public CompletableFuture<LearningSchedule> optimizeLearningSchedule(String developerId, List<LearningSession> learningHistory) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Optimizing learning schedule for developer: {} based on {} sessions", 
                       developerId, learningHistory.size());
            
            try {
                return analyticsEngine.optimizeSchedule(developerId, learningHistory);
            } catch (Exception e) {
                logger.error("Error optimizing learning schedule for developer: {}", developerId, e);
                throw new RuntimeException("Failed to optimize learning schedule", e);
            }
        });
    }

    @Override
    public CompletableFuture<LearningAnalytics> getLearningAnalytics(String developerId, String timeRange) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Generating learning analytics for developer: {}, time range: {}", developerId, timeRange);
            
            try {
                return analyticsEngine.generateAnalytics(developerId, timeRange);
            } catch (Exception e) {
                logger.error("Error generating learning analytics for developer: {}", developerId, e);
                throw new RuntimeException("Failed to generate learning analytics", e);
            }
        });
    }

    // Private helper methods

    private List<SkillGap> identifySkillGapsSync(DeveloperProfile profile, ProjectContext context, List<String> careerGoals) {
        List<SkillGap> gaps = new ArrayList<>();
        
        // Analyze project requirements vs current skills
        if (context != null) {
            gaps.addAll(analyzeProjectSkillGaps(profile, context));
        }
        
        // Analyze career goal requirements
        if (careerGoals != null && !careerGoals.isEmpty()) {
            gaps.addAll(analyzeCareerGoalGaps(profile, careerGoals));
        }
        
        // Identify general improvement opportunities
        gaps.addAll(identifyGeneralImprovements(profile));
        
        return gaps;
    }

    private List<SkillGap> analyzeProjectSkillGaps(DeveloperProfile profile, ProjectContext context) {
        List<SkillGap> gaps = new ArrayList<>();
        
        // Analyze project dependencies and patterns to identify required skills
        Set<String> requiredSkills = extractRequiredSkills(context);
        
        for (String skill : requiredSkills) {
            SkillLevel currentLevel = profile.getSkillLevels().get(skill);
            if (currentLevel == null || currentLevel.needsImprovement()) {
                double current = currentLevel != null ? currentLevel.getProficiency() : 0.0;
                double target = 0.7; // Target proficiency for project work
                
                if (current < target) {
                    SkillGap gap = SkillGap.builder()
                            .skillDomain(skill)
                            .currentLevel(current)
                            .targetLevel(target)
                            .priority(SkillGap.GapPriority.HIGH)
                            .projectContext(context.getProjectName())
                            .identificationReasons(List.of("Required for current project"))
                            .recommendedActions(List.of("Complete focused learning modules", "Practice with project examples"))
                            .estimatedLearningHours((int) ((target - current) * 20))
                            .build();
                    gaps.add(gap);
                }
            }
        }
        
        return gaps;
    }

    private Set<String> extractRequiredSkills(ProjectContext context) {
        Set<String> skills = new HashSet<>();
        
        // Extract skills from dependencies
        context.getDependencies().forEach(dep -> {
            String name = dep.getName().toLowerCase();
            if (name.contains("spring")) skills.add("spring-boot");
            if (name.contains("react")) skills.add("react");
            if (name.contains("junit")) skills.add("testing");
            if (name.contains("security")) skills.add("security");
        });
        
        // Extract skills from patterns
        context.getPatterns().forEach(pattern -> {
            String name = pattern.getName().toLowerCase();
            if (name.contains("mvc")) skills.add("mvc-pattern");
            if (name.contains("repository")) skills.add("repository-pattern");
            if (name.contains("service")) skills.add("service-layer");
        });
        
        return skills;
    }

    private List<SkillGap> analyzeCareerGoalGaps(DeveloperProfile profile, List<String> careerGoals) {
        List<SkillGap> gaps = new ArrayList<>();
        
        // Map career goals to required skills
        Map<String, List<String>> goalSkillMap = Map.of(
                "senior developer", List.of("architecture", "mentoring", "system-design"),
                "tech lead", List.of("leadership", "architecture", "project-management"),
                "full-stack", List.of("frontend", "backend", "database", "devops")
        );
        
        for (String goal : careerGoals) {
            List<String> requiredSkills = goalSkillMap.getOrDefault(goal.toLowerCase(), List.of());
            
            for (String skill : requiredSkills) {
                SkillLevel currentLevel = profile.getSkillLevels().get(skill);
                double current = currentLevel != null ? currentLevel.getProficiency() : 0.0;
                double target = 0.8; // Higher target for career advancement
                
                if (current < target) {
                    SkillGap gap = SkillGap.builder()
                            .skillDomain(skill)
                            .currentLevel(current)
                            .targetLevel(target)
                            .priority(SkillGap.GapPriority.MEDIUM)
                            .identificationReasons(List.of("Required for career goal: " + goal))
                            .recommendedActions(List.of("Advanced learning path", "Mentorship", "Real-world projects"))
                            .estimatedLearningHours((int) ((target - current) * 30))
                            .build();
                    gaps.add(gap);
                }
            }
        }
        
        return gaps;
    }

    private List<SkillGap> identifyGeneralImprovements(DeveloperProfile profile) {
        List<SkillGap> gaps = new ArrayList<>();
        
        // Identify skills that need improvement based on current levels
        profile.getSkillLevels().forEach((domain, level) -> {
            if (level.needsImprovement()) {
                double target = Math.min(1.0, level.getProficiency() + 0.3);
                
                SkillGap gap = SkillGap.builder()
                        .skillDomain(domain)
                        .currentLevel(level.getProficiency())
                        .targetLevel(target)
                        .priority(SkillGap.GapPriority.LOW)
                        .identificationReasons(List.of("General skill improvement"))
                        .recommendedActions(List.of("Regular practice", "Code reviews", "Learning resources"))
                        .estimatedLearningHours((int) ((target - level.getProficiency()) * 15))
                        .build();
                gaps.add(gap);
            }
        });
        
        return gaps;
    }

    private List<SkillGap> prioritizeSkillGaps(List<SkillGap> skillGaps, ProjectContext context) {
        return skillGaps.stream()
                .sorted((gap1, gap2) -> {
                    // Sort by priority first, then by gap size
                    int priorityCompare = Integer.compare(gap2.getPriority().getLevel(), gap1.getPriority().getLevel());
                    if (priorityCompare != 0) return priorityCompare;
                    
                    return Double.compare(gap2.getGapSize(), gap1.getGapSize());
                })
                .limit(5) // Limit to top 5 gaps for focused learning
                .collect(Collectors.toList());
    }

    private List<LearningModule> generateLearningModules(List<SkillGap> skillGaps, LearningPreferences preferences) {
        List<LearningModule> modules = new ArrayList<>();
        
        for (SkillGap gap : skillGaps) {
            LearningModule module = createModuleForSkillGap(gap, preferences);
            modules.add(module);
        }
        
        return modules;
    }

    private LearningModule createModuleForSkillGap(SkillGap gap, LearningPreferences preferences) {
        String moduleId = UUID.randomUUID().toString();
        String title = "Master " + gap.getSkillDomain().replace("-", " ").toUpperCase();
        String description = "Comprehensive learning module to address skill gap in " + gap.getSkillDomain();
        
        List<LearningContent> content = contentEngine.generateContentForSkill(gap.getSkillDomain(), gap.getGapSize(), preferences);
        
        return LearningModule.builder()
                .id(moduleId)
                .title(title)
                .description(description)
                .content(content)
                .prerequisites(List.of())
                .learningObjectives(gap.getRecommendedActions())
                .estimatedMinutes(gap.getEstimatedLearningHours() * 60)
                .type(LearningModule.ModuleType.TUTORIAL)
                .build();
    }

    private String generatePathTitle(List<SkillGap> skillGaps, ProjectContext context) {
        if (skillGaps.isEmpty()) return "General Learning Path";
        
        String primarySkill = skillGaps.get(0).getSkillDomain().replace("-", " ");
        if (context != null) {
            return String.format("%s Development Path for %s", 
                    capitalize(primarySkill), context.getProjectName());
        }
        return String.format("%s Mastery Path", capitalize(primarySkill));
    }

    private String generatePathDescription(List<SkillGap> skillGaps, ProjectContext context) {
        if (skillGaps.isEmpty()) return "A comprehensive learning path to improve your development skills.";
        
        StringBuilder description = new StringBuilder();
        description.append("This personalized learning path addresses your key skill gaps: ");
        
        List<String> skillNames = skillGaps.stream()
                .map(gap -> gap.getSkillDomain().replace("-", " "))
                .collect(Collectors.toList());
        
        description.append(String.join(", ", skillNames));
        
        if (context != null) {
            description.append(" with focus on your current project: ").append(context.getProjectName());
        }
        
        return description.toString();
    }

    private LearningPath.DifficultyLevel determineDifficulty(List<SkillGap> skillGaps, DeveloperProfile profile) {
        double averageGapSize = skillGaps.stream()
                .mapToDouble(SkillGap::getGapSize)
                .average()
                .orElse(0.3);
        
        if (averageGapSize > 0.6) return LearningPath.DifficultyLevel.ADVANCED;
        if (averageGapSize > 0.4) return LearningPath.DifficultyLevel.INTERMEDIATE;
        return LearningPath.DifficultyLevel.BEGINNER;
    }

    private List<LearningContent> generateSessionContent(String topic, SkillLevel currentSkillLevel, SessionPreferences preferences) {
        return contentEngine.generateSessionContent(topic, currentSkillLevel, preferences);
    }

    private double calculateSessionDifficulty(SkillLevel currentSkillLevel, SessionPreferences preferences) {
        double baseDifficulty = currentSkillLevel.getProficiency();
        double preferredDifficulty = preferences.getPreferredDifficulty();
        
        // Blend current skill level with preferred difficulty
        return (baseDifficulty * 0.6) + (preferredDifficulty * 0.4);
    }

    private String determineSessionType(SkillLevel currentSkillLevel, SessionPreferences preferences) {
        if (currentSkillLevel.getProficiency() < 0.3) return "beginner";
        if (currentSkillLevel.getProficiency() > 0.7) return "advanced";
        if (preferences.isIncludeExercises()) return "interactive";
        return "tutorial";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}