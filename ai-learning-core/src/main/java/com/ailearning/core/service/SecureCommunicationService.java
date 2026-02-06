package com.ailearning.core.service;

import java.util.Map;

/**
 * Service for secure communication including TLS configuration and certificate management.
 */
public interface SecureCommunicationService {
    
    /**
     * Configuration for secure communication settings.
     */
    class SecureConfig {
        private final boolean tlsEnabled;
        private final String[] supportedProtocols;
        private final String[] supportedCipherSuites;
        private final boolean clientAuthRequired;
        private final String keystorePath;
        private final String truststorePath;
        private final int connectionTimeoutMs;
        private final boolean certificateValidationEnabled;
        
        public SecureConfig(boolean tlsEnabled, String[] supportedProtocols, String[] supportedCipherSuites,
                           boolean clientAuthRequired, String keystorePath, String truststorePath,
                           int connectionTimeoutMs, boolean certificateValidationEnabled) {
            this.tlsEnabled = tlsEnabled;
            this.supportedProtocols = supportedProtocols != null ? supportedProtocols.clone() : new String[0];
            this.supportedCipherSuites = supportedCipherSuites != null ? supportedCipherSuites.clone() : new String[0];
            this.clientAuthRequired = clientAuthRequired;
            this.keystorePath = keystorePath;
            this.truststorePath = truststorePath;
            this.connectionTimeoutMs = connectionTimeoutMs;
            this.certificateValidationEnabled = certificateValidationEnabled;
        }
        
        public boolean isTlsEnabled() { return tlsEnabled; }
        public String[] getSupportedProtocols() { return supportedProtocols.clone(); }
        public String[] getSupportedCipherSuites() { return supportedCipherSuites.clone(); }
        public boolean isClientAuthRequired() { return clientAuthRequired; }
        public String getKeystorePath() { return keystorePath; }
        public String getTruststorePath() { return truststorePath; }
        public int getConnectionTimeoutMs() { return connectionTimeoutMs; }
        public boolean isCertificateValidationEnabled() { return certificateValidationEnabled; }
    }
    
    /**
     * Represents the result of a secure communication operation.
     */
    class CommunicationResult {
        private final boolean success;
        private final String message;
        private final byte[] data;
        private final Map<String, String> headers;
        private final long responseTimeMs;
        
        public CommunicationResult(boolean success, String message, byte[] data, 
                                 Map<String, String> headers, long responseTimeMs) {
            this.success = success;
            this.message = message;
            this.data = data != null ? data.clone() : null;
            this.headers = headers != null ? Map.copyOf(headers) : Map.of();
            this.responseTimeMs = responseTimeMs;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public byte[] getData() { return data != null ? data.clone() : null; }
        public Map<String, String> getHeaders() { return headers; }
        public long getResponseTimeMs() { return responseTimeMs; }
    }
    
    /**
     * Configures secure communication settings.
     * 
     * @param config The secure communication configuration
     * @throws SecureCommunicationException if configuration fails
     */
    void configure(SecureConfig config) throws SecureCommunicationException;
    
    /**
     * Sends data securely to a remote endpoint.
     * 
     * @param endpoint The target endpoint URL
     * @param data The data to send
     * @param headers Additional headers to include
     * @return The communication result
     * @throws SecureCommunicationException if communication fails
     */
    CommunicationResult sendSecure(String endpoint, byte[] data, Map<String, String> headers) throws SecureCommunicationException;
    
    /**
     * Receives data securely from a remote endpoint.
     * 
     * @param endpoint The source endpoint URL
     * @param headers Additional headers to include
     * @return The communication result
     * @throws SecureCommunicationException if communication fails
     */
    CommunicationResult receiveSecure(String endpoint, Map<String, String> headers) throws SecureCommunicationException;
    
    /**
     * Validates the security of a connection to an endpoint.
     * 
     * @param endpoint The endpoint to validate
     * @return True if the connection is secure and valid
     */
    boolean validateConnection(String endpoint);
    
    /**
     * Gets the current secure communication configuration.
     * 
     * @return The current configuration
     */
    SecureConfig getConfiguration();
    
    /**
     * Checks if TLS is properly configured and working.
     * 
     * @return True if TLS is working correctly
     */
    boolean isTlsWorking();
    
    /**
     * Gets information about the TLS connection to an endpoint.
     * 
     * @param endpoint The endpoint to check
     * @return Map containing TLS connection information
     */
    Map<String, Object> getTlsInfo(String endpoint);
    
    /**
     * Validates SSL/TLS certificates for an endpoint.
     * 
     * @param endpoint The endpoint to validate
     * @return True if certificates are valid
     */
    boolean validateCertificates(String endpoint);
    
    /**
     * Creates a secure HTTP client with proper TLS configuration.
     * 
     * @return A configured secure HTTP client
     * @throws SecureCommunicationException if client creation fails
     */
    Object createSecureHttpClient() throws SecureCommunicationException;
    
    /**
     * Exception thrown when secure communication operations fail.
     */
    class SecureCommunicationException extends Exception {
        public SecureCommunicationException(String message) {
            super(message);
        }
        
        public SecureCommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}