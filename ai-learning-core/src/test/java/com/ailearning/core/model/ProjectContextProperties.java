package com.ailearning.core.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for ProjectContext and related models.
 * 
 * Feature: ai-learning-companion, Property 9: Project Context Understanding and Maintenance
 * Validates: Requirements 5.1, 5.2, 5.4
 * 
 * Tests the property that for any project opening or code modification, 
 * the context engine should analyze and maintain accurate understanding of 
 * codebase structure, dependencies, relationships, and coding standards.
 */
class ProjectContextProperties {

    /**
     * Property: Project context maintains structural integrity
     * For any valid project context, all structural relationships should be consistent
     * and the context should accurately represent the project state.
     */
    @Property(tries = 100)
    @Label("Project context maintains structural integrity")
    void projectContextMaintainsStructuralIntegrity(
            @ForAll("validProjectContexts") ProjectContext context) {
        
        // Then: Structural integrity is maintained
        assertNotNull(context.getId(), "Project ID should not be null");
        assertNotNull(context.getStructure(), "Project structure should not be null");
        assertNotNull(context.getDependencies(), "Dependencies list should not be null");
        assertNotNull(context.getPatterns(), "Patterns list should not be null");
        assertNotNull(context.getConventions(), "Conventions list should not be null");
        assertNotNull(context.getComplexity(), "Complexity metrics should not be null");
        
        // Verify collections are properly encapsulated (immutable)
        List<Dependency> deps1 = context.getDependencies();
        List<Dependency> deps2 = context.getDependencies();
        assertNotSame(deps1, deps2, "Dependencies should return new instances for immutability");
        
        List<CodePattern> patterns1 = context.getPatterns();
        List<CodePattern> patterns2 = context.getPatterns();
        assertNotSame(patterns1, patterns2, "Patterns should return new instances for immutability");
        
        // Verify content consistency
        assertEquals(deps1, deps2, "Dependencies content should be consistent");
        assertEquals(patterns1, patterns2, "Patterns content should be consistent");
    }

    /**
     * Property: Project structure maintains file hierarchy consistency
     * For any project structure, the file hierarchy should be logically consistent
     * with proper relationships between files, modules, and entry points.
     */
    @Property(tries = 100)
    @Label("Project structure maintains file hierarchy consistency")
    void projectStructureMaintainsFileHierarchyConsistency(
            @ForAll("validProjectStructures") ProjectStructure structure) {
        
        // Then: File hierarchy consistency is maintained
        assertNotNull(structure.getFiles(), "Files list should not be null");
        assertNotNull(structure.getModules(), "Modules list should not be null");
        assertNotNull(structure.getRelationships(), "Relationships list should not be null");
        assertNotNull(structure.getEntryPoints(), "Entry points list should not be null");
        
        // Verify entry points reference actual files
        List<String> filePaths = structure.getFiles().stream()
                .map(FileNode::getPath)
                .toList();
        
        for (String entryPoint : structure.getEntryPoints()) {
            assertTrue(filePaths.contains(entryPoint) || entryPoint.isEmpty(),
                    "Entry point should reference an actual file: " + entryPoint);
        }
        
        // Verify relationships reference valid elements
        for (Relationship rel : structure.getRelationships()) {
            assertNotNull(rel.getFrom(), "Relationship 'from' should not be null");
            assertNotNull(rel.getTo(), "Relationship 'to' should not be null");
            assertNotNull(rel.getType(), "Relationship type should not be null");
            assertTrue(rel.getStrength() >= 0.0 && rel.getStrength() <= 1.0,
                    "Relationship strength should be between 0.0 and 1.0");
        }
    }

    /**
     * Property: Complexity metrics maintain mathematical consistency
     * For any complexity metrics, all calculated values should be mathematically
     * consistent and within expected ranges.
     */
    @Property(tries = 100)
    @Label("Complexity metrics maintain mathematical consistency")
    void complexityMetricsMaintainMathematicalConsistency(
            @ForAll("validComplexityMetrics") ComplexityMetrics metrics) {
        
        // Then: Mathematical consistency is maintained
        assertTrue(metrics.getCyclomaticComplexity() >= 0,
                "Cyclomatic complexity should be non-negative");
        assertTrue(metrics.getLinesOfCode() >= 0,
                "Lines of code should be non-negative");
        assertTrue(metrics.getCognitiveComplexity() >= 0,
                "Cognitive complexity should be non-negative");
        assertTrue(metrics.getNestingDepth() >= 0,
                "Nesting depth should be non-negative");
        assertTrue(metrics.getNumberOfMethods() >= 0,
                "Number of methods should be non-negative");
        assertTrue(metrics.getNumberOfClasses() >= 0,
                "Number of classes should be non-negative");
        
        // Verify overall complexity score calculation
        double overallScore = metrics.getOverallComplexityScore();
        assertTrue(overallScore >= 0.0 && overallScore <= 1.0,
                "Overall complexity score should be between 0.0 and 1.0");
        
        // Verify consistency between complexity indicators
        if (metrics.isHighlyComplex()) {
            assertTrue(overallScore > 0.5,
                    "Highly complex code should have high overall score");
        }
        
        if (metrics.needsRefactoring()) {
            assertTrue(metrics.isHighlyComplex() || 
                      (metrics.getLinesOfCode() > 500 && metrics.getNumberOfMethods() > 20),
                    "Code needing refactoring should be complex or large");
        }
    }

