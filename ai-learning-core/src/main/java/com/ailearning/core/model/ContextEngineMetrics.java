package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Performance metrics for the Context Engine.
 * Tracks analysis times, update times, cache usage, and memory consumption.
 */
public final class ContextEngineMetrics {
    
    private final double averageAnalysisTime;
    private final double averageUpdateTime;
    private final long totalAnalysisCount;
    private final long totalUpdateCount;
    private final int cacheSize;
    private final int astCacheSize;
    private final long memoryUsage;
    
    @JsonCreator
    public ContextEngineMetrics(
            @JsonProperty("averageAnalysisTime") double averageAnalysisTime,
            @JsonProperty("averageUpdateTime") double averageUpdateTime,
            @JsonProperty("totalAnalysisCount") long totalAnalysisCount,
            @JsonProperty("totalUpdateCount") long totalUpdateCount,
            @JsonProperty("cacheSize") int cacheSize,
            @JsonProperty("astCacheSize") int astCacheSize,
            @JsonProperty("memoryUsage") long memoryUsage) {
        this.averageAnalysisTime = averageAnalysisTime;
        this.averageUpdateTime = averageUpdateTime;
        this.totalAnalysisCount = totalAnalysisCount;
        this.totalUpdateCount = totalUpdateCount;
        this.cacheSize = cacheSize;
        this.astCacheSize = astCacheSize;
        this.memoryUsage = memoryUsage;
    }
    
    public double getAverageAnalysisTime() {
        return averageAnalysisTime;
    }
    
    public double getAverageUpdateTime() {
        return averageUpdateTime;
    }
    
    public long getTotalAnalysisCount() {
        return totalAnalysisCount;
    }
    
    public long getTotalUpdateCount() {
        return totalUpdateCount;
    }
    
    public int getCacheSize() {
        return cacheSize;
    }
    
    public int getAstCacheSize() {
        return astCacheSize;
    }
    
    public long getMemoryUsage() {
        return memoryUsage;
    }
    
    /**
     * Creates a builder for ContextEngineMetrics.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private double averageAnalysisTime = 0.0;
        private double averageUpdateTime = 0.0;
        private long totalAnalysisCount = 0;
        private long totalUpdateCount = 0;
        private int cacheSize = 0;
        private int astCacheSize = 0;
        private long memoryUsage = 0;
        
        public Builder averageAnalysisTime(double averageAnalysisTime) {
            this.averageAnalysisTime = averageAnalysisTime;
            return this;
        }
        
        public Builder averageUpdateTime(double averageUpdateTime) {
            this.averageUpdateTime = averageUpdateTime;
            return this;
        }
        
        public Builder totalAnalysisCount(long totalAnalysisCount) {
            this.totalAnalysisCount = totalAnalysisCount;
            return this;
        }
        
        public Builder totalUpdateCount(long totalUpdateCount) {
            this.totalUpdateCount = totalUpdateCount;
            return this;
        }
        
        public Builder cacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }
        
        public Builder astCacheSize(int astCacheSize) {
            this.astCacheSize = astCacheSize;
            return this;
        }
        
        public Builder memoryUsage(long memoryUsage) {
            this.memoryUsage = memoryUsage;
            return this;
        }
        
        public ContextEngineMetrics build() {
            return new ContextEngineMetrics(averageAnalysisTime, averageUpdateTime,
                    totalAnalysisCount, totalUpdateCount, cacheSize, astCacheSize, memoryUsage);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextEngineMetrics that = (ContextEngineMetrics) o;
        return Double.compare(that.averageAnalysisTime, averageAnalysisTime) == 0 &&
                Double.compare(that.averageUpdateTime, averageUpdateTime) == 0 &&
                totalAnalysisCount == that.totalAnalysisCount &&
                totalUpdateCount == that.totalUpdateCount &&
                cacheSize == that.cacheSize &&
                astCacheSize == that.astCacheSize &&
                memoryUsage == that.memoryUsage;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(averageAnalysisTime, averageUpdateTime, totalAnalysisCount,
                totalUpdateCount, cacheSize, astCacheSize, memoryUsage);
    }
    
    @Override
    public String toString() {
        return "ContextEngineMetrics{" +
                "averageAnalysisTime=" + averageAnalysisTime +
                ", averageUpdateTime=" + averageUpdateTime +
                ", totalAnalysisCount=" + totalAnalysisCount +
                ", totalUpdateCount=" + totalUpdateCount +
                ", cacheSize=" + cacheSize +
                ", astCacheSize=" + astCacheSize +
                ", memoryUsage=" + memoryUsage +
                '}';
    }
}