package com.ailearning.core.service.impl;

import com.ailearning.core.model.EncryptedData;
import com.ailearning.core.service.EncryptionService;
import com.ailearning.core.service.KeyManagementService;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of EncryptionService using AES-256-GCM for symmetric encryption.
 */
public class DefaultEncryptionService implements EncryptionService {
    
    private final KeyManagementService keyManagementService;
    private final SecureRandom secureRandom;
    
    // AES-GCM parameters
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    
    public DefaultEncryptionService(KeyManagementService keyManagementService) {
        this.keyManagementService = Objects.requireNonNull(keyManagementService, "Key management service cannot be null");
        this.secureRandom = new SecureRandom();
    }
    
    @Override
    public EncryptedData encrypt(byte[] data, String keyId) throws EncryptionException {
        return encryptWithAlgorithm(data, keyId, EncryptedData.EncryptionAlgorithm.AES_256_GCM);
    }
    
    @Override
    public EncryptedData encryptText(String text, String keyId) throws EncryptionException {
        Objects.requireNonNull(text, "Text cannot be null");
        return encrypt(text.getBytes(StandardCharsets.UTF_8), keyId);
    }
    
    @Override
    public byte[] decrypt(EncryptedData encryptedData) throws EncryptionException {
        Objects.requireNonNull(encryptedData, "Encrypted data cannot be null");
        
        try {
            // Get the key material
            Optional<byte[]> keyMaterial = keyManagementService.getKeyMaterial(encryptedData.getKeyId());
            if (keyMaterial.isEmpty()) {
                throw new EncryptionException("Key not found: " + encryptedData.getKeyId());
            }
            
            byte[] key = keyMaterial.get();
            try {
                return decryptWithAlgorithm(encryptedData, key);
            } finally {
                // Clear key from memory
                clearByteArray(key);
            }
            
        } catch (KeyManagementService.KeyManagementException e) {
            throw new EncryptionException("Failed to retrieve key for decryption", e);
        }
    }
    
    @Override
    public String decryptText(EncryptedData encryptedData) throws EncryptionException {
        byte[] decryptedData = decrypt(encryptedData);
        try {
            return new String(decryptedData, StandardCharsets.UTF_8);
        } finally {
            // Clear decrypted data from memory
            clearByteArray(decryptedData);
        }
    }
    
    @Override
    public EncryptedData encryptWithAlgorithm(byte[] data, String keyId, EncryptedData.EncryptionAlgorithm algorithm) throws EncryptionException {
        Objects.requireNonNull(data, "Data cannot be null");
        Objects.requireNonNull(keyId, "Key ID cannot be null");
        Objects.requireNonNull(algorithm, "Algorithm cannot be null");
        
        try {
            // Get the key material
            Optional<byte[]> keyMaterial = keyManagementService.getKeyMaterial(keyId);
            if (keyMaterial.isEmpty()) {
                throw new EncryptionException("Key not found: " + keyId);
            }
            
            byte[] key = keyMaterial.get();
            try {
                return encryptWithKey(data, keyId, key, algorithm);
            } finally {
                // Clear key from memory
                clearByteArray(key);
            }
            
        } catch (KeyManagementService.KeyManagementException e) {
            throw new EncryptionException("Failed to retrieve key for encryption", e);
        }
    }
    
    @Override
    public byte[] generateInitializationVector(EncryptedData.EncryptionAlgorithm algorithm) {
        Objects.requireNonNull(algorithm, "Algorithm cannot be null");
        
        int ivLength;
        switch (algorithm) {
            case AES_256_GCM:
                ivLength = GCM_IV_LENGTH;
                break;
            case AES_256_CBC:
                ivLength = 16; // 128 bits
                break;
            case CHACHA20_POLY1305:
                ivLength = 12; // 96 bits
                break;
            default:
                ivLength = 16; // Default to 128 bits
        }
        
        byte[] iv = new byte[ivLength];
        secureRandom.nextBytes(iv);
        return iv;
    }
    
    @Override
    public String calculateChecksum(byte[] data) {
        Objects.requireNonNull(data, "Data cannot be null");
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }
    
    @Override
    public boolean verifyIntegrity(EncryptedData encryptedData, byte[] originalData) {
        Objects.requireNonNull(encryptedData, "Encrypted data cannot be null");
        Objects.requireNonNull(originalData, "Original data cannot be null");
        
        if (encryptedData.getChecksum() == null) {
            return false;
        }
        
        String calculatedChecksum = calculateChecksum(originalData);
        return encryptedData.getChecksum().equals(calculatedChecksum);
    }
    
