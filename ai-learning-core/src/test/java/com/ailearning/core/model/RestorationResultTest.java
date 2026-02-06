package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestorationResult Tests")
class RestorationResultTest {

    @Nested
    @DisplayName("Success Result Tests")
    class SuccessResultTests {

        @Test
        @DisplayName("Should create successful restoration result")
        void shouldCreateSuccessfulRestorationResult() {
            WorkState workState = WorkState.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .build();

            List<RestorationInstruction> instructions = Arrays.asList(
                    RestorationInstruction.builder()
                            .type(RestorationInstruction.InstructionType.OPEN_FILES)
                            .description("Open required files")
                            .priority(1)
                            .build(),
                    RestorationInstruction.builder()
                            .type(RestorationInstruction.InstructionType.SET_ACTIVE_FILE)
                            .description("Set active file")
                            .priority(2)
                            .build()
            );

            RestorationResult result = RestorationResult.success(workState, instructions);

            assertTrue(result.isSuccess());
            assertEquals("Work state restored successfully", result.getMessage());
            assertEquals(workState, result.getWorkState());
            assertEquals(instructions, result.getInstructions());
            assertNull(result.getErrorCode());
        }

        @Test
        @DisplayName("Should handle null work state in success")
        void shouldHandleNullWorkStateInSuccess() {
            List<RestorationInstruction> instructions = Arrays.asList(
                    RestorationInstruction.builder()
                            .type(RestorationInstruction.InstructionType.OPEN_FILES)
                            .description("Open required files")
                            .build()
            );

            RestorationResult result = RestorationResult.success(null, instructions);

            assertTrue(result.isSuccess());
            assertNull(result.getWorkState());
            assertEquals(instructions, result.getInstructions());
        }

        @Test
        @DisplayName("Should handle null instructions in success")
        void shouldHandleNullInstructionsInSuccess() {
            WorkState workState = WorkState.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .build();

            RestorationResult result = RestorationResult.success(workState, null);

            assertTrue(result.isSuccess());
            assertEquals(workState, result.getWorkState());
            assertNotNull(result.getInstructions());
            assertTrue(result.getInstructions().isEmpty());
        }
    }

    @Nested
    @DisplayName("Failure Result Tests")
    class FailureResultTests {

        @Test
        @DisplayName("Should create failure result with message only")
        void shouldCreateFailureResultWithMessageOnly() {
            RestorationResult result = RestorationResult.failure("Restoration failed");

            assertFalse(result.isSuccess());
            assertEquals("Restoration failed", result.getMessage());
            assertNull(result.getWorkState());
            assertTrue(result.getInstructions().isEmpty());
            assertNull(result.getErrorCode());
        }

        @Test
        @DisplayName("Should create failure result with message and error code")
        void shouldCreateFailureResultWithMessageAndErrorCode() {
            RestorationResult result = RestorationResult.failure("Restoration failed", "ERR_001");

            assertFalse(result.isSuccess());
            assertEquals("Restoration failed", result.getMessage());
            assertNull(result.getWorkState());
            assertTrue(result.getInstructions().isEmpty());
            assertEquals("ERR_001", result.getErrorCode());
        }

        @Test
        @DisplayName("Should handle null error code in failure")
        void shouldHandleNullErrorCodeInFailure() {
            RestorationResult result = RestorationResult.failure("Restoration failed", null);

            assertFalse(result.isSuccess());
            assertEquals("Restoration failed", result.getMessage());
            assertNull(result.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Instructions Tests")
    class InstructionsTests {

        @Test
        @DisplayName("Should create defensive copy of instructions")
        void shouldCreateDefensiveCopyOfInstructions() {
            WorkState workState = WorkState.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .build();

            List<RestorationInstruction> originalInstructions = new ArrayList<>(Arrays.asList(
                    RestorationInstruction.builder()
                            .type(RestorationInstruction.InstructionType.OPEN_FILES)
                            .description("Open files")
                            .build()
            ));

            RestorationResult result = RestorationResult.success(workState, originalInstructions);

            // Modify original instructions
            originalInstructions.add(RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.SET_ACTIVE_FILE)
                    .description("Set active file")
                    .build());

            // Result instructions should not be affected
            assertEquals(1, result.getInstructions().size());
        }

        @Test
        @DisplayName("Should return defensive copy from getter")
        void shouldReturnDefensiveCopyFromGetter() {
            WorkState workState = WorkState.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .build();

            List<RestorationInstruction> instructions = Arrays.asList(
                    RestorationInstruction.builder()
                            .type(RestorationInstruction.InstructionType.OPEN_FILES)
                            .description("Open files")
                            .build()
            );

            RestorationResult result = RestorationResult.success(workState, instructions);

            List<RestorationInstruction> retrievedInstructions = result.getInstructions();
            retrievedInstructions.add(RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.SET_ACTIVE_FILE)
                    .description("Set active file")
                    .build());

            // Original result instructions should not be affected
            assertEquals(1, result.getInstructions().size());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have meaningful toString for success")
        void shouldHaveMeaningfulToStringForSuccess() {
            WorkState workState = WorkState.builder()
                    .developerId("dev123")
                    .workStateId("ws123")
                    .build();

            List<RestorationInstruction> instructions = Arrays.asList(
                    RestorationInstruction.builder()
                            .type(RestorationInstruction.InstructionType.OPEN_FILES)
                            .description("Open files")
                            .build()
            );

            RestorationResult result = RestorationResult.success(workState, instructions);
            String toString = result.toString();

            assertTrue(toString.contains("success=true"));
            assertTrue(toString.contains("Work state restored successfully"));
            assertTrue(toString.contains("instructionCount=1"));
        }

        @Test
        @DisplayName("Should have meaningful toString for failure")
        void shouldHaveMeaningfulToStringForFailure() {
            RestorationResult result = RestorationResult.failure("Restoration failed");
            String toString = result.toString();

            assertTrue(toString.contains("success=false"));
            assertTrue(toString.contains("Restoration failed"));
            assertTrue(toString.contains("instructionCount=0"));
        }
    }
}

@DisplayName("RestorationInstruction Tests")
class RestorationInstructionTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create restoration instruction with all fields")
        void shouldCreateRestorationInstructionWithAllFields() {
            Map<String, Object> data = new HashMap<>();
            data.put("filePath", "/path/to/file.java");
            data.put("lineNumber", 42);

            RestorationInstruction instruction = RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("Open the main source file")
                    .data(data)
                    .priority(1)
                    .build();

            assertEquals(RestorationInstruction.InstructionType.OPEN_FILES, instruction.getType());
            assertEquals("Open the main source file", instruction.getDescription());
            assertEquals(data, instruction.getData());
            assertEquals(1, instruction.getPriority());
        }