    /**
     * Property: Dependencies maintain type and scope consistency
     * For any dependency, the type and scope should be consistent with
     * the dependency's characteristics and usage patterns.
     */
    @Property(tries = 100)
    @Label("Dependencies maintain type and scope consistency")
    void dependenciesMaintainTypeAndScopeConsistency(
            @ForAll("validDependencies") Dependency dependency) {
        
        // Then: Type and scope consistency is maintained
        assertNotNull(dependency.getName(), "Dependency name should not be null");
        assertNotNull(dependency.getType(), "Dependency type should not be null");
        assertNotNull(dependency.getScope(), "Dependency scope should not be null");
        
        // Verify full identifier consistency
        String fullId = dependency.getFullIdentifier();
        assertTrue(fullId.contains(dependency.getName()),
                "Full identifier should contain dependency name");
        
        // Verify criticality assessment consistency
        boolean isCritical = dependency.isCritical();
        if (isCritical) {
            assertFalse(dependency.isOptional(),
                    "Critical dependencies should not be optional");
            assertTrue(dependency.getScope() == Dependency.DependencyScope.COMPILE ||
                      dependency.getScope() == Dependency.DependencyScope.RUNTIME,
                    "Critical dependencies should have compile or runtime scope");
        }
        
        // Verify type-specific characteristics
        if (dependency.getType() == Dependency.DependencyType.INTERNAL_MODULE) {
            assertNull(dependency.getVersion(),
                    "Internal modules typically don't have external versions");
        }
        
        if (dependency.getType() == Dependency.DependencyType.FRAMEWORK) {
            assertTrue(dependency.isCritical(),
                    "Frameworks are typically critical dependencies");
        }
    }

    /**
     * Property: Code patterns maintain detection consistency
     * For any code pattern, the detection confidence and categorization
     * should be consistent with the pattern type and characteristics.
     */
    @Property(tries = 100)
    @Label("Code patterns maintain detection consistency")
    void codePatternsMaintainDetectionConsistency(
            @ForAll("validCodePatterns") CodePattern pattern) {
        
        // Then: Detection consistency is maintained
        assertNotNull(pattern.getName(), "Pattern name should not be null");
        assertNotNull(pattern.getType(), "Pattern type should not be null");
        assertNotNull(pattern.getCategory(), "Pattern category should not be null");
        
        assertTrue(pattern.getConfidence() >= 0.0 && pattern.getConfidence() <= 1.0,
                "Pattern confidence should be between 0.0 and 1.0");
        
        // Verify pattern classification consistency
        if (pattern.isProblematic()) {
            assertTrue(pattern.getType() == CodePattern.PatternType.ANTI_PATTERN ||
                      pattern.getType() == CodePattern.PatternType.CODE_SMELL,
                    "Problematic patterns should be anti-patterns or code smells");
            assertNotNull(pattern.getRecommendation(),
                    "Problematic patterns should have recommendations");
        }
        
        if (pattern.isBeneficial()) {
            assertTrue(pattern.getType() == CodePattern.PatternType.DESIGN_PATTERN ||
                      pattern.getType() == CodePattern.PatternType.BEST_PRACTICE,
                    "Beneficial patterns should be design patterns or best practices");
        }
        
        if (pattern.isHighConfidence()) {
            assertTrue(pattern.getConfidence() >= 0.8,
                    "High confidence patterns should have confidence >= 0.8");
        }
        
        // Verify mutually exclusive classifications
        assertFalse(pattern.isProblematic() && pattern.isBeneficial(),
                "Patterns cannot be both problematic and beneficial");
    }

