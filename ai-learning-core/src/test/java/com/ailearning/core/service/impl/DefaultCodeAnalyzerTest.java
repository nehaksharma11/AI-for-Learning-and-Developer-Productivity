package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.CodeAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for DefaultCodeAnalyzer.
 * Tests static analysis capabilities including security, quality, and performance analysis.
 */
class DefaultCodeAnalyzerTest {
    
    private CodeAnalyzer codeAnalyzer;
    
    @BeforeEach
    void setUp() {
        codeAnalyzer = new DefaultCodeAnalyzer();
    }
    
    @Test
    @DisplayName("Should support multiple programming languages")
    void testSupportedLanguages() {
        assertTrue(codeAnalyzer.supportsLanguage("java"));
        assertTrue(codeAnalyzer.supportsLanguage("javascript"));
        assertTrue(codeAnalyzer.supportsLanguage("typescript"));
        assertTrue(codeAnalyzer.supportsLanguage("python"));
        assertFalse(codeAnalyzer.supportsLanguage("cobol"));
        
        List<String> supportedLanguages = codeAnalyzer.getSupportedLanguages();
        assertTrue(supportedLanguages.contains("java"));
        assertTrue(supportedLanguages.contains("javascript"));
        assertTrue(supportedLanguages.size() >= 4);
    }
    
    @Test
    @DisplayName("Should analyze Java code and detect issues")
    void testAnalyzeJavaCode() throws Exception {
        String javaCode = """
            public class TestClass {
                private static String password = "hardcoded123";
                
                public void method() {
                    String sql = "SELECT * FROM users WHERE id = " + userId;
                    System.out.println("Debug message");
                }
                
                public void longMethod() {
                    if (condition1) {
                        if (condition2) {
                            if (condition3) {
                                // deeply nested code
                            }
                        }
                    }
                }
            }
            """;
        
        CompletableFuture<AnalysisResult> future = codeAnalyzer.analyzeCode(javaCode, "java");
        AnalysisResult result = future.get();
        
        assertNotNull(result);
        assertEquals("java", result.getLanguage());
        assertTrue(result.getCodeSize() > 0);
        assertTrue(result.getAnalysisTimeMs() >= 0);
        
        // Should detect security issues
        List<SecurityIssue> securityIssues = result.getSecurityIssues();
        assertFalse(securityIssues.isEmpty());
        
        boolean hasPasswordIssue = securityIssues.stream()
                .anyMatch(issue -> issue.getCategory() == SecurityIssue.Category.AUTHENTICATION);
        assertTrue(hasPasswordIssue, "Should detect hardcoded password");
        
        boolean hasSqlInjection = securityIssues.stream()
                .anyMatch(issue -> issue.getCategory() == SecurityIssue.Category.INJECTION);
        assertTrue(hasSqlInjection, "Should detect SQL injection risk");
        
        // Should have complexity metrics
        ComplexityMetrics complexity = result.getComplexity();
        assertNotNull(complexity);
        assertTrue(complexity.getCyclomaticComplexity() > 1);
        assertTrue(complexity.getLinesOfCode() > 0);
    }
    
    @Test
    @DisplayName("Should analyze JavaScript code and detect XSS risks")
    void testAnalyzeJavaScriptCode() throws Exception {
        String jsCode = """
            function updateContent(userInput) {
                document.getElementById('content').innerHTML = userInput;
                eval('var x = ' + userInput);
                
                var apiKey = "sk-1234567890abcdef";
                console.log("Debug: " + apiKey);
            }
            
            function nestedCallback() {
                getData(function(data) {
                    processData(data, function(result) {
                        saveResult(result, function(saved) {
                            console.log("Saved");
                        });
                    });
                });
            }
            """;
        
        CompletableFuture<AnalysisResult> future = codeAnalyzer.analyzeCode(jsCode, "javascript");
        AnalysisResult result = future.get();
        
        assertNotNull(result);
        assertEquals("javascript", result.getLanguage());
        
        // Should detect security issues
        List<SecurityIssue> securityIssues = result.getSecurityIssues();
        assertFalse(securityIssues.isEmpty());
        
        boolean hasXssRisk = securityIssues.stream()
                .anyMatch(issue -> issue.getCategory() == SecurityIssue.Category.XSS);
        assertTrue(hasXssRisk, "Should detect XSS risk from innerHTML");
        
        boolean hasEvalRisk = securityIssues.stream()
                .anyMatch(issue -> issue.getCategory() == SecurityIssue.Category.INJECTION);
        assertTrue(hasEvalRisk, "Should detect eval() usage risk");
    }
    
