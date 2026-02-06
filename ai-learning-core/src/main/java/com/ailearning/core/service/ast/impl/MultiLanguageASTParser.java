package com.ailearning.core.service.ast.impl;

import com.ailearning.core.model.ast.ASTNode;
import com.ailearning.core.model.ast.ParseResult;
import com.ailearning.core.service.ast.ASTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-language AST parser that delegates to language-specific parsers.
 * Provides a unified interface for parsing different programming languages.
 */
public class MultiLanguageASTParser implements ASTParser {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiLanguageASTParser.class);
    
    private final Map<String, ASTParser> languageParsers = new ConcurrentHashMap<>();
    
    public MultiLanguageASTParser() {
        // Initialize language-specific parsers
        languageParsers.put("java", new JavaASTParser());
        languageParsers.put("javascript", new JavaScriptASTParser());
        languageParsers.put("typescript", new JavaScriptASTParser()); // TypeScript uses same parser
        languageParsers.put("python", new PythonASTParser());
        
        logger.info("Initialized multi-language AST parser with {} languages", languageParsers.size());
    }
    
    @Override
    public CompletableFuture<ParseResult> parseCode(String sourceCode, String language, String filePath) {
        logger.debug("Parsing {} code from {}", language, filePath);
        
        ASTParser parser = getParserForLanguage(language);
        if (parser == null) {
            return CompletableFuture.completedFuture(
                ParseResult.failure(language, filePath, 
                    java.util.List.of(com.ailearning.core.model.ast.ParseError.of(
                        "Unsupported language: " + language,
                        com.ailearning.core.model.ast.SourceLocation.at(filePath, 1, 1)
                    )), 0)
            );
        }
        
        long startTime = System.currentTimeMillis();
        return parser.parseCode(sourceCode, language, filePath)
                .whenComplete((result, throwable) -> {
                    long duration = System.currentTimeMillis() - startTime;
                    if (throwable != null) {
                        logger.error("Failed to parse {} code from {}: {}", language, filePath, throwable.getMessage());
                    } else {
                        logger.debug("Parsed {} code from {} in {}ms (success: {})", 
                                   language, filePath, duration, result.isSuccessful());
                    }
                });
    }
    
    @Override
    public CompletableFuture<ParseResult> incrementalParse(ASTNode existingAST, String changes, String language) {
        logger.debug("Performing incremental parse for {} language", language);
        
        ASTParser parser = getParserForLanguage(language);
        if (parser == null) {
            return CompletableFuture.completedFuture(
                ParseResult.failure(language, "unknown", 
                    java.util.List.of(com.ailearning.core.model.ast.ParseError.of(
                        "Unsupported language for incremental parsing: " + language,
                        com.ailearning.core.model.ast.SourceLocation.at("unknown", 1, 1)
                    )), 0)
            );
        }
        
        return parser.incrementalParse(existingAST, changes, language);
    }
    
    @Override
    public CompletableFuture<Boolean> validateSyntax(String sourceCode, String language) {
        logger.debug("Validating syntax for {} language", language);
        
        ASTParser parser = getParserForLanguage(language);
        if (parser == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        return parser.validateSyntax(sourceCode, language);
    }
    
    @Override
    public boolean supportsLanguage(String language) {
        return languageParsers.containsKey(language.toLowerCase());
    }
    
    @Override
    public String[] getSupportedLanguages() {
        return languageParsers.keySet().toArray(new String[0]);
    }
    
    /**
     * Gets the parser for a specific language.
     * 
     * @param language the programming language
     * @return the parser, or null if not supported
     */
    private ASTParser getParserForLanguage(String language) {
        return languageParsers.get(language.toLowerCase());
    }
    
    /**
     * Adds a parser for a specific language.
     * 
     * @param language the programming language
     * @param parser the parser implementation
     */
    public void addLanguageParser(String language, ASTParser parser) {
        languageParsers.put(language.toLowerCase(), parser);
        logger.info("Added parser for {} language", language);
    }
    
    /**
     * Removes a parser for a specific language.
     * 
     * @param language the programming language
     */
    public void removeLanguageParser(String language) {
        languageParsers.remove(language.toLowerCase());
        logger.info("Removed parser for {} language", language);
    }
    
    /**
     * Gets statistics about the registered parsers.
     * 
     * @return map of language to parser class name
     */
    public Map<String, String> getParserStatistics() {
        Map<String, String> stats = new ConcurrentHashMap<>();
        languageParsers.forEach((lang, parser) -> 
            stats.put(lang, parser.getClass().getSimpleName()));
        return stats;
    }
}