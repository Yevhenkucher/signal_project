package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory for creating blood oxygen alerts.
 */
public class BloodOxygenAlertFactory extends AlertFactory {

    /**
     * Creates a blood oxygen alert.
     *
     * @param patientId the patient ID
     * @param condition the oxygen saturation condition
     * @param timestamp the time of the alert
     * @return a new Alert for blood oxygen
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "BloodOxygen: " + condition, timestamp);
    }
}
