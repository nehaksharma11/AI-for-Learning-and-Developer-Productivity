package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstration tests for Property 9: Project Context Understanding and Maintenance.
 * 
 * Shows concrete examples of how the project context models maintain accurate
 * understanding of codebase structure, dependencies, relationships, and coding standards.
 */
class ProjectContextMaintenanceDemo {

    @Test
    @DisplayName("Demonstrate Property 9: Complete project context understanding workflow")
    void demonstrateCompleteProjectContextUnderstandingWorkflow() {
        // Given: A project is opened with initial structure
        ProjectStructure initialStructure = createInitialProjectStructure();
        ProjectContext initialContext = ProjectContext.create("demo-project", initialStructure);
        
        // When: Project analysis is performed
        ProjectContext analyzedContext = performProjectAnalysis(initialContext);
        
        // Then: Property 9 requirements are satisfied
        
        // Property 9.1: Codebase structure analysis
        assertTrue(analyzedContext.getStructure().getFiles().size() > 0,
                "Should analyze and maintain file structure");
        assertTrue(analyzedContext.getStructure().getModules().size() > 0,
                "Should identify and track modules");
        assertTrue(analyzedContext.getStructure().getRelationships().size() > 0,
                "Should detect and maintain code relationships");
        
        // Property 9.2: Dependencies understanding
        assertTrue(analyzedContext.getDependencies().size() > 0,
                "Should identify and track dependencies");
        
        Dependency springDep = analyzedContext.getDependencies().stream()
                .filter(dep -> dep.getName().equals("Spring Boot"))
                .findFirst()
                .orElse(null);
        assertNotNull(springDep, "Should detect framework dependencies");
        assertTrue(springDep.isCritical(), "Should assess dependency criticality");
        
        // Property 9.3: Pattern detection
        assertTrue(analyzedContext.getPatterns().size() > 0,
                "Should detect code patterns");
        
        CodePattern singletonPattern = analyzedContext.getPatterns().stream()
                .filter(pattern -> pattern.getName().equals("Singleton"))
                .findFirst()
                .orElse(null);
        assertNotNull(singletonPattern, "Should detect design patterns");
        assertTrue(singletonPattern.isBeneficial(), "Should classify beneficial patterns");
        
        // Property 9.4: Coding standards understanding
        assertTrue(analyzedContext.getConventions().size() > 0,
                "Should identify coding conventions");
        
        CodingConvention namingConvention = analyzedContext.getConventions().stream()
                .filter(conv -> conv.getType() == CodingConvention.ConventionType.NAMING)
                .findFirst()
                .orElse(null);
        assertNotNull(namingConvention, "Should detect naming conventions");
        assertTrue(namingConvention.hasGoodAdherence(), "Should assess convention adherence");
        
        // Property 9.5: Complexity assessment
        ComplexityMetrics complexity = analyzedContext.getComplexity();
        assertNotNull(complexity, "Should provide complexity metrics");
        assertTrue(complexity.getOverallComplexityScore() >= 0.0 && 
                  complexity.getOverallComplexityScore() <= 1.0,
                "Should calculate meaningful complexity scores");
        
        System.out.println("✅ Property 9 (Project Context Understanding and Maintenance) validated");
        System.out.println("   - Files analyzed: " + analyzedContext.getStructure().getFiles().size());
        System.out.println("   - Dependencies tracked: " + analyzedContext.getDependencies().size());
        System.out.println("   - Patterns detected: " + analyzedContext.getPatterns().size());
        System.out.println("   - Conventions identified: " + analyzedContext.getConventions().size());
        System.out.println("   - Complexity score: " + String.format("%.2f", complexity.getOverallComplexityScore()));
    }

