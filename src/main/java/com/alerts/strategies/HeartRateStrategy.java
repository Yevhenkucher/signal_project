package com.alerts.strategies;

import java.util.List;

import com.alerts.Alert;
import com.alerts.RecordUtils;
import com.data_management.Patient;
import com.data_management.PatientRecord;


/**
 * Checks ECG data for abnormal peaks using a sliding window average.
 */
public class HeartRateStrategy implements AlertStrategy{

        
    /**
     * Checks for abnormal ECG peaks by analyzing the ECG records for the patient and triggers alerts.
     * 
     * @param patient the patient whose ECG records are being evaluated
     * @return an Alert if a condition is triggered, null otherwise
     */
    @Override
    public Alert checkAlert(Patient patient) {
        List<PatientRecord> ecgRecords = RecordUtils.filterByType(patient.getRecords(0, System.currentTimeMillis()), "ECG");
 
        if (ecgRecords.size() < 10) {
            return null;
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
                return new Alert(String.valueOf(patient.getPatientId()),"Abnormal ECG Alert", ecgRecords.get(i).getTimestamp());
            }
        }
        return null;
    }
}
