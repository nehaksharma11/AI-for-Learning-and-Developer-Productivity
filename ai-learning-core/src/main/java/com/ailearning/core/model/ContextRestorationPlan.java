package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A plan for restoring developer context after an interruption or context switch.
 */
public class ContextRestorationPlan {
    
    private final String developerId;
    private final String workStateId;
    private final WorkState workState;
    private final List<RestorationStep> steps;
    private final int totalEstimatedTimeMinutes;
    private final LocalDateTime createdAt;

    private ContextRestorationPlan(String developerId, String workStateId, WorkState workState,
                                 List<RestorationStep> steps, int totalEstimatedTimeMinutes, 
                                 LocalDateTime createdAt) {
        this.developerId = developerId;
        this.workStateId = workStateId;
        this.workState = workState;
        this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
        this.totalEstimatedTimeMinutes = Math.max(0, totalEstimatedTimeMinutes);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ContextRestorationPlan empty() {
        return builder()
                .developerId("")
                .workStateId("")
                .workState(null)
                .steps(new ArrayList<>())
                .totalEstimatedTimeMinutes(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Getters
    public String getDeveloperId() { return developerId; }
    public String getWorkStateId() { return workStateId; }
    public WorkState getWorkState() { return workState; }
    public List<RestorationStep> getSteps() { return new ArrayList<>(steps); }
    public int getTotalEstimatedTimeMinutes() { return totalEstimatedTimeMinutes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class Builder {
        private String developerId;
        private String workStateId;
        private WorkState workState;
        private List<RestorationStep> steps = new ArrayList<>();
        private int totalEstimatedTimeMinutes = 0;
        private LocalDateTime createdAt;

        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder workStateId(String workStateId) { this.workStateId = workStateId; return this; }
        public Builder workState(WorkState workState) { this.workState = workState; return this; }
        public Builder steps(List<RestorationStep> steps) { this.steps = steps; return this; }
        public Builder totalEstimatedTimeMinutes(int totalEstimatedTimeMinutes) { this.totalEstimatedTimeMinutes = totalEstimatedTimeMinutes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ContextRestorationPlan build() {
            return new ContextRestorationPlan(developerId, workStateId, workState, steps, 
                    totalEstimatedTimeMinutes, createdAt);
        }
    }
}

/**
 * A single step in a context restoration plan.
 */
class RestorationStep {
    
    private final int stepNumber;
    private final String title;
    private final String description;
    private final int estimatedTimeMinutes;
    private final List<String> instructions;

    private RestorationStep(int stepNumber, String title, String description, 
                          int estimatedTimeMinutes, List<String> instructions) {
        this.stepNumber = stepNumber;
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.estimatedTimeMinutes = Math.max(0, estimatedTimeMinutes);
        this.instructions = instructions != null ? new ArrayList<>(instructions) : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public int getStepNumber() { return stepNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public List<String> getInstructions() { return new ArrayList<>(instructions); }

    public static class Builder {
        private int stepNumber;
        private String title;
        private String description;
        private int estimatedTimeMinutes = 0;
        private List<String> instructions = new ArrayList<>();

        public Builder stepNumber(int stepNumber) { this.stepNumber = stepNumber; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder estimatedTimeMinutes(int estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; return this; }
        public Builder instructions(List<String> instructions) { this.instructions = instructions; return this; }

        public RestorationStep build() {
            return new RestorationStep(stepNumber, title, description, estimatedTimeMinutes, instructions);
        }
    }
}