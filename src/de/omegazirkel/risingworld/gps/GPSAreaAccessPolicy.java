package de.omegazirkel.risingworld.gps;

import de.omegazirkel.risingworld.GPS;
import net.risingworld.api.objects.Area;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Utils.ChunkUtils;
import net.risingworld.api.utils.Vector2i;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.utils.Vector3i;

/** Central authorization for GPS sector and area restrictions. */
public final class GPSAreaAccessPolicy {
    // Rising World sectors span 256 chunk columns on each horizontal axis.
    // Keep this in the same coordinate system as Player#getSectorPosition().
    private static final int SECTOR_SIZE_CHUNKS = 256;
    private static final PluginSettings settings = PluginSettings.getInstance();

    private GPSAreaAccessPolicy() {
    }

    public static boolean adminOverride(Player player) {
        return player != null && settings.allowAdminOverride && player.isAdmin()
                && GPSPlayerPreferences.adminOverride(player);
    }

    public static String teleportDenialKey(Player player, Vector3f target, MarkerType type) {
        if (adminOverride(player)) {
            return null;
        }
        if (settings.requireGPSArea && !(type == MarkerType.STATIC && settings.staticMarkersIgnoreGPSArea)
                && !isCurrentAreaAllowed(player)) {
            return "TC_GPS_AREA_REQUIRED";
        }
        if (restrictedToCurrentSector(type) && !isSameSector(player, target)) {
            return "TC_GPS_SECTOR_REQUIRED";
        }
        return null;
    }

    public static String markerCreationDenialKey(Player player, MarkerType type) {
        if (adminOverride(player) || !settings.requireGPSAreaForMarkerCreation
                || (type != MarkerType.PRIVATE && type != MarkerType.GROUP)) {
            return null;
        }
        return isCurrentAreaAllowed(player) ? null : "TC_GPS_AREA_REQUIRED";
    }

    public static boolean areaFeaturesEnabled() {
        return settings.requireGPSArea || settings.requireGPSAreaForMarkerCreation;
    }

    public static boolean gpsUseBlocked(Player player, MarkerType type) {
        return !adminOverride(player) && settings.requireGPSArea
                && !(type == MarkerType.STATIC && settings.staticMarkersIgnoreGPSArea)
                && !isCurrentAreaAllowed(player);
    }

    public static boolean sectorRestrictionEnabled(MarkerType type) {
        return restrictedToCurrentSector(type);
    }

    public static boolean isCurrentAreaAllowed(Player player) {
        if (player == null) {
            return false;
        }
        Area area = player.getCurrentArea();
        return area != null && GPSDatabase.getInstance() != null && GPSDatabase.getInstance().isGPSAreaAllowed(area.getID());
    }

    private static boolean restrictedToCurrentSector(MarkerType type) {
        return switch (type) {
            case PRIVATE -> settings.restrictPrivateMarkersToSector;
            case GROUP -> settings.restrictGroupMarkersToSector;
            case GLOBAL -> settings.restrictGlobalMarkersToSector;
            case STATIC -> false;
        };
    }

    private static boolean isSameSector(Player player, Vector3f target) {
        if (player == null || target == null) {
            return false;
        }
        Vector2i source = player.getSectorPosition();
        if (source == null) {
            Vector3i sourceChunk = player.getChunkPosition();
            if (sourceChunk == null) {
                sourceChunk = ChunkUtils.getChunkPosition(player.getPosition());
            }
            if (sourceChunk == null) {
                return false;
            }
            source = new Vector2i(sectorCoordinate(sourceChunk.x), sectorCoordinate(sourceChunk.z));
        }
        Vector3i targetChunk = ChunkUtils.getChunkPosition(target);
        return targetChunk != null && source.x == sectorCoordinate(targetChunk.x)
                && source.y == sectorCoordinate(targetChunk.z);
    }

    private static int sectorCoordinate(int chunkCoordinate) {
        return Math.floorDiv(chunkCoordinate, SECTOR_SIZE_CHUNKS);
    }
}
