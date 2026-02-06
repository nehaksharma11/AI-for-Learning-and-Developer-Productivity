package com.ailearning.lsp.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.URI;

/**
 * Text Document Service implementation for AI Learning Companion.
 * Handles document lifecycle, hover, completion, and other text-based operations.
 */
public class AILearningTextDocumentService implements TextDocumentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AILearningTextDocumentService.class);
    
    private final Map<String, TextDocumentItem> documents = new ConcurrentHashMap<>();
    private LanguageClient client;
    private ClientCapabilities clientCapabilities;
    private boolean initialized = false;

    /**
     * Initialize the service with client capabilities
     */
    public void initialize(ClientCapabilities capabilities) {
        this.clientCapabilities = capabilities;
        logger.info("Text document service initialized with client capabilities");
    }

    /**
     * Mark the service as fully initialized
     */
    public void initialized() {
        this.initialized = true;
        logger.info("Text document service initialization complete");
    }

    /**
     * Connect to the language client
     */
    public void connect(LanguageClient client) {
        this.client = client;
        logger.info("Text document service connected to client");
    }

    /**
     * Check if the service is ready to handle requests
     */
    public boolean isReady() {
        return initialized && client != null;
    }

    /**
     * Shutdown the service
     */
    public void shutdown() {
        documents.clear();
        logger.info("Text document service shutdown complete");
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        TextDocumentItem document = params.getTextDocument();
        documents.put(document.getUri(), document);
        
        logger.info("Document opened: {} (language: {}, version: {})", 
                document.getUri(), document.getLanguageId(), document.getVersion());
        
        // Trigger initial analysis
        analyzeDocument(document);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        TextDocumentItem document = documents.get(uri);
        
        if (document != null) {
            // Apply changes to document content
            String newContent = applyChanges(document.getText(), params.getContentChanges());
            
            // Update document
            TextDocumentItem updatedDocument = new TextDocumentItem(
                document.getUri(),
                document.getLanguageId(),
                params.getTextDocument().getVersion(),
                newContent
            );
            documents.put(uri, updatedDocument);
            
            logger.debug("Document changed: {} (version: {})", uri, params.getTextDocument().getVersion());
            
            // Trigger incremental analysis
            analyzeDocument(updatedDocument);
        } else {
            logger.warn("Received change for unknown document: {}", uri);
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        documents.remove(uri);
        
        logger.info("Document closed: {}", uri);
        
        // Clear diagnostics for closed document
        if (client != null) {
            PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
            diagnostics.setUri(uri);
            diagnostics.setDiagnostics(new ArrayList<>());
            client.publishDiagnostics(diagnostics);
        }
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        logger.info("Document saved: {}", uri);
        
        // Trigger comprehensive analysis on save
        TextDocumentItem document = documents.get(uri);
        if (document != null) {
            analyzeDocument(document);
        }
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();
        
        logger.debug("Hover request for {} at {}:{}", uri, position.getLine(), position.getCharacter());
        
        return CompletableFuture.supplyAsync(() -> {
            TextDocumentItem document = documents.get(uri);
            if (document == null) {
                return null;
            }
            
            // Generate hover information
            String hoverText = generateHoverContent(document, position);
            if (hoverText != null) {
                MarkupContent content = new MarkupContent();
                content.setKind(MarkupKind.MARKDOWN);
                content.setValue(hoverText);
                
                return new Hover(content);
            }
            
            return null;
        });
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();
        
        logger.debug("Completion request for {} at {}:{}", uri, position.getLine(), position.getCharacter());
        
        return CompletableFuture.supplyAsync(() -> {
            TextDocumentItem document = documents.get(uri);
            if (document == null) {
                return Either.forRight(new CompletionList(false, new ArrayList<>()));
            }
            
            // Generate completion items
            List<CompletionItem> items = generateCompletionItems(document, position);
            
            CompletionList completionList = new CompletionList();
            completionList.setIsIncomplete(false);
            completionList.setItems(items);
            
            return Either.forRight(completionList);
        });
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        return CompletableFuture.supplyAsync(() -> {
            // Add detailed information to completion item
            if (unresolved.getDocumentation() == null) {
                MarkupContent documentation = new MarkupContent();
                documentation.setKind(MarkupKind.MARKDOWN);
                documentation.setValue("AI Learning Companion suggestion: " + unresolved.getLabel());
                unresolved.setDocumentation(documentation);
            }
            
            return unresolved;
        });
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        String uri = params.getTextDocument().getUri();
        Range range = params.getRange();
        
        logger.debug("Code action request for {} at range {}:{} - {}:{}", 
                uri, range.getStart().getLine(), range.getStart().getCharacter(),
                range.getEnd().getLine(), range.getEnd().getCharacter());
        
        return CompletableFuture.supplyAsync(() -> {
            List<Either<Command, CodeAction>> actions = new ArrayList<>();
            
            // Generate code actions based on context
            TextDocumentItem document = documents.get(uri);
            if (document != null) {
                actions.addAll(generateCodeActions(document, range, params.getContext()));
            }
            
            return actions;
        });
    }

    /**
     * Apply text changes to document content
     */
    private String applyChanges(String content, List<TextDocumentContentChangeEvent> changes) {
        String result = content;
        
        for (TextDocumentContentChangeEvent change : changes) {
            if (change.getRange() == null) {
                // Full document change
                result = change.getText();
            } else {
                // Incremental change
                result = applyIncrementalChange(result, change);
            }
        }
        
        return result;
    }

    /**
     * Apply an incremental change to document content
     */
    private String applyIncrementalChange(String content, TextDocumentContentChangeEvent change) {
        String[] lines = content.split("\n", -1);
        Range range = change.getRange();
        
        int startLine = range.getStart().getLine();
        int startChar = range.getStart().getCharacter();
        int endLine = range.getEnd().getLine();
        int endChar = range.getEnd().getCharacter();
        
        if (startLine == endLine) {
            // Single line change
            String line = lines[startLine];
            String newLine = line.substring(0, startChar) + change.getText() + line.substring(endChar);
            lines[startLine] = newLine;
        } else {
            // Multi-line change
            String startLineContent = lines[startLine].substring(0, startChar);
            String endLineContent = lines[endLine].substring(endChar);
            String newContent = startLineContent + change.getText() + endLineContent;
            
            // Replace the range with new content
            String[] newLines = newContent.split("\n", -1);
            String[] result = new String[lines.length - (endLine - startLine) + newLines.length - 1];
            
            System.arraycopy(lines, 0, result, 0, startLine);
            System.arraycopy(newLines, 0, result, startLine, newLines.length);
            System.arraycopy(lines, endLine + 1, result, startLine + newLines.length, 
                    lines.length - endLine - 1);
            
            lines = result;
        }
        
        return String.join("\n", lines);
    }

    /**
     * Analyze document and publish diagnostics
     */
    private void analyzeDocument(TextDocumentItem document) {
        if (client == null) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                // Perform document analysis
                List<Diagnostic> diagnostics = performAnalysis(document);
                
                // Publish diagnostics
                PublishDiagnosticsParams params = new PublishDiagnosticsParams();
                params.setUri(document.getUri());
                params.setDiagnostics(diagnostics);
                
                client.publishDiagnostics(params);
                
                logger.debug("Published {} diagnostics for {}", diagnostics.size(), document.getUri());
            } catch (Exception e) {
                logger.error("Error analyzing document: " + document.getUri(), e);
            }
        });
    }

    /**
     * Perform analysis on document and return diagnostics
     */
    private List<Diagnostic> performAnalysis(TextDocumentItem document) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        
        // Basic syntax checking (placeholder implementation)
        String[] lines = document.getText().split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Example: Check for TODO comments
            if (line.contains("TODO")) {
                Diagnostic diagnostic = new Diagnostic();
                diagnostic.setRange(new Range(
                    new Position(i, line.indexOf("TODO")),
                    new Position(i, line.indexOf("TODO") + 4)
                ));
                diagnostic.setSeverity(DiagnosticSeverity.Information);
                diagnostic.setMessage("TODO comment found - consider addressing this item");
                diagnostic.setSource("AI Learning Companion");
                
                diagnostics.add(diagnostic);
            }
        }
        
        return diagnostics;
    }

    /**
     * Generate hover content for position in document
     */
    private String generateHoverContent(TextDocumentItem document, Position position) {
        try {
            String[] lines = document.getText().split("\n");
            if (position.getLine() >= lines.length) {
                return null;
            }
            
            String line = lines[position.getLine()];
            if (position.getCharacter() >= line.length()) {
                return null;
            }
            
            // Extract word at position
            String word = extractWordAtPosition(line, position.getCharacter());
            if (word.isEmpty()) {
                return null;
            }
            
            // Generate contextual explanation based on language and context
            String language = document.getLanguageId();
            return generateContextualExplanation(word, line, language, document);
            
        } catch (Exception e) {
            logger.error("Error generating hover content", e);
            return "**AI Learning Companion**\n\nError generating explanation";
        }
    }

    /**
     * Extract word at specific character position
     */
    private String extractWordAtPosition(String line, int character) {
        if (character >= line.length()) return "";
        
        // Find word boundaries
        int start = character;
        int end = character;
        
        // Move start backward to find word start
        while (start > 0 && Character.isJavaIdentifierPart(line.charAt(start - 1))) {
            start--;
        }
        
        // Move end forward to find word end
        while (end < line.length() && Character.isJavaIdentifierPart(line.charAt(end))) {
            end++;
        }
        
        return start < end ? line.substring(start, end) : "";
    }

    /**
     * Generate contextual explanation for code element
     */
    private String generateContextualExplanation(String word, String line, String language, TextDocumentItem document) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("**AI Learning Companion - Code Explanation**\n\n");
        
        // Analyze the word in context
        if (isKeyword(word, language)) {
            explanation.append("🔑 **Keyword**: `").append(word).append("`\n\n");
            explanation.append(getKeywordExplanation(word, language));
        } else if (isBuiltInFunction(word, language)) {
            explanation.append("⚙️ **Built-in Function**: `").append(word).append("`\n\n");
            explanation.append(getBuiltInFunctionExplanation(word, language));
        } else if (isVariableOrMethod(word, line)) {
            explanation.append("📝 **Variable/Method**: `").append(word).append("`\n\n");
            explanation.append(getVariableMethodExplanation(word, line, document));
        } else {
            explanation.append("💡 **Code Element**: `").append(word).append("`\n\n");
            explanation.append("This appears to be a user-defined identifier. ");
            explanation.append("Consider adding documentation to help others understand its purpose.");
        }
        
        // Add learning suggestions
        explanation.append("\n\n---\n");
        explanation.append("💡 **Learning Tip**: ");
        explanation.append(generateLearningTip(word, language));
        
        return explanation.toString();
    }

    /**
     * Check if word is a language keyword
     */
    private boolean isKeyword(String word, String language) {
        switch (language.toLowerCase()) {
            case "java":
                return List.of("public", "private", "protected", "static", "final", "abstract", 
                              "class", "interface", "extends", "implements", "if", "else", "for", 
                              "while", "do", "switch", "case", "default", "try", "catch", "finally",
                              "throw", "throws", "return", "void", "int", "String", "boolean").contains(word);
            case "javascript":
            case "typescript":
                return List.of("function", "var", "let", "const", "if", "else", "for", "while", 
                              "do", "switch", "case", "default", "try", "catch", "finally", 
                              "return", "class", "extends", "import", "export").contains(word);
            case "python":
                return List.of("def", "class", "if", "elif", "else", "for", "while", "try", 
                              "except", "finally", "return", "import", "from", "as", "with", 
                              "lambda", "yield", "async", "await").contains(word);
            default:
                return false;
        }
    }

    /**
     * Check if word is a built-in function
     */
    private boolean isBuiltInFunction(String word, String language) {
        switch (language.toLowerCase()) {
            case "java":
                return List.of("println", "print", "length", "size", "isEmpty", "contains", 
                              "equals", "hashCode", "toString").contains(word);
            case "javascript":
            case "typescript":
                return List.of("console", "log", "length", "push", "pop", "slice", "splice", 
                              "map", "filter", "reduce", "forEach").contains(word);
            case "python":
                return List.of("print", "len", "range", "enumerate", "zip", "map", "filter", 
                              "sorted", "reversed", "sum", "max", "min").contains(word);
            default:
                return false;
        }
    }

    /**
     * Check if word appears to be a variable or method
     */
    private boolean isVariableOrMethod(String word, String line) {
        return line.contains(word + "(") || line.contains(word + " =") || 
               line.contains("= " + word) || line.contains(word + ".");
    }

    /**
     * Get explanation for language keyword
     */
    private String getKeywordExplanation(String keyword, String language) {
        // This would typically be loaded from a knowledge base
        switch (keyword.toLowerCase()) {
            case "public":
                return "Access modifier that makes a class, method, or field accessible from anywhere in the program.";
            case "private":
                return "Access modifier that restricts access to within the same class only.";
            case "static":
                return "Modifier that belongs to the class rather than any instance. Can be called without creating an object.";
            case "final":
                return "Modifier that prevents modification. For variables: constant value. For methods: cannot be overridden. For classes: cannot be extended.";
            case "class":
                return "Blueprint for creating objects. Defines properties (fields) and behaviors (methods) that objects will have.";
            case "if":
                return "Conditional statement that executes code block only if the specified condition is true.";
            case "for":
                return "Loop statement that repeats code block for a specified number of iterations or over a collection.";
            default:
                return "Language keyword with specific meaning in " + language + ". Check language documentation for details.";
        }
    }

    /**
     * Get explanation for built-in function
     */
    private String getBuiltInFunctionExplanation(String function, String language) {
        switch (function.toLowerCase()) {
            case "println":
                return "Prints text to console followed by a newline character. Useful for debugging and output.";
            case "length":
                return "Returns the number of elements in a collection or characters in a string.";
            case "equals":
                return "Compares two objects for equality. Returns true if objects are equal, false otherwise.";
            case "console":
                return "Global object providing access to browser's debugging console. Use console.log() for output.";
            case "print":
                return "Outputs text to console or standard output. Essential for debugging and user feedback.";
            default:
                return "Built-in function provided by " + language + ". Check documentation for parameters and return value.";
        }
    }

    /**
     * Get explanation for variable or method
     */
    private String getVariableMethodExplanation(String identifier, String line, TextDocumentItem document) {
        StringBuilder explanation = new StringBuilder();
        
        if (line.contains(identifier + "(")) {
            explanation.append("This appears to be a method call. ");
            explanation.append("Methods encapsulate behavior and can accept parameters and return values.");
        } else if (line.contains(identifier + " =") || line.contains("= " + identifier)) {
            explanation.append("This appears to be a variable assignment. ");
            explanation.append("Variables store data that can be used and modified throughout your program.");
        } else {
            explanation.append("This appears to be a variable or method reference. ");
        }
        
        explanation.append("\n\n**Best Practice**: Use descriptive names that clearly indicate the purpose or content.");
        
        return explanation.toString();
    }

    /**
     * Generate learning tip based on context
     */
    private String generateLearningTip(String word, String language) {
        List<String> tips = List.of(
            "Try explaining this code element to someone else - teaching helps reinforce understanding.",
            "Consider writing a unit test for this functionality to better understand its behavior.",
            "Look up the official documentation to learn about additional features and best practices.",
            "Experiment with this in a small test program to see how it works in different contexts.",
            "Think about edge cases - what happens with null values, empty inputs, or boundary conditions?",
            "Consider the performance implications - is this the most efficient approach for your use case?",
            "Review code examples from open source projects to see how others use this pattern."
        );
        
        return tips.get(Math.abs(word.hashCode()) % tips.size());
    }

    /**
     * Generate completion items for position in document
     */
    private List<CompletionItem> generateCompletionItems(TextDocumentItem document, Position position) {
        List<CompletionItem> items = new ArrayList<>();
        
        try {
            String[] lines = document.getText().split("\n");
            if (position.getLine() >= lines.length) {
                return items;
            }
            
            String line = lines[position.getLine()];
            String linePrefix = position.getCharacter() <= line.length() ? 
                line.substring(0, position.getCharacter()) : line;
            
            String language = document.getLanguageId();
            
            // Generate context-aware completions
            items.addAll(generateLanguageSpecificCompletions(language, linePrefix, document));
            items.addAll(generateLearningCompletions(linePrefix, language));
            items.addAll(generatePatternCompletions(linePrefix, language));
            
        } catch (Exception e) {
            logger.error("Error generating completion items", e);
        }
        
        return items;
    }

    /**
     * Generate language-specific completion items
     */
    private List<CompletionItem> generateLanguageSpecificCompletions(String language, String linePrefix, TextDocumentItem document) {
        List<CompletionItem> items = new ArrayList<>();
        
        switch (language.toLowerCase()) {
            case "java":
                items.addAll(generateJavaCompletions(linePrefix, document));
                break;
            case "javascript":
            case "typescript":
                items.addAll(generateJavaScriptCompletions(linePrefix, document));
                break;
            case "python":
                items.addAll(generatePythonCompletions(linePrefix, document));
                break;
        }
        
        return items;
    }

    /**
     * Generate Java-specific completions
     */
    private List<CompletionItem> generateJavaCompletions(String linePrefix, TextDocumentItem document) {
        List<CompletionItem> items = new ArrayList<>();
        
        // Common Java patterns
        if (linePrefix.trim().isEmpty() || linePrefix.endsWith(" ")) {
            // Method declarations
            CompletionItem publicMethod = createCompletionItem(
                "public method", 
                "public ${1:returnType} ${2:methodName}(${3:parameters}) {\n    ${4:// implementation}\n}",
                CompletionItemKind.Snippet,
                "Create a public method with proper structure"
            );
            items.add(publicMethod);
            
            // Class declaration
            CompletionItem classDecl = createCompletionItem(
                "class declaration",
                "public class ${1:ClassName} {\n    ${2:// class body}\n}",
                CompletionItemKind.Snippet,
                "Create a new class with proper structure"
            );
            items.add(classDecl);
        }
        
        // Exception handling
        if (linePrefix.contains("try") || linePrefix.trim().startsWith("try")) {
            CompletionItem tryCatch = createCompletionItem(
                "try-catch block",
                "try {\n    ${1:// code that might throw exception}\n} catch (${2:Exception} e) {\n    ${3:// handle exception}\n}",
                CompletionItemKind.Snippet,
                "Proper exception handling pattern"
            );
            items.add(tryCatch);
        }
        
        // Logging
        if (linePrefix.contains("log") || linePrefix.contains("Log")) {
            CompletionItem logger = createCompletionItem(
                "logger declaration",
                "private static final Logger logger = LoggerFactory.getLogger(${1:ClassName}.class);",
                CompletionItemKind.Snippet,
                "Standard logger declaration using SLF4J"
            );
            items.add(logger);
        }
        
        return items;
    }

    /**
     * Generate JavaScript/TypeScript completions
     */
    private List<CompletionItem> generateJavaScriptCompletions(String linePrefix, TextDocumentItem document) {
        List<CompletionItem> items = new ArrayList<>();
        
        // Function declarations
        if (linePrefix.trim().isEmpty() || linePrefix.endsWith(" ")) {
            CompletionItem arrowFunction = createCompletionItem(
                "arrow function",
                "const ${1:functionName} = (${2:parameters}) => {\n    ${3:// implementation}\n};",
                CompletionItemKind.Snippet,
                "Modern arrow function syntax"
            );
            items.add(arrowFunction);
            
            CompletionItem asyncFunction = createCompletionItem(
                "async function",
                "async function ${1:functionName}(${2:parameters}) {\n    ${3:// async implementation}\n}",
                CompletionItemKind.Snippet,
                "Asynchronous function for handling promises"
            );
            items.add(asyncFunction);
        }
        
        // Promise handling
        if (linePrefix.contains("await") || linePrefix.contains("async")) {
            CompletionItem tryAwait = createCompletionItem(
                "try-await pattern",
                "try {\n    const result = await ${1:asyncOperation};\n    ${2:// handle success}\n} catch (error) {\n    ${3:// handle error}\n}",
                CompletionItemKind.Snippet,
                "Proper async/await error handling"
            );
            items.add(tryAwait);
        }
        
        return items;
    }

    /**
     * Generate Python completions
     */
    private List<CompletionItem> generatePythonCompletions(String linePrefix, TextDocumentItem document) {
        List<CompletionItem> items = new ArrayList<>();
        
        // Function definitions
        if (linePrefix.trim().startsWith("def") || linePrefix.trim().isEmpty()) {
            CompletionItem function = createCompletionItem(
                "function definition",
                "def ${1:function_name}(${2:parameters}):\n    \"\"\"${3:Function description}\"\"\"\n    ${4:pass}",
                CompletionItemKind.Snippet,
                "Function with docstring following PEP conventions"
            );
            items.add(function);
        }
        
        // Class definitions
        if (linePrefix.trim().startsWith("class") || linePrefix.trim().isEmpty()) {
            CompletionItem classDecl = createCompletionItem(
                "class definition",
                "class ${1:ClassName}:\n    \"\"\"${2:Class description}\"\"\"\n    \n    def __init__(self, ${3:parameters}):\n        ${4:pass}",
                CompletionItemKind.Snippet,
                "Class with constructor following Python conventions"
            );
            items.add(classDecl);
        }
        
        return items;
    }

    /**
     * Generate learning-focused completions
     */
    private List<CompletionItem> generateLearningCompletions(String linePrefix, String language) {
        List<CompletionItem> items = new ArrayList<>();
        
        // Documentation templates
        CompletionItem docComment = createCompletionItem(
            "documentation comment",
            generateDocumentationTemplate(language),
            CompletionItemKind.Snippet,
            "Add comprehensive documentation to improve code understanding"
        );
        items.add(docComment);
        
        // TODO with learning context
        CompletionItem learningTodo = createCompletionItem(
            "learning TODO",
            "// TODO: ${1:Describe what needs to be done}\n// LEARN: ${2:What concept should be researched?}\n// WHY: ${3:Why is this important for the project?}",
            CompletionItemKind.Snippet,
            "Structured TODO that promotes learning and understanding"
        );
        items.add(learningTodo);
        
        return items;
    }

    /**
     * Generate pattern-based completions
     */
    private List<CompletionItem> generatePatternCompletions(String linePrefix, String language) {
        List<CompletionItem> items = new ArrayList<>();
        
        // Design patterns
        if (linePrefix.contains("pattern") || linePrefix.contains("Pattern")) {
            CompletionItem singleton = createCompletionItem(
                "singleton pattern",
                generateSingletonPattern(language),
                CompletionItemKind.Snippet,
                "Thread-safe singleton implementation"
            );
            items.add(singleton);
        }
        
        // Testing patterns
        if (linePrefix.contains("test") || linePrefix.contains("Test")) {
            CompletionItem testMethod = createCompletionItem(
                "test method",
                generateTestMethodTemplate(language),
                CompletionItemKind.Snippet,
                "Well-structured test method following AAA pattern"
            );
            items.add(testMethod);
        }
        
        return items;
    }

    /**
     * Create a completion item with learning context
     */
    private CompletionItem createCompletionItem(String label, String insertText, CompletionItemKind kind, String detail) {
        CompletionItem item = new CompletionItem(label);
        item.setKind(kind);
        item.setDetail("AI Learning Companion: " + detail);
        item.setInsertText(insertText);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        
        // Add documentation
        MarkupContent documentation = new MarkupContent();
        documentation.setKind(MarkupKind.MARKDOWN);
        documentation.setValue("**AI Learning Companion Suggestion**\n\n" + detail + 
                              "\n\n*This suggestion is designed to help you learn best practices and improve code quality.*");
        item.setDocumentation(documentation);
        
        return item;
    }

    /**
     * Generate documentation template for language
     */
    private String generateDocumentationTemplate(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "/**\n * ${1:Brief description of the method/class}\n * \n * @param ${2:paramName} ${3:parameter description}\n * @return ${4:return value description}\n * @throws ${5:ExceptionType} ${6:when this exception is thrown}\n */";
            case "javascript":
            case "typescript":
                return "/**\n * ${1:Brief description of the function}\n * @param {${2:type}} ${3:paramName} - ${4:parameter description}\n * @returns {${5:returnType}} ${6:return value description}\n */";
            case "python":
                return "\"\"\"\n${1:Brief description of the function/class}\n\nArgs:\n    ${2:param_name} (${3:type}): ${4:parameter description}\n\nReturns:\n    ${5:return_type}: ${6:return value description}\n\nRaises:\n    ${7:ExceptionType}: ${8:when this exception is raised}\n\"\"\"";
            default:
                return "// ${1:Add description of what this code does and why}";
        }
    }

    /**
     * Generate singleton pattern for language
     */
    private String generateSingletonPattern(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "public class ${1:Singleton} {\n    private static volatile ${1:Singleton} instance;\n    \n    private ${1:Singleton}() {\n        // Private constructor\n    }\n    \n    public static ${1:Singleton} getInstance() {\n        if (instance == null) {\n            synchronized (${1:Singleton}.class) {\n                if (instance == null) {\n                    instance = new ${1:Singleton}();\n                }\n            }\n        }\n        return instance;\n    }\n}";
            case "javascript":
            case "typescript":
                return "class ${1:Singleton} {\n    constructor() {\n        if (${1:Singleton}.instance) {\n            return ${1:Singleton}.instance;\n        }\n        ${1:Singleton}.instance = this;\n    }\n    \n    static getInstance() {\n        return new ${1:Singleton}();\n    }\n}";
            default:
                return "// Singleton pattern implementation for ${1:ClassName}";
        }
    }

    /**
     * Generate test method template for language
     */
    private String generateTestMethodTemplate(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return "@Test\npublic void ${1:testMethodName}() {\n    // Arrange\n    ${2:// Set up test data and conditions}\n    \n    // Act\n    ${3:// Execute the method being tested}\n    \n    // Assert\n    ${4:// Verify the expected outcome}\n}";
            case "javascript":
            case "typescript":
                return "test('${1:should describe expected behavior}', () => {\n    // Arrange\n    ${2:// Set up test data and conditions}\n    \n    // Act\n    ${3:// Execute the function being tested}\n    \n    // Assert\n    ${4:// Verify the expected outcome}\n});";
            case "python":
                return "def test_${1:method_name}(self):\n    \"\"\"Test ${2:description of what is being tested}\"\"\"\n    # Arrange\n    ${3:# Set up test data and conditions}\n    \n    # Act\n    ${4:# Execute the method being tested}\n    \n    # Assert\n    ${5:# Verify the expected outcome}";
            default:
                return "// Test: ${1:describe what this test validates}";
        }
    }

    /**
     * Generate code actions for range in document
     */
    private List<Either<Command, CodeAction>> generateCodeActions(TextDocumentItem document, 
                                                                Range range, 
                                                                CodeActionContext context) {
        List<Either<Command, CodeAction>> actions = new ArrayList<>();
        
        try {
            String selectedText = extractTextFromRange(document.getText(), range);
            String language = document.getLanguageId();
            
            // Learning-focused code actions
            actions.addAll(generateLearningActions(document.getUri(), range, selectedText, language));
            
            // Code improvement actions
            actions.addAll(generateImprovementActions(document.getUri(), range, selectedText, language));
            
            // Documentation actions
            actions.addAll(generateDocumentationActions(document.getUri(), range, selectedText, language));
            
            // Refactoring actions
            actions.addAll(generateRefactoringActions(document.getUri(), range, selectedText, language));
            
        } catch (Exception e) {
            logger.error("Error generating code actions", e);
        }
        
        return actions;
    }

    /**
     * Extract text from document range
     */
    private String extractTextFromRange(String documentText, Range range) {
        String[] lines = documentText.split("\n");
        
        int startLine = range.getStart().getLine();
        int endLine = range.getEnd().getLine();
        int startChar = range.getStart().getCharacter();
        int endChar = range.getEnd().getCharacter();
        
        if (startLine == endLine) {
            // Single line selection
            if (startLine < lines.length) {
                String line = lines[startLine];
                return line.substring(
                    Math.min(startChar, line.length()), 
                    Math.min(endChar, line.length())
                );
            }
        } else {
            // Multi-line selection
            StringBuilder result = new StringBuilder();
            for (int i = startLine; i <= endLine && i < lines.length; i++) {
                String line = lines[i];
                if (i == startLine) {
                    result.append(line.substring(Math.min(startChar, line.length())));
                } else if (i == endLine) {
                    result.append("\n").append(line.substring(0, Math.min(endChar, line.length())));
                } else {
                    result.append("\n").append(line);
                }
            }
            return result.toString();
        }
        
        return "";
    }

    /**
     * Generate learning-focused code actions
     */
    private List<Either<Command, CodeAction>> generateLearningActions(String uri, Range range, String selectedText, String language) {
        List<Either<Command, CodeAction>> actions = new ArrayList<>();
        
        // Explain code action
        CodeAction explainAction = new CodeAction("🧠 Explain this code");
        explainAction.setKind(CodeActionKind.Source);
        Command explainCommand = new Command();
        explainCommand.setTitle("Explain selected code");
        explainCommand.setCommand("ailearning.explainCode");
        explainCommand.setArguments(List.of(uri, range, selectedText, language));
        explainAction.setCommand(explainCommand);
        actions.add(Either.forRight(explainAction));
        
        // Generate examples action
        CodeAction examplesAction = new CodeAction("📚 Show similar examples");
        examplesAction.setKind(CodeActionKind.Source);
        Command examplesCommand = new Command();
        examplesCommand.setTitle("Find similar code examples");
        examplesCommand.setCommand("ailearning.showExamples");
        examplesCommand.setArguments(List.of(uri, range, selectedText, language));
        examplesAction.setCommand(examplesCommand);
        actions.add(Either.forRight(examplesAction));
        
        // Learning path action
        if (!selectedText.trim().isEmpty()) {
            CodeAction learningPathAction = new CodeAction("🎯 Create learning path");
            learningPathAction.setKind(CodeActionKind.Source);
            Command learningPathCommand = new Command();
            learningPathCommand.setTitle("Generate learning path for this concept");
            learningPathCommand.setCommand("ailearning.createLearningPath");
            learningPathCommand.setArguments(List.of(uri, selectedText, language));
            learningPathAction.setCommand(learningPathCommand);
            actions.add(Either.forRight(learningPathAction));
        }
        
        return actions;
    }

    /**
     * Generate code improvement actions
     */
    private List<Either<Command, CodeAction>> generateImprovementActions(String uri, Range range, String selectedText, String language) {
        List<Either<Command, CodeAction>> actions = new ArrayList<>();
        
        // Suggest improvements action
        CodeAction improveAction = new CodeAction("⚡ Suggest improvements");
        improveAction.setKind(CodeActionKind.QuickFix);
        Command improveCommand = new Command();
        improveCommand.setTitle("Analyze and suggest code improvements");
        improveCommand.setCommand("ailearning.suggestImprovements");
        improveCommand.setArguments(List.of(uri, range, selectedText, language));
        improveAction.setCommand(improveCommand);
        actions.add(Either.forRight(improveAction));
        
        // Performance analysis action
        if (selectedText.contains("for") || selectedText.contains("while") || 
            selectedText.contains("forEach") || selectedText.contains("map")) {
            CodeAction performanceAction = new CodeAction("🚀 Analyze performance");
            performanceAction.setKind(CodeActionKind.Source);
            Command performanceCommand = new Command();
            performanceCommand.setTitle("Analyze performance characteristics");
            performanceCommand.setCommand("ailearning.analyzePerformance");
            performanceCommand.setArguments(List.of(uri, range, selectedText, language));
            performanceAction.setCommand(performanceCommand);
            actions.add(Either.forRight(performanceAction));
        }
        
        // Security analysis action
        if (containsSecurityRelevantCode(selectedText)) {
            CodeAction securityAction = new CodeAction("🔒 Security analysis");
            securityAction.setKind(CodeActionKind.Source);
            Command securityCommand = new Command();
            securityCommand.setTitle("Analyze for security vulnerabilities");
            securityCommand.setCommand("ailearning.analyzeSecurity");
            securityCommand.setArguments(List.of(uri, range, selectedText, language));
            securityAction.setCommand(securityCommand);
            actions.add(Either.forRight(securityAction));
        }
        
        return actions;
    }

    /**
     * Generate documentation actions
     */
    private List<Either<Command, CodeAction>> generateDocumentationActions(String uri, Range range, String selectedText, String language) {
        List<Either<Command, CodeAction>> actions = new ArrayList<>();
        
        // Generate documentation action
        if (isMethodOrFunction(selectedText, language)) {
            CodeAction docAction = new CodeAction("📝 Generate documentation");
            docAction.setKind(CodeActionKind.Source);
            Command docCommand = new Command();
            docCommand.setTitle("Generate comprehensive documentation");
            docCommand.setCommand("ailearning.generateDocumentation");
            docCommand.setArguments(List.of(uri, range, selectedText, language));
            docAction.setCommand(docCommand);
            actions.add(Either.forRight(docAction));
        }
        
        // Add inline comments action
        CodeAction commentAction = new CodeAction("💬 Add explanatory comments");
        commentAction.setKind(CodeActionKind.Source);
        Command commentCommand = new Command();
        commentCommand.setTitle("Add inline comments explaining the code");
        commentCommand.setCommand("ailearning.addComments");
        commentCommand.setArguments(List.of(uri, range, selectedText, language));
        commentAction.setCommand(commentCommand);
        actions.add(Either.forRight(commentAction));
        
        return actions;
    }

    /**
     * Generate refactoring actions
     */
    private List<Either<Command, CodeAction>> generateRefactoringActions(String uri, Range range, String selectedText, String language) {
        List<Either<Command, CodeAction>> actions = new ArrayList<>();
        
        // Extract method action
        if (selectedText.split("\n").length > 3) {
            CodeAction extractAction = new CodeAction("🔧 Extract method");
            extractAction.setKind(CodeActionKind.Refactor);
            Command extractCommand = new Command();
            extractCommand.setTitle("Extract selected code into a method");
            extractCommand.setCommand("ailearning.extractMethod");
            extractCommand.setArguments(List.of(uri, range, selectedText, language));
            extractAction.setCommand(extractCommand);
            actions.add(Either.forRight(extractAction));
        }
        
        // Simplify code action
        if (isComplexExpression(selectedText)) {
            CodeAction simplifyAction = new CodeAction("🎯 Simplify expression");
            simplifyAction.setKind(CodeActionKind.Refactor);
            Command simplifyCommand = new Command();
            simplifyCommand.setTitle("Simplify complex expression");
            simplifyCommand.setCommand("ailearning.simplifyCode");
            simplifyCommand.setArguments(List.of(uri, range, selectedText, language));
            simplifyAction.setCommand(simplifyCommand);
            actions.add(Either.forRight(simplifyAction));
        }
        
        return actions;
    }

    /**
     * Check if text contains security-relevant code
     */
    private boolean containsSecurityRelevantCode(String text) {
        String lowerText = text.toLowerCase();
        return lowerText.contains("password") || lowerText.contains("token") || 
               lowerText.contains("secret") || lowerText.contains("key") ||
               lowerText.contains("sql") || lowerText.contains("query") ||
               lowerText.contains("input") || lowerText.contains("user") ||
               lowerText.contains("request") || lowerText.contains("response");
    }

    /**
     * Check if text represents a method or function
     */
    private boolean isMethodOrFunction(String text, String language) {
        switch (language.toLowerCase()) {
            case "java":
                return text.matches(".*\\b(public|private|protected)\\s+.*\\s+\\w+\\s*\\([^)]*\\).*");
            case "javascript":
            case "typescript":
                return text.contains("function") || text.matches(".*\\w+\\s*\\([^)]*\\)\\s*=>.*") ||
                       text.matches(".*\\w+\\s*\\([^)]*\\)\\s*\\{.*");
            case "python":
                return text.trim().startsWith("def ");
            default:
                return text.contains("(") && text.contains(")");
        }
    }

    /**
     * Check if text represents a complex expression
     */
    private boolean isComplexExpression(String text) {
        // Count operators and nesting levels
        long operatorCount = text.chars().filter(ch -> "+-*/%&|^<>=!".indexOf(ch) >= 0).count();
        long parenthesesCount = text.chars().filter(ch -> ch == '(' || ch == ')').count();
        
        return operatorCount > 3 || parenthesesCount > 4 || text.length() > 100;
    }

    /**
     * Get document by URI
     */
    public TextDocumentItem getDocument(String uri) {
        return documents.get(uri);
    }

    /**
     * Get all open documents
     */
    public Map<String, TextDocumentItem> getAllDocuments() {
        return new ConcurrentHashMap<>(documents);
    }
}