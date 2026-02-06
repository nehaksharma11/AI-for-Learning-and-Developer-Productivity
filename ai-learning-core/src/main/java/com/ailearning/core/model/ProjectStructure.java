package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the structure of a project including files, modules, and relationships.
 */
public class ProjectStructure {
    
    @NotNull
    private final List<FileNode> files;
    
    @NotNull
    private final List<ModuleDefinition> modules;
    
    @NotNull
    private final List<Relationship> relationships;
    
    @NotNull
    private final List<String> entryPoints;

    @JsonCreator
    public ProjectStructure(
            @JsonProperty("files") List<FileNode> files,
            @JsonProperty("modules") List<ModuleDefinition> modules,
            @JsonProperty("relationships") List<Relationship> relationships,
            @JsonProperty("entryPoints") List<String> entryPoints) {
        this.files = files != null ? new ArrayList<>(files) : new ArrayList<>();
        this.modules = modules != null ? new ArrayList<>(modules) : new ArrayList<>();
        this.relationships = relationships != null ? new ArrayList<>(relationships) : new ArrayList<>();
        this.entryPoints = entryPoints != null ? new ArrayList<>(entryPoints) : new ArrayList<>();
    }

    public static ProjectStructure empty() {
        return new ProjectStructure(new ArrayList<>(), new ArrayList<>(), 
                new ArrayList<>(), new ArrayList<>());
    }

    // Getters
    public List<FileNode> getFiles() { return new ArrayList<>(files); }
    public List<ModuleDefinition> getModules() { return new ArrayList<>(modules); }
    public List<Relationship> getRelationships() { return new ArrayList<>(relationships); }
    public List<String> getEntryPoints() { return new ArrayList<>(entryPoints); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectStructure that = (ProjectStructure) o;
        return Objects.equals(files, that.files) &&
                Objects.equals(modules, that.modules) &&
                Objects.equals(relationships, that.relationships) &&
                Objects.equals(entryPoints, that.entryPoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files, modules, relationships, entryPoints);
    }

    @Override
    public String toString() {
        return "ProjectStructure{" +
                "fileCount=" + files.size() +
                ", moduleCount=" + modules.size() +
                ", relationshipCount=" + relationships.size() +
                ", entryPointCount=" + entryPoints.size() +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<FileNode> files = new ArrayList<>();
        private List<ModuleDefinition> modules = new ArrayList<>();
        private List<Relationship> relationships = new ArrayList<>();
        private List<String> entryPoints = new ArrayList<>();

        public Builder files(List<FileNode> files) { this.files = files; return this; }
        public Builder modules(List<ModuleDefinition> modules) { this.modules = modules; return this; }
        public Builder relationships(List<Relationship> relationships) { this.relationships = relationships; return this; }
        public Builder entryPoints(List<String> entryPoints) { this.entryPoints = entryPoints; return this; }

        public ProjectStructure build() {
            return new ProjectStructure(files, modules, relationships, entryPoints);
        }
    }
}