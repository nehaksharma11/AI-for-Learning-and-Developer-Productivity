package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.ContextEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for IntelligentSuggestionEngine.
 * Tests context-aware, pattern-based, and framework-specific suggestions.
 */
class IntelligentSuggestionEngineTest {
    
    @Mock
    private ContextEngine contextEngine;
    
    private IntelligentSuggestionEngine suggestionEngine;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        suggestionEngine = new IntelligentSuggestionEngine(contextEngine);
    }
    
    @Test
    @DisplayName("Should generate pattern-based suggestions for anti-patterns")
    void testPatternBasedSuggestions() throws Exception {
        // Given
        CodeContext context = createJavaContext("GodClass.java", """
            public class GodClass {
                // Very large class with many responsibilities
            }
            """);
        
        List<Pattern> patterns = List.of(
                Pattern.builder()
                        .id("god-class-1")
                        .name("God Class Anti-Pattern")
                        .description("Class with too many responsibilities")
                        .type(Pattern.Type.ANTI_PATTERN)
                        .confidence(Pattern.Confidence.HIGH)
                        .files(List.of("GodClass.java"))
                        .build()
        );
        
        AnalysisResult analysisResult = createBasicAnalysisResult();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                context, patterns, null, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        boolean hasGodClassSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("God Class") && 
                              s.getType() == Suggestion.Type.REFACTORING);
        assertTrue(hasGodClassSuggestion, "Should suggest refactoring for God Class anti-pattern");
    }
    
    @Test
    @DisplayName("Should generate framework-specific suggestions for Spring Boot")
    void testSpringBootSuggestions() throws Exception {
        // Given
        CodeContext context = createJavaContext("UserController.java", """
            @RestController
            public class UserController {
                @Autowired
                private UserService userService;
                
                @PostMapping("/users")
                public User createUser(@RequestBody User user) {
                    return userService.save(user);
                }
            }
            """);
        
        AnalysisResult analysisResult = createBasicAnalysisResult();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                context, List.of(), null, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        
        boolean hasValidationSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Validation") && 
                              s.getCategory().equals("Spring Boot"));
        assertTrue(hasValidationSuggestion, "Should suggest adding validation for Spring Boot controller");
        
        boolean hasConstructorInjectionSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Constructor Injection"));
        assertTrue(hasConstructorInjectionSuggestion, "Should suggest constructor injection over field injection");
    }
    
    @Test
    @DisplayName("Should generate React-specific suggestions")
    void testReactSuggestions() throws Exception {
        // Given
        CodeContext context = createJavaScriptContext("UserComponent.jsx", """
            import React, { useState, useEffect } from 'react';
            
            function UserComponent() {
                const [users, setUsers] = useState([]);
                
                useEffect(() => {
                    fetchUsers().then(setUsers);
                }, []);
                
                return <div>{users.map(user => <div key={user.id}>{user.name}</div>)}</div>;
            }
            """);
        
        AnalysisResult analysisResult = createBasicAnalysisResult();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                context, List.of(), null, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        
        boolean hasReactSuggestion = suggestions.stream()
                .anyMatch(s -> s.getCategory().equals("React"));
        assertTrue(hasReactSuggestion, "Should provide React-specific suggestions");
    }
    
    @Test
    @DisplayName("Should generate context-aware suggestions for API projects")
    void testApiProjectSuggestions() throws Exception {
        // Given
        CodeContext context = CodeContext.builder()
                .fileName("UserController.java")
                .currentFile("@RestController public class UserController { }")
                .projectType("api")
                .build();
        
        AnalysisResult analysisResult = createBasicAnalysisResult();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                context, List.of(), null, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        
        boolean hasApiVersioningSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("API Versioning"));
        assertTrue(hasApiVersioningSuggestion, "Should suggest API versioning for API projects");
        
        boolean hasRateLimitingSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Rate Limiting"));
        assertTrue(hasRateLimitingSuggestion, "Should suggest rate limiting for API projects");
    }
    
    @Test
    @DisplayName("Should generate web project security suggestions")
    void testWebProjectSuggestions() throws Exception {
        // Given
        CodeContext context = CodeContext.builder()
                .fileName("LoginController.java")
                .currentFile("@Controller public class LoginController { }")
                .projectType("web")
                .build();
        
        AnalysisResult analysisResult = createBasicAnalysisResult();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                context, List.of(), null, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        
        boolean hasSecuritySuggestion = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.SECURITY && 
                              s.getTitle().contains("CSRF"));
        assertTrue(hasSecuritySuggestion, "Should suggest CSRF protection for web projects");
        
        boolean hasPerformanceSuggestion = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.PERFORMANCE);
        assertTrue(hasPerformanceSuggestion, "Should suggest performance optimizations for web projects");
    }
    
    @Test
    @DisplayName("Should generate refactoring suggestions based on complexity")
    void testRefactoringSuggestions() {
        // Given
        CodeContext context = createJavaContext("ComplexClass.java", "public class ComplexClass { }");
        ComplexityMetrics highComplexity = ComplexityMetrics.builder()
                .cyclomaticComplexity(15)
                .nestingDepth(5)
                .linesOfCode(600)
                .build();
        
        // When
        List<Suggestion> suggestions = suggestionEngine.generateRefactoringSuggestions(context, highComplexity);
        
        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        boolean hasExtractMethodSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Extract Method"));
        assertTrue(hasExtractMethodSuggestion, "Should suggest method extraction for high complexity");
        
        boolean hasReduceNestingSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Reduce Nesting"));
        assertTrue(hasReduceNestingSuggestion, "Should suggest reducing nesting for deep nesting");
        
        boolean hasSplitClassSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Split Large Class"));
        assertTrue(hasSplitClassSuggestion, "Should suggest splitting large classes");
    }
    
    @Test
    @DisplayName("Should generate architectural suggestions")
    void testArchitecturalSuggestions() {
        // Given
        ProjectContext projectContext = createProjectContext();
        List<Pattern> patterns = List.of(); // No existing patterns
        
        // When
        List<Suggestion> suggestions = suggestionEngine.generateArchitecturalSuggestions(
                projectContext, patterns);
        
        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        boolean hasRepositoryPatternSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Repository Pattern"));
        assertTrue(hasRepositoryPatternSuggestion, "Should suggest Repository pattern for data access");
        
        boolean hasServiceLayerSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Service Layer"));
        assertTrue(hasServiceLayerSuggestion, "Should suggest Service Layer pattern");
    }
    
    @Test
    @DisplayName("Should generate learning-oriented suggestions based on developer profile")
    void testLearningOrientedSuggestions() throws Exception {
        // Given
        DeveloperProfile profile = createDeveloperProfile();
        CodeContext context = createJavaContext("TestClass.java", "public class TestClass { }");
        AnalysisResult analysisResult = createComplexAnalysisResult();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                context, List.of(), profile, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        
        boolean hasLearningSuggestion = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.LEARNING);
        assertTrue(hasLearningSuggestion, "Should provide learning suggestions based on profile");
        
        boolean hasDesignPatternSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Design Patterns"));
        assertTrue(hasDesignPatternSuggestion, "Should suggest learning design patterns for low skill level");
    }
    
    @Test
    @DisplayName("Should prioritize suggestions correctly")
    void testSuggestionPrioritization() throws Exception {
        // Given
        CodeContext context = createJavaContext("SecurityIssueClass.java", """
            public class SecurityIssueClass {
                private String password = "hardcoded123";
            }
            """);
        
        AnalysisResult analysisResult = createAnalysisResultWithSecurityIssues();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                context, List.of(), null, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // Security suggestions should be prioritized
        Suggestion firstSuggestion = suggestions.get(0);
        assertTrue(firstSuggestion.getPriority() == Suggestion.Priority.CRITICAL ||
                  firstSuggestion.getPriority() == Suggestion.Priority.HIGH,
                  "High priority suggestions should be first");
    }
    
    @Test
    @DisplayName("Should limit suggestions to avoid overwhelming users")
    void testSuggestionLimiting() throws Exception {
        // Given
        CodeContext context = createJavaContext("TestClass.java", """
            public class TestClass {
                // Code with many potential improvements
            }
            """);
        
        AnalysisResult analysisResult = createBasicAnalysisResult();
        DeveloperProfile profile = createDeveloperProfile();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                context, List.of(), profile, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        assertTrue(suggestions.size() <= 15, "Should limit suggestions to avoid overwhelming users");
    }
    
    @Test
    @DisplayName("Should handle empty context gracefully")
    void testEmptyContext() throws Exception {
        // Given
        CodeContext emptyContext = CodeContext.builder().build();
        AnalysisResult analysisResult = createBasicAnalysisResult();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                emptyContext, List.of(), null, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        // Should not crash and may return empty list or general suggestions
    }
    
    @Test
    @DisplayName("Should provide actionable suggestions with examples")
    void testActionableSuggestions() throws Exception {
        // Given
        CodeContext context = createJavaContext("UserService.java", """
            @Service
            public class UserService {
                public void saveUser(User user) {
                    // Save user logic
                }
            }
            """);
        
        AnalysisResult analysisResult = createBasicAnalysisResult();
        
        // When
        CompletableFuture<List<Suggestion>> future = suggestionEngine.generateSuggestions(
                context, List.of(), null, analysisResult);
        List<Suggestion> suggestions = future.get();
        
        // Then
        assertNotNull(suggestions);
        
        boolean hasActionableSuggestion = suggestions.stream()
                .anyMatch(s -> s.isActionable() && 
                              (s.getExample() != null || s.getCodeExample() != null));
        assertTrue(hasActionableSuggestion, "Should provide actionable suggestions with examples");
    }
    
    // Helper methods
    
    private CodeContext createJavaContext(String fileName, String code) {
        return CodeContext.builder()
                .fileName(fileName)
                .currentFile(code)
                .projectType("library")
                .build();
    }
    
    private CodeContext createJavaScriptContext(String fileName, String code) {
        return CodeContext.builder()
                .fileName(fileName)
                .currentFile(code)
                .projectType("web")
                .build();
    }
    
    private AnalysisResult createBasicAnalysisResult() {
        return AnalysisResult.builder()
                .id("test-analysis")
                .language("java")
                .codeSize(100)
                .analysisTimeMs(50)
                .issues(List.of())
                .securityIssues(List.of())
                .suggestions(List.of())
                .complexity(ComplexityMetrics.builder()
                        .cyclomaticComplexity(5)
                        .cognitiveComplexity(3)
                        .linesOfCode(50)
                        .build())
                .build();
    }
    
    private AnalysisResult createComplexAnalysisResult() {
        return AnalysisResult.builder()
                .id("complex-analysis")
                .language("java")
                .codeSize(1000)
                .analysisTimeMs(100)
                .issues(List.of())
                .securityIssues(List.of())
                .suggestions(List.of())
                .complexity(ComplexityMetrics.builder()
                        .cyclomaticComplexity(20)
                        .cognitiveComplexity(15)
                        .linesOfCode(500)
                        .build())
                .build();
    }
    
    private AnalysisResult createAnalysisResultWithSecurityIssues() {
        SecurityIssue securityIssue = SecurityIssue.builder()
                .id("hardcoded-password")
                .title("Hardcoded Password")
                .description("Password hardcoded in source code")
                .severity(SecurityIssue.Severity.CRITICAL)
                .category(SecurityIssue.Category.AUTHENTICATION)
                .build();
        
        return AnalysisResult.builder()
                .id("security-analysis")
                .language("java")
                .codeSize(100)
                .analysisTimeMs(50)
                .issues(List.of())
                .securityIssues(List.of(securityIssue))
                .suggestions(List.of())
                .complexity(ComplexityMetrics.builder()
                        .cyclomaticComplexity(5)
                        .cognitiveComplexity(3)
                        .linesOfCode(50)
                        .build())
                .build();
    }
    
    private DeveloperProfile createDeveloperProfile() {
        Map<String, SkillLevel> skills = new HashMap<>();
        skills.put("design-patterns", SkillLevel.builder()
                .domain("design-patterns")
                .proficiency(0.4) // Low proficiency
                .confidence(0.3)
                .build());
        skills.put("testing", SkillLevel.builder()
                .domain("testing")
                .proficiency(0.5) // Medium proficiency
                .confidence(0.4)
                .build());
        
        return DeveloperProfile.builder()
                .id("test-developer")
                .skillLevels(skills)
                .learningPreferences(LearningPreferences.builder()
                        .detailLevel("detailed")
                        .build())
                .build();
    }
    
    private ProjectContext createProjectContext() {
        List<FileNode> files = List.of(
                FileNode.builder()
                        .name("UserController.java")
                        .path("src/main/java/UserController.java")
                        .build(),
                FileNode.builder()
                        .name("UserService.java")
                        .path("src/main/java/UserService.java")
                        .build(),
                FileNode.builder()
                        .name("UserData.java")
                        .path("src/main/java/data/UserData.java")
                        .build()
        );
        
        ProjectStructure structure = ProjectStructure.builder()
                .files(files)
                .build();
        
        return ProjectContext.builder()
                .id("test-project")
                .structure(structure)
                .build();
    }
}