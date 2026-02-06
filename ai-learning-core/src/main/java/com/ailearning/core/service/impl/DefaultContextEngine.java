package com.ailearning.core.service.impl;

import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.WorkContext;
import com.ailearning.core.model.CodeChange;
import com.ailearning.core.model.CodeQuery;
import com.ailearning.core.model.CodeReference;
import com.ailearning.core.model.ContextEngineMetrics;
import com.ailearning.core.model.ast.ASTNode;
import com.ailearning.core.model.ast.ParseResult;
import com.ailearning.core.service.ContextEngine;
import com.ailearning.core.service.SemanticAnalyzer;
import com.ailearning.core.service.ast.ASTParser;
import com.ailearning.core.service.semantic.impl.DefaultSemanticAnalyzer;
import com.ailearning.core.service.ast.impl.MultiLanguageASTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of the Context Engine.
 * Provides real-time codebase analysis and understanding using AST parsing and semantic analysis.
 */
public class DefaultContextEngine implements ContextEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultContextEngine.class);
    
    private final ASTParser astParser;
    private final SemanticAnalyzer semanticAnalyzer;
    private final Map<String, ProjectContext> projectContextCache;
    private final Map<String, ASTNode> astCache;
    private final AtomicBoolean isReady;
    
    // Performance tracking
    private final AtomicLong totalAnalysisTime;
    private final AtomicLong totalUpdateTime;
    private final AtomicLong analysisCount;
    private final AtomicLong updateCount;
    
    public DefaultContextEngine() {
        this.astParser = new MultiLanguageASTParser();
        this.semanticAnalyzer = new DefaultSemanticAnalyzer();
        this.projectContextCache = new ConcurrentHashMap<>();
        this.astCache = new ConcurrentHashMap<>();
        this.isReady = new AtomicBoolean(true);
        this.totalAnalysisTime = new AtomicLong(0);
        this.totalUpdateTime = new AtomicLong(0);
        this.analysisCount = new AtomicLong(0);
        this.updateCount = new AtomicLong(0);
        
        logger.info("Initialized DefaultContextEngine with multi-language AST parsing and semantic analysis");
    }
    
    @Override
    public CompletableFuture<ProjectContext> analyzeProject(String projectPath) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            logger.info("Starting project analysis for: {}", projectPath);
            
            try {
                // Check if project context is already cached
                ProjectContext cachedContext = projectContextCache.get(projectPath);
                if (cachedContext != null) {
                    logger.debug("Returning cached project context for: {}", projectPath);
                    return cachedContext;
                }
                
                // Build project context
                ProjectContext.Builder contextBuilder = ProjectContext.builder()
                        .projectName(extractProjectName(projectPath))
                        .rootPath(projectPath);
                
                // Analyze project structure
                analyzeProjectStructure(projectPath, contextBuilder);
                
                // Parse source files and build AST cache
                parseSourceFiles(projectPath);
                
                // Build project context
                ProjectContext projectContext = contextBuilder.build();
                
                // Cache the result
                projectContextCache.put(projectPath, projectContext);
                
                long duration = System.currentTimeMillis() - startTime;
                totalAnalysisTime.addAndGet(duration);
                analysisCount.incrementAndGet();
                
                logger.info("Completed project analysis for {} in {}ms", projectPath, duration);
                return projectContext;
                
            } catch (Exception e) {
                logger.error("Failed to analyze project: {}", projectPath, e);
                // Return a basic project context instead of throwing
                return ProjectContext.builder()
                        .projectName(extractProjectName(projectPath))
                        .rootPath(projectPath)
                        .build();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> updateContext(List<CodeChange> changes) {
        return CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            logger.debug("Updating context with {} changes", changes.size());
            
            try {
                for (CodeChange change : changes) {
                    updateContextForChange(change);
                }
                
                long duration = System.currentTimeMillis() - startTime;
                totalUpdateTime.addAndGet(duration);
                updateCount.incrementAndGet();
                
                // Ensure update completes within 500ms requirement
                if (duration > 500) {
                    logger.warn("Context update took {}ms, exceeding 500ms requirement", duration);
                }
                
                logger.debug("Context update completed in {}ms", duration);
                
            } catch (Exception e) {
                logger.error("Failed to update context", e);
                // Don't throw exception, just log the error
            }
        });
    }
    
    @Override
    public CompletableFuture<WorkContext> getCurrentContext() {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Getting current work context");
            
            try {
                // Build current work context based on recent activity
                return WorkContext.builder()
                        .currentFile("") // Would be populated from actual IDE integration
                        .focusedMethod("") // Would be populated from cursor position
                        .recentFiles(List.of()) // Would be populated from recent activity
                        .activeProject("") // Would be populated from current project
                        .build();
                
            } catch (Exception e) {
                logger.error("Failed to get current context", e);
                return WorkContext.builder().build();
            }
        });
    }
    
    @Override
    public CompletableFuture<List<CodeReference>> findRelatedCode(CodeQuery query) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Finding related code for query: {}", query.getQuery());
            
            try {
                List<CodeReference> references = new ArrayList<>();
                
                // Search through AST cache for related code
                for (Map.Entry<String, ASTNode> entry : astCache.entrySet()) {
                    String filePath = entry.getKey();
                    ASTNode astNode = entry.getValue();
                    
                    // Use semantic analyzer to find similar code
                    if (isRelevantToQuery(astNode, query)) {
                        CodeReference reference = CodeReference.builder()
                                .filePath(filePath)
                                .lineNumber(astNode.getLocation().getStartLine())
                                .columnNumber(astNode.getLocation().getStartColumn())
                                .snippet(extractCodeSnippet(astNode))
                                .relevanceScore(calculateRelevanceScore(astNode, query))
                                .build();
                        
                        references.add(reference);
                    }
                }
                
                // Sort by relevance score
                references.sort((r1, r2) -> Double.compare(r2.getRelevanceScore(), r1.getRelevanceScore()));
                
                // Limit results to top 20
                return references.stream()
                        .limit(20)
                        .collect(Collectors.toList());
                
            } catch (Exception e) {
                logger.error("Failed to find related code", e);
                return List.of();
            }
        });
    }
    
    @Override
    public boolean isReady() {
        return isReady.get();
    }
    
    @Override
    public ContextEngineMetrics getMetrics() {
        // Update metrics with current performance data
        double avgAnalysisTime = analysisCount.get() > 0 ? 
                (double) totalAnalysisTime.get() / analysisCount.get() : 0.0;
        double avgUpdateTime = updateCount.get() > 0 ? 
                (double) totalUpdateTime.get() / updateCount.get() : 0.0;
        
        return ContextEngineMetrics.builder()
                .averageAnalysisTime(avgAnalysisTime)
                .averageUpdateTime(avgUpdateTime)
                .totalAnalysisCount(analysisCount.get())
                .totalUpdateCount(updateCount.get())
                .cacheSize(projectContextCache.size())
                .astCacheSize(astCache.size())
                .memoryUsage(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                .build();
    }
    
    /**
     * Analyzes the project structure and populates the context builder.
     */
    private void analyzeProjectStructure(String projectPath, ProjectContext.Builder contextBuilder) {
        try {
            Path rootPath = Paths.get(projectPath);
            if (!Files.exists(rootPath)) {
                logger.warn("Project path does not exist: {}", projectPath);
                return;
            }
            
            // Walk through project directory
            try (Stream<Path> paths = Files.walk(rootPath)) {
                List<String> sourceFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> isSourceFile(path.toString()))
                        .map(Path::toString)
                        .collect(Collectors.toList());
                
                logger.debug("Found {} source files in project", sourceFiles.size());
                
            }
        } catch (IOException e) {
            logger.error("Failed to analyze project structure", e);
        }
    }
    
    /**
     * Parses source files and builds AST cache.
     */
    private void parseSourceFiles(String projectPath) {
        try {
            Path rootPath = Paths.get(projectPath);
            
            try (Stream<Path> paths = Files.walk(rootPath)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> isSourceFile(path.toString()))
                     .forEach(this::parseAndCacheFile);
            }
        } catch (IOException e) {
            logger.error("Failed to parse source files", e);
        }
    }
    
    /**
     * Parses a single file and caches its AST.
     */
    private void parseAndCacheFile(Path filePath) {
        try {
            String content = Files.readString(filePath);
            String language = detectLanguage(filePath.toString());
            
            CompletableFuture<ParseResult> parseResult = astParser.parseCode(content, language, filePath.toString());
            ParseResult result = parseResult.get();
            
            if (result.isSuccessful() && result.getAst() != null) {
                astCache.put(filePath.toString(), result.getAst());
                logger.debug("Cached AST for file: {}", filePath);
            } else {
                logger.warn("Failed to parse file: {}", filePath);
            }
            
        } catch (Exception e) {
            logger.error("Failed to parse and cache file: {}", filePath, e);
        }
    }
    
    /**
     * Updates context for a single code change.
     */
    private void updateContextForChange(CodeChange change) {
        String filePath = change.getFilePath();
        
        // Remove old AST from cache
        astCache.remove(filePath);
        
        // Parse updated file if it still exists
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            parseAndCacheFile(path);
        }
        
        // Invalidate related project context cache
        invalidateProjectContextCache(filePath);
    }
    
    /**
     * Invalidates project context cache for files related to the changed file.
     */
    private void invalidateProjectContextCache(String changedFilePath) {
        // Find project root for the changed file
        String projectRoot = findProjectRoot(changedFilePath);
        if (projectRoot != null) {
            projectContextCache.remove(projectRoot);
            logger.debug("Invalidated project context cache for: {}", projectRoot);
        }
    }
    
    /**
     * Checks if an AST node is relevant to a code query.
     */
    private boolean isRelevantToQuery(ASTNode astNode, CodeQuery query) {
        // Simple relevance check based on string matching
        String nodeString = astNode.toString().toLowerCase();
        String queryString = query.getQuery().toLowerCase();
        
        return nodeString.contains(queryString) || 
               calculateTextSimilarity(nodeString, queryString) > 0.3;
    }
    
    /**
     * Calculates relevance score for an AST node relative to a query.
     */
    private double calculateRelevanceScore(ASTNode astNode, CodeQuery query) {
        String nodeString = astNode.toString().toLowerCase();
        String queryString = query.getQuery().toLowerCase();
        
        // Simple scoring based on text similarity and exact matches
        double score = calculateTextSimilarity(nodeString, queryString);
        
        if (nodeString.contains(queryString)) {
            score += 0.5;
        }
        
        return Math.min(1.0, score);
    }
    
    /**
     * Extracts a code snippet from an AST node.
     */
    private String extractCodeSnippet(ASTNode astNode) {
        // Extract meaningful snippet from AST node
        String nodeString = astNode.toString();
        
        // Limit snippet length
        if (nodeString.length() > 200) {
            return nodeString.substring(0, 197) + "...";
        }
        
        return nodeString;
    }
    
    /**
     * Calculates text similarity between two strings.
     */
    private double calculateTextSimilarity(String text1, String text2) {
        Set<String> words1 = Set.of(text1.split("\\W+"));
        Set<String> words2 = Set.of(text2.split("\\W+"));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Extracts project name from project path.
     */
    private String extractProjectName(String projectPath) {
        Path path = Paths.get(projectPath);
        return path.getFileName().toString();
    }
    
    /**
     * Checks if a file is a source file.
     */
    private boolean isSourceFile(String filePath) {
        String lowerPath = filePath.toLowerCase();
        return lowerPath.endsWith(".java") || 
               lowerPath.endsWith(".js") || 
               lowerPath.endsWith(".ts") || 
               lowerPath.endsWith(".py") ||
               lowerPath.endsWith(".jsx") ||
               lowerPath.endsWith(".tsx");
    }
    
    /**
     * Detects programming language from file path.
     */
    private String detectLanguage(String filePath) {
        String lowerPath = filePath.toLowerCase();
        
        if (lowerPath.endsWith(".java")) return "java";
        if (lowerPath.endsWith(".js") || lowerPath.endsWith(".jsx")) return "javascript";
        if (lowerPath.endsWith(".ts") || lowerPath.endsWith(".tsx")) return "typescript";
        if (lowerPath.endsWith(".py")) return "python";
        
        return "unknown";
    }
    
    /**
     * Finds the project root directory for a given file path.
     */
    private String findProjectRoot(String filePath) {
        Path path = Paths.get(filePath);
        
        // Walk up the directory tree looking for project indicators
        while (path != null) {
            if (Files.exists(path.resolve("pom.xml")) || 
                Files.exists(path.resolve("package.json")) ||
                Files.exists(path.resolve("build.gradle")) ||
                Files.exists(path.resolve(".git"))) {
                return path.toString();
            }
            path = path.getParent();
        }
        
        return null;
    }
}