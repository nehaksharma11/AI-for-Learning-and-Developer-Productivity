package com.ailearning.core.service.ai.impl;

import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ai.AIBreakdown;
import com.ailearning.core.model.ai.AIExample;
import com.ailearning.core.model.ai.AIExplanation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FallbackAIService Tests")
class FallbackAIServiceTest {

    private FallbackAIService fallbackService;
    private CodeContext codeContext;
    private ProjectContext projectContext;

    @BeforeEach
    void setUp() {
        fallbackService = new FallbackAIService(true);
        
        codeContext = CodeContext.builder()
                .description("Test context")
                .build();
        
        projectContext = ProjectContext.builder()
                .projectName("TestProject")
                .build();
    }

    @Nested
    @DisplayName("Service Configuration Tests")
    class ServiceConfigurationTests {

        @Test
        @DisplayName("Should be available when enabled")
        void shouldBeAvailableWhenEnabled() {
            assertTrue(fallbackService.isAvailable());
            assertEquals("Fallback", fallbackService.getServiceName());
            assertEquals(10, fallbackService.getPriority());
        }

        @Test
        @DisplayName("Should not be available when disabled")
        void shouldNotBeAvailableWhenDisabled() {
            FallbackAIService disabledService = new FallbackAIService(false);
            assertFalse(disabledService.isAvailable());
        }

        @Test
        @DisplayName("Should be enabled by default")
        void shouldBeEnabledByDefault() {
            FallbackAIService defaultService = new FallbackAIService();
            assertTrue(defaultService.isAvailable());
        }
    }

    @Nested
    @DisplayName("Code Explanation Tests")
    class CodeExplanationTests {

        @Test
        @DisplayName("Should explain Java class code")
        void shouldExplainJavaClassCode() throws ExecutionException, InterruptedException {
            String javaCode = "public class TestClass {\n    private String name;\n    public void setName(String name) {\n        this.name = name;\n    }\n}";
            
            AIExplanation explanation = fallbackService.explainCode(javaCode, codeContext, projectContext).get();
            
            assertNotNull(explanation);
            assertEquals(javaCode, explanation.getCodeSnippet());
            assertTrue(explanation.getExplanation().contains("class"));
            assertTrue(explanation.getExplanation().contains("method"));
            assertEquals("Java", explanation.getLanguage());
            assertEquals("Fallback", explanation.getServiceProvider());
            assertTrue(explanation.getConfidenceScore() > 0);
        }

        @Test
        @DisplayName("Should explain JavaScript function code")
        void shouldExplainJavaScriptFunctionCode() throws ExecutionException, InterruptedException {
            String jsCode = "function calculateSum(a, b) {\n    return a + b;\n}";
            
            AIExplanation explanation = fallbackService.explainCode(jsCode, codeContext, projectContext).get();
            
            assertNotNull(explanation);
            assertEquals("JavaScript", explanation.getLanguage());
            assertTrue(explanation.getExplanation().contains("method") || explanation.getExplanation().contains("function"));
            assertFalse(explanation.getKeyPoints().isEmpty());
        }

        @Test
        @DisplayName("Should explain Python code")
        void shouldExplainPythonCode() throws ExecutionException, InterruptedException {
            String pythonCode = "def calculate_average(numbers):\n    return sum(numbers) / len(numbers)";
            
            AIExplanation explanation = fallbackService.explainCode(pythonCode, codeContext, projectContext).get();
            
            assertNotNull(explanation);
            assertEquals("Python", explanation.getLanguage());
            assertTrue(explanation.getExplanation().contains("method") || explanation.getExplanation().contains("function"));
        }

        @Test
        @DisplayName("Should handle loop code")
        void shouldHandleLoopCode() throws ExecutionException, InterruptedException {
            String loopCode = "for (int i = 0; i < 10; i++) {\n    System.out.println(i);\n}";
            
            AIExplanation explanation = fallbackService.explainCode(loopCode, codeContext, projectContext).get();
            
            assertNotNull(explanation);
            assertTrue(explanation.getExplanation().contains("iterative") || explanation.getExplanation().contains("loop"));
            assertEquals("INTERMEDIATE", explanation.getDifficulty());
        }

        @Test
        @DisplayName("Should handle conditional code")
        void shouldHandleConditionalCode() throws ExecutionException, InterruptedException {
            String conditionalCode = "if (x > 0) {\n    return true;\n} else {\n    return false;\n}";
            
            AIExplanation explanation = fallbackService.explainCode(conditionalCode, codeContext, projectContext).get();
            
            assertNotNull(explanation);
            assertTrue(explanation.getExplanation().contains("conditional"));
            assertFalse(explanation.getKeyPoints().isEmpty());
        }

