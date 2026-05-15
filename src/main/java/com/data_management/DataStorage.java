package com.data_management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DataStorage class is responsible for managing patient data, adding
 * new records, and retrieving existing records based on patient ID and time.
 * 
 * It uses a HashMap to store patient data, where the key is the patient ID and
 * the value is a Patient object that contains the patient's records.
 */
public class DataStorage {
    private Map<Integer, Patient> patientMap; 

    /**
     * Constructs a new instance of DataStorage, initializing the 
     * patientMap to an empty HashMap to hold patient data.
     */
    public DataStorage() {
        this.patientMap = new HashMap<>();
    }

    /**
     * Adds or updates patient data in the storage.
     * If the patient does not exist, a new Patient object is created and added to
     * the storage.
     * Otherwise, the new data is added to the existing patient's records.
     *
     * @param patientId the unique identifier of the patient
     * @param measurementValue the value of the health metric being recorded
     * @param recordType the type of record, e.g., "HeartRate",
     *                   "BloodPressure"
     * @param timestamp the time at which the measurement was taken in
     *                  milliseconds
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            patient = new Patient(patientId);
            patientMap.put(patientId, patient);
        }
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a list of PatientRecord objects for a specific patient, filtered by
     * a time range.
     *
     * @param patientId the unique identifier of the patient whose records are to be
     *                  retrieved
     * @param startTime the start of the time range in milliseconds
     * @param endTime the end of the time range in milliseconds
     * @return a list of PatientRecord objects that fall within the specified time
     *         range
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        return new ArrayList<>(); // return an empty list if no patient is found
    }

    /**
     * Retrieves a collection of all patients stored in the data storage.
     *
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }
}
