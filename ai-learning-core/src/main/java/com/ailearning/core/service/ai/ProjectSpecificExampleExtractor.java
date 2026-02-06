package com.ailearning.core.service.ai;

import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ai.AIExample;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for extracting and generating project-specific code examples
 * that are relevant to the current codebase and development context.
 */
public interface ProjectSpecificExampleExtractor {

    /**
     * Extracts examples from the current project that match a given pattern.
     *
     * @param pattern the code pattern to find examples for
     * @param projectContext the project context
     * @return a future containing project-specific examples
     */
    CompletableFuture<List<AIExample>> extractProjectExamples(String pattern, ProjectContext projectContext);

    /**
     * Generates examples that would fit well in the current project context.
     *
     * @param pattern the pattern to generate examples for
     * @param projectContext the project context
     * @param maxExamples maximum number of examples to generate
     * @return a future containing generated examples
     */
    CompletableFuture<List<AIExample>> generateContextualExamples(String pattern, ProjectContext projectContext, int maxExamples);

    /**
     * Finds similar code patterns in the project and formats them as examples.
     *
     * @param codeSnippet the code snippet to find similar patterns for
     * @param projectContext the project context
     * @return a future containing similar examples from the project
     */
    CompletableFuture<List<AIExample>> findSimilarExamples(String codeSnippet, ProjectContext projectContext);

    /**
     * Extracts examples that demonstrate best practices used in the project.
     *
     * @param category the category of best practices (e.g., "error-handling", "testing")
     * @param projectContext the project context
     * @return a future containing best practice examples
     */
    CompletableFuture<List<AIExample>> extractBestPracticeExamples(String category, ProjectContext projectContext);

    /**
     * Checks if the extractor is available and operational.
     *
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}