package de.omegazirkel.risingworld.gps.exports;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import de.omegazirkel.risingworld.gps.PluginSettings;

public class GpsRouteExposureTest {

    @Test
    public void loadsGlobalMarkerExposureFlagFromSettings() throws Exception {
        Path directory = Files.createTempDirectory("oz-gps-settings-");
        Path settings = directory.resolve("settings.world.json");
        Files.writeString(directory.resolve("settings.default.json"), "{\"exposeGlobalMarkers\":true}");
        Files.writeString(settings, "{\"exposeGlobalMarkers\":false}");

        PluginSettings pluginSettings = PluginSettings.getInstance();
        pluginSettings.initSettings(settings.toString());

        assertFalse(GpsRouteExposure.from(pluginSettings).globalMarkers());

        Files.writeString(settings, "");
        pluginSettings.initSettings(settings.toString());

        assertTrue(GpsRouteExposure.from(pluginSettings).globalMarkers());
    }
}
