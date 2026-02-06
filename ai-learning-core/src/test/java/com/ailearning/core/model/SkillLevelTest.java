package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SkillLevel model.
 */
class SkillLevelTest {

    @Nested
    @DisplayName("Skill Level Creation")
    class SkillLevelCreation {

        @Test
        @DisplayName("Should create skill level with valid parameters")
        void shouldCreateSkillLevelWithValidParameters() {
            // Given
            String domain = "Java";
            double proficiency = 0.7;
            double confidence = 0.8;
            LocalDateTime assessed = LocalDateTime.now();
            int evidence = 10;

            // When
            SkillLevel skill = new SkillLevel(domain, proficiency, confidence, assessed, evidence);

            // Then
            assertEquals(domain, skill.getDomain());
            assertEquals(proficiency, skill.getProficiency());
            assertEquals(confidence, skill.getConfidence());
            assertEquals(assessed, skill.getLastAssessed());
            assertEquals(evidence, skill.getEvidenceCount());
        }

        @Test
        @DisplayName("Should create beginner skill level")
        void shouldCreateBeginnerSkillLevel() {
            // When
            SkillLevel skill = SkillLevel.beginner("Python");

            // Then
            assertEquals("Python", skill.getDomain());
            assertEquals(0.1, skill.getProficiency());
            assertEquals(0.1, skill.getConfidence());
            assertEquals(0, skill.getEvidenceCount());
            assertFalse(skill.isExpert());
            assertTrue(skill.needsImprovement());
        }

        @Test
        @DisplayName("Should create intermediate skill level")
        void shouldCreateIntermediateSkillLevel() {
            // When
            SkillLevel skill = SkillLevel.intermediate("JavaScript");

            // Then
            assertEquals("JavaScript", skill.getDomain());
            assertEquals(0.5, skill.getProficiency());
            assertEquals(0.6, skill.getConfidence());
            assertEquals(5, skill.getEvidenceCount());
            assertFalse(skill.isExpert());
            assertFalse(skill.needsImprovement());
        }

        @Test
        @DisplayName("Should create advanced skill level")
        void shouldCreateAdvancedSkillLevel() {
            // When
            SkillLevel skill = SkillLevel.advanced("TypeScript");

            // Then
            assertEquals("TypeScript", skill.getDomain());
            assertEquals(0.8, skill.getProficiency());
            assertEquals(0.9, skill.getConfidence());
            assertEquals(15, skill.getEvidenceCount());
            assertTrue(skill.isExpert());
            assertFalse(skill.needsImprovement());
        }

        @Test
        @DisplayName("Should throw exception for invalid proficiency range")
        void shouldThrowExceptionForInvalidProficiencyRange() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                new SkillLevel("Java", 1.5, 0.5, LocalDateTime.now(), 5));
            
            assertThrows(IllegalArgumentException.class, () -> 
                new SkillLevel("Java", -0.1, 0.5, LocalDateTime.now(), 5));
        }

        @Test
        @DisplayName("Should throw exception for invalid confidence range")
        void shouldThrowExceptionForInvalidConfidenceRange() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                new SkillLevel("Java", 0.5, 1.5, LocalDateTime.now(), 5));
            
            assertThrows(IllegalArgumentException.class, () -> 
                new SkillLevel("Java", 0.5, -0.1, LocalDateTime.now(), 5));
        }
    }

    @Nested
    @DisplayName("Skill Level Updates")
    class SkillLevelUpdates {

        @Test
        @DisplayName("Should update proficiency correctly")
        void shouldUpdateProficiencyCorrectly() {
            // Given
            SkillLevel original = SkillLevel.beginner("Java");
            double newProficiency = 0.6;
            int additionalEvidence = 3;

            // When
            SkillLevel updated = original.updateProficiency(newProficiency, additionalEvidence);

            // Then
            assertNotSame(original, updated);
            assertEquals(newProficiency, updated.getProficiency());
            assertEquals(original.getConfidence(), updated.getConfidence());
            assertEquals(original.getEvidenceCount() + additionalEvidence, updated.getEvidenceCount());
            assertTrue(updated.getLastAssessed().isAfter(original.getLastAssessed()));
        }

        @Test
        @DisplayName("Should update confidence correctly")
        void shouldUpdateConfidenceCorrectly() {
            // Given
            SkillLevel original = SkillLevel.intermediate("Python");
            double newConfidence = 0.8;

            // When
            SkillLevel updated = original.updateConfidence(newConfidence);

            // Then
            assertNotSame(original, updated);
            assertEquals(original.getProficiency(), updated.getProficiency());
            assertEquals(newConfidence, updated.getConfidence());
            assertEquals(original.getEvidenceCount(), updated.getEvidenceCount());
        }

        @Test
        @DisplayName("Should add evidence correctly")
        void shouldAddEvidenceCorrectly() {
            // Given
            SkillLevel original = SkillLevel.intermediate("JavaScript");
            int evidenceToAdd = 5;

            // When
            SkillLevel updated = original.addEvidence(evidenceToAdd);

            // Then
            assertEquals(original.getEvidenceCount() + evidenceToAdd, updated.getEvidenceCount());
        }
    }

    @Nested
    @DisplayName("Skill Level Calculations")
    class SkillLevelCalculations {

        @Test
        @DisplayName("Should calculate overall score correctly")
        void shouldCalculateOverallScoreCorrectly() {
            // Given
            SkillLevel skill = new SkillLevel("Java", 0.8, 0.6, LocalDateTime.now(), 10);

            // When
            double overallScore = skill.getOverallScore();

            // Then
            double expected = (0.8 * 0.7) + (0.6 * 0.3); // 0.56 + 0.18 = 0.74
            assertEquals(expected, overallScore, 0.001);
        }

        @Test
        @DisplayName("Should identify expert correctly")
        void shouldIdentifyExpertCorrectly() {
            // Given
            SkillLevel expert = new SkillLevel("Java", 0.9, 0.9, LocalDateTime.now(), 15);
            SkillLevel notExpert = new SkillLevel("Python", 0.7, 0.8, LocalDateTime.now(), 5);

            // When & Then
            assertTrue(expert.isExpert());
            assertFalse(notExpert.isExpert());
        }

        @Test
        @DisplayName("Should identify skills needing improvement")
        void shouldIdentifySkillsNeedingImprovement() {
            // Given
            SkillLevel needsWork = new SkillLevel("C++", 0.3, 0.2, LocalDateTime.now(), 2);
            SkillLevel adequate = new SkillLevel("Go", 0.6, 0.7, LocalDateTime.now(), 8);

            // When & Then
            assertTrue(needsWork.needsImprovement());
            assertFalse(adequate.needsImprovement());
        }
    }
}