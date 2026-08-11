package de.omegazirkel.risingworld.gps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;

public class ServerPinPersistenceTest {
    @Test public void createsReadsUpdatesAndDeletesIsolatedServerPins() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            GPSDatabase database = GPSDatabase.createForTesting(connection);
            ServerPin pin = new ServerPin(17, "Moon", "marker-server-2", "play.example.org:4255", "secret");
            assertTrue(database.saveServerPin(pin));
            assertNotNull(pin.getId());
            assertEquals(1, database.getServerPins().size());
            pin.setName("Moon II"); pin.setAddress("192.168.1.15:4256"); pin.setPassword("");
            assertTrue(database.saveServerPin(pin));
            ServerPin updated = database.getServerPins().get(0);
            assertEquals("Moon II", updated.getName());
            assertEquals("192.168.1.15:4256", updated.getAddress());
            assertTrue(database.deleteServerPin(pin.getId()));
            assertTrue(database.getServerPins().isEmpty());
        }
    }
}
