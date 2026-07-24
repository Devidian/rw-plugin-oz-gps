package de.omegazirkel.risingworld.gps;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.bridge.DiscordBridge;
import net.risingworld.api.Plugin;

public class DiscordConnect extends DiscordBridge {

    private static DiscordConnect bridge;
    private static final PluginSettings s = PluginSettings.getInstance();

    private DiscordConnect(Plugin owner) {
        super(owner);
    }

    public static final String botLang() {
        return bridge == null ? "en" : bridge.getBotLanguage();
    }

    public static void init(Plugin plugin) {
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

    public static void sendStaticGPSEventMessage(String message) {
        if (s.discordStaticGPSChannelId > 0)
            sendDiscordMessage(message, s.discordStaticGPSChannelId);
    }

    public static void sendPrivateGPSEventMessage(String message) {
        if (s.discordPrivateGPSChannelId > 0)
            sendDiscordMessage(message, s.discordPrivateGPSChannelId);
    }

    public static void sendGroupGPSEventMessage(String message) {
        if (s.discordGroupGPSChannelId > 0)
            sendDiscordMessage(message, s.discordGroupGPSChannelId);
    }

    public static void sendGlobalGPSEventMessage(String message) {
        if (s.discordGlobalGPSChannelId > 0)
            sendDiscordMessage(message, s.discordGlobalGPSChannelId);
    }

}
