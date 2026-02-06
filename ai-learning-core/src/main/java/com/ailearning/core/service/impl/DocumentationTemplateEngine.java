package com.ailearning.core.service.impl;

import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ast.ASTNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Template engine for generating documentation based on predefined templates.
 */
class DocumentationTemplateEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentationTemplateEngine.class);
    
    private final Map<String, Map<String, String>> templates;
    
    public DocumentationTemplateEngine() {
        this.templates = initializeTemplates();
    }
    
    /**
     * Generates a comment for a code element using appropriate template.
     */
    public String generateComment(String elementName, String elementType, String language, ASTNode node) {
        try {
            String template = getTemplate(language, elementType);
            if (template == null) {
                return generateDefaultComment(elementName, elementType, language);
            }
            
            return applyTemplate(template, elementName, elementType, node);
            
        } catch (Exception e) {
            logger.error("Error generating comment for element: {}", elementName, e);
            return generateDefaultComment(elementName, elementType, language);
        }
    }
    
    /**
     * Generates full documentation for a code element.
     */
    public String generateDocumentation(ASTNode node, String language, String elementType) {
        try {
            String elementName = extractElementName(node);
            String template = getDocumentationTemplate(language, elementType);
            
            if (template == null) {
                return generateDefaultDocumentation(elementName, elementType, language);
            }
            
            return applyDocumentationTemplate(template, elementName, elementType, node, language);
            
        } catch (Exception e) {
            logger.error("Error generating documentation", e);
            return generateDefaultDocumentation("unknown", elementType, language);
        }
    }
    
    /**
     * Generates markdown documentation for a project.
     */
    public String generateMarkdown(ProjectContext projectContext, String template) {
        try {
            if (template == null || template.trim().isEmpty()) {
                template = getDefaultMarkdownTemplate();
            }
            
            return applyMarkdownTemplate(template, projectContext);
            
        } catch (Exception e) {
            logger.error("Error generating markdown documentation", e);
            return generateDefaultMarkdown(projectContext);
        }
    }
    
    /**
     * Generates documentation for a code element using element details.
     */
    public String generateDocumentationForElement(String elementName, String elementType, 
                                                 String signature, List<String> parameters, 
                                                 String returnType) {
        try {
            // Determine language from context (simplified - would be passed as parameter)
            String language = "java"; // Default to Java for now
            
            String template = getTemplate(language, elementType);
            if (template == null) {
                return generateDefaultDocumentationForElement(elementName, elementType, language);
            }
            
            return applyElementTemplate(template, elementName, elementType, signature, parameters, returnType);
            
        } catch (Exception e) {
            logger.error("Error generating documentation for element: {}", elementName, e);
            return generateDefaultDocumentationForElement(elementName, elementType, "java");
        }
    }
    
    /**
     * Gets available templates for a language and element type.
     */
    public List<String> getAvailableTemplates(String language, String elementType) {
        Map<String, String> languageTemplates = templates.get(language.toLowerCase());
        if (languageTemplates == null) {
            return List.of("default");
        }
        
        return languageTemplates.keySet().stream()
                .filter(key -> elementType == null || key.contains(elementType.toLowerCase()))
                .sorted()
                .toList();
    }
    
    /**
     * Gets the template name used for a specific language and element type.
     */
    public String getTemplateName(String language, String elementType) {
        return language.toLowerCase() + "_" + elementType.toLowerCase();
    }
    
    // Private helper methods
    
    private String applyElementTemplate(String template, String elementName, String elementType,
                                      String signature, List<String> parameters, String returnType) {
        String result = template;
        
        // Replace placeholders
        result = result.replace("{name}", elementName);
        result = result.replace("{description}", generateElementDescription(elementName, elementType));
        result = result.replace("{params}", generateParameterDocsFromList(parameters));
        result = result.replace("{return_description}", generateReturnDescriptionFromType(returnType));
        result = result.replace("{type}", returnType != null ? returnType : "unknown");
        result = result.replace("{signature}", signature != null ? signature : "");
        
        return result;
    }
    
    private String generateDefaultDocumentationForElement(String elementName, String elementType, String language) {
        switch (language.toLowerCase()) {
            case "java" -> {
                return "/**\n * " + generateElementDescription(elementName, elementType) + "\n */";
            }
            case "javascript", "typescript" -> {
                return "/**\n * " + generateElementDescription(elementName, elementType) + "\n */";
            }
            case "python" -> {
                return "\"\"\"" + generateElementDescription(elementName, elementType) + "\"\"\"";
            }
            default -> {
                return "// " + generateElementDescription(elementName, elementType);
            }
        }
    }
    
    private String generateElementDescription(String elementName, String elementType) {
        return capitalizeFirst(elementType) + " " + elementName + " - auto-generated description.";
    }
    
    private String generateParameterDocsFromList(List<String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "No parameters";
        }
        
        StringBuilder paramDocs = new StringBuilder();
        for (String param : parameters) {
            if (paramDocs.length() > 0) {
                paramDocs.append(", ");
            }
            paramDocs.append(param).append(" - parameter description");
        }
        
        return paramDocs.toString();
    }
    
    private String generateReturnDescriptionFromType(String returnType) {
        if (returnType == null || returnType.equals("void")) {
            return "No return value";
        }
        return "Returns " + returnType + " - return value description";
    }
    
    private Map<String, Map<String, String>> initializeTemplates() {
        Map<String, Map<String, String>> templates = new HashMap<>();
        
        // Java templates
        Map<String, String> javaTemplates = new HashMap<>();
        javaTemplates.put("class", 
                "/**\n" +
                " * {description}\n" +
                " * \n" +
                " * @author Generated\n" +
                " * @version 1.0\n" +
                " */");
        javaTemplates.put("method", 
                "/**\n" +
                " * {description}\n" +
                " * \n" +
                " * @param {params}\n" +
                " * @return {return_description}\n" +
                " */");
        javaTemplates.put("field", 
                "/**\n" +
                " * {description}\n" +
                " */");
        templates.put("java", javaTemplates);
        
        // JavaScript/TypeScript templates
        Map<String, String> jsTemplates = new HashMap<>();
        jsTemplates.put("function", 
                "/**\n" +
                " * {description}\n" +
                " * \n" +
                " * @param {params}\n" +
                " * @returns {return_description}\n" +
                " */");
        jsTemplates.put("class", 
                "/**\n" +
                " * {description}\n" +
                " * \n" +
                " * @class {name}\n" +
                " */");
        jsTemplates.put("variable", 
                "/**\n" +
                " * {description}\n" +
                " * @type {type}\n" +
                " */");
        templates.put("javascript", jsTemplates);
        templates.put("typescript", jsTemplates);
        
        // Python templates
        Map<String, String> pythonTemplates = new HashMap<>();
        pythonTemplates.put("function", 
                "\"\"\"\n" +
                "{description}\n" +
                "\n" +
                "Args:\n" +
                "    {params}\n" +
                "\n" +
                "Returns:\n" +
                "    {return_description}\n" +
                "\"\"\"");
        pythonTemplates.put("class", 
                "\"\"\"\n" +
                "{description}\n" +
                "\n" +
                "Attributes:\n" +
                "    {attributes}\n" +
                "\"\"\"");
        templates.put("python", pythonTemplates);
        
        return templates;
    }
    
    private String getTemplate(String language, String elementType) {
        Map<String, String> languageTemplates = templates.get(language.toLowerCase());
        if (languageTemplates == null) {
            return null;
        }
        
        return languageTemplates.get(elementType.toLowerCase());
    }
    
    private String getDocumentationTemplate(String language, String elementType) {
        // For now, use the same templates as comments
        return getTemplate(language, elementType);
    }
    
    private String applyTemplate(String template, String elementName, String elementType, ASTNode node) {
        String result = template;
        
        // Replace placeholders
        result = result.replace("{name}", elementName);
        result = result.replace("{description}", generateDescription(elementName, elementType));
        result = result.replace("{params}", generateParameterDocs(node));
        result = result.replace("{return_description}", generateReturnDescription(node));
        result = result.replace("{type}", inferType(node));
        result = result.replace("{attributes}", generateAttributeDocs(node));
        
        return result;
    }
    
    private String applyDocumentationTemplate(String template, String elementName, String elementType, 
                                            ASTNode node, String language) {
        return applyTemplate(template, elementName, elementType, node);
    }
    
    private String applyMarkdownTemplate(String template, ProjectContext projectContext) {
        String result = template;
        
        result = result.replace("{project_name}", projectContext.getProjectName());
        result = result.replace("{description}", "Auto-generated project documentation");
        result = result.replace("{version}", "1.0.0");
        result = result.replace("{author}", "AI Learning Companion");
        
        return result;
    }
    
    private String generateDefaultComment(String elementName, String elementType, String language) {
        switch (language.toLowerCase()) {
            case "java" -> {
                return "/**\n * " + capitalizeFirst(elementType) + " " + elementName + "\n */";
            }
            case "javascript", "typescript" -> {
                return "/**\n * " + capitalizeFirst(elementType) + " " + elementName + "\n */";
            }
            case "python" -> {
                return "\"\"\"" + capitalizeFirst(elementType) + " " + elementName + "\"\"\"";
            }
            default -> {
                return "// " + capitalizeFirst(elementType) + " " + elementName;
            }
        }
    }
    
    private String generateDefaultDocumentation(String elementName, String elementType, String language) {
        return generateDefaultComment(elementName, elementType, language);
    }
    
    private String generateDefaultMarkdown(ProjectContext projectContext) {
        return "# " + projectContext.getProjectName() + "\n\n" +
               "Auto-generated documentation for " + projectContext.getProjectName() + ".\n\n" +
               "## Overview\n\n" +
               "This project contains " + projectContext.getStructure().getFiles().size() + " files.\n";
    }
    
    private String getDefaultMarkdownTemplate() {
        return "# {project_name}\n\n" +
               "{description}\n\n" +
               "## Installation\n\n" +
               "```bash\n" +
               "# Installation instructions\n" +
               "```\n\n" +
               "## Usage\n\n" +
               "```\n" +
               "// Usage examples\n" +
               "```\n\n" +
               "## API Reference\n\n" +
               "Auto-generated API documentation.\n\n" +
               "## Contributing\n\n" +
               "Please read the contributing guidelines.\n\n" +
               "## License\n\n" +
               "This project is licensed under the MIT License.\n";
    }
    
    private String extractElementName(ASTNode node) {
        // This would be implemented based on the specific AST node types
        if (node.getText() != null) {
            // Simple extraction - in a real implementation, this would be more sophisticated
            String[] words = node.getText().split("\\s+");
            for (String word : words) {
                if (word.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    return word;
                }
            }
        }
        return "unknown";
    }
    
    private String generateDescription(String elementName, String elementType) {
        return capitalizeFirst(elementType) + " " + elementName + " - auto-generated description.";
    }
    
    private String generateParameterDocs(ASTNode node) {
        try {
            if (node instanceof MethodNode methodNode) {
                List<String> parameters = extractParametersFromMethodNode(methodNode);
                if (parameters.isEmpty()) {
                    return "";
                }
                
                StringBuilder paramDocs = new StringBuilder();
                for (String param : parameters) {
                    if (paramDocs.length() > 0) {
                        paramDocs.append("\n * ");
                    }
                    paramDocs.append("@param ").append(param).append(" ");
                    paramDocs.append(generateParameterDescription(param));
                }
                return paramDocs.toString();
            }
            
            // Fallback: extract from node text
            String nodeText = node.getText();
            if (nodeText != null && nodeText.contains("(") && nodeText.contains(")")) {
                List<String> params = extractParametersFromSignature(nodeText);
                if (!params.isEmpty()) {
                    StringBuilder paramDocs = new StringBuilder();
                    for (String param : params) {
                        if (paramDocs.length() > 0) {
                            paramDocs.append("\n * ");
                        }
                        paramDocs.append("@param ").append(param).append(" ");
                        paramDocs.append(generateParameterDescription(param));
                    }
                    return paramDocs.toString();
                }
            }
            
            return "";
        } catch (Exception e) {
            logger.warn("Error generating parameter documentation for node", e);
            return "";
        }
    }
    
    private String generateReturnDescription(ASTNode node) {
        try {
            String returnType = extractReturnType(node);
            if (returnType == null || returnType.equals("void")) {
                return "";
            }
            
            return "@return " + generateReturnTypeDescription(returnType);
        } catch (Exception e) {
            logger.warn("Error generating return description for node", e);
            return "@return the result of the operation";
        }
    }
    
    private String inferType(ASTNode node) {
        try {
            if (node instanceof VariableNode variableNode) {
                return extractVariableType(variableNode);
            } else if (node instanceof MethodNode methodNode) {
                return extractMethodReturnType(methodNode);
            }
            
            // Fallback: try to infer from text
            String nodeText = node.getText();
            if (nodeText != null) {
                return inferTypeFromText(nodeText);
            }
            
            return "Object";
        } catch (Exception e) {
            logger.warn("Error inferring type for node", e);
            return "Object";
        }
    }
    
    private String generateAttributeDocs(ASTNode node) {
        try {
            if (node instanceof ClassNode classNode) {
                List<String> attributes = extractClassAttributes(classNode);
                if (attributes.isEmpty()) {
                    return "";
                }
                
                StringBuilder attrDocs = new StringBuilder();
                for (String attr : attributes) {
                    if (attrDocs.length() > 0) {
                        attrDocs.append("\n * ");
                    }
                    attrDocs.append(attr).append(" - ").append(generateAttributeDescription(attr));
                }
                return attrDocs.toString();
            }
            
            return "";
        } catch (Exception e) {
            logger.warn("Error generating attribute documentation for node", e);
            return "";
        }
    }
    
    // Helper methods for enhanced AST analysis
    
    private List<String> extractParametersFromMethodNode(MethodNode methodNode) {
        // This would be implemented based on the actual MethodNode structure
        // For now, return empty list as the actual AST structure may vary
        return List.of();
    }
    
    private List<String> extractParametersFromSignature(String signature) {
        List<String> parameters = new ArrayList<>();
        
        int startParen = signature.indexOf('(');
        int endParen = signature.lastIndexOf(')');
        
        if (startParen >= 0 && endParen > startParen) {
            String paramString = signature.substring(startParen + 1, endParen).trim();
            if (!paramString.isEmpty()) {
                String[] params = paramString.split(",");
                for (String param : params) {
                    String paramName = extractParameterName(param.trim());
                    if (!paramName.isEmpty()) {
                        parameters.add(paramName);
                    }
                }
            }
        }
        
        return parameters;
    }
    
    private String extractParameterName(String paramDeclaration) {
        // Handle different parameter formats: "int x", "String name", "final List<String> items"
        String[] parts = paramDeclaration.trim().split("\\s+");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            // Remove any array brackets or generic type info
            return lastPart.replaceAll("[\\[\\]<>].*", "");
        }
        return "";
    }
    
    private String generateParameterDescription(String paramName) {
        // Generate intelligent parameter descriptions based on naming conventions
        if (paramName.toLowerCase().contains("id")) {
            return "the unique identifier";
        } else if (paramName.toLowerCase().contains("name")) {
            return "the name value";
        } else if (paramName.toLowerCase().contains("count") || paramName.toLowerCase().contains("size")) {
            return "the count or size value";
        } else if (paramName.toLowerCase().contains("flag") || paramName.toLowerCase().contains("enabled")) {
            return "flag indicating whether the feature is enabled";
        } else if (paramName.toLowerCase().contains("list") || paramName.toLowerCase().contains("array")) {
            return "the list of items";
        } else if (paramName.toLowerCase().contains("map") || paramName.toLowerCase().contains("dict")) {
            return "the mapping of key-value pairs";
        } else if (paramName.toLowerCase().contains("callback") || paramName.toLowerCase().contains("handler")) {
            return "the callback function to handle the operation";
        } else {
            return "the " + paramName + " parameter";
        }
    }
    
    private String extractReturnType(ASTNode node) {
        if (node instanceof MethodNode methodNode) {
            return extractMethodReturnType(methodNode);
        }
        
        // Fallback: try to extract from text
        String nodeText = node.getText();
        if (nodeText != null) {
            return inferReturnTypeFromText(nodeText);
        }
        
        return "void";
    }
    
    private String extractMethodReturnType(MethodNode methodNode) {
        // This would be implemented based on the actual MethodNode structure
        // For now, return a default type
        return "Object";
    }
    
    private String extractVariableType(VariableNode variableNode) {
        // This would be implemented based on the actual VariableNode structure
        // For now, return a default type
        return "Object";
    }
    
    private String inferTypeFromText(String text) {
        // Simple type inference from method signatures
        if (text.contains("boolean ") || text.contains("Boolean")) return "boolean";
        if (text.contains("int ") || text.contains("Integer")) return "int";
        if (text.contains("long ") || text.contains("Long")) return "long";
        if (text.contains("double ") || text.contains("Double")) return "double";
        if (text.contains("float ") || text.contains("Float")) return "float";
        if (text.contains("String")) return "String";
        if (text.contains("List")) return "List";
        if (text.contains("Map")) return "Map";
        if (text.contains("Set")) return "Set";
        if (text.contains("void ")) return "void";
        
        return "Object";
    }
    
    private String inferReturnTypeFromText(String text) {
        // Look for return type in method signature
        String[] words = text.split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].equals("public") || words[i].equals("private") || 
                words[i].equals("protected") || words[i].equals("static") ||
                words[i].equals("final")) {
                continue;
            }
            // Next non-modifier word should be the return type
            return words[i];
        }
        
        return "void";
    }
    
    private String generateReturnTypeDescription(String returnType) {
        switch (returnType.toLowerCase()) {
            case "boolean" -> {
                return "true if the operation succeeds, false otherwise";
            }
            case "int", "integer", "long" -> {
                return "the numeric result of the operation";
            }
            case "string" -> {
                return "the string result of the operation";
            }
            case "list" -> {
                return "a list containing the results";
            }
            case "map" -> {
                return "a map containing the key-value pairs";
            }
            case "set" -> {
                return "a set containing unique elements";
            }
            case "optional" -> {
                return "an Optional containing the result, or empty if not found";
            }
            default -> {
                return "the " + returnType + " result of the operation";
            }
        }
    }
    
    private List<String> extractClassAttributes(ClassNode classNode) {
        // This would be implemented based on the actual ClassNode structure
        // For now, return empty list
        return List.of();
    }
    
    private String generateAttributeDescription(String attributeName) {
        // Generate intelligent attribute descriptions based on naming conventions
        if (attributeName.toLowerCase().contains("id")) {
            return "unique identifier for this instance";
        } else if (attributeName.toLowerCase().contains("name")) {
            return "the name of this instance";
        } else if (attributeName.toLowerCase().contains("count") || attributeName.toLowerCase().contains("size")) {
            return "the count or size value";
        } else if (attributeName.toLowerCase().contains("flag") || attributeName.toLowerCase().contains("enabled")) {
            return "flag indicating the state of this feature";
        } else if (attributeName.toLowerCase().contains("list") || attributeName.toLowerCase().contains("array")) {
            return "collection of related items";
        } else if (attributeName.toLowerCase().contains("map") || attributeName.toLowerCase().contains("cache")) {
            return "mapping or cache for efficient lookups";
        } else {
            return "the " + attributeName + " attribute";
        }
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}