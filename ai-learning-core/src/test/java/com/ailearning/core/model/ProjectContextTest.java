package com.ailearning.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProjectContext model.
 */
class ProjectContextTest {

    @Nested
    @DisplayName("Project Context Creation")
    class ProjectContextCreation {

        @Test
        @DisplayName("Should create project context with valid parameters")
        void shouldCreateProjectContextWithValidParameters() {
            // Given
            String projectId = "test-project";
            ProjectStructure structure = ProjectStructure.empty();

            // When
            ProjectContext context = ProjectContext.create(projectId, structure);

            // Then
            assertNotNull(context);
            assertEquals(projectId, context.getId());
            assertEquals(structure, context.getStructure());
            assertTrue(context.getDependencies().isEmpty());
            assertTrue(context.getPatterns().isEmpty());
            assertTrue(context.getConventions().isEmpty());
            assertNotNull(context.getComplexity());
        }

        @Test
        @DisplayName("Should throw exception for null project ID")
        void shouldThrowExceptionForNullProjectId() {
            // Given
            ProjectStructure structure = ProjectStructure.empty();

            // When & Then
            assertThrows(NullPointerException.class, () -> 
                ProjectContext.create(null, structure));
        }

        @Test
        @DisplayName("Should throw exception for null project structure")
        void shouldThrowExceptionForNullProjectStructure() {
            // Given
            String projectId = "test-project";

            // When & Then
            assertThrows(NullPointerException.class, () -> 
                ProjectContext.create(projectId, null));
        }
    }

    @Nested
    @DisplayName("Project Context Collections")
    class ProjectContextCollections {

        @Test
        @DisplayName("Should handle dependencies correctly")
        void shouldHandleDependenciesCorrectly() {
            // Given
            ProjectContext context = createTestProjectContext();
            Dependency springDep = Dependency.framework("Spring Boot", "3.2.0");
            List<Dependency> dependencies = List.of(springDep);

            // When
            ProjectContext contextWithDeps = new ProjectContext(
                context.getId(), context.getStructure(), dependencies,
                context.getPatterns(), context.getConventions(), context.getComplexity()
            );

            // Then
            assertEquals(1, contextWithDeps.getDependencies().size());
            assertTrue(contextWithDeps.getDependencies().contains(springDep));
            
            // Verify immutability
            List<Dependency> retrievedDeps = contextWithDeps.getDependencies();
            assertNotSame(dependencies, retrievedDeps);
        }

        @Test
        @DisplayName("Should handle patterns correctly")
        void shouldHandlePatternsCorrectly() {
            // Given
            ProjectContext context = createTestProjectContext();
            CodePattern singletonPattern = CodePattern.designPattern("Singleton", 
                CodePattern.PatternCategory.CREATIONAL, "com.example.Config");
            List<CodePattern> patterns = List.of(singletonPattern);

            // When
            ProjectContext contextWithPatterns = new ProjectContext(
                context.getId(), context.getStructure(), context.getDependencies(),
                patterns, context.getConventions(), context.getComplexity()
            );

            // Then
            assertEquals(1, contextWithPatterns.getPatterns().size());
            assertTrue(contextWithPatterns.getPatterns().contains(singletonPattern));
        }

        @Test
        @DisplayName("Should handle conventions correctly")
        void shouldHandleConventionsCorrectly() {
            // Given
            ProjectContext context = createTestProjectContext();
            CodingConvention namingConvention = CodingConvention.namingConvention(
                "CamelCase", "Use camelCase for variables", "myVariable");
            List<CodingConvention> conventions = List.of(namingConvention);

            // When
            ProjectContext contextWithConventions = new ProjectContext(
                context.getId(), context.getStructure(), context.getDependencies(),
                context.getPatterns(), conventions, context.getComplexity()
            );

            // Then
            assertEquals(1, contextWithConventions.getConventions().size());
            assertTrue(contextWithConventions.getConventions().contains(namingConvention));
        }
    }

    @Nested
    @DisplayName("Equality and Hash Code")
    class EqualityAndHashCode {

        @Test
        @DisplayName("Should be equal when IDs are same")
        void shouldBeEqualWhenIdsAreSame() {
            // Given
            ProjectContext context1 = createTestProjectContext();
            ProjectContext context2 = ProjectContext.create("test-project", ProjectStructure.empty());

            // When & Then
            assertEquals(context1, context2);
            assertEquals(context1.hashCode(), context2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            // Given
            ProjectContext context1 = createTestProjectContext();
            ProjectContext context2 = ProjectContext.create("different-project", ProjectStructure.empty());

            // When & Then
            assertNotEquals(context1, context2);
        }
    }

    private ProjectContext createTestProjectContext() {
        return ProjectContext.create("test-project", ProjectStructure.empty());
    }
}