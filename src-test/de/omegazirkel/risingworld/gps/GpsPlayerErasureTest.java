package de.omegazirkel.risingworld.gps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;

public class GpsPlayerErasureTest {
    @Test public void deletesPrivateMarkersAndAnonymizesSharedObjects() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            GPSDatabase database = GPSDatabase.createForTesting(connection);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO marker (player_id,type,created_at,pos_x,pos_y,pos_z,name,icon,color,cost) "
                        + "VALUES (17,'PRIVATE',1,0,0,0,'Private','pin',0,0),"
                        + "(17,'GROUP',1,0,0,0,'Shared','pin',0,0),"
                        + "(18,'PRIVATE',1,0,0,0,'Other','pin',0,0)");
                statement.executeUpdate("INSERT INTO gps_area VALUES (1,17,1,1),(2,18,1,1)");
                statement.executeUpdate("INSERT INTO server_pin (creator_id,created_at,name,icon,address,password) "
                        + "VALUES (17,1,'Shared','pin','example.org:4255','secret')");
            }

            assertTrue(database.erasePlayerData(17));
            assertTrue(database.erasePlayerData(17));
            assertFalse(database.erasePlayerData(0));
            assertEquals(0, count(connection, "SELECT COUNT(*) FROM marker WHERE player_id=17"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM marker WHERE player_id=0 AND name='Shared'"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM marker WHERE player_id=18"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM gps_area WHERE area_id=1 AND admin_id=0"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM gps_area WHERE area_id=2 AND admin_id=18"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM server_pin WHERE creator_id=0 AND name='Shared'"));
        }
    }

    private static int count(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }
}
