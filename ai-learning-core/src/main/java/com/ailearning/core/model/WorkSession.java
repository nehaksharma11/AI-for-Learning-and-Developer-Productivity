package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a developer's work session with productivity metrics.
 */
public class WorkSession {
    
    @NotBlank
    private final String sessionId;
    
    @NotBlank
    private final String developerId;
    
    @NotNull
    private final LocalDateTime startTime;
    
    @NotNull
    private final LocalDateTime endTime;
    
    @PositiveOrZero
    private final int linesOfCodeWritten;
    
    @PositiveOrZero
    private final int filesModified;
    
    @PositiveOrZero
    private final int bugsFixed;
    
    @PositiveOrZero
    private final int testsWritten;

    @JsonCreator
    public WorkSession(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("developerId") String developerId,
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("endTime") LocalDateTime endTime,
            @JsonProperty("linesOfCodeWritten") int linesOfCodeWritten,
            @JsonProperty("filesModified") int filesModified,
            @JsonProperty("bugsFixed") int bugsFixed,
            @JsonProperty("testsWritten") int testsWritten) {
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
        this.developerId = Objects.requireNonNull(developerId, "Developer ID cannot be null");
        this.startTime = Objects.requireNonNull(startTime, "Start time cannot be null");
        this.endTime = Objects.requireNonNull(endTime, "End time cannot be null");
        
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        
        this.linesOfCodeWritten = Math.max(0, linesOfCodeWritten);
        this.filesModified = Math.max(0, filesModified);
        this.bugsFixed = Math.max(0, bugsFixed);
        this.testsWritten = Math.max(0, testsWritten);
    }

    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    public double getProductivityScore() {
        long minutes = getDuration().toMinutes();
        if (minutes == 0) return 0.0;
        
        // Calculate productivity based on various metrics
        double codeProductivity = linesOfCodeWritten / (double) minutes;
        double qualityScore = (bugsFixed + testsWritten) / (double) minutes;
        double fileEfficiency = filesModified / (double) minutes;
        
        return (codeProductivity * 0.4) + (qualityScore * 0.4) + (fileEfficiency * 0.2);
    }

    // Getters
    public String getSessionId() { return sessionId; }
    public String getDeveloperId() { return developerId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getLinesOfCodeWritten() { return linesOfCodeWritten; }
    public int getFilesModified() { return filesModified; }
    public int getBugsFixed() { return bugsFixed; }
    public int getTestsWritten() { return testsWritten; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkSession that = (WorkSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "WorkSession{" +
                "sessionId='" + sessionId + '\'' +
                ", duration=" + getDuration().toMinutes() + "min" +
                ", linesOfCode=" + linesOfCodeWritten +
                ", filesModified=" + filesModified +
                ", productivityScore=" + String.format("%.2f", getProductivityScore()) +
                '}';
    }
}