package com.ailearning.core.service;

import com.ailearning.core.model.ResourceAllocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for dynamic resource allocation and scaling based on system capacity.
 */
public interface ResourceScalingService {
    
    /**
     * Allocates resources for an operation.
     * 
     * @param resourceType The type of resource to allocate
     * @param amount The amount to allocate
     * @param allocatedFor Description of what the resource is for
     * @return The resource allocation
     */
    ResourceAllocation allocateResource(ResourceAllocation.ResourceType resourceType, 
                                       long amount, 
                                       String allocatedFor);
    
    /**
     * Allocates resources with a specific strategy.
     * 
     * @param resourceType The type of resource to allocate
     * @param amount The amount to allocate
     * @param allocatedFor Description of what the resource is for
     * @param strategy The allocation strategy
     * @return The resource allocation
     */
    ResourceAllocation allocateResource(ResourceAllocation.ResourceType resourceType,
                                       long amount,
                                       String allocatedFor,
                                       ResourceAllocation.AllocationStrategy strategy);
    
    /**
     * Releases allocated resources.
     * 
     * @param allocationId The allocation ID
     * @return True if resources were released
     */
    boolean releaseResource(String allocationId);
    
    /**
     * Gets a resource allocation by ID.
     * 
     * @param allocationId The allocation ID
     * @return The resource allocation, if found
     */
    Optional<ResourceAllocation> getAllocation(String allocationId);
    
    /**
     * Gets all resource allocations.
     * 
     * @return List of all allocations
     */
    List<ResourceAllocation> getAllAllocations();
    
    /**
     * Gets allocations by resource type.
     * 
     * @param resourceType The resource type
     * @return List of allocations for the resource type
     */
    List<ResourceAllocation> getAllocationsByType(ResourceAllocation.ResourceType resourceType);
    
    /**
     * Gets available capacity for a resource type.
     * 
     * @param resourceType The resource type
     * @return Available capacity
     */
    long getAvailableCapacity(ResourceAllocation.ResourceType resourceType);
    
    /**
     * Gets total capacity for a resource type.
     * 
     * @param resourceType The resource type
     * @return Total capacity
     */
    long getTotalCapacity(ResourceAllocation.ResourceType resourceType);
    
    /**
     * Gets current utilization for a resource type.
     * 
     * @param resourceType The resource type
     * @return Utilization percentage (0-100)
     */
    double getUtilization(ResourceAllocation.ResourceType resourceType);
    
    /**
     * Checks if resources are available for allocation.
     * 
     * @param resourceType The resource type
     * @param amount The amount needed
     * @return True if resources are available
     */
    boolean isAvailable(ResourceAllocation.ResourceType resourceType, long amount);
    
    /**
     * Scales resources dynamically based on current load.
     * 
     * @param resourceType The resource type to scale
     * @return The new capacity after scaling
     */
    long scaleResources(ResourceAllocation.ResourceType resourceType);
    
    /**
     * Sets the capacity for a resource type.
     * 
     * @param resourceType The resource type
     * @param capacity The new capacity
     */
    void setCapacity(ResourceAllocation.ResourceType resourceType, long capacity);
    
    /**
     * Gets resource scaling recommendations.
     * 
     * @return Map of resource types to recommended scaling actions
     */
    Map<ResourceAllocation.ResourceType, String> getScalingRecommendations();
    
    /**
     * Gets resource utilization statistics.
     * 
     * @return Map containing utilization statistics
     */
    Map<String, Object> getUtilizationStatistics();
    
    /**
     * Optimizes resource allocation across all operations.
     * 
     * @return Number of allocations optimized
     */
    int optimizeAllocations();
    
    /**
     * Checks if the system is under resource pressure.
     * 
     * @return True if any resource is near capacity
     */
    boolean isUnderResourcePressure();
    
    /**
     * Gets the current system load level.
     * 
     * @return Load level ("LOW", "MEDIUM", "HIGH", "CRITICAL")
     */
    String getSystemLoadLevel();
}
