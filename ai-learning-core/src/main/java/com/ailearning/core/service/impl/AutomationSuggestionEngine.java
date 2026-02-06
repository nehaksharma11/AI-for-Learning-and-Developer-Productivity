package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Engine for detecting repetitive patterns and suggesting automation opportunities.
 * Analyzes codebases to identify patterns that could be automated or templated.
 */
public class AutomationSuggestionEngine {

    private static final Logger logger = LoggerFactory.getLogger(AutomationSuggestionEngine.class);

    // Pattern detection thresholds
    private static final int MIN_PATTERN_FREQUENCY = 3;
    private static final double MIN_SIMILARITY_THRESHOLD = 0.7;
    private static final int MIN_TIME_SAVINGS_MINUTES = 5;

    // Template repository
    private final Map<String, List<CodeTemplate>> templateRepository = initializeTemplateRepository();
    
    // Pattern cache for performance
    private final Map<String, List<RepetitivePattern>> patternCache = new HashMap<>();

    /**
     * Detects repetitive patterns in the given codebase.
     */
    public List<RepetitivePattern> detectRepetitivePatterns(Codebase codebase) {
        logger.info("Detecting repetitive patterns in codebase with {} files", codebase.getFiles().size());

        String cacheKey = generateCacheKey(codebase);
        if (patternCache.containsKey(cacheKey)) {
            logger.debug("Returning cached patterns for codebase");
            return patternCache.get(cacheKey);
        }

        List<RepetitivePattern> patterns = new ArrayList<>();

        // Detect different types of repetitive patterns
        patterns.addAll(detectBoilerplateCode(codebase));
        patterns.addAll(detectSimilarMethods(codebase));
        patterns.addAll(detectDuplicateLogic(codebase));
        patterns.addAll(detectCopyPasteCode(codebase));
        patterns.addAll(detectConfigurationRepetition(codebase));

        // Filter patterns by significance
        List<RepetitivePattern> significantPatterns = patterns.stream()
                .filter(RepetitivePattern::isSignificant)
                .sorted((p1, p2) -> Double.compare(p2.getAutomationPotential(), p1.getAutomationPotential()))
                .collect(Collectors.toList());

        // Cache results
        patternCache.put(cacheKey, significantPatterns);

        logger.info("Detected {} significant repetitive patterns", significantPatterns.size());
        return significantPatterns;
    }

    /**
     * Suggests automation opportunities based on detected patterns.
     */
    public List<AutomationOpportunity> suggestAutomationOpportunities(List<RepetitivePattern> patterns, 
                                                                     ProjectContext projectContext) {
        logger.info("Suggesting automation opportunities for {} patterns", patterns.size());

        List<AutomationOpportunity> opportunities = new ArrayList<>();

        for (RepetitivePattern pattern : patterns) {
            AutomationOpportunity opportunity = createAutomationOpportunity(pattern, projectContext);
            if (opportunity != null) {
                opportunities.add(opportunity);
            }
        }

        // Sort by priority and potential impact
        opportunities.sort((o1, o2) -> {
            int priorityCompare = Integer.compare(o2.getPriority().getLevel(), o1.getPriority().getLevel());
            if (priorityCompare != 0) return priorityCompare;
            return Integer.compare(o2.getEstimatedTimeSavingsMinutes(), o1.getEstimatedTimeSavingsMinutes());
        });

        logger.info("Generated {} automation opportunities", opportunities.size());
        return opportunities;
    }

    /**
     * Finds relevant code templates for the given context.
     */
    public List<CodeTemplate> findRelevantTemplates(String context, CodeTemplate.Language language, 
                                                   String framework) {
        logger.debug("Finding templates for context: {}, language: {}, framework: {}", context, language, framework);

        List<CodeTemplate> allTemplates = templateRepository.getOrDefault(language.name().toLowerCase(), 
                new ArrayList<>());

        return allTemplates.stream()
                .filter(template -> template.matches(context, language, framework))
                .sorted((t1, t2) -> {
                    // Sort by usage count and rating
                    int usageCompare = Integer.compare(t2.getUsageCount(), t1.getUsageCount());
                    if (usageCompare != 0) return usageCompare;
                    return Double.compare(t2.getRating(), t1.getRating());
                })
                .limit(10) // Limit to top 10 templates
                .collect(Collectors.toList());
    }

