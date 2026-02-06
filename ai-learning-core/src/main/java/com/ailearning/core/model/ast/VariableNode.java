package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * AST node representing a variable declaration or field.
 */
public final class VariableNode extends ASTNode {
    
    @JsonCreator
    public VariableNode(
            @JsonProperty("name") String name,
            @JsonProperty("location") SourceLocation location,
            @JsonProperty("children") List<ASTNode> children,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        super("variable", name, location, children, attributes);
    }
    
    /**
     * Gets the type of this variable.
     * 
     * @return the variable type
     */
    public String getType() {
        return (String) getAttribute("type");
    }
    
    /**
     * Gets the access modifiers for this variable.
     * 
     * @return list of modifiers (public, private, static, final, etc.)
     */
    @SuppressWarnings("unchecked")
    public List<String> getModifiers() {
        Object modifiers = getAttribute("modifiers");
        return modifiers instanceof List ? (List<String>) modifiers : List.of();
    }
    
    /**
     * Gets the initial value expression if present.
     * 
     * @return the initializer expression node, or null if not present
     */
    public ExpressionNode getInitializer() {
        return getChildren().stream()
                .filter(ExpressionNode.class::isInstance)
                .map(ExpressionNode.class::cast)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Checks if this variable has an initializer.
     * 
     * @return true if the variable is initialized
     */
    public boolean hasInitializer() {
        return getInitializer() != null;
    }
    
    /**
     * Checks if this variable is static.
     * 
     * @return true if the variable is static
     */
    public boolean isStatic() {
        return getModifiers().contains("static");
    }
    
    /**
     * Checks if this variable is final.
     * 
     * @return true if the variable is final
     */
    public boolean isFinal() {
        return getModifiers().contains("final");
    }
    
    /**
     * Checks if this variable is public.
     * 
     * @return true if the variable is public
     */
    public boolean isPublic() {
        return getModifiers().contains("public");
    }
    
    /**
     * Checks if this variable is private.
     * 
     * @return true if the variable is private
     */
    public boolean isPrivate() {
        return getModifiers().contains("private");
    }
    
    /**
     * Checks if this is a field (class-level variable).
     * 
     * @return true if this is a field
     */
    public boolean isField() {
        return Boolean.TRUE.equals(getAttribute("field"));
    }
    
    /**
     * Checks if this is a local variable.
     * 
     * @return true if this is a local variable
     */
    public boolean isLocal() {
        return !isField();
    }
    
    /**
     * Checks if this is a parameter.
     * 
     * @return true if this is a method parameter
     */
    public boolean isParameter() {
        return Boolean.TRUE.equals(getAttribute("parameter"));
    }
}