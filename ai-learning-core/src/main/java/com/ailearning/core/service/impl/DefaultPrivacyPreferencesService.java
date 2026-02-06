package com.ailearning.core.service.impl;

import com.ailearning.core.model.DataClassification;
import com.ailearning.core.model.PrivacyPreferences;
import com.ailearning.core.service.PrivacyPreferencesService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of PrivacyPreferencesService that manages user privacy preferences
 * and opt-out settings with in-memory storage.
 */
public class DefaultPrivacyPreferencesService implements PrivacyPreferencesService {
    
    private final Map<String, PrivacyPreferences> preferencesStore = new ConcurrentHashMap<>();
    
    @Override
    public Optional<PrivacyPreferences> getPreferences(String userId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        return Optional.ofNullable(preferencesStore.get(userId));
    }
    
    @Override
    public PrivacyPreferences updatePreferences(PrivacyPreferences preferences) {
        Objects.requireNonNull(preferences, "Preferences cannot be null");
        
        if (!validatePreferences(preferences)) {
            throw new IllegalArgumentException("Invalid privacy preferences");
        }
        
        // Update the last modified timestamp
        PrivacyPreferences updated = PrivacyPreferences.builder()
                .userId(preferences.getUserId())
                .allowCloudProcessing(preferences.allowsCloudProcessing())
                .allowDataCollection(preferences.allowsDataCollection())
                .allowTelemetry(preferences.allowsTelemetry())
                .optedOutProjects(preferences.getOptedOutProjects())
                .optedOutDataTypes(preferences.getOptedOutDataTypes())
                .optedOutServices(preferences.getOptedOutServices())
                .optOutScope(preferences.getOptOutScope())
                .lastUpdated(LocalDateTime.now())
                .requireExplicitConsent(preferences.requiresExplicitConsent())
                .dataRetentionDays(preferences.getDataRetentionDays())
                .allowAnonymizedAnalytics(preferences.allowsAnonymizedAnalytics())
                .build();
        
        preferencesStore.put(preferences.getUserId(), updated);
        return updated;
    }
    
    @Override
    public PrivacyPreferences createDefaultPreferences(String userId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        
        PrivacyPreferences defaultPreferences = PrivacyPreferences.builder()
                .userId(userId)
                .allowCloudProcessing(true)
                .allowDataCollection(true)
                .allowTelemetry(true)
                .optedOutProjects(Set.of())
                .optedOutDataTypes(Set.of())
                .optedOutServices(Set.of())
                .optOutScope(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC)
                .lastUpdated(LocalDateTime.now())
                .requireExplicitConsent(false)
                .dataRetentionDays(30)
                .allowAnonymizedAnalytics(true)
                .build();
        
        preferencesStore.put(userId, defaultPreferences);
        return defaultPreferences;
    }
    
    @Override
    public PrivacyPreferences optOutProject(String userId, String projectId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(projectId, "Project ID cannot be null");
        
        PrivacyPreferences current = getPreferences(userId)
                .orElse(createDefaultPreferences(userId));
        
        Set<String> updatedOptedOutProjects = new HashSet<>(current.getOptedOutProjects());
        updatedOptedOutProjects.add(projectId);
        
        PrivacyPreferences updated = PrivacyPreferences.builder()
                .userId(current.getUserId())
                .allowCloudProcessing(current.allowsCloudProcessing())
                .allowDataCollection(current.allowsDataCollection())
                .allowTelemetry(current.allowsTelemetry())
                .optedOutProjects(updatedOptedOutProjects)
                .optedOutDataTypes(current.getOptedOutDataTypes())
                .optedOutServices(current.getOptedOutServices())
                .optOutScope(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC)
                .lastUpdated(LocalDateTime.now())
                .requireExplicitConsent(current.requiresExplicitConsent())
                .dataRetentionDays(current.getDataRetentionDays())
                .allowAnonymizedAnalytics(current.allowsAnonymizedAnalytics())
                .build();
        
        preferencesStore.put(userId, updated);
        return updated;
    }
    
