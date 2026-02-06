package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a development session with tracked activities and metrics.
 * Used to collect real-time productivity data during development work.
 */
public class DevelopmentSession {
    
    public enum SessionState {
        ACTIVE, PAUSED, COMPLETED, INTERRUPTED
    }
    
    public enum ActivityType {
        CODING, DEBUGGING, TESTING, REVIEWING, LEARNING, MEETING, BREAK
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String developerId;
    
    @NotBlank
    private final String projectId;
    
    @NotNull
    private final LocalDateTime startTime;
    
    private final LocalDateTime endTime;
    
    @NotNull
    private final SessionState state;
    
    @NotNull
    private final List<SessionActivity> activities;
    
    @NotNull
    private final Map<String, Object> sessionData;
    
    // Real-time metrics
    private final int totalKeystrokes;
    private final int totalMouseClicks;
    private final int filesModified;
    private final int linesAdded;
    private final int linesDeleted;
    private final int contextSwitches;
    
    // Focus and productivity indicators
    private final double focusScore;
    private final int interruptionCount;
    private final long idleTimeMinutes;
    
    @NotNull
    private final LocalDateTime lastUpdated;

    @JsonCreator
    public DevelopmentSession(
            @JsonProperty("id") String id,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("projectId") String projectId,
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("endTime") LocalDateTime endTime,
            @JsonProperty("state") SessionState state,
            @JsonProperty("activities") List<SessionActivity> activities,
            @JsonProperty("sessionData") Map<String, Object> sessionData,
            @JsonProperty("totalKeystrokes") int totalKeystrokes,
            @JsonProperty("totalMouseClicks") int totalMouseClicks,
            @JsonProperty("filesModified") int filesModified,
            @JsonProperty("linesAdded") int linesAdded,
            @JsonProperty("linesDeleted") int linesDeleted,
            @JsonProperty("contextSwitches") int contextSwitches,
            @JsonProperty("focusScore") double focusScore,
            @JsonProperty("interruptionCount") int interruptionCount,
            @JsonProperty("idleTimeMinutes") long idleTimeMinutes,
            @JsonProperty("lastUpdated") LocalDateTime lastUpdated) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.projectId = Objects.requireNonNull(projectId, "Project ID cannot be null");
        this.startTime = Objects.requireNonNull(startTime, "Start time cannot be null");
        this.endTime = endTime;
        this.state = Objects.requireNonNull(state, "State cannot be null");
        this.activities = activities != null ? new ArrayList<>(activities) : new ArrayList<>();
        this.sessionData = sessionData != null ? new HashMap<>(sessionData) : new HashMap<>();
        this.totalKeystrokes = Math.max(0, totalKeystrokes);
        this.totalMouseClicks = Math.max(0, totalMouseClicks);
        this.filesModified = Math.max(0, filesModified);
        this.linesAdded = Math.max(0, linesAdded);
        this.linesDeleted = Math.max(0, linesDeleted);
        this.contextSwitches = Math.max(0, contextSwitches);
        this.focusScore = Math.max(0.0, Math.min(1.0, focusScore));
        this.interruptionCount = Math.max(0, interruptionCount);
        this.idleTimeMinutes = Math.max(0, idleTimeMinutes);
        this.lastUpdated = lastUpdated != null ? lastUpdated : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Calculates the total duration of the session in minutes.
     */
    public long getDurationMinutes() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).toMinutes();
    }

    /**
     * Calculates active coding time (excluding breaks and idle time).
     */
    public long getActiveCodingMinutes() {
        return getDurationMinutes() - idleTimeMinutes - getBreakTimeMinutes();
    }

    /**
     * Calculates total break time from activities.
     */
    public long getBreakTimeMinutes() {
        return activities.stream()
                .filter(activity -> activity.getType() == ActivityType.BREAK)
                .mapToLong(SessionActivity::getDurationMinutes)
                .sum();
    }

    /**
     * Gets the current activity if session is active.
     */
    public SessionActivity getCurrentActivity() {
        if (state != SessionState.ACTIVE || activities.isEmpty()) {
            return null;
        }
        return activities.get(activities.size() - 1);
    }

    /**
     * Calculates productivity score based on session metrics.
     */
    public double getProductivityScore() {
        if (getDurationMinutes() == 0) return 0.0;
        
        double activityScore = calculateActivityScore();
        double outputScore = calculateOutputScore();
        double focusWeight = focusScore * 0.3;
        
        return (activityScore * 0.4) + (outputScore * 0.3) + focusWeight;
    }

    private double calculateActivityScore() {
        long activeCoding = getActiveCodingMinutes();
        long totalDuration = getDurationMinutes();
        
        if (totalDuration == 0) return 0.0;
        return Math.min(1.0, (double) activeCoding / totalDuration);
    }

    private double calculateOutputScore() {
        int netLines = linesAdded - linesDeleted;
        double lineScore = Math.min(1.0, netLines / 100.0); // 100 lines as baseline
        double fileScore = Math.min(1.0, filesModified / 5.0); // 5 files as baseline
        
        return (lineScore + fileScore) / 2.0;
    }

    // Getters
    public String getId() { return id; }
    public String getDeveloperId() { return developerId; }
    public String getProjectId() { return projectId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public SessionState getState() { return state; }
    public List<SessionActivity> getActivities() { return new ArrayList<>(activities); }
    public Map<String, Object> getSessionData() { return new HashMap<>(sessionData); }
    public int getTotalKeystrokes() { return totalKeystrokes; }
    public int getTotalMouseClicks() { return totalMouseClicks; }
    public int getFilesModified() { return filesModified; }
    public int getLinesAdded() { return linesAdded; }
    public int getLinesDeleted() { return linesDeleted; }
    public int getContextSwitches() { return contextSwitches; }
    public double getFocusScore() { return focusScore; }
    public int getInterruptionCount() { return interruptionCount; }
    public long getIdleTimeMinutes() { return idleTimeMinutes; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    /**
     * Represents an activity within a development session.
     */
    public static class SessionActivity {
        @NotNull
        private final ActivityType type;
        
        @NotNull
        private final LocalDateTime startTime;
        
        private final LocalDateTime endTime;
        
        private final String description;
        
        @NotNull
        private final Map<String, Object> metadata;

        @JsonCreator
        public SessionActivity(
                @JsonProperty("type") ActivityType type,
                @JsonProperty("startTime") LocalDateTime startTime,
                @JsonProperty("endTime") LocalDateTime endTime,
                @JsonProperty("description") String description,
                @JsonProperty("metadata") Map<String, Object> metadata) {
            this.type = Objects.requireNonNull(type, "Activity type cannot be null");
            this.startTime = Objects.requireNonNull(startTime, "Start time cannot be null");
            this.endTime = endTime;
            this.description = description;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        }

        public long getDurationMinutes() {
            LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
            return java.time.Duration.between(startTime, end).toMinutes();
        }

        // Getters
        public ActivityType getType() { return type; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public String getDescription() { return description; }
        public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    }

    public static class Builder {
        private String id;
        private String developerId;
        private String projectId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private SessionState state = SessionState.ACTIVE;
        private List<SessionActivity> activities = new ArrayList<>();
        private Map<String, Object> sessionData = new HashMap<>();
        private int totalKeystrokes = 0;
        private int totalMouseClicks = 0;
        private int filesModified = 0;
        private int linesAdded = 0;
        private int linesDeleted = 0;
        private int contextSwitches = 0;
        private double focusScore = 0.0;
        private int interruptionCount = 0;
        private long idleTimeMinutes = 0;
        private LocalDateTime lastUpdated = LocalDateTime.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder developerId(String developerId) { this.developerId = developerId; return this; }
        public Builder projectId(String projectId) { this.projectId = projectId; return this; }
        public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public Builder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public Builder state(SessionState state) { this.state = state; return this; }
        public Builder activities(List<SessionActivity> activities) { this.activities = activities; return this; }
        public Builder sessionData(Map<String, Object> sessionData) { this.sessionData = sessionData; return this; }
        public Builder totalKeystrokes(int totalKeystrokes) { this.totalKeystrokes = totalKeystrokes; return this; }
        public Builder totalMouseClicks(int totalMouseClicks) { this.totalMouseClicks = totalMouseClicks; return this; }
        public Builder filesModified(int filesModified) { this.filesModified = filesModified; return this; }
        public Builder linesAdded(int linesAdded) { this.linesAdded = linesAdded; return this; }
        public Builder linesDeleted(int linesDeleted) { this.linesDeleted = linesDeleted; return this; }
        public Builder contextSwitches(int contextSwitches) { this.contextSwitches = contextSwitches; return this; }
        public Builder focusScore(double focusScore) { this.focusScore = focusScore; return this; }
        public Builder interruptionCount(int interruptionCount) { this.interruptionCount = interruptionCount; return this; }
        public Builder idleTimeMinutes(long idleTimeMinutes) { this.idleTimeMinutes = idleTimeMinutes; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }

        public DevelopmentSession build() {
            return new DevelopmentSession(id, developerId, projectId, startTime, endTime, state,
                    activities, sessionData, totalKeystrokes, totalMouseClicks, filesModified,
                    linesAdded, linesDeleted, contextSwitches, focusScore, interruptionCount,
                    idleTimeMinutes, lastUpdated);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevelopmentSession that = (DevelopmentSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DevelopmentSession{" +
                "id='" + id + '\'' +
                ", developerId='" + developerId + '\'' +
                ", projectId='" + projectId + '\'' +
                ", state=" + state +
                ", duration=" + getDurationMinutes() + "min" +
                ", productivity=" + String.format("%.2f", getProductivityScore()) +
                '}';
    }
}