package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents preferences for a learning session.
 * Contains settings for duration, difficulty, content types, and interaction preferences.
 */
public class SessionPreferences {
    
    @Min(value = 5, message = "Duration must be at least 5 minutes")
    private final int durationMinutes;
    
    private final double preferredDifficulty;
    
    private final List<LearningContent.ContentType> preferredContentTypes;
    
    private final boolean interactiveMode;
    
    private final boolean includeExercises;
    
    private final boolean includeQuizzes;
    
    private final String focusArea;
    
    private final int maxConcepts;
    
    private final boolean adaptiveDifficulty;

    @JsonCreator
    public SessionPreferences(
            @JsonProperty("durationMinutes") int durationMinutes,
            @JsonProperty("preferredDifficulty") double preferredDifficulty,
            @JsonProperty("preferredContentTypes") List<LearningContent.ContentType> preferredContentTypes,
            @JsonProperty("interactiveMode") boolean interactiveMode,
            @JsonProperty("includeExercises") boolean includeExercises,
            @JsonProperty("includeQuizzes") boolean includeQuizzes,
            @JsonProperty("focusArea") String focusArea,
            @JsonProperty("maxConcepts") int maxConcepts,
            @JsonProperty("adaptiveDifficulty") boolean adaptiveDifficulty) {
        this.durationMinutes = Math.max(5, durationMinutes);
        this.preferredDifficulty = Math.max(0.0, Math.min(1.0, preferredDifficulty));
        this.preferredContentTypes = preferredContentTypes != null ? new ArrayList<>(preferredContentTypes) : new ArrayList<>();
        this.interactiveMode = interactiveMode;
        this.includeExercises = includeExercises;
        this.includeQuizzes = includeQuizzes;
        this.focusArea = focusArea;
        this.maxConcepts = Math.max(1, maxConcepts);
        this.adaptiveDifficulty = adaptiveDifficulty;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SessionPreferences defaultPreferences() {
        return new SessionPreferences(30, 0.5, List.of(LearningContent.ContentType.EXPLANATION, LearningContent.ContentType.CODE_EXAMPLE),
                true, true, true, null, 5, true);
    }

    public static SessionPreferences quickSession() {
        return new SessionPreferences(15, 0.3, List.of(LearningContent.ContentType.EXPLANATION),
                false, false, false, null, 3, false);
    }

    public static SessionPreferences intensiveSession() {
        return new SessionPreferences(60, 0.7, List.of(LearningContent.ContentType.EXERCISE, LearningContent.ContentType.QUIZ),
                true, true, true, null, 8, true);
    }

    // Getters
    public int getDurationMinutes() { return durationMinutes; }
    public double getPreferredDifficulty() { return preferredDifficulty; }
    public List<LearningContent.ContentType> getPreferredContentTypes() { return new ArrayList<>(preferredContentTypes); }
    public boolean isInteractiveMode() { return interactiveMode; }
    public boolean isIncludeExercises() { return includeExercises; }
    public boolean isIncludeQuizzes() { return includeQuizzes; }
    public String getFocusArea() { return focusArea; }
    public int getMaxConcepts() { return maxConcepts; }
    public boolean isAdaptiveDifficulty() { return adaptiveDifficulty; }

    public static class Builder {
        private int durationMinutes = 30;
        private double preferredDifficulty = 0.5;
        private List<LearningContent.ContentType> preferredContentTypes = new ArrayList<>();
        private boolean interactiveMode = true;
        private boolean includeExercises = true;
        private boolean includeQuizzes = true;
        private String focusArea;
        private int maxConcepts = 5;
        private boolean adaptiveDifficulty = true;

        public Builder durationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder preferredDifficulty(double preferredDifficulty) { this.preferredDifficulty = preferredDifficulty; return this; }
        public Builder preferredContentTypes(List<LearningContent.ContentType> preferredContentTypes) { this.preferredContentTypes = preferredContentTypes; return this; }
        public Builder interactiveMode(boolean interactiveMode) { this.interactiveMode = interactiveMode; return this; }
        public Builder includeExercises(boolean includeExercises) { this.includeExercises = includeExercises; return this; }
        public Builder includeQuizzes(boolean includeQuizzes) { this.includeQuizzes = includeQuizzes; return this; }
        public Builder focusArea(String focusArea) { this.focusArea = focusArea; return this; }
        public Builder maxConcepts(int maxConcepts) { this.maxConcepts = maxConcepts; return this; }
        public Builder adaptiveDifficulty(boolean adaptiveDifficulty) { this.adaptiveDifficulty = adaptiveDifficulty; return this; }

        public SessionPreferences build() {
            return new SessionPreferences(durationMinutes, preferredDifficulty, preferredContentTypes,
                    interactiveMode, includeExercises, includeQuizzes, focusArea, maxConcepts, adaptiveDifficulty);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionPreferences that = (SessionPreferences) o;
        return durationMinutes == that.durationMinutes &&
                Double.compare(that.preferredDifficulty, preferredDifficulty) == 0 &&
                interactiveMode == that.interactiveMode &&
                includeExercises == that.includeExercises &&
                includeQuizzes == that.includeQuizzes &&
                maxConcepts == that.maxConcepts &&
                adaptiveDifficulty == that.adaptiveDifficulty &&
                Objects.equals(preferredContentTypes, that.preferredContentTypes) &&
                Objects.equals(focusArea, that.focusArea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationMinutes, preferredDifficulty, preferredContentTypes, interactiveMode,
                includeExercises, includeQuizzes, focusArea, maxConcepts, adaptiveDifficulty);
    }

    @Override
    public String toString() {
        return "SessionPreferences{" +
                "durationMinutes=" + durationMinutes +
                ", preferredDifficulty=" + String.format("%.2f", preferredDifficulty) +
                ", interactiveMode=" + interactiveMode +
                ", focusArea='" + focusArea + '\'' +
                ", maxConcepts=" + maxConcepts +
                '}';
    }
}