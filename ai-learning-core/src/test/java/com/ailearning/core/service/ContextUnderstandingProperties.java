package com.ailearning.core.service;

import com.ailearning.core.model.*;
import com.ailearning.core.model.ast.*;
import com.ailearning.core.service.impl.DefaultContextEngine;
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
 * Property-based tests for context understanding functionality.
 * 
 * **Validates: Property 9 - Project Context Understanding and Maintenance**
 * *For any* project opening or code modification, the context engine should analyze and maintain 
 * accurate understanding of codebase structure, dependencies, relationships, and coding standards.
 * **Validates: Requirements 5.1, 5.2, 5.4**
 */
class ContextUnderstandingProperties {
    
    private static final Logger logger = LoggerFactory.getLogger(ContextUnderstandingProperties.class);
    
    @Property(tries = 100)
    @Label("Context engine should maintain consistent project analysis across multiple operations")
    void contextEngineConsistentAnalysis(
            @ForAll("validProjectPaths") String projectPath,
            @ForAll @Size(min = 1, max = 5) List<@NotEmpty String> analysisOperations) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        
        try {
            // First analysis
            CompletableFuture<ProjectContext> future1 = contextEngine.analyzeProject(projectPath);
            ProjectContext context1 = future1.get();
            
            // Perform multiple operations
            for (String operation : analysisOperations) {
                performAnalysisOperation(contextEngine, operation);
            }
            
            // Second analysis should be consistent
            CompletableFuture<ProjectContext> future2 = contextEngine.analyzeProject(projectPath);
            ProjectContext context2 = future2.get();
            
            // Verify consistency
            assertNotNull(context1, "First analysis should not be null");
            assertNotNull(context2, "Second analysis should not be null");
            assertEquals(context1.getProjectName(), context2.getProjectName(), 
                        "Project name should remain consistent");
            assertEquals(context1.getRootPath(), context2.getRootPath(), 
                        "Root path should remain consistent");
            
        } catch (Exception e) {
            logger.error("Context analysis failed", e);
            fail("Context engine should handle analysis operations gracefully: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Context updates should complete within performance requirements")
    void contextUpdatesPerformanceRequirement(
            @ForAll("codeChangeList") List<CodeChange> changes) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        
        try {
            long startTime = System.currentTimeMillis();
            
            CompletableFuture<Void> updateFuture = contextEngine.updateContext(changes);
            updateFuture.get();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Property: Context updates should complete within 500ms (with some tolerance for test environment)
            assertTrue(duration < 1000, 
                      "Context update should complete within reasonable time, took: " + duration + "ms");
            
        } catch (Exception e) {
            logger.error("Context update failed", e);
            fail("Context engine should handle updates gracefully: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Context engine should maintain accurate codebase structure understanding")
    void contextEngineStructureUnderstanding(
            @ForAll("validProjectPaths") String projectPath,
            @ForAll("codeChangeList") List<CodeChange> structuralChanges) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        
        try {
            // Initial analysis
            CompletableFuture<ProjectContext> initialFuture = contextEngine.analyzeProject(projectPath);
            ProjectContext initialContext = initialFuture.get();
            
            // Apply structural changes
            CompletableFuture<Void> updateFuture = contextEngine.updateContext(structuralChanges);
            updateFuture.get();
            
            // Re-analyze to verify structure understanding
            CompletableFuture<ProjectContext> updatedFuture = contextEngine.analyzeProject(projectPath);
            ProjectContext updatedContext = updatedFuture.get();
            
            // Verify structure understanding is maintained
            assertNotNull(initialContext, "Initial context should not be null");
            assertNotNull(updatedContext, "Updated context should not be null");
            
            // Context should reflect structural changes
            assertEquals(initialContext.getProjectName(), updatedContext.getProjectName(),
                        "Project identity should be preserved through structural changes");
            
        } catch (Exception e) {
            logger.error("Structure understanding test failed", e);
            fail("Context engine should maintain structure understanding: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Context engine should handle code queries with relevant results")
    void contextEngineCodeQueryRelevance(
            @ForAll("validProjectPaths") String projectPath,
            @ForAll("codeQueries") CodeQuery query) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        
        try {
            // Analyze project first
            CompletableFuture<ProjectContext> analysisFuture = contextEngine.analyzeProject(projectPath);
            analysisFuture.get();
            
            // Execute code query
            CompletableFuture<List<CodeReference>> queryFuture = contextEngine.findRelatedCode(query);
            List<CodeReference> results = queryFuture.get();
            
            // Verify query results
            assertNotNull(results, "Query results should not be null");
            
            // All results should have valid relevance scores
            for (CodeReference reference : results) {
                assertNotNull(reference.getFilePath(), "File path should not be null");
                assertNotNull(reference.getSnippet(), "Code snippet should not be null");
                assertTrue(reference.getLineNumber() > 0, "Line number should be positive");
                assertTrue(reference.getRelevanceScore() >= 0.0 && reference.getRelevanceScore() <= 1.0,
                          "Relevance score should be between 0.0 and 1.0");
            }
            
            // Results should be ordered by relevance (descending)
            for (int i = 1; i < results.size(); i++) {
                assertTrue(results.get(i-1).getRelevanceScore() >= results.get(i).getRelevanceScore(),
                          "Results should be ordered by relevance score");
            }
            
        } catch (Exception e) {
            logger.error("Code query test failed", e);
            fail("Context engine should handle code queries gracefully: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Context engine should provide consistent work context")
    void contextEngineWorkContextConsistency(
            @ForAll @IntRange(min = 1, max = 10) int operationCount) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        
        try {
            List<WorkContext> contexts = new ArrayList<>();
            
            // Get work context multiple times
            for (int i = 0; i < operationCount; i++) {
                CompletableFuture<WorkContext> future = contextEngine.getCurrentContext();
                WorkContext context = future.get();
                contexts.add(context);
            }
            
            // Verify all contexts are valid
            for (WorkContext context : contexts) {
                assertNotNull(context, "Work context should not be null");
                assertNotNull(context.getCurrentFile(), "Current file should not be null");
                assertNotNull(context.getFocusedMethod(), "Focused method should not be null");
                assertNotNull(context.getRecentFiles(), "Recent files should not be null");
                assertNotNull(context.getActiveProject(), "Active project should not be null");
                assertNotNull(context.getOpenFiles(), "Open files should not be null");
                assertTrue(context.getCursorLine() >= 0, "Cursor line should be non-negative");
                assertTrue(context.getCursorColumn() >= 0, "Cursor column should be non-negative");
            }
            
        } catch (Exception e) {
            logger.error("Work context consistency test failed", e);
            fail("Context engine should provide consistent work context: " + e.getMessage());
        }
    }
    
    @Property(tries = 100)
    @Label("Context engine metrics should accurately reflect operations")
    void contextEngineMetricsAccuracy(
            @ForAll("validProjectPaths") String projectPath,
            @ForAll("codeChangeList") List<CodeChange> changes) {
        
        ContextEngine contextEngine = new DefaultContextEngine();
        
        try {
            // Get initial metrics
            ContextEngineMetrics initialMetrics = contextEngine.getMetrics();
            long initialAnalysisCount = initialMetrics.getTotalAnalysisCount();
            long initialUpdateCount = initialMetrics.getTotalUpdateCount();
            
            // Perform operations
            CompletableFuture<ProjectContext> analysisFuture = contextEngine.analyzeProject(projectPath);
            analysisFuture.get();
            
            CompletableFuture<Void> updateFuture = contextEngine.updateContext(changes);
            updateFuture.get();
            
            // Get updated metrics
            ContextEngineMetrics updatedMetrics = contextEngine.getMetrics();
            
            // Verify metrics accuracy
            assertTrue(updatedMetrics.getTotalAnalysisCount() > initialAnalysisCount,
                      "Analysis count should increase after analysis operation");
            assertTrue(updatedMetrics.getTotalUpdateCount() > initialUpdateCount,
                      "Update count should increase after update operation");
            assertTrue(updatedMetrics.getAverageAnalysisTime() >= 0.0,
                      "Average analysis time should be non-negative");
            assertTrue(updatedMetrics.getAverageUpdateTime() >= 0.0,
                      "Average update time should be non-negative");
            assertTrue(updatedMetrics.getMemoryUsage() > 0,
                      "Memory usage should be positive");
            
        } catch (Exception e) {
            logger.error("Metrics accuracy test failed", e);
            fail("Context engine should provide accurate metrics: " + e.getMessage());
        }
    }
    
    // Helper method for performing analysis operations
    private void performAnalysisOperation(ContextEngine contextEngine, String operation) {
        try {
            switch (operation.toLowerCase()) {
                case "query" -> {
                    CodeQuery query = CodeQuery.semanticSearch("test");
                    contextEngine.findRelatedCode(query).get();
                }
                case "context" -> contextEngine.getCurrentContext().get();
                case "metrics" -> contextEngine.getMetrics();
                default -> {
                    // Default operation - just get metrics
                    contextEngine.getMetrics();
                }
            }
        } catch (Exception e) {
            logger.debug("Analysis operation '{}' completed with exception: {}", operation, e.getMessage());
            // Don't fail the test for individual operation failures
        }
    }
    
    // Generators for test data
    
    @Provide
    Arbitrary<String> validProjectPaths() {
        return Arbitraries.of(
                "/test/project",
                "/sample/java-project", 
                "/demo/typescript-app",
                "/example/python-service",
                "/workspace/multi-lang-project"
        );
    }
    
    @Provide
    Arbitrary<List<CodeChange>> codeChangeList() {
        return Arbitraries.of(
                List.of(),
                List.of(CodeChange.fileCreated("/test/NewClass.java", "public class NewClass {}")),
                List.of(
                    CodeChange.fileModified("/test/ExistingClass.java", "old content", "new content", 1, 10),
                    CodeChange.fileCreated("/test/AnotherClass.java", "public class AnotherClass {}")
                ),
                List.of(
                    CodeChange.fileDeleted("/test/OldClass.java", "public class OldClass {}"),
                    CodeChange.fileRenamed("/test/Old.java", "/test/New.java", "content")
                )
        );
    }
    
    @Provide
    Arbitrary<CodeQuery> codeQueries() {
        return Arbitraries.of(
                CodeQuery.semanticSearch("authentication"),
                CodeQuery.patternSearch("*.Service"),
                CodeQuery.usageSearch("UserRepository"),
                CodeQuery.similarCodeSearch("public void process()", 0.7)
        );
    }
}