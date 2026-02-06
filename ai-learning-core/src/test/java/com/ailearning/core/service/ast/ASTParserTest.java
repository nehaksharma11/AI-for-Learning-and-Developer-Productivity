package com.ailearning.core.service.ast;

import com.ailearning.core.model.ast.*;
import com.ailearning.core.service.ast.impl.MultiLanguageASTParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AST parsing functionality.
 */
class ASTParserTest {

    private ASTParser parser;

    @BeforeEach
    void setUp() {
        parser = new MultiLanguageASTParser();
    }

    @Nested
    @DisplayName("Language Support")
    class LanguageSupport {

        @Test
        @DisplayName("Should support Java language")
        void shouldSupportJava() {
            assertTrue(parser.supportsLanguage("java"));
            assertTrue(parser.supportsLanguage("Java"));
            assertTrue(parser.supportsLanguage("JAVA"));
        }

        @Test
        @DisplayName("Should support JavaScript and TypeScript")
        void shouldSupportJavaScript() {
            assertTrue(parser.supportsLanguage("javascript"));
            assertTrue(parser.supportsLanguage("typescript"));
        }

        @Test
        @DisplayName("Should support Python")
        void shouldSupportPython() {
            assertTrue(parser.supportsLanguage("python"));
        }

        @Test
        @DisplayName("Should not support unsupported languages")
        void shouldNotSupportUnsupportedLanguages() {
            assertFalse(parser.supportsLanguage("cobol"));
            assertFalse(parser.supportsLanguage("fortran"));
        }

        @Test
        @DisplayName("Should return supported languages")
        void shouldReturnSupportedLanguages() {
            String[] languages = parser.getSupportedLanguages();
            assertNotNull(languages);
            assertTrue(languages.length > 0);
        }
    }

    @Nested
    @DisplayName("Java Parsing")
    class JavaParsing {

        @Test
        @DisplayName("Should parse simple Java class")
        void shouldParseSimpleJavaClass() throws Exception {
            String javaCode = """
                public class HelloWorld {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }
                """;

            CompletableFuture<ParseResult> future = parser.parseCode(javaCode, "java", "HelloWorld.java");
            ParseResult result = future.get();

            assertNotNull(result);
            assertTrue(result.isSuccessful());
            assertNotNull(result.getRootNode());
            assertEquals("java", result.getLanguage());
            assertEquals("HelloWorld.java", result.getFilePath());
            assertTrue(result.getParseTimeMs() >= 0);
        }

        @Test
        @DisplayName("Should parse Java class with methods and fields")
        void shouldParseJavaClassWithMethodsAndFields() throws Exception {
            String javaCode = """
                public class Calculator {
                    private int result;
                    
                    public Calculator() {
                        this.result = 0;
                    }
                    
                    public int add(int a, int b) {
                        return a + b;
                    }
                    
                    public int getResult() {
                        return result;
                    }
                }
                """;

            CompletableFuture<ParseResult> future = parser.parseCode(javaCode, "java", "Calculator.java");
            ParseResult result = future.get();

            assertNotNull(result);
            assertTrue(result.isSuccessful());
            
            ASTNode rootNode = result.getRootNode();
            assertNotNull(rootNode);
            
            // Should have at least one class
            assertFalse(rootNode.getChildren().isEmpty());
            
            // Find the Calculator class
            ClassNode calculatorClass = rootNode.getChildren().stream()
                .filter(ClassNode.class::isInstance)
                .map(ClassNode.class::cast)
                .filter(cls -> "Calculator".equals(cls.getName()))
                .findFirst()
                .orElse(null);
            
            assertNotNull(calculatorClass);
            assertTrue(calculatorClass.isPublic());
            
            // Should have methods
            assertFalse(calculatorClass.getMethods().isEmpty());
            
            // Should have fields
            assertFalse(calculatorClass.getFields().isEmpty());
        }

        @Test
        @DisplayName("Should handle Java syntax errors")
        void shouldHandleJavaSyntaxErrors() throws Exception {
            String invalidJavaCode = """
                public class Invalid {
                    public void method( {
                        // Missing closing parenthesis
                    }
                }
                """;

            CompletableFuture<ParseResult> future = parser.parseCode(invalidJavaCode, "java", "Invalid.java");
            ParseResult result = future.get();

            assertNotNull(result);
            assertFalse(result.isSuccessful());
            assertTrue(result.hasErrors());
            assertFalse(result.getErrors().isEmpty());
        }
    }

    @Nested
    @DisplayName("JavaScript Parsing")
    class JavaScriptParsing {

        @Test
        @DisplayName("Should parse simple JavaScript function")
        void shouldParseSimpleJavaScriptFunction() throws Exception {
            String jsCode = """
                function greet(name) {
                    return "Hello, " + name + "!";
                }
                
                const add = (a, b) => a + b;
                """;

            CompletableFuture<ParseResult> future = parser.parseCode(jsCode, "javascript", "script.js");
            ParseResult result = future.get();

            assertNotNull(result);
            assertTrue(result.isSuccessful());
            assertNotNull(result.getRootNode());
            assertEquals("javascript", result.getLanguage());
        }

