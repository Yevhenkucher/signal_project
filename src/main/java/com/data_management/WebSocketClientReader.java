package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;

/**
 * Connects to a WebSocket server to receive real-time patient data
 * and store it in DataStorage.
 */
public class WebSocketClientReader implements DataReader {

    private String serverURI;
    private WebSocketClient client;

    /**
     * Constructs a WebSocketClientReader for the given server URI.
     *
     * @param serverUri the URI of the WebSocket server 
     */
    public WebSocketClientReader(String serverUri) {
        this.serverURI = serverUri;
    }

    /**
     * Connects to the WebSocket server and gets patient data and
     * stores it in the provided DataStorage.
     *
     * @param dataStorage the storage where incoming data will be stored
     * @throws IOException if the connection fails
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        try {
            client = new WebSocketClient(new URI(serverURI)) {

                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to WebSocket server: " + serverURI);
                }

                @Override
                public void onMessage(String message) {
                    parseAndStore(message, dataStorage);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket connection closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                }
            };

            client.connect();
        } catch (Exception e) {
            throw new IOException("Failed to connect to WebSocket server: " + serverURI, e);
        }
    }

    /**
     * Parses comma-separated data and stores the record in DataStorage.
     * Expected format: "patientId,timestamp,label,data"
     *
     * @param data the raw data from the WebSocket server
     * @param dataStorage the storage to add the record to
     */
    private void parseAndStore(String data, DataStorage dataStorage) {

        try {
            String[] parts = data.split(",");
            if (parts.length < 4) {
                System.err.println("Incorrect message format: " + data);
                return;
            }
            int patientId = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String recordType = parts[2].trim();
            if (recordType.isEmpty()) {
                System.err.println("Blank record type, skipping: " + data);
                return;
            }
            String stringMeasurement = parts[3].trim().replace("%", "");
            double measurementValue = Double.parseDouble(stringMeasurement);

            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
        } catch (Exception e) {
            System.err.println("Error parsing WebSocket message: " + data + " - " + e.getMessage());
        }
    }

    /**
     * Closes the WebSocket connection if open.
     */
    public void close() {
        if (client != null && client.isOpen()) {
            client.close();
        }
    }
}
