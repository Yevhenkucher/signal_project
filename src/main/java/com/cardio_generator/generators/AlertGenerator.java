package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;
import java.util.Random; // Alphabetical imports, no extra empty lines between imports
/**
 * Generates simulated alert events for patients based on probabilistic models.
 * This generator tracks the alert state of each patient and simulates both the
 * triggering of new alerts and the resolution of existing ones.
 */
public class AlertGenerator implements PatientDataGenerator {

    public static final Random randomGenerator = new Random();
    // Instance fields must be lowerCamelCase
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Initializes the AlertGenerator for a specific number of patients.
     *
     * @param patientCount the total number of patients to track alert states for
     */
    public AlertGenerator(int patientCount) {
        // Braces must follow K&R style (new line for block content)
        this.alertStates = new boolean[patientCount + 1];
    }
    /**
     * Generates or resolves alerts for a specific patient based on probability.
     * If a patient has an active alert, there is a 90% chance it will resolve.
     * If no alert is active, a new alert is triggered based on a Poisson
     * distribution probability.
     *
     * @param patientId the unique identifier of the patient
     * @param outputStrategy the mechanism used to record or transmit alert events
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // The comments should be meaningful
                    // Sending a data point about a patient and his state
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Local variables must be lowerCamelCase
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // The comments should be meaningful
                    // Sending a data point about a patient and his state
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}