    /**
     * Property: Coding conventions maintain adherence consistency
     * For any coding convention, the adherence score and enforcement
     * should be consistent with the convention's importance and scope.
     */
    @Property(tries = 100)
    @Label("Coding conventions maintain adherence consistency")
    void codingConventionsMaintainAdherenceConsistency(
            @ForAll("validCodingConventions") CodingConvention convention) {
        
        // Then: Adherence consistency is maintained
        assertNotNull(convention.getName(), "Convention name should not be null");
        assertNotNull(convention.getType(), "Convention type should not be null");
        assertNotNull(convention.getScope(), "Convention scope should not be null");
        
        assertTrue(convention.getAdherenceScore() >= 0.0 && convention.getAdherenceScore() <= 1.0,
                "Adherence score should be between 0.0 and 1.0");
        
        // Verify adherence assessment consistency
        if (convention.hasGoodAdherence()) {
            assertTrue(convention.getAdherenceScore() >= 0.8,
                    "Good adherence should have score >= 0.8");
        }
        
        if (convention.needsAttention()) {
            assertTrue(convention.isEnforced(),
                    "Only enforced conventions should need attention");
            assertTrue(convention.getAdherenceScore() < 0.7,
                    "Conventions needing attention should have score < 0.7");
        }
        
        if (convention.isCritical()) {
            assertTrue(convention.isEnforced(),
                    "Critical conventions should be enforced");
            assertTrue(convention.getType() == CodingConvention.ConventionType.SECURITY ||
                      convention.getType() == CodingConvention.ConventionType.TESTING,
                    "Critical conventions should be security or testing related");
        }
    }

    /**
     * Property: Analysis results maintain quality assessment consistency
     * For any analysis result, the quality score and status should be
     * consistent with the issues found and complexity metrics.
     */
    @Property(tries = 100)
    @Label("Analysis results maintain quality assessment consistency")
    void analysisResultsMaintainQualityAssessmentConsistency(
            @ForAll("validAnalysisResults") AnalysisResult result) {
        
        // Then: Quality assessment consistency is maintained
        assertNotNull(result.getAnalysisId(), "Analysis ID should not be null");
        assertNotNull(result.getFilePath(), "File path should not be null");
        assertNotNull(result.getStatus(), "Analysis status should not be null");
        assertNotNull(result.getIssues(), "Issues list should not be null");
        assertNotNull(result.getSuggestions(), "Suggestions list should not be null");
        assertNotNull(result.getComplexityMetrics(), "Complexity metrics should not be null");
        
        assertTrue(result.getAnalysisTimeMs() >= 0,
                "Analysis time should be non-negative");
        
        double qualityScore = result.getQualityScore();
        assertTrue(qualityScore >= 0.0 && qualityScore <= 1.0,
                "Quality score should be between 0.0 and 1.0");
        
        // Verify status consistency
        if (result.getStatus() == AnalysisResult.AnalysisStatus.ERROR) {
            assertEquals(0.0, qualityScore,
                    "Error status should result in zero quality score");
        }
        
        if (result.hasCriticalIssues()) {
            assertTrue(result.getIssueCountBySeverity(CodeIssue.Severity.ERROR) > 0,
                    "Critical issues should include at least one error");
            assertFalse(result.isSuccessful(),
                    "Results with critical issues should not be successful");
        }
        
        if (result.isSuccessful()) {
            assertEquals(AnalysisResult.AnalysisStatus.SUCCESS, result.getStatus(),
                    "Successful results should have SUCCESS status");
            assertFalse(result.hasCriticalIssues(),
                    "Successful results should not have critical issues");
        }
        
        // Verify issue count consistency
        long totalIssues = result.getIssueCountBySeverity(CodeIssue.Severity.ERROR) +
                          result.getIssueCountBySeverity(CodeIssue.Severity.WARNING) +
                          result.getIssueCountBySeverity(CodeIssue.Severity.INFO);
        assertEquals(totalIssues, result.getIssues().size(),
                "Total issue count should match actual issues list size");
    }

    // Generators for property-based testing

    @Provide
    Arbitrary<ProjectContext> validProjectContexts() {
        return Combinators.combine(
                validProjectIds(),
                validProjectStructures(),
                validDependencyLists(),
                validCodePatternLists(),
                validCodingConventionLists(),
                validComplexityMetrics()
        ).as(ProjectContext::new);
    }

