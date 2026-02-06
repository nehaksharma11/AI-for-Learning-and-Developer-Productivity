package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Represents a file node in the project structure.
 */
public class FileNode {
    
    @NotBlank
    private final String path;
    
    @NotBlank
    private final String name;
    
    private final String language;
    private final long size;
    private final boolean isDirectory;

    @JsonCreator
    public FileNode(
            @JsonProperty("path") String path,
            @JsonProperty("name") String name,
            @JsonProperty("language") String language,
            @JsonProperty("size") long size,
            @JsonProperty("isDirectory") boolean isDirectory) {
        this.path = Objects.requireNonNull(path, "Path cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.language = language;
        this.size = Math.max(0, size);
        this.isDirectory = isDirectory;
    }

    // Getters
    public String getPath() { return path; }
    public String getName() { return name; }
    public String getLanguage() { return language; }
    public long getSize() { return size; }
    public boolean isDirectory() { return isDirectory; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileNode fileNode = (FileNode) o;
        return Objects.equals(path, fileNode.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "FileNode{" +
                "path='" + path + '\'' +
                ", language='" + language + '\'' +
                ", size=" + size +
                '}';
    }
}