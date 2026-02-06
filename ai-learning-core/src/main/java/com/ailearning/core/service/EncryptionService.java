package com.ailearning.core.service;

import com.ailearning.core.model.EncryptedData;

/**
 * Service for encrypting and decrypting data using various encryption algorithms.
 */
public interface EncryptionService {
    
    /**
     * Encrypts data using AES-256-GCM encryption.
     * 
     * @param data The data to encrypt
     * @param keyId The ID of the encryption key to use
     * @return The encrypted data with metadata
     * @throws EncryptionException if encryption fails
     */
    EncryptedData encrypt(byte[] data, String keyId) throws EncryptionException;
    
    /**
     * Encrypts text data using AES-256-GCM encryption.
     * 
     * @param text The text to encrypt
     * @param keyId The ID of the encryption key to use
     * @return The encrypted data with metadata
     * @throws EncryptionException if encryption fails
     */
    EncryptedData encryptText(String text, String keyId) throws EncryptionException;
    
    /**
     * Decrypts data using the specified encryption metadata.
     * 
     * @param encryptedData The encrypted data with metadata
     * @return The decrypted data
     * @throws EncryptionException if decryption fails
     */
    byte[] decrypt(EncryptedData encryptedData) throws EncryptionException;
    
    /**
     * Decrypts data and returns it as text.
     * 
     * @param encryptedData The encrypted data with metadata
     * @return The decrypted text
     * @throws EncryptionException if decryption fails
     */
    String decryptText(EncryptedData encryptedData) throws EncryptionException;
    
    /**
     * Encrypts data with a specific algorithm.
     * 
     * @param data The data to encrypt
     * @param keyId The ID of the encryption key to use
     * @param algorithm The encryption algorithm to use
     * @return The encrypted data with metadata
     * @throws EncryptionException if encryption fails
     */
    EncryptedData encryptWithAlgorithm(byte[] data, String keyId, EncryptedData.EncryptionAlgorithm algorithm) throws EncryptionException;
    
    /**
     * Generates a secure random initialization vector for the specified algorithm.
     * 
     * @param algorithm The encryption algorithm
     * @return The initialization vector
     */
    byte[] generateInitializationVector(EncryptedData.EncryptionAlgorithm algorithm);
    
    /**
     * Calculates a checksum for the given data.
     * 
     * @param data The data to checksum
     * @return The checksum string
     */
    String calculateChecksum(byte[] data);
    
    /**
     * Verifies the integrity of encrypted data using its checksum.
     * 
     * @param encryptedData The encrypted data to verify
     * @param originalData The original data to compare against
     * @return True if the data integrity is verified
     */
    boolean verifyIntegrity(EncryptedData encryptedData, byte[] originalData);
    
    /**
     * Exception thrown when encryption or decryption operations fail.
     */
    class EncryptionException extends Exception {
        public EncryptionException(String message) {
            super(message);
        }
        
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}