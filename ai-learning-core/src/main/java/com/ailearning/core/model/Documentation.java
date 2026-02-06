package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents generated documentation for code elements.
 */
public final class Documentation {
    
    public enum Type {
        INLINE_COMMENT, JAVADOC, JSDOC, PYTHON_DOCSTRING, MARKDOWN, API_DOC
    }
    
    public enum Format {
        PLAIN_TEXT, MARKDOWN, HTML, JSON
    }

    @NotBlank
    private final String id;
    
    @NotNull
    private final Type type;
    
    @NotNull
    private final Format format;
    
    @NotBlank
    private final String content;
    
    private final String filePath;
    private final Integer lineNumber;
    private final String elementName;
    private final String elementType;
    private final List<String> tags;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String template;
    private final double accuracyScore;

    @JsonCreator
    public Documentation(
            @JsonProperty("id") String id,
            @JsonProperty("type") Type type,
            @JsonProperty("format") Format format,
            @JsonProperty("content") String content,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("lineNumber") Integer lineNumber,
            @JsonProperty("elementName") String elementName,
            @JsonProperty("elementType") String elementType,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt,
            @JsonProperty("template") String template,
            @JsonProperty("accuracyScore") double accuracyScore) {
        this.id = Objects.requireNonNull(id, "Documentation ID cannot be null");
        this.type = Objects.requireNonNull(type, "Documentation type cannot be null");
        this.format = Objects.requireNonNull(format, "Documentation format cannot be null");
        this.content = Objects.requireNonNull(content, "Documentation content cannot be null");
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.elementName = elementName;
        this.elementType = elementType;
        this.tags = tags != null ? List.copyOf(tags) : List.of();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.template = template;
        this.accuracyScore = Math.max(0.0, Math.min(1.0, accuracyScore));
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private Type type;
        private Format format;
        private String content;
        private String filePath;
        private Integer lineNumber;
        private String elementName;
        private String elementType;
        private List<String> tags;
        private Instant createdAt;
        private Instant updatedAt;
        private String template;
        private double accuracyScore = 0.8;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder type(Type type) { this.type = type; return this; }
        public Builder format(Format format) { this.format = format; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder lineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return this; }
        public Builder elementName(String elementName) { this.elementName = elementName; return this; }
        public Builder elementType(String elementType) { this.elementType = elementType; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder template(String template) { this.template = template; return this; }
        public Builder accuracyScore(double accuracyScore) { this.accuracyScore = accuracyScore; return this; }
        
        public Documentation build() {
            if (id == null) id = java.util.UUID.randomUUID().toString();
            return new Documentation(id, type, format, content, filePath, lineNumber,
                    elementName, elementType, tags, createdAt, updatedAt, template, accuracyScore);
        }
    }

    // Convenience factory methods
    public static Documentation javadoc(String content, String filePath, Integer lineNumber, 
                                      String elementName, String elementType) {
        return builder()
                .type(Type.JAVADOC)
                .format(Format.PLAIN_TEXT)
                .content(content)
                .filePath(filePath)
                .lineNumber(lineNumber)
                .elementName(elementName)
                .elementType(elementType)
                .build();
    }

    public static Documentation jsdoc(String content, String filePath, Integer lineNumber, 
                                    String elementName, String elementType) {
        return builder()
                .type(Type.JSDOC)
                .format(Format.PLAIN_TEXT)
                .content(content)
                .filePath(filePath)
                .lineNumber(lineNumber)
                .elementName(elementName)
                .elementType(elementType)
                .build();
    }

    public static Documentation pythonDocstring(String content, String filePath, Integer lineNumber, 
                                              String elementName, String elementType) {
        return builder()
                .type(Type.PYTHON_DOCSTRING)
                .format(Format.PLAIN_TEXT)
                .content(content)
                .filePath(filePath)
                .lineNumber(lineNumber)
                .elementName(elementName)
                .elementType(elementType)
                .build();
    }

    public static Documentation markdown(String content, String filePath, String elementName) {
        return builder()
                .type(Type.MARKDOWN)
                .format(Format.MARKDOWN)
                .content(content)
                .filePath(filePath)
                .elementName(elementName)
                .build();
    }

    public static Documentation apiDoc(String content, String elementName, String elementType) {
        return builder()
                .type(Type.API_DOC)
                .format(Format.JSON)
                .content(content)
                .elementName(elementName)
                .elementType(elementType)
                .build();
    }

    /**
     * Checks if this documentation needs updating based on age
     */
    public boolean isStale(long maxAgeMillis) {
        return Instant.now().toEpochMilli() - updatedAt.toEpochMilli() > maxAgeMillis;
    }

    /**
     * Gets the location string if available
     */
    public String getLocationString() {
        if (filePath != null && lineNumber != null && lineNumber > 0) {
            return filePath + ":" + lineNumber;
        } else if (filePath != null) {
            return filePath;
        } else {
            return "Unknown";
        }
    }

    /**
     * Checks if this documentation has high accuracy
     */
    public boolean isHighAccuracy() {
        return accuracyScore >= 0.8;
    }

    // Getters
    public String getId() { return id; }
    public Type getType() { return type; }
    public Format getFormat() { return format; }
    public String getContent() { return content; }
    public String getFilePath() { return filePath; }
    public Integer getLineNumber() { return lineNumber; }
    public String getElementName() { return elementName; }
    public String getElementType() { return elementType; }
    public List<String> getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getTemplate() { return template; }
    public double getAccuracyScore() { return accuracyScore; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Documentation that = (Documentation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Documentation{id='%s', type=%s, element='%s', location='%s'}", 
                id, type, elementName, getLocationString());
    }
}