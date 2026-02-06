package com.ailearning.core.service;

import com.ailearning.core.model.*;
import com.ailearning.core.service.impl.DefaultCodeAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the complete static analysis engine.
 * Validates end-to-end functionality of code analysis, security detection, and pattern recognition.
 */
class StaticAnalysisIntegrationTest {
    
    private CodeAnalyzer codeAnalyzer;
    
    @BeforeEach
    void setUp() {
        codeAnalyzer = new DefaultCodeAnalyzer();
    }
    
    @Test
    @DisplayName("Static Analysis Engine - Complete Integration Test")
    void testCompleteStaticAnalysis() throws Exception {
        // Test Java code with multiple issues
        String javaCode = """
            public class UserService {
                private static String dbPassword = "admin123";
                
                public User findUser(String userId) {
                    String sql = "SELECT * FROM users WHERE id = " + userId;
                    // Execute SQL query
                    return executeQuery(sql);
                }
                
                public void processUsers() {
                    ArrayList<User> users = new ArrayList<>();
                    String result = "";
                    
                    for (int i = 0; i < 1000; i++) {
                        if (condition1(i)) {
                            if (condition2(i)) {
                                if (condition3(i)) {
                                    result += "User " + i;
                                    users.add(new User());
                                }
                            }
                        }
                    }
                    
                    System.out.println(result);
                }
                
                private boolean condition1(int i) { return i % 2 == 0; }
                private boolean condition2(int i) { return i > 10; }
                private boolean condition3(int i) { return i < 500; }
                private User executeQuery(String sql) { return null; }
            }
            """;
        
        // Perform complete analysis
        CompletableFuture<AnalysisResult> analysisResult = codeAnalyzer.analyzeCode(javaCode, "java");
        AnalysisResult result = analysisResult.get();
        
        // Validate analysis result structure
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("java", result.getLanguage());
        assertTrue(result.getCodeSize() > 0);
        assertTrue(result.getAnalysisTimeMs() >= 0);
        
        // Validate security analysis
        List<SecurityIssue> securityIssues = result.getSecurityIssues();
        assertNotNull(securityIssues);
        assertTrue(securityIssues.size() >= 2, "Should detect hardcoded password and SQL injection");
        
        // Check for hardcoded password
        boolean hasPasswordIssue = securityIssues.stream()
                .anyMatch(issue -> 
                    issue.getCategory() == SecurityIssue.Category.AUTHENTICATION &&
                    issue.getSeverity() == SecurityIssue.Severity.CRITICAL);
        assertTrue(hasPasswordIssue, "Should detect hardcoded password as critical security issue");
        
        // Check for SQL injection
        boolean hasSqlInjection = securityIssues.stream()
                .anyMatch(issue -> 
                    issue.getCategory() == SecurityIssue.Category.INJECTION &&
                    issue.getSeverity() == SecurityIssue.Severity.HIGH);
        assertTrue(hasSqlInjection, "Should detect SQL injection as high severity issue");
        
        // Validate code quality analysis
        List<CodeIssue> codeIssues = result.getIssues();
        assertNotNull(codeIssues);
        // May have quality issues like long methods, magic numbers, etc.
        
        // Validate performance suggestions
        List<Suggestion> suggestions = result.getSuggestions();
        assertNotNull(suggestions);
        
        boolean hasPerformanceSuggestion = suggestions.stream()
                .anyMatch(suggestion -> suggestion.getType() == Suggestion.Type.PERFORMANCE);
        assertTrue(hasPerformanceSuggestion, "Should provide performance improvement suggestions");
        
        // Validate complexity metrics
        ComplexityMetrics complexity = result.getComplexity();
        assertNotNull(complexity);
        assertTrue(complexity.getCyclomaticComplexity() > 5, "Should detect high cyclomatic complexity");
        assertTrue(complexity.getCognitiveComplexity() > 0, "Should calculate cognitive complexity");
        assertTrue(complexity.getLinesOfCode() > 20, "Should count lines of code correctly");
        assertTrue(complexity.getNestingDepth() >= 3, "Should detect deep nesting");
        assertTrue(complexity.getNumberOfMethods() >= 4, "Should count methods");
        assertTrue(complexity.getMaintainabilityIndex() >= 0 && 
                  complexity.getMaintainabilityIndex() <= 100, 
                  "Maintainability index should be valid");
        
        // Test pattern detection
        Codebase codebase = createTestCodebase();
        CompletableFuture<List<Pattern>> patternsResult = codeAnalyzer.detectPatterns(codebase);
        List<Pattern> patterns = patternsResult.get();
        
        assertNotNull(patterns);
        // Should detect architectural patterns like Service Layer
        boolean hasServicePattern = patterns.stream()
                .anyMatch(pattern -> pattern.getName().contains("Service"));
        assertTrue(hasServicePattern, "Should detect service layer pattern");
        
        // Test context-aware suggestions
        CodeContext context = CodeContext.builder()
                .fileName("UserService.java")
                .currentFile(javaCode)
                .projectType("api")
                .build();
        
        CompletableFuture<List<Suggestion>> contextSuggestions = codeAnalyzer.suggestImprovements(context);
        List<Suggestion> contextualSuggestions = contextSuggestions.get();
        
        assertNotNull(contextualSuggestions);
        assertFalse(contextualSuggestions.isEmpty());
        
        // Should have API-specific suggestions
        boolean hasApiSuggestion = contextualSuggestions.stream()
                .anyMatch(suggestion -> suggestion.getCategory().contains("API"));
        assertTrue(hasApiSuggestion, "Should provide API-specific suggestions");
        
        System.out.println("✅ Static Analysis Integration Test Passed!");
        System.out.println("📊 Analysis Results:");
        System.out.println("   - Security Issues: " + securityIssues.size());
        System.out.println("   - Code Issues: " + codeIssues.size());
        System.out.println("   - Suggestions: " + suggestions.size());
        System.out.println("   - Patterns: " + patterns.size());
        System.out.println("   - Cyclomatic Complexity: " + complexity.getCyclomaticComplexity());
        System.out.println("   - Analysis Time: " + result.getAnalysisTimeMs() + "ms");
    }
    
