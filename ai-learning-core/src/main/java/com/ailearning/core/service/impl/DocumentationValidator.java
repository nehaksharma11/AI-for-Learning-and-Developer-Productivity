package com.ailearning.core.service.impl;

import com.ailearning.core.model.Documentation;
import com.ailearning.core.model.ValidationResult;
import com.ailearning.core.model.ValidationResult.ValidationIssue;
import com.ailearning.core.model.ValidationResult.IssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Validates documentation against code and style guidelines.
 */
class DocumentationValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentationValidator.class);
    
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_LINE_LENGTH = 120;
    private static final Pattern JAVADOC_PATTERN = Pattern.compile("/\\*\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern JSDOC_PATTERN = Pattern.compile("/\\*\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern PYTHON_DOCSTRING_PATTERN = Pattern.compile("\"\"\".*?\"\"\"", Pattern.DOTALL);
    
    /**
     * Validates documentation against the corresponding code.
     */
    public ValidationResult validate(Documentation documentation, String code, String language) {
        try {
            logger.debug("Validating documentation for element: {}", documentation.getElementName());
            
            List<ValidationIssue> issues = new ArrayList<>();
            List<String> suggestions = new ArrayList<>();
            double accuracyScore = 1.0;
            
            // Validate content quality
            issues.addAll(validateContentQuality(documentation));
            
            // Validate format compliance
            issues.addAll(validateFormatCompliance(documentation, language));
            
            // Validate accuracy against code
            ValidationResult codeValidation = validateAgainstCode(documentation, code, language);
            issues.addAll(codeValidation.getIssues());
            accuracyScore = Math.min(accuracyScore, codeValidation.getAccuracyScore());
            
            // Validate style compliance
            issues.addAll(validateStyleCompliance(documentation, language));
            
            // Generate suggestions based on issues
            suggestions.addAll(generateSuggestions(issues, documentation));
            
            // Calculate overall accuracy score
            accuracyScore = calculateAccuracyScore(issues, accuracyScore);
            
            // Determine validation status
            ValidationResult.Status status = determineStatus(issues, accuracyScore);
            
            return ValidationResult.builder()
                    .status(status)
                    .accuracyScore(accuracyScore)
                    .issues(issues)
                    .suggestions(suggestions)
                    .validatorVersion("1.0.0")
                    .build();
            
        } catch (Exception e) {
            logger.error("Error validating documentation", e);
            return ValidationResult.invalid(
                    List.of(ValidationIssue.create(IssueType.ACCURACY, 
                            ValidationIssue.Severity.HIGH, 
                            "Validation failed: " + e.getMessage())),
                    List.of("Fix validation errors and try again")
            );
        }
    }
    
    private List<ValidationIssue> validateContentQuality(Documentation documentation) {
        List<ValidationIssue> issues = new ArrayList<>();
        String content = documentation.getContent();
        
        if (content == null || content.trim().isEmpty()) {
            issues.add(ValidationIssue.create(
                    IssueType.COMPLETENESS,
                    ValidationIssue.Severity.HIGH,
                    "Documentation content is empty"
            ));
            return issues;
        }
        
        // Check minimum description length
        String cleanContent = stripDocumentationSyntax(content);
        if (cleanContent.length() < MIN_DESCRIPTION_LENGTH) {
            issues.add(ValidationIssue.create(
                    IssueType.COMPLETENESS,
                    ValidationIssue.Severity.MEDIUM,
                    "Description is too short (minimum " + MIN_DESCRIPTION_LENGTH + " characters)",
                    null,
                    "Provide a more detailed description"
            ));
        }
        
        // Check for placeholder text
        if (containsPlaceholderText(cleanContent)) {
            issues.add(ValidationIssue.create(
                    IssueType.COMPLETENESS,
                    ValidationIssue.Severity.HIGH,
                    "Documentation contains placeholder text",
                    null,
                    "Replace placeholder text with actual descriptions"
            ));
        }
        
        // Check for spelling and grammar (basic)
        issues.addAll(validateSpellingAndGrammar(cleanContent));
        
        return issues;
    }
    
    private List<ValidationIssue> validateFormatCompliance(Documentation documentation, String language) {
        List<ValidationIssue> issues = new ArrayList<>();
        String content = documentation.getContent();
        
        switch (documentation.getType()) {
            case JAVADOC -> issues.addAll(validateJavadocFormat(content));
            case JSDOC -> issues.addAll(validateJSDocFormat(content));
            case PYTHON_DOCSTRING -> issues.addAll(validatePythonDocstringFormat(content));
            case MARKDOWN -> issues.addAll(validateMarkdownFormat(content));
        }
        
        // Check line length
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length() > MAX_LINE_LENGTH) {
                issues.add(ValidationIssue.create(
                        IssueType.STYLE,
                        ValidationIssue.Severity.LOW,
                        "Line " + (i + 1) + " exceeds maximum length (" + MAX_LINE_LENGTH + " characters)",
                        "Line " + (i + 1),
                        "Break long lines into multiple lines"
                ));
            }
        }
        
        return issues;
    }
    
    private ValidationResult validateAgainstCode(Documentation documentation, String code, String language) {
        List<ValidationIssue> issues = new ArrayList<>();
        double accuracyScore = 1.0;
        
        if (code == null || code.trim().isEmpty()) {
            return ValidationResult.builder()
                    .status(ValidationResult.Status.WARNING)
                    .accuracyScore(0.5)
                    .issues(List.of(ValidationIssue.create(
                            IssueType.ACCURACY,
                            ValidationIssue.Severity.MEDIUM,
                            "No code provided for validation"
                    )))
                    .build();
        }
        
        // Check if element exists in code
        String elementName = documentation.getElementName();
        if (elementName != null && !code.contains(elementName)) {
            issues.add(ValidationIssue.create(
                    IssueType.ACCURACY,
                    ValidationIssue.Severity.HIGH,
                    "Documented element '" + elementName + "' not found in code",
                    null,
                    "Verify element name or update documentation"
            ));
            accuracyScore -= 0.3;
        }
        
        // Validate parameter documentation against code
        if (documentation.getElementType() != null && 
            (documentation.getElementType().equals("method") || documentation.getElementType().equals("function"))) {
            ValidationResult paramValidation = validateParameterDocumentation(documentation, code, language);
            issues.addAll(paramValidation.getIssues());
            accuracyScore = Math.min(accuracyScore, paramValidation.getAccuracyScore());
        }
        
        return ValidationResult.builder()
                .status(issues.isEmpty() ? ValidationResult.Status.VALID : ValidationResult.Status.WARNING)
                .accuracyScore(Math.max(0.0, accuracyScore))
                .issues(issues)
                .build();
    }
    
    private List<ValidationIssue> validateStyleCompliance(Documentation documentation, String language) {
        List<ValidationIssue> issues = new ArrayList<>();
        String content = documentation.getContent();
        
        // Language-specific style checks
        switch (language.toLowerCase()) {
            case "java" -> issues.addAll(validateJavaStyleCompliance(content));
            case "javascript", "typescript" -> issues.addAll(validateJavaScriptStyleCompliance(content));
            case "python" -> issues.addAll(validatePythonStyleCompliance(content));
        }
        
        // General style checks
        if (!startsWithCapitalLetter(stripDocumentationSyntax(content))) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.LOW,
                    "Description should start with a capital letter",
                    null,
                    "Capitalize the first letter of the description"
            ));
        }
        
        return issues;
    }
    
    private List<String> generateSuggestions(List<ValidationIssue> issues, Documentation documentation) {
        List<String> suggestions = new ArrayList<>();
        
        // Generate suggestions based on issue types
        Map<IssueType, Long> issueTypeCounts = issues.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ValidationIssue::getType,
                        java.util.stream.Collectors.counting()
                ));
        
        if (issueTypeCounts.getOrDefault(IssueType.COMPLETENESS, 0L) > 0) {
            suggestions.add("Add more detailed descriptions and complete missing sections");
        }
        
        if (issueTypeCounts.getOrDefault(IssueType.ACCURACY, 0L) > 0) {
            suggestions.add("Review documentation against the actual code implementation");
        }
        
        if (issueTypeCounts.getOrDefault(IssueType.STYLE, 0L) > 0) {
            suggestions.add("Follow the style guide for " + documentation.getType() + " documentation");
        }
        
        if (issueTypeCounts.getOrDefault(IssueType.CONSISTENCY, 0L) > 0) {
            suggestions.add("Ensure consistency with other documentation in the project");
        }
        
        return suggestions;
    }
    
    private double calculateAccuracyScore(List<ValidationIssue> issues, double baseScore) {
        double penalty = 0.0;
        
        for (ValidationIssue issue : issues) {
            switch (issue.getSeverity()) {
                case HIGH -> penalty += 0.2;
                case MEDIUM -> penalty += 0.1;
                case LOW -> penalty += 0.05;
            }
        }
        
        return Math.max(0.0, Math.min(1.0, baseScore - penalty));
    }
    
    private ValidationResult.Status determineStatus(List<ValidationIssue> issues, double accuracyScore) {
        boolean hasCriticalIssues = issues.stream()
                .anyMatch(issue -> issue.getSeverity() == ValidationIssue.Severity.HIGH);
        
        if (hasCriticalIssues) {
            return ValidationResult.Status.INVALID;
        } else if (!issues.isEmpty()) {
            return ValidationResult.Status.WARNING;
        } else if (accuracyScore < 0.8) {
            return ValidationResult.Status.NEEDS_UPDATE;
        } else {
            return ValidationResult.Status.VALID;
        }
    }
    
    // Helper methods for specific validation checks
    
    private String stripDocumentationSyntax(String content) {
        String cleaned = content;
        
        // Remove Javadoc/JSDoc syntax
        cleaned = cleaned.replaceAll("/\\*\\*|\\*/|\\*", "");
        
        // Remove Python docstring syntax
        cleaned = cleaned.replaceAll("\"\"\"", "");
        
        // Remove common prefixes and clean up
        cleaned = cleaned.replaceAll("^\\s*[*\\-]\\s*", "");
        cleaned = cleaned.replaceAll("\\s+", " ");
        
        return cleaned.trim();
    }
    
    private boolean containsPlaceholderText(String content) {
        String lowerContent = content.toLowerCase();
        return lowerContent.contains("todo") ||
               lowerContent.contains("fixme") ||
               lowerContent.contains("placeholder") ||
               lowerContent.contains("auto-generated") ||
               lowerContent.contains("description here");
    }
    
    private List<ValidationIssue> validateSpellingAndGrammar(String content) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Basic grammar checks
        if (content.contains("  ")) { // Double spaces
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.LOW,
                    "Contains multiple consecutive spaces",
                    null,
                    "Remove extra spaces"
            ));
        }
        
        // Check for common typos (basic implementation)
        String[] commonTypos = {"teh", "adn", "recieve", "seperate"};
        for (String typo : commonTypos) {
            if (content.toLowerCase().contains(typo)) {
                issues.add(ValidationIssue.create(
                        IssueType.STYLE,
                        ValidationIssue.Severity.LOW,
                        "Possible typo: " + typo,
                        null,
                        "Check spelling"
                ));
            }
        }
        
        return issues;
    }
    
    private List<ValidationIssue> validateJavadocFormat(String content) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        if (!content.startsWith("/**") || !content.endsWith("*/")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.MEDIUM,
                    "Invalid Javadoc format - must start with /** and end with */",
                    null,
                    "Use proper Javadoc comment syntax"
            ));
        }
        
        // Check for required tags
        if (content.contains("@param") && !content.matches(".*@param\\s+\\w+\\s+.+")) {
            issues.add(ValidationIssue.create(
                    IssueType.COMPLETENESS,
                    ValidationIssue.Severity.MEDIUM,
                    "@param tag is missing parameter description",
                    null,
                    "Add descriptions for all @param tags"
            ));
        }
        
        return issues;
    }
    
    private List<ValidationIssue> validateJSDocFormat(String content) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        if (!content.startsWith("/**") || !content.endsWith("*/")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.MEDIUM,
                    "Invalid JSDoc format - must start with /** and end with */",
                    null,
                    "Use proper JSDoc comment syntax"
            ));
        }
        
        return issues;
    }
    
    private List<ValidationIssue> validatePythonDocstringFormat(String content) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        if (!content.startsWith("\"\"\"") || !content.endsWith("\"\"\"")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.MEDIUM,
                    "Invalid Python docstring format - must be enclosed in triple quotes",
                    null,
                    "Use proper Python docstring syntax"
            ));
        }
        
        return issues;
    }
    
    private List<ValidationIssue> validateMarkdownFormat(String content) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Basic markdown validation
        if (content.contains("#") && !content.matches(".*#+\\s+.+")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.LOW,
                    "Markdown headers should have space after #",
                    null,
                    "Add space after # in headers"
            ));
        }
        
        return issues;
    }
    
    private ValidationResult validateParameterDocumentation(Documentation documentation, String code, String language) {
        List<ValidationIssue> issues = new ArrayList<>();
        double accuracyScore = 1.0;
        
        // Extract parameters from code (simplified)
        Set<String> codeParameters = extractParametersFromCode(code, language);
        Set<String> docParameters = extractParametersFromDocumentation(documentation.getContent());
        
        // Check for missing parameter documentation
        for (String param : codeParameters) {
            if (!docParameters.contains(param)) {
                issues.add(ValidationIssue.create(
                        IssueType.COMPLETENESS,
                        ValidationIssue.Severity.MEDIUM,
                        "Parameter '" + param + "' is not documented",
                        null,
                        "Add documentation for parameter " + param
                ));
                accuracyScore -= 0.1;
            }
        }
        
        // Check for extra parameter documentation
        for (String param : docParameters) {
            if (!codeParameters.contains(param)) {
                issues.add(ValidationIssue.create(
                        IssueType.ACCURACY,
                        ValidationIssue.Severity.MEDIUM,
                        "Documented parameter '" + param + "' not found in code",
                        null,
                        "Remove documentation for non-existent parameter " + param
                ));
                accuracyScore -= 0.1;
            }
        }
        
        return ValidationResult.builder()
                .status(issues.isEmpty() ? ValidationResult.Status.VALID : ValidationResult.Status.WARNING)
                .accuracyScore(Math.max(0.0, accuracyScore))
                .issues(issues)
                .build();
    }
    
    private Set<String> extractParametersFromCode(String code, String language) {
        Set<String> parameters = new HashSet<>();
        
        // Simplified parameter extraction - in a real implementation, this would use AST parsing
        Pattern paramPattern = switch (language.toLowerCase()) {
            case "java" -> Pattern.compile("\\((.*?)\\)");
            case "javascript", "typescript" -> Pattern.compile("\\((.*?)\\)");
            case "python" -> Pattern.compile("def\\s+\\w+\\s*\\((.*?)\\)");
            default -> Pattern.compile("\\((.*?)\\)");
        };
        
        java.util.regex.Matcher matcher = paramPattern.matcher(code);
        if (matcher.find()) {
            String paramString = matcher.group(1);
            if (!paramString.trim().isEmpty()) {
                String[] params = paramString.split(",");
                for (String param : params) {
                    String paramName = param.trim().split("\\s+")[0];
                    if (!paramName.isEmpty()) {
                        parameters.add(paramName);
                    }
                }
            }
        }
        
        return parameters;
    }
    
    private Set<String> extractParametersFromDocumentation(String content) {
        Set<String> parameters = new HashSet<>();
        
        // Extract @param tags
        Pattern paramPattern = Pattern.compile("@param\\s+(\\w+)");
        java.util.regex.Matcher matcher = paramPattern.matcher(content);
        while (matcher.find()) {
            parameters.add(matcher.group(1));
        }
        
        // Extract Python-style parameter documentation
        Pattern pythonParamPattern = Pattern.compile("Args:\\s*\\n\\s*(\\w+):");
        matcher = pythonParamPattern.matcher(content);
        while (matcher.find()) {
            parameters.add(matcher.group(1));
        }
        
        return parameters;
    }
    
    private List<ValidationIssue> validateJavaStyleCompliance(String content) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Check for proper sentence structure
        if (!content.matches(".*[.!?]\\s*\\*/\\s*$") && content.contains("*/")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.LOW,
                    "Javadoc should end with proper punctuation",
                    null,
                    "End description with a period"
            ));
        }
        
        return issues;
    }
    
    private List<ValidationIssue> validateJavaScriptStyleCompliance(String content) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Similar to Java style compliance
        return validateJavaStyleCompliance(content);
    }
    
    private List<ValidationIssue> validatePythonStyleCompliance(String content) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Check PEP 257 compliance
        if (content.startsWith("\"\"\"") && !content.matches("\"\"\"[A-Z].*")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.LOW,
                    "Python docstring should start with a capital letter",
                    null,
                    "Capitalize the first letter of the docstring"
            ));
        }
        
        return issues;
    }
    
    private boolean startsWithCapitalLetter(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        return Character.isUpperCase(content.charAt(0));
    }
}