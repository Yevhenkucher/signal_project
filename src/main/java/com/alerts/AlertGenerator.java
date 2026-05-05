package com.alerts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
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

    private void checkTriggeredAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> alertRecords = filterByType(records, "Alert");
        for (PatientRecord record : alertRecords) {
            if (record.getMeasurementValue() == 1.0) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()),"Manual Alert Triggered by Patient/Nurse", record.getTimestamp()));
            }
        }
    }
 
    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("[ALERT] Patient " + alert.getPatientId() + " | Condition: " + alert.getCondition()
        + " | Time: " + alert.getTimestamp());
    }
    
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
