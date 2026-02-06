package com.ailearning.core.service.impl;

import com.ailearning.core.model.DataClassification;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ProjectStructure;
import com.ailearning.core.model.FileNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDataClassificationServiceTest {
    
    private DefaultDataClassificationService service;
    
    @BeforeEach
    void setUp() {
        service = new DefaultDataClassificationService();
    }
    
    @Test
    void testClassifyDataWithSensitiveContent() {
        ProjectContext context = createTestProjectContext("test-project");
        String sensitiveContent = "String password = \"secret123\"; // API key: abc123";
        
        DataClassification result = service.classifyData(context, sensitiveContent);
        
        assertEquals("test-project", result.getProjectId());
        assertEquals(DataClassification.SensitivityLevel.CONFIDENTIAL, result.getSensitivityLevel());
        assertTrue(result.getDataTypes().contains(DataClassification.DataType.SOURCE_CODE));
        assertTrue(result.getDataTypes().contains(DataClassification.DataType.COMMENTS));
        assertFalse(result.isUserOverride());
        assertEquals("automatic-analysis", result.getClassificationSource());
        assertTrue(result.getReason().contains("Sensitive patterns detected"));
    }
    
    @Test
    void testClassifyDataWithNormalContent() {
        ProjectContext context = createTestProjectContext("normal-project");
        String normalContent = "public class Calculator { public int add(int a, int b) { return a + b; } }";
        
        DataClassification result = service.classifyData(context, normalContent);
        
        assertEquals("normal-project", result.getProjectId());
        assertEquals(DataClassification.SensitivityLevel.INTERNAL, result.getSensitivityLevel());
        assertTrue(result.getDataTypes().contains(DataClassification.DataType.SOURCE_CODE));
        assertFalse(result.isUserOverride());
    }
    
    @Test
    void testClassifyDataWithSensitiveProjectName() {
        ProjectContext context = createTestProjectContext("banking-system");
        String normalContent = "public class Account { }";
        
        DataClassification result = service.classifyData(context, normalContent);
        
        assertEquals("banking-system", result.getProjectId());
        assertEquals(DataClassification.SensitivityLevel.CONFIDENTIAL, result.getSensitivityLevel());
        assertTrue(result.getReason().contains("Project name indicates sensitive domain"));
    }
    
    @Test
    void testClassifyProject() {
        List<DataClassification.DataType> dataTypes = List.of(
                DataClassification.DataType.SOURCE_CODE,
                DataClassification.DataType.CONFIGURATION
        );
        
        DataClassification result = service.classifyProject("test-project", dataTypes);
        
        assertEquals("test-project", result.getProjectId());
        assertEquals(DataClassification.SensitivityLevel.INTERNAL, result.getSensitivityLevel());
        assertEquals(Set.copyOf(dataTypes), result.getDataTypes());
        assertEquals("project-analysis", result.getClassificationSource());
    }
    
    @Test
    void testClassifyProjectWithSensitiveName() {
        List<DataClassification.DataType> dataTypes = List.of(DataClassification.DataType.SOURCE_CODE);
        
        DataClassification result = service.classifyProject("healthcare-portal", dataTypes);
        
        assertEquals("healthcare-portal", result.getProjectId());
        assertEquals(DataClassification.SensitivityLevel.CONFIDENTIAL, result.getSensitivityLevel());
        assertTrue(result.getReason().contains("Project name indicates sensitive domain"));
    }
    
    @Test
    void testUpdateClassification() {
        // First classify normally
        service.classifyProject("test-project", List.of(DataClassification.DataType.SOURCE_CODE));
        
        // Then update with user override
        DataClassification updated = service.updateClassification(
                "test-project", 
                DataClassification.SensitivityLevel.RESTRICTED, 
                "User marked as highly sensitive");
        
        assertEquals("test-project", updated.getProjectId());
        assertEquals(DataClassification.SensitivityLevel.RESTRICTED, updated.getSensitivityLevel());
        assertTrue(updated.isUserOverride());
        assertEquals("User marked as highly sensitive", updated.getReason());
        assertEquals("user-override", updated.getClassificationSource());
    }
    
    @Test
    void testGetClassification() {
        // Initially empty
        Optional<DataClassification> result = service.getClassification("nonexistent-project");
        assertTrue(result.isEmpty());
        
        // After classification
        service.classifyProject("test-project", List.of(DataClassification.DataType.SOURCE_CODE));
        result = service.getClassification("test-project");
        assertTrue(result.isPresent());
        assertEquals("test-project", result.get().getProjectId());
    }
    
    @Test
    void testSuggestSensitivityLevel() {
        ProjectContext normalContext = createTestProjectContext("normal-project");
        assertEquals(DataClassification.SensitivityLevel.INTERNAL, 
                service.suggestSensitivityLevel(normalContext));
        
        ProjectContext sensitiveContext = createTestProjectContext("government-system");
        assertEquals(DataClassification.SensitivityLevel.CONFIDENTIAL, 
                service.suggestSensitivityLevel(sensitiveContext));
    }
    
    @Test
    void testContainsSensitivePatterns() {
        assertTrue(service.containsSensitivePatterns("String password = \"secret\";"));
        assertTrue(service.containsSensitivePatterns("API_KEY = \"abc123\";"));
        assertTrue(service.containsSensitivePatterns("// This is confidential information"));
        assertTrue(service.containsSensitivePatterns("private_key = loadKey();"));
        assertTrue(service.containsSensitivePatterns("database_url = \"jdbc://localhost\";"));
        
        assertFalse(service.containsSensitivePatterns("public class Calculator { }"));
        assertFalse(service.containsSensitivePatterns("int result = add(1, 2);"));
    }
    
    @Test
    void testGetAllClassifications() {
        assertTrue(service.getAllClassifications().isEmpty());
        
        service.classifyProject("project1", List.of(DataClassification.DataType.SOURCE_CODE));
        service.classifyProject("project2", List.of(DataClassification.DataType.DOCUMENTATION));
        
        List<DataClassification> all = service.getAllClassifications();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(c -> c.getProjectId().equals("project1")));
        assertTrue(all.stream().anyMatch(c -> c.getProjectId().equals("project2")));
    }
    
    @Test
    void testCaching() {
        ProjectContext context = createTestProjectContext("test-project");
        String content = "public class Test { }";
        
        // First call
        DataClassification result1 = service.classifyData(context, content);
        
        // Second call should return cached result
        DataClassification result2 = service.classifyData(context, content);
        
        assertSame(result1, result2);
    }
    
    @Test
    void testNullInputHandling() {
        ProjectContext context = createTestProjectContext("test-project");
        
        assertThrows(NullPointerException.class, () -> 
                service.classifyData(null, "content"));
        
        assertThrows(NullPointerException.class, () -> 
                service.classifyData(context, null));
        
        assertThrows(NullPointerException.class, () -> 
                service.classifyProject(null, List.of()));
        
        assertThrows(NullPointerException.class, () -> 
                service.classifyProject("project", null));
        
        assertThrows(NullPointerException.class, () -> 
                service.updateClassification(null, DataClassification.SensitivityLevel.INTERNAL, "reason"));
        
        assertThrows(NullPointerException.class, () -> 
                service.updateClassification("project", null, "reason"));
        
        assertThrows(NullPointerException.class, () -> 
                service.getClassification(null));
        
        assertThrows(NullPointerException.class, () -> 
                service.suggestSensitivityLevel(null));
        
        assertThrows(NullPointerException.class, () -> 
                service.containsSensitivePatterns(null));
    }
    
    private ProjectContext createTestProjectContext(String projectId) {
        ProjectStructure structure = ProjectStructure.builder()
                .files(List.of(
                        FileNode.builder()
                                .name("Main.java")
                                .path("/src/Main.java")
                                .size(1000L)
                                .build()
                ))
                .modules(List.of())
                .relationships(List.of())
                .entryPoints(List.of("Main.java"))
                .build();
        
        return ProjectContext.builder()
                .id(projectId)
                .structure(structure)
                .dependencies(List.of())
                .patterns(List.of())
                .conventions(List.of())
                .build();
    }
}