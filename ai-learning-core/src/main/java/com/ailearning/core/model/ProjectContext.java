package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the context and understanding of a project's codebase.
 */
public class ProjectContext {
    
    @NotBlank
    private final String id;
    
    @NotNull
    private final ProjectStructure structure;
    
    @NotNull
    private final List<Dependency> dependencies;
    
    @NotNull
    private final List<CodePattern> patterns;
    
    @NotNull
    private final List<CodingConvention> conventions;
    
    @NotNull
    private final ComplexityMetrics complexity;

    @JsonCreator
    public ProjectContext(
            @JsonProperty("id") String id,
            @JsonProperty("structure") ProjectStructure structure,
            @JsonProperty("dependencies") List<Dependency> dependencies,
            @JsonProperty("patterns") List<CodePattern> patterns,
            @JsonProperty("conventions") List<CodingConvention> conventions,
            @JsonProperty("complexity") ComplexityMetrics complexity) {
        this.id = Objects.requireNonNull(id, "Project ID cannot be null");
        this.structure = Objects.requireNonNull(structure, "Project structure cannot be null");
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
        this.patterns = patterns != null ? new ArrayList<>(patterns) : new ArrayList<>();
        this.conventions = conventions != null ? new ArrayList<>(conventions) : new ArrayList<>();
        this.complexity = Objects.requireNonNull(complexity, "Complexity metrics cannot be null");
    }

    public static ProjectContext create(String id, ProjectStructure structure) {
        return new ProjectContext(id, structure, new ArrayList<>(), 
                new ArrayList<>(), new ArrayList<>(), ComplexityMetrics.empty());
    }

    // Getters
    public String getId() { return id; }
    public String getProjectName() { return id; } // Use ID as project name for now
    public ProjectStructure getStructure() { return structure; }
    public List<Dependency> getDependencies() { return new ArrayList<>(dependencies); }
    public List<CodePattern> getPatterns() { return new ArrayList<>(patterns); }
    public List<CodingConvention> getConventions() { return new ArrayList<>(conventions); }
    public ComplexityMetrics getComplexity() { return complexity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectContext that = (ProjectContext) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProjectContext{" +
                "id='" + id + '\'' +
                ", fileCount=" + structure.getFiles().size() +
                ", dependencyCount=" + dependencies.size() +
                ", patternCount=" + patterns.size() +
                '}';
    }
}