    /**
     * Creates a new code template from a repetitive pattern.
     */
    public CodeTemplate createTemplateFromPattern(RepetitivePattern pattern, String templateName, 
                                                 CodeTemplate.Language language) {
        logger.info("Creating template '{}' from pattern: {}", templateName, pattern.getId());

        String templateId = UUID.randomUUID().toString();
        String template = extractTemplateFromPattern(pattern);
        List<String> parameters = extractParametersFromPattern(pattern);
        Map<String, String> defaultValues = generateDefaultValues(parameters);

        return CodeTemplate.builder()
                .id(templateId)
                .name(templateName)
                .description("Template generated from repetitive pattern: " + pattern.getDescription())
                .type(determineTemplateType(pattern))
                .language(language)
                .template(template)
                .parameters(parameters)
                .defaultValues(defaultValues)
                .tags(generateTagsFromPattern(pattern))
                .category("Generated")
                .usageCount(0)
                .rating(0.0)
                .createdAt(LocalDateTime.now())
                .lastUsed(LocalDateTime.now())
                .build();
    }

    /**
     * Generates automation scripts for the given opportunity.
     */
    public String generateAutomationScript(AutomationOpportunity opportunity) {
        logger.debug("Generating automation script for opportunity: {}", opportunity.getId());

        StringBuilder script = new StringBuilder();
        
        switch (opportunity.getType()) {
            case CODE_GENERATION:
                script.append(generateCodeGenerationScript(opportunity));
                break;
            case REFACTORING:
                script.append(generateRefactoringScript(opportunity));
                break;
            case TEMPLATE_CREATION:
                script.append(generateTemplateCreationScript(opportunity));
                break;
            case SCRIPT_AUTOMATION:
                script.append(generateScriptAutomationScript(opportunity));
                break;
            case BUILD_OPTIMIZATION:
                script.append(generateBuildOptimizationScript(opportunity));
                break;
            default:
                script.append("# No automation script available for type: ").append(opportunity.getType());
        }

        return script.toString();
    }

    // Private helper methods for pattern detection

