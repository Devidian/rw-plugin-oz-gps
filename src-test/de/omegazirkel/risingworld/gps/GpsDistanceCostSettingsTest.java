package de.omegazirkel.risingworld.gps;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public class GpsDistanceCostSettingsTest {

    @Test
    public void usesNewDefaultsAndPreservesCustomizedLegacyCost() throws Exception {
        Path directory = Files.createTempDirectory("oz-gps-distance-cost-");
        Path worldSettings = directory.resolve("settings.world.json");
        Files.writeString(directory.resolve("settings.default.json"), "{\"general\":{\"travelDistanceCostPerBlock\":1}}");
        PluginSettings settings = PluginSettings.getInstance();

        Files.writeString(worldSettings, "{\"general\":{\"travelDistanceCostPerBlock\":1}}");
        settings.initSettings(worldSettings.toString());
        assertCosts(settings, 100, 100, 100, 100);

        Files.writeString(worldSettings, "{\"general\":{\"travelDistanceCostPerBlock\":75}}");
        settings.initSettings(worldSettings.toString());
        assertCosts(settings, 75, 75, 75, 75);

        Files.writeString(worldSettings, "{\"general\":{\"travelDistanceCostPerBlock\":75,"
                + "\"travelDistanceBaseCostStatic\":0,\"travelDistanceBaseCostPrivate\":120,"
                + "\"travelDistanceBaseCostGroup\":80,\"travelDistanceBaseCostGlobal\":30}}");
        settings.initSettings(worldSettings.toString());
        assertCosts(settings, 0, 120, 80, 30);
    }

    private static void assertCosts(PluginSettings settings, int staticCost, int privateCost, int groupCost,
            int globalCost) {
        assertEquals(staticCost, settings.travelDistanceBaseCostStatic.intValue());
        assertEquals(privateCost, settings.travelDistanceBaseCostPrivate.intValue());
        assertEquals(groupCost, settings.travelDistanceBaseCostGroup.intValue());
        assertEquals(globalCost, settings.travelDistanceBaseCostGlobal.intValue());
    }
}
