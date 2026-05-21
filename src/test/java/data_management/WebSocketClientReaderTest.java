package data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClientReader;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketClientReaderTest {

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = DataStorage.getInstance();
        storage.clearAll();
    }

    /**
     * A fake WebSocketClient that allows us to manually trigger onMessage()
     * without opening a real network connection.
     */
    private static class FakeClient extends WebSocketClient {

        private final WebSocketClientReader reader;
        private final DataStorage storage;

        public FakeClient(WebSocketClientReader reader, DataStorage storage) {
            super(URI.create("ws://fake"));
            this.reader = reader;
            this.storage = storage;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {}

        @Override
        public void onMessage(String message) {
            readerTestHook_parse(reader, message, storage);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {}

        @Override
        public void onError(Exception ex) {}
    }

    /**
     * Helper to call the private parseAndStore method.
     */
    private static void readerTestHook_parse(WebSocketClientReader reader,
                                             String msg,
                                             DataStorage storage) {
        try {
            var method = WebSocketClientReader.class.getDeclaredMethod("parseAndStore", String.class, DataStorage.class);
            method.setAccessible(true);
            method.invoke(reader, msg, storage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testParse_validMessage_storesRecord() {
        WebSocketClientReader reader = new WebSocketClientReader("ws://fake");
        FakeClient fake = new FakeClient(reader, storage);

        fake.onMessage("42,1000,HeartRate,75.0");

        List<PatientRecord> records = storage.getRecords(42, 1000L, 1000L);
        assertEquals(1, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
    }

    @Test
    void testParse_percentValue_parsedCorrectly() {
        WebSocketClientReader reader = new WebSocketClientReader("ws://fake");
        FakeClient fake = new FakeClient(reader, storage);

        fake.onMessage("43,2000,Saturation,96.5%");

        List<PatientRecord> records = storage.getRecords(43, 2000L, 2000L);
        assertEquals(96.5, records.get(0).getMeasurementValue());
    }

    @Test
    void testParse_malformedMessages_doNotThrow() {
        WebSocketClientReader reader = new WebSocketClientReader("ws://fake");
        FakeClient fake = new FakeClient(reader, storage);

        assertDoesNotThrow(() -> fake.onMessage((String) null));
        assertDoesNotThrow(() -> fake.onMessage(""));
        assertDoesNotThrow(() -> fake.onMessage("1,1000,HeartRate"));
        assertDoesNotThrow(() -> fake.onMessage("abc,1000,HeartRate,75"));
        assertDoesNotThrow(() -> fake.onMessage("1,notATime,HeartRate,75"));
        assertDoesNotThrow(() -> fake.onMessage("1,1000,HeartRate,notANumber"));
    }

    @Test
    void testParse_blankRecordType_ignored() {
        WebSocketClientReader reader = new WebSocketClientReader("ws://fake");
        FakeClient fake = new FakeClient(reader, storage);

        fake.onMessage("99,1000, ,75.0");

        assertTrue(storage.getRecords(99, 1000L, 1000L).isEmpty());
    }

    @Test
    void testReadData_badUri_throwsIOException() {
        WebSocketClientReader bad = new WebSocketClientReader("://bad");
        assertThrows(IOException.class, () -> bad.readData(storage));
    }

    @Test
    void testClose_safeWhenNotConnected() {
        WebSocketClientReader reader = new WebSocketClientReader("ws://fake");
        assertDoesNotThrow(reader::close);
    }
}
