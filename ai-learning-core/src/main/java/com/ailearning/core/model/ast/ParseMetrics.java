package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Metrics collected during AST parsing.
 */
public final class ParseMetrics {
    
    private final int nodeCount;
    private final int maxDepth;
    private final int errorCount;
    private final int warningCount;
    
    @JsonCreator
    public ParseMetrics(
            @JsonProperty("nodeCount") int nodeCount,
            @JsonProperty("maxDepth") int maxDepth,
            @JsonProperty("errorCount") int errorCount,
            @JsonProperty("warningCount") int warningCount) {
        this.nodeCount = Math.max(0, nodeCount);
        this.maxDepth = Math.max(0, maxDepth);
        this.errorCount = Math.max(0, errorCount);
        this.warningCount = Math.max(0, warningCount);
    }
    
    /**
     * Creates empty metrics.
     * 
     * @return new ParseMetrics with zero values
     */
    public static ParseMetrics empty() {
        return new ParseMetrics(0, 0, 0, 0);
    }
    
    /**
     * Gets the total number of AST nodes.
     * 
     * @return the node count
     */
    public int getNodeCount() {
        return nodeCount;
    }
    
    /**
     * Gets the maximum depth of the AST.
     * 
     * @return the maximum depth
     */
    public int getMaxDepth() {
        return maxDepth;
    }
    
    /**
     * Gets the number of parse errors.
     * 
     * @return the error count
     */
    public int getErrorCount() {
        return errorCount;
    }
    
    /**
     * Gets the number of parse warnings.
     * 
     * @return the warning count
     */
    public int getWarningCount() {
        return warningCount;
    }
    
    /**
     * Gets the total number of issues (errors + warnings).
     * 
     * @return the total issue count
     */
    public int getTotalIssues() {
        return errorCount + warningCount;
    }
    
    /**
     * Checks if parsing was successful (no errors).
     * 
     * @return true if there are no errors
     */
    public boolean isSuccessful() {
        return errorCount == 0;
    }
    
    /**
     * Calculates the complexity score based on node count and depth.
     * 
     * @return complexity score (0-100)
     */
    public int getComplexityScore() {
        if (nodeCount == 0) return 0;
        
        // Simple complexity calculation based on nodes and depth
        double score = Math.min(100, (nodeCount / 10.0) + (maxDepth * 2.0));
        return (int) Math.round(score);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ParseMetrics that = (ParseMetrics) obj;
        return nodeCount == that.nodeCount &&
               maxDepth == that.maxDepth &&
               errorCount == that.errorCount &&
               warningCount == that.warningCount;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nodeCount, maxDepth, errorCount, warningCount);
    }
    
    @Override
    public String toString() {
        return String.format("ParseMetrics{nodes=%d, depth=%d, errors=%d, warnings=%d, complexity=%d}", 
                           nodeCount, maxDepth, errorCount, warningCount, getComplexityScore());
    }
}