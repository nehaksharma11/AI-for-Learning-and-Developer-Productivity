package com.ailearning.core.service.impl;

import com.ailearning.core.service.SecureCommunicationService;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of SecureCommunicationService providing TLS configuration and secure communication.
 */
public class DefaultSecureCommunicationService implements SecureCommunicationService {
    
    private SecureConfig currentConfig;
    private final Map<String, Object> connectionCache = new ConcurrentHashMap<>();
    
    // Default secure configuration
    private static final String[] DEFAULT_PROTOCOLS = {"TLSv1.3", "TLSv1.2"};
    private static final String[] DEFAULT_CIPHER_SUITES = {
        "TLS_AES_256_GCM_SHA384",
        "TLS_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
    };
    
    public DefaultSecureCommunicationService() {
        // Initialize with default secure configuration
        this.currentConfig = new SecureConfig(
            true, // TLS enabled
            DEFAULT_PROTOCOLS,
            DEFAULT_CIPHER_SUITES,
            false, // Client auth not required by default
            null, // No keystore path by default
            null, // No truststore path by default
            30000, // 30 second timeout
            true // Certificate validation enabled
        );
    }
    
    @Override
    public void configure(SecureConfig config) throws SecureCommunicationException {
        Objects.requireNonNull(config, "Configuration cannot be null");
        
        try {
            validateConfiguration(config);
            this.currentConfig = config;
            
            // Configure system properties for TLS
            if (config.isTlsEnabled()) {
                configureTlsSystemProperties(config);
            }
            
            // Clear connection cache when configuration changes
            connectionCache.clear();
            
        } catch (Exception e) {
            throw new SecureCommunicationException("Failed to configure secure communication", e);
        }
    }
    
    @Override
    public CommunicationResult sendSecure(String endpoint, byte[] data, Map<String, String> headers) throws SecureCommunicationException {
        Objects.requireNonNull(endpoint, "Endpoint cannot be null");
        Objects.requireNonNull(data, "Data cannot be null");
        
        if (!currentConfig.isTlsEnabled()) {
            throw new SecureCommunicationException("TLS is not enabled");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate endpoint is HTTPS
            if (!endpoint.toLowerCase().startsWith("https://")) {
                throw new SecureCommunicationException("Endpoint must use HTTPS: " + endpoint);
            }
            
            // Create secure connection
            HttpsURLConnection connection = createSecureConnection(endpoint);
            
            // Set headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }
            
            // Configure for POST
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));
            
            // Send data
            try (var outputStream = connection.getOutputStream()) {
                outputStream.write(data);
                outputStream.flush();
            }
            
            // Get response
            int responseCode = connection.getResponseCode();
            byte[] responseData = null;
            Map<String, String> responseHeaders = new HashMap<>();
            
