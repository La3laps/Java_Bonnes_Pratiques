package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Multi-threaded chat server that manages client connections and message
 * broadcasting.
 */
public class Server {
    /** Maximum number of messages stored in chat history */
    private static final int MAX_HISTORY_SIZE = 100;

    /** Port number on which the server listens */
    private final int port;

    /** Thread-safe list of all connected clients */
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    /** List storing chat message history */
    private final List<String> messageHistory = new CopyOnWriteArrayList<>();

    /** Server socket for accepting incoming connections */
    private ServerSocket serverSocket;

    /** Flag indicating if the server is currently running */
    private volatile boolean isRunning = false;

    /** Counter for assigning unique IDs to clients */
    private int clientCounter = 0;

    /**
     * Creates a new server instance.
     * 
     * @param port The port number on which the server will listen
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Starts the server and begins accepting client connections.
     * This method runs in a loop, accepting connections until the server is
     * stopped.
     * Each accepted connection is handled in a separate thread.
     * 
     * @throws IOException If an I/O error occurs when opening the socket
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
        isRunning = true;

        System.out.println("Chat server started on port " + port);

        // Main server loop - accepts new client connections
        while (isRunning) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(clientSocket, this);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    /**
     * Stops the server and closes the server socket.
     * 
     * @throws IOException If an I/O error occurs when closing the socket
     */
    public void stop() throws IOException {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    /**
     * Broadcasts a message to all connected clients except the sender.
     * Also adds the message to the server's chat history.
     * This method is synchronized to ensure thread-safe access to message history.
     * 
     * @param sender  The client who sent the message (will not receive the
     *                broadcast)
     * @param message The message to broadcast
     */
    public synchronized void broadcastMessage(ClientHandler sender, String message) {
        messageHistory.add(message);

        // Maintain history size limit by removing oldest message
        if (messageHistory.size() > MAX_HISTORY_SIZE) {
            messageHistory.remove(0);
        }

        // Send message to all clients except the sender
        for (ClientHandler client : clients) {
            if (client != sender && client.username != null) {
                try {
                    client.sendMessage(message);
                } catch (Exception e) {
                    // Client disconnected, will be cleaned up later
                }
            }
        }
    }

    /**
     * Sends the complete chat history to a newly connected client.
     * This method is synchronized to ensure thread-safe access to message history.
     * 
     * @param client The client to receive the chat history
     */
    public synchronized void sendHistoryToClient(ClientHandler client) {
        for (String message : messageHistory) {
            client.sendMessage(message);
        }
    }

    /**
     * Removes a client from the active clients list.
     * Called when a client disconnects.
     * 
     * @param client The client to remove
     */
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Inner class that handles individual client connections.
     * Each instance runs in its own thread and manages communication with one
     * client.
     */
    class ClientHandler implements Runnable {
        /** Socket connection to the client */
        private final Socket socket;

        /** Unique identifier for this client */
        @SuppressWarnings("unused")
        private final int clientId;

        /** Output stream for sending messages to the client */
        private PrintWriter out;

        /** Username chosen by the client */
        private String username;

        /**
         * Creates a new client handler.
         * 
         * @param socket The socket connection to the client
         * @param server Reference to the parent server instance
         */
        public ClientHandler(Socket socket, Server server) {
            this.socket = socket;
            this.clientId = clientCounter++;
        }

        /**
         * Main execution method for the client handler thread.
         * Handles the complete lifecycle of a client connection:
         * 1. Prompts for and receives username
         * 2. Announces user joining
         * 3. Sends chat history to the new user
         * 4. Receives and broadcasts messages
         * 5. Announces user leaving and cleans up
         */
        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(
                            new OutputStreamWriter(socket.getOutputStream()), true)) {

                this.out = writer;

                // Get username from client
                out.println("Enter your name: ");
                username = reader.readLine();

                if (username == null || username.trim().isEmpty()) {
                    return;
                }

                // Announce new user joining
                String joinMessage = username + " has joined the chat.";
                System.out.println(joinMessage);

                // Send chat history and join announcement
                sendHistoryToClient(this);
                broadcastMessage(this, joinMessage);

                // Main message receiving loop
                String messageReceived;
                while ((messageReceived = reader.readLine()) != null) {
                    String formattedMessage = username + ": " + messageReceived;
                    System.out.println(formattedMessage);
                    broadcastMessage(this, formattedMessage);
                }

                // Announce user leaving
                String leaveMessage = username + " has left the chat.";
                System.out.println(leaveMessage);
                broadcastMessage(this, leaveMessage);

            } catch (IOException e) {
                System.out.println("Client error: " + e.getMessage());
            } finally {
                // Clean up client resources
                removeClient(this);
                closeSocket();
            }
        }

        /**
         * Sends a message to this client.
         * 
         * @param message The message to send
         */
        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        /**
         * Closes the client socket connection.
         * Called during cleanup to ensure proper resource release.
         */
        private void closeSocket() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}