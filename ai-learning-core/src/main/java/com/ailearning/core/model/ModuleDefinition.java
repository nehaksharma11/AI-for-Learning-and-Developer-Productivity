package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a module definition within a project structure.
 * A module is a logical grouping of related code files and functionality.
 */
public class ModuleDefinition {
    
    public enum ModuleType {
        LIBRARY, APPLICATION, TEST, CONFIGURATION, DOCUMENTATION
    }

    @NotBlank
    private final String name;
    
    @NotBlank
    private final String path;
    
    @NotNull
    private final ModuleType type;
    
    @NotNull
    private final List<String> dependencies;
    
    @NotNull
    private final List<String> exports;
    
    private final String description;
    private final String version;

    @JsonCreator
    public ModuleDefinition(
            @JsonProperty("name") String name,
            @JsonProperty("path") String path,
            @JsonProperty("type") ModuleType type,
            @JsonProperty("dependencies") List<String> dependencies,
            @JsonProperty("exports") List<String> exports,
            @JsonProperty("description") String description,
            @JsonProperty("version") String version) {
        this.name = Objects.requireNonNull(name, "Module name cannot be null");
        this.path = Objects.requireNonNull(path, "Module path cannot be null");
        this.type = Objects.requireNonNull(type, "Module type cannot be null");
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
        this.exports = exports != null ? new ArrayList<>(exports) : new ArrayList<>();
        this.description = description;
        this.version = version;
    }

    public static ModuleDefinition library(String name, String path) {
        return new ModuleDefinition(name, path, ModuleType.LIBRARY, 
                new ArrayList<>(), new ArrayList<>(), null, null);
    }

    public static ModuleDefinition application(String name, String path, String version) {
        return new ModuleDefinition(name, path, ModuleType.APPLICATION, 
                new ArrayList<>(), new ArrayList<>(), null, version);
    }

    public ModuleDefinition withDependency(String dependency) {
        List<String> newDependencies = new ArrayList<>(this.dependencies);
        newDependencies.add(dependency);
        return new ModuleDefinition(name, path, type, newDependencies, exports, description, version);
    }

    public ModuleDefinition withExport(String export) {
        List<String> newExports = new ArrayList<>(this.exports);
        newExports.add(export);
        return new ModuleDefinition(name, path, type, dependencies, newExports, description, version);
    }

    // Getters
    public String getName() { return name; }
    public String getPath() { return path; }
    public ModuleType getType() { return type; }
    public List<String> getDependencies() { return new ArrayList<>(dependencies); }
    public List<String> getExports() { return new ArrayList<>(exports); }
    public String getDescription() { return description; }
    public String getVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleDefinition that = (ModuleDefinition) o;
        return Objects.equals(name, that.name) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    @Override
    public String toString() {
        return "ModuleDefinition{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", dependencies=" + dependencies.size() +
                ", exports=" + exports.size() +
                '}';
    }
}