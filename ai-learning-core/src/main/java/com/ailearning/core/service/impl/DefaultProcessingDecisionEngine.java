package com.ailearning.core.service.impl;

import com.ailearning.core.model.DataClassification;
import com.ailearning.core.model.ProcessingDecision;
import com.ailearning.core.model.PrivacyPreferences;
import com.ailearning.core.service.ProcessingDecisionEngine;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Default implementation of ProcessingDecisionEngine that makes intelligent decisions
 * about where and how to process data based on privacy requirements and user preferences.
 */
public class DefaultProcessingDecisionEngine implements ProcessingDecisionEngine {
    
    // Services that are considered safe for most processing
    private static final Set<String> TRUSTED_SERVICES = Set.of(
        "local-analyzer", "local-documentation", "local-learning"
    );
    
    // Services that require higher security clearance
    private static final Set<String> CLOUD_SERVICES = Set.of(
        "openai-service", "huggingface-service", "cloud-analyzer"
    );
    
    // Processing types that can be done locally
    private static final Set<ProcessingDecision.ProcessingType> LOCAL_CAPABLE_TYPES = Set.of(
        ProcessingDecision.ProcessingType.CODE_ANALYSIS,
        ProcessingDecision.ProcessingType.PATTERN_DETECTION,
        ProcessingDecision.ProcessingType.SECURITY_ANALYSIS
    );
    
    @Override
    public ProcessingDecision makeDecision(String requestId,
                                         ProcessingDecision.ProcessingType processingType,
                                         DataClassification dataClassification,
                                         PrivacyPreferences privacyPreferences) {
        
        Objects.requireNonNull(requestId, "Request ID cannot be null");
        Objects.requireNonNull(processingType, "Processing type cannot be null");
        Objects.requireNonNull(dataClassification, "Data classification cannot be null");
        Objects.requireNonNull(privacyPreferences, "Privacy preferences cannot be null");
        
        ProcessingDecision.ProcessingLocation location = determineProcessingLocation(
                processingType, dataClassification, privacyPreferences);
        
        Set<String> allowedServices = getAllowedServices(processingType, dataClassification, privacyPreferences);
        Set<String> blockedServices = getBlockedServices(processingType, dataClassification, privacyPreferences);
        
        boolean requiresConsent = requiresUserConsent(processingType, dataClassification, privacyPreferences);
        
        String reasoning = generateReasoning(location, dataClassification, privacyPreferences, processingType);
        
        return ProcessingDecision.builder()
                .requestId(requestId)
                .projectId(dataClassification.getProjectId())
                .location(location)
                .type(processingType)
                .dataClassification(dataClassification)
                .reasoning(reasoning)
                .decidedAt(LocalDateTime.now())
                .allowedServices(allowedServices)
                .blockedServices(blockedServices)
                .requiresUserConsent(requiresConsent)
                .build();
    }
    
