package com.ailearning.core.service.impl;

import com.ailearning.core.model.CodeIssue;
import com.ailearning.core.model.Suggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code quality analyzer for detecting code smells, style issues, and best practice violations.
 * Implements language-specific quality rules and suggestions.
 */
public class QualityAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(QualityAnalyzer.class);
    
    private final Map<String, List<QualityRule>> qualityRules;
    
    public QualityAnalyzer() {
        this.qualityRules = initializeQualityRules();
    }
    
    public List<CodeIssue> analyze(String code, String language) {
        List<CodeIssue> issues = new ArrayList<>();
        
        List<QualityRule> rules = qualityRules.getOrDefault(language.toLowerCase(), List.of());
        
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;
            
            for (QualityRule rule : rules) {
                Matcher matcher = rule.pattern.matcher(line);
                if (matcher.find()) {
                    CodeIssue issue = CodeIssue.builder()
                            .id(rule.id + "-" + lineNumber)
                            .title(rule.title)
                            .description(rule.description)
                            .severity(rule.severity)
                            .category(rule.category)
                            .file("current")
                            .line(lineNumber)
                            .column(matcher.start())
                            .suggestion(rule.suggestion)
                            .build();
                    
                    issues.add(issue);
                    logger.debug("Quality issue detected: {} at line {}", rule.title, lineNumber);
                }
            }
        }
        
        // Add method-level analysis
        issues.addAll(analyzeMethodComplexity(code, language));
        issues.addAll(analyzeNamingConventions(code, language));
        
        return issues;
    }
    
    public List<Suggestion> getSuggestions(String code, String language) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        // Generate suggestions based on detected patterns
        if (language.equals("java")) {
            suggestions.addAll(getJavaSuggestions(code));
        } else if (language.equals("javascript") || language.equals("typescript")) {
            suggestions.addAll(getJavaScriptSuggestions(code));
        } else if (language.equals("python")) {
            suggestions.addAll(getPythonSuggestions(code));
        }
        
        return suggestions;
    }
    
    private List<CodeIssue> analyzeMethodComplexity(String code, String language) {
        List<CodeIssue> issues = new ArrayList<>();
        
        // Simple method complexity analysis
        String[] lines = code.split("\n");
        int methodStart = -1;
        int complexity = 0;
        String methodName = "";
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // Detect method start (simplified)
            if (isMethodDeclaration(line, language)) {
                if (methodStart != -1 && complexity > 10) {
                    // Previous method was too complex
                    issues.add(CodeIssue.builder()
                            .id("high-complexity-" + methodStart)
                            .title("High Method Complexity")
                            .description("Method '" + methodName + "' has high cyclomatic complexity: " + complexity)
                            .severity(CodeIssue.Severity.WARNING)
                            .category("Code Quality")
                            .file("current")
                            .line(methodStart)
                            .suggestion("Consider breaking this method into smaller methods")
                            .build());
                }
                
                methodStart = i + 1;
                complexity = 1;
                methodName = extractMethodName(line, language);
            }
            
            // Count complexity contributors
            if (line.contains("if ") || line.contains("else") || 
                line.contains("while ") || line.contains("for ") ||
                line.contains("switch ") || line.contains("case ") ||
                line.contains("catch ") || line.contains("&&") || line.contains("||")) {
                complexity++;
            }
        }
        
        return issues;
    }
    
    private List<CodeIssue> analyzeNamingConventions(String code, String language) {
        List<CodeIssue> issues = new ArrayList<>();
        
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;
            
            // Check variable naming conventions
            if (language.equals("java")) {
                // Java camelCase convention
                Pattern variablePattern = Pattern.compile("\\b(int|String|boolean|double|float)\\s+([A-Z][a-zA-Z0-9_]*)\\b");
                Matcher matcher = variablePattern.matcher(line);
                if (matcher.find()) {
                    issues.add(CodeIssue.builder()
                            .id("naming-convention-" + lineNumber)
                            .title("Naming Convention Violation")
                            .description("Variable '" + matcher.group(2) + "' should start with lowercase letter")
                            .severity(CodeIssue.Severity.INFO)
                            .category("Style")
                            .file("current")
                            .line(lineNumber)
                            .suggestion("Use camelCase naming convention")
                            .build());
                }
            }
        }
        
        return issues;
    }
    
    private boolean isMethodDeclaration(String line, String language) {
        return switch (language) {
            case "java" -> line.contains("public ") || line.contains("private ") || line.contains("protected ");
            case "javascript", "typescript" -> line.contains("function ") || line.matches(".*\\w+\\s*\\(.*\\)\\s*\\{.*");
            case "python" -> line.startsWith("def ");
            default -> false;
        };
    }
    
    private String extractMethodName(String line, String language) {
        return switch (language) {
            case "java" -> {
                Pattern pattern = Pattern.compile("\\b(\\w+)\\s*\\(");
                Matcher matcher = pattern.matcher(line);
                yield matcher.find() ? matcher.group(1) : "unknown";
            }
            case "javascript", "typescript" -> {
                Pattern pattern = Pattern.compile("function\\s+(\\w+)|\\b(\\w+)\\s*\\(");
                Matcher matcher = pattern.matcher(line);
                yield matcher.find() ? (matcher.group(1) != null ? matcher.group(1) : matcher.group(2)) : "unknown";
            }
            case "python" -> {
                Pattern pattern = Pattern.compile("def\\s+(\\w+)");
                Matcher matcher = pattern.matcher(line);
                yield matcher.find() ? matcher.group(1) : "unknown";
            }
            default -> "unknown";
        };
    }
    
    private List<Suggestion> getJavaSuggestions(String code) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        if (code.contains("System.out.println")) {
            suggestions.add(Suggestion.builder()
                    .id("java-logging")
                    .title("Use Proper Logging")
                    .description("Replace System.out.println with proper logging framework")
                    .type(Suggestion.Type.BEST_PRACTICE)
                    .priority(Suggestion.Priority.MEDIUM)
                    .category("Logging")
                    .example("logger.info(\"message\") instead of System.out.println(\"message\")")
                    .build());
        }
        
        if (code.contains("catch (Exception e)")) {
            suggestions.add(Suggestion.builder()
                    .id("java-exception-handling")
                    .title("Specific Exception Handling")
                    .description("Catch specific exceptions instead of generic Exception")
                    .type(Suggestion.Type.BEST_PRACTICE)
                    .priority(Suggestion.Priority.MEDIUM)
                    .category("Error Handling")
                    .example("catch (IOException e) instead of catch (Exception e)")
                    .build());
        }
        
        return suggestions;
    }
    
    private List<Suggestion> getJavaScriptSuggestions(String code) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        if (code.contains("var ")) {
            suggestions.add(Suggestion.builder()
                    .id("js-var-usage")
                    .title("Use let/const instead of var")
                    .description("Prefer let or const over var for better scoping")
                    .type(Suggestion.Type.BEST_PRACTICE)
                    .priority(Suggestion.Priority.LOW)
                    .category("Modern JavaScript")
                    .example("let variable = value; or const CONSTANT = value;")
                    .build());
        }
        
        if (code.contains("== ") || code.contains("!= ")) {
            suggestions.add(Suggestion.builder()
                    .id("js-strict-equality")
                    .title("Use Strict Equality")
                    .description("Use === and !== for strict equality comparison")
                    .type(Suggestion.Type.BEST_PRACTICE)
                    .priority(Suggestion.Priority.MEDIUM)
                    .category("Type Safety")
                    .example("if (a === b) instead of if (a == b)")
                    .build());
        }
        
        return suggestions;
    }
    
    private List<Suggestion> getPythonSuggestions(String code) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        if (code.contains("print(")) {
            suggestions.add(Suggestion.builder()
                    .id("python-logging")
                    .title("Use Logging Instead of Print")
                    .description("Use logging module for better control over output")
                    .type(Suggestion.Type.BEST_PRACTICE)
                    .priority(Suggestion.Priority.MEDIUM)
                    .category("Logging")
                    .example("logging.info('message') instead of print('message')")
                    .build());
        }
        
        return suggestions;
    }
    
    private Map<String, List<QualityRule>> initializeQualityRules() {
        Map<String, List<QualityRule>> rules = new HashMap<>();
        
        // Java quality rules
        rules.put("java", Arrays.asList(
                new QualityRule(
                        "java-long-line",
                        "Line Too Long",
                        "Line exceeds recommended length of 120 characters",
                        Pattern.compile("^.{121,}$"),
                        CodeIssue.Severity.INFO,
                        "Style",
                        "Break long lines for better readability"
                ),
                new QualityRule(
                        "java-empty-catch",
                        "Empty Catch Block",
                        "Empty catch blocks hide exceptions and make debugging difficult",
                        Pattern.compile("catch\\s*\\([^)]+\\)\\s*\\{\\s*\\}"),
                        CodeIssue.Severity.WARNING,
                        "Error Handling",
                        "Add proper exception handling or logging"
                ),
                new QualityRule(
                        "java-magic-number",
                        "Magic Number",
                        "Numeric literals should be defined as named constants",
                        Pattern.compile("\\b\\d{2,}\\b"),
                        CodeIssue.Severity.INFO,
                        "Maintainability",
                        "Define numeric literals as named constants"
                )
        ));
        
        // JavaScript quality rules
        List<QualityRule> jsRules = Arrays.asList(
                new QualityRule(
                        "js-console-log",
                        "Console Log Statement",
                        "Console.log statements should be removed from production code",
                        Pattern.compile("console\\.log\\s*\\("),
                        CodeIssue.Severity.INFO,
                        "Debugging",
                        "Remove console.log statements or use proper logging"
                ),
                new QualityRule(
                        "js-unused-var",
                        "Unused Variable Declaration",
                        "Variable declared but never used",
                        Pattern.compile("var\\s+\\w+\\s*=.*"),
                        CodeIssue.Severity.WARNING,
                        "Code Quality",
                        "Remove unused variables"
                )
        );
        rules.put("javascript", jsRules);
        rules.put("typescript", jsRules);
        
        return rules;
    }
    
    private static class QualityRule {
        final String id;
        final String title;
        final String description;
        final Pattern pattern;
        final CodeIssue.Severity severity;
        final String category;
        final String suggestion;
        
        QualityRule(String id, String title, String description, Pattern pattern,
                   CodeIssue.Severity severity, String category, String suggestion) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.pattern = pattern;
            this.severity = severity;
            this.category = category;
            this.suggestion = suggestion;
        }
    }
}