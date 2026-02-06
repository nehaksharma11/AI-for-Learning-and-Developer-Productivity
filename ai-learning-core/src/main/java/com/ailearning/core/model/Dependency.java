package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Represents a dependency in the project structure.
 * Can be external libraries, internal modules, or system dependencies.
 */
public class Dependency {
    
    public enum DependencyType {
        EXTERNAL_LIBRARY, INTERNAL_MODULE, SYSTEM, FRAMEWORK, TOOL
    }
    
    public enum DependencyScope {
        COMPILE, RUNTIME, TEST, PROVIDED, SYSTEM
    }

    @NotBlank
    private final String name;
    
    private final String version;
    private final String groupId;
    
    @NotNull
    private final DependencyType type;
    
    @NotNull
    private final DependencyScope scope;
    
    private final String description;
    private final boolean optional;

    @JsonCreator
    public Dependency(
            @JsonProperty("name") String name,
            @JsonProperty("version") String version,
            @JsonProperty("groupId") String groupId,
            @JsonProperty("type") DependencyType type,
            @JsonProperty("scope") DependencyScope scope,
            @JsonProperty("description") String description,
            @JsonProperty("optional") boolean optional) {
        this.name = Objects.requireNonNull(name, "Dependency name cannot be null");
        this.version = version;
        this.groupId = groupId;
        this.type = Objects.requireNonNull(type, "Dependency type cannot be null");
        this.scope = Objects.requireNonNull(scope, "Dependency scope cannot be null");
        this.description = description;
        this.optional = optional;
    }

    public Dependency(String name) {
        this(name, null, null, DependencyType.EXTERNAL_LIBRARY, DependencyScope.COMPILE, null, false);
    }

    public static Dependency externalLibrary(String name, String version) {
        return new Dependency(name, version, null, DependencyType.EXTERNAL_LIBRARY, 
                DependencyScope.COMPILE, null, false);
    }

    public static Dependency internalModule(String name) {
        return new Dependency(name, null, null, DependencyType.INTERNAL_MODULE, 
                DependencyScope.COMPILE, null, false);
    }

    public static Dependency testDependency(String name, String version) {
        return new Dependency(name, version, null, DependencyType.EXTERNAL_LIBRARY, 
                DependencyScope.TEST, null, false);
    }

    public static Dependency framework(String name, String version) {
        return new Dependency(name, version, null, DependencyType.FRAMEWORK, 
                DependencyScope.COMPILE, null, false);
    }

    /**
     * Gets the full identifier for this dependency
     */
    public String getFullIdentifier() {
        if (groupId != null && version != null) {
            return groupId + ":" + name + ":" + version;
        } else if (version != null) {
            return name + ":" + version;
        } else {
            return name;
        }
    }

    /**
     * Checks if this is a critical dependency (required at runtime)
     */
    public boolean isCritical() {
        return !optional && (scope == DependencyScope.COMPILE || scope == DependencyScope.RUNTIME);
    }

    // Getters
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getGroupId() { return groupId; }
    public DependencyType getType() { return type; }
    public DependencyScope getScope() { return scope; }
    public String getDescription() { return description; }
    public boolean isOptional() { return optional; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, groupId);
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", type=" + type +
                ", scope=" + scope +
                '}';
    }
}