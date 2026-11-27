package de.omegazirkel.risingworld.gps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.Test;

import net.risingworld.api.utils.Vector3f;

public class MarkerPersistenceFailureTest {
    @Test
    public void failedInsertDoesNotAssignAnEarlierRowId() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            GPSDatabase database = GPSDatabase.createForTesting(connection);
            Marker first = marker("first");
            database.saveMarker(first);
            assertNotNull(first.getId());
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TRIGGER reject_marker BEFORE INSERT ON marker BEGIN SELECT RAISE(FAIL, 'injected'); END");
            }
            Marker rejected = marker("rejected");
            database.saveMarker(rejected);
            assertNull("failed insertion must not reuse the previous row ID", rejected.getId());
            assertFalse(database.saveMarkerResult(marker("second rejected")));
            try (Statement statement = connection.createStatement();
                    var rows = statement.executeQuery("SELECT count(*) FROM marker")) {
                assertEquals(1, rows.getInt(1));
            }
        }
    }

    @Test
    public void updateReportsMissingRowAndValidUpdateSucceeds() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            GPSDatabase database = GPSDatabase.createForTesting(connection);
            Marker marker = marker("first");
            assertTrue(database.saveMarkerResult(marker));
            marker.setName("updated");
            assertTrue(database.saveMarkerResult(marker));
            try (Statement statement = connection.createStatement()) {
                assertEquals(1, statement.executeUpdate("DELETE FROM marker"));
            }
            assertFalse(database.saveMarkerResult(marker));
        }
    }

    private Marker marker(String name) {
        return new Marker(42, MarkerType.PRIVATE, null, new Vector3f(1, 2, 3), name, "marker", 0, 0);
    }
}
