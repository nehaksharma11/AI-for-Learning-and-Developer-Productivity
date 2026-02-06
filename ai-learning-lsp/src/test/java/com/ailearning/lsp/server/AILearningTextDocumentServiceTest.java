package com.ailearning.lsp.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AILearningTextDocumentService.
 */
class AILearningTextDocumentServiceTest {

    private AILearningTextDocumentService service;
    
    @Mock
    private LanguageClient mockClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AILearningTextDocumentService();
        service.initialize(new ClientCapabilities());
        service.connect(mockClient);
        service.initialized();
    }

    @Nested
    @DisplayName("Document Lifecycle")
    class DocumentLifecycle {

        @Test
        @DisplayName("Should handle document open")
        void shouldHandleDocumentOpen() {
            // Given
            TextDocumentItem document = new TextDocumentItem(
                "file:///test.java", "java", 1, "public class Test {}"
            );
            DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(document);

            // When
            assertDoesNotThrow(() -> service.didOpen(params));

            // Then
            assertEquals(document, service.getDocument("file:///test.java"));
            verify(mockClient).publishDiagnostics(any(PublishDiagnosticsParams.class));
        }

        @Test
        @DisplayName("Should handle document changes")
        void shouldHandleDocumentChanges() {
            // Given
            TextDocumentItem document = new TextDocumentItem(
                "file:///test.java", "java", 1, "public class Test {}"
            );
            service.didOpen(new DidOpenTextDocumentParams(document));

            VersionedTextDocumentIdentifier versionedDoc = new VersionedTextDocumentIdentifier(
                "file:///test.java", 2
            );
            TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent(
                "public class Test { /* modified */ }"
            );
            DidChangeTextDocumentParams changeParams = new DidChangeTextDocumentParams(
                versionedDoc, List.of(change)
            );

            // When
            assertDoesNotThrow(() -> service.didChange(changeParams));

            // Then
            TextDocumentItem updatedDoc = service.getDocument("file:///test.java");
            assertEquals(2, updatedDoc.getVersion());
            assertTrue(updatedDoc.getText().contains("modified"));
        }

        @Test
        @DisplayName("Should handle document close")
        void shouldHandleDocumentClose() {
            // Given
            TextDocumentItem document = new TextDocumentItem(
                "file:///test.java", "java", 1, "public class Test {}"
            );
            service.didOpen(new DidOpenTextDocumentParams(document));

            TextDocumentIdentifier docId = new TextDocumentIdentifier("file:///test.java");
            DidCloseTextDocumentParams closeParams = new DidCloseTextDocumentParams(docId);

            // When
            service.didClose(closeParams);

            // Then
            assertNull(service.getDocument("file:///test.java"));
            verify(mockClient, atLeast(2)).publishDiagnostics(any(PublishDiagnosticsParams.class));
        }

        @Test
        @DisplayName("Should handle document save")
        void shouldHandleDocumentSave() {
            // Given
            TextDocumentItem document = new TextDocumentItem(
                "file:///test.java", "java", 1, "public class Test {}"
            );
            service.didOpen(new DidOpenTextDocumentParams(document));

            TextDocumentIdentifier docId = new TextDocumentIdentifier("file:///test.java");
            DidSaveTextDocumentParams saveParams = new DidSaveTextDocumentParams(docId);

            // When
            assertDoesNotThrow(() -> service.didSave(saveParams));

            // Then
            verify(mockClient, atLeast(2)).publishDiagnostics(any(PublishDiagnosticsParams.class));
        }
    }

    @Nested
    @DisplayName("Language Features")
    class LanguageFeatures {

        @Test
        @DisplayName("Should provide hover information")
        void shouldProvideHoverInformation() throws Exception {
            // Given
            TextDocumentItem document = new TextDocumentItem(
                "file:///test.java", "java", 1, "public class Test {}"
            );
            service.didOpen(new DidOpenTextDocumentParams(document));

            TextDocumentIdentifier docId = new TextDocumentIdentifier("file:///test.java");
            Position position = new Position(0, 5);
            HoverParams hoverParams = new HoverParams(docId, position);

            // When
            CompletableFuture<Hover> future = service.hover(hoverParams);
            Hover hover = future.get();

            // Then
            assertNotNull(hover);
            assertNotNull(hover.getContents());
        }

        @Test
        @DisplayName("Should provide completion items")
        void shouldProvideCompletionItems() throws Exception {
            // Given
            TextDocumentItem document = new TextDocumentItem(
                "file:///test.java", "java", 1, "public class Test {}"
            );
            service.didOpen(new DidOpenTextDocumentParams(document));

            TextDocumentIdentifier docId = new TextDocumentIdentifier("file:///test.java");
            Position position = new Position(0, 20);
            CompletionParams completionParams = new CompletionParams(docId, position);

            // When
            CompletableFuture<Either<List<CompletionItem>, CompletionList>> future = 
                service.completion(completionParams);
            Either<List<CompletionItem>, CompletionList> result = future.get();

            // Then
            assertNotNull(result);
            assertTrue(result.isRight());
            CompletionList completionList = result.getRight();
            assertNotNull(completionList.getItems());
            assertFalse(completionList.getItems().isEmpty());
        }

        @Test
        @DisplayName("Should resolve completion items")
        void shouldResolveCompletionItems() throws Exception {
            // Given
            CompletionItem item = new CompletionItem("testItem");
            item.setKind(CompletionItemKind.Function);

            // When
            CompletableFuture<CompletionItem> future = service.resolveCompletionItem(item);
            CompletionItem resolved = future.get();

            // Then
            assertNotNull(resolved);
            assertEquals("testItem", resolved.getLabel());
            assertNotNull(resolved.getDocumentation());
        }

        @Test
        @DisplayName("Should provide code actions")
        void shouldProvideCodeActions() throws Exception {
            // Given
            TextDocumentItem document = new TextDocumentItem(
                "file:///test.java", "java", 1, "public class Test {}"
            );
            service.didOpen(new DidOpenTextDocumentParams(document));

            TextDocumentIdentifier docId = new TextDocumentIdentifier("file:///test.java");
            Range range = new Range(new Position(0, 0), new Position(0, 10));
            CodeActionContext context = new CodeActionContext(List.of());
            CodeActionParams actionParams = new CodeActionParams(docId, range, context);

            // When
            CompletableFuture<List<Either<Command, CodeAction>>> future = 
                service.codeAction(actionParams);
            List<Either<Command, CodeAction>> actions = future.get();

            // Then
            assertNotNull(actions);
            assertFalse(actions.isEmpty());
            
            Either<Command, CodeAction> firstAction = actions.get(0);
            assertTrue(firstAction.isRight());
            CodeAction codeAction = firstAction.getRight();
            assertEquals("AI Learning: Explain Code", codeAction.getTitle());
        }
    }

    @Nested
    @DisplayName("Service State")
    class ServiceState {

        @Test
        @DisplayName("Should track service readiness")
        void shouldTrackServiceReadiness() {
            // Given
            AILearningTextDocumentService newService = new AILearningTextDocumentService();

            // Then
            assertFalse(newService.isReady());

            // When
            newService.initialize(new ClientCapabilities());
            newService.connect(mockClient);
            newService.initialized();

            // Then
            assertTrue(newService.isReady());
        }

        @Test
        @DisplayName("Should track open documents")
        void shouldTrackOpenDocuments() {
            // Given
            TextDocumentItem doc1 = new TextDocumentItem(
                "file:///test1.java", "java", 1, "class Test1 {}"
            );
            TextDocumentItem doc2 = new TextDocumentItem(
                "file:///test2.java", "java", 1, "class Test2 {}"
            );

            // When
            service.didOpen(new DidOpenTextDocumentParams(doc1));
            service.didOpen(new DidOpenTextDocumentParams(doc2));

            // Then
            assertEquals(2, service.getAllDocuments().size());
            assertTrue(service.getAllDocuments().containsKey("file:///test1.java"));
            assertTrue(service.getAllDocuments().containsKey("file:///test2.java"));
        }

        @Test
        @DisplayName("Should handle shutdown gracefully")
        void shouldHandleShutdownGracefully() {
            // Given
            TextDocumentItem document = new TextDocumentItem(
                "file:///test.java", "java", 1, "public class Test {}"
            );
            service.didOpen(new DidOpenTextDocumentParams(document));

            // When
            service.shutdown();

            // Then
            assertTrue(service.getAllDocuments().isEmpty());
        }
    }

    @Nested
    @DisplayName("Incremental Changes")
    class IncrementalChanges {

        @Test
        @DisplayName("Should handle single line changes")
        void shouldHandleSingleLineChanges() {
            // Given
            TextDocumentItem document = new TextDocumentItem(
                "file:///test.java", "java", 1, "public class Test {}"
            );
            service.didOpen(new DidOpenTextDocumentParams(document));

            VersionedTextDocumentIdentifier versionedDoc = new VersionedTextDocumentIdentifier(
                "file:///test.java", 2
            );
            
            Range range = new Range(new Position(0, 13), new Position(0, 17));
            TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent(
                range, 4, "Example"
            );
            
            DidChangeTextDocumentParams changeParams = new DidChangeTextDocumentParams(
                versionedDoc, List.of(change)
            );

            // When
            service.didChange(changeParams);

            // Then
            TextDocumentItem updatedDoc = service.getDocument("file:///test.java");
            assertTrue(updatedDoc.getText().contains("Example"));
            assertFalse(updatedDoc.getText().contains("Test"));
        }

        @Test
        @DisplayName("Should handle unknown document changes gracefully")
        void shouldHandleUnknownDocumentChangesGracefully() {
            // Given
            VersionedTextDocumentIdentifier unknownDoc = new VersionedTextDocumentIdentifier(
                "file:///unknown.java", 1
            );
            TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent("new content");
            DidChangeTextDocumentParams changeParams = new DidChangeTextDocumentParams(
                unknownDoc, List.of(change)
            );

            // When & Then
            assertDoesNotThrow(() -> service.didChange(changeParams));
        }
    }
}