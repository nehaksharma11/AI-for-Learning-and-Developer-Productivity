package com.ailearning.core.service;

import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.WorkContext;
import com.ailearning.core.model.CodeChange;
import com.ailearning.core.model.CodeQuery;
import com.ailearning.core.model.CodeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Core interface for the Context Engine component.
 * Responsible for maintaining real-time understanding of codebase structure,
 * tracking developer focus, and managing project-specific knowledge.
 */
public interface ContextEngine {
    
    /**
     * Analyzes a project and builds initial context understanding.
     * 
     * @param projectPath the root path of the project to analyze
     * @return CompletableFuture containing the project context
     */
    CompletableFuture<ProjectContext> analyzeProject(String projectPath);
    
    /**
     * Updates the context based on code changes.
     * Should complete within 500ms as per requirements.
     * 
     * @param changes list of code changes to process
     * @return CompletableFuture that completes when context is updated
     */
    CompletableFuture<Void> updateContext(List<CodeChange> changes);
    
    /**
     * Gets the current work context for the developer.
     * 
     * @return CompletableFuture containing the current work context
     */
    CompletableFuture<WorkContext> getCurrentContext();
    
    /**
     * Finds code related to a specific query.
     * 
     * @param query the code query to search for
     * @return CompletableFuture containing list of related code references
     */
    CompletableFuture<List<CodeReference>> findRelatedCode(CodeQuery query);
    
    /**
     * Checks if the context engine is ready to process requests.
     * 
     * @return true if the engine is initialized and ready
     */
    boolean isReady();
    
    /**
     * Gets performance metrics for the context engine.
     * 
     * @return performance metrics including response times and memory usage
     */
    ContextEngineMetrics getMetrics();
}