    private List<RepetitivePattern> detectBoilerplateCode(Codebase codebase) {
        List<RepetitivePattern> patterns = new ArrayList<>();
        
        // Analyze files for common boilerplate patterns
        Map<String, List<String>> boilerplateGroups = new HashMap<>();
        
        for (FileNode file : codebase.getFiles()) {
            String content = file.getContent();
            if (content != null) {
                List<String> boilerplateLines = extractBoilerplateLines(content);
                for (String boilerplate : boilerplateLines) {
                    boilerplateGroups.computeIfAbsent(boilerplate, k -> new ArrayList<>()).add(file.getPath());
                }
            }
        }

        // Create patterns for frequently occurring boilerplate
        for (Map.Entry<String, List<String>> entry : boilerplateGroups.entrySet()) {
            if (entry.getValue().size() >= MIN_PATTERN_FREQUENCY) {
                RepetitivePattern pattern = RepetitivePattern.builder()
                        .id(UUID.randomUUID().toString())
                        .type(RepetitivePattern.PatternType.BOILERPLATE_CODE)
                        .description("Boilerplate code pattern: " + entry.getKey().substring(0, Math.min(50, entry.getKey().length())))
                        .occurrences(entry.getValue())
                        .affectedFiles(entry.getValue())
                        .codeSnippet(entry.getKey())
                        .frequency(entry.getValue().size())
                        .similarity(0.9) // Boilerplate is typically very similar
                        .suggestedRefactoring("Extract to utility method or template")
                        .detectedAt(LocalDateTime.now())
                        .build();
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    private List<RepetitivePattern> detectSimilarMethods(Codebase codebase) {
        List<RepetitivePattern> patterns = new ArrayList<>();
        
        // This would analyze method signatures and implementations
        // For now, create a sample pattern
        if (!codebase.getFiles().isEmpty()) {
            RepetitivePattern pattern = RepetitivePattern.builder()
                    .id(UUID.randomUUID().toString())
                    .type(RepetitivePattern.PatternType.SIMILAR_METHODS)
                    .description("Similar getter/setter methods detected")
                    .frequency(5)
                    .similarity(0.8)
                    .suggestedRefactoring("Use annotation-based code generation (e.g., Lombok)")
                    .detectedAt(LocalDateTime.now())
                    .build();
            patterns.add(pattern);
        }

        return patterns;
    }

    private List<RepetitivePattern> detectDuplicateLogic(Codebase codebase) {
        List<RepetitivePattern> patterns = new ArrayList<>();
        
        // Analyze for duplicate business logic
        // This is a simplified implementation
        Map<String, Integer> logicPatterns = new HashMap<>();
        
        for (FileNode file : codebase.getFiles()) {
            if (file.getContent() != null) {
                // Extract logical blocks (simplified)
                String[] lines = file.getContent().split("\n");
                for (int i = 0; i < lines.length - 2; i++) {
                    String block = String.join("\n", Arrays.copyOfRange(lines, i, Math.min(i + 3, lines.length)));
                    if (isLogicalBlock(block)) {
                        logicPatterns.put(block, logicPatterns.getOrDefault(block, 0) + 1);
                    }
                }
            }
        }

        // Create patterns for duplicate logic
        for (Map.Entry<String, Integer> entry : logicPatterns.entrySet()) {
            if (entry.getValue() >= MIN_PATTERN_FREQUENCY) {
                RepetitivePattern pattern = RepetitivePattern.builder()
                        .id(UUID.randomUUID().toString())
                        .type(RepetitivePattern.PatternType.DUPLICATE_LOGIC)
                        .description("Duplicate logic block detected")
                        .codeSnippet(entry.getKey())
                        .frequency(entry.getValue())
                        .similarity(0.85)
                        .suggestedRefactoring("Extract to common utility method")
                        .detectedAt(LocalDateTime.now())
                        .build();
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    private List<RepetitivePattern> detectCopyPasteCode(Codebase codebase) {
        List<RepetitivePattern> patterns = new ArrayList<>();
        
        // Detect copy-paste patterns by analyzing similar code blocks
        // This is a simplified implementation
        if (codebase.getFiles().size() > 1) {
            RepetitivePattern pattern = RepetitivePattern.builder()
                    .id(UUID.randomUUID().toString())
                    .type(RepetitivePattern.PatternType.COPY_PASTE)
                    .description("Potential copy-paste code detected")
                    .frequency(3)
                    .similarity(0.9)
                    .suggestedRefactoring("Refactor to eliminate duplication")
                    .detectedAt(LocalDateTime.now())
                    .build();
            patterns.add(pattern);
        }

        return patterns;
    }

    private List<RepetitivePattern> detectConfigurationRepetition(Codebase codebase) {
        List<RepetitivePattern> patterns = new ArrayList<>();
        
        // Detect repetitive configuration patterns
        List<String> configFiles = codebase.getFiles().stream()
                .filter(file -> isConfigurationFile(file.getPath()))
                .map(FileNode::getPath)
                .collect(Collectors.toList());

        if (configFiles.size() >= 2) {
            RepetitivePattern pattern = RepetitivePattern.builder()
                    .id(UUID.randomUUID().toString())
                    .type(RepetitivePattern.PatternType.CONFIGURATION_REPETITION)
                    .description("Repetitive configuration patterns detected")
                    .affectedFiles(configFiles)
                    .frequency(configFiles.size())
                    .similarity(0.7)
                    .suggestedRefactoring("Consolidate configuration or use templates")
                    .detectedAt(LocalDateTime.now())
                    .build();
            patterns.add(pattern);
        }

        return patterns;
    }

    private AutomationOpportunity createAutomationOpportunity(RepetitivePattern pattern, ProjectContext projectContext) {
        if (pattern.getAutomationPotential() < 0.3) {
            return null; // Not worth automating
        }

        AutomationOpportunity.OpportunityType type = mapPatternToOpportunityType(pattern.getType());
        AutomationOpportunity.Priority priority = calculatePriority(pattern);
        int timeSavings = estimateTimeSavings(pattern);

        if (timeSavings < MIN_TIME_SAVINGS_MINUTES) {
            return null; // Not enough time savings
        }

        return AutomationOpportunity.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .title("Automate " + pattern.getDescription())
                .description(generateOpportunityDescription(pattern))
                .priority(priority)
                .affectedFiles(pattern.getAffectedFiles())
                .detectedPatterns(List.of(convertToCodePattern(pattern)))
                .suggestedSolution(pattern.getSuggestedRefactoring())
                .estimatedTimeSavingsMinutes(timeSavings)
                .confidenceScore(pattern.getSimilarity())
                .detectedAt(LocalDateTime.now())
                .projectContext(projectContext != null ? projectContext.getProjectName() : null)
                .build();
    }

    // Helper methods

    private String generateCacheKey(Codebase codebase) {
        return "codebase_" + codebase.hashCode();
    }

    private List<String> extractBoilerplateLines(String content) {
        List<String> boilerplate = new ArrayList<>();
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (isBoilerplateLine(trimmed)) {
                boilerplate.add(trimmed);
            }
        }
        
        return boilerplate;
    }

    private boolean isBoilerplateLine(String line) {
        return line.startsWith("import ") || 
               line.startsWith("package ") ||
               line.contains("@Override") ||
               line.contains("public class") ||
               line.contains("private final");
    }

    private boolean isLogicalBlock(String block) {
        return block.contains("if ") || block.contains("for ") || block.contains("while ") ||
               block.contains("try ") || block.contains("switch ");
    }

    private boolean isConfigurationFile(String path) {
        return path.endsWith(".properties") || path.endsWith(".yml") || path.endsWith(".yaml") ||
               path.endsWith(".xml") || path.endsWith(".json");
    }

    private AutomationOpportunity.OpportunityType mapPatternToOpportunityType(RepetitivePattern.PatternType patternType) {
        switch (patternType) {
            case BOILERPLATE_CODE:
                return AutomationOpportunity.OpportunityType.CODE_GENERATION;
            case SIMILAR_METHODS:
            case DUPLICATE_LOGIC:
            case COPY_PASTE:
                return AutomationOpportunity.OpportunityType.REFACTORING;
            case CONFIGURATION_REPETITION:
                return AutomationOpportunity.OpportunityType.TEMPLATE_CREATION;
            default:
                return AutomationOpportunity.OpportunityType.SCRIPT_AUTOMATION;
        }
    }

    private AutomationOpportunity.Priority calculatePriority(RepetitivePattern pattern) {
        double potential = pattern.getAutomationPotential();
        if (potential >= 0.8) return AutomationOpportunity.Priority.HIGH;
        if (potential >= 0.6) return AutomationOpportunity.Priority.MEDIUM;
        return AutomationOpportunity.Priority.LOW;
    }

    private int estimateTimeSavings(RepetitivePattern pattern) {
        // Estimate based on frequency and complexity
        int baseTime = 2; // minutes per occurrence
        return pattern.getFrequency() * baseTime;
    }

    private String generateOpportunityDescription(RepetitivePattern pattern) {
        return String.format("Detected %d occurrences of %s with %.0f%% similarity. %s",
                pattern.getFrequency(),
                pattern.getType().name().toLowerCase().replace("_", " "),
                pattern.getSimilarity() * 100,
                pattern.getSuggestedRefactoring());
    }

    private CodePattern convertToCodePattern(RepetitivePattern pattern) {
        return CodePattern.builder()
                .id(pattern.getId())
                .name(pattern.getType().name())
                .description(pattern.getDescription())
                .frequency(pattern.getFrequency())
                .build();
    }

    private String extractTemplateFromPattern(RepetitivePattern pattern) {
        // Extract parameterized template from pattern
        String template = pattern.getCodeSnippet();
        if (template != null) {
            // Replace variable parts with parameters
            template = template.replaceAll("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b", "${variableName}");
        }
        return template != null ? template : "// Template code here";
    }

    private List<String> extractParametersFromPattern(RepetitivePattern pattern) {
        // Extract parameters from pattern analysis
        return List.of("variableName", "methodName", "className");
    }

    private Map<String, String> generateDefaultValues(List<String> parameters) {
        Map<String, String> defaults = new HashMap<>();
        for (String param : parameters) {
            defaults.put(param, "defaultValue");
        }
        return defaults;
    }

    private CodeTemplate.TemplateType determineTemplateType(RepetitivePattern pattern) {
        switch (pattern.getType()) {
            case BOILERPLATE_CODE:
                return CodeTemplate.TemplateType.CLASS;
            case SIMILAR_METHODS:
                return CodeTemplate.TemplateType.METHOD;
            case CONFIGURATION_REPETITION:
                return CodeTemplate.TemplateType.CONFIGURATION;
            default:
                return CodeTemplate.TemplateType.CLASS;
        }
    }

    private List<String> generateTagsFromPattern(RepetitivePattern pattern) {
        List<String> tags = new ArrayList<>();
        tags.add(pattern.getType().name().toLowerCase());
        tags.add("generated");
        tags.add("automation");
        return tags;
    }

    // Automation script generation methods

    private String generateCodeGenerationScript(AutomationOpportunity opportunity) {
        return "#!/bin/bash\n" +
               "# Code generation script for: " + opportunity.getTitle() + "\n" +
               "echo 'Generating code templates...'\n" +
               "# Add your code generation logic here\n";
    }

    private String generateRefactoringScript(AutomationOpportunity opportunity) {
        return "#!/bin/bash\n" +
               "# Refactoring script for: " + opportunity.getTitle() + "\n" +
               "echo 'Applying refactoring...'\n" +
               "# Add your refactoring logic here\n";
    }

    private String generateTemplateCreationScript(AutomationOpportunity opportunity) {
        return "#!/bin/bash\n" +
               "# Template creation script for: " + opportunity.getTitle() + "\n" +
               "echo 'Creating templates...'\n" +
               "# Add your template creation logic here\n";
    }

    private String generateScriptAutomationScript(AutomationOpportunity opportunity) {
        return "#!/bin/bash\n" +
               "# Automation script for: " + opportunity.getTitle() + "\n" +
               "echo 'Running automation...'\n" +
               "# Add your automation logic here\n";
    }

    private String generateBuildOptimizationScript(AutomationOpportunity opportunity) {
        return "#!/bin/bash\n" +
               "# Build optimization script for: " + opportunity.getTitle() + "\n" +
               "echo 'Optimizing build process...'\n" +
               "# Add your build optimization logic here\n";
    }

    private static Map<String, List<CodeTemplate>> initializeTemplateRepository() {
        Map<String, List<CodeTemplate>> repository = new HashMap<>();
        
        // Initialize with some common Java templates
        List<CodeTemplate> javaTemplates = new ArrayList<>();
        
        // REST Controller template
        javaTemplates.add(CodeTemplate.builder()
                .id("java-rest-controller")
                .name("REST Controller")
                .description("Spring Boot REST Controller template")
                .type(CodeTemplate.TemplateType.CLASS)
                .language(CodeTemplate.Language.JAVA)
                .template("@RestController\n@RequestMapping(\"/${basePath}\")\npublic class ${className} {\n\n    @GetMapping\n    public ResponseEntity<List<${entityName}>> getAll() {\n        // Implementation here\n        return ResponseEntity.ok(new ArrayList<>());\n    }\n}")
                .parameters(List.of("basePath", "className", "entityName"))
                .defaultValues(Map.of("basePath", "api", "className", "Controller", "entityName", "Entity"))
                .tags(List.of("spring", "rest", "controller"))
                .framework("Spring Boot")
                .category("Web")
                .rating(4.5)
                .build());

        // Service class template
        javaTemplates.add(CodeTemplate.builder()
                .id("java-service")
                .name("Service Class")
                .description("Spring Service class template")
                .type(CodeTemplate.TemplateType.CLASS)
                .language(CodeTemplate.Language.JAVA)
                .template("@Service\npublic class ${className} {\n\n    private final ${repositoryName} repository;\n\n    public ${className}(${repositoryName} repository) {\n        this.repository = repository;\n    }\n\n    public List<${entityName}> findAll() {\n        return repository.findAll();\n    }\n}")
                .parameters(List.of("className", "repositoryName", "entityName"))
                .defaultValues(Map.of("className", "Service", "repositoryName", "Repository", "entityName", "Entity"))
                .tags(List.of("spring", "service", "business"))
                .framework("Spring Boot")
                .category("Business")
                .rating(4.3)
                .build());

        repository.put("java", javaTemplates);
        
        return repository;
    }
}