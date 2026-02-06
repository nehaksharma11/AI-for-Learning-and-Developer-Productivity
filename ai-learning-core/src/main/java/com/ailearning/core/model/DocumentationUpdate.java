package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an update to existing documentation.
 */
public final class DocumentationUpdate {
    
    public enum UpdateType {
        CREATED, MODIFIED, DELETED, SYNCHRONIZED
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String documentationId;
    
    @NotNull
    private final UpdateType updateType;
    
    private final String oldContent;
    private final String newContent;
    private final String reason;
    private final Instant timestamp;
    private final String triggeredBy;

    @JsonCreator
    public DocumentationUpdate(
            @JsonProperty("id") String id,
            @JsonProperty("documentationId") String documentationId,
            @JsonProperty("updateType") UpdateType updateType,
            @JsonProperty("oldContent") String oldContent,
            @JsonProperty("newContent") String newContent,
            @JsonProperty("reason") String reason,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("triggeredBy") String triggeredBy) {
        this.id = Objects.requireNonNull(id, "Update ID cannot be null");
        this.documentationId = Objects.requireNonNull(documentationId, "Documentation ID cannot be null");
        this.updateType = Objects.requireNonNull(updateType, "Update type cannot be null");
        this.oldContent = oldContent;
        this.newContent = newContent;
        this.reason = reason;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.triggeredBy = triggeredBy;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String documentationId;
        private UpdateType updateType;
        private String oldContent;
        private String newContent;
        private String reason;
        private Instant timestamp;
        private String triggeredBy;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder documentationId(String documentationId) { this.documentationId = documentationId; return this; }
        public Builder updateType(UpdateType updateType) { this.updateType = updateType; return this; }
        public Builder oldContent(String oldContent) { this.oldContent = oldContent; return this; }
        public Builder newContent(String newContent) { this.newContent = newContent; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder triggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; return this; }
        
        public DocumentationUpdate build() {
            if (id == null) id = java.util.UUID.randomUUID().toString();
            return new DocumentationUpdate(id, documentationId, updateType, oldContent, 
                    newContent, reason, timestamp, triggeredBy);
        }
    }

    // Convenience factory methods
    public static DocumentationUpdate created(String documentationId, String content, String reason) {
        return builder()
                .documentationId(documentationId)
                .updateType(UpdateType.CREATED)
                .newContent(content)
                .reason(reason)
                .triggeredBy("system")
                .build();
    }

    public static DocumentationUpdate modified(String documentationId, String oldContent, 
                                             String newContent, String reason) {
        return builder()
                .documentationId(documentationId)
                .updateType(UpdateType.MODIFIED)
                .oldContent(oldContent)
                .newContent(newContent)
                .reason(reason)
                .triggeredBy("system")
                .build();
    }

    public static DocumentationUpdate deleted(String documentationId, String oldContent, String reason) {
        return builder()
                .documentationId(documentationId)
                .updateType(UpdateType.DELETED)
                .oldContent(oldContent)
                .reason(reason)
                .triggeredBy("system")
                .build();
    }

    public static DocumentationUpdate synchronized(String documentationId, String oldContent, 
                                                 String newContent, String reason) {
        return builder()
                .documentationId(documentationId)
                .updateType(UpdateType.SYNCHRONIZED)
                .oldContent(oldContent)
                .newContent(newContent)
                .reason(reason)
                .triggeredBy("synchronization")
                .build();
    }

    /**
     * Checks if this update represents a significant change
     */
    public boolean isSignificantChange() {
        if (oldContent == null || newContent == null) {
            return updateType == UpdateType.CREATED || updateType == UpdateType.DELETED;
        }
        
        // Simple heuristic: significant if content length changes by more than 20%
        double lengthRatio = (double) newContent.length() / oldContent.length();
        return lengthRatio < 0.8 || lengthRatio > 1.2;
    }

    // Getters
    public String getId() { return id; }
    public String getDocumentationId() { return documentationId; }
    public UpdateType getUpdateType() { return updateType; }
    public String getOldContent() { return oldContent; }
    public String getNewContent() { return newContent; }
    public String getReason() { return reason; }
    public Instant getTimestamp() { return timestamp; }
    public String getTriggeredBy() { return triggeredBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentationUpdate that = (DocumentationUpdate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("DocumentationUpdate{id='%s', type=%s, docId='%s', timestamp=%s}", 
                id, updateType, documentationId, timestamp);
    }
}