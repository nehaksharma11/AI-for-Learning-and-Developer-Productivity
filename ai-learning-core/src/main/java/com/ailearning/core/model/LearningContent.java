package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Represents individual learning content within a module.
 * Can be text, code examples, exercises, or interactive elements.
 */
public class LearningContent {
    
    @NotBlank
    private final String id;
    
    @NotNull
    private final ContentType type;
    
    @NotBlank
    private final String title;
    
    @NotBlank
    private final String content;
    
    private final String codeExample;
    
    private final String language;
    
    private final int estimatedMinutes;
    
    private final DifficultyLevel difficulty;
    
    private final String interactionData;

    @JsonCreator
    public LearningContent(
            @JsonProperty("id") String id,
            @JsonProperty("type") ContentType type,
            @JsonProperty("title") String title,
            @JsonProperty("content") String content,
            @JsonProperty("codeExample") String codeExample,
            @JsonProperty("language") String language,
            @JsonProperty("estimatedMinutes") int estimatedMinutes,
            @JsonProperty("difficulty") DifficultyLevel difficulty,
            @JsonProperty("interactionData") String interactionData) {
        this.id = Objects.requireNonNull(id, "Content ID cannot be null");
        this.type = Objects.requireNonNull(type, "Content type cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.codeExample = codeExample;
        this.language = language;
        this.estimatedMinutes = Math.max(0, estimatedMinutes);
        this.difficulty = difficulty != null ? difficulty : DifficultyLevel.BEGINNER;
        this.interactionData = interactionData;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static LearningContent explanation(String id, String title, String content) {
        return new LearningContent(id, ContentType.EXPLANATION, title, content, null, null, 5, DifficultyLevel.BEGINNER, null);
    }

    public static LearningContent codeExample(String id, String title, String content, String code, String language) {
        return new LearningContent(id, ContentType.CODE_EXAMPLE, title, content, code, language, 10, DifficultyLevel.INTERMEDIATE, null);
    }

    public static LearningContent exercise(String id, String title, String content, String interactionData) {
        return new LearningContent(id, ContentType.EXERCISE, title, content, null, null, 15, DifficultyLevel.INTERMEDIATE, interactionData);
    }

    public boolean hasCodeExample() {
        return codeExample != null && !codeExample.trim().isEmpty();
    }

    public boolean isInteractive() {
        return type == ContentType.EXERCISE || type == ContentType.QUIZ || type == ContentType.INTERACTIVE_DEMO;
    }

    // Getters
    public String getId() { return id; }
    public ContentType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCodeExample() { return codeExample; }
    public String getLanguage() { return language; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public DifficultyLevel getDifficulty() { return difficulty; }
    public String getInteractionData() { return interactionData; }

    public enum ContentType {
        EXPLANATION,
        CODE_EXAMPLE,
        EXERCISE,
        QUIZ,
        READING,
        VIDEO,
        INTERACTIVE_DEMO
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public static class Builder {
        private String id;
        private ContentType type;
        private String title;
        private String content;
        private String codeExample;
        private String language;
        private int estimatedMinutes;
        private DifficultyLevel difficulty = DifficultyLevel.BEGINNER;
        private String interactionData;

        public Builder id(String id) { this.id = id; return this; }
        public Builder type(ContentType type) { this.type = type; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder codeExample(String codeExample) { this.codeExample = codeExample; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder estimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; return this; }
        public Builder difficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; return this; }
        public Builder interactionData(String interactionData) { this.interactionData = interactionData; return this; }

        public LearningContent build() {
            return new LearningContent(id, type, title, content, codeExample, language, estimatedMinutes, difficulty, interactionData);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningContent that = (LearningContent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LearningContent{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", estimatedMinutes=" + estimatedMinutes +
                ", difficulty=" + difficulty +
                '}';
    }
}