        @Test
        @DisplayName("Should assess difficulty correctly")
        void shouldAssessDifficultyCorrectly() throws ExecutionException, InterruptedException {
            // Simple code
            String simpleCode = "int x = 5;";
            AIExplanation simpleExplanation = fallbackService.explainCode(simpleCode, codeContext, projectContext).get();
            assertEquals("BEGINNER", simpleExplanation.getDifficulty());
            
            // Complex code
            String complexCode = "public class ComplexClass {\n" +
                    "    public void method1() { for(int i=0; i<10; i++) { if(i%2==0) { System.out.println(i); } } }\n" +
                    "    public void method2() { while(true) { break; } }\n" +
                    "    public void method3() { switch(x) { case 1: break; } }\n" +
                    "}";
            AIExplanation complexExplanation = fallbackService.explainCode(complexCode, codeContext, projectContext).get();
            assertEquals("ADVANCED", complexExplanation.getDifficulty());
        }
    }

    @Nested
    @DisplayName("Example Generation Tests")
    class ExampleGenerationTests {

        @Test
        @DisplayName("Should generate examples for pattern")
        void shouldGenerateExamplesForPattern() throws ExecutionException, InterruptedException {
            List<AIExample> examples = fallbackService.generateExamples("Factory Pattern", projectContext).get();
            
            assertNotNull(examples);
            assertEquals(2, examples.size());
            
            AIExample basicExample = examples.get(0);
            assertEquals("Basic Factory Pattern Pattern", basicExample.getTitle());
            assertTrue(basicExample.getDescription().contains("Factory Pattern"));
            assertEquals("Java", basicExample.getLanguage());
            assertEquals("BEGINNER", basicExample.getDifficulty());
            assertEquals("Fallback", basicExample.getServiceProvider());
            
            AIExample commonExample = examples.get(1);
            assertEquals("Common Factory Pattern Usage", commonExample.getTitle());
            assertEquals("INTERMEDIATE", commonExample.getDifficulty());
        }

        @Test
        @DisplayName("Should handle null project context")
        void shouldHandleNullProjectContext() throws ExecutionException, InterruptedException {
            List<AIExample> examples = fallbackService.generateExamples("Singleton", null).get();
            
            assertNotNull(examples);
            assertFalse(examples.isEmpty());
            assertFalse(examples.get(0).isProjectSpecific());
        }
    }

    @Nested
    @DisplayName("Breakdown Creation Tests")
    class BreakdownCreationTests {

        @Test
        @DisplayName("Should create breakdown for complex code")
        void shouldCreateBreakdownForComplexCode() throws ExecutionException, InterruptedException {
            String complexCode = "public class Calculator {\n" +
                    "    public double calculate(String operation, double a, double b) {\n" +
                    "        switch(operation) {\n" +
                    "            case \"add\": return a + b;\n" +
                    "            case \"subtract\": return a - b;\n" +
                    "            default: throw new IllegalArgumentException();\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";
            
            AIBreakdown breakdown = fallbackService.createBreakdown(complexCode, codeContext).get();
            
            assertNotNull(breakdown);
            assertEquals(complexCode, breakdown.getOriginalCode());
            assertTrue(breakdown.getOverview().contains("object-oriented"));
            assertEquals("Java", breakdown.getLanguage());
            assertEquals("Fallback", breakdown.getServiceProvider());
            assertFalse(breakdown.getSteps().isEmpty());
            assertEquals(2, breakdown.getSteps().size());
            assertFalse(breakdown.getPrerequisites().isEmpty());
            assertFalse(breakdown.getLearningObjectives().isEmpty());
        }

        @Test
        @DisplayName("Should assess complexity correctly")
        void shouldAssessComplexityCorrectly() throws ExecutionException, InterruptedException {
            // Simple code
            String simpleCode = "int x = 5;";
            AIBreakdown simpleBreakdown = fallbackService.createBreakdown(simpleCode, codeContext).get();
            assertEquals("LOW", simpleBreakdown.getComplexity());
            
            // Complex code with multiple constructs
            String complexCode = "public class Test {\n" +
                    "    public void method() {\n" +
                    "        for(int i=0; i<10; i++) {\n" +
                    "            if(i%2==0) {\n" +
                    "                while(true) { break; }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";
            AIBreakdown complexBreakdown = fallbackService.createBreakdown(complexCode, codeContext).get();
            assertEquals("HIGH", complexBreakdown.getComplexity());
        }
    }

    @Nested
    @DisplayName("Language Detection Tests")
    class LanguageDetectionTests {

        @Test
        @DisplayName("Should detect Java correctly")
        void shouldDetectJavaCorrectly() throws ExecutionException, InterruptedException {
            String javaCode = "public class Test { public static void main(String[] args) { System.out.println(\"Hello\"); } }";
            AIExplanation explanation = fallbackService.explainCode(javaCode, codeContext, projectContext).get();
            assertEquals("Java", explanation.getLanguage());
        }

