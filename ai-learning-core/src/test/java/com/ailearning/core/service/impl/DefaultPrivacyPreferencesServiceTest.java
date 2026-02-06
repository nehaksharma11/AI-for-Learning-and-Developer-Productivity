package com.ailearning.core.service.impl;

import com.ailearning.core.model.DataClassification;
import com.ailearning.core.model.PrivacyPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPrivacyPreferencesServiceTest {
    
    private DefaultPrivacyPreferencesService service;
    
    @BeforeEach
    void setUp() {
        service = new DefaultPrivacyPreferencesService();
    }
    
    @Test
    void testGetPreferencesForNonExistentUser() {
        Optional<PrivacyPreferences> result = service.getPreferences("nonexistent-user");
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testCreateDefaultPreferences() {
        PrivacyPreferences preferences = service.createDefaultPreferences("user-123");
        
        assertEquals("user-123", preferences.getUserId());
        assertTrue(preferences.allowsCloudProcessing());
        assertTrue(preferences.allowsDataCollection());
        assertTrue(preferences.allowsTelemetry());
        assertTrue(preferences.getOptedOutProjects().isEmpty());
        assertTrue(preferences.getOptedOutDataTypes().isEmpty());
        assertTrue(preferences.getOptedOutServices().isEmpty());
        assertEquals(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC, preferences.getOptOutScope());
        assertFalse(preferences.requiresExplicitConsent());
        assertEquals(30, preferences.getDataRetentionDays());
        assertTrue(preferences.allowsAnonymizedAnalytics());
        
        // Should be stored and retrievable
        Optional<PrivacyPreferences> retrieved = service.getPreferences("user-123");
        assertTrue(retrieved.isPresent());
        assertEquals(preferences, retrieved.get());
    }
    
    @Test
    void testUpdatePreferences() {
        PrivacyPreferences original = service.createDefaultPreferences("user-123");
        
        PrivacyPreferences updated = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(false)
                .allowDataCollection(false)
                .allowTelemetry(false)
                .requireExplicitConsent(true)
                .dataRetentionDays(60)
                .allowAnonymizedAnalytics(false)
                .build();
        
        PrivacyPreferences result = service.updatePreferences(updated);
        
        assertEquals("user-123", result.getUserId());
        assertFalse(result.allowsCloudProcessing());
        assertFalse(result.allowsDataCollection());
        assertFalse(result.allowsTelemetry());
        assertTrue(result.requiresExplicitConsent());
        assertEquals(60, result.getDataRetentionDays());
        assertFalse(result.allowsAnonymizedAnalytics());
        assertTrue(result.getLastUpdated().isAfter(original.getLastUpdated()));
    }
    
    @Test
    void testOptOutProject() {
        service.createDefaultPreferences("user-123");
        
        PrivacyPreferences result = service.optOutProject("user-123", "sensitive-project");
        
        assertEquals("user-123", result.getUserId());
        assertTrue(result.getOptedOutProjects().contains("sensitive-project"));
        assertEquals(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC, result.getOptOutScope());
        
        // Opt out another project
        result = service.optOutProject("user-123", "another-project");
        assertTrue(result.getOptedOutProjects().contains("sensitive-project"));
        assertTrue(result.getOptedOutProjects().contains("another-project"));
    }
    
    @Test
    void testOptOutProjectForNewUser() {
        // Should create default preferences if user doesn't exist
        PrivacyPreferences result = service.optOutProject("new-user", "project1");
        
        assertEquals("new-user", result.getUserId());
        assertTrue(result.getOptedOutProjects().contains("project1"));
    }
    
    @Test
    void testOptOutDataTypes() {
        service.createDefaultPreferences("user-123");
        
        Set<DataClassification.DataType> dataTypes = Set.of(
                DataClassification.DataType.SOURCE_CODE,
                DataClassification.DataType.CONFIGURATION
        );
        
        PrivacyPreferences result = service.optOutDataTypes("user-123", dataTypes);
        
        assertEquals("user-123", result.getUserId());
        assertTrue(result.getOptedOutDataTypes().contains(DataClassification.DataType.SOURCE_CODE));
        assertTrue(result.getOptedOutDataTypes().contains(DataClassification.DataType.CONFIGURATION));
        assertEquals(PrivacyPreferences.OptOutScope.DATA_TYPE, result.getOptOutScope());
    }
    
    @Test
    void testOptOutServices() {
        service.createDefaultPreferences("user-123");
        
        Set<String> services = Set.of("openai-service", "cloud-analyzer");
        
        PrivacyPreferences result = service.optOutServices("user-123", services);
        
        assertEquals("user-123", result.getUserId());
        assertTrue(result.getOptedOutServices().contains("openai-service"));
        assertTrue(result.getOptedOutServices().contains("cloud-analyzer"));
        assertEquals(PrivacyPreferences.OptOutScope.SERVICE_SPECIFIC, result.getOptOutScope());
    }
    
    @Test
    void testOptInProject() {
        service.createDefaultPreferences("user-123");
        service.optOutProject("user-123", "project1");
        service.optOutProject("user-123", "project2");
        
        PrivacyPreferences result = service.optInProject("user-123", "project1");
        
        assertFalse(result.getOptedOutProjects().contains("project1"));
        assertTrue(result.getOptedOutProjects().contains("project2"));
    }
    
    @Test
    void testIsOptedOutWithNoPreferences() {
        // Should default to allowing processing if no preferences set
        assertFalse(service.isOptedOut("nonexistent-user", "project", 
                DataClassification.DataType.SOURCE_CODE, "service"));
    }
    
    @Test
    void testIsOptedOutWithGlobalOptOut() {
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .optOutScope(PrivacyPreferences.OptOutScope.GLOBAL)
                .build();
        
        service.updatePreferences(preferences);
        
        assertTrue(service.isOptedOut("user-123", "any-project", 
                DataClassification.DataType.SOURCE_CODE, "any-service"));
    }
    
    @Test
    void testIsOptedOutWithProjectSpecificOptOut() {
        service.createDefaultPreferences("user-123");
        service.optOutProject("user-123", "sensitive-project");
        
        assertTrue(service.isOptedOut("user-123", "sensitive-project", 
                DataClassification.DataType.SOURCE_CODE, "service"));
        
        assertFalse(service.isOptedOut("user-123", "normal-project", 
                DataClassification.DataType.SOURCE_CODE, "service"));
    }
    
    @Test
    void testIsOptedOutWithDataTypeOptOut() {
        service.createDefaultPreferences("user-123");
        service.optOutDataTypes("user-123", Set.of(DataClassification.DataType.CONFIGURATION));
        
        assertTrue(service.isOptedOut("user-123", "project", 
                DataClassification.DataType.CONFIGURATION, "service"));
        
        assertFalse(service.isOptedOut("user-123", "project", 
                DataClassification.DataType.SOURCE_CODE, "service"));
    }
    
    @Test
    void testIsOptedOutWithServiceOptOut() {
        service.createDefaultPreferences("user-123");
        service.optOutServices("user-123", Set.of("blocked-service"));
        
        assertTrue(service.isOptedOut("user-123", "project", 
                DataClassification.DataType.SOURCE_CODE, "blocked-service"));
        
        assertFalse(service.isOptedOut("user-123", "project", 
                DataClassification.DataType.SOURCE_CODE, "allowed-service"));
    }
    
    @Test
    void testSetGlobalOptOut() {
        service.createDefaultPreferences("user-123");
        
        PrivacyPreferences result = service.setGlobalOptOut("user-123");
        
        assertEquals("user-123", result.getUserId());
        assertFalse(result.allowsCloudProcessing());
        assertFalse(result.allowsDataCollection());
        assertFalse(result.allowsTelemetry());
        assertEquals(PrivacyPreferences.OptOutScope.GLOBAL, result.getOptOutScope());
        assertTrue(result.requiresExplicitConsent());
        assertFalse(result.allowsAnonymizedAnalytics());
    }
    
    @Test
    void testRemoveGlobalOptOut() {
        service.createDefaultPreferences("user-123");
        service.setGlobalOptOut("user-123");
        
        PrivacyPreferences result = service.removeGlobalOptOut("user-123");
        
        assertEquals("user-123", result.getUserId());
        assertTrue(result.allowsCloudProcessing());
        assertTrue(result.allowsDataCollection());
        assertTrue(result.allowsTelemetry());
        assertEquals(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC, result.getOptOutScope());
        assertFalse(result.requiresExplicitConsent());
        assertTrue(result.allowsAnonymizedAnalytics());
    }
    
    @Test
    void testValidatePreferences() {
        // Valid preferences
        PrivacyPreferences valid = PrivacyPreferences.builder()
                .userId("user-123")
                .dataRetentionDays(30)
                .build();
        
        assertTrue(service.validatePreferences(valid));
        
        // Invalid: null user ID
        PrivacyPreferences invalidUserId = PrivacyPreferences.builder()
                .userId(null)
                .build();
        
        assertFalse(service.validatePreferences(invalidUserId));
        
        // Invalid: empty user ID
        PrivacyPreferences emptyUserId = PrivacyPreferences.builder()
                .userId("")
                .build();
        
        assertFalse(service.validatePreferences(emptyUserId));
        
        // Invalid: negative retention days
        PrivacyPreferences negativeRetention = PrivacyPreferences.builder()
                .userId("user-123")
                .dataRetentionDays(-1)
                .build();
        
        assertFalse(service.validatePreferences(negativeRetention));
        
        // Invalid: too many retention days
        PrivacyPreferences tooManyRetentionDays = PrivacyPreferences.builder()
                .userId("user-123")
                .dataRetentionDays(400)
                .build();
        
        assertFalse(service.validatePreferences(tooManyRetentionDays));
        
        // Invalid: global opt-out but cloud processing enabled
        PrivacyPreferences inconsistent = PrivacyPreferences.builder()
                .userId("user-123")
                .optOutScope(PrivacyPreferences.OptOutScope.GLOBAL)
                .allowCloudProcessing(true)
                .build();
        
        assertFalse(service.validatePreferences(inconsistent));
    }
    
    @Test
    void testUpdatePreferencesWithInvalidData() {
        PrivacyPreferences invalid = PrivacyPreferences.builder()
                .userId("user-123")
                .dataRetentionDays(-1)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> 
                service.updatePreferences(invalid));
    }
    
    @Test
    void testNullInputHandling() {
        assertThrows(NullPointerException.class, () -> 
                service.getPreferences(null));
        
        assertThrows(NullPointerException.class, () -> 
                service.updatePreferences(null));
        
        assertThrows(NullPointerException.class, () -> 
                service.createDefaultPreferences(null));
        
        assertThrows(NullPointerException.class, () -> 
                service.optOutProject(null, "project"));
        
        assertThrows(NullPointerException.class, () -> 
                service.optOutProject("user", null));
        
        assertThrows(NullPointerException.class, () -> 
                service.optOutDataTypes("user", null));
        
        assertThrows(NullPointerException.class, () -> 
                service.optOutServices("user", null));
        
        assertThrows(NullPointerException.class, () -> 
                service.validatePreferences(null));
    }
}