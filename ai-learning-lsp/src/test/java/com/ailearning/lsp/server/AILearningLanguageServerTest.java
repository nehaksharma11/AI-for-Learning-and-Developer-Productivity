package com.ailearning.lsp.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AILearningLanguageServer.
 */
class AILearningLanguageServerTest {

    private AILearningLanguageServer server;
    
    @Mock
    private LanguageClient mockClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        server = new AILearningLanguageServer();
    }

    @Nested
    @DisplayName("Server Initialization")
    class ServerInitialization {

        @Test
        @DisplayName("Should initialize server with proper capabilities")
        void shouldInitializeServerWithProperCapabilities() throws Exception {
            // Given
            InitializeParams params = new InitializeParams();
            params.setRootUri("file:///test/project");
            
            ClientInfo clientInfo = new ClientInfo();
            clientInfo.setName("Test Client");
            clientInfo.setVersion("1.0.0");
            params.setClientInfo(clientInfo);
            
            ClientCapabilities capabilities = new ClientCapabilities();
            params.setCapabilities(capabilities);

            // When
            CompletableFuture<InitializeResult> future = server.initialize(params);
            InitializeResult result = future.get();

            // Then
            assertNotNull(result);
            assertNotNull(result.getCapabilities());
            assertNotNull(result.getServerInfo());
            
            ServerCapabilities serverCapabilities = result.getCapabilities();
            assertEquals(TextDocumentSyncKind.Incremental, serverCapabilities.getTextDocumentSync());
            assertTrue(serverCapabilities.getHoverProvider());
            assertNotNull(serverCapabilities.getCompletionProvider());
            assertNotNull(serverCapabilities.getCodeActionProvider());
            
            ServerInfo serverInfo = result.getServerInfo();
            assertEquals("AI Learning Companion Language Server", serverInfo.getName());
            assertEquals("1.0.0", serverInfo.getVersion());
        }

        @Test
        @DisplayName("Should handle initialized notification")
        void shouldHandleInitializedNotification() {
            // Given
            server.connect(mockClient);
            InitializedParams params = new InitializedParams();

            // When
            assertDoesNotThrow(() -> server.initialized(params));

            // Then
            verify(mockClient).registerCapability(any(RegistrationParams.class));
        }

        @Test
        @DisplayName("Should handle shutdown gracefully")
        void shouldHandleShutdownGracefully() throws Exception {
            // When
            CompletableFuture<Object> future = server.shutdown();
            Object result = future.get();

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Client Connection")
    class ClientConnection {

        @Test
        @DisplayName("Should connect to language client")
        void shouldConnectToLanguageClient() {
            // When
            server.connect(mockClient);

            // Then
            assertEquals(mockClient, server.getClient());
            assertTrue(server.isReady());
        }

        @Test
        @DisplayName("Should send notifications to client")
        void shouldSendNotificationsToClient() {
            // Given
            server.connect(mockClient);

            // When
            server.sendInfo("Test message");

            // Then
            verify(mockClient).showMessage(any(MessageParams.class));
        }

        @Test
        @DisplayName("Should send error messages to client")
        void shouldSendErrorMessagesToClient() {
            // Given
            server.connect(mockClient);
            Exception testException = new RuntimeException("Test error");

            // When
            server.sendError("Test error message", testException);

            // Then
            verify(mockClient).showMessage(argThat(params -> 
                params.getType() == MessageType.Error &&
                params.getMessage().contains("Test error message")
            ));
        }

        @Test
        @DisplayName("Should send warning messages to client")
        void shouldSendWarningMessagesToClient() {
            // Given
            server.connect(mockClient);

            // When
            server.sendWarning("Test warning");

            // Then
            verify(mockClient).showMessage(argThat(params -> 
                params.getType() == MessageType.Warning &&
                params.getMessage().contains("Test warning")
            ));
        }
    }

    @Nested
    @DisplayName("Service Access")
    class ServiceAccess {

        @Test
        @DisplayName("Should provide text document service")
        void shouldProvideTextDocumentService() {
            // When
            var textDocumentService = server.getTextDocumentService();

            // Then
            assertNotNull(textDocumentService);
            assertInstanceOf(AILearningTextDocumentService.class, textDocumentService);
        }

        @Test
        @DisplayName("Should provide workspace service")
        void shouldProvideWorkspaceService() {
            // When
            var workspaceService = server.getWorkspaceService();

            // Then
            assertNotNull(workspaceService);
            assertInstanceOf(AILearningWorkspaceService.class, workspaceService);
        }
    }

    @Nested
    @DisplayName("Server State")
    class ServerState {

        @Test
        @DisplayName("Should not be ready without client connection")
        void shouldNotBeReadyWithoutClientConnection() {
            // Then
            assertFalse(server.isReady());
        }

        @Test
        @DisplayName("Should be ready after client connection and initialization")
        void shouldBeReadyAfterClientConnectionAndInitialization() throws Exception {
            // Given
            InitializeParams params = new InitializeParams();
            params.setCapabilities(new ClientCapabilities());
            
            // When
            server.initialize(params).get();
            server.connect(mockClient);
            server.initialized(new InitializedParams());

            // Then
            assertTrue(server.isReady());
        }
    }
}