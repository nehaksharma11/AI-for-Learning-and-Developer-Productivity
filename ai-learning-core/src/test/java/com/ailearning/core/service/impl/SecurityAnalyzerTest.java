package com.ailearning.core.service.impl;

import com.ailearning.core.model.SecurityIssue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SecurityAnalyzer component.
 * Validates security vulnerability detection across multiple programming languages.
 */
class SecurityAnalyzerTest {
    
    private SecurityAnalyzer securityAnalyzer;
    
    @BeforeEach
    void setUp() {
        securityAnalyzer = new SecurityAnalyzer();
    }
    
    @Test
    @DisplayName("Should detect SQL injection in Java code")
    void testDetectSqlInjectionJava() {
        String javaCode = """
            public void getUserData(String userId) {
                String query = "SELECT * FROM users WHERE id = " + userId;
                executeQuery(query);
            }
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(javaCode, "java");
        
        assertFalse(issues.isEmpty());
        SecurityIssue sqlIssue = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.INJECTION)
                .findFirst()
                .orElse(null);
        
        assertNotNull(sqlIssue);
        assertEquals(SecurityIssue.Severity.HIGH, sqlIssue.getSeverity());
        assertEquals("CWE-89", sqlIssue.getCweId());
        assertTrue(sqlIssue.getDescription().toLowerCase().contains("sql injection"));
        assertNotNull(sqlIssue.getRecommendation());
    }
    
    @Test
    @DisplayName("Should detect hardcoded passwords in Java code")
    void testDetectHardcodedPasswordJava() {
        String javaCode = """
            public class DatabaseConfig {
                private String password = "admin123";
                private String dbPassword = "secretPassword";
            }
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(javaCode, "java");
        
        assertFalse(issues.isEmpty());
        
        long passwordIssues = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.AUTHENTICATION)
                .count();
        
        assertTrue(passwordIssues >= 1, "Should detect hardcoded passwords");
        
