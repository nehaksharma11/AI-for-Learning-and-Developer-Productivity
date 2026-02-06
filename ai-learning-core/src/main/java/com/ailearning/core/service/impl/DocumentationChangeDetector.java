package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.model.ast.*;
import com.ailearning.core.service.ast.ASTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects changes in code that require documentation updates.
 * Provides intelligent change detection and impact analysis.
 */
public class DocumentationChangeDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentationChangeDetector.class);
    
    private final ASTParser astParser;
    
    public DocumentationChangeDetector(ASTParser astParser) {
        this.astParser = astParser;
    }
    
    /**
     * Detects changes between old and new code that affect documentation.
     */
    public List<DocumentationChangeEvent> detectChanges(String filePath, String oldCode, 
                                                       String newCode, String language) {
        List<DocumentationChangeEvent> changes = new ArrayList<>();
        
        try {
            logger.debug("Detecting documentation changes for file: {}", filePath);
            
            // Parse both versions
            ParseResult oldParseResult = astParser.parse(oldCode, language);
            ParseResult newParseResult = astParser.parse(newCode, language);
            
            if (!oldParseResult.isSuccess() || !newParseResult.isSuccess()) {
                logger.warn("Failed to parse code for change detection: {}", filePath);
                return changes;
            }
            
            // Extract elements from both versions
            Map<String, CodeElement> oldElements = extractCodeElements(oldParseResult.getRootNode());
            Map<String, CodeElement> newElements = extractCodeElements(newParseResult.getRootNode());
            
            // Detect added elements
            for (Map.Entry<String, CodeElement> entry : newElements.entrySet()) {
                String elementKey = entry.getKey();
                CodeElement newElement = entry.getValue();
                
                if (!oldElements.containsKey(elementKey)) {
                    changes.add(DocumentationChangeEvent.elementAdded(
                            filePath, newElement, "New element requires documentation"));
                }
            }
            
            // Detect deleted elements
            for (Map.Entry<String, CodeElement> entry : oldElements.entrySet()) {
                String elementKey = entry.getKey();
                CodeElement oldElement = entry.getValue();
                
                if (!newElements.containsKey(elementKey)) {
                    changes.add(DocumentationChangeEvent.elementDeleted(
                            filePath, oldElement, "Element removed, documentation should be updated"));
                }
            }
            
            // Detect modified elements
            for (Map.Entry<String, CodeElement> entry : newElements.entrySet()) {
                String elementKey = entry.getKey();
                CodeElement newElement = entry.getValue();
                CodeElement oldElement = oldElements.get(elementKey);
                
                if (oldElement != null && hasSignificantChanges(oldElement, newElement)) {
                    changes.add(DocumentationChangeEvent.elementModified(
                            filePath, oldElement, newElement, 
                            "Element modified, documentation may need updates"));
                }
            }
            
            // Detect signature changes that require parameter documentation updates
            changes.addAll(detectSignatureChanges(oldElements, newElements, filePath));
            
            logger.debug("Detected {} documentation change events for file: {}", changes.size(), filePath);
            return changes;
            
        } catch (Exception e) {
            logger.error("Error detecting documentation changes", e);
            return changes;
        }
    }
    
    /**
     * Analyzes the impact of code changes on existing documentation.
     */
    public DocumentationImpactAnalysis analyzeImpact(List<DocumentationChangeEvent> changes, 
                                                    List<Documentation> existingDocs) {
        try {
            logger.debug("Analyzing documentation impact for {} changes", changes.size());
            
            List<Documentation> affectedDocs = new ArrayList<>();
            List<String> requiredUpdates = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();
            int criticalChanges = 0;
            
            for (DocumentationChangeEvent change : changes) {
                // Find affected documentation
                List<Documentation> affected = findAffectedDocumentation(change, existingDocs);
                affectedDocs.addAll(affected);
                
                // Analyze change severity
                if (change.getSeverity() == DocumentationChangeEvent.Severity.CRITICAL) {
                    criticalChanges++;
                    requiredUpdates.add("Critical: " + change.getDescription());
                } else if (change.getSeverity() == DocumentationChangeEvent.Severity.HIGH) {
                    requiredUpdates.add("High: " + change.getDescription());
                }
                
                // Generate recommendations
                recommendations.addAll(generateRecommendations(change));
            }
            
            // Remove duplicates
            affectedDocs = affectedDocs.stream().distinct().collect(Collectors.toList());
            
            return new DocumentationImpactAnalysis(
                    changes.size(),
                    affectedDocs.size(),
                    criticalChanges,
                    affectedDocs,
                    requiredUpdates,
                    recommendations
            );
            
        } catch (Exception e) {
            logger.error("Error analyzing documentation impact", e);
            return new DocumentationImpactAnalysis(0, 0, 0, List.of(), List.of(), List.of());
        }
    }
    
    /**
     * Determines if documentation needs to be updated based on change events.
     */
    public boolean requiresDocumentationUpdate(List<DocumentationChangeEvent> changes) {
        return changes.stream().anyMatch(change -> 
                change.getSeverity() == DocumentationChangeEvent.Severity.CRITICAL ||
                change.getSeverity() == DocumentationChangeEvent.Severity.HIGH);
    }
    
    /**
     * Calculates a priority score for documentation updates.
     */
    public double calculateUpdatePriority(List<DocumentationChangeEvent> changes) {
        double priority = 0.0;
        
        for (DocumentationChangeEvent change : changes) {
            switch (change.getSeverity()) {
                case CRITICAL -> priority += 1.0;
                case HIGH -> priority += 0.7;
                case MEDIUM -> priority += 0.4;
                case LOW -> priority += 0.1;
            }
        }
        
        return Math.min(1.0, priority / changes.size());
    }
    
    // Helper methods
    
    private Map<String, CodeElement> extractCodeElements(ASTNode rootNode) {
        Map<String, CodeElement> elements = new HashMap<>();
        
        if (rootNode != null) {
            extractElementsRecursively(rootNode, elements);
        }
        
        return elements;
    }
    
    private void extractElementsRecursively(ASTNode node, Map<String, CodeElement> elements) {
        if (isDocumentableElement(node)) {
            CodeElement element = createCodeElement(node);
            String key = generateElementKey(element);
            elements.put(key, element);
        }
        
        for (ASTNode child : node.getChildren()) {
            extractElementsRecursively(child, elements);
        }
    }
    
    private boolean isDocumentableElement(ASTNode node) {
        return node.getNodeType() == ASTNode.NodeType.CLASS ||
               node.getNodeType() == ASTNode.NodeType.METHOD ||
               node.getNodeType() == ASTNode.NodeType.FUNCTION ||
               (node.getNodeType() == ASTNode.NodeType.VARIABLE && isPublicVariable(node));
    }
    
    private boolean isPublicVariable(ASTNode node) {
        String text = node.getText();
        return text != null && (text.contains("public") || text.contains("export"));
    }
    
    private CodeElement createCodeElement(ASTNode node) {
        return new CodeElement(
                extractElementName(node),
                node.getNodeType().toString().toLowerCase(),
                node.getText(),
                node.getLocation(),
                extractSignature(node),
                extractParameters(node),
                extractReturnType(node)
        );
    }
    
    private String generateElementKey(CodeElement element) {
        return element.getType() + ":" + element.getName() + ":" + element.getSignature();
    }
    
    private String extractElementName(ASTNode node) {
        if (node instanceof ClassNode classNode) {
            return classNode.getName();
        } else if (node instanceof MethodNode methodNode) {
            return methodNode.getName();
        } else if (node instanceof VariableNode variableNode) {
            return variableNode.getName();
        }
        
        // Fallback extraction
        String text = node.getText();
        if (text != null) {
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (word.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    return word;
                }
            }
        }
        
        return "unknown";
    }
    
    private String extractSignature(ASTNode node) {
        String text = node.getText();
        if (text == null) return "";
        
        // Extract method/function signature
        if (text.contains("(") && text.contains(")")) {
            int start = text.indexOf("(");
            int end = text.indexOf(")", start);
            if (end > start) {
                return text.substring(start, end + 1);
            }
        }
        
        return "";
    }
    
    private List<String> extractParameters(ASTNode node) {
        List<String> parameters = new ArrayList<>();
        String signature = extractSignature(node);
        
        if (!signature.isEmpty()) {
            String paramString = signature.substring(1, signature.length() - 1).trim();
            if (!paramString.isEmpty()) {
                String[] params = paramString.split(",");
                for (String param : params) {
                    String paramName = param.trim().split("\\s+")[0];
                    if (!paramName.isEmpty()) {
                        parameters.add(paramName);
                    }
                }
            }
        }
        
        return parameters;
    }
    
    private String extractReturnType(ASTNode node) {
        String text = node.getText();
        if (text == null) return "void";
        
        // Simple return type extraction - would be more sophisticated in real implementation
        if (text.contains("return ")) {
            return "unknown";
        }
        
        return "void";
    }
    
    private boolean hasSignificantChanges(CodeElement oldElement, CodeElement newElement) {
        // Check signature changes
        if (!Objects.equals(oldElement.getSignature(), newElement.getSignature())) {
            return true;
        }
        
        // Check parameter changes
        if (!Objects.equals(oldElement.getParameters(), newElement.getParameters())) {
            return true;
        }
        
        // Check return type changes
        if (!Objects.equals(oldElement.getReturnType(), newElement.getReturnType())) {
            return true;
        }
        
        // Check significant content changes (simplified)
        String oldContent = oldElement.getContent();
        String newContent = newElement.getContent();
        
        if (oldContent != null && newContent != null) {
            double similarity = calculateSimilarity(oldContent, newContent);
            return similarity < 0.8; // Less than 80% similar
        }
        
        return false;
    }
    
    private double calculateSimilarity(String text1, String text2) {
        // Simple similarity calculation - in practice would use more sophisticated algorithms
        if (text1.equals(text2)) return 1.0;
        
        int maxLength = Math.max(text1.length(), text2.length());
        if (maxLength == 0) return 1.0;
        
        int commonChars = 0;
        int minLength = Math.min(text1.length(), text2.length());
        
        for (int i = 0; i < minLength; i++) {
            if (text1.charAt(i) == text2.charAt(i)) {
                commonChars++;
            }
        }
        
        return (double) commonChars / maxLength;
    }
    
    private List<DocumentationChangeEvent> detectSignatureChanges(Map<String, CodeElement> oldElements,
                                                                Map<String, CodeElement> newElements,
                                                                String filePath) {
        List<DocumentationChangeEvent> changes = new ArrayList<>();
        
        for (Map.Entry<String, CodeElement> entry : newElements.entrySet()) {
            String elementKey = entry.getKey();
            CodeElement newElement = entry.getValue();
            CodeElement oldElement = oldElements.get(elementKey);
            
            if (oldElement != null) {
                // Check parameter changes
                if (!Objects.equals(oldElement.getParameters(), newElement.getParameters())) {
                    changes.add(DocumentationChangeEvent.signatureChanged(
                            filePath, oldElement, newElement,
                            "Method parameters changed, update @param documentation"));
                }
                
                // Check return type changes
                if (!Objects.equals(oldElement.getReturnType(), newElement.getReturnType())) {
                    changes.add(DocumentationChangeEvent.signatureChanged(
                            filePath, oldElement, newElement,
                            "Return type changed, update @return documentation"));
                }
            }
        }
        
        return changes;
    }
    
    private List<Documentation> findAffectedDocumentation(DocumentationChangeEvent change, 
                                                         List<Documentation> existingDocs) {
        return existingDocs.stream()
                .filter(doc -> isDocumentationAffected(doc, change))
                .collect(Collectors.toList());
    }
    
    private boolean isDocumentationAffected(Documentation doc, DocumentationChangeEvent change) {
        // Check if documentation is for the same file
        if (!Objects.equals(doc.getFilePath(), change.getFilePath())) {
            return false;
        }
        
        // Check if documentation is for the same element
        if (change.getOldElement() != null) {
            return Objects.equals(doc.getElementName(), change.getOldElement().getName()) &&
                   Objects.equals(doc.getElementType(), change.getOldElement().getType());
        }
        
        if (change.getNewElement() != null) {
            return Objects.equals(doc.getElementName(), change.getNewElement().getName()) &&
                   Objects.equals(doc.getElementType(), change.getNewElement().getType());
        }
        
        return false;
    }
    
    private List<String> generateRecommendations(DocumentationChangeEvent change) {
        List<String> recommendations = new ArrayList<>();
        
        switch (change.getChangeType()) {
            case ELEMENT_ADDED -> {
                recommendations.add("Generate initial documentation for new " + 
                                  change.getNewElement().getType() + " '" + 
                                  change.getNewElement().getName() + "'");
            }
            case ELEMENT_DELETED -> {
                recommendations.add("Remove documentation for deleted " + 
                                  change.getOldElement().getType() + " '" + 
                                  change.getOldElement().getName() + "'");
            }
            case ELEMENT_MODIFIED -> {
                recommendations.add("Review and update documentation for modified " + 
                                  change.getNewElement().getType() + " '" + 
                                  change.getNewElement().getName() + "'");
            }
            case SIGNATURE_CHANGED -> {
                recommendations.add("Update parameter and return type documentation for " + 
                                  change.getNewElement().getName());
            }
        }
        
        return recommendations;
    }
    
    /**
     * Represents a code element for change detection.
     */
    public static class CodeElement {
        private final String name;
        private final String type;
        private final String content;
        private final SourceLocation location;
        private final String signature;
        private final List<String> parameters;
        private final String returnType;
        
        public CodeElement(String name, String type, String content, SourceLocation location,
                          String signature, List<String> parameters, String returnType) {
            this.name = name;
            this.type = type;
            this.content = content;
            this.location = location;
            this.signature = signature;
            this.parameters = parameters != null ? List.copyOf(parameters) : List.of();
            this.returnType = returnType;
        }
        
        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public String getContent() { return content; }
        public SourceLocation getLocation() { return location; }
        public String getSignature() { return signature; }
        public List<String> getParameters() { return parameters; }
        public String getReturnType() { return returnType; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CodeElement that = (CodeElement) o;
            return Objects.equals(name, that.name) &&
                   Objects.equals(type, that.type) &&
                   Objects.equals(signature, that.signature);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, type, signature);
        }
        
        @Override
        public String toString() {
            return String.format("CodeElement{name='%s', type='%s', signature='%s'}", 
                    name, type, signature);
        }
    }
    
    /**
     * Represents the impact analysis of documentation changes.
     */
    public static class DocumentationImpactAnalysis {
        private final int totalChanges;
        private final int affectedDocuments;
        private final int criticalChanges;
        private final List<Documentation> affectedDocs;
        private final List<String> requiredUpdates;
        private final List<String> recommendations;
        
        public DocumentationImpactAnalysis(int totalChanges, int affectedDocuments, int criticalChanges,
                                         List<Documentation> affectedDocs, List<String> requiredUpdates,
                                         List<String> recommendations) {
            this.totalChanges = totalChanges;
            this.affectedDocuments = affectedDocuments;
            this.criticalChanges = criticalChanges;
            this.affectedDocs = affectedDocs != null ? List.copyOf(affectedDocs) : List.of();
            this.requiredUpdates = requiredUpdates != null ? List.copyOf(requiredUpdates) : List.of();
            this.recommendations = recommendations != null ? List.copyOf(recommendations) : List.of();
        }
        
        public boolean hasSignificantImpact() {
            return criticalChanges > 0 || affectedDocuments > 2;
        }
        
        public double getImpactScore() {
            if (totalChanges == 0) return 0.0;
            return (double) (criticalChanges * 2 + affectedDocuments) / (totalChanges * 2);
        }
        
        // Getters
        public int getTotalChanges() { return totalChanges; }
        public int getAffectedDocuments() { return affectedDocuments; }
        public int getCriticalChanges() { return criticalChanges; }
        public List<Documentation> getAffectedDocs() { return affectedDocs; }
        public List<String> getRequiredUpdates() { return requiredUpdates; }
        public List<String> getRecommendations() { return recommendations; }
        
        @Override
        public String toString() {
            return String.format("DocumentationImpactAnalysis{changes=%d, affected=%d, critical=%d, impact=%.2f}", 
                    totalChanges, affectedDocuments, criticalChanges, getImpactScore());
        }
    }
}