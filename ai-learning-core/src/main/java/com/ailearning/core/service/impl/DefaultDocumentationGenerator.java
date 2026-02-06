package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.DocumentationGenerator;
import com.ailearning.core.service.ast.ASTParser;
import com.ailearning.core.model.ast.*;
import com.ailearning.core.service.impl.DocumentationStyleGuideChecker.ProjectStyleGuide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Default implementation of DocumentationGenerator that provides template-based
 * documentation generation for multiple programming languages.
 */
public class DefaultDocumentationGenerator implements DocumentationGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultDocumentationGenerator.class);
    
    private final ASTParser astParser;
    private final DocumentationTemplateEngine templateEngine;
    private final DocumentationValidator validator;
    private final DocumentationChangeDetector changeDetector;
    private final DocumentationStyleGuideChecker styleGuideChecker;
    private final Map<String, List<Documentation>> documentationCache;
    
    public DefaultDocumentationGenerator(ASTParser astParser) {
        this.astParser = astParser;
        this.templateEngine = new DocumentationTemplateEngine();
        this.validator = new DocumentationValidator();
        this.changeDetector = new DocumentationChangeDetector(astParser);
        this.styleGuideChecker = new DocumentationStyleGuideChecker();
        this.documentationCache = new HashMap<>();
    }
    
    public DefaultDocumentationGenerator(ASTParser astParser, ProjectStyleGuide projectStyleGuide) {
        this.astParser = astParser;
        this.templateEngine = new DocumentationTemplateEngine();
        this.validator = new DocumentationValidator();
        this.changeDetector = new DocumentationChangeDetector(astParser);
        this.styleGuideChecker = new DocumentationStyleGuideChecker(projectStyleGuide);
        this.documentationCache = new HashMap<>();
    }
    
    @Override
    public CompletableFuture<String> generateInlineComments(String code, String language, String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating inline comments for file: {}", filePath);
                
                // Parse the code to identify elements that need documentation
                ParseResult parseResult = astParser.parse(code, language);
                if (!parseResult.isSuccess()) {
                    logger.warn("Failed to parse code for inline comments: {}", parseResult.getErrors());
                    return code; // Return original code if parsing fails
                }
                
                StringBuilder documentedCode = new StringBuilder();
                String[] lines = code.split("\n");
                int currentLine = 0;
                
                // Process each AST node and add comments
                for (ASTNode node : parseResult.getRootNode().getChildren()) {
                    if (needsDocumentation(node, language)) {
                        // Add comment before the element
                        String comment = generateCommentForNode(node, language);
                        if (comment != null && !comment.trim().isEmpty()) {
                            // Insert comment at the appropriate line
                            int nodeLineNumber = node.getLocation().getLine() - 1;
                            
                            // Add lines up to the node
                            while (currentLine < nodeLineNumber) {
                                documentedCode.append(lines[currentLine]).append("\n");
                                currentLine++;
                            }
                            
                            // Add the generated comment
                            documentedCode.append(comment).append("\n");
                        }
                    }
                }
                
                // Add remaining lines
                while (currentLine < lines.length) {
                    documentedCode.append(lines[currentLine]).append("\n");
                    currentLine++;
                }
                
                return documentedCode.toString();
                
            } catch (Exception e) {
                logger.error("Error generating inline comments", e);
                return code; // Return original code on error
            }
        });
    }
    
    @Override
    public CompletableFuture<Documentation> createAPIDocumentation(CodeContext moduleContext, 
                                                                  Documentation.Format format) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Creating API documentation for module: {}", moduleContext.getFileName());
                
                String code = moduleContext.getCurrentFile();
                String language = moduleContext.getLanguage();
                
                ParseResult parseResult = astParser.parse(code, language);
                if (!parseResult.isSuccess()) {
                    throw new RuntimeException("Failed to parse code for API documentation");
                }
                
                StringBuilder apiDoc = new StringBuilder();
                
                // Generate API documentation based on format
                switch (format) {
                    case MARKDOWN -> apiDoc.append(generateMarkdownAPI(parseResult, moduleContext));
                    case JSON -> apiDoc.append(generateJsonAPI(parseResult, moduleContext));
                    case HTML -> apiDoc.append(generateHtmlAPI(parseResult, moduleContext));
                    default -> apiDoc.append(generatePlainTextAPI(parseResult, moduleContext));
                }
                
                return Documentation.apiDoc(apiDoc.toString(), 
                        moduleContext.getFileName(), "module");
                
            } catch (Exception e) {
                logger.error("Error creating API documentation", e);
                throw new RuntimeException("Failed to create API documentation", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<DocumentationUpdate>> updateDocumentation(List<CodeChange> changes, 
                                                                           List<Documentation> existingDocs) {
        return CompletableFuture.supplyAsync(() -> {
            List<DocumentationUpdate> updates = new ArrayList<>();
            
            try {
                logger.debug("Processing {} code changes for documentation updates", changes.size());
                
                for (CodeChange change : changes) {
                    // Use change detector for intelligent change analysis
                    if (change.getType() == CodeChange.ChangeType.FILE_MODIFIED && 
                        change.getOldContent() != null && change.getNewContent() != null) {
                        
                        List<DocumentationChangeEvent> changeEvents = changeDetector.detectChanges(
                                change.getFilePath(), 
                                change.getOldContent(), 
                                change.getNewContent(), 
                                determineLanguage(change.getFilePath())
                        );
                        
                        // Analyze impact of changes
                        DocumentationChangeDetector.DocumentationImpactAnalysis impact = 
                                changeDetector.analyzeImpact(changeEvents, existingDocs);
                        
                        // Generate updates based on change events
                        updates.addAll(processChangeEvents(changeEvents, existingDocs));
                        
                        // Log impact analysis
                        if (impact.hasSignificantImpact()) {
                            logger.info("Significant documentation impact detected: {}", impact);
                        }
                        
                    } else {
                        // Fallback to original change processing
                        List<DocumentationUpdate> changeUpdates = processCodeChange(change, existingDocs);
                        updates.addAll(changeUpdates);
                    }
                }
                
                logger.debug("Generated {} documentation updates for {} changes", 
                           updates.size(), changes.size());
                return updates;
                
            } catch (Exception e) {
                logger.error("Error updating documentation", e);
                return updates;
            }
        });
    }
    
    @Override
    public CompletableFuture<ValidationResult> validateDocumentation(Documentation documentation, 
                                                                   String code, String language) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Validating documentation: {}", documentation.getId());
                
                // Perform basic validation
                ValidationResult basicValidation = validator.validate(documentation, code, language);
                
                // Perform style guide compliance checking
                DocumentationStyleGuideChecker.StyleGuideComplianceResult styleCompliance = 
                        styleGuideChecker.checkCompliance(documentation, language, null);
                
                // Combine results
                List<ValidationResult.ValidationIssue> allIssues = new ArrayList<>(basicValidation.getIssues());
                allIssues.addAll(styleCompliance.getIssues());
                
                List<String> allSuggestions = new ArrayList<>(basicValidation.getSuggestions());
                allSuggestions.addAll(styleCompliance.getSuggestions());
                
                // Calculate combined accuracy score
                double combinedScore = (basicValidation.getAccuracyScore() + styleCompliance.getComplianceScore()) / 2.0;
                
                // Determine overall status
                ValidationResult.Status status = determineValidationStatus(allIssues, styleCompliance);
                
                return ValidationResult.builder()
                        .status(status)
                        .accuracyScore(combinedScore)
                        .issues(allIssues)
                        .suggestions(allSuggestions)
                        .build();
                
            } catch (Exception e) {
                logger.error("Error validating documentation", e);
                return ValidationResult.invalid(
                        List.of(ValidationResult.ValidationIssue.create(
                                ValidationResult.IssueType.ACCURACY,
                                ValidationResult.ValidationIssue.Severity.HIGH,
                                "Validation failed: " + e.getMessage()
                        )),
                        List.of("Fix validation errors and try again")
                );
            }
        });
    }
    
    @Override
    public CompletableFuture<Documentation> generateElementDocumentation(String elementName, 
                                                                        String elementType,
                                                                        String code, 
                                                                        String language, 
                                                                        String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Generating documentation for element: {} ({})", elementName, elementType);
                
                ParseResult parseResult = astParser.parse(code, language);
                if (!parseResult.isSuccess()) {
                    throw new RuntimeException("Failed to parse code for element documentation");
                }
                
                // Find the specific element in the AST
                ASTNode elementNode = findElementNode(parseResult.getRootNode(), elementName, elementType);
                if (elementNode == null) {
                    throw new RuntimeException("Element not found: " + elementName);
                }
                
                // Generate documentation based on language
                String docContent = generateDocumentationContent(elementNode, language, elementType);
                Documentation.Type docType = getDocumentationType(language);
                
                return Documentation.builder()
                        .type(docType)
                        .format(Documentation.Format.PLAIN_TEXT)
                        .content(docContent)
                        .filePath(filePath)
                        .lineNumber(elementNode.getLocation().getLine())
                        .elementName(elementName)
                        .elementType(elementType)
                        .template(templateEngine.getTemplateName(language, elementType))
                        .build();
                
            } catch (Exception e) {
                logger.error("Error generating element documentation", e);
                throw new RuntimeException("Failed to generate element documentation", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Documentation> createMarkdownDocumentation(ProjectContext projectContext, 
                                                                       String template) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Creating markdown documentation for project: {}", projectContext.getProjectName());
                
                String markdownContent = templateEngine.generateMarkdown(projectContext, template);
                
                return Documentation.markdown(markdownContent, 
                        projectContext.getRootPath() + "/README.md", 
                        projectContext.getProjectName());
                
            } catch (Exception e) {
                logger.error("Error creating markdown documentation", e);
                throw new RuntimeException("Failed to create markdown documentation", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<DocumentationUpdate>> synchronizeDocumentation(String filePath, 
                                                                                String oldCode, 
                                                                                String newCode, 
                                                                                String language) {
        return CompletableFuture.supplyAsync(() -> {
            List<DocumentationUpdate> updates = new ArrayList<>();
            
            try {
                logger.debug("Synchronizing documentation for file: {}", filePath);
                
                // Use intelligent change detection
                List<DocumentationChangeEvent> changeEvents = changeDetector.detectChanges(
                        filePath, oldCode, newCode, language);
                
                if (changeEvents.isEmpty()) {
                    logger.debug("No significant changes detected for documentation synchronization");
                    return updates;
                }
                
                // Get existing documentation for this file
                List<Documentation> existingDocs = documentationCache.getOrDefault(filePath, List.of());
                
                // Analyze impact
                DocumentationChangeDetector.DocumentationImpactAnalysis impact = 
                        changeDetector.analyzeImpact(changeEvents, existingDocs);
                
                // Generate updates based on change events
                updates.addAll(processChangeEvents(changeEvents, existingDocs));
                
                // Add recommendations from impact analysis
                for (String recommendation : impact.getRecommendations()) {
                    updates.add(DocumentationUpdate.created(
                            "recommendation-" + UUID.randomUUID(),
                            recommendation,
                            "Automated recommendation based on code changes"
                    ));
                }
                
                logger.debug("Generated {} synchronization updates for file: {} (impact score: {:.2f})", 
                           updates.size(), filePath, impact.getImpactScore());
                return updates;
                
            } catch (Exception e) {
                logger.error("Error synchronizing documentation", e);
                return updates;
            }
        });
    }
    
    @Override
    public List<String> getAvailableTemplates(String language, String elementType) {
        return templateEngine.getAvailableTemplates(language, elementType);
    }
    
    @Override
    public boolean hasDocumentation(String filePath, String elementName, String elementType) {
        List<Documentation> docs = documentationCache.get(filePath);
        if (docs == null) return false;
        
        return docs.stream().anyMatch(doc -> 
                Objects.equals(doc.getElementName(), elementName) &&
                Objects.equals(doc.getElementType(), elementType));
    }
    
    @Override
    public DocumentationStats getDocumentationStats(ProjectContext projectContext, String filePath) {
        try {
            if (filePath != null) {
                return calculateFileStats(filePath);
            } else if (projectContext != null) {
                return calculateProjectStats(projectContext);
            } else {
                return new DocumentationStats(0, 0, 0, 0.0, 0.0);
            }
        } catch (Exception e) {
            logger.error("Error calculating documentation stats", e);
            return new DocumentationStats(0, 0, 0, 0.0, 0.0);
        }
    }
    
    /**
     * Learns project-specific documentation conventions from existing documentation.
     */
    public void learnProjectConventions(List<Documentation> existingDocs, ProjectContext projectContext) {
        try {
            logger.debug("Learning project documentation conventions from {} documents", existingDocs.size());
            styleGuideChecker.learnProjectConventions(existingDocs, projectContext);
            
            // Also update template engine with learned patterns
            updateTemplateEngineWithConventions(existingDocs, projectContext);
        } catch (Exception e) {
            logger.error("Error learning project conventions", e);
        }
    }
    
    /**
     * Validates documentation against project conventions.
     */
    public CompletableFuture<ValidationResult> validateAgainstConventions(Documentation documentation, 
                                                                         List<Documentation> existingDocs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return styleGuideChecker.validateAgainstConventions(documentation, existingDocs);
            } catch (Exception e) {
                logger.error("Error validating against conventions", e);
                return ValidationResult.invalid(
                        List.of(ValidationResult.ValidationIssue.create(
                                ValidationResult.IssueType.CONSISTENCY,
                                ValidationResult.ValidationIssue.Severity.HIGH,
                                "Convention validation failed: " + e.getMessage()
                        )),
                        List.of("Fix validation errors and try again")
                );
            }
        });
    }
    
    /**
     * Checks style guide compliance for documentation.
     */
    public CompletableFuture<DocumentationStyleGuideChecker.StyleGuideComplianceResult> checkStyleCompliance(
            Documentation documentation, String language, ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return styleGuideChecker.checkCompliance(documentation, language, projectContext);
            } catch (Exception e) {
                logger.error("Error checking style compliance", e);
                return DocumentationStyleGuideChecker.StyleGuideComplianceResult.error(
                        "Style compliance check failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Performs intelligent documentation analysis and provides improvement suggestions.
     */
    public CompletableFuture<DocumentationAnalysisResult> analyzeDocumentation(
            Documentation documentation, String code, String language, ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Performing intelligent analysis for documentation: {}", documentation.getId());
                
                List<String> suggestions = new ArrayList<>();
                List<String> improvements = new ArrayList<>();
                double qualityScore = 1.0;
                
                // Validate against code
                ValidationResult codeValidation = validateDocumentation(documentation, code, language).join();
                qualityScore = Math.min(qualityScore, codeValidation.getAccuracyScore());
                
                // Check style compliance
                DocumentationStyleGuideChecker.StyleGuideComplianceResult styleResult = 
                        checkStyleCompliance(documentation, language, projectContext).join();
                qualityScore = Math.min(qualityScore, styleResult.getComplianceScore());
                
                // Analyze content quality
                ContentQualityAnalysis contentAnalysis = analyzeContentQuality(documentation, code, language);
                qualityScore = Math.min(qualityScore, contentAnalysis.getQualityScore());
                
                // Generate intelligent suggestions
                suggestions.addAll(codeValidation.getSuggestions());
                suggestions.addAll(styleResult.getSuggestions());
                suggestions.addAll(contentAnalysis.getSuggestions());
                
                // Generate improvement recommendations
                improvements.addAll(generateImprovementRecommendations(documentation, code, language));
                
                return new DocumentationAnalysisResult(
                        qualityScore,
                        suggestions,
                        improvements,
                        codeValidation,
                        styleResult,
                        contentAnalysis
                );
                
            } catch (Exception e) {
                logger.error("Error analyzing documentation", e);
                return DocumentationAnalysisResult.error("Analysis failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Automatically improves documentation based on analysis results.
     */
    public CompletableFuture<DocumentationUpdate> autoImproveDocumentation(
            Documentation documentation, String code, String language, ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Auto-improving documentation: {}", documentation.getId());
                
                // Analyze current documentation
                DocumentationAnalysisResult analysis = analyzeDocumentation(
                        documentation, code, language, projectContext).join();
                
                if (analysis.getQualityScore() > 0.8) {
                    // Documentation is already good, no changes needed
                    return DocumentationUpdate.builder()
                            .documentationId(documentation.getId())
                            .updateType(DocumentationUpdate.UpdateType.SYNCHRONIZED)
                            .newContent(documentation.getContent())
                            .reason("Documentation quality is already high")
                            .build();
                }
                
                // Apply improvements
                String improvedContent = applyAutomaticImprovements(
                        documentation.getContent(), analysis, code, language);
                
                return DocumentationUpdate.builder()
                        .documentationId(documentation.getId())
                        .updateType(DocumentationUpdate.UpdateType.MODIFIED)
                        .oldContent(documentation.getContent())
                        .newContent(improvedContent)
                        .reason("Automatic improvements applied based on analysis")
                        .build();
                
            } catch (Exception e) {
                logger.error("Error auto-improving documentation", e);
                return DocumentationUpdate.builder()
                        .documentationId(documentation.getId())
                        .updateType(DocumentationUpdate.UpdateType.SYNCHRONIZED)
                        .newContent(documentation.getContent())
                        .reason("Auto-improvement failed: " + e.getMessage())
                        .build();
            }
        });
    }
    
    /**
     * Detects outdated documentation that needs updates.
     */
    public CompletableFuture<List<Documentation>> detectOutdatedDocumentation(
            List<Documentation> allDocumentation, ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Detecting outdated documentation from {} documents", allDocumentation.size());
                
                List<Documentation> outdated = new ArrayList<>();
                
                for (Documentation doc : allDocumentation) {
                    if (isDocumentationOutdated(doc, projectContext)) {
                        outdated.add(doc);
                    }
                }
                
                logger.debug("Found {} outdated documents", outdated.size());
                return outdated;
                
            } catch (Exception e) {
                logger.error("Error detecting outdated documentation", e);
                return List.of();
            }
        });
    }
    
    // Helper methods for enhanced intelligent documentation updates
    
    /**
     * Updates template engine with learned project conventions.
     */
    private void updateTemplateEngineWithConventions(List<Documentation> existingDocs, ProjectContext projectContext) {
        // Analyze existing documentation patterns and update template engine
        // This would involve learning common phrases, structures, and styles
        logger.debug("Updating template engine with project conventions");
    }
    
    /**
     * Analyzes content quality of documentation.
     */
    private ContentQualityAnalysis analyzeContentQuality(Documentation documentation, String code, String language) {
        try {
            String content = documentation.getContent();
            List<String> suggestions = new ArrayList<>();
            double qualityScore = 1.0;
            
            // Check content length and detail
            String cleanContent = stripDocumentationSyntax(content);
            if (cleanContent.length() < 20) {
                suggestions.add("Add more detailed description");
                qualityScore -= 0.2;
            }
            
            // Check for meaningful descriptions
            if (containsGenericPhrases(cleanContent)) {
                suggestions.add("Replace generic phrases with specific descriptions");
                qualityScore -= 0.1;
            }
            
            // Check for examples
            if (isComplexMethod(code) && !containsExamples(content)) {
                suggestions.add("Add usage examples for complex methods");
                qualityScore -= 0.1;
            }
            
            // Check parameter documentation completeness
            if (hasParameters(code) && !hasParameterDocumentation(content)) {
                suggestions.add("Add parameter documentation");
                qualityScore -= 0.2;
            }
            
            return new ContentQualityAnalysis(Math.max(0.0, qualityScore), suggestions);
            
        } catch (Exception e) {
            logger.warn("Error analyzing content quality", e);
            return new ContentQualityAnalysis(0.5, List.of("Content quality analysis failed"));
        }
    }
    
    /**
     * Generates improvement recommendations based on documentation analysis.
     */
    private List<String> generateImprovementRecommendations(Documentation documentation, String code, String language) {
        List<String> recommendations = new ArrayList<>();
        
        try {
            String content = documentation.getContent();
            
            // Analyze code complexity and suggest appropriate documentation level
            if (isComplexMethod(code)) {
                recommendations.add("Consider adding detailed explanation for complex logic");
                recommendations.add("Add usage examples to clarify expected behavior");
            }
            
            // Check for missing sections based on element type
            if ("method".equals(documentation.getElementType()) || "function".equals(documentation.getElementType())) {
                if (!content.contains("@param") && hasParameters(code)) {
                    recommendations.add("Add @param documentation for all parameters");
                }
                if (!content.contains("@return") && hasReturnValue(code)) {
                    recommendations.add("Add @return documentation for return value");
                }
                if (!content.contains("@throws") && mayThrowExceptions(code)) {
                    recommendations.add("Document potential exceptions with @throws");
                }
            }
            
            // Language-specific recommendations
            switch (language.toLowerCase()) {
                case "java" -> {
                    if (!content.contains("@since") && isPublicAPI(code)) {
                        recommendations.add("Add @since tag for public API methods");
                    }
                }
                case "javascript", "typescript" -> {
                    if (!content.contains("@example") && isComplexMethod(code)) {
                        recommendations.add("Add @example section for complex functions");
                    }
                }
                case "python" -> {
                    if (!content.contains("Examples:") && isComplexMethod(code)) {
                        recommendations.add("Add Examples section following Python conventions");
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Error generating improvement recommendations", e);
            recommendations.add("Review documentation for completeness and accuracy");
        }
        
        return recommendations;
    }
    
    /**
     * Applies automatic improvements to documentation content.
     */
    private String applyAutomaticImprovements(String originalContent, DocumentationAnalysisResult analysis, 
                                            String code, String language) {
        try {
            String improvedContent = originalContent;
            
            // Fix common formatting issues
            improvedContent = fixCommonFormattingIssues(improvedContent, language);
            
            // Add missing parameter documentation
            if (hasParameters(code) && !hasParameterDocumentation(improvedContent)) {
                improvedContent = addParameterDocumentation(improvedContent, code, language);
            }
            
            // Add missing return documentation
            if (hasReturnValue(code) && !hasReturnDocumentation(improvedContent)) {
                improvedContent = addReturnDocumentation(improvedContent, code, language);
            }
            
            // Improve generic descriptions
            improvedContent = improveGenericDescriptions(improvedContent);
            
            return improvedContent;
            
        } catch (Exception e) {
            logger.warn("Error applying automatic improvements", e);
            return originalContent;
        }
    }
    
    /**
     * Checks if documentation is outdated based on various factors.
     */
    private boolean isDocumentationOutdated(Documentation documentation, ProjectContext projectContext) {
        try {
            // Check if documentation is stale (older than 30 days)
            if (documentation.isStale(30 * 24 * 60 * 60 * 1000L)) {
                return true;
            }
            
            // Check if file has been modified recently but documentation hasn't
            String filePath = documentation.getFilePath();
            if (filePath != null) {
                // This would check file modification times in a real implementation
                // For now, return false as we don't have access to file system
            }
            
            // Check if documentation contains outdated patterns or references
            String content = documentation.getContent();
            if (containsOutdatedReferences(content, projectContext)) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.warn("Error checking if documentation is outdated", e);
            return false;
        }
    }
    
    // Utility methods for content analysis
    
    private String stripDocumentationSyntax(String content) {
        if (content == null) return "";
        
        return content
                .replaceAll("/\\*\\*|\\*/|\\*", "")
                .replaceAll("\"\"\"", "")
                .replaceAll("^\\s*[*\\-]\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
    
    private boolean containsGenericPhrases(String content) {
        String lowerContent = content.toLowerCase();
        return lowerContent.contains("auto-generated") ||
               lowerContent.contains("generated automatically") ||
               lowerContent.contains("description here") ||
               lowerContent.contains("todo") ||
               lowerContent.contains("fixme");
    }
    
    private boolean isComplexMethod(String code) {
        if (code == null) return false;
        
        // Simple heuristics for complexity
        int lineCount = code.split("\n").length;
        int braceCount = code.length() - code.replace("{", "").length();
        int ifCount = code.split("\\bif\\b").length - 1;
        int forCount = code.split("\\bfor\\b").length - 1;
        int whileCount = code.split("\\bwhile\\b").length - 1;
        
        return lineCount > 10 || braceCount > 3 || (ifCount + forCount + whileCount) > 2;
    }
    
    private boolean containsExamples(String content) {
        String lowerContent = content.toLowerCase();
        return lowerContent.contains("@example") ||
               lowerContent.contains("example:") ||
               lowerContent.contains("examples:") ||
               lowerContent.contains("usage:");
    }
    
    private boolean hasParameters(String code) {
        return code != null && code.contains("(") && code.contains(")") && 
               !code.matches(".*\\(\\s*\\).*");
    }
    
    private boolean hasParameterDocumentation(String content) {
        return content != null && content.contains("@param");
    }
    
    private boolean hasReturnValue(String code) {
        return code != null && !code.contains("void ") && code.contains("return ");
    }
    
    private boolean hasReturnDocumentation(String content) {
        return content != null && (content.contains("@return") || content.contains("Returns:"));
    }
    
    private boolean mayThrowExceptions(String code) {
        return code != null && (code.contains("throw ") || code.contains("throws "));
    }
    
    private boolean isPublicAPI(String code) {
        return code != null && code.contains("public ");
    }
    
    private boolean containsOutdatedReferences(String content, ProjectContext projectContext) {
        // This would check for outdated version references, deprecated APIs, etc.
        // For now, return false as this requires more context
        return false;
    }
    
    private String fixCommonFormattingIssues(String content, String language) {
        String fixed = content;
        
        // Fix double spaces
        fixed = fixed.replaceAll("  +", " ");
        
        // Ensure proper sentence capitalization
        if (!fixed.isEmpty() && Character.isLowerCase(fixed.charAt(0))) {
            fixed = Character.toUpperCase(fixed.charAt(0)) + fixed.substring(1);
        }
        
        // Language-specific formatting fixes
        switch (language.toLowerCase()) {
            case "java" -> {
                // Ensure Javadoc ends with period
                if (!fixed.trim().endsWith(".") && !fixed.trim().endsWith("*/")) {
                    fixed = fixed.trim() + ".";
                }
            }
        }
        
        return fixed;
    }
    
    private String addParameterDocumentation(String content, String code, String language) {
        // Extract parameters and add basic documentation
        List<String> parameters = extractParametersFromCode(code);
        if (parameters.isEmpty()) {
            return content;
        }
        
        StringBuilder paramDocs = new StringBuilder();
        for (String param : parameters) {
            paramDocs.append("\n * @param ").append(param).append(" the ").append(param).append(" parameter");
        }
        
        // Insert before closing comment
        if (content.contains("*/")) {
            return content.replace("*/", paramDocs.toString() + "\n */");
        } else {
            return content + paramDocs.toString();
        }
    }
    
    private String addReturnDocumentation(String content, String code, String language) {
        String returnType = inferReturnTypeFromCode(code);
        String returnDoc = "\n * @return the " + returnType + " result";
        
        // Insert before closing comment
        if (content.contains("*/")) {
            return content.replace("*/", returnDoc + "\n */");
        } else {
            return content + returnDoc;
        }
    }
    
    private String improveGenericDescriptions(String content) {
        return content
                .replace("auto-generated description", "description of the functionality")
                .replace("TODO: Add description", "description of the functionality")
                .replace("FIXME", "note");
    }
    
    private List<String> extractParametersFromCode(String code) {
        List<String> parameters = new ArrayList<>();
        
        int startParen = code.indexOf('(');
        int endParen = code.lastIndexOf(')');
        
        if (startParen >= 0 && endParen > startParen) {
            String paramString = code.substring(startParen + 1, endParen).trim();
            if (!paramString.isEmpty()) {
                String[] params = paramString.split(",");
                for (String param : params) {
                    String paramName = extractParameterName(param.trim());
                    if (!paramName.isEmpty()) {
                        parameters.add(paramName);
                    }
                }
            }
        }
        
        return parameters;
    }
    
    private String extractParameterName(String paramDeclaration) {
        String[] parts = paramDeclaration.trim().split("\\s+");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            return lastPart.replaceAll("[\\[\\]<>].*", "");
        }
        return "";
    }
    
    private String inferReturnTypeFromCode(String code) {
        // Simple return type inference
        if (code.contains("boolean ")) return "boolean";
        if (code.contains("int ")) return "integer";
        if (code.contains("String ")) return "string";
        if (code.contains("List")) return "list";
        if (code.contains("Map")) return "map";
        return "result";
    }
    
    // Supporting classes for enhanced functionality
    
    /**
     * Result of content quality analysis.
     */
    public static class ContentQualityAnalysis {
        private final double qualityScore;
        private final List<String> suggestions;
        
        public ContentQualityAnalysis(double qualityScore, List<String> suggestions) {
            this.qualityScore = qualityScore;
            this.suggestions = suggestions != null ? List.copyOf(suggestions) : List.of();
        }
        
        public double getQualityScore() { return qualityScore; }
        public List<String> getSuggestions() { return suggestions; }
    }
    
    /**
     * Comprehensive documentation analysis result.
     */
    public static class DocumentationAnalysisResult {
        private final double qualityScore;
        private final List<String> suggestions;
        private final List<String> improvements;
        private final ValidationResult codeValidation;
        private final DocumentationStyleGuideChecker.StyleGuideComplianceResult styleCompliance;
        private final ContentQualityAnalysis contentAnalysis;
        private final String errorMessage;
        
        public DocumentationAnalysisResult(double qualityScore, List<String> suggestions, 
                                         List<String> improvements, ValidationResult codeValidation,
                                         DocumentationStyleGuideChecker.StyleGuideComplianceResult styleCompliance,
                                         ContentQualityAnalysis contentAnalysis) {
            this.qualityScore = qualityScore;
            this.suggestions = suggestions != null ? List.copyOf(suggestions) : List.of();
            this.improvements = improvements != null ? List.copyOf(improvements) : List.of();
            this.codeValidation = codeValidation;
            this.styleCompliance = styleCompliance;
            this.contentAnalysis = contentAnalysis;
            this.errorMessage = null;
        }
        
        private DocumentationAnalysisResult(String errorMessage) {
            this.qualityScore = 0.0;
            this.suggestions = List.of();
            this.improvements = List.of();
            this.codeValidation = null;
            this.styleCompliance = null;
            this.contentAnalysis = null;
            this.errorMessage = errorMessage;
        }
        
        public static DocumentationAnalysisResult error(String message) {
            return new DocumentationAnalysisResult(message);
        }
        
        public boolean hasErrors() { return errorMessage != null; }
        
        // Getters
        public double getQualityScore() { return qualityScore; }
        public List<String> getSuggestions() { return suggestions; }
        public List<String> getImprovements() { return improvements; }
        public ValidationResult getCodeValidation() { return codeValidation; }
        public DocumentationStyleGuideChecker.StyleGuideComplianceResult getStyleCompliance() { return styleCompliance; }
        public ContentQualityAnalysis getContentAnalysis() { return contentAnalysis; }
        public String getErrorMessage() { return errorMessage; }
    }
    private List<DocumentationUpdate> processChangeEvents(List<DocumentationChangeEvent> changeEvents, 
                                                         List<Documentation> existingDocs) {
        List<DocumentationUpdate> updates = new ArrayList<>();
        
        for (DocumentationChangeEvent event : changeEvents) {
            switch (event.getChangeType()) {
                case ELEMENT_ADDED -> {
                    if (event.getNewElement() != null) {
                        updates.add(DocumentationUpdate.created(
                                "element-" + event.getNewElement().getName(),
                                generateDocumentationForElement(event.getNewElement()),
                                "New element requires documentation: " + event.getDescription()
                        ));
                    }
                }
                case ELEMENT_DELETED -> {
                    if (event.getOldElement() != null) {
                        // Find existing documentation for this element
                        Documentation existingDoc = findDocumentationForElement(
                                event.getOldElement(), existingDocs);
                        if (existingDoc != null) {
                            updates.add(DocumentationUpdate.deleted(
                                    existingDoc.getId(),
                                    existingDoc.getContent(),
                                    "Element deleted: " + event.getDescription()
                            ));
                        }
                    }
                }
                case ELEMENT_MODIFIED, SIGNATURE_CHANGED -> {
                    if (event.getNewElement() != null) {
                        // Find existing documentation for this element
                        Documentation existingDoc = findDocumentationForElement(
                                event.getNewElement(), existingDocs);
                        if (existingDoc != null) {
                            String updatedContent = updateDocumentationContent(
                                    existingDoc, event.getNewElement());
                            updates.add(DocumentationUpdate.modified(
                                    existingDoc.getId(),
                                    updatedContent,
                                    "Element modified: " + event.getDescription(),
                                    event.getReason()
                            ));
                        } else {
                            // Create new documentation if none exists
                            updates.add(DocumentationUpdate.created(
                                    "element-" + event.getNewElement().getName(),
                                    generateDocumentationForElement(event.getNewElement()),
                                    "Documentation needed for modified element: " + event.getDescription()
                            ));
                        }
                    }
                }
            }
        }
        
        return updates;
    }
    
    /**
     * Generates documentation content for a code element.
     */
    private String generateDocumentationForElement(DocumentationChangeDetector.CodeElement element) {
        // Use template engine to generate appropriate documentation
        return templateEngine.generateDocumentationForElement(
                element.getName(), 
                element.getType(), 
                element.getSignature(),
                element.getParameters(),
                element.getReturnType()
        );
    }
    
    /**
     * Finds existing documentation for a code element.
     */
    private Documentation findDocumentationForElement(DocumentationChangeDetector.CodeElement element, 
                                                     List<Documentation> existingDocs) {
        return existingDocs.stream()
                .filter(doc -> Objects.equals(doc.getElementName(), element.getName()) &&
                              Objects.equals(doc.getElementType(), element.getType()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Updates existing documentation content based on element changes.
     */
    private String updateDocumentationContent(Documentation existingDoc, 
                                            DocumentationChangeDetector.CodeElement newElement) {
        // Intelligent content update based on what changed
        String existingContent = existingDoc.getContent();
        
        // Update parameter documentation if parameters changed
        if (newElement.getParameters() != null && !newElement.getParameters().isEmpty()) {
            existingContent = updateParameterDocumentation(existingContent, newElement.getParameters());
        }
        
        // Update return type documentation if return type changed
        if (newElement.getReturnType() != null && !newElement.getReturnType().equals("void")) {
            existingContent = updateReturnTypeDocumentation(existingContent, newElement.getReturnType());
        }
        
        return existingContent;
    }
    
    /**
     * Updates parameter documentation in existing content.
     */
    private String updateParameterDocumentation(String content, List<String> parameters) {
        // Simple parameter documentation update - in practice would be more sophisticated
        StringBuilder updated = new StringBuilder(content);
        
        for (String param : parameters) {
            if (!content.contains("@param " + param)) {
                // Add missing parameter documentation
                int insertPos = content.lastIndexOf("*/");
                if (insertPos > 0) {
                    updated.insert(insertPos, " * @param " + param + " TODO: Add parameter description\n ");
                }
            }
        }
        
        return updated.toString();
    }
    
    /**
     * Updates return type documentation in existing content.
     */
    private String updateReturnTypeDocumentation(String content, String returnType) {
        if (!content.contains("@return") && !returnType.equals("void")) {
            // Add missing return documentation
            int insertPos = content.lastIndexOf("*/");
            if (insertPos > 0) {
                StringBuilder updated = new StringBuilder(content);
                updated.insert(insertPos, " * @return TODO: Add return description\n ");
                return updated.toString();
            }
        }
        return content;
    }
    
    /**
     * Determines the programming language from file path.
     */
    private String determineLanguage(String filePath) {
        if (filePath == null) return "unknown";
        
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "java" -> "java";
            case "js" -> "javascript";
            case "ts" -> "typescript";
            case "py" -> "python";
            case "cpp", "cc", "cxx" -> "cpp";
            case "c" -> "c";
            case "cs" -> "csharp";
            case "go" -> "go";
            default -> "unknown";
        };
    }
    
    /**
     * Determines validation status based on issues and style compliance.
     */
    private ValidationResult.Status determineValidationStatus(
            List<ValidationResult.ValidationIssue> issues,
            DocumentationStyleGuideChecker.StyleGuideComplianceResult styleCompliance) {
        
        if (styleCompliance.hasErrors()) {
            return ValidationResult.Status.INVALID;
        }
        
        boolean hasCriticalIssues = issues.stream()
                .anyMatch(issue -> issue.getSeverity() == ValidationResult.ValidationIssue.Severity.HIGH);
        
        if (hasCriticalIssues) {
            return ValidationResult.Status.INVALID;
        }
        
        if (!styleCompliance.isCompliant()) {
            return ValidationResult.Status.WARNING;
        }
        
        return issues.isEmpty() ? ValidationResult.Status.VALID : ValidationResult.Status.WARNING;
    }
    
    // Existing helper methods
    
    private boolean needsDocumentation(ASTNode node, String language) {
        // Check if the node represents a documentable element
        switch (node.getNodeType()) {
            case CLASS, METHOD, FUNCTION -> {
                return true;
            }
            case VARIABLE -> {
                // Only document public/exported variables
                return isPublicElement(node, language);
            }
            default -> {
                return false;
            }
        }
    }
    
    private boolean isPublicElement(ASTNode node, String language) {
        // Simple heuristic for public elements
        String nodeText = node.getText();
        if (nodeText == null) return false;
        
        switch (language.toLowerCase()) {
            case "java" -> {
                return nodeText.contains("public");
            }
            case "javascript", "typescript" -> {
                return nodeText.contains("export") || nodeText.contains("public");
            }
            case "python" -> {
                return !nodeText.startsWith("_"); // Not private
            }
            default -> {
                return true; // Default to documenting everything
            }
        }
    }
    
    private String generateCommentForNode(ASTNode node, String language) {
        String elementType = node.getNodeType().toString().toLowerCase();
        String elementName = extractElementName(node);
        
        return templateEngine.generateComment(elementName, elementType, language, node);
    }
    
    private String extractElementName(ASTNode node) {
        // Extract the name of the element from the AST node
        if (node instanceof ClassNode classNode) {
            return classNode.getName();
        } else if (node instanceof MethodNode methodNode) {
            return methodNode.getName();
        } else if (node instanceof VariableNode variableNode) {
            return variableNode.getName();
        }
        
        // Fallback: try to extract from text
        String text = node.getText();
        if (text != null) {
            // Simple regex to extract identifiers
            Pattern pattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        return "unknown";
    }
    
    private String generateMarkdownAPI(ParseResult parseResult, CodeContext context) {
        StringBuilder md = new StringBuilder();
        md.append("# API Documentation\n\n");
        md.append("## ").append(context.getFileName()).append("\n\n");
        
        List<ASTNode> nodes = flattenAST(parseResult.getRootNode());
        
        // Group by type
        Map<ASTNode.NodeType, List<ASTNode>> groupedNodes = nodes.stream()
                .collect(Collectors.groupingBy(ASTNode::getNodeType));
        
        // Generate sections for each type
        if (groupedNodes.containsKey(ASTNode.NodeType.CLASS)) {
            md.append("### Classes\n\n");
            for (ASTNode classNode : groupedNodes.get(ASTNode.NodeType.CLASS)) {
                md.append("#### ").append(extractElementName(classNode)).append("\n\n");
                md.append("```").append(context.getLanguage()).append("\n");
                md.append(classNode.getText()).append("\n");
                md.append("```\n\n");
            }
        }
        
        if (groupedNodes.containsKey(ASTNode.NodeType.METHOD)) {
            md.append("### Methods\n\n");
            for (ASTNode methodNode : groupedNodes.get(ASTNode.NodeType.METHOD)) {
                md.append("#### ").append(extractElementName(methodNode)).append("\n\n");
                md.append("```").append(context.getLanguage()).append("\n");
                md.append(methodNode.getText()).append("\n");
                md.append("```\n\n");
            }
        }
        
        return md.toString();
    }
    
    private String generateJsonAPI(ParseResult parseResult, CodeContext context) {
        // Simple JSON API documentation
        return "{\n  \"module\": \"" + context.getFileName() + "\",\n  \"language\": \"" + 
               context.getLanguage() + "\",\n  \"elements\": []\n}";
    }
    
    private String generateHtmlAPI(ParseResult parseResult, CodeContext context) {
        return "<html><head><title>API Documentation</title></head><body>" +
               "<h1>" + context.getFileName() + "</h1>" +
               "<p>API documentation generated automatically.</p>" +
               "</body></html>";
    }
    
    private String generatePlainTextAPI(ParseResult parseResult, CodeContext context) {
        return "API Documentation for " + context.getFileName() + "\n" +
               "Language: " + context.getLanguage() + "\n" +
               "Generated automatically.";
    }
    
    private List<DocumentationUpdate> processCodeChange(CodeChange change, List<Documentation> existingDocs) {
        List<DocumentationUpdate> updates = new ArrayList<>();
        
        switch (change.getType()) {
            case FILE_CREATED -> {
                updates.add(DocumentationUpdate.created(
                        "new-" + UUID.randomUUID(),
                        "New file created: " + change.getFilePath(),
                        "File creation detected"
                ));
            }
            case FILE_MODIFIED -> {
                // Find existing documentation for this file
                List<Documentation> fileDocs = existingDocs.stream()
                        .filter(doc -> Objects.equals(doc.getFilePath(), change.getFilePath()))
                        .toList();
                
                for (Documentation doc : fileDocs) {
                    updates.add(DocumentationUpdate.modified(
                            doc.getId(),
                            doc.getContent(),
                            "Updated due to code changes",
                            "Code modification detected"
                    ));
                }
            }
            case FILE_DELETED -> {
                // Mark documentation for deletion
                List<Documentation> fileDocs = existingDocs.stream()
                        .filter(doc -> Objects.equals(doc.getFilePath(), change.getFilePath()))
                        .toList();
                
                for (Documentation doc : fileDocs) {
                    updates.add(DocumentationUpdate.deleted(
                            doc.getId(),
                            doc.getContent(),
                            "File deletion detected"
                    ));
                }
            }
        }
        
        return updates;
    }
    
    private ASTNode findElementNode(ASTNode root, String elementName, String elementType) {
        if (root == null) return null;
        
        // Check if this node matches
        if (elementName.equals(extractElementName(root)) && 
            elementType.equalsIgnoreCase(root.getNodeType().toString())) {
            return root;
        }
        
        // Search children
        for (ASTNode child : root.getChildren()) {
            ASTNode found = findElementNode(child, elementName, elementType);
            if (found != null) return found;
        }
        
        return null;
    }
    
    private String generateDocumentationContent(ASTNode node, String language, String elementType) {
        return templateEngine.generateDocumentation(node, language, elementType);
    }
    
    private Documentation.Type getDocumentationType(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> Documentation.Type.JAVADOC;
            case "javascript", "typescript" -> Documentation.Type.JSDOC;
            case "python" -> Documentation.Type.PYTHON_DOCSTRING;
            default -> Documentation.Type.INLINE_COMMENT;
        };
    }
    
    private List<ASTNode> flattenAST(ASTNode root) {
        List<ASTNode> nodes = new ArrayList<>();
        if (root != null) {
            nodes.add(root);
            for (ASTNode child : root.getChildren()) {
                nodes.addAll(flattenAST(child));
            }
        }
        return nodes;
    }
    
    private List<DocumentationUpdate> detectDocumentationChanges(List<ASTNode> oldNodes, 
                                                                List<ASTNode> newNodes, 
                                                                String filePath, 
                                                                String language) {
        List<DocumentationUpdate> updates = new ArrayList<>();
        
        // Simple change detection based on node names and types
        Set<String> oldElementNames = oldNodes.stream()
                .map(this::extractElementName)
                .collect(Collectors.toSet());
        
        Set<String> newElementNames = newNodes.stream()
                .map(this::extractElementName)
                .collect(Collectors.toSet());
        
        // Detect new elements
        for (String newElement : newElementNames) {
            if (!oldElementNames.contains(newElement)) {
                updates.add(DocumentationUpdate.created(
                        "element-" + newElement,
                        "Documentation for new element: " + newElement,
                        "New element detected"
                ));
            }
        }
        
        // Detect deleted elements
        for (String oldElement : oldElementNames) {
            if (!newElementNames.contains(oldElement)) {
                updates.add(DocumentationUpdate.deleted(
                        "element-" + oldElement,
                        "Documentation for deleted element: " + oldElement,
                        "Element deletion detected"
                ));
            }
        }
        
        return updates;
    }
    
    private DocumentationStats calculateFileStats(String filePath) {
        List<Documentation> docs = documentationCache.get(filePath);
        if (docs == null) {
            return new DocumentationStats(0, 0, 0, 0.0, 0.0);
        }
        
        int totalElements = 10; // Placeholder - would be calculated from AST
        int documentedElements = docs.size();
        int outdatedDocuments = (int) docs.stream()
                .filter(doc -> doc.isStale(24 * 60 * 60 * 1000)) // 24 hours
                .count();
        
        double coverage = totalElements > 0 ? (double) documentedElements / totalElements * 100 : 0.0;
        double averageAccuracy = docs.stream()
                .mapToDouble(Documentation::getAccuracyScore)
                .average()
                .orElse(0.0);
        
        return new DocumentationStats(totalElements, documentedElements, outdatedDocuments, 
                coverage, averageAccuracy);
    }
    
    private DocumentationStats calculateProjectStats(ProjectContext projectContext) {
        // Aggregate stats across all files in the project
        int totalElements = 0;
        int documentedElements = 0;
        int outdatedDocuments = 0;
        double totalAccuracy = 0.0;
        int docCount = 0;
        
        for (List<Documentation> docs : documentationCache.values()) {
            totalElements += 10; // Placeholder
            documentedElements += docs.size();
            outdatedDocuments += docs.stream()
                    .mapToInt(doc -> doc.isStale(24 * 60 * 60 * 1000) ? 1 : 0)
                    .sum();
            totalAccuracy += docs.stream()
                    .mapToDouble(Documentation::getAccuracyScore)
                    .sum();
            docCount += docs.size();
        }
        
        double coverage = totalElements > 0 ? (double) documentedElements / totalElements * 100 : 0.0;
        double averageAccuracy = docCount > 0 ? totalAccuracy / docCount : 0.0;
        
        return new DocumentationStats(totalElements, documentedElements, outdatedDocuments, 
                coverage, averageAccuracy);
    }
}