    private EncryptedData encryptWithKey(byte[] data, String keyId, byte[] key, EncryptedData.EncryptionAlgorithm algorithm) throws EncryptionException {
        try {
            switch (algorithm) {
                case AES_256_GCM:
                    return encryptAesGcm(data, keyId, key);
                case AES_256_CBC:
                    return encryptAesCbc(data, keyId, key);
                default:
                    throw new EncryptionException("Unsupported encryption algorithm: " + algorithm);
            }
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }
    
    private EncryptedData encryptAesGcm(byte[] data, String keyId, byte[] key) throws Exception {
        // Generate IV
        byte[] iv = generateInitializationVector(EncryptedData.EncryptionAlgorithm.AES_256_GCM);
        
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        
        // Encrypt data
        byte[] encryptedData = cipher.doFinal(data);
        
        // Extract authentication tag (last 16 bytes)
        byte[] ciphertext = new byte[encryptedData.length - GCM_TAG_LENGTH];
        byte[] authTag = new byte[GCM_TAG_LENGTH];
        System.arraycopy(encryptedData, 0, ciphertext, 0, ciphertext.length);
        System.arraycopy(encryptedData, ciphertext.length, authTag, 0, GCM_TAG_LENGTH);
        
        // Calculate checksum of original data
        String checksum = calculateChecksum(data);
        
        // Update key usage
        try {
            keyManagementService.updateLastUsed(keyId);
        } catch (KeyManagementService.KeyManagementException e) {
            // Log warning but don't fail encryption
        }
        
        return EncryptedData.builder()
                .dataId(UUID.randomUUID().toString())
                .encryptedContent(ciphertext)
                .initializationVector(iv)
                .authenticationTag(authTag)
                .keyId(keyId)
                .algorithm(EncryptedData.EncryptionAlgorithm.AES_256_GCM)
                .encryptedAt(LocalDateTime.now())
                .originalSize(data.length)
                .checksum(checksum)
                .build();
    }
    
    private EncryptedData encryptAesCbc(byte[] data, String keyId, byte[] key) throws Exception {
        // Generate IV
        byte[] iv = generateInitializationVector(EncryptedData.EncryptionAlgorithm.AES_256_CBC);
        
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new javax.crypto.spec.IvParameterSpec(iv));
        
        // Encrypt data
        byte[] encryptedData = cipher.doFinal(data);
        
        // Calculate checksum of original data
        String checksum = calculateChecksum(data);
        
        // Update key usage
        try {
            keyManagementService.updateLastUsed(keyId);
        } catch (KeyManagementService.KeyManagementException e) {
            // Log warning but don't fail encryption
        }
        
        return EncryptedData.builder()
                .dataId(UUID.randomUUID().toString())
                .encryptedContent(encryptedData)
                .initializationVector(iv)
                .keyId(keyId)
                .algorithm(EncryptedData.EncryptionAlgorithm.AES_256_CBC)
                .encryptedAt(LocalDateTime.now())
                .originalSize(data.length)
                .checksum(checksum)
                .build();
    }
    
    private byte[] decryptWithAlgorithm(EncryptedData encryptedData, byte[] key) throws EncryptionException {
        try {
            switch (encryptedData.getAlgorithm()) {
                case AES_256_GCM:
                    return decryptAesGcm(encryptedData, key);
                case AES_256_CBC:
                    return decryptAesCbc(encryptedData, key);
                default:
                    throw new EncryptionException("Unsupported decryption algorithm: " + encryptedData.getAlgorithm());
            }
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }
    
    private byte[] decryptAesGcm(EncryptedData encryptedData, byte[] key) throws Exception {
        // Reconstruct full encrypted data with auth tag
        byte[] ciphertext = encryptedData.getEncryptedContent();
        byte[] authTag = encryptedData.getAuthenticationTag();
        byte[] fullEncryptedData = new byte[ciphertext.length + authTag.length];
        System.arraycopy(ciphertext, 0, fullEncryptedData, 0, ciphertext.length);
        System.arraycopy(authTag, 0, fullEncryptedData, ciphertext.length, authTag.length);
        
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, encryptedData.getInitializationVector());
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        
        // Decrypt data
        return cipher.doFinal(fullEncryptedData);
    }
    
    private byte[] decryptAesCbc(EncryptedData encryptedData, byte[] key) throws Exception {
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new javax.crypto.spec.IvParameterSpec(encryptedData.getInitializationVector()));
        
        // Decrypt data
        return cipher.doFinal(encryptedData.getEncryptedContent());
    }
    
    private void clearByteArray(byte[] array) {
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                array[i] = 0;
            }
        }
    }
}