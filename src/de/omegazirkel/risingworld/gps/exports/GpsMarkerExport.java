package de.omegazirkel.risingworld.gps.exports;

public record GpsMarkerExport(
        int id,
        String name,
        float x,
        float y,
        float z,
        String icon,
        String color,
        long createdAt) {
}
