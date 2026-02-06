package com.ailearning.core.service.ast;

import com.ailearning.core.model.ast.ASTNode;
import com.ailearning.core.model.ast.ParseResult;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for Abstract Syntax Tree parsing across multiple programming languages.
 * Provides language-agnostic AST parsing capabilities for code analysis.
 */
public interface ASTParser {
    
    /**
     * Parses source code and returns an AST representation.
     * 
     * @param sourceCode the source code to parse
     * @param language the programming language (java, javascript, python, etc.)
     * @param filePath the file path for context and error reporting
     * @return CompletableFuture containing the parse result
     */
    CompletableFuture<ParseResult> parseCode(String sourceCode, String language, String filePath);
    
    /**
     * Performs incremental parsing for real-time updates.
     * Updates an existing AST with changes instead of full re-parsing.
     * 
     * @param existingAST the existing AST to update
     * @param changes the code changes to apply
     * @param language the programming language
     * @return CompletableFuture containing the updated parse result
     */
    CompletableFuture<ParseResult> incrementalParse(ASTNode existingAST, String changes, String language);
    
    /**
     * Checks if the parser supports the given programming language.
     * 
     * @param language the programming language to check
     * @return true if the language is supported
     */
    boolean supportsLanguage(String language);
    
    /**
     * Gets the list of supported programming languages.
     * 
     * @return array of supported language identifiers
     */
    String[] getSupportedLanguages();
    
    /**
     * Validates syntax without full parsing for quick error detection.
     * 
     * @param sourceCode the source code to validate
     * @param language the programming language
     * @return CompletableFuture containing validation result
     */
    CompletableFuture<Boolean> validateSyntax(String sourceCode, String language);
}