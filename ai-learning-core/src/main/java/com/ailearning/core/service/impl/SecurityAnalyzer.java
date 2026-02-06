package com.ailearning.core.service.impl;

import com.ailearning.core.model.SecurityIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Security analyzer for detecting common security vulnerabilities in code.
 * Implements OWASP Top 10 and CWE-based security checks.
 */
public class SecurityAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityAnalyzer.class);
    
    // Security patterns for different languages
    private final Map<String, List<SecurityRule>> securityRules;
    
    public SecurityAnalyzer() {
        this.securityRules = initializeSecurityRules();
    }
    
    public List<SecurityIssue> analyze(String code, String language) {
        List<SecurityIssue> issues = new ArrayList<>();
        
        List<SecurityRule> rules = securityRules.getOrDefault(language.toLowerCase(), List.of());
        
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;
            
            for (SecurityRule rule : rules) {
                Matcher matcher = rule.pattern.matcher(line);
                if (matcher.find()) {
                    SecurityIssue issue = SecurityIssue.builder()
                            .id(rule.id + "-" + lineNumber)
                            .title(rule.title)
                            .description(rule.description)
                            .severity(rule.severity)
                            .category(rule.category)
                            .line(lineNumber)
                            .column(matcher.start())
                            .cweId(rule.cweId)
                            .recommendation(rule.recommendation)
                            .codeSnippet(line.trim())
                            .build();
                    
                    issues.add(issue);
                    logger.debug("Security issue detected: {} at line {}", rule.title, lineNumber);
                }
            }
        }
        
        return issues;
    }
    
    private Map<String, List<SecurityRule>> initializeSecurityRules() {
        Map<String, List<SecurityRule>> rules = new HashMap<>();
        
        // Java security rules
        rules.put("java", Arrays.asList(
                new SecurityRule(
                        "java-sql-injection",
                        "SQL Injection Risk",
                        "Direct string concatenation in SQL queries can lead to SQL injection",
                        Pattern.compile(".*\\+.*[\"'].*SELECT|INSERT|UPDATE|DELETE.*[\"'].*", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.HIGH,
                        SecurityIssue.Category.INJECTION,
                        "CWE-89",
                        "Use parameterized queries or prepared statements"
                ),
                new SecurityRule(
                        "java-hardcoded-password",
                        "Hardcoded Password",
                        "Hardcoded passwords in source code are a security risk",
                        Pattern.compile(".*password\\s*=\\s*[\"'][^\"']+[\"']", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.CRITICAL,
                        SecurityIssue.Category.AUTHENTICATION,
                        "CWE-798",
                        "Use environment variables or secure configuration files"
                ),
                new SecurityRule(
                        "java-weak-crypto",
                        "Weak Cryptographic Algorithm",
                        "MD5 and SHA1 are cryptographically weak",
                        Pattern.compile(".*(MD5|SHA1).*", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.MEDIUM,
                        SecurityIssue.Category.CRYPTOGRAPHY,
                        "CWE-327",
                        "Use SHA-256 or stronger cryptographic algorithms"
                ),
                new SecurityRule(
                        "java-deserialization",
                        "Unsafe Deserialization",
                        "Deserializing untrusted data can lead to remote code execution",
                        Pattern.compile(".*ObjectInputStream.*readObject.*", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.HIGH,
                        SecurityIssue.Category.INPUT_VALIDATION,
                        "CWE-502",
                        "Validate and sanitize serialized data before deserialization"
                )
        ));
        
        // JavaScript/TypeScript security rules
        List<SecurityRule> jsRules = Arrays.asList(
                new SecurityRule(
                        "js-xss-risk",
                        "XSS Vulnerability Risk",
                        "Direct DOM manipulation with user input can lead to XSS",
                        Pattern.compile(".*innerHTML\\s*=.*", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.HIGH,
                        SecurityIssue.Category.XSS,
                        "CWE-79",
                        "Use textContent or properly escape HTML content"
                ),
                new SecurityRule(
                        "js-eval-usage",
                        "Dangerous eval() Usage",
                        "Using eval() with user input can lead to code injection",
                        Pattern.compile(".*eval\\s*\\(.*", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.CRITICAL,
                        SecurityIssue.Category.INJECTION,
                        "CWE-95",
                        "Avoid eval() or use safer alternatives like JSON.parse()"
                ),
                new SecurityRule(
                        "js-hardcoded-secret",
                        "Hardcoded Secret",
                        "Hardcoded API keys or secrets in client-side code",
                        Pattern.compile(".*(api[_-]?key|secret|token)\\s*[=:]\\s*[\"'][^\"']+[\"']", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.CRITICAL,
                        SecurityIssue.Category.CONFIGURATION,
                        "CWE-798",
                        "Move secrets to environment variables or secure configuration"
                )
        );
        rules.put("javascript", jsRules);
        rules.put("typescript", jsRules);
        
        // Python security rules
        rules.put("python", Arrays.asList(
                new SecurityRule(
                        "python-sql-injection",
                        "SQL Injection Risk",
                        "String formatting in SQL queries can lead to SQL injection",
                        Pattern.compile(".*%.*SELECT|INSERT|UPDATE|DELETE.*%.*", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.HIGH,
                        SecurityIssue.Category.INJECTION,
                        "CWE-89",
                        "Use parameterized queries with ? placeholders"
                ),
                new SecurityRule(
                        "python-exec-usage",
                        "Dangerous exec() Usage",
                        "Using exec() with user input can lead to code injection",
                        Pattern.compile(".*exec\\s*\\(.*", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.CRITICAL,
                        SecurityIssue.Category.INJECTION,
                        "CWE-95",
                        "Avoid exec() or validate input thoroughly"
                ),
                new SecurityRule(
                        "python-pickle-usage",
                        "Unsafe Pickle Usage",
                        "Unpickling untrusted data can lead to arbitrary code execution",
                        Pattern.compile(".*pickle\\.loads?\\s*\\(.*", Pattern.CASE_INSENSITIVE),
                        SecurityIssue.Severity.HIGH,
                        SecurityIssue.Category.INPUT_VALIDATION,
                        "CWE-502",
                        "Use safer serialization formats like JSON"
                )
        ));
        
        return rules;
    }
    
    private static class SecurityRule {
        final String id;
        final String title;
        final String description;
        final Pattern pattern;
        final SecurityIssue.Severity severity;
        final SecurityIssue.Category category;
        final String cweId;
        final String recommendation;
        
        SecurityRule(String id, String title, String description, Pattern pattern,
                    SecurityIssue.Severity severity, SecurityIssue.Category category,
                    String cweId, String recommendation) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.pattern = pattern;
            this.severity = severity;
            this.category = category;
            this.cweId = cweId;
            this.recommendation = recommendation;
        }
    }
}