package de.omegazirkel.risingworld.gps.exports;

import java.util.List;

public record GpsMarkerExportResponse(
        int schemaVersion,
        String type,
        List<GpsMarkerExport> markers) {

    public GpsMarkerExportResponse {
        markers = List.copyOf(markers);
    }
}
