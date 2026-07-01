package de.omegazirkel.risingworld.gps.exports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class GpsMarkerExportService {
    private static final int SCHEMA_VERSION = 1;

    private final Connection connection;

    public GpsMarkerExportService(Connection connection) {
        this.connection = connection;
    }

    public GpsMarkerExportResponse exportGlobalMarkers(Long lastChange) throws SQLException {
        long cursor = lastChange == null ? -1L : lastChange.longValue();
        List<GpsMarkerExport> markers = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT id, name, pos_x, pos_y, pos_z, icon, color, created_at
                FROM marker
                WHERE type = 'GLOBAL' AND created_at > ?
                ORDER BY created_at DESC, id DESC;
                """)) {
            statement.setLong(1, cursor);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    markers.add(readMarker(result));
                }
            }
        }
        return new GpsMarkerExportResponse(SCHEMA_VERSION, "global", markers);
    }

    private static GpsMarkerExport readMarker(ResultSet result) throws SQLException {
        return new GpsMarkerExport(
                result.getInt("id"),
                result.getString("name"),
                result.getFloat("pos_x"),
                result.getFloat("pos_y"),
                result.getFloat("pos_z"),
                result.getString("icon"),
                packedIntRgba(result.getInt("color")),
                result.getLong("created_at"));
    }

    private static String packedIntRgba(int value) {
        return "#" + String.format("%08X", value);
    }
}
