package com.alerts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    /**
     * Constructs an AlertGenerator with the specified data storage system. The
     * data storage system is used to access patient data for evaluation.
     * 
     * @param dataStorage the data storage system that provides access to the patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the patient data to identify conditions that warrant alerts. 
     * Retrieves all relevant patient records and applies various checks to
     * determine if any alert conditions are met.
     *
     * @param patient the patient whose data is being evaluated for potential alerts
     */
    public void evaluateData(Patient patient) {
        long now = System.currentTimeMillis();
        // Use a wide window to capture all relevant records
        long windowStart = 0;
        List<PatientRecord> allRecords = patient.getRecords(windowStart, now);
 
        checkBloodPressureAlerts(patient, allRecords);
        checkBloodSaturationAlerts(patient, allRecords);
        checkHypotensiveHypoxemiaAlert(patient, allRecords);
        checkEcgAlerts(patient, allRecords);
        checkTriggeredAlerts(patient, allRecords);
    }
    
    /**
     * Checks for blood pressure related alerts based on the patient's records. This includes
     * critical high/low thresholds for systolic and diastolic pressure, as well as
     * trends indicating rapid increases or decreases in blood pressure over time.
     * 
     * @param patient the patient whose blood pressure records are being evaluated 
     * @param records the list of the patient's records
     */
    private void checkBloodPressureAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> systolicRecords = filterByType(records, "SystolicPressure");
        List<PatientRecord> diastolicRecords = filterByType(records, "DiastolicPressure");
 
        checkPressureTrend(patient, systolicRecords, "SystolicPressure");
        checkPressureTrend(patient, diastolicRecords, "DiastolicPressure");
 
        for (PatientRecord record : systolicRecords) {
            double val = record.getMeasurementValue();
            if (val > 180) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Systolic Pressure High: " + val + " mmHg", record.getTimestamp()));
            } else if (val < 90) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Systolic Pressure Low: " + val + " mmHg", record.getTimestamp()));
            }
        }
 
        for (PatientRecord record : diastolicRecords) {
            double val = record.getMeasurementValue();
            if (val > 120) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Diastolic Pressure High: " + val + " mmHg", record.getTimestamp()));
            } else if (val < 60) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Diastolic Pressure Low: " + val + " mmHg", record.getTimestamp()));
            }
        }
    }
 
    /**
     * Checks for trends in blood pressure measurements that indicate rapid increases or decreases over time.
     * 
     * @param patient the patient whose blood pressure records are being evaluated 
     * @param records the list of the patient's records
     * @param type the type of blood pressure measurement (systolic or diastolic)
     */
    private void checkPressureTrend(Patient patient, List<PatientRecord> records, String type) {
        if (records.size() < 3) {
            return;
        }
        for (int i = 0; i <= records.size() - 3; i++) {
            double v1 = records.get(i).getMeasurementValue();
            double v2 = records.get(i + 1).getMeasurementValue();
            double v3 = records.get(i + 2).getMeasurementValue();
 
            boolean increasing = (v2 - v1 > 10) && (v3 - v2 > 10);
            boolean decreasing = (v1 - v2 > 10) && (v2 - v3 > 10);
 
            if (increasing) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), type + " Increasing Trend Alert", records.get(i + 2).getTimestamp()));
            } else if (decreasing) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), type + " Decreasing Trend Alert", records.get(i + 2).getTimestamp()));
            }
        }
    }

    /**
     * Checks for  blood saturation levels and triggers alerts based on the patient's records. 
     * 
     * @param patient the patient whose records are being evaluated
     * @param records the list of the patient's records
     */
    private void checkBloodSaturationAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> satRecords = filterByType(records, "Saturation");
 
        for (PatientRecord record : satRecords) {
            double saturation = record.getMeasurementValue();
            if (saturation < 92) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Low Blood Saturation Alert: " + saturation + "%", record.getTimestamp()));
            }
        }
 
        long tenMinutes = TimeUnit.MINUTES.toMillis(10);
        for (int i = 0; i < satRecords.size(); i++) {
            double startSat = satRecords.get(i).getMeasurementValue();
            long startTime = satRecords.get(i).getTimestamp();
 
            for (int j = i + 1; j < satRecords.size(); j++) {
                long endTime = satRecords.get(j).getTimestamp();
                if (endTime - startTime > tenMinutes) {
                    break;
                }
                double endSat = satRecords.get(j).getMeasurementValue();
                if (startSat - endSat >= 5) {
                    triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Rapid Blood Saturation Drop Alert: dropped " + (startSat - endSat) + "% in 10 min",
                    satRecords.get(j).getTimestamp()));
                }
            }
        }
    }

    /**
     * Checks for the combination of low blood pressure and low blood saturation and triggers
     * an alert if both conditions are met.
     * 
     * @param patient the patient whose records are being evaluated
     * @param records the list of the patient's records
     */
    private void checkHypotensiveHypoxemiaAlert(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> systolicRecords = filterByType(records, "SystolicPressure");
        List<PatientRecord> satRecords = filterByType(records, "Saturation");
 
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
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()),"Hypotensive Hypoxemia Alert: Low BP and Low O2 Saturation",
                System.currentTimeMillis()
            ));
        }
    }
 
    /**
     * Checks for abnormal ECG peaks by analyzing the ECG records for the patient and triggers alerts.
     * 
     * @param patient the patient whose ECG records are being evaluated
     * @param records the list of the patient's records
     */
    private void checkEcgAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> ecgRecords = filterByType(records, "ECG");
 
        if (ecgRecords.size() < 10) {
            return;
        }
 
        int windowSize = 10;
        for (int i = windowSize; i < ecgRecords.size(); i++) {

            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += ecgRecords.get(j).getMeasurementValue();
            }
            double avg = sum / windowSize;

            double variance = 0;
            for (int j = i - windowSize; j < i; j++) {
                double diff = ecgRecords.get(j).getMeasurementValue() - avg;
                variance += diff * diff;
            }
            double stdDev = Math.sqrt(variance / windowSize);
 
            double currentValue = ecgRecords.get(i).getMeasurementValue();
            double threshold = avg + 2 * stdDev;
            if (stdDev > 0 && Math.abs(currentValue - avg) > threshold) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()),"Abnormal ECG Peak Alert: value=" + currentValue + " avg=" + avg,
                ecgRecords.get(i).getTimestamp()));
            }
        }
    }

    /**
     * Checks for manually triggered alerts by the patient or nursing staff. 
     * 
     * @param patient the patient whose records are being evaluated for manually triggered alerts
     * @param records the list of the patient's records
     */
    private void checkTriggeredAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> alertRecords = filterByType(records, "Alert");
        for (PatientRecord record : alertRecords) {
            if (record.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()),"Manual Alert Triggered by Patient/Nurse", record.getTimestamp()));
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
    
    /** 
     * Filters the list of patient records by the specified record type. 
     * 
     * @param records the list of patient records to filter
     * @param recordType the type of record to filter by
     * @return a list of patient records that match the specified record type
     */
    private List<PatientRecord> filterByType(List<PatientRecord> records, String recordType) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : records) {
            if (recordType.equals(r.getRecordType())) {
                result.add(r);
            }
        }
        return result;
    }
 
}
