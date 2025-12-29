package de.omegazirkel.risingworld.gps;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.db.SQLite;
import net.risingworld.api.utils.Vector3f;

public class GPSDatabase {
    private static GPSDatabase instance = null;
    private final SQLite db;
    static private final String tableName = "marker";

    private GPSDatabase(SQLite database) {
        this.db = database;
        initialize();
    }

    public static GPSDatabase getInstance(SQLite database) {
        if (instance == null) {
            instance = new GPSDatabase(database);
        }
        return instance;
    }

    public static GPSDatabase getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    // --- Query Helpers -----------------------------------------------------

    private String q(String value) {
        return "'" + escape(value) + "'";
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    // --- Save Queries ------------------------------------------------------

    public void saveMarker(Marker marker) {
        try {
            if (marker.getId() == null) {
                // --- INSERT ----------------------------------------------------
                String query = "INSERT INTO " + tableName + " ("
                        + "player_id, type, group_name, created_at, "
                        + "pos_x, pos_y, pos_z, "
                        + "name, icon, color, cost"
                        + ") VALUES ("
                        + marker.getPlayerId() + ", "
                        + q(marker.getType().toString()) + ", "
                        + (marker.getGroup() == null ? "NULL" : q(marker.getGroup())) + ", "
                        + marker.getCreatedAt() + ", "
                        + marker.getPosition().x + ", "
                        + marker.getPosition().y + ", "
                        + marker.getPosition().z + ", "
                        + q(marker.getName()) + ", "
                        + q(marker.getIcon()) + ", "
                        + marker.getColor() + ", "
                        + marker.getCost()
                        + ");";

                db.execute(query);

                try (ResultSet rs = db.executeQuery("SELECT last_insert_rowid();")) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        marker.setId(id);
                    }
                }

            } else {
                // --- UPDATE ----------------------------------------------------
                String query = "UPDATE " + tableName + " SET "
                        + "type=" + q(marker.getType().toString()) + ", "
                        + "group_name=" + (marker.getGroup() == null ? "NULL" : q(marker.getGroup())) + ", "
                        + "pos_x=" + marker.getPosition().x + ", "
                        + "pos_y=" + marker.getPosition().y + ", "
                        + "pos_z=" + marker.getPosition().z + ", "
                        + "name=" + q(marker.getName()) + ", "
                        + "icon=" + q(marker.getIcon()) + ", "
                        + "color=" + marker.getColor() + ", "
                        + "cost=" + marker.getCost()
                        + " WHERE id=" + marker.getId() + ";";

                db.execute(query);
            }

        } catch (Exception e) {
            GPS.logger().error("saveMarker failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Delete Queries ----------------------------------------------------

    public boolean deleteMarker(int markerId, int playerId) {
        try {
            db.execute(
                    "DELETE FROM " + tableName
                            + " WHERE id=" + markerId
                            + " AND player_id=" + playerId + ";");
            return true;
        } catch (Exception e) {
            GPS.logger().error("deleteMarker failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- List Queries ------------------------------------------------------

    public List<Marker> getPrivateMarker(int playerId, int page, int pageSize) {
        return getPrivateMarker(playerId, page, pageSize, "DESC");
    }

    public List<Marker> getPrivateMarker(int playerId, int page, int pageSize, String orderBy) {
        String sql = "SELECT * FROM " + tableName + " WHERE type=" + q(MarkerType.PRIVATE.toString())
                + " AND player_id=" + playerId
                + " ORDER BY created_at " + orderBy
                + " LIMIT " + pageSize + " OFFSET " + (page * pageSize) + ";";
        return executeListQuery(sql);
    }

    public List<Marker> getGroupMarker(String groupName, int page, int pageSize) {
        return getGroupMarker(groupName, page, pageSize, "DESC");
    }

    public List<Marker> getGroupMarker(String groupName, int page, int pageSize, String orderBy) {
        return executeListQuery(
                "SELECT * FROM " + tableName + " WHERE type=" + q(MarkerType.GROUP.toString())
                        + " AND group_name=" + q(groupName)
                        + " ORDER BY created_at " + orderBy
                        + " LIMIT " + pageSize + " OFFSET " + (page * pageSize) + ";");
    }

    public List<Marker> getGlobalMarker(int page, int pageSize) {
        return getGlobalMarker(page, pageSize, "DESC");
    }

    public List<Marker> getGlobalMarker(int page, int pageSize, String orderBy) {
        return executeListQuery(
                "SELECT * FROM " + tableName + " WHERE type=" + q(MarkerType.GLOBAL.toString())
                        + " ORDER BY created_at " + orderBy
                        + " LIMIT " + pageSize + " OFFSET " + (page * pageSize) + ";");
    }

    private List<Marker> executeListQuery(String query) {
        List<Marker> markers = new ArrayList<>();

        try (ResultSet result = db.executeQuery(query)) {
            while (result.next()) {
                Vector3f pos = new Vector3f(
                        result.getFloat("pos_x"),
                        result.getFloat("pos_y"),
                        result.getFloat("pos_z"));
                long timestamp = result.getLong("created_at");
                markers.add(new Marker(
                        result.getInt("id"),
                        result.getInt("player_id"),
                        MarkerType.valueOf(result.getString("type")),
                        result.getString("group_name"),
                        timestamp,
                        pos,
                        result.getString("name"),
                        result.getString("icon"),
                        result.getInt("color"),
                        result.getInt("cost")));
            }
        } catch (Exception e) {
            GPS.logger().error("executeListQuery failed: " + e.getMessage());
            e.printStackTrace();
        }

        return markers;
    }

    private void initialize() {
        // create table
        db.execute(
                "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "player_id INTEGER NOT NULL,"
                        + "type VARCHAR(16) NOT NULL,"
                        + "group_name TEXT,"
                        + "created_at BIGINT NOT NULL,"
                        + "pos_x REAL NOT NULL,"
                        + "pos_y REAL NOT NULL,"
                        + "pos_z REAL NOT NULL,"
                        + "name TEXT NOT NULL,"
                        + "icon TEXT NOT NULL,"
                        + "color INTEGER NOT NULL,"
                        + "cost INTEGER NOT NULL"
                        + ");");
    }
}
