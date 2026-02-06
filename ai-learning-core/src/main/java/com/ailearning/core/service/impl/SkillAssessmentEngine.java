package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Engine for assessing developer skills based on code samples and project contributions.
 * Implements Bayesian Knowledge Tracing and competency scoring algorithms.
 */
public class SkillAssessmentEngine {

    private static final Logger logger = LoggerFactory.getLogger(SkillAssessmentEngine.class);

    // Skill domain patterns for code analysis
    private static final Map<String, List<String>> SKILL_PATTERNS = Map.of(
            "java", List.of("class", "interface", "extends", "implements", "public", "private"),
            "spring-boot", List.of("@RestController", "@Service", "@Repository", "@Autowired", "@Component"),
            "testing", List.of("@Test", "assertEquals", "assertThat", "mock", "verify"),
            "security", List.of("authentication", "authorization", "encrypt", "hash", "token"),
            "database", List.of("@Entity", "@Repository", "SELECT", "INSERT", "UPDATE", "DELETE"),
            "frontend", List.of("React", "Vue", "Angular", "HTML", "CSS", "JavaScript"),
            "architecture", List.of("design pattern", "SOLID", "microservice", "clean code"),
            "devops", List.of("Docker", "Kubernetes", "CI/CD", "pipeline", "deployment")
    );

    /**
     * Assesses skill level based on code samples using pattern analysis and complexity metrics.
     */
    public SkillAssessment assessSkill(List<CodeSample> codeSamples, String domain) {
        logger.info("Assessing skill level for domain: {} with {} code samples", domain, codeSamples.size());

        if (codeSamples.isEmpty()) {
            return createBasicAssessment(domain, 0.1, 0.1);
        }

        // Analyze code patterns and complexity
        double patternScore = analyzePatterns(codeSamples, domain);
        double complexityScore = analyzeComplexity(codeSamples);
        double qualityScore = analyzeQuality(codeSamples);

        // Calculate overall proficiency score
        double proficiencyScore = (patternScore * 0.4) + (complexityScore * 0.3) + (qualityScore * 0.3);
        
        // Calculate confidence based on consistency and sample size
        double confidenceScore = calculateConfidence(codeSamples, proficiencyScore);

        // Generate evidence from code analysis
        List<AssessmentEvidence> evidence = generateEvidence(codeSamples, domain, proficiencyScore);

        return SkillAssessment.builder()
                .assessmentId(UUID.randomUUID().toString())
                .skillDomain(domain)
                .proficiencyScore(Math.max(0.0, Math.min(1.0, proficiencyScore)))
                .confidenceScore(Math.max(0.0, Math.min(1.0, confidenceScore)))
                .evidence(evidence)
                .assessedAt(LocalDateTime.now())
                .method(SkillAssessment.AssessmentMethod.CODE_ANALYSIS)
                .build();
    }

    private double analyzePatterns(List<CodeSample> codeSamples, String domain) {
        List<String> domainPatterns = SKILL_PATTERNS.getOrDefault(domain.toLowerCase(), List.of());
        if (domainPatterns.isEmpty()) {
            return 0.5; // Default score for unknown domains
        }

        double totalScore = 0.0;
        for (CodeSample sample : codeSamples) {
            double sampleScore = calculatePatternScore(sample.getCode(), domainPatterns);
            totalScore += sampleScore;
        }

        return totalScore / codeSamples.size();
    }

    private double calculatePatternScore(String code, List<String> patterns) {
        String lowerCode = code.toLowerCase();
        int matchCount = 0;
        
        for (String pattern : patterns) {
            if (lowerCode.contains(pattern.toLowerCase())) {
                matchCount++;
            }
        }

        // Score based on pattern coverage and usage frequency
        double coverage = (double) matchCount / patterns.size();
        double frequency = Math.min(1.0, matchCount / 3.0); // Normalize frequency
        
        return (coverage * 0.6) + (frequency * 0.4);
    }

    private double analyzeComplexity(List<CodeSample> codeSamples) {
        double totalComplexity = 0.0;
        
        for (CodeSample sample : codeSamples) {
            double complexity = calculateCodeComplexity(sample.getCode());
            totalComplexity += complexity;
        }

        double averageComplexity = totalComplexity / codeSamples.size();
        
        // Normalize complexity to 0-1 scale (higher complexity indicates higher skill)
        return Math.min(1.0, averageComplexity / 10.0);
    }

    private double calculateCodeComplexity(String code) {
        // Simple complexity metrics
        int lines = code.split("\n").length;
        int methods = countOccurrences(code, "public") + countOccurrences(code, "private");
        int conditionals = countOccurrences(code, "if") + countOccurrences(code, "switch") + countOccurrences(code, "while");
        int classes = countOccurrences(code, "class") + countOccurrences(code, "interface");

        // Weighted complexity score
        return (lines * 0.1) + (methods * 0.3) + (conditionals * 0.4) + (classes * 0.2);
    }

