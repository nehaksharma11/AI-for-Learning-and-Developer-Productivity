package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.CodeAnalyzer;
import com.ailearning.core.service.ContextEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Default implementation of CodeAnalyzer providing comprehensive static analysis.
 * Supports multiple programming languages and analysis types including:
 * - Code quality analysis
 * - Security vulnerability detection  
 * - Performance bottleneck identification
 * - Pattern detection
 * - Intelligent context-aware suggestions
 */
public class DefaultCodeAnalyzer implements CodeAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultCodeAnalyzer.class);
    
    private final ExecutorService executorService;
    private final SecurityAnalyzer securityAnalyzer;
    private final QualityAnalyzer qualityAnalyzer;
    private final PatternDetector patternDetector;
    private final PerformanceAnalyzer performanceAnalyzer;
    private final IntelligentSuggestionEngine suggestionEngine;
    private final ContextEngine contextEngine;
    
    // Supported languages
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
            "java", "javascript", "typescript", "python", "kotlin", "scala"
    );
    
    public DefaultCodeAnalyzer() {
        this(null); // No context engine by default
    }
    
    public DefaultCodeAnalyzer(ContextEngine contextEngine) {
        this.executorService = Executors.newCachedThreadPool();
        this.securityAnalyzer = new SecurityAnalyzer();
        this.qualityAnalyzer = new QualityAnalyzer();
        this.patternDetector = new PatternDetector();
        this.performanceAnalyzer = new PerformanceAnalyzer();
        this.contextEngine = contextEngine;
        this.suggestionEngine = new IntelligentSuggestionEngine(contextEngine);
    }
    
    @Override
    public CompletableFuture<AnalysisResult> analyzeCode(String code, String language) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                logger.debug("Starting code analysis for language: {}", language);
                
                if (!supportsLanguage(language)) {
                    return createUnsupportedLanguageResult(language);
                }
                
                // Perform parallel analysis
                CompletableFuture<List<CodeIssue>> qualityIssues = 
                        CompletableFuture.supplyAsync(() -> qualityAnalyzer.analyze(code, language));
                
                CompletableFuture<List<SecurityIssue>> securityIssues = 
                        CompletableFuture.supplyAsync(() -> securityAnalyzer.analyze(code, language));
                
                CompletableFuture<List<Suggestion>> suggestions = 
                        CompletableFuture.supplyAsync(() -> performanceAnalyzer.analyzeSuggestions(code, language));
                
                CompletableFuture<ComplexityMetrics> complexity = 
                        CompletableFuture.supplyAsync(() -> calculateComplexity(code, language));
                
                // Wait for all analyses to complete
                CompletableFuture.allOf(qualityIssues, securityIssues, suggestions, complexity).join();
                
                long analysisTime = System.currentTimeMillis() - startTime;
                
                return AnalysisResult.builder()
                        .id(UUID.randomUUID().toString())
                        .timestamp(new Date())
                        .language(language)
                        .codeSize(code.length())
                        .analysisTimeMs(analysisTime)
                        .issues(qualityIssues.join())
                        .securityIssues(securityIssues.join())
                        .suggestions(suggestions.join())
                        .complexity(complexity.join())
                        .build();
                        
            } catch (Exception e) {
                logger.error("Error during code analysis", e);
                return createErrorResult(e, language);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<List<Suggestion>> suggestImprovements(CodeContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Suggestion> suggestions = new ArrayList<>();
                
                // Analyze based on context
                if (context.getCurrentFile() != null) {
                    String code = context.getCurrentFile();
                    String language = detectLanguage(context.getFileName());
                    
                    // Get basic analysis results
                    AnalysisResult analysisResult = analyzeCode(code, language).join();
                    
                    // Detect patterns for intelligent suggestions
                    Codebase codebase = createCodebaseFromContext(context);
                    List<Pattern> patterns = patternDetector.detectPatterns(codebase);
                    
                    // Generate intelligent suggestions
                    if (suggestionEngine != null) {
                        CompletableFuture<List<Suggestion>> intelligentSuggestions = 
                                suggestionEngine.generateSuggestions(context, patterns, null, analysisResult);
                        suggestions.addAll(intelligentSuggestions.join());
                    }
                    
                    // Add refactoring suggestions based on complexity
                    if (suggestionEngine != null) {
                        suggestions.addAll(suggestionEngine.generateRefactoringSuggestions(
                                context, analysisResult.getComplexity()));
                    }
                    
                    // Performance suggestions
                    suggestions.addAll(performanceAnalyzer.analyzeSuggestions(code, language));
                    
                    // Code quality suggestions
                    suggestions.addAll(qualityAnalyzer.getSuggestions(code, language));
                    
                    // Context-aware suggestions based on project patterns
                    suggestions.addAll(generateContextualSuggestions(context));
                }
                
                return suggestions;
                
            } catch (Exception e) {
                logger.error("Error generating suggestions", e);
                return List.of();
            }
        }, executorService);
    }
    
    /**
     * Enhanced suggestion generation with developer profile support.
     */
    public CompletableFuture<List<Suggestion>> suggestImprovements(CodeContext context, 
                                                                  DeveloperProfile developerProfile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (context.getCurrentFile() == null) {
                    return List.of();
                }
                
                String code = context.getCurrentFile();
                String language = detectLanguage(context.getFileName());
                
                // Get comprehensive analysis results
                AnalysisResult analysisResult = analyzeCode(code, language).join();
                
                // Detect patterns for intelligent suggestions
                Codebase codebase = createCodebaseFromContext(context);
                List<Pattern> patterns = patternDetector.detectPatterns(codebase);
                
                // Generate intelligent suggestions with developer profile
                if (suggestionEngine != null) {
                    return suggestionEngine.generateSuggestions(
                            context, patterns, developerProfile, analysisResult).join();
                }
                
                return List.of();
                
            } catch (Exception e) {
                logger.error("Error generating profile-aware suggestions", e);
                return List.of();
            }
        }, executorService);
    }
    
    /**
     * Generates architectural suggestions for the entire project.
     */
    public CompletableFuture<List<Suggestion>> generateArchitecturalSuggestions(ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (suggestionEngine == null) {
                    return List.of();
                }
                
                // Detect patterns across the entire codebase
                Codebase codebase = createCodebaseFromProject(projectContext);
                List<Pattern> patterns = patternDetector.detectPatterns(codebase);
                
                // Generate architectural suggestions
                return suggestionEngine.generateArchitecturalSuggestions(projectContext, patterns);
                
            } catch (Exception e) {
                logger.error("Error generating architectural suggestions", e);
                return List.of();
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<List<Pattern>> detectPatterns(Codebase codebase) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return patternDetector.detectPatterns(codebase);
            } catch (Exception e) {
                logger.error("Error detecting patterns", e);
                return List.of();
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<List<SecurityIssue>> validateSecurity(String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String language = detectLanguageFromCode(code);
                return securityAnalyzer.analyze(code, language);
            } catch (Exception e) {
                logger.error("Error in security validation", e);
                return List.of();
            }
        }, executorService);
    }
    
    @Override
    public boolean supportsLanguage(String language) {
        return SUPPORTED_LANGUAGES.contains(language.toLowerCase());
    }
    
    @Override
    public List<String> getSupportedLanguages() {
        return new ArrayList<>(SUPPORTED_LANGUAGES);
    }
    
    // Private helper methods
    
    private ComplexityMetrics calculateComplexity(String code, String language) {
        ComplexityAnalyzer analyzer = new ComplexityAnalyzer();
        return analyzer.analyze(code, language);
    }
    
    private String detectLanguage(String fileName) {
        if (fileName == null) return "unknown";
        
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "java" -> "java";
            case "js" -> "javascript";
            case "ts" -> "typescript";
            case "py" -> "python";
            case "kt" -> "kotlin";
            case "scala" -> "scala";
            default -> "unknown";
        };
    }
    
    private String detectLanguageFromCode(String code) {
        // Simple heuristic-based language detection
        if (code.contains("public class") || code.contains("import java")) return "java";
        if (code.contains("function") || code.contains("const ") || code.contains("let ")) return "javascript";
        if (code.contains("def ") || code.contains("import ")) return "python";
        return "unknown";
    }
    
    private Codebase createCodebaseFromContext(CodeContext context) {
        return Codebase.builder()
                .rootPath(context.getProjectRoot() != null ? context.getProjectRoot() : "/tmp")
                .sourceFiles(context.getFileName() != null ? List.of(context.getFileName()) : List.of())
                .fileContents(context.getCurrentFile() != null ? 
                        Map.of(context.getFileName(), context.getCurrentFile()) : Map.of())
                .languageMapping(context.getFileName() != null ? 
                        Map.of(context.getFileName(), detectLanguage(context.getFileName())) : Map.of())
                .totalFiles(1)
                .totalLines(context.getCurrentFile() != null ? 
                        context.getCurrentFile().split("\n").length : 0)
                .build();
    }
    
    private Codebase createCodebaseFromProject(ProjectContext projectContext) {
        Map<String, String> fileContents = new HashMap<>();
        Map<String, String> languageMapping = new HashMap<>();
        List<String> sourceFiles = new ArrayList<>();
        
        for (FileNode file : projectContext.getStructure().getFiles()) {
            sourceFiles.add(file.getPath());
            languageMapping.put(file.getPath(), detectLanguage(file.getName()));
            // Note: In a real implementation, we would load file contents
            fileContents.put(file.getPath(), ""); // Placeholder
        }
        
        return Codebase.builder()
                .rootPath(projectContext.getId())
                .sourceFiles(sourceFiles)
                .fileContents(fileContents)
                .languageMapping(languageMapping)
                .totalFiles(sourceFiles.size())
                .totalLines(1000) // Placeholder
                .build();
    }
    
    private List<Suggestion> generateContextualSuggestions(CodeContext context) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        // Add suggestions based on project context
        if (context.getProjectType() != null) {
            switch (context.getProjectType().toLowerCase()) {
                case "web" -> suggestions.addAll(getWebProjectSuggestions(context));
                case "api" -> suggestions.addAll(getApiProjectSuggestions(context));
                case "library" -> suggestions.addAll(getLibraryProjectSuggestions(context));
            }
        }
        
        return suggestions;
    }
    
    private List<Suggestion> getWebProjectSuggestions(CodeContext context) {
        return List.of(
                Suggestion.builder()
                        .id("web-security")
                        .title("Web Security Best Practices")
                        .description("Consider implementing CSRF protection and input validation")
                        .type(Suggestion.Type.SECURITY)
                        .priority(Suggestion.Priority.HIGH)
                        .category("Web Security")
                        .build()
        );
    }
    
    private List<Suggestion> getApiProjectSuggestions(CodeContext context) {
        return List.of(
                Suggestion.builder()
                        .id("api-versioning")
                        .title("API Versioning")
                        .description("Consider implementing API versioning for backward compatibility")
                        .type(Suggestion.Type.ARCHITECTURE)
                        .priority(Suggestion.Priority.MEDIUM)
                        .category("API Design")
                        .build()
        );
    }
    
    private List<Suggestion> getLibraryProjectSuggestions(CodeContext context) {
        return List.of(
                Suggestion.builder()
                        .id("library-documentation")
                        .title("Library Documentation")
                        .description("Ensure comprehensive documentation for public APIs")
                        .type(Suggestion.Type.DOCUMENTATION)
                        .priority(Suggestion.Priority.MEDIUM)
                        .category("Documentation")
                        .build()
        );
    }
    
    private AnalysisResult createUnsupportedLanguageResult(String language) {
        return AnalysisResult.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(new Date())
                .language(language)
                .codeSize(0)
                .analysisTimeMs(0)
                .issues(List.of(CodeIssue.builder()
                        .id("unsupported-language")
                        .title("Unsupported Language")
                        .description("Language '" + language + "' is not supported for analysis")
                        .severity(CodeIssue.Severity.INFO)
                        .category("Language Support")
                        .build()))
                .securityIssues(List.of())
                .suggestions(List.of())
                .complexity(ComplexityMetrics.builder()
                        .cyclomaticComplexity(0)
                        .cognitiveComplexity(0)
                        .linesOfCode(0)
                        .build())
                .build();
    }
    
    private AnalysisResult createErrorResult(Exception e, String language) {
        return AnalysisResult.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(new Date())
                .language(language)
                .codeSize(0)
                .analysisTimeMs(0)
                .issues(List.of(CodeIssue.builder()
                        .id("analysis-error")
                        .title("Analysis Error")
                        .description("Error during analysis: " + e.getMessage())
                        .severity(CodeIssue.Severity.ERROR)
                        .category("System Error")
                        .build()))
                .securityIssues(List.of())
                .suggestions(List.of())
                .complexity(ComplexityMetrics.builder()
                        .cyclomaticComplexity(0)
                        .cognitiveComplexity(0)
                        .linesOfCode(0)
                        .build())
                .build();
    }
    
    // Cleanup resources
    public void shutdown() {
        executorService.shutdown();
    }
}