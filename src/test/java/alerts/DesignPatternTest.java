package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.ECGAlertFactory;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.decorators.RepeatedAlertDecorator;
import com.data_management.DataStorage;
import com.cardio_generator.HealthDataSimulator;

class DesignPatternTest {

    @Test
    void testBloodPressureAlertFactory() {
        BloodPressureAlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("1", "High Systolic", 1000L);
        assertNotNull(alert);
        assertEquals("1", alert.getPatientId());
        assertTrue(alert.getCondition().contains("BloodPressure"));
    }

    @Test
    void testBloodOxygenAlertFactory() {
        BloodOxygenAlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("2", "Low Saturation", 2000L);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("BloodOxygen"));
    }

    @Test
    void testECGAlertFactory() {
        ECGAlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("3", "Abnormal Peak", 3000L);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("ECG"));
    }

    @Test
    void testPriorityAlertDecorator() {
        Alert base = new Alert("1", "Low Saturation", 1000L);
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(base);
        assertTrue(decorated.getCondition().contains("[HIGH PRIORITY]"));
        assertEquals("1", decorated.getPatientId());
    }

    @Test
    void testRepeatedAlertDecorator() {
        Alert base = new Alert("2", "High BP", 2000L);
        RepeatedAlertDecorator decorated = new RepeatedAlertDecorator(base, 3);
        assertTrue(decorated.getCondition().contains("[REPEATED x3]"));
        assertTrue(decorated.getCondition().contains("High BP"));
    }

    @Test
    void testDataStorageSingleton() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();
        assertSame(a, b);
    }

    @Test
    void testHealthDataSimulatorSingleton() {
        HealthDataSimulator a = HealthDataSimulator.getInstance();
        HealthDataSimulator b = HealthDataSimulator.getInstance();
        assertSame(a, b);
    }
}
