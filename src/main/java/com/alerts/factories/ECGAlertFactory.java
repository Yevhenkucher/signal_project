package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory for creating ECG alerts.
 */
public class ECGAlertFactory extends AlertFactory {

    /**
     * Creates an ECG alert.
     *
     * @param patientId the patient ID
     * @param condition the ECG condition
     * @param timestamp the time of the alert
     * @return a new Alert for ECG
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "ECG: " + condition, timestamp);
    }
}
