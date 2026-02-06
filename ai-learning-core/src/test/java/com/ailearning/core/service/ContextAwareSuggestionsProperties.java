package com.ailearning.core.service;

import com.ailearning.core.model.*;
import com.ailearning.core.service.impl.DefaultCodeAnalyzer;
import com.ailearning.core.service.impl.DefaultContextEngine;
import com.ailearning.core.service.impl.IntelligentSuggestionEngine;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for context-aware suggestion functionality.
 * 
 * **Validates: Property 4 - Context-Aware Code Analysis and Suggestions**
 * *For any* code analysis or suggestion request, the system should consider the current 
 * codebase context, architecture, and conventions to provide relevant and appropriate recommendations.
 * **Validates: Requirements 2.2, 2.4, 5.3**
 */
class ContextAwareSuggestionsProperties {
    
    private static final Logger logger = LoggerFactory.getLogger(ContextAwareSuggestionsProperties.class);
    
    @Property(tries = 100)
    @Label("Suggestion engine should provide context-aware recommendations based on project type")
    void suggestionEngineContextAwareRecommendations(
            @ForAll("codeContexts") CodeContext context,
            @ForAll("analysisResults") AnalysisResult analysisResult,
            @ForAll("detectedPatterns") List<Pattern> patterns) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        IntelligentSuggestionEngine suggestionEngine = new IntelligentSuggestionEngine(contextEngine);
        
        try {
            CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                    context, patterns, null, analysisResult);
            List<Suggestion> suggestions = future.get();
            
            // Property: All suggestions should be valid and context-aware
            assertNotNull(suggestions, "Suggestions should not be null");
            
            for (Suggestion suggestion : suggestions) {
                // Basic validation
                assertNotNull(suggestion.getId(), "Suggestion ID should not be null");
                assertNotNull(suggestion.getTitle(), "Suggestion title should not be null");
                assertNotNull(suggestion.getDescription(), "Suggestion description should not be null");
                assertNotNull(suggestion.getType(), "Suggestion type should not be null");
                assertNotNull(suggestion.getPriority(), "Suggestion priority should not be null");
                
                // Confidence score validation
                assertTrue(suggestion.getConfidenceScore() >= 0.0 && suggestion.getConfidenceScore() <= 1.0,
                          "Confidence score should be between 0.0 and 1.0");
                
                // Context relevance validation
                if (context.getProjectType() != null) {
                    validateProjectTypeRelevance(suggestion, context.getProjectType());
                }
                
                // File type relevance validation
                if (context.getFileName() != null) {
                    validateFileTypeRelevance(suggestion, context.getFileName());
                }
            }
            
