package com.ailearning.core.service.semantic.impl;

import com.ailearning.core.model.ast.ASTNode;
import com.ailearning.core.model.ast.ClassNode;
import com.ailearning.core.model.ast.MethodNode;
import com.ailearning.core.model.ast.VariableNode;
import com.ailearning.core.model.CodePattern;
import com.ailearning.core.model.Relationship;
import com.ailearning.core.model.CodingConvention;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.SemanticContext;
import com.ailearning.core.service.SemanticAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Default implementation of semantic code analysis.
 * Provides pattern detection, relationship mapping, and semantic similarity analysis.
 */
public class DefaultSemanticAnalyzer implements SemanticAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultSemanticAnalyzer.class);
    
    private final PatternDetector patternDetector;
    private final RelationshipMapper relationshipMapper;
    private final SimilarityCalculator similarityCalculator;
    private final ConventionLearner conventionLearner;
    
    public DefaultSemanticAnalyzer() {
        this.patternDetector = new PatternDetector();
        this.relationshipMapper = new RelationshipMapper();
        this.similarityCalculator = new SimilarityCalculator();
        this.conventionLearner = new ConventionLearner();
        
        logger.info("Initialized DefaultSemanticAnalyzer with all components");
    }
    
    @Override
    public CompletableFuture<List<Relationship>> analyzeCodeRelationships(ASTNode astNode, ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Analyzing code relationships for AST node: {}", astNode != null ? astNode.getNodeType() : "null");
            
            try {
                if (astNode == null) {
                    return List.of();
                }
                return relationshipMapper.mapRelationships(astNode, projectContext);
            } catch (Exception e) {
                logger.error("Failed to analyze code relationships", e);
                return List.of();
            }
        });
    }
    
    @Override
    public CompletableFuture<List<CodePattern>> detectCodePatterns(ASTNode astNode, String language) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Detecting code patterns for {} language", language);
            
            try {
                if (astNode == null) {
                    return List.of();
                }
                return patternDetector.detectPatterns(astNode, language);
            } catch (Exception e) {
                logger.error("Failed to detect code patterns", e);
                return List.of();
            }
        });
    }
    
    @Override
    public CompletableFuture<Double> calculateSemanticSimilarity(ASTNode node1, ASTNode node2) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Calculating semantic similarity between {} and {}", 
                        node1 != null ? node1.getNodeType() : "null", 
                        node2 != null ? node2.getNodeType() : "null");
            
            try {
                if (node1 == null || node2 == null) {
                    return 0.0;
                }
                return similarityCalculator.calculateSimilarity(node1, node2);
            } catch (Exception e) {
                logger.error("Failed to calculate semantic similarity", e);
                return 0.0;
            }
        });
    }
    
    @Override
    public CompletableFuture<List<CodingConvention>> learnCodingConventions(ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Learning coding conventions from project context");
            
            try {
                return conventionLearner.learnConventions(projectContext);
            } catch (Exception e) {
                logger.error("Failed to learn coding conventions", e);
                return List.of();
            }
        });
    }
    
    @Override
    public CompletableFuture<Map<ASTNode, Double>> findSimilarCode(ASTNode targetNode, ProjectContext projectContext, double threshold) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Finding similar code for target node with threshold: {}", threshold);
            
            try {
                Map<ASTNode, Double> similarNodes = new HashMap<>();
                
                // Extract all AST nodes from project context (simplified approach)
                List<ASTNode> allNodes = extractAllNodes(projectContext);
                
                for (ASTNode node : allNodes) {
                    if (node.equals(targetNode)) continue;
                    
                    double similarity = similarityCalculator.calculateSimilarity(targetNode, node);
                    if (similarity >= threshold) {
                        similarNodes.put(node, similarity);
                    }
                }
                
                return similarNodes.entrySet().stream()
                        .sorted(Map.Entry.<ASTNode, Double>comparingByValue().reversed())
                        .collect(Collectors.toLinkedHashMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ));
                        
            } catch (Exception e) {
                logger.error("Failed to find similar code", e);
                return Map.of();
            }
        });
    }
    
    @Override
    public CompletableFuture<SemanticContext> analyzeSemanticContext(ASTNode astNode, ProjectContext projectContext) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Analyzing semantic context for AST node: {}", astNode != null ? astNode.getNodeType() : "null");
            
            try {
                if (astNode == null) {
                    return SemanticContext.builder()
                            .filePath("unknown")
                            .build();
                }
                return buildSemanticContext(astNode, projectContext);
            } catch (Exception e) {
                logger.error("Failed to analyze semantic context", e);
                return SemanticContext.builder()
                        .filePath("unknown")
                        .build();
            }
        });
    }
    
    /**
     * Builds semantic context information for an AST node.
     */
    private SemanticContext buildSemanticContext(ASTNode astNode, ProjectContext projectContext) {
        SemanticContext.Builder builder = SemanticContext.builder()
                .filePath(astNode.getLocation().getFilePath());
        
        // Extract context information based on node type
        if (astNode instanceof ClassNode classNode) {
            builder.className(classNode.getName())
                    .semanticRole("class_definition");
        } else if (astNode instanceof MethodNode methodNode) {
            builder.functionName(methodNode.getName())
                    .semanticRole("method_definition");
        } else if (astNode instanceof VariableNode variableNode) {
            builder.usedVariables(List.of(variableNode.getName()))
                    .semanticRole("variable_usage");
        }
        
        // Analyze relationships
        List<Relationship> relationships = relationshipMapper.mapRelationships(astNode, projectContext);
        builder.relationships(relationships);
        
        // Detect nearby patterns
        List<CodePattern> patterns = patternDetector.detectPatterns(astNode, "java"); // Default to Java
        builder.nearbyPatterns(patterns);
        
        // Calculate complexity score (simplified)
        double complexity = calculateComplexityScore(astNode);
        builder.complexityScore(complexity);
        
        return builder.build();
    }
    
    /**
     * Extracts all AST nodes from project context (simplified implementation).
     */
    private List<ASTNode> extractAllNodes(ProjectContext projectContext) {
        // This is a simplified implementation
        // In a real implementation, this would traverse the project structure
        // and extract AST nodes from all files
        return List.of();
    }
    
    /**
     * Calculates a complexity score for an AST node.
     */
    private double calculateComplexityScore(ASTNode astNode) {
        // Simplified complexity calculation based on node depth and children count
        int depth = calculateDepth(astNode);
        int childrenCount = astNode.getChildren().size();
        
        return Math.min(10.0, (depth * 0.5) + (childrenCount * 0.3));
    }
    
    /**
     * Calculates the depth of an AST node.
     */
    private int calculateDepth(ASTNode node) {
        if (node.getChildren().isEmpty()) {
            return 1;
        }
        
        return 1 + node.getChildren().stream()
                .mapToInt(this::calculateDepth)
                .max()
                .orElse(0);
    }
}