    @Test
    @DisplayName("Should analyze Python code and detect security issues")
    void testAnalyzePythonCode() throws Exception {
        String pythonCode = """
            import pickle
            
            def process_data(user_input):
                # Dangerous operations
                exec(user_input)
                data = pickle.loads(user_input)
                
                # SQL injection risk
                query = "SELECT * FROM users WHERE name = '%s'" % user_input
                
                return data
            """;
        
        CompletableFuture<AnalysisResult> future = codeAnalyzer.analyzeCode(pythonCode, "python");
        AnalysisResult result = future.get();
        
        assertNotNull(result);
        assertEquals("python", result.getLanguage());
        
        // Should detect multiple security issues
        List<SecurityIssue> securityIssues = result.getSecurityIssues();
        assertTrue(securityIssues.size() >= 2, "Should detect multiple security issues");
        
        boolean hasExecRisk = securityIssues.stream()
                .anyMatch(issue -> issue.getTitle().contains("exec"));
        assertTrue(hasExecRisk, "Should detect exec() usage");
        
        boolean hasPickleRisk = securityIssues.stream()
                .anyMatch(issue -> issue.getTitle().contains("Pickle"));
        assertTrue(hasPickleRisk, "Should detect unsafe pickle usage");
    }
    
    @Test
    @DisplayName("Should suggest improvements based on code context")
    void testSuggestImprovements() throws Exception {
        CodeContext context = CodeContext.builder()
                .fileName("TestService.java")
                .currentFile("""
                    public class TestService {
                        public void process() {
                            ArrayList<String> list = new ArrayList<>();
                            String result = "";
                            for (int i = 0; i < 1000; i++) {
                                result += "item" + i;
                            }
                        }
                    }
                    """)
                .projectType("api")
                .build();
        
        CompletableFuture<List<Suggestion>> future = codeAnalyzer.suggestImprovements(context);
        List<Suggestion> suggestions = future.get();
        
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // Should have performance suggestions
        boolean hasPerformanceSuggestion = suggestions.stream()
                .anyMatch(suggestion -> suggestion.getType() == Suggestion.Type.PERFORMANCE);
        assertTrue(hasPerformanceSuggestion, "Should suggest performance improvements");
        
        // Should have project-specific suggestions for API projects
        boolean hasApiSuggestion = suggestions.stream()
                .anyMatch(suggestion -> suggestion.getCategory().contains("API"));
        assertTrue(hasApiSuggestion, "Should provide API-specific suggestions");
    }
    
    @Test
    @DisplayName("Should detect design patterns in codebase")
    void testDetectPatterns() throws Exception {
        Codebase codebase = Codebase.builder()
                .rootPath("/test/project")
                .sourceFiles(List.of("Singleton.java", "Builder.java"))
                .fileContents(Map.of(
                        "Singleton.java", """
                            public class Singleton {
                                private static Singleton instance;
                                
                                private Singleton() {}
                                
                                public static Singleton getInstance() {
                                    if (instance == null) {
                                        instance = new Singleton();
                                    }
                                    return instance;
                                }
                            }
                            """,
                        "Builder.java", """
                            public class Product {
                                public static class Builder {
                                    public Builder setName(String name) { return this; }
                                    public Product build() { return new Product(); }
                                }
                            }
                            """
                ))
                .languageMapping(Map.of(
                        "Singleton.java", "java",
                        "Builder.java", "java"
                ))
                .build();
        
        CompletableFuture<List<Pattern>> future = codeAnalyzer.detectPatterns(codebase);
        List<Pattern> patterns = future.get();
        
        assertNotNull(patterns);
        assertFalse(patterns.isEmpty());
        
        boolean hasSingleton = patterns.stream()
                .anyMatch(pattern -> pattern.getName().contains("Singleton"));
        assertTrue(hasSingleton, "Should detect Singleton pattern");
        
        boolean hasBuilder = patterns.stream()
                .anyMatch(pattern -> pattern.getName().contains("Builder"));
        assertTrue(hasBuilder, "Should detect Builder pattern");
    }
    