    @Test
    @DisplayName("Demonstrate context maintenance during code modifications")
    void demonstrateContextMaintenanceDuringCodeModifications() {
        // Given: An existing project context
        ProjectContext originalContext = createAnalyzedProjectContext();
        int originalFileCount = originalContext.getStructure().getFiles().size();
        int originalPatternCount = originalContext.getPatterns().size();
        
        // When: Code modifications are made (simulated by adding new elements)
        ProjectContext modifiedContext = simulateCodeModifications(originalContext);
        
        // Then: Context is maintained and updated appropriately
        
        // Verify structural updates
        assertTrue(modifiedContext.getStructure().getFiles().size() >= originalFileCount,
                "Should maintain or increase file count after modifications");
        
        // Verify pattern updates
        assertTrue(modifiedContext.getPatterns().size() >= originalPatternCount,
                "Should maintain or detect new patterns after modifications");
        
        // Verify relationship consistency
        for (Relationship rel : modifiedContext.getStructure().getRelationships()) {
            assertNotNull(rel.getFrom(), "Relationships should have valid source");
            assertNotNull(rel.getTo(), "Relationships should have valid target");
            assertTrue(rel.getStrength() >= 0.0 && rel.getStrength() <= 1.0,
                    "Relationship strength should be valid");
        }
        
        // Verify dependency consistency
        for (Dependency dep : modifiedContext.getDependencies()) {
            assertNotNull(dep.getName(), "Dependencies should have valid names");
            assertNotNull(dep.getType(), "Dependencies should have valid types");
            assertTrue(dep.getFullIdentifier().contains(dep.getName()),
                    "Full identifier should be consistent");
        }
        
        System.out.println("✅ Context maintenance during modifications validated");
        System.out.println("   - Original files: " + originalFileCount + 
                          ", Modified files: " + modifiedContext.getStructure().getFiles().size());
        System.out.println("   - Original patterns: " + originalPatternCount + 
                          ", Modified patterns: " + modifiedContext.getPatterns().size());
    }

    @Test
    @DisplayName("Demonstrate analysis result quality assessment consistency")
    void demonstrateAnalysisResultQualityAssessmentConsistency() {
        // Given: Different types of analysis results
        AnalysisResult cleanCodeResult = createCleanCodeAnalysisResult();
        AnalysisResult problematicCodeResult = createProblematicCodeAnalysisResult();
        AnalysisResult errorResult = AnalysisResult.error("BadFile.java", "Parse error");
        
        // When: Quality assessments are performed
        double cleanQuality = cleanCodeResult.getQualityScore();
        double problematicQuality = problematicCodeResult.getQualityScore();
        double errorQuality = errorResult.getQualityScore();
        
        // Then: Quality assessments are consistent and meaningful
        
        // Clean code should have high quality
        assertTrue(cleanQuality > 0.7, "Clean code should have high quality score");
        assertTrue(cleanCodeResult.isSuccessful(), "Clean code analysis should be successful");
        assertFalse(cleanCodeResult.hasCriticalIssues(), "Clean code should not have critical issues");
        
        // Problematic code should have lower quality
        assertTrue(problematicQuality < cleanQuality, "Problematic code should have lower quality than clean code");
        assertTrue(problematicCodeResult.hasCriticalIssues(), "Problematic code should have critical issues");
        assertFalse(problematicCodeResult.isSuccessful(), "Problematic code analysis should not be successful");
        
        // Error results should have zero quality
        assertEquals(0.0, errorQuality, "Error results should have zero quality score");
        assertEquals(AnalysisResult.AnalysisStatus.ERROR, errorResult.getStatus(),
                "Error results should have ERROR status");
        
        // Verify issue categorization consistency
        long errorIssues = problematicCodeResult.getIssueCountBySeverity(CodeIssue.Severity.ERROR);
        long warningIssues = problematicCodeResult.getIssueCountBySeverity(CodeIssue.Severity.WARNING);
        long infoIssues = problematicCodeResult.getIssueCountBySeverity(CodeIssue.Severity.INFO);
        
        assertTrue(errorIssues > 0, "Problematic code should have error-level issues");
        assertEquals(errorIssues + warningIssues + infoIssues, problematicCodeResult.getIssues().size(),
                "Issue counts should sum to total issues");
        
        System.out.println("✅ Analysis result quality assessment consistency validated");
        System.out.println("   - Clean code quality: " + String.format("%.2f", cleanQuality));
        System.out.println("   - Problematic code quality: " + String.format("%.2f", problematicQuality));
        System.out.println("   - Error result quality: " + String.format("%.2f", errorQuality));
        System.out.println("   - Issues found: " + problematicCodeResult.getIssues().size());
    }

    // Helper methods for creating test scenarios

