package com.ailearning.core.model;

import com.ailearning.core.service.impl.DocumentationChangeDetector.CodeElement;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a change event that affects documentation.
 */
public final class DocumentationChangeEvent {
    
    public enum ChangeType {
        ELEMENT_ADDED, ELEMENT_DELETED, ELEMENT_MODIFIED, SIGNATURE_CHANGED, CONTENT_CHANGED
    }
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    @NotBlank
    private final String id;
    
    @NotNull
    private final ChangeType changeType;
    
    @NotNull
    private final Severity severity;
    
    @NotBlank
    private final String filePath;
    
    private final CodeElement oldElement;
    private final CodeElement newElement;
    private final String description;
    private final Instant timestamp;
    private final String reason;

    @JsonCreator
    public DocumentationChangeEvent(
            @JsonProperty("id") String id,
            @JsonProperty("changeType") ChangeType changeType,
            @JsonProperty("severity") Severity severity,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("oldElement") CodeElement oldElement,
            @JsonProperty("newElement") CodeElement newElement,
            @JsonProperty("description") String description,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("reason") String reason) {
        this.id = Objects.requireNonNull(id, "Change event ID cannot be null");
        this.changeType = Objects.requireNonNull(changeType, "Change type cannot be null");
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null");
        this.oldElement = oldElement;
        this.newElement = newElement;
        this.description = description;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.reason = reason;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private ChangeType changeType;
        private Severity severity;
        private String filePath;
        private CodeElement oldElement;
        private CodeElement newElement;
        private String description;
        private Instant timestamp;
        private String reason;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder changeType(ChangeType changeType) { this.changeType = changeType; return this; }
        public Builder severity(Severity severity) { this.severity = severity; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder oldElement(CodeElement oldElement) { this.oldElement = oldElement; return this; }
        public Builder newElement(CodeElement newElement) { this.newElement = newElement; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        
        public DocumentationChangeEvent build() {
            if (id == null) id = java.util.UUID.randomUUID().toString();
            return new DocumentationChangeEvent(id, changeType, severity, filePath,
                    oldElement, newElement, description, timestamp, reason);
        }
    }

    // Convenience factory methods
    public static DocumentationChangeEvent elementAdded(String filePath, CodeElement newElement, String reason) {
        return builder()
                .changeType(ChangeType.ELEMENT_ADDED)
                .severity(determineSeverityForAddition(newElement))
                .filePath(filePath)
                .newElement(newElement)
                .description("New " + newElement.getType() + " '" + newElement.getName() + "' added")
                .reason(reason)
                .build();
    }

    public static DocumentationChangeEvent elementDeleted(String filePath, CodeElement oldElement, String reason) {
        return builder()
                .changeType(ChangeType.ELEMENT_DELETED)
                .severity(determineSeverityForDeletion(oldElement))
                .filePath(filePath)
                .oldElement(oldElement)
                .description("Element '" + oldElement.getName() + "' deleted")
                .reason(reason)
                .build();
    }

    public static DocumentationChangeEvent elementModified(String filePath, CodeElement oldElement, 
                                                         CodeElement newElement, String reason) {
        return builder()
                .changeType(ChangeType.ELEMENT_MODIFIED)
                .severity(determineSeverityForModification(oldElement, newElement))
                .filePath(filePath)
                .oldElement(oldElement)
                .newElement(newElement)
                .description("Element '" + newElement.getName() + "' modified")
                .reason(reason)
                .build();
    }

    public static DocumentationChangeEvent signatureChanged(String filePath, CodeElement oldElement, 
                                                          CodeElement newElement, String reason) {
        return builder()
                .changeType(ChangeType.SIGNATURE_CHANGED)
                .severity(Severity.HIGH)
                .filePath(filePath)
                .oldElement(oldElement)
                .newElement(newElement)
                .description("Signature changed for '" + newElement.getName() + "'")
                .reason(reason)
                .build();
    }

    public static DocumentationChangeEvent contentChanged(String filePath, CodeElement oldElement, 
                                                        CodeElement newElement, String reason) {
        return builder()
                .changeType(ChangeType.CONTENT_CHANGED)
                .severity(Severity.MEDIUM)
                .filePath(filePath)
                .oldElement(oldElement)
                .newElement(newElement)
                .description("Content changed for '" + newElement.getName() + "'")
                .reason(reason)
                .build();
    }

    /**
     * Checks if this change event requires immediate documentation update.
     */
    public boolean requiresImmediateUpdate() {
        return severity == Severity.CRITICAL || 
               (severity == Severity.HIGH && changeType == ChangeType.SIGNATURE_CHANGED);
    }

    /**
     * Gets the affected element (new element if added, old element if deleted, new element otherwise).
     */
    public CodeElement getAffectedElement() {
        return switch (changeType) {
            case ELEMENT_ADDED -> newElement;
            case ELEMENT_DELETED -> oldElement;
            default -> newElement != null ? newElement : oldElement;
        };
    }

    /**
     * Gets a human-readable summary of the change.
     */
    public String getSummary() {
        CodeElement affected = getAffectedElement();
        if (affected == null) return description;
        
        return String.format("%s: %s '%s' in %s", 
                severity, changeType.toString().toLowerCase().replace('_', ' '), 
                affected.getName(), filePath);
    }

    // Private helper methods for severity determination
    
    private static Severity determineSeverityForAddition(CodeElement element) {
        if (element == null) return Severity.LOW;
        
        return switch (element.getType().toLowerCase()) {
            case "class" -> Severity.HIGH;
            case "method", "function" -> element.getName().startsWith("_") ? Severity.LOW : Severity.MEDIUM;
            case "variable" -> Severity.LOW;
            default -> Severity.LOW;
        };
    }
    
    private static Severity determineSeverityForDeletion(CodeElement element) {
        if (element == null) return Severity.LOW;
        
        return switch (element.getType().toLowerCase()) {
            case "class" -> Severity.CRITICAL;
            case "method", "function" -> element.getName().startsWith("_") ? Severity.MEDIUM : Severity.HIGH;
            case "variable" -> Severity.LOW;
            default -> Severity.MEDIUM;
        };
    }
    
    private static Severity determineSeverityForModification(CodeElement oldElement, CodeElement newElement) {
        if (oldElement == null || newElement == null) return Severity.MEDIUM;
        
        // Check if signature changed
        if (!Objects.equals(oldElement.getSignature(), newElement.getSignature())) {
            return Severity.HIGH;
        }
        
        // Check if parameters changed
        if (!Objects.equals(oldElement.getParameters(), newElement.getParameters())) {
            return Severity.HIGH;
        }
        
        // Check if return type changed
        if (!Objects.equals(oldElement.getReturnType(), newElement.getReturnType())) {
            return Severity.HIGH;
        }
        
        return Severity.MEDIUM;
    }

    // Getters
    public String getId() { return id; }
    public ChangeType getChangeType() { return changeType; }
    public Severity getSeverity() { return severity; }
    public String getFilePath() { return filePath; }
    public CodeElement getOldElement() { return oldElement; }
    public CodeElement getNewElement() { return newElement; }
    public String getDescription() { return description; }
    public Instant getTimestamp() { return timestamp; }
    public String getReason() { return reason; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentationChangeEvent that = (DocumentationChangeEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("DocumentationChangeEvent{id='%s', type=%s, severity=%s, file='%s', description='%s'}", 
                id, changeType, severity, filePath, description);
    }
}