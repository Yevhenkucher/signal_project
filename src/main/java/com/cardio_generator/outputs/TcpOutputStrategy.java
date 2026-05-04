package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * This strategy starts a server on a specified port and waits for a single client to connect.
 * Once connected, data points are transmitted as comma-separated strings.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Initializes a TCP server on the specified port.
     * Starts a background thread to listen for and accept an incoming client connection
     * to avoid blocking the main simulation execution.
     *
     * @param port the network port number on which the server will listen
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a data point to the connected TCP client.
     * The data is formatted as a comma-separated string. If no client is connected,
     * the data point is silently ignored.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time at which the data was recorded
     * @param label the category of the data (e.g., "Alert", "ECG")
     * @param data the specific health data value or status
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
