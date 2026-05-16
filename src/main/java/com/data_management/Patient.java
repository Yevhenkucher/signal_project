package com.data_management;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a patient and manages their medical records.
 */
public class Patient {
    private int patientId;
    private List<PatientRecord> patientRecords;

    /**
     * Constructs a new Patient with the given ID.
     *
     * @param patientId the unique identifier for the patient
     */
    public Patient(int patientId) {
        this.patientId = patientId;
        this.patientRecords = new ArrayList<>();
    }

    /**
     * Adds a new record to this patient's records.
     *
     * @param measurementValue the value of the measurement
     * @param recordType the type of measurement
     * @param timestamp the time of measurement in milliseconds 
     */
    public void addRecord(double measurementValue, String recordType, long timestamp) {
        PatientRecord record = new PatientRecord(this.patientId, measurementValue, recordType, timestamp);
        this.patientRecords.add(record);
    }

    /**
     * Returns records within the specified time range.
     *
     * @param startTime the start of the time range in milliseconds
     * @param endTime the end of the time range in milliseconds
     * @return a list of records within the time range
     */
    public List<PatientRecord> getRecords(long startTime, long endTime) {
        List<PatientRecord> recordsInRange = new ArrayList<>();
        for (PatientRecord record : patientRecords) {
            if (record.getTimestamp() >= startTime && record.getTimestamp() <= endTime) {
                recordsInRange.add(record);
            }
        }
        return recordsInRange;
    }

    /**
    * Retrieves the unique identifier of this patient.
    *
    * @return the patient ID
    */
    public int getPatientId() {
        return patientId;
    }
}
