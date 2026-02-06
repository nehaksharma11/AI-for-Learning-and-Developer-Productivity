package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an interaction within a learning session.
 * Tracks user actions, responses, and engagement metrics.
 */
public class SessionInteraction {
    
    @NotBlank
    private final String id;
    
    @NotNull
    private final InteractionType type;
    
    @NotBlank
    private final String contentId;
    
    private final String userResponse;
    
    private final String correctAnswer;
    
    private final boolean correct;
    
    @NotNull
    private final LocalDateTime timestamp;
    
    private final int timeSpentSeconds;
    
    private final String feedback;

    @JsonCreator
    public SessionInteraction(
            @JsonProperty("id") String id,
            @JsonProperty("type") InteractionType type,
            @JsonProperty("contentId") String contentId,
            @JsonProperty("userResponse") String userResponse,
            @JsonProperty("correctAnswer") String correctAnswer,
            @JsonProperty("correct") boolean correct,
            @JsonProperty("timestamp") LocalDateTime timestamp,
            @JsonProperty("timeSpentSeconds") int timeSpentSeconds,
            @JsonProperty("feedback") String feedback) {
        this.id = Objects.requireNonNull(id, "Interaction ID cannot be null");
        this.type = Objects.requireNonNull(type, "Interaction type cannot be null");
        this.contentId = Objects.requireNonNull(contentId, "Content ID cannot be null");
        this.userResponse = userResponse;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.timeSpentSeconds = Math.max(0, timeSpentSeconds);
        this.feedback = feedback;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SessionInteraction view(String id, String contentId, int timeSpentSeconds) {
        return new SessionInteraction(id, InteractionType.VIEW, contentId, null, null, false,
                LocalDateTime.now(), timeSpentSeconds, null);
    }

    public static SessionInteraction quiz(String id, String contentId, String userResponse, String correctAnswer, boolean correct) {
        return new SessionInteraction(id, InteractionType.QUIZ_RESPONSE, contentId, userResponse, correctAnswer, correct,
                LocalDateTime.now(), 0, null);
    }

    public boolean isCorrectResponse() {
        return correct && userResponse != null;
    }

    public boolean hasUserResponse() {
        return userResponse != null && !userResponse.trim().isEmpty();
    }

    // Getters
    public String getId() { return id; }
    public InteractionType getType() { return type; }
    public String getContentId() { return contentId; }
    public String getUserResponse() { return userResponse; }
    public String getCorrectAnswer() { return correctAnswer; }
    public boolean isCorrect() { return correct; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public String getFeedback() { return feedback; }

    public enum InteractionType {
        VIEW,
        QUIZ_RESPONSE,
        EXERCISE_SUBMISSION,
        CODE_EXECUTION,
        FEEDBACK_PROVIDED,
        HELP_REQUESTED,
        BOOKMARK_ADDED,
        NOTE_TAKEN
    }

    public static class Builder {
        private String id;
        private InteractionType type;
        private String contentId;
        private String userResponse;
        private String correctAnswer;
        private boolean correct = false;
        private LocalDateTime timestamp = LocalDateTime.now();
        private int timeSpentSeconds = 0;
        private String feedback;

        public Builder id(String id) { this.id = id; return this; }
        public Builder type(InteractionType type) { this.type = type; return this; }
        public Builder contentId(String contentId) { this.contentId = contentId; return this; }
        public Builder userResponse(String userResponse) { this.userResponse = userResponse; return this; }
        public Builder correctAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; return this; }
        public Builder correct(boolean correct) { this.correct = correct; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder timeSpentSeconds(int timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; return this; }
        public Builder feedback(String feedback) { this.feedback = feedback; return this; }

        public SessionInteraction build() {
            return new SessionInteraction(id, type, contentId, userResponse, correctAnswer, correct,
                    timestamp, timeSpentSeconds, feedback);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInteraction that = (SessionInteraction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SessionInteraction{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", contentId='" + contentId + '\'' +
                ", correct=" + correct +
                ", timeSpentSeconds=" + timeSpentSeconds +
                '}';
    }
}