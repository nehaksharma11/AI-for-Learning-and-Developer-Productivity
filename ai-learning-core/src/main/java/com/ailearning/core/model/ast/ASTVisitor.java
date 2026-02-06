package com.ailearning.core.model.ast;

/**
 * Functional interface for visiting AST nodes during traversal.
 * Used with the visitor pattern for AST processing.
 */
@FunctionalInterface
public interface ASTVisitor {
    
    /**
     * Visits an AST node during traversal.
     * 
     * @param node the AST node being visited
     */
    void visit(ASTNode node);
}