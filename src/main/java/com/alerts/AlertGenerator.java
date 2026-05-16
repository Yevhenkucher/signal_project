package com.alerts;

import java.util.ArrayList;
import java.util.List;

import com.alerts.strategies.AlertStrategy;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.alerts.strategies.OxygenSaturationStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Evaluates patient data and triggers alerts when health conditions are detected.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private List<AlertStrategy> strategies;

    /**
     * Constructs an AlertGenerator with the given data storage.
     *
     * @param dataStorage the data storage providing patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.strategies = new ArrayList<>();
        strategies.add(new BloodPressureStrategy());
        strategies.add(new OxygenSaturationStrategy());
        strategies.add(new HeartRateStrategy());
    }

    /**
     * Evaluates all alert conditions for the given patient.
     * Triggers an alert for each condition that is met.
     *
     * @param patient the patient to evaluate
     */
    public void evaluateData(Patient patient) {
        for (AlertStrategy strategy : strategies) {
            Alert alert = strategy.checkAlert(patient);
            if (alert != null) {
                triggerAlert(alert);
            }
        }

        checkHypotensiveHypoxemiaAlert(patient);
        checkTriggeredAlert(patient);
    }

    /**
     * Checks for the combined Hypotensive Hypoxemia condition.
     *
     * @param patient the patient to check
     */
    private void checkHypotensiveHypoxemiaAlert(Patient patient) {
        List<PatientRecord> systolicRecords = RecordUtils.filterByType(patient.getRecords(0, System.currentTimeMillis()), "SystolicPressure");
        List<PatientRecord> satRecords = RecordUtils.filterByType(patient.getRecords(0, System.currentTimeMillis()), "Saturation");
 
        boolean lowBP = false;
        for (PatientRecord r : systolicRecords) {
            if (r.getMeasurementValue() < 90) {
                lowBP = true;
                break;
            }
        }

        boolean lowSat = false;
        for (PatientRecord r : satRecords) {
            if (r.getMeasurementValue() < 92) {
                lowSat = true;
                break;
            }
        }

        if (lowBP && lowSat) {
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()),"Hypotensive Hypoxemia Alert",
                System.currentTimeMillis()
            ));
        }
    }
 
    /**
     * Checks if a manual (nurse/patient button) alert has been triggered.
     *
     * @param patient the patient to check
     */
    private void checkTriggeredAlert(Patient patient) {
        List<PatientRecord> alertRecords = RecordUtils.filterByType(patient.getRecords(0, System.currentTimeMillis()), "Alert");
        for (PatientRecord record : alertRecords) {
            if (record.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()),"Manual Alert Triggered", record.getTimestamp()));
            }
        }
    }
 
    /**
     * Triggers an alert for the monitoring system. 
     *
     * @param alert the alert to trigger
     */
    private void triggerAlert(Alert alert) {
        System.out.println("[ALERT] Patient " + alert.getPatientId() + " | Condition: " + alert.getCondition()
                            + " | Time: " + alert.getTimestamp());
    }
 
}
