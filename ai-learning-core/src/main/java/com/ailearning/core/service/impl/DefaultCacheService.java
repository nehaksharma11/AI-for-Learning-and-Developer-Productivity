package com.ailearning.core.service.impl;

import com.ailearning.core.model.CacheEntry;
import com.ailearning.core.service.CacheService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Default implementation of CacheService with LRU eviction strategy.
 */
public class DefaultCacheService implements CacheService {
    
    private final Map<String, CacheEntry<Object>> cache = new ConcurrentHashMap<>();
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private long maxSizeBytes = 100 * 1024 * 1024; // 100MB default
    
    @Override
    public <T> void put(String key, T value) {
        put(key, value, Duration.ofHours(1)); // Default 1 hour TTL
    }
    
    @Override
    public <T> void put(String key, T value, Duration ttl) {
        put(key, value, CacheEntry.CachePriority.NORMAL, ttl);
    }
    
    @Override
    public <T> void put(String key, T value, CacheEntry.CachePriority priority) {
        put(key, value, priority, Duration.ofHours(1));
    }
    
    private <T> void put(String key, T value, CacheEntry.CachePriority priority, Duration ttl) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        long sizeBytes = estimateSize(value);
        LocalDateTime expiresAt = ttl != null ? LocalDateTime.now().plus(ttl) : null;
        
        CacheEntry<Object> entry = CacheEntry.builder()
                .key(key)
                .value(value)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .sizeBytes(sizeBytes)
                .accessCount(0)
                .priority(priority)
                .build();
        
        // Check if we need to evict entries
        long currentSize = getTotalSizeBytes();
        if (currentSize + sizeBytes > maxSizeBytes) {
            evictToSize(maxSizeBytes - sizeBytes);
        }
        
        cache.put(key, entry);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        CacheEntry<Object> entry = cache.get(key);
        
        if (entry == null) {
            misses.incrementAndGet();
            return Optional.empty();
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            misses.incrementAndGet();
            return Optional.empty();
        }
        
        // Update access metadata
        CacheEntry<Object> updatedEntry = CacheEntry.builder()
                .key(entry.getKey())
                .value(entry.getValue())
                .createdAt(entry.getCreatedAt())
                .lastAccessedAt(LocalDateTime.now())
                .expiresAt(entry.getExpiresAt())
                .sizeBytes(entry.getSizeBytes())
                .accessCount(entry.getAccessCount() + 1)
                .priority(entry.getPriority())
                .build();
        
        cache.put(key, updatedEntry);
        hits.incrementAndGet();
        
        return Optional.ofNullable((T) entry.getValue());
    }
    
    @Override
    public <T> T getOrCompute(String key, Supplier<T> supplier) {
        return getOrCompute(key, supplier, Duration.ofHours(1));
    }
    
    @Override
    public <T> T getOrCompute(String key, Supplier<T> supplier, Duration ttl) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(supplier, "Supplier cannot be null");
        
        Optional<T> cached = get(key);
        if (cached.isPresent()) {
            return cached.get();
        }
        
        T value = supplier.get();
        put(key, value, ttl);
        return value;
    }
    
    @Override
    public boolean remove(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return cache.remove(key) != null;
    }
    
    @Override
    public void clear() {
        cache.clear();
        hits.set(0);
        misses.set(0);
    }
    
    @Override
    public int clearExpired() {
        List<String> expiredKeys = cache.entrySet().stream()
                .filter(e -> e.getValue().isExpired())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        expiredKeys.forEach(cache::remove);
        return expiredKeys.size();
    }
    
    @Override
    public int size() {
        return cache.size();
    }
    
    @Override
    public long getTotalSizeBytes() {
        return cache.values().stream()
                .mapToLong(CacheEntry::getSizeBytes)
                .sum();
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("size", size());
        stats.put("totalSizeBytes", getTotalSizeBytes());
        stats.put("maxSizeBytes", maxSizeBytes);
        stats.put("hits", hits.get());
        stats.put("misses", misses.get());
        stats.put("hitRate", getHitRate());
        stats.put("utilizationPercentage", (getTotalSizeBytes() / (double) maxSizeBytes) * 100.0);
        
        return stats;
    }
    
    @Override
    public double getHitRate() {
        long totalRequests = hits.get() + misses.get();
        if (totalRequests == 0) {
            return 0.0;
        }
        return (hits.get() / (double) totalRequests) * 100.0;
    }
    
    @Override
    public List<String> getKeys() {
        return new ArrayList<>(cache.keySet());
    }
    
    @Override
    public boolean containsKey(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        return cache.containsKey(key) && !cache.get(key).isExpired();
    }
    
    @Override
    public int evictToSize(long targetSizeBytes) {
        if (getTotalSizeBytes() <= targetSizeBytes) {
            return 0;
        }
        
        // Sort entries by eviction priority (LRU with priority consideration)
        List<Map.Entry<String, CacheEntry<Object>>> sortedEntries = cache.entrySet().stream()
                .sorted((e1, e2) -> {
                    CacheEntry<Object> c1 = e1.getValue();
                    CacheEntry<Object> c2 = e2.getValue();
                    
                    // First by priority (lower priority evicted first)
                    int priorityCompare = c1.getPriority().compareTo(c2.getPriority());
                    if (priorityCompare != 0) {
                        return priorityCompare;
                    }
                    
                    // Then by last access time (older evicted first)
                    return c1.getLastAccessedAt().compareTo(c2.getLastAccessedAt());
                })
                .collect(Collectors.toList());
        
        int evicted = 0;
        long currentSize = getTotalSizeBytes();
        
        for (Map.Entry<String, CacheEntry<Object>> entry : sortedEntries) {
            if (currentSize <= targetSizeBytes) {
                break;
            }
            
            cache.remove(entry.getKey());
            currentSize -= entry.getValue().getSizeBytes();
            evicted++;
        }
        
        return evicted;
    }
    
    @Override
    public void setMaxSize(long maxSizeBytes) {
        if (maxSizeBytes < 0) {
            throw new IllegalArgumentException("Max size cannot be negative");
        }
        
        this.maxSizeBytes = maxSizeBytes;
        
        // Evict if current size exceeds new max
        if (getTotalSizeBytes() > maxSizeBytes) {
            evictToSize(maxSizeBytes);
        }
    }
    
    @Override
    public long getMaxSize() {
        return maxSizeBytes;
    }
    
    private long estimateSize(Object value) {
        if (value == null) {
            return 0;
        }
        
        // Simple size estimation
        if (value instanceof String) {
            return ((String) value).length() * 2L; // 2 bytes per char
        } else if (value instanceof byte[]) {
            return ((byte[]) value).length;
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).size() * 100L; // Rough estimate
        } else if (value instanceof Map) {
            return ((Map<?, ?>) value).size() * 200L; // Rough estimate
        }
        
        return 1000L; // Default estimate for unknown types
    }
}