        @Test
        @DisplayName("Should create restoration instruction with minimal fields")
        void shouldCreateRestorationInstructionWithMinimalFields() {
            RestorationInstruction instruction = RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("Open files")
                    .build();

            assertEquals(RestorationInstruction.InstructionType.OPEN_FILES, instruction.getType());
            assertEquals("Open files", instruction.getDescription());
            assertTrue(instruction.getData().isEmpty());
            assertEquals(0, instruction.getPriority());
        }

        @Test
        @DisplayName("Should handle null type")
        void shouldHandleNullType() {
            assertThrows(NullPointerException.class, () ->
                    RestorationInstruction.builder()
                            .type(null)
                            .description("Description")
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null description")
        void shouldHandleNullDescription() {
            assertThrows(NullPointerException.class, () ->
                    RestorationInstruction.builder()
                            .type(RestorationInstruction.InstructionType.OPEN_FILES)
                            .description(null)
                            .build()
            );
        }

        @Test
        @DisplayName("Should handle null data")
        void shouldHandleNullData() {
            RestorationInstruction instruction = RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("Open files")
                    .data(null)
                    .build();

            assertNotNull(instruction.getData());
            assertTrue(instruction.getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("Should have all expected instruction types")
        void shouldHaveAllExpectedInstructionTypes() {
            RestorationInstruction.InstructionType[] types = RestorationInstruction.InstructionType.values();
            
            assertEquals(7, types.length);
            assertTrue(java.util.Arrays.asList(types).contains(RestorationInstruction.InstructionType.OPEN_FILES));
            assertTrue(java.util.Arrays.asList(types).contains(RestorationInstruction.InstructionType.SET_ACTIVE_FILE));
            assertTrue(java.util.Arrays.asList(types).contains(RestorationInstruction.InstructionType.SET_CURSOR_POSITION));
            assertTrue(java.util.Arrays.asList(types).contains(RestorationInstruction.InstructionType.SELECT_TEXT));
            assertTrue(java.util.Arrays.asList(types).contains(RestorationInstruction.InstructionType.SHOW_CONTEXT));
            assertTrue(java.util.Arrays.asList(types).contains(RestorationInstruction.InstructionType.RESTORE_IDE_STATE));
            assertTrue(java.util.Arrays.asList(types).contains(RestorationInstruction.InstructionType.DISPLAY_NOTES));
        }
    }

    @Nested
    @DisplayName("Data Tests")
    class DataTests {

        @Test
        @DisplayName("Should create defensive copy of data")
        void shouldCreateDefensiveCopyOfData() {
            Map<String, Object> originalData = new HashMap<>();
            originalData.put("key", "value");

            RestorationInstruction instruction = RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("Open files")
                    .data(originalData)
                    .build();

            // Modify original data
            originalData.put("newKey", "newValue");

            // Instruction data should not be affected
            assertFalse(instruction.getData().containsKey("newKey"));
            assertEquals(1, instruction.getData().size());
        }

        @Test
        @DisplayName("Should return defensive copy from getter")
        void shouldReturnDefensiveCopyFromGetter() {
            Map<String, Object> data = new HashMap<>();
            data.put("key", "value");

            RestorationInstruction instruction = RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("Open files")
                    .data(data)
                    .build();

            Map<String, Object> retrievedData = instruction.getData();
            retrievedData.put("newKey", "newValue");

            // Original instruction data should not be affected
            assertFalse(instruction.getData().containsKey("newKey"));
            assertEquals(1, instruction.getData().size());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            RestorationInstruction instruction = RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("Open the main source file")
                    .priority(1)
                    .build();

            String toString = instruction.toString();

            assertTrue(toString.contains("type=OPEN_FILES"));
            assertTrue(toString.contains("Open the main source file"));
            assertTrue(toString.contains("priority=1"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty description")
        void shouldHandleEmptyDescription() {
            RestorationInstruction instruction = RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("")
                    .build();

            assertEquals("", instruction.getDescription());
        }

        @Test
        @DisplayName("Should handle negative priority")
        void shouldHandleNegativePriority() {
            RestorationInstruction instruction = RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("Open files")
                    .priority(-1)
                    .build();

            assertEquals(-1, instruction.getPriority());
        }

        @Test
        @DisplayName("Should handle large priority")
        void shouldHandleLargePriority() {
            RestorationInstruction instruction = RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("Open files")
                    .priority(1000)
                    .build();

            assertEquals(1000, instruction.getPriority());
        }
    }
}