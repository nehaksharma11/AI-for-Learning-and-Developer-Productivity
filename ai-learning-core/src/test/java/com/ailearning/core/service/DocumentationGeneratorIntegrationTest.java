package com.ailearning.core.service;

import com.ailearning.core.model.*;
import com.ailearning.core.service.impl.DefaultDocumentationGenerator;
import com.ailearning.core.service.ast.impl.MultiLanguageASTParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DocumentationGenerator with real AST parsing.
 */
class DocumentationGeneratorIntegrationTest {
    
    private DocumentationGenerator documentationGenerator;
    
    @BeforeEach
    void setUp() {
        MultiLanguageASTParser astParser = new MultiLanguageASTParser();
        documentationGenerator = new DefaultDocumentationGenerator(astParser);
    }
    
    @Test
    @DisplayName("Should generate comprehensive Java documentation")
    void shouldGenerateComprehensiveJavaDocumentation() throws ExecutionException, InterruptedException {
        // Given
        String javaCode = """
                package com.example;
                
                public class UserService {
                    private String name;
                    
                    public UserService(String name) {
                        this.name = name;
                    }
                    
                    public String getName() {
                        return name;
                    }
                    
                    public void setName(String name) {
                        this.name = name;
                    }
                    
                    public boolean validateUser(String username, String password) {
                        return username != null && password != null && password.length() >= 8;
                    }
                }
                """;
        
        // When - Generate inline comments
        CompletableFuture<String> commentsFuture = documentationGenerator.generateInlineComments(
                javaCode, "java", "/src/UserService.java");
        String documentedCode = commentsFuture.get();
        
        // Then
        assertNotNull(documentedCode);
        assertFalse(documentedCode.trim().isEmpty());
        
        // When - Generate element documentation
        CompletableFuture<Documentation> docFuture = documentationGenerator.generateElementDocumentation(
                "validateUser", "method", javaCode, "java", "/src/UserService.java");
        Documentation methodDoc = docFuture.get();
        
        // Then
        assertNotNull(methodDoc);
        assertEquals(Documentation.Type.JAVADOC, methodDoc.getType());
        assertEquals("validateUser", methodDoc.getElementName());
        assertEquals("method", methodDoc.getElementType());
        assertTrue(methodDoc.getContent().contains("/**"));
        assertTrue(methodDoc.getContent().contains("*/"));
    }
    
    @Test
    @DisplayName("Should generate JavaScript/JSDoc documentation")
    void shouldGenerateJavaScriptJSDocDocumentation() throws ExecutionException, InterruptedException {
        // Given
        String jsCode = """
                class Calculator {
                    constructor() {
                        this.result = 0;
                    }
                    
                    add(a, b) {
                        return a + b;
                    }
                    
                    multiply(a, b) {
                        return a * b;
                    }
                    
                    calculate(operation, ...numbers) {
                        switch(operation) {
                            case 'add':
                                return numbers.reduce((sum, num) => sum + num, 0);
                            case 'multiply':
                                return numbers.reduce((product, num) => product * num, 1);
                            default:
                                return 0;
                        }
                    }
                }
                """;
        
        // When - Generate element documentation
        CompletableFuture<Documentation> docFuture = documentationGenerator.generateElementDocumentation(
                "calculate", "function", jsCode, "javascript", "/src/calculator.js");
        Documentation functionDoc = docFuture.get();
        
        // Then
        assertNotNull(functionDoc);
        assertEquals(Documentation.Type.JSDOC, functionDoc.getType());
        assertEquals("calculate", functionDoc.getElementName());
        assertEquals("function", functionDoc.getElementType());
        assertNotNull(functionDoc.getContent());
    }
    
    @Test
    @DisplayName("Should generate Python docstring documentation")
    void shouldGeneratePythonDocstringDocumentation() throws ExecutionException, InterruptedException {
        // Given
        String pythonCode = """
                class DataProcessor:
                    def __init__(self, data_source):
                        self.data_source = data_source
                        self.processed_data = []
                    
                    def process_data(self, filters=None):
                        if filters is None:
                            filters = []
                        
                        for item in self.data_source:
                            if self._apply_filters(item, filters):
                                self.processed_data.append(self._transform_item(item))
                        
                        return self.processed_data
                    
                    def _apply_filters(self, item, filters):
                        for filter_func in filters:
                            if not filter_func(item):
                                return False
                        return True
                    
                    def _transform_item(self, item):
                        return item.upper() if isinstance(item, str) else item
                """;
        
        // When - Generate element documentation
        CompletableFuture<Documentation> docFuture = documentationGenerator.generateElementDocumentation(
                "process_data", "function", pythonCode, "python", "/src/data_processor.py");
        Documentation functionDoc = docFuture.get();
        
        // Then
        assertNotNull(functionDoc);
        assertEquals(Documentation.Type.PYTHON_DOCSTRING, functionDoc.getType());
        assertEquals("process_data", functionDoc.getElementName());
        assertEquals("function", functionDoc.getElementType());
        assertNotNull(functionDoc.getContent());
    }
    
