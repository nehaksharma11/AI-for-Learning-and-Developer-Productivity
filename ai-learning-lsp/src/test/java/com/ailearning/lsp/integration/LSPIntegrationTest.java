package com.ailearning.lsp.integration;

import com.ailearning.lsp.server.AILearningLanguageServer;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the complete LSP server workflow.
 */
class LSPIntegrationTest {

    private AILearningLanguageServer server;
    
    @Mock
    private LanguageClient mockClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new AILearningLanguageServer();
    }

    @Test
    @DisplayName("Should handle complete LSP workflow")
    void shouldHandleCompleteLSPWorkflow() throws Exception {
        // 1. Initialize server
        InitializeParams initParams = createInitializeParams();
        CompletableFuture<InitializeResult> initFuture = server.initialize(initParams);
        InitializeResult initResult = initFuture.get();
        
        assertNotNull(initResult);
        assertNotNull(initResult.getCapabilities());
        
        // 2. Connect client
        server.connect(mockClient);
        assertTrue(server.isReady());
        
        // 3. Send initialized notification
        server.initialized(new InitializedParams());
        verify(mockClient).registerCapability(any(RegistrationParams.class));
        
        // 4. Open a document
        TextDocumentItem document = new TextDocumentItem(
            "file:///test.java", "java", 1, 
            "public class Test {\n    // TODO: implement\n}"
        );
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(document));
        
        // Verify diagnostics are published
        verify(mockClient, timeout(1000)).publishDiagnostics(any(PublishDiagnosticsParams.class));
        
        // 5. Request hover information
        HoverParams hoverParams = new HoverParams(
            new TextDocumentIdentifier("file:///test.java"),
            new Position(0, 5)
        );
        CompletableFuture<Hover> hoverFuture = server.getTextDocumentService().hover(hoverParams);
        Hover hover = hoverFuture.get();
        
        assertNotNull(hover);
        assertNotNull(hover.getContents());
        
        // 6. Request completion
        CompletionParams completionParams = new CompletionParams(
            new TextDocumentIdentifier("file:///test.java"),
            new Position(1, 10)
        );
        CompletableFuture<Either<List<CompletionItem>, CompletionList>> completionFuture = 
            server.getTextDocumentService().completion(completionParams);
        Either<List<CompletionItem>, CompletionList> completion = completionFuture.get();
        
        assertNotNull(completion);
        assertTrue(completion.isRight());
        assertFalse(completion.getRight().getItems().isEmpty());
        
        // 7. Request code actions
        CodeActionParams codeActionParams = new CodeActionParams(
            new TextDocumentIdentifier("file:///test.java"),
            new Range(new Position(0, 0), new Position(0, 10)),
            new CodeActionContext(List.of())
        );
        CompletableFuture<List<Either<Command, CodeAction>>> codeActionFuture = 
            server.getTextDocumentService().codeAction(codeActionParams);
        List<Either<Command, CodeAction>> codeActions = codeActionFuture.get();
        
        assertNotNull(codeActions);
        assertFalse(codeActions.isEmpty());
        
        // 8. Execute a command
        ExecuteCommandParams commandParams = new ExecuteCommandParams(
            "ailearning.explainCode",
            List.of("file:///test.java", new Range(new Position(0, 0), new Position(0, 10)))
        );
        CompletableFuture<Object> commandFuture = 
            server.getWorkspaceService().executeCommand(commandParams);
        Object commandResult = commandFuture.get();
        
        assertNotNull(commandResult);
        
        // 9. Close document
        server.getTextDocumentService().didClose(
            new DidCloseTextDocumentParams(new TextDocumentIdentifier("file:///test.java"))
        );
        
        // 10. Shutdown server
        CompletableFuture<Object> shutdownFuture = server.shutdown();
        Object shutdownResult = shutdownFuture.get();
        
        assertNull(shutdownResult);
    }

    @Test
    @DisplayName("Should handle document changes correctly")
    void shouldHandleDocumentChangesCorrectly() throws Exception {
        // Initialize and connect
        server.initialize(createInitializeParams()).get();
        server.connect(mockClient);
        server.initialized(new InitializedParams());
        
        // Open document
        TextDocumentItem document = new TextDocumentItem(
            "file:///test.java", "java", 1, "public class Test {}"
        );
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(document));
        
        // Make incremental change
        VersionedTextDocumentIdentifier versionedDoc = new VersionedTextDocumentIdentifier(
            "file:///test.java", 2
        );
        TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent(
            new Range(new Position(0, 13), new Position(0, 17)),
            4,
            "Example"
        );
        DidChangeTextDocumentParams changeParams = new DidChangeTextDocumentParams(
            versionedDoc, List.of(change)
        );
        
        server.getTextDocumentService().didChange(changeParams);
        
        // Verify diagnostics are published for the change
        verify(mockClient, timeout(1000).atLeast(2))
            .publishDiagnostics(any(PublishDiagnosticsParams.class));
        
        // Save document
        server.getTextDocumentService().didSave(
            new DidSaveTextDocumentParams(new TextDocumentIdentifier("file:///test.java"))
        );
        
        // Verify additional diagnostics on save
        verify(mockClient, timeout(1000).atLeast(3))
            .publishDiagnostics(any(PublishDiagnosticsParams.class));
    }

    @Test
    @DisplayName("Should handle workspace operations")
    void shouldHandleWorkspaceOperations() throws Exception {
        // Initialize and connect
        server.initialize(createInitializeParams()).get();
        server.connect(mockClient);
        server.initialized(new InitializedParams());
        
        // Add workspace folder
        WorkspaceFolder folder = new WorkspaceFolder("file:///test/project", "Test Project");
        DidChangeWorkspaceFoldersParams folderParams = new DidChangeWorkspaceFoldersParams(
            new WorkspaceFoldersChangeEvent(List.of(folder), List.of())
        );
        
        server.getWorkspaceService().didChangeWorkspaceFolders(folderParams);
        
        // Search for symbols
        WorkspaceSymbolParams symbolParams = new WorkspaceSymbolParams("example");
        CompletableFuture<List<? extends SymbolInformation>> symbolFuture = 
            server.getWorkspaceService().symbol(symbolParams);
        List<? extends SymbolInformation> symbols = symbolFuture.get();
        
        assertNotNull(symbols);
        
        // Change configuration
        DidChangeConfigurationParams configParams = new DidChangeConfigurationParams(
            java.util.Map.of("aiLearning.analysis.enabled", true)
        );
        
        server.getWorkspaceService().didChangeConfiguration(configParams);
        
        // Simulate file changes
        FileEvent fileEvent = new FileEvent("file:///test/project/NewFile.java", FileChangeType.Created);
        DidChangeWatchedFilesParams watchedFilesParams = new DidChangeWatchedFilesParams(
            List.of(fileEvent)
        );
        
        server.getWorkspaceService().didChangeWatchedFiles(watchedFilesParams);
    }

    private InitializeParams createInitializeParams() {
        InitializeParams params = new InitializeParams();
        params.setRootUri("file:///test/project");
        
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setName("Test Client");
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