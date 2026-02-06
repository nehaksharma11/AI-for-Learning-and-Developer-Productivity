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
 * Represents a learning module within a learning path.
 * Contains content, exercises, and tracks completion status.
 */
public class LearningModule {
    
    @NotBlank
    private final String id;
    
    @NotBlank
    private final String title;
    
    private final String description;
    
    @NotNull
    @Valid
    private final List<LearningContent> content;
    
    @NotNull
    private final List<String> prerequisites;
    
    @NotNull
    private final List<String> learningObjectives;
    
    private final int estimatedMinutes;
    
    private final ModuleType type;
    
    private final boolean completed;
    
    private final LocalDateTime completedAt;
    
    private final double score;

    @JsonCreator
    public LearningModule(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("content") List<LearningContent> content,
            @JsonProperty("prerequisites") List<String> prerequisites,
            @JsonProperty("learningObjectives") List<String> learningObjectives,
            @JsonProperty("estimatedMinutes") int estimatedMinutes,
            @JsonProperty("type") ModuleType type,
            @JsonProperty("completed") boolean completed,
            @JsonProperty("completedAt") LocalDateTime completedAt,
            @JsonProperty("score") double score) {
        this.id = Objects.requireNonNull(id, "Module ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = description;
        this.content = content != null ? new ArrayList<>(content) : new ArrayList<>();
        this.prerequisites = prerequisites != null ? new ArrayList<>(prerequisites) : new ArrayList<>();
        this.learningObjectives = learningObjectives != null ? new ArrayList<>(learningObjectives) : new ArrayList<>();
        this.estimatedMinutes = Math.max(0, estimatedMinutes);
        this.type = type != null ? type : ModuleType.TUTORIAL;
        this.completed = completed;
        this.completedAt = completedAt;
        this.score = Math.max(0.0, Math.min(100.0, score));
    }

    public static Builder builder() {
        return new Builder();
    }

    public LearningModule markCompleted(double score) {
        return new LearningModule(id, title, description, content, prerequisites, learningObjectives,
                estimatedMinutes, type, true, LocalDateTime.now(), score);
    }

    public LearningModule addContent(LearningContent newContent) {
        List<LearningContent> updatedContent = new ArrayList<>(this.content);
        updatedContent.add(newContent);
        
        return new LearningModule(id, title, description, updatedContent, prerequisites, learningObjectives,
                estimatedMinutes, type, completed, completedAt, score);
    }

    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }

    public boolean isInteractive() {
        return content.stream().anyMatch(c -> c.getType() == LearningContent.ContentType.EXERCISE ||
                                              c.getType() == LearningContent.ContentType.QUIZ);
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<LearningContent> getContent() { return new ArrayList<>(content); }
    public List<String> getPrerequisites() { return new ArrayList<>(prerequisites); }
    public List<String> getLearningObjectives() { return new ArrayList<>(learningObjectives); }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public ModuleType getType() { return type; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public double getScore() { return score; }

    public enum ModuleType {
        TUTORIAL,
        EXERCISE,
        QUIZ,
        PROJECT,
        READING,
        VIDEO,
        INTERACTIVE_DEMO
    }

    public static class Builder {
        private String id;
        private String title;
        private String description;
        private List<LearningContent> content = new ArrayList<>();
        private List<String> prerequisites = new ArrayList<>();
        private List<String> learningObjectives = new ArrayList<>();
        private int estimatedMinutes;
        private ModuleType type = ModuleType.TUTORIAL;
        private boolean completed = false;
        private LocalDateTime completedAt;
        private double score = 0.0;

        public Builder id(String id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder content(List<LearningContent> content) { this.content = content; return this; }
        public Builder prerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; return this; }
        public Builder learningObjectives(List<String> learningObjectives) { this.learningObjectives = learningObjectives; return this; }
        public Builder estimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; return this; }
        public Builder type(ModuleType type) { this.type = type; return this; }
        public Builder completed(boolean completed) { this.completed = completed; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder score(double score) { this.score = score; return this; }

        public LearningModule build() {
            return new LearningModule(id, title, description, content, prerequisites, learningObjectives,
                    estimatedMinutes, type, completed, completedAt, score);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningModule that = (LearningModule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LearningModule{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", estimatedMinutes=" + estimatedMinutes +
                ", completed=" + completed +
                ", score=" + String.format("%.1f", score) +
                '}';
    }
}