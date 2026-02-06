package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.ast.ASTParser;
import com.ailearning.core.service.ast.impl.MultiLanguageASTParser;
import com.ailearning.core.model.ast.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DocumentationChangeDetector.
 */
class DocumentationChangeDetectorTest {
    
    @Mock
    private ASTParser astParser;
    
    private DocumentationChangeDetector changeDetector;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        changeDetector = new DocumentationChangeDetector(astParser);
    }
    
    @Test
    @DisplayName("Should detect new method addition")
    void shouldDetectNewMethodAddition() {
        // Given
        String oldCode = "public class TestClass {\n}";
        String newCode = "public class TestClass {\n    public void newMethod() {}\n}";
        String filePath = "TestClass.java";
        String language = "java";
        
        // Mock AST parsing
        ParseResult oldParseResult = createMockParseResult(List.of());
        ParseResult newParseResult = createMockParseResult(List.of(createMethodNode("newMethod")));
        
        when(astParser.parse(oldCode, language)).thenReturn(oldParseResult);
        when(astParser.parse(newCode, language)).thenReturn(newParseResult);
        
        // When
        List<DocumentationChangeEvent> changes = changeDetector.detectChanges(filePath, oldCode, newCode, language);
        
        // Then
        assertFalse(changes.isEmpty());
        DocumentationChangeEvent change = changes.get(0);
        assertEquals(DocumentationChangeEvent.ChangeType.ELEMENT_ADDED, change.getChangeType());
        assertEquals("newMethod", change.getNewElement().getName());
        assertEquals("method", change.getNewElement().getType());
    }
    
    @Test
    @DisplayName("Should detect method deletion")
    void shouldDetectMethodDeletion() {
        // Given
        String oldCode = "public class TestClass {\n    public void oldMethod() {}\n}";
        String newCode = "public class TestClass {\n}";
        String filePath = "TestClass.java";
        String language = "java";
        
        // Mock AST parsing
        ParseResult oldParseResult = createMockParseResult(List.of(createMethodNode("oldMethod")));
        ParseResult newParseResult = createMockParseResult(List.of());
        
        when(astParser.parse(oldCode, language)).thenReturn(oldParseResult);
        when(astParser.parse(newCode, language)).thenReturn(newParseResult);
        
        // When
        List<DocumentationChangeEvent> changes = changeDetector.detectChanges(filePath, oldCode, newCode, language);
        
        // Then
        assertFalse(changes.isEmpty());
        DocumentationChangeEvent change = changes.get(0);
        assertEquals(DocumentationChangeEvent.ChangeType.ELEMENT_DELETED, change.getChangeType());
        assertEquals("oldMethod", change.getOldElement().getName());
        assertEquals("method", change.getOldElement().getType());
    }
    
    @Test
    @DisplayName("Should detect method signature changes")
    void shouldDetectMethodSignatureChanges() {
        // Given
        String oldCode = "public class TestClass {\n    public void method() {}\n}";
        String newCode = "public class TestClass {\n    public void method(String param) {}\n}";
        String filePath = "TestClass.java";
        String language = "java";
        
        // Mock AST parsing
        ASTNode oldMethod = createMethodNode("method", "()");
        ASTNode newMethod = createMethodNode("method", "(String param)");
        
        ParseResult oldParseResult = createMockParseResult(List.of(oldMethod));
        ParseResult newParseResult = createMockParseResult(List.of(newMethod));
        
        when(astParser.parse(oldCode, language)).thenReturn(oldParseResult);
        when(astParser.parse(newCode, language)).thenReturn(newParseResult);
        
        // When
        List<DocumentationChangeEvent> changes = changeDetector.detectChanges(filePath, oldCode, newCode, language);
        
        // Then
        assertFalse(changes.isEmpty());
        boolean hasSignatureChange = changes.stream()
                .anyMatch(change -> change.getChangeType() == DocumentationChangeEvent.ChangeType.SIGNATURE_CHANGED);
        assertTrue(hasSignatureChange);
    }
    
    @Test
    @DisplayName("Should analyze documentation impact correctly")
    void shouldAnalyzeDocumentationImpact() {
        // Given
        List<DocumentationChangeEvent> changes = List.of(
                DocumentationChangeEvent.elementAdded("TestClass.java", 
                        createCodeElement("newMethod", "method"), "New method added"),
                DocumentationChangeEvent.elementDeleted("TestClass.java", 
                        createCodeElement("oldMethod", "method"), "Method removed")
        );
        
        List<Documentation> existingDocs = List.of(
                Documentation.builder()
                        .type(Documentation.Type.JAVADOC)
                        .format(Documentation.Format.PLAIN_TEXT)
                        .content("Documentation for oldMethod")
                        .filePath("TestClass.java")
                        .elementName("oldMethod")
                        .elementType("method")
                        .build()
        );
        
        // When
        DocumentationChangeDetector.DocumentationImpactAnalysis impact = 
                changeDetector.analyzeImpact(changes, existingDocs);
        
        // Then
        assertEquals(2, impact.getTotalChanges());
        assertEquals(1, impact.getAffectedDocuments());
        assertTrue(impact.getImpactScore() > 0.0);
        assertFalse(impact.getRecommendations().isEmpty());
    }
    
    @Test
    @DisplayName("Should determine update priority correctly")
    void shouldDetermineUpdatePriority() {
        // Given
        List<DocumentationChangeEvent> criticalChanges = List.of(
                DocumentationChangeEvent.builder()
                        .changeType(DocumentationChangeEvent.ChangeType.ELEMENT_DELETED)
                        .severity(DocumentationChangeEvent.Severity.CRITICAL)
                        .filePath("TestClass.java")
                        .build()
        );
        
        List<DocumentationChangeEvent> lowChanges = List.of(
                DocumentationChangeEvent.builder()
                        .changeType(DocumentationChangeEvent.ChangeType.ELEMENT_ADDED)
                        .severity(DocumentationChangeEvent.Severity.LOW)
                        .filePath("TestClass.java")
                        .build()
        );
        
        // When
        double criticalPriority = changeDetector.calculateUpdatePriority(criticalChanges);
        double lowPriority = changeDetector.calculateUpdatePriority(lowChanges);
        
        // Then
        assertTrue(criticalPriority > lowPriority);
        assertTrue(criticalPriority >= 0.8); // Critical changes should have high priority
        assertTrue(lowPriority <= 0.2); // Low changes should have low priority
    }
    
    @Test
    @DisplayName("Should handle parsing failures gracefully")
    void shouldHandleParsingFailuresGracefully() {
        // Given
        String oldCode = "invalid code";
        String newCode = "still invalid";
        String filePath = "TestClass.java";
        String language = "java";
        
        ParseResult failedResult = ParseResult.builder()
                .success(false)
                .errors(List.of(ParseError.syntaxError("Invalid syntax", new SourceLocation(1, 1))))
                .build();
        
        when(astParser.parse(anyString(), eq(language))).thenReturn(failedResult);
        
        // When
        List<DocumentationChangeEvent> changes = changeDetector.detectChanges(filePath, oldCode, newCode, language);
        
        // Then
        assertTrue(changes.isEmpty()); // Should return empty list on parsing failure
    }
    
    @Test
    @DisplayName("Should require immediate update for critical changes")
    void shouldRequireImmediateUpdateForCriticalChanges() {
        // Given
        List<DocumentationChangeEvent> changes = List.of(
                DocumentationChangeEvent.builder()
                        .changeType(DocumentationChangeEvent.ChangeType.SIGNATURE_CHANGED)
                        .severity(DocumentationChangeEvent.Severity.HIGH)
                        .filePath("TestClass.java")
                        .build()
        );
        
        // When
        boolean requiresUpdate = changeDetector.requiresDocumentationUpdate(changes);
        
        // Then
        assertTrue(requiresUpdate);
    }
    
    // Helper methods
    
    private ParseResult createMockParseResult(List<ASTNode> nodes) {
        ASTNode rootNode = mock(ASTNode.class);
        when(rootNode.getChildren()).thenReturn(nodes);
        
        return ParseResult.builder()
                .success(true)
                .rootNode(rootNode)
                .errors(List.of())
                .warnings(List.of())
                .build();
    }
    
    private ASTNode createMethodNode(String name) {
        return createMethodNode(name, "()");
    }
    
    private ASTNode createMethodNode(String name, String signature) {
        MethodNode methodNode = mock(MethodNode.class);
        when(methodNode.getName()).thenReturn(name);
        when(methodNode.getNodeType()).thenReturn(ASTNode.NodeType.METHOD);
        when(methodNode.getText()).thenReturn("public void " + name + signature + " {}");
        when(methodNode.getLocation()).thenReturn(new SourceLocation(1, 1));
        when(methodNode.getChildren()).thenReturn(List.of());
        return methodNode;
    }
    
    private DocumentationChangeDetector.CodeElement createCodeElement(String name, String type) {
        return new DocumentationChangeDetector.CodeElement(
                name, type, "mock content", new SourceLocation(1, 1), 
                "()", List.of(), "void");
    }
}