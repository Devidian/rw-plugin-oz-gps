package de.omegazirkel.risingworld.gps;

import de.omegazirkel.risingworld.GPS;
import net.risingworld.api.objects.Player;

public final class GPSPlayerPreferences {
    public static final String SORT_ORDER_KEY = "oz.gps.sort-order";
    public static final String CONFIRM_MARKER_DELETE_KEY = "oz.gps.confirmMarkerDelete";
    public static final String ADMIN_OVERRIDE_KEY = "oz.gps.adminOverride";
    public static final String ENTRY_MODE_KEY = "oz.gps.entryMode";
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
}
