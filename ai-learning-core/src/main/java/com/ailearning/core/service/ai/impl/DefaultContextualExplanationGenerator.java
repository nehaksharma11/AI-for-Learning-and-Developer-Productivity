package com.ailearning.core.service.ai.impl;

import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ai.AIExplanation;
import com.ailearning.core.service.ai.AIService;
import com.ailearning.core.service.ai.ContextualExplanationGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Default implementation of contextual explanation generator that enhances
 * AI-generated explanations with project-specific context and learning guidance.
 */
public class DefaultContextualExplanationGenerator implements ContextualExplanationGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultContextualExplanationGenerator.class);
    
    private final AIService aiService;
    private final boolean enhanceWithContext;
    private final boolean includeLearningTips;
    
    // Patterns for code element recognition
    private static final Pattern METHOD_PATTERN = Pattern.compile("\\b(public|private|protected)?\\s*(static)?\\s*\\w+\\s+(\\w+)\\s*\\(");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\b(int|String|boolean|double|float|long|char|var|let|const)\\s+(\\w+)");
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\b(class|interface|enum)\\s+(\\w+)");
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b(if|else|for|while|switch|case|try|catch|finally|return|break|continue)\\b");
    
    public DefaultContextualExplanationGenerator(AIService aiService, boolean enhanceWithContext, boolean includeLearningTips) {
        this.aiService = aiService;
        this.enhanceWithContext = enhanceWithContext;
        this.includeLearningTips = includeLearningTips;
        
        logger.info("Contextual explanation generator initialized with AI service: {}, enhance={}, tips={}",
                aiService.getServiceName(), enhanceWithContext, includeLearningTips);
    }
    
    public DefaultContextualExplanationGenerator(AIService aiService) {
        this(aiService, true, true);
    }

    @Override
    public CompletableFuture<AIExplanation> generateExplanation(String codeSnippet, CodeContext codeContext, ProjectContext projectContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Explanation generator is not available"));
        }

        return aiService.explainCode(codeSnippet, codeContext, projectContext)
                .thenApply(explanation -> enhanceExplanation(explanation, codeContext, projectContext, false));
    }

    @Override
    public CompletableFuture<AIExplanation> generateHoverExplanation(String codeElement, CodeContext codeContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Explanation generator is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            // Generate quick hover explanation
            String explanation = generateQuickExplanation(codeElement, codeContext);
            
            return AIExplanation.builder()
                    .codeSnippet(codeElement)
                    .explanation(explanation)
                    .summary(generateHoverSummary(codeElement))
                    .keyPoints(extractQuickKeyPoints(codeElement))
                    .relatedConcepts(extractQuickConcepts(codeElement))
                    .difficulty(assessQuickDifficulty(codeElement))
                    .confidenceScore(0.70) // Moderate confidence for quick explanations
                    .language(detectLanguage(codeElement, codeContext))
                    .generatedAt(LocalDateTime.now())
                    .serviceProvider("ContextualGenerator")
                    .build();
        });
    }

    @Override
    public CompletableFuture<AIExplanation> generateEducationalExplanation(String codeSnippet, CodeContext codeContext, 
                                                                          ProjectContext projectContext, boolean includeImprovements) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Explanation generator is not available"));
        }

        return aiService.explainCode(codeSnippet, codeContext, projectContext)
                .thenApply(explanation -> enhanceExplanation(explanation, codeContext, projectContext, includeImprovements));
    }

    @Override
    public boolean isAvailable() {
        return aiService != null && aiService.isAvailable();
    }
    
    // Enhancement methods
    
    private AIExplanation enhanceExplanation(AIExplanation originalExplanation, CodeContext codeContext, 
                                           ProjectContext projectContext, boolean includeImprovements) {
        
        StringBuilder enhancedExplanation = new StringBuilder(originalExplanation.getExplanation());
        List<String> enhancedKeyPoints = originalExplanation.getKeyPoints();
        List<String> enhancedConcepts = originalExplanation.getRelatedConcepts();
        
        // Add contextual information
        if (enhanceWithContext && codeContext != null) {
            String contextualInfo = generateContextualInfo(originalExplanation.getCodeSnippet(), codeContext);
            if (!contextualInfo.isEmpty()) {
                enhancedExplanation.append("\n\nContextual Information: ").append(contextualInfo);
            }
        }
        
        // Add project-specific information
        if (projectContext != null) {
            String projectInfo = generateProjectSpecificInfo(originalExplanation.getCodeSnippet(), projectContext);
            if (!projectInfo.isEmpty()) {
                enhancedExplanation.append("\n\nProject Context: ").append(projectInfo);
            }
        }
        
        // Add learning tips
        if (includeLearningTips) {
            List<String> learningTips = generateLearningTips(originalExplanation.getCodeSnippet(), originalExplanation.getLanguage());
            if (!learningTips.isEmpty()) {
                enhancedExplanation.append("\n\nLearning Tips:\n");
                for (String tip : learningTips) {
                    enhancedExplanation.append("• ").append(tip).append("\n");
                }
            }
        }
        
        // Add improvement suggestions
        if (includeImprovements) {
            List<String> improvements = generateImprovementSuggestions(originalExplanation.getCodeSnippet());
            if (!improvements.isEmpty()) {
                enhancedExplanation.append("\n\nImprovement Suggestions:\n");
                for (String improvement : improvements) {
                    enhancedExplanation.append("• ").append(improvement).append("\n");
                }
            }
        }
        
        return AIExplanation.builder()
                .codeSnippet(originalExplanation.getCodeSnippet())
                .explanation(enhancedExplanation.toString())
                .summary(originalExplanation.getSummary())
                .keyPoints(enhancedKeyPoints)
                .relatedConcepts(enhancedConcepts)
                .difficulty(originalExplanation.getDifficulty())
                .confidenceScore(Math.min(1.0, originalExplanation.getConfidenceScore() + 0.1)) // Slight boost for enhancement
                .language(originalExplanation.getLanguage())
                .generatedAt(LocalDateTime.now())
                .serviceProvider("Enhanced-" + originalExplanation.getServiceProvider())
                .build();
    }
    
    private String generateQuickExplanation(String codeElement, CodeContext codeContext) {
        // Quick pattern-based explanation for hover
        if (METHOD_PATTERN.matcher(codeElement).find()) {
            return "This is a method definition. Methods encapsulate behavior and can accept parameters and return values.";
        }
        
        if (VARIABLE_PATTERN.matcher(codeElement).find()) {
            return "This is a variable declaration. Variables store data values that can be used throughout the program.";
        }
        
        if (CLASS_PATTERN.matcher(codeElement).find()) {
            return "This is a class definition. Classes are blueprints for creating objects in object-oriented programming.";
        }
        
        if (KEYWORD_PATTERN.matcher(codeElement).find()) {
            return "This is a programming keyword that controls program flow or defines program structure.";
        }
        
        return "This is a code element. Hover explanations provide quick insights into code structure and purpose.";
    }
    
    private String generateHoverSummary(String codeElement) {
        if (METHOD_PATTERN.matcher(codeElement).find()) {
            return "Method definition";
        }
        if (VARIABLE_PATTERN.matcher(codeElement).find()) {
            return "Variable declaration";
        }
        if (CLASS_PATTERN.matcher(codeElement).find()) {
            return "Class definition";
        }
        if (KEYWORD_PATTERN.matcher(codeElement).find()) {
            return "Programming keyword";
        }
        return "Code element";
    }
    
    private List<String> extractQuickKeyPoints(String codeElement) {
        if (METHOD_PATTERN.matcher(codeElement).find()) {
            return Arrays.asList("Method definition", "Encapsulation", "Reusable code");
        }
        if (VARIABLE_PATTERN.matcher(codeElement).find()) {
            return Arrays.asList("Data storage", "Type declaration", "Memory allocation");
        }
        if (CLASS_PATTERN.matcher(codeElement).find()) {
            return Arrays.asList("Object blueprint", "Encapsulation", "Inheritance");
        }
        return Arrays.asList("Code structure", "Programming construct");
    }
    
    private List<String> extractQuickConcepts(String codeElement) {
        if (METHOD_PATTERN.matcher(codeElement).find()) {
            return Arrays.asList("Functions", "Procedures", "Object-oriented programming");
        }
        if (VARIABLE_PATTERN.matcher(codeElement).find()) {
            return Arrays.asList("Data types", "Memory management", "Scope");
        }
        if (CLASS_PATTERN.matcher(codeElement).find()) {
            return Arrays.asList("Object-oriented design", "Abstraction", "Modularity");
        }
        return Arrays.asList("Programming fundamentals");
    }
    
    private String assessQuickDifficulty(String codeElement) {
        if (VARIABLE_PATTERN.matcher(codeElement).find()) {
            return "BEGINNER";
        }
        if (METHOD_PATTERN.matcher(codeElement).find()) {
            return "INTERMEDIATE";
        }
        if (CLASS_PATTERN.matcher(codeElement).find()) {
            return "INTERMEDIATE";
        }
        return "BEGINNER";
    }
    
    private String generateContextualInfo(String codeSnippet, CodeContext codeContext) {
        StringBuilder contextInfo = new StringBuilder();
        
        if (codeContext.getDescription() != null && !codeContext.getDescription().isEmpty()) {
            contextInfo.append("This code appears in the context of: ").append(codeContext.getDescription());
        }
        
        // Add more contextual analysis based on surrounding code
        if (codeContext.getSurroundingCode() != null && !codeContext.getSurroundingCode().isEmpty()) {
            contextInfo.append(" The surrounding code suggests this is part of a larger implementation.");
        }
        
        return contextInfo.toString();
    }
    
    private String generateProjectSpecificInfo(String codeSnippet, ProjectContext projectContext) {
        StringBuilder projectInfo = new StringBuilder();
        
        if (projectContext.getProjectName() != null) {
            projectInfo.append("In the ").append(projectContext.getProjectName()).append(" project, ");
        }
        
        projectInfo.append("this code follows the project's established patterns and conventions.");
        
        return projectInfo.toString();
    }
    
    private List<String> generateLearningTips(String codeSnippet, String language) {
        List<String> tips = Arrays.asList();
        
        if ("Java".equals(language)) {
            if (codeSnippet.contains("public class")) {
                tips = Arrays.asList(
                    "Java classes should follow PascalCase naming convention",
                    "Consider the single responsibility principle when designing classes",
                    "Use access modifiers appropriately to control visibility"
                );
            } else if (codeSnippet.contains("public") && codeSnippet.contains("(")) {
                tips = Arrays.asList(
                    "Method names should be descriptive and follow camelCase",
                    "Consider method parameters and return types carefully",
                    "Document complex methods with Javadoc comments"
                );
            }
        } else if ("JavaScript".equals(language)) {
            if (codeSnippet.contains("function")) {
                tips = Arrays.asList(
                    "Consider using arrow functions for shorter syntax",
                    "Be mindful of 'this' context in JavaScript functions",
                    "Use const/let instead of var for better scoping"
                );
            }
        }
        
        if (tips.isEmpty()) {
            tips = Arrays.asList(
                "Read code carefully to understand its purpose",
                "Look for patterns and common programming constructs",
                "Practice writing similar code to reinforce learning"
            );
        }
        
        return tips;
    }
    
    private List<String> generateImprovementSuggestions(String codeSnippet) {
        List<String> suggestions = Arrays.asList();
        
        // Basic improvement suggestions based on code analysis
        if (codeSnippet.contains("System.out.println")) {
            suggestions = Arrays.asList(
                "Consider using a logging framework instead of System.out.println",
                "Add appropriate log levels (INFO, DEBUG, ERROR) for better debugging"
            );
        } else if (codeSnippet.contains("catch (Exception e)")) {
            suggestions = Arrays.asList(
                "Catch specific exceptions rather than generic Exception",
                "Consider proper error handling and recovery strategies"
            );
        } else if (codeSnippet.length() > 500) {
            suggestions = Arrays.asList(
                "Consider breaking this code into smaller, more focused methods",
                "Look for opportunities to extract reusable components"
            );
        }
        
        return suggestions;
    }
    
    private String detectLanguage(String codeElement, CodeContext codeContext) {
        // Simple language detection for quick explanations
        if (codeElement.contains("public") || codeElement.contains("class") || codeElement.contains("void")) {
            return "Java";
        }
        if (codeElement.contains("function") || codeElement.contains("const") || codeElement.contains("let")) {
            return "JavaScript";
        }
        if (codeElement.contains("def") || codeElement.contains("import")) {
            return "Python";
        }
        return "UNKNOWN";
    }
}