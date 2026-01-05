package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.BasePlayerPluginSettingsPanel;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import de.omegazirkel.risingworld.tools.ui.PlayerPluginSettings;
import net.risingworld.api.objects.Player;

public class GPSPlayerPluginSettings extends PlayerPluginSettings {

    public GPSPlayerPluginSettings() {
        this.pluginLabel = GPS.name;
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
            }

            protected OZUIElement playerSettingMarkerOrder(Player uiPlayer) {
                OZUIElement element = defaultSettingsContainer();
                // label
                element.addChild(defaultSettingsLabel(t().get("TC_LABEL_MARKER_ORDER", uiPlayer)));
                // current value
                String attributeKey = "oz.gps.sort-order";
                String currentValue = uiPlayer.hasAttribute(attributeKey) ? (String) uiPlayer.getAttribute(attributeKey)
                        : "DESC";
                element.addChild(switchButtons(uiPlayer, currentValue == "DESC", event -> {
                    uiPlayer.setAttribute(attributeKey, currentValue == "DESC" ? "ASC" : "DESC");
                    redrawContent();
                }, t().get("TC_BTN_ORDER_ASC", uiPlayer), t().get("TC_BTN_ORDER_DESC", uiPlayer)));
                return element;
            }

        };
    }

}
