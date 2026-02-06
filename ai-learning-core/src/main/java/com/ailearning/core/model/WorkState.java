package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents the complete work state of a developer at a specific point in time.
 * Used for context preservation and restoration across sessions and interruptions.
 */
public class WorkState {
    
    public enum StateType {
        CODING, DEBUGGING, TESTING, REVIEWING, LEARNING, RESEARCHING, MEETING
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String developerId;
    
    @NotBlank
    private final String projectId;
    
    @NotNull
    private final StateType currentActivity;
    
    @NotNull
    private final LocalDateTime capturedAt;
    
    // Current work context
    @NotNull
    private final List<String> openFiles;
    
    private final String activeFile;
    private final int cursorPosition;
    private final String selectedText;
    
    // IDE state
    @NotNull
    private final Map<String, Object> ideState;
    
    // Task and goal context
    private final String currentTask;
    private final String currentGoal;
    @NotNull
    private final List<String> recentActions;
    
    // Code context
    @NotNull
    private final List<CodeReference> relevantCodeReferences;
    @NotNull
    private final List<String> recentSearchQueries;
    
    // Learning context
    private final String activeLearningSession;
    @NotNull
    private final List<String> recentLearningTopics;
    
    // Productivity context
    private final String activeProductivitySession;
    @NotNull
    private final Map<String, Object> sessionMetrics;
    
    // Mental model and notes
    private final String mentalModel;
    @NotNull
    private final List<String> developerNotes;
    
    // Restoration hints
    @NotNull
    private final Map<String, Object> restorationHints;
    
    @NotNull
    private final LocalDateTime expiresAt;