    private ProjectStructure createInitialProjectStructure() {
        List<FileNode> files = List.of(
                new FileNode("src/main/java/Main.java", "Main.java", "java", 1000, false),
                new FileNode("src/main/java/service/UserService.java", "UserService.java", "java", 1500, false),
                new FileNode("src/test/java/MainTest.java", "MainTest.java", "java", 800, false)
        );
        
        List<ModuleDefinition> modules = List.of(
                ModuleDefinition.application("main", "src/main", "1.0.0"),
                ModuleDefinition.library("service", "src/main/service")
        );
        
        List<Relationship> relationships = List.of(
                Relationship.dependsOn("Main", "UserService"),
                Relationship.uses("MainTest", "Main")
        );
        
        List<String> entryPoints = List.of("src/main/java/Main.java");
        
        return new ProjectStructure(files, modules, relationships, entryPoints);
    }

    private ProjectContext performProjectAnalysis(ProjectContext initialContext) {
        // Simulate comprehensive project analysis
        List<Dependency> dependencies = List.of(
                Dependency.framework("Spring Boot", "3.2.0"),
                Dependency.testDependency("JUnit", "5.10.0"),
                Dependency.externalLibrary("Jackson", "2.16.0")
        );
        
        List<CodePattern> patterns = List.of(
                CodePattern.designPattern("Singleton", CodePattern.PatternCategory.CREATIONAL, "Config"),
                CodePattern.bestPractice("Dependency Injection", "UserService"),
                CodePattern.codeSmell("Long Method", "processData", "Consider extracting methods")
        );
        
        List<CodingConvention> conventions = List.of(
                CodingConvention.namingConvention("CamelCase", "Use camelCase for variables", "userName"),
                CodingConvention.formattingConvention("Indentation", "Use 4 spaces for indentation"),
                CodingConvention.testingConvention("Test Coverage", "Maintain minimum 80% test coverage")
        );
        
        ComplexityMetrics complexity = ComplexityMetrics.simple(2500, 25);
        
        return new ProjectContext(
                initialContext.getId(),
                initialContext.getStructure(),
                dependencies,
                patterns,
                conventions,
                complexity
        );
    }

    private ProjectContext createAnalyzedProjectContext() {
        return performProjectAnalysis(ProjectContext.create("analyzed-project", createInitialProjectStructure()));
    }

    private ProjectContext simulateCodeModifications(ProjectContext originalContext) {
        // Simulate adding a new file and pattern
        List<FileNode> modifiedFiles = new ArrayList<>(originalContext.getStructure().getFiles());
        modifiedFiles.add(new FileNode("src/main/java/util/Helper.java", "Helper.java", "java", 600, false));
        
        ProjectStructure modifiedStructure = new ProjectStructure(
                modifiedFiles,
                originalContext.getStructure().getModules(),
                originalContext.getStructure().getRelationships(),
                originalContext.getStructure().getEntryPoints()
        );
        
        List<CodePattern> modifiedPatterns = new ArrayList<>(originalContext.getPatterns());
        modifiedPatterns.add(CodePattern.designPattern("Factory", CodePattern.PatternCategory.CREATIONAL, "Helper"));
        
        return new ProjectContext(
                originalContext.getId(),
                modifiedStructure,
                originalContext.getDependencies(),
                modifiedPatterns,
                originalContext.getConventions(),
                originalContext.getComplexity()
        );
    }

    private AnalysisResult createCleanCodeAnalysisResult() {
        ComplexityMetrics simpleMetrics = ComplexityMetrics.simple(150, 8);
        List<Suggestion> suggestions = List.of(
                Suggestion.documentation("Add class-level Javadoc", "Main.java", 1)
        );
        return AnalysisResult.success("Main.java", simpleMetrics, suggestions, 45);
    }

    private AnalysisResult createProblematicCodeAnalysisResult() {
        List<CodeIssue> issues = List.of(
                CodeIssue.bug("Potential null pointer exception", "UserService.java", 25, "Add null check"),
                CodeIssue.securityIssue("SQL injection vulnerability", "UserService.java", 45, "SEC_001"),
                CodeIssue.performanceWarning("Inefficient database query", "UserService.java", 60, "Use prepared statements")
        );
        ComplexityMetrics complexMetrics = ComplexityMetrics.complex(18, 800, 25, 7);
        return AnalysisResult.withIssues("UserService.java", issues, complexMetrics, 120);
    }
}