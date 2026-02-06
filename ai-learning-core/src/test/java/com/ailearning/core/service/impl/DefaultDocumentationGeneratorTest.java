package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.DocumentationGenerator;
import com.ailearning.core.service.ast.ASTParser;
import com.ailearning.core.service.ast.impl.MultiLanguageASTParser;
import com.ailearning.core.model.ast.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DefaultDocumentationGenerator.
 */
class DefaultDocumentationGeneratorTest {
    
    @Mock
    private ASTParser astParser;
    
    private DocumentationGenerator documentationGenerator;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        documentationGenerator = new DefaultDocumentationGenerator(astParser);
    }
    
    @Test
    @DisplayName("Should generate inline comments for Java code")
    void shouldGenerateInlineCommentsForJavaCode() throws ExecutionException, InterruptedException {
        // Given
        String javaCode = "public class TestClass {\n    public void testMethod() {\n    }\n}";
        String language = "java";
        String filePath = "/test/TestClass.java";
        
        ParseResult parseResult = createMockParseResult(true);
        when(astParser.parse(javaCode, language)).thenReturn(parseResult);
        
        // When
        CompletableFuture<String> future = documentationGenerator.generateInlineComments(javaCode, language, filePath);
        String result = future.get();
        
        // Then
        assertNotNull(result);
        assertFalse(result.trim().isEmpty());
        verify(astParser).parse(javaCode, language);
    }
    
    @Test
    @DisplayName("Should create API documentation in markdown format")
    void shouldCreateAPIDocumentationInMarkdownFormat() throws ExecutionException, InterruptedException {
        // Given
        CodeContext context = createTestCodeContext();
        Documentation.Format format = Documentation.Format.MARKDOWN;
        
        ParseResult parseResult = createMockParseResult(true);
        when(astParser.parse(anyString(), anyString())).thenReturn(parseResult);
        
        // When
        CompletableFuture<Documentation> future = documentationGenerator.createAPIDocumentation(context, format);
        Documentation result = future.get();
        
        // Then
        assertNotNull(result);
        assertEquals(Documentation.Type.API_DOC, result.getType());
        assertEquals("TestClass.java", result.getElementName());
        assertEquals("module", result.getElementType());
        assertNotNull(result.getContent());
        verify(astParser).parse(anyString(), eq("java"));
    }
    
    @Test
    @DisplayName("Should update documentation based on code changes")
    void shouldUpdateDocumentationBasedOnCodeChanges() throws ExecutionException, InterruptedException {
        // Given
        List<CodeChange> changes = List.of(
                CodeChange.fileModified("/test/TestClass.java", "old content", "new content", 1, 10)
        );
        List<Documentation> existingDocs = List.of(
                createTestDocumentation("/test/TestClass.java")
        );
        
        // When
        CompletableFuture<List<DocumentationUpdate>> future = documentationGenerator.updateDocumentation(changes, existingDocs);
        List<DocumentationUpdate> result = future.get();
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(DocumentationUpdate.UpdateType.MODIFIED, result.get(0).getUpdateType());
    }
    
    @Test
    @DisplayName("Should validate documentation against code")
    void shouldValidateDocumentationAgainstCode() throws ExecutionException, InterruptedException {
        // Given
        Documentation documentation = createTestDocumentation("/test/TestClass.java");
        String code = "public class TestClass {\n    public void testMethod() {\n    }\n}";
        String language = "java";
        
        // When
        CompletableFuture<ValidationResult> future = documentationGenerator.validateDocumentation(documentation, code, language);
        ValidationResult result = future.get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.getAccuracyScore() >= 0.0 && result.getAccuracyScore() <= 1.0);
        assertNotNull(result.getStatus());
    }
    
    @Test
    @DisplayName("Should generate element documentation for Java method")
    void shouldGenerateElementDocumentationForJavaMethod() throws ExecutionException, InterruptedException {
        // Given
        String elementName = "testMethod";
        String elementType = "method";
        String code = "public void testMethod(String param) { return \"test\"; }";
        String language = "java";
        String filePath = "/test/TestClass.java";
        
        ParseResult parseResult = createMockParseResult(true);
        ASTNode methodNode = createMockMethodNode(elementName);
        when(astParser.parse(code, language)).thenReturn(parseResult);
        when(parseResult.getRootNode().getChildren()).thenReturn(List.of(methodNode));
        
        // When
        CompletableFuture<Documentation> future = documentationGenerator.generateElementDocumentation(
                elementName, elementType, code, language, filePath);
        Documentation result = future.get();
        
        // Then
        assertNotNull(result);
        assertEquals(Documentation.Type.JAVADOC, result.getType());
        assertEquals(elementName, result.getElementName());
        assertEquals(elementType, result.getElementType());
        assertEquals(filePath, result.getFilePath());
        assertNotNull(result.getContent());
    }
    
    @Test
    @DisplayName("Should create markdown documentation for project")
    void shouldCreateMarkdownDocumentationForProject() throws ExecutionException, InterruptedException {
        // Given
        ProjectContext projectContext = createTestProjectContext();
        String template = "# {project_name}\n\n{description}";
        
        // When
        CompletableFuture<Documentation> future = documentationGenerator.createMarkdownDocumentation(projectContext, template);
        Documentation result = future.get();
        
        // Then
        assertNotNull(result);
        assertEquals(Documentation.Type.MARKDOWN, result.getType());
        assertEquals(Documentation.Format.MARKDOWN, result.getFormat());
        assertEquals("test-project", result.getElementName());
        assertTrue(result.getContent().contains("test-project"));
    }
    
    @Test
    @DisplayName("Should synchronize documentation with code changes")
    void shouldSynchronizeDocumentationWithCodeChanges() throws ExecutionException, InterruptedException {
        // Given
        String filePath = "/test/TestClass.java";
        String oldCode = "public class TestClass { }";
        String newCode = "public class TestClass {\n    public void newMethod() { }\n}";
        String language = "java";
        
        ParseResult oldParseResult = createMockParseResult(true);
        ParseResult newParseResult = createMockParseResult(true);
        when(astParser.parse(oldCode, language)).thenReturn(oldParseResult);
        when(astParser.parse(newCode, language)).thenReturn(newParseResult);
        
        // When
        CompletableFuture<List<DocumentationUpdate>> future = documentationGenerator.synchronizeDocumentation(
                filePath, oldCode, newCode, language);
        List<DocumentationUpdate> result = future.get();
        
        // Then
        assertNotNull(result);
        // Result may be empty if no significant changes are detected
    }
    
    @Test
    @DisplayName("Should return available templates for language")
    void shouldReturnAvailableTemplatesForLanguage() {
        // Given
        String language = "java";
        String elementType = "method";
        
        // When
        List<String> templates = documentationGenerator.getAvailableTemplates(language, elementType);
        
        // Then
        assertNotNull(templates);
        assertFalse(templates.isEmpty());
        assertTrue(templates.contains("method") || templates.contains("default"));
    }
    
    @Test
    @DisplayName("Should check if documentation exists")
    void shouldCheckIfDocumentationExists() {
        // Given
        String filePath = "/test/TestClass.java";
        String elementName = "testMethod";
        String elementType = "method";
        
        // When
        boolean exists = documentationGenerator.hasDocumentation(filePath, elementName, elementType);
        
        // Then
        assertFalse(exists); // Should be false for new generator instance
    }
    
    @Test
    @DisplayName("Should calculate documentation statistics")
    void shouldCalculateDocumentationStatistics() {
        // Given
        ProjectContext projectContext = createTestProjectContext();
        
        // When
        DocumentationGenerator.DocumentationStats stats = documentationGenerator.getDocumentationStats(projectContext, null);
        
        // Then
        assertNotNull(stats);
        assertTrue(stats.getTotalElements() >= 0);
        assertTrue(stats.getDocumentedElements() >= 0);
        assertTrue(stats.getCoveragePercentage() >= 0.0 && stats.getCoveragePercentage() <= 100.0);
        assertTrue(stats.getAverageAccuracy() >= 0.0 && stats.getAverageAccuracy() <= 1.0);
    }
    
    @Test
    @DisplayName("Should handle parsing errors gracefully")
    void shouldHandleParsingErrorsGracefully() throws ExecutionException, InterruptedException {
        // Given
        String invalidCode = "invalid java code {{{";
        String language = "java";
        String filePath = "/test/Invalid.java";
        
        ParseResult parseResult = createMockParseResult(false);
        when(astParser.parse(invalidCode, language)).thenReturn(parseResult);
        
        // When
        CompletableFuture<String> future = documentationGenerator.generateInlineComments(invalidCode, language, filePath);
        String result = future.get();
        
        // Then
        assertNotNull(result);
        assertEquals(invalidCode, result); // Should return original code on parse failure
    }
    
    @Test
    @DisplayName("Should handle null or empty code gracefully")
    void shouldHandleNullOrEmptyCodeGracefully() throws ExecutionException, InterruptedException {
        // Given
        String emptyCode = "";
        String language = "java";
        String filePath = "/test/Empty.java";
        
        ParseResult parseResult = createMockParseResult(true);
        when(astParser.parse(emptyCode, language)).thenReturn(parseResult);
        
        // When
        CompletableFuture<String> future = documentationGenerator.generateInlineComments(emptyCode, language, filePath);
        String result = future.get();
        
        // Then
        assertNotNull(result);
    }
    
    // Helper methods for creating test data
    
    private CodeContext createTestCodeContext() {
        return CodeContext.builder()
                .fileName("TestClass.java")
                .language("java")
                .currentFile("public class TestClass {\n    public void testMethod() {\n    }\n}")
                .projectType("library")
                .build();
    }
    
    private Documentation createTestDocumentation(String filePath) {
        return Documentation.javadoc(
                "/**\n * Test class documentation\n */",
                filePath,
                1,
                "TestClass",
                "class"
        );
    }
    
    private ProjectContext createTestProjectContext() {
        ProjectStructure structure = ProjectStructure.builder()
                .files(List.of(
                        FileNode.builder()
                                .name("TestClass.java")
                                .path("/test/TestClass.java")
                                .size(1000L)
                                .lastModified(Instant.now())
                                .build()
                ))
                .modules(List.of())
                .relationships(List.of())
                .entryPoints(List.of("/test/Main.java"))
                .build();
        
        return ProjectContext.builder()
                .projectName("test-project")
                .rootPath("/test")
                .structure(structure)
                .dependencies(List.of())
                .patterns(List.of())
                .conventions(List.of())
                .complexity(ComplexityMetrics.builder()
                        .cyclomaticComplexity(5)
                        .nestingDepth(2)
                        .linesOfCode(100)
                        .numberOfMethods(5)
                        .numberOfClasses(2)
                        .build())
                .build();
    }
    
    private ParseResult createMockParseResult(boolean success) {
        ParseResult parseResult = mock(ParseResult.class);
        when(parseResult.isSuccess()).thenReturn(success);
        
        if (success) {
            ASTNode rootNode = mock(ASTNode.class);
            when(rootNode.getChildren()).thenReturn(List.of());
            when(parseResult.getRootNode()).thenReturn(rootNode);
        } else {
            when(parseResult.getErrors()).thenReturn(List.of(
                    ParseError.builder()
                            .message("Parse error")
                            .location(SourceLocation.builder().line(1).column(1).build())
                            .build()
            ));
        }
        
        return parseResult;
    }
    
    private ASTNode createMockMethodNode(String methodName) {
        ASTNode methodNode = mock(ASTNode.class);
        when(methodNode.getNodeType()).thenReturn(ASTNode.NodeType.METHOD);
        when(methodNode.getText()).thenReturn("public void " + methodName + "() {}");
        when(methodNode.getLocation()).thenReturn(SourceLocation.builder().line(2).column(5).build());
        when(methodNode.getChildren()).thenReturn(List.of());
        return methodNode;
    }
}