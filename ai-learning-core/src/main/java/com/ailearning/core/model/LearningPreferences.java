package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a developer's learning preferences and settings.
 * Used to personalize the learning experience and content delivery.
 */
public class LearningPreferences {
    
    public enum LearningStyle {
        VISUAL, AUDITORY, KINESTHETIC, READING_WRITING
    }
    
    public enum DifficultyPreference {
        EASY_FIRST, CHALLENGING_FIRST, ADAPTIVE
    }
    
    public enum ContentType {
        TUTORIALS, EXAMPLES, EXERCISES, DOCUMENTATION, VIDEOS
    }

    @NotNull
    private final LearningStyle preferredLearningStyle;
    
    @NotNull
    private final DifficultyPreference difficultyPreference;
    
    @NotNull
    private final List<ContentType> preferredContentTypes;
    
    private final String developerId; // For collaborative filtering
    
    @Min(value = 5, message = "Session length must be at least 5 minutes")
    @Max(value = 120, message = "Session length cannot exceed 120 minutes")
    private final int preferredSessionLengthMinutes;
    
    @Min(value = 1, message = "Sessions per week must be at least 1")
    @Max(value = 14, message = "Sessions per week cannot exceed 14")
    private final int sessionsPerWeek;
    
    private final boolean enableRealTimeHints;
    private final boolean enableProactiveRecommendations;
    private final boolean enableProgressTracking;
    private final boolean enablePeerComparison;

    @JsonCreator
    public LearningPreferences(
            @JsonProperty("preferredLearningStyle") LearningStyle preferredLearningStyle,
            @JsonProperty("difficultyPreference") DifficultyPreference difficultyPreference,
            @JsonProperty("preferredContentTypes") List<ContentType> preferredContentTypes,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("preferredSessionLengthMinutes") int preferredSessionLengthMinutes,
            @JsonProperty("sessionsPerWeek") int sessionsPerWeek,
            @JsonProperty("enableRealTimeHints") boolean enableRealTimeHints,
            @JsonProperty("enableProactiveRecommendations") boolean enableProactiveRecommendations,
            @JsonProperty("enableProgressTracking") boolean enableProgressTracking,
            @JsonProperty("enablePeerComparison") boolean enablePeerComparison) {
        this.preferredLearningStyle = Objects.requireNonNull(preferredLearningStyle, "Learning style cannot be null");
        this.difficultyPreference = Objects.requireNonNull(difficultyPreference, "Difficulty preference cannot be null");
        this.preferredContentTypes = preferredContentTypes != null ? 
                new ArrayList<>(preferredContentTypes) : List.of(ContentType.EXAMPLES, ContentType.TUTORIALS);
        this.developerId = developerId;
        this.preferredSessionLengthMinutes = Math.max(5, Math.min(120, preferredSessionLengthMinutes));
        this.sessionsPerWeek = Math.max(1, Math.min(14, sessionsPerWeek));
        this.enableRealTimeHints = enableRealTimeHints;
        this.enableProactiveRecommendations = enableProactiveRecommendations;
        this.enableProgressTracking = enableProgressTracking;
        this.enablePeerComparison = enablePeerComparison;
    }

    public static LearningPreferences defaultPreferences() {
        return new LearningPreferences(
                LearningStyle.VISUAL,
                DifficultyPreference.ADAPTIVE,
                List.of(ContentType.EXAMPLES, ContentType.TUTORIALS, ContentType.EXERCISES),
                null,
                30,
                3,
                true,
                true,
                true,
                false
        );
    }

    public static LearningPreferences beginnerFriendly() {
        return new LearningPreferences(
                LearningStyle.VISUAL,
                DifficultyPreference.EASY_FIRST,
                List.of(ContentType.TUTORIALS, ContentType.EXAMPLES),
                null,
                20,
                5,
                true,
                true,
                true,
                false
        );
    }

    public static LearningPreferences expertMode() {
        return new LearningPreferences(
                LearningStyle.READING_WRITING,
                DifficultyPreference.CHALLENGING_FIRST,
                List.of(ContentType.DOCUMENTATION, ContentType.EXERCISES),
                null,
                45,
                2,
                false,
                false,
                true,
                true
        );
    }

    public LearningPreferences withSessionLength(int minutes) {
        return new LearningPreferences(preferredLearningStyle, difficultyPreference, 
                preferredContentTypes, developerId, minutes, sessionsPerWeek, enableRealTimeHints,
                enableProactiveRecommendations, enableProgressTracking, enablePeerComparison);
    }

    public LearningPreferences withContentTypes(List<ContentType> contentTypes) {
        return new LearningPreferences(preferredLearningStyle, difficultyPreference, 
                contentTypes, developerId, preferredSessionLengthMinutes, sessionsPerWeek, enableRealTimeHints,
                enableProactiveRecommendations, enableProgressTracking, enablePeerComparison);
    }

    public LearningPreferences withRealTimeHints(boolean enabled) {
        return new LearningPreferences(preferredLearningStyle, difficultyPreference, 
                preferredContentTypes, developerId, preferredSessionLengthMinutes, sessionsPerWeek, enabled,
                enableProactiveRecommendations, enableProgressTracking, enablePeerComparison);
    }

    public LearningPreferences withDeveloperId(String developerId) {
        return new LearningPreferences(preferredLearningStyle, difficultyPreference, 
                preferredContentTypes, developerId, preferredSessionLengthMinutes, sessionsPerWeek, enableRealTimeHints,
                enableProactiveRecommendations, enableProgressTracking, enablePeerComparison);
    }

