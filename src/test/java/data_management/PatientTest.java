package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

class PatientTest {

    @Test
    void testGetRecordsWithinRange() {
        Patient patient = new Patient(1);
        patient.addRecord(120.0, "SystolicPressure", 1000L);
        patient.addRecord(125.0, "SystolicPressure", 2000L);
        patient.addRecord(130.0, "SystolicPressure", 3000L);

        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(2, records.size());
    }

    @Test
    void testGetRecordsOutsideRange() {
        Patient patient = new Patient(2);
        patient.addRecord(80.0, "HeartRate", 5000L);

        List<PatientRecord> records = patient.getRecords(1000L, 3000L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetAllRecords() {
        Patient patient = new Patient(3);
        patient.addRecord(95.0, "Saturation", 1000L);
        patient.addRecord(96.0, "Saturation", 2000L);

        assertEquals(2, patient.getRecords(0, 3000L).size());
    }
}