            // Read response headers
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null && !header.getValue().isEmpty()) {
                    responseHeaders.put(header.getKey(), header.getValue().get(0));
                }
            }
            
            // Read response data
            try (var inputStream = responseCode >= 200 && responseCode < 300 ? 
                    connection.getInputStream() : connection.getErrorStream()) {
                if (inputStream != null) {
                    responseData = inputStream.readAllBytes();
                }
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return new CommunicationResult(
                responseCode >= 200 && responseCode < 300,
                "HTTP " + responseCode,
                responseData,
                responseHeaders,
                responseTime
            );
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new CommunicationResult(
                false,
                "Communication failed: " + e.getMessage(),
                null,
                Map.of(),
                responseTime
            );
        }
    }
    
    @Override
    public CommunicationResult receiveSecure(String endpoint, Map<String, String> headers) throws SecureCommunicationException {
        Objects.requireNonNull(endpoint, "Endpoint cannot be null");
        
        if (!currentConfig.isTlsEnabled()) {
            throw new SecureCommunicationException("TLS is not enabled");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate endpoint is HTTPS
            if (!endpoint.toLowerCase().startsWith("https://")) {
                throw new SecureCommunicationException("Endpoint must use HTTPS: " + endpoint);
            }
            
            // Create secure connection
            HttpsURLConnection connection = createSecureConnection(endpoint);
            
            // Set headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }
            
            // Configure for GET
            connection.setRequestMethod("GET");
            
            // Get response
            int responseCode = connection.getResponseCode();
            byte[] responseData = null;
            Map<String, String> responseHeaders = new HashMap<>();
            
            // Read response headers
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null && !header.getValue().isEmpty()) {
                    responseHeaders.put(header.getKey(), header.getValue().get(0));
                }
            }
            
            // Read response data
            try (var inputStream = responseCode >= 200 && responseCode < 300 ? 
                    connection.getInputStream() : connection.getErrorStream()) {
                if (inputStream != null) {
                    responseData = inputStream.readAllBytes();
                }
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return new CommunicationResult(
                responseCode >= 200 && responseCode < 300,
                "HTTP " + responseCode,
                responseData,
                responseHeaders,
                responseTime
            );
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new CommunicationResult(
                false,
                "Communication failed: " + e.getMessage(),
                null,
                Map.of(),
                responseTime
            );
        }
    }
    
    @Override
    public boolean validateConnection(String endpoint) {
        try {
            if (!endpoint.toLowerCase().startsWith("https://")) {
                return false;
            }
            
            HttpsURLConnection connection = createSecureConnection(endpoint);
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000); // 5 second timeout for validation
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 400;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public SecureConfig getConfiguration() {
        return currentConfig;
    }
    
    @Override
    public boolean isTlsWorking() {
        try {
            // Test TLS by connecting to a known HTTPS endpoint
            return validateConnection("https://www.google.com");
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getTlsInfo(String endpoint) {
        Map<String, Object> tlsInfo = new HashMap<>();
        
        try {
            if (!endpoint.toLowerCase().startsWith("https://")) {
                tlsInfo.put("error", "Endpoint is not HTTPS");
                return tlsInfo;
            }
            
            HttpsURLConnection connection = createSecureConnection(endpoint);
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000);
            connection.connect();
            
            // Get SSL session info
            SSLSession session = connection.getSSLSession();
            if (session != null) {
                tlsInfo.put("protocol", session.getProtocol());
                tlsInfo.put("cipherSuite", session.getCipherSuite());
                tlsInfo.put("peerHost", session.getPeerHost());
                tlsInfo.put("peerPort", session.getPeerPort());
                
                // Get certificate info
                Certificate[] certificates = session.getPeerCertificates();
                if (certificates.length > 0 && certificates[0] instanceof X509Certificate) {
                    X509Certificate cert = (X509Certificate) certificates[0];
                    tlsInfo.put("certificateSubject", cert.getSubjectDN().toString());
                    tlsInfo.put("certificateIssuer", cert.getIssuerDN().toString());
                    tlsInfo.put("certificateNotBefore", cert.getNotBefore().toString());
                    tlsInfo.put("certificateNotAfter", cert.getNotAfter().toString());
                }
            }
            
            tlsInfo.put("responseCode", connection.getResponseCode());
            
        } catch (Exception e) {
            tlsInfo.put("error", e.getMessage());
        }
        
        return tlsInfo;
    }
    
    @Override
    public boolean validateCertificates(String endpoint) {
        try {
            if (!endpoint.toLowerCase().startsWith("https://")) {
                return false;
            }
            
            HttpsURLConnection connection = createSecureConnection(endpoint);
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000);
            connection.connect();
            
            // If we get here without exception, certificates are valid
            return connection.getResponseCode() > 0;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Object createSecureHttpClient() throws SecureCommunicationException {
        try {
            // Create SSL context with current configuration
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null); // Use default trust managers
            
            // Return a configured SSL socket factory
            return sslContext.getSocketFactory();
            
        } catch (Exception e) {
            throw new SecureCommunicationException("Failed to create secure HTTP client", e);
        }
    }
    
    private HttpsURLConnection createSecureConnection(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        
        // Configure timeouts
        connection.setConnectTimeout(currentConfig.getConnectionTimeoutMs());
        connection.setReadTimeout(currentConfig.getConnectionTimeoutMs());
        
        // Configure SSL/TLS
        if (currentConfig.isCertificateValidationEnabled()) {
            // Use default hostname verifier and SSL socket factory
            connection.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
            connection.setSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
        } else {
            // Disable certificate validation (not recommended for production)
            connection.setHostnameVerifier((hostname, session) -> true);
            
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{new TrustAllTrustManager()}, null);
                connection.setSSLSocketFactory(sslContext.getSocketFactory());
            } catch (Exception e) {
                throw new IOException("Failed to configure SSL context", e);
            }
        }
        
        return connection;
    }
    
    private void validateConfiguration(SecureConfig config) throws SecureCommunicationException {
        if (config.isTlsEnabled()) {
            if (config.getSupportedProtocols().length == 0) {
                throw new SecureCommunicationException("At least one TLS protocol must be specified");
            }
            
            if (config.getConnectionTimeoutMs() <= 0) {
                throw new SecureCommunicationException("Connection timeout must be positive");
            }
        }
    }
    
    private void configureTlsSystemProperties(SecureConfig config) {
        // Set supported protocols
        if (config.getSupportedProtocols().length > 0) {
            System.setProperty("https.protocols", String.join(",", config.getSupportedProtocols()));
        }
        
        // Set keystore and truststore if specified
        if (config.getKeystorePath() != null) {
            System.setProperty("javax.net.ssl.keyStore", config.getKeystorePath());
        }
        
        if (config.getTruststorePath() != null) {
            System.setProperty("javax.net.ssl.trustStore", config.getTruststorePath());
        }
    }
    
    /**
     * Trust manager that accepts all certificates (for testing only - not secure).
     */
    private static class TrustAllTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // Accept all certificates
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // Accept all certificates
        }
        
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}