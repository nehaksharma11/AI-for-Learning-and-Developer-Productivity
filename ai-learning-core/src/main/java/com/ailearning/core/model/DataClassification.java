package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the privacy classification of data for processing decisions.
 * Used to determine whether data should be processed locally or can be sent to cloud services.
 */
public class DataClassification {
    
    public enum SensitivityLevel {
        PUBLIC,        // Can be processed anywhere
        INTERNAL,      // Company internal, prefer local processing
        CONFIDENTIAL,  // Must be processed locally
        RESTRICTED     // Absolutely no external processing
    }
    
    public enum DataType {
        SOURCE_CODE,
        COMMENTS,
        VARIABLE_NAMES,
        DOCUMENTATION,
        CONFIGURATION,
        DEPENDENCIES,
        METADATA
    }
    
    private final String projectId;
    private final SensitivityLevel sensitivityLevel;
    private final Set<DataType> dataTypes;
    private final String reason;
    private final LocalDateTime classifiedAt;
    private final boolean userOverride;
    private final String classificationSource;
    
    private DataClassification(Builder builder) {
        this.projectId = Objects.requireNonNull(builder.projectId, "Project ID cannot be null");
        this.sensitivityLevel = Objects.requireNonNull(builder.sensitivityLevel, "Sensitivity level cannot be null");
        this.dataTypes = Set.copyOf(Objects.requireNonNull(builder.dataTypes, "Data types cannot be null"));
        this.reason = builder.reason;
        this.classifiedAt = Objects.requireNonNull(builder.classifiedAt, "Classification time cannot be null");
        this.userOverride = builder.userOverride;
        this.classificationSource = Objects.requireNonNull(builder.classificationSource, "Classification source cannot be null");
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public SensitivityLevel getSensitivityLevel() {
        return sensitivityLevel;
    }
    
    public Set<DataType> getDataTypes() {
        return dataTypes;
    }
    
    public String getReason() {
        return reason;
    }
    
    public LocalDateTime getClassifiedAt() {
        return classifiedAt;
    }
    
    public boolean isUserOverride() {
        return userOverride;
    }
    
    public String getClassificationSource() {
        return classificationSource;
    }
    
    /**
     * Determines if this data classification allows cloud processing.
     */
    public boolean allowsCloudProcessing() {
        return sensitivityLevel == SensitivityLevel.PUBLIC || 
               (sensitivityLevel == SensitivityLevel.INTERNAL && !userOverride);
    }
    
    /**
     * Determines if this data classification requires local-only processing.
     */
    public boolean requiresLocalProcessing() {
        return sensitivityLevel == SensitivityLevel.CONFIDENTIAL || 
               sensitivityLevel == SensitivityLevel.RESTRICTED ||
               userOverride;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String projectId;
        private SensitivityLevel sensitivityLevel;
        private Set<DataType> dataTypes;
        private String reason;
        private LocalDateTime classifiedAt = LocalDateTime.now();
        private boolean userOverride = false;
        private String classificationSource;
        
        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }
        
        public Builder sensitivityLevel(SensitivityLevel sensitivityLevel) {
            this.sensitivityLevel = sensitivityLevel;
            return this;
        }
        
        public Builder dataTypes(Set<DataType> dataTypes) {
            this.dataTypes = dataTypes;
            return this;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public Builder classifiedAt(LocalDateTime classifiedAt) {
            this.classifiedAt = classifiedAt;
            return this;
        }
        
        public Builder userOverride(boolean userOverride) {
            this.userOverride = userOverride;
            return this;
        }
        
        public Builder classificationSource(String classificationSource) {
            this.classificationSource = classificationSource;
            return this;
        }
        
        public DataClassification build() {
            return new DataClassification(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataClassification that = (DataClassification) o;
        return userOverride == that.userOverride &&
               Objects.equals(projectId, that.projectId) &&
               sensitivityLevel == that.sensitivityLevel &&
               Objects.equals(dataTypes, that.dataTypes) &&
               Objects.equals(reason, that.reason) &&
               Objects.equals(classifiedAt, that.classifiedAt) &&
               Objects.equals(classificationSource, that.classificationSource);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(projectId, sensitivityLevel, dataTypes, reason, classifiedAt, userOverride, classificationSource);
    }
    
    @Override
    public String toString() {
        return "DataClassification{" +
               "projectId='" + projectId + '\'' +
               ", sensitivityLevel=" + sensitivityLevel +
               ", dataTypes=" + dataTypes +
               ", reason='" + reason + '\'' +
               ", classifiedAt=" + classifiedAt +
               ", userOverride=" + userOverride +
               ", classificationSource='" + classificationSource + '\'' +
               '}';
    }
}