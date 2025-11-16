package org.example;

import java.io.IOException;

public class ClientMain {
    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) {
        String address = DEFAULT_ADDRESS;
        int port = DEFAULT_PORT;

        Client client = new Client(address, port);
        try {
            client.connect();
        } catch (IOException e) {
            client.shutdown();
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}