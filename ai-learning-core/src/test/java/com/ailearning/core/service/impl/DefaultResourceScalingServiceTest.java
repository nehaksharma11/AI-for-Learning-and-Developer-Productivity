package com.ailearning.core.service.impl;

import com.ailearning.core.model.ResourceAllocation;
import com.ailearning.core.service.ResourceScalingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultResourceScalingService.
 */
class DefaultResourceScalingServiceTest {
    
    private ResourceScalingService service;
    
    @BeforeEach
    void setUp() {
        service = new DefaultResourceScalingService();
    }
    
    @Test
    void testAllocateResource() {
        ResourceAllocation allocation = service.allocateResource(
                ResourceAllocation.ResourceType.MEMORY,
                100L,
                "Test operation"
        );
        
        assertNotNull(allocation);
        assertNotNull(allocation.getAllocationId());
        assertEquals(ResourceAllocation.ResourceType.MEMORY, allocation.getResourceType());
        assertEquals(100L, allocation.getAllocatedAmount());
        assertEquals("Test operation", allocation.getAllocatedFor());
    }
    
    @Test
    void testAllocateResourceWithStrategy() {
        ResourceAllocation allocation = service.allocateResource(
                ResourceAllocation.ResourceType.CPU,
                50L,
                "CPU intensive task",
                ResourceAllocation.AllocationStrategy.ADAPTIVE
        );
        
        assertNotNull(allocation);
        assertEquals(ResourceAllocation.AllocationStrategy.ADAPTIVE, allocation.getStrategy());
    }
    
    @Test
    void testReleaseResource() {
        ResourceAllocation allocation = service.allocateResource(
                ResourceAllocation.ResourceType.MEMORY,
                100L,
                "Test"
        );
        
        boolean released = service.releaseResource(allocation.getAllocationId());
        assertTrue(released);
        
        Optional<ResourceAllocation> retrieved = service.getAllocation(allocation.getAllocationId());
        assertFalse(retrieved.isPresent());
    }
    
    @Test
    void testGetAllocation() {
        ResourceAllocation allocation = service.allocateResource(
                ResourceAllocation.ResourceType.THREADS,
                10L,
                "Thread pool"
        );
        
        Optional<ResourceAllocation> retrieved = service.getAllocation(allocation.getAllocationId());
        assertTrue(retrieved.isPresent());
        assertEquals(allocation.getAllocationId(), retrieved.get().getAllocationId());
    }
    
    @Test
    void testGetAllAllocations() {
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 100L, "Op1");
        service.allocateResource(ResourceAllocation.ResourceType.CPU, 50L, "Op2");
        
