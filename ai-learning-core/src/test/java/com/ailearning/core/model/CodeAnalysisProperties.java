package com.ailearning.core.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for code analysis models (CodeIssue, Suggestion, etc.).
 * 
 * Feature: ai-learning-companion, Property 9: Project Context Understanding and Maintenance
 * Validates: Requirements 5.1, 5.2, 5.4
 * 
 * Tests properties related to code analysis consistency and correctness.
 */
class CodeAnalysisProperties {

    /**
     * Property: Code issues maintain severity and category consistency
     * For any code issue, the severity level should be consistent with
     * the issue category and the criticality assessment should be accurate.
     */
    @Property(tries = 100)
    @Label("Code issues maintain severity and category consistency")
    void codeIssuesMaintainSeverityAndCategoryConsistency(
            @ForAll("validCodeIssues") CodeIssue issue) {
        
        // Then: Severity and category consistency is maintained
        assertNotNull(issue.getId(), "Issue ID should not be null");
        assertNotNull(issue.getMessage(), "Issue message should not be null");
        assertNotNull(issue.getSeverity(), "Issue severity should not be null");
        assertNotNull(issue.getCategory(), "Issue category should not be null");
        assertNotNull(issue.getFilePath(), "File path should not be null");
        
        assertTrue(issue.getLineNumber() >= 1, "Line number should be at least 1");
        assertTrue(issue.getColumnNumber() >= 0, "Column number should be non-negative");
        
        // Verify location string consistency
        String location = issue.getLocationString();
        assertTrue(location.contains(issue.getFilePath()),
                "Location string should contain file path");
        assertTrue(location.contains(String.valueOf(issue.getLineNumber())),
                "Location string should contain line number");
        
        // Verify criticality assessment consistency
        if (issue.isCritical()) {
            assertEquals(CodeIssue.Severity.ERROR, issue.getSeverity(),
                    "Critical issues should have ERROR severity");
            assertTrue(issue.getCategory() == CodeIssue.Category.BUG ||
                      issue.getCategory() == CodeIssue.Category.SECURITY,
                    "Critical issues should be bugs or security issues");
        }
        
        // Verify maintainability impact consistency
        if (issue.affectsMaintainability()) {
            assertTrue(issue.getCategory() == CodeIssue.Category.MAINTAINABILITY ||
                      issue.getCategory() == CodeIssue.Category.STYLE ||
                      issue.getCategory() == CodeIssue.Category.DOCUMENTATION,
                    "Maintainability issues should have appropriate categories");
        }
        
        // Verify severity-category relationships
        if (issue.getCategory() == CodeIssue.Category.SECURITY) {
            assertEquals(CodeIssue.Severity.ERROR, issue.getSeverity(),
                    "Security issues should typically be errors");
        }
        
        if (issue.getCategory() == CodeIssue.Category.STYLE) {
            assertEquals(CodeIssue.Severity.INFO, issue.getSeverity(),
                    "Style issues should typically be informational");
        }
    }

    /**
     * Property: Suggestions maintain priority and type consistency
     * For any suggestion, the priority level should be consistent with
     * the suggestion type and urgency assessment should be accurate.
     */
    @Property(tries = 100)
    @Label("Suggestions maintain priority and type consistency")
    void suggestionsMaintainPriorityAndTypeConsistency(
            @ForAll("validSuggestions") Suggestion suggestion) {
        
        // Then: Priority and type consistency is maintained
        assertNotNull(suggestion.getId(), "Suggestion ID should not be null");
        assertNotNull(suggestion.getDescription(), "Description should not be null");
        assertNotNull(suggestion.getType(), "Suggestion type should not be null");
        assertNotNull(suggestion.getPriority(), "Priority should not be null");
        
        assertTrue(suggestion.getLineNumber() >= 0, "Line number should be non-negative");
        
        // Verify location string consistency
        String location = suggestion.getLocationString();
        assertNotNull(location, "Location string should not be null");
        
        // Verify urgency assessment consistency
        if (suggestion.isUrgent()) {
            assertTrue(suggestion.getPriority() == Suggestion.Priority.CRITICAL ||
                      (suggestion.getPriority() == Suggestion.Priority.HIGH &&
                       suggestion.getType() == Suggestion.SuggestionType.SECURITY),
                    "Urgent suggestions should have critical priority or be high-priority security suggestions");
        }
        
        // Verify learning opportunity assessment consistency
        if (suggestion.isLearningOpportunity()) {
            assertTrue(suggestion.getType() == Suggestion.SuggestionType.BEST_PRACTICE ||
                      suggestion.getType() == Suggestion.SuggestionType.DOCUMENTATION ||
                      suggestion.getLearnMoreUrl() != null,
                    "Learning opportunities should be best practices, documentation, or have learn more URLs");
        }
        
        // Verify type-priority relationships
        if (suggestion.getType() == Suggestion.SuggestionType.SECURITY) {
            assertEquals(Suggestion.Priority.CRITICAL, suggestion.getPriority(),
                    "Security suggestions should have critical priority");
        }
        
        if (suggestion.getType() == Suggestion.SuggestionType.DOCUMENTATION) {
            assertEquals(Suggestion.Priority.LOW, suggestion.getPriority(),
                    "Documentation suggestions should typically have low priority");
        }
        
        if (suggestion.getType() == Suggestion.SuggestionType.TESTING) {
            assertEquals(Suggestion.Priority.HIGH, suggestion.getPriority(),
                    "Testing suggestions should have high priority");
        }
    }

