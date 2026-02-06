package com.ailearning.core.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionKeyTest {
    
    @Test
    void testBuilderCreatesValidEncryptionKey() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(365);
        LocalDateTime nextRotation = now.plusDays(90);
        
        EncryptionKey key = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(now)
                .expiresAt(expiry)
                .lastUsed(now.minusHours(1))
                .algorithm("AES")
                .keySize(256)
                .purpose("data-encryption")
                .rotationRequired(false)
                .nextRotationDate(nextRotation)
                .build();
        
        assertEquals("key-123", key.getKeyId());
        assertEquals(EncryptionKey.KeyType.AES_256, key.getKeyType());
        assertEquals(EncryptionKey.KeyStatus.ACTIVE, key.getStatus());
        assertEquals(now, key.getCreatedAt());
        assertEquals(expiry, key.getExpiresAt());
        assertEquals(now.minusHours(1), key.getLastUsed());
        assertEquals("AES", key.getAlgorithm());
        assertEquals(256, key.getKeySize());
        assertEquals("data-encryption", key.getPurpose());
        assertFalse(key.isRotationRequired());
        assertEquals(nextRotation, key.getNextRotationDate());
    }
    
    @Test
    void testBuilderRequiresNonNullFields() {
        assertThrows(NullPointerException.class, () -> 
            EncryptionKey.builder().build());
        
        assertThrows(NullPointerException.class, () -> 
            EncryptionKey.builder()
                .keyId("key-123")
                .build());
        
        assertThrows(NullPointerException.class, () -> 
            EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .build());
    }
    
    @Test
    void testIsActiveForActiveKey() {
        EncryptionKey activeKey = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .algorithm("AES")
                .build();
        
        assertTrue(activeKey.isActive());
    }
    
    @Test
    void testIsActiveForInactiveKey() {
        EncryptionKey inactiveKey = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.INACTIVE)
                .createdAt(LocalDateTime.now())
                .algorithm("AES")
                .build();
        
        assertFalse(inactiveKey.isActive());
    }
    
    @Test
    void testIsActiveForExpiredKey() {
        EncryptionKey expiredKey = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().minusDays(1))
                .algorithm("AES")
                .build();
        
        assertFalse(expiredKey.isActive());
    }
    
    @Test
    void testIsExpiredForExpiredStatus() {
        EncryptionKey expiredKey = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.EXPIRED)
                .createdAt(LocalDateTime.now())
                .algorithm("AES")
                .build();
        
        assertTrue(expiredKey.isExpired());
    }
    
    @Test
    void testIsExpiredForExpiredDate() {
        EncryptionKey expiredKey = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().minusDays(1))
                .algorithm("AES")
                .build();
        
        assertTrue(expiredKey.isExpired());
    }
    
    @Test
    void testNeedsRotationForRotationRequired() {
        EncryptionKey key = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .algorithm("AES")
                .rotationRequired(true)
                .build();
        
        assertTrue(key.needsRotation());
    }
    
    @Test
    void testNeedsRotationForPendingRotationStatus() {
        EncryptionKey key = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.PENDING_ROTATION)
                .createdAt(LocalDateTime.now())
                .algorithm("AES")
                .build();
        
        assertTrue(key.needsRotation());
    }
    
    @Test
    void testNeedsRotationForPastRotationDate() {
        EncryptionKey key = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .algorithm("AES")
                .nextRotationDate(LocalDateTime.now().minusDays(1))
                .build();
        
        assertTrue(key.needsRotation());
    }
    
    @Test
    void testDoesNotNeedRotation() {
        EncryptionKey key = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .algorithm("AES")
                .rotationRequired(false)
                .nextRotationDate(LocalDateTime.now().plusDays(30))
                .build();
        
        assertFalse(key.needsRotation());
    }
    
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        EncryptionKey key1 = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(now)
                .algorithm("AES")
                .keySize(256)
                .build();
        
        EncryptionKey key2 = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(now)
                .algorithm("AES")
                .keySize(256)
                .build();
        
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }
    
    @Test
    void testToString() {
        EncryptionKey key = EncryptionKey.builder()
                .keyId("key-123")
                .keyType(EncryptionKey.KeyType.AES_256)
                .status(EncryptionKey.KeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .algorithm("AES")
                .keySize(256)
                .purpose("test")
                .build();
        
        String toString = key.toString();
        assertTrue(toString.contains("key-123"));
        assertTrue(toString.contains("AES_256"));
        assertTrue(toString.contains("ACTIVE"));
        assertTrue(toString.contains("AES"));
        assertTrue(toString.contains("256"));
        assertTrue(toString.contains("test"));
    }
    
    @Test
    void testAllKeyTypes() {
        for (EncryptionKey.KeyType keyType : EncryptionKey.KeyType.values()) {
            EncryptionKey key = EncryptionKey.builder()
                    .keyId("key-" + keyType.name())
                    .keyType(keyType)
                    .status(EncryptionKey.KeyStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .algorithm("TEST")
                    .build();
            
            assertEquals(keyType, key.getKeyType());
        }
    }
    
    @Test
    void testAllKeyStatuses() {
        for (EncryptionKey.KeyStatus status : EncryptionKey.KeyStatus.values()) {
            EncryptionKey key = EncryptionKey.builder()
                    .keyId("key-" + status.name())
                    .keyType(EncryptionKey.KeyType.AES_256)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .algorithm("AES")
                    .build();
            
            assertEquals(status, key.getStatus());
        }
    }
}