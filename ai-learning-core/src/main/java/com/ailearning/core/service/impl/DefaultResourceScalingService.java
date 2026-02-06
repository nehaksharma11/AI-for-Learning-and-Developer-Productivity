package com.ailearning.core.service.impl;

import com.ailearning.core.model.ResourceAllocation;
import com.ailearning.core.service.ResourceScalingService;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Default implementation of ResourceScalingService with dynamic allocation.
 */
public class DefaultResourceScalingService implements ResourceScalingService {
    
    private final Map<String, ResourceAllocation> allocations = new ConcurrentHashMap<>();
    private final Map<ResourceAllocation.ResourceType, Long> capacities = new ConcurrentHashMap<>();
    private final AtomicLong allocationIdCounter = new AtomicLong(0);
    
    // JVM monitoring beans
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    
    // Default capacities
    private static final long DEFAULT_MEMORY_CAPACITY_MB = 500; // 500MB as per requirement 10.3
    private static final long DEFAULT_CPU_CAPACITY_PERCENT = 80;
    private static final long DEFAULT_THREAD_CAPACITY = 50;
    private static final long DEFAULT_CACHE_CAPACITY_MB = 100;
    
    public DefaultResourceScalingService() {
        initializeCapacities();
    }
    
    private void initializeCapacities() {
        capacities.put(ResourceAllocation.ResourceType.MEMORY, DEFAULT_MEMORY_CAPACITY_MB);
        capacities.put(ResourceAllocation.ResourceType.CPU, DEFAULT_CPU_CAPACITY_PERCENT);
        capacities.put(ResourceAllocation.ResourceType.THREADS, DEFAULT_THREAD_CAPACITY);
        capacities.put(ResourceAllocation.ResourceType.CACHE, DEFAULT_CACHE_CAPACITY_MB);
        capacities.put(ResourceAllocation.ResourceType.DISK_SPACE, 1000L); // 1GB
        capacities.put(ResourceAllocation.ResourceType.NETWORK_BANDWIDTH, 100L); // 100 Mbps
    }
    
    @Override
    public ResourceAllocation allocateResource(ResourceAllocation.ResourceType resourceType, 
                                              long amount, 
                                              String allocatedFor) {
        return allocateResource(resourceType, amount, allocatedFor, 
                ResourceAllocation.AllocationStrategy.DYNAMIC);
    }
    
    @Override
    public ResourceAllocation allocateResource(ResourceAllocation.ResourceType resourceType,
                                              long amount,
                                              String allocatedFor,
                                              ResourceAllocation.AllocationStrategy strategy) {
        Objects.requireNonNull(resourceType, "Resource type cannot be null");
        Objects.requireNonNull(allocatedFor, "Allocated for cannot be null");
        
        long available = getAvailableCapacity(resourceType);
        long actualAmount = amount;
        
        // Apply strategy
        if (strategy == ResourceAllocation.AllocationStrategy.ADAPTIVE) {
            // Adjust amount based on current utilization
            double utilization = getUtilization(resourceType);
            if (utilization > 80.0) {
                actualAmount = Math.min(amount, available / 2); // Be conservative
            }
        } else if (strategy == ResourceAllocation.AllocationStrategy.PROPORTIONAL) {
            // Allocate proportionally to available capacity
            actualAmount = Math.min(amount, available);
        }
        
        String allocationId = generateAllocationId();
        long maxCapacity = getTotalCapacity(resourceType);
        
        ResourceAllocation allocation = ResourceAllocation.builder()
                .allocationId(allocationId)
                .resourceType(resourceType)
                .allocatedAmount(actualAmount)
                .maxAmount(maxCapacity)
                .unit(getUnitForResourceType(resourceType))
                .allocatedFor(allocatedFor)
                .strategy(strategy)
                .build();
        
        allocations.put(allocationId, allocation);
        
        return allocation;
    }
    
