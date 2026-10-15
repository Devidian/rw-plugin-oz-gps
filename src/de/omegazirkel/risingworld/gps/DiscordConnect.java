package de.omegazirkel.risingworld.gps;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.bridge.DiscordBridge;
import net.risingworld.api.Plugin;

public class DiscordConnect extends DiscordBridge {

    private static DiscordConnect bridge;
    private static final PluginSettings s = PluginSettings.getInstance();
    private static String displayName = "OZ - GPS";

    private DiscordConnect(Plugin owner) {
        super(owner);
    }

    public static final String botLang() {
        return bridge == null ? "en" : bridge.getBotLanguage();
    }

    public static void init(Plugin plugin) {
        displayName = plugin.getDescription("name");
        bridge = new DiscordConnect(plugin);
        if (bridge.isAvailable())
            GPS.logger().info("✅ OZ - Discord Connect found!");
        else
            GPS.logger().warn("⚠️ OZ - Discord Connect not available!");
    }

    public static void sendDiscordMessage(String message, long channelId) {
        sendDiscordMessage(message, channelId, null);
    }

    public static void sendDiscordMessage(String message, long channelId, byte[] image) {
        if (bridge != null) bridge.sendTextMessage(message, channelId, image);
    }

    private static void sendEvent(String message, long channelId, String override) {
        if (bridge != null) bridge.sendTextMessage(message, channelId, null,
                override == null || override.isBlank() ? displayName : override.trim());
    }

    public static void sendStaticGPSEventMessage(String message) {
        if (s.discordStaticGPSChannelId > 0)
            sendEvent(message, s.discordStaticGPSChannelId, s.discordStaticGPSThreadTopic);
    }

    public static void sendPrivateGPSEventMessage(String message) {
        if (s.discordPrivateGPSChannelId > 0)
            sendEvent(message, s.discordPrivateGPSChannelId, s.discordPrivateGPSThreadTopic);
    }

    public static void sendGroupGPSEventMessage(String message) {
        if (s.discordGroupGPSChannelId > 0)
            sendEvent(message, s.discordGroupGPSChannelId, s.discordGroupGPSThreadTopic);
    }

    public static void sendGlobalGPSEventMessage(String message) {
        if (s.discordGlobalGPSChannelId > 0)
            sendEvent(message, s.discordGlobalGPSChannelId, s.discordGlobalGPSThreadTopic);
    }

}
