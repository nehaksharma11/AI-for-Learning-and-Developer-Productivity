package com.ailearning.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for AI Learning Companion.
 * Maps to application.yml properties with prefix "ailearning".
 */
@ConfigurationProperties(prefix = "ailearning")
public class AILearningProperties {
    
    private final Performance performance = new Performance();
    private final Security security = new Security();
    private final AI ai = new AI();
    private final Cache cache = new Cache();
    private final Learning learning = new Learning();
    
    public Performance getPerformance() {
        return performance;
    }
    
    public Security getSecurity() {
        return security;
    }
    
    public AI getAi() {
        return ai;
    }
    
    public Cache getCache() {
        return cache;
    }
    
    public Learning getLearning() {
        return learning;
    }
    
    /**
     * Performance-related configuration.
     */
    public static class Performance {
        private long contextUpdateThresholdMs = 500;
        private long typicalOperationThresholdMs = 100;
        private double memoryThresholdMb = 500.0;
        private double cpuThresholdPercent = 80.0;
        private int backgroundThreadPoolSize = 4;
        
        public long getContextUpdateThresholdMs() {
            return contextUpdateThresholdMs;
        }
        
        public void setContextUpdateThresholdMs(long contextUpdateThresholdMs) {
            this.contextUpdateThresholdMs = contextUpdateThresholdMs;
        }
        
        public long getTypicalOperationThresholdMs() {
            return typicalOperationThresholdMs;
        }
        
        public void setTypicalOperationThresholdMs(long typicalOperationThresholdMs) {
            this.typicalOperationThresholdMs = typicalOperationThresholdMs;
        }
        
        public double getMemoryThresholdMb() {
            return memoryThresholdMb;
        }
        
        public void setMemoryThresholdMb(double memoryThresholdMb) {
            this.memoryThresholdMb = memoryThresholdMb;
        }
        
        public double getCpuThresholdPercent() {
            return cpuThresholdPercent;
        }
        
        public void setCpuThresholdPercent(double cpuThresholdPercent) {
            this.cpuThresholdPercent = cpuThresholdPercent;
        }
        
        public int getBackgroundThreadPoolSize() {
            return backgroundThreadPoolSize;
        }
        
        public void setBackgroundThreadPoolSize(int backgroundThreadPoolSize) {
            this.backgroundThreadPoolSize = backgroundThreadPoolSize;
        }
    }
    
    /**
     * Security-related configuration.
     */
    public static class Security {
        private boolean encryptionEnabled = true;
        private String encryptionAlgorithm = "AES-256-GCM";
        private boolean tlsEnabled = true;
        private String tlsVersion = "TLSv1.3";
        private int keyRotationDays = 90;
        
        public boolean isEncryptionEnabled() {
            return encryptionEnabled;
        }
        
        public void setEncryptionEnabled(boolean encryptionEnabled) {
            this.encryptionEnabled = encryptionEnabled;
        }
        
        public String getEncryptionAlgorithm() {
            return encryptionAlgorithm;
        }
        
        public void setEncryptionAlgorithm(String encryptionAlgorithm) {
            this.encryptionAlgorithm = encryptionAlgorithm;
        }
        
        public boolean isTlsEnabled() {
            return tlsEnabled;
        }
        
        public void setTlsEnabled(boolean tlsEnabled) {
            this.tlsEnabled = tlsEnabled;
        }
        
        public String getTlsVersion() {
            return tlsVersion;
        }
        
        public void setTlsVersion(String tlsVersion) {
            this.tlsVersion = tlsVersion;
        }
        
        public int getKeyRotationDays() {
            return keyRotationDays;
        }
        
        public void setKeyRotationDays(int keyRotationDays) {
            this.keyRotationDays = keyRotationDays;
        }
    }
    
    /**
     * AI service configuration.
     */
    public static class AI {
        private boolean openaiEnabled = false;
        private String openaiApiKey = "";
        private String openaiModel = "gpt-4";
        private boolean huggingfaceEnabled = false;
        private String huggingfaceApiKey = "";
        private String huggingfaceModel = "codellama/CodeLlama-7b-hf";
        private boolean fallbackEnabled = true;
        private int timeoutSeconds = 30;
        
