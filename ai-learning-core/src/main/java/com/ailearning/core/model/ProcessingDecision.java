package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a decision about where and how to process data based on privacy requirements.
 */
public class ProcessingDecision {
    
    public enum ProcessingLocation {
        LOCAL_ONLY,      // Process only on local machine
        CLOUD_PREFERRED, // Prefer cloud but can fallback to local
        CLOUD_REQUIRED,  // Must use cloud services
        HYBRID          // Use both local and cloud processing
    }
    
    public enum ProcessingType {
        CODE_ANALYSIS,
        DOCUMENTATION_GENERATION,
        LEARNING_RECOMMENDATION,
        EXPLANATION_GENERATION,
        PATTERN_DETECTION,
        SECURITY_ANALYSIS
    }
    
    private final String requestId;
    private final String projectId;
    private final ProcessingLocation location;
    private final ProcessingType type;
    private final DataClassification dataClassification;
    private final String reasoning;
    private final LocalDateTime decidedAt;
    private final Set<String> allowedServices;
    private final Set<String> blockedServices;
    private final boolean requiresUserConsent;
    
    private ProcessingDecision(Builder builder) {
        this.requestId = Objects.requireNonNull(builder.requestId, "Request ID cannot be null");
        this.projectId = Objects.requireNonNull(builder.projectId, "Project ID cannot be null");
        this.location = Objects.requireNonNull(builder.location, "Processing location cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Processing type cannot be null");
        this.dataClassification = Objects.requireNonNull(builder.dataClassification, "Data classification cannot be null");
        this.reasoning = builder.reasoning;
        this.decidedAt = Objects.requireNonNull(builder.decidedAt, "Decision time cannot be null");
        this.allowedServices = Set.copyOf(Objects.requireNonNull(builder.allowedServices, "Allowed services cannot be null"));
        this.blockedServices = Set.copyOf(Objects.requireNonNull(builder.blockedServices, "Blocked services cannot be null"));
        this.requiresUserConsent = builder.requiresUserConsent;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public ProcessingLocation getLocation() {
        return location;
    }
    
    public ProcessingType getType() {
        return type;
    }
    
    public DataClassification getDataClassification() {
        return dataClassification;
    }
    
    public String getReasoning() {
        return reasoning;
    }
    
    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }
    
    public Set<String> getAllowedServices() {
        return allowedServices;
    }
    
    public Set<String> getBlockedServices() {
        return blockedServices;
    }
    
    public boolean requiresUserConsent() {
        return requiresUserConsent;
    }
    
    /**
     * Determines if cloud processing is allowed for this decision.
     */
    public boolean allowsCloudProcessing() {
        return location == ProcessingLocation.CLOUD_PREFERRED || 
               location == ProcessingLocation.CLOUD_REQUIRED ||
               location == ProcessingLocation.HYBRID;
    }
    
    /**
     * Determines if local processing is required for this decision.
     */
    public boolean requiresLocalProcessing() {
        return location == ProcessingLocation.LOCAL_ONLY ||
               location == ProcessingLocation.HYBRID;
    }
    
    /**
     * Checks if a specific service is allowed for processing.
     */
    public boolean isServiceAllowed(String serviceName) {
        return allowedServices.contains(serviceName) && !blockedServices.contains(serviceName);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String requestId;
        private String projectId;
        private ProcessingLocation location;
        private ProcessingType type;
        private DataClassification dataClassification;
        private String reasoning;
        private LocalDateTime decidedAt = LocalDateTime.now();
        private Set<String> allowedServices = Set.of();
        private Set<String> blockedServices = Set.of();
        private boolean requiresUserConsent = false;
        
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }
        
        public Builder location(ProcessingLocation location) {
            this.location = location;
            return this;
        }
        
        public Builder type(ProcessingType type) {
            this.type = type;
            return this;
        }
        
        public Builder dataClassification(DataClassification dataClassification) {
            this.dataClassification = dataClassification;
            return this;
        }
        
        public Builder reasoning(String reasoning) {
            this.reasoning = reasoning;
            return this;
        }
        
        public Builder decidedAt(LocalDateTime decidedAt) {
            this.decidedAt = decidedAt;
            return this;
        }
        
        public Builder allowedServices(Set<String> allowedServices) {
            this.allowedServices = allowedServices;
            return this;
        }
        
        public Builder blockedServices(Set<String> blockedServices) {
            this.blockedServices = blockedServices;
            return this;
        }
        
        public Builder requiresUserConsent(boolean requiresUserConsent) {
            this.requiresUserConsent = requiresUserConsent;
            return this;
        }
        
        public ProcessingDecision build() {
            return new ProcessingDecision(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessingDecision that = (ProcessingDecision) o;
        return requiresUserConsent == that.requiresUserConsent &&
               Objects.equals(requestId, that.requestId) &&
               Objects.equals(projectId, that.projectId) &&
               location == that.location &&
               type == that.type &&
               Objects.equals(dataClassification, that.dataClassification) &&
               Objects.equals(reasoning, that.reasoning) &&
               Objects.equals(decidedAt, that.decidedAt) &&
               Objects.equals(allowedServices, that.allowedServices) &&
               Objects.equals(blockedServices, that.blockedServices);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(requestId, projectId, location, type, dataClassification, reasoning, decidedAt, allowedServices, blockedServices, requiresUserConsent);
    }
    
    @Override
    public String toString() {
        return "ProcessingDecision{" +
               "requestId='" + requestId + '\'' +
               ", projectId='" + projectId + '\'' +
               ", location=" + location +
               ", type=" + type +
               ", dataClassification=" + dataClassification +
               ", reasoning='" + reasoning + '\'' +
               ", decidedAt=" + decidedAt +
               ", allowedServices=" + allowedServices +
               ", blockedServices=" + blockedServices +
               ", requiresUserConsent=" + requiresUserConsent +
               '}';
    }
}