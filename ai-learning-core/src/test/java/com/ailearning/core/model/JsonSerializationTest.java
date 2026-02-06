package com.ailearning.core.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JSON serialization and deserialization for all core models.
 * Ensures caching and persistence support works correctly.
 */
class JsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("Developer Profile Serialization")
    class DeveloperProfileSerialization {

        @Test
        @DisplayName("Should serialize and deserialize DeveloperProfile correctly")
        void shouldSerializeAndDeserializeDeveloperProfileCorrectly() throws Exception {
            // Given
            DeveloperProfile original = DeveloperProfile.create("dev123", 
                LearningPreferences.defaultPreferences())
                .updateSkillLevel("Java", SkillLevel.intermediate("Java"))
                .addAchievement(Achievement.skillMilestone("Java", "Intermediate"));

            // When
            String json = objectMapper.writeValueAsString(original);
            DeveloperProfile deserialized = objectMapper.readValue(json, DeveloperProfile.class);

            // Then
            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getSkillLevels().size(), deserialized.getSkillLevels().size());
            assertEquals(original.getAchievements().size(), deserialized.getAchievements().size());
            assertEquals(original.getLearningPreferences().getPreferredLearningStyle(), 
                deserialized.getLearningPreferences().getPreferredLearningStyle());
        }

        @Test
        @DisplayName("Should serialize and deserialize SkillLevel correctly")
        void shouldSerializeAndDeserializeSkillLevelCorrectly() throws Exception {
            // Given
            SkillLevel original = SkillLevel.advanced("Python");

            // When
            String json = objectMapper.writeValueAsString(original);
            SkillLevel deserialized = objectMapper.readValue(json, SkillLevel.class);

            // Then
            assertEquals(original.getDomain(), deserialized.getDomain());
            assertEquals(original.getProficiency(), deserialized.getProficiency(), 0.001);
            assertEquals(original.getConfidence(), deserialized.getConfidence(), 0.001);
            assertEquals(original.getEvidenceCount(), deserialized.getEvidenceCount());
        }

        @Test
        @DisplayName("Should serialize and deserialize LearningPreferences correctly")
        void shouldSerializeAndDeserializeLearningPreferencesCorrectly() throws Exception {
            // Given
            LearningPreferences original = LearningPreferences.expertMode();

            // When
            String json = objectMapper.writeValueAsString(original);
            LearningPreferences deserialized = objectMapper.readValue(json, LearningPreferences.class);

            // Then
            assertEquals(original.getPreferredLearningStyle(), deserialized.getPreferredLearningStyle());
            assertEquals(original.getDifficultyPreference(), deserialized.getDifficultyPreference());
            assertEquals(original.getPreferredSessionLengthMinutes(), deserialized.getPreferredSessionLengthMinutes());
            assertEquals(original.isEnableRealTimeHints(), deserialized.isEnableRealTimeHints());
        }
    }

    @Nested
    @DisplayName("Project Context Serialization")
    class ProjectContextSerialization {

        @Test
        @DisplayName("Should serialize and deserialize ProjectContext correctly")
        void shouldSerializeAndDeserializeProjectContextCorrectly() throws Exception {
            // Given
            ProjectStructure structure = new ProjectStructure(
                List.of(new FileNode("src/Main.java", "Main.java", "java", 1000, false)),
                List.of(ModuleDefinition.application("main", "src", "1.0.0")),
                List.of(Relationship.dependsOn("Main", "Utils")),
                List.of("src/Main.java")
            );
            
            ProjectContext original = new ProjectContext(
                "test-project",
                structure,
                List.of(Dependency.framework("Spring", "6.0.0")),
                List.of(CodePattern.designPattern("Singleton", CodePattern.PatternCategory.CREATIONAL, "Config")),
                List.of(CodingConvention.namingConvention("CamelCase", "Use camelCase", "myVar")),
                ComplexityMetrics.simple(500, 20)
            );

            // When
            String json = objectMapper.writeValueAsString(original);
            ProjectContext deserialized = objectMapper.readValue(json, ProjectContext.class);

            // Then
            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getStructure().getFiles().size(), deserialized.getStructure().getFiles().size());
            assertEquals(original.getDependencies().size(), deserialized.getDependencies().size());
            assertEquals(original.getPatterns().size(), deserialized.getPatterns().size());
            assertEquals(original.getConventions().size(), deserialized.getConventions().size());
        }

        @Test
        @DisplayName("Should serialize and deserialize ComplexityMetrics correctly")
        void shouldSerializeAndDeserializeComplexityMetricsCorrectly() throws Exception {
            // Given
            ComplexityMetrics original = ComplexityMetrics.complex(15, 800, 20, 6)
                .withAdditionalMetric("testCoverage", 0.85);

            // When
            String json = objectMapper.writeValueAsString(original);
            ComplexityMetrics deserialized = objectMapper.readValue(json, ComplexityMetrics.class);

            // Then
            assertEquals(original.getCyclomaticComplexity(), deserialized.getCyclomaticComplexity());
            assertEquals(original.getLinesOfCode(), deserialized.getLinesOfCode());
            assertEquals(original.getCognitiveComplexity(), deserialized.getCognitiveComplexity());
            assertEquals(original.getNestingDepth(), deserialized.getNestingDepth());
            assertEquals(original.getAdditionalMetrics().size(), deserialized.getAdditionalMetrics().size());
        }
    }

    @Nested
    @DisplayName("Analysis Result Serialization")
    class AnalysisResultSerialization {

        @Test
        @DisplayName("Should serialize and deserialize AnalysisResult correctly")
        void shouldSerializeAndDeserializeAnalysisResultCorrectly() throws Exception {
            // Given
            List<CodeIssue> issues = List.of(
                CodeIssue.bug("Null pointer", "Test.java", 10, "Add null check"),
                CodeIssue.performanceWarning("Slow loop", "Test.java", 20, "Use streams")
            );
            List<Suggestion> suggestions = List.of(
                Suggestion.refactoring("Extract method", "Test.java", 15, "public void extracted() {}", "Improves readability")
            );
            ComplexityMetrics metrics = ComplexityMetrics.simple(200, 10);
            
            AnalysisResult original = AnalysisResult.withIssues("Test.java", issues, metrics, 100);

            // When
            String json = objectMapper.writeValueAsString(original);
            AnalysisResult deserialized = objectMapper.readValue(json, AnalysisResult.class);

            // Then
            assertEquals(original.getFilePath(), deserialized.getFilePath());
            assertEquals(original.getStatus(), deserialized.getStatus());
            assertEquals(original.getIssues().size(), deserialized.getIssues().size());
            assertEquals(original.getAnalysisTimeMs(), deserialized.getAnalysisTimeMs());
        }

        @Test
        @DisplayName("Should serialize and deserialize CodeIssue correctly")
        void shouldSerializeAndDeserializeCodeIssueCorrectly() throws Exception {
            // Given
            CodeIssue original = CodeIssue.securityIssue("SQL Injection vulnerability", 
                "UserService.java", 45, "SEC_001");

            // When
            String json = objectMapper.writeValueAsString(original);
            CodeIssue deserialized = objectMapper.readValue(json, CodeIssue.class);

            // Then
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getSeverity(), deserialized.getSeverity());
            assertEquals(original.getCategory(), deserialized.getCategory());
            assertEquals(original.getFilePath(), deserialized.getFilePath());
            assertEquals(original.getLineNumber(), deserialized.getLineNumber());
            assertEquals(original.getRuleId(), deserialized.getRuleId());
        }

        @Test
        @DisplayName("Should serialize and deserialize Suggestion correctly")
        void shouldSerializeAndDeserializeSuggestionCorrectly() throws Exception {
            // Given
            Suggestion original = Suggestion.optimization("Use StringBuilder for concatenation", 
                "StringUtils.java", 25, "StringBuilder is more efficient for multiple concatenations");

            // When
            String json = objectMapper.writeValueAsString(original);
            Suggestion deserialized = objectMapper.readValue(json, Suggestion.class);

            // Then
            assertEquals(original.getDescription(), deserialized.getDescription());
            assertEquals(original.getType(), deserialized.getType());
            assertEquals(original.getPriority(), deserialized.getPriority());
            assertEquals(original.getFilePath(), deserialized.getFilePath());
            assertEquals(original.getLineNumber(), deserialized.getLineNumber());
            assertEquals(original.getRationale(), deserialized.getRationale());
        }
    }

    @Nested
    @DisplayName("Work Session Serialization")
    class WorkSessionSerialization {

        @Test
        @DisplayName("Should serialize and deserialize WorkSession correctly")
        void shouldSerializeAndDeserializeWorkSessionCorrectly() throws Exception {
            // Given
            LocalDateTime start = LocalDateTime.now().minusHours(2);
            LocalDateTime end = LocalDateTime.now();
            WorkSession original = new WorkSession("session123", "dev456", start, end, 
                150, 8, 3, 5);

            // When
            String json = objectMapper.writeValueAsString(original);
            WorkSession deserialized = objectMapper.readValue(json, WorkSession.class);

            // Then
            assertEquals(original.getSessionId(), deserialized.getSessionId());
            assertEquals(original.getDeveloperId(), deserialized.getDeveloperId());
            assertEquals(original.getStartTime(), deserialized.getStartTime());
            assertEquals(original.getEndTime(), deserialized.getEndTime());
            assertEquals(original.getLinesOfCodeWritten(), deserialized.getLinesOfCodeWritten());
            assertEquals(original.getFilesModified(), deserialized.getFilesModified());
            assertEquals(original.getBugsFixed(), deserialized.getBugsFixed());
            assertEquals(original.getTestsWritten(), deserialized.getTestsWritten());
        }

        @Test
        @DisplayName("Should serialize and deserialize Achievement correctly")
        void shouldSerializeAndDeserializeAchievementCorrectly() throws Exception {
            // Given
            Achievement original = Achievement.learningStreak(7);

            // When
            String json = objectMapper.writeValueAsString(original);
            Achievement deserialized = objectMapper.readValue(json, Achievement.class);

            // Then
            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getTitle(), deserialized.getTitle());
            assertEquals(original.getDescription(), deserialized.getDescription());
            assertEquals(original.getType(), deserialized.getType());
            assertEquals(original.getSkillDomain(), deserialized.getSkillDomain());
        }
    }

    @Test
    @DisplayName("Should handle null values gracefully in serialization")
    void shouldHandleNullValuesGracefullyInSerialization() throws Exception {
        // Given
        Suggestion suggestionWithNulls = new Suggestion(
            "test-id", "Test suggestion", Suggestion.SuggestionType.BEST_PRACTICE, 
            Suggestion.Priority.LOW, null, 0, null, null, null);

        // When
        String json = objectMapper.writeValueAsString(suggestionWithNulls);
        Suggestion deserialized = objectMapper.readValue(json, Suggestion.class);

        // Then
        assertEquals(suggestionWithNulls.getDescription(), deserialized.getDescription());
        assertEquals(suggestionWithNulls.getType(), deserialized.getType());
        assertNull(deserialized.getFilePath());
        assertNull(deserialized.getCodeExample());
        assertNull(deserialized.getRationale());
    }
}