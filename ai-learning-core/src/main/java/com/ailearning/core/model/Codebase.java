package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a complete codebase for analysis.
 * Immutable value object containing all necessary information for static analysis.
 */
public final class Codebase {
    
    @NotBlank
    private final String rootPath;
    
    @NotNull
    private final List<String> sourceFiles;
    
    @NotNull
    private final Map<String, String> fileContents;
    
    private final List<String> excludePatterns;
    private final Map<String, String> languageMapping;
    private final long totalLines;
    private final long totalFiles;
    
    @JsonCreator
    public Codebase(
            @JsonProperty("rootPath") String rootPath,
            @JsonProperty("sourceFiles") List<String> sourceFiles,
            @JsonProperty("fileContents") Map<String, String> fileContents,
            @JsonProperty("excludePatterns") List<String> excludePatterns,
            @JsonProperty("languageMapping") Map<String, String> languageMapping,
            @JsonProperty("totalLines") long totalLines,
            @JsonProperty("totalFiles") long totalFiles) {
        this.rootPath = Objects.requireNonNull(rootPath, "Root path cannot be null");
        this.sourceFiles = sourceFiles != null ? List.copyOf(sourceFiles) : List.of();
        this.fileContents = fileContents != null ? Map.copyOf(fileContents) : Map.of();
        this.excludePatterns = excludePatterns != null ? List.copyOf(excludePatterns) : List.of();
        this.languageMapping = languageMapping != null ? Map.copyOf(languageMapping) : Map.of();
        this.totalLines = totalLines;
        this.totalFiles = totalFiles;
    }
    
    // Getters
    public String getRootPath() { return rootPath; }
    public List<String> getSourceFiles() { return sourceFiles; }
    public Map<String, String> getFileContents() { return fileContents; }
    public List<String> getExcludePatterns() { return excludePatterns; }
    public Map<String, String> getLanguageMapping() { return languageMapping; }
    public long getTotalLines() { return totalLines; }
    public long getTotalFiles() { return totalFiles; }
    
    /**
     * Gets the content of a specific file.
     */
    public String getFileContent(String filePath) {
        return fileContents.get(filePath);
    }
    
    /**
     * Gets the programming language for a file.
     */
    public String getLanguageForFile(String filePath) {
        return languageMapping.get(filePath);
    }
    
    /**
     * Checks if a file should be excluded from analysis.
     */
    public boolean isExcluded(String filePath) {
        return excludePatterns.stream()
                .anyMatch(pattern -> filePath.matches(pattern.replace("*", ".*")));
    }
    
    /**
     * Gets the relative path from root.
     */
    public String getRelativePath(String absolutePath) {
        Path root = Paths.get(rootPath);
        Path absolute = Paths.get(absolutePath);
        return root.relativize(absolute).toString();
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String rootPath;
        private List<String> sourceFiles;
        private Map<String, String> fileContents;
        private List<String> excludePatterns;
        private Map<String, String> languageMapping;
        private long totalLines;
        private long totalFiles;
        
        public Builder rootPath(String rootPath) { this.rootPath = rootPath; return this; }
        public Builder sourceFiles(List<String> sourceFiles) { this.sourceFiles = sourceFiles; return this; }
        public Builder fileContents(Map<String, String> fileContents) { this.fileContents = fileContents; return this; }
        public Builder excludePatterns(List<String> excludePatterns) { this.excludePatterns = excludePatterns; return this; }
        public Builder languageMapping(Map<String, String> languageMapping) { this.languageMapping = languageMapping; return this; }
        public Builder totalLines(long totalLines) { this.totalLines = totalLines; return this; }
        public Builder totalFiles(long totalFiles) { this.totalFiles = totalFiles; return this; }
        
        public Codebase build() {
            return new Codebase(rootPath, sourceFiles, fileContents, 
                    excludePatterns, languageMapping, totalLines, totalFiles);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Codebase codebase = (Codebase) o;
        return totalLines == codebase.totalLines &&
               totalFiles == codebase.totalFiles &&
               Objects.equals(rootPath, codebase.rootPath) &&
               Objects.equals(sourceFiles, codebase.sourceFiles);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rootPath, sourceFiles, totalLines, totalFiles);
    }
    
    @Override
    public String toString() {
        return String.format("Codebase{rootPath='%s', files=%d, lines=%d}", 
                rootPath, totalFiles, totalLines);
    }
}