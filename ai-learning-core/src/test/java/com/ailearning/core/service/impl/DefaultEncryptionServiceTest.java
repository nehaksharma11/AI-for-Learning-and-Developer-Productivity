package com.ailearning.core.service.impl;

import com.ailearning.core.model.EncryptedData;
import com.ailearning.core.model.EncryptionKey;
import com.ailearning.core.service.EncryptionService;
import com.ailearning.core.service.KeyManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DefaultEncryptionServiceTest {
    
    @Mock
    private KeyManagementService keyManagementService;
    
    private DefaultEncryptionService encryptionService;
    
    private static final String TEST_KEY_ID = "test-key-123";
    private static final byte[] TEST_AES_KEY = new byte[32]; // 256-bit key
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        encryptionService = new DefaultEncryptionService(keyManagementService);
        
        // Initialize test key with random bytes
        for (int i = 0; i < TEST_AES_KEY.length; i++) {
            TEST_AES_KEY[i] = (byte) (i % 256);
        }
        
        // Mock key management service
        when(keyManagementService.getKeyMaterial(TEST_KEY_ID))
                .thenReturn(Optional.of(TEST_AES_KEY.clone()));
    }
    
    @Test
    void testConstructorRequiresKeyManagementService() {
        assertThrows(NullPointerException.class, () -> 
                new DefaultEncryptionService(null));
    }
    
    @Test
    void testEncryptAndDecryptBytes() throws Exception {
        byte[] originalData = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        
        // Encrypt
        EncryptedData encrypted = encryptionService.encrypt(originalData, TEST_KEY_ID);
        
        assertNotNull(encrypted);
        assertEquals(TEST_KEY_ID, encrypted.getKeyId());
        assertEquals(EncryptedData.EncryptionAlgorithm.AES_256_GCM, encrypted.getAlgorithm());
        assertNotNull(encrypted.getEncryptedContent());
        assertNotNull(encrypted.getInitializationVector());
        assertNotNull(encrypted.getAuthenticationTag());
        assertEquals(originalData.length, encrypted.getOriginalSize());
        assertNotNull(encrypted.getChecksum());
        assertTrue(encrypted.hasInitializationVector());
        assertTrue(encrypted.hasAuthentication());
        
        // Decrypt
        byte[] decrypted = encryptionService.decrypt(encrypted);
        
        assertArrayEquals(originalData, decrypted);
        
        // Verify key usage was updated
        verify(keyManagementService, times(2)).updateLastUsed(TEST_KEY_ID);
    }
    
    @Test
    void testEncryptAndDecryptText() throws Exception {
        String originalText = "Hello, World! This is a test message.";
        
        // Encrypt
        EncryptedData encrypted = encryptionService.encryptText(originalText, TEST_KEY_ID);
        
        assertNotNull(encrypted);
        assertEquals(TEST_KEY_ID, encrypted.getKeyId());
        assertEquals(EncryptedData.EncryptionAlgorithm.AES_256_GCM, encrypted.getAlgorithm());
        
        // Decrypt
        String decrypted = encryptionService.decryptText(encrypted);
        
        assertEquals(originalText, decrypted);
    }
    
    @Test
    void testEncryptWithSpecificAlgorithm() throws Exception {
        byte[] originalData = "Test data for CBC encryption".getBytes(StandardCharsets.UTF_8);
        
        // Encrypt with AES-256-CBC
        EncryptedData encrypted = encryptionService.encryptWithAlgorithm(
                originalData, TEST_KEY_ID, EncryptedData.EncryptionAlgorithm.AES_256_CBC);
        
        assertNotNull(encrypted);
        assertEquals(TEST_KEY_ID, encrypted.getKeyId());
        assertEquals(EncryptedData.EncryptionAlgorithm.AES_256_CBC, encrypted.getAlgorithm());
        assertNotNull(encrypted.getEncryptedContent());
        assertNotNull(encrypted.getInitializationVector());
        assertNull(encrypted.getAuthenticationTag()); // CBC doesn't have auth tag
        assertTrue(encrypted.hasInitializationVector());
        assertFalse(encrypted.hasAuthentication());
        
        // Decrypt
        byte[] decrypted = encryptionService.decrypt(encrypted);
        
        assertArrayEquals(originalData, decrypted);
    }
    
    @Test
    void testEncryptWithNonExistentKey() throws Exception {
        when(keyManagementService.getKeyMaterial("nonexistent-key"))
                .thenReturn(Optional.empty());
        
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        
        EncryptionService.EncryptionException exception = assertThrows(
                EncryptionService.EncryptionException.class,
                () -> encryptionService.encrypt(data, "nonexistent-key"));
        
        assertTrue(exception.getMessage().contains("Key not found"));
    }
    
    @Test
    void testDecryptWithNonExistentKey() throws Exception {
        EncryptedData encryptedData = EncryptedData.builder()
                .dataId("test-data")
                .encryptedContent("encrypted".getBytes())
                .keyId("nonexistent-key")
                .algorithm(EncryptedData.EncryptionAlgorithm.AES_256_GCM)
                .encryptedAt(LocalDateTime.now())
                .build();
        
        when(keyManagementService.getKeyMaterial("nonexistent-key"))
                .thenReturn(Optional.empty());
        
        EncryptionService.EncryptionException exception = assertThrows(
                EncryptionService.EncryptionException.class,
                () -> encryptionService.decrypt(encryptedData));
        
        assertTrue(exception.getMessage().contains("Key not found"));
    }
    
    @Test
    void testKeyManagementServiceException() throws Exception {
        when(keyManagementService.getKeyMaterial(TEST_KEY_ID))
                .thenThrow(new KeyManagementService.KeyManagementException("Key service error"));
        
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        
        EncryptionService.EncryptionException exception = assertThrows(
                EncryptionService.EncryptionException.class,
                () -> encryptionService.encrypt(data, TEST_KEY_ID));
        
        assertTrue(exception.getMessage().contains("Failed to retrieve key"));
        assertTrue(exception.getCause() instanceof KeyManagementService.KeyManagementException);
    }
    
    @Test
    void testGenerateInitializationVector() {
        // Test AES-256-GCM IV generation
        byte[] gcmIv = encryptionService.generateInitializationVector(EncryptedData.EncryptionAlgorithm.AES_256_GCM);
        assertEquals(12, gcmIv.length); // 96 bits
        
        // Test AES-256-CBC IV generation
        byte[] cbcIv = encryptionService.generateInitializationVector(EncryptedData.EncryptionAlgorithm.AES_256_CBC);
        assertEquals(16, cbcIv.length); // 128 bits
        
        // Test ChaCha20-Poly1305 IV generation
        byte[] chachaIv = encryptionService.generateInitializationVector(EncryptedData.EncryptionAlgorithm.CHACHA20_POLY1305);
        assertEquals(12, chachaIv.length); // 96 bits
        
        // Verify IVs are different (very high probability)
        byte[] iv1 = encryptionService.generateInitializationVector(EncryptedData.EncryptionAlgorithm.AES_256_GCM);
        byte[] iv2 = encryptionService.generateInitializationVector(EncryptedData.EncryptionAlgorithm.AES_256_GCM);
        assertFalse(java.util.Arrays.equals(iv1, iv2));
    }
    
    @Test
    void testCalculateChecksum() {
        byte[] data1 = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        byte[] data3 = "Different data".getBytes(StandardCharsets.UTF_8);
        
        String checksum1 = encryptionService.calculateChecksum(data1);
        String checksum2 = encryptionService.calculateChecksum(data2);
        String checksum3 = encryptionService.calculateChecksum(data3);
        
        assertNotNull(checksum1);
        assertEquals(64, checksum1.length()); // SHA-256 produces 64 hex characters
        assertEquals(checksum1, checksum2); // Same data should produce same checksum
        assertNotEquals(checksum1, checksum3); // Different data should produce different checksum
    }
    
    @Test
    void testVerifyIntegrity() throws Exception {
        byte[] originalData = "Test data for integrity check".getBytes(StandardCharsets.UTF_8);
        
        // Encrypt data
        EncryptedData encrypted = encryptionService.encrypt(originalData, TEST_KEY_ID);
        
        // Verify integrity with correct data
        assertTrue(encryptionService.verifyIntegrity(encrypted, originalData));
        
        // Verify integrity with incorrect data
        byte[] modifiedData = "Modified test data".getBytes(StandardCharsets.UTF_8);
        assertFalse(encryptionService.verifyIntegrity(encrypted, modifiedData));
        
        // Test with encrypted data without checksum
        EncryptedData noChecksumData = EncryptedData.builder()
                .dataId("test")
                .encryptedContent("content".getBytes())
                .keyId(TEST_KEY_ID)
                .algorithm(EncryptedData.EncryptionAlgorithm.AES_256_GCM)
                .encryptedAt(LocalDateTime.now())
                .build();
        
        assertFalse(encryptionService.verifyIntegrity(noChecksumData, originalData));
    }
    
    @Test
    void testEncryptionRoundTripWithLargeData() throws Exception {
        // Test with larger data to ensure chunking works correctly
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append("This is line ").append(i).append(" of test data.\n");
        }
        
        byte[] originalData = largeData.toString().getBytes(StandardCharsets.UTF_8);
        
        // Encrypt
        EncryptedData encrypted = encryptionService.encrypt(originalData, TEST_KEY_ID);
        
        // Decrypt
        byte[] decrypted = encryptionService.decrypt(encrypted);
        
        assertArrayEquals(originalData, decrypted);
    }
    
    @Test
    void testNullInputHandling() {
        assertThrows(NullPointerException.class, () -> 
                encryptionService.encrypt(null, TEST_KEY_ID));
        
        assertThrows(NullPointerException.class, () -> 
                encryptionService.encrypt("test".getBytes(), null));
        
        assertThrows(NullPointerException.class, () -> 
                encryptionService.encryptText(null, TEST_KEY_ID));
        
        assertThrows(NullPointerException.class, () -> 
                encryptionService.decrypt(null));
        
        assertThrows(NullPointerException.class, () -> 
                encryptionService.decryptText(null));
        
        assertThrows(NullPointerException.class, () -> 
                encryptionService.encryptWithAlgorithm(null, TEST_KEY_ID, EncryptedData.EncryptionAlgorithm.AES_256_GCM));
        
        assertThrows(NullPointerException.class, () -> 
                encryptionService.generateInitializationVector(null));
        
        assertThrows(NullPointerException.class, () -> 
                encryptionService.calculateChecksum(null));
        
        assertThrows(NullPointerException.class, () -> 
                encryptionService.verifyIntegrity(null, "test".getBytes()));
        
        EncryptedData testData = EncryptedData.builder()
                .dataId("test")
                .encryptedContent("content".getBytes())
                .keyId(TEST_KEY_ID)
                .algorithm(EncryptedData.EncryptionAlgorithm.AES_256_GCM)
                .encryptedAt(LocalDateTime.now())
                .build();
        
        assertThrows(NullPointerException.class, () -> 
                encryptionService.verifyIntegrity(testData, null));
    }
}