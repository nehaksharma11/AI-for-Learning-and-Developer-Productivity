package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a location in source code with line and column information.
 * Immutable value object for tracking code positions.
 */
public final class SourceLocation {
    
    private final String filePath;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;
    
    @JsonCreator
    public SourceLocation(
            @JsonProperty("filePath") String filePath,
            @JsonProperty("startLine") int startLine,
            @JsonProperty("startColumn") int startColumn,
            @JsonProperty("endLine") int endLine,
            @JsonProperty("endColumn") int endColumn) {
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null");
        this.startLine = Math.max(1, startLine);
        this.startColumn = Math.max(1, startColumn);
        this.endLine = Math.max(startLine, endLine);
        this.endColumn = Math.max(startColumn, endColumn);
    }
    
    /**
     * Creates a single-point location.
     * 
     * @param filePath the file path
     * @param line the line number (1-based)
     * @param column the column number (1-based)
     * @return new SourceLocation
     */
    public static SourceLocation at(String filePath, int line, int column) {
        return new SourceLocation(filePath, line, column, line, column);
    }
    
    /**
     * Creates a range location.
     * 
     * @param filePath the file path
     * @param startLine the start line number (1-based)
     * @param startColumn the start column number (1-based)
     * @param endLine the end line number (1-based)
     * @param endColumn the end column number (1-based)
     * @return new SourceLocation
     */
    public static SourceLocation range(String filePath, int startLine, int startColumn, int endLine, int endColumn) {
        return new SourceLocation(filePath, startLine, startColumn, endLine, endColumn);
    }
    
    /**
     * Gets the file path.
     * 
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Gets the start line number (1-based).
     * 
     * @return the start line number
     */
    public int getStartLine() {
        return startLine;
    }
    
    /**
     * Gets the start column number (1-based).
     * 
     * @return the start column number
     */
    public int getStartColumn() {
        return startColumn;
    }
    
    /**
     * Gets the end line number (1-based).
     * 
     * @return the end line number
     */
    public int getEndLine() {
        return endLine;
    }
    
    /**
     * Gets the end column number (1-based).
     * 
     * @return the end column number
     */
    public int getEndColumn() {
        return endColumn;
    }
    
    /**
     * Checks if this location represents a single point.
     * 
     * @return true if start and end positions are the same
     */
    public boolean isPoint() {
        return startLine == endLine && startColumn == endColumn;
    }
    
    /**
     * Checks if this location spans multiple lines.
     * 
     * @return true if the location spans multiple lines
     */
    public boolean isMultiLine() {
        return startLine != endLine;
    }
    
    /**
     * Gets the number of lines spanned by this location.
     * 
     * @return the number of lines (minimum 1)
     */
    public int getLineSpan() {
        return endLine - startLine + 1;
    }
    
    /**
     * Checks if this location contains another location.
     * 
     * @param other the other location to check
     * @return true if this location contains the other location
     */
    public boolean contains(SourceLocation other) {
        if (!filePath.equals(other.filePath)) {
            return false;
        }
        
        // Check if other location is within this location's bounds
        if (other.startLine < startLine || other.endLine > endLine) {
            return false;
        }
        
        if (other.startLine == startLine && other.startColumn < startColumn) {
            return false;
        }
        
        if (other.endLine == endLine && other.endColumn > endColumn) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if this location overlaps with another location.
     * 
     * @param other the other location to check
     * @return true if the locations overlap
     */
    public boolean overlaps(SourceLocation other) {
        if (!filePath.equals(other.filePath)) {
            return false;
        }
        
        // Check for overlap
        return !(endLine < other.startLine || 
                (endLine == other.startLine && endColumn < other.startColumn) ||
                startLine > other.endLine ||
                (startLine == other.endLine && startColumn > other.endColumn));
    }
    
    /**
     * Creates a new location that spans from this location to another.
     * 
     * @param other the other location
     * @return new SourceLocation spanning both locations
     * @throws IllegalArgumentException if locations are in different files
     */
    public SourceLocation spanTo(SourceLocation other) {
        if (!filePath.equals(other.filePath)) {
            throw new IllegalArgumentException("Cannot span locations in different files");
        }
        
        int newStartLine = Math.min(startLine, other.startLine);
        int newStartColumn = (newStartLine == startLine) ? startColumn : other.startColumn;
        int newEndLine = Math.max(endLine, other.endLine);
        int newEndColumn = (newEndLine == endLine) ? endColumn : other.endColumn;
        
        return new SourceLocation(filePath, newStartLine, newStartColumn, newEndLine, newEndColumn);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SourceLocation that = (SourceLocation) obj;
        return startLine == that.startLine &&
               startColumn == that.startColumn &&
               endLine == that.endLine &&
               endColumn == that.endColumn &&
               Objects.equals(filePath, that.filePath);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(filePath, startLine, startColumn, endLine, endColumn);
    }
    
    @Override
    public String toString() {
        if (isPoint()) {
            return String.format("%s:%d:%d", filePath, startLine, startColumn);
        } else {
            return String.format("%s:%d:%d-%d:%d", filePath, startLine, startColumn, endLine, endColumn);
        }
    }
}