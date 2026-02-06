package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents resource allocation for system operations.
 */
public class ResourceAllocation {
    
    private final String allocationId;
    private final ResourceType resourceType;
    private final long allocatedAmount;
    private final long maxAmount;
    private final String unit;
    private final LocalDateTime allocatedAt;
    private final String allocatedFor;
    private final AllocationStrategy strategy;
    
    private ResourceAllocation(Builder builder) {
        this.allocationId = Objects.requireNonNull(builder.allocationId, "Allocation ID cannot be null");
        this.resourceType = Objects.requireNonNull(builder.resourceType, "Resource type cannot be null");
        this.allocatedAmount = builder.allocatedAmount;
        this.maxAmount = builder.maxAmount;
        this.unit = Objects.requireNonNull(builder.unit, "Unit cannot be null");
        this.allocatedAt = Objects.requireNonNull(builder.allocatedAt, "Allocated time cannot be null");
        this.allocatedFor = Objects.requireNonNull(builder.allocatedFor, "Allocated for cannot be null");
        this.strategy = Objects.requireNonNull(builder.strategy, "Strategy cannot be null");
    }
    
    public enum ResourceType {
        MEMORY,
        CPU,
        THREADS,
        CACHE,
        DISK_SPACE,
        NETWORK_BANDWIDTH
    }
    
    public enum AllocationStrategy {
        FIXED,
        DYNAMIC,
        ADAPTIVE,
        PROPORTIONAL
    }
    
    public String getAllocationId() {
        return allocationId;
    }
    
    public ResourceType getResourceType() {
        return resourceType;
    }
    
    public long getAllocatedAmount() {
        return allocatedAmount;
    }
    
    public long getMaxAmount() {
        return maxAmount;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }
    
    public String getAllocatedFor() {
        return allocatedFor;
    }
    
    public AllocationStrategy getStrategy() {
        return strategy;
    }
    
    public double getUtilizationPercentage() {
        if (maxAmount <= 0) {
            return 0.0;
        }
        return (allocatedAmount / (double) maxAmount) * 100.0;
    }
    
    public long getAvailableAmount() {
        return Math.max(0, maxAmount - allocatedAmount);
    }
    
    public boolean isOverAllocated() {
        return allocatedAmount > maxAmount;
    }
    
    public boolean isNearCapacity(double thresholdPercentage) {
        return getUtilizationPercentage() >= thresholdPercentage;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String allocationId;
        private ResourceType resourceType;
        private long allocatedAmount;
        private long maxAmount;
        private String unit;
        private LocalDateTime allocatedAt = LocalDateTime.now();
        private String allocatedFor;
        private AllocationStrategy strategy = AllocationStrategy.DYNAMIC;
        
        public Builder allocationId(String allocationId) {
            this.allocationId = allocationId;
            return this;
        }
        
        public Builder resourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }
        
        public Builder allocatedAmount(long allocatedAmount) {
            this.allocatedAmount = allocatedAmount;
            return this;
        }
        
        public Builder maxAmount(long maxAmount) {
            this.maxAmount = maxAmount;
            return this;
        }
        
        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }
        
        public Builder allocatedAt(LocalDateTime allocatedAt) {
            this.allocatedAt = allocatedAt;
            return this;
        }
        
        public Builder allocatedFor(String allocatedFor) {
            this.allocatedFor = allocatedFor;
            return this;
        }
        
        public Builder strategy(AllocationStrategy strategy) {
            this.strategy = strategy;
            return this;
        }
        
        public ResourceAllocation build() {
            return new ResourceAllocation(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceAllocation that = (ResourceAllocation) o;
        return allocatedAmount == that.allocatedAmount &&
               maxAmount == that.maxAmount &&
               Objects.equals(allocationId, that.allocationId) &&
               resourceType == that.resourceType &&
               Objects.equals(unit, that.unit) &&
               Objects.equals(allocatedAt, that.allocatedAt) &&
               Objects.equals(allocatedFor, that.allocatedFor) &&
               strategy == that.strategy;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(allocationId, resourceType, allocatedAmount, maxAmount, unit, allocatedAt, allocatedFor, strategy);
    }
    
    @Override
    public String toString() {
        return "ResourceAllocation{" +
               "allocationId='" + allocationId + '\'' +
               ", resourceType=" + resourceType +
               ", allocatedAmount=" + allocatedAmount +
               ", maxAmount=" + maxAmount +
               ", unit='" + unit + '\'' +
               ", allocatedFor='" + allocatedFor + '\'' +
               ", strategy=" + strategy +
               ", utilizationPercentage=" + String.format("%.1f%%", getUtilizationPercentage()) +
               ", overAllocated=" + isOverAllocated() +
               '}';
    }
}
