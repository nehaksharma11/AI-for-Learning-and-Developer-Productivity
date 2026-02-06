package com.ailearning.core.service.ai.impl;

import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ai.AIExample;
import com.ailearning.core.service.ai.AIService;
import com.ailearning.core.service.ai.ProjectSpecificExampleExtractor;
import com.ailearning.core.service.ContextEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Default implementation of project-specific example extractor that analyzes
 * the current project to find relevant code examples and patterns.
 */
public class DefaultProjectSpecificExampleExtractor implements ProjectSpecificExampleExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultProjectSpecificExampleExtractor.class);
    
    private final AIService aiService;
    private final ContextEngine contextEngine;
    private final boolean enableProjectAnalysis;
    private final int maxAnalysisDepth;
    
    public DefaultProjectSpecificExampleExtractor(AIService aiService, ContextEngine contextEngine, 
                                                 boolean enableProjectAnalysis, int maxAnalysisDepth) {
        this.aiService = aiService;
        this.contextEngine = contextEngine;
        this.enableProjectAnalysis = enableProjectAnalysis;
        this.maxAnalysisDepth = Math.max(1, Math.min(10, maxAnalysisDepth));
        
        logger.info("Project-specific example extractor initialized with analysis={}, depth={}",
                enableProjectAnalysis, this.maxAnalysisDepth);
    }
    
    public DefaultProjectSpecificExampleExtractor(AIService aiService, ContextEngine contextEngine) {
        this(aiService, contextEngine, true, 5);
    }

    @Override
    public CompletableFuture<List<AIExample>> extractProjectExamples(String pattern, ProjectContext projectContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Example extractor is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<AIExample> examples = new ArrayList<>();
                
                if (enableProjectAnalysis && projectContext != null) {
                    // Analyze project structure for relevant examples
                    examples.addAll(analyzeProjectForPattern(pattern, projectContext));
                }
                
                // Generate AI-based examples if we don't have enough project-specific ones
                if (examples.size() < 2) {
                    return aiService.generateExamples(pattern, projectContext)
                            .thenApply(aiExamples -> {
                                List<AIExample> combined = new ArrayList<>(examples);
                                combined.addAll(enhanceExamplesWithProjectContext(aiExamples, projectContext));
                                return combined;
                            }).get();
                }
                
                return examples;
                
            } catch (Exception e) {
                logger.error("Failed to extract project examples for pattern: {}", pattern, e);
                return createFallbackExamples(pattern, projectContext);
            }
        });
    }

    @Override
    public CompletableFuture<List<AIExample>> generateContextualExamples(String pattern, ProjectContext projectContext, int maxExamples) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Example extractor is not available"));
        }

        return aiService.generateExamples(pattern, projectContext)
                .thenApply(examples -> {
                    List<AIExample> contextualExamples = enhanceExamplesWithProjectContext(examples, projectContext);
                    return contextualExamples.stream()
                            .limit(Math.max(1, maxExamples))
                            .collect(Collectors.toList());
                });
    }

    @Override
    public CompletableFuture<List<AIExample>> findSimilarExamples(String codeSnippet, ProjectContext projectContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Example extractor is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<AIExample> similarExamples = new ArrayList<>();
                
                if (enableProjectAnalysis && projectContext != null) {
                    // Use context engine to find similar code patterns
                    similarExamples.addAll(findSimilarPatternsInProject(codeSnippet, projectContext));
                }
                
                // If no similar examples found in project, generate generic ones
                if (similarExamples.isEmpty()) {
                    similarExamples.addAll(generateSimilarExamples(codeSnippet, projectContext));
                }
                
                return similarExamples;
                
            } catch (Exception e) {
                logger.error("Failed to find similar examples for code snippet", e);
                return createFallbackSimilarExamples(codeSnippet, projectContext);
            }
        });
    }

    @Override
    public CompletableFuture<List<AIExample>> extractBestPracticeExamples(String category, ProjectContext projectContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Example extractor is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<AIExample> bestPractices = new ArrayList<>();
                
                // Extract best practices based on category
                switch (category.toLowerCase()) {
                    case "error-handling":
                        bestPractices.addAll(extractErrorHandlingExamples(projectContext));
                        break;
                    case "testing":
                        bestPractices.addAll(extractTestingExamples(projectContext));
                        break;
                    case "documentation":
                        bestPractices.addAll(extractDocumentationExamples(projectContext));
                        break;
                    case "design-patterns":
                        bestPractices.addAll(extractDesignPatternExamples(projectContext));
                        break;
                    default:
                        bestPractices.addAll(extractGeneralBestPractices(projectContext));
                }
                
                return bestPractices;
                
            } catch (Exception e) {
                logger.error("Failed to extract best practice examples for category: {}", category, e);
                return createFallbackBestPracticeExamples(category, projectContext);
            }
        });
    }

    @Override
    public boolean isAvailable() {
        return aiService != null && aiService.isAvailable();
    }
    
    // Project analysis methods
    
    private List<AIExample> analyzeProjectForPattern(String pattern, ProjectContext projectContext) {
        List<AIExample> examples = new ArrayList<>();
        
        // Simulate project analysis (in real implementation, this would scan project files)
        if (projectContext.getProjectName() != null) {
            examples.add(AIExample.builder()
                    .title("Project-specific " + pattern + " Example")
                    .description("An example of " + pattern + " found in " + projectContext.getProjectName())
                    .codeExample(generateProjectSpecificCode(pattern, projectContext))
                    .language(detectProjectLanguage(projectContext))
                    .tags(Arrays.asList("project-specific", pattern.toLowerCase(), "real-world"))
                    .difficulty("INTERMEDIATE")
                    .isProjectSpecific(true)
                    .sourceFile(projectContext.getProjectName() + "/src/main/Example.java")
                    .relevanceScore(0.95) // High relevance for project-specific examples
                    .generatedAt(LocalDateTime.now())
                    .serviceProvider("ProjectExtractor")
                    .build());
        }
        
        return examples;
    }
    
    private List<AIExample> enhanceExamplesWithProjectContext(List<AIExample> examples, ProjectContext projectContext) {
        if (projectContext == null) {
            return examples;
        }
        
        return examples.stream()
                .map(example -> AIExample.builder()
                        .title(example.getTitle() + " (Project Context)")
                        .description(example.getDescription() + " This example is adapted for the " + 
                                   projectContext.getProjectName() + " project.")
                        .codeExample(adaptCodeForProject(example.getCodeExample(), projectContext))
                        .language(example.getLanguage())
                        .tags(combineWithProjectTags(example.getTags(), projectContext))
                        .difficulty(example.getDifficulty())
                        .isProjectSpecific(true)
                        .sourceFile(generateProjectSourceFile(projectContext))
                        .relevanceScore(Math.min(1.0, example.getRelevanceScore() + 0.2))
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("Enhanced-" + example.getServiceProvider())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<AIExample> findSimilarPatternsInProject(String codeSnippet, ProjectContext projectContext) {
        List<AIExample> similarExamples = new ArrayList<>();
        
        // Simulate finding similar patterns in project
        if (projectContext != null && contextEngine != null) {
            similarExamples.add(AIExample.builder()
                    .title("Similar Pattern in " + projectContext.getProjectName())
                    .description("A similar code pattern found elsewhere in the project")
                    .codeExample(generateSimilarProjectCode(codeSnippet, projectContext))
                    .language(detectProjectLanguage(projectContext))
                    .tags(Arrays.asList("similar-pattern", "project-code", "reference"))
                    .difficulty(assessCodeDifficulty(codeSnippet))
                    .isProjectSpecific(true)
                    .sourceFile(projectContext.getProjectName() + "/src/main/SimilarExample.java")
                    .relevanceScore(0.85)
                    .generatedAt(LocalDateTime.now())
                    .serviceProvider("ProjectExtractor")
                    .build());
        }
        
        return similarExamples;
    }
    
    private List<AIExample> generateSimilarExamples(String codeSnippet, ProjectContext projectContext) {
        // Generate similar examples based on code analysis
        return Arrays.asList(
                AIExample.builder()
                        .title("Similar Code Pattern")
                        .description("A code pattern similar to the provided snippet")
                        .codeExample(generateSimilarCode(codeSnippet))
                        .language(detectLanguageFromCode(codeSnippet))
                        .tags(Arrays.asList("similar", "pattern", "generated"))
                        .difficulty(assessCodeDifficulty(codeSnippet))
                        .isProjectSpecific(false)
                        .relevanceScore(0.70)
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("ProjectExtractor")
                        .build()
        );
    }
    
    // Best practice extraction methods
    
    private List<AIExample> extractErrorHandlingExamples(ProjectContext projectContext) {
        return Arrays.asList(
                AIExample.builder()
                        .title("Error Handling Best Practice")
                        .description("Proper exception handling pattern used in the project")
                        .codeExample(generateErrorHandlingExample(projectContext))
                        .language(detectProjectLanguage(projectContext))
                        .tags(Arrays.asList("error-handling", "best-practice", "exception"))
                        .difficulty("INTERMEDIATE")
                        .isProjectSpecific(projectContext != null)
                        .relevanceScore(0.90)
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("ProjectExtractor")
                        .build()
        );
    }
    
    private List<AIExample> extractTestingExamples(ProjectContext projectContext) {
        return Arrays.asList(
                AIExample.builder()
                        .title("Testing Best Practice")
                        .description("Unit testing pattern commonly used in the project")
                        .codeExample(generateTestingExample(projectContext))
                        .language(detectProjectLanguage(projectContext))
                        .tags(Arrays.asList("testing", "unit-test", "best-practice"))
                        .difficulty("INTERMEDIATE")
                        .isProjectSpecific(projectContext != null)
                        .relevanceScore(0.88)
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("ProjectExtractor")
                        .build()
        );
    }
    
    private List<AIExample> extractDocumentationExamples(ProjectContext projectContext) {
        return Arrays.asList(
                AIExample.builder()
                        .title("Documentation Best Practice")
                        .description("Javadoc documentation pattern used in the project")
                        .codeExample(generateDocumentationExample(projectContext))
                        .language(detectProjectLanguage(projectContext))
                        .tags(Arrays.asList("documentation", "javadoc", "best-practice"))
                        .difficulty("BEGINNER")
                        .isProjectSpecific(projectContext != null)
                        .relevanceScore(0.85)
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("ProjectExtractor")
                        .build()
        );
    }
    
    private List<AIExample> extractDesignPatternExamples(ProjectContext projectContext) {
        return Arrays.asList(
                AIExample.builder()
                        .title("Design Pattern Example")
                        .description("A design pattern implementation found in the project")
                        .codeExample(generateDesignPatternExample(projectContext))
                        .language(detectProjectLanguage(projectContext))
                        .tags(Arrays.asList("design-pattern", "architecture", "best-practice"))
                        .difficulty("ADVANCED")
                        .isProjectSpecific(projectContext != null)
                        .relevanceScore(0.92)
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("ProjectExtractor")
                        .build()
        );
    }
    
    private List<AIExample> extractGeneralBestPractices(ProjectContext projectContext) {
        return Arrays.asList(
                AIExample.builder()
                        .title("General Best Practice")
                        .description("A general coding best practice observed in the project")
                        .codeExample(generateGeneralBestPracticeExample(projectContext))
                        .language(detectProjectLanguage(projectContext))
                        .tags(Arrays.asList("best-practice", "coding-standards", "general"))
                        .difficulty("INTERMEDIATE")
                        .isProjectSpecific(projectContext != null)
                        .relevanceScore(0.80)
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("ProjectExtractor")
                        .build()
        );
    }
    
    // Helper methods for code generation and analysis
    
    private String generateProjectSpecificCode(String pattern, ProjectContext projectContext) {
        return "// " + pattern + " example from " + projectContext.getProjectName() + "\n" +
               "public class " + pattern.replace(" ", "") + "Example {\n" +
               "    // Project-specific implementation\n" +
               "    public void demonstrate" + pattern.replace(" ", "") + "() {\n" +
               "        // Implementation follows project conventions\n" +
               "    }\n" +
               "}";
    }
    
    private String adaptCodeForProject(String originalCode, ProjectContext projectContext) {
        // Add project-specific comments and adaptations
        return "// Adapted for " + projectContext.getProjectName() + " project\n" + originalCode;
    }
    
    private List<String> combineWithProjectTags(List<String> originalTags, ProjectContext projectContext) {
        List<String> combinedTags = new ArrayList<>(originalTags);
        if (projectContext.getProjectName() != null) {
            combinedTags.add(projectContext.getProjectName().toLowerCase());
        }
        combinedTags.add("project-adapted");
        return combinedTags;
    }
    
    private String generateProjectSourceFile(ProjectContext projectContext) {
        if (projectContext != null && projectContext.getProjectName() != null) {
            return projectContext.getProjectName() + "/src/main/java/Example.java";
        }
        return "src/main/java/Example.java";
    }
    
    private String generateSimilarProjectCode(String codeSnippet, ProjectContext projectContext) {
        return "// Similar pattern found in " + projectContext.getProjectName() + "\n" +
               "// Original: " + codeSnippet.substring(0, Math.min(50, codeSnippet.length())) + "...\n" +
               "public void similarMethod() {\n" +
               "    // Similar implementation pattern\n" +
               "}";
    }
    
    private String generateSimilarCode(String codeSnippet) {
        return "// Similar pattern\n" +
               "public void similarImplementation() {\n" +
               "    // Code with similar structure and purpose\n" +
               "}";
    }
    
    private String generateErrorHandlingExample(ProjectContext projectContext) {
        return "// Error handling best practice\n" +
               "public void processData() {\n" +
               "    try {\n" +
               "        // Main logic\n" +
               "    } catch (SpecificException e) {\n" +
               "        logger.error(\"Specific error occurred\", e);\n" +
               "        // Handle specific case\n" +
               "    } catch (Exception e) {\n" +
               "        logger.error(\"Unexpected error\", e);\n" +
               "        throw new ProcessingException(\"Failed to process data\", e);\n" +
               "    }\n" +
               "}";
    }
    
    private String generateTestingExample(ProjectContext projectContext) {
        return "// Unit test best practice\n" +
               "@Test\n" +
               "public void shouldProcessDataSuccessfully() {\n" +
               "    // Given\n" +
               "    TestData testData = createTestData();\n" +
               "    \n" +
               "    // When\n" +
               "    Result result = processor.process(testData);\n" +
               "    \n" +
               "    // Then\n" +
               "    assertThat(result).isNotNull();\n" +
               "    assertThat(result.isSuccess()).isTrue();\n" +
               "}";
    }
    
    private String generateDocumentationExample(ProjectContext projectContext) {
        return "/**\n" +
               " * Processes the given data according to business rules.\n" +
               " * \n" +
               " * @param data the input data to process\n" +
               " * @return the processing result\n" +
               " * @throws ProcessingException if processing fails\n" +
               " */\n" +
               "public Result processData(Data data) throws ProcessingException {\n" +
               "    // Implementation\n" +
               "}";
    }
    
    private String generateDesignPatternExample(ProjectContext projectContext) {
        return "// Builder pattern example\n" +
               "public class DataBuilder {\n" +
               "    private String name;\n" +
               "    private int value;\n" +
               "    \n" +
               "    public DataBuilder name(String name) {\n" +
               "        this.name = name;\n" +
               "        return this;\n" +
               "    }\n" +
               "    \n" +
               "    public DataBuilder value(int value) {\n" +
               "        this.value = value;\n" +
               "        return this;\n" +
               "    }\n" +
               "    \n" +
               "    public Data build() {\n" +
               "        return new Data(name, value);\n" +
               "    }\n" +
               "}";
    }
    
    private String generateGeneralBestPracticeExample(ProjectContext projectContext) {
        return "// General best practice: meaningful names and single responsibility\n" +
               "public class UserAccountValidator {\n" +
               "    \n" +
               "    public ValidationResult validateAccount(UserAccount account) {\n" +
               "        if (account == null) {\n" +
               "            return ValidationResult.invalid(\"Account cannot be null\");\n" +
               "        }\n" +
               "        \n" +
               "        return ValidationResult.valid();\n" +
               "    }\n" +
               "}";
    }
    
    private String detectProjectLanguage(ProjectContext projectContext) {
        // In real implementation, this would analyze project files
        return "Java"; // Default assumption
    }
    
    private String detectLanguageFromCode(String codeSnippet) {
        if (codeSnippet.contains("public class") || codeSnippet.contains("import java")) {
            return "Java";
        }
        if (codeSnippet.contains("function") || codeSnippet.contains("const ")) {
            return "JavaScript";
        }
        if (codeSnippet.contains("def ") || codeSnippet.contains("import ")) {
            return "Python";
        }
        return "UNKNOWN";
    }
    
    private String assessCodeDifficulty(String codeSnippet) {
        int complexity = codeSnippet.split("\n").length;
        if (complexity <= 5) return "BEGINNER";
        if (complexity <= 15) return "INTERMEDIATE";
        return "ADVANCED";
    }
    
    // Fallback methods
    
    private List<AIExample> createFallbackExamples(String pattern, ProjectContext projectContext) {
        return Arrays.asList(
                AIExample.builder()
                        .title("Basic " + pattern + " Example")
                        .description("A basic example of " + pattern)
                        .codeExample("// Basic " + pattern + " implementation\npublic void example() {\n    // Implementation\n}")
                        .language("Java")
                        .tags(Arrays.asList("basic", "fallback"))
                        .difficulty("BEGINNER")
                        .isProjectSpecific(false)
                        .relevanceScore(0.50)
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("ProjectExtractor-Fallback")
                        .build()
        );
    }
    
    private List<AIExample> createFallbackSimilarExamples(String codeSnippet, ProjectContext projectContext) {
        return Arrays.asList(
                AIExample.builder()
                        .title("Similar Code Structure")
                        .description("A code structure similar to the provided snippet")
                        .codeExample("// Similar structure\npublic void similarMethod() {\n    // Similar logic\n}")
                        .language(detectLanguageFromCode(codeSnippet))
                        .tags(Arrays.asList("similar", "fallback"))
                        .difficulty(assessCodeDifficulty(codeSnippet))
                        .isProjectSpecific(false)
                        .relevanceScore(0.40)
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("ProjectExtractor-Fallback")
                        .build()
        );
    }
    
    private List<AIExample> createFallbackBestPracticeExamples(String category, ProjectContext projectContext) {
        return Arrays.asList(
                AIExample.builder()
                        .title("Basic " + category + " Practice")
                        .description("A basic best practice for " + category)
                        .codeExample("// Basic " + category + " example\npublic void example() {\n    // Best practice implementation\n}")
                        .language("Java")
                        .tags(Arrays.asList(category, "best-practice", "fallback"))
                        .difficulty("INTERMEDIATE")
                        .isProjectSpecific(false)
                        .relevanceScore(0.45)
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("ProjectExtractor-Fallback")
                        .build()
        );
    }
}