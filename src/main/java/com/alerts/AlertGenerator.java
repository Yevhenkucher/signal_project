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
 * Responsible for generating alerts based on patient data. This class evaluates
 * patient records to identify conditions that warrant alerts, such as critical
 * vital sign thresholds, rapid changes in measurements, or specific combinations
 * of conditions. 
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private List<AlertStrategy> strategies;

    /**
     * Constructs an AlertGenerator with the specified data storage system. The
     * data storage system is used to access patient data for evaluation.
     * 
     * @param dataStorage the data storage system that provides access to the patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.strategies = new ArrayList<>();
        strategies.add(new BloodPressureStrategy());
        strategies.add(new OxygenSaturationStrategy());
        strategies.add(new HeartRateStrategy());
    }

    /**
     * Evaluates the patient data to identify conditions that warrant alerts. 
     * Retrieves all relevant patient records and applies various checks to
     * determine if any alert conditions are met.
     *
     * @param patient the patient whose data is being evaluated for potential alerts
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
     * Checks for the combination of low blood pressure and low blood saturation and triggers
     * an alert if both conditions are met.
     * 
     * @param patient the patient whose records are being evaluated
     * @param records the list of the patient's records
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
     * Checks for manually triggered alerts by the patient or nursing staff. 
     * 
     * @param patient the patient whose records are being evaluated for manually triggered alerts
     * @param records the list of the patient's records
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
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("[ALERT] Patient " + alert.getPatientId() + " | Condition: " + alert.getCondition()
                            + " | Time: " + alert.getTimestamp());
    }
 
}
