package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContextSwitchAnalysis Tests")
class ContextSwitchAnalysisTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create context switch analysis with all fields")
        void shouldCreateContextSwitchAnalysisWithAllFields() {
            LocalDateTime start = LocalDateTime.now().minusHours(8);
            LocalDateTime end = LocalDateTime.now();
            List<String> recommendations = Arrays.asList("Reduce interruptions", "Use focus blocks");

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .totalSwitches(25)
                    .averageSwitchesPerHour(3.1)
                    .totalProductivityLoss(0.4)
                    .mostCommonSwitchType(ContextSwitchEvent.SwitchType.INTERRUPTION)
                    .mostCommonSwitchReason(ContextSwitchEvent.SwitchReason.DISTRACTION)
                    .averageRecoveryTime(12.5)
                    .significantDisruptions(5)
                    .recommendations(recommendations)
                    .build();

            assertEquals("dev123", analysis.getDeveloperId());
            assertEquals(start, analysis.getAnalysisStart());
            assertEquals(end, analysis.getAnalysisEnd());
            assertEquals(25, analysis.getTotalSwitches());
            assertEquals(3.1, analysis.getAverageSwitchesPerHour(), 0.001);
            assertEquals(0.4, analysis.getTotalProductivityLoss(), 0.001);
            assertEquals(ContextSwitchEvent.SwitchType.INTERRUPTION, analysis.getMostCommonSwitchType());
            assertEquals(ContextSwitchEvent.SwitchReason.DISTRACTION, analysis.getMostCommonSwitchReason());
            assertEquals(12.5, analysis.getAverageRecoveryTime(), 0.001);
            assertEquals(5, analysis.getSignificantDisruptions());
            assertEquals(recommendations, analysis.getRecommendations());
        }

        @Test
        @DisplayName("Should create context switch analysis with minimal fields")
        void shouldCreateContextSwitchAnalysisWithMinimalFields() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .build();

            assertEquals("dev123", analysis.getDeveloperId());
            assertEquals(start, analysis.getAnalysisStart());
            assertEquals(end, analysis.getAnalysisEnd());
            assertEquals(0, analysis.getTotalSwitches());
            assertEquals(0.0, analysis.getAverageSwitchesPerHour(), 0.001);
            assertEquals(0.0, analysis.getTotalProductivityLoss(), 0.001);
            assertNull(analysis.getMostCommonSwitchType());
            assertNull(analysis.getMostCommonSwitchReason());
            assertEquals(0.0, analysis.getAverageRecoveryTime(), 0.001);
            assertEquals(0, analysis.getSignificantDisruptions());
            assertTrue(analysis.getRecommendations().isEmpty());
        }

        @Test
        @DisplayName("Should handle null developer ID")
        void shouldHandleNullDeveloperId() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            assertThrows(NullPointerException.class, () ->
                    ContextSwitchAnalysis.builder()
                            .developerId(null)
                            .analysisStart(start)
                            .analysisEnd(end)
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null analysis start")
        void shouldHandleNullAnalysisStart() {
            LocalDateTime end = LocalDateTime.now();

            assertThrows(NullPointerException.class, () ->
                    ContextSwitchAnalysis.builder()
                            .developerId("dev123")
                            .analysisStart(null)
                            .analysisEnd(end)
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null analysis end")
        void shouldHandleNullAnalysisEnd() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);

            assertThrows(NullPointerException.class, () ->
                    ContextSwitchAnalysis.builder()
                            .developerId("dev123")
                            .analysisStart(start)
                            .analysisEnd(null)
                            .build()
            );
        }

        @Test
        @DisplayName("Should clamp negative total switches to zero")
        void shouldClampNegativeTotalSwitchesToZero() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .totalSwitches(-5)
                    .build();

            assertEquals(0, analysis.getTotalSwitches());
        }

        @Test
        @DisplayName("Should clamp negative average switches per hour to zero")
        void shouldClampNegativeAverageSwitchesPerHourToZero() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .averageSwitchesPerHour(-2.5)
                    .build();

            assertEquals(0.0, analysis.getAverageSwitchesPerHour(), 0.001);
        }

        @Test
        @DisplayName("Should clamp negative total productivity loss to zero")
        void shouldClampNegativeTotalProductivityLossToZero() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .totalProductivityLoss(-0.3)
                    .build();

            assertEquals(0.0, analysis.getTotalProductivityLoss(), 0.001);
        }

        @Test
        @DisplayName("Should clamp negative average recovery time to zero")
        void shouldClampNegativeAverageRecoveryTimeToZero() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .averageRecoveryTime(-10.0)
                    .build();

            assertEquals(0.0, analysis.getAverageRecoveryTime(), 0.001);
        }

        @Test
        @DisplayName("Should clamp negative significant disruptions to zero")
        void shouldClampNegativeSignificantDisruptionsToZero() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .significantDisruptions(-3)
                    .build();

            assertEquals(0, analysis.getSignificantDisruptions());
        }
    }

    @Nested
    @DisplayName("Empty Analysis Tests")
    class EmptyAnalysisTests {

        @Test
        @DisplayName("Should create empty analysis")
        void shouldCreateEmptyAnalysis() {
            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.empty("dev123");

            assertEquals("dev123", analysis.getDeveloperId());
            assertNotNull(analysis.getAnalysisStart());
            assertNotNull(analysis.getAnalysisEnd());
            assertEquals(0, analysis.getTotalSwitches());
            assertEquals(0.0, analysis.getAverageSwitchesPerHour(), 0.001);
            assertEquals(0.0, analysis.getTotalProductivityLoss(), 0.001);
            assertEquals(ContextSwitchEvent.SwitchType.TASK_CHANGE, analysis.getMostCommonSwitchType());
            assertEquals(ContextSwitchEvent.SwitchReason.UNKNOWN, analysis.getMostCommonSwitchReason());
            assertEquals(0.0, analysis.getAverageRecoveryTime(), 0.001);
            assertEquals(0, analysis.getSignificantDisruptions());
            assertTrue(analysis.getRecommendations().isEmpty());
        }

        @Test
        @DisplayName("Should have analysis end after analysis start in empty analysis")
        void shouldHaveAnalysisEndAfterAnalysisStartInEmptyAnalysis() {
            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.empty("dev123");

            assertTrue(analysis.getAnalysisEnd().isAfter(analysis.getAnalysisStart()));
        }
    }

    @Nested
    @DisplayName("Recommendations Tests")
    class RecommendationsTests {

        @Test
        @DisplayName("Should handle null recommendations")
        void shouldHandleNullRecommendations() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .recommendations(null)
                    .build();

            assertNotNull(analysis.getRecommendations());
            assertTrue(analysis.getRecommendations().isEmpty());
        }

        @Test
        @DisplayName("Should create defensive copy of recommendations")
        void shouldCreateDefensiveCopyOfRecommendations() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();
            List<String> originalRecommendations = new ArrayList<>(Arrays.asList("Recommendation 1", "Recommendation 2"));

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .recommendations(originalRecommendations)
                    .build();

            // Modify original recommendations
            originalRecommendations.add("New Recommendation");

            // Analysis recommendations should not be affected
            assertEquals(2, analysis.getRecommendations().size());
            assertFalse(analysis.getRecommendations().contains("New Recommendation"));
        }

        @Test
        @DisplayName("Should return defensive copy from getter")
        void shouldReturnDefensiveCopyFromGetter() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();
            List<String> recommendations = Arrays.asList("Recommendation 1", "Recommendation 2");

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .recommendations(recommendations)
                    .build();

            List<String> retrievedRecommendations = analysis.getRecommendations();
            retrievedRecommendations.add("New Recommendation");

            // Original analysis recommendations should not be affected
            assertEquals(2, analysis.getRecommendations().size());
            assertFalse(analysis.getRecommendations().contains("New Recommendation"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle zero values")
        void shouldHandleZeroValues() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .totalSwitches(0)
                    .averageSwitchesPerHour(0.0)
                    .totalProductivityLoss(0.0)
                    .averageRecoveryTime(0.0)
                    .significantDisruptions(0)
                    .build();

            assertEquals(0, analysis.getTotalSwitches());
            assertEquals(0.0, analysis.getAverageSwitchesPerHour(), 0.001);
            assertEquals(0.0, analysis.getTotalProductivityLoss(), 0.001);
            assertEquals(0.0, analysis.getAverageRecoveryTime(), 0.001);
            assertEquals(0, analysis.getSignificantDisruptions());
        }

        @Test
        @DisplayName("Should handle large values")
        void shouldHandleLargeValues() {
            LocalDateTime start = LocalDateTime.now().minusHours(24);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .totalSwitches(1000)
                    .averageSwitchesPerHour(41.7)
                    .totalProductivityLoss(0.95)
                    .averageRecoveryTime(120.0)
                    .significantDisruptions(50)
                    .build();

            assertEquals(1000, analysis.getTotalSwitches());
            assertEquals(41.7, analysis.getAverageSwitchesPerHour(), 0.001);
            assertEquals(0.95, analysis.getTotalProductivityLoss(), 0.001);
            assertEquals(120.0, analysis.getAverageRecoveryTime(), 0.001);
            assertEquals(50, analysis.getSignificantDisruptions());
        }

        @Test
        @DisplayName("Should handle same start and end time")
        void shouldHandleSameStartAndEndTime() {
            LocalDateTime timestamp = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("dev123")
                    .analysisStart(timestamp)
                    .analysisEnd(timestamp)
                    .build();

            assertEquals(timestamp, analysis.getAnalysisStart());
            assertEquals(timestamp, analysis.getAnalysisEnd());
        }

        @Test
        @DisplayName("Should handle empty developer ID")
        void shouldHandleEmptyDeveloperId() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now();

            ContextSwitchAnalysis analysis = ContextSwitchAnalysis.builder()
                    .developerId("")
                    .analysisStart(start)
                    .analysisEnd(end)
                    .build();

            assertEquals("", analysis.getDeveloperId());
        }
    }
}