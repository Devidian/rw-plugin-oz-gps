package de.omegazirkel.risingworld.gps;

import java.lang.reflect.Method;

import de.omegazirkel.risingworld.GPS;
import net.risingworld.api.Plugin;

public class DiscordConnect {

    private static Plugin pluginRef = null;
    private static final PluginSettings s = PluginSettings.getInstance();

    public static final String botLang() {
        return (String) callPluginMethod("getBotLanguage", null, null);
    }

    public static void init(Plugin plugin) {
        pluginRef = plugin.getPluginByName("OZ - Discord Connect");
        if (pluginRef != null)
            GPS.logger().info("✅ " + pluginRef.getName() + " found! ID: " + pluginRef.getID());
        else
            GPS.logger().warn("⚠️ OZ - Discord Connect not available!");
    }

    private static boolean isPluginAvailable() {
        try {
            Class.forName("de.omegazirkel.risingworld.DiscordConnect");
            return pluginRef != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Object callPluginMethod(String methodName, Class<?>[] paramTypes, Object[] args) {
        if (!isPluginAvailable()) {
            return null;
        }

        try {
            Object plugin = pluginRef;
            Class<?> clazz = plugin.getClass();
            Method method = clazz.getMethod(methodName, paramTypes);
            return method.invoke(plugin, args);
        } catch (Exception e) {
            GPS.logger().error("Error while calling DiscordConnect Method");
            e.printStackTrace();
            return null;
        }
    }

    public static void sendDiscordMessage(String message, long channelId) {
        sendDiscordMessage(message, channelId, null);
    }

    public static void sendDiscordMessage(String message, long channelId, byte[] image) {
        callPluginMethod("sendDiscordMessageToTextChannel",
                new Class<?>[] { String.class, long.class, byte[].class },
                new Object[] { message, channelId, image });
    }

    public static void sendStaticGPSEventMessage(String message) {
        if (s.enableDiscordStaticGPSEvents && s.discordStaticGPSChannelId > 0)
            sendDiscordMessage(message, s.discordStaticGPSChannelId);
    }

    public static void sendPrivateGPSEventMessage(String message) {
        if (s.enableDiscordPrivateGPSEvents && s.discordPrivateGPSChannelId > 0)
            sendDiscordMessage(message, s.discordPrivateGPSChannelId);
    }

    public static void sendGroupGPSEventMessage(String message) {
        if (s.enableDiscordGroupGPSEvents && s.discordGroupGPSChannelId > 0)
            sendDiscordMessage(message, s.discordGroupGPSChannelId);
    }

    public static void sendGlobalGPSEventMessage(String message) {
        if (s.enableDiscordGlobalGPSEvents && s.discordGlobalGPSChannelId > 0)
            sendDiscordMessage(message, s.discordGlobalGPSChannelId);
    }

}
