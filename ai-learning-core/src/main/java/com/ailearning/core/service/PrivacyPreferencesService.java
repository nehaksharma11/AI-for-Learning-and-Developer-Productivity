package com.ailearning.core.service;

import com.ailearning.core.model.DataClassification;
import com.ailearning.core.model.PrivacyPreferences;

import java.util.Optional;
import java.util.Set;

/**
 * Service for managing user privacy preferences and opt-out settings.
 */
public interface PrivacyPreferencesService {
    
    /**
     * Retrieves privacy preferences for a user.
     * 
     * @param userId The user identifier
     * @return The user's privacy preferences, if available
     */
    Optional<PrivacyPreferences> getPreferences(String userId);
    
    /**
     * Updates privacy preferences for a user.
     * 
     * @param preferences The updated privacy preferences
     * @return The saved privacy preferences
     */
    PrivacyPreferences updatePreferences(PrivacyPreferences preferences);
    
    /**
     * Creates default privacy preferences for a new user.
     * 
     * @param userId The user identifier
     * @return The default privacy preferences
     */
    PrivacyPreferences createDefaultPreferences(String userId);
    
    /**
     * Opts out a user from processing for a specific project.
     * 
     * @param userId The user identifier
     * @param projectId The project identifier
     * @return The updated privacy preferences
     */
    PrivacyPreferences optOutProject(String userId, String projectId);
    
    /**
     * Opts out a user from processing for specific data types.
     * 
     * @param userId The user identifier
     * @param dataTypes The data types to opt out from
     * @return The updated privacy preferences
     */
    PrivacyPreferences optOutDataTypes(String userId, Set<DataClassification.DataType> dataTypes);
    
    /**
     * Opts out a user from specific services.
     * 
     * @param userId The user identifier
     * @param services The services to opt out from
     * @return The updated privacy preferences
     */
    PrivacyPreferences optOutServices(String userId, Set<String> services);
    
    /**
     * Opts a user back in for a specific project.
     * 
     * @param userId The user identifier
     * @param projectId The project identifier
     * @return The updated privacy preferences
     */
    PrivacyPreferences optInProject(String userId, String projectId);
    
    /**
     * Checks if a user has opted out of processing for a specific context.
     * 
     * @param userId The user identifier
     * @param projectId The project identifier
     * @param dataType The data type
     * @param serviceName The service name
     * @return True if the user has opted out
     */
    boolean isOptedOut(String userId, String projectId, DataClassification.DataType dataType, String serviceName);
    
    /**
     * Sets global opt-out for a user (opts out of all external processing).
     * 
     * @param userId The user identifier
     * @return The updated privacy preferences
     */
    PrivacyPreferences setGlobalOptOut(String userId);
    
    /**
     * Removes global opt-out for a user.
     * 
     * @param userId The user identifier
     * @return The updated privacy preferences
     */
    PrivacyPreferences removeGlobalOptOut(String userId);
    
    /**
     * Validates privacy preferences for consistency and compliance.
     * 
     * @param preferences The preferences to validate
     * @return True if preferences are valid
     */
    boolean validatePreferences(PrivacyPreferences preferences);
}