    @Override
    public boolean releaseResource(String allocationId) {
        Objects.requireNonNull(allocationId, "Allocation ID cannot be null");
        return allocations.remove(allocationId) != null;
    }
    
    @Override
    public Optional<ResourceAllocation> getAllocation(String allocationId) {
        Objects.requireNonNull(allocationId, "Allocation ID cannot be null");
        return Optional.ofNullable(allocations.get(allocationId));
    }
    
    @Override
    public List<ResourceAllocation> getAllAllocations() {
        return new ArrayList<>(allocations.values());
    }
    
    @Override
    public List<ResourceAllocation> getAllocationsByType(ResourceAllocation.ResourceType resourceType) {
        Objects.requireNonNull(resourceType, "Resource type cannot be null");
        
        return allocations.values().stream()
                .filter(a -> resourceType.equals(a.getResourceType()))
                .collect(Collectors.toList());
    }
    
    @Override
    public long getAvailableCapacity(ResourceAllocation.ResourceType resourceType) {
        Objects.requireNonNull(resourceType, "Resource type cannot be null");
        
        long totalCapacity = getTotalCapacity(resourceType);
        long allocated = getAllocationsByType(resourceType).stream()
                .mapToLong(ResourceAllocation::getAllocatedAmount)
                .sum();
        
        return Math.max(0, totalCapacity - allocated);
    }
    
    @Override
    public long getTotalCapacity(ResourceAllocation.ResourceType resourceType) {
        Objects.requireNonNull(resourceType, "Resource type cannot be null");
        return capacities.getOrDefault(resourceType, 0L);
    }
    
    @Override
    public double getUtilization(ResourceAllocation.ResourceType resourceType) {
        Objects.requireNonNull(resourceType, "Resource type cannot be null");
        
        long totalCapacity = getTotalCapacity(resourceType);
        if (totalCapacity == 0) {
            return 0.0;
        }
        
        long allocated = getAllocationsByType(resourceType).stream()
                .mapToLong(ResourceAllocation::getAllocatedAmount)
                .sum();
        
        return (allocated / (double) totalCapacity) * 100.0;
    }
    
    @Override
    public boolean isAvailable(ResourceAllocation.ResourceType resourceType, long amount) {
        Objects.requireNonNull(resourceType, "Resource type cannot be null");
        return getAvailableCapacity(resourceType) >= amount;
    }
    
    @Override
    public long scaleResources(ResourceAllocation.ResourceType resourceType) {
        Objects.requireNonNull(resourceType, "Resource type cannot be null");
        
        double utilization = getUtilization(resourceType);
        long currentCapacity = getTotalCapacity(resourceType);
        
        // Scale up if utilization is high
        if (utilization > 80.0) {
            long newCapacity = (long) (currentCapacity * 1.5); // Increase by 50%
            setCapacity(resourceType, newCapacity);
            return newCapacity;
        }
        
        // Scale down if utilization is low
        if (utilization < 30.0 && currentCapacity > getMinimumCapacity(resourceType)) {
            long newCapacity = (long) (currentCapacity * 0.8); // Decrease by 20%
            newCapacity = Math.max(newCapacity, getMinimumCapacity(resourceType));
            setCapacity(resourceType, newCapacity);
            return newCapacity;
        }
        
        return currentCapacity;
    }
    
    @Override
    public void setCapacity(ResourceAllocation.ResourceType resourceType, long capacity) {
        Objects.requireNonNull(resourceType, "Resource type cannot be null");
        
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }
        
