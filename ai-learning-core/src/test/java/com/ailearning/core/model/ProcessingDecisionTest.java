package com.ailearning.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingDecisionTest {
    
    private DataClassification createTestClassification() {
        return DataClassification.builder()
                .projectId("test-project")
                .sensitivityLevel(DataClassification.SensitivityLevel.INTERNAL)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
    }
    
    @Test
    void testBuilderCreatesValidProcessingDecision() {
        DataClassification classification = createTestClassification();
        LocalDateTime now = LocalDateTime.now();
        
        ProcessingDecision decision = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.LOCAL_ONLY)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .reasoning("Test reasoning")
                .decidedAt(now)
                .allowedServices(Set.of("local-service"))
                .blockedServices(Set.of("cloud-service"))
                .requiresUserConsent(true)
                .build();
        
        assertEquals("req-123", decision.getRequestId());
        assertEquals("test-project", decision.getProjectId());
        assertEquals(ProcessingDecision.ProcessingLocation.LOCAL_ONLY, decision.getLocation());
        assertEquals(ProcessingDecision.ProcessingType.CODE_ANALYSIS, decision.getType());
        assertEquals(classification, decision.getDataClassification());
        assertEquals("Test reasoning", decision.getReasoning());
        assertEquals(now, decision.getDecidedAt());
        assertEquals(Set.of("local-service"), decision.getAllowedServices());
        assertEquals(Set.of("cloud-service"), decision.getBlockedServices());
        assertTrue(decision.requiresUserConsent());
    }
    
    @Test
    void testBuilderRequiresNonNullFields() {
        assertThrows(NullPointerException.class, () -> 
            ProcessingDecision.builder().build());
        
        assertThrows(NullPointerException.class, () -> 
            ProcessingDecision.builder()
                .requestId("req-123")
                .build());
    }
    
    @Test
    void testAllowsCloudProcessing() {
        DataClassification classification = createTestClassification();
        
        ProcessingDecision cloudPreferred = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.CLOUD_PREFERRED)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(LocalDateTime.now())
                .allowedServices(Set.of())
                .blockedServices(Set.of())
                .build();
        
        assertTrue(cloudPreferred.allowsCloudProcessing());
        
        ProcessingDecision localOnly = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.LOCAL_ONLY)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(LocalDateTime.now())
                .allowedServices(Set.of())
                .blockedServices(Set.of())
                .build();
        
        assertFalse(localOnly.allowsCloudProcessing());
    }
    
    @Test
    void testRequiresLocalProcessing() {
        DataClassification classification = createTestClassification();
        
        ProcessingDecision localOnly = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.LOCAL_ONLY)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(LocalDateTime.now())
                .allowedServices(Set.of())
                .blockedServices(Set.of())
                .build();
        
        assertTrue(localOnly.requiresLocalProcessing());
        
        ProcessingDecision hybrid = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.HYBRID)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(LocalDateTime.now())
                .allowedServices(Set.of())
                .blockedServices(Set.of())
                .build();
        
        assertTrue(hybrid.requiresLocalProcessing());
        
        ProcessingDecision cloudRequired = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.CLOUD_REQUIRED)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(LocalDateTime.now())
                .allowedServices(Set.of())
                .blockedServices(Set.of())
                .build();
        
        assertFalse(cloudRequired.requiresLocalProcessing());
    }
    
    @Test
    void testIsServiceAllowed() {
        DataClassification classification = createTestClassification();
        
        ProcessingDecision decision = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.HYBRID)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(LocalDateTime.now())
                .allowedServices(Set.of("service1", "service2"))
                .blockedServices(Set.of("service3"))
                .build();
        
        assertTrue(decision.isServiceAllowed("service1"));
        assertTrue(decision.isServiceAllowed("service2"));
        assertFalse(decision.isServiceAllowed("service3"));
        assertFalse(decision.isServiceAllowed("service4"));
    }
    
    @Test
    void testServiceBlockedTakesPrecedenceOverAllowed() {
        DataClassification classification = createTestClassification();
        
        ProcessingDecision decision = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.HYBRID)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(LocalDateTime.now())
                .allowedServices(Set.of("service1"))
                .blockedServices(Set.of("service1"))
                .build();
        
        assertFalse(decision.isServiceAllowed("service1"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        DataClassification classification = createTestClassification();
        LocalDateTime now = LocalDateTime.now();
        
        ProcessingDecision decision1 = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.LOCAL_ONLY)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(now)
                .allowedServices(Set.of())
                .blockedServices(Set.of())
                .build();
        
        ProcessingDecision decision2 = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.LOCAL_ONLY)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(now)
                .allowedServices(Set.of())
                .blockedServices(Set.of())
                .build();
        
        assertEquals(decision1, decision2);
        assertEquals(decision1.hashCode(), decision2.hashCode());
    }
    
    @Test
    void testToString() {
        DataClassification classification = createTestClassification();
        
        ProcessingDecision decision = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.LOCAL_ONLY)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(LocalDateTime.now())
                .allowedServices(Set.of())
                .blockedServices(Set.of())
                .build();
        
        String toString = decision.toString();
        assertTrue(toString.contains("req-123"));
        assertTrue(toString.contains("test-project"));
        assertTrue(toString.contains("LOCAL_ONLY"));
        assertTrue(toString.contains("CODE_ANALYSIS"));
    }
    
    @Test
    void testServicesAreImmutable() {
        DataClassification classification = createTestClassification();
        
        ProcessingDecision decision = ProcessingDecision.builder()
                .requestId("req-123")
                .projectId("test-project")
                .location(ProcessingDecision.ProcessingLocation.LOCAL_ONLY)
                .type(ProcessingDecision.ProcessingType.CODE_ANALYSIS)
                .dataClassification(classification)
                .decidedAt(LocalDateTime.now())
                .allowedServices(Set.of("service1"))
                .blockedServices(Set.of("service2"))
                .build();
        
        assertThrows(UnsupportedOperationException.class, () -> 
            decision.getAllowedServices().add("service3"));
        
        assertThrows(UnsupportedOperationException.class, () -> 
            decision.getBlockedServices().add("service4"));
    }
}