    @Test
    @DisplayName("Should validate security for code snippets")
    void testValidateSecurity() throws Exception {
        String insecureCode = """
            String password = "admin123";
            String query = "SELECT * FROM users WHERE id = " + userId;
            eval(userInput);
            """;
        
        CompletableFuture<List<SecurityIssue>> future = codeAnalyzer.validateSecurity(insecureCode);
        List<SecurityIssue> issues = future.get();
        
        assertNotNull(issues);
        assertFalse(issues.isEmpty());
        
        // Should detect multiple security issues
        assertTrue(issues.size() >= 2, "Should detect multiple security vulnerabilities");
        
        // Verify issue details
        for (SecurityIssue issue : issues) {
            assertNotNull(issue.getId());
            assertNotNull(issue.getTitle());
            assertNotNull(issue.getDescription());
            assertNotNull(issue.getSeverity());
            assertNotNull(issue.getCategory());
        }
    }
    
    @Test
    @DisplayName("Should handle unsupported languages gracefully")
    void testUnsupportedLanguage() throws Exception {
        String code = "IDENTIFICATION DIVISION.";
        
        CompletableFuture<AnalysisResult> future = codeAnalyzer.analyzeCode(code, "cobol");
        AnalysisResult result = future.get();
        
        assertNotNull(result);
        assertEquals("cobol", result.getLanguage());
        
        // Should have an informational issue about unsupported language
        List<CodeIssue> issues = result.getIssues();
        assertFalse(issues.isEmpty());
        
        CodeIssue unsupportedIssue = issues.get(0);
        assertEquals("unsupported-language", unsupportedIssue.getId());
        assertEquals(CodeIssue.Severity.INFO, unsupportedIssue.getSeverity());
    }
    
    @Test
    @DisplayName("Should complete analysis within performance requirements")
    void testPerformanceRequirements() throws Exception {
        String code = """
            public class PerformanceTest {
                public void method1() { System.out.println("test"); }
                public void method2() { System.out.println("test"); }
                public void method3() { System.out.println("test"); }
            }
            """;
        
        long startTime = System.currentTimeMillis();
        CompletableFuture<AnalysisResult> future = codeAnalyzer.analyzeCode(code, "java");
        AnalysisResult result = future.get();
        long endTime = System.currentTimeMillis();
        
        assertNotNull(result);
        
        // Should complete within 500ms for typical operations (as per requirements)
        long actualTime = endTime - startTime;
        assertTrue(actualTime < 500, 
                "Analysis should complete within 500ms, took: " + actualTime + "ms");
        
        // Analysis time should be recorded
        assertTrue(result.getAnalysisTimeMs() >= 0);
    }
    
    @Test
    @DisplayName("Should provide comprehensive complexity metrics")
    void testComplexityMetrics() throws Exception {
        String complexCode = """
            public class ComplexClass {
                public void complexMethod(int x) {
                    if (x > 0) {
                        for (int i = 0; i < x; i++) {
                            if (i % 2 == 0) {
                                while (condition()) {
                                    try {
                                        process();
                                    } catch (Exception e) {
                                        handle(e);
                                    }
                                }
                            }
                        }
                    }
                }
                
                private boolean condition() { return true; }
                private void process() {}
                private void handle(Exception e) {}
            }
            """;
        
        CompletableFuture<AnalysisResult> future = codeAnalyzer.analyzeCode(complexCode, "java");
        AnalysisResult result = future.get();
        
        ComplexityMetrics complexity = result.getComplexity();
        assertNotNull(complexity);
        
        // Should have reasonable complexity values
        assertTrue(complexity.getCyclomaticComplexity() > 5, 
                "Should detect high cyclomatic complexity");
        assertTrue(complexity.getCognitiveComplexity() > 0, 
                "Should calculate cognitive complexity");
        assertTrue(complexity.getLinesOfCode() > 10, 
                "Should count lines of code");
        assertTrue(complexity.getNestingDepth() > 2, 
                "Should detect deep nesting");
        assertTrue(complexity.getNumberOfMethods() >= 3, 
                "Should count methods");
        assertTrue(complexity.getMaintainabilityIndex() >= 0 && 
                  complexity.getMaintainabilityIndex() <= 100, 
                "Maintainability index should be between 0 and 100");
    }
}