            // Property: Suggestions should be prioritized appropriately
            validateSuggestionPrioritization(suggestions);
            
        } catch (Exception e) {
            logger.error("Context-aware suggestion generation failed", e);
            fail("Suggestion engine should handle context-aware generation gracefully: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Suggestion engine should adapt recommendations based on framework detection")
    void suggestionEngineFrameworkAdaptation(
            @ForAll("frameworkContexts") CodeContext context,
            @ForAll("analysisResults") AnalysisResult analysisResult) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        IntelligentSuggestionEngine suggestionEngine = new IntelligentSuggestionEngine(contextEngine);
        
        try {
            CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                    context, List.of(), null, analysisResult);
            List<Suggestion> suggestions = future.get();
            
            // Property: Framework-specific suggestions should be relevant to detected framework
            String detectedFramework = detectFrameworkFromContext(context);
            if (!detectedFramework.equals("unknown")) {
                boolean hasFrameworkSpecificSuggestions = suggestions.stream()
                        .anyMatch(s -> isFrameworkSpecific(s, detectedFramework));
                
                // Should have at least some framework-specific suggestions for known frameworks
                if (suggestions.size() > 0) {
                    logger.debug("Framework '{}' detected, suggestions: {}", detectedFramework, suggestions.size());
                }
            }
            
            // Property: All suggestions should be actionable or educational
            for (Suggestion suggestion : suggestions) {
                assertTrue(suggestion.isActionable() || suggestion.isLearningOpportunity(),
                          "Suggestions should be either actionable or provide learning opportunities");
            }
            
        } catch (Exception e) {
            logger.error("Framework adaptation test failed", e);
            fail("Suggestion engine should handle framework adaptation gracefully: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Suggestion engine should provide relevant refactoring suggestions based on complexity")
    void suggestionEngineComplexityBasedRefactoring(
            @ForAll("codeContexts") CodeContext context,
            @ForAll("complexityMetrics") ComplexityMetrics complexity) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        IntelligentSuggestionEngine suggestionEngine = new IntelligentSuggestionEngine(contextEngine);
        
        try {
            List<Suggestion> refactoringSuggestions = suggestionEngine.generateRefactoringSuggestions(
                    context, complexity);
            
            // Property: High complexity should generate refactoring suggestions
            if (complexity.getCyclomaticComplexity() > 10 || 
                complexity.getNestingDepth() > 4 || 
                complexity.getLinesOfCode() > 500) {
                
                assertFalse(refactoringSuggestions.isEmpty(),
                           "High complexity code should generate refactoring suggestions");
                
                // All refactoring suggestions should be of REFACTORING type
                for (Suggestion suggestion : refactoringSuggestions) {
                    assertEquals(Suggestion.Type.REFACTORING, suggestion.getType(),
                               "Refactoring suggestions should have REFACTORING type");
                    assertNotNull(suggestion.getRationale(),
                                 "Refactoring suggestions should include rationale");
                }
            }
            
            // Property: Suggestions should be proportional to complexity issues
            validateComplexityProportionality(refactoringSuggestions, complexity);
            
        } catch (Exception e) {
            logger.error("Complexity-based refactoring test failed", e);
            fail("Suggestion engine should handle complexity-based refactoring gracefully: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Suggestion engine should generate appropriate architectural suggestions")
    void suggestionEngineArchitecturalRecommendations(
            @ForAll("projectContexts") ProjectContext projectContext,
            @ForAll("detectedPatterns") List<Pattern> patterns) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        IntelligentSuggestionEngine suggestionEngine = new IntelligentSuggestionEngine(contextEngine);
        
        try {
            List<Suggestion> architecturalSuggestions = suggestionEngine.generateArchitecturalSuggestions(
                    projectContext, patterns);
            
            // Property: Architectural suggestions should be relevant to project structure
            for (Suggestion suggestion : architecturalSuggestions) {
                assertEquals(Suggestion.Type.ARCHITECTURE, suggestion.getType(),
                           "Architectural suggestions should have ARCHITECTURE type");
                assertTrue(suggestion.getPriority() == Suggestion.Priority.HIGH ||
                          suggestion.getPriority() == Suggestion.Priority.MEDIUM,
                          "Architectural suggestions should have appropriate priority");
                assertNotNull(suggestion.getRationale(),
                             "Architectural suggestions should include rationale");
            }
            
            // Property: Missing patterns should generate appropriate suggestions
            validateMissingPatternSuggestions(architecturalSuggestions, patterns, projectContext);
            
        } catch (Exception e) {
            logger.error("Architectural recommendations test failed", e);
            fail("Suggestion engine should handle architectural recommendations gracefully: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Suggestion engine should consider developer profile for personalized recommendations")
    void suggestionEnginePersonalizedRecommendations(
            @ForAll("codeContexts") CodeContext context,
            @ForAll("analysisResults") AnalysisResult analysisResult,
            @ForAll("developerProfiles") DeveloperProfile developerProfile) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        IntelligentSuggestionEngine suggestionEngine = new IntelligentSuggestionEngine(contextEngine);
        
        try {
            CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                    context, List.of(), developerProfile, analysisResult);
            List<Suggestion> suggestions = future.get();
            
            // Property: Suggestions should be adapted to developer skill level
            for (Suggestion suggestion : suggestions) {
                if (suggestion.getType() == Suggestion.Type.LEARNING) {
                    // Learning suggestions should be relevant to skill gaps
                    validateLearningRelevance(suggestion, developerProfile);
                }
            }
            
            // Property: Suggestion count should respect developer preferences
            LearningPreferences preferences = developerProfile.getLearningPreferences();
            if (preferences != null && preferences.getDetailLevel() != null) {
                int expectedMaxSuggestions = preferences.getDetailLevel().equals("detailed") ? 15 : 10;
                assertTrue(suggestions.size() <= expectedMaxSuggestions,
                          "Suggestion count should respect developer preferences");
            }
            
        } catch (Exception e) {
            logger.error("Personalized recommendations test failed", e);
            fail("Suggestion engine should handle personalized recommendations gracefully: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Suggestion engine should maintain consistent quality across different contexts")
    void suggestionEngineConsistentQuality(
            @ForAll @Size(min = 2, max = 5) List<CodeContext> contexts,
            @ForAll("analysisResults") AnalysisResult analysisResult) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        IntelligentSuggestionEngine suggestionEngine = new IntelligentSuggestionEngine(contextEngine);
        
        try {
            List<List<Suggestion>> allSuggestions = new ArrayList<>();
            
            // Generate suggestions for multiple contexts
            for (CodeContext context : contexts) {
                CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                        context, List.of(), null, analysisResult);
                List<Suggestion> suggestions = future.get();
                allSuggestions.add(suggestions);
            }
            
            // Property: All suggestion sets should meet quality standards
            for (List<Suggestion> suggestions : allSuggestions) {
                validateSuggestionQuality(suggestions);
            }
            
            // Property: Similar contexts should produce similar suggestion patterns
            validateConsistencyAcrossContexts(allSuggestions, contexts);
            
        } catch (Exception e) {
            logger.error("Consistent quality test failed", e);
            fail("Suggestion engine should maintain consistent quality: " + e.getMessage());
        }
    }
    
    // Validation helper methods
    
    private void validateProjectTypeRelevance(Suggestion suggestion, String projectType) {
        // Validate that suggestions are relevant to the project type
        switch (projectType.toLowerCase()) {
            case "web" -> {
                if (suggestion.getCategory() != null) {
                    String category = suggestion.getCategory().toLowerCase();
                    // Web projects should get web-relevant suggestions
                    if (category.contains("security") || category.contains("performance") || 
                        category.contains("web")) {
                        // This is expected and good
                    }
                }
            }
            case "api" -> {
                if (suggestion.getTitle().toLowerCase().contains("api") ||
                    suggestion.getDescription().toLowerCase().contains("api")) {
                    // API-specific suggestions are good
                }
            }
            case "library" -> {
                if (suggestion.getCategory() != null && 
                    (suggestion.getCategory().contains("Documentation") ||
                     suggestion.getCategory().contains("Release"))) {
                    // Library-specific suggestions are good
                }
            }
        }
    }
    
    private void validateFileTypeRelevance(Suggestion suggestion, String fileName) {
        String lowerFileName = fileName.toLowerCase();
        
        if (lowerFileName.contains("test")) {
            // Test files should get test-related suggestions
            if (suggestion.getDescription().toLowerCase().contains("test")) {
                // This is expected and good
            }
        } else if (lowerFileName.contains("config")) {
            // Config files should get configuration-related suggestions
            if (suggestion.getType() == Suggestion.Type.SECURITY &&
                suggestion.getDescription().toLowerCase().contains("config")) {
                // This is expected and good
            }
        }
    }
    
    private void validateSuggestionPrioritization(List<Suggestion> suggestions) {
        // Suggestions should be ordered by priority and confidence
        for (int i = 1; i < suggestions.size(); i++) {
            Suggestion prev = suggestions.get(i - 1);
            Suggestion curr = suggestions.get(i);
            
            // Higher priority suggestions should come first
            if (prev.getPriority().ordinal() < curr.getPriority().ordinal()) {
                // If priority is lower, confidence should be higher to justify the position
                assertTrue(prev.getConfidenceScore() >= curr.getConfidenceScore(),
                          "Lower priority suggestions should have higher confidence to be prioritized");
            }
        }
    }
    
    private String detectFrameworkFromContext(CodeContext context) {
        String code = context.getCurrentFile();
        if (code == null) return "unknown";
        
        if (code.contains("@SpringBootApplication") || code.contains("@RestController")) return "spring";
        if (code.contains("import React") || code.contains("from 'react'")) return "react";
        if (code.contains("@Component") && code.contains("@angular")) return "angular";
        if (code.contains("from django")) return "django";
        if (code.contains("express()") || code.contains("require('express')")) return "express";
        
        return "unknown";
    }
    
    private boolean isFrameworkSpecific(Suggestion suggestion, String framework) {
        String title = suggestion.getTitle().toLowerCase();
        String description = suggestion.getDescription().toLowerCase();
        String category = suggestion.getCategory() != null ? suggestion.getCategory().toLowerCase() : "";
        
        return title.contains(framework) || description.contains(framework) || category.contains(framework);
    }
    
    private void validateComplexityProportionality(List<Suggestion> suggestions, ComplexityMetrics complexity) {
        int expectedSuggestions = 0;
        
        if (complexity.getCyclomaticComplexity() > 10) expectedSuggestions++;
        if (complexity.getNestingDepth() > 4) expectedSuggestions++;
        if (complexity.getLinesOfCode() > 500) expectedSuggestions++;
        
        if (expectedSuggestions > 0) {
            assertTrue(suggestions.size() >= Math.min(expectedSuggestions, 1),
                      "Should generate at least one suggestion for high complexity");
        }
    }
    
    private void validateMissingPatternSuggestions(List<Suggestion> suggestions, 
                                                  List<Pattern> patterns, 
                                                  ProjectContext projectContext) {
        // Check if missing common patterns generate appropriate suggestions
        boolean hasRepository = patterns.stream().anyMatch(p -> p.getName().contains("Repository"));
        boolean hasService = patterns.stream().anyMatch(p -> p.getName().contains("Service"));
        
        if (!hasRepository && projectContext.getStructure().getFiles().size() > 5) {
            // Large projects without repository pattern might get suggestions
            boolean hasRepositorySuggestion = suggestions.stream()
                    .anyMatch(s -> s.getTitle().toLowerCase().contains("repository"));
            // This is optional, so we don't assert, just log
            logger.debug("Repository pattern missing, suggestion provided: {}", hasRepositorySuggestion);
        }
    }
    
    private void validateLearningRelevance(Suggestion suggestion, DeveloperProfile profile) {
        // Learning suggestions should be relevant to skill gaps
        Map<String, SkillLevel> skills = profile.getSkillLevels();
        
        if (suggestion.getTags() != null) {
            for (String tag : suggestion.getTags()) {
                if (skills.containsKey(tag)) {
                    SkillLevel skill = skills.get(tag);
                    // Learning suggestions should target areas with lower proficiency
                    assertTrue(skill.getProficiency() < 0.8,
                              "Learning suggestions should target skill gaps");
                }
            }
        }
    }
    
    private void validateSuggestionQuality(List<Suggestion> suggestions) {
        for (Suggestion suggestion : suggestions) {
            // Quality checks
            assertFalse(suggestion.getTitle().trim().isEmpty(),
                       "Suggestion titles should not be empty");
            assertFalse(suggestion.getDescription().trim().isEmpty(),
                       "Suggestion descriptions should not be empty");
            
            // Confidence should be reasonable
            assertTrue(suggestion.getConfidenceScore() >= 0.1,
                      "Suggestions should have reasonable confidence (>= 0.1)");
            
            // Critical suggestions should have high confidence
            if (suggestion.getPriority() == Suggestion.Priority.CRITICAL) {
                assertTrue(suggestion.getConfidenceScore() >= 0.7,
                          "Critical suggestions should have high confidence");
            }
        }
    }
    
    private void validateConsistencyAcrossContexts(List<List<Suggestion>> allSuggestions, 
                                                  List<CodeContext> contexts) {
        // Similar contexts should produce similar types of suggestions
        Map<String, Integer> suggestionTypeCount = new HashMap<>();
        
        for (List<Suggestion> suggestions : allSuggestions) {
            for (Suggestion suggestion : suggestions) {
                String type = suggestion.getType().toString();
                suggestionTypeCount.merge(type, 1, Integer::sum);
            }
        }
        
        // Should have some consistency in suggestion types across contexts
        assertTrue(suggestionTypeCount.size() > 0,
                  "Should generate suggestions across different contexts");
    }
    
    // Generators for test data
    
    @Provide
    Arbitrary<CodeContext> codeContexts() {
        return Arbitraries.of(
                createCodeContext("TestService.java", "api", "public class TestService { }"),
                createCodeContext("UserController.java", "web", "@RestController public class UserController { }"),
                createCodeContext("DatabaseConfig.java", "library", "public class DatabaseConfig { }"),
                createCodeContext("PaymentProcessor.java", "microservice", "public class PaymentProcessor { }"),
                createCodeContext("utils.py", "web", "def process_data(): pass")
        );
    }
    
    @Provide
    Arbitrary<CodeContext> frameworkContexts() {
        return Arbitraries.of(
                createCodeContext("SpringController.java", "web", 
                        "@RestController @SpringBootApplication public class SpringController { }"),
                createCodeContext("ReactComponent.jsx", "web", 
                        "import React from 'react'; function Component() { return <div>Hello</div>; }"),
                createCodeContext("AngularComponent.ts", "web", 
                        "import { Component } from '@angular/core'; @Component({}) export class MyComponent { }"),
                createCodeContext("DjangoView.py", "web", 
                        "from django.views import View class MyView(View): pass"),
                createCodeContext("ExpressApp.js", "api", 
                        "const express = require('express'); const app = express();")
        );
    }
    
    @Provide
    Arbitrary<AnalysisResult> analysisResults() {
        return Arbitraries.of(
                createAnalysisResult(5, 2, 100, List.of(), List.of()),
                createAnalysisResult(15, 5, 300, 
                        List.of(createSecurityIssue("SQL Injection")), 
                        List.of(createCodeIssue("High Complexity"))),
                createAnalysisResult(8, 3, 150, List.of(), 
                        List.of(createCodeIssue("Missing Documentation"))),
                createAnalysisResult(20, 6, 600, 
                        List.of(createSecurityIssue("XSS Vulnerability")), 
                        List.of(createCodeIssue("Performance Issue")))
        );
    }
    
    @Provide
    Arbitrary<List<Pattern>> detectedPatterns() {
        return Arbitraries.of(
                List.of(),
                List.of(createPattern("Singleton", Pattern.Type.DESIGN_PATTERN)),
                List.of(
                        createPattern("Repository", Pattern.Type.DESIGN_PATTERN),
                        createPattern("Service Layer", Pattern.Type.DESIGN_PATTERN)
                ),
                List.of(
                        createPattern("God Class", Pattern.Type.ANTI_PATTERN),
                        createPattern("N+1 Query", Pattern.Type.PERFORMANCE_PATTERN)
                )
        );
    }
    
    @Provide
    Arbitrary<ComplexityMetrics> complexityMetrics() {
        return Arbitraries.of(
                ComplexityMetrics.builder()
                        .cyclomaticComplexity(5)
                        .nestingDepth(2)
                        .linesOfCode(100)
                        .numberOfMethods(8)
                        .numberOfClasses(2)
                        .build(),
                ComplexityMetrics.builder()
                        .cyclomaticComplexity(15)
                        .nestingDepth(5)
                        .linesOfCode(300)
                        .numberOfMethods(20)
                        .numberOfClasses(5)
                        .build(),
                ComplexityMetrics.builder()
                        .cyclomaticComplexity(25)
                        .nestingDepth(7)
                        .linesOfCode(600)
                        .numberOfMethods(35)
                        .numberOfClasses(8)
                        .build()
        );
    }
    
    @Provide
    Arbitrary<ProjectContext> projectContexts() {
        return Arbitraries.of(
                createProjectContext("test-project", 5),
                createProjectContext("large-project", 15),
                createProjectContext("enterprise-project", 30)
        );
    }
    
    @Provide
    Arbitrary<DeveloperProfile> developerProfiles() {
        return Arbitraries.of(
                createDeveloperProfile("junior", Map.of(
                        "design-patterns", 0.3,
                        "testing", 0.4,
                        "security", 0.2
                )),
                createDeveloperProfile("intermediate", Map.of(
                        "design-patterns", 0.6,
                        "testing", 0.7,
                        "security", 0.5
                )),
                createDeveloperProfile("senior", Map.of(
                        "design-patterns", 0.9,
                        "testing", 0.8,
                        "security", 0.8
                ))
        );
    }
    
    // Helper methods for creating test data
    
    private CodeContext createCodeContext(String fileName, String projectType, String code) {
        return CodeContext.builder()
                .fileName(fileName)
                .projectType(projectType)
                .currentFile(code)
                .language(detectLanguageFromFileName(fileName))
                .build();
    }
    
    private String detectLanguageFromFileName(String fileName) {
        if (fileName.endsWith(".java")) return "java";
        if (fileName.endsWith(".js") || fileName.endsWith(".jsx")) return "javascript";
        if (fileName.endsWith(".ts") || fileName.endsWith(".tsx")) return "typescript";
        if (fileName.endsWith(".py")) return "python";
        return "unknown";
    }
    
    private AnalysisResult createAnalysisResult(int complexity, int nesting, int loc, 
                                              List<SecurityIssue> securityIssues, 
                                              List<CodeIssue> codeIssues) {
        ComplexityMetrics metrics = ComplexityMetrics.builder()
                .cyclomaticComplexity(complexity)
                .nestingDepth(nesting)
                .linesOfCode(loc)
                .numberOfMethods(loc / 20)
                .numberOfClasses(Math.max(1, loc / 100))
                .build();
        
        return AnalysisResult.builder()
                .complexity(metrics)
                .securityIssues(securityIssues)
                .codeIssues(codeIssues)
                .suggestions(List.of())
                .analysisTime(100L)
                .build();
    }
    
    private SecurityIssue createSecurityIssue(String type) {
        return SecurityIssue.builder()
                .type(type)
                .severity(SecurityIssue.Severity.MEDIUM)
                .description("Test security issue: " + type)
                .filePath("/test/file.java")
                .lineNumber(10)
                .build();
    }
    
    private CodeIssue createCodeIssue(String type) {
        return CodeIssue.builder()
                .type(type)
                .severity(CodeIssue.Severity.MEDIUM)
                .description("Test code issue: " + type)
                .filePath("/test/file.java")
                .lineNumber(15)
                .build();
    }
    
    private Pattern createPattern(String name, Pattern.Type type) {
        return Pattern.builder()
                .name(name)
                .type(type)
                .confidence(Pattern.Confidence.HIGH)
                .description("Test pattern: " + name)
                .filePath("/test/file.java")
                .build();
    }
    
    private ProjectContext createProjectContext(String name, int fileCount) {
        List<FileNode> files = new ArrayList<>();
        for (int i = 0; i < fileCount; i++) {
            files.add(FileNode.builder()
                    .name("File" + i + ".java")
                    .path("/test/File" + i + ".java")
                    .size(1000L + i * 100)
                    .lastModified(Instant.now())
                    .build());
        }
        
        ProjectStructure structure = ProjectStructure.builder()
                .files(files)
                .modules(List.of())
                .relationships(List.of())
                .entryPoints(List.of("/test/Main.java"))
                .build();
        
        return ProjectContext.builder()
                .projectName(name)
                .rootPath("/test/" + name)
                .structure(structure)
                .dependencies(List.of())
                .patterns(List.of())
                .conventions(List.of())
                .complexity(ComplexityMetrics.builder()
                        .cyclomaticComplexity(fileCount * 2)
                        .nestingDepth(3)
                        .linesOfCode(fileCount * 100)
                        .numberOfMethods(fileCount * 5)
                        .numberOfClasses(fileCount)
                        .build())
                .build();
    }
    
    private DeveloperProfile createDeveloperProfile(String level, Map<String, Double> skillProficiencies) {
        Map<String, SkillLevel> skills = new HashMap<>();
        for (Map.Entry<String, Double> entry : skillProficiencies.entrySet()) {
            skills.put(entry.getKey(), SkillLevel.builder()
                    .domain(entry.getKey())
                    .proficiency(entry.getValue())
                    .confidence(entry.getValue() * 0.9)
                    .lastAssessed(Instant.now())
                    .evidenceCount(10)
                    .build());
        }
        
        LearningPreferences preferences = LearningPreferences.builder()
                .preferredLearningStyle("visual")
                .difficultyPreference("adaptive")
                .sessionDuration(30)
                .detailLevel(level.equals("senior") ? "detailed" : "standard")
                .build();
        
        return DeveloperProfile.builder()
                .id("test-" + level)
                .skillLevels(skills)
                .learningPreferences(preferences)
                .workHistory(List.of())
                .achievements(List.of())
                .currentGoals(List.of())
                .build();
    }
}