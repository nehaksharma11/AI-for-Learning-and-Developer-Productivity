package com.ailearning.core.config;

import com.ailearning.core.service.*;
import com.ailearning.core.service.ai.*;
import com.ailearning.core.service.ai.impl.*;
import com.ailearning.core.service.ast.ASTParser;
import com.ailearning.core.service.ast.DependencyGraphBuilder;
import com.ailearning.core.service.ast.impl.MultiLanguageASTParser;
import com.ailearning.core.service.impl.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main Spring configuration for AI Learning Companion core services.
 * Wires all components together using dependency injection.
 */
@Configuration
@EnableConfigurationProperties(AILearningProperties.class)
public class AILearningCoreConfiguration {
    
    // ========== Context Engine Components ==========
    
    @Bean
    public ASTParser astParser() {
        return new MultiLanguageASTParser();
    }
    
    @Bean
    public DependencyGraphBuilder dependencyGraphBuilder() {
        return new DependencyGraphBuilder();
    }
    
    @Bean
    public SemanticAnalyzer semanticAnalyzer() {
        return new com.ailearning.core.service.semantic.impl.DefaultSemanticAnalyzer();
    }
    
    @Bean
    public ContextEngine contextEngine(
            ASTParser astParser,
            DependencyGraphBuilder dependencyGraphBuilder,
            SemanticAnalyzer semanticAnalyzer,
            CacheService cacheService,
            PerformanceMonitoringService performanceMonitoring) {
        return new DefaultContextEngine(
                astParser,
                dependencyGraphBuilder,
                semanticAnalyzer,
                cacheService,
                performanceMonitoring
        );
    }
    
    // ========== Code Analyzer Components ==========
    
    @Bean
    public CodeAnalyzer codeAnalyzer(
            ContextEngine contextEngine,
            PerformanceMonitoringService performanceMonitoring) {
        return new DefaultCodeAnalyzer(contextEngine, performanceMonitoring);
    }
    
    // ========== Documentation Generator Components ==========
    
    @Bean
    public DocumentationGenerator documentationGenerator(
            ContextEngine contextEngine,
            CodeAnalyzer codeAnalyzer) {
        return new DefaultDocumentationGenerator(contextEngine, codeAnalyzer);
    }
    
    // ========== Learning Path Generator Components ==========
    
    @Bean
    public LearningPathGenerator learningPathGenerator(
            ContextEngine contextEngine,
            CodeAnalyzer codeAnalyzer) {
        return new DefaultLearningPathGenerator(contextEngine, codeAnalyzer);
    }
    
    // ========== AI/ML Integration Components ==========
    
    @Bean
    public AIServiceManager aiServiceManager(AILearningProperties properties) {
        AIServiceManager manager = new AIServiceManager();
        
        // Register AI services based on configuration
        if (properties.getAi().isOpenaiEnabled()) {
            manager.registerService(new OpenAIService(properties.getAi().getOpenaiApiKey()), 1);
        }
        
        if (properties.getAi().isHuggingfaceEnabled()) {
            manager.registerService(new HuggingFaceService(properties.getAi().getHuggingfaceApiKey()), 2);
        }
        
        // Always register fallback service
        manager.registerService(new FallbackAIService(), 99);
        
        return manager;
    }
    
    @Bean
    public ContextualExplanationGenerator contextualExplanationGenerator(
            AIServiceManager aiServiceManager,
            ContextEngine contextEngine) {
        return new DefaultContextualExplanationGenerator(aiServiceManager, contextEngine);
    }
    
    @Bean
    public ProjectSpecificExampleExtractor projectSpecificExampleExtractor(
            ContextEngine contextEngine) {
        return new DefaultProjectSpecificExampleExtractor(contextEngine);
    }
    
    @Bean
    public StepByStepBreakdownGenerator stepByStepBreakdownGenerator(
            AIServiceManager aiServiceManager) {
        return new DefaultStepByStepBreakdownGenerator(aiServiceManager);
    }
    
    // ========== Privacy and Security Components ==========
    
    @Bean
    public DataClassificationService dataClassificationService() {
        return new DefaultDataClassificationService();
    }
    
    @Bean
    public PrivacyPreferencesService privacyPreferencesService() {
        return new DefaultPrivacyPreferencesService();
    }
    
    @Bean
    public ProcessingDecisionEngine processingDecisionEngine(
            DataClassificationService dataClassificationService,
            PrivacyPreferencesService privacyPreferencesService) {
        return new DefaultProcessingDecisionEngine(
                dataClassificationService,
                privacyPreferencesService
        );
    }
    
    @Bean
    public KeyManagementService keyManagementService() {
        return new DefaultKeyManagementService();
    }
    
    @Bean
    public EncryptionService encryptionService(KeyManagementService keyManagementService) {
        return new DefaultEncryptionService(keyManagementService);
    }
    
    @Bean
    public SecureCommunicationService secureCommunicationService(
            EncryptionService encryptionService) {
        return new DefaultSecureCommunicationService(encryptionService);
    }
    
    // ========== Performance and Resource Management Components ==========
    
    @Bean
    public PerformanceMonitoringService performanceMonitoringService() {
        return new DefaultPerformanceMonitoringService();
    }
    
    @Bean
    public BackgroundTaskService backgroundTaskService(
            PerformanceMonitoringService performanceMonitoring) {
        return new DefaultBackgroundTaskService(performanceMonitoring);
    }
    
    @Bean
    public CacheService cacheService() {
        return new DefaultCacheService();
    }
    
    @Bean
    public ResourceScalingService resourceScalingService(
            PerformanceMonitoringService performanceMonitoring) {
        return new DefaultResourceScalingService(performanceMonitoring);
    }
    
    // ========== Productivity Enhancement Components ==========
    
    @Bean
    public AutomationSuggestionEngine automationSuggestionEngine(
            ContextEngine contextEngine) {
        return new AutomationSuggestionEngine(contextEngine);
    }
    
    @Bean
    public ProductivityTracker productivityTracker() {
        return new ProductivityTracker();
    }
    
    @Bean
    public ContextPreservationService contextPreservationService(
            ContextEngine contextEngine) {
        return new ContextPreservationService(contextEngine);
    }
    
    // ========== Learning Retention Components ==========
    
    @Bean
    public RetentionSchedulingService retentionSchedulingService() {
        return new DefaultRetentionSchedulingService();
    }
}
