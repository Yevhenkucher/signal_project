package com.alerts.strategies;

import java.util.List;
import com.alerts.Alert;
import com.alerts.RecordUtils;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Checks blood pressure readings for trend violations and critical threshold breaches.
 */
public class BloodPressureStrategy implements AlertStrategy {

    /**
     * Checks for blood pressure trends (3 consecutive changes > 10 mmHg) or
     * critical thresholds (systolic > 180 / < 90, diastolic > 120 / < 60).
     *
     * @param patient the patient to evaluate
     * @return an Alert if a condition is triggered, null otherwise
     */
    @Override
    public Alert checkAlert(Patient patient) {
        List<PatientRecord> systolicRecords = RecordUtils.filterByType(patient.getRecords(0, System.currentTimeMillis()), "SystolicPressure");
        List<PatientRecord> diastolicRecords = RecordUtils.filterByType(patient.getRecords(0, System.currentTimeMillis()), "DiastolicPressure");
 
        Alert systolicAlert = checkPressureTrend(patient, systolicRecords, "SystolicPressure");
        if (systolicAlert != null) {
            return systolicAlert;
        }
 
        Alert diastolicAlert = checkPressureTrend(patient, diastolicRecords, "DiastolicPressure");
        if (diastolicAlert != null) {
            return diastolicAlert;
        }
 
        for (PatientRecord record : systolicRecords) {
            double val = record.getMeasurementValue();
            if (val > 180) {
                return new Alert(String.valueOf(patient.getPatientId()), "Critical Systolic Pressure High", record.getTimestamp());
            } else if (val < 90) {
                return new Alert(String.valueOf(patient.getPatientId()), "Critical Systolic Pressure Low", record.getTimestamp());
            }
        }
 
        for (PatientRecord record : diastolicRecords) {
            double val = record.getMeasurementValue();
            if (val > 120) {
                return new Alert(String.valueOf(patient.getPatientId()), "Critical Diastolic Pressure High", record.getTimestamp());
            } else if (val < 60) {
                return new Alert(String.valueOf(patient.getPatientId()), "Critical Diastolic Pressure Low", record.getTimestamp());
            }
        }
        return null;
    }

    /**
     * Checks for trends in blood pressure measurements that indicate rapid increases or decreases over time.
     * 
     * @param patient the patient whose blood pressure records are being evaluated 
     * @param records the list of the patient's records
     * @param type the type of blood pressure measurement (systolic or diastolic)
     * @return an Alert if a trend is detected or null otherwise
     */
    private Alert checkPressureTrend(Patient patient, List<PatientRecord> records, String type) {
        if (records.size() < 3) {
            return null;
        }
        for (int i = 0; i <= records.size() - 3; i++) {
            double v1 = records.get(i).getMeasurementValue();
            double v2 = records.get(i + 1).getMeasurementValue();
            double v3 = records.get(i + 2).getMeasurementValue();
 
            boolean increasing = (v2 - v1 > 10) && (v3 - v2 > 10);
            boolean decreasing = (v1 - v2 > 10) && (v2 - v3 > 10);
 
            if (increasing) {
                return new Alert(String.valueOf(patient.getPatientId()), type + " Increasing Trend Alert", records.get(i + 2).getTimestamp());
            } else if (decreasing) {
                return new Alert(String.valueOf(patient.getPatientId()), type + " Decreasing Trend Alert", records.get(i + 2).getTimestamp());
            }
        }
        return null;
    }

}
