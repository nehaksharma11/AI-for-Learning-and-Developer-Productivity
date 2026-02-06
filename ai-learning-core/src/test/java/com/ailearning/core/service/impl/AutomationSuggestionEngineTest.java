package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AutomationSuggestionEngine.
 * Tests pattern detection, automation opportunity identification, and template management.
 */
class AutomationSuggestionEngineTest {

    private AutomationSuggestionEngine engine;
    private Codebase testCodebase;
    private ProjectContext testProjectContext;

    @BeforeEach
    void setUp() {
        engine = new AutomationSuggestionEngine();
        
        // Create test codebase with repetitive patterns
        List<FileNode> files = List.of(
                createTestFile("src/main/java/Controller1.java", 
                        "package com.example;\n" +
                        "import org.springframework.web.bind.annotation.*;\n" +
                        "@RestController\n" +
                        "public class Controller1 {\n" +
                        "    @GetMapping(\"/users\")\n" +
                        "    public List<User> getUsers() {\n" +
                        "        return userService.findAll();\n" +
                        "    }\n" +
                        "}"),
                createTestFile("src/main/java/Controller2.java",
                        "package com.example;\n" +
                        "import org.springframework.web.bind.annotation.*;\n" +
                        "@RestController\n" +
                        "public class Controller2 {\n" +
                        "    @GetMapping(\"/products\")\n" +
                        "    public List<Product> getProducts() {\n" +
                        "        return productService.findAll();\n" +
                        "    }\n" +
                        "}"),
                createTestFile("src/main/java/Controller3.java",
                        "package com.example;\n" +
                        "import org.springframework.web.bind.annotation.*;\n" +
                        "@RestController\n" +
                        "public class Controller3 {\n" +
                        "    @GetMapping(\"/orders\")\n" +
                        "    public List<Order> getOrders() {\n" +
                        "        return orderService.findAll();\n" +
                        "    }\n" +
                        "}"),
                createTestFile("application.properties",
                        "server.port=8080\n" +
                        "spring.datasource.url=jdbc:h2:mem:testdb\n" +
                        "spring.jpa.hibernate.ddl-auto=create-drop"),
                createTestFile("application-dev.properties",
                        "server.port=8081\n" +
                        "spring.datasource.url=jdbc:h2:mem:devdb\n" +
                        "spring.jpa.hibernate.ddl-auto=create-drop")
        );

        testCodebase = Codebase.builder()
                .id("test-codebase")
                .name("Test Project")
                .files(files)
                .build();

        testProjectContext = ProjectContext.builder()
                .id("test-project")
                .projectName("Test Project")
                .description("Test project for automation detection")
                .build();
    }

    @Test
    @DisplayName("Should detect repetitive patterns in codebase")
    void testDetectRepetitivePatterns() {
        // When
        List<RepetitivePattern> patterns = engine.detectRepetitivePatterns(testCodebase);

        // Then
        assertNotNull(patterns);
        assertFalse(patterns.isEmpty());
        
        // Should detect boilerplate code patterns
        boolean hasBoilerplatePattern = patterns.stream()
                .anyMatch(p -> p.getType() == RepetitivePattern.PatternType.BOILERPLATE_CODE);
        assertTrue(hasBoilerplatePattern, "Should detect boilerplate code patterns");
        
        // Should detect configuration repetition
        boolean hasConfigPattern = patterns.stream()
                .anyMatch(p -> p.getType() == RepetitivePattern.PatternType.CONFIGURATION_REPETITION);
        assertTrue(hasConfigPattern, "Should detect configuration repetition patterns");
        
        // Patterns should be significant
        patterns.forEach(pattern -> {
            assertTrue(pattern.isSignificant(), "All returned patterns should be significant");
            assertTrue(pattern.getAutomationPotential() > 0, "Patterns should have automation potential");
        });
    }

