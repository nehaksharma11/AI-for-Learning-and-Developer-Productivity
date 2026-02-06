package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Represents user privacy preferences and opt-out settings for data processing.
 */
public class PrivacyPreferences {
    
    public enum OptOutScope {
        GLOBAL,           // Opt out of all external processing
        PROJECT_SPECIFIC, // Opt out for specific projects
        DATA_TYPE,        // Opt out for specific data types
        SERVICE_SPECIFIC  // Opt out for specific services
    }
    
    private final String userId;
    private final boolean allowCloudProcessing;
    private final boolean allowDataCollection;
    private final boolean allowTelemetry;
    private final Set<String> optedOutProjects;
    private final Set<DataClassification.DataType> optedOutDataTypes;
    private final Set<String> optedOutServices;
    private final OptOutScope optOutScope;
    private final LocalDateTime lastUpdated;
    private final boolean requireExplicitConsent;
    private final int dataRetentionDays;
    private final boolean allowAnonymizedAnalytics;
    
    private PrivacyPreferences(Builder builder) {
        this.userId = Objects.requireNonNull(builder.userId, "User ID cannot be null");
        this.allowCloudProcessing = builder.allowCloudProcessing;
        this.allowDataCollection = builder.allowDataCollection;
        this.allowTelemetry = builder.allowTelemetry;
        this.optedOutProjects = Set.copyOf(Objects.requireNonNull(builder.optedOutProjects, "Opted out projects cannot be null"));
        this.optedOutDataTypes = Set.copyOf(Objects.requireNonNull(builder.optedOutDataTypes, "Opted out data types cannot be null"));
        this.optedOutServices = Set.copyOf(Objects.requireNonNull(builder.optedOutServices, "Opted out services cannot be null"));
        this.optOutScope = Objects.requireNonNull(builder.optOutScope, "Opt out scope cannot be null");
        this.lastUpdated = Objects.requireNonNull(builder.lastUpdated, "Last updated time cannot be null");
        this.requireExplicitConsent = builder.requireExplicitConsent;
        this.dataRetentionDays = builder.dataRetentionDays;
        this.allowAnonymizedAnalytics = builder.allowAnonymizedAnalytics;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public boolean allowsCloudProcessing() {
        return allowCloudProcessing;
    }
    
    public boolean allowsDataCollection() {
        return allowDataCollection;
    }
    
    public boolean allowsTelemetry() {
        return allowTelemetry;
    }
    
    public Set<String> getOptedOutProjects() {
        return optedOutProjects;
    }
    
    public Set<DataClassification.DataType> getOptedOutDataTypes() {
        return optedOutDataTypes;
    }
    
    public Set<String> getOptedOutServices() {
        return optedOutServices;
    }
    
    public OptOutScope getOptOutScope() {
        return optOutScope;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public boolean requiresExplicitConsent() {
        return requireExplicitConsent;
    }
    
    public int getDataRetentionDays() {
        return dataRetentionDays;
    }
    
    public boolean allowsAnonymizedAnalytics() {
        return allowAnonymizedAnalytics;
    }
    
    /**
     * Checks if processing is allowed for a specific project.
     */
    public boolean isProjectOptedOut(String projectId) {
        return optOutScope == OptOutScope.GLOBAL || 
               (optOutScope == OptOutScope.PROJECT_SPECIFIC && optedOutProjects.contains(projectId));
    }
    
    /**
     * Checks if processing is allowed for a specific data type.
     */
    public boolean isDataTypeOptedOut(DataClassification.DataType dataType) {
        return optOutScope == OptOutScope.GLOBAL ||
               (optOutScope == OptOutScope.DATA_TYPE && optedOutDataTypes.contains(dataType));
    }
    
    /**
     * Checks if processing is allowed for a specific service.
     */
    public boolean isServiceOptedOut(String serviceName) {
        return optOutScope == OptOutScope.GLOBAL ||
               (optOutScope == OptOutScope.SERVICE_SPECIFIC && optedOutServices.contains(serviceName));
    }
    
    /**
     * Determines if cloud processing is allowed for the given context.
     */
    public boolean allowsCloudProcessingFor(String projectId, DataClassification.DataType dataType, String serviceName) {
        if (!allowCloudProcessing) {
            return false;
        }
        
        return !isProjectOptedOut(projectId) && 
               !isDataTypeOptedOut(dataType) && 
               !isServiceOptedOut(serviceName);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String userId;
        private boolean allowCloudProcessing = true;
        private boolean allowDataCollection = true;
        private boolean allowTelemetry = true;
        private Set<String> optedOutProjects = Set.of();
        private Set<DataClassification.DataType> optedOutDataTypes = Set.of();
        private Set<String> optedOutServices = Set.of();
        private OptOutScope optOutScope = OptOutScope.PROJECT_SPECIFIC;
        private LocalDateTime lastUpdated = LocalDateTime.now();
        private boolean requireExplicitConsent = false;
        private int dataRetentionDays = 30;
        private boolean allowAnonymizedAnalytics = true;
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder allowCloudProcessing(boolean allowCloudProcessing) {
            this.allowCloudProcessing = allowCloudProcessing;
            return this;
        }
        
        public Builder allowDataCollection(boolean allowDataCollection) {
            this.allowDataCollection = allowDataCollection;
            return this;
        }
        
        public Builder allowTelemetry(boolean allowTelemetry) {
            this.allowTelemetry = allowTelemetry;
            return this;
        }
        
        public Builder optedOutProjects(Set<String> optedOutProjects) {
            this.optedOutProjects = optedOutProjects;
            return this;
        }
        
        public Builder optedOutDataTypes(Set<DataClassification.DataType> optedOutDataTypes) {
            this.optedOutDataTypes = optedOutDataTypes;
            return this;
        }
        
        public Builder optedOutServices(Set<String> optedOutServices) {
            this.optedOutServices = optedOutServices;
            return this;
        }
        
        public Builder optOutScope(OptOutScope optOutScope) {
            this.optOutScope = optOutScope;
            return this;
        }
        
        public Builder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }
        
        public Builder requireExplicitConsent(boolean requireExplicitConsent) {
            this.requireExplicitConsent = requireExplicitConsent;
            return this;
        }
        
        public Builder dataRetentionDays(int dataRetentionDays) {
            this.dataRetentionDays = dataRetentionDays;
            return this;
        }
        
        public Builder allowAnonymizedAnalytics(boolean allowAnonymizedAnalytics) {
            this.allowAnonymizedAnalytics = allowAnonymizedAnalytics;
            return this;
        }
        
        public PrivacyPreferences build() {
            return new PrivacyPreferences(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivacyPreferences that = (PrivacyPreferences) o;
        return allowCloudProcessing == that.allowCloudProcessing &&
               allowDataCollection == that.allowDataCollection &&
               allowTelemetry == that.allowTelemetry &&
               requireExplicitConsent == that.requireExplicitConsent &&
               dataRetentionDays == that.dataRetentionDays &&
               allowAnonymizedAnalytics == that.allowAnonymizedAnalytics &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(optedOutProjects, that.optedOutProjects) &&
               Objects.equals(optedOutDataTypes, that.optedOutDataTypes) &&
               Objects.equals(optedOutServices, that.optedOutServices) &&
               optOutScope == that.optOutScope &&
               Objects.equals(lastUpdated, that.lastUpdated);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, allowCloudProcessing, allowDataCollection, allowTelemetry, optedOutProjects, optedOutDataTypes, optedOutServices, optOutScope, lastUpdated, requireExplicitConsent, dataRetentionDays, allowAnonymizedAnalytics);
    }
    
    @Override
    public String toString() {
        return "PrivacyPreferences{" +
               "userId='" + userId + '\'' +
               ", allowCloudProcessing=" + allowCloudProcessing +
               ", allowDataCollection=" + allowDataCollection +
               ", allowTelemetry=" + allowTelemetry +
               ", optedOutProjects=" + optedOutProjects +
               ", optedOutDataTypes=" + optedOutDataTypes +
               ", optedOutServices=" + optedOutServices +
               ", optOutScope=" + optOutScope +
               ", lastUpdated=" + lastUpdated +
               ", requireExplicitConsent=" + requireExplicitConsent +
               ", dataRetentionDays=" + dataRetentionDays +
               ", allowAnonymizedAnalytics=" + allowAnonymizedAnalytics +
               '}';
    }
}