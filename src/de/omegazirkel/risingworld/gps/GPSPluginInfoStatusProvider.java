package de.omegazirkel.risingworld.gps;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.PluginInfoStatusProvider;
import net.risingworld.api.objects.Player;

public class GPSPluginInfoStatusProvider implements PluginInfoStatusProvider {
    private final GPS plugin;
    private final String pluginName;
    private final String version;

    public GPSPluginInfoStatusProvider(GPS plugin, String version) {
        this.plugin = plugin;
        this.pluginName = GPS.name == null || GPS.name.isBlank() ? "OZ - GPS" : GPS.name;
        this.version = version == null ? "" : version;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public String getInfo(Player player) {
        return t().get("TC_GPS_INFO_PANEL_INFO", player)
                .replace("PH_PLUGIN_NAME", pluginName)
                .replace("PH_VERSION", version)
                .replace("PH_PLUGIN_CMD", "gps");
    }

    @Override
    public String getStatus(Player player) {
        PluginSettings settings = PluginSettings.getInstance();
        GPSEconomy economy = GPSEconomy.getInstance();
        return t().get("TC_GPS_INFO_PANEL_STATUS", player)
                .replace("PH_ENTRY_MODE", GPSPlayerPreferences.entryMode(player))
                .replace("PH_TRAVEL_COST_MODE", settings.travelCostMode)
                .replace("PH_MARKER_CREATE_COSTS", String.valueOf(settings.enableMarkerCreateCosts))
                .replace("PH_TELEPORT_TOKENS", String.valueOf(settings.enableTeleportTokens))
                .replace("PH_WALLET_STATUS", available(economy != null && economy.walletAvailable()))
                .replace("PH_SHOP_STATUS", available(economy != null && economy.shopAvailable()))
                .replace("PH_LANGUAGE", player.getLanguage() + " / " + player.getSystemLanguage())
                .replace("PH_USEDLANG", t().getLanguageUsed(player.getSystemLanguage()))
                .replace("PH_LANG_AVAILABLE", t().getLanguageAvailable());
    }

    private I18n t() {
        return I18n.getInstance(plugin);
    }

    private static String available(boolean value) {
        return value ? "available" : "missing";
    }
}
