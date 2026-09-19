package de.omegazirkel.risingworld.gps;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class MarkerIconCatalogTest {
    private static final String[] NEW_MARKER_KEYS = {
            "marker-stargate-desert", "marker-stargate-forest", "marker-stargate-arctic", "marker-stargate-meadow", "marker-stargate-inferno",
            "marker-mountain-tavern", "marker-coast-town", "marker-desert-town", "marker-arctic-town",
            "marker-mountain-mine", "marker-cave-mineshaft", "marker-village-trading-post", "marker-village-blacksmith",
            "marker-coast-fisherman", "marker-village-weekly-market" };

    @Test
    public void exposesRequestedMarkerIconsInBothThemes() {
        for (String key : NEW_MARKER_KEYS) {
            assertTrue(key, PluginGUI.markerKeys.contains(key));
            assertNotNull(key + " modern", getClass().getResource("/assets/icons/modern/" + key + ".png"));
            assertNotNull(key + " classic", getClass().getResource("/assets/icons/classic/" + key + ".png"));
        }
    }
}
