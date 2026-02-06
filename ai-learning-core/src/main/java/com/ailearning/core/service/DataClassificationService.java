package com.ailearning.core.service;

import com.ailearning.core.model.DataClassification;
import com.ailearning.core.model.ProjectContext;

import java.util.List;
import java.util.Optional;

/**
 * Service for classifying data based on privacy sensitivity and project context.
 */
public interface DataClassificationService {
    
    /**
     * Classifies data for a project based on its context and content.
     * 
     * @param projectContext The project context to analyze
     * @param content The content to classify
     * @return The data classification result
     */
    DataClassification classifyData(ProjectContext projectContext, String content);
    
    /**
     * Classifies data based on project patterns and heuristics.
     * 
     * @param projectId The project identifier
     * @param dataTypes The types of data being processed
     * @return The data classification result
     */
    DataClassification classifyProject(String projectId, List<DataClassification.DataType> dataTypes);
    
    /**
     * Updates the classification for a project based on user input.
     * 
     * @param projectId The project identifier
     * @param sensitivityLevel The user-specified sensitivity level
     * @param reason The reason for the classification
     * @return The updated data classification
     */
    DataClassification updateClassification(String projectId, 
                                          DataClassification.SensitivityLevel sensitivityLevel, 
                                          String reason);
    
    /**
     * Retrieves the current classification for a project.
     * 
     * @param projectId The project identifier
     * @return The current classification, if available
     */
    Optional<DataClassification> getClassification(String projectId);
    
    /**
     * Analyzes project content to automatically determine sensitivity level.
     * 
     * @param projectContext The project context to analyze
     * @return The suggested sensitivity level with reasoning
     */
    DataClassification.SensitivityLevel suggestSensitivityLevel(ProjectContext projectContext);
    
    /**
     * Checks if the given content contains sensitive patterns.
     * 
     * @param content The content to analyze
     * @return True if sensitive patterns are detected
     */
    boolean containsSensitivePatterns(String content);
    
    /**
     * Gets all classifications for projects.
     * 
     * @return List of all project classifications
     */
    List<DataClassification> getAllClassifications();
}