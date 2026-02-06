package com.ailearning.core.service;

import com.ailearning.core.model.CacheEntry;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Service for managing cached data with intelligent eviction strategies.
 */
public interface CacheService {
    
    /**
     * Puts a value in the cache.
     * 
     * @param key The cache key
     * @param value The value to cache
     * @param <T> The value type
     */
    <T> void put(String key, T value);
    
    /**
     * Puts a value in the cache with expiration.
     * 
     * @param key The cache key
     * @param value The value to cache
     * @param ttl Time to live
     * @param <T> The value type
     */
    <T> void put(String key, T value, Duration ttl);
    
    /**
     * Puts a value in the cache with priority.
     * 
     * @param key The cache key
     * @param value The value to cache
     * @param priority Cache priority
     * @param <T> The value type
     */
    <T> void put(String key, T value, CacheEntry.CachePriority priority);
    
    /**
     * Gets a value from the cache.
     * 
     * @param key The cache key
     * @param <T> The value type
     * @return The cached value, if present
     */
    <T> Optional<T> get(String key);
    
    /**
     * Gets a value from the cache or computes it if not present.
     * 
     * @param key The cache key
     * @param supplier Supplier to compute the value if not cached
     * @param <T> The value type
     * @return The cached or computed value
     */
    <T> T getOrCompute(String key, Supplier<T> supplier);
    
    /**
     * Gets a value from the cache or computes it with TTL.
     * 
     * @param key The cache key
     * @param supplier Supplier to compute the value if not cached
     * @param ttl Time to live for the computed value
     * @param <T> The value type
     * @return The cached or computed value
     */
    <T> T getOrCompute(String key, Supplier<T> supplier, Duration ttl);
    
    /**
     * Removes a value from the cache.
     * 
     * @param key The cache key
     * @return True if the value was removed
     */
    boolean remove(String key);
    
    /**
     * Clears all entries from the cache.
     */
    void clear();
    
    /**
     * Clears expired entries from the cache.
     * 
     * @return Number of entries cleared
     */
    int clearExpired();
    
    /**
     * Gets the number of entries in the cache.
     * 
     * @return Cache size
     */
    int size();
    
    /**
     * Gets the total size of cached data in bytes.
     * 
     * @return Total cache size in bytes
     */
    long getTotalSizeBytes();
    
    /**
     * Gets cache statistics.
     * 
     * @return Map containing cache statistics
     */
    java.util.Map<String, Object> getStatistics();
    
    /**
     * Gets cache hit rate.
     * 
     * @return Hit rate as a percentage (0-100)
     */
    double getHitRate();
    
    /**
     * Gets all cache keys.
     * 
     * @return List of cache keys
     */
    List<String> getKeys();
    
    /**
     * Checks if a key exists in the cache.
     * 
     * @param key The cache key
     * @return True if the key exists
     */
    boolean containsKey(String key);
    
    /**
     * Evicts least recently used entries to free space.
     * 
     * @param targetSizeBytes Target size in bytes
     * @return Number of entries evicted
     */
    int evictToSize(long targetSizeBytes);
    
    /**
     * Sets the maximum cache size.
     * 
     * @param maxSizeBytes Maximum size in bytes
     */
    void setMaxSize(long maxSizeBytes);
    
    /**
     * Gets the maximum cache size.
     * 
     * @return Maximum size in bytes
     */
    long getMaxSize();
}
