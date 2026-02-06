package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a parsing warning encountered during AST construction.
 */
public final class ParseWarning {
    
    private final String message;
    private final SourceLocation location;
    private final WarningSeverity severity;
    private final String warningCode;
    private final String suggestion;
    
    @JsonCreator
    public ParseWarning(
            @JsonProperty("message") String message,
            @JsonProperty("location") SourceLocation location,
            @JsonProperty("severity") WarningSeverity severity,
            @JsonProperty("warningCode") String warningCode,
            @JsonProperty("suggestion") String suggestion) {
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.location = Objects.requireNonNull(location, "Location cannot be null");
        this.severity = severity != null ? severity : WarningSeverity.WARNING;
        this.warningCode = warningCode;
        this.suggestion = suggestion;
    }
    
    /**
     * Creates a simple parse warning.
     * 
     * @param message the warning message
     * @param location the warning location
     * @return new ParseWarning
     */
    public static ParseWarning of(String message, SourceLocation location) {
        return new ParseWarning(message, location, WarningSeverity.WARNING, null, null);
    }
    
    /**
     * Creates a parse warning with suggestion.
     * 
     * @param message the warning message
     * @param location the warning location
     * @param suggestion the suggested improvement
     * @return new ParseWarning
     */
    public static ParseWarning withSuggestion(String message, SourceLocation location, String suggestion) {
        return new ParseWarning(message, location, WarningSeverity.WARNING, null, suggestion);
    }
    
    /**
     * Gets the warning message.
     * 
     * @return the warning message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the warning location.
     * 
     * @return the source location
     */
    public SourceLocation getLocation() {
        return location;
    }
    
    /**
     * Gets the warning severity.
     * 
     * @return the severity level
     */
    public WarningSeverity getSeverity() {
        return severity;
    }
    
    /**
     * Gets the warning code.
     * 
     * @return the warning code, or null if not specified
     */
    public String getWarningCode() {
        return warningCode;
    }
    
    /**
     * Gets the suggested improvement.
     * 
     * @return the suggestion, or null if not provided
     */
    public String getSuggestion() {
        return suggestion;
    }
    
    /**
     * Checks if this warning has a suggestion.
     * 
     * @return true if a suggestion is provided
     */
    public boolean hasSuggestion() {
        return suggestion != null && !suggestion.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ParseWarning that = (ParseWarning) obj;
        return Objects.equals(message, that.message) &&
               Objects.equals(location, that.location) &&
               severity == that.severity &&
               Objects.equals(warningCode, that.warningCode) &&
               Objects.equals(suggestion, that.suggestion);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(message, location, severity, warningCode, suggestion);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(severity).append(" at ").append(location).append(": ").append(message);
        if (warningCode != null) {
            sb.append(" [").append(warningCode).append("]");
        }
        if (hasSuggestion()) {
            sb.append(" (Suggestion: ").append(suggestion).append(")");
        }
        return sb.toString();
    }
    
    /**
     * Warning severity levels.
     */
    public enum WarningSeverity {
        INFO,
        WARNING
    }
}