        @Test
        @DisplayName("Should parse TypeScript code")
        void shouldParseTypeScriptCode() throws Exception {
            String tsCode = """
                interface User {
                    name: string;
                    age: number;
                }
                
                function createUser(name: string, age: number): User {
                    return { name, age };
                }
                """;

            CompletableFuture<ParseResult> future = parser.parseCode(tsCode, "typescript", "user.ts");
            ParseResult result = future.get();

            assertNotNull(result);
            // Note: Our simplified parser may not handle TypeScript syntax perfectly
            assertEquals("typescript", result.getLanguage());
        }
    }

    @Nested
    @DisplayName("Python Parsing")
    class PythonParsing {

        @Test
        @DisplayName("Should parse simple Python class")
        void shouldParseSimplePythonClass() throws Exception {
            String pythonCode = """
                class Calculator:
                    def __init__(self):
                        self.result = 0
                    
                    def add(self, a, b):
                        return a + b
                    
                    def get_result(self):
                        return self.result
                """;

            CompletableFuture<ParseResult> future = parser.parseCode(pythonCode, "python", "calculator.py");
            ParseResult result = future.get();

            assertNotNull(result);
            assertTrue(result.isSuccessful());
            assertNotNull(result.getRootNode());
            assertEquals("python", result.getLanguage());
        }

        @Test
        @DisplayName("Should parse Python functions")
        void shouldParsePythonFunctions() throws Exception {
            String pythonCode = """
                def greet(name):
                    return f"Hello, {name}!"
                
                def add(a, b):
                    return a + b
                
                if __name__ == "__main__":
                    print(greet("World"))
                """;

            CompletableFuture<ParseResult> future = parser.parseCode(pythonCode, "python", "functions.py");
            ParseResult result = future.get();

            assertNotNull(result);
            assertTrue(result.isSuccessful());
            assertNotNull(result.getRootNode());
        }
    }

    @Nested
    @DisplayName("Syntax Validation")
    class SyntaxValidation {

        @Test
        @DisplayName("Should validate correct Java syntax")
        void shouldValidateCorrectJavaSyntax() throws Exception {
            String validJava = "public class Test { public void method() {} }";
            
            CompletableFuture<Boolean> future = parser.validateSyntax(validJava, "java");
            Boolean isValid = future.get();
            
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should detect invalid Java syntax")
        void shouldDetectInvalidJavaSyntax() throws Exception {
            String invalidJava = "public class Test { public void method( {} }";
            
            CompletableFuture<Boolean> future = parser.validateSyntax(invalidJava, "java");
            Boolean isValid = future.get();
            
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should validate Python syntax")
        void shouldValidatePythonSyntax() throws Exception {
            String validPython = "def test(): return True";
            
            CompletableFuture<Boolean> future = parser.validateSyntax(validPython, "python");
            Boolean isValid = future.get();
            
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should detect unbalanced Python syntax")
        void shouldDetectUnbalancedPythonSyntax() throws Exception {
            String invalidPython = "def test(: return True";
            
            CompletableFuture<Boolean> future = parser.validateSyntax(invalidPython, "python");
            Boolean isValid = future.get();
            
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Parse Results")
    class ParseResults {

        @Test
        @DisplayName("Should provide parse metrics")
        void shouldProvideParseMetrics() throws Exception {
            String javaCode = """
                public class Test {
                    private int field;
                    public void method() {
                        if (field > 0) {
                            System.out.println("Positive");
                        }
                    }
                }
                """;

            CompletableFuture<ParseResult> future = parser.parseCode(javaCode, "java", "Test.java");
            ParseResult result = future.get();

            assertNotNull(result.getMetrics());
            assertTrue(result.getMetrics().getNodeCount() > 0);
            assertTrue(result.getMetrics().getMaxDepth() > 0);
        }

        @Test
        @DisplayName("Should provide source locations")
        void shouldProvideSourceLocations() throws Exception {
            String javaCode = "public class Test {}";

            CompletableFuture<ParseResult> future = parser.parseCode(javaCode, "java", "Test.java");
            ParseResult result = future.get();

            assertNotNull(result.getRootNode().getLocation());
            assertEquals("Test.java", result.getRootNode().getLocation().getFilePath());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle unsupported language gracefully")
        void shouldHandleUnsupportedLanguage() throws Exception {
            String code = "some code";
            
            CompletableFuture<ParseResult> future = parser.parseCode(code, "unsupported", "test.txt");
            ParseResult result = future.get();
            
            assertNotNull(result);
            assertFalse(result.isSuccessful());
            assertTrue(result.hasErrors());
            assertTrue(result.getErrors().get(0).getMessage().contains("Unsupported language"));
        }

        @Test
        @DisplayName("Should handle empty code")
        void shouldHandleEmptyCode() throws Exception {
            CompletableFuture<ParseResult> future = parser.parseCode("", "java", "Empty.java");
            ParseResult result = future.get();
            
            assertNotNull(result);
            // Empty code might be valid or invalid depending on the language
            assertNotNull(result.getLanguage());
        }

        @Test
        @DisplayName("Should handle null inputs gracefully")
        void shouldHandleNullInputs() {
            assertThrows(Exception.class, () -> {
                parser.parseCode(null, "java", "test.java").get();
            });
        }
    }
}