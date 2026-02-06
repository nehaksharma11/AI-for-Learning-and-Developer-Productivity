package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComplexityMetrics model.
 */
class ComplexityMetricsTest {

    @Nested
    @DisplayName("Complexity Metrics Creation")
    class ComplexityMetricsCreation {

        @Test
        @DisplayName("Should create empty complexity metrics")
        void shouldCreateEmptyComplexityMetrics() {
            // When
            ComplexityMetrics metrics = ComplexityMetrics.empty();

            // Then
            assertEquals(0, metrics.getCyclomaticComplexity());
            assertEquals(0, metrics.getLinesOfCode());
            assertEquals(0, metrics.getCognitiveComplexity());
            assertEquals(0, metrics.getNestingDepth());
            assertEquals(0, metrics.getNumberOfMethods());
            assertEquals(0, metrics.getNumberOfClasses());
            assertEquals(0.0, metrics.getOverallComplexityScore());
            assertFalse(metrics.isHighlyComplex());
            assertFalse(metrics.needsRefactoring());
        }

        @Test
        @DisplayName("Should create simple complexity metrics")
        void shouldCreateSimpleComplexityMetrics() {
            // Given
            int linesOfCode = 100;
            int numberOfMethods = 5;

            // When
            ComplexityMetrics metrics = ComplexityMetrics.simple(linesOfCode, numberOfMethods);

            // Then
            assertEquals(numberOfMethods, metrics.getCyclomaticComplexity());
            assertEquals(linesOfCode, metrics.getLinesOfCode());
            assertEquals(numberOfMethods, metrics.getCognitiveComplexity());
            assertEquals(2, metrics.getNestingDepth());
            assertEquals(numberOfMethods, metrics.getNumberOfMethods());
            assertEquals(1, metrics.getNumberOfClasses());
            assertFalse(metrics.isHighlyComplex());
        }

        @Test
        @DisplayName("Should create complex complexity metrics")
        void shouldCreateComplexComplexityMetrics() {
            // Given
            int cyclomaticComplexity = 20;
            int linesOfCode = 1000;
            int cognitiveComplexity = 30;
            int nestingDepth = 8;

            // When
            ComplexityMetrics metrics = ComplexityMetrics.complex(
                cyclomaticComplexity, linesOfCode, cognitiveComplexity, nestingDepth);

            // Then
            assertEquals(cyclomaticComplexity, metrics.getCyclomaticComplexity());
            assertEquals(linesOfCode, metrics.getLinesOfCode());
            assertEquals(cognitiveComplexity, metrics.getCognitiveComplexity());
            assertEquals(nestingDepth, metrics.getNestingDepth());
            assertTrue(metrics.isHighlyComplex());
            assertTrue(metrics.needsRefactoring());
        }

        @Test
        @DisplayName("Should handle negative values by setting them to zero")
        void shouldHandleNegativeValuesBySettingThemToZero() {
            // When
            ComplexityMetrics metrics = new ComplexityMetrics(
                -5, -100, -10, -2, -3, -1, null);

            // Then
            assertEquals(0, metrics.getCyclomaticComplexity());
            assertEquals(0, metrics.getLinesOfCode());
            assertEquals(0, metrics.getCognitiveComplexity());
            assertEquals(0, metrics.getNestingDepth());
            assertEquals(0, metrics.getNumberOfMethods());
            assertEquals(0, metrics.getNumberOfClasses());
        }
    }

    @Nested
    @DisplayName("Complexity Score Calculation")
    class ComplexityScoreCalculation {

        @Test
        @DisplayName("Should calculate overall complexity score correctly for simple code")
        void shouldCalculateOverallComplexityScoreCorrectlyForSimpleCode() {
            // Given
            ComplexityMetrics metrics = new ComplexityMetrics(
                3, 50, 2, 2, 5, 1, null);

            // When
            double score = metrics.getOverallComplexityScore();

            // Then
            assertTrue(score >= 0.0 && score <= 1.0, "Score should be between 0.0 and 1.0");
            assertTrue(score < 0.3, "Simple code should have low complexity score");
        }

        @Test
        @DisplayName("Should calculate overall complexity score correctly for complex code")
        void shouldCalculateOverallComplexityScoreCorrectlyForComplexCode() {
            // Given
            ComplexityMetrics metrics = new ComplexityMetrics(
                25, 2000, 40, 10, 50, 10, null);

            // When
            double score = metrics.getOverallComplexityScore();

            // Then
            assertTrue(score >= 0.0 && score <= 1.0, "Score should be between 0.0 and 1.0");
            assertTrue(score > 0.7, "Complex code should have high complexity score");
        }

