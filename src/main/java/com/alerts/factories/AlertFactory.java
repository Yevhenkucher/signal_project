package com.alerts.factories;

import com.alerts.Alert;

/**
 * Abstract factory for creating alert instances.
 */
public abstract class AlertFactory {

    /**
     * Creates an Alert for the given patient, condition, and timestamp.
     *
     * @param patientId the patient ID
     * @param condition the condition that triggered the alert
     * @param timestamp the time of the alert in milliseconds since epoch
     * @return a new Alert instance
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);

}
