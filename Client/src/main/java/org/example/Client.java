package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chat client that connects to a chat server and enables two-way communication.
 */
public class Client {
    /** Address of the chat server to connect to */
    private final String serverAddress;
    /** Thread pool for managing send/receive threads */
    private ExecutorService executorService;
    private final int serverPort;
    private Socket socket;
    private volatile boolean isConnected = false;
    private final int MSG_MAX_CHAR_LEN = 128;

    /** Timeout in seconds */
    private final int TIMEOUT_TIME = 100;
    private Timer timeout = null;

    /**
     * Creates a new chat client.
     * 
     * @param serverAddress The address of the server to connect to
     * @param serverPort    The port number of the server
     */
    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Connects to the chat server and starts message send/receive threads.
     * Creates two threads:
     * - One for receiving messages from the server
     * - One for sending messages typed by the user
     * 
     * @throws IOException If connection to the server fails
     */
    public void connect() throws IOException {
        socket = new Socket(serverAddress, serverPort);
        isConnected = true;
        executorService = Executors.newFixedThreadPool(2);

        executorService.submit(this::receiveMessages);
        executorService.submit(this::sendMessages);

    }

    /**
     * Continuously receives messages from the server and displays them.
     * Runs in a separate thread until disconnection occurs.
     * This method handles the input stream from the server and prints
     * all received messages to the console.
     */
    private void receiveMessages() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String message;
            while (isConnected && (message = reader.readLine()) != null) {
                System.out.println("\r" + message);
                System.out.print("You: ");
            }
        } catch (IOException e) {
            if (isConnected) {
                System.out.println("\nDisconnected from server");
            }
        } finally {
            shutdown();
        }
    }

    /**
     * Continuously reads user input from console and sends it to the server.
     * Runs in a separate thread until disconnection occurs.
     * Each line typed by the user is sent as a separate message.
     */
    private void sendMessages() {
        setTimeoutClient();

        try (BufferedReader consoleReader = new BufferedReader(
                new InputStreamReader(System.in));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream()))) {

            String input;
            while (isConnected && (input = consoleReader.readLine()) != null) {
                resetTimeoutClient();
                if (input.length() > MSG_MAX_CHAR_LEN) {
                    MessageLogs.printLog(MessageLogs.MSG_TOO_LONG);
                } else {
                    writer.write(input);
                    writer.newLine();
                }

                writer.flush();
                System.out.print("You: ");
            }
        } catch (IOException e) {
            if (isConnected) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        } finally {
            shutdown();
        }
    }

    /**
     * Creates a timer for timeout and informs the user once he's timed out
     */
    private void setTimeoutClient() {
        this.timeout = new Timer();

        timeout.schedule(new TimerTask() {
            @Override
            public void run() {
                Integer time = Integer.valueOf(getTimeout());
                MessageLogs.printInfo(
                        MessageLogs.CLIENT_TIMEOUT + "\n You have been idle for : " + time.toString() + "sec");
                shutdown();
            }
        }, getTimeout());
    }

    private void resetTimeoutClient() {
        if (this.timeout == null)
            return;

        this.timeout.cancel();
        setTimeoutClient();
    }

    private int getTimeout() {
        return TIMEOUT_TIME * 1000;
    }

    /**
     * Performs graceful shutdown of the client.
     * Closes all resources including:
     * - The executor service (thread pool)
     * - The socket connection to the server
     */
    public void shutdown() {
        if (!isConnected) {
            return;
        }

        isConnected = false;

        // Shutdown thread pool
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        // Close socket connection
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}