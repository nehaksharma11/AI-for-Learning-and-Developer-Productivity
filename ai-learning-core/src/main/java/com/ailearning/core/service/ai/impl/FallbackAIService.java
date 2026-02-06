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
import java.util.regex.Pattern;

/**
 * Fallback AI service that provides basic code analysis without external dependencies.
 * This service ensures the system remains functional even when other AI services are unavailable.
 */
public class FallbackAIService implements AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(FallbackAIService.class);
    
    // Common programming patterns for recognition
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\b(class|interface|enum)\\s+\\w+");
    private static final Pattern METHOD_PATTERN = Pattern.compile("\\b(public|private|protected)?\\s*(static)?\\s*\\w+\\s+\\w+\\s*\\(");
    private static final Pattern LOOP_PATTERN = Pattern.compile("\\b(for|while|do)\\s*\\(");
    private static final Pattern CONDITIONAL_PATTERN = Pattern.compile("\\b(if|else|switch|case)\\b");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\b(int|String|boolean|double|float|long|char)\\s+\\w+");
    
    private final boolean isEnabled;
    
    public FallbackAIService(boolean isEnabled) {
        this.isEnabled = isEnabled;
        logger.info("Fallback AI service initialized with enabled={}", isEnabled);
    }
    
    public FallbackAIService() {
        this(true); // Always enabled by default
    }

    @Override
    public CompletableFuture<AIExplanation> explainCode(String codeSnippet, CodeContext codeContext, ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String explanation = generateFallbackExplanation(codeSnippet, codeContext);
                
                return AIExplanation.builder()
                        .codeSnippet(codeSnippet)
                        .explanation(explanation)
                        .summary(generateFallbackSummary(explanation))
                        .keyPoints(extractFallbackKeyPoints(codeSnippet))
                        .relatedConcepts(extractFallbackConcepts(codeSnippet))
                        .difficulty(assessFallbackDifficulty(codeSnippet))
                        .confidenceScore(0.60) // Lower confidence for rule-based analysis
                        .language(detectLanguageFallback(codeSnippet))
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("Fallback")
                        .build();
                        
            } catch (Exception e) {
                logger.error("Failed to generate fallback explanation", e);
                return createEmptyExplanation(codeSnippet);
            }
        });
    }

    @Override
    public CompletableFuture<List<AIExample>> generateExamples(String codePattern, ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Arrays.asList(
                        AIExample.builder()
                                .title("Basic " + codePattern + " Pattern")
                                .description("A fundamental example of " + codePattern + " usage")
                                .codeExample(generateFallbackExample(codePattern, "basic"))
                                .language(detectLanguageFromPatternFallback(codePattern))
                                .tags(Arrays.asList("basic", "pattern", "fallback"))
                                .difficulty("BEGINNER")
                                .isProjectSpecific(false)
                                .relevanceScore(0.5) // Moderate relevance for generic examples
                                .generatedAt(LocalDateTime.now())
                                .serviceProvider("Fallback")
                                .build(),
                        
                        AIExample.builder()
                                .title("Common " + codePattern + " Usage")
                                .description("A typical implementation pattern for " + codePattern)
                                .codeExample(generateFallbackExample(codePattern, "common"))
                                .language(detectLanguageFromPatternFallback(codePattern))
                                .tags(Arrays.asList("common", "implementation", "fallback"))
                                .difficulty("INTERMEDIATE")
                                .isProjectSpecific(false)
                                .relevanceScore(0.6)
                                .generatedAt(LocalDateTime.now())
                                .serviceProvider("Fallback")
                                .build()
                );
                
            } catch (Exception e) {
                logger.error("Failed to generate fallback examples", e);
                return Arrays.asList(createEmptyExample(codePattern));
            }
        });
    }

    @Override
    public CompletableFuture<AIBreakdown> createBreakdown(String complexCode, CodeContext codeContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return AIBreakdown.builder()
                        .originalCode(complexCode)
                        .overview("Rule-based analysis of " + analyzeFallbackPurpose(complexCode))
                        .steps(generateFallbackBreakdownSteps(complexCode))
                        .prerequisites(Arrays.asList("Basic programming concepts", "Code reading skills"))
                        .learningObjectives(Arrays.asList("Understand code structure", "Recognize patterns", "Learn syntax"))
                        .complexity(assessFallbackComplexity(complexCode))
                        .confidenceScore(0.55) // Lower confidence for rule-based breakdown
                        .language(detectLanguageFallback(complexCode))
                        .generatedAt(LocalDateTime.now())
                        .serviceProvider("Fallback")
                        .build();
                
            } catch (Exception e) {
                logger.error("Failed to create fallback breakdown", e);
                return createEmptyBreakdown(complexCode);
            }
        });
    }

    @Override
    public boolean isAvailable() {
        return isEnabled; // Fallback service is always available when enabled
    }

    @Override
    public String getServiceName() {
        return "Fallback";
    }

    @Override
    public int getPriority() {
        return 10; // Lowest priority - only used when other services fail
    }

    // Fallback analysis methods using rule-based approaches
    
    private String generateFallbackExplanation(String codeSnippet, CodeContext codeContext) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("This code snippet ");
        
        // Analyze code structure using pattern matching
        if (CLASS_PATTERN.matcher(codeSnippet).find()) {
            explanation.append("defines a class or interface structure. ");
        }
        
        if (METHOD_PATTERN.matcher(codeSnippet).find()) {
            explanation.append("It contains method definitions with specific access modifiers and return types. ");
        }
        
        if (LOOP_PATTERN.matcher(codeSnippet).find()) {
            explanation.append("The code includes iterative constructs (loops) for repeated execution. ");
        }
        
        if (CONDITIONAL_PATTERN.matcher(codeSnippet).find()) {
            explanation.append("It uses conditional statements to control program flow. ");
        }
        
        if (VARIABLE_PATTERN.matcher(codeSnippet).find()) {
            explanation.append("Variables are declared with specific data types. ");
        }
        
        if (explanation.length() == 20) { // Only "This code snippet " was added
            explanation.append("contains programming logic that follows standard syntax patterns.");
        }
        
        return explanation.toString();
    }
    
    private String generateFallbackSummary(String explanation) {
        // Extract first sentence as summary
        int firstPeriod = explanation.indexOf('.');
        return firstPeriod > 0 ? explanation.substring(0, firstPeriod + 1) : explanation;
    }
    
    private List<String> extractFallbackKeyPoints(String codeSnippet) {
        List<String> keyPoints = Arrays.asList();
        
        if (CLASS_PATTERN.matcher(codeSnippet).find()) {
            keyPoints = Arrays.asList("Object-oriented structure", "Class definition", "Encapsulation");
        } else if (METHOD_PATTERN.matcher(codeSnippet).find()) {
            keyPoints = Arrays.asList("Method implementation", "Function logic", "Code organization");
        } else if (LOOP_PATTERN.matcher(codeSnippet).find()) {
            keyPoints = Arrays.asList("Iterative processing", "Loop control", "Repeated execution");
        } else if (CONDITIONAL_PATTERN.matcher(codeSnippet).find()) {
            keyPoints = Arrays.asList("Conditional logic", "Decision making", "Control flow");
        } else {
            keyPoints = Arrays.asList("Basic syntax", "Programming constructs", "Code structure");
        }
        
        return keyPoints;
    }
    
    private List<String> extractFallbackConcepts(String codeSnippet) {
        // Generic concepts based on detected patterns
        return Arrays.asList("Programming fundamentals", "Syntax patterns", "Code organization");
    }
    
    private String assessFallbackDifficulty(String codeSnippet) {
        int complexityScore = 0;
        
        // Count various complexity indicators
        complexityScore += countPatternMatches(CLASS_PATTERN, codeSnippet) * 2;
        complexityScore += countPatternMatches(METHOD_PATTERN, codeSnippet) * 1;
        complexityScore += countPatternMatches(LOOP_PATTERN, codeSnippet) * 2;
        complexityScore += countPatternMatches(CONDITIONAL_PATTERN, codeSnippet) * 1;
        complexityScore += codeSnippet.split("\n").length / 5; // Lines of code factor
        
        if (complexityScore <= 3) return "BEGINNER";
        if (complexityScore <= 8) return "INTERMEDIATE";
        return "ADVANCED";
    }
    
    private String assessFallbackComplexity(String codeSnippet) {
        int complexityScore = 0;
        
        complexityScore += countPatternMatches(CLASS_PATTERN, codeSnippet) * 3;
        complexityScore += countPatternMatches(METHOD_PATTERN, codeSnippet) * 2;
        complexityScore += countPatternMatches(LOOP_PATTERN, codeSnippet) * 3;
        complexityScore += countPatternMatches(CONDITIONAL_PATTERN, codeSnippet) * 2;
        complexityScore += codeSnippet.split("\n").length / 3;
        
        if (complexityScore <= 5) return "LOW";
        if (complexityScore <= 12) return "MEDIUM";
        return "HIGH";
    }
    
    private int countPatternMatches(Pattern pattern, String text) {
        return (int) pattern.matcher(text).results().count();
    }
    
    private String detectLanguageFallback(String codeSnippet) {
        // Rule-based language detection
        if (codeSnippet.contains("public class") || codeSnippet.contains("import java") || codeSnippet.contains("System.out")) {
            return "Java";
        }
        if (codeSnippet.contains("function") || codeSnippet.contains("const ") || codeSnippet.contains("let ") || codeSnippet.contains("=>")) {
            return "JavaScript";
        }
        if (codeSnippet.contains("def ") || codeSnippet.contains("import ") && !codeSnippet.contains("java")) {
            return "Python";
        }
        if (codeSnippet.contains("using ") || codeSnippet.contains("namespace") || codeSnippet.contains("Console.WriteLine")) {
            return "C#";
        }
        if (codeSnippet.contains("#include") || codeSnippet.contains("std::")) {
            return "C++";
        }
        return "UNKNOWN";
    }
    
    private String detectLanguageFromPatternFallback(String pattern) {
        // Default to Java for fallback examples
        return "Java";
    }
    
    private String analyzeFallbackPurpose(String codeSnippet) {
        if (CLASS_PATTERN.matcher(codeSnippet).find()) {
            return "object-oriented programming concepts";
        }
        if (METHOD_PATTERN.matcher(codeSnippet).find()) {
            return "function-based programming logic";
        }
        if (LOOP_PATTERN.matcher(codeSnippet).find()) {
            return "iterative processing patterns";
        }
        if (CONDITIONAL_PATTERN.matcher(codeSnippet).find()) {
            return "conditional logic implementation";
        }
        return "basic programming constructs";
    }
    
    private String generateFallbackExample(String pattern, String type) {
        if ("basic".equals(type)) {
            return "// Basic " + pattern + " example (fallback)\npublic void basicExample() {\n    // Simple implementation\n    System.out.println(\"Basic " + pattern + "\");\n}";
        } else {
            return "// Common " + pattern + " pattern (fallback)\npublic class Common" + pattern + " {\n    private String value;\n    \n    public void process() {\n        // Common implementation\n    }\n}";
        }
    }
    
    private List<com.ailearning.core.model.ai.BreakdownStep> generateFallbackBreakdownSteps(String complexCode) {
        return Arrays.asList(
            com.ailearning.core.model.ai.BreakdownStep.builder()
                    .stepNumber(1)
                    .title("Syntax Analysis")
                    .description("Identifying basic syntax elements and structure")
                    .codeFragment(complexCode.substring(0, Math.min(60, complexCode.length())))
                    .keyPoints(Arrays.asList("Syntax recognition", "Structure identification", "Pattern matching"))
                    .difficulty("BEGINNER")
                    .build(),
            
            com.ailearning.core.model.ai.BreakdownStep.builder()
                    .stepNumber(2)
                    .title("Logic Flow Recognition")
                    .description("Understanding the basic flow of execution")
                    .codeFragment("// Flow analysis section")
                    .keyPoints(Arrays.asList("Execution order", "Control structures", "Method calls"))
                    .difficulty("INTERMEDIATE")
                    .build()
        );
    }
    
    // Helper methods for creating empty/default responses
    
    private AIExplanation createEmptyExplanation(String codeSnippet) {
        return AIExplanation.builder()
                .codeSnippet(codeSnippet)
                .explanation("Unable to generate detailed explanation. This appears to be a code snippet.")
                .summary("Code analysis unavailable.")
                .keyPoints(Arrays.asList("Code snippet", "Analysis unavailable"))
                .relatedConcepts(Arrays.asList("Programming"))
                .difficulty("UNKNOWN")
                .confidenceScore(0.1)
                .language("UNKNOWN")
                .generatedAt(LocalDateTime.now())
                .serviceProvider("Fallback")
                .build();
    }
    
    private AIExample createEmptyExample(String pattern) {
        return AIExample.builder()
                .title("Example for " + pattern)
                .description("Basic example (limited analysis available)")
                .codeExample("// Example code for " + pattern + "\n// Analysis unavailable")
                .language("UNKNOWN")
                .tags(Arrays.asList("example", "fallback"))
                .difficulty("UNKNOWN")
                .isProjectSpecific(false)
                .relevanceScore(0.1)
                .generatedAt(LocalDateTime.now())
                .serviceProvider("Fallback")
                .build();
    }
    
    private AIBreakdown createEmptyBreakdown(String complexCode) {
        return AIBreakdown.builder()
                .originalCode(complexCode)
                .overview("Limited analysis available for this code")
                .steps(Arrays.asList(
                    com.ailearning.core.model.ai.BreakdownStep.builder()
                            .stepNumber(1)
                            .title("Basic Recognition")
                            .description("Code structure identified")
                            .codeFragment(complexCode.substring(0, Math.min(50, complexCode.length())))
                            .keyPoints(Arrays.asList("Code present"))
                            .difficulty("UNKNOWN")
                            .build()
                ))
                .prerequisites(Arrays.asList("Programming knowledge"))
                .learningObjectives(Arrays.asList("Code understanding"))
                .complexity("UNKNOWN")
                .confidenceScore(0.1)
                .language("UNKNOWN")
                .generatedAt(LocalDateTime.now())
                .serviceProvider("Fallback")
                .build();
    }
}