    @Provide
    Arbitrary<String> validProjectIds() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .withChars('-', '_')
                .ofMinLength(3)
                .ofMaxLength(50);
    }

    @Provide
    Arbitrary<ProjectStructure> validProjectStructures() {
        return Combinators.combine(
                validFileNodeLists(),
                validModuleDefinitionLists(),
                validRelationshipLists(),
                validEntryPointLists()
        ).as(ProjectStructure::new);
    }

    @Provide
    Arbitrary<List<FileNode>> validFileNodeLists() {
        return Arbitraries.of(
                List.of(new FileNode("src/Main.java", "Main.java", "java", 1000, false)),
                List.of(
                    new FileNode("src/Main.java", "Main.java", "java", 1000, false),
                    new FileNode("src/Utils.java", "Utils.java", "java", 500, false)
                ),
                new ArrayList<>()
        );
    }

    @Provide
    Arbitrary<List<ModuleDefinition>> validModuleDefinitionLists() {
        return Arbitraries.of(
                List.of(ModuleDefinition.application("main", "src", "1.0.0")),
                List.of(
                    ModuleDefinition.application("main", "src", "1.0.0"),
                    ModuleDefinition.library("utils", "src/utils")
                ),
                new ArrayList<>()
        );
    }

    @Provide
    Arbitrary<List<Relationship>> validRelationshipLists() {
        return Arbitraries.of(
                List.of(Relationship.dependsOn("Main", "Utils")),
                List.of(
                    Relationship.dependsOn("Main", "Utils"),
                    Relationship.uses("Utils", "Config")
                ),
                new ArrayList<>()
        );
    }

    @Provide
    Arbitrary<List<String>> validEntryPointLists() {
        return Arbitraries.of(
                List.of("src/Main.java"),
                List.of("src/Main.java", "src/App.java"),
                new ArrayList<>()
        );
    }

    @Provide
    Arbitrary<List<Dependency>> validDependencyLists() {
        return Arbitraries.of(
                List.of(Dependency.framework("Spring Boot", "3.2.0")),
                List.of(
                    Dependency.framework("Spring Boot", "3.2.0"),
                    Dependency.testDependency("JUnit", "5.10.0")
                ),
                new ArrayList<>()
        );
    }

    @Provide
    Arbitrary<List<CodePattern>> validCodePatternLists() {
        return Arbitraries.of(
                List.of(CodePattern.designPattern("Singleton", CodePattern.PatternCategory.CREATIONAL, "Config")),
                List.of(
                    CodePattern.designPattern("Singleton", CodePattern.PatternCategory.CREATIONAL, "Config"),
                    CodePattern.antiPattern("God Class", "UserService", "Split into smaller classes")
                ),
                new ArrayList<>()
        );
    }

    @Provide
    Arbitrary<List<CodingConvention>> validCodingConventionLists() {
        return Arbitraries.of(
                List.of(CodingConvention.namingConvention("CamelCase", "Use camelCase", "myVariable")),
                List.of(
                    CodingConvention.namingConvention("CamelCase", "Use camelCase", "myVariable"),
                    CodingConvention.formattingConvention("Indentation", "Use 4 spaces")
                ),
                new ArrayList<>()
        );
    }

    @Provide
    Arbitrary<ComplexityMetrics> validComplexityMetrics() {
        return Combinators.combine(
                Arbitraries.integers().between(0, 50),
                Arbitraries.integers().between(0, 2000),
                Arbitraries.integers().between(0, 60),
                Arbitraries.integers().between(0, 15),
                Arbitraries.integers().between(0, 100),
                Arbitraries.integers().between(0, 20)
        ).as((cyclomatic, loc, cognitive, nesting, methods, classes) ->
                new ComplexityMetrics(cyclomatic, loc, cognitive, nesting, methods, classes, null));
    }

    @Provide
    Arbitrary<Dependency> validDependencies() {
        return Arbitraries.of(
                Dependency.framework("Spring Boot", "3.2.0"),
                Dependency.externalLibrary("Jackson", "2.16.0"),
                Dependency.testDependency("JUnit", "5.10.0"),
                Dependency.internalModule("utils")
        );
    }

    @Provide
    Arbitrary<CodePattern> validCodePatterns() {
        return Arbitraries.of(
                CodePattern.designPattern("Singleton", CodePattern.PatternCategory.CREATIONAL, "Config"),
                CodePattern.antiPattern("God Class", "UserService", "Split class"),
                CodePattern.codeSmell("Long Method", "processData", "Extract methods"),
                CodePattern.bestPractice("Dependency Injection", "UserController")
        );
    }

    @Provide
    Arbitrary<CodingConvention> validCodingConventions() {
        return Arbitraries.of(
                CodingConvention.namingConvention("CamelCase", "Use camelCase", "myVariable"),
                CodingConvention.formattingConvention("Indentation", "Use 4 spaces"),
                CodingConvention.documentationConvention("Javadoc", "Document public methods"),
                CodingConvention.testingConvention("Test Coverage", "Maintain 80% coverage")
        );
    }

    @Provide
    Arbitrary<AnalysisResult> validAnalysisResults() {
        return Arbitraries.of(
                AnalysisResult.success("Test.java", ComplexityMetrics.simple(100, 5), List.of(), 50),
                AnalysisResult.withIssues("Test.java", 
                    List.of(CodeIssue.performanceWarning("Slow loop", "Test.java", 10, "Use streams")),
                    ComplexityMetrics.simple(200, 8), 75),
                AnalysisResult.error("Test.java", "Parse error")
        );
    }
}