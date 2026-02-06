package com.ailearning.core.service.ast;

import com.ailearning.core.model.ast.*;
import com.ailearning.core.service.ast.DependencyGraphBuilder.DependencyGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for dependency graph building functionality.
 */
class DependencyGraphBuilderTest {

    private DependencyGraphBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new DependencyGraphBuilder();
    }

    @Test
    @DisplayName("Should build dependency graph from AST nodes")
    void shouldBuildDependencyGraph() throws Exception {
        // Create sample AST nodes
        List<ASTNode> astNodes = createSampleASTNodes();

        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(astNodes);
        DependencyGraph graph = future.get();

        assertNotNull(graph);
        assertFalse(graph.getAllNodes().isEmpty());
    }

    @Test
    @DisplayName("Should detect class inheritance dependencies")
    void shouldDetectClassInheritanceDependencies() throws Exception {
        // Create a class that extends another class
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("superclass", "BaseClass");
        attributes.put("interfaces", List.of("Interface1", "Interface2"));

        ClassNode childClass = new ClassNode(
            "ChildClass",
            SourceLocation.at("Child.java", 1, 1),
            List.of(),
            attributes
        );

        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(List.of(childClass));
        DependencyGraph graph = future.get();

        // Find the child class node
        String childNodeId = graph.getAllNodes().stream()
            .filter(nodeId -> nodeId.contains("ChildClass"))
            .findFirst()
            .orElse(null);

        assertNotNull(childNodeId);

        Set<String> dependencies = graph.getDependencies(childNodeId);
        assertTrue(dependencies.stream().anyMatch(dep -> dep.contains("BaseClass")));
        assertTrue(dependencies.stream().anyMatch(dep -> dep.contains("Interface1")));
        assertTrue(dependencies.stream().anyMatch(dep -> dep.contains("Interface2")));
    }

    @Test
    @DisplayName("Should detect method parameter type dependencies")
    void shouldDetectMethodParameterTypeDependencies() throws Exception {
        // Create a method with custom parameter types
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("returnType", "CustomReturnType");
        attributes.put("parameters", List.of(
            Map.of("name", "param1", "type", "CustomType1"),
            Map.of("name", "param2", "type", "CustomType2")
        ));
        attributes.put("throws", List.of("CustomException"));

        MethodNode method = new MethodNode(
            "testMethod",
            SourceLocation.at("Test.java", 10, 5),
            List.of(),
            attributes
        );

        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(List.of(method));
        DependencyGraph graph = future.get();

        String methodNodeId = graph.getAllNodes().stream()
            .filter(nodeId -> nodeId.contains("testMethod"))
            .findFirst()
            .orElse(null);

        assertNotNull(methodNodeId);

        Set<String> dependencies = graph.getDependencies(methodNodeId);
        assertTrue(dependencies.stream().anyMatch(dep -> dep.contains("CustomReturnType")));
        assertTrue(dependencies.stream().anyMatch(dep -> dep.contains("CustomType1")));
        assertTrue(dependencies.stream().anyMatch(dep -> dep.contains("CustomType2")));
        assertTrue(dependencies.stream().anyMatch(dep -> dep.contains("CustomException")));
    }

    @Test
    @DisplayName("Should not create dependencies for primitive types")
    void shouldNotCreateDependenciesForPrimitiveTypes() throws Exception {
        // Create a method with primitive parameter types
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("returnType", "int");
        attributes.put("parameters", List.of(
            Map.of("name", "param1", "type", "int"),
            Map.of("name", "param2", "type", "String"),
            Map.of("name", "param3", "type", "boolean")
        ));

        MethodNode method = new MethodNode(
            "primitiveMethod",
            SourceLocation.at("Test.java", 5, 1),
            List.of(),
            attributes
        );

        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(List.of(method));
        DependencyGraph graph = future.get();

        String methodNodeId = graph.getAllNodes().stream()
            .filter(nodeId -> nodeId.contains("primitiveMethod"))
            .findFirst()
            .orElse(null);

        assertNotNull(methodNodeId);

        Set<String> dependencies = graph.getDependencies(methodNodeId);
        // Should not have dependencies on primitive types
        assertFalse(dependencies.stream().anyMatch(dep -> dep.contains("int")));
        assertFalse(dependencies.stream().anyMatch(dep -> dep.contains("String")));
        assertFalse(dependencies.stream().anyMatch(dep -> dep.contains("boolean")));
    }

    @Test
    @DisplayName("Should detect field type dependencies")
    void shouldDetectFieldTypeDependencies() throws Exception {
        // Create fields with custom types
        VariableNode field1 = new VariableNode(
            "customField",
            SourceLocation.at("Test.java", 3, 5),
            List.of(),
            Map.of("type", "CustomFieldType", "field", true)
        );

        VariableNode field2 = new VariableNode(
            "primitiveField",
            SourceLocation.at("Test.java", 4, 5),
            List.of(),
            Map.of("type", "int", "field", true)
        );

        ClassNode testClass = new ClassNode(
            "TestClass",
            SourceLocation.at("Test.java", 1, 1),
            List.of(field1, field2),
            Map.of()
        );

        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(List.of(testClass));
        DependencyGraph graph = future.get();

        String classNodeId = graph.getAllNodes().stream()
            .filter(nodeId -> nodeId.contains("TestClass"))
            .findFirst()
            .orElse(null);

        assertNotNull(classNodeId);

        Set<String> dependencies = graph.getDependencies(classNodeId);
        assertTrue(dependencies.stream().anyMatch(dep -> dep.contains("CustomFieldType")));
        assertFalse(dependencies.stream().anyMatch(dep -> dep.contains("int")));
    }

    @Test
    @DisplayName("Should provide graph statistics")
    void shouldProvideGraphStatistics() throws Exception {
        List<ASTNode> astNodes = createSampleASTNodes();

        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(astNodes);
        DependencyGraph graph = future.get();

        Map<String, Object> stats = graph.getStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("nodeCount"));
        assertTrue(stats.containsKey("edgeCount"));
        assertTrue(stats.containsKey("averageDependencies"));
        assertTrue(stats.containsKey("maxDependencies"));
        assertTrue(stats.containsKey("circularDependencies"));

        assertTrue((Integer) stats.get("nodeCount") > 0);
        assertTrue((Integer) stats.get("edgeCount") >= 0);
        assertTrue((Double) stats.get("averageDependencies") >= 0.0);
        assertTrue((Integer) stats.get("maxDependencies") >= 0);
        assertTrue((Integer) stats.get("circularDependencies") >= 0);
    }

    @Test
    @DisplayName("Should detect circular dependencies")
    void shouldDetectCircularDependencies() throws Exception {
        // This is a simplified test - in practice, circular dependencies
        // would be detected through more complex analysis
        List<ASTNode> astNodes = createSampleASTNodes();

        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(astNodes);
        DependencyGraph graph = future.get();

        List<List<String>> cycles = graph.detectCircularDependencies();
        assertNotNull(cycles);
        // For our simple test case, we don't expect circular dependencies
        assertTrue(cycles.isEmpty());
    }

    @Test
    @DisplayName("Should find dependents of a node")
    void shouldFindDependentsOfNode() throws Exception {
        List<ASTNode> astNodes = createSampleASTNodes();

        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(astNodes);
        DependencyGraph graph = future.get();

        // Test that we can find dependents (nodes that depend on a given node)
        for (String nodeId : graph.getAllNodes()) {
            Set<String> dependents = graph.getDependents(nodeId);
            assertNotNull(dependents);
            // Dependents set can be empty, which is valid
        }
    }

    @Test
    @DisplayName("Should handle empty node collection")
    void shouldHandleEmptyNodeCollection() throws Exception {
        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(List.of());
        DependencyGraph graph = future.get();

        assertNotNull(graph);
        assertTrue(graph.getAllNodes().isEmpty());

        Map<String, Object> stats = graph.getStatistics();
        assertEquals(0, stats.get("nodeCount"));
        assertEquals(0, stats.get("edgeCount"));
    }

    @Test
    @DisplayName("Should handle nodes with no dependencies")
    void shouldHandleNodesWithNoDependencies() throws Exception {
        // Create a simple class with no external dependencies
        ClassNode simpleClass = new ClassNode(
            "SimpleClass",
            SourceLocation.at("Simple.java", 1, 1),
            List.of(),
            Map.of()
        );

        CompletableFuture<DependencyGraph> future = builder.buildDependencyGraph(List.of(simpleClass));
        DependencyGraph graph = future.get();

        assertNotNull(graph);
        assertEquals(1, graph.getAllNodes().size());

        String nodeId = graph.getAllNodes().iterator().next();
        Set<String> dependencies = graph.getDependencies(nodeId);
        assertTrue(dependencies.isEmpty());
    }

    /**
     * Creates sample AST nodes for testing.
     */
    private List<ASTNode> createSampleASTNodes() {
        List<ASTNode> nodes = new ArrayList<>();

        // Create a sample class with methods and fields
        Map<String, Object> classAttributes = new HashMap<>();
        classAttributes.put("modifiers", List.of("public"));
        classAttributes.put("superclass", "BaseClass");

        // Create a method
        Map<String, Object> methodAttributes = new HashMap<>();
        methodAttributes.put("returnType", "void");
        methodAttributes.put("modifiers", List.of("public"));
        methodAttributes.put("parameters", List.of(
            Map.of("name", "param", "type", "String")
        ));

        MethodNode method = new MethodNode(
            "testMethod",
            SourceLocation.at("Test.java", 5, 5),
            List.of(),
            methodAttributes
        );

        // Create a field
        VariableNode field = new VariableNode(
            "testField",
            SourceLocation.at("Test.java", 3, 5),
            List.of(),
            Map.of("type", "CustomType", "field", true)
        );

        ClassNode testClass = new ClassNode(
            "TestClass",
            SourceLocation.at("Test.java", 1, 1),
            List.of(method, field),
            classAttributes
        );

        nodes.add(testClass);
        return nodes;
    }
}