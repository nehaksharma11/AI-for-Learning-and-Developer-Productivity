package com.ailearning.core.service;

import com.ailearning.core.model.*;
import com.ailearning.core.service.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating privacy and security features working together.
 */
class PrivacySecurityIntegrationTest {
    
    private DefaultDataClassificationService classificationService;
    private DefaultProcessingDecisionEngine decisionEngine;
    private DefaultPrivacyPreferencesService preferencesService;
    private DefaultKeyManagementService keyManagementService;
    private DefaultEncryptionService encryptionService;
    private DefaultSecureCommunicationService communicationService;
    
    @BeforeEach
    void setUp() {
        classificationService = new DefaultDataClassificationService();
        decisionEngine = new DefaultProcessingDecisionEngine();
        preferencesService = new DefaultPrivacyPreferencesService();
        keyManagementService = new DefaultKeyManagementService();
        encryptionService = new DefaultEncryptionService(keyManagementService);
        communicationService = new DefaultSecureCommunicationService();
    }
    
    @Test
    void testCompletePrivacyWorkflow() throws Exception {
        // 1. Create a project context for a sensitive project
        ProjectContext sensitiveProject = createSensitiveProjectContext();
        
        // 2. Classify the project data
        String sensitiveCode = "String apiKey = \"sk-1234567890abcdef\"; // Confidential API key";
        DataClassification classification = classificationService.classifyData(sensitiveProject, sensitiveCode);
        
        assertEquals(DataClassification.SensitivityLevel.CONFIDENTIAL, classification.getSensitivityLevel());
        assertTrue(classification.getReason().contains("Sensitive patterns detected"));
        
        // 3. Create user privacy preferences
        PrivacyPreferences preferences = preferencesService.createDefaultPreferences("user-123");
        
        // 4. Make processing decision
        ProcessingDecision decision = decisionEngine.makeDecision(
                "req-001",
                ProcessingDecision.ProcessingType.CODE_ANALYSIS,
                classification,
                preferences
        );
        
        // Should require local processing due to confidential data
        assertEquals(ProcessingDecision.ProcessingLocation.LOCAL_ONLY, decision.getLocation());
        assertTrue(decision.requiresUserConsent());
        
        // 5. Generate encryption key for secure storage
        EncryptionKey dataKey = keyManagementService.generateKey(
                EncryptionKey.KeyType.AES_256, 
                "sensitive-data-encryption"
        );
        
        assertTrue(dataKey.isActive());
        assertEquals("AES", dataKey.getAlgorithm());
        assertEquals(256, dataKey.getKeySize());
        
        // 6. Encrypt the sensitive data
        EncryptedData encrypted = encryptionService.encryptText(sensitiveCode, dataKey.getKeyId());
        
        assertNotNull(encrypted);
        assertEquals(dataKey.getKeyId(), encrypted.getKeyId());
        assertTrue(encrypted.hasAuthentication());
        assertTrue(encrypted.hasInitializationVector());
        
        // 7. Verify data can be decrypted
        String decrypted = encryptionService.decryptText(encrypted);
        assertEquals(sensitiveCode, decrypted);
        
        // 8. Verify integrity
        assertTrue(encryptionService.verifyIntegrity(encrypted, sensitiveCode.getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void testUserOptOutWorkflow() throws Exception {
        // 1. Create user with default preferences
        PrivacyPreferences preferences = preferencesService.createDefaultPreferences("privacy-conscious-user");
        assertTrue(preferences.allowsCloudProcessing());
        
        // 2. User opts out of cloud processing for sensitive project
        preferences = preferencesService.optOutProject("privacy-conscious-user", "banking-system");
        assertTrue(preferences.getOptedOutProjects().contains("banking-system"));
        
        // 3. Create project classification
        DataClassification classification = classificationService.classifyProject(
                "banking-system", 
                List.of(DataClassification.DataType.SOURCE_CODE)
        );
        
        // 4. Make processing decision
        ProcessingDecision decision = decisionEngine.makeDecision(
                "req-002",
                ProcessingDecision.ProcessingType.EXPLANATION_GENERATION,
                classification,
                preferences
        );
        
        // Should enforce local processing due to user opt-out
        assertEquals(ProcessingDecision.ProcessingLocation.LOCAL_ONLY, decision.getLocation());
        assertTrue(decision.getReasoning().contains("User has opted out"));
        
        // 5. Verify cloud services are blocked
        assertFalse(decision.isServiceAllowed("openai-service"));
        assertFalse(decision.isServiceAllowed("huggingface-service"));
        assertTrue(decision.isServiceAllowed("local-analyzer"));
    }
    
    @Test
    void testGlobalOptOutWorkflow() throws Exception {
        // 1. Create user and set global opt-out
        preferencesService.createDefaultPreferences("global-opt-out-user");
        PrivacyPreferences preferences = preferencesService.setGlobalOptOut("global-opt-out-user");
        
        assertFalse(preferences.allowsCloudProcessing());
        assertFalse(preferences.allowsDataCollection());
        assertFalse(preferences.allowsTelemetry());
        assertEquals(PrivacyPreferences.OptOutScope.GLOBAL, preferences.getOptOutScope());
        assertTrue(preferences.requiresExplicitConsent());
        
        // 2. Test with any project
        DataClassification classification = classificationService.classifyProject(
                "any-project", 
                List.of(DataClassification.DataType.SOURCE_CODE)
        );
        
        // 3. Make processing decision
        ProcessingDecision decision = decisionEngine.makeDecision(
                "req-003",
                ProcessingDecision.ProcessingType.LEARNING_RECOMMENDATION,
                classification,
                preferences
        );
        
        // Should enforce local processing and require consent
        assertEquals(ProcessingDecision.ProcessingLocation.LOCAL_ONLY, decision.getLocation());
        assertTrue(decision.requiresUserConsent());
        
        // 4. Verify all cloud services are blocked
        Set<String> blockedServices = decision.getBlockedServices();
        assertTrue(blockedServices.contains("openai-service"));
        assertTrue(blockedServices.contains("huggingface-service"));
    }
    
    @Test
    void testKeyRotationWorkflow() throws Exception {
        // 1. Generate initial key
        EncryptionKey originalKey = keyManagementService.generateKey(
                EncryptionKey.KeyType.AES_256, 
                "data-encryption"
        );
        
        String originalKeyId = originalKey.getKeyId();
        
        // 2. Encrypt some data with original key
        String testData = "Sensitive information to be encrypted";
        EncryptedData encrypted = encryptionService.encryptText(testData, originalKeyId);
        
        // 3. Rotate the key
        EncryptionKey newKey = keyManagementService.rotateKey(originalKeyId);
        
        assertNotEquals(originalKeyId, newKey.getKeyId());
        assertEquals(originalKey.getKeyType(), newKey.getKeyType());
        assertEquals(originalKey.getPurpose(), newKey.getPurpose());
        
        // 4. Verify old key is marked for rotation
        EncryptionKey oldKey = keyManagementService.getKey(originalKeyId).orElseThrow();
        assertEquals(EncryptionKey.KeyStatus.PENDING_ROTATION, oldKey.getStatus());
        
        // 5. Verify we can still decrypt with old key
        String decrypted = encryptionService.decryptText(encrypted);
        assertEquals(testData, decrypted);
        
        // 6. Verify new key can encrypt new data
        EncryptedData newEncrypted = encryptionService.encryptText("New data", newKey.getKeyId());
        String newDecrypted = encryptionService.decryptText(newEncrypted);
        assertEquals("New data", newDecrypted);
    }
    
    @Test
    void testSecureCommunicationConfiguration() throws Exception {
        // 1. Configure secure communication
        SecureCommunicationService.SecureConfig config = new SecureCommunicationService.SecureConfig(
                true, // TLS enabled
                new String[]{"TLSv1.3", "TLSv1.2"},
                new String[]{"TLS_AES_256_GCM_SHA384", "TLS_AES_128_GCM_SHA256"},
                false, // Client auth not required
                null, // No keystore
                null, // No truststore
                30000, // 30 second timeout
                true // Certificate validation enabled
        );
        
        communicationService.configure(config);
        
        // 2. Verify configuration
        SecureCommunicationService.SecureConfig retrievedConfig = communicationService.getConfiguration();
        assertTrue(retrievedConfig.isTlsEnabled());
        assertArrayEquals(new String[]{"TLSv1.3", "TLSv1.2"}, retrievedConfig.getSupportedProtocols());
        assertTrue(retrievedConfig.isCertificateValidationEnabled());
        assertEquals(30000, retrievedConfig.getConnectionTimeoutMs());
        
        // 3. Test TLS functionality
        assertTrue(communicationService.isTlsWorking());
        
        // 4. Validate connection to HTTPS endpoint
        boolean isValid = communicationService.validateConnection("https://www.google.com");
        assertTrue(isValid);
    }
    
    @Test
    void testDataClassificationWithProjectStructure() {
        // 1. Create project with configuration files
        ProjectStructure structure = ProjectStructure.builder()
                .files(List.of(
                        FileNode.builder()
                                .name("application.properties")
                                .path("/config/application.properties")
                                .size(1024L)
                                .build(),
                        FileNode.builder()
                                .name("secrets.yml")
                                .path("/config/secrets.yml")
                                .size(512L)
                                .build()
                ))
                .modules(List.of())
                .relationships(List.of())
                .entryPoints(List.of())
                .build();
        
        ProjectContext project = ProjectContext.builder()
                .id("config-heavy-project")
                .structure(structure)
                .dependencies(List.of())
                .patterns(List.of())
                .conventions(List.of())
                .build();
        
        // 2. Classify project
        DataClassification classification = classificationService.classifyData(
                project, 
                "database.url=jdbc:mysql://localhost/mydb"
        );
        
        // Should be classified as internal due to config files and database URL
        assertEquals(DataClassification.SensitivityLevel.INTERNAL, classification.getSensitivityLevel());
        assertTrue(classification.getDataTypes().contains(DataClassification.DataType.SOURCE_CODE));
        assertTrue(classification.getDataTypes().contains(DataClassification.DataType.CONFIGURATION));
    }
    
    private ProjectContext createSensitiveProjectContext() {
        ProjectStructure structure = ProjectStructure.builder()
                .files(List.of(
                        FileNode.builder()
                                .name("PaymentProcessor.java")
                                .path("/src/main/java/PaymentProcessor.java")
                                .size(2048L)
                                .build()
                ))
                .modules(List.of())
                .relationships(List.of())
                .entryPoints(List.of("PaymentProcessor.java"))
                .build();
        
        return ProjectContext.builder()
                .id("payment-system")
                .structure(structure)
                .dependencies(List.of())
                .patterns(List.of())
                .conventions(List.of())
                .build();
    }
}