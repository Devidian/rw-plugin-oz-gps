package de.omegazirkel.risingworld.gps;

import net.risingworld.api.objects.Player;

public final class MarkerPermissions {
    private static final PluginSettings s = PluginSettings.getInstance();

    private MarkerPermissions() {
    }

    public static boolean canManage(Player player, Marker marker) {
        if (player == null || marker == null) {
            return false;
        }

        switch (marker.getType()) {
            case PRIVATE:
                return s.enablePrivateMarkers && marker.getPlayerId() == player.getDbID();
            case GROUP:
                return s.enableGroupMarkers && sameGroup(player, marker);
            case GLOBAL:
                return s.enableGlobalMarkers && player.isAdmin();
            case STATIC:
            default:
                return false;
        }
    }

    private static boolean sameGroup(Player player, Marker marker) {
        String playerGroup = player.getPermissionGroup();
        String markerGroup = marker.getGroup();
        return playerGroup != null && markerGroup != null && playerGroup.equals(markerGroup);
    }
}