        SecurityIssue passwordIssue = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.AUTHENTICATION)
                .findFirst()
                .orElse(null);
        
        assertNotNull(passwordIssue);
        assertEquals(SecurityIssue.Severity.CRITICAL, passwordIssue.getSeverity());
        assertEquals("CWE-798", passwordIssue.getCweId());
    }
    
    @Test
    @DisplayName("Should detect weak cryptographic algorithms in Java code")
    void testDetectWeakCryptoJava() {
        String javaCode = """
            public String hashPassword(String password) {
                MessageDigest md = MessageDigest.getInstance("MD5");
                return md.digest(password.getBytes());
            }
            
            public String hashData(String data) {
                MessageDigest sha1 = MessageDigest.getInstance("SHA1");
                return sha1.digest(data.getBytes());
            }
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(javaCode, "java");
        
        assertFalse(issues.isEmpty());
        
        long cryptoIssues = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.CRYPTOGRAPHY)
                .count();
        
        assertTrue(cryptoIssues >= 1, "Should detect weak cryptographic algorithms");
        
        SecurityIssue cryptoIssue = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.CRYPTOGRAPHY)
                .findFirst()
                .orElse(null);
        
        assertNotNull(cryptoIssue);
        assertEquals(SecurityIssue.Severity.MEDIUM, cryptoIssue.getSeverity());
        assertEquals("CWE-327", cryptoIssue.getCweId());
    }
    
    @Test
    @DisplayName("Should detect XSS vulnerabilities in JavaScript code")
    void testDetectXssJavaScript() {
        String jsCode = """
            function updateContent(userInput) {
                document.getElementById('content').innerHTML = userInput;
                document.body.innerHTML = '<div>' + userInput + '</div>';
            }
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(jsCode, "javascript");
        
        assertFalse(issues.isEmpty());
        
        SecurityIssue xssIssue = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.XSS)
                .findFirst()
                .orElse(null);
        
        assertNotNull(xssIssue);
        assertEquals(SecurityIssue.Severity.HIGH, xssIssue.getSeverity());
        assertEquals("CWE-79", xssIssue.getCweId());
        assertTrue(xssIssue.getDescription().toLowerCase().contains("xss"));
    }
    
    @Test
    @DisplayName("Should detect eval usage in JavaScript code")
    void testDetectEvalJavaScript() {
        String jsCode = """
            function processUserCode(code) {
                eval(code);
                return eval('(' + code + ')');
            }
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(jsCode, "javascript");
        
        assertFalse(issues.isEmpty());
        
        long evalIssues = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.INJECTION)
                .count();
        
        assertTrue(evalIssues >= 1, "Should detect eval() usage");
        
        SecurityIssue evalIssue = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.INJECTION)
                .findFirst()
                .orElse(null);
        
        assertNotNull(evalIssue);
        assertEquals(SecurityIssue.Severity.CRITICAL, evalIssue.getSeverity());
        assertEquals("CWE-95", evalIssue.getCweId());
    }
    
    @Test
    @DisplayName("Should detect hardcoded secrets in JavaScript code")
    void testDetectHardcodedSecretsJavaScript() {
        String jsCode = """
            const config = {
                api_key: "sk-1234567890abcdef",
                secret: "my-secret-token",
                token: "bearer-token-123"
            };
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(jsCode, "javascript");
        
        assertFalse(issues.isEmpty());
        
        SecurityIssue secretIssue = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.CONFIGURATION)
                .findFirst()
                .orElse(null);
        
        assertNotNull(secretIssue);
        assertEquals(SecurityIssue.Severity.CRITICAL, secretIssue.getSeverity());
        assertEquals("CWE-798", secretIssue.getCweId());
    }
    
    @Test
    @DisplayName("Should detect exec usage in Python code")
    void testDetectExecPython() {
        String pythonCode = """
            def process_user_input(user_code):
                exec(user_code)
                return exec('print("' + user_code + '")')
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(pythonCode, "python");
        
        assertFalse(issues.isEmpty());
        
        SecurityIssue execIssue = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.INJECTION)
                .findFirst()
                .orElse(null);
        
        assertNotNull(execIssue);
        assertEquals(SecurityIssue.Severity.CRITICAL, execIssue.getSeverity());
        assertEquals("CWE-95", execIssue.getCweId());
    }
    
    @Test
    @DisplayName("Should detect unsafe pickle usage in Python code")
    void testDetectUnsafePicklePython() {
        String pythonCode = """
            import pickle
            
            def load_user_data(data):
                return pickle.loads(data)
                
            def load_from_file(filename):
                with open(filename, 'rb') as f:
                    return pickle.load(f)
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(pythonCode, "python");
        
        assertFalse(issues.isEmpty());
        
        SecurityIssue pickleIssue = issues.stream()
                .filter(issue -> issue.getCategory() == SecurityIssue.Category.INPUT_VALIDATION)
                .findFirst()
                .orElse(null);
        
        assertNotNull(pickleIssue);
        assertEquals(SecurityIssue.Severity.HIGH, pickleIssue.getSeverity());
        assertEquals("CWE-502", pickleIssue.getCweId());
    }
    
    @Test
    @DisplayName("Should provide detailed issue information")
    void testIssueDetails() {
        String javaCode = """
            public void login(String username, String password) {
                String query = "SELECT * FROM users WHERE username = '" + username + "'";
            }
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(javaCode, "java");
        
        assertFalse(issues.isEmpty());
        SecurityIssue issue = issues.get(0);
        
        // Verify all required fields are populated
        assertNotNull(issue.getId());
        assertNotNull(issue.getTitle());
        assertNotNull(issue.getDescription());
        assertNotNull(issue.getSeverity());
        assertNotNull(issue.getCategory());
        assertNotNull(issue.getCweId());
        assertNotNull(issue.getRecommendation());
        assertNotNull(issue.getCodeSnippet());
        
        // Verify line and column information
        assertNotNull(issue.getLine());
        assertTrue(issue.getLine() > 0);
        assertNotNull(issue.getColumn());
        assertTrue(issue.getColumn() >= 0);
    }
    
    @Test
    @DisplayName("Should handle code without security issues")
    void testCleanCode() {
        String cleanCode = """
            public class SafeClass {
                private static final Logger logger = LoggerFactory.getLogger(SafeClass.class);
                
                public void safeMethod() {
                    logger.info("This is a safe method");
                }
            }
            """;
        
        List<SecurityIssue> issues = securityAnalyzer.analyze(cleanCode, "java");
        
        // Clean code should have no security issues
        assertTrue(issues.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle empty code")
    void testEmptyCode() {
        List<SecurityIssue> issues = securityAnalyzer.analyze("", "java");
        assertTrue(issues.isEmpty());
        
        issues = securityAnalyzer.analyze("   \n  \n  ", "java");
        assertTrue(issues.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle unsupported language gracefully")
    void testUnsupportedLanguage() {
        String code = "IDENTIFICATION DIVISION.";
        List<SecurityIssue> issues = securityAnalyzer.analyze(code, "cobol");
        
        // Should not crash and return empty list for unsupported languages
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }
}