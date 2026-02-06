package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents progress information for a learning session.
 * Tracks completion status, scores, and learning outcomes.
 */
public class LearningProgress {
    
    @NotBlank
    private final String sessionId;
    
    @NotBlank
    private final String developerId;
    
    @DecimalMin(value = "0.0", message = "Completion percentage must be between 0.0 and 100.0")
    @DecimalMax(value = "100.0", message = "Completion percentage must be between 0.0 and 100.0")
    private final double completionPercentage;
    
    @DecimalMin(value = "0.0", message = "Score must be between 0.0 and 100.0")
    @DecimalMax(value = "100.0", message = "Score must be between 0.0 and 100.0")
    private final double score;
    
    private final int timeSpentMinutes;
    
    @NotNull
    private final Map<String, Double> moduleScores;
    
    @NotNull
    private final List<String> completedModules;
    
    @NotNull
    private final List<String> strugglingAreas;
    
    @NotNull
    private final List<String> masteredConcepts;
    
    @NotNull
    private final LocalDateTime lastUpdated;
    
    private final String currentModule;
    
    private final boolean sessionCompleted;
    
    private final String feedback;
    
    // Additional fields for enhanced recommendation engine
    private final String contentId;
    private final String previousContentId;
    private final double accuracyScore;
    private final int expectedTimeMinutes;
    private final int actualTimeMinutes;

