package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a developer's profile including skills, preferences, and learning history.
 * This is a core domain model that tracks developer competency and learning progress.
 */
public class DeveloperProfile {
    
    @NotBlank
    private final String id;
    
    @NotNull
    @Valid
    private final Map<String, SkillLevel> skillLevels;
    
    @NotNull
    @Valid
    private final LearningPreferences learningPreferences;
    
    @NotNull
    private final List<WorkSession> workHistory;
    
    @NotNull
    private final List<Achievement> achievements;
    
    @NotNull
    private final List<LearningGoal> currentGoals;
    
    private final LocalDateTime createdAt;
    private final LocalDateTime lastUpdated;

    @JsonCreator
    public DeveloperProfile(
            @JsonProperty("id") String id,
            @JsonProperty("skillLevels") Map<String, SkillLevel> skillLevels,
            @JsonProperty("learningPreferences") LearningPreferences learningPreferences,
            @JsonProperty("workHistory") List<WorkSession> workHistory,
            @JsonProperty("achievements") List<Achievement> achievements,
            @JsonProperty("currentGoals") List<LearningGoal> currentGoals,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("lastUpdated") LocalDateTime lastUpdated) {
        this.id = Objects.requireNonNull(id, "Developer ID cannot be null");
        this.skillLevels = skillLevels != null ? new HashMap<>(skillLevels) : new HashMap<>();
        this.learningPreferences = Objects.requireNonNull(learningPreferences, "Learning preferences cannot be null");
        this.workHistory = workHistory != null ? new ArrayList<>(workHistory) : new ArrayList<>();
        this.achievements = achievements != null ? new ArrayList<>(achievements) : new ArrayList<>();
        this.currentGoals = currentGoals != null ? new ArrayList<>(currentGoals) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastUpdated = lastUpdated != null ? lastUpdated : LocalDateTime.now();
    }

    public static DeveloperProfile create(String id, LearningPreferences preferences) {
        return new DeveloperProfile(id, new HashMap<>(), preferences, 
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
                LocalDateTime.now(), LocalDateTime.now());
    }

    public DeveloperProfile updateSkillLevel(String domain, SkillLevel skillLevel) {
        Map<String, SkillLevel> updatedSkills = new HashMap<>(this.skillLevels);
        updatedSkills.put(domain, skillLevel);
        
        return new DeveloperProfile(id, updatedSkills, learningPreferences, 
                workHistory, achievements, currentGoals, createdAt, LocalDateTime.now());
    }

    public DeveloperProfile addWorkSession(WorkSession session) {
        List<WorkSession> updatedHistory = new ArrayList<>(this.workHistory);
        updatedHistory.add(session);
        
        return new DeveloperProfile(id, skillLevels, learningPreferences, 
                updatedHistory, achievements, currentGoals, createdAt, LocalDateTime.now());
    }

    public DeveloperProfile addAchievement(Achievement achievement) {
        List<Achievement> updatedAchievements = new ArrayList<>(this.achievements);
        updatedAchievements.add(achievement);
        
        return new DeveloperProfile(id, skillLevels, learningPreferences, 
                workHistory, updatedAchievements, currentGoals, createdAt, LocalDateTime.now());
    }

    // Getters
    public String getId() { return id; }
    public Map<String, SkillLevel> getSkillLevels() { return new HashMap<>(skillLevels); }
    public LearningPreferences getLearningPreferences() { return learningPreferences; }
    public List<WorkSession> getWorkHistory() { return new ArrayList<>(workHistory); }
    public List<Achievement> getAchievements() { return new ArrayList<>(achievements); }
    public List<LearningGoal> getCurrentGoals() { return new ArrayList<>(currentGoals); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeveloperProfile that = (DeveloperProfile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DeveloperProfile{" +
                "id='" + id + '\'' +
                ", skillCount=" + skillLevels.size() +
                ", workSessionCount=" + workHistory.size() +
                ", achievementCount=" + achievements.size() +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Map<String, SkillLevel> skillLevels = new HashMap<>();
        private LearningPreferences learningPreferences;
        private List<WorkSession> workHistory = new ArrayList<>();
        private List<Achievement> achievements = new ArrayList<>();
        private List<LearningGoal> currentGoals = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdated;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder skillLevels(Map<String, SkillLevel> skillLevels) {
            this.skillLevels = skillLevels != null ? new HashMap<>(skillLevels) : new HashMap<>();
            return this;
        }

        public Builder learningPreferences(LearningPreferences learningPreferences) {
            this.learningPreferences = learningPreferences;
            return this;
        }

        public Builder workHistory(List<WorkSession> workHistory) {
            this.workHistory = workHistory != null ? new ArrayList<>(workHistory) : new ArrayList<>();
            return this;
        }

        public Builder achievements(List<Achievement> achievements) {
            this.achievements = achievements != null ? new ArrayList<>(achievements) : new ArrayList<>();
            return this;
        }

        public Builder currentGoals(List<LearningGoal> currentGoals) {
            this.currentGoals = currentGoals != null ? new ArrayList<>(currentGoals) : new ArrayList<>();
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public DeveloperProfile build() {
            return new DeveloperProfile(id, skillLevels, learningPreferences, workHistory, 
                    achievements, currentGoals, createdAt, lastUpdated);
        }
    }
}