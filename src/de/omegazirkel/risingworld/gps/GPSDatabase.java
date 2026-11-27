package de.omegazirkel.risingworld.gps;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.omegazirkel.risingworld.GPS;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Vector3f;

public class GPSDatabase {
    private static GPSDatabase instance = null;
    private final Connection db;
    static private final String tableName = "marker";
    static private final String gpsAreaTableName = "gps_area";
    static private final String serverPinTableName = "server_pin";

    public record GPSArea(long areaId, int adminId, long createdAt, boolean allowGPS) {
    }

    private GPSDatabase(Connection database) {
        this.db = database;
        initialize();
    }

    public static GPSDatabase getInstance(Connection database) {
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

    static GPSDatabase createForTesting(Connection database) {
        return new GPSDatabase(database);
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
        saveMarkerResult(marker);
    }

    public boolean saveMarkerResult(Marker marker) {
        synchronized (db) {
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

                    try (Statement statement = db.createStatement()) {
                        if (statement.executeUpdate(query) != 1) return false;
                    }

                    try (Statement statement = db.createStatement();
                            ResultSet rs = statement.executeQuery("SELECT last_insert_rowid();")) {
                        if (rs.next()) {
                            int id = rs.getInt(1);
                            marker.setId(id);
                            return true;
                        }
                    }
                    return false;

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

                    try (Statement statement = db.createStatement()) {
                        return statement.executeUpdate(query) == 1;
                    }
                }

            } catch (Exception e) {
                GPS.logger().error("saveMarker failed: " + e.getMessage());
                return false;
            }
        }
    }

    boolean beginMarkerPurchase(String operation, Marker marker, long amount, String currency) throws SQLException {
        synchronized (db) {
            if (!db.getAutoCommit() || marker.getId() != null) return false;
            try (var statement = db.prepareStatement("""
                    INSERT INTO gps_marker_purchase(operation_id, player_id, amount, currency, marker_name,
                        marker_type, marker_position, stage)
                    SELECT ?, ?, ?, ?, ?, ?, ?, 'DEBIT_ATTEMPTED'
                    WHERE NOT EXISTS (SELECT 1 FROM gps_marker_purchase WHERE player_id = ?)
                    """)) {
                statement.setString(1, operation);
                statement.setInt(2, marker.getPlayerId());
                statement.setLong(3, amount);
                statement.setString(4, currency);
                statement.setString(5, marker.getName());
                statement.setString(6, marker.getType().name());
                statement.setString(7, marker.getPosition().toString());
                statement.setInt(8, marker.getPlayerId());
                return statement.executeUpdate() == 1;
            }
        }
    }

    void recordMarkerPurchaseStage(String operation, String stage) throws SQLException {
        synchronized (db) {
            try (var statement = db.prepareStatement("UPDATE gps_marker_purchase SET stage = ? WHERE operation_id = ?")) {
                statement.setString(1, stage);
                statement.setString(2, operation);
                if (statement.executeUpdate() != 1) throw new SQLException("Missing marker purchase " + operation);
            }
        }
    }

    void finishMarkerPurchase(String operation) throws SQLException {
        synchronized (db) {
            try (var statement = db.prepareStatement("DELETE FROM gps_marker_purchase WHERE operation_id = ?")) {
                statement.setString(1, operation);
                if (statement.executeUpdate() != 1) throw new SQLException("Missing marker purchase " + operation);
            }
        }
    }

    boolean commitPurchasedMarker(String operation, Marker marker) throws SQLException {
        synchronized (db) {
            if (!db.getAutoCommit()) throw new SQLException("Marker DB already in a transaction");
            db.setAutoCommit(false);
            boolean transactionFinished = false;
            try {
                if (!saveMarkerResult(marker)) {
                    db.rollback();
                    transactionFinished = true;
                    marker.setId(null);
                    return false;
                }
                finishMarkerPurchase(operation);
                db.commit();
                transactionFinished = true;
                return true;
            } catch (SQLException | RuntimeException ex) {
                try {
                    db.rollback();
                    transactionFinished = true;
                    marker.setId(null);
                } catch (SQLException rollbackFailure) {
                    ex.addSuppressed(rollbackFailure);
                }
                throw ex;
            } finally {
                if (transactionFinished) db.setAutoCommit(true);
            }
        }
    }

    // --- Delete Queries ----------------------------------------------------

    public boolean updateMarkerDetails(Marker marker, Player player, String name, String icon) {
        if (!MarkerPermissions.canManage(player, marker)) {
            return false;
        }

        try {
            String query = "UPDATE " + tableName
                    + " SET name=" + q(name)
                    + ", icon=" + q(icon)
                    + " WHERE " + managementWhereClause(marker, player) + ";";
            try (Statement statement = db.createStatement()) {
                if (statement.executeUpdate(query) != 1) return false;
            }
            marker.setName(name);
            marker.setIcon(icon);
            return true;
        } catch (Exception e) {
            GPS.logger().error("updateMarkerDetails failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMarker(Marker marker, Player player) {
        if (!MarkerPermissions.canDelete(player, marker)) {
            return false;
        }

        try {
            String query = "DELETE FROM " + tableName
                    + " WHERE " + deletionWhereClause(marker, player) + ";";
            try (Statement statement = db.createStatement()) {
                return statement.executeUpdate(query) == 1;
            }
        } catch (Exception e) {
            GPS.logger().error("deleteMarker failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public GPSArea getGPSArea(long areaId) {
        if (areaId <= 0L) {
            return null;
        }
        String query = "SELECT area_id, admin_id, created_at, allow_gps FROM " + gpsAreaTableName
                + " WHERE area_id=" + areaId + " LIMIT 1;";
        try (Statement statement = db.createStatement(); ResultSet result = statement.executeQuery(query)) {
            if (!result.next()) {
                return null;
            }
            return new GPSArea(result.getLong("area_id"), result.getInt("admin_id"),
                    result.getLong("created_at"), result.getInt("allow_gps") != 0);
        } catch (Exception e) {
            GPS.logger().error("GPS area lookup failed: " + e.getMessage());
            return null;
        }
    }

    public boolean isGPSAreaAllowed(long areaId) {
        GPSArea area = getGPSArea(areaId);
        return area != null && area.allowGPS();
    }

    public boolean saveGPSArea(long areaId, int adminId, boolean allowGPS) {
        if (areaId <= 0L || adminId <= 0) {
            return false;
        }
        return executeUpdate("INSERT OR REPLACE INTO " + gpsAreaTableName
                + " (area_id, admin_id, created_at, allow_gps) VALUES ("
                + areaId + ", " + adminId + ", " + System.currentTimeMillis() + ", " + (allowGPS ? 1 : 0) + ");");
    }

    public boolean deleteGPSArea(long areaId) {
        return areaId > 0L && executeUpdate("DELETE FROM " + gpsAreaTableName + " WHERE area_id=" + areaId + ";");
    }

    public List<ServerPin> getServerPins() {
        List<ServerPin> pins = new ArrayList<>();
        try (Statement statement = db.createStatement(); ResultSet result = statement.executeQuery(
                "SELECT id, creator_id, created_at, name, icon, address, password FROM " + serverPinTableName
                        + " ORDER BY created_at DESC;")) {
            while (result.next()) {
                pins.add(new ServerPin(result.getInt("id"), result.getInt("creator_id"), result.getLong("created_at"),
                        result.getString("name"), result.getString("icon"), result.getString("address"),
                        result.getString("password")));
            }
        } catch (Exception e) {
            GPS.logger().error("server pin lookup failed: " + e.getMessage());
        }
        return pins;
    }

    public boolean saveServerPin(ServerPin pin) {
        if (!ServerPinAddress.isValid(pin.getAddress())) return false;
        try {
            if (pin.getId() == null) {
                if (!executeUpdate("INSERT INTO " + serverPinTableName + " (creator_id, created_at, name, icon, address, password) VALUES ("
                        + pin.getCreatorId() + ", " + pin.getCreatedAt() + ", " + q(pin.getName()) + ", " + q(pin.getIcon())
                        + ", " + q(pin.getAddress()) + ", " + q(pin.getPassword()) + ");")) return false;
                try (Statement statement = db.createStatement(); ResultSet result = statement.executeQuery("SELECT last_insert_rowid();")) {
                    if (result.next()) pin.setId(result.getInt(1));
                }
            } else {
                if (!executeUpdate("UPDATE " + serverPinTableName + " SET name=" + q(pin.getName()) + ", icon=" + q(pin.getIcon())
                        + ", address=" + q(pin.getAddress()) + ", password=" + q(pin.getPassword()) + " WHERE id=" + pin.getId() + ";")) return false;
            }
            return true;
        } catch (Exception e) {
            GPS.logger().error("server pin save failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteServerPin(int id) {
        return id > 0 && executeUpdate("DELETE FROM " + serverPinTableName + " WHERE id=" + id + ";");
    }

    /** Removes private GPS data and detaches the creator from shared markers and pins. */
    public synchronized boolean erasePlayerData(int playerId) {
        if (playerId <= 0) return false;
        try {
            if (!db.getAutoCommit()) {
                GPS.logger().error("GPS player erasure requires an idle database connection");
                return false;
            }
            db.setAutoCommit(false);
            try {
                updatePlayerRows("DELETE FROM marker WHERE player_id=? AND type='PRIVATE'", playerId);
                updatePlayerRows("UPDATE marker SET player_id=0 WHERE player_id=?", playerId);
                updatePlayerRows("UPDATE gps_area SET admin_id=0 WHERE admin_id=?", playerId);
                updatePlayerRows("UPDATE server_pin SET creator_id=0 WHERE creator_id=?", playerId);
                db.commit();
                return true;
            } catch (SQLException ex) {
                db.rollback();
                GPS.logger().error("GPS player erasure failed: " + ex.getMessage());
                return false;
            } finally {
                db.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            GPS.logger().error("GPS player erasure failed: " + ex.getMessage());
            return false;
        }
    }

    private void updatePlayerRows(String sql, int playerId) throws SQLException {
        try (PreparedStatement statement = db.prepareStatement(sql)) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }

    private String managementWhereClause(Marker marker, Player player) {
        switch (marker.getType()) {
            case PRIVATE:
                return "id=" + marker.getId()
                        + " AND type=" + q(MarkerType.PRIVATE.toString())
                        + " AND player_id=" + player.getDbID();
            case GROUP:
                return "id=" + marker.getId()
                        + " AND type=" + q(MarkerType.GROUP.toString())
                        + " AND group_name=" + q(player.getPermissionGroup());
            case GLOBAL:
                return "id=" + marker.getId()
                        + " AND type=" + q(MarkerType.GLOBAL.toString());
            case STATIC:
            default:
                return "id=-1";
        }
    }

    private String deletionWhereClause(Marker marker, Player player) {
        if (marker.getType() == MarkerType.GROUP) {
            return "id=" + marker.getId()
                    + " AND type=" + q(MarkerType.GROUP.toString())
                    + (player.isAdmin() ? "" : " AND player_id=" + player.getDbID());
        }
        return managementWhereClause(marker, player);
    }

    // --- List Queries ------------------------------------------------------

    public List<Marker> getPrivateMarker(int playerId, int page, int pageSize) {
        return getPrivateMarker(playerId, page, pageSize, "DESC");
    }

    public List<Marker> getPrivateMarker(int playerId, int page, int pageSize, String orderBy) {
        if (!orderBy.equals("ASC") && !orderBy.equals("DESC")) {
            GPS.logger().warn("Invalid value for orderBy: " + orderBy);
            orderBy = "DESC";
        }
        String sql = "SELECT * FROM " + tableName + " WHERE type=" + q(MarkerType.PRIVATE.toString())
                + " AND player_id=" + playerId
                + " ORDER BY created_at " + orderBy
                + " LIMIT " + pageSize + " OFFSET " + (page * pageSize) + ";";
        return executeListQuery(sql);
    }

    public List<Marker> getPrivateMarkers(int playerId, String orderBy) {
        if (!orderBy.equals("ASC") && !orderBy.equals("DESC")) {
            GPS.logger().warn("Invalid value for orderBy: " + orderBy);
            orderBy = "DESC";
        }
        String sql = "SELECT * FROM " + tableName + " WHERE type=" + q(MarkerType.PRIVATE.toString())
                + " AND player_id=" + playerId
                + " ORDER BY created_at " + orderBy + ";";
        return executeListQuery(sql);
    }

    public List<Marker> getGroupMarker(String groupName, int page, int pageSize) {
        return getGroupMarker(groupName, page, pageSize, "DESC");
    }

    public List<Marker> getGroupMarker(String groupName, int page, int pageSize, String orderBy) {
        if (!orderBy.equals("ASC") && !orderBy.equals("DESC")) {
            GPS.logger().warn("Invalid value for orderBy: " + orderBy);
            orderBy = "DESC";
        }
        return executeListQuery(
                "SELECT * FROM " + tableName + " WHERE type=" + q(MarkerType.GROUP.toString())
                        + " AND group_name=" + q(groupName)
                        + " ORDER BY created_at " + orderBy
                        + " LIMIT " + pageSize + " OFFSET " + (page * pageSize) + ";");
    }

    public List<Marker> getGroupMarkers(String groupName, String orderBy) {
        if (!orderBy.equals("ASC") && !orderBy.equals("DESC")) {
            GPS.logger().warn("Invalid value for orderBy: " + orderBy);
            orderBy = "DESC";
        }
        return executeListQuery(
                "SELECT * FROM " + tableName + " WHERE type=" + q(MarkerType.GROUP.toString())
                        + " AND group_name=" + q(groupName)
                        + " ORDER BY created_at " + orderBy + ";");
    }

    public List<Marker> getGlobalMarker(int page, int pageSize) {
        return getGlobalMarker(page, pageSize, "DESC");
    }

    public List<Marker> getGlobalMarker(int page, int pageSize, String orderBy) {
        if (!orderBy.equals("ASC") && !orderBy.equals("DESC")) {
            GPS.logger().warn("Invalid value for orderBy: " + orderBy);
            orderBy = "DESC";
        }
        return executeListQuery(
                "SELECT * FROM " + tableName + " WHERE type=" + q(MarkerType.GLOBAL.toString())
                        + " ORDER BY created_at " + orderBy
                        + " LIMIT " + pageSize + " OFFSET " + (page * pageSize) + ";");
    }

    public List<Marker> getGlobalMarkers(String orderBy) {
        if (!orderBy.equals("ASC") && !orderBy.equals("DESC")) {
            GPS.logger().warn("Invalid value for orderBy: " + orderBy);
            orderBy = "DESC";
        }
        return executeListQuery(
                "SELECT * FROM " + tableName + " WHERE type=" + q(MarkerType.GLOBAL.toString())
                        + " ORDER BY created_at " + orderBy + ";");
    }

    public int countPrivateMarkers(int playerId) {
        return count("SELECT COUNT(*) FROM " + tableName + " WHERE type=" + q(MarkerType.PRIVATE.toString())
                + " AND player_id=" + playerId + ";");
    }

    public int countGroupMarkers(String groupName) {
        return count("SELECT COUNT(*) FROM " + tableName + " WHERE type=" + q(MarkerType.GROUP.toString())
                + " AND group_name=" + q(groupName) + ";");
    }

    private int count(String query) {
        try (Statement statement = db.createStatement();
                ResultSet result = statement.executeQuery(query)) {
            return result.next() ? result.getInt(1) : 0;
        } catch (Exception e) {
            GPS.logger().error("count markers failed: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private List<Marker> executeListQuery(String query) {
        List<Marker> markers = new ArrayList<>();

        try (Statement statement = db.createStatement();
                ResultSet result = statement.executeQuery(query)) {
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
        execute("""
                CREATE TABLE IF NOT EXISTS gps_marker_purchase (
                    operation_id TEXT PRIMARY KEY, player_id INTEGER NOT NULL, amount INTEGER NOT NULL,
                    currency TEXT NOT NULL, marker_name TEXT NOT NULL, marker_type TEXT NOT NULL,
                    marker_position TEXT NOT NULL, stage TEXT NOT NULL
                );
                """);

        // create table
        execute(
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
        execute("CREATE TABLE IF NOT EXISTS " + gpsAreaTableName + " ("
                + "area_id INTEGER PRIMARY KEY,"
                + "admin_id INTEGER NOT NULL,"
                + "created_at BIGINT NOT NULL,"
                + "allow_gps INTEGER NOT NULL DEFAULT 1"
                + ");");
        execute("CREATE INDEX IF NOT EXISTS idx_gps_area_allow_gps ON " + gpsAreaTableName + " (allow_gps);");
        execute("CREATE TABLE IF NOT EXISTS " + serverPinTableName + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "creator_id INTEGER NOT NULL,"
                + "created_at BIGINT NOT NULL,"
                + "name TEXT NOT NULL,"
                + "icon TEXT NOT NULL,"
                + "address TEXT NOT NULL,"
                + "password TEXT"
                + ");");
    }

    private void execute(String sql) {
        try (Statement statement = db.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            GPS.logger().error("GPS database statement failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean executeUpdate(String sql) {
        try (Statement statement = db.createStatement()) {
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            GPS.logger().error("GPS database update failed: " + e.getMessage());
            return false;
        }
    }
}
