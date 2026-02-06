package com.ailearning.core.service.impl;

import com.ailearning.core.model.Suggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performance analyzer for detecting performance bottlenecks and optimization opportunities.
 * Analyzes code for common performance anti-patterns and suggests improvements.
 */
public class PerformanceAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceAnalyzer.class);
    
    private final Map<String, List<PerformanceRule>> performanceRules;
    
    public PerformanceAnalyzer() {
        this.performanceRules = initializePerformanceRules();
    }
    
    public List<Suggestion> analyzeSuggestions(String code, String language) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        List<PerformanceRule> rules = performanceRules.getOrDefault(language.toLowerCase(), List.of());
        
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;
            
            for (PerformanceRule rule : rules) {
                Matcher matcher = rule.pattern.matcher(line);
                if (matcher.find()) {
                    Suggestion suggestion = Suggestion.builder()
                            .id(rule.id + "-" + lineNumber)
                            .title(rule.title)
                            .description(rule.description)
                            .type(Suggestion.Type.PERFORMANCE)
                            .priority(rule.priority)
                            .category("Performance")
                            .example(rule.example)
                            .estimatedImpact(rule.estimatedImpact)
                            .build();
                    
                    suggestions.add(suggestion);
                    logger.debug("Performance suggestion: {} at line {}", rule.title, lineNumber);
                }
            }
        }
        
        // Add algorithmic complexity suggestions
        suggestions.addAll(analyzeAlgorithmicComplexity(code, language));
        
        // Add memory usage suggestions
        suggestions.addAll(analyzeMemoryUsage(code, language));
        
        return suggestions;
    }
    
    private List<Suggestion> analyzeAlgorithmicComplexity(String code, String language) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        // Detect nested loops (potential O(n²) or worse)
        Pattern nestedLoopPattern = Pattern.compile("for\\s*\\([^}]*\\{[^}]*for\\s*\\(", Pattern.DOTALL);
        Matcher matcher = nestedLoopPattern.matcher(code);
        
        if (matcher.find()) {
            suggestions.add(Suggestion.builder()
                    .id("nested-loops")
                    .title("Nested Loops Detected")
                    .description("Nested loops can lead to quadratic time complexity")
                    .type(Suggestion.Type.PERFORMANCE)
                    .priority(Suggestion.Priority.MEDIUM)
                    .category("Algorithmic Complexity")
                    .example("Consider using HashMap for O(1) lookups instead of nested iteration")
                    .estimatedImpact("High for large datasets")
                    .build());
        }
        
        // Detect potential inefficient string concatenation
        if (language.equals("java")) {
            Pattern stringConcatPattern = Pattern.compile("String\\s+\\w+\\s*=\\s*[^;]*\\+[^;]*;");
            Matcher stringMatcher = stringConcatPattern.matcher(code);
            
            if (stringMatcher.find()) {
                suggestions.add(Suggestion.builder()
                        .id("string-concatenation")
                        .title("Inefficient String Concatenation")
                        .description("String concatenation in loops can be inefficient")
                        .type(Suggestion.Type.PERFORMANCE)
                        .priority(Suggestion.Priority.MEDIUM)
                        .category("String Operations")
                        .example("Use StringBuilder for multiple string concatenations")
                        .estimatedImpact("Medium for multiple concatenations")
                        .build());
            }
        }
        
        return suggestions;
    }
    
    private List<Suggestion> analyzeMemoryUsage(String code, String language) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        // Detect potential memory leaks
        if (language.equals("java")) {
            // Static collections that might grow indefinitely
            Pattern staticCollectionPattern = Pattern.compile("static.*(?:List|Set|Map).*=.*new");
            Matcher matcher = staticCollectionPattern.matcher(code);
            
            if (matcher.find()) {
                suggestions.add(Suggestion.builder()
                        .id("static-collection")
                        .title("Static Collection Usage")
                        .description("Static collections can cause memory leaks if not managed properly")
                        .type(Suggestion.Type.PERFORMANCE)
                        .priority(Suggestion.Priority.HIGH)
                        .category("Memory Management")
                        .example("Consider using WeakHashMap or implement proper cleanup")
                        .estimatedImpact("High - potential memory leak")
                        .build());
            }
        }
        
        // Detect large object creation in loops
        Pattern objectCreationInLoopPattern = Pattern.compile("for\\s*\\([^}]*new\\s+\\w+", Pattern.DOTALL);
        Matcher objectMatcher = objectCreationInLoopPattern.matcher(code);
        
        if (objectMatcher.find()) {
            suggestions.add(Suggestion.builder()
                    .id("object-creation-loop")
                    .title("Object Creation in Loop")
                    .description("Creating objects inside loops can cause excessive garbage collection")
                    .type(Suggestion.Type.PERFORMANCE)
                    .priority(Suggestion.Priority.MEDIUM)
                    .category("Memory Management")
                    .example("Consider object pooling or move object creation outside the loop")
                    .estimatedImpact("Medium - increased GC pressure")
                    .build());
        }
        
        return suggestions;
    }
    
    private Map<String, List<PerformanceRule>> initializePerformanceRules() {
        Map<String, List<PerformanceRule>> rules = new HashMap<>();
        
        // Java performance rules
        rules.put("java", Arrays.asList(
                new PerformanceRule(
                        "java-arraylist-size",
                        "ArrayList Without Initial Capacity",
                        "ArrayList without initial capacity may cause multiple array resizing",
                        Pattern.compile("new\\s+ArrayList\\s*\\(\\s*\\)"),
                        Suggestion.Priority.LOW,
                        "new ArrayList<>(expectedSize)",
                        "Low - reduces array resizing overhead"
                ),
                new PerformanceRule(
                        "java-string-equals",
                        "String Comparison Order",
                        "Put string literals first in equals() to avoid NullPointerException",
                        Pattern.compile("\\w+\\.equals\\s*\\(\\s*\"[^\"]*\"\\s*\\)"),
                        Suggestion.Priority.LOW,
                        "\"literal\".equals(variable) instead of variable.equals(\"literal\")",
                        "Low - prevents NPE and slightly faster"
                ),
                new PerformanceRule(
                        "java-boxing-unboxing",
                        "Unnecessary Boxing/Unboxing",
                        "Avoid unnecessary autoboxing in performance-critical code",
                        Pattern.compile("Integer\\s+\\w+\\s*=\\s*\\d+"),
                        Suggestion.Priority.MEDIUM,
                        "Use primitive int instead of Integer wrapper",
                        "Medium - reduces object creation overhead"
                ),
                new PerformanceRule(
                        "java-stream-collect",
                        "Stream Collection Performance",
                        "Consider parallel streams for large collections",
                        Pattern.compile("\\.stream\\(\\)\\.(?:filter|map|reduce).*\\.collect"),
                        Suggestion.Priority.LOW,
                        "Use parallelStream() for CPU-intensive operations on large collections",
                        "High for large datasets with CPU-intensive operations"
                )
        ));
        
        // JavaScript performance rules
        List<PerformanceRule> jsRules = Arrays.asList(
                new PerformanceRule(
                        "js-dom-query",
                        "Repeated DOM Queries",
                        "Cache DOM element references instead of querying repeatedly",
                        Pattern.compile("document\\.(?:getElementById|querySelector)"),
                        Suggestion.Priority.MEDIUM,
                        "const element = document.getElementById('id'); // cache the reference",
                        "Medium - reduces DOM traversal overhead"
                ),
                new PerformanceRule(
                        "js-array-length",
                        "Array Length in Loop",
                        "Cache array length in loop condition for better performance",
                        Pattern.compile("for\\s*\\([^;]*;[^;]*\\.length[^;]*;"),
                        Suggestion.Priority.LOW,
                        "for (let i = 0, len = arr.length; i < len; i++)",
                        "Low - avoids repeated length property access"
                ),
                new PerformanceRule(
                        "js-string-concatenation",
                        "String Concatenation in Loop",
                        "Use array join() for multiple string concatenations",
                        Pattern.compile("for\\s*\\([^}]*\\+=.*[\"']"),
                        Suggestion.Priority.MEDIUM,
                        "Use array.push() and join() instead of string concatenation in loops",
                        "High for many concatenations"
                )
        );
        rules.put("javascript", jsRules);
        rules.put("typescript", jsRules);
        
        // Python performance rules
        rules.put("python", Arrays.asList(
                new PerformanceRule(
                        "python-list-comprehension",
                        "Use List Comprehension",
                        "List comprehensions are generally faster than equivalent for loops",
                        Pattern.compile("for\\s+\\w+\\s+in\\s+.*:\\s*\\w+\\.append\\("),
                        Suggestion.Priority.LOW,
                        "[expression for item in iterable] instead of for loop with append",
                        "Low to Medium - more Pythonic and often faster"
                ),
                new PerformanceRule(
                        "python-string-join",
                        "String Join Performance",
                        "Use join() for concatenating multiple strings",
                        Pattern.compile("\\w+\\s*\\+=\\s*\\w+"),
                        Suggestion.Priority.MEDIUM,
                        "Use ''.join(string_list) for multiple string concatenations",
                        "High for many string concatenations"
                )
        ));
        
        return rules;
    }
    
    private static class PerformanceRule {
        final String id;
        final String title;
        final String description;
        final Pattern pattern;
        final Suggestion.Priority priority;
        final String example;
        final String estimatedImpact;
        
        PerformanceRule(String id, String title, String description, Pattern pattern,
                       Suggestion.Priority priority, String example, String estimatedImpact) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.pattern = pattern;
            this.priority = priority;
            this.example = example;
            this.estimatedImpact = estimatedImpact;
        }
    }
}