    // Getters
    public LearningStyle getPreferredLearningStyle() { return preferredLearningStyle; }
    public DifficultyPreference getDifficultyPreference() { return difficultyPreference; }
    public List<ContentType> getPreferredContentTypes() { return new ArrayList<>(preferredContentTypes); }
    public String getDeveloperId() { return developerId; }
    public int getPreferredSessionLengthMinutes() { return preferredSessionLengthMinutes; }
    public int getSessionsPerWeek() { return sessionsPerWeek; }
    public boolean isEnableRealTimeHints() { return enableRealTimeHints; }
    public boolean isEnableProactiveRecommendations() { return enableProactiveRecommendations; }
    public boolean isEnableProgressTracking() { return enableProgressTracking; }
    public boolean isEnablePeerComparison() { return enablePeerComparison; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningPreferences that = (LearningPreferences) o;
        return preferredSessionLengthMinutes == that.preferredSessionLengthMinutes &&
                sessionsPerWeek == that.sessionsPerWeek &&
                enableRealTimeHints == that.enableRealTimeHints &&
                enableProactiveRecommendations == that.enableProactiveRecommendations &&
                enableProgressTracking == that.enableProgressTracking &&
                enablePeerComparison == that.enablePeerComparison &&
                preferredLearningStyle == that.preferredLearningStyle &&
                difficultyPreference == that.difficultyPreference &&
                Objects.equals(preferredContentTypes, that.preferredContentTypes) &&
                Objects.equals(developerId, that.developerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preferredLearningStyle, difficultyPreference, preferredContentTypes,
                developerId, preferredSessionLengthMinutes, sessionsPerWeek, enableRealTimeHints,
                enableProactiveRecommendations, enableProgressTracking, enablePeerComparison);
    }

    @Override
    public String toString() {
        return "LearningPreferences{" +
                "style=" + preferredLearningStyle +
                ", difficulty=" + difficultyPreference +
                ", contentTypes=" + preferredContentTypes +
                ", sessionLength=" + preferredSessionLengthMinutes + "min" +
                ", sessionsPerWeek=" + sessionsPerWeek +
                ", realTimeHints=" + enableRealTimeHints +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LearningStyle preferredLearningStyle = LearningStyle.VISUAL;
        private DifficultyPreference difficultyPreference = DifficultyPreference.ADAPTIVE;
        private List<ContentType> preferredContentTypes = List.of(ContentType.EXAMPLES, ContentType.TUTORIALS);
        private String developerId = null;
        private int preferredSessionLengthMinutes = 30;
        private int sessionsPerWeek = 3;
        private boolean enableRealTimeHints = true;
        private boolean enableProactiveRecommendations = true;
        private boolean enableProgressTracking = true;
        private boolean enablePeerComparison = false;

        public Builder preferredLearningStyle(LearningStyle preferredLearningStyle) {
            this.preferredLearningStyle = preferredLearningStyle;
            return this;
        }

        public Builder difficultyPreference(DifficultyPreference difficultyPreference) {
            this.difficultyPreference = difficultyPreference;
            return this;
        }

        public Builder developerId(String developerId) {
            this.developerId = developerId;
            return this;
        }

        public Builder preferredContentTypes(List<ContentType> preferredContentTypes) {
            this.preferredContentTypes = preferredContentTypes;
            return this;
        }

        public Builder preferredSessionLengthMinutes(int preferredSessionLengthMinutes) {
            this.preferredSessionLengthMinutes = preferredSessionLengthMinutes;
            return this;
        }

        public Builder sessionsPerWeek(int sessionsPerWeek) {
            this.sessionsPerWeek = sessionsPerWeek;
            return this;
        }

        public Builder enableRealTimeHints(boolean enableRealTimeHints) {
            this.enableRealTimeHints = enableRealTimeHints;
            return this;
        }

        public Builder enableProactiveRecommendations(boolean enableProactiveRecommendations) {
            this.enableProactiveRecommendations = enableProactiveRecommendations;
            return this;
        }

        public Builder enableProgressTracking(boolean enableProgressTracking) {
            this.enableProgressTracking = enableProgressTracking;
            return this;
        }

        public Builder enablePeerComparison(boolean enablePeerComparison) {
            this.enablePeerComparison = enablePeerComparison;
            return this;
        }

        public Builder detailLevel(String detailLevel) {
            // This is a convenience method for compatibility
            return this;
        }

        public LearningPreferences build() {
            return new LearningPreferences(preferredLearningStyle, difficultyPreference, preferredContentTypes,
                    developerId, preferredSessionLengthMinutes, sessionsPerWeek, enableRealTimeHints,
                    enableProactiveRecommendations, enableProgressTracking, enablePeerComparison);
        }
    }

    // Convenience methods for compatibility with existing code
    public String getDetailLevel() {
        return difficultyPreference == DifficultyPreference.EASY_FIRST ? "beginner" :
               difficultyPreference == DifficultyPreference.CHALLENGING_FIRST ? "advanced" : "intermediate";
    }

    public List<String> getPreferredContentTypesAsStrings() {
        return preferredContentTypes.stream()
                .map(type -> type.name().toLowerCase())
                .collect(java.util.stream.Collectors.toList());
    }

    public double getPreferredDifficulty() {
        return difficultyPreference == DifficultyPreference.EASY_FIRST ? 0.3 :
               difficultyPreference == DifficultyPreference.CHALLENGING_FIRST ? 0.8 : 0.5;
    }
}