    @Override
    public boolean isCloudProcessingAllowed(DataClassification dataClassification,
                                          PrivacyPreferences privacyPreferences,
                                          String serviceName) {
        
        Objects.requireNonNull(dataClassification, "Data classification cannot be null");
        Objects.requireNonNull(privacyPreferences, "Privacy preferences cannot be null");
        Objects.requireNonNull(serviceName, "Service name cannot be null");
        
        // Check if user has globally opted out of cloud processing
        if (!privacyPreferences.allowsCloudProcessing()) {
            return false;
        }
        
        // Check if data classification allows cloud processing
        if (!dataClassification.allowsCloudProcessing()) {
            return false;
        }
        
        // Check if user has opted out of this specific service
        if (privacyPreferences.isServiceOptedOut(serviceName)) {
            return false;
        }
        
        // Check if user has opted out of this project
        if (privacyPreferences.isProjectOptedOut(dataClassification.getProjectId())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public ProcessingDecision.ProcessingLocation determineProcessingLocation(
            ProcessingDecision.ProcessingType processingType,
            DataClassification dataClassification,
            PrivacyPreferences privacyPreferences) {
        
        Objects.requireNonNull(processingType, "Processing type cannot be null");
        Objects.requireNonNull(dataClassification, "Data classification cannot be null");
        Objects.requireNonNull(privacyPreferences, "Privacy preferences cannot be null");
        
        // If data requires local processing, enforce it
        if (dataClassification.requiresLocalProcessing()) {
            return ProcessingDecision.ProcessingLocation.LOCAL_ONLY;
        }
        
        // If user has opted out of cloud processing globally
        if (!privacyPreferences.allowsCloudProcessing()) {
            return ProcessingDecision.ProcessingLocation.LOCAL_ONLY;
        }
        
        // If user has opted out of this specific project
        if (privacyPreferences.isProjectOptedOut(dataClassification.getProjectId())) {
            return ProcessingDecision.ProcessingLocation.LOCAL_ONLY;
        }
        
        // For highly sensitive data, prefer local processing
        if (dataClassification.getSensitivityLevel() == DataClassification.SensitivityLevel.CONFIDENTIAL ||
            dataClassification.getSensitivityLevel() == DataClassification.SensitivityLevel.RESTRICTED) {
            return LOCAL_CAPABLE_TYPES.contains(processingType) ? 
                    ProcessingDecision.ProcessingLocation.LOCAL_ONLY : 
                    ProcessingDecision.ProcessingLocation.HYBRID;
        }
        
        // For complex processing that benefits from cloud capabilities
        if (processingType == ProcessingDecision.ProcessingType.EXPLANATION_GENERATION ||
            processingType == ProcessingDecision.ProcessingType.LEARNING_RECOMMENDATION) {
            return ProcessingDecision.ProcessingLocation.CLOUD_PREFERRED;
        }
        
        // Default to hybrid approach for flexibility
        return ProcessingDecision.ProcessingLocation.HYBRID;
    }
    
    @Override
    public boolean requiresUserConsent(ProcessingDecision.ProcessingType processingType,
                                     DataClassification dataClassification,
                                     PrivacyPreferences privacyPreferences) {
        
        Objects.requireNonNull(processingType, "Processing type cannot be null");
        Objects.requireNonNull(dataClassification, "Data classification cannot be null");
        Objects.requireNonNull(privacyPreferences, "Privacy preferences cannot be null");
        
        // Always require consent if user has set this preference
        if (privacyPreferences.requiresExplicitConsent()) {
            return true;
        }
        
        // Require consent for restricted data
        if (dataClassification.getSensitivityLevel() == DataClassification.SensitivityLevel.RESTRICTED) {
            return true;
        }
        
        // Require consent for cloud processing of confidential data
        if (dataClassification.getSensitivityLevel() == DataClassification.SensitivityLevel.CONFIDENTIAL &&
            dataClassification.allowsCloudProcessing()) {
            return true;
        }
        
        // Require consent for learning recommendations with personal data
        if (processingType == ProcessingDecision.ProcessingType.LEARNING_RECOMMENDATION &&
            dataClassification.getDataTypes().contains(DataClassification.DataType.SOURCE_CODE)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public Set<String> getAllowedServices(ProcessingDecision.ProcessingType processingType,
                                        DataClassification dataClassification,
                                        PrivacyPreferences privacyPreferences) {
        
        Objects.requireNonNull(processingType, "Processing type cannot be null");
        Objects.requireNonNull(dataClassification, "Data classification cannot be null");
        Objects.requireNonNull(privacyPreferences, "Privacy preferences cannot be null");
        
        Set<String> allowedServices = new HashSet<>();
        
        // Always allow trusted local services
        allowedServices.addAll(TRUSTED_SERVICES);
        
        // Add cloud services if allowed
        if (isCloudProcessingAllowed(dataClassification, privacyPreferences, "cloud-services")) {
            allowedServices.addAll(CLOUD_SERVICES);
        }
        
        // Remove any services the user has specifically opted out of
        allowedServices.removeIf(service -> privacyPreferences.isServiceOptedOut(service));
        
        return allowedServices;
    }
    
    @Override
    public Set<String> getBlockedServices(ProcessingDecision.ProcessingType processingType,
                                        DataClassification dataClassification,
                                        PrivacyPreferences privacyPreferences) {
        
        Objects.requireNonNull(processingType, "Processing type cannot be null");
        Objects.requireNonNull(dataClassification, "Data classification cannot be null");
        Objects.requireNonNull(privacyPreferences, "Privacy preferences cannot be null");
        
        Set<String> blockedServices = new HashSet<>();
        
        // Block cloud services if not allowed
        if (!isCloudProcessingAllowed(dataClassification, privacyPreferences, "cloud-services")) {
            blockedServices.addAll(CLOUD_SERVICES);
        }
        
        // Add user-specified blocked services
        blockedServices.addAll(privacyPreferences.getOptedOutServices());
        
        // Block all services for restricted data except local ones
        if (dataClassification.getSensitivityLevel() == DataClassification.SensitivityLevel.RESTRICTED) {
            blockedServices.addAll(CLOUD_SERVICES);
        }
        
        return blockedServices;
    }
    
    private String generateReasoning(ProcessingDecision.ProcessingLocation location,
                                   DataClassification dataClassification,
                                   PrivacyPreferences privacyPreferences,
                                   ProcessingDecision.ProcessingType processingType) {
        
        StringBuilder reasoning = new StringBuilder();
        
        reasoning.append("Processing location: ").append(location.name()).append(". ");
        
        if (location == ProcessingDecision.ProcessingLocation.LOCAL_ONLY) {
            if (dataClassification.requiresLocalProcessing()) {
                reasoning.append("Data classification requires local processing. ");
            } else if (!privacyPreferences.allowsCloudProcessing()) {
                reasoning.append("User preferences require local processing. ");
            } else if (privacyPreferences.isProjectOptedOut(dataClassification.getProjectId())) {
                reasoning.append("User has opted out of cloud processing for this project. ");
            }
        } else if (location == ProcessingDecision.ProcessingLocation.CLOUD_PREFERRED) {
            reasoning.append("Processing type benefits from cloud capabilities. ");
        } else if (location == ProcessingDecision.ProcessingLocation.HYBRID) {
            reasoning.append("Hybrid approach balances privacy and functionality. ");
        }
        
        reasoning.append("Sensitivity level: ").append(dataClassification.getSensitivityLevel().name()).append(".");
        
        return reasoning.toString();
    }
}