    /**
     * Property: Module definitions maintain dependency consistency
     * For any module definition, the dependencies and exports should be
     * consistent with the module type and characteristics.
     */
    @Property(tries = 100)
    @Label("Module definitions maintain dependency consistency")
    void moduleDefinitionsMaintainDependencyConsistency(
            @ForAll("validModuleDefinitions") ModuleDefinition module) {
        
        // Then: Dependency consistency is maintained
        assertNotNull(module.getName(), "Module name should not be null");
        assertNotNull(module.getPath(), "Module path should not be null");
        assertNotNull(module.getType(), "Module type should not be null");
        assertNotNull(module.getDependencies(), "Dependencies list should not be null");
        assertNotNull(module.getExports(), "Exports list should not be null");
        
        // Verify immutability
        List<String> deps1 = module.getDependencies();
        List<String> deps2 = module.getDependencies();
        assertNotSame(deps1, deps2, "Dependencies should return new instances");
        assertEquals(deps1, deps2, "Dependencies content should be consistent");
        
        List<String> exports1 = module.getExports();
        List<String> exports2 = module.getExports();
        assertNotSame(exports1, exports2, "Exports should return new instances");
        assertEquals(exports1, exports2, "Exports content should be consistent");
        
        // Verify type-specific characteristics
        if (module.getType() == ModuleDefinition.ModuleType.APPLICATION) {
            assertNotNull(module.getVersion(), "Applications should typically have versions");
        }
        
        if (module.getType() == ModuleDefinition.ModuleType.TEST) {
            assertTrue(module.getName().contains("test") || 
                      module.getPath().contains("test"),
                    "Test modules should have 'test' in name or path");
        }
        
        // Verify builder pattern consistency
        ModuleDefinition withDep = module.withDependency("new-dep");
        assertNotSame(module, withDep, "withDependency should return new instance");
        assertEquals(module.getDependencies().size() + 1, withDep.getDependencies().size(),
                "New module should have one more dependency");
        assertTrue(withDep.getDependencies().contains("new-dep"),
                "New module should contain the added dependency");
        
        ModuleDefinition withExport = module.withExport("new-export");
        assertNotSame(module, withExport, "withExport should return new instance");
        assertEquals(module.getExports().size() + 1, withExport.getExports().size(),
                "New module should have one more export");
        assertTrue(withExport.getExports().contains("new-export"),
                "New module should contain the added export");
    }

    /**
     * Property: Relationships maintain bidirectionality and strength consistency
     * For any relationship, the bidirectionality assessment and strength
     * should be consistent with the relationship type.
     */
    @Property(tries = 100)
    @Label("Relationships maintain bidirectionality and strength consistency")
    void relationshipsMaintainBidirectionalityAndStrengthConsistency(
            @ForAll("validRelationships") Relationship relationship) {
        
        // Then: Bidirectionality and strength consistency is maintained
        assertNotNull(relationship.getFrom(), "From element should not be null");
        assertNotNull(relationship.getTo(), "To element should not be null");
        assertNotNull(relationship.getType(), "Relationship type should not be null");
        
        assertTrue(relationship.getStrength() >= 0.0 && relationship.getStrength() <= 1.0,
                "Relationship strength should be between 0.0 and 1.0");
        
        // Verify bidirectionality consistency
        if (relationship.isBidirectional()) {
            assertTrue(relationship.getType() == Relationship.RelationshipType.USES ||
                      relationship.getType() == Relationship.RelationshipType.REFERENCES,
                    "Bidirectional relationships should be USES or REFERENCES");
        }
        
        // Verify strong coupling assessment consistency
        if (relationship.isStrongCoupling()) {
            assertTrue(relationship.getStrength() > 0.7,
                    "Strong coupling should have high strength");
            assertTrue(relationship.getType() == Relationship.RelationshipType.INHERITS_FROM ||
                      relationship.getType() == Relationship.RelationshipType.DEPENDS_ON,
                    "Strong coupling should be inheritance or dependency");
        }
        
        // Verify type-strength relationships
        if (relationship.getType() == Relationship.RelationshipType.INHERITS_FROM) {
            assertEquals(1.0, relationship.getStrength(), 0.001,
                    "Inheritance relationships should have maximum strength");
        }
        
        if (relationship.getType() == Relationship.RelationshipType.USES) {
            assertTrue(relationship.getStrength() <= 0.8,
                    "Usage relationships should have moderate strength");
        }
    }

