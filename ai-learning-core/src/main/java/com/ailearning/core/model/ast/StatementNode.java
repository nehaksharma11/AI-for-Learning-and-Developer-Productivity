package com.ailearning.core.model.ast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * AST node representing a statement.
 */
public final class StatementNode extends ASTNode {
    
    @JsonCreator
    public StatementNode(
            @JsonProperty("name") String name,
            @JsonProperty("location") SourceLocation location,
            @JsonProperty("children") List<ASTNode> children,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        super("statement", name, location, children, attributes);
    }
    
    /**
     * Gets the statement type (if, for, while, return, etc.).
     * 
     * @return the statement type
     */
    public String getStatementType() {
        return (String) getAttribute("statementType");
    }
    
    /**
     * Gets the condition expression for conditional statements.
     * 
     * @return the condition expression, or null if not applicable
     */
    public ExpressionNode getCondition() {
        return getChildren().stream()
                .filter(ExpressionNode.class::isInstance)
                .map(ExpressionNode.class::cast)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Gets the body statements for block statements.
     * 
     * @return list of body statements
     */
    public List<StatementNode> getBodyStatements() {
        return getChildren().stream()
                .filter(StatementNode.class::isInstance)
                .map(StatementNode.class::cast)
                .toList();
    }
    
    /**
     * Checks if this is a control flow statement.
     * 
     * @return true if this is a control flow statement
     */
    public boolean isControlFlow() {
        String type = getStatementType();
        return type != null && (type.equals("if") || type.equals("for") || 
                               type.equals("while") || type.equals("switch") ||
                               type.equals("try") || type.equals("return") ||
                               type.equals("break") || type.equals("continue"));
    }
    
    /**
     * Checks if this is a loop statement.
     * 
     * @return true if this is a loop statement
     */
    public boolean isLoop() {
        String type = getStatementType();
        return type != null && (type.equals("for") || type.equals("while") || type.equals("do"));
    }
    
    /**
     * Checks if this is a conditional statement.
     * 
     * @return true if this is a conditional statement
     */
    public boolean isConditional() {
        String type = getStatementType();
        return type != null && (type.equals("if") || type.equals("switch"));
    }
    
    /**
     * Checks if this is a return statement.
     * 
     * @return true if this is a return statement
     */
    public boolean isReturn() {
        return "return".equals(getStatementType());
    }
    
    /**
     * Checks if this is a throw statement.
     * 
     * @return true if this is a throw statement
     */
    public boolean isThrow() {
        return "throw".equals(getStatementType());
    }
    
    /**
     * Checks if this is an expression statement.
     * 
     * @return true if this is an expression statement
     */
    public boolean isExpressionStatement() {
        return "expression".equals(getStatementType());
    }
}