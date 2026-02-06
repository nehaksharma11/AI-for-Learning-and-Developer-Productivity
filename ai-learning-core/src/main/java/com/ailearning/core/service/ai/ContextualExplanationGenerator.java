package com.ailearning.core.service.ai;

import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ai.AIExplanation;

import java.util.concurrent.CompletableFuture;

/**
 * Service for generating contextual code explanations that take into account
 * the surrounding code context and project-specific information.
 */
public interface ContextualExplanationGenerator {

    /**
     * Generates a contextual explanation for a code snippet.
     *
     * @param codeSnippet the code to explain
     * @param codeContext the surrounding code context
     * @param projectContext the project context for better understanding
     * @return a future containing the contextual explanation
     */
    CompletableFuture<AIExplanation> generateExplanation(String codeSnippet, CodeContext codeContext, ProjectContext projectContext);

    /**
     * Generates a quick explanation for hover interactions.
     *
     * @param codeElement the specific code element (variable, method, class, etc.)
     * @param codeContext the surrounding context
     * @return a future containing a brief explanation
     */
    CompletableFuture<AIExplanation> generateHoverExplanation(String codeElement, CodeContext codeContext);

    /**
     * Generates an explanation with learning tips and best practices.
     *
     * @param codeSnippet the code to explain
     * @param codeContext the surrounding context
     * @param projectContext the project context
     * @param includeImprovements whether to include improvement suggestions
     * @return a future containing an educational explanation
     */
    CompletableFuture<AIExplanation> generateEducationalExplanation(String codeSnippet, CodeContext codeContext, 
                                                                   ProjectContext projectContext, boolean includeImprovements);

    /**
     * Checks if the generator is available and operational.
     *
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}