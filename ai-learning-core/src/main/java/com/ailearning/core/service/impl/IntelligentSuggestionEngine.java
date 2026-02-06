package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.ContextEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Intelligent suggestion engine that provides context-aware, pattern-based, 
 * and framework-specific recommendations for code improvement.
 * 
 * Features:
 * - Context-aware suggestions based on project structure and conventions
 * - Pattern-based recommendations using detected code patterns
 * - Framework-specific best practices
 * - Learning-oriented suggestions for skill development
 * - Adaptive suggestions based on developer profile
 */
public class IntelligentSuggestionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(IntelligentSuggestionEngine.class);
    
    private final ContextEngine contextEngine;
    private final PatternBasedSuggestionProvider patternProvider;
    private final FrameworkSpecificSuggestionProvider frameworkProvider;
    private final ContextAwareSuggestionProvider contextProvider;
    private final LearningOrientedSuggestionProvider learningProvider;
    
    public IntelligentSuggestionEngine(ContextEngine contextEngine) {
        this.contextEngine = contextEngine;
        this.patternProvider = new PatternBasedSuggestionProvider();
        this.frameworkProvider = new FrameworkSpecificSuggestionProvider();
        this.contextProvider = new ContextAwareSuggestionProvider();
        this.learningProvider = new LearningOrientedSuggestionProvider();
    }
    
    /**
     * Generates intelligent suggestions based on code context, patterns, and developer profile.
     */
    public CompletableFuture<List<Suggestion>> generateSuggestions(
            CodeContext context, 
            List<Pattern> detectedPatterns,
            DeveloperProfile developerProfile,
            AnalysisResult analysisResult) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating intelligent suggestions for context: {}", context.getFileName());
                
                List<Suggestion> suggestions = new ArrayList<>();
                
                // 1. Pattern-based suggestions
                suggestions.addAll(patternProvider.generateSuggestions(detectedPatterns, context));
                
                // 2. Framework-specific suggestions
                suggestions.addAll(frameworkProvider.generateSuggestions(context, analysisResult));
                
                // 3. Context-aware suggestions
                suggestions.addAll(contextProvider.generateSuggestions(context, analysisResult));
                
                // 4. Learning-oriented suggestions
                if (developerProfile != null) {
                    suggestions.addAll(learningProvider.generateSuggestions(
                            context, analysisResult, developerProfile));
                }
                
                // 5. Prioritize and filter suggestions
                suggestions = prioritizeAndFilter(suggestions, context, developerProfile);
                
                logger.debug("Generated {} intelligent suggestions", suggestions.size());
                return suggestions;
                
            } catch (Exception e) {
                logger.error("Error generating intelligent suggestions", e);
                return List.of();
            }
        });
    }
    
    /**
     * Generates suggestions for refactoring opportunities.
     */
    public List<Suggestion> generateRefactoringSuggestions(CodeContext context, ComplexityMetrics complexity) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        // High complexity method refactoring
        if (complexity.getCyclomaticComplexity() > 10) {
            suggestions.add(Suggestion.refactoring(
                    "Extract Method",
                    "Method has high cyclomatic complexity (" + complexity.getCyclomaticComplexity() + 
                    "). Consider extracting smaller methods.",
                    context.getFileName(),
                    null,
                    "Break down complex methods into smaller, focused methods",
                    "High complexity makes code harder to understand, test, and maintain"
            ));
        }
        
        // Deep nesting refactoring
        if (complexity.getNestingDepth() > 4) {
            suggestions.add(Suggestion.refactoring(
                    "Reduce Nesting",
                    "Deep nesting detected (depth: " + complexity.getNestingDepth() + 
                    "). Consider using guard clauses or extracting methods.",
                    context.getFileName(),
                    null,
                    "Use early returns, guard clauses, or extract nested logic into methods",
                    "Deep nesting reduces code readability and increases cognitive load"
            ));
        }
        
        // Large class refactoring
        if (complexity.getLinesOfCode() > 500) {
            suggestions.add(Suggestion.refactoring(
                    "Split Large Class",
                    "Class is very large (" + complexity.getLinesOfCode() + 
                    " lines). Consider splitting into smaller, focused classes.",
                    context.getFileName(),
                    null,
                    "Apply Single Responsibility Principle and extract related functionality",
                    "Large classes violate SRP and are harder to maintain and test"
            ));
        }
        
        return suggestions;
    }
    
    /**
     * Generates architecture-level suggestions.
     */
    public List<Suggestion> generateArchitecturalSuggestions(ProjectContext projectContext, 
                                                            List<Pattern> patterns) {
        List<Suggestion> suggestions = new ArrayList<>();
        
        // Suggest missing architectural patterns
        boolean hasMVC = patterns.stream().anyMatch(p -> p.getName().contains("MVC"));
        boolean hasRepository = patterns.stream().anyMatch(p -> p.getName().contains("Repository"));
        boolean hasService = patterns.stream().anyMatch(p -> p.getName().contains("Service"));
        
        if (!hasRepository && projectContext.getStructure().getFiles().stream()
                .anyMatch(f -> f.getName().toLowerCase().contains("data"))) {
            suggestions.add(Suggestion.architecture(
                    "Implement Repository Pattern",
                    "Consider implementing Repository pattern for data access abstraction",
                    "Architecture",
                    "Repository pattern provides a uniform interface for accessing data sources",
                    "https://martinfowler.com/eaaCatalog/repository.html"
            ));
        }
        
        if (!hasService && projectContext.getStructure().getFiles().size() > 10) {
            suggestions.add(Suggestion.architecture(
                    "Implement Service Layer",
                    "Consider implementing Service Layer pattern for business logic organization",
                    "Architecture",
                    "Service Layer provides a clear API for business operations",
                    "https://martinfowler.com/eaaCatalog/serviceLayer.html"
            ));
        }
        
        // Dependency injection suggestions
        if (hasCircularDependencies(projectContext)) {
            suggestions.add(Suggestion.architecture(
                    "Resolve Circular Dependencies",
                    "Circular dependencies detected. Consider using dependency injection or redesigning relationships",
                    "Architecture",
                    "Circular dependencies make code harder to test and can cause runtime issues",
                    null
            ));
        }
        
        return suggestions;
    }
    
    private List<Suggestion> prioritizeAndFilter(List<Suggestion> suggestions, 
                                               CodeContext context, 
                                               DeveloperProfile developerProfile) {
        // Remove duplicates
        Map<String, Suggestion> uniqueSuggestions = suggestions.stream()
                .collect(Collectors.toMap(
                        s -> s.getTitle() + s.getCategory(),
                        s -> s,
                        (existing, replacement) -> existing.getConfidenceScore() > replacement.getConfidenceScore() 
                                ? existing : replacement
                ));
        
        List<Suggestion> filtered = new ArrayList<>(uniqueSuggestions.values());
        
        // Sort by priority and confidence
        filtered.sort((s1, s2) -> {
            int priorityCompare = s2.getPriority().compareTo(s1.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            return Double.compare(s2.getConfidenceScore(), s1.getConfidenceScore());
        });
        
        // Limit to top suggestions to avoid overwhelming the user
        int maxSuggestions = developerProfile != null && 
                           developerProfile.getLearningPreferences().getDetailLevel().equals("detailed") ? 15 : 10;
        
        return filtered.stream()
                .limit(maxSuggestions)
                .collect(Collectors.toList());
    }
    
    private boolean hasCircularDependencies(ProjectContext projectContext) {
        // Simplified circular dependency detection
        // In a real implementation, this would use graph algorithms
        return projectContext.getStructure().getRelationships().stream()
                .anyMatch(rel -> rel.getType().equals("CIRCULAR_DEPENDENCY"));
    }
    
    /**
     * Pattern-based suggestion provider.
     */
    private static class PatternBasedSuggestionProvider {
        
        public List<Suggestion> generateSuggestions(List<Pattern> patterns, CodeContext context) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            for (Pattern pattern : patterns) {
                switch (pattern.getType()) {
                    case ANTI_PATTERN -> suggestions.addAll(generateAntiPatternSuggestions(pattern, context));
                    case DESIGN_PATTERN -> suggestions.addAll(generateDesignPatternSuggestions(pattern, context));
                    case PERFORMANCE_PATTERN -> suggestions.addAll(generatePerformancePatternSuggestions(pattern, context));
                }
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateAntiPatternSuggestions(Pattern pattern, CodeContext context) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            if (pattern.getName().contains("God Class")) {
                suggestions.add(Suggestion.refactoring(
                        "Refactor God Class",
                        "God Class anti-pattern detected. Break down into smaller, focused classes.",
                        context.getFileName(),
                        null,
                        "Apply Single Responsibility Principle and extract related functionality",
                        "God classes are hard to understand, test, and maintain"
                ));
            }
            
            if (pattern.getName().contains("Callback Hell")) {
                suggestions.add(Suggestion.refactoring(
                        "Flatten Callback Structure",
                        "Callback Hell detected. Consider using Promises or async/await.",
                        context.getFileName(),
                        null,
                        "Use Promise chains or async/await syntax for better readability",
                        "Deeply nested callbacks are hard to read and debug"
                ));
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateDesignPatternSuggestions(Pattern pattern, CodeContext context) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            if (pattern.getName().contains("Singleton") && pattern.getConfidence() == Pattern.Confidence.MEDIUM) {
                suggestions.add(Suggestion.bestPractice(
                        "Improve Singleton Implementation",
                        "Consider using enum singleton or double-checked locking for thread safety",
                        "Design Patterns",
                        "public enum Singleton { INSTANCE; }",
                        "https://effective-java.com/singleton-pattern"
                ));
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generatePerformancePatternSuggestions(Pattern pattern, CodeContext context) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            if (pattern.getName().contains("N+1 Query")) {
                suggestions.add(Suggestion.performance(
                        "Optimize Database Queries",
                        "N+1 query pattern detected. Consider using batch loading or joins.",
                        Suggestion.Priority.HIGH,
                        "Database Performance",
                        "Use @BatchSize, @Fetch(FetchMode.JOIN), or custom batch loading",
                        "High - can significantly reduce database load"
                ));
            }
            
            return suggestions;
        }
    }
    
    /**
     * Framework-specific suggestion provider.
     */
    private static class FrameworkSpecificSuggestionProvider {
        
        public List<Suggestion> generateSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            String language = detectLanguage(context.getFileName());
            String framework = detectFramework(context);
            
            switch (framework.toLowerCase()) {
                case "spring" -> suggestions.addAll(generateSpringSuggestions(context, analysisResult));
                case "react" -> suggestions.addAll(generateReactSuggestions(context, analysisResult));
                case "angular" -> suggestions.addAll(generateAngularSuggestions(context, analysisResult));
                case "django" -> suggestions.addAll(generateDjangoSuggestions(context, analysisResult));
                case "express" -> suggestions.addAll(generateExpressSuggestions(context, analysisResult));
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateSpringSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            String code = context.getCurrentFile();
            if (code != null) {
                // Spring Boot specific suggestions
                if (code.contains("@RestController") && !code.contains("@Validated")) {
                    suggestions.add(Suggestion.bestPractice(
                            "Add Input Validation",
                            "Consider adding @Validated annotation for request validation",
                            "Spring Boot",
                            "@Validated @RestController public class MyController { }",
                            "https://spring.io/guides/gs/validating-form-input/"
                    ));
                }
                
                if (code.contains("@Autowired") && code.contains("private")) {
                    suggestions.add(Suggestion.bestPractice(
                            "Use Constructor Injection",
                            "Prefer constructor injection over field injection for better testability",
                            "Spring Boot",
                            "Use constructor parameters instead of @Autowired fields",
                            "https://spring.io/blog/2007/07/11/setter-injection-versus-constructor-injection-and-the-use-of-required"
                    ));
                }
                
                if (code.contains("@Service") && !code.contains("@Transactional")) {
                    suggestions.add(Suggestion.bestPractice(
                            "Consider Transaction Management",
                            "Service classes often need transaction management. Consider @Transactional",
                            "Spring Boot",
                            "@Transactional @Service public class MyService { }",
                            "https://spring.io/guides/gs/managing-transactions/"
                    ));
                }
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateReactSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            String code = context.getCurrentFile();
            if (code != null) {
                // React specific suggestions
                if (code.contains("useState") && code.contains("useEffect")) {
                    suggestions.add(Suggestion.bestPractice(
                            "Consider useCallback for Performance",
                            "When using useState with useEffect, consider useCallback to prevent unnecessary re-renders",
                            "React",
                            "const memoizedCallback = useCallback(() => { doSomething(a, b); }, [a, b]);",
                            "https://reactjs.org/docs/hooks-reference.html#usecallback"
                    ));
                }
                
                if (code.contains("class") && code.contains("extends Component")) {
                    suggestions.add(Suggestion.refactoring(
                            "Convert to Functional Component",
                            "Consider converting class components to functional components with hooks",
                            context.getFileName(),
                            null,
                            "Use function components with useState and useEffect hooks",
                            "Functional components are more concise and performant"
                    ));
                }
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateAngularSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            String code = context.getCurrentFile();
            if (code != null) {
                if (code.contains("@Component") && !code.contains("OnDestroy")) {
                    suggestions.add(Suggestion.bestPractice(
                            "Implement OnDestroy",
                            "Components should implement OnDestroy for proper cleanup",
                            "Angular",
                            "export class MyComponent implements OnDestroy { ngOnDestroy() { } }",
                            "https://angular.io/guide/lifecycle-hooks"
                    ));
                }
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateDjangoSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            String code = context.getCurrentFile();
            if (code != null) {
                if (code.contains("models.Model") && !code.contains("__str__")) {
                    suggestions.add(Suggestion.bestPractice(
                            "Add String Representation",
                            "Django models should implement __str__ method for better debugging",
                            "Django",
                            "def __str__(self): return self.name",
                            "https://docs.djangoproject.com/en/stable/ref/models/instances/#str"
                    ));
                }
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateExpressSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            String code = context.getCurrentFile();
            if (code != null) {
                if (code.contains("app.get") && !code.contains("try")) {
                    suggestions.add(Suggestion.bestPractice(
                            "Add Error Handling",
                            "Express route handlers should include proper error handling",
                            "Express.js",
                            "app.get('/api', async (req, res, next) => { try { ... } catch (err) { next(err); } });",
                            "https://expressjs.com/en/guide/error-handling.html"
                    ));
                }
            }
            
            return suggestions;
        }
        
        private String detectLanguage(String fileName) {
            if (fileName == null) return "unknown";
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            return switch (extension) {
                case "java" -> "java";
                case "js", "jsx" -> "javascript";
                case "ts", "tsx" -> "typescript";
                case "py" -> "python";
                default -> "unknown";
            };
        }
        
        private String detectFramework(CodeContext context) {
            String code = context.getCurrentFile();
            if (code == null) return "unknown";
            
            // Simple framework detection based on imports and annotations
            if (code.contains("@SpringBootApplication") || code.contains("@RestController")) return "spring";
            if (code.contains("import React") || code.contains("from 'react'")) return "react";
            if (code.contains("@Component") && code.contains("@angular")) return "angular";
            if (code.contains("from django")) return "django";
            if (code.contains("express()") || code.contains("require('express')")) return "express";
            
            return "unknown";
        }
    }
    
    /**
     * Context-aware suggestion provider.
     */
    private static class ContextAwareSuggestionProvider {
        
        public List<Suggestion> generateSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            // Project type specific suggestions
            String projectType = context.getProjectType();
            if (projectType != null) {
                switch (projectType.toLowerCase()) {
                    case "web" -> suggestions.addAll(generateWebProjectSuggestions(context, analysisResult));
                    case "api" -> suggestions.addAll(generateApiProjectSuggestions(context, analysisResult));
                    case "library" -> suggestions.addAll(generateLibraryProjectSuggestions(context, analysisResult));
                    case "microservice" -> suggestions.addAll(generateMicroserviceSuggestions(context, analysisResult));
                }
            }
            
            // File type specific suggestions
            suggestions.addAll(generateFileTypeSuggestions(context, analysisResult));
            
            return suggestions;
        }
        
        private List<Suggestion> generateWebProjectSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            // Security suggestions for web projects
            suggestions.add(Suggestion.security(
                    "Implement CSRF Protection",
                    "Web applications should implement CSRF protection for state-changing operations",
                    context.getFileName(),
                    null,
                    "CSRF attacks can be prevented with proper token validation"
            ));
            
            // Performance suggestions
            suggestions.add(Suggestion.performance(
                    "Optimize Asset Loading",
                    "Consider implementing lazy loading and code splitting for better performance",
                    Suggestion.Priority.MEDIUM,
                    "Web Performance",
                    "Use dynamic imports and lazy loading for non-critical resources",
                    "Medium - improves initial page load time"
            ));
            
            return suggestions;
        }
        
        private List<Suggestion> generateApiProjectSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            suggestions.add(Suggestion.architecture(
                    "Implement API Versioning",
                    "APIs should implement versioning strategy for backward compatibility",
                    "API Design",
                    "Versioning prevents breaking changes from affecting existing clients",
                    "https://restfulapi.net/versioning/"
            ));
            
            suggestions.add(Suggestion.bestPractice(
                    "Add Rate Limiting",
                    "Implement rate limiting to prevent API abuse and ensure fair usage",
                    "API Security",
                    "Use middleware or API gateway for rate limiting",
                    "https://tools.ietf.org/html/rfc6585#section-4"
            ));
            
            return suggestions;
        }
        
        private List<Suggestion> generateLibraryProjectSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            suggestions.add(Suggestion.bestPractice(
                    "Comprehensive Documentation",
                    "Libraries should have comprehensive documentation with examples",
                    "Documentation",
                    "Include README, API docs, and usage examples",
                    null
            ));
            
            suggestions.add(Suggestion.bestPractice(
                    "Semantic Versioning",
                    "Use semantic versioning for library releases",
                    "Release Management",
                    "Follow MAJOR.MINOR.PATCH versioning scheme",
                    "https://semver.org/"
            ));
            
            return suggestions;
        }
        
        private List<Suggestion> generateMicroserviceSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            suggestions.add(Suggestion.architecture(
                    "Implement Health Checks",
                    "Microservices should expose health check endpoints for monitoring",
                    "Microservices",
                    "Health checks enable proper service discovery and load balancing",
                    null
            ));
            
            suggestions.add(Suggestion.architecture(
                    "Add Circuit Breaker Pattern",
                    "Implement circuit breaker pattern for resilient service communication",
                    "Microservices",
                    "Circuit breakers prevent cascade failures in distributed systems",
                    "https://martinfowler.com/bliki/CircuitBreaker.html"
            ));
            
            return suggestions;
        }
        
        private List<Suggestion> generateFileTypeSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            String fileName = context.getFileName();
            if (fileName != null) {
                if (fileName.toLowerCase().contains("test")) {
                    suggestions.addAll(generateTestFileSuggestions(context, analysisResult));
                } else if (fileName.toLowerCase().contains("config")) {
                    suggestions.addAll(generateConfigFileSuggestions(context, analysisResult));
                } else if (fileName.toLowerCase().contains("util")) {
                    suggestions.addAll(generateUtilFileSuggestions(context, analysisResult));
                }
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateTestFileSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            String code = context.getCurrentFile();
            if (code != null && !code.contains("@Test") && !code.contains("it(") && !code.contains("test(")) {
                suggestions.add(Suggestion.bestPractice(
                        "Add Test Methods",
                        "Test files should contain actual test methods",
                        "Testing",
                        "Add @Test annotations or test functions",
                        null
                ));
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateConfigFileSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            suggestions.add(Suggestion.security(
                    "Externalize Sensitive Configuration",
                    "Configuration files should not contain sensitive data like passwords",
                    context.getFileName(),
                    null,
                    "Use environment variables or secure configuration management"
            ));
            
            return suggestions;
        }
        
        private List<Suggestion> generateUtilFileSuggestions(CodeContext context, AnalysisResult analysisResult) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            suggestions.add(Suggestion.bestPractice(
                    "Make Utility Methods Static",
                    "Utility methods should typically be static for better performance and clarity",
                    "Code Organization",
                    "public static ReturnType utilityMethod(params) { }",
                    null
            ));
            
            return suggestions;
        }
    }
    
    /**
     * Learning-oriented suggestion provider.
     */
    private static class LearningOrientedSuggestionProvider {
        
        public List<Suggestion> generateSuggestions(CodeContext context, AnalysisResult analysisResult, 
                                                   DeveloperProfile developerProfile) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            // Skill-based suggestions
            suggestions.addAll(generateSkillBasedSuggestions(context, developerProfile));
            
            // Learning opportunity suggestions
            suggestions.addAll(generateLearningOpportunitySuggestions(context, analysisResult, developerProfile));
            
            return suggestions;
        }
        
        private List<Suggestion> generateSkillBasedSuggestions(CodeContext context, DeveloperProfile profile) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            // Suggest learning opportunities based on skill gaps
            Map<String, SkillLevel> skills = profile.getSkillLevels();
            
            if (skills.containsKey("design-patterns") && 
                skills.get("design-patterns").getProficiency() < 0.7) {
                suggestions.add(Suggestion.learning(
                        "Learn Design Patterns",
                        "Improve your design pattern knowledge to write more maintainable code",
                        "Learning",
                        "https://refactoring.guru/design-patterns",
                        List.of("design-patterns", "architecture", "best-practices")
                ));
            }
            
            if (skills.containsKey("testing") && 
                skills.get("testing").getProficiency() < 0.6) {
                suggestions.add(Suggestion.learning(
                        "Improve Testing Skills",
                        "Learn about unit testing, integration testing, and TDD practices",
                        "Learning",
                        "https://martinfowler.com/testing/",
                        List.of("testing", "tdd", "quality-assurance")
                ));
            }
            
            return suggestions;
        }
        
        private List<Suggestion> generateLearningOpportunitySuggestions(CodeContext context, 
                                                                       AnalysisResult analysisResult,
                                                                       DeveloperProfile profile) {
            List<Suggestion> suggestions = new ArrayList<>();
            
            // Suggest learning based on code complexity
            if (analysisResult.getComplexity().getCyclomaticComplexity() > 15) {
                suggestions.add(Suggestion.learning(
                        "Learn Refactoring Techniques",
                        "Your code has high complexity. Learn refactoring techniques to improve maintainability",
                        "Learning",
                        "https://refactoring.com/",
                        List.of("refactoring", "clean-code", "maintainability")
                ));
            }
            
            // Suggest learning based on security issues
            if (!analysisResult.getSecurityIssues().isEmpty()) {
                suggestions.add(Suggestion.learning(
                        "Learn Secure Coding Practices",
                        "Security issues detected. Learn about secure coding practices and OWASP guidelines",
                        "Learning",
                        "https://owasp.org/www-project-secure-coding-practices-quick-reference-guide/",
                        List.of("security", "owasp", "secure-coding")
                ));
            }
            
            return suggestions;
        }
    }
}