        List<ResourceAllocation> allocations = service.getAllAllocations();
        assertEquals(2, allocations.size());
    }
    
    @Test
    void testGetAllocationsByType() {
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 100L, "Op1");
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 200L, "Op2");
        service.allocateResource(ResourceAllocation.ResourceType.CPU, 50L, "Op3");
        
        List<ResourceAllocation> memoryAllocations = service.getAllocationsByType(
                ResourceAllocation.ResourceType.MEMORY);
        assertEquals(2, memoryAllocations.size());
        
        List<ResourceAllocation> cpuAllocations = service.getAllocationsByType(
                ResourceAllocation.ResourceType.CPU);
        assertEquals(1, cpuAllocations.size());
    }
    
    @Test
    void testGetAvailableCapacity() {
        long initialCapacity = service.getAvailableCapacity(ResourceAllocation.ResourceType.MEMORY);
        
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 100L, "Test");
        
        long afterAllocation = service.getAvailableCapacity(ResourceAllocation.ResourceType.MEMORY);
        assertEquals(initialCapacity - 100L, afterAllocation);
    }
    
    @Test
    void testGetTotalCapacity() {
        long capacity = service.getTotalCapacity(ResourceAllocation.ResourceType.MEMORY);
        assertTrue(capacity > 0);
    }
    
    @Test
    void testGetUtilization() {
        double initialUtilization = service.getUtilization(ResourceAllocation.ResourceType.MEMORY);
        
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 100L, "Test");
        
        double afterAllocation = service.getUtilization(ResourceAllocation.ResourceType.MEMORY);
        assertTrue(afterAllocation > initialUtilization);
    }
    
    @Test
    void testIsAvailable() {
        assertTrue(service.isAvailable(ResourceAllocation.ResourceType.MEMORY, 100L));
        
        // Allocate most of the capacity
        long capacity = service.getTotalCapacity(ResourceAllocation.ResourceType.MEMORY);
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, capacity - 10L, "Test");
        
        assertFalse(service.isAvailable(ResourceAllocation.ResourceType.MEMORY, 100L));
        assertTrue(service.isAvailable(ResourceAllocation.ResourceType.MEMORY, 5L));
    }
    
    @Test
    void testScaleResources() {
        // Allocate resources to trigger high utilization
        long capacity = service.getTotalCapacity(ResourceAllocation.ResourceType.MEMORY);
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 
                (long)(capacity * 0.85), "High load");
        
        long newCapacity = service.scaleResources(ResourceAllocation.ResourceType.MEMORY);
        assertTrue(newCapacity > capacity);
    }
    
    @Test
    void testSetCapacity() {
        service.setCapacity(ResourceAllocation.ResourceType.CACHE, 200L);
        assertEquals(200L, service.getTotalCapacity(ResourceAllocation.ResourceType.CACHE));
    }
    
    @Test
    void testGetScalingRecommendations() {
        // Allocate resources to create different utilization levels
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 450L, "High load");
        
        Map<ResourceAllocation.ResourceType, String> recommendations = 
                service.getScalingRecommendations();
        
        assertNotNull(recommendations);
        assertTrue(recommendations.containsKey(ResourceAllocation.ResourceType.MEMORY));
        
        String memoryRec = recommendations.get(ResourceAllocation.ResourceType.MEMORY);
        assertTrue(memoryRec.contains("SCALE_UP") || memoryRec.contains("OPTIMAL"));
    }
    
    @Test
    void testGetUtilizationStatistics() {
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 100L, "Test");
        
        Map<String, Object> stats = service.getUtilizationStatistics();
        
        assertNotNull(stats);
        assertTrue(stats.containsKey("MEMORY"));
        assertTrue(stats.containsKey("totalAllocations"));
        assertTrue(stats.containsKey("systemLoadLevel"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> memoryStats = (Map<String, Object>) stats.get("MEMORY");
        assertTrue(memoryStats.containsKey("totalCapacity"));
        assertTrue(memoryStats.containsKey("availableCapacity"));
        assertTrue(memoryStats.containsKey("utilization"));
    }
    
    @Test
    void testOptimizeAllocations() {
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 100L, "Test1");
        service.allocateResource(ResourceAllocation.ResourceType.CPU, 50L, "Test2");
        
        int optimized = service.optimizeAllocations();
        assertTrue(optimized >= 0);
    }
    
    @Test
    void testIsUnderResourcePressure() {
        assertFalse(service.isUnderResourcePressure());
        
        // Allocate resources to create pressure
        long capacity = service.getTotalCapacity(ResourceAllocation.ResourceType.MEMORY);
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 
                (long)(capacity * 0.85), "High load");
        
        assertTrue(service.isUnderResourcePressure());
    }
    
    @Test
    void testGetSystemLoadLevel() {
        String loadLevel = service.getSystemLoadLevel();
        assertNotNull(loadLevel);
        assertTrue(loadLevel.equals("LOW") || loadLevel.equals("MEDIUM") || 
                  loadLevel.equals("HIGH") || loadLevel.equals("CRITICAL"));
    }
    
    @Test
    void testAdaptiveAllocationStrategy() {
        // Create high utilization
        long capacity = service.getTotalCapacity(ResourceAllocation.ResourceType.MEMORY);
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 
                (long)(capacity * 0.85), "Existing load");
        
        // Try to allocate with adaptive strategy
        ResourceAllocation allocation = service.allocateResource(
                ResourceAllocation.ResourceType.MEMORY,
                100L,
                "Adaptive test",
                ResourceAllocation.AllocationStrategy.ADAPTIVE
        );
        
        // Should allocate less than requested due to high utilization
        assertTrue(allocation.getAllocatedAmount() <= 100L);
    }
    
    @Test
    void testProportionalAllocationStrategy() {
        ResourceAllocation allocation = service.allocateResource(
                ResourceAllocation.ResourceType.THREADS,
                1000L, // Request more than available
                "Proportional test",
                ResourceAllocation.AllocationStrategy.PROPORTIONAL
        );
        
        // Should allocate only what's available
        long available = service.getTotalCapacity(ResourceAllocation.ResourceType.THREADS);
        assertTrue(allocation.getAllocatedAmount() <= available);
    }
    
    @Test
    void testMultipleResourceTypes() {
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, 100L, "Memory op");
        service.allocateResource(ResourceAllocation.ResourceType.CPU, 50L, "CPU op");
        service.allocateResource(ResourceAllocation.ResourceType.THREADS, 10L, "Thread op");
        service.allocateResource(ResourceAllocation.ResourceType.CACHE, 50L, "Cache op");
        
        assertEquals(4, service.getAllAllocations().size());
        
        // Each resource type should have its own allocation
        assertEquals(1, service.getAllocationsByType(ResourceAllocation.ResourceType.MEMORY).size());
        assertEquals(1, service.getAllocationsByType(ResourceAllocation.ResourceType.CPU).size());
        assertEquals(1, service.getAllocationsByType(ResourceAllocation.ResourceType.THREADS).size());
        assertEquals(1, service.getAllocationsByType(ResourceAllocation.ResourceType.CACHE).size());
    }
    
    @Test
    void testResourceUtilizationTracking() {
        // Initial utilization should be low
        double initialUtil = service.getUtilization(ResourceAllocation.ResourceType.MEMORY);
        assertTrue(initialUtil < 50.0);
        
        // Allocate half the capacity
        long capacity = service.getTotalCapacity(ResourceAllocation.ResourceType.MEMORY);
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, capacity / 2, "Test");
        
        double midUtil = service.getUtilization(ResourceAllocation.ResourceType.MEMORY);
        assertTrue(midUtil >= 40.0 && midUtil <= 60.0);
        
        // Allocate more
        service.allocateResource(ResourceAllocation.ResourceType.MEMORY, capacity / 3, "Test2");
        
        double highUtil = service.getUtilization(ResourceAllocation.ResourceType.MEMORY);
        assertTrue(highUtil > midUtil);
    }
}
