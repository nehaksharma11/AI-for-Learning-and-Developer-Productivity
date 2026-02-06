package com.ailearning.lsp.launcher;

import com.ailearning.lsp.server.AILearningLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

/**
 * Launcher for the AI Learning Companion Language Server.
 * Handles the setup and startup of the LSP server with JSON-RPC communication.
 */
public class LSPServerLauncher {
    
    private static final Logger logger = LoggerFactory.getLogger(LSPServerLauncher.class);
    
    /**
     * Launch the language server with standard input/output streams
     */
    public static void launch(InputStream in, OutputStream out) {
        logger.info("Starting AI Learning Companion Language Server");
        
        try {
            // Create the language server instance
            AILearningLanguageServer server = new AILearningLanguageServer();
            
            // Create the launcher
            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);
            
            // Connect the server to the client
            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);
            
            // Start listening for client requests
            Future<Void> listening = launcher.startListening();
            
            logger.info("AI Learning Companion Language Server started successfully");
            logger.info("Server is listening for client requests...");
            
            // Wait for the server to finish
            listening.get();
            
        } catch (Exception e) {
            logger.error("Failed to start AI Learning Companion Language Server", e);
            System.exit(1);
        }
    }
    
    /**
     * Main entry point for the language server
     */
    public static void main(String[] args) {
        logger.info("AI Learning Companion Language Server - Version 1.0.0");
        logger.info("Starting server with standard I/O communication");
        
        // Parse command line arguments
        boolean debugMode = false;
        int port = -1;
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--debug":
                    debugMode = true;
                    logger.info("Debug mode enabled");
                    break;
                case "--port":
                    if (i + 1 < args.length) {
                        try {
                            port = Integer.parseInt(args[i + 1]);
                            i++; // Skip the port number argument
                        } catch (NumberFormatException e) {
                            logger.error("Invalid port number: {}", args[i + 1]);
                            System.exit(1);
                        }
                    }
                    break;
                case "--help":
                    printUsage();
                    System.exit(0);
                    break;
                default:
                    logger.warn("Unknown argument: {}", args[i]);
                    break;
            }
        }
        
        if (debugMode) {
            // Enable debug logging
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        }
        
        if (port > 0) {
            // Launch with socket communication
            launchWithSocket(port);
        } else {
            // Launch with standard I/O
            launch(System.in, System.out);
        }
    }
    
    /**
     * Launch the server with socket communication
     */
    private static void launchWithSocket(int port) {
        logger.info("Starting server with socket communication on port {}", port);
        
        try {
            java.net.ServerSocket serverSocket = new java.net.ServerSocket(port);
            logger.info("Server socket listening on port {}", port);
            
            while (true) {
                java.net.Socket clientSocket = serverSocket.accept();
                logger.info("Client connected from {}", clientSocket.getRemoteSocketAddress());
                
                // Handle client in a separate thread
                Thread clientThread = new Thread(() -> {
                    try {
                        launch(clientSocket.getInputStream(), clientSocket.getOutputStream());
                    } catch (Exception e) {
                        logger.error("Error handling client connection", e);
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (Exception e) {
                            logger.error("Error closing client socket", e);
                        }
                    }
                });
                
                clientThread.setName("LSP-Client-" + clientSocket.getRemoteSocketAddress());
                clientThread.start();
            }
            
        } catch (Exception e) {
            logger.error("Failed to start server with socket communication", e);
            System.exit(1);
        }
    }
    
    /**
     * Print usage information
     */
    private static void printUsage() {
        System.out.println("AI Learning Companion Language Server");
        System.out.println("Usage: java -jar ai-learning-lsp.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --debug          Enable debug logging");
        System.out.println("  --port <port>    Use socket communication on specified port");
        System.out.println("  --help           Show this help message");
        System.out.println();
        System.out.println("By default, the server uses standard input/output for communication.");
    }
}