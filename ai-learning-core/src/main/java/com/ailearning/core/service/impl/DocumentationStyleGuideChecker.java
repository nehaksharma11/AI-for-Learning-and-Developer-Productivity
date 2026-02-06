package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.model.ValidationResult.ValidationIssue;
import com.ailearning.core.model.ValidationResult.IssueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Checks documentation compliance with style guides and project conventions.
 */
public class DocumentationStyleGuideChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentationStyleGuideChecker.class);
    
    private final Map<String, StyleGuideRules> languageRules;
    private final ProjectStyleGuide projectStyleGuide;
    
    public DocumentationStyleGuideChecker() {
        this.languageRules = initializeLanguageRules();
        this.projectStyleGuide = new ProjectStyleGuide();
    }
    
    public DocumentationStyleGuideChecker(ProjectStyleGuide projectStyleGuide) {
        this.languageRules = initializeLanguageRules();
        this.projectStyleGuide = projectStyleGuide != null ? projectStyleGuide : new ProjectStyleGuide();
    }
    
    /**
     * Checks documentation compliance with style guides.
     */
    public StyleGuideComplianceResult checkCompliance(Documentation documentation, 
                                                     String language, 
                                                     ProjectContext projectContext) {
        try {
            logger.debug("Checking style guide compliance for documentation: {}", documentation.getId());
            
            List<ValidationIssue> issues = new ArrayList<>();
            List<String> suggestions = new ArrayList<>();
            
            // Check language-specific rules
            StyleGuideRules rules = languageRules.get(language.toLowerCase());
            if (rules != null) {
                issues.addAll(checkLanguageSpecificRules(documentation, rules));
            }
            
            // Check project-specific conventions
            issues.addAll(checkProjectConventions(documentation, projectContext));
            
            // Check general documentation standards
            issues.addAll(checkGeneralStandards(documentation));
            
            // Generate improvement suggestions
            suggestions.addAll(generateImprovementSuggestions(issues, documentation));
            
            // Calculate compliance score
            double complianceScore = calculateComplianceScore(issues, documentation);
            
            return new StyleGuideComplianceResult(
                    complianceScore,
                    issues,
                    suggestions,
                    determineComplianceLevel(complianceScore, issues)
            );
            
        } catch (Exception e) {
            logger.error("Error checking style guide compliance", e);
            return StyleGuideComplianceResult.error("Style guide check failed: " + e.getMessage());
        }
    }
    
    /**
     * Learns project-specific style conventions from existing documentation.
     */
    public void learnProjectConventions(List<Documentation> existingDocs, ProjectContext projectContext) {
        try {
            logger.debug("Learning project conventions from {} documents", existingDocs.size());
            
            // Analyze existing documentation patterns
            Map<String, Integer> commonPhrases = new HashMap<>();
            Map<String, Integer> formatPatterns = new HashMap<>();
            Set<String> commonTags = new HashSet<>();
            
            for (Documentation doc : existingDocs) {
                analyzeDocumentationPatterns(doc, commonPhrases, formatPatterns, commonTags);
            }
            
            // Update project style guide
            projectStyleGuide.updateConventions(commonPhrases, formatPatterns, commonTags);
            
            logger.debug("Learned {} common phrases, {} format patterns, {} tags", 
                        commonPhrases.size(), formatPatterns.size(), commonTags.size());
            
        } catch (Exception e) {
            logger.error("Error learning project conventions", e);
        }
    }
    
    /**
     * Validates documentation against established project conventions.
     */
    public ValidationResult validateAgainstConventions(Documentation documentation, 
                                                      List<Documentation> existingDocs) {
        try {
            List<ValidationIssue> issues = new ArrayList<>();
            
            // Check consistency with existing documentation
            issues.addAll(checkConsistencyWithExisting(documentation, existingDocs));
            
            // Check adherence to learned conventions
            issues.addAll(checkConventionAdherence(documentation));
            
            double accuracyScore = calculateConsistencyScore(issues);
            ValidationResult.Status status = issues.isEmpty() ? 
                    ValidationResult.Status.VALID : ValidationResult.Status.WARNING;
            
            return ValidationResult.builder()
                    .status(status)
                    .accuracyScore(accuracyScore)
                    .issues(issues)
                    .suggestions(generateConsistencySuggestions(issues))
                    .build();
            
        } catch (Exception e) {
            logger.error("Error validating against conventions", e);
            return ValidationResult.invalid(
                    List.of(ValidationIssue.create(IssueType.CONSISTENCY, 
                            ValidationIssue.Severity.HIGH, 
                            "Convention validation failed: " + e.getMessage())),
                    List.of("Fix validation errors and try again")
            );
        }
    }
    
    // Private helper methods
    
    private Map<String, StyleGuideRules> initializeLanguageRules() {
        Map<String, StyleGuideRules> rules = new HashMap<>();
        
        // Java/Javadoc rules
        StyleGuideRules javaRules = new StyleGuideRules();
        javaRules.addRule("javadoc_format", "Must start with /** and end with */");
        javaRules.addRule("first_sentence", "First sentence should be a complete sentence ending with period");
        javaRules.addRule("param_tags", "@param tags must have parameter name and description");
        javaRules.addRule("return_tag", "@return tag should describe what is returned");
        javaRules.addRule("throws_tag", "@throws tags should document checked exceptions");
        javaRules.addRule("author_tag", "@author tag should be present for public classes");
        javaRules.addRule("since_tag", "@since tag should indicate version when element was added");
        rules.put("java", javaRules);
        
        // JavaScript/JSDoc rules
        StyleGuideRules jsRules = new StyleGuideRules();
        jsRules.addRule("jsdoc_format", "Must start with /** and end with */");
        jsRules.addRule("description", "Should have a clear description");
        jsRules.addRule("param_types", "@param should include type information");
        jsRules.addRule("returns_tag", "@returns should describe return value and type");
        jsRules.addRule("example_tag", "@example should be provided for complex functions");
        rules.put("javascript", jsRules);
        rules.put("typescript", jsRules);
        
        // Python docstring rules
        StyleGuideRules pythonRules = new StyleGuideRules();
        pythonRules.addRule("docstring_format", "Should use triple quotes");
        pythonRules.addRule("pep257_compliance", "Should follow PEP 257 conventions");
        pythonRules.addRule("args_section", "Should have Args section for parameters");
        pythonRules.addRule("returns_section", "Should have Returns section for return values");
        pythonRules.addRule("raises_section", "Should have Raises section for exceptions");
        pythonRules.addRule("examples_section", "Should have Examples section for complex functions");
        rules.put("python", pythonRules);
        
        return rules;
    }
    
    private List<ValidationIssue> checkLanguageSpecificRules(Documentation documentation, 
                                                            StyleGuideRules rules) {
        List<ValidationIssue> issues = new ArrayList<>();
        String content = documentation.getContent();
        
        switch (documentation.getType()) {
            case JAVADOC -> issues.addAll(checkJavadocRules(content, rules));
            case JSDOC -> issues.addAll(checkJSDocRules(content, rules));
            case PYTHON_DOCSTRING -> issues.addAll(checkPythonDocstringRules(content, rules));
            case MARKDOWN -> issues.addAll(checkMarkdownRules(content, rules));
        }
        
        return issues;
    }
    
    private List<ValidationIssue> checkJavadocRules(String content, StyleGuideRules rules) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Check format
        if (!content.startsWith("/**") || !content.endsWith("*/")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.HIGH,
                    "Javadoc must start with /** and end with */",
                    null,
                    "Use proper Javadoc comment format"
            ));
        }
        
        // Check first sentence
        String firstSentence = extractFirstSentence(content);
        if (firstSentence != null && !firstSentence.endsWith(".")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.MEDIUM,
                    "First sentence should end with a period",
                    null,
                    "Add period at the end of the first sentence"
            ));
        }
        
        // Check @param tags
        if (content.contains("@param")) {
            Pattern paramPattern = Pattern.compile("@param\\s+(\\w+)\\s*(.*)");
            java.util.regex.Matcher matcher = paramPattern.matcher(content);
            while (matcher.find()) {
                String description = matcher.group(2).trim();
                if (description.isEmpty()) {
                    issues.add(ValidationIssue.create(
                            IssueType.COMPLETENESS,
                            ValidationIssue.Severity.MEDIUM,
                            "@param tag missing description for parameter: " + matcher.group(1),
                            null,
                            "Add description for @param " + matcher.group(1)
                    ));
                }
            }
        }
        
        // Check @return tag
        if (content.contains("@return") && !content.matches(".*@return\\s+.+")) {
            issues.add(ValidationIssue.create(
                    IssueType.COMPLETENESS,
                    ValidationIssue.Severity.MEDIUM,
                    "@return tag is missing description",
                    null,
                    "Add description for @return tag"
            ));
        }
        
        return issues;
    }
    
    private List<ValidationIssue> checkJSDocRules(String content, StyleGuideRules rules) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Check format
        if (!content.startsWith("/**") || !content.endsWith("*/")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.HIGH,
                    "JSDoc must start with /** and end with */",
                    null,
                    "Use proper JSDoc comment format"
            ));
        }
        
        // Check @param type information
        if (content.contains("@param")) {
            Pattern paramPattern = Pattern.compile("@param\\s+(?:\\{([^}]+)\\})?\\s*(\\w+)");
            java.util.regex.Matcher matcher = paramPattern.matcher(content);
            while (matcher.find()) {
                String type = matcher.group(1);
                if (type == null || type.trim().isEmpty()) {
                    issues.add(ValidationIssue.create(
                            IssueType.COMPLETENESS,
                            ValidationIssue.Severity.LOW,
                            "@param should include type information: " + matcher.group(2),
                            null,
                            "Add type information: @param {type} " + matcher.group(2)
                    ));
                }
            }
        }
        
        return issues;
    }
    
    private List<ValidationIssue> checkPythonDocstringRules(String content, StyleGuideRules rules) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Check format
        if (!content.startsWith("\"\"\"") || !content.endsWith("\"\"\"")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.HIGH,
                    "Python docstring must use triple quotes",
                    null,
                    "Use \"\"\" for docstring format"
            ));
        }
        
        // Check PEP 257 compliance - first line should be summary
        String[] lines = content.split("\n");
        if (lines.length > 1) {
            String firstLine = lines[0].replace("\"\"\"", "").trim();
            if (firstLine.isEmpty()) {
                issues.add(ValidationIssue.create(
                        IssueType.STYLE,
                        ValidationIssue.Severity.MEDIUM,
                        "First line should contain a brief summary (PEP 257)",
                        null,
                        "Add a brief summary on the first line"
                ));
            }
        }
        
        // Check for Args section if parameters are mentioned
        if (content.toLowerCase().contains("param") && !content.contains("Args:")) {
            issues.add(ValidationIssue.create(
                    IssueType.COMPLETENESS,
                    ValidationIssue.Severity.MEDIUM,
                    "Should have 'Args:' section for parameters",
                    null,
                    "Add 'Args:' section to document parameters"
            ));
        }
        
        return issues;
    }
    
    private List<ValidationIssue> checkMarkdownRules(String content, StyleGuideRules rules) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Check header formatting
        if (content.contains("#") && !content.matches(".*#+\\s+.+")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.LOW,
                    "Markdown headers should have space after #",
                    null,
                    "Add space after # in headers"
            ));
        }
        
        // Check for proper list formatting
        if (content.contains("-") && content.matches(".*-\\w+.*")) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.LOW,
                    "List items should have space after dash",
                    null,
                    "Add space after - in list items"
            ));
        }
        
        return issues;
    }
    
    private List<ValidationIssue> checkProjectConventions(Documentation documentation, 
                                                         ProjectContext projectContext) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Check naming conventions
        if (projectContext != null) {
            List<CodingConvention> conventions = projectContext.getConventions();
            for (CodingConvention convention : conventions) {
                if (convention.getType().equals("documentation")) {
                    issues.addAll(checkConventionCompliance(documentation, convention));
                }
            }
        }
        
        return issues;
    }
    
    private List<ValidationIssue> checkConventionCompliance(Documentation documentation, 
                                                           CodingConvention convention) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        String content = documentation.getContent();
        String rule = convention.getRule();
        
        // Simple rule checking - in practice would be more sophisticated
        if (rule.contains("must_include") && !content.contains(convention.getExample())) {
            issues.add(ValidationIssue.create(
                    IssueType.CONSISTENCY,
                    ValidationIssue.Severity.MEDIUM,
                    "Documentation should follow project convention: " + convention.getDescription(),
                    null,
                    "Follow project convention: " + convention.getExample()
            ));
        }
        
        return issues;
    }
    
    private List<ValidationIssue> checkGeneralStandards(Documentation documentation) {
        List<ValidationIssue> issues = new ArrayList<>();
        String content = documentation.getContent();
        
        // Check minimum length
        String cleanContent = content.replaceAll("[*/\"\\s]", "");
        if (cleanContent.length() < 10) {
            issues.add(ValidationIssue.create(
                    IssueType.COMPLETENESS,
                    ValidationIssue.Severity.MEDIUM,
                    "Documentation is too brief",
                    null,
                    "Provide more detailed description"
            ));
        }
        
        // Check for proper capitalization
        String firstChar = cleanContent.isEmpty() ? "" : cleanContent.substring(0, 1);
        if (!firstChar.isEmpty() && !firstChar.equals(firstChar.toUpperCase())) {
            issues.add(ValidationIssue.create(
                    IssueType.STYLE,
                    ValidationIssue.Severity.LOW,
                    "Documentation should start with capital letter",
                    null,
                    "Capitalize the first letter"
            ));
        }
        
        return issues;
    }
    
    private List<String> generateImprovementSuggestions(List<ValidationIssue> issues, 
                                                       Documentation documentation) {
        List<String> suggestions = new ArrayList<>();
        
        Map<IssueType, Long> issueTypeCounts = issues.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ValidationIssue::getType,
                        java.util.stream.Collectors.counting()
                ));
        
        if (issueTypeCounts.getOrDefault(IssueType.STYLE, 0L) > 0) {
            suggestions.add("Review and fix style guide violations");
        }
        
        if (issueTypeCounts.getOrDefault(IssueType.COMPLETENESS, 0L) > 0) {
            suggestions.add("Add missing documentation sections and details");
        }
        
        if (issueTypeCounts.getOrDefault(IssueType.CONSISTENCY, 0L) > 0) {
            suggestions.add("Ensure consistency with project conventions");
        }
        
        return suggestions;
    }
    
    private double calculateComplianceScore(List<ValidationIssue> issues, Documentation documentation) {
        if (issues.isEmpty()) return 1.0;
        
        double penalty = 0.0;
        for (ValidationIssue issue : issues) {
            switch (issue.getSeverity()) {
                case HIGH -> penalty += 0.3;
                case MEDIUM -> penalty += 0.2;
                case LOW -> penalty += 0.1;
            }
        }
        
        return Math.max(0.0, 1.0 - (penalty / 2.0));
    }
    
    private StyleGuideComplianceResult.ComplianceLevel determineComplianceLevel(double score, 
                                                                               List<ValidationIssue> issues) {
        boolean hasCriticalIssues = issues.stream()
                .anyMatch(issue -> issue.getSeverity() == ValidationIssue.Severity.HIGH);
        
        if (hasCriticalIssues) {
            return StyleGuideComplianceResult.ComplianceLevel.NON_COMPLIANT;
        } else if (score >= 0.9) {
            return StyleGuideComplianceResult.ComplianceLevel.FULLY_COMPLIANT;
        } else if (score >= 0.7) {
            return StyleGuideComplianceResult.ComplianceLevel.MOSTLY_COMPLIANT;
        } else {
            return StyleGuideComplianceResult.ComplianceLevel.PARTIALLY_COMPLIANT;
        }
    }
    
    private void analyzeDocumentationPatterns(Documentation doc, 
                                            Map<String, Integer> commonPhrases,
                                            Map<String, Integer> formatPatterns,
                                            Set<String> commonTags) {
        String content = doc.getContent();
        if (content == null) return;
        
        // Extract common phrases (simplified)
        String[] words = content.toLowerCase().split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            String phrase = words[i] + " " + words[i + 1];
            commonPhrases.merge(phrase, 1, Integer::sum);
        }
        
        // Extract format patterns
        if (content.startsWith("/**")) {
            formatPatterns.merge("javadoc_format", 1, Integer::sum);
        }
        if (content.contains("@param")) {
            formatPatterns.merge("param_tags", 1, Integer::sum);
        }
        if (content.contains("@return")) {
            formatPatterns.merge("return_tags", 1, Integer::sum);
        }
        
        // Extract tags
        if (doc.getTags() != null) {
            commonTags.addAll(doc.getTags());
        }
    }
    
    private List<ValidationIssue> checkConsistencyWithExisting(Documentation documentation, 
                                                              List<Documentation> existingDocs) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        if (existingDocs.isEmpty()) return issues;
        
        // Check format consistency
        Documentation.Type currentType = documentation.getType();
        long sameTypeCount = existingDocs.stream()
                .filter(doc -> doc.getType() == currentType)
                .count();
        
        if (sameTypeCount > 0) {
            // Check if current documentation follows the same patterns as existing ones
            // This is a simplified check - in practice would be more sophisticated
            String currentContent = documentation.getContent();
            boolean followsPattern = existingDocs.stream()
                    .filter(doc -> doc.getType() == currentType)
                    .anyMatch(doc -> hasSimilarStructure(currentContent, doc.getContent()));
            
            if (!followsPattern) {
                issues.add(ValidationIssue.create(
                        IssueType.CONSISTENCY,
                        ValidationIssue.Severity.MEDIUM,
                        "Documentation structure differs from existing " + currentType + " documentation",
                        null,
                        "Follow the same structure as existing documentation"
                ));
            }
        }
        
        return issues;
    }
    
    private boolean hasSimilarStructure(String content1, String content2) {
        if (content1 == null || content2 == null) return false;
        
        // Simple structure similarity check
        boolean both1HasParam = content1.contains("@param") && content2.contains("@param");
        boolean both1HasReturn = content1.contains("@return") && content2.contains("@return");
        boolean neither1HasParam = !content1.contains("@param") && !content2.contains("@param");
        boolean neither1HasReturn = !content1.contains("@return") && !content2.contains("@return");
        
        return (both1HasParam || neither1HasParam) && (both1HasReturn || neither1HasReturn);
    }
    
    private List<ValidationIssue> checkConventionAdherence(Documentation documentation) {
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Check against learned project conventions
        String content = documentation.getContent();
        
        // Check common phrase usage
        Map<String, Integer> learnedPhrases = projectStyleGuide.getCommonPhrases();
        if (!learnedPhrases.isEmpty()) {
            boolean usesCommonPhrases = learnedPhrases.keySet().stream()
                    .anyMatch(phrase -> content.toLowerCase().contains(phrase));
            
            if (!usesCommonPhrases) {
                issues.add(ValidationIssue.create(
                        IssueType.CONSISTENCY,
                        ValidationIssue.Severity.LOW,
                        "Documentation doesn't use common project terminology",
                        null,
                        "Consider using established project terminology"
                ));
            }
        }
        
        return issues;
    }
    
    private double calculateConsistencyScore(List<ValidationIssue> issues) {
        if (issues.isEmpty()) return 1.0;
        
        double penalty = issues.size() * 0.1;
        return Math.max(0.0, 1.0 - penalty);
    }
    
    private List<String> generateConsistencySuggestions(List<ValidationIssue> issues) {
        List<String> suggestions = new ArrayList<>();
        
        if (!issues.isEmpty()) {
            suggestions.add("Review existing documentation for consistency patterns");
            suggestions.add("Follow established project conventions and terminology");
        }
        
        return suggestions;
    }
    
    private String extractFirstSentence(String content) {
        if (content == null) return null;
        
        String cleaned = content.replaceAll("/\\*\\*|\\*/|\\*", "").trim();
        int periodIndex = cleaned.indexOf('.');
        if (periodIndex > 0) {
            return cleaned.substring(0, periodIndex + 1).trim();
        }
        
        return cleaned;
    }
    
    // Supporting classes
    
    private static class StyleGuideRules {
        private final Map<String, String> rules = new HashMap<>();
        
        public void addRule(String name, String description) {
            rules.put(name, description);
        }
        
        public Map<String, String> getRules() {
            return Collections.unmodifiableMap(rules);
        }
    }
    
    private static class ProjectStyleGuide {
        private Map<String, Integer> commonPhrases = new HashMap<>();
        private Map<String, Integer> formatPatterns = new HashMap<>();
        private Set<String> commonTags = new HashSet<>();
        
        public void updateConventions(Map<String, Integer> phrases, 
                                    Map<String, Integer> patterns, 
                                    Set<String> tags) {
            this.commonPhrases = new HashMap<>(phrases);
            this.formatPatterns = new HashMap<>(patterns);
            this.commonTags = new HashSet<>(tags);
        }
        
        public Map<String, Integer> getCommonPhrases() {
            return Collections.unmodifiableMap(commonPhrases);
        }
        
        public Map<String, Integer> getFormatPatterns() {
            return Collections.unmodifiableMap(formatPatterns);
        }
        
        public Set<String> getCommonTags() {
            return Collections.unmodifiableSet(commonTags);
        }
    }
    
    /**
     * Result of style guide compliance checking.
     */
    public static class StyleGuideComplianceResult {
        
        public enum ComplianceLevel {
            FULLY_COMPLIANT, MOSTLY_COMPLIANT, PARTIALLY_COMPLIANT, NON_COMPLIANT
        }
        
        private final double complianceScore;
        private final List<ValidationIssue> issues;
        private final List<String> suggestions;
        private final ComplianceLevel complianceLevel;
        private final String errorMessage;
        
        public StyleGuideComplianceResult(double complianceScore, List<ValidationIssue> issues,
                                        List<String> suggestions, ComplianceLevel complianceLevel) {
            this.complianceScore = complianceScore;
            this.issues = issues != null ? List.copyOf(issues) : List.of();
            this.suggestions = suggestions != null ? List.copyOf(suggestions) : List.of();
            this.complianceLevel = complianceLevel;
            this.errorMessage = null;
        }
        
        private StyleGuideComplianceResult(String errorMessage) {
            this.complianceScore = 0.0;
            this.issues = List.of();
            this.suggestions = List.of();
            this.complianceLevel = ComplianceLevel.NON_COMPLIANT;
            this.errorMessage = errorMessage;
        }
        
        public static StyleGuideComplianceResult error(String message) {
            return new StyleGuideComplianceResult(message);
        }
        
        public boolean isCompliant() {
            return complianceLevel == ComplianceLevel.FULLY_COMPLIANT ||
                   complianceLevel == ComplianceLevel.MOSTLY_COMPLIANT;
        }
        
        public boolean hasErrors() {
            return errorMessage != null;
        }
        
        // Getters
        public double getComplianceScore() { return complianceScore; }
        public List<ValidationIssue> getIssues() { return issues; }
        public List<String> getSuggestions() { return suggestions; }
        public ComplianceLevel getComplianceLevel() { return complianceLevel; }
        public String getErrorMessage() { return errorMessage; }
        
        @Override
        public String toString() {
            if (hasErrors()) {
                return "StyleGuideComplianceResult{error='" + errorMessage + "'}";
            }
            return String.format("StyleGuideComplianceResult{score=%.2f, level=%s, issues=%d}", 
                    complianceScore, complianceLevel, issues.size());
        }
    }
}