    /**
     * Property: File nodes maintain path and language consistency
     * For any file node, the path, name, and language should be consistent
     * with file system conventions and language detection rules.
     */
    @Property(tries = 100)
    @Label("File nodes maintain path and language consistency")
    void fileNodesMaintainPathAndLanguageConsistency(
            @ForAll("validFileNodes") FileNode fileNode) {
        
        // Then: Path and language consistency is maintained
        assertNotNull(fileNode.getPath(), "File path should not be null");
        assertNotNull(fileNode.getName(), "File name should not be null");
        assertTrue(fileNode.getSize() >= 0, "File size should be non-negative");
        
        // Verify path-name consistency
        assertTrue(fileNode.getPath().endsWith(fileNode.getName()) ||
                  fileNode.getPath().contains(fileNode.getName()),
                "Path should contain or end with the file name");
        
        // Verify language detection consistency
        if (fileNode.getLanguage() != null) {
            String name = fileNode.getName().toLowerCase();
            String language = fileNode.getLanguage().toLowerCase();
            
            if (language.equals("java")) {
                assertTrue(name.endsWith(".java"), "Java files should have .java extension");
            } else if (language.equals("python")) {
                assertTrue(name.endsWith(".py"), "Python files should have .py extension");
            } else if (language.equals("javascript")) {
                assertTrue(name.endsWith(".js") || name.endsWith(".mjs"),
                        "JavaScript files should have .js or .mjs extension");
            } else if (language.equals("typescript")) {
                assertTrue(name.endsWith(".ts"), "TypeScript files should have .ts extension");
            }
        }
        
        // Verify directory consistency
        if (fileNode.isDirectory()) {
            assertEquals(0, fileNode.getSize(), "Directories should have zero size");
            assertNull(fileNode.getLanguage(), "Directories should not have a language");
        } else {
            assertTrue(fileNode.getSize() >= 0, "Files should have non-negative size");
        }
    }

    // Generators for property-based testing

    @Provide
    Arbitrary<CodeIssue> validCodeIssues() {
        return Arbitraries.of(
                CodeIssue.bug("Null pointer exception", "Test.java", 10, "Add null check"),
                CodeIssue.securityIssue("SQL injection", "UserService.java", 25, "SEC_001"),
                CodeIssue.performanceWarning("Inefficient loop", "Utils.java", 15, "Use streams"),
                CodeIssue.codeSmell("Long method", "Controller.java", 50, "Extract methods"),
                CodeIssue.styleViolation("Missing space", "Config.java", 5, "STYLE_001")
        );
    }

    @Provide
    Arbitrary<Suggestion> validSuggestions() {
        return Arbitraries.of(
                Suggestion.refactoring("Extract method", "Service.java", 20, "public void extracted() {}", "Improves readability"),
                Suggestion.optimization("Use StringBuilder", "Utils.java", 30, "StringBuilder is more efficient"),
                Suggestion.bestPractice("Add validation", "Improves robustness", "https://example.com/validation"),
                Suggestion.security("Sanitize input", "Controller.java", 15, "Prevents injection attacks"),
                Suggestion.documentation("Add Javadoc", "Service.java", 10),
                Suggestion.testing("Add unit test", "Service.java", "Improves code coverage")
        );
    }

    @Provide
    Arbitrary<ModuleDefinition> validModuleDefinitions() {
        return Arbitraries.of(
                ModuleDefinition.application("main-app", "src/main", "1.0.0"),
                ModuleDefinition.library("utils-lib", "src/utils"),
                new ModuleDefinition("test-module", "src/test", ModuleDefinition.ModuleType.TEST,
                        List.of("junit"), List.of(), "Test module", null),
                new ModuleDefinition("config-module", "src/config", ModuleDefinition.ModuleType.CONFIGURATION,
                        List.of(), List.of("config"), "Configuration module", null)
        );
    }

    @Provide
    Arbitrary<Relationship> validRelationships() {
        return Arbitraries.of(
                Relationship.dependsOn("ServiceA", "ServiceB"),
                Relationship.inheritsFrom("ChildClass", "ParentClass"),
                Relationship.uses("Controller", "Service"),
                Relationship.calls("MethodA", "MethodB"),
                new Relationship("ModuleA", "ModuleB", Relationship.RelationshipType.REFERENCES, "Reference relationship", 0.5)
        );
    }

    @Provide
    Arbitrary<FileNode> validFileNodes() {
        return Arbitraries.of(
                new FileNode("src/main/java/Main.java", "Main.java", "java", 1500, false),
                new FileNode("src/test/python/test_utils.py", "test_utils.py", "python", 800, false),
                new FileNode("src/frontend/app.js", "app.js", "javascript", 2000, false),
                new FileNode("src/types/index.ts", "index.ts", "typescript", 600, false),
                new FileNode("src/main", "main", null, 0, true),
                new FileNode("README.md", "README.md", "markdown", 1200, false)
        );
    }
}