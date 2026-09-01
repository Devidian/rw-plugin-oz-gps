package de.omegazirkel.risingworld.gps;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import de.omegazirkel.risingworld.GPS;
import net.risingworld.api.objects.Player;

public final class TeleportCooldowns {

    public static final String LAST_STATIC_TELEPORT_KEY = "oz.gps.last-teleport.static";
    public static final String LAST_PRIVATE_TELEPORT_KEY = "oz.gps.last-teleport.private";
    public static final String LAST_GROUP_TELEPORT_KEY = "oz.gps.last-teleport.group";
    public static final String LAST_GLOBAL_TELEPORT_KEY = "oz.gps.last-teleport.global";

    private static final PluginSettings settings = PluginSettings.getInstance();

    private TeleportCooldowns() {
    }

    public static boolean isEnabled(MarkerType type) {
        return configuredSeconds(type) > 0;
    }

    public static int configuredSeconds(MarkerType type) {
        switch (type) {
            case STATIC:
                return safeSeconds(settings.useStaticMarkerCooldownSeconds);
            case PRIVATE:
                return safeSeconds(settings.usePrivateMarkerCooldownSeconds);
            case GROUP:
                return safeSeconds(settings.useGroupMarkerCooldownSeconds);
            case GLOBAL:
                return safeSeconds(settings.useGlobalMarkerCooldownSeconds);
            default:
                return 0;
        }
    }

    public static String keyFor(MarkerType type) {
        switch (type) {
            case STATIC:
                return LAST_STATIC_TELEPORT_KEY;
            case PRIVATE:
                return LAST_PRIVATE_TELEPORT_KEY;
            case GROUP:
                return LAST_GROUP_TELEPORT_KEY;
            case GLOBAL:
                return LAST_GLOBAL_TELEPORT_KEY;
            default:
                return "";
        }
    }

    public static Optional<String> lastUsedIso(Player player, MarkerType type) {
        if (GPS.ps == null) {
            return Optional.empty();
        }
        return GPS.ps.getString(player.getDbID(), keyFor(type));
    }

    public static int remainingSeconds(Player player, MarkerType type) {
        int cooldownSeconds = configuredSeconds(type);
        if (cooldownSeconds <= 0 || GPS.ps == null) {
            return 0;
        }

        Optional<String> lastUsed = lastUsedIso(player, type);
        if (lastUsed.isEmpty()) {
            return 0;
        }

        try {
            Instant lastUsedAt = Instant.parse(lastUsed.get());
            long elapsedSeconds = Duration.between(lastUsedAt, Instant.now()).getSeconds();
            long remaining = cooldownSeconds - elapsedSeconds;
            return remaining > 0 ? (int) remaining : 0;
        } catch (DateTimeParseException ex) {
            GPS.logger().warn("Invalid GPS cooldown timestamp for player " + player.getDbID() + " and type " + type
                    + ": " + lastUsed.get());
            return 0;
        }
    }

    public static boolean isCoolingDown(Player player, MarkerType type) {
        return remainingSeconds(player, type) > 0;
    }

    public static void recordUse(Player player, MarkerType type) {
        if (!isEnabled(type) || GPS.ps == null) {
            return;
        }
        GPS.ps.setString(player.getDbID(), keyFor(type), Instant.now().toString());
    }

    public static String displayTypeKey(MarkerType type) {
        switch (type) {
            case STATIC:
                return "tc.menu.static.marker";
            case PRIVATE:
                return "tc.menu.private.marker";
            case GROUP:
                return "tc.menu.group.marker";
            case GLOBAL:
                return "tc.menu.global.marker";
            default:
                return "";
        }
    }

    private static int safeSeconds(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }
}
