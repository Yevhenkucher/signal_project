package data_management;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DataStorage, Patient, FileDataReader, and AlertGenerator.
 */
public class DataStorageTest {

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
    }

    // =========================================================================
    // DataStorage Tests
    // =========================================================================

    @Test
    void testAddAndRetrievePatientData() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        assertEquals(1, records.size());
        assertEquals(120.0, records.get(0).getMeasurementValue());
        assertEquals("SystolicPressure", records.get(0).getRecordType());
    }

    @Test
    void testAddMultipleRecordsForSamePatient() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 80.0, "DiastolicPressure", 2000L);
        storage.addPatientData(1, 95.0, "Saturation", 3000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 5000L);
        assertEquals(3, records.size());
    }

    @Test
    void testGetRecordsForNonExistentPatient() {
        List<PatientRecord> records = storage.getRecords(999, 0L, 5000L);
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetAllPatients() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(2, 75.0, "HeartRate", 2000L);
        List<Patient> patients = storage.getAllPatients();
        assertEquals(2, patients.size());
    }

    @Test
    void testGetAllPatientsEmpty() {
        List<Patient> patients = storage.getAllPatients();
        assertNotNull(patients);
        assertTrue(patients.isEmpty());
    }

    @Test
    void testAddDataCreatesNewPatientIfNotExists() {
        storage.addPatientData(42, 98.0, "Saturation", 1000L);
        List<Patient> patients = storage.getAllPatients();
        assertEquals(1, patients.size());
        assertEquals(42, patients.get(0).getPatientId());
    }

    // =========================================================================
    // Patient / getRecords Tests
    // =========================================================================

    @Test
    void testGetRecordsWithinTimeRange() {
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "ECG", 1000L);
        patient.addRecord(105.0, "ECG", 3000L);
        patient.addRecord(110.0, "ECG", 5000L);

        List<PatientRecord> records = patient.getRecords(2000L, 4000L);
        assertEquals(1, records.size());
        assertEquals(105.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecordsBoundaryInclusive() {
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "ECG", 1000L);
        patient.addRecord(200.0, "ECG", 2000L);
        patient.addRecord(300.0, "ECG", 3000L);

        List<PatientRecord> records = patient.getRecords(1000L, 3000L);
        assertEquals(3, records.size());
    }

    @Test
    void testGetRecordsEmptyResult() {
        Patient patient = new Patient(1);
        patient.addRecord(100.0, "ECG", 1000L);

        List<PatientRecord> records = patient.getRecords(5000L, 9000L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecordsNoDataAdded() {
        Patient patient = new Patient(1);
        List<PatientRecord> records = patient.getRecords(0L, Long.MAX_VALUE);
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void testPatientGetPatientId() {
        Patient patient = new Patient(7);
        assertEquals(7, patient.getPatientId());
    }

    // =========================================================================
    // PatientRecord Tests
    // =========================================================================

    @Test
    void testPatientRecordGetters() {
        PatientRecord record = new PatientRecord(3, 98.6, "Temperature", 12345L);
        assertEquals(3, record.getPatientId());
        assertEquals(98.6, record.getMeasurementValue());
        assertEquals("Temperature", record.getRecordType());
        assertEquals(12345L, record.getTimestamp());
    }

    // =========================================================================
    // FileDataReader Tests
    // =========================================================================

    @Test
    void testFileDataReaderCsvFormat() throws IOException {
        File tempDir = Files.createTempDirectory("test_data").toFile();
        File testFile = new File(tempDir, "test.txt");
        try (FileWriter fw = new FileWriter(testFile)) {
            fw.write("1,120.0,SystolicPressure,1000\n");
            fw.write("1,80.0,DiastolicPressure,2000\n");
            fw.write("2,95.0,Saturation,3000\n");
        }

        FileDataReader reader = new FileDataReader(tempDir.getAbsolutePath());
        reader.readData(storage);

        List<PatientRecord> p1Records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, p1Records.size());

        List<PatientRecord> p2Records = storage.getRecords(2, 0L, Long.MAX_VALUE);
        assertEquals(1, p2Records.size());

        // Cleanup
        testFile.delete();
        tempDir.delete();
    }

    @Test
    void testFileDataReaderFormattedFormat() throws IOException {
        File tempDir = Files.createTempDirectory("test_data2").toFile();
        File testFile = new File(tempDir, "Alert.txt");
        try (FileWriter fw = new FileWriter(testFile)) {
            fw.write("Patient ID: 1, Timestamp: 1000, Label: Alert, Data: triggered\n");
            fw.write("Patient ID: 1, Timestamp: 2000, Label: Alert, Data: resolved\n");
        }

        FileDataReader reader = new FileDataReader(tempDir.getAbsolutePath());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals(1.0, records.get(0).getMeasurementValue()); // triggered = 1.0
        assertEquals(0.0, records.get(1).getMeasurementValue()); // resolved = 0.0

        testFile.delete();
        tempDir.delete();
    }

    @Test
    void testFileDataReaderSaturationWithPercent() throws IOException {
        File tempDir = Files.createTempDirectory("test_sat").toFile();
        File testFile = new File(tempDir, "Saturation.txt");
        try (FileWriter fw = new FileWriter(testFile)) {
            fw.write("Patient ID: 1, Timestamp: 1000, Label: Saturation, Data: 95%\n");
        }

        FileDataReader reader = new FileDataReader(tempDir.getAbsolutePath());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(95.0, records.get(0).getMeasurementValue());

        testFile.delete();
        tempDir.delete();
    }

    @Test
    void testFileDataReaderThrowsOnEmptyDirectory() throws IOException {
        File tempDir = Files.createTempDirectory("empty_dir").toFile();
        FileDataReader reader = new FileDataReader(tempDir.getAbsolutePath());
        assertThrows(IOException.class, () -> reader.readData(storage));
        tempDir.delete();
    }

    @Test
    void testFileDataReaderThrowsOnNonExistentDirectory() {
        FileDataReader reader = new FileDataReader("/nonexistent/path/that/does/not/exist");
        assertThrows(IOException.class, () -> reader.readData(storage));
    }

    @Test
    void testFileDataReaderSkipsMalformedLines() throws IOException {
        File tempDir = Files.createTempDirectory("malformed").toFile();
        File testFile = new File(tempDir, "data.txt");
        try (FileWriter fw = new FileWriter(testFile)) {
            fw.write("1,120.0,SystolicPressure,1000\n");
            fw.write("this is not valid data\n");
            fw.write("2,80.0,DiastolicPressure,2000\n");
        }

        FileDataReader reader = new FileDataReader(tempDir.getAbsolutePath());
        // Should not throw even with malformed lines
        assertDoesNotThrow(() -> reader.readData(storage));

        testFile.delete();
        tempDir.delete();
    }

    // =========================================================================
    // AlertGenerator - Blood Pressure Tests
    // =========================================================================

    @Test
    void testBloodPressureIncreasingTrendAlert() {
        // Three consecutive readings each increasing by more than 10 mmHg
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 135.0, "SystolicPressure", 2000L); // +15
        storage.addPatientData(1, 150.0, "SystolicPressure", 3000L); // +15

        AlertGenerator alertGenerator = new AlertGenerator(storage);
        // Should not throw; alert is logged to console
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testBloodPressureDecreasingTrendAlert() {
        storage.addPatientData(1, 150.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 135.0, "SystolicPressure", 2000L); // -15
        storage.addPatientData(1, 120.0, "SystolicPressure", 3000L); // -15

        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testSystolicCriticalHighAlert() {
        // Systolic > 180 should trigger a critical alert
        storage.addPatientData(1, 185.0, "SystolicPressure", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testSystolicCriticalLowAlert() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testDiastolicCriticalHighAlert() {
        storage.addPatientData(1, 125.0, "DiastolicPressure", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testDiastolicCriticalLowAlert() {
        storage.addPatientData(1, 55.0, "DiastolicPressure", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testNormalBloodPressureNoAlert() {
        // All readings within safe range, no trend
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 122.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 121.0, "SystolicPressure", 3000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    // =========================================================================
    // AlertGenerator - Blood Saturation Tests
    // =========================================================================

    @Test
    void testLowSaturationAlert() {
        // Below 92% should trigger alert
        storage.addPatientData(1, 90.0, "Saturation", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testRapidSaturationDropAlert() {
        // Drop of 5% or more within 10 minutes (600,000 ms)
        storage.addPatientData(1, 98.0, "Saturation", 1000L);
        storage.addPatientData(1, 92.0, "Saturation", 300_000L); // 5 minutes later, -6%
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testNoRapidDropIfOutsideTimeWindow() {
        // Same drop but more than 10 minutes apart — no rapid drop alert
        storage.addPatientData(1, 98.0, "Saturation", 1000L);
        storage.addPatientData(1, 92.0, "Saturation", 700_000L); // >10 minutes later
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testNormalSaturationNoAlert() {
        storage.addPatientData(1, 97.0, "Saturation", 1000L);
        storage.addPatientData(1, 98.0, "Saturation", 2000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    // =========================================================================
    // AlertGenerator - Hypotensive Hypoxemia Tests
    // =========================================================================

    @Test
    void testHypotensiveHypoxemiaAlertTriggered() {
        // Both conditions: systolic < 90 AND saturation < 92
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 89.0, "Saturation", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testHypotensiveHypoxemiaNoAlertIfOnlyLowBP() {
        // Only low BP, saturation is fine
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 95.0, "Saturation", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testHypotensiveHypoxemiaNoAlertIfOnlyLowSat() {
        // Only low saturation, BP is fine
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 89.0, "Saturation", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    // =========================================================================
    // AlertGenerator - ECG Tests
    // =========================================================================

    @Test
    void testEcgAbnormalPeakAlert() {
        // Seed a window of normal values, then add a huge spike
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 0.5, "ECG", 1000L * i);
        }
        // Add a massive outlier
        storage.addPatientData(1, 100.0, "ECG", 10_000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testEcgNoPeakNoAlert() {
        // All ECG values consistent — no alert
        for (int i = 0; i < 15; i++) {
            storage.addPatientData(1, 0.5 + (i % 2 == 0 ? 0.01 : -0.01), "ECG", 1000L * i);
        }
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testEcgTooFewRecordsSkipsCheck() {
        // Fewer than 10 ECG records — no crash, no alert
        storage.addPatientData(1, 0.5, "ECG", 1000L);
        storage.addPatientData(1, 0.6, "ECG", 2000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    // =========================================================================
    // AlertGenerator - Triggered Alert Tests
    // =========================================================================

    @Test
    void testManualAlertTriggered() {
        // measurementValue = 1.0 means "triggered"
        storage.addPatientData(1, 1.0, "Alert", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    @Test
    void testManualAlertResolved() {
        // measurementValue = 0.0 means "resolved" — should not trigger alert
        storage.addPatientData(1, 0.0, "Alert", 1000L);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(storage.getAllPatients().get(0)));
    }

    // =========================================================================
    // AlertGenerator - No Data Edge Case
    // =========================================================================

    @Test
    void testEvaluateDataWithNoRecords() {
        Patient emptyPatient = new Patient(99);
        AlertGenerator alertGenerator = new AlertGenerator(storage);
        // Should handle a patient with no records without throwing
        assertDoesNotThrow(() -> alertGenerator.evaluateData(emptyPatient));
    }

    // =========================================================================
    // Alert Class Tests
    // =========================================================================

    @Test
    void testAlertGetters() {
        Alert alert = new Alert("5", "High Blood Pressure", 99999L);
        assertEquals("5", alert.getPatientId());
        assertEquals("High Blood Pressure", alert.getCondition());
        assertEquals(99999L, alert.getTimestamp());
    }
}