        @Test
        @DisplayName("Should detect JavaScript correctly")
        void shouldDetectJavaScriptCorrectly() throws ExecutionException, InterruptedException {
            String jsCode = "const greeting = () => { console.log('Hello'); };";
            AIExplanation explanation = fallbackService.explainCode(jsCode, codeContext, projectContext).get();
            assertEquals("JavaScript", explanation.getLanguage());
        }

        @Test
        @DisplayName("Should detect Python correctly")
        void shouldDetectPythonCorrectly() throws ExecutionException, InterruptedException {
            String pythonCode = "def greet():\n    print('Hello')";
            AIExplanation explanation = fallbackService.explainCode(pythonCode, codeContext, projectContext).get();
            assertEquals("Python", explanation.getLanguage());
        }

        @Test
        @DisplayName("Should detect C# correctly")
        void shouldDetectCSharpCorrectly() throws ExecutionException, InterruptedException {
            String csharpCode = "using System; namespace Test { class Program { static void Main() { Console.WriteLine(\"Hello\"); } } }";
            AIExplanation explanation = fallbackService.explainCode(csharpCode, codeContext, projectContext).get();
            assertEquals("C#", explanation.getLanguage());
        }

        @Test
        @DisplayName("Should detect C++ correctly")
        void shouldDetectCppCorrectly() throws ExecutionException, InterruptedException {
            String cppCode = "#include <iostream>\nint main() { std::cout << \"Hello\"; return 0; }";
            AIExplanation explanation = fallbackService.explainCode(cppCode, codeContext, projectContext).get();
            assertEquals("C++", explanation.getLanguage());
        }

        @Test
        @DisplayName("Should return UNKNOWN for unrecognized language")
        void shouldReturnUnknownForUnrecognizedLanguage() throws ExecutionException, InterruptedException {
            String unknownCode = "some random text that doesn't look like code";
            AIExplanation explanation = fallbackService.explainCode(unknownCode, codeContext, projectContext).get();
            assertEquals("UNKNOWN", explanation.getLanguage());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null code snippet gracefully")
        void shouldHandleNullCodeSnippetGracefully() {
            assertThrows(ExecutionException.class, () -> {
                fallbackService.explainCode(null, codeContext, projectContext).get();
            });
        }

        @Test
        @DisplayName("Should handle empty code snippet")
        void shouldHandleEmptyCodeSnippet() throws ExecutionException, InterruptedException {
            AIExplanation explanation = fallbackService.explainCode("", codeContext, projectContext).get();
            
            assertNotNull(explanation);
            assertEquals("", explanation.getCodeSnippet());
            assertNotNull(explanation.getExplanation());
        }

        @Test
        @DisplayName("Should handle null context gracefully")
        void shouldHandleNullContextGracefully() throws ExecutionException, InterruptedException {
            AIExplanation explanation = fallbackService.explainCode("int x = 5;", null, null).get();
            
            assertNotNull(explanation);
            assertNotNull(explanation.getExplanation());
        }
    }

    @Nested
    @DisplayName("Pattern Recognition Tests")
    class PatternRecognitionTests {

        @Test
        @DisplayName("Should recognize class patterns")
        void shouldRecognizeClassPatterns() throws ExecutionException, InterruptedException {
            String classCode = "public class MyClass { private int value; }";
            AIExplanation explanation = fallbackService.explainCode(classCode, codeContext, projectContext).get();
            
            assertTrue(explanation.getKeyPoints().contains("Object-oriented structure"));
        }

        @Test
        @DisplayName("Should recognize method patterns")
        void shouldRecognizeMethodPatterns() throws ExecutionException, InterruptedException {
            String methodCode = "public void doSomething() { return; }";
            AIExplanation explanation = fallbackService.explainCode(methodCode, codeContext, projectContext).get();
            
            assertTrue(explanation.getKeyPoints().contains("Method implementation"));
        }

        @Test
        @DisplayName("Should recognize loop patterns")
        void shouldRecognizeLoopPatterns() throws ExecutionException, InterruptedException {
            String loopCode = "for(int i = 0; i < 10; i++) { }";
            AIExplanation explanation = fallbackService.explainCode(loopCode, codeContext, projectContext).get();
            
            assertTrue(explanation.getKeyPoints().contains("Iterative processing"));
        }

        @Test
        @DisplayName("Should recognize conditional patterns")
        void shouldRecognizeConditionalPatterns() throws ExecutionException, InterruptedException {
            String conditionalCode = "if(x > 0) { return true; }";
            AIExplanation explanation = fallbackService.explainCode(conditionalCode, codeContext, projectContext).get();
            
            assertTrue(explanation.getKeyPoints().contains("Conditional logic"));
        }
    }
}