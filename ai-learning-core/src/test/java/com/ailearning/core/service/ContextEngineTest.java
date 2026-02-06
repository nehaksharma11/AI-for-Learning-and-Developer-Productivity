package com.ailearning.core.service;

import com.ailearning.core.model.*;
import com.ailearning.core.service.impl.DefaultContextEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for context engine functionality.
 */
class ContextEngineTest {
    
    private ContextEngine contextEngine;
    
    @BeforeEach
    void setUp() {
        contextEngine = new DefaultContextEngine();
    }
    
    @Test
    @DisplayName("Should be ready after initialization")
    void shouldBeReadyAfterInitialization() {
        assertTrue(contextEngine.isReady());
    }
    
    @Test
    @DisplayName("Should provide performance metrics")
    void shouldProvidePerformanceMetrics() {
        ContextEngineMetrics metrics = contextEngine.getMetrics();
        
        assertNotNull(metrics);
        assertTrue(metrics.getAverageAnalysisTime() >= 0.0);
        assertTrue(metrics.getAverageUpdateTime() >= 0.0);
        assertTrue(metrics.getTotalAnalysisCount() >= 0);
        assertTrue(metrics.getTotalUpdateCount() >= 0);
        assertTrue(metrics.getCacheSize() >= 0);
        assertTrue(metrics.getMemoryUsage() >= 0);
    }
    
    @Test
    @DisplayName("Should analyze non-existent project gracefully")
    void shouldAnalyzeNonExistentProjectGracefully() throws ExecutionException, InterruptedException {
        String nonExistentPath = "/non/existent/path";
        
        CompletableFuture<ProjectContext> future = contextEngine.analyzeProject(nonExistentPath);
        ProjectContext context = future.get();
        
        assertNotNull(context);
        assertNotNull(context.getProjectName());
        assertEquals(nonExistentPath, context.getRootPath());
    }
    
    @Test
    @DisplayName("Should get current work context")
    void shouldGetCurrentWorkContext() throws ExecutionException, InterruptedException {
        CompletableFuture<WorkContext> future = contextEngine.getCurrentContext();
        WorkContext workContext = future.get();
        
        assertNotNull(workContext);
        assertNotNull(workContext.getCurrentFile());
        assertNotNull(workContext.getFocusedMethod());
        assertNotNull(workContext.getRecentFiles());
        assertNotNull(workContext.getActiveProject());
    }
    
    @Test
    @DisplayName("Should handle empty code change lists")
    void shouldHandleEmptyCodeChangeLists() throws ExecutionException, InterruptedException {
        List<CodeChange> emptyChanges = List.of();
        
        assertDoesNotThrow(() -> {
            CompletableFuture<Void> future = contextEngine.updateContext(emptyChanges);
            future.get();
        });
    }
    
    @Test
    @DisplayName("Should find related code for query")
    void shouldFindRelatedCodeForQuery() throws ExecutionException, InterruptedException {
        CodeQuery query = CodeQuery.semanticSearch("user authentication");
        
        CompletableFuture<List<CodeReference>> future = contextEngine.findRelatedCode(query);
        List<CodeReference> references = future.get();
        
        assertNotNull(references);
        // References might be empty if no matching code is found, which is acceptable
        
        // Verify reference properties if any are found
        for (CodeReference reference : references) {
            assertNotNull(reference.getFilePath());
            assertNotNull(reference.getSnippet());
            assertTrue(reference.getLineNumber() > 0);
            assertTrue(reference.getRelevanceScore() >= 0.0 && reference.getRelevanceScore() <= 1.0);
        }
    }
    
    @Test
    @DisplayName("Should handle different types of code changes")
    void shouldHandleDifferentTypesOfCodeChanges() throws ExecutionException, InterruptedException {
        List<CodeChange> changes = List.of(
                CodeChange.fileCreated("/test/NewClass.java", "public class NewClass {}"),
                CodeChange.fileModified("/test/ExistingClass.java", "old", "new", 5, 10),
                CodeChange.fileDeleted("/test/OldClass.java", "public class OldClass {}"),
                CodeChange.fileRenamed("/test/Old.java", "/test/New.java", "content")
        );
        
        assertDoesNotThrow(() -> {
            CompletableFuture<Void> future = contextEngine.updateContext(changes);
            future.get();
        });
    }
}