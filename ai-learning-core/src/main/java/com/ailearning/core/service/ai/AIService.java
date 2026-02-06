package com.ailearning.core.service.ai;

import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.ProjectContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Core interface for AI services that provide code understanding and explanation capabilities.
 */
public interface AIService {

    /**
     * Generates a contextual explanation for the given code snippet.
     *
     * @param codeSnippet the code to explain
     * @param codeContext the surrounding code context
     * @param projectContext the project context for better understanding
     * @return a future containing the explanation
     */
    CompletableFuture<AIExplanation> explainCode(String codeSnippet, CodeContext codeContext, ProjectContext projectContext);

    /**
     * Generates examples relevant to the given code pattern.
     *
     * @param codePattern the pattern to find examples for
     * @param projectContext the project context for relevant examples
     * @return a future containing relevant examples
     */
    CompletableFuture<List<AIExample>> generateExamples(String codePattern, ProjectContext projectContext);

    /**
     * Creates a step-by-step breakdown of complex code patterns.
     *
     * @param complexCode the complex code to break down
     * @param codeContext the surrounding context
     * @return a future containing the step-by-step breakdown
     */
    CompletableFuture<AIBreakdown> createBreakdown(String complexCode, CodeContext codeContext);

    /**
     * Checks if the AI service is available and operational.
     *
     * @return true if the service is available, false otherwise
     */
    boolean isAvailable();

    /**
     * Gets the name of this AI service implementation.
     *
     * @return the service name
     */
    String getServiceName();

    /**
     * Gets the priority of this service (higher values = higher priority).
     *
     * @return the service priority
     */
    int getPriority();
}