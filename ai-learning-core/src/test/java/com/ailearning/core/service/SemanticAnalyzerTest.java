package com.ailearning.core.service;

import com.ailearning.core.model.ast.ASTNode;
import com.ailearning.core.model.ast.ClassNode;
import com.ailearning.core.model.ast.MethodNode;
import com.ailearning.core.model.ast.SourceLocation;
import com.ailearning.core.model.CodePattern;
import com.ailearning.core.model.Relationship;
import com.ailearning.core.model.CodingConvention;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.SemanticContext;
import com.ailearning.core.service.SemanticAnalyzer;
import com.ailearning.core.service.semantic.impl.DefaultSemanticAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for semantic analysis functionality.
 */
class SemanticAnalyzerTest {
    
    private SemanticAnalyzer semanticAnalyzer;
    private ProjectContext testProjectContext;
    private ASTNode testClassNode;
    private ASTNode testMethodNode;
    
    @BeforeEach
    void setUp() {
        semanticAnalyzer = new DefaultSemanticAnalyzer();
        testProjectContext = createTestProjectContext();
        testClassNode = createTestClassNode();
        testMethodNode = createTestMethodNode();
    }
    
    @Test
    @DisplayName("Should analyze code relationships successfully")
    void shouldAnalyzeCodeRelationships() throws ExecutionException, InterruptedException {
        CompletableFuture<List<Relationship>> future = semanticAnalyzer
                .analyzeCodeRelationships(testClassNode, testProjectContext);
        List<Relationship> relationships = future.get();
        
        assertNotNull(relationships);
        
        // Verify relationship properties
        for (Relationship relationship : relationships) {
            assertNotNull(relationship.getSourceId());
            assertNotNull(relationship.getTargetId());
            assertNotNull(relationship.getType());
            assertTrue(relationship.getStrength() >= 0.0 && relationship.getStrength() <= 1.0);
        }
    }
    
    @Test
    @DisplayName("Should detect code patterns in Java code")
    void shouldDetectCodePatternsInJava() throws ExecutionException, InterruptedException {
        CompletableFuture<List<CodePattern>> future = semanticAnalyzer
                .detectCodePatterns(testClassNode, "java");
        List<CodePattern> patterns = future.get();
        
        assertNotNull(patterns);
        
        // Verify pattern properties
        for (CodePattern pattern : patterns) {
            assertNotNull(pattern.getName());
            assertNotNull(pattern.getType());
            assertNotNull(pattern.getDescription());
            assertTrue(pattern.getConfidence() >= 0.0 && pattern.getConfidence() <= 1.0);
        }
    }
    
    @Test
    @DisplayName("Should calculate semantic similarity between nodes")
    void shouldCalculateSemanticSimilarity() throws ExecutionException, InterruptedException {
        ASTNode similarNode = createSimilarClassNode();
        ASTNode differentNode = createDifferentNode();
        
        CompletableFuture<Double> similarityFuture1 = semanticAnalyzer
                .calculateSemanticSimilarity(testClassNode, similarNode);
        CompletableFuture<Double> similarityFuture2 = semanticAnalyzer
                .calculateSemanticSimilarity(testClassNode, differentNode);
        
        double similaritySimilar = similarityFuture1.get();
        double similarityDifferent = similarityFuture2.get();
        
        assertTrue(similaritySimilar >= 0.0 && similaritySimilar <= 1.0);
        assertTrue(similarityDifferent >= 0.0 && similarityDifferent <= 1.0);
    }
    
    @Test
    @DisplayName("Should learn coding conventions from project")
    void shouldLearnCodingConventions() throws ExecutionException, InterruptedException {
        CompletableFuture<List<CodingConvention>> future = semanticAnalyzer
                .learnCodingConventions(testProjectContext);
        List<CodingConvention> conventions = future.get();
        
        assertNotNull(conventions);
        
        // Verify convention properties
        for (CodingConvention convention : conventions) {
            assertNotNull(convention.getName());
            assertNotNull(convention.getDescription());
            assertNotNull(convention.getType());
            assertTrue(convention.getConfidence() >= 0.0 && convention.getConfidence() <= 1.0);
        }
    }
    
    @Test
    @DisplayName("Should find similar code with threshold")
    void shouldFindSimilarCodeWithThreshold() throws ExecutionException, InterruptedException {
        double threshold = 0.5;
        
        CompletableFuture<Map<ASTNode, Double>> future = semanticAnalyzer
                .findSimilarCode(testClassNode, testProjectContext, threshold);
        Map<ASTNode, Double> similarCode = future.get();
        
        assertNotNull(similarCode);
        
        // Verify all similarity scores meet threshold
        for (Map.Entry<ASTNode, Double> entry : similarCode.entrySet()) {
            assertTrue(entry.getValue() >= threshold, 
                      "All similarity scores should meet threshold");
            assertNotEquals(testClassNode, entry.getKey(), 
                           "Should not include the target node itself");
        }
    }
    
    @Test
    @DisplayName("Should analyze semantic context for AST node")
    void shouldAnalyzeSemanticContext() throws ExecutionException, InterruptedException {
        CompletableFuture<SemanticContext> future = semanticAnalyzer
                .analyzeSemanticContext(testClassNode, testProjectContext);
        SemanticContext context = future.get();
        
        assertNotNull(context);
        assertNotNull(context.getFilePath());
        assertNotNull(context.getSemanticRole());
        assertNotNull(context.getRelationships());
        assertNotNull(context.getNearbyPatterns());
        assertTrue(context.getComplexityScore() >= 0.0);
    }
    
    @Test
    @DisplayName("Should handle null inputs gracefully")
    void shouldHandleNullInputsGracefully() throws ExecutionException, InterruptedException {
        CompletableFuture<List<Relationship>> relationshipsFuture = semanticAnalyzer
                .analyzeCodeRelationships(null, testProjectContext);
        List<Relationship> relationships = relationshipsFuture.get();
        assertNotNull(relationships);
        assertTrue(relationships.isEmpty());
        
        CompletableFuture<List<CodePattern>> patternsFuture = semanticAnalyzer
                .detectCodePatterns(null, "java");
        List<CodePattern> patterns = patternsFuture.get();
        assertNotNull(patterns);
        assertTrue(patterns.isEmpty());
    }
    
    // Helper methods for creating test data
    
    private ProjectContext createTestProjectContext() {
        return ProjectContext.builder()
                .projectName("TestProject")
                .rootPath("/test/project")
                .build();
    }
    
    private ASTNode createTestClassNode() {
        return new ClassNode("TestClass", 
                SourceLocation.at("/test/TestClass.java", 1, 1),
                List.of(),
                Map.of("modifiers", List.of("public")));
    }
    
    private ASTNode createTestMethodNode() {
        return new MethodNode("testMethod",
                SourceLocation.at("/test/TestClass.java", 10, 5),
                List.of(),
                Map.of("modifiers", List.of("public"), "returnType", "void"));
    }
    
    private ASTNode createSimilarClassNode() {
        return new ClassNode("SimilarTestClass",
                SourceLocation.at("/test/SimilarTestClass.java", 1, 1),
                List.of(),
                Map.of("modifiers", List.of("public")));
    }
    
    private ASTNode createDifferentNode() {
        return new MethodNode("completelyDifferentMethod",
                SourceLocation.at("/test/Different.java", 1, 1),
                List.of(),
                Map.of("modifiers", List.of("private"), "returnType", "String"));
    }
}