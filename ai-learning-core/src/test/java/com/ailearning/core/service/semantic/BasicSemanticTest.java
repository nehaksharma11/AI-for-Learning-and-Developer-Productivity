package com.ailearning.core.service.semantic;

import com.ailearning.core.service.SemanticAnalyzer;
import com.ailearning.core.service.ContextEngine;
import com.ailearning.core.service.semantic.impl.DefaultSemanticAnalyzer;
import com.ailearning.core.service.impl.DefaultContextEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests to verify semantic analysis components can be instantiated.
 */
class BasicSemanticTest {
    
    @Test
    @DisplayName("Should create semantic analyzer successfully")
    void shouldCreateSemanticAnalyzer() {
        assertDoesNotThrow(() -> {
            SemanticAnalyzer analyzer = new DefaultSemanticAnalyzer();
            assertNotNull(analyzer);
        });
    }
    
    @Test
    @DisplayName("Should create context engine successfully")
    void shouldCreateContextEngine() {
        assertDoesNotThrow(() -> {
            ContextEngine engine = new DefaultContextEngine();
            assertNotNull(engine);
            assertTrue(engine.isReady());
        });
    }
}