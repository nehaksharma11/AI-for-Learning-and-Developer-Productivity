package com.ailearning.core.service.ast;

import com.ailearning.core.model.ast.ASTNode;
import com.ailearning.core.model.ast.ClassNode;
import com.ailearning.core.model.ast.MethodNode;
import com.ailearning.core.model.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds dependency graphs from AST nodes to understand code relationships.
 * Analyzes imports, method calls, field accesses, and inheritance relationships.
 */
public class DependencyGraphBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);
    
    private final Map<String, Set<String>> dependencyGraph = new ConcurrentHashMap<>();
    private final Map<String, ASTNode> nodeRegistry = new ConcurrentHashMap<>();
    
    /**
     * Builds a dependency graph from a collection of AST nodes.
     * 
     * @param astNodes the AST nodes to analyze
     * @return CompletableFuture containing the dependency graph
     */
    public CompletableFuture<DependencyGraph> buildDependencyGraph(Collection<ASTNode> astNodes) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Building dependency graph from {} AST nodes", astNodes.size());
            
            // Clear previous state
            dependencyGraph.clear();
            nodeRegistry.clear();
            
            // Register all nodes
            astNodes.forEach(this::registerNode);
            
            // Analyze dependencies
            astNodes.forEach(this::analyzeDependencies);
            
            // Build the final graph
            DependencyGraph graph = new DependencyGraph(
                new HashMap<>(dependencyGraph),
                new HashMap<>(nodeRegistry)
            );
            
            logger.debug("Built dependency graph with {} nodes and {} edges", 
                        nodeRegistry.size(), dependencyGraph.size());
            
            return graph;
        });
    }
    
    /**
     * Registers a node in the registry for dependency analysis.
     */
    private void registerNode(ASTNode node) {
        String nodeId = getNodeId(node);
        nodeRegistry.put(nodeId, node);
        dependencyGraph.putIfAbsent(nodeId, new HashSet<>());
        
        // Recursively register child nodes
        node.getChildren().forEach(this::registerNode);
    }
    
    /**
     * Analyzes dependencies for a given AST node.
     */
    private void analyzeDependencies(ASTNode node) {
        String nodeId = getNodeId(node);
        Set<String> dependencies = dependencyGraph.get(nodeId);
        
        if (node instanceof ClassNode) {
            analyzeClassDependencies((ClassNode) node, dependencies);
        } else if (node instanceof MethodNode) {
            analyzeMethodDependencies((MethodNode) node, dependencies);
        }
        
        // Recursively analyze child nodes
        node.getChildren().forEach(this::analyzeDependencies);
    }
    
    /**
     * Analyzes dependencies for a class node.
     */
    private void analyzeClassDependencies(ClassNode classNode, Set<String> dependencies) {
        // Analyze superclass dependency
        String superclass = classNode.getSuperclass();
        if (superclass != null) {
            dependencies.add("class:" + superclass);
        }
        
        // Analyze interface dependencies
        classNode.getInterfaces().forEach(interfaceName -> 
            dependencies.add("interface:" + interfaceName));
        
        // Analyze import dependencies
        Object imports = classNode.getAttribute("imports");
        if (imports instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> importList = (List<String>) imports;
            importList.forEach(importName -> 
                dependencies.add("import:" + importName));
        }
        
        // Analyze field type dependencies
        classNode.getFields().forEach(field -> {
            String fieldType = field.getType();
            if (fieldType != null && !isPrimitiveType(fieldType)) {
                dependencies.add("type:" + fieldType);
            }
        });
        
        // Analyze method dependencies
        classNode.getMethods().forEach(method -> {
            String returnType = method.getReturnType();
            if (returnType != null && !isPrimitiveType(returnType) && !"void".equals(returnType)) {
                dependencies.add("type:" + returnType);
            }
            
            // Analyze parameter type dependencies
            method.getParameters().forEach(param -> {
                String paramType = param.get("type");
                if (paramType != null && !isPrimitiveType(paramType)) {
                    dependencies.add("type:" + paramType);
                }
            });
        });
    }
    
    /**
     * Analyzes dependencies for a method node.
     */
    private void analyzeMethodDependencies(MethodNode methodNode, Set<String> dependencies) {
        // Analyze return type dependency
        String returnType = methodNode.getReturnType();
        if (returnType != null && !isPrimitiveType(returnType) && !"void".equals(returnType)) {
            dependencies.add("type:" + returnType);
        }
        
        // Analyze parameter dependencies
        methodNode.getParameters().forEach(param -> {
            String paramType = param.get("type");
            if (paramType != null && !isPrimitiveType(paramType)) {
                dependencies.add("type:" + paramType);
            }
        });
        
        // Analyze exception dependencies
        methodNode.getThrows().forEach(exceptionType -> 
            dependencies.add("exception:" + exceptionType));
        
        // TODO: Analyze method body for method calls, field accesses, etc.
        // This would require more sophisticated AST traversal
    }
    
    /**
     * Generates a unique identifier for an AST node.
     */
    private String getNodeId(ASTNode node) {
        String location = node.getLocation().toString();
        return node.getNodeType() + ":" + (node.getName() != null ? node.getName() : "anonymous") + "@" + location;
    }
    
    /**
     * Checks if a type is a primitive type.
     */
    private boolean isPrimitiveType(String type) {
        return Set.of("int", "long", "short", "byte", "float", "double", "boolean", "char", 
                     "void", "String", "Object", "unknown").contains(type);
    }
    
    /**
     * Represents a dependency graph with nodes and their relationships.
     */
    public static class DependencyGraph {
        private final Map<String, Set<String>> adjacencyList;
        private final Map<String, ASTNode> nodes;
        
        public DependencyGraph(Map<String, Set<String>> adjacencyList, Map<String, ASTNode> nodes) {
            this.adjacencyList = Map.copyOf(adjacencyList);
            this.nodes = Map.copyOf(nodes);
        }
        
        /**
         * Gets all dependencies for a given node.
         * 
         * @param nodeId the node identifier
         * @return set of dependency identifiers
         */
        public Set<String> getDependencies(String nodeId) {
            return adjacencyList.getOrDefault(nodeId, Set.of());
        }
        
        /**
         * Gets all nodes that depend on a given node.
         * 
         * @param nodeId the node identifier
         * @return set of dependent node identifiers
         */
        public Set<String> getDependents(String nodeId) {
            Set<String> dependents = new HashSet<>();
            adjacencyList.forEach((node, deps) -> {
                if (deps.contains(nodeId)) {
                    dependents.add(node);
                }
            });
            return dependents;
        }
        
        /**
         * Gets all node identifiers in the graph.
         * 
         * @return set of node identifiers
         */
        public Set<String> getAllNodes() {
            return nodes.keySet();
        }
        
        /**
         * Gets the AST node for a given identifier.
         * 
         * @param nodeId the node identifier
         * @return the AST node, or null if not found
         */
        public ASTNode getNode(String nodeId) {
            return nodes.get(nodeId);
        }
        
        /**
         * Checks if there's a path between two nodes.
         * 
         * @param fromNode the source node
         * @param toNode the target node
         * @return true if a path exists
         */
        public boolean hasPath(String fromNode, String toNode) {
            if (fromNode.equals(toNode)) return true;
            
            Set<String> visited = new HashSet<>();
            Queue<String> queue = new LinkedList<>();
            queue.offer(fromNode);
            visited.add(fromNode);
            
            while (!queue.isEmpty()) {
                String current = queue.poll();
                Set<String> dependencies = adjacencyList.getOrDefault(current, Set.of());
                
                for (String dependency : dependencies) {
                    if (dependency.equals(toNode)) {
                        return true;
                    }
                    if (!visited.contains(dependency)) {
                        visited.add(dependency);
                        queue.offer(dependency);
                    }
                }
            }
            
            return false;
        }
        
        /**
         * Detects circular dependencies in the graph.
         * 
         * @return list of circular dependency chains
         */
        public List<List<String>> detectCircularDependencies() {
            List<List<String>> cycles = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            Set<String> recursionStack = new HashSet<>();
            
            for (String node : nodes.keySet()) {
                if (!visited.contains(node)) {
                    detectCyclesUtil(node, visited, recursionStack, new ArrayList<>(), cycles);
                }
            }
            
            return cycles;
        }
        
        /**
         * Utility method for cycle detection using DFS.
         */
        private void detectCyclesUtil(String node, Set<String> visited, Set<String> recursionStack, 
                                    List<String> currentPath, List<List<String>> cycles) {
            visited.add(node);
            recursionStack.add(node);
            currentPath.add(node);
            
            Set<String> dependencies = adjacencyList.getOrDefault(node, Set.of());
            for (String dependency : dependencies) {
                if (!visited.contains(dependency)) {
                    detectCyclesUtil(dependency, visited, recursionStack, currentPath, cycles);
                } else if (recursionStack.contains(dependency)) {
                    // Found a cycle
                    int cycleStart = currentPath.indexOf(dependency);
                    if (cycleStart >= 0) {
                        cycles.add(new ArrayList<>(currentPath.subList(cycleStart, currentPath.size())));
                    }
                }
            }
            
            recursionStack.remove(node);
            currentPath.remove(currentPath.size() - 1);
        }
        
        /**
         * Gets statistics about the dependency graph.
         * 
         * @return map of statistics
         */
        public Map<String, Object> getStatistics() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("nodeCount", nodes.size());
            stats.put("edgeCount", adjacencyList.values().stream().mapToInt(Set::size).sum());
            stats.put("averageDependencies", adjacencyList.values().stream()
                .mapToInt(Set::size).average().orElse(0.0));
            stats.put("maxDependencies", adjacencyList.values().stream()
                .mapToInt(Set::size).max().orElse(0));
            stats.put("circularDependencies", detectCircularDependencies().size());
            return stats;
        }
    }
}