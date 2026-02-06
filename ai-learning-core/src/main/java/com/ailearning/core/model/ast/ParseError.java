package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a parsing error encountered during AST construction.
 */
public final class ParseError {
    
    private final String message;
    private final SourceLocation location;
    private final ErrorSeverity severity;
    private final String errorCode;
    private final String suggestion;
    
    @JsonCreator
    public ParseError(
            @JsonProperty("message") String message,
            @JsonProperty("location") SourceLocation location,
            @JsonProperty("severity") ErrorSeverity severity,
            @JsonProperty("errorCode") String errorCode,
            @JsonProperty("suggestion") String suggestion) {
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.location = Objects.requireNonNull(location, "Location cannot be null");
        this.severity = severity != null ? severity : ErrorSeverity.ERROR;
        this.errorCode = errorCode;
        this.suggestion = suggestion;
    }
    
    /**
     * Creates a simple parse error.
     * 
     * @param message the error message
     * @param location the error location
     * @return new ParseError
     */
    public static ParseError of(String message, SourceLocation location) {
        return new ParseError(message, location, ErrorSeverity.ERROR, null, null);
    }
    
    /**
     * Creates a parse error with suggestion.
     * 
     * @param message the error message
     * @param location the error location
     * @param suggestion the suggested fix
     * @return new ParseError
     */
    public static ParseError withSuggestion(String message, SourceLocation location, String suggestion) {
        return new ParseError(message, location, ErrorSeverity.ERROR, null, suggestion);
    }
    
    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the error location.
     * 
     * @return the source location
     */
    public SourceLocation getLocation() {
        return location;
    }
    
    /**
     * Gets the error severity.
     * 
     * @return the severity level
     */
    public ErrorSeverity getSeverity() {
        return severity;
    }
    
    /**
     * Gets the error code.
     * 
     * @return the error code, or null if not specified
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the suggested fix.
     * 
     * @return the suggestion, or null if not provided
     */
    public String getSuggestion() {
        return suggestion;
    }
    
    /**
     * Checks if this error has a suggestion.
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
        
        ParseError that = (ParseError) obj;
        return Objects.equals(message, that.message) &&
               Objects.equals(location, that.location) &&
               severity == that.severity &&
               Objects.equals(errorCode, that.errorCode) &&
               Objects.equals(suggestion, that.suggestion);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(message, location, severity, errorCode, suggestion);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(severity).append(" at ").append(location).append(": ").append(message);
        if (errorCode != null) {
            sb.append(" [").append(errorCode).append("]");
        }
        if (hasSuggestion()) {
            sb.append(" (Suggestion: ").append(suggestion).append(")");
        }
        return sb.toString();
    }
    
    /**
     * Error severity levels.
     */
    public enum ErrorSeverity {
        ERROR,
        FATAL
    }
}