package com.ailearning.core.service.impl;

import com.ailearning.core.model.CacheEntry;
import com.ailearning.core.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultCacheService.
 */
class DefaultCacheServiceTest {
    
    private CacheService cacheService;
    
    @BeforeEach
    void setUp() {
        cacheService = new DefaultCacheService();
    }
    
    @Test
    void testPutAndGet() {
        cacheService.put("key1", "value1");
        
        Optional<String> result = cacheService.get("key1");
        assertTrue(result.isPresent());
        assertEquals("value1", result.get());
    }
    
    @Test
    void testPutWithTTL() throws Exception {
        cacheService.put("key1", "value1", Duration.ofMillis(100));
        
        Optional<String> result = cacheService.get("key1");
        assertTrue(result.isPresent());
        
        // Wait for expiration
        Thread.sleep(150);
        
        Optional<String> expired = cacheService.get("key1");
        assertFalse(expired.isPresent());
    }
    
    @Test
    void testPutWithPriority() {
        cacheService.put("key1", "value1", CacheEntry.CachePriority.HIGH);
        cacheService.put("key2", "value2", CacheEntry.CachePriority.LOW);
        
        Optional<String> result1 = cacheService.get("key1");
        Optional<String> result2 = cacheService.get("key2");
        
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
    }
    
    @Test
    void testGetOrCompute() {
        String result = cacheService.getOrCompute("key1", () -> "computed");
        assertEquals("computed", result);
        
        // Second call should return cached value
        String cached = cacheService.getOrCompute("key1", () -> "new-value");
        assertEquals("computed", cached);
    }
    
    @Test
    void testGetOrComputeWithTTL() throws Exception {
        String result = cacheService.getOrCompute("key1", () -> "computed", Duration.ofMillis(100));
        assertEquals("computed", result);
        
        // Wait for expiration
        Thread.sleep(150);
        
        // Should compute again
        String recomputed = cacheService.getOrCompute("key1", () -> "new-value", Duration.ofMillis(100));
        assertEquals("new-value", recomputed);
    }
    
    @Test
    void testRemove() {
        cacheService.put("key1", "value1");
        assertTrue(cacheService.containsKey("key1"));
        
        boolean removed = cacheService.remove("key1");
        assertTrue(removed);
        assertFalse(cacheService.containsKey("key1"));
    }
    
    @Test
    void testClear() {
        cacheService.put("key1", "value1");
        cacheService.put("key2", "value2");
        
        assertEquals(2, cacheService.size());
        
        cacheService.clear();
        assertEquals(0, cacheService.size());
    }
    
    @Test
    void testClearExpired() throws Exception {
        cacheService.put("key1", "value1", Duration.ofMillis(100));
        cacheService.put("key2", "value2", Duration.ofHours(1));
        
        Thread.sleep(150);
        
        int cleared = cacheService.clearExpired();
        assertEquals(1, cleared);
        assertEquals(1, cacheService.size());
    }
    
    @Test
    void testSize() {
        assertEquals(0, cacheService.size());
        
        cacheService.put("key1", "value1");
        assertEquals(1, cacheService.size());
        
        cacheService.put("key2", "value2");
        assertEquals(2, cacheService.size());
    }
    
    @Test
    void testGetTotalSizeBytes() {
        cacheService.put("key1", "value1");
        
        long size = cacheService.getTotalSizeBytes();
        assertTrue(size > 0);
    }
    
    @Test
    void testGetStatistics() {
        cacheService.put("key1", "value1");
        cacheService.get("key1");
        cacheService.get("key2"); // Miss
        
        Map<String, Object> stats = cacheService.getStatistics();
        
        assertNotNull(stats);
        assertTrue(stats.containsKey("size"));
        assertTrue(stats.containsKey("hits"));
        assertTrue(stats.containsKey("misses"));
        assertTrue(stats.containsKey("hitRate"));
        
        assertEquals(1, stats.get("size"));
        assertEquals(1L, stats.get("hits"));
        assertEquals(1L, stats.get("misses"));
    }
    
    @Test
    void testGetHitRate() {
        cacheService.put("key1", "value1");
        
        cacheService.get("key1"); // Hit
        cacheService.get("key1"); // Hit
        cacheService.get("key2"); // Miss
        
        double hitRate = cacheService.getHitRate();
        assertEquals(66.67, hitRate, 0.1);
    }
    
    @Test
    void testGetKeys() {
        cacheService.put("key1", "value1");
        cacheService.put("key2", "value2");
        
        List<String> keys = cacheService.getKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
    }
    
    @Test
    void testContainsKey() {
        cacheService.put("key1", "value1");
        
        assertTrue(cacheService.containsKey("key1"));
        assertFalse(cacheService.containsKey("key2"));
    }
    
    @Test
    void testEvictToSize() {
        // Set small max size
        cacheService.setMaxSize(1000);
        
        // Add entries
        for (int i = 0; i < 10; i++) {
            cacheService.put("key" + i, "value" + i);
        }
        
        int evicted = cacheService.evictToSize(500);
        assertTrue(evicted > 0);
        assertTrue(cacheService.getTotalSizeBytes() <= 500);
    }
    
    @Test
    void testSetMaxSize() {
        cacheService.setMaxSize(5000);
        assertEquals(5000, cacheService.getMaxSize());
    }
    
    @Test
    void testGetMaxSize() {
        long maxSize = cacheService.getMaxSize();
        assertTrue(maxSize > 0);
    }
    
    @Test
    void testLRUEviction() {
        cacheService.setMaxSize(1000);
        
        // Add entries
        cacheService.put("key1", "value1");
        cacheService.put("key2", "value2");
        cacheService.put("key3", "value3");
        
        // Access key1 to make it recently used
        cacheService.get("key1");
        
        // Add more entries to trigger eviction
        for (int i = 4; i < 20; i++) {
            cacheService.put("key" + i, "value" + i);
        }
        
        // key1 should still be present (recently accessed)
        // key2 and key3 might be evicted
        assertTrue(cacheService.containsKey("key1"));
    }
    
    @Test
    void testPriorityEviction() {
        cacheService.setMaxSize(1000);
        
        // Add high priority entry
        cacheService.put("high", "value", CacheEntry.CachePriority.HIGH);
        
        // Add low priority entries
        for (int i = 0; i < 10; i++) {
            cacheService.put("low" + i, "value" + i, CacheEntry.CachePriority.LOW);
        }
        
        // High priority entry should still be present
        assertTrue(cacheService.containsKey("high"));
    }
    
    @Test
    void testConcurrentAccess() throws Exception {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    cacheService.put("key" + index + "-" + j, "value" + j);
                    cacheService.get("key" + index + "-" + j);
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Should have entries from all threads
        assertTrue(cacheService.size() > 0);
    }
}
