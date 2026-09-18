package de.omegazirkel.risingworld.gps;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ServerPinIconCatalogTest {
    @Test
    public void exposesDedicatedPermanentServerIcons() {
        assertTrue(PluginGUI.serverPinKeys.contains("marker-server-16"));
        assertTrue(PluginGUI.serverPinKeys.contains("marker-server-17"));
        assertTrue(PluginGUI.serverPinKeys.contains("marker-server-18"));
        assertTrue(PluginGUI.serverPinKeys.contains("marker-server-19"));
    }
}
