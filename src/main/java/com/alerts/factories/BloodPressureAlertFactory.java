package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory for creating blood pressure alerts.
 */
public class BloodPressureAlertFactory extends AlertFactory {

    /**
     * Creates a blood pressure alert.
     *
     * @param patientId the patient ID
     * @param condition the blood pressure condition
     * @param timestamp the time of the alert
     * @return a new Alert for blood pressure
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "BloodPressure: " + condition, timestamp);
    }
}
