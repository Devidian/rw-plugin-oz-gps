package de.omegazirkel.risingworld.gps;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.I18n;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Vector3f;

public class GPSEventUtils {
    private static PluginSettings s = PluginSettings.getInstance();

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    public static void onStaticGPSEvent(Player p, String gpsName, Vector3f targetPos) {
        p.sendTextMessage(t().get("TC_GPS_STATIC", p).replace("PH_GPS_NAME", gpsName));
        String staticMsgKey = "TC_DISCORD_GPS_STATIC_EVENT";
        if (!s.discordGPSIncludeMarkerName && !s.discordGPSIncludeMarkerPosition) {
            staticMsgKey = "TC_DISCORD_GPS_STATIC_EVENT_NO_DETAILS";
        } else if (!s.discordGPSIncludeMarkerName) {
            staticMsgKey = "TC_DISCORD_GPS_STATIC_EVENT_POS_ONLY";
        } else if (!s.discordGPSIncludeMarkerPosition) {
            staticMsgKey = "TC_DISCORD_GPS_STATIC_EVENT_NAME_ONLY";
        }

        DiscordConnect.sendStaticGPSEventMessage(t().get(staticMsgKey, p)
                .replace("PH_PLAYER_NAME", p.getName())
                .replace("PH_GPS_NAME", gpsName)
                .replace("PH_GPS_POS", targetPos.toString()));
    }

    public static void onPrivateGPSEvent(Player p, String gpsName, Vector3f targetPos) {
        p.sendTextMessage(
                t().get("TC_GPS_PRIVATE", p).replace("PH_GPS_NAME", gpsName));
        String msgKey = "TC_DISCORD_GPS_PRIVATE_EVENT";
        if (!s.discordGPSIncludeMarkerName && !s.discordGPSIncludeMarkerPosition) {
            msgKey = "TC_DISCORD_GPS_PRIVATE_EVENT_NO_DETAILS";
        } else if (!s.discordGPSIncludeMarkerName) {
            msgKey = "TC_DISCORD_GPS_PRIVATE_EVENT_POS_ONLY";
        } else if (!s.discordGPSIncludeMarkerPosition) {
            msgKey = "TC_DISCORD_GPS_PRIVATE_EVENT_NAME_ONLY";
        }

        DiscordConnect.sendPrivateGPSEventMessage(
                t().get(msgKey, p)
                        .replace("PH_PLAYER_NAME", p.getName())
                        .replace("PH_GPS_NAME", gpsName)
                        .replace("PH_GPS_POS", targetPos.toString()));
    }

    public static void onGroupGPSEvent(Player p, String gpsName, Vector3f targetPos) {
        p.sendTextMessage(t().get("TC_GPS_GROUP", p).replace("PH_GPS_NAME", gpsName));
        String groupMsgKey = "TC_DISCORD_GPS_GROUP_EVENT";
        if (!s.discordGPSIncludeMarkerName && !s.discordGPSIncludeMarkerPosition) {
            groupMsgKey = "TC_DISCORD_GPS_GROUP_EVENT_NO_DETAILS";
        } else if (!s.discordGPSIncludeMarkerName) {
            groupMsgKey = "TC_DISCORD_GPS_GROUP_EVENT_POS_ONLY";
        } else if (!s.discordGPSIncludeMarkerPosition) {
            groupMsgKey = "TC_DISCORD_GPS_GROUP_EVENT_NAME_ONLY";
        }
        DiscordConnect.sendGroupGPSEventMessage(t().get(groupMsgKey, p)
                .replace("PH_PLAYER_NAME", p.getName())
                .replace("PH_GPS_NAME", gpsName)
                .replace("PH_GPS_POS", targetPos.toString()));
    }

    public static void onGlobalGPSEvent(Player p, String gpsName, Vector3f targetPos) {
        p.sendTextMessage(t().get("TC_GPS_GLOBAL", p).replace("PH_GPS_NAME", gpsName));
        String globalMsgKey = "TC_DISCORD_GPS_GLOBAL_EVENT";
        if (!s.discordGPSIncludeMarkerName && !s.discordGPSIncludeMarkerPosition) {
            globalMsgKey = "TC_DISCORD_GPS_GLOBAL_EVENT_NO_DETAILS";
        } else if (!s.discordGPSIncludeMarkerName) {
            globalMsgKey = "TC_DISCORD_GPS_GLOBAL_EVENT_POS_ONLY";
        } else if (!s.discordGPSIncludeMarkerPosition) {
            globalMsgKey = "TC_DISCORD_GPS_GLOBAL_EVENT_NAME_ONLY";
        }
        DiscordConnect.sendGlobalGPSEventMessage(t().get(globalMsgKey, p)
                .replace("PH_PLAYER_NAME", p.getName())
                .replace("PH_GPS_NAME", gpsName)
                .replace("PH_GPS_POS", targetPos.toString()));
    }
}
