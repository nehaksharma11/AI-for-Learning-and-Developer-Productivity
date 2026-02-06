package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContextSwitchEvent Tests")
class ContextSwitchEventTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create context switch event with all fields")
        void shouldCreateContextSwitchEventWithAllFields() {
            LocalDateTime timestamp = LocalDateTime.now();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("key", "value");

            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .timestamp(timestamp)
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .fromContext("context1")
                    .toContext("context2")
                    .productivityImpact(0.8)
                    .recoveryTimeMinutes(15)
                    .contextSwitchCost(0.3)
                    .metadata(metadata)
                    .build();

            assertEquals("dev123", event.getDeveloperId());
            assertEquals(timestamp, event.getTimestamp());
            assertEquals(ContextSwitchEvent.SwitchType.TASK_CHANGE, event.getSwitchType());
            assertEquals(ContextSwitchEvent.SwitchReason.PLANNED, event.getSwitchReason());
            assertEquals("context1", event.getFromContext());
            assertEquals("context2", event.getToContext());
            assertEquals(0.8, event.getProductivityImpact(), 0.001);
            assertEquals(15, event.getRecoveryTimeMinutes());
            assertEquals(0.3, event.getContextSwitchCost(), 0.001);
            assertEquals(metadata, event.getMetadata());
        }

        @Test
        @DisplayName("Should create context switch event with minimal fields")
        void shouldCreateContextSwitchEventWithMinimalFields() {
            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .build();

            assertEquals("dev123", event.getDeveloperId());
            assertNotNull(event.getTimestamp());
            assertEquals(ContextSwitchEvent.SwitchType.TASK_CHANGE, event.getSwitchType());
            assertEquals(ContextSwitchEvent.SwitchReason.PLANNED, event.getSwitchReason());
            assertEquals("", event.getFromContext());
            assertEquals("", event.getToContext());
            assertEquals(0.0, event.getProductivityImpact(), 0.001);
            assertEquals(0, event.getRecoveryTimeMinutes());
            assertEquals(0.0, event.getContextSwitchCost(), 0.001);
            assertTrue(event.getMetadata().isEmpty());
        }

        @Test
        @DisplayName("Should handle null developer ID")
        void shouldHandleNullDeveloperId() {
            assertThrows(NullPointerException.class, () ->
                    ContextSwitchEvent.builder()
                            .developerId(null)
                            .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                            .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null switch type")
        void shouldHandleNullSwitchType() {
            assertThrows(NullPointerException.class, () ->
                    ContextSwitchEvent.builder()
                            .developerId("dev123")
                            .switchType(null)
                            .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null switch reason")
        void shouldHandleNullSwitchReason() {
            assertThrows(NullPointerException.class, () ->
                    ContextSwitchEvent.builder()
                            .developerId("dev123")
                            .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                            .switchReason(null)
                            .build()
            );
        }

        @Test
        @DisplayName("Should clamp negative productivity impact to zero")
        void shouldClampNegativeProductivityImpactToZero() {
            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .productivityImpact(-0.5)
                    .build();

            assertEquals(0.0, event.getProductivityImpact(), 0.001);
        }

        @Test
        @DisplayName("Should clamp productivity impact above 1.0 to 1.0")
        void shouldClampProductivityImpactAboveOneToOne() {
            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .productivityImpact(1.5)
                    .build();

            assertEquals(1.0, event.getProductivityImpact(), 0.001);
        }

        @Test
        @DisplayName("Should clamp negative recovery time to zero")
        void shouldClampNegativeRecoveryTimeToZero() {
            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .recoveryTimeMinutes(-10)
                    .build();

            assertEquals(0, event.getRecoveryTimeMinutes());
        }

        @Test
        @DisplayName("Should clamp negative context switch cost to zero")
        void shouldClampNegativeContextSwitchCostToZero() {
            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .contextSwitchCost(-0.2)
                    .build();

            assertEquals(0.0, event.getContextSwitchCost(), 0.001);
        }

        @Test
        @DisplayName("Should clamp context switch cost above 1.0 to 1.0")
        void shouldClampContextSwitchCostAboveOneToOne() {
            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .contextSwitchCost(1.5)
                    .build();

            assertEquals(1.0, event.getContextSwitchCost(), 0.001);
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("Should have all expected switch types")
        void shouldHaveAllExpectedSwitchTypes() {
            ContextSwitchEvent.SwitchType[] types = ContextSwitchEvent.SwitchType.values();
            
            assertEquals(6, types.length);
            assertTrue(java.util.Arrays.asList(types).contains(ContextSwitchEvent.SwitchType.TASK_CHANGE));
            assertTrue(java.util.Arrays.asList(types).contains(ContextSwitchEvent.SwitchType.PROJECT_CHANGE));
            assertTrue(java.util.Arrays.asList(types).contains(ContextSwitchEvent.SwitchType.ACTIVITY_CHANGE));
            assertTrue(java.util.Arrays.asList(types).contains(ContextSwitchEvent.SwitchType.FILE_CHANGE));
            assertTrue(java.util.Arrays.asList(types).contains(ContextSwitchEvent.SwitchType.INTERRUPTION));
            assertTrue(java.util.Arrays.asList(types).contains(ContextSwitchEvent.SwitchType.BREAK));
        }

        @Test
        @DisplayName("Should have all expected switch reasons")
        void shouldHaveAllExpectedSwitchReasons() {
            ContextSwitchEvent.SwitchReason[] reasons = ContextSwitchEvent.SwitchReason.values();
            
            assertEquals(7, reasons.length);
            assertTrue(java.util.Arrays.asList(reasons).contains(ContextSwitchEvent.SwitchReason.PLANNED));
            assertTrue(java.util.Arrays.asList(reasons).contains(ContextSwitchEvent.SwitchReason.INTERRUPTION));
            assertTrue(java.util.Arrays.asList(reasons).contains(ContextSwitchEvent.SwitchReason.DISTRACTION));
            assertTrue(java.util.Arrays.asList(reasons).contains(ContextSwitchEvent.SwitchReason.COMPLETION));
            assertTrue(java.util.Arrays.asList(reasons).contains(ContextSwitchEvent.SwitchReason.BLOCKED));
            assertTrue(java.util.Arrays.asList(reasons).contains(ContextSwitchEvent.SwitchReason.PRIORITY_CHANGE));
            assertTrue(java.util.Arrays.asList(reasons).contains(ContextSwitchEvent.SwitchReason.UNKNOWN));
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("Should handle null metadata")
        void shouldHandleNullMetadata() {
            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .metadata(null)
                    .build();

            assertNotNull(event.getMetadata());
            assertTrue(event.getMetadata().isEmpty());
        }

        @Test
        @DisplayName("Should create defensive copy of metadata")
        void shouldCreateDefensiveCopyOfMetadata() {
            Map<String, Object> originalMetadata = new HashMap<>();
            originalMetadata.put("key", "value");

            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .metadata(originalMetadata)
                    .build();

            // Modify original metadata
            originalMetadata.put("newKey", "newValue");

            // Event metadata should not be affected
            assertFalse(event.getMetadata().containsKey("newKey"));
            assertEquals(1, event.getMetadata().size());
        }

        @Test
        @DisplayName("Should return defensive copy from getter")
        void shouldReturnDefensiveCopyFromGetter() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("key", "value");

            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .metadata(metadata)
                    .build();

            Map<String, Object> retrievedMetadata = event.getMetadata();
            retrievedMetadata.put("newKey", "newValue");

            // Original event metadata should not be affected
            assertFalse(event.getMetadata().containsKey("newKey"));
            assertEquals(1, event.getMetadata().size());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty strings for context")
        void shouldHandleEmptyStringsForContext() {
            ContextSwitchEvent event = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .fromContext("")
                    .toContext("")
                    .build();

            assertEquals("", event.getFromContext());
            assertEquals("", event.getToContext());
        }

        @Test
        @DisplayName("Should handle boundary values for productivity impact")
        void shouldHandleBoundaryValuesForProductivityImpact() {
            ContextSwitchEvent event1 = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .productivityImpact(0.0)
                    .build();

            ContextSwitchEvent event2 = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .productivityImpact(1.0)
                    .build();

            assertEquals(0.0, event1.getProductivityImpact(), 0.001);
            assertEquals(1.0, event2.getProductivityImpact(), 0.001);
        }

        @Test
        @DisplayName("Should handle boundary values for context switch cost")
        void shouldHandleBoundaryValuesForContextSwitchCost() {
            ContextSwitchEvent event1 = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .contextSwitchCost(0.0)
                    .build();

            ContextSwitchEvent event2 = ContextSwitchEvent.builder()
                    .developerId("dev123")
                    .switchType(ContextSwitchEvent.SwitchType.TASK_CHANGE)
                    .switchReason(ContextSwitchEvent.SwitchReason.PLANNED)
                    .contextSwitchCost(1.0)
                    .build();

            assertEquals(0.0, event1.getContextSwitchCost(), 0.001);
            assertEquals(1.0, event2.getContextSwitchCost(), 0.001);
        }
    }
}