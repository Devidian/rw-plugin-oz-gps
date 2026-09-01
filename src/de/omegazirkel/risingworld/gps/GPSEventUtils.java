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
		String restrictionDenialKey = GPSAreaAccessPolicy.teleportDenialKey(uiPlayer, pos, type);
		if (restrictionDenialKey != null) {
			uiPlayer.sendTextMessage(t().get(restrictionDenialKey, uiPlayer));
			return false;
		}
        if (!GPSAccessPolicy.canUse(uiPlayer, type)) {
            uiPlayer.sendTextMessage(t().get("tc.gps.playtime.required", uiPlayer)
                    .replace("PH_REQUIRED_MINUTES", String.valueOf(GPSAccessPolicy.requiredMinutes()))
                    .replace("PH_REMAINING_MINUTES", String.valueOf(GPSAccessPolicy.remainingMinutes(uiPlayer))));
            return false;
        }
        boolean canLeaveArea = (boolean) uiPlayer.getPermissionValue("area_canleave", true);

        if (!canLeaveArea) {
            uiPlayer.sendTextMessage(t().get("tc.gps.cant.leave", uiPlayer));
            return false;
        }
        int cooldownRemaining = TeleportCooldowns.remainingSeconds(uiPlayer, type);
        if (cooldownRemaining > 0) {
            uiPlayer.sendTextMessage(t().get("tc.gps.cooldown.active", uiPlayer)
                    .replace("PH_SECONDS", String.valueOf(cooldownRemaining))
                    .replace("PH_MARKER_TYPE", t().get(TeleportCooldowns.displayTypeKey(type), uiPlayer)));
            return false;
        }
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy != null) {
            GPSEconomy.EconomyResult charge = economy.chargeTeleport(uiPlayer, pos, type, label);
            if (!charge.success()) {
                uiPlayer.sendTextMessage(t().get("tc.gps.economy.failed", uiPlayer)
                        .replace("PH_MESSAGE", charge.message()));
                return false;
            }
            if (!charge.message().isBlank()) {
                uiPlayer.sendTextMessage(t().get("tc.gps.cost.charged", uiPlayer)
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
        p.sendTextMessage(t().get("tc.gps.static", p).replace("PH_GPS_NAME", gpsName));
        String staticMsgKey = "tc.discord.gps.static.event";
        if (!s.discordGPSIncludeMarkerName && !s.discordGPSIncludeMarkerPosition) {
            staticMsgKey = "tc.discord.gps.static.event.no.details";
        } else if (!s.discordGPSIncludeMarkerName) {
            staticMsgKey = "tc.discord.gps.static.event.pos.only";
        } else if (!s.discordGPSIncludeMarkerPosition) {
            staticMsgKey = "tc.discord.gps.static.event.name.only";
        }

        DiscordConnect.sendStaticGPSEventMessage(t().get(staticMsgKey, DiscordConnect.botLang())
                .replace("PH_PLAYER_NAME", p.getName())
                .replace("PH_GPS_NAME", gpsName)
                .replace("PH_GPS_POS", targetPos.toString()));
    }

    public static void onPrivateGPSEvent(Player p, String gpsName, Vector3f targetPos) {
        p.sendTextMessage(
                t().get("tc.gps.private", p).replace("PH_GPS_NAME", gpsName));
        String msgKey = "tc.discord.gps.private.event";
        if (!s.discordGPSIncludeMarkerName && !s.discordGPSIncludeMarkerPosition) {
            msgKey = "tc.discord.gps.private.event.no.details";
        } else if (!s.discordGPSIncludeMarkerName) {
            msgKey = "tc.discord.gps.private.event.pos.only";
        } else if (!s.discordGPSIncludeMarkerPosition) {
            msgKey = "tc.discord.gps.private.event.name.only";
        }

        DiscordConnect.sendPrivateGPSEventMessage(
                t().get(msgKey, DiscordConnect.botLang())
                        .replace("PH_PLAYER_NAME", p.getName())
                        .replace("PH_GPS_NAME", gpsName)
                        .replace("PH_GPS_POS", targetPos.toString()));
    }

    public static void onGroupGPSEvent(Player p, String gpsName, Vector3f targetPos) {
        p.sendTextMessage(t().get("tc.gps.group", p).replace("PH_GPS_NAME", gpsName));
        String groupMsgKey = "tc.discord.gps.group.event";
        if (!s.discordGPSIncludeMarkerName && !s.discordGPSIncludeMarkerPosition) {
            groupMsgKey = "tc.discord.gps.group.event.no.details";
        } else if (!s.discordGPSIncludeMarkerName) {
            groupMsgKey = "tc.discord.gps.group.event.pos.only";
        } else if (!s.discordGPSIncludeMarkerPosition) {
            groupMsgKey = "tc.discord.gps.group.event.name.only";
        }
        DiscordConnect.sendGroupGPSEventMessage(t().get(groupMsgKey, DiscordConnect.botLang())
                .replace("PH_PLAYER_NAME", p.getName())
                .replace("PH_GPS_NAME", gpsName)
                .replace("PH_GPS_POS", targetPos.toString()));
    }

    public static void onGlobalGPSEvent(Player p, String gpsName, Vector3f targetPos) {
        p.sendTextMessage(t().get("tc.gps.global", p).replace("PH_GPS_NAME", gpsName));
        String globalMsgKey = "tc.discord.gps.global.event";
        if (!s.discordGPSIncludeMarkerName && !s.discordGPSIncludeMarkerPosition) {
            globalMsgKey = "tc.discord.gps.global.event.no.details";
        } else if (!s.discordGPSIncludeMarkerName) {
            globalMsgKey = "tc.discord.gps.global.event.pos.only";
        } else if (!s.discordGPSIncludeMarkerPosition) {
            globalMsgKey = "tc.discord.gps.global.event.name.only";
        }
        DiscordConnect.sendGlobalGPSEventMessage(t().get(globalMsgKey, DiscordConnect.botLang())
                .replace("PH_PLAYER_NAME", p.getName())
                .replace("PH_GPS_NAME", gpsName)
                .replace("PH_GPS_POS", targetPos.toString()));
    }
}
