package com.ailearning.core.service.impl;

import com.ailearning.core.model.ComplexityMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Complexity analyzer for calculating various code complexity metrics.
 * Implements cyclomatic complexity, cognitive complexity, and other maintainability metrics.
 */
public class ComplexityAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(ComplexityAnalyzer.class);
    
    public ComplexityMetrics analyze(String code, String language) {
        logger.debug("Analyzing complexity for {} code", language);
        
        int linesOfCode = countLinesOfCode(code);
        int cyclomaticComplexity = calculateCyclomaticComplexity(code, language);
        int cognitiveComplexity = calculateCognitiveComplexity(code, language);
        int nestingDepth = calculateMaxNestingDepth(code, language);
        int numberOfMethods = countMethods(code, language);
        int numberOfClasses = countClasses(code, language);
        double maintainabilityIndex = calculateMaintainabilityIndex(
                linesOfCode, cyclomaticComplexity, numberOfMethods);
        
        return ComplexityMetrics.builder()
                .cyclomaticComplexity(cyclomaticComplexity)
                .cognitiveComplexity(cognitiveComplexity)
                .linesOfCode(linesOfCode)
                .nestingDepth(nestingDepth)
                .numberOfMethods(numberOfMethods)
                .numberOfClasses(numberOfClasses)
                .maintainabilityIndex(maintainabilityIndex)
                .build();
    }
    
    private int countLinesOfCode(String code) {
        String[] lines = code.split("\n");
        int count = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            // Skip empty lines and comments
            if (!trimmed.isEmpty() && 
                !trimmed.startsWith("//") && 
                !trimmed.startsWith("/*") && 
                !trimmed.startsWith("*") &&
                !trimmed.startsWith("#")) {
                count++;
            }
        }
        
        return count;
    }
    
    private int calculateCyclomaticComplexity(String code, String language) {
        int complexity = 1; // Base complexity
        
        // Decision points that increase complexity
        String[] decisionKeywords = getDecisionKeywords(language);
        
        for (String keyword : decisionKeywords) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(code);
            
            while (matcher.find()) {
                complexity++;
            }
        }
        
        // Logical operators also increase complexity
        Pattern logicalPattern = Pattern.compile("&&|\\|\\|");
        Matcher logicalMatcher = logicalPattern.matcher(code);
        
        while (logicalMatcher.find()) {
            complexity++;
        }
        
        return complexity;
    }
    
    private int calculateCognitiveComplexity(String code, String language) {
        int complexity = 0;
        int nestingLevel = 0;
        
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            // Track nesting level
            if (isNestingIncrement(trimmed, language)) {
                nestingLevel++;
            } else if (isNestingDecrement(trimmed, language)) {
                nestingLevel = Math.max(0, nestingLevel - 1);
            }
            
            // Add complexity based on cognitive load
            if (isCognitiveComplexityIncrement(trimmed, language)) {
                complexity += 1 + nestingLevel; // Nested structures add more complexity
            }
        }
        
        return complexity;
    }
    
    private int calculateMaxNestingDepth(String code, String language) {
        int maxDepth = 0;
        int currentDepth = 0;
        
        String[] lines = code.split("\n");
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            if (isNestingIncrement(trimmed, language)) {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (isNestingDecrement(trimmed, language)) {
                currentDepth = Math.max(0, currentDepth - 1);
            }
        }
        
        return maxDepth;
    }
    
    private int countMethods(String code, String language) {
        Pattern methodPattern = getMethodPattern(language);
        if (methodPattern == null) return 0;
        
        Matcher matcher = methodPattern.matcher(code);
        int count = 0;
        
        while (matcher.find()) {
            count++;
        }
        
        return count;
    }
    
    private int countClasses(String code, String language) {
        Pattern classPattern = getClassPattern(language);
        if (classPattern == null) return 0;
        
        Matcher matcher = classPattern.matcher(code);
        int count = 0;
        
        while (matcher.find()) {
            count++;
        }
        
        return count;
    }
    
    private double calculateMaintainabilityIndex(int linesOfCode, int cyclomaticComplexity, int numberOfMethods) {
        // Simplified maintainability index calculation
        // Real formula: 171 - 5.2 * ln(Halstead Volume) - 0.23 * (Cyclomatic Complexity) - 16.2 * ln(Lines of Code)
        // Simplified version for demonstration
        
        if (linesOfCode == 0) return 100.0;
        
        double complexityPenalty = cyclomaticComplexity * 2.0;
        double sizePenalty = Math.log(linesOfCode) * 5.0;
        double methodPenalty = numberOfMethods * 0.5;
        
        double index = 100.0 - complexityPenalty - sizePenalty - methodPenalty;
        return Math.max(0.0, Math.min(100.0, index));
    }
    
    private String[] getDecisionKeywords(String language) {
        return switch (language.toLowerCase()) {
            case "java", "javascript", "typescript" -> 
                new String[]{"if", "else", "while", "for", "switch", "case", "catch", "?", ":"};
            case "python" -> 
                new String[]{"if", "elif", "else", "while", "for", "except", "and", "or"};
            default -> 
                new String[]{"if", "else", "while", "for", "switch", "case"};
        };
    }
    
    private boolean isNestingIncrement(String line, String language) {
        return switch (language.toLowerCase()) {
            case "java", "javascript", "typescript" -> 
                line.contains("{") || 
                line.matches(".*\\b(if|while|for|switch|try|catch|finally)\\b.*") ||
                line.matches(".*\\b(function|class)\\b.*");
            case "python" -> 
                line.matches(".*\\b(if|elif|else|while|for|try|except|finally|def|class)\\b.*:$");
            default -> 
                line.contains("{") || line.matches(".*\\b(if|while|for)\\b.*");
        };
    }
    
    private boolean isNestingDecrement(String line, String language) {
        return switch (language.toLowerCase()) {
            case "java", "javascript", "typescript" -> line.contains("}");
            case "python" -> false; // Python uses indentation, harder to detect without parsing
            default -> line.contains("}");
        };
    }
    
    private boolean isCognitiveComplexityIncrement(String line, String language) {
        String[] cognitiveKeywords = switch (language.toLowerCase()) {
            case "java", "javascript", "typescript" -> 
                new String[]{"if", "else", "while", "for", "switch", "case", "catch", "break", "continue"};
            case "python" -> 
                new String[]{"if", "elif", "else", "while", "for", "except", "break", "continue"};
            default -> 
                new String[]{"if", "else", "while", "for"};
        };
        
        for (String keyword : cognitiveKeywords) {
            if (line.matches(".*\\b" + keyword + "\\b.*")) {
                return true;
            }
        }
        
        return false;
    }
    
    private Pattern getMethodPattern(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> 
                Pattern.compile("\\b(public|private|protected|static)\\s+[^=]*\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{");
            case "javascript" -> 
                Pattern.compile("function\\s+(\\w+)\\s*\\(|\\b(\\w+)\\s*:\\s*function\\s*\\(");
            case "typescript" -> 
                Pattern.compile("function\\s+(\\w+)\\s*\\(|\\b(\\w+)\\s*\\([^)]*\\)\\s*:\\s*\\w+\\s*\\{");
            case "python" -> 
                Pattern.compile("def\\s+(\\w+)\\s*\\(");
            default -> null;
        };
    }
    
    private Pattern getClassPattern(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> 
                Pattern.compile("\\b(public|private|protected)?\\s*class\\s+(\\w+)");
            case "javascript", "typescript" -> 
                Pattern.compile("class\\s+(\\w+)");
            case "python" -> 
                Pattern.compile("class\\s+(\\w+)\\s*:");
            default -> null;
        };
    }
}