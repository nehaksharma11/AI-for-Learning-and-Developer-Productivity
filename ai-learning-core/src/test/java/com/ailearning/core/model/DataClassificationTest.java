package com.ailearning.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DataClassificationTest {
    
    @Test
    void testBuilderCreatesValidDataClassification() {
        LocalDateTime now = LocalDateTime.now();
        DataClassification classification = DataClassification.builder()
                .projectId("test-project")
                .sensitivityLevel(DataClassification.SensitivityLevel.CONFIDENTIAL)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .reason("Test classification")
                .classifiedAt(now)
                .userOverride(true)
                .classificationSource("test")
                .build();
        
        assertEquals("test-project", classification.getProjectId());
        assertEquals(DataClassification.SensitivityLevel.CONFIDENTIAL, classification.getSensitivityLevel());
        assertEquals(Set.of(DataClassification.DataType.SOURCE_CODE), classification.getDataTypes());
        assertEquals("Test classification", classification.getReason());
        assertEquals(now, classification.getClassifiedAt());
        assertTrue(classification.isUserOverride());
        assertEquals("test", classification.getClassificationSource());
    }
    
    @Test
    void testBuilderRequiresNonNullFields() {
        assertThrows(NullPointerException.class, () -> 
            DataClassification.builder().build());
        
        assertThrows(NullPointerException.class, () -> 
            DataClassification.builder()
                .projectId("test")
                .build());
        
        assertThrows(NullPointerException.class, () -> 
            DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.PUBLIC)
                .build());
    }
    
    @Test
    void testAllowsCloudProcessing() {
        DataClassification publicData = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.PUBLIC)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
        
        assertTrue(publicData.allowsCloudProcessing());
        
        DataClassification internalData = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.INTERNAL)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
        
        assertTrue(internalData.allowsCloudProcessing());
        
        DataClassification confidentialData = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.CONFIDENTIAL)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
        
        assertFalse(confidentialData.allowsCloudProcessing());
    }
    
    @Test
    void testRequiresLocalProcessing() {
        DataClassification confidentialData = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.CONFIDENTIAL)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
        
        assertTrue(confidentialData.requiresLocalProcessing());
        
        DataClassification restrictedData = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.RESTRICTED)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
        
        assertTrue(restrictedData.requiresLocalProcessing());
        
        DataClassification publicData = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.PUBLIC)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
        
        assertFalse(publicData.requiresLocalProcessing());
    }
    
    @Test
    void testUserOverrideAffectsProcessingDecisions() {
        DataClassification internalWithOverride = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.INTERNAL)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .userOverride(true)
                .classificationSource("test")
                .build();
        
        assertTrue(internalWithOverride.requiresLocalProcessing());
        assertFalse(internalWithOverride.allowsCloudProcessing());
    }
    
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        DataClassification classification1 = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.INTERNAL)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(now)
                .classificationSource("test")
                .build();
        
        DataClassification classification2 = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.INTERNAL)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(now)
                .classificationSource("test")
                .build();
        
        assertEquals(classification1, classification2);
        assertEquals(classification1.hashCode(), classification2.hashCode());
    }
    
    @Test
    void testToString() {
        DataClassification classification = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.INTERNAL)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
        
        String toString = classification.toString();
        assertTrue(toString.contains("test"));
        assertTrue(toString.contains("INTERNAL"));
        assertTrue(toString.contains("SOURCE_CODE"));
    }
    
    @Test
    void testDataTypesAreImmutable() {
        Set<DataClassification.DataType> originalTypes = Set.of(DataClassification.DataType.SOURCE_CODE);
        DataClassification classification = DataClassification.builder()
                .projectId("test")
                .sensitivityLevel(DataClassification.SensitivityLevel.INTERNAL)
                .dataTypes(originalTypes)
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
        
        Set<DataClassification.DataType> retrievedTypes = classification.getDataTypes();
        assertThrows(UnsupportedOperationException.class, () -> 
            retrievedTypes.add(DataClassification.DataType.COMMENTS));
    }
}