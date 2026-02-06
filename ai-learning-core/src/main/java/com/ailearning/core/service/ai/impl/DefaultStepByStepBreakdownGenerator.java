package com.ailearning.core.service.ai.impl;

import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.ai.AIBreakdown;
import com.ailearning.core.model.ai.BreakdownStep;
import com.ailearning.core.service.ai.AIService;
import com.ailearning.core.service.ai.StepByStepBreakdownGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of step-by-step breakdown generator that creates
 * detailed, educational breakdowns of complex code patterns.
 */
public class DefaultStepByStepBreakdownGenerator implements StepByStepBreakdownGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultStepByStepBreakdownGenerator.class);
    
    private final AIService aiService;
    private final boolean enableDetailedAnalysis;
    private final int maxSteps;
    
    // Patterns for code structure analysis
    private static final Pattern CLASS_PATTERN = Pattern.compile("(class|interface|enum)\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*\\w+\\s+(\\w+)\\s*\\([^)]*\\)");
    private static final Pattern LOOP_PATTERN = Pattern.compile("(for|while|do)\\s*\\([^)]*\\)");
    private static final Pattern CONDITIONAL_PATTERN = Pattern.compile("(if|else\\s+if|switch)\\s*\\([^)]*\\)");
    private static final Pattern TRY_CATCH_PATTERN = Pattern.compile("try\\s*\\{[^}]*\\}\\s*catch\\s*\\([^)]*\\)");
    
    public DefaultStepByStepBreakdownGenerator(AIService aiService, boolean enableDetailedAnalysis, int maxSteps) {
        this.aiService = aiService;
        this.enableDetailedAnalysis = enableDetailedAnalysis;
        this.maxSteps = Math.max(3, Math.min(20, maxSteps));
        
        logger.info("Step-by-step breakdown generator initialized with detailed={}, maxSteps={}",
                enableDetailedAnalysis, this.maxSteps);
    }
    
    public DefaultStepByStepBreakdownGenerator(AIService aiService) {
        this(aiService, true, 10);
    }

    @Override
    public CompletableFuture<AIBreakdown> createDetailedBreakdown(String complexCode, CodeContext codeContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Breakdown generator is not available"));
        }

        return aiService.createBreakdown(complexCode, codeContext)
                .thenApply(breakdown -> enhanceBreakdownWithDetails(breakdown, complexCode, codeContext));
    }

    @Override
    public CompletableFuture<AIBreakdown> createBeginnerBreakdown(String complexCode, CodeContext codeContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Breakdown generator is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<BreakdownStep> beginnerSteps = createBeginnerFriendlySteps(complexCode);
            
            return AIBreakdown.builder()
                    .originalCode(complexCode)
                    .overview("Beginner-friendly breakdown: " + generateBeginnerOverview(complexCode))
                    .steps(beginnerSteps)
                    .prerequisites(Arrays.asList("Basic programming concepts", "Understanding of variables and methods"))
                    .learningObjectives(Arrays.asList("Understand code structure", "Learn basic patterns", "Build confidence"))
                    .complexity("BEGINNER-FRIENDLY")
                    .confidenceScore(0.80)
                    .language(detectLanguage(complexCode))
                    .generatedAt(LocalDateTime.now())
                    .serviceProvider("StepByStepGenerator-Beginner")
                    .build();
        });
    }

    @Override
    public CompletableFuture<AIBreakdown> createInteractiveBreakdown(String complexCode, CodeContext codeContext) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Breakdown generator is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<BreakdownStep> interactiveSteps = createInteractiveSteps(complexCode);
            
            return AIBreakdown.builder()
                    .originalCode(complexCode)
                    .overview("Interactive breakdown with exercises: " + generateInteractiveOverview(complexCode))
                    .steps(interactiveSteps)
                    .prerequisites(Arrays.asList("Basic programming knowledge", "Willingness to experiment"))
                    .learningObjectives(Arrays.asList("Active learning", "Hands-on practice", "Deep understanding"))
                    .complexity("INTERACTIVE")
                    .confidenceScore(0.85)
                    .language(detectLanguage(complexCode))
                    .generatedAt(LocalDateTime.now())
                    .serviceProvider("StepByStepGenerator-Interactive")
                    .build();
        });
    }

    @Override
    public CompletableFuture<AIBreakdown> createFocusedBreakdown(String complexCode, CodeContext codeContext, 
                                                               List<String> learningObjectives) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Breakdown generator is not available"));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<BreakdownStep> focusedSteps = createFocusedSteps(complexCode, learningObjectives);
            
            return AIBreakdown.builder()
                    .originalCode(complexCode)
                    .overview("Focused breakdown targeting specific objectives: " + generateFocusedOverview(complexCode, learningObjectives))
                    .steps(focusedSteps)
                    .prerequisites(Arrays.asList("Understanding of target concepts", "Relevant background knowledge"))
                    .learningObjectives(new ArrayList<>(learningObjectives))
                    .complexity("FOCUSED")
                    .confidenceScore(0.88)
                    .language(detectLanguage(complexCode))
                    .generatedAt(LocalDateTime.now())
                    .serviceProvider("StepByStepGenerator-Focused")
                    .build();
        });
    }

    @Override
    public boolean isAvailable() {
        return aiService != null && aiService.isAvailable();
    }
    
    // Enhancement and step creation methods
    
    private AIBreakdown enhanceBreakdownWithDetails(AIBreakdown originalBreakdown, String complexCode, CodeContext codeContext) {
        List<BreakdownStep> enhancedSteps = new ArrayList<>();
        
        // Add detailed analysis steps
        enhancedSteps.addAll(createStructuralAnalysisSteps(complexCode));
        enhancedSteps.addAll(originalBreakdown.getSteps());
        enhancedSteps.addAll(createImplementationSteps(complexCode));
        
        // Limit to maxSteps
        if (enhancedSteps.size() > maxSteps) {
            enhancedSteps = enhancedSteps.subList(0, maxSteps);
        }
        
        return AIBreakdown.builder()
                .originalCode(originalBreakdown.getOriginalCode())
                .overview("Detailed breakdown: " + originalBreakdown.getOverview())
                .steps(enhancedSteps)
                .prerequisites(enhancePrerequisites(originalBreakdown.getPrerequisites()))
                .learningObjectives(enhanceLearningObjectives(originalBreakdown.getLearningObjectives()))
                .complexity("DETAILED")
                .confidenceScore(Math.min(1.0, originalBreakdown.getConfidenceScore() + 0.1))
                .language(originalBreakdown.getLanguage())
                .generatedAt(LocalDateTime.now())
                .serviceProvider("Enhanced-" + originalBreakdown.getServiceProvider())
                .build();
    }
    
    private List<BreakdownStep> createBeginnerFriendlySteps(String complexCode) {
        List<BreakdownStep> steps = new ArrayList<>();
        int stepNumber = 1;
        
        // Step 1: Overall structure
        steps.add(BreakdownStep.builder()
                .stepNumber(stepNumber++)
                .title("Understanding the Overall Structure")
                .description("Let's start by looking at the big picture. This code has a specific structure that we'll explore piece by piece.")
                .codeFragment(extractMainStructure(complexCode))
                .keyPoints(Arrays.asList("Code organization", "Main components", "Overall purpose"))
                .difficulty("BEGINNER")
                .build());
        
        // Step 2: Key components
        if (CLASS_PATTERN.matcher(complexCode).find()) {
            steps.add(BreakdownStep.builder()
                    .stepNumber(stepNumber++)
                    .title("Identifying Classes and Objects")
                    .description("Classes are like blueprints for creating objects. Let's see what classes are defined here.")
                    .codeFragment(extractClassDefinitions(complexCode))
                    .keyPoints(Arrays.asList("Class definition", "Object-oriented concepts", "Encapsulation"))
                    .difficulty("BEGINNER")
                    .build());
        }
        
        // Step 3: Methods and functions
        if (METHOD_PATTERN.matcher(complexCode).find()) {
            steps.add(BreakdownStep.builder()
                    .stepNumber(stepNumber++)
                    .title("Understanding Methods")
                    .description("Methods are like mini-programs that do specific tasks. Let's examine the methods in this code.")
                    .codeFragment(extractMethodSignatures(complexCode))
                    .keyPoints(Arrays.asList("Method purpose", "Parameters", "Return values"))
                    .difficulty("BEGINNER")
                    .build());
        }
        
        // Step 4: Control flow
        if (LOOP_PATTERN.matcher(complexCode).find() || CONDITIONAL_PATTERN.matcher(complexCode).find()) {
            steps.add(BreakdownStep.builder()
                    .stepNumber(stepNumber++)
                    .title("Following the Logic Flow")
                    .description("Programs make decisions and repeat actions. Let's trace how this code flows from start to finish.")
                    .codeFragment(extractControlFlow(complexCode))
                    .keyPoints(Arrays.asList("Decision making", "Repetition", "Program flow"))
                    .difficulty("BEGINNER")
                    .build());
        }
        
        return steps;
    }
    
    private List<BreakdownStep> createInteractiveSteps(String complexCode) {
        List<BreakdownStep> steps = new ArrayList<>();
        int stepNumber = 1;
        
        // Interactive step 1: Prediction
        steps.add(BreakdownStep.builder()
                .stepNumber(stepNumber++)
                .title("Predict the Outcome")
                .description("Before we dive in, try to predict what this code does. Look at the structure and variable names for clues.")
                .codeFragment(complexCode.substring(0, Math.min(200, complexCode.length())) + "...")
                .keyPoints(Arrays.asList("Code reading skills", "Pattern recognition", "Logical thinking"))
                .difficulty("INTERACTIVE")
                .build());
        
        // Interactive step 2: Trace execution
        steps.add(BreakdownStep.builder()
                .stepNumber(stepNumber++)
                .title("Trace the Execution")
                .description("Now let's walk through the code step by step. Try to follow along and predict what happens at each line.")
                .codeFragment(extractExecutionPath(complexCode))
                .keyPoints(Arrays.asList("Step-by-step execution", "Variable changes", "Method calls"))
                .difficulty("INTERACTIVE")
                .build());
        
        // Interactive step 3: Experiment
        steps.add(BreakdownStep.builder()
                .stepNumber(stepNumber++)
                .title("Experiment and Modify")
                .description("Try modifying parts of this code. What happens if you change a variable or condition?")
                .codeFragment(generateExperimentalVariation(complexCode))
                .keyPoints(Arrays.asList("Experimentation", "Learning by doing", "Understanding impact"))
                .difficulty("INTERACTIVE")
                .build());
        
        return steps;
    }
    
    private List<BreakdownStep> createFocusedSteps(String complexCode, List<String> learningObjectives) {
        List<BreakdownStep> steps = new ArrayList<>();
        int stepNumber = 1;
        
        for (String objective : learningObjectives) {
            steps.add(BreakdownStep.builder()
                    .stepNumber(stepNumber++)
                    .title("Focus: " + objective)
                    .description("Let's examine how this code demonstrates " + objective.toLowerCase() + ".")
                    .codeFragment(extractRelevantCodeForObjective(complexCode, objective))
                    .keyPoints(generateKeyPointsForObjective(objective))
                    .difficulty("FOCUSED")
                    .build());
        }
        
        return steps;
    }
    
    private List<BreakdownStep> createStructuralAnalysisSteps(String complexCode) {
        List<BreakdownStep> steps = new ArrayList<>();
        int stepNumber = 1;
        
        steps.add(BreakdownStep.builder()
                .stepNumber(stepNumber++)
                .title("Structural Analysis")
                .description("Analyzing the overall structure and architecture of the code")
                .codeFragment(extractStructuralElements(complexCode))
                .keyPoints(Arrays.asList("Code organization", "Design patterns", "Architecture"))
                .difficulty("ADVANCED")
                .build());
        
        return steps;
    }
    
    private List<BreakdownStep> createImplementationSteps(String complexCode) {
        List<BreakdownStep> steps = new ArrayList<>();
        int stepNumber = 100; // Start high to avoid conflicts
        
        steps.add(BreakdownStep.builder()
                .stepNumber(stepNumber++)
                .title("Implementation Details")
                .description("Examining the specific implementation choices and their implications")
                .codeFragment(extractImplementationDetails(complexCode))
                .keyPoints(Arrays.asList("Implementation choices", "Performance considerations", "Best practices"))
                .difficulty("ADVANCED")
                .build());
        
        return steps;
    }
    
    // Helper methods for code analysis and extraction
    
    private String generateBeginnerOverview(String complexCode) {
        return "This code demonstrates fundamental programming concepts in a beginner-friendly way.";
    }
    
    private String generateInteractiveOverview(String complexCode) {
        return "This interactive breakdown encourages hands-on learning and experimentation.";
    }
    
    private String generateFocusedOverview(String complexCode, List<String> objectives) {
        return "This breakdown focuses specifically on: " + String.join(", ", objectives);
    }
    
    private String extractMainStructure(String complexCode) {
        // Extract the main structural elements
        String[] lines = complexCode.split("\n");
        StringBuilder structure = new StringBuilder();
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.contains("class ") || trimmed.contains("interface ") || 
                trimmed.contains("public ") || trimmed.contains("private ")) {
                structure.append(line).append("\n");
            }
        }
        
        return structure.length() > 0 ? structure.toString() : complexCode.substring(0, Math.min(100, complexCode.length()));
    }
    
    private String extractClassDefinitions(String complexCode) {
        Matcher matcher = CLASS_PATTERN.matcher(complexCode);
        StringBuilder classes = new StringBuilder();
        
        while (matcher.find()) {
            classes.append(matcher.group()).append("\n");
        }
        
        return classes.length() > 0 ? classes.toString() : "// No class definitions found";
    }
    
    private String extractMethodSignatures(String complexCode) {
        Matcher matcher = METHOD_PATTERN.matcher(complexCode);
        StringBuilder methods = new StringBuilder();
        
        while (matcher.find()) {
            methods.append(matcher.group()).append("\n");
        }
        
        return methods.length() > 0 ? methods.toString() : "// No method signatures found";
    }
    
    private String extractControlFlow(String complexCode) {
        StringBuilder controlFlow = new StringBuilder();
        
        Matcher loopMatcher = LOOP_PATTERN.matcher(complexCode);
        while (loopMatcher.find()) {
            controlFlow.append("Loop: ").append(loopMatcher.group()).append("\n");
        }
        
        Matcher condMatcher = CONDITIONAL_PATTERN.matcher(complexCode);
        while (condMatcher.find()) {
            controlFlow.append("Condition: ").append(condMatcher.group()).append("\n");
        }
        
        return controlFlow.length() > 0 ? controlFlow.toString() : "// Linear execution flow";
    }
    
    private String extractExecutionPath(String complexCode) {
        return "// Execution path analysis\n" + complexCode.substring(0, Math.min(150, complexCode.length())) + "...";
    }
    
    private String generateExperimentalVariation(String complexCode) {
        return "// Try changing this:\n" + complexCode.substring(0, Math.min(100, complexCode.length())) + "\n// What happens if you modify the values?";
    }
    
    private String extractRelevantCodeForObjective(String complexCode, String objective) {
        return "// Code relevant to: " + objective + "\n" + complexCode.substring(0, Math.min(120, complexCode.length()));
    }
    
    private List<String> generateKeyPointsForObjective(String objective) {
        return Arrays.asList(
            "Understanding " + objective,
            "Practical application",
            "Best practices for " + objective
        );
    }
    
    private String extractStructuralElements(String complexCode) {
        return "// Structural elements\n" + extractMainStructure(complexCode);
    }
    
    private String extractImplementationDetails(String complexCode) {
        return "// Implementation details\n" + complexCode.substring(Math.max(0, complexCode.length() - 150));
    }
    
    private List<String> enhancePrerequisites(List<String> originalPrerequisites) {
        List<String> enhanced = new ArrayList<>(originalPrerequisites);
        enhanced.add("Detailed code analysis skills");
        enhanced.add("Understanding of software design principles");
        return enhanced;
    }
    
    private List<String> enhanceLearningObjectives(List<String> originalObjectives) {
        List<String> enhanced = new ArrayList<>(originalObjectives);
        enhanced.add("Master detailed code analysis");
        enhanced.add("Understand implementation trade-offs");
        return enhanced;
    }
    
    private String detectLanguage(String complexCode) {
        if (complexCode.contains("public class") || complexCode.contains("import java")) {
            return "Java";
        }
        if (complexCode.contains("function") || complexCode.contains("const ")) {
            return "JavaScript";
        }
        if (complexCode.contains("def ") || complexCode.contains("import ")) {
            return "Python";
        }
        return "UNKNOWN";
    }
}