        capacities.put(resourceType, capacity);
    }
    
    @Override
    public Map<ResourceAllocation.ResourceType, String> getScalingRecommendations() {
        Map<ResourceAllocation.ResourceType, String> recommendations = new HashMap<>();
        
        for (ResourceAllocation.ResourceType type : ResourceAllocation.ResourceType.values()) {
            double utilization = getUtilization(type);
            
            if (utilization > 90.0) {
                recommendations.put(type, "SCALE_UP_URGENT: Utilization at " + 
                        String.format("%.1f%%", utilization));
            } else if (utilization > 80.0) {
                recommendations.put(type, "SCALE_UP: Utilization at " + 
                        String.format("%.1f%%", utilization));
            } else if (utilization < 20.0) {
                recommendations.put(type, "SCALE_DOWN: Utilization at " + 
                        String.format("%.1f%%", utilization));
            } else {
                recommendations.put(type, "OPTIMAL: Utilization at " + 
                        String.format("%.1f%%", utilization));
            }
        }
        
        return recommendations;
    }
    
    @Override
    public Map<String, Object> getUtilizationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        for (ResourceAllocation.ResourceType type : ResourceAllocation.ResourceType.values()) {
            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("totalCapacity", getTotalCapacity(type));
            typeStats.put("availableCapacity", getAvailableCapacity(type));
            typeStats.put("utilization", getUtilization(type));
            typeStats.put("allocations", getAllocationsByType(type).size());
            
            stats.put(type.name(), typeStats);
        }
        
        stats.put("totalAllocations", allocations.size());
        stats.put("systemLoadLevel", getSystemLoadLevel());
        stats.put("underPressure", isUnderResourcePressure());
        
        return stats;
    }
    
    @Override
    public int optimizeAllocations() {
        int optimized = 0;
        
        // Find and release unused allocations
        List<String> toRelease = new ArrayList<>();
        
        for (ResourceAllocation allocation : allocations.values()) {
            // Check if allocation is old and potentially unused
            long ageSeconds = java.time.Duration.between(
                    allocation.getAllocatedAt(), 
                    java.time.LocalDateTime.now()
            ).getSeconds();
            
            if (ageSeconds > 3600) { // Older than 1 hour
                toRelease.add(allocation.getAllocationId());
            }
        }
        
        for (String allocationId : toRelease) {
            if (releaseResource(allocationId)) {
                optimized++;
            }
        }
        
        // Scale resources based on utilization
        for (ResourceAllocation.ResourceType type : ResourceAllocation.ResourceType.values()) {
            scaleResources(type);
        }
        
        return optimized;
    }
    
    @Override
    public boolean isUnderResourcePressure() {
        for (ResourceAllocation.ResourceType type : ResourceAllocation.ResourceType.values()) {
            if (getUtilization(type) > 80.0) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String getSystemLoadLevel() {
        double maxUtilization = 0.0;
        
        for (ResourceAllocation.ResourceType type : ResourceAllocation.ResourceType.values()) {
            maxUtilization = Math.max(maxUtilization, getUtilization(type));
        }
        
        if (maxUtilization > 90.0) {
            return "CRITICAL";
        } else if (maxUtilization > 75.0) {
            return "HIGH";
        } else if (maxUtilization > 50.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    private String generateAllocationId() {
        return "alloc-" + allocationIdCounter.incrementAndGet() + "-" + System.currentTimeMillis();
    }
    
    private String getUnitForResourceType(ResourceAllocation.ResourceType type) {
        switch (type) {
            case MEMORY:
            case CACHE:
            case DISK_SPACE:
                return "MB";
            case CPU:
                return "%";
            case THREADS:
                return "threads";
            case NETWORK_BANDWIDTH:
                return "Mbps";
            default:
                return "units";
        }
    }
    
    private long getMinimumCapacity(ResourceAllocation.ResourceType type) {
        switch (type) {
            case MEMORY:
                return 100L; // 100MB minimum
            case CPU:
                return 20L; // 20% minimum
            case THREADS:
                return 10L; // 10 threads minimum
            case CACHE:
                return 50L; // 50MB minimum
            case DISK_SPACE:
                return 100L; // 100MB minimum
            case NETWORK_BANDWIDTH:
                return 10L; // 10 Mbps minimum
            default:
                return 10L;
        }
    }
}
