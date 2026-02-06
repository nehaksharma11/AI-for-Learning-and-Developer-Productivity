package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * AST node representing an expression.
 */
public final class ExpressionNode extends ASTNode {
    
    @JsonCreator
    public ExpressionNode(
            @JsonProperty("name") String name,
            @JsonProperty("location") SourceLocation location,
            @JsonProperty("children") List<ASTNode> children,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        super("expression", name, location, children, attributes);
    }
    
    /**
     * Gets the expression type (binary, unary, call, literal, etc.).
     * 
     * @return the expression type
     */
    public String getExpressionType() {
        return (String) getAttribute("expressionType");
    }
    
    /**
     * Gets the operator for binary/unary expressions.
     * 
     * @return the operator, or null if not applicable
     */
    public String getOperator() {
        return (String) getAttribute("operator");
    }
    
    /**
     * Gets the literal value for literal expressions.
     * 
     * @return the literal value, or null if not a literal
     */
    public Object getLiteralValue() {
        return getAttribute("literalValue");
    }
    
    /**
     * Gets the data type of this expression.
     * 
     * @return the data type, or null if not determined
     */
    public String getDataType() {
        return (String) getAttribute("dataType");
    }
    
    /**
     * Checks if this is a literal expression.
     * 
     * @return true if this is a literal
     */
    public boolean isLiteral() {
        return "literal".equals(getExpressionType());
    }
    
    /**
     * Checks if this is a binary expression.
     * 
     * @return true if this is a binary expression
     */
    public boolean isBinary() {
        return "binary".equals(getExpressionType());
    }
    
    /**
     * Checks if this is a unary expression.
     * 
     * @return true if this is a unary expression
     */
    public boolean isUnary() {
        return "unary".equals(getExpressionType());
    }
    
    /**
     * Checks if this is a method call expression.
     * 
     * @return true if this is a method call
     */
    public boolean isMethodCall() {
        return "call".equals(getExpressionType());
    }
    
    /**
     * Checks if this is a variable reference.
     * 
     * @return true if this is a variable reference
     */
    public boolean isVariableReference() {
        return "variable".equals(getExpressionType());
    }
    
    /**
     * Gets the left operand for binary expressions.
     * 
     * @return the left operand expression, or null if not binary
     */
    public ExpressionNode getLeftOperand() {
        if (!isBinary() || getChildren().isEmpty()) {
            return null;
        }
        return getChildren().get(0) instanceof ExpressionNode ? 
               (ExpressionNode) getChildren().get(0) : null;
    }
    
    /**
     * Gets the right operand for binary expressions.
     * 
     * @return the right operand expression, or null if not binary
     */
    public ExpressionNode getRightOperand() {
        if (!isBinary() || getChildren().size() < 2) {
            return null;
        }
        return getChildren().get(1) instanceof ExpressionNode ? 
               (ExpressionNode) getChildren().get(1) : null;
    }
    
    /**
     * Gets the operand for unary expressions.
     * 
     * @return the operand expression, or null if not unary
     */
    public ExpressionNode getOperand() {
        if (!isUnary() || getChildren().isEmpty()) {
            return null;
        }
        return getChildren().get(0) instanceof ExpressionNode ? 
               (ExpressionNode) getChildren().get(0) : null;
    }
}