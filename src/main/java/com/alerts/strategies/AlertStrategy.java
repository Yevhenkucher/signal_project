package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;

/**
 * Strategy interface for checking alert conditions on patient data.
 */
public interface AlertStrategy {

    /**
     * Checks patient data and returns an Alert if the condition is met or null if the reading is normal.
     *
     * @param patient the patient to check
     * @return an Alert if the condition is triggered or null otherwise
     */
    Alert checkAlert(Patient patient);
}
