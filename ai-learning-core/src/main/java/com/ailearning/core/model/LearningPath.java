package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a personalized learning path for a developer.
 * Contains ordered learning modules and tracks progress.
 */
public class LearningPath {
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String developerId;
    
    @NotBlank
    private final String title;
    
    private final String description;
    
    @NotNull
    @Valid
    private final List<LearningModule> modules;
    
    @NotNull
    private final List<String> targetSkills;
    
    private final int estimatedDurationMinutes;
    
    private final DifficultyLevel difficulty;
    
    @NotNull
    private final LocalDateTime createdAt;
    
    private final LocalDateTime lastAccessed;
    
    private final double completionPercentage;

    @JsonCreator
    public LearningPath(
            @JsonProperty("id") String id,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("modules") List<LearningModule> modules,
            @JsonProperty("targetSkills") List<String> targetSkills,
            @JsonProperty("estimatedDurationMinutes") int estimatedDurationMinutes,
            @JsonProperty("difficulty") DifficultyLevel difficulty,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("lastAccessed") LocalDateTime lastAccessed,
            @JsonProperty("completionPercentage") double completionPercentage) {
        this.id = Objects.requireNonNull(id, "Learning path ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = description;
        this.modules = modules != null ? new ArrayList<>(modules) : new ArrayList<>();
        this.targetSkills = targetSkills != null ? new ArrayList<>(targetSkills) : new ArrayList<>();
        this.estimatedDurationMinutes = Math.max(0, estimatedDurationMinutes);
        this.difficulty = difficulty != null ? difficulty : DifficultyLevel.BEGINNER;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastAccessed = lastAccessed;
        this.completionPercentage = Math.max(0.0, Math.min(100.0, completionPercentage));
    }

    public static Builder builder() {
        return new Builder();
    }

    public LearningPath updateProgress(double newCompletionPercentage) {
        return new LearningPath(id, developerId, title, description, modules, targetSkills,
                estimatedDurationMinutes, difficulty, createdAt, LocalDateTime.now(), newCompletionPercentage);
    }

    public LearningPath addModule(LearningModule module) {
        List<LearningModule> updatedModules = new ArrayList<>(this.modules);
        updatedModules.add(module);
        
        return new LearningPath(id, developerId, title, description, updatedModules, targetSkills,
                estimatedDurationMinutes, difficulty, createdAt, lastAccessed, completionPercentage);
    }

    public boolean isCompleted() {
        return completionPercentage >= 100.0;
    }

    public LearningModule getCurrentModule() {
        return modules.stream()
                .filter(module -> !module.isCompleted())
                .findFirst()
                .orElse(null);
    }

    public int getCompletedModuleCount() {
        return (int) modules.stream().mapToLong(module -> module.isCompleted() ? 1 : 0).sum();
    }

    // Getters
    public String getId() { return id; }
    public String getDeveloperId() { return developerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<LearningModule> getModules() { return new ArrayList<>(modules); }
    public List<String> getTargetSkills() { return new ArrayList<>(targetSkills); }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public DifficultyLevel getDifficulty() { return difficulty; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public double getCompletionPercentage() { return completionPercentage; }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public static class Builder {
        private String id;
        private String developerId;
        private String title;
        private String description;
        private List<LearningModule> modules = new ArrayList<>();
        private List<String> targetSkills = new ArrayList<>();
        private int estimatedDurationMinutes;
        private DifficultyLevel difficulty = DifficultyLevel.BEGINNER;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime lastAccessed;
        private double completionPercentage = 0.0;

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder modules(List<LearningModule> modules) { this.modules = modules; return this; }
        public Builder targetSkills(List<String> targetSkills) { this.targetSkills = targetSkills; return this; }
        public Builder estimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; return this; }
        public Builder difficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder lastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; return this; }
        public Builder completionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; return this; }

        public LearningPath build() {
            return new LearningPath(id, developerId, title, description, modules, targetSkills,
                    estimatedDurationMinutes, difficulty, createdAt, lastAccessed, completionPercentage);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningPath that = (LearningPath) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LearningPath{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", moduleCount=" + modules.size() +
                ", completionPercentage=" + String.format("%.1f", completionPercentage) + "%" +
                ", difficulty=" + difficulty +
                '}';
    }
}