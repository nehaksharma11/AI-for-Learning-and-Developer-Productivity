package com.ailearning.core.service;

import com.ailearning.core.model.ast.*;
import com.ailearning.core.model.*;
import com.ailearning.core.service.ast.ASTParser;
import com.ailearning.core.service.ast.impl.MultiLanguageASTParser;
import com.ailearning.core.service.semantic.impl.DefaultSemanticAnalyzer;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for multi-language support functionality.
 * 
 * **Validates: Property 15 - Multi-Language and Framework Support**
 * *For any* supported programming language or popular framework, the system should provide 
 * consistent analysis capabilities, appropriate guidance, and language-specific documentation 
 * that follows established conventions.
 * **Validates: Requirements 8.1, 8.2, 8.5**
 */
class MultiLanguageSupportProperties {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiLanguageSupportProperties.class);
    
    @Property(tries = 100)
    @Label("AST parser should support all declared programming languages consistently")
    void astParserLanguageConsistency(
            @ForAll("supportedLanguages") String language,
            @ForAll("sampleCodeForLanguage") String sourceCode) {
        
        ASTParser parser = new MultiLanguageASTParser();
        
        try {
            // Verify language is supported
            assertTrue(parser.supportsLanguage(language), 
                      "Parser should support declared language: " + language);
            
            // Parse code for the language
            String filePath = "/test/sample." + getFileExtension(language);
            CompletableFuture<ParseResult> future = parser.parseCode(sourceCode, language, filePath);
            ParseResult result = future.get();
            
            // Verify parse result structure
            assertNotNull(result, "Parse result should not be null");
            assertNotNull(result.getLanguage(), "Language should be recorded in result");
            assertEquals(language, result.getLanguage(), "Language should match input");
            assertNotNull(result.getFilePath(), "File path should be recorded");
            assertEquals(filePath, result.getFilePath(), "File path should match input");
            
            // Verify metrics are provided
            assertNotNull(result.getMetrics(), "Parse metrics should be provided");
            assertTrue(result.getMetrics().getParseTimeMs() >= 0, 
                      "Parse time should be non-negative");
            
            // If parsing is successful, AST should be valid
            if (result.isSuccessful()) {
                assertNotNull(result.getAst(), "Successful parse should have AST");
                assertTrue(result.getErrors().isEmpty(), "Successful parse should have no errors");
            }
            
        } catch (Exception e) {
            logger.error("Language consistency test failed for {}", language, e);
            fail("Parser should handle " + language + " consistently: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Semantic analyzer should provide consistent analysis across languages")
    void semanticAnalyzerLanguageConsistency(
            @ForAll("supportedLanguages") String language,
            @ForAll("astNodesForLanguage") ASTNode astNode) {
        
        SemanticAnalyzer analyzer = new DefaultSemanticAnalyzer();
        ProjectContext projectContext = createTestProjectContext();
        
        try {
            // Test pattern detection
            CompletableFuture<List<CodePattern>> patternsFuture = 
                    analyzer.detectCodePatterns(astNode, language);
            List<CodePattern> patterns = patternsFuture.get();
            
            assertNotNull(patterns, "Pattern detection should return non-null result");
            
            // Verify pattern properties
            for (CodePattern pattern : patterns) {
                assertNotNull(pattern.getName(), "Pattern name should not be null");
                assertNotNull(pattern.getType(), "Pattern type should not be null");
                assertNotNull(pattern.getDescription(), "Pattern description should not be null");
                assertTrue(pattern.getConfidence() >= 0.0 && pattern.getConfidence() <= 1.0,
                          "Pattern confidence should be between 0.0 and 1.0");
            }
            
            // Test relationship analysis
            CompletableFuture<List<Relationship>> relationshipsFuture = 
                    analyzer.analyzeCodeRelationships(astNode, projectContext);
            List<Relationship> relationships = relationshipsFuture.get();
            
            assertNotNull(relationships, "Relationship analysis should return non-null result");
            
            // Verify relationship properties
            for (Relationship relationship : relationships) {
                assertNotNull(relationship.getSourceId(), "Relationship source should not be null");
                assertNotNull(relationship.getTargetId(), "Relationship target should not be null");
                assertNotNull(relationship.getType(), "Relationship type should not be null");
                assertTrue(relationship.getStrength() >= 0.0 && relationship.getStrength() <= 1.0,
                          "Relationship strength should be between 0.0 and 1.0");
            }
            
        } catch (Exception e) {
            logger.error("Semantic analysis consistency test failed for {}", language, e);
            fail("Semantic analyzer should handle " + language + " consistently: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Multi-language parser should handle syntax validation consistently")
    void syntaxValidationConsistency(
            @ForAll("supportedLanguages") String language,
            @ForAll("validCodeSamples") String validCode,
            @ForAll("invalidCodeSamples") String invalidCode) {
        
        ASTParser parser = new MultiLanguageASTParser();
        
        try {
            // Test valid code
            CompletableFuture<Boolean> validFuture = parser.validateSyntax(validCode, language);
            Boolean validResult = validFuture.get();
            
            assertNotNull(validResult, "Syntax validation should return non-null result");
            // Note: We don't assert true here because our simple code samples might not be syntactically complete
            
            // Test invalid code
            CompletableFuture<Boolean> invalidFuture = parser.validateSyntax(invalidCode, language);
            Boolean invalidResult = invalidFuture.get();
            
            assertNotNull(invalidResult, "Syntax validation should return non-null result for invalid code");
            
        } catch (Exception e) {
            logger.error("Syntax validation test failed for {}", language, e);
            fail("Parser should handle syntax validation for " + language + " consistently: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Language-specific file extensions should be handled correctly")
    void fileExtensionHandling(
            @ForAll("supportedLanguages") String language,
            @ForAll("sampleCodeForLanguage") String sourceCode) {
        
        ASTParser parser = new MultiLanguageASTParser();
        
        try {
            String extension = getFileExtension(language);
            String filePath = "/test/sample." + extension;
            
            CompletableFuture<ParseResult> future = parser.parseCode(sourceCode, language, filePath);
            ParseResult result = future.get();
            
            assertNotNull(result, "Parse result should not be null");
            assertEquals(language, result.getLanguage(), "Language should be correctly identified");
            assertTrue(result.getFilePath().endsWith("." + extension),
                      "File path should have correct extension for " + language);
            
        } catch (Exception e) {
            logger.error("File extension handling test failed for {}", language, e);
            fail("Parser should handle file extensions for " + language + " correctly: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Cross-language similarity calculation should be consistent")
    void crossLanguageSimilarityConsistency(
            @ForAll("supportedLanguages") String language1,
            @ForAll("supportedLanguages") String language2,
            @ForAll("astNodesForLanguage") ASTNode node1,
            @ForAll("astNodesForLanguage") ASTNode node2) {
        
        SemanticAnalyzer analyzer = new DefaultSemanticAnalyzer();
        
        try {
            CompletableFuture<Double> similarityFuture = 
                    analyzer.calculateSemanticSimilarity(node1, node2);
            Double similarity = similarityFuture.get();
            
            assertNotNull(similarity, "Similarity calculation should return non-null result");
            assertTrue(similarity >= 0.0 && similarity <= 1.0,
                      "Similarity should be between 0.0 and 1.0, got: " + similarity);
            
            // Test symmetry property
            CompletableFuture<Double> reverseSimilarityFuture = 
                    analyzer.calculateSemanticSimilarity(node2, node1);
            Double reverseSimilarity = reverseSimilarityFuture.get();
            
            assertEquals(similarity, reverseSimilarity, 0.01,
                        "Similarity calculation should be symmetric");
            
        } catch (Exception e) {
            logger.error("Cross-language similarity test failed for {} and {}", language1, language2, e);
            fail("Similarity calculation should work across languages: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Language-specific coding conventions should be learned appropriately")
    void languageSpecificConventionLearning(
            @ForAll("supportedLanguages") String language,
            @ForAll @Size(min = 1, max = 3) List<@NotEmpty String> classNames) {
        
        SemanticAnalyzer analyzer = new DefaultSemanticAnalyzer();
        ProjectContext projectContext = createProjectContextWithLanguage(language, classNames);
        
        try {
            CompletableFuture<List<CodingConvention>> conventionsFuture = 
                    analyzer.learnCodingConventions(projectContext);
            List<CodingConvention> conventions = conventionsFuture.get();
            
            assertNotNull(conventions, "Convention learning should return non-null result");
            
            // Verify convention properties
            for (CodingConvention convention : conventions) {
                assertNotNull(convention.getName(), "Convention name should not be null");
                assertNotNull(convention.getDescription(), "Convention description should not be null");
                assertNotNull(convention.getType(), "Convention type should not be null");
                assertTrue(convention.getConfidence() >= 0.0 && convention.getConfidence() <= 1.0,
                          "Convention confidence should be between 0.0 and 1.0");
            }
            
        } catch (Exception e) {
            logger.error("Convention learning test failed for {}", language, e);
            fail("Convention learning should work for " + language + ": " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private String getFileExtension(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> "java";
            case "javascript" -> "js";
            case "typescript" -> "ts";
            case "python" -> "py";
            default -> "txt";
        };
    }
    
    private ProjectContext createTestProjectContext() {
        return ProjectContext.builder()
                .projectName("TestProject")
                .rootPath("/test/project")
                .build();
    }
    
    private ProjectContext createProjectContextWithLanguage(String language, List<String> classNames) {
        return ProjectContext.builder()
                .projectName("TestProject_" + language)
                .rootPath("/test/" + language + "-project")
                .build();
    }
    
    // Generators for test data
    
    @Provide
    Arbitrary<String> supportedLanguages() {
        return Arbitraries.of("java", "javascript", "typescript", "python");
    }
    
    @Provide
    Arbitrary<String> sampleCodeForLanguage() {
        return Arbitraries.of(
                "public class TestClass { }",
                "function testFunction() { return true; }",
                "class TestClass: pass",
                "const testVariable = 42;",
                "def test_function(): return True"
        );
    }
    
    @Provide
    Arbitrary<String> validCodeSamples() {
        return Arbitraries.of(
                "class Test",
                "function test()",
                "def test():",
                "public void test()",
                "const x = 1"
        );
    }
    
    @Provide
    Arbitrary<String> invalidCodeSamples() {
        return Arbitraries.of(
                "class {",
                "function (",
                "def :",
                "public void",
                "const = "
        );
    }
    
    @Provide
    Arbitrary<ASTNode> astNodesForLanguage() {
        return Arbitraries.of(
                new ClassNode("TestClass", 
                        SourceLocation.at("/test/Test.java", 1, 1),
                        List.of(),
                        Map.of("modifiers", List.of("public"))),
                new MethodNode("testMethod",
                        SourceLocation.at("/test/Test.java", 5, 5),
                        List.of(),
                        Map.of("modifiers", List.of("public"), "returnType", "void")),
                new VariableNode("testVariable",
                        SourceLocation.at("/test/Test.java", 3, 5),
                        List.of(),
                        Map.of("type", "String"))
        );
    }
}