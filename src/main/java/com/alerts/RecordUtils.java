package com.alerts;

import java.util.ArrayList;
import java.util.List;

import com.data_management.PatientRecord;

public final class RecordUtils {

    /**
     * Filters a list of patient records by the specified record type.
     */
    private RecordUtils() {
        // Private constructor to prevent instantiation
    }

    /** 
     * Filters the list of patient records by the specified record type. 
     * 
     * @param records the list of patient records to filter
     * @param recordType the type of record to filter by
     * @return a list of patient records that match the specified record type
     */
    public static List<PatientRecord> filterByType(List<PatientRecord> records, String recordType) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : records) {
            if (recordType.equals(r.getRecordType())) {
                result.add(r);
            }
        }
        return result;
    }
}