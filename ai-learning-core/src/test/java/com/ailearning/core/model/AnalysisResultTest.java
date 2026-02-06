package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnalysisResult model.
 */
class AnalysisResultTest {

    @Nested
    @DisplayName("Analysis Result Creation")
    class AnalysisResultCreation {

        @Test
        @DisplayName("Should create successful analysis result")
        void shouldCreateSuccessfulAnalysisResult() {
            // Given
            String filePath = "src/main/java/Example.java";
            ComplexityMetrics metrics = ComplexityMetrics.simple(100, 5);
            List<Suggestion> suggestions = List.of(
                Suggestion.bestPractice("Add documentation", "Improves maintainability", null)
            );
            long analysisTime = 50;

            // When
            AnalysisResult result = AnalysisResult.success(filePath, metrics, suggestions, analysisTime);

            // Then
            assertNotNull(result);
            assertEquals(filePath, result.getFilePath());
            assertEquals(AnalysisResult.AnalysisStatus.SUCCESS, result.getStatus());
            assertEquals(metrics, result.getComplexityMetrics());
            assertEquals(1, result.getSuggestions().size());
            assertEquals(analysisTime, result.getAnalysisTimeMs());
            assertTrue(result.getIssues().isEmpty());
            assertTrue(result.isSuccessful());
            assertFalse(result.hasCriticalIssues());
        }

        @Test
        @DisplayName("Should create analysis result with issues")
        void shouldCreateAnalysisResultWithIssues() {
            // Given
            String filePath = "src/main/java/Buggy.java";
            List<CodeIssue> issues = List.of(
                CodeIssue.bug("Null pointer exception", filePath, 10, "Add null check"),
                CodeIssue.performanceWarning("Inefficient loop", filePath, 20, "Use stream API")
            );
            ComplexityMetrics metrics = ComplexityMetrics.complex(15, 200, 20, 5);
            long analysisTime = 75;

            // When
            AnalysisResult result = AnalysisResult.withIssues(filePath, issues, metrics, analysisTime);

            // Then
            assertEquals(AnalysisResult.AnalysisStatus.ERROR, result.getStatus());
            assertEquals(2, result.getIssues().size());
            assertEquals(1, result.getIssueCountBySeverity(CodeIssue.Severity.ERROR));
            assertEquals(1, result.getIssueCountBySeverity(CodeIssue.Severity.WARNING));
            assertTrue(result.hasCriticalIssues());
            assertFalse(result.isSuccessful());
        }

        @Test
        @DisplayName("Should create error analysis result")
        void shouldCreateErrorAnalysisResult() {
            // Given
            String filePath = "src/main/java/Invalid.java";
            String errorMessage = "File not found";

            // When
            AnalysisResult result = AnalysisResult.error(filePath, errorMessage);

            // Then
            assertEquals(AnalysisResult.AnalysisStatus.ERROR, result.getStatus());
            assertTrue(result.getSummary().contains(errorMessage));
            assertEquals(0.0, result.getQualityScore());
            assertFalse(result.isSuccessful());
        }
    }

    @Nested
    @DisplayName("Quality Score Calculation")
    class QualityScoreCalculation {

        @Test
        @DisplayName("Should calculate quality score correctly for clean code")
        void shouldCalculateQualityScoreCorrectlyForCleanCode() {
            // Given
            ComplexityMetrics simpleMetrics = ComplexityMetrics.simple(50, 3);
            AnalysisResult result = AnalysisResult.success("test.java", simpleMetrics, List.of(), 25);

            // When
            double qualityScore = result.getQualityScore();

            // Then
            assertTrue(qualityScore > 0.8, "Clean code should have high quality score");
            assertTrue(qualityScore <= 1.0, "Quality score should not exceed 1.0");
        }

        @Test
        @DisplayName("Should calculate quality score correctly for complex code with issues")
        void shouldCalculateQualityScoreCorrectlyForComplexCodeWithIssues() {
            // Given
            List<CodeIssue> issues = List.of(
                CodeIssue.bug("Bug 1", "test.java", 10, "Fix it"),
                CodeIssue.performanceWarning("Warning 1", "test.java", 20, "Optimize")
            );
            ComplexityMetrics complexMetrics = ComplexityMetrics.complex(20, 500, 30, 8);
            AnalysisResult result = AnalysisResult.withIssues("test.java", issues, complexMetrics, 100);

            // When
            double qualityScore = result.getQualityScore();

            // Then
            assertTrue(qualityScore < 0.5, "Complex code with issues should have low quality score");
            assertTrue(qualityScore >= 0.0, "Quality score should not be negative");
        }

        @Test
        @DisplayName("Should return zero quality score for error status")
        void shouldReturnZeroQualityScoreForErrorStatus() {
            // Given
            AnalysisResult result = AnalysisResult.error("test.java", "Parse error");

            // When
            double qualityScore = result.getQualityScore();

            // Then
            assertEquals(0.0, qualityScore, "Error status should result in zero quality score");
        }
    }

    @Nested
    @DisplayName("Issue Analysis")
    class IssueAnalysis {

        @Test
        @DisplayName("Should count issues by severity correctly")
        void shouldCountIssuesBySeverityCorrectly() {
            // Given
            List<CodeIssue> issues = List.of(
                CodeIssue.bug("Error 1", "test.java", 10, "Fix"),
                CodeIssue.bug("Error 2", "test.java", 15, "Fix"),
                CodeIssue.performanceWarning("Warning 1", "test.java", 20, "Optimize"),
                CodeIssue.styleViolation("Style issue", "test.java", 25, "STYLE_001")
            );
            AnalysisResult result = AnalysisResult.withIssues("test.java", issues, 
                ComplexityMetrics.empty(), 50);

            // When & Then
            assertEquals(2, result.getIssueCountBySeverity(CodeIssue.Severity.ERROR));
            assertEquals(1, result.getIssueCountBySeverity(CodeIssue.Severity.WARNING));
            assertEquals(1, result.getIssueCountBySeverity(CodeIssue.Severity.INFO));
            assertEquals(0, result.getIssueCountBySeverity(CodeIssue.Severity.INFO)); // No INFO issues in this test
        }

        @Test
        @DisplayName("Should identify critical issues correctly")
        void shouldIdentifyCriticalIssuesCorrectly() {
            // Given
            List<CodeIssue> criticalIssues = List.of(
                CodeIssue.securityIssue("SQL Injection", "test.java", 10, "SEC_001")
            );
            List<CodeIssue> nonCriticalIssues = List.of(
                CodeIssue.styleViolation("Missing space", "test.java", 5, "STYLE_001")
            );

            // When
            AnalysisResult criticalResult = AnalysisResult.withIssues("test.java", criticalIssues, 
                ComplexityMetrics.empty(), 30);
            AnalysisResult nonCriticalResult = AnalysisResult.withIssues("test.java", nonCriticalIssues, 
                ComplexityMetrics.empty(), 20);

            // Then
            assertTrue(criticalResult.hasCriticalIssues());
            assertFalse(nonCriticalResult.hasCriticalIssues());
        }
    }
}