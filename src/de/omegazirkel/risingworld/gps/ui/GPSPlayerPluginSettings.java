package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.GPSPlayerPreferences;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.BasePlayerPluginSettingsPanel;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import de.omegazirkel.risingworld.tools.ui.PlayerPluginSettings;
import net.risingworld.api.objects.Player;

public class GPSPlayerPluginSettings extends PlayerPluginSettings {

    public GPSPlayerPluginSettings(String pluginVersion) {
        this.pluginLabel = GPS.name;
        this.pluginVersion = pluginVersion;
    }

    private final I18n t() {
        return I18n.getInstance(GPS.name);
    }

    @Override
    public BasePlayerPluginSettingsPanel createPlayerPluginSettingsUIElement(Player uiPlayer) {
        return new BasePlayerPluginSettingsPanel(uiPlayer, pluginLabel) {
            @Override
            protected void redrawContent() {
                flexWrapper.removeAllChilds();
                flexWrapper.addChild(playerSettingMarkerOrder(uiPlayer));
                flexWrapper.addChild(playerSettingEntryMode(uiPlayer));
                flexWrapper.addChild(playerSettingConfirmMarkerDelete(uiPlayer));
                if (uiPlayer.isAdmin()) {
                    flexWrapper.addChild(playerSettingAdminOverride(uiPlayer));
                }
            }

            protected OZUIElement playerSettingMarkerOrder(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                // label
                element.addChild(defaultSettingsLabel(t().get("TC_LABEL_MARKER_ORDER", uiPlayer)));
                // current value
                String currentValue = GPSPlayerPreferences.markerSortOrder(uiPlayer);
                element.addChild(switchButtons(uiPlayer, "DESC".equals(currentValue), event -> {
                    GPSPlayerPreferences.setMarkerSortOrder(uiPlayer, "DESC".equals(currentValue) ? "ASC" : "DESC");
                    redrawContent();
                }, t().get("TC_BTN_ORDER_ASC", uiPlayer), t().get("TC_BTN_ORDER_DESC", uiPlayer)));
                return element;
            }

            protected OZUIElement playerSettingConfirmMarkerDelete(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                element.addChild(defaultSettingsLabel(t().get("TC_LABEL_CONFIRM_MARKER_DELETE", uiPlayer)));
                boolean currentValue = GPSPlayerPreferences.confirmMarkerDelete(uiPlayer);
                element.addChild(switchButtons(uiPlayer, currentValue, event -> {
                    GPSPlayerPreferences.setConfirmMarkerDelete(uiPlayer, !currentValue);
                    redrawContent();
                }));
                return element;
            }

            protected OZUIElement playerSettingEntryMode(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                element.addChild(defaultSettingsLabel(t().get("TC_LABEL_ENTRY_MODE", uiPlayer)));
                String currentValue = GPSPlayerPreferences.entryMode(uiPlayer);
                boolean radialMode = GPSPlayerPreferences.ENTRY_MODE_RADIAL.equals(currentValue);
                element.addChild(switchButtons(uiPlayer, radialMode, event -> {
                    GPSPlayerPreferences.setEntryMode(uiPlayer,
                            radialMode ? GPSPlayerPreferences.ENTRY_MODE_GRID
                                    : GPSPlayerPreferences.ENTRY_MODE_RADIAL);
                    redrawContent();
                }, t().get("TC_BTN_ENTRY_GRID", uiPlayer), t().get("TC_BTN_ENTRY_RADIAL", uiPlayer)));
                return element;
            }

            protected OZUIElement playerSettingAdminOverride(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                element.addChild(defaultSettingsLabel(t().get("TC_LABEL_ADMIN_OVERRIDE", uiPlayer)));
                boolean currentValue = GPSPlayerPreferences.adminOverride(uiPlayer);
                element.addChild(switchButtons(uiPlayer, currentValue, event -> {
                    GPSPlayerPreferences.setAdminOverride(uiPlayer, !currentValue);
                    redrawContent();
                }));
                return element;
            }

        };
    }

}
