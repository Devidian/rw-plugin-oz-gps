package de.omegazirkel.risingworld.gps;

import net.risingworld.api.objects.Player;

public final class GPSAccessPolicy {
    private static final PluginSettings settings = PluginSettings.getInstance();

    private GPSAccessPolicy() {
    }

    public static boolean canUse(Player player, MarkerType type) {
        return type == MarkerType.STATIC || canUseNonStatic(player);
    }

    public static boolean canUseNonStatic(Player player) {
        if (player == null) {
            return false;
        }
        if (settings.minimumPlaytimeMinutes <= 0) {
            return true;
        }
        if (settings.allowAdminOverride && player.isAdmin() && GPSPlayerPreferences.adminOverride(player)) {
            return true;
        }
        return Math.max(0, player.getTotalPlayTime()) >= requiredSeconds();
    }

    public static int requiredMinutes() {
        return Math.max(0, settings.minimumPlaytimeMinutes);
    }

    public static int remainingMinutes(Player player) {
        if (canUseNonStatic(player)) {
            return 0;
        }
        long remainingSeconds = Math.max(0L, requiredSeconds() - Math.max(0, player.getTotalPlayTime()));
        return (int) Math.max(1L, (remainingSeconds + 59L) / 60L);
    }

    private static long requiredSeconds() {
        return (long) requiredMinutes() * 60L;
    }
}
