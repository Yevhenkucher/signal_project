package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads patient data from text files in a specified directory.
 * Each line of the file is expected to be in the format:
 * patientId, measurementValue, recordType, timestamp
 */
public class FileDataReader implements DataReader {

    private final String directoryPath;

    /**
     * Constructs a FileDataReader with the specified directory path.
     *
     * @param directoryPath the path to the directory containing all the patient data files
     */
    public FileDataReader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Reads all of the data in the provided directory and stores it in 
     * the given DataStorage instance.
     *
     * @param dataStorage the DataStorage where the read data will be stored
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
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        if (line.isBlank()) {
                            continue;
                        }

                        try {
                            String[] parts = line.split(", ");
                            int patientId = Integer.parseInt(parts[0].replace("Patient ID: ", "").trim());
                            long timestamp = Long.parseLong(parts[1].replace("Timestamp: ", "").trim());
                            String recordType = parts[2].replace("Label: ", "").trim();
                            double value = Double.parseDouble(parts[3].replace("Data: ", "").trim());
                            dataStorage.addPatientData(patientId, value, recordType, timestamp);
                        } catch (Exception e) {
                            System.err.println("Skipping malformed line: " + line);
                        }
                    } 
                }
            }
        }
    }
}