package com.data_management;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a patient and manages their medical records.
 * This class stores patient data and provides methods to add new medical 
 * records and retrieve existing records.
 */
public class Patient {
    private int patientId;
    private List<PatientRecord> patientRecords;

    /**
     * Constructs a new Patient with a specified ID.
     * Initializes an empty list of patient records.
     *
     * @param patientId the unique identifier for the patient
     */
    public Patient(int patientId) {
        this.patientId = patientId;
        this.patientRecords = new ArrayList<>();
    }

    /**
     * Adds a new record to this patient's list of medical records.
     * The record is created with the specified measurement value, record type, and
     * timestamp.
     *
     * @param measurementValue the measurement value to store in the record
     * @param recordType the type of record, e.g., "HeartRate",
     *                   "BloodPressure"
     * @param timestamp the time at which the measurement was taken in
     *                  milliseconds
     */
    public void addRecord(double measurementValue, String recordType, long timestamp) {
        PatientRecord record = new PatientRecord(this.patientId, measurementValue, recordType, timestamp);
        this.patientRecords.add(record);
    }

    /**
     * Retrieves a list of PatientRecord objects for this patient that are
     * within the specified time range.
     * The method filters records based on the start and end times provided.
     *
     * @param startTime the start of the time range in milliseconds
     * @param endTime the end of the time range in milliseconds
     * @return a list of PatientRecord objects that fall within the specified time
     *         range
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
