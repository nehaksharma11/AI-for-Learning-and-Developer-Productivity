package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeveloperProfile model.
 */
class DeveloperProfileTest {

    @Nested
    @DisplayName("Profile Creation")
    class ProfileCreation {

        @Test
        @DisplayName("Should create profile with valid parameters")
        void shouldCreateProfileWithValidParameters() {
            // Given
            String developerId = "dev123";
            LearningPreferences preferences = LearningPreferences.defaultPreferences();

            // When
            DeveloperProfile profile = DeveloperProfile.create(developerId, preferences);

            // Then
            assertNotNull(profile);
            assertEquals(developerId, profile.getId());
            assertEquals(preferences, profile.getLearningPreferences());
            assertTrue(profile.getSkillLevels().isEmpty());
            assertTrue(profile.getWorkHistory().isEmpty());
            assertTrue(profile.getAchievements().isEmpty());
            assertTrue(profile.getCurrentGoals().isEmpty());
            assertNotNull(profile.getCreatedAt());
            assertNotNull(profile.getLastUpdated());
        }

        @Test
        @DisplayName("Should throw exception for null developer ID")
        void shouldThrowExceptionForNullDeveloperId() {
            // Given
            LearningPreferences preferences = LearningPreferences.defaultPreferences();

            // When & Then
            assertThrows(NullPointerException.class, () -> 
                DeveloperProfile.create(null, preferences));
        }

        @Test
        @DisplayName("Should throw exception for null learning preferences")
        void shouldThrowExceptionForNullLearningPreferences() {
            // Given
            String developerId = "dev123";

            // When & Then
            assertThrows(NullPointerException.class, () -> 
                DeveloperProfile.create(developerId, null));
        }
    }

    @Nested
    @DisplayName("Profile Updates")
    class ProfileUpdates {

        @Test
        @DisplayName("Should update skill level correctly")
        void shouldUpdateSkillLevelCorrectly() {
            // Given
            DeveloperProfile profile = createTestProfile();
            SkillLevel javaSkill = SkillLevel.intermediate("Java");

            // When
            DeveloperProfile updatedProfile = profile.updateSkillLevel("Java", javaSkill);

            // Then
            assertNotSame(profile, updatedProfile);
            assertEquals(1, updatedProfile.getSkillLevels().size());
            assertEquals(javaSkill, updatedProfile.getSkillLevels().get("Java"));
            assertTrue(updatedProfile.getLastUpdated().isAfter(profile.getLastUpdated()));
        }

        @Test
        @DisplayName("Should add work session correctly")
        void shouldAddWorkSessionCorrectly() {
            // Given
            DeveloperProfile profile = createTestProfile();
            WorkSession session = createTestWorkSession();

            // When
            DeveloperProfile updatedProfile = profile.addWorkSession(session);

            // Then
            assertNotSame(profile, updatedProfile);
            assertEquals(1, updatedProfile.getWorkHistory().size());
            assertEquals(session, updatedProfile.getWorkHistory().get(0));
        }

        @Test
        @DisplayName("Should add achievement correctly")
        void shouldAddAchievementCorrectly() {
            // Given
            DeveloperProfile profile = createTestProfile();
            Achievement achievement = Achievement.skillMilestone("Java", "Intermediate");

            // When
            DeveloperProfile updatedProfile = profile.addAchievement(achievement);

            // Then
            assertNotSame(profile, updatedProfile);
            assertEquals(1, updatedProfile.getAchievements().size());
            assertEquals(achievement, updatedProfile.getAchievements().get(0));
        }
    }

    @Nested
    @DisplayName("Equality and Hash Code")
    class EqualityAndHashCode {

        @Test
        @DisplayName("Should be equal when IDs are same")
        void shouldBeEqualWhenIdsAreSame() {
            // Given
            DeveloperProfile profile1 = createTestProfile();
            DeveloperProfile profile2 = DeveloperProfile.create("dev123", 
                LearningPreferences.beginnerFriendly());

            // When & Then
            assertEquals(profile1, profile2);
            assertEquals(profile1.hashCode(), profile2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            // Given
            DeveloperProfile profile1 = createTestProfile();
            DeveloperProfile profile2 = DeveloperProfile.create("dev456", 
                LearningPreferences.defaultPreferences());

            // When & Then
            assertNotEquals(profile1, profile2);
        }
    }

    private DeveloperProfile createTestProfile() {
        return DeveloperProfile.create("dev123", LearningPreferences.defaultPreferences());
    }

    private WorkSession createTestWorkSession() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();
        return new WorkSession("session1", "dev123", start, end, 100, 5, 2, 3);
    }
}