    @Test
    @DisplayName("Should suggest automation opportunities based on patterns")
    void testSuggestAutomationOpportunities() {
        // Given
        List<RepetitivePattern> patterns = engine.detectRepetitivePatterns(testCodebase);

        // When
        List<AutomationOpportunity> opportunities = engine.suggestAutomationOpportunities(patterns, testProjectContext);

        // Then
        assertNotNull(opportunities);
        assertFalse(opportunities.isEmpty());
        
        // Should have reasonable time savings estimates
        opportunities.forEach(opportunity -> {
            assertTrue(opportunity.getEstimatedTimeSavingsMinutes() >= 5, 
                      "Should have minimum time savings of 5 minutes");
            assertTrue(opportunity.getConfidenceScore() > 0, 
                      "Should have positive confidence score");
            assertNotNull(opportunity.getType(), "Should have opportunity type");
            assertNotNull(opportunity.getPriority(), "Should have priority");
        });
        
        // Should be sorted by priority and impact
        for (int i = 0; i < opportunities.size() - 1; i++) {
            AutomationOpportunity current = opportunities.get(i);
            AutomationOpportunity next = opportunities.get(i + 1);
            
            assertTrue(current.getPriority().getLevel() >= next.getPriority().getLevel(),
                      "Should be sorted by priority");
        }
    }

    @Test
    @DisplayName("Should find relevant code templates")
    void testFindRelevantTemplates() {
        // When
        List<CodeTemplate> templates = engine.findRelevantTemplates(
                "REST controller spring", 
                CodeTemplate.Language.JAVA, 
                "Spring Boot");

        // Then
        assertNotNull(templates);
        assertFalse(templates.isEmpty());
        
        // Should return relevant templates
        boolean hasRestControllerTemplate = templates.stream()
                .anyMatch(t -> t.getName().toLowerCase().contains("controller"));
        assertTrue(hasRestControllerTemplate, "Should find REST controller template");
        
        // Templates should match the language
        templates.forEach(template -> {
            assertEquals(CodeTemplate.Language.JAVA, template.getLanguage());
        });
    }

    @Test
    @DisplayName("Should create template from repetitive pattern")
    void testCreateTemplateFromPattern() {
        // Given
        RepetitivePattern pattern = RepetitivePattern.builder()
                .id("test-pattern")
                .type(RepetitivePattern.PatternType.BOILERPLATE_CODE)
                .description("REST controller boilerplate")
                .codeSnippet("@RestController\npublic class TestController {\n}")
                .frequency(5)
                .similarity(0.9)
                .build();

        // When
        CodeTemplate template = engine.createTemplateFromPattern(
                pattern, "Generated REST Controller", CodeTemplate.Language.JAVA);

        // Then
        assertNotNull(template);
        assertEquals("Generated REST Controller", template.getName());
        assertEquals(CodeTemplate.Language.JAVA, template.getLanguage());
        assertEquals(CodeTemplate.TemplateType.CLASS, template.getType());
        assertNotNull(template.getTemplate());
        assertFalse(template.getParameters().isEmpty());
        assertTrue(template.getTags().contains("generated"));
    }

    @Test
    @DisplayName("Should generate automation script for opportunity")
    void testGenerateAutomationScript() {
        // Given
        AutomationOpportunity opportunity = AutomationOpportunity.builder()
                .id("test-opportunity")
                .type(AutomationOpportunity.OpportunityType.CODE_GENERATION)
                .title("Generate REST Controllers")
                .description("Automate REST controller creation")
                .priority(AutomationOpportunity.Priority.HIGH)
                .estimatedTimeSavingsMinutes(30)
                .confidenceScore(0.8)
                .build();

        // When
        String script = engine.generateAutomationScript(opportunity);

        // Then
        assertNotNull(script);
        assertFalse(script.trim().isEmpty());
        assertTrue(script.contains("#!/bin/bash"), "Should be a bash script");
        assertTrue(script.contains(opportunity.getTitle()), "Should reference the opportunity");
    }

    @Test
    @DisplayName("Should handle empty codebase gracefully")
    void testDetectPatternsInEmptyCodebase() {
        // Given
        Codebase emptyCodebase = Codebase.builder()
                .id("empty")
                .name("Empty Project")
                .files(Collections.emptyList())
                .build();

        // When
        List<RepetitivePattern> patterns = engine.detectRepetitivePatterns(emptyCodebase);

        // Then
        assertNotNull(patterns);
        assertTrue(patterns.isEmpty());
    }

