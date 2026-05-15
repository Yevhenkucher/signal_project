package com.alerts;

/**
 * Represents an alert triggered for a patient based on certain conditions.
 */
public class Alert {
    private String patientId;
    private String condition;
    private long timestamp;

    /**
     * Creates a new Alert instance with the specified patient ID, condition, and timestamp.
     * 
     * @param patientId patient's unique identifier
     * @param condition the condition that triggered the alert
     * @param timestamp the time when the alert was triggered in milliseconds
     */
    public Alert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    /**
     * Returns the patient ID associated with this alert.
     * 
     * @return the patient ID
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Returns the condition that triggered this alert.
     * 
     * @return the alert condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Returns the timestamp when this alert was triggered.
     * 
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
}
