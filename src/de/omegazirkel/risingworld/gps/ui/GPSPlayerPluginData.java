package de.omegazirkel.risingworld.gps.ui;

import java.util.ArrayList;
import java.util.Arrays;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.GPSPlayerPreferences;
import de.omegazirkel.risingworld.gps.MarkerType;
import de.omegazirkel.risingworld.gps.TeleportCooldowns;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.BasePlayerPluginDataPanel;
import de.omegazirkel.risingworld.tools.ui.PlayerPluginData;
import de.omegazirkel.risingworld.tools.ui.table.TableCell;
import de.omegazirkel.risingworld.tools.ui.table.TableRow;
import de.omegazirkel.risingworld.tools.ui.table.TableScrollView;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;

public class GPSPlayerPluginData extends PlayerPluginData {

    public GPSPlayerPluginData(String pluginVersion) {
        this.pluginLabel = GPS.name;
        this.pluginVersion = pluginVersion;
    }

    private I18n t() {
        return I18n.getInstance(GPS.name);
    }

    @Override
    public BasePlayerPluginDataPanel createPlayerPluginDataUIElement(Player uiPlayer) {
        return new BasePlayerPluginDataPanel(uiPlayer, pluginLabel) {
            @Override
            protected void redrawContent() {
                flexWrapper.removeAllChilds();

                TableScrollView table = new TableScrollView(
                        Arrays.asList(
                                t().get("TC_DATA_COL_DESCRIPTION", uiPlayer),
                                "key",
                                "value"),
                        Arrays.asList(38f, 42f, 20f));
                table.setPosition(0, 0, false);
                table.style.width.set(100, Unit.Percent);
                table.setScrollBodyHeight(400);

                addCooldownRows(table, uiPlayer, MarkerType.STATIC, t().get("TC_MENU_STATIC_MARKER", uiPlayer));
                addCooldownRows(table, uiPlayer, MarkerType.GROUP, t().get("TC_MENU_GROUP_MARKER", uiPlayer));
                addCooldownRows(table, uiPlayer, MarkerType.PRIVATE, t().get("TC_MENU_PRIVATE_MARKER", uiPlayer));
                addCooldownRows(table, uiPlayer, MarkerType.GLOBAL, t().get("TC_MENU_GLOBAL_MARKER", uiPlayer));
                addRow(table, t().get("TC_DATA_MARKER_SORT_ORDER", uiPlayer), "oz.gps.sort-order",
                        attributeValue(uiPlayer, "oz.gps.sort-order", "-"));
                addRow(table, t().get("TC_DATA_CONFIRM_MARKER_DELETE", uiPlayer),
                        GPSPlayerPreferences.CONFIRM_MARKER_DELETE_KEY,
                        String.valueOf(GPSPlayerPreferences.confirmMarkerDelete(uiPlayer)));
                addRow(table, t().get("TC_DATA_ENTRY_MODE", uiPlayer), GPSPlayerPreferences.ENTRY_MODE_KEY,
                        GPSPlayerPreferences.entryMode(uiPlayer));
                addRow(table, t().get("TC_DATA_LAST_DEATH_POSITION", uiPlayer), "death-location",
                        attributeValue(uiPlayer, "death-location", "-"));
                addRow(table, t().get("TC_DATA_PRE_PORT_POSITION", uiPlayer), "pre-port-location",
                        attributeValue(uiPlayer, "pre-port-location", "-"));

                flexWrapper.addChild(table.getRoot());
            }

            private void addCooldownRows(TableScrollView table, Player player, MarkerType type, String markerTypeLabel) {
                addRow(table,
                        t().get("TC_DATA_LAST_TELEPORT", player).replace("PH_MARKER_TYPE", markerTypeLabel),
                        TeleportCooldowns.keyFor(type),
                        TeleportCooldowns.lastUsedIso(player, type).orElse("-"));
                addRow(table,
                        t().get("TC_DATA_COOLDOWN_REMAINING", player).replace("PH_MARKER_TYPE", markerTypeLabel),
                        TeleportCooldowns.keyFor(type) + ".remainingSeconds",
                        String.valueOf(TeleportCooldowns.remainingSeconds(player, type)));
            }

            private void addRow(TableScrollView table, String description, String key, String value) {
                table.addRow(new TableRow(new ArrayList<>(Arrays.asList(
                        cell(description, 38f),
                        cell(key, 42f),
                        cell(value, 20f)))));
            }

            private String attributeValue(Player player, String key, String defaultValue) {
                if (!player.hasAttribute(key)) {
                    return defaultValue;
                }
                Object value = player.getAttribute(key);
                return value == null ? defaultValue : String.valueOf(value);
            }

            private TableCell cell(String text, float width) {
                UILabel label = new UILabel(text == null ? "" : text);
                label.setFont(Font.Default);
                label.setFontSize(13);
                label.setTextWrap(false);
                label.setTextAlign(TextAnchor.MiddleLeft);
                return new TableCell(label, width);
            }
        };
    }
}