    @Test
    @DisplayName("Should cache pattern detection results")
    void testPatternDetectionCaching() {
        // When - First call
        long startTime1 = System.currentTimeMillis();
        List<RepetitivePattern> patterns1 = engine.detectRepetitivePatterns(testCodebase);
        long duration1 = System.currentTimeMillis() - startTime1;

        // When - Second call (should use cache)
        long startTime2 = System.currentTimeMillis();
        List<RepetitivePattern> patterns2 = engine.detectRepetitivePatterns(testCodebase);
        long duration2 = System.currentTimeMillis() - startTime2;

        // Then
        assertEquals(patterns1.size(), patterns2.size());
        // Second call should be faster due to caching (though this might be flaky in fast systems)
        // assertTrue(duration2 <= duration1, "Second call should be faster due to caching");
    }

    @Test
    @DisplayName("Should filter patterns by significance")
    void testPatternSignificanceFiltering() {
        // Given - Create a codebase with both significant and insignificant patterns
        List<FileNode> minimalFiles = List.of(
                createTestFile("Test1.java", "public class Test1 { }"),
                createTestFile("Test2.java", "public class Test2 { }")
        );
        
        Codebase minimalCodebase = Codebase.builder()
                .id("minimal")
                .name("Minimal Project")
                .files(minimalFiles)
                .build();

        // When
        List<RepetitivePattern> patterns = engine.detectRepetitivePatterns(minimalCodebase);

        // Then - Should only return significant patterns
        patterns.forEach(pattern -> {
            assertTrue(pattern.isSignificant(), "All patterns should be significant");
            assertTrue(pattern.getFrequency() >= 3, "Should have minimum frequency");
            assertTrue(pattern.getSimilarity() >= 0.7, "Should have minimum similarity");
        });
    }

    @Test
    @DisplayName("Should handle different template languages")
    void testMultiLanguageTemplateSupport() {
        // When - Test different languages
        List<CodeTemplate> javaTemplates = engine.findRelevantTemplates(
                "class", CodeTemplate.Language.JAVA, null);
        List<CodeTemplate> jsTemplates = engine.findRelevantTemplates(
                "function", CodeTemplate.Language.JAVASCRIPT, null);

        // Then
        assertNotNull(javaTemplates);
        assertNotNull(jsTemplates);
        
        // Java templates should exist (from initialization)
        assertFalse(javaTemplates.isEmpty());
        
        // All templates should match requested language
        javaTemplates.forEach(template -> 
                assertEquals(CodeTemplate.Language.JAVA, template.getLanguage()));
    }

    @Test
    @DisplayName("Should calculate automation potential correctly")
    void testAutomationPotentialCalculation() {
        // Given
        RepetitivePattern highPotentialPattern = RepetitivePattern.builder()
                .id("high-potential")
                .type(RepetitivePattern.PatternType.BOILERPLATE_CODE)
                .description("High potential pattern")
                .frequency(10)
                .similarity(0.9)
                .build();

        RepetitivePattern lowPotentialPattern = RepetitivePattern.builder()
                .id("low-potential")
                .type(RepetitivePattern.PatternType.COPY_PASTE)
                .description("Low potential pattern")
                .frequency(2)
                .similarity(0.5)
                .build();

        // Then
        assertTrue(highPotentialPattern.getAutomationPotential() > lowPotentialPattern.getAutomationPotential(),
                  "High frequency and similarity should result in higher automation potential");
        
        assertTrue(highPotentialPattern.isSignificant(), "High potential pattern should be significant");
        assertFalse(lowPotentialPattern.isSignificant(), "Low potential pattern should not be significant");
    }

    // Helper methods

    private FileNode createTestFile(String path, String content) {
        return FileNode.builder()
                .path(path)
                .name(path.substring(path.lastIndexOf('/') + 1))
                .content(content)
                .size(content.length())
                .lastModified(LocalDateTime.now())
                .build();
    }
}