package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.OxygenSaturationStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.data_management.Patient;

class AlertGeneratorTest {

    @Test
    void testBloodPressureCriticalHighSystolic() {
        Patient patient = new Patient(1);
        long now = System.currentTimeMillis();
        patient.addRecord(185.0, "SystolicPressure", now);

        BloodPressureStrategy strategy = new BloodPressureStrategy();
        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Critical Systolic Pressure"));
    }

    @Test
    void testBloodPressureCriticalLowSystolic() {
        Patient patient = new Patient(2);
        long now = System.currentTimeMillis();
        patient.addRecord(85.0, "SystolicPressure", now);

        BloodPressureStrategy strategy = new BloodPressureStrategy();
        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
    }

    @Test
    void testBloodPressureCriticalHighDiastolic() {
        Patient patient = new Patient(3);
        long now = System.currentTimeMillis();
        patient.addRecord(125.0, "DiastolicPressure", now);

        BloodPressureStrategy strategy = new BloodPressureStrategy();
        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Critical Diastolic Pressure"));
    }

    @Test
    void testBloodPressureIncreasingTrend() {
        Patient patient = new Patient(4);
        long now = System.currentTimeMillis();
        patient.addRecord(100.0, "SystolicPressure", now - 3000);
        patient.addRecord(115.0, "SystolicPressure", now - 2000);
        patient.addRecord(130.0, "SystolicPressure", now - 1000);

        BloodPressureStrategy strategy = new BloodPressureStrategy();
        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Trend"));
    }

    @Test
    void testBloodPressureDecreasingTrend() {
        Patient patient = new Patient(5);
        long now = System.currentTimeMillis();
        patient.addRecord(160.0, "SystolicPressure", now - 3000);
        patient.addRecord(145.0, "SystolicPressure", now - 2000);
        patient.addRecord(130.0, "SystolicPressure", now - 1000);

        BloodPressureStrategy strategy = new BloodPressureStrategy();
        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
    }

    @Test
    void testBloodPressureNormalNoAlert() {
        Patient patient = new Patient(6);
        long now = System.currentTimeMillis();
        patient.addRecord(120.0, "SystolicPressure", now - 2000);
        patient.addRecord(121.0, "SystolicPressure", now - 1000);
        patient.addRecord(122.0, "SystolicPressure", now);
        patient.addRecord(80.0, "DiastolicPressure", now);

        BloodPressureStrategy strategy = new BloodPressureStrategy();
        Alert alert = strategy.checkAlert(patient);
        assertNull(alert);
    }

    @Test
    void testLowSaturationAlert() {
        Patient patient = new Patient(7);
        long now = System.currentTimeMillis();
        patient.addRecord(90.0, "Saturation", now);

        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Low Blood Saturation"));
    }

    @Test
    void testRapidSaturationDrop() {
        Patient patient = new Patient(8);
        long now = System.currentTimeMillis();
        patient.addRecord(98.0, "Saturation", now - 5 * 60 * 1000);
        patient.addRecord(92.0, "Saturation", now);

        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        Alert alert = strategy.checkAlert(patient);
        assertNotNull(alert);
    }

    @Test
    void testNormalSaturationNoAlert() {
        Patient patient = new Patient(9);
        long now = System.currentTimeMillis();
        patient.addRecord(97.0, "Saturation", now);

        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy();
        Alert alert = strategy.checkAlert(patient);
        assertNull(alert);
    }

    @Test
    void testEcgNoAlertWithFewRecords() {
        Patient patient = new Patient(10);
        long now = System.currentTimeMillis();
        patient.addRecord(0.5, "ECG", now);

        HeartRateStrategy strategy = new HeartRateStrategy();
        Alert alert = strategy.checkAlert(patient);
        // Not enough data for window
        assertNull(alert);
    }
}
