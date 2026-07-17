package de.omegazirkel.risingworld.gps;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.Plugin;
import net.risingworld.api.objects.Player;

public class ShopBridge {
    private final Plugin owner;
    private final WalletBridge wallet;

    public ShopBridge(Plugin owner, WalletBridge wallet) {
        this.owner = owner;
        this.wallet = wallet;
    }

    public boolean isAvailable() {
        try {
            return owner.getPluginByName("OZ - Shop") != null
                    && Class.forName("de.omegazirkel.risingworld.Shop") != null;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public void registerTokenOffers(PluginSettings settings) {
        if (!settings.enableTeleportTokens || !settings.enableTeleportTokenShopOffers || !isAvailable()) {
            return;
        }
        registerTokenOffer("ozgps.tokens.1", 1, settings.teleportTokenPackage1Price,
                settings);
        registerTokenOffer("ozgps.tokens.10", 10, settings.teleportTokenPackage10Price,
                settings);
        registerTokenOffer("ozgps.tokens.50", 50, settings.teleportTokenPackage50Price,
                settings);
    }

    private void registerTokenOffer(String id, int amount, long price, PluginSettings settings) {
        Plugin shopPlugin = owner.getPluginByName("OZ - Shop");
        if (shopPlugin == null || price < 0) {
            return;
        }
        try {
            Class<?> callbackType = Class.forName("de.omegazirkel.risingworld.shop.ShopPurchaseCallback");
            Class<?> localizationType = Class.forName("de.omegazirkel.risingworld.shop.ShopOfferLocalization");
            Object callback = Proxy.newProxyInstance(callbackType.getClassLoader(), new Class<?>[] { callbackType },
                    tokenCallback(amount, settings));
            Object localization = Proxy.newProxyInstance(localizationType.getClassLoader(),
                    new Class<?>[] { localizationType }, tokenLocalization(amount));
            Method registerOffer = shopPlugin.getClass().getMethod("registerOffer",
                    String.class, String.class, String.class, long.class, String.class, String.class, String.class,
                    callbackType, localizationType);
            registerOffer.invoke(shopPlugin, id, "GPS Teleport Token x" + amount, "GPS teleport token package", price,
                    settings.teleportTokenShopCurrencyIdentifier, settings.teleportTokenIcon, "OZ - GPS", callback,
                    localization);
        } catch (ReflectiveOperationException ex) {
            GPS.logger().warn("Could not register GPS token shop offer " + id + ": " + ex.getMessage());
        }
    }

    private InvocationHandler tokenLocalization(int amount) {
        I18n translations = I18n.getInstance(owner);
        return (proxy, method, args) -> {
            Player player = args != null && args.length > 0 && args[0] instanceof Player p ? p : null;
            return switch (method.getName()) {
                case "title" -> translations.get("GPS_SHOP_TOKEN_TITLE", player)
                        .replace("PH_AMOUNT", String.valueOf(amount));
                case "description" -> translations.get("GPS_SHOP_TOKEN_DESC", player);
                case "toString" -> "GpsTokenShopLocalization";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> false;
                default -> null;
            };
        };
    }

    private InvocationHandler tokenCallback(int amount, PluginSettings settings) {
        return (Object proxy, Method method, Object[] args) -> {
            Player player = args != null && args.length > 0 && args[0] instanceof Player ? (Player) args[0] : null;
            if (player == null) {
                return shopResult(false, "GPS token purchase has no player.", null);
            }
            WalletBridge.WalletCallResult deposit = wallet.deposit(player.getDbID(), amount,
                    "GPS teleport token package", settings.teleportTokenCurrencyIdentifier, "OZ - GPS");
            return shopResult(deposit.success(), deposit.success()
                    ? "Added " + amount + " GPS teleport tokens."
                    : deposit.message(), args != null && args.length > 1 ? args[1] : null);
        };
    }

    private Object shopResult(boolean success, String message, Object offer) {
        try {
            Class<?> resultType = Class.forName("de.omegazirkel.risingworld.shop.ShopPurchaseResult");
            if (success) {
                Class<?> offerType = Class.forName("de.omegazirkel.risingworld.shop.ShopOffer");
                return resultType.getMethod("success", String.class, offerType).invoke(null, message, offer);
            }
            Class<?> errorType = Class.forName("de.omegazirkel.risingworld.shop.ShopErrorCode");
            Object errorCode = Enum.valueOf(errorType.asSubclass(Enum.class), "CALLBACK_FAILED");
            return resultType.getMethod("failure", errorType, String.class).invoke(null, errorCode, message);
        } catch (ReflectiveOperationException ex) {
            GPS.logger().warn("Could not create ShopPurchaseResult: " + ex.getMessage());
            return null;
        }
    }
}
