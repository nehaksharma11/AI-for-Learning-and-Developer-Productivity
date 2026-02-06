package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Result of parsing source code into an Abstract Syntax Tree.
 * Contains the AST root node, parsing metadata, and any errors encountered.
 */
public final class ParseResult {
    
    private final ASTNode rootNode;
    private final String language;
    private final String filePath;
    private final boolean successful;
    private final List<ParseError> errors;
    private final List<ParseWarning> warnings;
    private final long parseTimeMs;
    private final Instant timestamp;
    private final ParseMetrics metrics;
    
    @JsonCreator
    public ParseResult(
            @JsonProperty("rootNode") ASTNode rootNode,
            @JsonProperty("language") String language,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("successful") boolean successful,
            @JsonProperty("errors") List<ParseError> errors,
            @JsonProperty("warnings") List<ParseWarning> warnings,
            @JsonProperty("parseTimeMs") long parseTimeMs,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("metrics") ParseMetrics metrics) {
        this.rootNode = rootNode;
        this.language = Objects.requireNonNull(language, "Language cannot be null");
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null");
        this.successful = successful;
        this.errors = errors != null ? List.copyOf(errors) : List.of();
        this.warnings = warnings != null ? List.copyOf(warnings) : List.of();
        this.parseTimeMs = Math.max(0, parseTimeMs);
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.metrics = metrics != null ? metrics : ParseMetrics.empty();
    }
    
    /**
     * Creates a successful parse result.
     * 
     * @param rootNode the root AST node
     * @param language the programming language
     * @param filePath the file path
     * @param parseTimeMs the parsing time in milliseconds
     * @return new successful ParseResult
     */
    public static ParseResult success(ASTNode rootNode, String language, String filePath, long parseTimeMs) {
        ParseMetrics metrics = new ParseMetrics(
                rootNode.getNodeCount(),
                rootNode.getDepth(),
                0,
                0
        );
        return new ParseResult(rootNode, language, filePath, true, List.of(), List.of(), 
                              parseTimeMs, Instant.now(), metrics);
    }
    
    /**
     * Creates a successful parse result with warnings.
     * 
     * @param rootNode the root AST node
     * @param language the programming language
     * @param filePath the file path
     * @param warnings the parse warnings
     * @param parseTimeMs the parsing time in milliseconds
     * @return new successful ParseResult with warnings
     */
    public static ParseResult successWithWarnings(ASTNode rootNode, String language, String filePath, 
                                                 List<ParseWarning> warnings, long parseTimeMs) {
        ParseMetrics metrics = new ParseMetrics(
                rootNode.getNodeCount(),
                rootNode.getDepth(),
                0,
                warnings.size()
        );
        return new ParseResult(rootNode, language, filePath, true, List.of(), warnings, 
                              parseTimeMs, Instant.now(), metrics);
    }
    
    /**
     * Creates a failed parse result.
     * 
     * @param language the programming language
     * @param filePath the file path
     * @param errors the parse errors
     * @param parseTimeMs the parsing time in milliseconds
     * @return new failed ParseResult
     */
    public static ParseResult failure(String language, String filePath, List<ParseError> errors, long parseTimeMs) {
        ParseMetrics metrics = new ParseMetrics(0, 0, errors.size(), 0);
        return new ParseResult(null, language, filePath, false, errors, List.of(), 
                              parseTimeMs, Instant.now(), metrics);
    }
    
    /**
     * Gets the root AST node.
     * 
     * @return the root node, or null if parsing failed
     */
    public ASTNode getRootNode() {
        return rootNode;
    }
    
    /**
     * Gets the programming language.
     * 
     * @return the language identifier
     */
    public String getLanguage() {
        return language;
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
     * Checks if parsing was successful.
     * 
     * @return true if parsing succeeded
     */
    public boolean isSuccessful() {
        return successful;
    }
    
    /**
     * Gets the parse errors.
     * 
     * @return immutable list of errors
     */
    public List<ParseError> getErrors() {
        return errors;
    }
    
    /**
     * Gets the parse warnings.
     * 
     * @return immutable list of warnings
     */
    public List<ParseWarning> getWarnings() {
        return warnings;
    }
    
    /**
     * Gets the parsing time in milliseconds.
     * 
     * @return the parse time
     */
    public long getParseTimeMs() {
        return parseTimeMs;
    }
    
    /**
     * Gets the timestamp when parsing was performed.
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the parsing metrics.
     * 
     * @return the metrics
     */
    public ParseMetrics getMetrics() {
        return metrics;
    }
    
    /**
     * Checks if there are any errors.
     * 
     * @return true if there are errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Checks if there are any warnings.
     * 
     * @return true if there are warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Gets the total number of issues (errors + warnings).
     * 
     * @return the total issue count
     */
    public int getIssueCount() {
        return errors.size() + warnings.size();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ParseResult that = (ParseResult) obj;
        return successful == that.successful &&
               parseTimeMs == that.parseTimeMs &&
               Objects.equals(rootNode, that.rootNode) &&
               Objects.equals(language, that.language) &&
               Objects.equals(filePath, that.filePath) &&
               Objects.equals(errors, that.errors) &&
               Objects.equals(warnings, that.warnings) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(metrics, that.metrics);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rootNode, language, filePath, successful, errors, warnings, 
                           parseTimeMs, timestamp, metrics);
    }
    
    @Override
    public String toString() {
        return String.format("ParseResult{language='%s', filePath='%s', successful=%s, " +
                           "errors=%d, warnings=%d, parseTime=%dms}", 
                           language, filePath, successful, errors.size(), warnings.size(), parseTimeMs);
    }
}