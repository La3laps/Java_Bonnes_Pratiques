package org.example;

import java.io.IOException;

/**
 * Main entry point for the chat server application.
 * Handles command-line argument parsing and server initialization.
 */
public class ServerMain {
    /** Default port number if none is specified via command-line arguments */
    private static final int DEFAULT_PORT = 12345;

    /**
     * Starts the chat server.
     * 
     * @param args Command-line arguments. First argument can specify port number.
     *             If no port is specified or invalid, defaults to 12345.
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        

        // Create and start the server
        Server server = new Server(port);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}