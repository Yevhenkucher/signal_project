package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import java.util.Random;

/**
 * Generates simulated blood oxygen saturation (SpO2) data for patients.
 * The generator maintains a state for each patient, starting from a healthy baseline
 * and introducing small, realistic fluctuations over time.
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private int[] lastSaturationValues;

    /**
     * Constructs a new BloodSaturationDataGenerator and initializes baseline values.
     * Each patient is assigned an initial saturation level between 95% and 100%.
     *
     * @param patientCount the total number of patients to initialize data for
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6);
        }
    }

    /**
     * Generates a new blood saturation data point for a specific patient.
     * Fluctuates the previous value by ±1% and ensures the result stays
     * within the physiological range of 90% to 100%.
     *
     * @param patientId the unique identifier of the patient
     * @param outputStrategy the mechanism used to transmit or record the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            int variation = random.nextInt(3) - 1;
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Clamp values between 90 and 100
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;

            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    newSaturationValue + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
