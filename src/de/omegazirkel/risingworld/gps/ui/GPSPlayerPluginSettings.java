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
                flexWrapper.addChild(playerSettingShortcut(uiPlayer));
                flexWrapper.addChild(playerSettingConfirmMarkerDelete(uiPlayer));
                if (uiPlayer.isAdmin()) {
                    flexWrapper.addChild(playerSettingAdminOverride(uiPlayer));
                }
            }

            protected OZUIElement playerSettingMarkerOrder(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                // label
                element.addChild(defaultSettingsLabel(t().get("tc.label.marker.order", uiPlayer)));
                // current value
                String currentValue = GPSPlayerPreferences.markerSortOrder(uiPlayer);
                element.addChild(switchButtons(uiPlayer, "DESC".equals(currentValue), event -> {
                    GPSPlayerPreferences.setMarkerSortOrder(uiPlayer, "DESC".equals(currentValue) ? "ASC" : "DESC");
                    redrawContent();
                }, t().get("tc.btn.order.asc", uiPlayer), t().get("tc.btn.order.desc", uiPlayer)));
                return element;
            }

            protected OZUIElement playerSettingConfirmMarkerDelete(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                element.addChild(defaultSettingsLabel(t().get("tc.label.confirm.marker.delete", uiPlayer)));
                boolean currentValue = GPSPlayerPreferences.confirmMarkerDelete(uiPlayer);
                element.addChild(switchButtons(uiPlayer, currentValue, event -> {
                    GPSPlayerPreferences.setConfirmMarkerDelete(uiPlayer, !currentValue);
                    redrawContent();
                }));
                return element;
            }

            protected OZUIElement playerSettingEntryMode(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                element.addChild(defaultSettingsLabel(t().get("tc.label.entry.mode", uiPlayer)));
                String currentValue = GPSPlayerPreferences.entryMode(uiPlayer);
                boolean radialMode = GPSPlayerPreferences.ENTRY_MODE_RADIAL.equals(currentValue);
                element.addChild(switchButtons(uiPlayer, radialMode, event -> {
                    GPSPlayerPreferences.setEntryMode(uiPlayer,
                            radialMode ? GPSPlayerPreferences.ENTRY_MODE_GRID
                                    : GPSPlayerPreferences.ENTRY_MODE_RADIAL);
                    redrawContent();
                }, t().get("tc.btn.entry.grid", uiPlayer), t().get("tc.btn.entry.radial", uiPlayer)));
                return element;
            }

            protected OZUIElement playerSettingAdminOverride(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                element.addChild(defaultSettingsLabel(t().get("tc.label.admin.override", uiPlayer)));
                boolean currentValue = GPSPlayerPreferences.adminOverride(uiPlayer);
                element.addChild(switchButtons(uiPlayer, currentValue, event -> {
                    GPSPlayerPreferences.setAdminOverride(uiPlayer, !currentValue);
                    redrawContent();
                }));
                return element;
            }

            protected OZUIElement playerSettingShortcut(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                element.addChild(defaultSettingsLabel(t().get("tc.label.gps.shortcut", uiPlayer)));
                boolean currentValue = GPSPlayerPreferences.shortcutVisible(uiPlayer);
                element.addChild(switchButtons(uiPlayer, currentValue, event -> {
                    GPSPlayerPreferences.setShortcutVisible(uiPlayer, !currentValue);
                    redrawContent();
                }));
                return element;
            }

        };
    }

}
