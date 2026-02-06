package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the semantic context around a specific code location.
 * Includes information about surrounding code, usage patterns, and semantic relationships.
 */
public final class SemanticContext {
    
    @NotNull
    private final String filePath;
    
    @NotNull
    private final String functionName;
    
    @NotNull
    private final String className;
    
    @NotNull
    @Size(min = 1)
    private final List<String> imports;
    
    @NotNull
    private final List<String> usedVariables;
    
    @NotNull
    private final List<String> calledMethods;
    
    @NotNull
    private final Map<String, String> typeInformation;
    
    @NotNull
    private final List<CodePattern> nearbyPatterns;
    
    @NotNull
    private final List<Relationship> relationships;
    
    private final double complexityScore;
    
    @NotNull
    private final String semanticRole;
    
    @JsonCreator
    public SemanticContext(
            @JsonProperty("filePath") String filePath,
            @JsonProperty("functionName") String functionName,
            @JsonProperty("className") String className,
            @JsonProperty("imports") List<String> imports,
            @JsonProperty("usedVariables") List<String> usedVariables,
            @JsonProperty("calledMethods") List<String> calledMethods,
            @JsonProperty("typeInformation") Map<String, String> typeInformation,
            @JsonProperty("nearbyPatterns") List<CodePattern> nearbyPatterns,
            @JsonProperty("relationships") List<Relationship> relationships,
            @JsonProperty("complexityScore") double complexityScore,
            @JsonProperty("semanticRole") String semanticRole) {
        this.filePath = Objects.requireNonNull(filePath, "filePath cannot be null");
        this.functionName = Objects.requireNonNull(functionName, "functionName cannot be null");
        this.className = Objects.requireNonNull(className, "className cannot be null");
        this.imports = List.copyOf(Objects.requireNonNull(imports, "imports cannot be null"));
        this.usedVariables = List.copyOf(Objects.requireNonNull(usedVariables, "usedVariables cannot be null"));
        this.calledMethods = List.copyOf(Objects.requireNonNull(calledMethods, "calledMethods cannot be null"));
        this.typeInformation = Map.copyOf(Objects.requireNonNull(typeInformation, "typeInformation cannot be null"));
        this.nearbyPatterns = List.copyOf(Objects.requireNonNull(nearbyPatterns, "nearbyPatterns cannot be null"));
        this.relationships = List.copyOf(Objects.requireNonNull(relationships, "relationships cannot be null"));
        this.complexityScore = complexityScore;
        this.semanticRole = Objects.requireNonNull(semanticRole, "semanticRole cannot be null");
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public List<String> getImports() {
        return imports;
    }
    
    public List<String> getUsedVariables() {
        return usedVariables;
    }
    
    public List<String> getCalledMethods() {
        return calledMethods;
    }
    
    public Map<String, String> getTypeInformation() {
        return typeInformation;
    }
    
    public List<CodePattern> getNearbyPatterns() {
        return nearbyPatterns;
    }
    
    public List<Relationship> getRelationships() {
        return relationships;
    }
    
    public double getComplexityScore() {
        return complexityScore;
    }
    
    public String getSemanticRole() {
        return semanticRole;
    }
    
    /**
     * Creates a builder for SemanticContext.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String filePath;
        private String functionName = "";
        private String className = "";
        private List<String> imports = List.of();
        private List<String> usedVariables = List.of();
        private List<String> calledMethods = List.of();
        private Map<String, String> typeInformation = Map.of();
        private List<CodePattern> nearbyPatterns = List.of();
        private List<Relationship> relationships = List.of();
        private double complexityScore = 0.0;
        private String semanticRole = "unknown";
        
        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }
        
        public Builder functionName(String functionName) {
            this.functionName = functionName;
            return this;
        }
        
        public Builder className(String className) {
            this.className = className;
            return this;
        }
        
        public Builder imports(List<String> imports) {
            this.imports = imports;
            return this;
        }
        
        public Builder usedVariables(List<String> usedVariables) {
            this.usedVariables = usedVariables;
            return this;
        }
        
        public Builder calledMethods(List<String> calledMethods) {
            this.calledMethods = calledMethods;
            return this;
        }
        
        public Builder typeInformation(Map<String, String> typeInformation) {
            this.typeInformation = typeInformation;
            return this;
        }
        
        public Builder nearbyPatterns(List<CodePattern> nearbyPatterns) {
            this.nearbyPatterns = nearbyPatterns;
            return this;
        }
        
        public Builder relationships(List<Relationship> relationships) {
            this.relationships = relationships;
            return this;
        }
        
        public Builder complexityScore(double complexityScore) {
            this.complexityScore = complexityScore;
            return this;
        }
        
        public Builder semanticRole(String semanticRole) {
            this.semanticRole = semanticRole;
            return this;
        }
        
        public SemanticContext build() {
            return new SemanticContext(filePath, functionName, className, imports, usedVariables,
                    calledMethods, typeInformation, nearbyPatterns, relationships, complexityScore, semanticRole);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemanticContext that = (SemanticContext) o;
        return Double.compare(that.complexityScore, complexityScore) == 0 &&
                Objects.equals(filePath, that.filePath) &&
                Objects.equals(functionName, that.functionName) &&
                Objects.equals(className, that.className) &&
                Objects.equals(imports, that.imports) &&
                Objects.equals(usedVariables, that.usedVariables) &&
                Objects.equals(calledMethods, that.calledMethods) &&
                Objects.equals(typeInformation, that.typeInformation) &&
                Objects.equals(nearbyPatterns, that.nearbyPatterns) &&
                Objects.equals(relationships, that.relationships) &&
                Objects.equals(semanticRole, that.semanticRole);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(filePath, functionName, className, imports, usedVariables,
                calledMethods, typeInformation, nearbyPatterns, relationships, complexityScore, semanticRole);
    }
    
    @Override
    public String toString() {
        return "SemanticContext{" +
                "filePath='" + filePath + '\'' +
                ", functionName='" + functionName + '\'' +
                ", className='" + className + '\'' +
                ", imports=" + imports.size() +
                ", usedVariables=" + usedVariables.size() +
                ", calledMethods=" + calledMethods.size() +
                ", typeInformation=" + typeInformation.size() +
                ", nearbyPatterns=" + nearbyPatterns.size() +
                ", relationships=" + relationships.size() +
                ", complexityScore=" + complexityScore +
                ", semanticRole='" + semanticRole + '\'' +
                '}';
    }
}