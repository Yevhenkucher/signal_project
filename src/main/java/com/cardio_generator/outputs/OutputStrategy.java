package com.cardio_generator.outputs;

/**
 * Defines a standard contract for transmitting or recording generated health data.
 * Implementations of this interface handle the actual delivery of data to various
 * destinations such as consoles, files, or network sockets.
 */
public interface OutputStrategy {

    /**
     * Outputs a specific data point for a patient.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time at which the data was recorded, in milliseconds
     * @param label the category of the data (e.g., "Alert", "ECG", "BloodPressure")
     * @param data the actual value or status being recorded
     */
    void output(int patientId, long timestamp, String label, String data);
}