    @Test
    @DisplayName("Multi-Language Support Test")
    void testMultiLanguageSupport() throws Exception {
        // Test JavaScript
        String jsCode = """
            function processData(userInput) {
                document.getElementById('content').innerHTML = userInput;
                eval('var result = ' + userInput);
                var apiKey = "sk-1234567890";
            }
            """;
        
        CompletableFuture<AnalysisResult> jsResult = codeAnalyzer.analyzeCode(jsCode, "javascript");
        AnalysisResult jsAnalysis = jsResult.get();
        
        assertNotNull(jsAnalysis);
        assertEquals("javascript", jsAnalysis.getLanguage());
        assertFalse(jsAnalysis.getSecurityIssues().isEmpty(), "Should detect JS security issues");
        
        // Test Python
        String pythonCode = """
            import pickle
            
            def process_user_data(data):
                exec(data)
                return pickle.loads(data)
            """;
        
        CompletableFuture<AnalysisResult> pyResult = codeAnalyzer.analyzeCode(pythonCode, "python");
        AnalysisResult pyAnalysis = pyResult.get();
        
        assertNotNull(pyAnalysis);
        assertEquals("python", pyAnalysis.getLanguage());
        assertFalse(pyAnalysis.getSecurityIssues().isEmpty(), "Should detect Python security issues");
        
        System.out.println("✅ Multi-Language Support Test Passed!");
        System.out.println("   - JavaScript issues: " + jsAnalysis.getSecurityIssues().size());
        System.out.println("   - Python issues: " + pyAnalysis.getSecurityIssues().size());
    }
    
    @Test
    @DisplayName("Performance Requirements Test")
    void testPerformanceRequirements() throws Exception {
        String code = """
            public class SimpleClass {
                public void simpleMethod() {
                    System.out.println("Hello World");
                }
            }
            """;
        
        // Test that analysis completes within 100ms for simple code
        long startTime = System.currentTimeMillis();
        CompletableFuture<AnalysisResult> future = codeAnalyzer.analyzeCode(code, "java");
        AnalysisResult result = future.get();
        long endTime = System.currentTimeMillis();
        
        long analysisTime = endTime - startTime;
        assertTrue(analysisTime < 100, 
                "Simple analysis should complete within 100ms, took: " + analysisTime + "ms");
        
        assertNotNull(result);
        assertTrue(result.getAnalysisTimeMs() >= 0);
        
        System.out.println("✅ Performance Requirements Test Passed!");
        System.out.println("   - Analysis completed in: " + analysisTime + "ms");
    }
    
    private Codebase createTestCodebase() {
        return Codebase.builder()
                .rootPath("/test/project")
                .sourceFiles(List.of(
                        "UserService.java",
                        "UserRepository.java", 
                        "UserController.java"
                ))
                .fileContents(Map.of(
                        "UserService.java", "public class UserService { }",
                        "UserRepository.java", "public class UserRepository { }",
                        "UserController.java", "public class UserController { }"
                ))
                .languageMapping(Map.of(
                        "UserService.java", "java",
                        "UserRepository.java", "java",
                        "UserController.java", "java"
                ))
                .totalFiles(3)
                .totalLines(100)
                .build();
    }
}