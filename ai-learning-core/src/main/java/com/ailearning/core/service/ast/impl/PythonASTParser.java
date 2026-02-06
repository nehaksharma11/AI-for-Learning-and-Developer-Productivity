package com.ailearning.core.service.ast.impl;

import com.ailearning.core.model.ast.*;
import com.ailearning.core.service.ast.ASTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Python AST parser with basic parsing capabilities.
 * This is a simplified implementation that uses regex-based parsing
 * for basic Python constructs. A full implementation would use
 * a proper Python parser like ANTLR or Jython.
 */
public class PythonASTParser implements ASTParser {
    
    private static final Logger logger = LoggerFactory.getLogger(PythonASTParser.class);
    
    // Basic regex patterns for Python constructs
    private static final Pattern CLASS_PATTERN = Pattern.compile("^\\s*class\\s+(\\w+)\\s*(?:\\([^)]*\\))?\\s*:");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^\\s*def\\s+(\\w+)\\s*\\([^)]*\\)\\s*:");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*(?:from\\s+\\w+\\s+)?import\\s+(.+)");
    
    public PythonASTParser() {
        logger.debug("Initialized Python AST parser (simplified)");
    }
    
    @Override
    public CompletableFuture<ParseResult> parseCode(String sourceCode, String language, String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                ASTNode rootNode = parseSimplePython(sourceCode, filePath);
                long parseTime = System.currentTimeMillis() - startTime;
                
                return ParseResult.success(rootNode, language, filePath, parseTime);
                
            } catch (Exception e) {
                long parseTime = System.currentTimeMillis() - startTime;
                logger.error("Error parsing Python code", e);
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
        // Fall back to full parsing
        logger.debug("Incremental parsing not implemented for Python, falling back to full parse");
        return parseCode(changes, language, existingAST.getLocation().getFilePath());
    }
    
    @Override
    public CompletableFuture<Boolean> validateSyntax(String sourceCode, String language) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Basic syntax validation - check for balanced parentheses, brackets, etc.
                return isValidPythonSyntax(sourceCode);
            } catch (Exception e) {
                logger.debug("Syntax validation failed for Python code: {}", e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public boolean supportsLanguage(String language) {
        return "python".equalsIgnoreCase(language);
    }
    
    @Override
    public String[] getSupportedLanguages() {
        return new String[]{"python"};
    }
    
    /**
     * Simple Python parser using regex patterns.
     * This is a basic implementation for demonstration purposes.
     */
    private ASTNode parseSimplePython(String sourceCode, String filePath) {
        List<ASTNode> children = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        
        String[] lines = sourceCode.split("\n");
        List<String> imports = new ArrayList<>();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;
            
            // Check for imports
            Matcher importMatcher = IMPORT_PATTERN.matcher(line);
            if (importMatcher.matches()) {
                imports.add(importMatcher.group(1).trim());
                continue;
            }
            
            // Check for class definitions
            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.matches()) {
                String className = classMatcher.group(1);
                ClassNode classNode = createPythonClass(className, lineNumber, filePath, lines, i);
                children.add(classNode);
                continue;
            }
            
            // Check for function definitions (top-level)
            Matcher functionMatcher = FUNCTION_PATTERN.matcher(line);
            if (functionMatcher.matches() && !line.startsWith("    ")) { // Not indented (top-level)
                String functionName = functionMatcher.group(1);
                MethodNode methodNode = createPythonFunction(functionName, lineNumber, filePath);
                children.add(methodNode);
            }
        }
        
        if (!imports.isEmpty()) {
            attributes.put("imports", imports);
        }
        
        SourceLocation location = SourceLocation.at(filePath, 1, 1);
        return new PythonModuleNode("module", location, children, attributes);
    }
    
    /**
     * Creates a Python class node.
     */
    private ClassNode createPythonClass(String className, int lineNumber, String filePath, String[] lines, int startIndex) {
        List<ASTNode> children = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        
        attributes.put("modifiers", List.of()); // Python doesn't have explicit access modifiers
        
        // Look for methods within the class (simplified)
        for (int i = startIndex + 1; i < lines.length; i++) {
            String line = lines[i];
            
            // Stop if we reach another class or unindented code
            if (!line.trim().isEmpty() && !line.startsWith("    ")) {
                break;
            }
            
            // Check for method definitions within the class
            Matcher methodMatcher = FUNCTION_PATTERN.matcher(line);
            if (methodMatcher.matches()) {
                String methodName = methodMatcher.group(1);
                MethodNode methodNode = createPythonFunction(methodName, i + 1, filePath);
                children.add(methodNode);
            }
        }
        
        SourceLocation location = SourceLocation.at(filePath, lineNumber, 1);
        return new ClassNode(className, location, children, attributes);
    }
    
    /**
     * Creates a Python function node.
     */
    private MethodNode createPythonFunction(String functionName, int lineNumber, String filePath) {
        List<ASTNode> children = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        
        attributes.put("returnType", "unknown"); // Python is dynamically typed
        attributes.put("modifiers", List.of());
        attributes.put("parameters", List.of()); // Simplified - would need proper parsing
        attributes.put("cyclomaticComplexity", 1); // Simplified
        
        SourceLocation location = SourceLocation.at(filePath, lineNumber, 1);
        return new MethodNode(functionName, location, children, attributes);
    }
    
    /**
     * Basic Python syntax validation.
     */
    private boolean isValidPythonSyntax(String sourceCode) {
        // Very basic validation - check for balanced parentheses and brackets
        int parenCount = 0;
        int bracketCount = 0;
        int braceCount = 0;
        
        for (char c : sourceCode.toCharArray()) {
            switch (c) {
                case '(' -> parenCount++;
                case ')' -> parenCount--;
                case '[' -> bracketCount++;
                case ']' -> bracketCount--;
                case '{' -> braceCount++;
                case '}' -> braceCount--;
            }
            
            // Early exit if unbalanced
            if (parenCount < 0 || bracketCount < 0 || braceCount < 0) {
                return false;
            }
        }
        
        return parenCount == 0 && bracketCount == 0 && braceCount == 0;
    }
    
    /**
     * Custom AST node for Python module.
     */
    private static class PythonModuleNode extends ASTNode {
        public PythonModuleNode(String name, SourceLocation location, 
                               List<ASTNode> children, Map<String, Object> attributes) {
            super("module", name, location, children, attributes);
        }
    }
}