package com.ailearning.core.service;

import com.ailearning.core.model.AnalysisResult;
import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.Suggestion;
import com.ailearning.core.model.Pattern;
import com.ailearning.core.model.SecurityIssue;
import com.ailearning.core.model.Codebase;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Core interface for the Code Analyzer component.
 * Responsible for real-time static analysis, security vulnerability detection,
 * and intelligent code suggestions.
 */
public interface CodeAnalyzer {
    
    /**
     * Analyzes code and returns comprehensive analysis results.
     * Should complete within 100ms for typical operations.
     * 
     * @param code the source code to analyze
     * @param language the programming language of the code
     * @return CompletableFuture containing analysis results
     */
    CompletableFuture<AnalysisResult> analyzeCode(String code, String language);
    
    /**
     * Suggests improvements based on code context.
     * Considers current codebase architecture and conventions.
     * 
     * @param context the current code context
     * @return CompletableFuture containing list of suggestions
     */
    CompletableFuture<List<Suggestion>> suggestImprovements(CodeContext context);
    
    /**
     * Detects patterns in the codebase.
     * 
     * @param codebase the codebase to analyze for patterns
     * @return CompletableFuture containing detected patterns
     */
    CompletableFuture<List<Pattern>> detectPatterns(Codebase codebase);
    
    /**
     * Validates code for security issues.
     * 
     * @param code the code to validate
     * @return CompletableFuture containing list of security issues
     */
    CompletableFuture<List<SecurityIssue>> validateSecurity(String code);
    
    /**
     * Checks if the analyzer supports the given programming language.
     * 
     * @param language the programming language to check
     * @return true if the language is supported
     */
    boolean supportsLanguage(String language);
    
    /**
     * Gets the list of supported programming languages.
     * 
     * @return list of supported language identifiers
     */
    List<String> getSupportedLanguages();
}