    @Test
    @DisplayName("Should create comprehensive API documentation")
    void shouldCreateComprehensiveAPIDocumentation() throws ExecutionException, InterruptedException {
        // Given
        CodeContext moduleContext = CodeContext.builder()
                .fileName("UserController.java")
                .language("java")
                .projectType("api")
                .currentFile("""
                        @RestController
                        @RequestMapping("/api/users")
                        public class UserController {
                            
                            @GetMapping
                            public List<User> getAllUsers() {
                                return userService.findAll();
                            }
                            
                            @GetMapping("/{id}")
                            public User getUserById(@PathVariable Long id) {
                                return userService.findById(id);
                            }
                            
                            @PostMapping
                            public User createUser(@RequestBody User user) {
                                return userService.save(user);
                            }
                            
                            @PutMapping("/{id}")
                            public User updateUser(@PathVariable Long id, @RequestBody User user) {
                                user.setId(id);
                                return userService.save(user);
                            }
                            
                            @DeleteMapping("/{id}")
                            public void deleteUser(@PathVariable Long id) {
                                userService.deleteById(id);
                            }
                        }
                        """)
                .build();
        
        // When - Generate API documentation in different formats
        CompletableFuture<Documentation> markdownFuture = documentationGenerator.createAPIDocumentation(
                moduleContext, Documentation.Format.MARKDOWN);
        Documentation markdownDoc = markdownFuture.get();
        
        CompletableFuture<Documentation> jsonFuture = documentationGenerator.createAPIDocumentation(
                moduleContext, Documentation.Format.JSON);
        Documentation jsonDoc = jsonFuture.get();
        
        // Then
        assertNotNull(markdownDoc);
        assertEquals(Documentation.Type.API_DOC, markdownDoc.getType());
        assertEquals("UserController.java", markdownDoc.getElementName());
        assertTrue(markdownDoc.getContent().contains("API Documentation"));
        
        assertNotNull(jsonDoc);
        assertEquals(Documentation.Type.API_DOC, jsonDoc.getType());
        assertTrue(jsonDoc.getContent().contains("UserController.java"));
    }
    
    @Test
    @DisplayName("Should validate documentation accuracy")
    void shouldValidateDocumentationAccuracy() throws ExecutionException, InterruptedException {
        // Given
        String code = """
                public class MathUtils {
                    public static int add(int a, int b) {
                        return a + b;
                    }
                    
                    public static int multiply(int x, int y) {
                        return x * y;
                    }
                }
                """;
        
        // Good documentation
        Documentation goodDoc = Documentation.javadoc(
                """
                /**
                 * Adds two integers and returns the result.
                 * 
                 * @param a the first integer
                 * @param b the second integer
                 * @return the sum of a and b
                 */
                """,
                "/src/MathUtils.java",
                2,
                "add",
                "method"
        );
        
        // Poor documentation
        Documentation poorDoc = Documentation.javadoc(
                """
                /**
                 * Does something with numbers.
                 * 
                 * @param x some number
                 * @param z another number
                 * @return something
                 */
                """,
                "/src/MathUtils.java",
                6,
                "multiply",
                "method"
        );
        
        // When
        CompletableFuture<ValidationResult> goodValidation = documentationGenerator.validateDocumentation(
                goodDoc, code, "java");
        ValidationResult goodResult = goodValidation.get();
        
        CompletableFuture<ValidationResult> poorValidation = documentationGenerator.validateDocumentation(
                poorDoc, code, "java");
        ValidationResult poorResult = poorValidation.get();
        
        // Then
        assertNotNull(goodResult);
        assertTrue(goodResult.getAccuracyScore() > 0.7, "Good documentation should have high accuracy score");
        
        assertNotNull(poorResult);
        assertTrue(poorResult.getAccuracyScore() < goodResult.getAccuracyScore(), 
                  "Poor documentation should have lower accuracy score");
        assertFalse(poorResult.getIssues().isEmpty(), "Poor documentation should have validation issues");
    }
    
