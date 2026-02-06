package com.ailearning.core.service;

import com.ailearning.core.model.DataClassification;
import com.ailearning.core.model.ProcessingDecision;
import com.ailearning.core.model.PrivacyPreferences;

/**
 * Service for making decisions about where and how to process data based on privacy requirements.
 */
public interface ProcessingDecisionEngine {
    
    /**
     * Makes a processing decision based on data classification and user preferences.
     * 
     * @param requestId The unique request identifier
     * @param processingType The type of processing being requested
     * @param dataClassification The classification of the data to be processed
     * @param privacyPreferences The user's privacy preferences
     * @return The processing decision
     */
    ProcessingDecision makeDecision(String requestId,
                                  ProcessingDecision.ProcessingType processingType,
                                  DataClassification dataClassification,
                                  PrivacyPreferences privacyPreferences);
    
    /**
     * Evaluates if cloud processing is allowed for the given context.
     * 
     * @param dataClassification The data classification
     * @param privacyPreferences The user's privacy preferences
     * @param serviceName The name of the cloud service
     * @return True if cloud processing is allowed
     */
    boolean isCloudProcessingAllowed(DataClassification dataClassification,
                                   PrivacyPreferences privacyPreferences,
                                   String serviceName);
    
    /**
     * Determines the optimal processing location based on constraints.
     * 
     * @param processingType The type of processing
     * @param dataClassification The data classification
     * @param privacyPreferences The user's privacy preferences
     * @return The recommended processing location
     */
    ProcessingDecision.ProcessingLocation determineProcessingLocation(
            ProcessingDecision.ProcessingType processingType,
            DataClassification dataClassification,
            PrivacyPreferences privacyPreferences);
    
    /**
     * Checks if user consent is required for the processing decision.
     * 
     * @param processingType The type of processing
     * @param dataClassification The data classification
     * @param privacyPreferences The user's privacy preferences
     * @return True if user consent is required
     */
    boolean requiresUserConsent(ProcessingDecision.ProcessingType processingType,
                              DataClassification dataClassification,
                              PrivacyPreferences privacyPreferences);
    
    /**
     * Gets the list of allowed services for the given processing context.
     * 
     * @param processingType The type of processing
     * @param dataClassification The data classification
     * @param privacyPreferences The user's privacy preferences
     * @return Set of allowed service names
     */
    java.util.Set<String> getAllowedServices(ProcessingDecision.ProcessingType processingType,
                                           DataClassification dataClassification,
                                           PrivacyPreferences privacyPreferences);
    
    /**
     * Gets the list of blocked services for the given processing context.
     * 
     * @param processingType The type of processing
     * @param dataClassification The data classification
     * @param privacyPreferences The user's privacy preferences
     * @return Set of blocked service names
     */
    java.util.Set<String> getBlockedServices(ProcessingDecision.ProcessingType processingType,
                                           DataClassification dataClassification,
                                           PrivacyPreferences privacyPreferences);
}