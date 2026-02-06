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
 * Represents a reusable code template for common patterns.
 * Templates can be used to generate boilerplate code and improve productivity.
 */
public class CodeTemplate {
    
    public enum TemplateType {
        CLASS, METHOD, INTERFACE, ANNOTATION, TEST, CONFIGURATION, DOCUMENTATION
    }
    
    public enum Language {
        JAVA, JAVASCRIPT, TYPESCRIPT, PYTHON, GO, CSHARP, KOTLIN
    }

    @NotBlank
    private final String id;
    
    @NotBlank
    private final String name;
    
    @NotBlank
    private final String description;
    
    @NotNull
    private final TemplateType type;
    
    @NotNull
    private final Language language;
    
    @NotBlank
    private final String template;
    
    @NotNull
    private final List<String> parameters;
    
    @NotNull
    private final Map<String, String> defaultValues;
    
    @NotNull
    private final List<String> tags;
    
    private final String framework;
    
    private final String category;
    
    private final int usageCount;
    
    private final double rating;
    
    @NotNull
    private final LocalDateTime createdAt;
    
    @NotNull
    private final LocalDateTime lastUsed;

    @JsonCreator
    public CodeTemplate(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("type") TemplateType type,
            @JsonProperty("language") Language language,
            @JsonProperty("template") String template,
            @JsonProperty("parameters") List<String> parameters,
            @JsonProperty("defaultValues") Map<String, String> defaultValues,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("framework") String framework,
            @JsonProperty("category") String category,
            @JsonProperty("usageCount") int usageCount,
            @JsonProperty("rating") double rating,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("lastUsed") LocalDateTime lastUsed) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.language = Objects.requireNonNull(language, "Language cannot be null");
        this.template = Objects.requireNonNull(template, "Template cannot be null");
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
        this.defaultValues = defaultValues != null ? new HashMap<>(defaultValues) : new HashMap<>();
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.framework = framework;
        this.category = category;
        this.usageCount = Math.max(0, usageCount);
        this.rating = Math.max(0.0, Math.min(5.0, rating));
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastUsed = lastUsed != null ? lastUsed : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Generates code from this template using the provided parameter values.
     */
    public String generateCode(Map<String, String> parameterValues) {
        String result = template;
        
        // Replace parameters with provided values or defaults
        for (String parameter : parameters) {
            String value = parameterValues.getOrDefault(parameter, defaultValues.get(parameter));
            if (value != null) {
                result = result.replace("${" + parameter + "}", value);
            }
        }
        
        return result;
    }

    /**
     * Checks if this template matches the given context.
     */
    public boolean matches(String context, Language targetLanguage, String targetFramework) {
        if (!language.equals(targetLanguage)) {
            return false;
        }
        
        if (framework != null && targetFramework != null && !framework.equalsIgnoreCase(targetFramework)) {
            return false;
        }
        
        // Check if any tags match the context
        return tags.stream().anyMatch(tag -> context.toLowerCase().contains(tag.toLowerCase()));
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public TemplateType getType() { return type; }
    public Language getLanguage() { return language; }
    public String getTemplate() { return template; }
    public List<String> getParameters() { return new ArrayList<>(parameters); }
    public Map<String, String> getDefaultValues() { return new HashMap<>(defaultValues); }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public String getFramework() { return framework; }
    public String getCategory() { return category; }
    public int getUsageCount() { return usageCount; }
    public double getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUsed() { return lastUsed; }

    public static class Builder {
        private String id;
        private String name;
        private String description;
        private TemplateType type;
        private Language language;
        private String template;
        private List<String> parameters = new ArrayList<>();
        private Map<String, String> defaultValues = new HashMap<>();
        private List<String> tags = new ArrayList<>();
        private String framework;
        private String category;
        private int usageCount = 0;
        private double rating = 0.0;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime lastUsed = LocalDateTime.now();

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder type(TemplateType type) { this.type = type; return this; }
        public Builder language(Language language) { this.language = language; return this; }
        public Builder template(String template) { this.template = template; return this; }
        public Builder parameters(List<String> parameters) { this.parameters = parameters; return this; }
        public Builder defaultValues(Map<String, String> defaultValues) { this.defaultValues = defaultValues; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder framework(String framework) { this.framework = framework; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder usageCount(int usageCount) { this.usageCount = usageCount; return this; }
        public Builder rating(double rating) { this.rating = rating; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder lastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; return this; }

        public CodeTemplate build() {
            return new CodeTemplate(id, name, description, type, language, template, parameters,
                    defaultValues, tags, framework, category, usageCount, rating, createdAt, lastUsed);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeTemplate that = (CodeTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CodeTemplate{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", language=" + language +
                ", usageCount=" + usageCount +
                ", rating=" + String.format("%.1f", rating) +
                '}';
    }
}