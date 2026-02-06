package com.ailearning.core.service.semantic.impl;

import com.ailearning.core.model.ast.ASTNode;
import com.ailearning.core.model.ast.ClassNode;
import com.ailearning.core.model.ast.MethodNode;
import com.ailearning.core.model.ast.VariableNode;
import com.ailearning.core.model.Relationship;
import com.ailearning.core.model.ProjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps code relationships and dependencies within AST structures.
 * Identifies inheritance, composition, aggregation, and usage relationships.
 */
public class RelationshipMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(RelationshipMapper.class);
    
    /**
     * Maps relationships for the given AST node within the project context.
     */
    public List<Relationship> mapRelationships(ASTNode astNode, ProjectContext projectContext) {
        logger.debug("Mapping relationships for AST node: {}", astNode.getType());
        
        List<Relationship> relationships = new ArrayList<>();
        
        try {
            // Map different types of relationships based on node type
            if (astNode instanceof ClassNode classNode) {
                relationships.addAll(mapClassRelationships(classNode, projectContext));
            } else if (astNode instanceof MethodNode methodNode) {
                relationships.addAll(mapMethodRelationships(methodNode, projectContext));
            } else if (astNode instanceof VariableNode variableNode) {
                relationships.addAll(mapVariableRelationships(variableNode, projectContext));
            }
            
            // Map general relationships for all node types
            relationships.addAll(mapGeneralRelationships(astNode, projectContext));
            
        } catch (Exception e) {
            logger.error("Failed to map relationships for AST node", e);
        }
        
        return relationships.stream()
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Maps relationships specific to class nodes.
     */
    private List<Relationship> mapClassRelationships(ClassNode classNode, ProjectContext projectContext) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Map inheritance relationships
        relationships.addAll(mapInheritanceRelationships(classNode));
        
        // Map composition relationships
        relationships.addAll(mapCompositionRelationships(classNode));
        
        // Map interface implementation relationships
        relationships.addAll(mapInterfaceRelationships(classNode));
        
        return relationships;
    }
    
    /**
     * Maps relationships specific to method nodes.
     */
    private List<Relationship> mapMethodRelationships(MethodNode methodNode, ProjectContext projectContext) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Map method call relationships
        relationships.addAll(mapMethodCallRelationships(methodNode));
        
        // Map parameter relationships
        relationships.addAll(mapParameterRelationships(methodNode));
        
        // Map return type relationships
        relationships.addAll(mapReturnTypeRelationships(methodNode));
        
        return relationships;
    }
    
    /**
     * Maps relationships specific to variable nodes.
     */
    private List<Relationship> mapVariableRelationships(VariableNode variableNode, ProjectContext projectContext) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Map variable usage relationships
        relationships.addAll(mapVariableUsageRelationships(variableNode));
        
        // Map type relationships
        relationships.addAll(mapVariableTypeRelationships(variableNode));
        
        return relationships;
    }
    
    /**
     * Maps general relationships that apply to all node types.
     */
    private List<Relationship> mapGeneralRelationships(ASTNode astNode, ProjectContext projectContext) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Map containment relationships (parent-child)
        relationships.addAll(mapContainmentRelationships(astNode));
        
        // Map dependency relationships
        relationships.addAll(mapDependencyRelationships(astNode));
        
        return relationships;
    }
    
    /**
     * Maps inheritance relationships for class nodes.
     */
    private List<Relationship> mapInheritanceRelationships(ClassNode classNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Look for extends keyword in class definition
        String classString = classNode.toString();
        if (classString.contains("extends")) {
            String parentClass = extractParentClass(classString);
            if (parentClass != null) {
                relationships.add(createRelationship(
                        classNode.getName(),
                        parentClass,
                        Relationship.RelationshipType.INHERITANCE,
                        "Class inheritance relationship",
                        1.0
                ));
            }
        }
        
        return relationships;
    }
    
    /**
     * Maps composition relationships for class nodes.
     */
    private List<Relationship> mapCompositionRelationships(ClassNode classNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Look for field declarations that indicate composition
        for (ASTNode child : classNode.getChildren()) {
            if (child instanceof VariableNode variableNode) {
                String variableType = extractVariableType(variableNode);
                if (variableType != null && !isPrimitiveType(variableType)) {
                    relationships.add(createRelationship(
                            classNode.getName(),
                            variableType,
                            Relationship.RelationshipType.COMPOSITION,
                            "Class composition through field: " + variableNode.getName(),
                            0.8
                    ));
                }
            }
        }
        
        return relationships;
    }
    
    /**
     * Maps interface implementation relationships.
     */
    private List<Relationship> mapInterfaceRelationships(ClassNode classNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Look for implements keyword in class definition
        String classString = classNode.toString();
        if (classString.contains("implements")) {
            List<String> interfaces = extractImplementedInterfaces(classString);
            for (String interfaceName : interfaces) {
                relationships.add(createRelationship(
                        classNode.getName(),
                        interfaceName,
                        Relationship.RelationshipType.IMPLEMENTATION,
                        "Interface implementation relationship",
                        1.0
                ));
            }
        }
        
        return relationships;
    }
    
    /**
     * Maps method call relationships.
     */
    private List<Relationship> mapMethodCallRelationships(MethodNode methodNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Analyze method body for method calls
        String methodBody = methodNode.toString();
        Set<String> calledMethods = extractMethodCalls(methodBody);
        
        for (String calledMethod : calledMethods) {
            relationships.add(createRelationship(
                    methodNode.getName(),
                    calledMethod,
                    Relationship.RelationshipType.USAGE,
                    "Method call relationship",
                    0.7
            ));
        }
        
        return relationships;
    }
    
    /**
     * Maps parameter relationships for methods.
     */
    private List<Relationship> mapParameterRelationships(MethodNode methodNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Extract parameter types from method signature
        List<String> parameterTypes = extractParameterTypes(methodNode);
        
        for (String parameterType : parameterTypes) {
            if (!isPrimitiveType(parameterType)) {
                relationships.add(createRelationship(
                        methodNode.getName(),
                        parameterType,
                        Relationship.RelationshipType.DEPENDENCY,
                        "Method parameter dependency",
                        0.6
                ));
            }
        }
        
        return relationships;
    }
    
    /**
     * Maps return type relationships for methods.
     */
    private List<Relationship> mapReturnTypeRelationships(MethodNode methodNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        String returnType = extractReturnType(methodNode);
        if (returnType != null && !isPrimitiveType(returnType) && !returnType.equals("void")) {
            relationships.add(createRelationship(
                    methodNode.getName(),
                    returnType,
                    Relationship.RelationshipType.DEPENDENCY,
                    "Method return type dependency",
                    0.6
            ));
        }
        
        return relationships;
    }
    
    /**
     * Maps variable usage relationships.
     */
    private List<Relationship> mapVariableUsageRelationships(VariableNode variableNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        // This would require more sophisticated analysis of variable usage
        // For now, we create a simple relationship based on variable type
        String variableType = extractVariableType(variableNode);
        if (variableType != null && !isPrimitiveType(variableType)) {
            relationships.add(createRelationship(
                    variableNode.getName(),
                    variableType,
                    Relationship.RelationshipType.USAGE,
                    "Variable type usage",
                    0.5
            ));
        }
        
        return relationships;
    }
    
    /**
     * Maps variable type relationships.
     */
    private List<Relationship> mapVariableTypeRelationships(VariableNode variableNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        String variableType = extractVariableType(variableNode);
        if (variableType != null) {
            relationships.add(createRelationship(
                    variableNode.getName(),
                    variableType,
                    Relationship.RelationshipType.DEPENDENCY,
                    "Variable type dependency",
                    0.8
            ));
        }
        
        return relationships;
    }
    
    /**
     * Maps containment relationships (parent-child).
     */
    private List<Relationship> mapContainmentRelationships(ASTNode astNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        for (ASTNode child : astNode.getChildren()) {
            relationships.add(createRelationship(
                    getNodeIdentifier(astNode),
                    getNodeIdentifier(child),
                    Relationship.RelationshipType.CONTAINMENT,
                    "Parent-child containment relationship",
                    1.0
            ));
        }
        
        return relationships;
    }
    
    /**
     * Maps dependency relationships.
     */
    private List<Relationship> mapDependencyRelationships(ASTNode astNode) {
        List<Relationship> relationships = new ArrayList<>();
        
        // Extract import statements and other dependencies
        String nodeString = astNode.toString();
        Set<String> dependencies = extractDependencies(nodeString);
        
        for (String dependency : dependencies) {
            relationships.add(createRelationship(
                    getNodeIdentifier(astNode),
                    dependency,
                    Relationship.RelationshipType.DEPENDENCY,
                    "Code dependency relationship",
                    0.6
            ));
        }
        
        return relationships;
    }
    
    // Helper methods for extracting information from AST nodes
    
    private String extractParentClass(String classString) {
        // Simple regex-based extraction (would be more sophisticated in real implementation)
        int extendsIndex = classString.indexOf("extends");
        if (extendsIndex != -1) {
            String afterExtends = classString.substring(extendsIndex + 7).trim();
            String[] parts = afterExtends.split("\\s+");
            if (parts.length > 0) {
                return parts[0].replaceAll("[{,]", "").trim();
            }
        }
        return null;
    }
    
    private List<String> extractImplementedInterfaces(String classString) {
        List<String> interfaces = new ArrayList<>();
        int implementsIndex = classString.indexOf("implements");
        if (implementsIndex != -1) {
            String afterImplements = classString.substring(implementsIndex + 10).trim();
            String interfaceList = afterImplements.split("\\{")[0].trim();
            String[] interfaceNames = interfaceList.split(",");
            for (String interfaceName : interfaceNames) {
                interfaces.add(interfaceName.trim());
            }
        }
        return interfaces;
    }
    
    private Set<String> extractMethodCalls(String methodBody) {
        Set<String> methodCalls = new HashSet<>();
        // Simple pattern matching for method calls (would be more sophisticated in real implementation)
        String[] lines = methodBody.split("\n");
        for (String line : lines) {
            if (line.contains("(") && line.contains(")")) {
                // Extract potential method calls
                String[] parts = line.split("\\.");
                if (parts.length > 1) {
                    String lastPart = parts[parts.length - 1];
                    int parenIndex = lastPart.indexOf("(");
                    if (parenIndex != -1) {
                        String methodName = lastPart.substring(0, parenIndex).trim();
                        if (!methodName.isEmpty()) {
                            methodCalls.add(methodName);
                        }
                    }
                }
            }
        }
        return methodCalls;
    }
    
    private List<String> extractParameterTypes(MethodNode methodNode) {
        List<String> parameterTypes = new ArrayList<>();
        // Extract from method signature (simplified)
        String methodString = methodNode.toString();
        int parenIndex = methodString.indexOf("(");
        int closeParenIndex = methodString.indexOf(")", parenIndex);
        if (parenIndex != -1 && closeParenIndex != -1) {
            String parameters = methodString.substring(parenIndex + 1, closeParenIndex);
            if (!parameters.trim().isEmpty()) {
                String[] params = parameters.split(",");
                for (String param : params) {
                    String[] parts = param.trim().split("\\s+");
                    if (parts.length >= 2) {
                        parameterTypes.add(parts[0]);
                    }
                }
            }
        }
        return parameterTypes;
    }
    
    private String extractReturnType(MethodNode methodNode) {
        // Extract return type from method signature (simplified)
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
        // Extract variable type (simplified)
        String variableString = variableNode.toString();
        String[] parts = variableString.split("\\s+");
        if (parts.length >= 2) {
            return parts[0];
        }
        return null;
    }
    
    private Set<String> extractDependencies(String nodeString) {
        Set<String> dependencies = new HashSet<>();
        // Extract import statements and other dependencies (simplified)
        String[] lines = nodeString.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("import")) {
                String importStatement = line.trim();
                String className = importStatement.substring(importStatement.lastIndexOf(".") + 1)
                        .replace(";", "").trim();
                dependencies.add(className);
            }
        }
        return dependencies;
    }
    
    private boolean isPrimitiveType(String type) {
        Set<String> primitiveTypes = Set.of("int", "long", "double", "float", "boolean", "char", "byte", "short", "void");
        return primitiveTypes.contains(type.toLowerCase());
    }
    
    private String getNodeIdentifier(ASTNode node) {
        if (node instanceof ClassNode classNode) {
            return classNode.getName();
        } else if (node instanceof MethodNode methodNode) {
            return methodNode.getName();
        } else if (node instanceof VariableNode variableNode) {
            return variableNode.getName();
        } else {
            return node.getType() + "@" + node.hashCode();
        }
    }
    
    private Relationship createRelationship(String source, String target, Relationship.RelationshipType type, 
                                          String description, double strength) {
        return Relationship.builder()
                .sourceId(source)
                .targetId(target)
                .type(type)
                .description(description)
                .strength(strength)
                .build();
    }
}