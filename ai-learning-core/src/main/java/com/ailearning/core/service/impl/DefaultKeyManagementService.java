package com.ailearning.core.service.impl;

import com.ailearning.core.model.EncryptionKey;
import com.ailearning.core.service.KeyManagementService;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of KeyManagementService with in-memory key storage.
 * In production, this should be replaced with a secure key management system (HSM, KMS, etc.).
 */
public class DefaultKeyManagementService implements KeyManagementService {
    
    private final Map<String, EncryptionKey> keyMetadata = new ConcurrentHashMap<>();
    private final Map<String, byte[]> keyMaterial = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    
    // Default rotation interval in days
    private static final int DEFAULT_ROTATION_INTERVAL_DAYS = 90;
    
    @Override
    public EncryptionKey generateKey(EncryptionKey.KeyType keyType, String purpose) throws KeyManagementException {
        Objects.requireNonNull(keyType, "Key type cannot be null");
        Objects.requireNonNull(purpose, "Purpose cannot be null");
        
        try {
            String keyId = UUID.randomUUID().toString();
            byte[] keyBytes = generateKeyMaterial(keyType);
            
            EncryptionKey key = EncryptionKey.builder()
                    .keyId(keyId)
                    .keyType(keyType)
                    .status(EncryptionKey.KeyStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(365)) // 1 year expiration
                    .algorithm(getAlgorithmForKeyType(keyType))
                    .keySize(getKeySizeForKeyType(keyType))
                    .purpose(purpose)
                    .nextRotationDate(LocalDateTime.now().plusDays(DEFAULT_ROTATION_INTERVAL_DAYS))
                    .build();
            
            keyMetadata.put(keyId, key);
            keyMaterial.put(keyId, keyBytes);
            
            return key;
            
        } catch (Exception e) {
            throw new KeyManagementException("Failed to generate key", e);
        }
    }
    
    @Override
    public Optional<EncryptionKey> getKey(String keyId) {
        Objects.requireNonNull(keyId, "Key ID cannot be null");
        return Optional.ofNullable(keyMetadata.get(keyId));
    }
    
    @Override
    public Optional<byte[]> getKeyMaterial(String keyId) throws KeyManagementException {
        Objects.requireNonNull(keyId, "Key ID cannot be null");
        
        EncryptionKey key = keyMetadata.get(keyId);
        if (key == null) {
            return Optional.empty();
        }
        
        // Check if key is active and not expired
        if (!key.isActive()) {
            throw new KeyManagementException("Key is not active: " + keyId);
        }
        
        byte[] material = keyMaterial.get(keyId);
        if (material == null) {
            throw new KeyManagementException("Key material not found: " + keyId);
        }
        
        // Return a copy to prevent modification
        return Optional.of(material.clone());
    }
    
    @Override
    public EncryptionKey rotateKey(String keyId) throws KeyManagementException {
        Objects.requireNonNull(keyId, "Key ID cannot be null");
        
        EncryptionKey oldKey = keyMetadata.get(keyId);
        if (oldKey == null) {
            throw new KeyManagementException("Key not found: " + keyId);
        }
        
        // Generate new key with same properties
        EncryptionKey newKey = generateKey(oldKey.getKeyType(), oldKey.getPurpose());
        
        // Mark old key as pending rotation
        EncryptionKey updatedOldKey = EncryptionKey.builder()
                .keyId(oldKey.getKeyId())
                .keyType(oldKey.getKeyType())
                .status(EncryptionKey.KeyStatus.PENDING_ROTATION)
                .createdAt(oldKey.getCreatedAt())
                .expiresAt(oldKey.getExpiresAt())
                .lastUsed(oldKey.getLastUsed())
                .algorithm(oldKey.getAlgorithm())
                .keySize(oldKey.getKeySize())
                .purpose(oldKey.getPurpose())
                .rotationRequired(false)
                .nextRotationDate(LocalDateTime.now().plusDays(DEFAULT_ROTATION_INTERVAL_DAYS))
                .build();
        
        keyMetadata.put(oldKey.getKeyId(), updatedOldKey);
        
        return newKey;
    }
    