        public boolean isOpenaiEnabled() {
            return openaiEnabled;
        }
        
        public void setOpenaiEnabled(boolean openaiEnabled) {
            this.openaiEnabled = openaiEnabled;
        }
        
        public String getOpenaiApiKey() {
            return openaiApiKey;
        }
        
        public void setOpenaiApiKey(String openaiApiKey) {
            this.openaiApiKey = openaiApiKey;
        }
        
        public String getOpenaiModel() {
            return openaiModel;
        }
        
        public void setOpenaiModel(String openaiModel) {
            this.openaiModel = openaiModel;
        }
        
        public boolean isHuggingfaceEnabled() {
            return huggingfaceEnabled;
        }
        
        public void setHuggingfaceEnabled(boolean huggingfaceEnabled) {
            this.huggingfaceEnabled = huggingfaceEnabled;
        }
        
        public String getHuggingfaceApiKey() {
            return huggingfaceApiKey;
        }
        
        public void setHuggingfaceApiKey(String huggingfaceApiKey) {
            this.huggingfaceApiKey = huggingfaceApiKey;
        }
        
        public String getHuggingfaceModel() {
            return huggingfaceModel;
        }
        
        public void setHuggingfaceModel(String huggingfaceModel) {
            this.huggingfaceModel = huggingfaceModel;
        }
        
        public boolean isFallbackEnabled() {
            return fallbackEnabled;
        }
        
        public void setFallbackEnabled(boolean fallbackEnabled) {
            this.fallbackEnabled = fallbackEnabled;
        }
        
        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }
        
        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }
    
    /**
     * Cache configuration.
     */
    public static class Cache {
        private long maxSizeMb = 100;
        private int defaultTtlMinutes = 60;
        private String evictionPolicy = "LRU";
        
        public long getMaxSizeMb() {
            return maxSizeMb;
        }
        
        public void setMaxSizeMb(long maxSizeMb) {
            this.maxSizeMb = maxSizeMb;
        }
        
        public int getDefaultTtlMinutes() {
            return defaultTtlMinutes;
        }
        
        public void setDefaultTtlMinutes(int defaultTtlMinutes) {
            this.defaultTtlMinutes = defaultTtlMinutes;
        }
        
        public String getEvictionPolicy() {
            return evictionPolicy;
        }
        
        public void setEvictionPolicy(String evictionPolicy) {
            this.evictionPolicy = evictionPolicy;
        }
    }
    
    /**
     * Learning system configuration.
     */
    public static class Learning {
        private boolean adaptiveDifficultyEnabled = true;
        private int minSessionDurationMinutes = 5;
        private int maxSessionDurationMinutes = 60;
        private int retentionCheckDays = 7;
        private double skillConfidenceThreshold = 0.7;
        
        public boolean isAdaptiveDifficultyEnabled() {
            return adaptiveDifficultyEnabled;
        }
        
        public void setAdaptiveDifficultyEnabled(boolean adaptiveDifficultyEnabled) {
            this.adaptiveDifficultyEnabled = adaptiveDifficultyEnabled;
        }
        
        public int getMinSessionDurationMinutes() {
            return minSessionDurationMinutes;
        }
        
        public void setMinSessionDurationMinutes(int minSessionDurationMinutes) {
            this.minSessionDurationMinutes = minSessionDurationMinutes;
        }
        
        public int getMaxSessionDurationMinutes() {
            return maxSessionDurationMinutes;
        }
        
        public void setMaxSessionDurationMinutes(int maxSessionDurationMinutes) {
            this.maxSessionDurationMinutes = maxSessionDurationMinutes;
        }
        
        public int getRetentionCheckDays() {
            return retentionCheckDays;
        }
        
        public void setRetentionCheckDays(int retentionCheckDays) {
            this.retentionCheckDays = retentionCheckDays;
        }
        
        public double getSkillConfidenceThreshold() {
            return skillConfidenceThreshold;
        }
        
        public void setSkillConfidenceThreshold(double skillConfidenceThreshold) {
            this.skillConfidenceThreshold = skillConfidenceThreshold;
        }
    }
}
