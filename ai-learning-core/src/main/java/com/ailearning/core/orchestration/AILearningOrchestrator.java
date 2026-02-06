package com.ailearning.core.orchestration;

import com.ailearning.core.model.*;
import com.ailearning.core.service.*;
import com.ailearning.core.service.ai.ContextualExplanationGenerator;
import com.ailearning.core.service.ai.ProjectSpecificExampleExtractor;
import com.ailearning.core.service.ai.StepByStepBreakdownGenerator;
import com.ailearning.core.service.impl.AutomationSuggestionEngine;
import com.ailearning.core.service.impl.ContextPreservationService;
import com.ailearning.core.service.impl.ProductivityTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main orchestrator that coordinates all AI Learning Companion services.
 * Provides high-level operations that combine multiple services.
 */
@Service
public class AILearningOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(AILearningOrchestrator.class);
    
    private final ContextEngine contextEngine;
    private final CodeAnalyzer codeAnalyzer;
    private final DocumentationGenerator documentationGenerator;
    private final LearningPathGenerator learningPathGenerator;
    private final ContextualExplanationGenerator explanationGenerator;
    private final ProjectSpecificExampleExtractor exampleExtractor;
    private final StepByStepBreakdownGenerator breakdownGenerator;
    private final ProcessingDecisionEngine processingDecisionEngine;
    private final EncryptionService encryptionService;
    private final PerformanceMonitoringService performanceMonitoring;
    private final BackgroundTaskService backgroundTaskService;
    private final AutomationSuggestionEngine automationSuggestionEngine;
    private final ProductivityTracker productivityTracker;
    private final ContextPreservationService contextPreservationService;
    
    public AILearningOrchestrator(
            ContextEngine contextEngine,
            CodeAnalyzer codeAnalyzer,
            DocumentationGenerator documentationGenerator,
            LearningPathGenerator learningPathGenerator,
            ContextualExplanationGenerator explanationGenerator,
            ProjectSpecificExampleExtractor exampleExtractor,
            StepByStepBreakdownGenerator breakdownGenerator,
            ProcessingDecisionEngine processingDecisionEngine,
            EncryptionService encryptionService,
            PerformanceMonitoringService performanceMonitoring,
            BackgroundTaskService backgroundTaskService,
            AutomationSuggestionEngine automationSuggestionEngine,
            ProductivityTracker productivityTracker,
            ContextPreservationService contextPreservationService) {
        this.contextEngine = contextEngine;
        this.codeAnalyzer = codeAnalyzer;
        this.documentationGenerator = documentationGenerator;
        this.learningPathGenerator = learningPathGenerator;
        this.explanationGenerator = explanationGenerator;
        this.exampleExtractor = exampleExtractor;
        this.breakdownGenerator = breakdownGenerator;
        this.processingDecisionEngine = processingDecisionEngine;
        this.encryptionService = encryptionService;
        this.performanceMonitoring = performanceMonitoring;
        this.backgroundTaskService = backgroundTaskService;
        this.automationSuggestionEngine = automationSuggestionEngine;
        this.productivityTracker = productivityTracker;
        this.contextPreservationService = contextPreservationService;
        
        logger.info("AI Learning Orchestrator initialized with all services");
    }
    
    /**
     * Analyzes a project and initializes all contexts.
     */
    public CompletableFuture<ProjectContext> initializeProject(String projectPath) {
        logger.info("Initializing project: {}", projectPath);
        
        long startTime = System.currentTimeMillis();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Analyze project structure
                ProjectContext context = contextEngine.analyzeProject(projectPath);
                
                long duration = System.currentTimeMillis() - startTime;
                performanceMonitoring.recordResponseTime("project_initialization", duration, null);
                
                logger.info("Project initialized in {}ms", duration);
                return context;
                
            } catch (Exception e) {
                logger.error("Failed to initialize project", e);
                throw new RuntimeException("Project initialization failed", e);
            }
        });
    }
    
    /**
     * Handles code change events with full analysis pipeline.
     */
    public CompletableFuture<CodeAnalysisResponse> handleCodeChange(CodeChange change) {
        logger.debug("Handling code change: {}", change.getFilePath());
        
        long startTime = System.currentTimeMillis();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Update context
                contextEngine.updateContext(List.of(change));
                
                // Get current context
                WorkContext workContext = contextEngine.getCurrentContext();
                
                // Analyze code
                CodeContext codeContext = new CodeContext(
                        change.getFilePath(),
                        change.getNewContent(),
                        change.getLanguage(),
                        workContext
                );
                
                AnalysisResult analysis = codeAnalyzer.analyzeCode(
                        change.getNewContent(),
                        change.getLanguage()
                );
                
                // Get suggestions
                List<Suggestion> suggestions = codeAnalyzer.suggestImprovements(codeContext);
                
                long duration = System.currentTimeMillis() - startTime;
                performanceMonitoring.recordResponseTime("code_change_analysis", duration, null);
                
                return new CodeAnalysisResponse(analysis, suggestions, duration);
                
            } catch (Exception e) {
                logger.error("Failed to handle code change", e);
                throw new RuntimeException("Code change handling failed", e);
            }
        });
    }
    
    /**
     * Generates comprehensive code explanation with examples.
     */
    public CompletableFuture<CodeExplanationResponse> explainCode(String code, String language) {
        logger.debug("Generating code explanation for {} code", language);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get contextual explanation
                var explanation = explanationGenerator.generateExplanation(code, language);
                
                // Extract project-specific examples
                var examples = exampleExtractor.extractExamples(code, language);
                
                // Generate step-by-step breakdown for complex code
                var breakdown = breakdownGenerator.generateBreakdown(code, language);
                
                return new CodeExplanationResponse(explanation, examples, breakdown);
                
            } catch (Exception e) {
                logger.error("Failed to generate code explanation", e);
                throw new RuntimeException("Code explanation failed", e);
            }
        });
    }
    
    /**
     * Generates or updates documentation for code changes.
     */
    public CompletableFuture<Documentation> generateDocumentation(String code, String language) {
        logger.debug("Generating documentation for {} code", language);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return documentationGenerator.generateInlineComments(code);
                
            } catch (Exception e) {
                logger.error("Failed to generate documentation", e);
                throw new RuntimeException("Documentation generation failed", e);
            }
        });
    }
    
    /**
     * Creates a personalized learning path for a developer.
     */
    public CompletableFuture<LearningPath> createLearningPath(DeveloperProfile profile) {
        logger.info("Creating learning path for developer: {}", profile.getId());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProjectContext projectContext = contextEngine.getCurrentContext().getProjectContext();
                return learningPathGenerator.generatePath(profile, projectContext);
                
            } catch (Exception e) {
                logger.error("Failed to create learning path", e);
                throw new RuntimeException("Learning path creation failed", e);
            }
        });
    }
    
    /**
     * Tracks productivity metrics for a development session.
     */
    public void trackProductivity(DevelopmentSession session) {
        logger.debug("Tracking productivity for session: {}", session.getSessionId());
        
        try {
            productivityTracker.trackSession(session);
            
        } catch (Exception e) {
            logger.error("Failed to track productivity", e);
        }
    }
    
    /**
     * Preserves work context for later restoration.
     */
    public CompletableFuture<WorkState> preserveContext(String developerId) {
        logger.debug("Preserving context for developer: {}", developerId);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                WorkContext currentContext = contextEngine.getCurrentContext();
                return contextPreservationService.captureWorkState(developerId, currentContext);
                
            } catch (Exception e) {
                logger.error("Failed to preserve context", e);
                throw new RuntimeException("Context preservation failed", e);
            }
        });
    }
    
    /**
     * Restores previously preserved work context.
     */
    public CompletableFuture<RestorationResult> restoreContext(String workStateId) {
        logger.debug("Restoring context: {}", workStateId);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return contextPreservationService.restoreWorkState(workStateId);
                
            } catch (Exception e) {
                logger.error("Failed to restore context", e);
                throw new RuntimeException("Context restoration failed", e);
            }
        });
    }
    
    /**
     * Gets system health status.
     */
    public SystemHealthStatus getSystemHealth() {
        String healthStatus = performanceMonitoring.getSystemHealthStatus();
        double memoryUsage = performanceMonitoring.getCurrentMemoryUsage();
        double cpuUsage = performanceMonitoring.getCurrentCpuUsage();
        boolean hasIssues = performanceMonitoring.hasPerformanceIssues();
        
        return new SystemHealthStatus(healthStatus, memoryUsage, cpuUsage, hasIssues);
    }
    
    // Response classes
    
    public static class CodeAnalysisResponse {
        private final AnalysisResult analysis;
        private final List<Suggestion> suggestions;
        private final long processingTimeMs;
        
        public CodeAnalysisResponse(AnalysisResult analysis, List<Suggestion> suggestions, long processingTimeMs) {
            this.analysis = analysis;
            this.suggestions = suggestions;
            this.processingTimeMs = processingTimeMs;
        }
        
        public AnalysisResult getAnalysis() {
            return analysis;
        }
        
        public List<Suggestion> getSuggestions() {
            return suggestions;
        }
        
        public long getProcessingTimeMs() {
            return processingTimeMs;
        }
    }
    
    public static class CodeExplanationResponse {
        private final Object explanation;
        private final Object examples;
        private final Object breakdown;
        
        public CodeExplanationResponse(Object explanation, Object examples, Object breakdown) {
            this.explanation = explanation;
            this.examples = examples;
            this.breakdown = breakdown;
        }
        
        public Object getExplanation() {
            return explanation;
        }
        
        public Object getExamples() {
            return examples;
        }
        
        public Object getBreakdown() {
            return breakdown;
        }
    }
    
    public static class SystemHealthStatus {
        private final String status;
        private final double memoryUsageMb;
        private final double cpuUsagePercent;
        private final boolean hasIssues;
        
        public SystemHealthStatus(String status, double memoryUsageMb, double cpuUsagePercent, boolean hasIssues) {
            this.status = status;
            this.memoryUsageMb = memoryUsageMb;
            this.cpuUsagePercent = cpuUsagePercent;
            this.hasIssues = hasIssues;
        }
        
        public String getStatus() {
            return status;
        }
        
        public double getMemoryUsageMb() {
            return memoryUsageMb;
        }
        
        public double getCpuUsagePercent() {
            return cpuUsagePercent;
        }
        
        public boolean isHasIssues() {
            return hasIssues;
        }
    }
}
