package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a cached entry with metadata for cache management.
 */
public class CacheEntry<T> {
    
    private final String key;
    private final T value;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastAccessedAt;
    private final LocalDateTime expiresAt;
    private final long sizeBytes;
    private final int accessCount;
    private final CachePriority priority;
    
    private CacheEntry(Builder<T> builder) {
        this.key = Objects.requireNonNull(builder.key, "Key cannot be null");
        this.value = builder.value;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created time cannot be null");
        this.lastAccessedAt = Objects.requireNonNull(builder.lastAccessedAt, "Last accessed time cannot be null");
        this.expiresAt = builder.expiresAt;
        this.sizeBytes = builder.sizeBytes;
        this.accessCount = builder.accessCount;
        this.priority = Objects.requireNonNull(builder.priority, "Priority cannot be null");
    }
    
    public enum CachePriority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }
    
    public String getKey() {
        return key;
    }
    
    public T getValue() {
        return value;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public long getSizeBytes() {
        return sizeBytes;
    }
    
    public int getAccessCount() {
        return accessCount;
    }
    
    public CachePriority getPriority() {
        return priority;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public long getAgeSeconds() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).getSeconds();
    }
    
    public long getTimeSinceLastAccessSeconds() {
        return java.time.Duration.between(lastAccessedAt, LocalDateTime.now()).getSeconds();
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    public static class Builder<T> {
        private String key;
        private T value;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime lastAccessedAt = LocalDateTime.now();
        private LocalDateTime expiresAt;
        private long sizeBytes = 0;
        private int accessCount = 0;
        private CachePriority priority = CachePriority.NORMAL;
        
        public Builder<T> key(String key) {
            this.key = key;
            return this;
        }
        
        public Builder<T> value(T value) {
            this.value = value;
            return this;
        }
        
        public Builder<T> createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder<T> lastAccessedAt(LocalDateTime lastAccessedAt) {
            this.lastAccessedAt = lastAccessedAt;
            return this;
        }
        
        public Builder<T> expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }
        
        public Builder<T> sizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
            return this;
        }
        
        public Builder<T> accessCount(int accessCount) {
            this.accessCount = accessCount;
            return this;
        }
        
        public Builder<T> priority(CachePriority priority) {
            this.priority = priority;
            return this;
        }
        
        public CacheEntry<T> build() {
            return new CacheEntry<>(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheEntry<?> that = (CacheEntry<?>) o;
        return sizeBytes == that.sizeBytes &&
               accessCount == that.accessCount &&
               Objects.equals(key, that.key) &&
               Objects.equals(value, that.value) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(lastAccessedAt, that.lastAccessedAt) &&
               Objects.equals(expiresAt, that.expiresAt) &&
               priority == that.priority;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(key, value, createdAt, lastAccessedAt, expiresAt, sizeBytes, accessCount, priority);
    }
    
    @Override
    public String toString() {
        return "CacheEntry{" +
               "key='" + key + '\'' +
               ", sizeBytes=" + sizeBytes +
               ", accessCount=" + accessCount +
               ", priority=" + priority +
               ", ageSeconds=" + getAgeSeconds() +
               ", expired=" + isExpired() +
               '}';
    }
}