    @Override
    public PrivacyPreferences optOutDataTypes(String userId, Set<DataClassification.DataType> dataTypes) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(dataTypes, "Data types cannot be null");
        
        PrivacyPreferences current = getPreferences(userId)
                .orElse(createDefaultPreferences(userId));
        
        Set<DataClassification.DataType> updatedOptedOutDataTypes = new HashSet<>(current.getOptedOutDataTypes());
        updatedOptedOutDataTypes.addAll(dataTypes);
        
        PrivacyPreferences updated = PrivacyPreferences.builder()
                .userId(current.getUserId())
                .allowCloudProcessing(current.allowsCloudProcessing())
                .allowDataCollection(current.allowsDataCollection())
                .allowTelemetry(current.allowsTelemetry())
                .optedOutProjects(current.getOptedOutProjects())
                .optedOutDataTypes(updatedOptedOutDataTypes)
                .optedOutServices(current.getOptedOutServices())
                .optOutScope(PrivacyPreferences.OptOutScope.DATA_TYPE)
                .lastUpdated(LocalDateTime.now())
                .requireExplicitConsent(current.requiresExplicitConsent())
                .dataRetentionDays(current.getDataRetentionDays())
                .allowAnonymizedAnalytics(current.allowsAnonymizedAnalytics())
                .build();
        
