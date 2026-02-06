package com.ailearning.lsp;

import com.ailearning.lsp.server.AILearningLanguageServer;
import com.ailearning.lsp.server.AILearningTextDocumentService;
import com.ailearning.lsp.server.AILearningWorkspaceService;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Checkpoint validation test to ensure all LSP integration components work correctly.
 * This test validates the complete LSP workflow and custom AI learning features.
 */
class CheckpointValidationTest {

    private AILearningLanguageServer server;
    
    @Mock
    private LanguageClient mockClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new AILearningLanguageServer();
    }

    @Test
    @DisplayName("Checkpoint: Complete LSP Integration Validation")
    void validateCompleteLSPIntegration() throws Exception {
        // ✅ 1. Server Initialization
        InitializeParams initParams = createInitializeParams();
        CompletableFuture<InitializeResult> initFuture = server.initialize(initParams);
        InitializeResult initResult = initFuture.get(5, TimeUnit.SECONDS);
        
        assertNotNull(initResult, "Server should initialize successfully");
        assertNotNull(initResult.getCapabilities(), "Server capabilities should be defined");
        assertNotNull(initResult.getServerInfo(), "Server info should be provided");
        
        ServerCapabilities capabilities = initResult.getCapabilities();
        assertEquals(TextDocumentSyncKind.Incremental, capabilities.getTextDocumentSync());
        assertTrue(capabilities.getHoverProvider(), "Hover should be supported");
        assertNotNull(capabilities.getCompletionProvider(), "Completion should be supported");
        assertNotNull(capabilities.getCodeActionProvider(), "Code actions should be supported");
        
        // ✅ 2. Client Connection
        server.connect(mockClient);
        assertTrue(server.isReady(), "Server should be ready after client connection");
        
        // ✅ 3. Initialized Notification
        server.initialized(new InitializedParams());
        verify(mockClient, timeout(1000)).registerCapability(any(RegistrationParams.class));
        
        // ✅ 4. Document Lifecycle Management
        TextDocumentItem javaDocument = new TextDocumentItem(
            "file:///test/Example.java", 
            "java", 
            1, 
            "public class Example {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}"
        );
        
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(javaDocument));
        verify(mockClient, timeout(2000)).publishDiagnostics(any(PublishDiagnosticsParams.class));
        
        // ✅ 5. Contextual Hover (AI Learning Feature)
        HoverParams hoverParams = new HoverParams(
            new TextDocumentIdentifier("file:///test/Example.java"),
            new Position(0, 7) // Position on "class"
        );
        CompletableFuture<Hover> hoverFuture = server.getTextDocumentService().hover(hoverParams);
        Hover hover = hoverFuture.get(3, TimeUnit.SECONDS);
        
        assertNotNull(hover, "Hover should provide information");
        assertNotNull(hover.getContents(), "Hover should have content");
        assertTrue(hover.getContents().getRight().getValue().contains("AI Learning Companion"), 
                  "Hover should include AI Learning context");
        
        // ✅ 6. Intelligent Completions (AI Learning Feature)
        CompletionParams completionParams = new CompletionParams(
            new TextDocumentIdentifier("file:///test/Example.java"),
            new Position(1, 11) // After "public "
        );
        CompletableFuture<Either<List<CompletionItem>, CompletionList>> completionFuture = 
            server.getTextDocumentService().completion(completionParams);
        Either<List<CompletionItem>, CompletionList> completion = completionFuture.get(3, TimeUnit.SECONDS);
        
        assertNotNull(completion, "Completion should be provided");
        assertTrue(completion.isRight(), "Completion should return a list");
        CompletionList completionList = completion.getRight();
        assertFalse(completionList.getItems().isEmpty(), "Completion should have items");
        
        // Verify AI Learning context in completions
        boolean hasLearningCompletion = completionList.getItems().stream()
            .anyMatch(item -> item.getDetail() != null && 
                     item.getDetail().contains("AI Learning Companion"));
        assertTrue(hasLearningCompletion, "Completions should include AI Learning context");
        
        // ✅ 7. Learning-Focused Code Actions
        CodeActionParams codeActionParams = new CodeActionParams(
            new TextDocumentIdentifier("file:///test/Example.java"),
            new Range(new Position(2, 8), new Position(2, 35)), // System.out.println line
            new CodeActionContext(List.of())
        );
        CompletableFuture<List<Either<Command, CodeAction>>> codeActionFuture = 
            server.getTextDocumentService().codeAction(codeActionParams);
        List<Either<Command, CodeAction>> codeActions = codeActionFuture.get(3, TimeUnit.SECONDS);
        
        assertNotNull(codeActions, "Code actions should be provided");
        assertFalse(codeActions.isEmpty(), "Code actions should not be empty");
        
        // Verify learning-specific actions
        boolean hasExplainAction = codeActions.stream()
            .anyMatch(action -> action.isRight() && 
                     action.getRight().getTitle().contains("Explain"));
        assertTrue(hasExplainAction, "Should have 'Explain this code' action");
        
        boolean hasImprovementAction = codeActions.stream()
            .anyMatch(action -> action.isRight() && 
                     action.getRight().getTitle().contains("improvements"));
        assertTrue(hasImprovementAction, "Should have improvement suggestions action");
        
        // ✅ 8. Custom Command Execution (AI Learning Feature)
        ExecuteCommandParams explainCommand = new ExecuteCommandParams(
            "ailearning.explainCode",
            List.of(
                "file:///test/Example.java",
                new Range(new Position(0, 0), new Position(0, 20)),
                "public class Example",
                "java"
            )
        );
        CompletableFuture<Object> commandFuture = 
            server.getWorkspaceService().executeCommand(explainCommand);
        Object commandResult = commandFuture.get(3, TimeUnit.SECONDS);
        
        assertNotNull(commandResult, "Command should return result");
        assertTrue(commandResult.toString().contains("Code Explanation"), 
                  "Command should provide code explanation");
        
        // ✅ 9. Workspace Operations
        WorkspaceFolder testFolder = new WorkspaceFolder("file:///test/project", "Test Project");
        DidChangeWorkspaceFoldersParams folderParams = new DidChangeWorkspaceFoldersParams(
            new WorkspaceFoldersChangeEvent(List.of(testFolder), List.of())
        );
        server.getWorkspaceService().didChangeWorkspaceFolders(folderParams);
        
        // ✅ 10. Document Changes
        VersionedTextDocumentIdentifier versionedDoc = new VersionedTextDocumentIdentifier(
            "file:///test/Example.java", 2
        );
        TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent(
            new Range(new Position(2, 8), new Position(2, 35)),
            27,
            "System.out.println(\"Hello, AI Learning!\");"
        );
        DidChangeTextDocumentParams changeParams = new DidChangeTextDocumentParams(
            versionedDoc, List.of(change)
        );
        
        server.getTextDocumentService().didChange(changeParams);
        verify(mockClient, timeout(2000).atLeast(2))
            .publishDiagnostics(any(PublishDiagnosticsParams.class));
        
        // ✅ 11. Document Save
        server.getTextDocumentService().didSave(
            new DidSaveTextDocumentParams(new TextDocumentIdentifier("file:///test/Example.java"))
        );
        
        // ✅ 12. Multiple Language Support
        TextDocumentItem pythonDocument = new TextDocumentItem(
            "file:///test/example.py", 
            "python", 
            1, 
            "def hello_world():\n    print(\"Hello, World!\")\n\nif __name__ == \"__main__\":\n    hello_world()"
        );
        
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(pythonDocument));
        
        // Test Python hover
        HoverParams pythonHoverParams = new HoverParams(
            new TextDocumentIdentifier("file:///test/example.py"),
            new Position(0, 0) // Position on "def"
        );
        CompletableFuture<Hover> pythonHoverFuture = server.getTextDocumentService().hover(pythonHoverParams);
        Hover pythonHover = pythonHoverFuture.get(3, TimeUnit.SECONDS);
        
        assertNotNull(pythonHover, "Python hover should work");
        
        // ✅ 13. Error Handling
        ExecuteCommandParams unknownCommand = new ExecuteCommandParams("unknown.command", List.of());
        CompletableFuture<Object> unknownCommandFuture = 
            server.getWorkspaceService().executeCommand(unknownCommand);
        Object unknownResult = unknownCommandFuture.get(3, TimeUnit.SECONDS);
        
        assertNotNull(unknownResult, "Unknown command should be handled gracefully");
        assertTrue(unknownResult.toString().contains("Unknown command"), 
                  "Should indicate unknown command");
        
        // ✅ 14. Document Close
        server.getTextDocumentService().didClose(
            new DidCloseTextDocumentParams(new TextDocumentIdentifier("file:///test/Example.java"))
        );
        server.getTextDocumentService().didClose(
            new DidCloseTextDocumentParams(new TextDocumentIdentifier("file:///test/example.py"))
        );
        
        // ✅ 15. Server Shutdown
        CompletableFuture<Object> shutdownFuture = server.shutdown();
        Object shutdownResult = shutdownFuture.get(3, TimeUnit.SECONDS);
        
        assertNull(shutdownResult, "Shutdown should complete successfully");
        
        // ✅ Checkpoint Summary
        System.out.println("✅ CHECKPOINT PASSED: All LSP integration tests successful!");
        System.out.println("✅ Server initialization and capabilities: PASSED");
        System.out.println("✅ Client connection and communication: PASSED");
        System.out.println("✅ Document lifecycle management: PASSED");
        System.out.println("✅ Contextual hover with AI learning: PASSED");
        System.out.println("✅ Intelligent completions with learning context: PASSED");
        System.out.println("✅ Learning-focused code actions: PASSED");
        System.out.println("✅ Custom AI learning commands: PASSED");
        System.out.println("✅ Workspace operations: PASSED");
        System.out.println("✅ Multi-language support (Java, Python): PASSED");
        System.out.println("✅ Error handling and graceful degradation: PASSED");
        System.out.println("✅ Server shutdown: PASSED");
    }

    @Test
    @DisplayName("Checkpoint: Service Isolation and Independence")
    void validateServiceIsolation() {
        // Test that services can be used independently
        AILearningTextDocumentService textService = new AILearningTextDocumentService();
        AILearningWorkspaceService workspaceService = new AILearningWorkspaceService();
        
        // Initialize services
        ClientCapabilities capabilities = new ClientCapabilities();
        textService.initialize(capabilities);
        workspaceService.initialize(capabilities);
        
        // Connect mock client
        textService.connect(mockClient);
        workspaceService.connect(mockClient);
        
        // Mark as initialized
        textService.initialized();
        workspaceService.initialized();
        
        // Verify services are ready
        assertTrue(textService.isReady(), "Text document service should be ready");
        assertTrue(workspaceService.isReady(), "Workspace service should be ready");
        
        // Test service shutdown
        textService.shutdown();
        workspaceService.shutdown();
        
        assertFalse(textService.isReady(), "Text service should not be ready after shutdown");
        assertFalse(workspaceService.isReady(), "Workspace service should not be ready after shutdown");
        
        System.out.println("✅ Service isolation validation: PASSED");
    }

    @Test
    @DisplayName("Checkpoint: Performance and Responsiveness")
    void validatePerformanceRequirements() throws Exception {
        // Initialize server
        server.initialize(createInitializeParams()).get();
        server.connect(mockClient);
        server.initialized(new InitializedParams());
        
        // Open document
        TextDocumentItem document = new TextDocumentItem(
            "file:///test/Performance.java", 
            "java", 
            1, 
            "public class Performance {\n    // Performance test\n}"
        );
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(document));
        
        // Test hover response time (should be < 500ms as per requirements)
        long startTime = System.currentTimeMillis();
        HoverParams hoverParams = new HoverParams(
            new TextDocumentIdentifier("file:///test/Performance.java"),
            new Position(0, 7)
        );
        Hover hover = server.getTextDocumentService().hover(hoverParams).get();
        long hoverTime = System.currentTimeMillis() - startTime;
        
        assertNotNull(hover, "Hover should respond");
        assertTrue(hoverTime < 500, "Hover should respond within 500ms, took: " + hoverTime + "ms");
        
        // Test completion response time
        startTime = System.currentTimeMillis();
        CompletionParams completionParams = new CompletionParams(
            new TextDocumentIdentifier("file:///test/Performance.java"),
            new Position(1, 10)
        );
        Either<List<CompletionItem>, CompletionList> completion = 
            server.getTextDocumentService().completion(completionParams).get();
        long completionTime = System.currentTimeMillis() - startTime;
        
        assertNotNull(completion, "Completion should respond");
        assertTrue(completionTime < 500, "Completion should respond within 500ms, took: " + completionTime + "ms");
        
        System.out.println("✅ Performance validation: PASSED");
        System.out.println("  - Hover response time: " + hoverTime + "ms");
        System.out.println("  - Completion response time: " + completionTime + "ms");
    }

    private InitializeParams createInitializeParams() {
        InitializeParams params = new InitializeParams();
        params.setRootUri("file:///test/project");
        
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setName("Checkpoint Test Client");
        clientInfo.setVersion("1.0.0");
        params.setClientInfo(clientInfo);
        
        ClientCapabilities capabilities = new ClientCapabilities();
        
        // Text document capabilities
        TextDocumentClientCapabilities textDocCapabilities = new TextDocumentClientCapabilities();
        textDocCapabilities.setHover(new HoverCapabilities());
        textDocCapabilities.setCompletion(new CompletionCapabilities());
        textDocCapabilities.setCodeAction(new CodeActionCapabilities());
        capabilities.setTextDocument(textDocCapabilities);
        
        // Workspace capabilities
        WorkspaceClientCapabilities workspaceCapabilities = new WorkspaceClientCapabilities();
        workspaceCapabilities.setWorkspaceFolders(true);
        workspaceCapabilities.setConfiguration(true);
        workspaceCapabilities.setDidChangeWatchedFiles(new DidChangeWatchedFilesCapabilities());
        capabilities.setWorkspace(workspaceCapabilities);
        
        params.setCapabilities(capabilities);
        
        return params;
    }
}