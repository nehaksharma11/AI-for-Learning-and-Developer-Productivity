package com.ailearning.core.service.ast.impl;

import com.ailearning.core.model.ast.*;
import com.ailearning.core.service.ast.ASTParser;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * JavaScript/TypeScript AST parser using Rhino parser.
 * Provides basic parsing capabilities for JavaScript and TypeScript code.
 */
public class JavaScriptASTParser implements ASTParser {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaScriptASTParser.class);
    
    private final Parser parser;
    
    public JavaScriptASTParser() {
        this.parser = new Parser();
        logger.debug("Initialized JavaScript AST parser");
    }
    
    @Override
    public CompletableFuture<ParseResult> parseCode(String sourceCode, String language, String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                AstRoot astRoot = parser.parse(sourceCode, filePath, 1);
                long parseTime = System.currentTimeMillis() - startTime;
                
                if (astRoot != null) {
                    ASTNode rootNode = convertToGenericAST(astRoot, filePath);
                    return ParseResult.success(rootNode, language, filePath, parseTime);
                } else {
                    List<ParseError> errors = List.of(
                        ParseError.of("Failed to parse JavaScript code", 
                                     SourceLocation.at(filePath, 1, 1))
                    );
                    return ParseResult.failure(language, filePath, errors, parseTime);
                }
                
            } catch (Exception e) {
                long parseTime = System.currentTimeMillis() - startTime;
                logger.error("Error parsing JavaScript code", e);
                List<ParseError> errors = List.of(
                    ParseError.of("Parse error: " + e.getMessage(), 
                                 SourceLocation.at(filePath, 1, 1))
                );
                return ParseResult.failure(language, filePath, errors, parseTime);
            }
        });
    }
    
    @Override
    public CompletableFuture<ParseResult> incrementalParse(ASTNode existingAST, String changes, String language) {
        // Fall back to full parsing for now
        logger.debug("Incremental parsing not implemented for JavaScript, falling back to full parse");
        return parseCode(changes, language, existingAST.getLocation().getFilePath());
    }
    
    @Override
    public CompletableFuture<Boolean> validateSyntax(String sourceCode, String language) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AstRoot result = parser.parse(sourceCode, "validation", 1);
                return result != null;
            } catch (Exception e) {
                logger.debug("Syntax validation failed for JavaScript code: {}", e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public boolean supportsLanguage(String language) {
        return "javascript".equalsIgnoreCase(language) || "typescript".equalsIgnoreCase(language);
    }
    
    @Override
    public String[] getSupportedLanguages() {
        return new String[]{"javascript", "typescript"};
    }
    
    /**
     * Converts Rhino AST to generic AST representation.
     */
    private ASTNode convertToGenericAST(AstRoot astRoot, String filePath) {
        List<ASTNode> children = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        
        // Basic conversion - this is a simplified implementation
        // In a full implementation, you would traverse the entire Rhino AST
        
        // Find functions
        astRoot.visit(node -> {
            if (node instanceof FunctionNode) {
                FunctionNode func = (FunctionNode) node;
                children.add(convertFunctionNode(func, filePath));
            }
            return true;
        });
        
        SourceLocation location = SourceLocation.at(filePath, 1, 1);
        return new JavaScriptRootNode("script", location, children, attributes);
    }
    
    /**
     * Converts a function node to generic AST.
     */
    private MethodNode convertFunctionNode(FunctionNode func, String filePath) {
        List<ASTNode> children = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        
        // Add function attributes
        attributes.put("returnType", "unknown"); // JavaScript is dynamically typed
        attributes.put("modifiers", List.of()); // No explicit modifiers in JavaScript
        
        // Add parameters
        if (!func.getParams().isEmpty()) {
            List<Map<String, String>> parameters = new ArrayList<>();
            func.getParams().forEach(param -> {
                if (param instanceof Name) {
                    Name nameParam = (Name) param;
                    parameters.add(Map.of(
                        "name", nameParam.getIdentifier(),
                        "type", "unknown"
                    ));
                }
            });
            attributes.put("parameters", parameters);
        }
        
        // Basic complexity calculation
        attributes.put("cyclomaticComplexity", 1); // Simplified
        
        SourceLocation location = SourceLocation.range(filePath,
            func.getLineno(), 0,
            func.getLineno() + 1, 0); // Simplified location
        
        String functionName = func.getName() != null ? func.getName() : "anonymous";
        return new MethodNode(functionName, location, children, attributes);
    }
    
    /**
     * Custom AST node for JavaScript root.
     */
    private static class JavaScriptRootNode extends ASTNode {
        public JavaScriptRootNode(String name, SourceLocation location, 
                                 List<ASTNode> children, Map<String, Object> attributes) {
            super("script", name, location, children, attributes);
        }
    }
}