        preferencesStore.put(userId, updated);
        return updated;
    }
    
    @Override
    public PrivacyPreferences optOutServices(String userId, Set<String> services) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(services, "Services cannot be null");
        
        PrivacyPreferences current = getPreferences(userId)
                .orElse(createDefaultPreferences(userId));
        
        Set<String> updatedOptedOutServices = new HashSet<>(current.getOptedOutServices());
        updatedOptedOutServices.addAll(services);
        
        PrivacyPreferences updated = PrivacyPreferences.builder()
                .userId(current.getUserId())
                .allowCloudProcessing(current.allowsCloudProcessing())
                .allowDataCollection(current.allowsDataCollection())
                .allowTelemetry(current.allowsTelemetry())
                .optedOutProjects(current.getOptedOutProjects())
                .optedOutDataTypes(current.getOptedOutDataTypes())
                .optedOutServices(updatedOptedOutServices)
                .optOutScope(PrivacyPreferences.OptOutScope.SERVICE_SPECIFIC)
                .lastUpdated(LocalDateTime.now())
                .requireExplicitConsent(current.requiresExplicitConsent())
                .dataRetentionDays(current.getDataRetentionDays())
                .allowAnonymizedAnalytics(current.allowsAnonymizedAnalytics())
                .build();
        
        preferencesStore.put(userId, updated);
        return updated;
    }
    
    @Override
    public PrivacyPreferences optInProject(String userId, String projectId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(projectId, "Project ID cannot be null");
        
        PrivacyPreferences current = getPreferences(userId)
                .orElse(createDefaultPreferences(userId));
        
        Set<String> updatedOptedOutProjects = new HashSet<>(current.getOptedOutProjects());
        updatedOptedOutProjects.remove(projectId);
        
        PrivacyPreferences updated = PrivacyPreferences.builder()
                .userId(current.getUserId())
                .allowCloudProcessing(current.allowsCloudProcessing())
                .allowDataCollection(current.allowsDataCollection())
                .allowTelemetry(current.allowsTelemetry())
                .optedOutProjects(updatedOptedOutProjects)
                .optedOutDataTypes(current.getOptedOutDataTypes())
                .optedOutServices(current.getOptedOutServices())
                .optOutScope(current.getOptOutScope())
                .lastUpdated(LocalDateTime.now())
                .requireExplicitConsent(current.requiresExplicitConsent())
                .dataRetentionDays(current.getDataRetentionDays())
                .allowAnonymizedAnalytics(current.allowsAnonymizedAnalytics())
                .build();
        
        preferencesStore.put(userId, updated);
        return updated;
    }
    
    @Override
    public boolean isOptedOut(String userId, String projectId, DataClassification.DataType dataType, String serviceName) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        
        Optional<PrivacyPreferences> preferencesOpt = getPreferences(userId);
        if (preferencesOpt.isEmpty()) {
            return false; // Default to allowing processing if no preferences set
        }
        
        PrivacyPreferences preferences = preferencesOpt.get();
        
        // Check global opt-out
        if (preferences.getOptOutScope() == PrivacyPreferences.OptOutScope.GLOBAL) {
            return true;
        }
        
        // Check project-specific opt-out
        if (projectId != null && preferences.isProjectOptedOut(projectId)) {
            return true;
        }
        
        // Check data type opt-out
        if (dataType != null && preferences.isDataTypeOptedOut(dataType)) {
            return true;
        }
        
        // Check service-specific opt-out
        if (serviceName != null && preferences.isServiceOptedOut(serviceName)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public PrivacyPreferences setGlobalOptOut(String userId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        
        PrivacyPreferences current = getPreferences(userId)
                .orElse(createDefaultPreferences(userId));
        
        PrivacyPreferences updated = PrivacyPreferences.builder()
                .userId(current.getUserId())
                .allowCloudProcessing(false)
                .allowDataCollection(false)
                .allowTelemetry(false)
                .optedOutProjects(current.getOptedOutProjects())
                .optedOutDataTypes(current.getOptedOutDataTypes())
                .optedOutServices(current.getOptedOutServices())
                .optOutScope(PrivacyPreferences.OptOutScope.GLOBAL)
                .lastUpdated(LocalDateTime.now())
                .requireExplicitConsent(true)
                .dataRetentionDays(current.getDataRetentionDays())
                .allowAnonymizedAnalytics(false)
                .build();
        
        preferencesStore.put(userId, updated);
        return updated;
    }
    
    @Override
    public PrivacyPreferences removeGlobalOptOut(String userId) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        
        PrivacyPreferences current = getPreferences(userId)
                .orElse(createDefaultPreferences(userId));
        
        PrivacyPreferences updated = PrivacyPreferences.builder()
                .userId(current.getUserId())
                .allowCloudProcessing(true)
                .allowDataCollection(true)
                .allowTelemetry(true)
                .optedOutProjects(current.getOptedOutProjects())
                .optedOutDataTypes(current.getOptedOutDataTypes())
                .optedOutServices(current.getOptedOutServices())
                .optOutScope(PrivacyPreferences.OptOutScope.PROJECT_SPECIFIC)
                .lastUpdated(LocalDateTime.now())
                .requireExplicitConsent(false)
                .dataRetentionDays(current.getDataRetentionDays())
                .allowAnonymizedAnalytics(true)
                .build();
        
        preferencesStore.put(userId, updated);
        return updated;
    }
    
    @Override
    public boolean validatePreferences(PrivacyPreferences preferences) {
        Objects.requireNonNull(preferences, "Preferences cannot be null");
        
        // Validate user ID
        if (preferences.getUserId() == null || preferences.getUserId().trim().isEmpty()) {
            return false;
        }
        
        // Validate data retention days
        if (preferences.getDataRetentionDays() < 0 || preferences.getDataRetentionDays() > 365) {
            return false;
        }
        
        // Validate consistency: if global opt-out, cloud processing should be disabled
        if (preferences.getOptOutScope() == PrivacyPreferences.OptOutScope.GLOBAL && 
            preferences.allowsCloudProcessing()) {
            return false;
        }
        
        // Validate that opted-out collections are not null
        if (preferences.getOptedOutProjects() == null ||
            preferences.getOptedOutDataTypes() == null ||
            preferences.getOptedOutServices() == null) {
            return false;
        }
        
        return true;
    }
}