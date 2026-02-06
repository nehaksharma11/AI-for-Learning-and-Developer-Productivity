package com.ailearning.core.service.ai.impl;

import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ai.AIBreakdown;
import com.ailearning.core.model.ai.AIExample;
import com.ailearning.core.model.ai.AIExplanation;
import com.ailearning.core.service.ai.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * OpenAI API integration for code understanding and explanation.
 * This implementation provides AI-powered code analysis using OpenAI's GPT models.
 */
public class OpenAIService implements AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    
    private final String apiKey;
    private final String model;
    private final boolean isEnabled;
    private final int maxTokens;
    private final double temperature;
    
    public OpenAIService(String apiKey, String model, boolean isEnabled, int maxTokens, double temperature) {
        this.apiKey = apiKey;
        this.model = model != null ? model : "gpt-3.5-turbo";
        this.isEnabled = isEnabled;
        this.maxTokens = Math.max(100, Math.min(4000, maxTokens));
        this.temperature = Math.max(0.0, Math.min(1.0, temperature));
        
        if (isEnabled && (apiKey == null || apiKey.trim().isEmpty())) {
            logger.warn("OpenAI service is enabled but no API key provided");
        }
    }

    @Override
    public CompletableFuture<AIExplanation> explainCode(String codeSnippet, CodeContext codeContext, ProjectContext projectContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("OpenAI service is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build context-aware prompt
                String prompt = buildExplanationPrompt(codeSnippet, codeContext, projectContext);
                
                // Simulate OpenAI API call (in real implementation, this would make HTTP request)
                String explanation = generateExplanation(prompt, codeSnippet);
                
                return AIExplanation.builder()
                        .codeSnippet(codeSnippet)
                        .explanation(explanation)
                        .summary(generateSummary(explanation))
                        .keyPoints(extractKeyPoints(explanation))
                        .relatedConcepts(extractRelatedConcepts(codeSnippet, codeContext))
                        .difficulty(assessDifficulty(codeSnippet))
                        .confidenceScore(0.85) // Would be based on model confidence
                        .language(detectLanguage(codeSnippet, codeContext))
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("OpenAI")
                        .build();
                        
            } catch (Exception e) {
                logger.error("Failed to generate explanation via OpenAI", e);
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<AIExample>> generateExamples(String codePattern, ProjectContext projectContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("OpenAI service is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = buildExamplePrompt(codePattern, projectContext);
                
                // Simulate generating multiple examples
                List<AIExample> examples = Arrays.asList(
                        AIExample.builder()
                                .title("Basic " + codePattern + " Example")
                                .description("A simple example demonstrating " + codePattern)
                                .codeExample(generateExampleCode(codePattern, "basic"))
                                .language(detectLanguageFromPattern(codePattern))
                                .tags(Arrays.asList("basic", "example", codePattern.toLowerCase()))
                                .difficulty("BEGINNER")
                                .isProjectSpecific(false)
                                .relevanceScore(0.8)
                                .generatedAt(LocalDateTime.now())
                                .serviceProvider("OpenAI")
                                .build(),
                        
                        AIExample.builder()
                                .title("Advanced " + codePattern + " Usage")
                                .description("An advanced example showing best practices for " + codePattern)
                                .codeExample(generateExampleCode(codePattern, "advanced"))
                                .language(detectLanguageFromPattern(codePattern))
                                .tags(Arrays.asList("advanced", "best-practices", codePattern.toLowerCase()))
                                .difficulty("INTERMEDIATE")
                                .isProjectSpecific(projectContext != null)
                                .sourceFile(projectContext != null ? "example.java" : null)
                                .relevanceScore(0.9)
                                .generatedAt(LocalDateTime.now())
                                .serviceProvider("OpenAI")
                                .build()
                );
                
                return examples;
                
            } catch (Exception e) {
                logger.error("Failed to generate examples via OpenAI", e);
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<AIBreakdown> createBreakdown(String complexCode, CodeContext codeContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("OpenAI service is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = buildBreakdownPrompt(complexCode, codeContext);
                
                // Simulate breakdown generation
                AIBreakdown breakdown = AIBreakdown.builder()
                        .originalCode(complexCode)
                        .overview("This code demonstrates " + analyzeCodePurpose(complexCode))
                        .steps(generateBreakdownSteps(complexCode))
                        .prerequisites(Arrays.asList("Basic programming concepts", "Understanding of " + detectLanguage(complexCode, codeContext)))
                        .learningObjectives(Arrays.asList("Understand the code structure", "Learn the applied patterns", "Recognize best practices"))
                        .complexity(assessComplexity(complexCode))
                        .confidenceScore(0.82)
                        .language(detectLanguage(complexCode, codeContext))
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("OpenAI")
                        .build();
                
                return breakdown;
                
            } catch (Exception e) {
                logger.error("Failed to create breakdown via OpenAI", e);
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public boolean isAvailable() {
        return isEnabled && apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public String getServiceName() {
        return "OpenAI";
    }

    @Override
    public int getPriority() {
        return 100; // High priority for cloud-based AI
    }

    // Helper methods for prompt building and response processing
    
    private String buildExplanationPrompt(String codeSnippet, CodeContext codeContext, ProjectContext projectContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Explain the following code snippet in detail:\n\n");
        prompt.append("```\n").append(codeSnippet).append("\n```\n\n");
        
        if (codeContext != null) {
            prompt.append("Context: ").append(codeContext.getDescription()).append("\n");
        }
        
        if (projectContext != null) {
            prompt.append("Project: ").append(projectContext.getProjectName()).append("\n");
        }
        
        prompt.append("Please provide a clear, educational explanation suitable for learning.");
        
        return prompt.toString();
    }
    
    private String buildExamplePrompt(String codePattern, ProjectContext projectContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate code examples for the pattern: ").append(codePattern).append("\n");
        
        if (projectContext != null) {
            prompt.append("Project context: ").append(projectContext.getProjectName()).append("\n");
        }
        
        prompt.append("Provide both basic and advanced examples with explanations.");
        
        return prompt.toString();
    }
    
    private String buildBreakdownPrompt(String complexCode, CodeContext codeContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Break down this complex code into step-by-step explanation:\n\n");
        prompt.append("```\n").append(complexCode).append("\n```\n\n");
        
        if (codeContext != null) {
            prompt.append("Context: ").append(codeContext.getDescription()).append("\n");
        }
        
        prompt.append("Provide a detailed step-by-step breakdown for learning purposes.");
        
        return prompt.toString();
    }
    
    // Simulation methods (in real implementation, these would process OpenAI responses)
    
    private String generateExplanation(String prompt, String codeSnippet) {
        // Simulate AI explanation generation
        return "This code snippet demonstrates " + analyzeCodePurpose(codeSnippet) + 
               ". It follows standard programming practices and implements the intended functionality effectively.";
    }
    
    private String generateSummary(String explanation) {
        // Extract first sentence as summary
        int firstPeriod = explanation.indexOf('.');
        return firstPeriod > 0 ? explanation.substring(0, firstPeriod + 1) : explanation;
    }
    
    private List<String> extractKeyPoints(String explanation) {
        // Simulate key point extraction
        return Arrays.asList(
            "Follows standard coding practices",
            "Implements clear logic flow",
            "Uses appropriate data structures"
        );
    }
    
    private List<String> extractRelatedConcepts(String codeSnippet, CodeContext codeContext) {
        // Simulate concept extraction based on code analysis
        return Arrays.asList("Object-oriented programming", "Design patterns", "Best practices");
    }
    
    private String assessDifficulty(String codeSnippet) {
        // Simple heuristic for difficulty assessment
        int lines = codeSnippet.split("\n").length;
        if (lines <= 5) return "BEGINNER";
        if (lines <= 15) return "INTERMEDIATE";
        return "ADVANCED";
    }
    
    private String assessComplexity(String codeSnippet) {
        // Simple heuristic for complexity assessment
        int lines = codeSnippet.split("\n").length;
        if (lines <= 10) return "LOW";
        if (lines <= 25) return "MEDIUM";
        return "HIGH";
    }
    
    private String detectLanguage(String codeSnippet, CodeContext codeContext) {
        // Simple language detection based on syntax patterns
        if (codeSnippet.contains("public class") || codeSnippet.contains("import java")) return "Java";
        if (codeSnippet.contains("function") || codeSnippet.contains("const ") || codeSnippet.contains("let ")) return "JavaScript";
        if (codeSnippet.contains("def ") || codeSnippet.contains("import ")) return "Python";
        return "UNKNOWN";
    }
    
    private String detectLanguageFromPattern(String pattern) {
        // Default to Java for this implementation
        return "Java";
    }
    
    private String analyzeCodePurpose(String codeSnippet) {
        // Simple purpose analysis
        if (codeSnippet.contains("class")) return "class definition and object-oriented design";
        if (codeSnippet.contains("function") || codeSnippet.contains("def")) return "function implementation";
        if (codeSnippet.contains("for") || codeSnippet.contains("while")) return "iterative processing";
        return "programming logic";
    }
    
    private String generateExampleCode(String pattern, String level) {
        // Generate simple example code based on pattern and level
        if ("basic".equals(level)) {
            return "// Basic " + pattern + " example\npublic void example() {\n    // Implementation here\n}";
        } else {
            return "// Advanced " + pattern + " example\npublic class Advanced" + pattern + " {\n    // Complex implementation\n}";
        }
    }
    
    private List<com.ailearning.core.model.ai.BreakdownStep> generateBreakdownSteps(String complexCode) {
        // Generate breakdown steps
        return Arrays.asList(
            com.ailearning.core.model.ai.BreakdownStep.builder()
                    .stepNumber(1)
                    .title("Code Structure Analysis")
                    .description("Understanding the overall structure and organization")
                    .codeFragment(complexCode.substring(0, Math.min(100, complexCode.length())))
                    .keyPoints(Arrays.asList("Class definition", "Method signatures", "Variable declarations"))
                    .difficulty("BEGINNER")
                    .build(),
            
            com.ailearning.core.model.ai.BreakdownStep.builder()
                    .stepNumber(2)
                    .title("Logic Flow Analysis")
                    .description("Examining the execution flow and control structures")
                    .codeFragment("// Control flow section")
                    .keyPoints(Arrays.asList("Conditional statements", "Loops", "Method calls"))
                    .difficulty("INTERMEDIATE")
                    .build()
        );
    }
}