    @Test
    @DisplayName("Should handle documentation updates and synchronization")
    void shouldHandleDocumentationUpdatesAndSynchronization() throws ExecutionException, InterruptedException {
        // Given
        String oldCode = """
                public class Calculator {
                    public int add(int a, int b) {
                        return a + b;
                    }
                }
                """;
        
        String newCode = """
                public class Calculator {
                    public int add(int a, int b) {
                        return a + b;
                    }
                    
                    public int subtract(int a, int b) {
                        return a - b;
                    }
                    
                    public int multiply(int a, int b) {
                        return a * b;
                    }
                }
                """;
        
        List<CodeChange> changes = List.of(
                CodeChange.fileModified("/src/Calculator.java", oldCode, newCode, 1, 15)
        );
        
        List<Documentation> existingDocs = List.of(
                Documentation.javadoc(
                        "/** Calculator class */",
                        "/src/Calculator.java",
                        1,
                        "Calculator",
                        "class"
                )
        );
        
        // When - Update documentation
        CompletableFuture<List<DocumentationUpdate>> updatesFuture = documentationGenerator.updateDocumentation(
                changes, existingDocs);
        List<DocumentationUpdate> updates = updatesFuture.get();
        
        // When - Synchronize documentation
        CompletableFuture<List<DocumentationUpdate>> syncFuture = documentationGenerator.synchronizeDocumentation(
                "/src/Calculator.java", oldCode, newCode, "java");
        List<DocumentationUpdate> syncUpdates = syncFuture.get();
        
        // Then
        assertNotNull(updates);
        assertFalse(updates.isEmpty());
        assertEquals(DocumentationUpdate.UpdateType.MODIFIED, updates.get(0).getUpdateType());
        
        assertNotNull(syncUpdates);
        // Sync updates may be empty if no significant structural changes are detected
    }
    
    @Test
    @DisplayName("Should generate project markdown documentation")
    void shouldGenerateProjectMarkdownDocumentation() throws ExecutionException, InterruptedException {
        // Given
        ProjectContext projectContext = createTestProjectContext();
        String customTemplate = """
                # {project_name}
                
                {description}
                
                ## Features
                
                - Feature 1
                - Feature 2
                
                ## Installation
                
                ```bash
                npm install {project_name}
                ```
                
                ## Usage
                
                ```javascript
                const lib = require('{project_name}');
                ```
                """;
        
        // When
        CompletableFuture<Documentation> docFuture = documentationGenerator.createMarkdownDocumentation(
                projectContext, customTemplate);
        Documentation markdownDoc = docFuture.get();
        
        // Then
        assertNotNull(markdownDoc);
        assertEquals(Documentation.Type.MARKDOWN, markdownDoc.getType());
        assertEquals(Documentation.Format.MARKDOWN, markdownDoc.getFormat());
        assertTrue(markdownDoc.getContent().contains("ai-learning-companion"));
        assertTrue(markdownDoc.getContent().contains("Features"));
        assertTrue(markdownDoc.getContent().contains("Installation"));
        assertTrue(markdownDoc.getContent().contains("Usage"));
    }
    
    @Test
    @DisplayName("Should calculate accurate documentation statistics")
    void shouldCalculateAccurateDocumentationStatistics() {
        // Given
        ProjectContext projectContext = createTestProjectContext();
        
        // When
        DocumentationGenerator.DocumentationStats projectStats = documentationGenerator.getDocumentationStats(
                projectContext, null);
        DocumentationGenerator.DocumentationStats fileStats = documentationGenerator.getDocumentationStats(
                null, "/src/TestClass.java");
        
        // Then
        assertNotNull(projectStats);
        assertTrue(projectStats.getCoveragePercentage() >= 0.0 && projectStats.getCoveragePercentage() <= 100.0);
        assertTrue(projectStats.getAverageAccuracy() >= 0.0 && projectStats.getAverageAccuracy() <= 1.0);
        
        assertNotNull(fileStats);
        assertTrue(fileStats.getCoveragePercentage() >= 0.0 && fileStats.getCoveragePercentage() <= 100.0);
        assertTrue(fileStats.getAverageAccuracy() >= 0.0 && fileStats.getAverageAccuracy() <= 1.0);
    }
    
    // Helper methods
    
    private ProjectContext createTestProjectContext() {
        ProjectStructure structure = ProjectStructure.builder()
                .files(List.of(
                        FileNode.builder()
                                .name("UserService.java")
                                .path("/src/UserService.java")
                                .size(2000L)
                                .lastModified(Instant.now())
                                .build(),
                        FileNode.builder()
                                .name("UserController.java")
                                .path("/src/UserController.java")
                                .size(1500L)
                                .lastModified(Instant.now())
                                .build(),
                        FileNode.builder()
                                .name("README.md")
                                .path("/README.md")
                                .size(500L)
                                .lastModified(Instant.now())
                                .build()
                ))
                .modules(List.of())
                .relationships(List.of())
                .entryPoints(List.of("/src/Application.java"))
                .build();
        
        return ProjectContext.builder()
                .projectName("ai-learning-companion")
                .rootPath("/project")
                .structure(structure)
                .dependencies(List.of())
                .patterns(List.of())
                .conventions(List.of())
                .complexity(ComplexityMetrics.builder()
                        .cyclomaticComplexity(15)
                        .nestingDepth(3)
                        .linesOfCode(500)
                        .numberOfMethods(25)
                        .numberOfClasses(5)
                        .build())
                .build();
    }
}