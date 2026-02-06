package com.ailearning.core.model.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AIExplanation Tests")
class AIExplanationTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create explanation with all fields")
        void shouldCreateExplanationWithAllFields() {
            LocalDateTime now = LocalDateTime.now();
            List<String> keyPoints = Arrays.asList("Point 1", "Point 2");
            List<String> concepts = Arrays.asList("Concept 1", "Concept 2");

            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet("public void test() {}")
                    .explanation("This is a test method")
                    .summary("Test method summary")
                    .keyPoints(keyPoints)
                    .relatedConcepts(concepts)
                    .difficulty("INTERMEDIATE")
                    .confidenceScore(0.85)
                    .language("Java")
                    .generatedAt(now)
                    .serviceProvider("TestService")
                    .build();

            assertEquals("public void test() {}", explanation.getCodeSnippet());
            assertEquals("This is a test method", explanation.getExplanation());
            assertEquals("Test method summary", explanation.getSummary());
            assertEquals(keyPoints, explanation.getKeyPoints());
            assertEquals(concepts, explanation.getRelatedConcepts());
            assertEquals("INTERMEDIATE", explanation.getDifficulty());
            assertEquals(0.85, explanation.getConfidenceScore(), 0.001);
            assertEquals("Java", explanation.getLanguage());
            assertEquals(now, explanation.getGeneratedAt());
            assertEquals("TestService", explanation.getServiceProvider());
        }

        @Test
        @DisplayName("Should create explanation with minimal fields")
        void shouldCreateExplanationWithMinimalFields() {
            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet("int x = 5;")
                    .explanation("Variable declaration")
                    .build();

            assertEquals("int x = 5;", explanation.getCodeSnippet());
            assertEquals("Variable declaration", explanation.getExplanation());
            assertEquals("", explanation.getSummary());
            assertTrue(explanation.getKeyPoints().isEmpty());
            assertTrue(explanation.getRelatedConcepts().isEmpty());
            assertEquals("UNKNOWN", explanation.getDifficulty());
            assertEquals(0.0, explanation.getConfidenceScore(), 0.001);
            assertEquals("UNKNOWN", explanation.getLanguage());
            assertEquals("UNKNOWN", explanation.getServiceProvider());
            assertNotNull(explanation.getGeneratedAt());
        }

        @Test
        @DisplayName("Should handle null code snippet")
        void shouldHandleNullCodeSnippet() {
            assertThrows(NullPointerException.class, () ->
                    AIExplanation.builder()
                            .codeSnippet(null)
                            .explanation("Test explanation")
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null explanation")
        void shouldHandleNullExplanation() {
            assertThrows(NullPointerException.class, () ->
                    AIExplanation.builder()
                            .codeSnippet("test code")
                            .explanation(null)
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet("test code")
                    .explanation("test explanation")
                    .summary(null)
                    .keyPoints(null)
                    .relatedConcepts(null)
                    .difficulty(null)
                    .language(null)
                    .generatedAt(null)
                    .serviceProvider(null)
                    .build();

            assertEquals("", explanation.getSummary());
            assertTrue(explanation.getKeyPoints().isEmpty());
            assertTrue(explanation.getRelatedConcepts().isEmpty());
            assertEquals("UNKNOWN", explanation.getDifficulty());
            assertEquals("UNKNOWN", explanation.getLanguage());
            assertEquals("UNKNOWN", explanation.getServiceProvider());
            assertNotNull(explanation.getGeneratedAt());
        }

        @Test
        @DisplayName("Should clamp confidence score to valid range")
        void shouldClampConfidenceScoreToValidRange() {
            AIExplanation lowConfidence = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("test")
                    .confidenceScore(-0.5)
                    .build();
            assertEquals(0.0, lowConfidence.getConfidenceScore(), 0.001);

            AIExplanation highConfidence = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("test")
                    .confidenceScore(1.5)
                    .build();
            assertEquals(1.0, highConfidence.getConfidenceScore(), 0.001);

            AIExplanation validConfidence = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("test")
                    .confidenceScore(0.75)
                    .build();
            assertEquals(0.75, validConfidence.getConfidenceScore(), 0.001);
        }
    }

    @Nested
    @DisplayName("Defensive Copy Tests")
    class DefensiveCopyTests {

        @Test
        @DisplayName("Should create defensive copy of key points")
        void shouldCreateDefensiveCopyOfKeyPoints() {
            List<String> originalKeyPoints = Arrays.asList("Point 1", "Point 2");

            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("test")
                    .keyPoints(originalKeyPoints)
                    .build();

            // Modify original list
            originalKeyPoints.add("Point 3");

            // Explanation should not be affected
            assertEquals(2, explanation.getKeyPoints().size());
            assertFalse(explanation.getKeyPoints().contains("Point 3"));
        }

        @Test
        @DisplayName("Should return defensive copy from getter")
        void shouldReturnDefensiveCopyFromGetter() {
            List<String> keyPoints = Arrays.asList("Point 1", "Point 2");

            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("test")
                    .keyPoints(keyPoints)
                    .build();

            List<String> retrievedKeyPoints = explanation.getKeyPoints();
            retrievedKeyPoints.add("Point 3");

            // Original explanation should not be affected
            assertEquals(2, explanation.getKeyPoints().size());
            assertFalse(explanation.getKeyPoints().contains("Point 3"));
        }

        @Test
        @DisplayName("Should create defensive copy of related concepts")
        void shouldCreateDefensiveCopyOfRelatedConcepts() {
            List<String> originalConcepts = Arrays.asList("Concept 1", "Concept 2");

            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("test")
                    .relatedConcepts(originalConcepts)
                    .build();

            // Modify original list
            originalConcepts.add("Concept 3");

            // Explanation should not be affected
            assertEquals(2, explanation.getRelatedConcepts().size());
            assertFalse(explanation.getRelatedConcepts().contains("Concept 3"));
        }

        @Test
        @DisplayName("Should return defensive copy of related concepts from getter")
        void shouldReturnDefensiveCopyOfRelatedConceptsFromGetter() {
            List<String> concepts = Arrays.asList("Concept 1", "Concept 2");

            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("test")
                    .relatedConcepts(concepts)
                    .build();

            List<String> retrievedConcepts = explanation.getRelatedConcepts();
            retrievedConcepts.add("Concept 3");

            // Original explanation should not be affected
            assertEquals(2, explanation.getRelatedConcepts().size());
            assertFalse(explanation.getRelatedConcepts().contains("Concept 3"));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet("test code")
                    .explanation("test explanation")
                    .language("Java")
                    .difficulty("INTERMEDIATE")
                    .confidenceScore(0.85)
                    .serviceProvider("TestService")
                    .build();

            String toString = explanation.toString();

            assertTrue(toString.contains("language='Java'"));
            assertTrue(toString.contains("difficulty='INTERMEDIATE'"));
            assertTrue(toString.contains("confidenceScore=0.85"));
            assertTrue(toString.contains("serviceProvider='TestService'"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet("")
                    .explanation("")
                    .summary("")
                    .difficulty("")
                    .language("")
                    .serviceProvider("")
                    .build();

            assertEquals("", explanation.getCodeSnippet());
            assertEquals("", explanation.getExplanation());
            assertEquals("", explanation.getSummary());
            assertEquals("", explanation.getDifficulty());
            assertEquals("", explanation.getLanguage());
            assertEquals("", explanation.getServiceProvider());
        }

        @Test
        @DisplayName("Should handle boundary confidence scores")
        void shouldHandleBoundaryConfidenceScores() {
            AIExplanation minConfidence = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("test")
                    .confidenceScore(0.0)
                    .build();
            assertEquals(0.0, minConfidence.getConfidenceScore(), 0.001);

            AIExplanation maxConfidence = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("test")
                    .confidenceScore(1.0)
                    .build();
            assertEquals(1.0, maxConfidence.getConfidenceScore(), 0.001);
        }

        @Test
        @DisplayName("Should handle large text content")
        void shouldHandleLargeTextContent() {
            String largeCode = "x".repeat(10000);
            String largeExplanation = "y".repeat(10000);

            AIExplanation explanation = AIExplanation.builder()
                    .codeSnippet(largeCode)
                    .explanation(largeExplanation)
                    .build();

            assertEquals(largeCode, explanation.getCodeSnippet());
            assertEquals(largeExplanation, explanation.getExplanation());
        }
    }
}