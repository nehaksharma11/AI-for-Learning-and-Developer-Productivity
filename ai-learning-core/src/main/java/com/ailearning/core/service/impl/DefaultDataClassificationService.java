package com.ailearning.core.service.impl;

import com.ailearning.core.model.DataClassification;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.service.DataClassificationService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Default implementation of DataClassificationService that analyzes projects and content
 * to determine appropriate privacy sensitivity levels.
 */
public class DefaultDataClassificationService implements DataClassificationService {
    
    private final Map<String, DataClassification> classificationCache = new ConcurrentHashMap<>();
    
    // Patterns that indicate sensitive content
    private static final List<Pattern> SENSITIVE_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)(password|secret|key|token|credential)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(api[_-]?key|auth[_-]?token|private[_-]?key)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(confidential|proprietary|internal[_-]?only)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(ssn|social[_-]?security|credit[_-]?card)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(database[_-]?url|connection[_-]?string)", Pattern.CASE_INSENSITIVE)
    );
    
    // Project patterns that indicate higher sensitivity
    private static final List<Pattern> SENSITIVE_PROJECT_PATTERNS = Arrays.asList(
        Pattern.compile("(?i)(bank|financial|payment|healthcare|medical)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(government|defense|military|classified)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(internal|proprietary|confidential|private)", Pattern.CASE_INSENSITIVE)
    );
    
    @Override
    public DataClassification classifyData(ProjectContext projectContext, String content) {
        Objects.requireNonNull(projectContext, "Project context cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");
        
        String projectId = projectContext.getId();
        
        // Check cache first
        DataClassification cached = classificationCache.get(projectId);
        if (cached != null) {
            return cached;
        }
        
        // Analyze content for sensitive patterns
        boolean hasSensitiveContent = containsSensitivePatterns(content);
        
        // Analyze project context
        DataClassification.SensitivityLevel suggestedLevel = suggestSensitivityLevel(projectContext);
        
        // Combine content and project analysis
        DataClassification.SensitivityLevel finalLevel = combineSensitivityLevels(suggestedLevel, hasSensitiveContent);
        
        Set<DataClassification.DataType> dataTypes = inferDataTypes(content);
        
        DataClassification classification = DataClassification.builder()
                .projectId(projectId)
                .sensitivityLevel(finalLevel)
                .dataTypes(dataTypes)
                .reason(generateReasoning(finalLevel, hasSensitiveContent, projectContext))
                .classifiedAt(LocalDateTime.now())
                .userOverride(false)
                .classificationSource("automatic-analysis")
                .build();
        
        classificationCache.put(projectId, classification);
        return classification;
    }
    
    @Override
    public DataClassification classifyProject(String projectId, List<DataClassification.DataType> dataTypes) {
        Objects.requireNonNull(projectId, "Project ID cannot be null");
        Objects.requireNonNull(dataTypes, "Data types cannot be null");
        
        // Check cache first
        DataClassification cached = classificationCache.get(projectId);
        if (cached != null) {
            return cached;
        }
        
        // Analyze project name and structure for sensitivity indicators
        boolean hasSensitiveProjectName = SENSITIVE_PROJECT_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(projectId).find());
        
        DataClassification.SensitivityLevel level = hasSensitiveProjectName ? 
                DataClassification.SensitivityLevel.CONFIDENTIAL : 
                DataClassification.SensitivityLevel.INTERNAL;
        
        DataClassification classification = DataClassification.builder()
                .projectId(projectId)
                .sensitivityLevel(level)
                .dataTypes(Set.copyOf(dataTypes))
                .reason(hasSensitiveProjectName ? "Project name indicates sensitive domain" : "Standard internal project")
                .classifiedAt(LocalDateTime.now())
                .userOverride(false)
                .classificationSource("project-analysis")
                .build();
        
        classificationCache.put(projectId, classification);
        return classification;
    }
    
    @Override
    public DataClassification updateClassification(String projectId, 
                                                 DataClassification.SensitivityLevel sensitivityLevel, 
                                                 String reason) {
        Objects.requireNonNull(projectId, "Project ID cannot be null");
        Objects.requireNonNull(sensitivityLevel, "Sensitivity level cannot be null");
        
        DataClassification existing = classificationCache.get(projectId);
        Set<DataClassification.DataType> dataTypes = existing != null ? 
                existing.getDataTypes() : 
                Set.of(DataClassification.DataType.SOURCE_CODE);
        
        DataClassification updated = DataClassification.builder()
                .projectId(projectId)
                .sensitivityLevel(sensitivityLevel)
                .dataTypes(dataTypes)
                .reason(reason != null ? reason : "User override")
                .classifiedAt(LocalDateTime.now())
                .userOverride(true)
                .classificationSource("user-override")
                .build();
        
        classificationCache.put(projectId, updated);
        return updated;
    }
    
    @Override
    public Optional<DataClassification> getClassification(String projectId) {
        Objects.requireNonNull(projectId, "Project ID cannot be null");
        return Optional.ofNullable(classificationCache.get(projectId));
    }
    
    @Override
    public DataClassification.SensitivityLevel suggestSensitivityLevel(ProjectContext projectContext) {
        Objects.requireNonNull(projectContext, "Project context cannot be null");
        
        String projectId = projectContext.getId().toLowerCase();
        
        // Check for high-sensitivity indicators
        if (SENSITIVE_PROJECT_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(projectId).find())) {
            return DataClassification.SensitivityLevel.CONFIDENTIAL;
        }
        
        // Check project structure for sensitivity indicators
        if (projectContext.getStructure() != null) {
            long configFiles = projectContext.getStructure().getFiles().stream()
                    .filter(file -> file.getName().contains("config") || file.getName().contains("secret"))
                    .count();
            
            if (configFiles > 0) {
                return DataClassification.SensitivityLevel.INTERNAL;
            }
        }
        
        // Default to internal for most projects
        return DataClassification.SensitivityLevel.INTERNAL;
    }
    
    @Override
    public boolean containsSensitivePatterns(String content) {
        Objects.requireNonNull(content, "Content cannot be null");
        
        return SENSITIVE_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(content).find());
    }
    
    @Override
    public List<DataClassification> getAllClassifications() {
        return new ArrayList<>(classificationCache.values());
    }
    
    private DataClassification.SensitivityLevel combineSensitivityLevels(
            DataClassification.SensitivityLevel projectLevel, boolean hasSensitiveContent) {
        
        if (hasSensitiveContent) {
            // Upgrade sensitivity if sensitive content is detected
            return projectLevel == DataClassification.SensitivityLevel.PUBLIC ? 
                    DataClassification.SensitivityLevel.INTERNAL : 
                    DataClassification.SensitivityLevel.CONFIDENTIAL;
        }
        
        return projectLevel;
    }
    
    private Set<DataClassification.DataType> inferDataTypes(String content) {
        Set<DataClassification.DataType> types = new HashSet<>();
        
        // Always include source code as we're analyzing code content
        types.add(DataClassification.DataType.SOURCE_CODE);
        
        // Check for comments
        if (content.contains("//") || content.contains("/*") || content.contains("#")) {
            types.add(DataClassification.DataType.COMMENTS);
        }
        
        // Check for configuration patterns
        if (content.contains("config") || content.contains("properties") || content.contains("yml")) {
            types.add(DataClassification.DataType.CONFIGURATION);
        }
        
        // Check for documentation patterns
        if (content.contains("@param") || content.contains("@return") || content.contains("/**")) {
            types.add(DataClassification.DataType.DOCUMENTATION);
        }
        
        return types;
    }
    
    private String generateReasoning(DataClassification.SensitivityLevel level, 
                                   boolean hasSensitiveContent, 
                                   ProjectContext projectContext) {
        StringBuilder reasoning = new StringBuilder();
        
        reasoning.append("Classification: ").append(level.name()).append(". ");
        
        if (hasSensitiveContent) {
            reasoning.append("Sensitive patterns detected in content. ");
        }
        
        if (projectContext.getId() != null) {
            boolean hasSensitiveProjectName = SENSITIVE_PROJECT_PATTERNS.stream()
                    .anyMatch(pattern -> pattern.matcher(projectContext.getId()).find());
            if (hasSensitiveProjectName) {
                reasoning.append("Project name indicates sensitive domain. ");
            }
        }
        
        if (reasoning.length() == 0) {
            reasoning.append("Standard classification based on project analysis.");
        }
        
        return reasoning.toString().trim();
    }
}