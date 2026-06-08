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

    public static boolean executeTeleport(Player uiPlayer, Vector3f pos, String label, MarkerType type) {
        return executeTeleport(uiPlayer, pos, label, type, true);
    }

    public static boolean executeTeleport(Player uiPlayer, Vector3f pos, String label, MarkerType type,
            boolean saveLastPosition) {
        if (!GPSAccessPolicy.canUse(uiPlayer, type)) {
            uiPlayer.sendTextMessage(t().get("TC_GPS_PLAYTIME_REQUIRED", uiPlayer)
                    .replace("PH_REQUIRED_MINUTES", String.valueOf(GPSAccessPolicy.requiredMinutes()))
                    .replace("PH_REMAINING_MINUTES", String.valueOf(GPSAccessPolicy.remainingMinutes(uiPlayer))));
            return false;
        }
        boolean canLeaveArea = (boolean) uiPlayer.getPermissionValue("area_canleave", true);

        if (!canLeaveArea) {
            uiPlayer.sendTextMessage(t().get("TC_GPS_CANT_LEAVE", uiPlayer));
            return false;
        }
        int cooldownRemaining = TeleportCooldowns.remainingSeconds(uiPlayer, type);
        if (cooldownRemaining > 0) {
            uiPlayer.sendTextMessage(t().get("TC_GPS_COOLDOWN_ACTIVE", uiPlayer)
                    .replace("PH_SECONDS", String.valueOf(cooldownRemaining))
                    .replace("PH_MARKER_TYPE", t().get(TeleportCooldowns.displayTypeKey(type), uiPlayer)));
            return false;
        }
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy != null) {
            GPSEconomy.EconomyResult charge = economy.chargeTeleport(uiPlayer, pos, type, label);
            if (!charge.success()) {
                uiPlayer.sendTextMessage(t().get("TC_GPS_ECONOMY_FAILED", uiPlayer)
                        .replace("PH_MESSAGE", charge.message()));
                return false;
            }
            if (!charge.message().isBlank()) {
                uiPlayer.sendTextMessage(t().get("TC_GPS_COST_CHARGED", uiPlayer)
                        .replace("PH_COST", charge.message()));
            }
        }
        if (saveLastPosition)
            uiPlayer.setAttribute("pre-port-location", uiPlayer.getPosition());
        uiPlayer.setPosition(pos);
        TeleportCooldowns.recordUse(uiPlayer, type);

        switch (type) {
            case GLOBAL:
                GPSEventUtils.onGlobalGPSEvent(uiPlayer, label, pos);
                break;
            case GROUP:
                GPSEventUtils.onGroupGPSEvent(uiPlayer, label, pos);
                break;
            case PRIVATE:
                GPSEventUtils.onPrivateGPSEvent(uiPlayer, label, pos);
                break;
            case STATIC:
                GPSEventUtils.onStaticGPSEvent(uiPlayer, label, pos);
                break;
            default:
                break;
        }
        return true;
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
