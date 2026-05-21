package com.data_management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alerts.AlertGenerator;

/**
 * Manages storage and retrieval of patient data.
 * Implemented as a singleton to ensure a single shared data store.
 */
public class DataStorage {

    private static DataStorage instance;
    private Map<Integer, Patient> patientMap; 

    /** Private constructor for singleton. */
    private DataStorage() {
        this.patientMap = new HashMap<>();
    }

    /**
     * Returns the single instance of DataStorage.
     *
     * @return the singleton DataStorage instance
     */
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }


    /**
     * Adds or updates patient data in storage.
     *
     * @param patientId the unique identifier of the patient
     * @param measurementValue the measurement value
     * @param recordType the type of record
     * @param timestamp the time of measurement in milliseconds
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
     * Retrieves patient records within a time range.
     *
     * @param patientId the patient's ID
     * @param startTime the start of the range in milliseconds
     * @param endTime the end of the range in milliseconds
     * @return a list of matching records, or empty if the patient is not found
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        return new ArrayList<>(); // return an empty list if no patient is found
    }

    /**
     * Returns all patients in storage.
     *
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * Main method: demonstrates reading and evaluating stored data.
     *
     * @param args commandline arguments
     */
    public static void main(String[] args) {
        DataStorage storage = DataStorage.getInstance();

        List<PatientRecord> records = storage.getRecords(1, 1700000000000L, 1800000000000L);
        for (PatientRecord record : records) {
            System.out.println("Record for Patient ID: " + record.getPatientId()
                    + ", Type: " + record.getRecordType()
                    + ", Data: " + record.getMeasurementValue()
                    + ", Timestamp: " + record.getTimestamp());
        }

        AlertGenerator alertGenerator = new AlertGenerator(storage);
        for (Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }
    }
    
    /**
     * Clears all stored patients and records.
     * Intended for unit testing only.
     */
    public void clearAll() {
        patientMap.clear();
    }
}
