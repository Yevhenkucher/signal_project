package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

class DataStorageTest {

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        // Use a fresh instance for each test
        storage = DataStorage.getInstance();
        storage.clearAll();
    }

    @Test
    void testAddAndGetRecords() {
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
        assertEquals(200.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testGetRecordsEmptyForUnknownPatient() {
        List<PatientRecord> records = storage.getRecords(9999, 0, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecordsTimeRangeFilter() {
        storage.addPatientData(2, 50.0, "HeartRate", 1000L);
        storage.addPatientData(2, 60.0, "HeartRate", 2000L);
        storage.addPatientData(2, 70.0, "HeartRate", 3000L);

        List<PatientRecord> records = storage.getRecords(2, 1500L, 2500L);
        assertEquals(1, records.size());
        assertEquals(60.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetAllPatients() {
        storage.addPatientData(10, 98.0, "Saturation", 1000L);
        assertFalse(storage.getAllPatients().isEmpty());
    }
}
