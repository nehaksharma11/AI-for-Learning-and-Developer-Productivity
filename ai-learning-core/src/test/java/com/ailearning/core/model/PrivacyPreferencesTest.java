package com.ailearning.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PrivacyPreferencesTest {
    
    @Test
    void testBuilderCreatesValidPrivacyPreferences() {
        LocalDateTime now = LocalDateTime.now();
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(true)
                .allowDataCollection(false)
                .allowTelemetry(true)
                .optedOutProjects(Set.of("project1"))
                .optedOutDataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .optedOutServices(Set.of("service1"))
                .optOutScope(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC)
                .lastUpdated(now)
                .requireExplicitConsent(true)
                .dataRetentionDays(60)
                .allowAnonymizedAnalytics(false)
                .build();
        
        assertEquals("user-123", preferences.getUserId());
        assertTrue(preferences.allowsCloudProcessing());
        assertFalse(preferences.allowsDataCollection());
        assertTrue(preferences.allowsTelemetry());
        assertEquals(Set.of("project1"), preferences.getOptedOutProjects());
        assertEquals(Set.of(DataClassification.DataType.SOURCE_CODE), preferences.getOptedOutDataTypes());
        assertEquals(Set.of("service1"), preferences.getOptedOutServices());
        assertEquals(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC, preferences.getOptOutScope());
        assertEquals(now, preferences.getLastUpdated());
        assertTrue(preferences.requiresExplicitConsent());
        assertEquals(60, preferences.getDataRetentionDays());
        assertFalse(preferences.allowsAnonymizedAnalytics());
    }
    
    @Test
    void testBuilderRequiresNonNullFields() {
        assertThrows(NullPointerException.class, () -> 
            PrivacyPreferences.builder().build());
        
        assertThrows(NullPointerException.class, () -> 
            PrivacyPreferences.builder()
                .userId("user-123")
                .optedOutProjects(null)
                .build());
    }
    
    @Test
    void testBuilderUsesDefaults() {
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .build();
        
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
    }
    
    @Test
    void testIsProjectOptedOut() {
        PrivacyPreferences projectSpecific = PrivacyPreferences.builder()
                .userId("user-123")
                .optOutScope(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC)
                .optedOutProjects(Set.of("project1", "project2"))
                .build();
        
        assertTrue(projectSpecific.isProjectOptedOut("project1"));
        assertTrue(projectSpecific.isProjectOptedOut("project2"));
        assertFalse(projectSpecific.isProjectOptedOut("project3"));
        
        PrivacyPreferences globalOptOut = PrivacyPreferences.builder()
                .userId("user-123")
                .optOutScope(PrivacyPreferences.OptOutScope.GLOBAL)
                .build();
        
        assertTrue(globalOptOut.isProjectOptedOut("any-project"));
    }
    
    @Test
    void testIsDataTypeOptedOut() {
        PrivacyPreferences dataTypeSpecific = PrivacyPreferences.builder()
                .userId("user-123")
                .optOutScope(PrivacyPreferences.OptOutScope.DATA_TYPE)
                .optedOutDataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .build();
        
        assertTrue(dataTypeSpecific.isDataTypeOptedOut(DataClassification.DataType.SOURCE_CODE));
        assertFalse(dataTypeSpecific.isDataTypeOptedOut(DataClassification.DataType.COMMENTS));
        
        PrivacyPreferences globalOptOut = PrivacyPreferences.builder()
                .userId("user-123")
                .optOutScope(PrivacyPreferences.OptOutScope.GLOBAL)
                .build();
        
        assertTrue(globalOptOut.isDataTypeOptedOut(DataClassification.DataType.SOURCE_CODE));
    }
    
    @Test
    void testIsServiceOptedOut() {
        PrivacyPreferences serviceSpecific = PrivacyPreferences.builder()
                .userId("user-123")
                .optOutScope(PrivacyPreferences.OptOutScope.SERVICE_SPECIFIC)
                .optedOutServices(Set.of("openai-service"))
                .build();
        
        assertTrue(serviceSpecific.isServiceOptedOut("openai-service"));
        assertFalse(serviceSpecific.isServiceOptedOut("local-service"));
        
        PrivacyPreferences globalOptOut = PrivacyPreferences.builder()
                .userId("user-123")
                .optOutScope(PrivacyPreferences.OptOutScope.GLOBAL)
                .build();
        
        assertTrue(globalOptOut.isServiceOptedOut("any-service"));
    }
    
    @Test
    void testAllowsCloudProcessingFor() {
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(true)
                .optOutScope(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC)
                .optedOutProjects(Set.of("sensitive-project"))
                .optedOutDataTypes(Set.of(DataClassification.DataType.CONFIGURATION))
                .optedOutServices(Set.of("untrusted-service"))
                .build();
        
        // Should allow for normal case
        assertTrue(preferences.allowsCloudProcessingFor(
                "normal-project", 
                DataClassification.DataType.SOURCE_CODE, 
                "trusted-service"));
        
        // Should not allow for opted-out project
        assertFalse(preferences.allowsCloudProcessingFor(
                "sensitive-project", 
                DataClassification.DataType.SOURCE_CODE, 
                "trusted-service"));
        
        // Should not allow for opted-out data type
        assertFalse(preferences.allowsCloudProcessingFor(
                "normal-project", 
                DataClassification.DataType.CONFIGURATION, 
                "trusted-service"));
        
        // Should not allow for opted-out service
        assertFalse(preferences.allowsCloudProcessingFor(
                "normal-project", 
                DataClassification.DataType.SOURCE_CODE, 
                "untrusted-service"));
    }
    
    @Test
    void testCloudProcessingDisabledGlobally() {
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(false)
                .build();
        
        assertFalse(preferences.allowsCloudProcessingFor(
                "any-project", 
                DataClassification.DataType.SOURCE_CODE, 
                "any-service"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        PrivacyPreferences preferences1 = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(true)
                .lastUpdated(now)
                .build();
        
        PrivacyPreferences preferences2 = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(true)
                .lastUpdated(now)
                .build();
        
        assertEquals(preferences1, preferences2);
        assertEquals(preferences1.hashCode(), preferences2.hashCode());
    }
    
    @Test
    void testToString() {
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .allowCloudProcessing(false)
                .optOutScope(PrivacyPreferences.OptOutScope.GLOBAL)
                .build();
        
        String toString = preferences.toString();
        assertTrue(toString.contains("user-123"));
        assertTrue(toString.contains("allowCloudProcessing=false"));
        assertTrue(toString.contains("GLOBAL"));
    }
    
    @Test
    void testCollectionsAreImmutable() {
        PrivacyPreferences preferences = PrivacyPreferences.builder()
                .userId("user-123")
                .optedOutProjects(Set.of("project1"))
                .optedOutDataTypes(Set.of(DataClassification.DataType.SOURCE_CODE))
                .optedOutServices(Set.of("service1"))
                .build();
        
        assertThrows(UnsupportedOperationException.class, () -> 
            preferences.getOptedOutProjects().add("project2"));
        
        assertThrows(UnsupportedOperationException.class, () -> 
            preferences.getOptedOutDataTypes().add(DataClassification.DataType.COMMENTS));
        
        assertThrows(UnsupportedOperationException.class, () -> 
            preferences.getOptedOutServices().add("service2"));
    }
}