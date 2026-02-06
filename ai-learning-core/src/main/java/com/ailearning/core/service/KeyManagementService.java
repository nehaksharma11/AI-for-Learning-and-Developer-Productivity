package com.ailearning.core.service;

import com.ailearning.core.model.EncryptionKey;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing encryption keys including generation, rotation, and lifecycle management.
 */
public interface KeyManagementService {
    
    /**
     * Generates a new encryption key.
     * 
     * @param keyType The type of key to generate
     * @param purpose The purpose of the key (e.g., "data-encryption", "communication")
     * @return The generated encryption key metadata
     * @throws KeyManagementException if key generation fails
     */
    EncryptionKey generateKey(EncryptionKey.KeyType keyType, String purpose) throws KeyManagementException;
    
    /**
     * Retrieves an encryption key by its ID.
     * 
     * @param keyId The key identifier
     * @return The encryption key metadata, if found
     */
    Optional<EncryptionKey> getKey(String keyId);
    
    /**
     * Retrieves the actual key material for encryption/decryption operations.
     * This method should be used carefully and the returned key should be cleared from memory after use.
     * 
     * @param keyId The key identifier
     * @return The key material as bytes, if found
     * @throws KeyManagementException if key retrieval fails
     */
    Optional<byte[]> getKeyMaterial(String keyId) throws KeyManagementException;
    
    /**
     * Rotates an encryption key by generating a new key and marking the old one for retirement.
     * 
     * @param keyId The ID of the key to rotate
     * @return The new encryption key metadata
     * @throws KeyManagementException if key rotation fails
     */
    EncryptionKey rotateKey(String keyId) throws KeyManagementException;
    
    /**
     * Revokes an encryption key, making it unusable for future operations.
     * 
     * @param keyId The ID of the key to revoke
     * @param reason The reason for revocation
     * @throws KeyManagementException if key revocation fails
     */
    void revokeKey(String keyId, String reason) throws KeyManagementException;
    
    /**
     * Lists all encryption keys, optionally filtered by status.
     * 
     * @param status The key status to filter by, or null for all keys
     * @return List of encryption key metadata
     */
    List<EncryptionKey> listKeys(EncryptionKey.KeyStatus status);
    
    /**
     * Gets the currently active key for a specific purpose.
     * 
     * @param purpose The purpose of the key
     * @return The active encryption key metadata, if found
     */
    Optional<EncryptionKey> getActiveKey(String purpose);
    
    /**
     * Checks if a key needs rotation based on age, usage, or policy.
     * 
     * @param keyId The key identifier
     * @return True if the key needs rotation
     */
    boolean needsRotation(String keyId);
    
    /**
     * Updates the last used timestamp for a key.
     * 
     * @param keyId The key identifier
     * @throws KeyManagementException if the update fails
     */
    void updateLastUsed(String keyId) throws KeyManagementException;
    
    /**
     * Schedules automatic key rotation for a key.
     * 
     * @param keyId The key identifier
     * @param rotationIntervalDays The number of days between rotations
     * @throws KeyManagementException if scheduling fails
     */
    void scheduleRotation(String keyId, int rotationIntervalDays) throws KeyManagementException;
    
    /**
     * Performs cleanup of expired and revoked keys according to retention policy.
     * 
     * @return The number of keys cleaned up
     */
    int cleanupExpiredKeys();
    
    /**
     * Validates the integrity and security of stored keys.
     * 
     * @return True if all keys pass validation
     */
    boolean validateKeyIntegrity();
    
    /**
     * Exception thrown when key management operations fail.
     */
    class KeyManagementException extends Exception {
        public KeyManagementException(String message) {
            super(message);
        }
        
        public KeyManagementException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}