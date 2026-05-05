package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads patient data from text files in a specified directory.
 * Each line of the file is expected to be in the format:
 * patientId,measurementValue,recordType,timestamp
 */
public class FileDataReader implements DataReader {

    private final String directoryPath;

    /**
     * Constructs a FileDataReader targeting the specified directory.
     *
     * @param directoryPath the path to the directory containing data files
     */
    public FileDataReader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Reads all files in the directory and loads their data into the provided DataStorage.
     *
     * @param dataStorage the storage where the parsed data will be stored
     * @throws IOException if the directory cannot be read or no files are found
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        File dir = new File(directoryPath);

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            throw new IOException("No files found in directory: " + directoryPath);
        }

        for (File file : files) {
            if (file.isFile()) {
                parseFile(file, dataStorage);
            }
        }
    }

    /**
     * Parses a single file and loads each valid line into data storage.
     * Lines with unexpected formats are skipped with a warning.
     *
     * @param file        the file to parse
     * @param dataStorage the storage to populate
     * @throws IOException if the file cannot be read
     */
    private void parseFile(File file, DataStorage dataStorage) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // Expected format: "Patient ID: X, Timestamp: Y, Label: Z, Data: W"
                // Also support plain CSV: X,Y,Z,W
                try {
                    if (line.startsWith("Patient ID:")) {
                        parseFormattedLine(line, dataStorage);
                    } else {
                        parseCsvLine(line, dataStorage);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Skipping malformed line " + lineNumber
                            + " in " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses a line in the format output by FileOutputStrategy:
     * "Patient ID: X, Timestamp: Y, Label: Z, Data: W"
     */
    private void parseFormattedLine(String line, DataStorage dataStorage) {
        // Split on ", " to get key:value pairs
        String[] parts = line.split(", ");
        int patientId = Integer.parseInt(parts[0].replace("Patient ID: ", "").trim());
        long timestamp = Long.parseLong(parts[1].replace("Timestamp: ", "").trim());
        String recordType = parts[2].replace("Label: ", "").trim();
        String dataStr = parts[3].replace("Data: ", "").trim();

        double measurementValue = parseDataValue(dataStr);
        dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
    }

    /**
     * Parses a plain CSV line: patientId,measurementValue,recordType,timestamp
     */
    private void parseCsvLine(String line, DataStorage dataStorage) {
        String[] parts = line.split(",");
        int patientId = Integer.parseInt(parts[0].trim());
        double measurementValue = Double.parseDouble(parts[1].trim());
        String recordType = parts[2].trim();
        long timestamp = Long.parseLong(parts[3].trim());
        dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
    }

    /**
     * Converts a data string to a double measurement value.
     * Handles percentage values (e.g., "95%") and alert status strings.
     *
     * @param dataStr the raw data string from the file
     * @return the numeric measurement value
     */
    private double parseDataValue(String dataStr) {
        if (dataStr.endsWith("%")) {
            // Blood saturation stored as "95%"
            return Double.parseDouble(dataStr.replace("%", "").trim());
        }
        if ("triggered".equalsIgnoreCase(dataStr)) {
            return 1.0;
        }
        if ("resolved".equalsIgnoreCase(dataStr)) {
            return 0.0;
        }
        return Double.parseDouble(dataStr);
    }
}