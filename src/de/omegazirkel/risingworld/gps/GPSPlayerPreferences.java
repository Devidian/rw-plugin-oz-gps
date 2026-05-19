package de.omegazirkel.risingworld.gps;

import de.omegazirkel.risingworld.GPS;
import net.risingworld.api.objects.Player;

public final class GPSPlayerPreferences {
    public static final String SORT_ORDER_KEY = "oz.gps.sort-order";
    public static final String CONFIRM_MARKER_DELETE_KEY = "oz.gps.confirmMarkerDelete";

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
}
