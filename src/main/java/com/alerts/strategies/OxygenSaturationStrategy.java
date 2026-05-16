package com.alerts.strategies;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alerts.Alert;
import com.alerts.RecordUtils;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Checks blood oxygen saturation for low levels or rapid drops.
 */
public class OxygenSaturationStrategy implements AlertStrategy{

    /**
     * Checks for  blood saturation levels and triggers alerts based on the patient's records. 
     * 
     * @param patient the patient whose records are being evaluated
     * @return an Alert if a condition is triggered, null otherwise
     */
    @Override
    public Alert checkAlert(Patient patient) {
                List<PatientRecord> satRecords = RecordUtils.filterByType(patient.getRecords(0, System.currentTimeMillis()), "Saturation");
 
        for (PatientRecord record : satRecords) {
            double saturation = record.getMeasurementValue();
            if (saturation < 92) {
                return new Alert(String.valueOf(patient.getPatientId()), "Low Blood Saturation Alert", record.getTimestamp());
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
                    return new Alert(String.valueOf(patient.getPatientId()), "Rapid Blood Saturation Drop Alert", satRecords.get(j).getTimestamp());
                }
            }
        }
        return null;
    }
}
