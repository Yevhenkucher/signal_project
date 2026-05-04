package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Each data label is stored in its own file within a specified base directory.
 * This class ensures that the directory structure exists before writing and
 * appends new data to existing files to prevent data loss.
 */
public class FileOutputStrategy implements OutputStrategy {

    // Changed to lowerCamelCase
    private String baseDirectory;

    // Changed from file_map to lowerCamelCase
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();
    /**
     * Constructs a new FileOutputStrategy with a target directory.
     *
     * @param baseDirectory the path to the directory where data files will be stored
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;  // Removed unnecessary blank line after opening brace
    }
    /**
     * Writes a data point to a text file named after the provided label.
     * The file is created if it does not exist, and new entries are appended to the end.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time the data was recorded
     * @param label the category of data (e.g., "Alert"), which determines the filename
     * @param data the specific health data value or status to be recorded
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // The comments should be meaningful
            // Ensure the target directory exists before attempting to write files
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // The comments should be meaningful
        // Retrieve the file path from the map or create a new one if this label is new
        // Changed local variable FilePath to lowerCamelCase
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // The comments should be meaningful
        // Open the file in append mode to ensure old data is not overwritten
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}


