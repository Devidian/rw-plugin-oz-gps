package de.omegazirkel.risingworld.gps;

import de.omegazirkel.risingworld.GPS;
import net.risingworld.api.Plugin;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Utils.ChunkUtils;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.utils.Vector3i;

public class GPSEconomy {
    private static final String PLUGIN_IDENTIFIER = "OZ - GPS";
    private static GPSEconomy instance;

    private final WalletBridge wallet;
    private final ShopBridge shop;
    private PluginSettings settings;

    private GPSEconomy(Plugin plugin, PluginSettings settings) {
        this.wallet = new WalletBridge(plugin);
        this.shop = new ShopBridge(plugin, wallet);
        this.settings = settings;
    }

    public static GPSEconomy init(Plugin plugin, PluginSettings settings) {
        instance = new GPSEconomy(plugin, settings);
        instance.refreshIntegrations();
        return instance;
    }

    public static GPSEconomy getInstance() {
        return instance;
    }

    public void updateSettings(PluginSettings settings) {
        this.settings = settings;
        refreshIntegrations();
    }

    public void refreshIntegrations() {
        if (wallet.isAvailable() && settings.enableTeleportTokens) {
            WalletBridge.WalletCallResult result = wallet.registerCurrency(settings.teleportTokenCurrencyIdentifier,
                    settings.teleportTokenCurrencyName, settings.teleportTokenIcon, PLUGIN_IDENTIFIER);
            if (!result.success()) {
                GPS.logger().warn("Could not register GPS teleport token currency: " + result.message());
            }
        }
        shop.registerTokenOffers(settings);
    }

    public boolean walletAvailable() {
        return wallet.isAvailable();
    }

    public boolean shopAvailable() {
        return shop.isAvailable();
    }

    public boolean hasAdminOverride(Player player) {
        return settings.allowAdminOverride
                && player != null
                && player.isAdmin()
                && GPSPlayerPreferences.adminOverride(player);
    }

    public long teleportCost(Player player, Vector3f targetPosition, MarkerType type) {
        if (settings.travelCostMode == null || settings.travelCostMode.equalsIgnoreCase("disabled")) {
            return 0L;
        }
        if (settings.travelCostMode.equalsIgnoreCase("fixed")) {
            return fixedUseCost(type);
        }
        if (settings.travelCostMode.equalsIgnoreCase("distance")) {
            return distanceCost(player, targetPosition);
        }
        return 0L;
    }

    public String teleportCurrency(MarkerType type) {
        return settings.travelCostCurrencyIdentifier;
    }

    public EconomyResult chargeTeleport(Player player, Vector3f targetPosition, MarkerType type, String label) {
        long cost = teleportCost(player, targetPosition, type);
        if (cost <= 0 || hasAdminOverride(player)) {
            return EconomyResult.ok("");
        }
        if (!wallet.isAvailable()) {
            return EconomyResult.fail("OZ - Wallet is required for GPS travel costs.");
        }
        WalletBridge.WalletCallResult result = wallet.withdraw(player.getDbID(), cost,
                "GPS teleport: " + label, teleportCurrency(type), PLUGIN_IDENTIFIER);
        return result.success() ? EconomyResult.ok(costLabel(cost, teleportCurrency(type)))
                : EconomyResult.fail(result.message());
    }

    public long markerCreateCost(MarkerType type) {
        if (!settings.enableMarkerCreateCosts) {
            return 0L;
        }
        return switch (type) {
            case PRIVATE -> settings.createPrivateMarkerCost;
            case GROUP -> settings.createGroupMarkerCost;
            case GLOBAL, STATIC -> 0L;
        };
    }

    public EconomyResult chargeMarkerCreation(Player player, MarkerType type) {
        long cost = markerCreateCost(type);
        if (cost <= 0 || hasAdminOverride(player)) {
            return EconomyResult.ok("");
        }
        if (!wallet.isAvailable()) {
            return EconomyResult.fail("OZ - Wallet is required for GPS marker creation costs.");
        }
        WalletBridge.WalletCallResult result = wallet.withdraw(player.getDbID(), cost,
                "GPS marker creation: " + type, settings.markerCreateCostCurrencyIdentifier, PLUGIN_IDENTIFIER);
        return result.success() ? EconomyResult.ok(costLabel(cost, settings.markerCreateCostCurrencyIdentifier))
                : EconomyResult.fail(result.message());
    }

    public int markerLimit(MarkerType type) {
        return switch (type) {
            case PRIVATE -> settings.maxPrivateMarkers;
            case GROUP -> settings.maxGroupMarkers;
            case GLOBAL, STATIC -> -1;
        };
    }

    public boolean markerLimitReached(Player player, MarkerType type) {
        if (hasAdminOverride(player)) {
            return false;
        }
        int limit = markerLimit(type);
        if (limit < 0) {
            return false;
        }
        return markerCount(player, type) >= limit;
    }

    public String costLabel(long cost, String currencyIdentifier) {
        if (cost <= 0) {
            return "";
        }
        return cost + " " + displayCurrency(currencyIdentifier);
    }

    public String displayCurrency(String currencyIdentifier) {
        if (currencyIdentifier != null && !currencyIdentifier.isBlank()) {
            return currencyIdentifier;
        }
        String defaultCurrency = wallet.defaultCurrencyIdentifier();
        return defaultCurrency == null ? "" : defaultCurrency;
    }

    public int markerCount(Player player, MarkerType type) {
        return switch (type) {
            case PRIVATE -> GPSDatabase.getInstance().countPrivateMarkers(player.getDbID());
            case GROUP -> GPSDatabase.getInstance().countGroupMarkers(player.getPermissionGroup());
            case GLOBAL, STATIC -> 0;
        };
    }

    private long fixedUseCost(MarkerType type) {
        return switch (type) {
            case STATIC -> settings.useStaticMarkerCost;
            case PRIVATE -> settings.usePrivateMarkerCost;
            case GROUP -> settings.useGroupMarkerCost;
            case GLOBAL -> settings.useGlobalMarkerCost;
        };
    }

    private long distanceCost(Player player, Vector3f targetPosition) {
        if (player == null || targetPosition == null || settings.travelDistanceCostPerBlock <= 0) {
            return 0L;
        }
        Vector3i source = player.getChunkPosition();
        if (source == null) {
            source = ChunkUtils.getChunkPosition(player.getPosition());
        }
        Vector3i target = ChunkUtils.getChunkPosition(targetPosition);
        long distance = Math.abs(source.x - target.x)
                + Math.abs(source.y - target.y)
                + Math.abs(source.z - target.z);
        return distance * settings.travelDistanceCostPerBlock;
    }

    public record EconomyResult(boolean success, String message) {
        public static EconomyResult ok(String message) {
            return new EconomyResult(true, message);
        }

        public static EconomyResult fail(String message) {
            return new EconomyResult(false, message == null || message.isBlank() ? "GPS economy action failed." : message);
        }
    }
}
