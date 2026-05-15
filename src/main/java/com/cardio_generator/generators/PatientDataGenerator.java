package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Defines the standard contract for all health data generators.
 * Any class implementing this interface must provide logic to generate
 * simulated physiological data for a specific patient.
 */
public interface PatientDataGenerator {
    /**
     * Generates a single data point for the specified patient and sends it
     * to the provided output strategy.
     *
     * @param patientId the unique identifier of the patient
     * @param outputStrategy the mechanism used to record or transmit the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
