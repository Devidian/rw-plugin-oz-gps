package de.omegazirkel.risingworld.gps.exports;

import de.omegazirkel.risingworld.gps.PluginSettings;

public record GpsRouteExposure(boolean globalMarkers) {

    public static GpsRouteExposure from(PluginSettings settings) {
        return new GpsRouteExposure(settings.exposeGlobalMarkers);
    }
}
