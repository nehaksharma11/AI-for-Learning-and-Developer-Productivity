package com.ailearning.lsp.server;

import org.eclipse.lsp4j.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for custom AI Learning features in the LSP server.
 */
class CustomLearningFeaturesTest {

    @Mock
    private org.eclipse.lsp4j.services.LanguageClient mockClient;

    private AILearningTextDocumentService textDocumentService;
    private AILearningWorkspaceService workspaceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        textDocumentService = new AILearningTextDocumentService();
        workspaceService = new AILearningWorkspaceService();
        
        // Initialize services
        ClientCapabilities capabilities = new ClientCapabilities();
        textDocumentService.initialize(capabilities);
        textDocumentService.initialized();
        textDocumentService.connect(mockClient);
        
        workspaceService.initialize(capabilities);
        workspaceService.initialized();
        workspaceService.connect(mockClient);
    }

    @Test
    @DisplayName("Should provide contextual hover information")
    void testContextualHover() throws ExecutionException, InterruptedException {
        // Arrange
        TextDocumentItem document = new TextDocumentItem(
            "file:///test.java",
            "java",
            1,
            "public class Test {\n    public static void main(String[] args) {\n        System.out.println(\"Hello\");\n    }\n}"
        );
        
        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(document);
        textDocumentService.didOpen(openParams);
        
        HoverParams hoverParams = new HoverParams();
        hoverParams.setTextDocument(new TextDocumentIdentifier("file:///test.java"));
        hoverParams.setPosition(new Position(0, 7)); // Position on "class"
        
        // Act
        CompletableFuture<Hover> hoverFuture = textDocumentService.hover(hoverParams);
        Hover hover = hoverFuture.get();
        
        // Assert
        assertNotNull(hover);
        assertNotNull(hover.getContents());
        assertTrue(hover.getContents().getRight().getValue().contains("AI Learning Companion"));
    }

    @Test
    @DisplayName("Should provide intelligent completion suggestions")
    void testIntelligentCompletion() throws ExecutionException, InterruptedException {
        // Arrange
        TextDocumentItem document = new TextDocumentItem(
            "file:///test.java",
            "java",
            1,
            "public class Test {\n    public \n}"
        );
        
        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(document);
        textDocumentService.didOpen(openParams);
        
        CompletionParams completionParams = new CompletionParams();
        completionParams.setTextDocument(new TextDocumentIdentifier("file:///test.java"));
        completionParams.setPosition(new Position(1, 11)); // After "public "
        
        // Act
        CompletableFuture<Either<List<CompletionItem>, CompletionList>> completionFuture = 
            textDocumentService.completion(completionParams);
        Either<List<CompletionItem>, CompletionList> completion = completionFuture.get();
        
        // Assert
        assertNotNull(completion);
        assertTrue(completion.isRight());
        CompletionList completionList = completion.getRight();
        assertFalse(completionList.getItems().isEmpty());
        
        // Check that we have learning-focused completions
        boolean hasLearningCompletion = completionList.getItems().stream()
            .anyMatch(item -> item.getDetail() != null && 
                     item.getDetail().contains("AI Learning Companion"));
        assertTrue(hasLearningCompletion);
    }

    @Test
    @DisplayName("Should provide learning-focused code actions")
    void testLearningCodeActions() throws ExecutionException, InterruptedException {
        // Arrange
        TextDocumentItem document = new TextDocumentItem(
            "file:///test.java",
            "java",
            1,
            "public class Test {\n    public void complexMethod() {\n        // Complex logic here\n    }\n}"
        );
        
        DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(document);
        textDocumentService.didOpen(openParams);
        
        CodeActionParams codeActionParams = new CodeActionParams();
        codeActionParams.setTextDocument(new TextDocumentIdentifier("file:///test.java"));
        codeActionParams.setRange(new Range(new Position(1, 4), new Position(3, 5)));
        codeActionParams.setContext(new CodeActionContext(List.of()));
        
        // Act
        CompletableFuture<List<Either<Command, CodeAction>>> codeActionFuture = 
            textDocumentService.codeAction(codeActionParams);
        List<Either<Command, CodeAction>> codeActions = codeActionFuture.get();
        
        // Assert
        assertNotNull(codeActions);
        assertFalse(codeActions.isEmpty());
        
        // Check for learning-specific actions
        boolean hasExplainAction = codeActions.stream()
            .anyMatch(action -> action.isRight() && 
                     action.getRight().getTitle().contains("Explain"));
        assertTrue(hasExplainAction);
    }

    @Test
    @DisplayName("Should handle explain code command")
    void testExplainCodeCommand() throws ExecutionException, InterruptedException {
        // Arrange
        ExecuteCommandParams commandParams = new ExecuteCommandParams();
        commandParams.setCommand("ailearning.explainCode");
        commandParams.setArguments(List.of(
            "file:///test.java",
            new Range(new Position(0, 0), new Position(1, 0)),
            "public class Test {}",
            "java"
        ));
        
        // Act
        CompletableFuture<Object> commandFuture = workspaceService.executeCommand(commandParams);
        Object result = commandFuture.get();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.toString().contains("Code Explanation"));
    }

    @Test
    @DisplayName("Should handle suggest improvements command")
    void testSuggestImprovementsCommand() throws ExecutionException, InterruptedException {
        // Arrange
        ExecuteCommandParams commandParams = new ExecuteCommandParams();
        commandParams.setCommand("ailearning.suggestImprovements");
        commandParams.setArguments(List.of(
            "file:///test.java",
            new Range(new Position(0, 0), new Position(1, 0)),
            "for(int i=0;i<list.size();i++){System.out.println(list.get(i));}",
            "java"
        ));
        
        // Act
        CompletableFuture<Object> commandFuture = workspaceService.executeCommand(commandParams);
        Object result = commandFuture.get();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.toString().contains("Improvement Suggestions"));
    }

    @Test
    @DisplayName("Should handle create learning path command")
    void testCreateLearningPathCommand() throws ExecutionException, InterruptedException {
        // Arrange
        ExecuteCommandParams commandParams = new ExecuteCommandParams();
        commandParams.setCommand("ailearning.createLearningPath");
        commandParams.setArguments(List.of(
            "file:///test.java",
            "lambda expressions",
            "java"
        ));
        
        // Act
        CompletableFuture<Object> commandFuture = workspaceService.executeCommand(commandParams);
        Object result = commandFuture.get();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.toString().contains("Learning Path"));
    }

    @Test
    @DisplayName("Should handle security analysis command")
    void testSecurityAnalysisCommand() throws ExecutionException, InterruptedException {
        // Arrange
        ExecuteCommandParams commandParams = new ExecuteCommandParams();
        commandParams.setCommand("ailearning.analyzeSecurity");
        commandParams.setArguments(List.of(
            "file:///test.java",
            new Range(new Position(0, 0), new Position(1, 0)),
            "String sql = \"SELECT * FROM users WHERE id = \" + userId;",
            "java"
        ));
        
        // Act
        CompletableFuture<Object> commandFuture = workspaceService.executeCommand(commandParams);
        Object result = commandFuture.get();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.toString().contains("Security Analysis"));
        
        // Verify client was notified (security issues should trigger warnings)
        verify(mockClient, atLeastOnce()).showMessage(any(MessageParams.class));
    }

    @Test
    @DisplayName("Should resolve completion items with learning context")
    void testCompletionItemResolve() throws ExecutionException, InterruptedException {
        // Arrange
        CompletionItem unresolved = new CompletionItem("testMethod");
        unresolved.setKind(CompletionItemKind.Method);
        
        // Act
        CompletableFuture<CompletionItem> resolveFuture = 
            textDocumentService.resolveCompletionItem(unresolved);
        CompletionItem resolved = resolveFuture.get();
        
        // Assert
        assertNotNull(resolved);
        assertNotNull(resolved.getDocumentation());
        assertTrue(resolved.getDocumentation().getRight().getValue()
            .contains("AI Learning Companion"));
    }

    @Test
    @DisplayName("Should handle unknown commands gracefully")
    void testUnknownCommand() throws ExecutionException, InterruptedException {
        // Arrange
        ExecuteCommandParams commandParams = new ExecuteCommandParams();
        commandParams.setCommand("unknown.command");
        commandParams.setArguments(List.of());
        
        // Act
        CompletableFuture<Object> commandFuture = workspaceService.executeCommand(commandParams);
        Object result = commandFuture.get();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.toString().contains("Unknown command"));
    }
}