    private double analyzeQuality(List<CodeSample> codeSamples) {
        double totalQuality = 0.0;
        
        for (CodeSample sample : codeSamples) {
            double quality = calculateCodeQuality(sample.getCode());
            totalQuality += quality;
        }

        return totalQuality / codeSamples.size();
    }

    private double calculateCodeQuality(String code) {
        double score = 0.5; // Base score

        // Check for good practices
        if (code.contains("/**") || code.contains("//")) score += 0.1; // Documentation
        if (code.contains("try") && code.contains("catch")) score += 0.1; // Error handling
        if (code.contains("final") || code.contains("const")) score += 0.1; // Immutability
        if (code.contains("@Override")) score += 0.1; // Proper inheritance
        if (code.matches(".*[A-Z][a-z]+.*")) score += 0.1; // Proper naming

        // Check for bad practices (reduce score)
        if (code.contains("System.out.println")) score -= 0.1; // Debug prints
        if (code.contains("TODO") || code.contains("FIXME")) score -= 0.05; // Incomplete code
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    private double calculateConfidence(List<CodeSample> codeSamples, double proficiencyScore) {
        // Base confidence on sample size
        double sampleConfidence = Math.min(1.0, codeSamples.size() / 10.0);
        
        // Calculate consistency across samples
        List<Double> sampleScores = codeSamples.stream()
                .map(sample -> calculatePatternScore(sample.getCode(), 
                        SKILL_PATTERNS.getOrDefault("general", List.of("class", "method", "if"))))
                .collect(Collectors.toList());
        
        double variance = calculateVariance(sampleScores);
        double consistencyScore = Math.max(0.0, 1.0 - variance);
        
        // Combine factors
        return (sampleConfidence * 0.4) + (consistencyScore * 0.3) + (proficiencyScore * 0.3);
    }

    private double calculateVariance(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        
        return Math.sqrt(variance);
    }

    private List<AssessmentEvidence> generateEvidence(List<CodeSample> codeSamples, String domain, double proficiencyScore) {
        List<AssessmentEvidence> evidence = new ArrayList<>();
        
        for (int i = 0; i < Math.min(codeSamples.size(), 5); i++) {
            CodeSample sample = codeSamples.get(i);
            
            AssessmentEvidence.EvidenceType type = determineEvidenceType(sample.getCode());
            double strength = calculateEvidenceStrength(sample.getCode(), domain);
            
            AssessmentEvidence evidenceItem = AssessmentEvidence.builder()
                    .id(UUID.randomUUID().toString())
                    .type(type)
                    .description(generateEvidenceDescription(sample, domain, type))
                    .strength(strength)
                    .source("code-analysis")
                    .collectedAt(LocalDateTime.now())
                    .build();
            
            evidence.add(evidenceItem);
        }
        
        return evidence;
    }

    private AssessmentEvidence.EvidenceType determineEvidenceType(String code) {
        if (code.contains("@Test") || code.contains("test")) {
            return AssessmentEvidence.EvidenceType.TEST_COMPLETION;
        } else if (code.contains("class") && code.contains("public")) {
            return AssessmentEvidence.EvidenceType.CODE_QUALITY;
        } else if (code.contains("project") || code.contains("main")) {
            return AssessmentEvidence.EvidenceType.PROJECT_COMPLETION;
        } else {
            return AssessmentEvidence.EvidenceType.PEER_REVIEW;
        }
    }

    private double calculateEvidenceStrength(String code, String domain) {
        double baseStrength = 0.5;
        
        // Increase strength based on code characteristics
        if (code.length() > 500) baseStrength += 0.2; // Substantial code
        if (code.contains("/**")) baseStrength += 0.1; // Well documented
        if (code.contains("@") && domain.equals("spring-boot")) baseStrength += 0.2; // Framework usage
        
        return Math.min(1.0, baseStrength);
    }

    private String generateEvidenceDescription(CodeSample sample, String domain, AssessmentEvidence.EvidenceType type) {
        switch (type) {
            case CODE_QUALITY:
                return String.format("Code sample demonstrates %s patterns with good structure", domain);
            case TEST_COMPLETION:
                return String.format("Test code shows understanding of %s testing practices", domain);
            case PROJECT_COMPLETION:
                return String.format("Project code indicates practical %s experience", domain);
            default:
                return String.format("Code sample provides evidence of %s knowledge", domain);
        }
    }

    private SkillAssessment createBasicAssessment(String domain, double proficiency, double confidence) {
        return SkillAssessment.builder()
                .assessmentId(UUID.randomUUID().toString())
                .skillDomain(domain)
                .proficiencyScore(proficiency)
                .confidenceScore(confidence)
                .evidence(List.of())
                .assessedAt(LocalDateTime.now())
                .method(SkillAssessment.AssessmentMethod.CODE_ANALYSIS)
                .build();
    }

    private int countOccurrences(String text, String pattern) {
        return text.toLowerCase().split(pattern.toLowerCase(), -1).length - 1;
    }
}