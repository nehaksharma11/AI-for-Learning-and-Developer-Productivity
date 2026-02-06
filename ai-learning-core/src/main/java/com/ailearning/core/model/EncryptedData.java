package com.ailearning.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents encrypted data with metadata for secure storage and transmission.
 */
public class EncryptedData {
    
    public enum EncryptionAlgorithm {
        AES_256_GCM,
        AES_256_CBC,
        RSA_OAEP,
        CHACHA20_POLY1305
    }
    
    private final String dataId;
    private final byte[] encryptedContent;
    private final byte[] initializationVector;
    private final byte[] authenticationTag;
    private final String keyId;
    private final EncryptionAlgorithm algorithm;
    private final LocalDateTime encryptedAt;
    private final String contentType;
    private final long originalSize;
    private final String checksum;
    
    private EncryptedData(Builder builder) {
        this.dataId = Objects.requireNonNull(builder.dataId, "Data ID cannot be null");
        this.encryptedContent = Objects.requireNonNull(builder.encryptedContent, "Encrypted content cannot be null").clone();
        this.initializationVector = builder.initializationVector != null ? builder.initializationVector.clone() : null;
        this.authenticationTag = builder.authenticationTag != null ? builder.authenticationTag.clone() : null;
        this.keyId = Objects.requireNonNull(builder.keyId, "Key ID cannot be null");
        this.algorithm = Objects.requireNonNull(builder.algorithm, "Algorithm cannot be null");
        this.encryptedAt = Objects.requireNonNull(builder.encryptedAt, "Encrypted date cannot be null");
        this.contentType = builder.contentType;
        this.originalSize = builder.originalSize;
        this.checksum = builder.checksum;
    }
    
    public String getDataId() {
        return dataId;
    }
    
    public byte[] getEncryptedContent() {
        return encryptedContent.clone();
    }
    
    public byte[] getInitializationVector() {
        return initializationVector != null ? initializationVector.clone() : null;
    }
    
    public byte[] getAuthenticationTag() {
        return authenticationTag != null ? authenticationTag.clone() : null;
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    public EncryptionAlgorithm getAlgorithm() {
        return algorithm;
    }
    
    public LocalDateTime getEncryptedAt() {
        return encryptedAt;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public long getOriginalSize() {
        return originalSize;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    /**
     * Gets the size of the encrypted content.
     */
    public long getEncryptedSize() {
        return encryptedContent.length;
    }
    
    /**
     * Checks if the encrypted data includes authentication.
     */
    public boolean hasAuthentication() {
        return authenticationTag != null && authenticationTag.length > 0;
    }
    
    /**
     * Checks if the encrypted data uses an initialization vector.
     */
    public boolean hasInitializationVector() {
        return initializationVector != null && initializationVector.length > 0;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String dataId;
        private byte[] encryptedContent;
        private byte[] initializationVector;
        private byte[] authenticationTag;
        private String keyId;
        private EncryptionAlgorithm algorithm;
        private LocalDateTime encryptedAt = LocalDateTime.now();
        private String contentType;
        private long originalSize;
        private String checksum;
        
        public Builder dataId(String dataId) {
            this.dataId = dataId;
            return this;
        }
        
        public Builder encryptedContent(byte[] encryptedContent) {
            this.encryptedContent = encryptedContent;
            return this;
        }
        
        public Builder initializationVector(byte[] initializationVector) {
            this.initializationVector = initializationVector;
            return this;
        }
        
        public Builder authenticationTag(byte[] authenticationTag) {
            this.authenticationTag = authenticationTag;
            return this;
        }
        
        public Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }
        
        public Builder algorithm(EncryptionAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }
        
        public Builder encryptedAt(LocalDateTime encryptedAt) {
            this.encryptedAt = encryptedAt;
            return this;
        }
        
        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }
        
        public Builder originalSize(long originalSize) {
            this.originalSize = originalSize;
            return this;
        }
        
        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }
        
        public EncryptedData build() {
            return new EncryptedData(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncryptedData that = (EncryptedData) o;
        return originalSize == that.originalSize &&
               Objects.equals(dataId, that.dataId) &&
               Objects.deepEquals(encryptedContent, that.encryptedContent) &&
               Objects.deepEquals(initializationVector, that.initializationVector) &&
               Objects.deepEquals(authenticationTag, that.authenticationTag) &&
               Objects.equals(keyId, that.keyId) &&
               algorithm == that.algorithm &&
               Objects.equals(encryptedAt, that.encryptedAt) &&
               Objects.equals(contentType, that.contentType) &&
               Objects.equals(checksum, that.checksum);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dataId, Objects.hashCode(encryptedContent), Objects.hashCode(initializationVector), 
                Objects.hashCode(authenticationTag), keyId, algorithm, encryptedAt, contentType, originalSize, checksum);
    }
    
    @Override
    public String toString() {
        return "EncryptedData{" +
               "dataId='" + dataId + '\'' +
               ", encryptedContentSize=" + encryptedContent.length +
               ", keyId='" + keyId + '\'' +
               ", algorithm=" + algorithm +
               ", encryptedAt=" + encryptedAt +
               ", contentType='" + contentType + '\'' +
               ", originalSize=" + originalSize +
               ", hasIV=" + hasInitializationVector() +
               ", hasAuth=" + hasAuthentication() +
               '}';
    }
}