package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an encryption key with metadata for secure key management.
 */
public class EncryptionKey {
    
    public enum KeyType {
        AES_256,
        RSA_2048,
        RSA_4096,
        ECDSA_P256,
        ECDSA_P384
    }
    
    public enum KeyStatus {
        ACTIVE,
        INACTIVE,
        EXPIRED,
        REVOKED,
        PENDING_ROTATION
    }
    
    private final String keyId;
    private final KeyType keyType;
    private final KeyStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final LocalDateTime lastUsed;
    private final String algorithm;
    private final int keySize;
    private final String purpose;
    private final boolean rotationRequired;
    private final LocalDateTime nextRotationDate;
    
    private EncryptionKey(Builder builder) {
        this.keyId = Objects.requireNonNull(builder.keyId, "Key ID cannot be null");
        this.keyType = Objects.requireNonNull(builder.keyType, "Key type cannot be null");
        this.status = Objects.requireNonNull(builder.status, "Key status cannot be null");
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created date cannot be null");
        this.expiresAt = builder.expiresAt;
        this.lastUsed = builder.lastUsed;
        this.algorithm = Objects.requireNonNull(builder.algorithm, "Algorithm cannot be null");
        this.keySize = builder.keySize;
        this.purpose = builder.purpose;
        this.rotationRequired = builder.rotationRequired;
        this.nextRotationDate = builder.nextRotationDate;
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    public KeyType getKeyType() {
        return keyType;
    }
    
    public KeyStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public LocalDateTime getLastUsed() {
        return lastUsed;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public int getKeySize() {
        return keySize;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public boolean isRotationRequired() {
        return rotationRequired;
    }
    
    public LocalDateTime getNextRotationDate() {
        return nextRotationDate;
    }
    
    /**
     * Checks if the key is currently active and usable.
     */
    public boolean isActive() {
        return status == KeyStatus.ACTIVE && 
               (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
    
    /**
     * Checks if the key has expired.
     */
    public boolean isExpired() {
        return status == KeyStatus.EXPIRED || 
               (expiresAt != null && expiresAt.isBefore(LocalDateTime.now()));
    }
    
    /**
     * Checks if the key needs rotation.
     */
    public boolean needsRotation() {
        return rotationRequired || 
               status == KeyStatus.PENDING_ROTATION ||
               (nextRotationDate != null && nextRotationDate.isBefore(LocalDateTime.now()));
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String keyId;
        private KeyType keyType;
        private KeyStatus status = KeyStatus.ACTIVE;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime expiresAt;
        private LocalDateTime lastUsed;
        private String algorithm;
        private int keySize;
        private String purpose;
        private boolean rotationRequired = false;
        private LocalDateTime nextRotationDate;
        
        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }
        
        public Builder keyType(KeyType keyType) {
            this.keyType = keyType;
            return this;
        }
        
        public Builder status(KeyStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }
        
        public Builder lastUsed(LocalDateTime lastUsed) {
            this.lastUsed = lastUsed;
            return this;
        }
        
        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }
        
        public Builder keySize(int keySize) {
            this.keySize = keySize;
            return this;
        }
        
        public Builder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }
        
        public Builder rotationRequired(boolean rotationRequired) {
            this.rotationRequired = rotationRequired;
            return this;
        }
        
        public Builder nextRotationDate(LocalDateTime nextRotationDate) {
            this.nextRotationDate = nextRotationDate;
            return this;
        }
        
        public EncryptionKey build() {
            return new EncryptionKey(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncryptionKey that = (EncryptionKey) o;
        return keySize == that.keySize &&
               rotationRequired == that.rotationRequired &&
               Objects.equals(keyId, that.keyId) &&
               keyType == that.keyType &&
               status == that.status &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(expiresAt, that.expiresAt) &&
               Objects.equals(lastUsed, that.lastUsed) &&
               Objects.equals(algorithm, that.algorithm) &&
               Objects.equals(purpose, that.purpose) &&
               Objects.equals(nextRotationDate, that.nextRotationDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(keyId, keyType, status, createdAt, expiresAt, lastUsed, algorithm, keySize, purpose, rotationRequired, nextRotationDate);
    }
    
    @Override
    public String toString() {
        return "EncryptionKey{" +
               "keyId='" + keyId + '\'' +
               ", keyType=" + keyType +
               ", status=" + status +
               ", createdAt=" + createdAt +
               ", expiresAt=" + expiresAt +
               ", lastUsed=" + lastUsed +
               ", algorithm='" + algorithm + '\'' +
               ", keySize=" + keySize +
               ", purpose='" + purpose + '\'' +
               ", rotationRequired=" + rotationRequired +
               ", nextRotationDate=" + nextRotationDate +
               '}';
    }
}