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
        Path settings = Files.createTempFile("oz-gps-settings-", ".properties");
        Files.writeString(settings, "exposeGlobalMarkers=false\n");

        PluginSettings pluginSettings = PluginSettings.getInstance();
        pluginSettings.initSettings(settings.toString());

        assertFalse(GpsRouteExposure.from(pluginSettings).globalMarkers());

        Files.writeString(settings, "");
        pluginSettings.initSettings(settings.toString());

        assertTrue(GpsRouteExposure.from(pluginSettings).globalMarkers());
    }
}
