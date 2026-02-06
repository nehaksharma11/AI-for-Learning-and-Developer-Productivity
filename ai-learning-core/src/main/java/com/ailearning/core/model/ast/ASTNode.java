package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract base class for Abstract Syntax Tree nodes.
 * Provides a language-agnostic representation of code structure.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "nodeType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ClassNode.class, name = "class"),
    @JsonSubTypes.Type(value = MethodNode.class, name = "method"),
    @JsonSubTypes.Type(value = VariableNode.class, name = "variable"),
    @JsonSubTypes.Type(value = ExpressionNode.class, name = "expression"),
    @JsonSubTypes.Type(value = StatementNode.class, name = "statement")
})
public abstract class ASTNode {
    
    private final String nodeType;
    private final String name;
    private final SourceLocation location;
    private final List<ASTNode> children;
    private final Map<String, Object> attributes;
    
    @JsonCreator
    protected ASTNode(
            @JsonProperty("nodeType") String nodeType,
            @JsonProperty("name") String name,
            @JsonProperty("location") SourceLocation location,
            @JsonProperty("children") List<ASTNode> children,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        this.nodeType = Objects.requireNonNull(nodeType, "Node type cannot be null");
        this.name = name;
        this.location = Objects.requireNonNull(location, "Location cannot be null");
        this.children = children != null ? List.copyOf(children) : List.of();
        this.attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
    }
    
    /**
     * Gets the type of this AST node.
     * 
     * @return the node type (class, method, variable, etc.)
     */
    public String getNodeType() {
        return nodeType;
    }
    
    /**
     * Gets the name of this node (if applicable).
     * 
     * @return the node name, or null if not applicable
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the source location of this node.
     * 
     * @return the source location
     */
    public SourceLocation getLocation() {
        return location;
    }
    
    /**
     * Gets the child nodes of this AST node.
     * 
     * @return immutable list of child nodes
     */
    public List<ASTNode> getChildren() {
        return children;
    }
    
    /**
     * Gets additional attributes for this node.
     * 
     * @return immutable map of attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    /**
     * Gets a specific attribute value.
     * 
     * @param key the attribute key
     * @return the attribute value, or null if not found
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * Checks if this node has a specific attribute.
     * 
     * @param key the attribute key
     * @return true if the attribute exists
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
    
    /**
     * Finds all child nodes of a specific type.
     * 
     * @param nodeType the node type to search for
     * @return list of matching child nodes
     */
    public List<ASTNode> findChildrenByType(String nodeType) {
        return children.stream()
                .filter(child -> nodeType.equals(child.getNodeType()))
                .toList();
    }
    
    /**
     * Finds the first child node with a specific name.
     * 
     * @param name the node name to search for
     * @return the matching node, or null if not found
     */
    public ASTNode findChildByName(String name) {
        return children.stream()
                .filter(child -> name.equals(child.getName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Performs a depth-first traversal of the AST.
     * 
     * @param visitor the visitor function to apply to each node
     */
    public void traverse(ASTVisitor visitor) {
        visitor.visit(this);
        for (ASTNode child : children) {
            child.traverse(visitor);
        }
    }
    
    /**
     * Gets the depth of this node in the AST.
     * 
     * @return the maximum depth of child nodes + 1
     */
    public int getDepth() {
        if (children.isEmpty()) {
            return 1;
        }
        return 1 + children.stream()
                .mapToInt(ASTNode::getDepth)
                .max()
                .orElse(0);
    }
    
    /**
     * Gets the total number of nodes in this subtree.
     * 
     * @return the count of this node plus all descendants
     */
    public int getNodeCount() {
        return 1 + children.stream()
                .mapToInt(ASTNode::getNodeCount)
                .sum();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ASTNode astNode = (ASTNode) obj;
        return Objects.equals(nodeType, astNode.nodeType) &&
               Objects.equals(name, astNode.name) &&
               Objects.equals(location, astNode.location) &&
               Objects.equals(children, astNode.children) &&
               Objects.equals(attributes, astNode.attributes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nodeType, name, location, children, attributes);
    }
    
    @Override
    public String toString() {
        return String.format("%s{name='%s', location=%s, children=%d}", 
                getClass().getSimpleName(), name, location, children.size());
    }
}