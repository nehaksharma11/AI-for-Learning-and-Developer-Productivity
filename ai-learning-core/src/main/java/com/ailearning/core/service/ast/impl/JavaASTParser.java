package com.ailearning.core.service.ast.impl;

import com.ailearning.core.model.ast.*;
import com.ailearning.core.service.ast.ASTParser;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Java-specific AST parser using JavaParser library.
 * Converts Java source code into language-agnostic AST representation.
 */
public class JavaASTParser implements ASTParser {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaASTParser.class);
    
    private final JavaParser javaParser;
    
    public JavaASTParser() {
        this.javaParser = new JavaParser();
        logger.debug("Initialized Java AST parser");
    }
    
    @Override
    public CompletableFuture<ParseResult> parseCode(String sourceCode, String language, String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                com.github.javaparser.ParseResult<CompilationUnit> parseResult = 
                    javaParser.parse(sourceCode);
                
                long parseTime = System.currentTimeMillis() - startTime;
                
                if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                    CompilationUnit cu = parseResult.getResult().get();
                    ASTNode rootNode = convertToGenericAST(cu, filePath);
                    
                    List<ParseWarning> warnings = parseResult.getProblems().stream()
                        .map(problem -> ParseWarning.of(
                            problem.getMessage(),
                            SourceLocation.at(filePath, 
                                problem.getLocation().isPresent() ? 
                                    problem.getLocation().get().begin.line : 1,
                                problem.getLocation().isPresent() ? 
                                    problem.getLocation().get().begin.column : 1)
                        ))
                        .toList();
                    
                    if (warnings.isEmpty()) {
                        return ParseResult.success(rootNode, language, filePath, parseTime);
                    } else {
                        return ParseResult.successWithWarnings(rootNode, language, filePath, warnings, parseTime);
                    }
                } else {
                    List<ParseError> errors = parseResult.getProblems().stream()
                        .map(problem -> ParseError.of(
                            problem.getMessage(),
                            SourceLocation.at(filePath,
                                problem.getLocation().isPresent() ? 
                                    problem.getLocation().get().begin.line : 1,
                                problem.getLocation().isPresent() ? 
                                    problem.getLocation().get().begin.column : 1)
                        ))
                        .toList();
                    
                    return ParseResult.failure(language, filePath, errors, parseTime);
                }
                
            } catch (ParseProblemException e) {
                long parseTime = System.currentTimeMillis() - startTime;
                List<ParseError> errors = List.of(
                    ParseError.of("Parse error: " + e.getMessage(), 
                                 SourceLocation.at(filePath, 1, 1))
                );
                return ParseResult.failure(language, filePath, errors, parseTime);
            } catch (Exception e) {
                long parseTime = System.currentTimeMillis() - startTime;
                logger.error("Unexpected error parsing Java code", e);
                List<ParseError> errors = List.of(
                    ParseError.of("Unexpected error: " + e.getMessage(), 
                                 SourceLocation.at(filePath, 1, 1))
                );
                return ParseResult.failure(language, filePath, errors, parseTime);
            }
        });
    }
    
    @Override
    public CompletableFuture<ParseResult> incrementalParse(ASTNode existingAST, String changes, String language) {
        // For now, fall back to full parsing
        // TODO: Implement true incremental parsing
        logger.debug("Incremental parsing not yet implemented for Java, falling back to full parse");
        return parseCode(changes, language, existingAST.getLocation().getFilePath());
    }
    
    @Override
    public CompletableFuture<Boolean> validateSyntax(String sourceCode, String language) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                com.github.javaparser.ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
                return result.isSuccessful();
            } catch (Exception e) {
                logger.debug("Syntax validation failed for Java code: {}", e.getMessage());
                return false;
            }
        });
    }
    
    @Override
    public boolean supportsLanguage(String language) {
        return "java".equalsIgnoreCase(language);
    }
    
    @Override
    public String[] getSupportedLanguages() {
        return new String[]{"java"};
    }
    
    /**
     * Converts JavaParser AST to generic AST representation.
     */
    private ASTNode convertToGenericAST(CompilationUnit cu, String filePath) {
        List<ASTNode> children = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        
        // Add package information
        cu.getPackageDeclaration().ifPresent(pkg -> 
            attributes.put("package", pkg.getNameAsString()));
        
        // Add imports
        if (!cu.getImports().isEmpty()) {
            List<String> imports = cu.getImports().stream()
                .map(imp -> imp.getNameAsString())
                .toList();
            attributes.put("imports", imports);
        }
        
        // Convert classes and interfaces
        cu.getTypes().forEach(type -> {
            if (type instanceof ClassOrInterfaceDeclaration) {
                children.add(convertClassDeclaration((ClassOrInterfaceDeclaration) type, filePath));
            }
        });
        
        SourceLocation location = SourceLocation.at(filePath, 1, 1);
        return new CompilationUnitNode("compilation_unit", location, children, attributes);
    }
    
    /**
     * Converts a class declaration to generic AST.
     */
    private ClassNode convertClassDeclaration(ClassOrInterfaceDeclaration classDecl, String filePath) {
        List<ASTNode> children = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        
        // Add modifiers
        List<String> modifiers = classDecl.getModifiers().stream()
            .map(mod -> mod.getKeyword().asString())
            .toList();
        attributes.put("modifiers", modifiers);
        
        // Add superclass
        classDecl.getExtendedTypes().stream()
            .findFirst()
            .ifPresent(superType -> attributes.put("superclass", superType.getNameAsString()));
        
        // Add interfaces
        if (!classDecl.getImplementedTypes().isEmpty()) {
            List<String> interfaces = classDecl.getImplementedTypes().stream()
                .map(type -> type.getNameAsString())
                .toList();
            attributes.put("interfaces", interfaces);
        }
        
        // Convert methods
        classDecl.getMethods().forEach(method -> 
            children.add(convertMethodDeclaration(method, filePath)));
        
        // Convert fields
        classDecl.getFields().forEach(field -> 
            field.getVariables().forEach(var -> 
                children.add(convertFieldDeclaration(var, field, filePath))));
        
        SourceLocation location = getSourceLocation(classDecl, filePath);
        return new ClassNode(classDecl.getNameAsString(), location, children, attributes);
    }
    
    /**
     * Converts a method declaration to generic AST.
     */
    private MethodNode convertMethodDeclaration(MethodDeclaration methodDecl, String filePath) {
        List<ASTNode> children = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        
        // Add modifiers
        List<String> modifiers = methodDecl.getModifiers().stream()
            .map(mod -> mod.getKeyword().asString())
            .toList();
        attributes.put("modifiers", modifiers);
        
        // Add return type
        attributes.put("returnType", methodDecl.getTypeAsString());
        
        // Add parameters
        if (!methodDecl.getParameters().isEmpty()) {
            List<Map<String, String>> parameters = methodDecl.getParameters().stream()
                .map(param -> Map.of(
                    "name", param.getNameAsString(),
                    "type", param.getTypeAsString()
                ))
                .toList();
            attributes.put("parameters", parameters);
        }
        
        // Add throws clause
        if (!methodDecl.getThrownExceptions().isEmpty()) {
            List<String> throwsClause = methodDecl.getThrownExceptions().stream()
                .map(type -> type.asString())
                .toList();
            attributes.put("throws", throwsClause);
        }
        
        // Calculate cyclomatic complexity (basic implementation)
        int complexity = calculateCyclomaticComplexity(methodDecl);
        attributes.put("cyclomaticComplexity", complexity);
        
        // Convert method body statements
        methodDecl.getBody().ifPresent(body -> 
            body.getStatements().forEach(stmt -> 
                children.add(convertStatement(stmt, filePath))));
        
        SourceLocation location = getSourceLocation(methodDecl, filePath);
        return new MethodNode(methodDecl.getNameAsString(), location, children, attributes);
    }
    
    /**
     * Converts a field declaration to generic AST.
     */
    private VariableNode convertFieldDeclaration(VariableDeclarator var, FieldDeclaration field, String filePath) {
        Map<String, Object> attributes = new HashMap<>();
        List<ASTNode> children = new ArrayList<>();
        
        // Add modifiers
        List<String> modifiers = field.getModifiers().stream()
            .map(mod -> mod.getKeyword().asString())
            .toList();
        attributes.put("modifiers", modifiers);
        
        // Add type
        attributes.put("type", var.getTypeAsString());
        attributes.put("field", true);
        
        // Add initializer if present
        var.getInitializer().ifPresent(init -> 
            children.add(convertExpression(init, filePath)));
        
        SourceLocation location = getSourceLocation(var, filePath);
        return new VariableNode(var.getNameAsString(), location, children, attributes);
    }
    
    /**
     * Converts a statement to generic AST.
     */
    private StatementNode convertStatement(Statement stmt, String filePath) {
        Map<String, Object> attributes = new HashMap<>();
        List<ASTNode> children = new ArrayList<>();
        
        // Determine statement type
        String stmtType = stmt.getClass().getSimpleName().toLowerCase()
            .replace("statement", "").replace("stmt", "");
        attributes.put("statementType", stmtType);
        
        // Add child nodes based on statement type
        stmt.getChildNodes().forEach(child -> {
            if (child instanceof Expression) {
                children.add(convertExpression((Expression) child, filePath));
            } else if (child instanceof Statement) {
                children.add(convertStatement((Statement) child, filePath));
            }
        });
        
        SourceLocation location = getSourceLocation(stmt, filePath);
        return new StatementNode(stmtType, location, children, attributes);
    }
    
    /**
     * Converts an expression to generic AST.
     */
    private ExpressionNode convertExpression(Expression expr, String filePath) {
        Map<String, Object> attributes = new HashMap<>();
        List<ASTNode> children = new ArrayList<>();
        
        // Determine expression type
        String exprType = expr.getClass().getSimpleName().toLowerCase()
            .replace("expression", "").replace("expr", "");
        attributes.put("expressionType", exprType);
        
        // Add expression-specific attributes
        if (expr.isLiteralExpr()) {
            attributes.put("literalValue", expr.toString());
        }
        
        // Add child expressions
        expr.getChildNodes().forEach(child -> {
            if (child instanceof Expression) {
                children.add(convertExpression((Expression) child, filePath));
            }
        });
        
        SourceLocation location = getSourceLocation(expr, filePath);
        return new ExpressionNode(exprType, location, children, attributes);
    }
    
    /**
     * Gets source location from JavaParser node.
     */
    private SourceLocation getSourceLocation(Node node, String filePath) {
        if (node.getRange().isPresent()) {
            com.github.javaparser.Range range = node.getRange().get();
            return SourceLocation.range(filePath,
                range.begin.line, range.begin.column,
                range.end.line, range.end.column);
        } else {
            return SourceLocation.at(filePath, 1, 1);
        }
    }
    
    /**
     * Calculates basic cyclomatic complexity for a method.
     */
    private int calculateCyclomaticComplexity(MethodDeclaration method) {
        ComplexityCalculator calculator = new ComplexityCalculator();
        method.accept(calculator, null);
        return calculator.getComplexity();
    }
    
    /**
     * Visitor for calculating cyclomatic complexity.
     */
    private static class ComplexityCalculator extends VoidVisitorAdapter<Void> {
        private int complexity = 1; // Base complexity
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.IfStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.ForStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.WhileStmt n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.SwitchEntry n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.CatchClause n, Void arg) {
            complexity++;
            super.visit(n, arg);
        }
        
        public int getComplexity() {
            return complexity;
        }
    }
    
    /**
     * Custom AST node for compilation unit.
     */
    private static class CompilationUnitNode extends ASTNode {
        public CompilationUnitNode(String name, SourceLocation location, 
                                  List<ASTNode> children, Map<String, Object> attributes) {
            super("compilation_unit", name, location, children, attributes);
        }
    }
}