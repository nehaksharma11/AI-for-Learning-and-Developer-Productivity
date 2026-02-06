package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContextRestorationPlan Tests")
class ContextRestorationPlanTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create context restoration plan with all fields")
        void shouldCreateContextRestorationPlanWithAllFields() {
            LocalDateTime createdAt = LocalDateTime.now();
            WorkState workState = WorkState.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .build();

            List<RestorationStep> steps = Arrays.asList(
                    RestorationStep.builder()
                            .stepNumber(1)
                            .title("Open Files")
                            .description("Open required files")
                            .estimatedTimeMinutes(2)
                            .instructions(Arrays.asList("Open file1.java", "Open file2.java"))
                            .build(),
                    RestorationStep.builder()
                            .stepNumber(2)
                            .title("Set Context")
                            .description("Set active context")
                            .estimatedTimeMinutes(3)
                            .instructions(Arrays.asList("Navigate to method", "Set breakpoint"))
                            .build()
            );

            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .workState(workState)
                    .steps(steps)
                    .totalEstimatedTimeMinutes(5)
                    .createdAt(createdAt)
                    .build();

            assertEquals("dev123", plan.getDeveloperId());
            assertEquals("ws123", plan.getWorkStateId());
            assertEquals(workState, plan.getWorkState());
            assertEquals(steps, plan.getSteps());
            assertEquals(5, plan.getTotalEstimatedTimeMinutes());
            assertEquals(createdAt, plan.getCreatedAt());
        }

        @Test
        @DisplayName("Should create context restoration plan with minimal fields")
        void shouldCreateContextRestorationPlanWithMinimalFields() {
            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .build();

            assertEquals("dev123", plan.getDeveloperId());
            assertEquals("ws123", plan.getWorkStateId());
            assertNull(plan.getWorkState());
            assertTrue(plan.getSteps().isEmpty());
            assertEquals(0, plan.getTotalEstimatedTimeMinutes());
            assertNotNull(plan.getCreatedAt());
        }

        @Test
        @DisplayName("Should handle null work state")
        void shouldHandleNullWorkState() {
            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .workState(null)
                    .build();

            assertNull(plan.getWorkState());
        }

        @Test
        @DisplayName("Should handle null steps")
        void shouldHandleNullSteps() {
            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .steps(null)
                    .build();

            assertNotNull(plan.getSteps());
            assertTrue(plan.getSteps().isEmpty());
        }

        @Test
        @DisplayName("Should handle null created at")
        void shouldHandleNullCreatedAt() {
            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .createdAt(null)
                    .build();

            assertNotNull(plan.getCreatedAt());
        }

        @Test
        @DisplayName("Should clamp negative total estimated time to zero")
        void shouldClampNegativeTotalEstimatedTimeToZero() {
            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .totalEstimatedTimeMinutes(-10)
                    .build();

            assertEquals(0, plan.getTotalEstimatedTimeMinutes());
        }
    }

    @Nested
    @DisplayName("Empty Plan Tests")
    class EmptyPlanTests {

        @Test
        @DisplayName("Should create empty plan")
        void shouldCreateEmptyPlan() {
            ContextRestorationPlan plan = ContextRestorationPlan.empty();

            assertEquals("", plan.getDeveloperId());
            assertEquals("", plan.getWorkStateId());
            assertNull(plan.getWorkState());
            assertTrue(plan.getSteps().isEmpty());
            assertEquals(0, plan.getTotalEstimatedTimeMinutes());
            assertNotNull(plan.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Steps Tests")
    class StepsTests {

        @Test
        @DisplayName("Should create defensive copy of steps")
        void shouldCreateDefensiveCopyOfSteps() {
            List<RestorationStep> originalSteps = new ArrayList<>(Arrays.asList(
                    RestorationStep.builder()
                            .stepNumber(1)
                            .title("Step 1")
                            .description("Description 1")
                            .build()
            ));

            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .steps(originalSteps)
                    .build();

            // Modify original steps
            originalSteps.add(RestorationStep.builder()
                    .stepNumber(2)
                    .title("Step 2")
                    .description("Description 2")
                    .build());

            // Plan steps should not be affected
            assertEquals(1, plan.getSteps().size());
        }

        @Test
        @DisplayName("Should return defensive copy from getter")
        void shouldReturnDefensiveCopyFromGetter() {
            List<RestorationStep> steps = Arrays.asList(
                    RestorationStep.builder()
                            .stepNumber(1)
                            .title("Step 1")
                            .description("Description 1")
                            .build()
            );

            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .steps(steps)
                    .build();

            List<RestorationStep> retrievedSteps = plan.getSteps();
            retrievedSteps.add(RestorationStep.builder()
                    .stepNumber(2)
                    .title("Step 2")
                    .description("Description 2")
                    .build());

            // Original plan steps should not be affected
            assertEquals(1, plan.getSteps().size());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("")
                    .workStateId("")
                    .build();

            assertEquals("", plan.getDeveloperId());
            assertEquals("", plan.getWorkStateId());
        }

        @Test
        @DisplayName("Should handle zero estimated time")
        void shouldHandleZeroEstimatedTime() {
            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .totalEstimatedTimeMinutes(0)
                    .build();

            assertEquals(0, plan.getTotalEstimatedTimeMinutes());
        }

        @Test
        @DisplayName("Should handle large estimated time")
        void shouldHandleLargeEstimatedTime() {
            ContextRestorationPlan plan = ContextRestorationPlan.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .totalEstimatedTimeMinutes(1440) // 24 hours
                    .build();

            assertEquals(1440, plan.getTotalEstimatedTimeMinutes());
        }
    }
}

@DisplayName("RestorationStep Tests")
class RestorationStepTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create restoration step with all fields")
        void shouldCreateRestorationStepWithAllFields() {
            List<String> instructions = Arrays.asList("Instruction 1", "Instruction 2");

            RestorationStep step = RestorationStep.builder()
                    .stepNumber(1)
                    .title("Open Files")
                    .description("Open required files for the task")
                    .estimatedTimeMinutes(5)
                    .instructions(instructions)
                    .build();

            assertEquals(1, step.getStepNumber());
            assertEquals("Open Files", step.getTitle());
            assertEquals("Open required files for the task", step.getDescription());
            assertEquals(5, step.getEstimatedTimeMinutes());
            assertEquals(instructions, step.getInstructions());
        }

        @Test
        @DisplayName("Should create restoration step with minimal fields")
        void shouldCreateRestorationStepWithMinimalFields() {
            RestorationStep step = RestorationStep.builder()
                    .stepNumber(1)
                    .title("Step Title")
                    .description("Step Description")
                    .build();

            assertEquals(1, step.getStepNumber());
            assertEquals("Step Title", step.getTitle());
            assertEquals("Step Description", step.getDescription());
            assertEquals(0, step.getEstimatedTimeMinutes());
            assertTrue(step.getInstructions().isEmpty());
        }

        @Test
        @DisplayName("Should handle null title")
        void shouldHandleNullTitle() {
            assertThrows(NullPointerException.class, () ->
                    RestorationStep.builder()
                            .stepNumber(1)
                            .title(null)
                            .description("Description")
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null description")
        void shouldHandleNullDescription() {
            assertThrows(NullPointerException.class, () ->
                    RestorationStep.builder()
                            .stepNumber(1)
                            .title("Title")
                            .description(null)
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null instructions")
        void shouldHandleNullInstructions() {
            RestorationStep step = RestorationStep.builder()
                    .stepNumber(1)
                    .title("Title")
                    .description("Description")
                    .instructions(null)
                    .build();

            assertNotNull(step.getInstructions());
            assertTrue(step.getInstructions().isEmpty());
        }

        @Test
        @DisplayName("Should clamp negative estimated time to zero")
        void shouldClampNegativeEstimatedTimeToZero() {
            RestorationStep step = RestorationStep.builder()
                    .stepNumber(1)
                    .title("Title")
                    .description("Description")
                    .estimatedTimeMinutes(-5)
                    .build();

            assertEquals(0, step.getEstimatedTimeMinutes());
        }
    }

    @Nested
    @DisplayName("Instructions Tests")
    class InstructionsTests {

        @Test
        @DisplayName("Should create defensive copy of instructions")
        void shouldCreateDefensiveCopyOfInstructions() {
            List<String> originalInstructions = new ArrayList<>(Arrays.asList("Instruction 1", "Instruction 2"));

            RestorationStep step = RestorationStep.builder()
                    .stepNumber(1)
                    .title("Title")
                    .description("Description")
                    .instructions(originalInstructions)
                    .build();

            // Modify original instructions
            originalInstructions.add("New Instruction");

            // Step instructions should not be affected
            assertEquals(2, step.getInstructions().size());
            assertFalse(step.getInstructions().contains("New Instruction"));
        }

        @Test
        @DisplayName("Should return defensive copy from getter")
        void shouldReturnDefensiveCopyFromGetter() {
            List<String> instructions = Arrays.asList("Instruction 1", "Instruction 2");

            RestorationStep step = RestorationStep.builder()
                    .stepNumber(1)
                    .title("Title")
                    .description("Description")
                    .instructions(instructions)
                    .build();

            List<String> retrievedInstructions = step.getInstructions();
            retrievedInstructions.add("New Instruction");

            // Original step instructions should not be affected
            assertEquals(2, step.getInstructions().size());
            assertFalse(step.getInstructions().contains("New Instruction"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            RestorationStep step = RestorationStep.builder()
                    .stepNumber(0)
                    .title("")
                    .description("")
                    .build();

            assertEquals(0, step.getStepNumber());
            assertEquals("", step.getTitle());
            assertEquals("", step.getDescription());
        }

        @Test
        @DisplayName("Should handle negative step number")
        void shouldHandleNegativeStepNumber() {
            RestorationStep step = RestorationStep.builder()
                    .stepNumber(-1)
                    .title("Title")
                    .description("Description")
                    .build();

            assertEquals(-1, step.getStepNumber());
        }

        @Test
        @DisplayName("Should handle zero estimated time")
        void shouldHandleZeroEstimatedTime() {
            RestorationStep step = RestorationStep.builder()
                    .stepNumber(1)
                    .title("Title")
                    .description("Description")
                    .estimatedTimeMinutes(0)
                    .build();

            assertEquals(0, step.getEstimatedTimeMinutes());
        }

        @Test
        @DisplayName("Should handle large estimated time")
        void shouldHandleLargeEstimatedTime() {
            RestorationStep step = RestorationStep.builder()
                    .stepNumber(1)
                    .title("Title")
                    .description("Description")
                    .estimatedTimeMinutes(120) // 2 hours
                    .build();

            assertEquals(120, step.getEstimatedTimeMinutes());
        }
    }
}