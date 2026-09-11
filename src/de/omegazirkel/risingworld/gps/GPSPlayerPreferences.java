package de.omegazirkel.risingworld.gps;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.ui.PluginShortcutVisibility;
import net.risingworld.api.objects.Player;

public final class GPSPlayerPreferences {
    public static final String SORT_ORDER_KEY = "oz.gps.sort-order";
    public static final String CONFIRM_MARKER_DELETE_KEY = "oz.gps.confirmMarkerDelete";
    public static final String ADMIN_OVERRIDE_KEY = "oz.gps.adminOverride";
    public static final String ENTRY_MODE_KEY = "oz.gps.entryMode";
    public static final String OBSERVE_PRIVATE_TELEPORTS_KEY = "oz.gps.observePrivateTeleports";
    public static final String OBSERVE_GROUP_TELEPORTS_KEY = "oz.gps.observeGroupTeleports";
    public static final String OBSERVE_GLOBAL_TELEPORTS_KEY = "oz.gps.observeGlobalTeleports";
    public static final String ENTRY_MODE_GRID = "GRID";
    public static final String ENTRY_MODE_RADIAL = "RADIAL";

    private GPSPlayerPreferences() {
    }

    public static void load(Player player) {
        int dbId = player.getDbID();
        if (!player.hasAttribute(SORT_ORDER_KEY)) {
            player.setAttribute(SORT_ORDER_KEY, GPS.ps.getString(dbId, SORT_ORDER_KEY).orElse("DESC"));
        }
        if (!player.hasAttribute(CONFIRM_MARKER_DELETE_KEY)) {
            player.setAttribute(CONFIRM_MARKER_DELETE_KEY,
                    GPS.ps.getBoolean(dbId, CONFIRM_MARKER_DELETE_KEY).orElse(true));
        }
        if (!player.hasAttribute(ADMIN_OVERRIDE_KEY)) {
            player.setAttribute(ADMIN_OVERRIDE_KEY, GPS.ps.getBoolean(dbId, ADMIN_OVERRIDE_KEY).orElse(false));
        }
        if (!player.hasAttribute(ENTRY_MODE_KEY)) {
            player.setAttribute(ENTRY_MODE_KEY, GPS.ps.getString(dbId, ENTRY_MODE_KEY).orElse(ENTRY_MODE_GRID));
        }
        loadBoolean(player, OBSERVE_PRIVATE_TELEPORTS_KEY, false);
        loadBoolean(player, OBSERVE_GROUP_TELEPORTS_KEY, false);
        loadBoolean(player, OBSERVE_GLOBAL_TELEPORTS_KEY, false);
        String shortcutKey = shortcutKey();
        if (!player.hasAttribute(shortcutKey)) {
            player.setAttribute(shortcutKey, GPS.ps.getBoolean(dbId, shortcutKey).orElse(true));
        }
    }

    public static boolean confirmMarkerDelete(Player player) {
        if (!player.hasAttribute(CONFIRM_MARKER_DELETE_KEY)) {
            load(player);
        }
        Object value = player.getAttribute(CONFIRM_MARKER_DELETE_KEY);
        return !(value instanceof Boolean) || (Boolean) value;
    }

    public static void setConfirmMarkerDelete(Player player, boolean value) {
        player.setAttribute(CONFIRM_MARKER_DELETE_KEY, value);
        GPS.ps.setBoolean(player.getDbID(), CONFIRM_MARKER_DELETE_KEY, value);
    }

    public static String markerSortOrder(Player player) {
        if (!player.hasAttribute(SORT_ORDER_KEY)) {
            load(player);
        }
        return player.getAttribute(SORT_ORDER_KEY).toString();
    }

    public static void setMarkerSortOrder(Player player, String value) {
        player.setAttribute(SORT_ORDER_KEY, value);
        GPS.ps.setString(player.getDbID(), SORT_ORDER_KEY, value);
    }

    public static boolean adminOverride(Player player) {
        if (!player.hasAttribute(ADMIN_OVERRIDE_KEY)) {
            load(player);
        }
        Object value = player.getAttribute(ADMIN_OVERRIDE_KEY);
        return value instanceof Boolean && (Boolean) value;
    }

    public static void setAdminOverride(Player player, boolean value) {
        player.setAttribute(ADMIN_OVERRIDE_KEY, value);
        GPS.ps.setBoolean(player.getDbID(), ADMIN_OVERRIDE_KEY, value);
    }

    public static String entryMode(Player player) {
        if (!player.hasAttribute(ENTRY_MODE_KEY)) {
            load(player);
        }
        Object value = player.getAttribute(ENTRY_MODE_KEY);
        return ENTRY_MODE_RADIAL.equals(value) ? ENTRY_MODE_RADIAL : ENTRY_MODE_GRID;
    }

    public static void setEntryMode(Player player, String value) {
        String normalizedValue = ENTRY_MODE_RADIAL.equals(value) ? ENTRY_MODE_RADIAL : ENTRY_MODE_GRID;
        player.setAttribute(ENTRY_MODE_KEY, normalizedValue);
        GPS.ps.setString(player.getDbID(), ENTRY_MODE_KEY, normalizedValue);
    }

    public static boolean observesTeleports(Player player, MarkerType type) {
        String key = switch (type) {
            case PRIVATE -> OBSERVE_PRIVATE_TELEPORTS_KEY;
            case GROUP -> OBSERVE_GROUP_TELEPORTS_KEY;
            case GLOBAL -> OBSERVE_GLOBAL_TELEPORTS_KEY;
            default -> "";
        };
        if (key.isEmpty()) return false;
        if (!player.hasAttribute(key)) load(player);
        return Boolean.TRUE.equals(player.getAttribute(key));
    }

    public static void setObservesTeleports(Player player, MarkerType type, boolean value) {
        String key = switch (type) {
            case PRIVATE -> OBSERVE_PRIVATE_TELEPORTS_KEY;
            case GROUP -> OBSERVE_GROUP_TELEPORTS_KEY;
            case GLOBAL -> OBSERVE_GLOBAL_TELEPORTS_KEY;
            default -> "";
        };
        if (key.isEmpty()) return;
        player.setAttribute(key, value);
        GPS.ps.setBoolean(player.getDbID(), key, value);
    }

    public static boolean shortcutVisible(Player player) {
        if (!player.hasAttribute(shortcutKey())) {
            load(player);
        }
        Object value = player.getAttribute(shortcutKey());
        return !(value instanceof Boolean) || (Boolean) value;
    }

    public static void setShortcutVisible(Player player, boolean value) {
        String key = shortcutKey();
        player.setAttribute(key, value);
        GPS.ps.setBoolean(player.getDbID(), key, value);
    }

    private static String shortcutKey() {
        return PluginShortcutVisibility.playerSettingKey(GPS.name);
    }

    private static void loadBoolean(Player player, String key, boolean defaultValue) {
        if (!player.hasAttribute(key)) {
            player.setAttribute(key, GPS.ps.getBoolean(player.getDbID(), key).orElse(defaultValue));
        }
    }
}
