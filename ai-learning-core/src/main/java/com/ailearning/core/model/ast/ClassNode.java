package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * AST node representing a class declaration.
 */
public final class ClassNode extends ASTNode {
    
    @JsonCreator
    public ClassNode(
            @JsonProperty("name") String name,
            @JsonProperty("location") SourceLocation location,
            @JsonProperty("children") List<ASTNode> children,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        super("class", name, location, children, attributes);
    }
    
    /**
     * Gets the methods declared in this class.
     * 
     * @return list of method nodes
     */
    public List<MethodNode> getMethods() {
        return getChildren().stream()
                .filter(MethodNode.class::isInstance)
                .map(MethodNode.class::cast)
                .toList();
    }
    
    /**
     * Gets the fields declared in this class.
     * 
     * @return list of variable nodes representing fields
     */
    public List<VariableNode> getFields() {
        return getChildren().stream()
                .filter(VariableNode.class::isInstance)
                .map(VariableNode.class::cast)
                .toList();
    }
    
    /**
     * Gets the superclass name if specified.
     * 
     * @return the superclass name, or null if not specified
     */
    public String getSuperclass() {
        return (String) getAttribute("superclass");
    }
    
    /**
     * Gets the implemented interfaces.
     * 
     * @return list of interface names
     */
    @SuppressWarnings("unchecked")
    public List<String> getInterfaces() {
        Object interfaces = getAttribute("interfaces");
        return interfaces instanceof List ? (List<String>) interfaces : List.of();
    }
    
    /**
     * Gets the access modifiers for this class.
     * 
     * @return list of modifiers (public, abstract, final, etc.)
     */
    @SuppressWarnings("unchecked")
    public List<String> getModifiers() {
        Object modifiers = getAttribute("modifiers");
        return modifiers instanceof List ? (List<String>) modifiers : List.of();
    }
    
    /**
     * Checks if this class is abstract.
     * 
     * @return true if the class is abstract
     */
    public boolean isAbstract() {
        return getModifiers().contains("abstract");
    }
    
    /**
     * Checks if this class is final.
     * 
     * @return true if the class is final
     */
    public boolean isFinal() {
        return getModifiers().contains("final");
    }
    
    /**
     * Checks if this class is public.
     * 
     * @return true if the class is public
     */
    public boolean isPublic() {
        return getModifiers().contains("public");
    }
}