    @JsonCreator
    public WorkState(
            @JsonProperty("id") String id,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("projectId") String projectId,
            @JsonProperty("currentActivity") StateType currentActivity,
            @JsonProperty("capturedAt") LocalDateTime capturedAt,
            @JsonProperty("openFiles") List<String> openFiles,
            @JsonProperty("activeFile") String activeFile,
            @JsonProperty("cursorPosition") int cursorPosition,
            @JsonProperty("selectedText") String selectedText,
            @JsonProperty("ideState") Map<String, Object> ideState,
            @JsonProperty("currentTask") String currentTask,
            @JsonProperty("currentGoal") String currentGoal,
            @JsonProperty("recentActions") List<String> recentActions,
            @JsonProperty("relevantCodeReferences") List<CodeReference> relevantCodeReferences,
            @JsonProperty("recentSearchQueries") List<String> recentSearchQueries,
            @JsonProperty("activeLearningSession") String activeLearningSession,
            @JsonProperty("recentLearningTopics") List<String> recentLearningTopics,
            @JsonProperty("activeProductivitySession") String activeProductivitySession,
            @JsonProperty("sessionMetrics") Map<String, Object> sessionMetrics,
            @JsonProperty("mentalModel") String mentalModel,
            @JsonProperty("developerNotes") List<String> developerNotes,
            @JsonProperty("restorationHints") Map<String, Object> restorationHints,
            @JsonProperty("expiresAt") LocalDateTime expiresAt) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.projectId = Objects.requireNonNull(projectId, "Project ID cannot be null");
        this.currentActivity = Objects.requireNonNull(currentActivity, "Current activity cannot be null");
        this.capturedAt = Objects.requireNonNull(capturedAt, "Captured at cannot be null");
        this.openFiles = openFiles != null ? new ArrayList<>(openFiles) : new ArrayList<>();
        this.activeFile = activeFile;
        this.cursorPosition = Math.max(0, cursorPosition);
        this.selectedText = selectedText;
        this.ideState = ideState != null ? new HashMap<>(ideState) : new HashMap<>();
        this.currentTask = currentTask;
        this.currentGoal = currentGoal;
        this.recentActions = recentActions != null ? new ArrayList<>(recentActions) : new ArrayList<>();
        this.relevantCodeReferences = relevantCodeReferences != null ? new ArrayList<>(relevantCodeReferences) : new ArrayList<>();
        this.recentSearchQueries = recentSearchQueries != null ? new ArrayList<>(recentSearchQueries) : new ArrayList<>();
        this.activeLearningSession = activeLearningSession;
        this.recentLearningTopics = recentLearningTopics != null ? new ArrayList<>(recentLearningTopics) : new ArrayList<>();
        this.activeProductivitySession = activeProductivitySession;
        this.sessionMetrics = sessionMetrics != null ? new HashMap<>(sessionMetrics) : new HashMap<>();
        this.mentalModel = mentalModel;
        this.developerNotes = developerNotes != null ? new ArrayList<>(developerNotes) : new ArrayList<>();
        this.restorationHints = restorationHints != null ? new HashMap<>(restorationHints) : new HashMap<>();
        this.expiresAt = expiresAt != null ? expiresAt : LocalDateTime.now().plusDays(7); // Default 7 days
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Checks if this work state is still valid (not expired).
     */
    public boolean isValid() {
        return LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Calculates the age of this work state in minutes.
     */
    public long getAgeMinutes() {
        return java.time.Duration.between(capturedAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * Gets the priority score for this work state (higher = more important to restore).
     */
    public double getPriorityScore() {
        double recencyScore = calculateRecencyScore();
        double contextRichnessScore = calculateContextRichnessScore();
        double activityImportanceScore = calculateActivityImportanceScore();
        
        return (recencyScore * 0.4) + (contextRichnessScore * 0.3) + (activityImportanceScore * 0.3);
    }

    /**
     * Creates a summary of this work state for quick reference.
     */
    public WorkStateSummary createSummary() {
        return WorkStateSummary.builder()
                .id(id)
                .developerId(developerId)
                .projectId(projectId)
                .currentActivity(currentActivity)
                .capturedAt(capturedAt)
                .activeFile(activeFile)
                .currentTask(currentTask)
                .openFileCount(openFiles.size())
                .ageMinutes(getAgeMinutes())
                .priorityScore(getPriorityScore())
                .isValid(isValid())
                .build();
    }

    private double calculateRecencyScore() {
        long ageMinutes = getAgeMinutes();
        if (ageMinutes <= 5) return 1.0;
        if (ageMinutes <= 30) return 0.8;
        if (ageMinutes <= 120) return 0.6;
        if (ageMinutes <= 480) return 0.4;
        if (ageMinutes <= 1440) return 0.2;
        return 0.1;
    }

    private double calculateContextRichnessScore() {
        double score = 0.0;
        
        // File context
        score += Math.min(0.3, openFiles.size() * 0.05);
        if (activeFile != null) score += 0.1;
        if (selectedText != null && !selectedText.trim().isEmpty()) score += 0.1;
        
        // Task context
        if (currentTask != null && !currentTask.trim().isEmpty()) score += 0.2;
        if (currentGoal != null && !currentGoal.trim().isEmpty()) score += 0.1;
        
        // Action history
        score += Math.min(0.2, recentActions.size() * 0.02);
        
        return Math.min(1.0, score);
    }

    private double calculateActivityImportanceScore() {
        switch (currentActivity) {
            case CODING: return 0.9;
            case DEBUGGING: return 0.8;
            case TESTING: return 0.7;
            case REVIEWING: return 0.6;
            case LEARNING: return 0.5;
            case RESEARCHING: return 0.4;
            case MEETING: return 0.3;
            default: return 0.5;
        }
    }

    // Getters
    public String getId() { return id; }
    public String getDeveloperId() { return developerId; }
    public String getProjectId() { return projectId; }
    public StateType getCurrentActivity() { return currentActivity; }
    public LocalDateTime getCapturedAt() { return capturedAt; }
    public List<String> getOpenFiles() { return new ArrayList<>(openFiles); }
    public String getActiveFile() { return activeFile; }
    public int getCursorPosition() { return cursorPosition; }
    public String getSelectedText() { return selectedText; }
    public Map<String, Object> getIdeState() { return new HashMap<>(ideState); }
    public String getCurrentTask() { return currentTask; }
    public String getCurrentGoal() { return currentGoal; }
    public List<String> getRecentActions() { return new ArrayList<>(recentActions); }
    public List<CodeReference> getRelevantCodeReferences() { return new ArrayList<>(relevantCodeReferences); }
    public List<String> getRecentSearchQueries() { return new ArrayList<>(recentSearchQueries); }
    public String getActiveLearningSession() { return activeLearningSession; }
    public List<String> getRecentLearningTopics() { return new ArrayList<>(recentLearningTopics); }
    public String getActiveProductivitySession() { return activeProductivitySession; }
    public Map<String, Object> getSessionMetrics() { return new HashMap<>(sessionMetrics); }
    public String getMentalModel() { return mentalModel; }
    public List<String> getDeveloperNotes() { return new ArrayList<>(developerNotes); }
    public Map<String, Object> getRestorationHints() { return new HashMap<>(restorationHints); }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public static class Builder {
        private String id;
        private String developerId;
        private String projectId;
        private StateType currentActivity = StateType.CODING;
        private LocalDateTime capturedAt = LocalDateTime.now();
        private List<String> openFiles = new ArrayList<>();
        private String activeFile;
        private int cursorPosition = 0;
        private String selectedText;
        private Map<String, Object> ideState = new HashMap<>();
        private String currentTask;
        private String currentGoal;
        private List<String> recentActions = new ArrayList<>();
        private List<CodeReference> relevantCodeReferences = new ArrayList<>();
        private List<String> recentSearchQueries = new ArrayList<>();
        private String activeLearningSession;
        private List<String> recentLearningTopics = new ArrayList<>();
        private String activeProductivitySession;
        private Map<String, Object> sessionMetrics = new HashMap<>();
        private String mentalModel;
        private List<String> developerNotes = new ArrayList<>();
        private Map<String, Object> restorationHints = new HashMap<>();
        private LocalDateTime expiresAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder projectId(String projectId) { this.projectId = projectId; return this; }
        public Builder currentActivity(StateType currentActivity) { this.currentActivity = currentActivity; return this; }
        public Builder capturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; return this; }
        public Builder openFiles(List<String> openFiles) { this.openFiles = openFiles; return this; }
        public Builder activeFile(String activeFile) { this.activeFile = activeFile; return this; }
        public Builder cursorPosition(int cursorPosition) { this.cursorPosition = cursorPosition; return this; }
        public Builder selectedText(String selectedText) { this.selectedText = selectedText; return this; }
        public Builder ideState(Map<String, Object> ideState) { this.ideState = ideState; return this; }
        public Builder currentTask(String currentTask) { this.currentTask = currentTask; return this; }
        public Builder currentGoal(String currentGoal) { this.currentGoal = currentGoal; return this; }
        public Builder recentActions(List<String> recentActions) { this.recentActions = recentActions; return this; }
        public Builder relevantCodeReferences(List<CodeReference> relevantCodeReferences) { this.relevantCodeReferences = relevantCodeReferences; return this; }
        public Builder recentSearchQueries(List<String> recentSearchQueries) { this.recentSearchQueries = recentSearchQueries; return this; }
        public Builder activeLearningSession(String activeLearningSession) { this.activeLearningSession = activeLearningSession; return this; }
        public Builder recentLearningTopics(List<String> recentLearningTopics) { this.recentLearningTopics = recentLearningTopics; return this; }
        public Builder activeProductivitySession(String activeProductivitySession) { this.activeProductivitySession = activeProductivitySession; return this; }
        public Builder sessionMetrics(Map<String, Object> sessionMetrics) { this.sessionMetrics = sessionMetrics; return this; }
        public Builder mentalModel(String mentalModel) { this.mentalModel = mentalModel; return this; }
        public Builder developerNotes(List<String> developerNotes) { this.developerNotes = developerNotes; return this; }
        public Builder restorationHints(Map<String, Object> restorationHints) { this.restorationHints = restorationHints; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }

        public WorkState build() {
            return new WorkState(id, developerId, projectId, currentActivity, capturedAt, openFiles,
                    activeFile, cursorPosition, selectedText, ideState, currentTask, currentGoal,
                    recentActions, relevantCodeReferences, recentSearchQueries, activeLearningSession,
                    recentLearningTopics, activeProductivitySession, sessionMetrics, mentalModel,
                    developerNotes, restorationHints, expiresAt);
        }
    }

    /**
     * Summary representation of a work state for quick reference.
     */
    public static class WorkStateSummary {
        private final String id;
        private final String developerId;
        private final String projectId;
        private final StateType currentActivity;
        private final LocalDateTime capturedAt;
        private final String activeFile;
        private final String currentTask;
        private final int openFileCount;
        private final long ageMinutes;
        private final double priorityScore;
        private final boolean isValid;

        private WorkStateSummary(String id, String developerId, String projectId, StateType currentActivity,
                               LocalDateTime capturedAt, String activeFile, String currentTask,
                               int openFileCount, long ageMinutes, double priorityScore, boolean isValid) {
            this.id = id;
            this.developerId = developerId;
            this.projectId = projectId;
            this.currentActivity = currentActivity;
            this.capturedAt = capturedAt;
            this.activeFile = activeFile;
            this.currentTask = currentTask;
            this.openFileCount = openFileCount;
            this.ageMinutes = ageMinutes;
            this.priorityScore = priorityScore;
            this.isValid = isValid;
        }

        public static Builder builder() { return new Builder(); }

        // Getters
        public String getId() { return id; }
        public String getDeveloperId() { return developerId; }
        public String getProjectId() { return projectId; }
        public StateType getCurrentActivity() { return currentActivity; }
        public LocalDateTime getCapturedAt() { return capturedAt; }
        public String getActiveFile() { return activeFile; }
        public String getCurrentTask() { return currentTask; }
        public int getOpenFileCount() { return openFileCount; }
        public long getAgeMinutes() { return ageMinutes; }
        public double getPriorityScore() { return priorityScore; }
        public boolean isValid() { return isValid; }

        public static class Builder {
            private String id;
            private String developerId;
            private String projectId;
            private StateType currentActivity;
            private LocalDateTime capturedAt;
            private String activeFile;
            private String currentTask;
            private int openFileCount;
            private long ageMinutes;
            private double priorityScore;
            private boolean isValid;

            public Builder id(String id) { this.id = id; return this; }
            public Builder developerId(String developerId) { this.developerId = developerId; return this; }
            public Builder projectId(String projectId) { this.projectId = projectId; return this; }
            public Builder currentActivity(StateType currentActivity) { this.currentActivity = currentActivity; return this; }
            public Builder capturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; return this; }
            public Builder activeFile(String activeFile) { this.activeFile = activeFile; return this; }
            public Builder currentTask(String currentTask) { this.currentTask = currentTask; return this; }
            public Builder openFileCount(int openFileCount) { this.openFileCount = openFileCount; return this; }
            public Builder ageMinutes(long ageMinutes) { this.ageMinutes = ageMinutes; return this; }
            public Builder priorityScore(double priorityScore) { this.priorityScore = priorityScore; return this; }
            public Builder isValid(boolean isValid) { this.isValid = isValid; return this; }

            public WorkStateSummary build() {
                return new WorkStateSummary(id, developerId, projectId, currentActivity, capturedAt,
                        activeFile, currentTask, openFileCount, ageMinutes, priorityScore, isValid);
            }
        }

        @Override
        public String toString() {
            return "WorkStateSummary{" +
                    "id='" + id + '\'' +
                    ", activity=" + currentActivity +
                    ", task='" + currentTask + '\'' +
                    ", activeFile='" + activeFile + '\'' +
                    ", age=" + ageMinutes + "min" +
                    ", priority=" + String.format("%.2f", priorityScore) +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkState workState = (WorkState) o;
        return Objects.equals(id, workState.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WorkState{" +
                "id='" + id + '\'' +
                ", developerId='" + developerId + '\'' +
                ", projectId='" + projectId + '\'' +
                ", currentActivity=" + currentActivity +
                ", activeFile='" + activeFile + '\'' +
                ", age=" + getAgeMinutes() + "min" +
                ", priority=" + String.format("%.2f", getPriorityScore()) +
                '}';
    }
}