        @Test
        @DisplayName("Should return zero score for empty metrics")
        void shouldReturnZeroScoreForEmptyMetrics() {
            // Given
            ComplexityMetrics metrics = ComplexityMetrics.empty();

            // When
            double score = metrics.getOverallComplexityScore();

            // Then
            assertEquals(0.0, score, "Empty metrics should have zero complexity score");
        }
    }

    @Nested
    @DisplayName("Complexity Assessment")
    class ComplexityAssessment {

        @Test
        @DisplayName("Should identify highly complex code correctly")
        void shouldIdentifyHighlyComplexCodeCorrectly() {
            // Given
            ComplexityMetrics highComplexity = new ComplexityMetrics(
                20, 1000, 30, 8, 25, 5, null);
            ComplexityMetrics lowComplexity = new ComplexityMetrics(
                5, 100, 3, 2, 8, 2, null);

            // When & Then
            assertTrue(highComplexity.isHighlyComplex(), "Should identify high complexity");
            assertFalse(lowComplexity.isHighlyComplex(), "Should not identify low complexity as high");
        }

        @Test
        @DisplayName("Should identify code needing refactoring correctly")
        void shouldIdentifyCodeNeedingRefactoringCorrectly() {
            // Given - Code with high complexity
            ComplexityMetrics needsRefactoring1 = new ComplexityMetrics(
                20, 800, 30, 7, 40, 8, null);
            
            // Given - Code with many methods and lines
            ComplexityMetrics needsRefactoring2 = new ComplexityMetrics(
                12, 600, 18, 4, 25, 3, null);
            
            // Given - Simple code
            ComplexityMetrics doesNotNeedRefactoring = new ComplexityMetrics(
                5, 200, 4, 3, 10, 2, null);

            // When & Then
            assertTrue(needsRefactoring1.needsRefactoring(), 
                "High complexity code should need refactoring");
            assertTrue(needsRefactoring2.needsRefactoring(), 
                "Code with many methods should need refactoring");
            assertFalse(doesNotNeedRefactoring.needsRefactoring(), 
                "Simple code should not need refactoring");
        }
    }

    @Nested
    @DisplayName("Additional Metrics")
    class AdditionalMetrics {

        @Test
        @DisplayName("Should handle additional metrics correctly")
        void shouldHandleAdditionalMetricsCorrectly() {
            // Given
            ComplexityMetrics baseMetrics = ComplexityMetrics.simple(100, 5);
            
            // When
            ComplexityMetrics withAdditional = baseMetrics
                .withAdditionalMetric("testCoverage", 0.85)
                .withAdditionalMetric("duplication", 0.15);

            // Then
            Map<String, Double> additionalMetrics = withAdditional.getAdditionalMetrics();
            assertEquals(2, additionalMetrics.size());
            assertEquals(0.85, additionalMetrics.get("testCoverage"));
            assertEquals(0.15, additionalMetrics.get("duplication"));
            
            // Verify immutability
            assertNotSame(baseMetrics, withAdditional);
            assertTrue(baseMetrics.getAdditionalMetrics().isEmpty());
        }

        @Test
        @DisplayName("Should preserve immutability when adding metrics")
        void shouldPreserveImmutabilityWhenAddingMetrics() {
            // Given
            ComplexityMetrics original = ComplexityMetrics.simple(100, 5);
            
            // When
            ComplexityMetrics modified = original.withAdditionalMetric("newMetric", 0.5);
            
            // Then
            assertNotSame(original, modified);
            assertTrue(original.getAdditionalMetrics().isEmpty());
            assertEquals(1, modified.getAdditionalMetrics().size());
            
            // Verify other properties remain the same
            assertEquals(original.getCyclomaticComplexity(), modified.getCyclomaticComplexity());
            assertEquals(original.getLinesOfCode(), modified.getLinesOfCode());
        }
    }

    @Nested
    @DisplayName("Equality and Hash Code")
    class EqualityAndHashCode {

        @Test
        @DisplayName("Should be equal when all metrics are same")
        void shouldBeEqualWhenAllMetricsAreSame() {
            // Given
            ComplexityMetrics metrics1 = new ComplexityMetrics(10, 200, 8, 3, 15, 2, null);
            ComplexityMetrics metrics2 = new ComplexityMetrics(10, 200, 8, 3, 15, 2, null);

            // When & Then
            assertEquals(metrics1, metrics2);
            assertEquals(metrics1.hashCode(), metrics2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when metrics differ")
        void shouldNotBeEqualWhenMetricsDiffer() {
            // Given
            ComplexityMetrics metrics1 = new ComplexityMetrics(10, 200, 8, 3, 15, 2, null);
            ComplexityMetrics metrics2 = new ComplexityMetrics(15, 200, 8, 3, 15, 2, null);

            // When & Then
            assertNotEquals(metrics1, metrics2);
        }
    }
}