    @JsonCreator
    public LearningProgress(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("completionPercentage") double completionPercentage,
            @JsonProperty("score") double score,
            @JsonProperty("timeSpentMinutes") int timeSpentMinutes,
            @JsonProperty("moduleScores") Map<String, Double> moduleScores,
            @JsonProperty("completedModules") List<String> completedModules,
            @JsonProperty("strugglingAreas") List<String> strugglingAreas,
            @JsonProperty("masteredConcepts") List<String> masteredConcepts,
            @JsonProperty("lastUpdated") LocalDateTime lastUpdated,
            @JsonProperty("currentModule") String currentModule,
            @JsonProperty("sessionCompleted") boolean sessionCompleted,
            @JsonProperty("feedback") String feedback,
            @JsonProperty("contentId") String contentId,
            @JsonProperty("previousContentId") String previousContentId,
            @JsonProperty("accuracyScore") double accuracyScore,
            @JsonProperty("expectedTimeMinutes") int expectedTimeMinutes,
            @JsonProperty("actualTimeMinutes") int actualTimeMinutes) {
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.completionPercentage = validateRange(completionPercentage, 0.0, 100.0, "completionPercentage");
        this.score = validateRange(score, 0.0, 100.0, "score");
        this.timeSpentMinutes = Math.max(0, timeSpentMinutes);
        this.moduleScores = moduleScores != null ? new HashMap<>(moduleScores) : new HashMap<>();
        this.completedModules = completedModules != null ? new ArrayList<>(completedModules) : new ArrayList<>();
        this.strugglingAreas = strugglingAreas != null ? new ArrayList<>(strugglingAreas) : new ArrayList<>();
        this.masteredConcepts = masteredConcepts != null ? new ArrayList<>(masteredConcepts) : new ArrayList<>();
        this.lastUpdated = lastUpdated != null ? lastUpdated : LocalDateTime.now();
        this.currentModule = currentModule;
        this.sessionCompleted = sessionCompleted;
        this.feedback = feedback;
        this.contentId = contentId;
        this.previousContentId = previousContentId;
        this.accuracyScore = Math.max(0.0, Math.min(1.0, accuracyScore));
        this.expectedTimeMinutes = Math.max(0, expectedTimeMinutes);
        this.actualTimeMinutes = Math.max(0, actualTimeMinutes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public LearningProgress updateCompletion(double newCompletionPercentage) {
        return new LearningProgress(sessionId, developerId, newCompletionPercentage, score, timeSpentMinutes,
                moduleScores, completedModules, strugglingAreas, masteredConcepts, LocalDateTime.now(),
                currentModule, newCompletionPercentage >= 100.0, feedback, contentId, previousContentId,
                accuracyScore, expectedTimeMinutes, actualTimeMinutes);
    }

    public LearningProgress addCompletedModule(String moduleId, double moduleScore) {
        List<String> updatedCompleted = new ArrayList<>(this.completedModules);
        updatedCompleted.add(moduleId);
        
        Map<String, Double> updatedScores = new HashMap<>(this.moduleScores);
        updatedScores.put(moduleId, moduleScore);
        
        return new LearningProgress(sessionId, developerId, completionPercentage, score, timeSpentMinutes,
                updatedScores, updatedCompleted, strugglingAreas, masteredConcepts, LocalDateTime.now(),
                currentModule, sessionCompleted, feedback, contentId, previousContentId,
                accuracyScore, expectedTimeMinutes, actualTimeMinutes);
    }

    public LearningProgress addTimeSpent(int additionalMinutes) {
        return new LearningProgress(sessionId, developerId, completionPercentage, score, 
                timeSpentMinutes + additionalMinutes, moduleScores, completedModules, strugglingAreas,
                masteredConcepts, LocalDateTime.now(), currentModule, sessionCompleted, feedback,
                contentId, previousContentId, accuracyScore, expectedTimeMinutes, actualTimeMinutes + additionalMinutes);
    }

    public double getAverageModuleScore() {
        return moduleScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public boolean isStruggling() {
        return !strugglingAreas.isEmpty() || getAverageModuleScore() < 60.0;
    }

    public boolean isExcelling() {
        return masteredConcepts.size() > strugglingAreas.size() && getAverageModuleScore() >= 85.0;
    }

    private static double validateRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + ", got: " + value);
        }
        return value;
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public String getDeveloperId() { return developerId; }
    public double getCompletionPercentage() { return completionPercentage; }
    public double getScore() { return score; }
    public int getTimeSpentMinutes() { return timeSpentMinutes; }
    public Map<String, Double> getModuleScores() { return new HashMap<>(moduleScores); }
    public List<String> getCompletedModules() { return new ArrayList<>(completedModules); }
    public List<String> getStrugglingAreas() { return new ArrayList<>(strugglingAreas); }
    public List<String> getMasteredConcepts() { return new ArrayList<>(masteredConcepts); }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public String getCurrentModule() { return currentModule; }
    public boolean isSessionCompleted() { return sessionCompleted; }
    public String getFeedback() { return feedback; }
    public String getContentId() { return contentId; }
    public String getPreviousContentId() { return previousContentId; }
    public double getAccuracyScore() { return accuracyScore; }
    public int getExpectedTimeMinutes() { return expectedTimeMinutes; }
    public int getActualTimeMinutes() { return actualTimeMinutes; }

    public static class Builder {
        private String sessionId;
        private String developerId;
        private double completionPercentage = 0.0;
        private double score = 0.0;
        private int timeSpentMinutes = 0;
        private Map<String, Double> moduleScores = new HashMap<>();
        private List<String> completedModules = new ArrayList<>();
        private List<String> strugglingAreas = new ArrayList<>();
        private List<String> masteredConcepts = new ArrayList<>();
        private LocalDateTime lastUpdated = LocalDateTime.now();
        private String currentModule;
        private boolean sessionCompleted = false;
        private String feedback;
        private String contentId;
        private String previousContentId;
        private double accuracyScore = 0.0;
        private int expectedTimeMinutes = 0;
        private int actualTimeMinutes = 0;

        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder completionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; return this; }
        public Builder score(double score) { this.score = score; return this; }
        public Builder timeSpentMinutes(int timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; return this; }
        public Builder moduleScores(Map<String, Double> moduleScores) { this.moduleScores = moduleScores; return this; }
        public Builder completedModules(List<String> completedModules) { this.completedModules = completedModules; return this; }
        public Builder strugglingAreas(List<String> strugglingAreas) { this.strugglingAreas = strugglingAreas; return this; }
        public Builder masteredConcepts(List<String> masteredConcepts) { this.masteredConcepts = masteredConcepts; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public Builder currentModule(String currentModule) { this.currentModule = currentModule; return this; }
        public Builder sessionCompleted(boolean sessionCompleted) { this.sessionCompleted = sessionCompleted; return this; }
        public Builder feedback(String feedback) { this.feedback = feedback; return this; }
        public Builder contentId(String contentId) { this.contentId = contentId; return this; }
        public Builder previousContentId(String previousContentId) { this.previousContentId = previousContentId; return this; }
        public Builder accuracyScore(double accuracyScore) { this.accuracyScore = accuracyScore; return this; }
        public Builder expectedTimeMinutes(int expectedTimeMinutes) { this.expectedTimeMinutes = expectedTimeMinutes; return this; }
        public Builder actualTimeMinutes(int actualTimeMinutes) { this.actualTimeMinutes = actualTimeMinutes; return this; }

        public LearningProgress build() {
            return new LearningProgress(sessionId, developerId, completionPercentage, score, timeSpentMinutes,
                    moduleScores, completedModules, strugglingAreas, masteredConcepts, lastUpdated,
                    currentModule, sessionCompleted, feedback, contentId, previousContentId,
                    accuracyScore, expectedTimeMinutes, actualTimeMinutes);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningProgress that = (LearningProgress) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "LearningProgress{" +
                "sessionId='" + sessionId + '\'' +
                ", completionPercentage=" + String.format("%.1f", completionPercentage) + "%" +
                ", score=" + String.format("%.1f", score) +
                ", timeSpentMinutes=" + timeSpentMinutes +
                ", sessionCompleted=" + sessionCompleted +
                '}';
    }
}