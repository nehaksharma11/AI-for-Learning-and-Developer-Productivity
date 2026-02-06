package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * AST node representing a method declaration.
 */
public final class MethodNode extends ASTNode {
    
    @JsonCreator
    public MethodNode(
            @JsonProperty("name") String name,
            @JsonProperty("location") SourceLocation location,
            @JsonProperty("children") List<ASTNode> children,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        super("method", name, location, children, attributes);
    }
    
    /**
     * Gets the return type of this method.
     * 
     * @return the return type, or "void" if not specified
     */
    public String getReturnType() {
        return (String) getAttribute("returnType");
    }
    
    /**
     * Gets the parameter list for this method.
     * 
     * @return list of parameter information
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getParameters() {
        Object params = getAttribute("parameters");
        return params instanceof List ? (List<Map<String, String>>) params : List.of();
    }
    
    /**
     * Gets the access modifiers for this method.
     * 
     * @return list of modifiers (public, private, static, etc.)
     */
    @SuppressWarnings("unchecked")
    public List<String> getModifiers() {
        Object modifiers = getAttribute("modifiers");
        return modifiers instanceof List ? (List<String>) modifiers : List.of();
    }
    
    /**
     * Gets the thrown exceptions for this method.
     * 
     * @return list of exception types
     */
    @SuppressWarnings("unchecked")
    public List<String> getThrows() {
        Object throwsClause = getAttribute("throws");
        return throwsClause instanceof List ? (List<String>) throwsClause : List.of();
    }
    
    /**
     * Gets the statements in the method body.
     * 
     * @return list of statement nodes
     */
    public List<StatementNode> getStatements() {
        return getChildren().stream()
                .filter(StatementNode.class::isInstance)
                .map(StatementNode.class::cast)
                .toList();
    }
    
    /**
     * Checks if this method is static.
     * 
     * @return true if the method is static
     */
    public boolean isStatic() {
        return getModifiers().contains("static");
    }
    
    /**
     * Checks if this method is abstract.
     * 
     * @return true if the method is abstract
     */
    public boolean isAbstract() {
        return getModifiers().contains("abstract");
    }
    
    /**
     * Checks if this method is public.
     * 
     * @return true if the method is public
     */
    public boolean isPublic() {
        return getModifiers().contains("public");
    }
    
    /**
     * Checks if this method is private.
     * 
     * @return true if the method is private
     */
    public boolean isPrivate() {
        return getModifiers().contains("private");
    }
    
    /**
     * Checks if this method is a constructor.
     * 
     * @return true if this is a constructor
     */
    public boolean isConstructor() {
        return Boolean.TRUE.equals(getAttribute("constructor"));
    }
    
    /**
     * Gets the cyclomatic complexity of this method.
     * 
     * @return the complexity score, or 1 if not calculated
     */
    public int getCyclomaticComplexity() {
        Object complexity = getAttribute("cyclomaticComplexity");
        return complexity instanceof Integer ? (Integer) complexity : 1;
    }
}