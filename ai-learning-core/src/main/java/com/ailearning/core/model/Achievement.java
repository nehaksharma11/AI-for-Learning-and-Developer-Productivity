package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a learning achievement earned by a developer.
 */
public class Achievement {
    
    public enum AchievementType {
        SKILL_MILESTONE, LEARNING_STREAK, CODE_QUALITY, PRODUCTIVITY, COLLABORATION
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String title;
    
    @NotBlank
    private final String description;
    
    @NotNull
    private final AchievementType type;
    
    @NotNull
    private final LocalDateTime earnedAt;
    
    private final String skillDomain;

    @JsonCreator
    public Achievement(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("type") AchievementType type,
            @JsonProperty("earnedAt") LocalDateTime earnedAt,
            @JsonProperty("skillDomain") String skillDomain) {
        this.id = Objects.requireNonNull(id, "Achievement ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.type = Objects.requireNonNull(type, "Achievement type cannot be null");
        this.earnedAt = Objects.requireNonNull(earnedAt, "Earned date cannot be null");
        this.skillDomain = skillDomain;
    }

    public static Achievement skillMilestone(String skillDomain, String level) {
        return new Achievement(
                "skill_" + skillDomain + "_" + level,
                level + " in " + skillDomain,
                "Reached " + level + " proficiency in " + skillDomain,
                AchievementType.SKILL_MILESTONE,
                LocalDateTime.now(),
                skillDomain
        );
    }

    public static Achievement learningStreak(int days) {
        return new Achievement(
                "streak_" + days + "_days",
                days + " Day Learning Streak",
                "Completed learning activities for " + days + " consecutive days",
                AchievementType.LEARNING_STREAK,
                LocalDateTime.now(),
                null
        );
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public AchievementType getType() { return type; }
    public LocalDateTime getEarnedAt() { return earnedAt; }
    public String getSkillDomain() { return skillDomain; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Achievement that = (Achievement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Achievement{" +
                "title='" + title + '\'' +
                ", type=" + type +
                ", earnedAt=" + earnedAt +
                '}';
    }
}