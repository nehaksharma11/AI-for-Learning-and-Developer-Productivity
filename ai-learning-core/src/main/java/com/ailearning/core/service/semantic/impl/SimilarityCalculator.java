package com.ailearning.core.service.semantic.impl;

import com.ailearning.core.model.ast.ASTNode;
import com.ailearning.core.model.ast.ClassNode;
import com.ailearning.core.model.ast.MethodNode;
import com.ailearning.core.model.ast.VariableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates semantic similarity between AST nodes.
 * Uses multiple similarity metrics including structural, lexical, and semantic similarity.
 */
public class SimilarityCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(SimilarityCalculator.class);
    
    // Weights for different similarity components
    private static final double STRUCTURAL_WEIGHT = 0.4;
    private static final double LEXICAL_WEIGHT = 0.3;
    private static final double SEMANTIC_WEIGHT = 0.3;
    
    /**
     * Calculates the overall similarity between two AST nodes.
     */
    public double calculateSimilarity(ASTNode node1, ASTNode node2) {
        logger.debug("Calculating similarity between {} and {}", node1.getType(), node2.getType());
        
        try {
            // If nodes are of different types, similarity is lower
            if (!node1.getType().equals(node2.getType())) {
                return calculateCrossTypeSimilarity(node1, node2);
            }
            
            // Calculate different types of similarity
            double structuralSimilarity = calculateStructuralSimilarity(node1, node2);
            double lexicalSimilarity = calculateLexicalSimilarity(node1, node2);
            double semanticSimilarity = calculateSemanticSimilarity(node1, node2);
            
            // Weighted combination of similarity scores
            double overallSimilarity = (structuralSimilarity * STRUCTURAL_WEIGHT) +
                                     (lexicalSimilarity * LEXICAL_WEIGHT) +
                                     (semanticSimilarity * SEMANTIC_WEIGHT);
            
            logger.debug("Similarity scores - Structural: {}, Lexical: {}, Semantic: {}, Overall: {}",
                    structuralSimilarity, lexicalSimilarity, semanticSimilarity, overallSimilarity);
            
            return Math.min(1.0, Math.max(0.0, overallSimilarity));
            
        } catch (Exception e) {
            logger.error("Failed to calculate similarity", e);
            return 0.0;
        }
    }
    
    /**
     * Calculates structural similarity based on AST structure.
     */
    private double calculateStructuralSimilarity(ASTNode node1, ASTNode node2) {
        // Compare tree structure
        double depthSimilarity = calculateDepthSimilarity(node1, node2);
        double childrenSimilarity = calculateChildrenSimilarity(node1, node2);
        double shapeSimilarity = calculateShapeSimilarity(node1, node2);
        
        return (depthSimilarity + childrenSimilarity + shapeSimilarity) / 3.0;
    }
    
    /**
     * Calculates lexical similarity based on text content.
     */
    private double calculateLexicalSimilarity(ASTNode node1, ASTNode node2) {
        String text1 = extractTextContent(node1);
        String text2 = extractTextContent(node2);
        
        // Use Jaccard similarity for text comparison
        return calculateJaccardSimilarity(text1, text2);
    }
    
    /**
     * Calculates semantic similarity based on meaning and purpose.
     */
    private double calculateSemanticSimilarity(ASTNode node1, ASTNode node2) {
        // For same-type nodes, use type-specific semantic comparison
        if (node1 instanceof ClassNode && node2 instanceof ClassNode) {
            return calculateClassSemanticSimilarity((ClassNode) node1, (ClassNode) node2);
        } else if (node1 instanceof MethodNode && node2 instanceof MethodNode) {
            return calculateMethodSemanticSimilarity((MethodNode) node1, (MethodNode) node2);
        } else if (node1 instanceof VariableNode && node2 instanceof VariableNode) {
            return calculateVariableSemanticSimilarity((VariableNode) node1, (VariableNode) node2);
        }
        
        // Generic semantic similarity for other node types
        return calculateGenericSemanticSimilarity(node1, node2);
    }
    
    /**
     * Calculates similarity between nodes of different types.
     */
    private double calculateCrossTypeSimilarity(ASTNode node1, ASTNode node2) {
        // Cross-type similarity is generally lower, but we can still compare some aspects
        double lexicalSimilarity = calculateLexicalSimilarity(node1, node2);
        double basicStructuralSimilarity = calculateBasicStructuralSimilarity(node1, node2);
        
        // Reduce similarity for cross-type comparisons
        return (lexicalSimilarity + basicStructuralSimilarity) / 2.0 * 0.5;
    }
    
    /**
     * Calculates depth similarity between two nodes.
     */
    private double calculateDepthSimilarity(ASTNode node1, ASTNode node2) {
        int depth1 = calculateDepth(node1);
        int depth2 = calculateDepth(node2);
        
        if (depth1 == 0 && depth2 == 0) return 1.0;
        
        int maxDepth = Math.max(depth1, depth2);
        int depthDifference = Math.abs(depth1 - depth2);
        
        return 1.0 - ((double) depthDifference / maxDepth);
    }
    
    /**
     * Calculates similarity based on number of children.
     */
    private double calculateChildrenSimilarity(ASTNode node1, ASTNode node2) {
        int children1 = node1.getChildren().size();
        int children2 = node2.getChildren().size();
        
        if (children1 == 0 && children2 == 0) return 1.0;
        
        int maxChildren = Math.max(children1, children2);
        int childrenDifference = Math.abs(children1 - children2);
        
        return 1.0 - ((double) childrenDifference / maxChildren);
    }
    
    /**
     * Calculates similarity based on overall tree shape.
     */
    private double calculateShapeSimilarity(ASTNode node1, ASTNode node2) {
        // Compare the distribution of node types in the subtrees
        Map<String, Integer> typeCount1 = getNodeTypeDistribution(node1);
        Map<String, Integer> typeCount2 = getNodeTypeDistribution(node2);
        
        return calculateDistributionSimilarity(typeCount1, typeCount2);
    }
    
    /**
     * Calculates semantic similarity for class nodes.
     */
    private double calculateClassSemanticSimilarity(ClassNode class1, ClassNode class2) {
        double nameSimilarity = calculateNameSimilarity(class1.getName(), class2.getName());
        double methodSimilarity = calculateMethodSignatureSimilarity(class1, class2);
        double fieldSimilarity = calculateFieldSimilarity(class1, class2);
        
        return (nameSimilarity + methodSimilarity + fieldSimilarity) / 3.0;
    }
    
    /**
     * Calculates semantic similarity for method nodes.
     */
    private double calculateMethodSemanticSimilarity(MethodNode method1, MethodNode method2) {
        double nameSimilarity = calculateNameSimilarity(method1.getName(), method2.getName());
        double parameterSimilarity = calculateParameterSimilarity(method1, method2);
        double returnTypeSimilarity = calculateReturnTypeSimilarity(method1, method2);
        
        return (nameSimilarity + parameterSimilarity + returnTypeSimilarity) / 3.0;
    }
    
    /**
     * Calculates semantic similarity for variable nodes.
     */
    private double calculateVariableSemanticSimilarity(VariableNode var1, VariableNode var2) {
        double nameSimilarity = calculateNameSimilarity(var1.getName(), var2.getName());
        double typeSimilarity = calculateTypeSimilarity(var1, var2);
        
        return (nameSimilarity + typeSimilarity) / 2.0;
    }
    
    /**
     * Calculates generic semantic similarity for other node types.
     */
    private double calculateGenericSemanticSimilarity(ASTNode node1, ASTNode node2) {
        // Use string similarity as a fallback
        return calculateJaccardSimilarity(node1.toString(), node2.toString());
    }
    
    /**
     * Calculates basic structural similarity for cross-type comparisons.
     */
    private double calculateBasicStructuralSimilarity(ASTNode node1, ASTNode node2) {
        return calculateChildrenSimilarity(node1, node2);
    }
    
    /**
     * Calculates Jaccard similarity between two strings.
     */
    private double calculateJaccardSimilarity(String text1, String text2) {
        Set<String> tokens1 = tokenize(text1);
        Set<String> tokens2 = tokenize(text2);
        
        if (tokens1.isEmpty() && tokens2.isEmpty()) return 1.0;
        
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);
        
        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Calculates name similarity using edit distance.
     */
    private double calculateNameSimilarity(String name1, String name2) {
        if (name1.equals(name2)) return 1.0;
        
        int editDistance = calculateEditDistance(name1.toLowerCase(), name2.toLowerCase());
        int maxLength = Math.max(name1.length(), name2.length());
        
        if (maxLength == 0) return 1.0;
        
        return 1.0 - ((double) editDistance / maxLength);
    }
    
    /**
     * Calculates method signature similarity.
     */
    private double calculateMethodSignatureSimilarity(ClassNode class1, ClassNode class2) {
        List<String> methods1 = extractMethodSignatures(class1);
        List<String> methods2 = extractMethodSignatures(class2);
        
        return calculateListSimilarity(methods1, methods2);
    }
    
    /**
     * Calculates field similarity between classes.
     */
    private double calculateFieldSimilarity(ClassNode class1, ClassNode class2) {
        List<String> fields1 = extractFieldNames(class1);
        List<String> fields2 = extractFieldNames(class2);
        
        return calculateListSimilarity(fields1, fields2);
    }
    
    /**
     * Calculates parameter similarity between methods.
     */
    private double calculateParameterSimilarity(MethodNode method1, MethodNode method2) {
        List<String> params1 = extractParameterTypes(method1);
        List<String> params2 = extractParameterTypes(method2);
        
        return calculateListSimilarity(params1, params2);
    }
    
    /**
     * Calculates return type similarity between methods.
     */
    private double calculateReturnTypeSimilarity(MethodNode method1, MethodNode method2) {
        String returnType1 = extractReturnType(method1);
        String returnType2 = extractReturnType(method2);
        
        if (returnType1 == null || returnType2 == null) return 0.5;
        
        return returnType1.equals(returnType2) ? 1.0 : 0.0;
    }
    
    /**
     * Calculates type similarity between variables.
     */
    private double calculateTypeSimilarity(VariableNode var1, VariableNode var2) {
        String type1 = extractVariableType(var1);
        String type2 = extractVariableType(var2);
        
        if (type1 == null || type2 == null) return 0.5;
        
        return type1.equals(type2) ? 1.0 : 0.0;
    }
    
    // Helper methods
    
    private int calculateDepth(ASTNode node) {
        if (node.getChildren().isEmpty()) {
            return 1;
        }
        
        return 1 + node.getChildren().stream()
                .mapToInt(this::calculateDepth)
                .max()
                .orElse(0);
    }
    
    private String extractTextContent(ASTNode node) {
        // Extract meaningful text content from the node
        StringBuilder content = new StringBuilder();
        
        if (node instanceof ClassNode classNode) {
            content.append(classNode.getName()).append(" ");
        } else if (node instanceof MethodNode methodNode) {
            content.append(methodNode.getName()).append(" ");
        } else if (node instanceof VariableNode variableNode) {
            content.append(variableNode.getName()).append(" ");
        }
        
        // Add content from children
        for (ASTNode child : node.getChildren()) {
            content.append(extractTextContent(child)).append(" ");
        }
        
        return content.toString().trim();
    }
    
    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toSet());
    }
    
    private Map<String, Integer> getNodeTypeDistribution(ASTNode node) {
        Map<String, Integer> distribution = new HashMap<>();
        collectNodeTypes(node, distribution);
        return distribution;
    }
    
    private void collectNodeTypes(ASTNode node, Map<String, Integer> distribution) {
        distribution.merge(node.getType(), 1, Integer::sum);
        for (ASTNode child : node.getChildren()) {
            collectNodeTypes(child, distribution);
        }
    }
    
    private double calculateDistributionSimilarity(Map<String, Integer> dist1, Map<String, Integer> dist2) {
        Set<String> allTypes = new HashSet<>(dist1.keySet());
        allTypes.addAll(dist2.keySet());
        
        if (allTypes.isEmpty()) return 1.0;
        
        double similarity = 0.0;
        for (String type : allTypes) {
            int count1 = dist1.getOrDefault(type, 0);
            int count2 = dist2.getOrDefault(type, 0);
            int maxCount = Math.max(count1, count2);
            
            if (maxCount > 0) {
                similarity += 1.0 - ((double) Math.abs(count1 - count2) / maxCount);
            }
        }
        
        return similarity / allTypes.size();
    }
    
    private int calculateEditDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    private List<String> extractMethodSignatures(ClassNode classNode) {
        return classNode.getChildren().stream()
                .filter(child -> child instanceof MethodNode)
                .map(child -> ((MethodNode) child).getName())
                .collect(Collectors.toList());
    }
    
    private List<String> extractFieldNames(ClassNode classNode) {
        return classNode.getChildren().stream()
                .filter(child -> child instanceof VariableNode)
                .map(child -> ((VariableNode) child).getName())
                .collect(Collectors.toList());
    }
    
    private List<String> extractParameterTypes(MethodNode methodNode) {
        // Simplified parameter extraction
        String methodString = methodNode.toString();
        List<String> paramTypes = new ArrayList<>();
        
        int parenIndex = methodString.indexOf("(");
        int closeParenIndex = methodString.indexOf(")", parenIndex);
        if (parenIndex != -1 && closeParenIndex != -1) {
            String parameters = methodString.substring(parenIndex + 1, closeParenIndex);
            if (!parameters.trim().isEmpty()) {
                String[] params = parameters.split(",");
                for (String param : params) {
                    String[] parts = param.trim().split("\\s+");
                    if (parts.length >= 2) {
                        paramTypes.add(parts[0]);
                    }
                }
            }
        }
        
        return paramTypes;
    }
    
    private String extractReturnType(MethodNode methodNode) {
        // Simplified return type extraction
        String methodString = methodNode.toString();
        String[] parts = methodString.split("\\s+");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i + 1].equals(methodNode.getName())) {
                return parts[i];
            }
        }
        return null;
    }
    
    private String extractVariableType(VariableNode variableNode) {
        // Simplified variable type extraction
        String variableString = variableNode.toString();
        String[] parts = variableString.split("\\s+");
        if (parts.length >= 2) {
            return parts[0];
        }
        return null;
    }
    
    private double calculateListSimilarity(List<String> list1, List<String> list2) {
        if (list1.isEmpty() && list2.isEmpty()) return 1.0;
        
        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 1.0 : (double) intersection.size() / union.size();
    }
}