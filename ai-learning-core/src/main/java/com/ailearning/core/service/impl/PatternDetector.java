package com.ailearning.core.service.impl;

import com.ailearning.core.model.Codebase;
import com.ailearning.core.model.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Pattern detector for identifying design patterns, anti-patterns, and architectural patterns in code.
 * Analyzes codebase structure to detect common software engineering patterns.
 */
public class PatternDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(PatternDetector.class);
    
    private final Map<String, List<PatternRule>> patternRules;
    
    public PatternDetector() {
        this.patternRules = initializePatternRules();
    }
    
    public List<Pattern> detectPatterns(Codebase codebase) {
        List<Pattern> detectedPatterns = new ArrayList<>();
        
        logger.debug("Starting pattern detection for codebase: {}", codebase.getRootPath());
        
        // Analyze each file for patterns
        for (String filePath : codebase.getSourceFiles()) {
            String content = codebase.getFileContent(filePath);
            if (content != null) {
                String language = codebase.getLanguageForFile(filePath);
                detectedPatterns.addAll(detectPatternsInFile(content, filePath, language));
            }
        }
        
        // Analyze cross-file patterns
        detectedPatterns.addAll(detectArchitecturalPatterns(codebase));
        
        logger.debug("Detected {} patterns in codebase", detectedPatterns.size());
        return detectedPatterns;
    }
    
    private List<Pattern> detectPatternsInFile(String content, String filePath, String language) {
        List<Pattern> patterns = new ArrayList<>();
        
        List<PatternRule> rules = patternRules.getOrDefault(language, List.of());
        
        for (PatternRule rule : rules) {
            Matcher matcher = rule.pattern.matcher(content);
            if (matcher.find()) {
                Pattern pattern = Pattern.builder()
                        .id(rule.id + "-" + filePath.hashCode())
                        .name(rule.name)
                        .description(rule.description)
                        .type(rule.type)
                        .confidence(rule.confidence)
                        .files(List.of(filePath))
                        .recommendation(rule.recommendation)
                        .example(rule.example)
                        .impactScore(rule.impactScore)
                        .build();
                
                patterns.add(pattern);
                logger.debug("Pattern detected: {} in file {}", rule.name, filePath);
            }
        }
        
        return patterns;
    }
    
    private List<Pattern> detectArchitecturalPatterns(Codebase codebase) {
        List<Pattern> patterns = new ArrayList<>();
        
        // Detect MVC pattern
        if (hasMVCStructure(codebase)) {
            patterns.add(Pattern.builder()
                    .id("mvc-architecture")
                    .name("Model-View-Controller (MVC)")
                    .description("MVC architectural pattern detected")
                    .type(Pattern.Type.ARCHITECTURAL_PATTERN)
                    .confidence(Pattern.Confidence.HIGH)
                    .files(getMVCFiles(codebase))
                    .recommendation("Ensure proper separation of concerns between Model, View, and Controller")
                    .impactScore(0.8)
                    .build());
        }
        
        // Detect Repository pattern
        if (hasRepositoryPattern(codebase)) {
            patterns.add(Pattern.builder()
                    .id("repository-pattern")
                    .name("Repository Pattern")
                    .description("Repository pattern for data access abstraction")
                    .type(Pattern.Type.DESIGN_PATTERN)
                    .confidence(Pattern.Confidence.HIGH)
                    .files(getRepositoryFiles(codebase))
                    .recommendation("Consider implementing unit of work pattern for transaction management")
                    .impactScore(0.7)
                    .build());
        }
        
        // Detect Service Layer pattern
        if (hasServiceLayerPattern(codebase)) {
            patterns.add(Pattern.builder()
                    .id("service-layer")
                    .name("Service Layer Pattern")
                    .description("Service layer for business logic encapsulation")
                    .type(Pattern.Type.ARCHITECTURAL_PATTERN)
                    .confidence(Pattern.Confidence.MEDIUM)
                    .files(getServiceFiles(codebase))
                    .recommendation("Ensure services are stateless and focused on single responsibilities")
                    .impactScore(0.6)
                    .build());
        }
        
        return patterns;
    }
    
    private boolean hasMVCStructure(Codebase codebase) {
        boolean hasModel = false, hasView = false, hasController = false;
        
        for (String file : codebase.getSourceFiles()) {
            String lowerFile = file.toLowerCase();
            if (lowerFile.contains("model") || lowerFile.contains("entity")) hasModel = true;
            if (lowerFile.contains("view") || lowerFile.contains("template")) hasView = true;
            if (lowerFile.contains("controller") || lowerFile.contains("handler")) hasController = true;
        }
        
        return hasModel && hasView && hasController;
    }
    
    private List<String> getMVCFiles(Codebase codebase) {
        return codebase.getSourceFiles().stream()
                .filter(file -> {
                    String lower = file.toLowerCase();
                    return lower.contains("model") || lower.contains("view") || 
                           lower.contains("controller") || lower.contains("entity") ||
                           lower.contains("template") || lower.contains("handler");
                })
                .toList();
    }
    
    private boolean hasRepositoryPattern(Codebase codebase) {
        return codebase.getSourceFiles().stream()
                .anyMatch(file -> file.toLowerCase().contains("repository"));
    }
    
    private List<String> getRepositoryFiles(Codebase codebase) {
        return codebase.getSourceFiles().stream()
                .filter(file -> file.toLowerCase().contains("repository"))
                .toList();
    }
    
    private boolean hasServiceLayerPattern(Codebase codebase) {
        return codebase.getSourceFiles().stream()
                .anyMatch(file -> file.toLowerCase().contains("service"));
    }
    
    private List<String> getServiceFiles(Codebase codebase) {
        return codebase.getSourceFiles().stream()
                .filter(file -> file.toLowerCase().contains("service"))
                .toList();
    }
    
    private Map<String, List<PatternRule>> initializePatternRules() {
        Map<String, List<PatternRule>> rules = new HashMap<>();
        
        // Java patterns
        rules.put("java", Arrays.asList(
                new PatternRule(
                        "singleton-pattern",
                        "Singleton Pattern",
                        "Singleton design pattern implementation detected",
                        java.util.regex.Pattern.compile("private\\s+static\\s+\\w+\\s+instance.*getInstance\\(\\)", 
                                java.util.regex.Pattern.DOTALL),
                        Pattern.Type.DESIGN_PATTERN,
                        Pattern.Confidence.HIGH,
                        "Ensure thread safety in singleton implementation",
                        "Use enum singleton or double-checked locking",
                        0.6
                ),
                new PatternRule(
                        "builder-pattern",
                        "Builder Pattern",
                        "Builder pattern for object construction",
                        java.util.regex.Pattern.compile("public\\s+static\\s+class\\s+Builder.*\\.build\\(\\)", 
                                java.util.regex.Pattern.DOTALL),
                        Pattern.Type.DESIGN_PATTERN,
                        Pattern.Confidence.HIGH,
                        "Consider using Lombok @Builder for simpler implementation",
                        "Use builder pattern for objects with many optional parameters",
                        0.7
                ),
                new PatternRule(
                        "god-class",
                        "God Class Anti-Pattern",
                        "Class with too many responsibilities (potential god class)",
                        java.util.regex.Pattern.compile("class\\s+\\w+[^}]{2000,}", 
                                java.util.regex.Pattern.DOTALL),
                        Pattern.Type.ANTI_PATTERN,
                        Pattern.Confidence.MEDIUM,
                        "Break down large classes into smaller, focused classes",
                        "Apply Single Responsibility Principle",
                        0.8
                )
        ));
        
        // JavaScript patterns
        List<PatternRule> jsRules = Arrays.asList(
                new PatternRule(
                        "module-pattern",
                        "Module Pattern",
                        "JavaScript module pattern for encapsulation",
                        java.util.regex.Pattern.compile("\\(function\\s*\\(\\)\\s*\\{.*\\}\\)\\(\\);", 
                                java.util.regex.Pattern.DOTALL),
                        Pattern.Type.DESIGN_PATTERN,
                        Pattern.Confidence.HIGH,
                        "Consider using ES6 modules instead of IIFE pattern",
                        "Use import/export for better module management",
                        0.5
                ),
                new PatternRule(
                        "callback-hell",
                        "Callback Hell Anti-Pattern",
                        "Deeply nested callbacks making code hard to read",
                        java.util.regex.Pattern.compile("function\\s*\\([^)]*\\)\\s*\\{[^}]*function\\s*\\([^)]*\\)\\s*\\{[^}]*function", 
                                java.util.regex.Pattern.DOTALL),
                        Pattern.Type.ANTI_PATTERN,
                        Pattern.Confidence.MEDIUM,
                        "Use Promises or async/await to flatten callback structure",
                        "Refactor nested callbacks to use Promise chains",
                        0.7
                )
        );
        rules.put("javascript", jsRules);
        rules.put("typescript", jsRules);
        
        // Python patterns
        rules.put("python", Arrays.asList(
                new PatternRule(
                        "decorator-pattern",
                        "Decorator Pattern",
                        "Python decorator pattern usage",
                        java.util.regex.Pattern.compile("@\\w+\\s*\\ndef\\s+\\w+"),
                        Pattern.Type.DESIGN_PATTERN,
                        Pattern.Confidence.HIGH,
                        "Ensure decorators are well-documented and tested",
                        "Use functools.wraps to preserve function metadata",
                        0.6
                )
        ));
        
        return rules;
    }
    
    private static class PatternRule {
        final String id;
        final String name;
        final String description;
        final java.util.regex.Pattern pattern;
        final Pattern.Type type;
        final Pattern.Confidence confidence;
        final String recommendation;
        final String example;
        final double impactScore;
        
        PatternRule(String id, String name, String description, java.util.regex.Pattern pattern,
                   Pattern.Type type, Pattern.Confidence confidence, String recommendation,
                   String example, double impactScore) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.pattern = pattern;
            this.type = type;
            this.confidence = confidence;
            this.recommendation = recommendation;
            this.example = example;
            this.impactScore = impactScore;
        }
    }
}