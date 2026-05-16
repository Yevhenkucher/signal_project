package com;

import java.io.IOException;

import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;

/**
 * Main class to run either the DataStorage or HealthDataSimulator based on command-line arguments.
 */
public class Main {
    
    /**
     * Runs either DataStorage or HealthDataSimulator depending on the first argument.
     *
     * @param args pass "DataStorage" to run DataStorage, otherwise runs HealthDataSimulator
     * @throws IOException if the simulator encounters an IO error
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            DataStorage.main(new String[]{});
        } else {
            HealthDataSimulator.main(args);
        }
    }
}
