package com.ailearning.core.service.impl;

import com.ailearning.core.model.DataClassification;
import com.ailearning.core.model.ProcessingDecision;
import com.ailearning.core.model.PrivacyPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultProcessingDecisionEngineTest {
    
    private DefaultProcessingDecisionEngine engine;
    
    @BeforeEach
    void setUp() {
        engine = new DefaultProcessingDecisionEngine();
    }
    
    private DataClassification createTestClassification(DataClassification.SensitivityLevel level) {
        return DataClassification.builder()
                .projectId("test-project")
                .sensitivityLevel(level)
                .dataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .classifiedAt(LocalDateTime.now())
                .classificationSource("test")
                .build();
    }
    
    private PrivacyPreferences createTestPreferences() {
        return PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(true)
                .allowDataCollection(true)
                .allowTelemetry(true)
                .build();
    }
    
    @Test
    void testMakeDecisionForPublicData() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.PUBLIC);
        PrivacyPreferences preferences = createTestPreferences();
        
        ProcessingDecision decision = engine.makeDecision(
                "req-123",
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        );
        
        assertEquals("req-123", decision.getRequestId());
        assertEquals("test-project", decision.getProjectId());
        assertEquals(ProcessingDecision.ProcessingType.CODE_ANALYSIS, decision.getType());
        assertEquals(classification, decision.getDataClassification());
        assertNotNull(decision.getReasoning());
        assertFalse(decision.requiresUserConsent());
    }
    
    @Test
    void testMakeDecisionForRestrictedData() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.RESTRICTED);
        PrivacyPreferences preferences = createTestPreferences();
        
        ProcessingDecision decision = engine.makeDecision(
                "req-123",
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        );
        
        assertEquals(ProcessingDecision.ProcessingLocation.LOCAL_ONLY, decision.getLocation());
        assertTrue(decision.requiresUserConsent());
        assertTrue(decision.getReasoning().contains("RESTRICTED"));
    }
    
    @Test
    void testMakeDecisionWithUserOptOut() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.INTERNAL);
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(false)
                .build();
        
        ProcessingDecision decision = engine.makeDecision(
                "req-123",
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        );
        
        assertEquals(ProcessingDecision.ProcessingLocation.LOCAL_ONLY, decision.getLocation());
        assertTrue(decision.getReasoning().contains("User preferences require local processing"));
    }
    
    @Test
    void testIsCloudProcessingAllowed() {
        DataClassification publicClassification = createTestClassification(DataClassification.SensitivityLevel.PUBLIC);
        DataClassification restrictedClassification = createTestClassification(DataClassification.SensitivityLevel.RESTRICTED);
        PrivacyPreferences allowingPreferences = createTestPreferences();
        PrivacyPreferences restrictivePreferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(false)
                .build();
        
        // Public data with allowing preferences
        assertTrue(engine.isCloudProcessingAllowed(publicClassification, allowingPreferences, "test-service"));
        
        // Public data with restrictive preferences
        assertFalse(engine.isCloudProcessingAllowed(publicClassification, restrictivePreferences, "test-service"));
        
        // Restricted data with allowing preferences
        assertFalse(engine.isCloudProcessingAllowed(restrictedClassification, allowingPreferences, "test-service"));
        
        // Restricted data with restrictive preferences
        assertFalse(engine.isCloudProcessingAllowed(restrictedClassification, restrictivePreferences, "test-service"));
    }
    
    @Test
    void testIsCloudProcessingAllowedWithOptedOutService() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.PUBLIC);
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(true)
                .optedOutServices(Set.of("blocked-service"))
                .build();
        
        assertTrue(engine.isCloudProcessingAllowed(classification, preferences, "allowed-service"));
        assertFalse(engine.isCloudProcessingAllowed(classification, preferences, "blocked-service"));
    }
    
    @Test
    void testIsCloudProcessingAllowedWithOptedOutProject() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.PUBLIC);
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(true)
                .optedOutProjects(Set.of("test-project"))
                .build();
        
        assertFalse(engine.isCloudProcessingAllowed(classification, preferences, "test-service"));
    }
    
    @Test
    void testDetermineProcessingLocationForExplanationGeneration() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.INTERNAL);
        PrivacyPreferences preferences = createTestPreferences();
        
        ProcessingDecision.ProcessingLocation location = engine.determineProcessingLocation(
                ProcessingDecision.ProcessingType.EXPLANATION_GENERATION,
                classification,
                preferences
        );
        
        assertEquals(ProcessingDecision.ProcessingLocation.CLOUD_PREFERRED, location);
    }
    
    @Test
    void testDetermineProcessingLocationForConfidentialData() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.CONFIDENTIAL);
        PrivacyPreferences preferences = createTestPreferences();
        
        ProcessingDecision.ProcessingLocation location = engine.determineProcessingLocation(
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        );
        
        assertEquals(ProcessingDecision.ProcessingLocation.LOCAL_ONLY, location);
    }
    
    @Test
    void testRequiresUserConsentForExplicitConsentPreference() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.INTERNAL);
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .requireExplicitConsent(true)
                .build();
        
        assertTrue(engine.requiresUserConsent(
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        ));
    }
    
    @Test
    void testRequiresUserConsentForRestrictedData() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.RESTRICTED);
        PrivacyPreferences preferences = createTestPreferences();
        
        assertTrue(engine.requiresUserConsent(
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        ));
    }
    
    @Test
    void testRequiresUserConsentForLearningRecommendations() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.INTERNAL);
        PrivacyPreferences preferences = createTestPreferences();
        
        assertTrue(engine.requiresUserConsent(
                ProcessingDecision.ProcessingType.LEARNING_RECOMMENDATION,
                classification,
                preferences
        ));
    }
    
    @Test
    void testGetAllowedServices() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.PUBLIC);
        PrivacyPreferences preferences = createTestPreferences();
        
        Set<String> allowedServices = engine.getAllowedServices(
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        );
        
        assertTrue(allowedServices.contains("local-analyzer"));
        assertTrue(allowedServices.contains("openai-service"));
        assertTrue(allowedServices.contains("huggingface-service"));
    }
    
    @Test
    void testGetAllowedServicesWithOptedOutService() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.PUBLIC);
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(true)
                .optedOutServices(Set.of("openai-service"))
                .build();
        
        Set<String> allowedServices = engine.getAllowedServices(
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        );
        
        assertTrue(allowedServices.contains("local-analyzer"));
        assertFalse(allowedServices.contains("openai-service"));
        assertTrue(allowedServices.contains("huggingface-service"));
    }
    
    @Test
    void testGetBlockedServices() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.RESTRICTED);
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(false)
                .optedOutServices(Set.of("custom-service"))
                .build();
        
        Set<String> blockedServices = engine.getBlockedServices(
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        );
        
        assertTrue(blockedServices.contains("openai-service"));
        assertTrue(blockedServices.contains("huggingface-service"));
        assertTrue(blockedServices.contains("custom-service"));
    }
    
    @Test
    void testNullInputHandling() {
        DataClassification classification = createTestClassification(DataClassification.SensitivityLevel.INTERNAL);
        PrivacyPreferences preferences = createTestPreferences();
        
        assertThrows(NullPointerException.class, () -> 
                engine.makeDecision(null, ProcessingDecision.ProcessingType.CODE_ANALYSIS, classification, preferences));
        
        assertThrows(NullPointerException.class, () -> 
                engine.makeDecision("req-123", null, classification, preferences));
        
        assertThrows(NullPointerException.class, () -> 
                engine.makeDecision("req-123", ProcessingDecision.ProcessingType.CODE_ANALYSIS, null, preferences));
        
        assertThrows(NullPointerException.class, () -> 
                engine.makeDecision("req-123", ProcessingDecision.ProcessingType.CODE_ANALYSIS, classification, null));
        
        assertThrows(NullPointerException.class, () -> 
                engine.isCloudProcessingAllowed(null, preferences, "service"));
        
        assertThrows(NullPointerException.class, () -> 
                engine.isCloudProcessingAllowed(classification, null, "service"));
        
        assertThrows(NullPointerException.class, () -> 
                engine.isCloudProcessingAllowed(classification, preferences, null));
    }
}