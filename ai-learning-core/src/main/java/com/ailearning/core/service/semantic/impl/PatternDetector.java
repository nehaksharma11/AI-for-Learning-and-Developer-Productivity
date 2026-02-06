package com.ailearning.core.service.semantic.impl;

import com.ailearning.core.model.ast.ASTNode;
import com.ailearning.core.model.ast.ClassNode;
import com.ailearning.core.model.ast.MethodNode;
import com.ailearning.core.model.CodePattern;
import com.ailearning.core.model.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects code patterns and design patterns in AST structures.
 * Identifies common programming patterns, design patterns, and anti-patterns.
 */
public class PatternDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(PatternDetector.class);
    
    private final Map<String, PatternMatcher> patternMatchers;
    
    public PatternDetector() {
        this.patternMatchers = initializePatternMatchers();
        logger.info("Initialized PatternDetector with {} pattern matchers", patternMatchers.size());
    }
    
    /**
     * Detects patterns in the given AST node.
     */
    public List<CodePattern> detectPatterns(ASTNode astNode, String language) {
        logger.debug("Detecting patterns for {} language in AST node: {}", language, astNode.getType());
        
        List<CodePattern> detectedPatterns = new ArrayList<>();
        
        // Apply all pattern matchers
        for (Map.Entry<String, PatternMatcher> entry : patternMatchers.entrySet()) {
            String patternName = entry.getKey();
            PatternMatcher matcher = entry.getValue();
            
            if (matcher.matches(astNode, language)) {
                CodePattern pattern = createCodePattern(patternName, astNode, matcher.getConfidence());
                detectedPatterns.add(pattern);
                logger.debug("Detected pattern: {} with confidence: {}", patternName, matcher.getConfidence());
            }
        }
        
        return detectedPatterns;
    }
    
    /**
     * Initializes pattern matchers for different design patterns and code patterns.
     */
    private Map<String, PatternMatcher> initializePatternMatchers() {
        Map<String, PatternMatcher> matchers = new HashMap<>();
        
        // Design Patterns
        matchers.put("Singleton", new SingletonPatternMatcher());
        matchers.put("Factory", new FactoryPatternMatcher());
        matchers.put("Observer", new ObserverPatternMatcher());
        matchers.put("Builder", new BuilderPatternMatcher());
        matchers.put("Strategy", new StrategyPatternMatcher());
        
        // Code Patterns
        matchers.put("NullCheck", new NullCheckPatternMatcher());
        matchers.put("LoopPattern", new LoopPatternMatcher());
        matchers.put("ExceptionHandling", new ExceptionHandlingPatternMatcher());
        matchers.put("ResourceManagement", new ResourceManagementPatternMatcher());
        
        // Anti-patterns
        matchers.put("GodClass", new GodClassAntiPatternMatcher());
        matchers.put("LongMethod", new LongMethodAntiPatternMatcher());
        
        return matchers;
    }
    
    /**
     * Creates a CodePattern instance from detected pattern information.
     */
    private CodePattern createCodePattern(String patternName, ASTNode astNode, double confidence) {
        return CodePattern.builder()
                .name(patternName)
                .type(determinePatternType(patternName))
                .description(generatePatternDescription(patternName))
                .location(astNode.getSourceLocation().getFilePath())
                .confidence(confidence)
                .examples(List.of(astNode.toString()))
                .build();
    }
    
    /**
     * Determines the pattern type based on pattern name.
     */
    private Pattern determinePatternType(String patternName) {
        return switch (patternName) {
            case "Singleton", "Factory", "Observer", "Builder", "Strategy" -> Pattern.DESIGN_PATTERN;
            case "GodClass", "LongMethod" -> Pattern.ANTI_PATTERN;
            default -> Pattern.CODE_PATTERN;
        };
    }
    
    /**
     * Generates a description for the detected pattern.
     */
    private String generatePatternDescription(String patternName) {
        return switch (patternName) {
            case "Singleton" -> "Ensures a class has only one instance and provides global access";
            case "Factory" -> "Creates objects without specifying their concrete classes";
            case "Observer" -> "Defines a one-to-many dependency between objects";
            case "Builder" -> "Constructs complex objects step by step";
            case "Strategy" -> "Defines a family of algorithms and makes them interchangeable";
            case "NullCheck" -> "Checks for null values before using objects";
            case "LoopPattern" -> "Iterates over collections or performs repetitive operations";
            case "ExceptionHandling" -> "Handles exceptions and error conditions";
            case "ResourceManagement" -> "Manages system resources like files or connections";
            case "GodClass" -> "A class that knows too much or does too much";
            case "LongMethod" -> "A method that is too long and does too many things";
            default -> "Code pattern: " + patternName;
        };
    }
    
    // Pattern Matcher Implementations
    
    private interface PatternMatcher {
        boolean matches(ASTNode node, String language);
        double getConfidence();
    }
    
    private static class SingletonPatternMatcher implements PatternMatcher {
        private double confidence = 0.0;
        
        @Override
        public boolean matches(ASTNode node, String language) {
            if (!(node instanceof ClassNode classNode)) return false;
            
            // Look for singleton characteristics
            boolean hasPrivateConstructor = classNode.getChildren().stream()
                    .filter(child -> child instanceof MethodNode)
                    .map(child -> (MethodNode) child)
                    .anyMatch(method -> method.getName().equals(classNode.getName()) && 
                             method.getModifiers().contains("private"));
            
            boolean hasStaticInstance = classNode.getChildren().stream()
                    .anyMatch(child -> child.toString().contains("static") && 
                             child.toString().contains(classNode.getName()));
            
            if (hasPrivateConstructor && hasStaticInstance) {
                confidence = 0.8;
                return true;
            }
            
            confidence = 0.0;
            return false;
        }
        
        @Override
        public double getConfidence() {
            return confidence;
        }
    }
    
    private static class FactoryPatternMatcher implements PatternMatcher {
        private double confidence = 0.0;
        
        @Override
        public boolean matches(ASTNode node, String language) {
            if (!(node instanceof ClassNode classNode)) return false;
            
            // Look for factory method characteristics
            boolean hasCreateMethod = classNode.getChildren().stream()
                    .filter(child -> child instanceof MethodNode)
                    .map(child -> (MethodNode) child)
                    .anyMatch(method -> method.getName().toLowerCase().contains("create") ||
                             method.getName().toLowerCase().contains("factory"));
            
            if (hasCreateMethod) {
                confidence = 0.7;
                return true;
            }
            
            confidence = 0.0;
            return false;
        }
        
        @Override
        public double getConfidence() {
            return confidence;
        }
    }
    
    private static class BuilderPatternMatcher implements PatternMatcher {
        private double confidence = 0.0;
        
        @Override
        public boolean matches(ASTNode node, String language) {
            if (!(node instanceof ClassNode classNode)) return false;
            
            // Look for builder pattern characteristics
            boolean hasBuilderMethod = classNode.getChildren().stream()
                    .filter(child -> child instanceof MethodNode)
                    .map(child -> (MethodNode) child)
                    .anyMatch(method -> method.getName().equals("builder") && 
                             method.getModifiers().contains("static"));
            
            boolean hasBuildMethod = classNode.getChildren().stream()
                    .filter(child -> child instanceof MethodNode)
                    .map(child -> (MethodNode) method)
                    .anyMatch(method -> method.getName().equals("build"));
            
            if (hasBuilderMethod || hasBuildMethod) {
                confidence = 0.75;
                return true;
            }
            
            confidence = 0.0;
            return false;
        }
        
        @Override
        public double getConfidence() {
            return confidence;
        }
    }
    
    // Simplified implementations for other pattern matchers
    private static class ObserverPatternMatcher implements PatternMatcher {
        @Override
        public boolean matches(ASTNode node, String language) {
            return node.toString().toLowerCase().contains("observer") ||
                   node.toString().toLowerCase().contains("listener");
        }
        
        @Override
        public double getConfidence() {
            return 0.6;
        }
    }
    
    private static class StrategyPatternMatcher implements PatternMatcher {
        @Override
        public boolean matches(ASTNode node, String language) {
            return node.toString().toLowerCase().contains("strategy") ||
                   (node instanceof ClassNode && node.toString().contains("interface"));
        }
        
        @Override
        public double getConfidence() {
            return 0.6;
        }
    }
    
    private static class NullCheckPatternMatcher implements PatternMatcher {
        @Override
        public boolean matches(ASTNode node, String language) {
            return node.toString().contains("!= null") ||
                   node.toString().contains("== null") ||
                   node.toString().contains("Objects.requireNonNull");
        }
        
        @Override
        public double getConfidence() {
            return 0.9;
        }
    }
    
    private static class LoopPatternMatcher implements PatternMatcher {
        @Override
        public boolean matches(ASTNode node, String language) {
            String nodeStr = node.toString().toLowerCase();
            return nodeStr.contains("for") || nodeStr.contains("while") || nodeStr.contains("foreach");
        }
        
        @Override
        public double getConfidence() {
            return 0.95;
        }
    }
    
    private static class ExceptionHandlingPatternMatcher implements PatternMatcher {
        @Override
        public boolean matches(ASTNode node, String language) {
            String nodeStr = node.toString().toLowerCase();
            return nodeStr.contains("try") || nodeStr.contains("catch") || nodeStr.contains("finally");
        }
        
        @Override
        public double getConfidence() {
            return 0.9;
        }
    }
    
    private static class ResourceManagementPatternMatcher implements PatternMatcher {
        @Override
        public boolean matches(ASTNode node, String language) {
            String nodeStr = node.toString().toLowerCase();
            return nodeStr.contains("try-with-resources") ||
                   (nodeStr.contains("try") && nodeStr.contains("close"));
        }
        
        @Override
        public double getConfidence() {
            return 0.8;
        }
    }
    
    private static class GodClassAntiPatternMatcher implements PatternMatcher {
        @Override
        public boolean matches(ASTNode node, String language) {
            if (!(node instanceof ClassNode classNode)) return false;
            
            // Simple heuristic: class with too many methods or too many lines
            int methodCount = (int) classNode.getChildren().stream()
                    .filter(child -> child instanceof MethodNode)
                    .count();
            
            return methodCount > 20; // Arbitrary threshold
        }
        
        @Override
        public double getConfidence() {
            return 0.7;
        }
    }
    
    private static class LongMethodAntiPatternMatcher implements PatternMatcher {
        @Override
        public boolean matches(ASTNode node, String language) {
            if (!(node instanceof MethodNode)) return false;
            
            // Simple heuristic: method with too many children (statements)
            return node.getChildren().size() > 30; // Arbitrary threshold
        }
        
        @Override
        public double getConfidence() {
            return 0.8;
        }
    }
}