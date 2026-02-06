package com.ailearning.core.service.ai;

import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.ai.AIBreakdown;

import java.util.concurrent.CompletableFuture;

/**
 * Service for generating step-by-step breakdowns of complex code patterns
 * to help developers understand intricate programming concepts.
 */
public interface StepByStepBreakdownGenerator {

    /**
     * Creates a detailed step-by-step breakdown of complex code.
     *
     * @param complexCode the complex code to break down
     * @param codeContext the surrounding context
     * @return a future containing the step-by-step breakdown
     */
    CompletableFuture<AIBreakdown> createDetailedBreakdown(String complexCode, CodeContext codeContext);

    /**
     * Creates a beginner-friendly breakdown with simplified explanations.
     *
     * @param complexCode the complex code to break down
     * @param codeContext the surrounding context
     * @return a future containing a beginner-friendly breakdown
     */
    CompletableFuture<AIBreakdown> createBeginnerBreakdown(String complexCode, CodeContext codeContext);

    /**
     * Creates an interactive breakdown with questions and exercises.
     *
     * @param complexCode the complex code to break down
     * @param codeContext the surrounding context
     * @return a future containing an interactive breakdown
     */
    CompletableFuture<AIBreakdown> createInteractiveBreakdown(String complexCode, CodeContext codeContext);

    /**
     * Creates a breakdown focused on specific learning objectives.
     *
     * @param complexCode the complex code to break down
     * @param codeContext the surrounding context
     * @param learningObjectives the specific objectives to focus on
     * @return a future containing a focused breakdown
     */
    CompletableFuture<AIBreakdown> createFocusedBreakdown(String complexCode, CodeContext codeContext, 
                                                         java.util.List<String> learningObjectives);

    /**
     * Checks if the generator is available and operational.
     *
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}