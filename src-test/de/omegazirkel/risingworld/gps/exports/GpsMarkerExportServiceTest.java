package de.omegazirkel.risingworld.gps.exports;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.junit.Test;

public class GpsMarkerExportServiceTest {

    @Test
    public void exportsGlobalMarkers() throws Exception {
        try (Connection connection = database()) {
            insertMarker(connection, 1, "GLOBAL", 1000L, "Spawn", 0xff00ff80);
            insertMarker(connection, 2, "PRIVATE", 2000L, "Private", 0xff0000ff);
            insertMarker(connection, 3, "GLOBAL", 3000L, "Market", 0x01020304);

            GpsMarkerExportResponse response = new GpsMarkerExportService(connection).exportGlobalMarkers(null);

            assertEquals(1, response.schemaVersion());
            assertEquals("global", response.type());
            assertEquals(2, response.markers().size());
            assertEquals(3, response.markers().get(0).id());
            assertEquals("Market", response.markers().get(0).name());
            assertEquals("#01020304", response.markers().get(0).color());
            assertEquals(1, response.markers().get(1).id());
        }
    }

    @Test
    public void filtersGlobalMarkersByLastChange() throws Exception {
        try (Connection connection = database()) {
            insertMarker(connection, 1, "GLOBAL", 1000L, "Spawn", 0xff00ff80);
            insertMarker(connection, 2, "GLOBAL", 3000L, "Market", 0x01020304);

            GpsMarkerExportResponse response = new GpsMarkerExportService(connection).exportGlobalMarkers(1000L);

            assertEquals(1, response.markers().size());
            assertEquals(2, response.markers().get(0).id());
        }
    }

    private static Connection database() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE marker (
                      id INTEGER,
                      player_id INTEGER,
                      type TEXT,
                      group_name TEXT,
                      created_at INTEGER,
                      pos_x REAL,
                      pos_y REAL,
                      pos_z REAL,
                      name TEXT,
                      icon TEXT,
                      color INTEGER,
                      cost INTEGER
                    );
                    """);
        }
        return connection;
    }

    private static void insertMarker(
            Connection connection,
            int id,
            String type,
            long createdAt,
            String name,
            int color) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO marker
                (id, player_id, type, group_name, created_at, pos_x, pos_y, pos_z, name, icon, color, cost)
                VALUES (?, 0, ?, NULL, ?, 10, 20, 30, ?, 'icon-ki-gps-global', ?, 0);
                """)) {
            statement.setInt(1, id);
            statement.setString(2, type);
            statement.setLong(3, createdAt);
            statement.setString(4, name);
            statement.setInt(5, color);
            statement.executeUpdate();
        }
    }
}
