package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.impl.DocumentationStyleGuideChecker.StyleGuideComplianceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DocumentationStyleGuideChecker.
 */
class DocumentationStyleGuideCheckerTest {
    
    private DocumentationStyleGuideChecker styleGuideChecker;
    
    @BeforeEach
    void setUp() {
        styleGuideChecker = new DocumentationStyleGuideChecker();
    }
    
    @Test
    @DisplayName("Should validate proper Javadoc format")
    void shouldValidateProperJavadocFormat() {
        // Given
        Documentation javadoc = Documentation.builder()
                .type(Documentation.Type.JAVADOC)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("/**\n * This is a proper Javadoc comment.\n * @param param The parameter\n * @return The return value\n */")
                .filePath("TestClass.java")
                .elementName("testMethod")
                .elementType("method")
                .build();
        
        // When
        StyleGuideComplianceResult result = styleGuideChecker.checkCompliance(javadoc, "java", null);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getComplianceScore() > 0.7); // Should have good compliance
        assertEquals(StyleGuideComplianceResult.ComplianceLevel.MOSTLY_COMPLIANT, result.getComplianceLevel());
    }
    
    @Test
    @DisplayName("Should detect Javadoc format violations")
    void shouldDetectJavadocFormatViolations() {
        // Given
        Documentation badJavadoc = Documentation.builder()
                .type(Documentation.Type.JAVADOC)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("// This is not proper Javadoc format")
                .filePath("TestClass.java")
                .elementName("testMethod")
                .elementType("method")
                .build();
        
        // When
        StyleGuideComplianceResult result = styleGuideChecker.checkCompliance(badJavadoc, "java", null);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isCompliant());
        assertFalse(result.getIssues().isEmpty());
        
        boolean hasFormatIssue = result.getIssues().stream()
                .anyMatch(issue -> issue.getType() == ValidationResult.IssueType.STYLE);
        assertTrue(hasFormatIssue);
    }
    
    @Test
    @DisplayName("Should validate JSDoc format")
    void shouldValidateJSDocFormat() {
        // Given
        Documentation jsdoc = Documentation.builder()
                .type(Documentation.Type.JSDOC)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("/**\n * This is a JSDoc comment.\n * @param {string} param The parameter\n * @returns {boolean} The return value\n */")
                .filePath("test.js")
                .elementName("testFunction")
                .elementType("function")
                .build();
        
        // When
        StyleGuideComplianceResult result = styleGuideChecker.checkCompliance(jsdoc, "javascript", null);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getComplianceScore() > 0.8);
        assertTrue(result.isCompliant());
    }
    
    @Test
    @DisplayName("Should detect missing parameter types in JSDoc")
    void shouldDetectMissingParameterTypesInJSDoc() {
        // Given
        Documentation badJsdoc = Documentation.builder()
                .type(Documentation.Type.JSDOC)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("/**\n * Function description.\n * @param param Missing type information\n */")
                .filePath("test.js")
                .elementName("testFunction")
                .elementType("function")
                .build();
        
        // When
        StyleGuideComplianceResult result = styleGuideChecker.checkCompliance(badJsdoc, "javascript", null);
        
        // Then
        assertNotNull(result);
        assertFalse(result.getIssues().isEmpty());
        
        boolean hasTypeIssue = result.getIssues().stream()
                .anyMatch(issue -> issue.getType() == ValidationResult.IssueType.COMPLETENESS);
        assertTrue(hasTypeIssue);
    }
    
    @Test
    @DisplayName("Should validate Python docstring format")
    void shouldValidatePythonDocstringFormat() {
        // Given
        Documentation docstring = Documentation.builder()
                .type(Documentation.Type.PYTHON_DOCSTRING)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("\"\"\"This is a proper Python docstring.\n\nArgs:\n    param: The parameter\n\nReturns:\n    The return value\n\"\"\"")
                .filePath("test.py")
                .elementName("test_function")
                .elementType("function")
                .build();
        
        // When
        StyleGuideComplianceResult result = styleGuideChecker.checkCompliance(docstring, "python", null);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getComplianceScore() > 0.7);
        assertTrue(result.isCompliant());
    }
    
    @Test
    @DisplayName("Should detect Python docstring format violations")
    void shouldDetectPythonDocstringFormatViolations() {
        // Given
        Documentation badDocstring = Documentation.builder()
                .type(Documentation.Type.PYTHON_DOCSTRING)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("'Single quotes are not proper docstring format'")
                .filePath("test.py")
                .elementName("test_function")
                .elementType("function")
                .build();
        
        // When
        StyleGuideComplianceResult result = styleGuideChecker.checkCompliance(badDocstring, "python", null);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isCompliant());
        assertFalse(result.getIssues().isEmpty());
    }
    
    @Test
    @DisplayName("Should learn project conventions from existing documentation")
    void shouldLearnProjectConventionsFromExistingDocumentation() {
        // Given
        List<Documentation> existingDocs = List.of(
                Documentation.builder()
                        .type(Documentation.Type.JAVADOC)
                        .format(Documentation.Format.PLAIN_TEXT)
                        .content("/**\n * Common phrase: This method performs\n */")
                        .filePath("Class1.java")
                        .build(),
                Documentation.builder()
                        .type(Documentation.Type.JAVADOC)
                        .format(Documentation.Format.PLAIN_TEXT)
                        .content("/**\n * Common phrase: This method performs\n * @param param description\n */")
                        .filePath("Class2.java")
                        .build()
        );
        
        ProjectContext projectContext = ProjectContext.builder()
                .projectName("TestProject")
                .rootPath("/test")
                .build();
        
        // When
        styleGuideChecker.learnProjectConventions(existingDocs, projectContext);
        
        // Then - Should not throw exception and should learn patterns
        // This is more of an integration test to ensure the method works
        assertDoesNotThrow(() -> styleGuideChecker.learnProjectConventions(existingDocs, projectContext));
    }
    
    @Test
    @DisplayName("Should validate against learned conventions")
    void shouldValidateAgainstLearnedConventions() {
        // Given
        List<Documentation> existingDocs = List.of(
                Documentation.builder()
                        .type(Documentation.Type.JAVADOC)
                        .format(Documentation.Format.PLAIN_TEXT)
                        .content("/**\n * Standard format with @param tags\n * @param param description\n */")
                        .filePath("Class1.java")
                        .build()
        );
        
        Documentation newDoc = Documentation.builder()
                .type(Documentation.Type.JAVADOC)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("/**\n * Different format without standard tags\n */")
                .filePath("Class2.java")
                .build();
        
        // When
        ValidationResult result = styleGuideChecker.validateAgainstConventions(newDoc, existingDocs);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getAccuracyScore() >= 0.0);
        assertNotNull(result.getIssues());
        assertNotNull(result.getSuggestions());
    }
    
    @Test
    @DisplayName("Should handle empty documentation gracefully")
    void shouldHandleEmptyDocumentationGracefully() {
        // Given
        Documentation emptyDoc = Documentation.builder()
                .type(Documentation.Type.INLINE_COMMENT)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("")
                .filePath("test.java")
                .build();
        
        // When
        StyleGuideComplianceResult result = styleGuideChecker.checkCompliance(emptyDoc, "java", null);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isCompliant());
        assertFalse(result.getIssues().isEmpty());
        
        boolean hasCompletenessIssue = result.getIssues().stream()
                .anyMatch(issue -> issue.getType() == ValidationResult.IssueType.COMPLETENESS);
        assertTrue(hasCompletenessIssue);
    }
    
    @Test
    @DisplayName("Should generate improvement suggestions")
    void shouldGenerateImprovementSuggestions() {
        // Given
        Documentation poorDoc = Documentation.builder()
                .type(Documentation.Type.JAVADOC)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("// bad format\nno proper structure")
                .filePath("test.java")
                .elementName("method")
                .elementType("method")
                .build();
        
        // When
        StyleGuideComplianceResult result = styleGuideChecker.checkCompliance(poorDoc, "java", null);
        
        // Then
        assertNotNull(result);
        assertFalse(result.getSuggestions().isEmpty());
        
        boolean hasSuggestions = result.getSuggestions().stream()
                .anyMatch(suggestion -> suggestion.contains("style") || suggestion.contains("format"));
        assertTrue(hasSuggestions);
    }
    
    @Test
    @DisplayName("Should handle error conditions gracefully")
    void shouldHandleErrorConditionsGracefully() {
        // Given
        Documentation nullContentDoc = Documentation.builder()
                .type(Documentation.Type.JAVADOC)
                .format(Documentation.Format.PLAIN_TEXT)
                .content(null)
                .filePath("test.java")
                .build();
        
        // When & Then
        assertDoesNotThrow(() -> {
            StyleGuideComplianceResult result = styleGuideChecker.checkCompliance(nullContentDoc, "java", null);
            assertNotNull(result);
        });
    }
    
    @Test
    @DisplayName("Should calculate compliance scores correctly")
    void shouldCalculateComplianceScoresCorrectly() {
        // Given
        Documentation perfectDoc = Documentation.builder()
                .type(Documentation.Type.JAVADOC)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("/**\n * Perfect Javadoc comment with proper format.\n * @param param Parameter description\n * @return Return description\n */")
                .filePath("test.java")
                .elementName("method")
                .elementType("method")
                .build();
        
        Documentation poorDoc = Documentation.builder()
                .type(Documentation.Type.JAVADOC)
                .format(Documentation.Format.PLAIN_TEXT)
                .content("bad")
                .filePath("test.java")
                .elementName("method")
                .elementType("method")
                .build();
        
        // When
        StyleGuideComplianceResult perfectResult = styleGuideChecker.checkCompliance(perfectDoc, "java", null);
        StyleGuideComplianceResult poorResult = styleGuideChecker.checkCompliance(poorDoc, "java", null);
        
        // Then
        assertTrue(perfectResult.getComplianceScore() > poorResult.getComplianceScore());
        assertTrue(perfectResult.getComplianceScore() >= 0.0 && perfectResult.getComplianceScore() <= 1.0);
        assertTrue(poorResult.getComplianceScore() >= 0.0 && poorResult.getComplianceScore() <= 1.0);
    }
}