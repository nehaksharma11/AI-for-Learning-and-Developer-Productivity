package com.ailearning.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the result of a work state restoration operation.
 */
public class RestorationResult {
    
    private final boolean success;
    private final String message;
    private final WorkState workState;
    private final List<RestorationInstruction> instructions;
    private final String errorCode;

    private RestorationResult(boolean success, String message, WorkState workState, 
                            List<RestorationInstruction> instructions, String errorCode) {
        this.success = success;
        this.message = message;
        this.workState = workState;
        this.instructions = instructions != null ? new ArrayList<>(instructions) : new ArrayList<>();
        this.errorCode = errorCode;
    }

    public static RestorationResult success(WorkState workState, List<RestorationInstruction> instructions) {
        return new RestorationResult(true, "Work state restored successfully", workState, instructions, null);
    }

    public static RestorationResult failure(String message) {
        return failure(message, null);
    }

    public static RestorationResult failure(String message, String errorCode) {
        return new RestorationResult(false, message, null, null, errorCode);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public WorkState getWorkState() { return workState; }
    public List<RestorationInstruction> getInstructions() { return new ArrayList<>(instructions); }
    public String getErrorCode() { return errorCode; }

    @Override
    public String toString() {
        return "RestorationResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", instructionCount=" + instructions.size() +
                '}';
    }
}

/**
 * Represents an instruction for restoring work state.
 */
class RestorationInstruction {
    
    public enum InstructionType {
        OPEN_FILES,
        SET_ACTIVE_FILE,
        SET_CURSOR_POSITION,
        SELECT_TEXT,
        SHOW_CONTEXT,
        RESTORE_IDE_STATE,
        DISPLAY_NOTES
    }

    private final InstructionType type;
    private final String description;
    private final Map<String, Object> data;
    private final int priority;

    private RestorationInstruction(InstructionType type, String description, 
                                 Map<String, Object> data, int priority) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.data = data != null ? new java.util.HashMap<>(data) : new java.util.HashMap<>();
        this.priority = priority;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public InstructionType getType() { return type; }
    public String getDescription() { return description; }
    public Map<String, Object> getData() { return new java.util.HashMap<>(data); }
    public int getPriority() { return priority; }

    public static class Builder {
        private InstructionType type;
        private String description;
        private Map<String, Object> data = new java.util.HashMap<>();
        private int priority = 0;

        public Builder type(InstructionType type) { this.type = type; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder data(Map<String, Object> data) { this.data = data; return this; }
        public Builder priority(int priority) { this.priority = priority; return this; }

        public RestorationInstruction build() {
            return new RestorationInstruction(type, description, data, priority);
        }
    }

    @Override
    public String toString() {
        return "RestorationInstruction{" +
                "type=" + type +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                '}';
    }
}