    @Override
    public void revokeKey(String keyId, String reason) throws KeyManagementException {
        Objects.requireNonNull(keyId, "Key ID cannot be null");
        
        EncryptionKey key = keyMetadata.get(keyId);
        if (key == null) {
            throw new KeyManagementException("Key not found: " + keyId);
        }
        
        EncryptionKey revokedKey = EncryptionKey.builder()
                .keyId(key.getKeyId())
                .keyType(key.getKeyType())
                .status(EncryptionKey.KeyStatus.REVOKED)
                .createdAt(key.getCreatedAt())
                .expiresAt(key.getExpiresAt())
                .lastUsed(key.getLastUsed())
                .algorithm(key.getAlgorithm())
                .keySize(key.getKeySize())
                .purpose(key.getPurpose() + " (REVOKED: " + reason + ")")
                .rotationRequired(false)
                .nextRotationDate(key.getNextRotationDate())
                .build();
        
        keyMetadata.put(keyId, revokedKey);
        
        // Clear key material for revoked keys
        byte[] material = keyMaterial.remove(keyId);
        if (material != null) {
            clearByteArray(material);
        }
    }
    
    @Override
    public List<EncryptionKey> listKeys(EncryptionKey.KeyStatus status) {
        if (status == null) {
            return new ArrayList<>(keyMetadata.values());
        }
        
        return keyMetadata.values().stream()
                .filter(key -> key.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<EncryptionKey> getActiveKey(String purpose) {
        Objects.requireNonNull(purpose, "Purpose cannot be null");
        
        return keyMetadata.values().stream()
                .filter(key -> key.isActive() && purpose.equals(key.getPurpose()))
                .findFirst();
    }
    
    @Override
    public boolean needsRotation(String keyId) {
        Objects.requireNonNull(keyId, "Key ID cannot be null");
        
        EncryptionKey key = keyMetadata.get(keyId);
        return key != null && key.needsRotation();
    }
    
    @Override
    public void updateLastUsed(String keyId) throws KeyManagementException {
        Objects.requireNonNull(keyId, "Key ID cannot be null");
        
        EncryptionKey key = keyMetadata.get(keyId);
        if (key == null) {
            throw new KeyManagementException("Key not found: " + keyId);
        }
        
        EncryptionKey updatedKey = EncryptionKey.builder()
                .keyId(key.getKeyId())
                .keyType(key.getKeyType())
                .status(key.getStatus())
                .createdAt(key.getCreatedAt())
                .expiresAt(key.getExpiresAt())
                .lastUsed(LocalDateTime.now())
                .algorithm(key.getAlgorithm())
                .keySize(key.getKeySize())
                .purpose(key.getPurpose())
                .rotationRequired(key.isRotationRequired())
                .nextRotationDate(key.getNextRotationDate())
                .build();
        
        keyMetadata.put(keyId, updatedKey);
    }
    
    @Override
    public void scheduleRotation(String keyId, int rotationIntervalDays) throws KeyManagementException {
        Objects.requireNonNull(keyId, "Key ID cannot be null");
        
        if (rotationIntervalDays <= 0) {
            throw new KeyManagementException("Rotation interval must be positive");
        }
        
        EncryptionKey key = keyMetadata.get(keyId);
        if (key == null) {
            throw new KeyManagementException("Key not found: " + keyId);
        }
        
        EncryptionKey updatedKey = EncryptionKey.builder()
                .keyId(key.getKeyId())
                .keyType(key.getKeyType())
                .status(key.getStatus())
                .createdAt(key.getCreatedAt())
                .expiresAt(key.getExpiresAt())
                .lastUsed(key.getLastUsed())
                .algorithm(key.getAlgorithm())
                .keySize(key.getKeySize())
                .purpose(key.getPurpose())
                .rotationRequired(false)
                .nextRotationDate(LocalDateTime.now().plusDays(rotationIntervalDays))
                .build();
        
        keyMetadata.put(keyId, updatedKey);
    }
    
    @Override
    public int cleanupExpiredKeys() {
        LocalDateTime now = LocalDateTime.now();
        int cleanedUp = 0;
        
        Iterator<Map.Entry<String, EncryptionKey>> iterator = keyMetadata.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, EncryptionKey> entry = iterator.next();
            EncryptionKey key = entry.getValue();
            
            // Clean up keys that are expired for more than 30 days or revoked for more than 7 days
            boolean shouldCleanup = false;
            if (key.getStatus() == EncryptionKey.KeyStatus.EXPIRED && 
                key.getExpiresAt() != null && 
                key.getExpiresAt().plusDays(30).isBefore(now)) {
                shouldCleanup = true;
            } else if (key.getStatus() == EncryptionKey.KeyStatus.REVOKED) {
                // For revoked keys, we assume they were revoked recently if no specific date is available
                shouldCleanup = true;
            }
            
            if (shouldCleanup) {
                iterator.remove();
                byte[] material = keyMaterial.remove(entry.getKey());
                if (material != null) {
                    clearByteArray(material);
                }
                cleanedUp++;
            }
        }
        
        return cleanedUp;
    }
    
    @Override
    public boolean validateKeyIntegrity() {
        // Check that all keys in metadata have corresponding key material (except revoked keys)
        for (EncryptionKey key : keyMetadata.values()) {
            if (key.getStatus() != EncryptionKey.KeyStatus.REVOKED) {
                if (!keyMaterial.containsKey(key.getKeyId())) {
                    return false;
                }
            }
        }
        
        // Check that all key material has corresponding metadata
        for (String keyId : keyMaterial.keySet()) {
            if (!keyMetadata.containsKey(keyId)) {
                return false;
            }
        }
        
        return true;
    }
    
    private byte[] generateKeyMaterial(EncryptionKey.KeyType keyType) throws NoSuchAlgorithmException {
        switch (keyType) {
            case AES_256:
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256, secureRandom);
                SecretKey secretKey = keyGen.generateKey();
                return secretKey.getEncoded();
                
            case RSA_2048:
            case RSA_4096:
                // For RSA keys, we would typically generate a key pair
                // For simplicity, we'll generate random bytes of appropriate size
                int rsaKeySize = keyType == EncryptionKey.KeyType.RSA_2048 ? 256 : 512; // bytes
                byte[] rsaKey = new byte[rsaKeySize];
                secureRandom.nextBytes(rsaKey);
                return rsaKey;
                
            case ECDSA_P256:
                byte[] ecdsaP256Key = new byte[32]; // 256 bits
                secureRandom.nextBytes(ecdsaP256Key);
                return ecdsaP256Key;
                
            case ECDSA_P384:
                byte[] ecdsaP384Key = new byte[48]; // 384 bits
                secureRandom.nextBytes(ecdsaP384Key);
                return ecdsaP384Key;
                
            default:
                throw new NoSuchAlgorithmException("Unsupported key type: " + keyType);
        }
    }
    
    private String getAlgorithmForKeyType(EncryptionKey.KeyType keyType) {
        switch (keyType) {
            case AES_256:
                return "AES";
            case RSA_2048:
            case RSA_4096:
                return "RSA";
            case ECDSA_P256:
            case ECDSA_P384:
                return "ECDSA";
            default:
                return "UNKNOWN";
        }
    }
    
    private int getKeySizeForKeyType(EncryptionKey.KeyType keyType) {
        switch (keyType) {
            case AES_256:
                return 256;
            case RSA_2048:
                return 2048;
            case RSA_4096:
                return 4096;
            case ECDSA_P256:
                return 256;
            case ECDSA_P384:
                return 384;
            default:
                return 0;
        }
    }
    
    private void clearByteArray(byte[] array) {
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                array[i] = 0;
            }
        }
    }
}