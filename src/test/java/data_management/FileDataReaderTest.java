package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class FileDataReaderTest {

    @Test
    void testReadDataFromFile(@TempDir Path tempDir) throws IOException {
        // Make a new file with some test data
        Path file = tempDir.resolve("HeartRate.txt");
        Files.writeString(file,
                "Patient ID: 5, Timestamp: 1000, Label: HeartRate, Data: 75.0\n" +
                "Patient ID: 5, Timestamp: 2000, Label: HeartRate, Data: 80.0\n");

        DataStorage storage = DataStorage.getInstance();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(5, 1000L, 2000L);
        assertTrue(records.size() >= 2);
    }

    @Test
    void testReadDataSkipsMalformedLines(@TempDir Path tempDir) throws IOException {
        // Make a new file with some test data
        Path file = tempDir.resolve("Bad.txt");
        Files.writeString(file, "this is a bad line\n" + 
                "Patient ID: 6, Timestamp: 3000, Label: ECG, Data: 0.5\n");

        DataStorage storage = DataStorage.getInstance();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        // Should not throw even with a bad line
        assertDoesNotThrow(() -> reader.readData(storage));
    }
}
