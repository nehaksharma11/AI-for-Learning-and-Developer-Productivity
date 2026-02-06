package com.ailearning.lsp.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

/**
 * Main Language Server implementation for AI Learning Companion.
 * Implements the Language Server Protocol to provide IDE integration.
 */
public class AILearningLanguageServer implements LanguageServer, LanguageClientAware {
    
    private static final Logger logger = LoggerFactory.getLogger(AILearningLanguageServer.class);
    
    private final AILearningTextDocumentService textDocumentService;
    private final AILearningWorkspaceService workspaceService;
    private LanguageClient client;
    private int errorCode = 1;

    public AILearningLanguageServer() {
        this.textDocumentService = new AILearningTextDocumentService();
        this.workspaceService = new AILearningWorkspaceService();
        logger.info("AI Learning Language Server initialized");
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        logger.info("Language server initialize request received");
        logger.info("Client: {}", params.getClientInfo() != null ? params.getClientInfo().getName() : "Unknown");
        logger.info("Root URI: {}", params.getRootUri());
        
        // Set up server capabilities
        ServerCapabilities capabilities = new ServerCapabilities();
        
        // Text document sync
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
        
        // Hover support
        capabilities.setHoverProvider(true);
        
        // Completion support
        CompletionOptions completionOptions = new CompletionOptions();
        completionOptions.setResolveProvider(true);
        completionOptions.setTriggerCharacters(List.of(".", "@", "("));
        capabilities.setCompletionProvider(completionOptions);
        
        // Code action support
        CodeActionOptions codeActionOptions = new CodeActionOptions();
        codeActionOptions.setCodeActionKinds(List.of(
            CodeActionKind.QuickFix,
            CodeActionKind.Refactor,
            CodeActionKind.Source
        ));
        capabilities.setCodeActionProvider(codeActionOptions);
        
        // Diagnostic support
        capabilities.setDiagnosticProvider(new DiagnosticRegistrationOptions());
        
        // Workspace capabilities
        WorkspaceServerCapabilities workspaceCapabilities = new WorkspaceServerCapabilities();
        WorkspaceFoldersOptions workspaceFoldersOptions = new WorkspaceFoldersOptions();
        workspaceFoldersOptions.setSupported(true);
        workspaceFoldersOptions.setChangeNotifications(true);
        workspaceCapabilities.setWorkspaceFolders(workspaceFoldersOptions);
        capabilities.setWorkspace(workspaceCapabilities);
        
        // Server info
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setName("AI Learning Companion Language Server");
        serverInfo.setVersion("1.0.0");
        
        // Initialize services with client capabilities
        textDocumentService.initialize(params.getCapabilities());
        workspaceService.initialize(params.getCapabilities());
        
        InitializeResult result = new InitializeResult(capabilities, serverInfo);
        
        logger.info("Language server initialized successfully");
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public void initialized(InitializedParams params) {
        logger.info("Language server initialized notification received");
        
        // Register for configuration changes and custom commands
        if (client != null) {
            List<Registration> registrations = new ArrayList<>();
            
            // Configuration changes
            registrations.add(new Registration("workspace/didChangeConfiguration", 
                "workspace/didChangeConfiguration", null));
            
            // Custom AI Learning commands
            registrations.add(new Registration("ailearning.explainCode", 
                "workspace/executeCommand", null));
            registrations.add(new Registration("ailearning.showExamples", 
                "workspace/executeCommand", null));
            registrations.add(new Registration("ailearning.createLearningPath", 
                "workspace/executeCommand", null));
            registrations.add(new Registration("ailearning.suggestImprovements", 
                "workspace/executeCommand", null));
            registrations.add(new Registration("ailearning.analyzePerformance", 
                "workspace/executeCommand", null));
            registrations.add(new Registration("ailearning.analyzeSecurity", 
                "workspace/executeCommand", null));
            registrations.add(new Registration("ailearning.generateDocumentation", 
                "workspace/executeCommand", null));
            registrations.add(new Registration("ailearning.addComments", 
                "workspace/executeCommand", null));
            registrations.add(new Registration("ailearning.extractMethod", 
                "workspace/executeCommand", null));
            registrations.add(new Registration("ailearning.simplifyCode", 
                "workspace/executeCommand", null));
            
            RegistrationParams registrationParams = new RegistrationParams();
            registrationParams.setRegistrations(registrations);
            
            client.registerCapability(registrationParams).thenAccept(result -> {
                logger.info("Successfully registered AI Learning capabilities and commands");
                sendInfo("AI Learning Companion is ready! Right-click on code to access learning features.");
            }).exceptionally(throwable -> {
                logger.warn("Failed to register AI Learning capabilities", throwable);
                sendWarning("Some AI Learning features may not be available");
                return null;
            });
        }
        
        // Notify services that initialization is complete
        textDocumentService.initialized();
        workspaceService.initialized();
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        logger.info("Language server shutdown request received");
        
        // Cleanup services
        textDocumentService.shutdown();
        workspaceService.shutdown();
        
        errorCode = 0;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        logger.info("Language server exit notification received");
        System.exit(errorCode);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
        textDocumentService.connect(client);
        workspaceService.connect(client);
        logger.info("Language client connected");
    }

    /**
     * Gets the connected language client
     */
    public LanguageClient getClient() {
        return client;
    }

    /**
     * Checks if the server is properly initialized and connected
     */
    public boolean isReady() {
        return client != null && textDocumentService.isReady() && workspaceService.isReady();
    }

    /**
     * Sends a notification to the client
     */
    public void sendNotification(String method, Object params) {
        if (client != null) {
            client.notifyUser(new MessageParams(MessageType.Info, 
                "AI Learning Companion: " + method));
        }
    }

    /**
     * Sends an error message to the client
     */
    public void sendError(String message, Throwable throwable) {
        logger.error(message, throwable);
        if (client != null) {
            client.showMessage(new MessageParams(MessageType.Error, 
                "AI Learning Companion Error: " + message));
        }
    }

    /**
     * Sends an info message to the client
     */
    public void sendInfo(String message) {
        logger.info(message);
        if (client != null) {
            client.showMessage(new MessageParams(MessageType.Info, 
                "AI Learning Companion: " + message));
        }
    }

    /**
     * Sends a warning message to the client
     */
    public void sendWarning(String message) {
        logger.warn(message);
        if (client != null) {
            client.showMessage(new MessageParams(MessageType.Warning, 
                "AI Learning Companion: " + message));
        }
    }
}