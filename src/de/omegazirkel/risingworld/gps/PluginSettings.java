package de.omegazirkel.risingworld.gps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsEntry;
import de.omegazirkel.risingworld.tools.settings.AdminSettingsType;
import de.omegazirkel.risingworld.tools.settings.SettingsFileEditor;

public class PluginSettings {
	private static PluginSettings instance = null;

	private static GPS plugin;

	private static OZLogger logger() {
		return GPS.logger();
	}

	// Settings
	public String logLevel = "ALL";
	public boolean reloadOnChange = true;
	public boolean enableWelcomeMessage = false;
	public Integer minimumPlaytimeMinutes = 15;

	public boolean enablePrivateMarkers = false;
	public boolean enableGroupMarkers = false;
	public boolean enableGlobalMarkers = false;
	public boolean enableStaticMarkers = false;
	public boolean exposeGlobalMarkers = true;

	public boolean enableBackport = true;
	public boolean enableDeathport = true;
	public boolean enableDefaultSpawn = true;
	public boolean enablePrimarySpawn = true;
	public boolean enableSecondarySpawn = true;
	public boolean enableTertiarySpawn = true;
	public boolean enableQuaternarySpawn = true;

	// Discord Connect settings
	public boolean enableDiscordStaticGPSEvents = false;
	public boolean enableDiscordPrivateGPSEvents = false;
	public boolean enableDiscordGroupGPSEvents = false;
	public boolean enableDiscordGlobalGPSEvents = false;
	public long discordStaticGPSChannelId = 0;
	public long discordPrivateGPSChannelId = 0;
	public long discordGroupGPSChannelId = 0;
	public long discordGlobalGPSChannelId = 0;
	public boolean discordGPSIncludeMarkerPosition = false;
	public boolean discordGPSIncludeMarkerName = false;

	// Not yet implemented
	public Integer maxPrivateMarkers = -1;
	public Integer maxGroupMarkers = -1;

	public Integer useStaticMarkerCooldownSeconds = 10;
	public Integer usePrivateMarkerCooldownSeconds = 60;
	public Integer useGroupMarkerCooldownSeconds = 30;
	public Integer useGlobalMarkerCooldownSeconds = 300;

	public Integer createPrivateMarkerCost = 25;
	public Integer createGroupMarkerCost = 75;
	public boolean enableMarkerCreateCosts = false;
	public String markerCreateCostCurrencyIdentifier = "";

	public Integer useStaticMarkerCost = 10;
	public Integer usePrivateMarkerCost = 10;
	public Integer useGroupMarkerCost = 10;
	public Integer useGlobalMarkerCost = 10;
	public String travelCostMode = "disabled";
	public String travelCostCurrencyIdentifier = "";
	public Integer travelDistanceCostPerBlock = 1;

	public boolean enableTeleportTokens = false;
	public String teleportTokenCurrencyIdentifier = "GPSTP";
	public String teleportTokenCurrencyName = "GPS Teleport Token";
	public String teleportTokenIcon = "coin-gps-token";
	public boolean enableTeleportTokenShopOffers = false;
	public String teleportTokenShopCurrencyIdentifier = "";
	public Integer teleportTokenPackage1Price = 25;
	public Integer teleportTokenPackage10Price = 200;
	public Integer teleportTokenPackage50Price = 900;

	public boolean allowAdminOverride = false;

	// END Settings

	public static PluginSettings getInstance(GPS p) {
		plugin = p;
		return getInstance();
	}

	public static PluginSettings getInstance() {

		if (instance == null) {
			instance = new PluginSettings();
		}
		return instance;
	}

	private PluginSettings() {
	}

	public void initSettings() {
		initSettings((plugin.getPath() != null ? plugin.getPath() : ".") + "/settings.properties");
	}

	public void initSettings(String filePath) {
		Path settingsFile = Paths.get(filePath);
		Path defaultSettingsFile = settingsFile.resolveSibling("settings.default.properties");

		try {
			if (Files.notExists(settingsFile) && Files.exists(defaultSettingsFile)) {
				logger().info("settings.properties not found, copying from settings.default.properties...");
				Files.copy(defaultSettingsFile, settingsFile);
			}

			Properties settings = new Properties();
			if (Files.exists(settingsFile)) {
				try (FileInputStream in = new FileInputStream(settingsFile.toFile())) {
					settings.load(new InputStreamReader(in, "UTF8"));
				}
			} else {
				logger().warn(
						"⚠️ Neither settings.properties nor settings.default.properties found. Using default values.");
			}
			// fill global values
			logLevel = settings.getProperty("logLevel", "ALL");
			reloadOnChange = settings.getProperty("reloadOnChange", "true").contentEquals("true");

			// motd settings
			enableWelcomeMessage = settings.getProperty("sendPluginWelcome", "false").contentEquals("true");
			minimumPlaytimeMinutes = Integer.parseInt(settings.getProperty("minimumPlaytimeMinutes", "15"));
			// markers
			enablePrivateMarkers = settings.getProperty("enablePrivateMarkers", "false").contentEquals("true");
			enableGroupMarkers = settings.getProperty("enableGroupMarkers", "false").contentEquals("true");
			enableGlobalMarkers = settings.getProperty("enableGlobalMarkers", "false").contentEquals("true");
			enableStaticMarkers = settings.getProperty("enableStaticMarkers", "false").contentEquals("true");
			exposeGlobalMarkers = settings.getProperty("exposeGlobalMarkers", "true").contentEquals("true");

			enableBackport = settings.getProperty("enableBackport", "true").contentEquals("true");
			enableDeathport = settings.getProperty("enableDeathport", "true").contentEquals("true");
			enableDefaultSpawn = settings.getProperty("enableDefaultSpawn", "true").contentEquals("true");
			enablePrimarySpawn = settings.getProperty("enablePrimarySpawn", "true").contentEquals("true");
			enableSecondarySpawn = settings.getProperty("enableSecondarySpawn", "true").contentEquals("true");
			enableTertiarySpawn = settings.getProperty("enableTertiarySpawn", "true").contentEquals("true");
			enableQuaternarySpawn = settings.getProperty("enableQuaternarySpawn", "true").contentEquals("true");

			maxPrivateMarkers = Integer.parseInt(settings.getProperty("maxPrivateMarkers", "-1"));
			maxGroupMarkers = Integer.parseInt(settings.getProperty("maxGroupMarkers", "-1"));

			useStaticMarkerCooldownSeconds = Integer
					.parseInt(settings.getProperty("useStaticMarkerCooldownSeconds", "10"));
			usePrivateMarkerCooldownSeconds = Integer
					.parseInt(settings.getProperty("usePrivateMarkerCooldownSeconds", "60"));
			useGroupMarkerCooldownSeconds = Integer
					.parseInt(settings.getProperty("useGroupMarkerCooldownSeconds", "30"));
			useGlobalMarkerCooldownSeconds = Integer
					.parseInt(settings.getProperty("useGlobalMarkerCooldownSeconds", "300"));

			createPrivateMarkerCost = Integer.parseInt(settings.getProperty("createPrivateMarkerCost", "25"));
			createGroupMarkerCost = Integer.parseInt(settings.getProperty("createGroupMarkerCost", "75"));
			enableMarkerCreateCosts = settings.getProperty("enableMarkerCreateCosts", "false").contentEquals("true");
			markerCreateCostCurrencyIdentifier = settings.getProperty("markerCreateCostCurrencyIdentifier", "");

			useStaticMarkerCost = Integer.parseInt(settings.getProperty("useStaticMarkerCost", "10"));
			usePrivateMarkerCost = Integer.parseInt(settings.getProperty("usePrivateMarkerCost", "10"));
			useGroupMarkerCost = Integer.parseInt(settings.getProperty("useGroupMarkerCost", "10"));
			useGlobalMarkerCost = Integer.parseInt(settings.getProperty("useGlobalMarkerCost", "10"));
			travelCostMode = settings.getProperty("travelCostMode", "disabled").trim().toLowerCase();
			travelCostCurrencyIdentifier = settings.getProperty("travelCostCurrencyIdentifier", "");
			travelDistanceCostPerBlock = Integer.parseInt(settings.getProperty("travelDistanceCostPerBlock", "1"));

			enableTeleportTokens = settings.getProperty("enableTeleportTokens", "false").contentEquals("true");
			teleportTokenCurrencyIdentifier = settings.getProperty("teleportTokenCurrencyIdentifier", "GPSTP");
			teleportTokenCurrencyName = settings.getProperty("teleportTokenCurrencyName", "GPS Teleport Token");
			teleportTokenIcon = settings.getProperty("teleportTokenIcon", "coin-gps-token");
			enableTeleportTokenShopOffers = settings.getProperty("enableTeleportTokenShopOffers", "false")
					.contentEquals("true");
			teleportTokenShopCurrencyIdentifier = settings.getProperty("teleportTokenShopCurrencyIdentifier", "");
			teleportTokenPackage1Price = Integer.parseInt(settings.getProperty("teleportTokenPackage1Price", "25"));
			teleportTokenPackage10Price = Integer.parseInt(settings.getProperty("teleportTokenPackage10Price", "200"));
			teleportTokenPackage50Price = Integer.parseInt(settings.getProperty("teleportTokenPackage50Price", "900"));

			allowAdminOverride = settings.getProperty("allowAdminOverride", "false").contentEquals("true");

			// Discord Connect settings
			enableDiscordStaticGPSEvents = settings.getProperty("enableDiscordStaticGPSEvents", "false")
					.contentEquals("true");
			enableDiscordPrivateGPSEvents = settings.getProperty("enableDiscordPrivateGPSEvents", "false")
					.contentEquals("true");
			enableDiscordGroupGPSEvents = settings.getProperty("enableDiscordGroupGPSEvents", "false")
					.contentEquals("true");
			enableDiscordGlobalGPSEvents = settings.getProperty("enableDiscordGlobalGPSEvents", "false")
					.contentEquals("true");
			discordStaticGPSChannelId = Long.parseLong(settings.getProperty("discordStaticGPSChannelId", "0"));
			discordPrivateGPSChannelId = Long.parseLong(settings.getProperty("discordPrivateGPSChannelId", "0"));
			discordGroupGPSChannelId = Long.parseLong(settings.getProperty("discordGroupGPSChannelId", "0"));
			discordGlobalGPSChannelId = Long.parseLong(settings.getProperty("discordGlobalGPSChannelId", "0"));
			discordGPSIncludeMarkerPosition = settings.getProperty("discordGPSIncludeMarkerPosition", "true")
					.contentEquals("true");
			discordGPSIncludeMarkerName = settings.getProperty("discordGPSIncludeMarkerName", "true")
					.contentEquals("true");

			logger().info((plugin == null ? "OZGPS" : plugin.getName()) + " Plugin settings loaded");
			logger().info("Sending welcome message on login is: " + String.valueOf(enableWelcomeMessage));
			logger().info("Loglevel is set to " + logLevel);
			logger().setLevel(logLevel);

		} catch (IOException ex) {
			logger().error("IOException on initSettings: " + ex.getMessage());
			ex.printStackTrace();
		} catch (NumberFormatException ex) {
			logger().error("NumberFormatException on initSettings: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public java.util.List<AdminSettingsEntry> adminSettingsEntries() {
		return java.util.List.of(
				AdminSettingsEntry.group("general", "General", "Logging, reload, welcome, and admin override behavior."),
				entry("logLevel", "Log level", "Controls GPS logging verbosity.", logLevel, "ALL",
						AdminSettingsType.STRING),
				entry("reloadOnChange", "Reload on change",
						"Documents that GPS settings reload when settings.properties changes.", reloadOnChange, "true",
						AdminSettingsType.BOOLEAN),
				entry("sendPluginWelcome", "Welcome message", "Shows a short GPS message when a player joins.",
						enableWelcomeMessage, "false", AdminSettingsType.BOOLEAN),
				entry("allowAdminOverride", "Allow admin override",
						"Allows admins with their personal GPS override enabled to bypass costs, limits, and minimum playtime.",
						allowAdminOverride, "false", AdminSettingsType.BOOLEAN),
				entry("minimumPlaytimeMinutes", "Minimum playtime",
						"Required total playtime in minutes for non-static GPS features; 0 disables the restriction.",
						minimumPlaytimeMinutes, "15", AdminSettingsType.INTEGER),
				AdminSettingsEntry.group("markers", "Marker categories", "Marker categories exposed through GPS workflows."),
				entry("enablePrivateMarkers", "Private markers", "Enables private marker workflows.",
						enablePrivateMarkers, "true", AdminSettingsType.BOOLEAN),
				entry("enableGroupMarkers", "Group markers", "Enables group marker workflows.", enableGroupMarkers,
						"true", AdminSettingsType.BOOLEAN),
				entry("enableGlobalMarkers", "Global markers", "Enables global marker workflows.", enableGlobalMarkers,
						"true", AdminSettingsType.BOOLEAN),
				entry("enableStaticMarkers", "Static markers", "Enables static marker workflows.", enableStaticMarkers,
						"true", AdminSettingsType.BOOLEAN),
				AdminSettingsEntry.group("exportRoutes", "Export routes",
						"Future native route exposure flags for external manager services."),
				entry("exposeGlobalMarkers", "Expose global markers",
						"Enables the future GPS global-marker export route.", exposeGlobalMarkers, "true",
						AdminSettingsType.BOOLEAN),
				AdminSettingsEntry.group("staticTeleports", "Static teleports", "Built-in static teleport targets."),
				entry("enableBackport", "Backport", "Enables teleporting back to the position before the last GPS teleport.",
						enableBackport, "true", AdminSettingsType.BOOLEAN),
				entry("enableDeathport", "Deathport", "Enables teleporting to the last known death position.",
						enableDeathport, "true", AdminSettingsType.BOOLEAN),
				entry("enableDefaultSpawn", "Default spawn", "Enables teleporting to the default spawn.",
						enableDefaultSpawn, "true", AdminSettingsType.BOOLEAN),
				entry("enablePrimarySpawn", "Primary spawn", "Enables teleporting to the primary spawn.",
						enablePrimarySpawn, "true", AdminSettingsType.BOOLEAN),
				entry("enableSecondarySpawn", "Secondary spawn", "Enables teleporting to the secondary spawn.",
						enableSecondarySpawn, "true", AdminSettingsType.BOOLEAN),
				entry("enableTertiarySpawn", "Tertiary spawn", "Enables teleporting to the tertiary spawn.",
						enableTertiarySpawn, "true", AdminSettingsType.BOOLEAN),
				entry("enableQuaternarySpawn", "Quaternary spawn", "Enables teleporting to the quaternary spawn.",
						enableQuaternarySpawn, "true", AdminSettingsType.BOOLEAN),
				AdminSettingsEntry.group("limits", "Marker limits", "Per-player and per-group marker limits."),
				entry("maxPrivateMarkers", "Max private markers", "Maximum private markers per player; -1 is unlimited.",
						maxPrivateMarkers, "-1", AdminSettingsType.INTEGER),
				entry("maxGroupMarkers", "Max group markers", "Maximum group markers per player; -1 is unlimited.",
						maxGroupMarkers, "-1", AdminSettingsType.INTEGER),
				AdminSettingsEntry.group("cooldowns", "Teleport cooldowns", "Cooldowns in seconds for marker teleport types."),
				entry("useStaticMarkerCooldownSeconds", "Static cooldown", "Cooldown in seconds for static marker teleports.",
						useStaticMarkerCooldownSeconds, "10", AdminSettingsType.INTEGER),
				entry("usePrivateMarkerCooldownSeconds", "Private cooldown",
						"Cooldown in seconds for private marker teleports.", usePrivateMarkerCooldownSeconds, "60",
						AdminSettingsType.INTEGER),
				entry("useGroupMarkerCooldownSeconds", "Group cooldown", "Cooldown in seconds for group marker teleports.",
						useGroupMarkerCooldownSeconds, "30", AdminSettingsType.INTEGER),
				entry("useGlobalMarkerCooldownSeconds", "Global cooldown",
						"Cooldown in seconds for global marker teleports.", useGlobalMarkerCooldownSeconds, "300",
						AdminSettingsType.INTEGER),
				AdminSettingsEntry.group("travelCosts", "Travel costs", "Wallet-backed travel cost settings."),
				selectEntry("travelCostMode", "Travel cost mode", "disabled, fixed, or distance.", travelCostMode,
						"disabled", List.of("disabled", "fixed", "distance")),
				entry("travelCostCurrencyIdentifier", "Travel cost currency",
						"Wallet currency identifier for fixed and distance travel costs; empty uses Wallet default.",
						travelCostCurrencyIdentifier, "", AdminSettingsType.STRING),
				entry("travelDistanceCostPerBlock", "Distance cost base",
						"Base cost for sector-distance pricing when travelCostMode=distance.",
						travelDistanceCostPerBlock, "1", AdminSettingsType.INTEGER),
				entry("useStaticMarkerCost", "Static travel cost", "Wallet cost for using static markers.",
						useStaticMarkerCost, "10", AdminSettingsType.INTEGER),
				entry("usePrivateMarkerCost", "Private travel cost", "Wallet cost for using private markers.",
						usePrivateMarkerCost, "10", AdminSettingsType.INTEGER),
				entry("useGroupMarkerCost", "Group travel cost", "Wallet cost for using group markers.",
						useGroupMarkerCost, "10", AdminSettingsType.INTEGER),
				entry("useGlobalMarkerCost", "Global travel cost", "Wallet cost for using global markers.",
						useGlobalMarkerCost, "10", AdminSettingsType.INTEGER),
				AdminSettingsEntry.group("markerCreateCosts", "Marker creation costs",
						"Wallet-backed marker creation cost settings."),
				entry("enableMarkerCreateCosts", "Marker create costs",
						"Enables Wallet costs for creating private and group markers.",
						enableMarkerCreateCosts, "false", AdminSettingsType.BOOLEAN),
				entry("createPrivateMarkerCost", "Private marker cost", "Wallet cost for creating a private marker.",
						createPrivateMarkerCost, "25", AdminSettingsType.INTEGER),
				entry("createGroupMarkerCost", "Group marker cost", "Wallet cost for creating a group marker.",
						createGroupMarkerCost, "75", AdminSettingsType.INTEGER),
				entry("markerCreateCostCurrencyIdentifier", "Marker cost currency",
						"Wallet currency identifier for marker creation costs; empty uses Wallet default.",
						markerCreateCostCurrencyIdentifier, "", AdminSettingsType.STRING),
				AdminSettingsEntry.group("teleportTokens", "Teleport tokens",
						"GPS teleport-token currency and optional Shop offer settings."),
				entry("enableTeleportTokens", "Teleport tokens", "Registers GPS teleport tokens in Wallet.",
						enableTeleportTokens, "false", AdminSettingsType.BOOLEAN),
				entry("teleportTokenCurrencyIdentifier", "Token currency", "Wallet currency identifier for GPS tokens.",
						teleportTokenCurrencyIdentifier, "GPSTP", AdminSettingsType.STRING),
				entry("teleportTokenCurrencyName", "Token currency name", "Display name for GPS teleport tokens.",
						teleportTokenCurrencyName, "GPS Teleport Token", AdminSettingsType.STRING),
				entry("teleportTokenIcon", "Token icon", "Asset icon used for GPS teleport tokens.",
						teleportTokenIcon, "coin-gps-token", AdminSettingsType.STRING),
				entry("enableTeleportTokenShopOffers", "Token shop offers",
						"Registers GPS teleport-token packages in OZ Shop when Shop is available.",
						enableTeleportTokenShopOffers, "false", AdminSettingsType.BOOLEAN),
				entry("teleportTokenShopCurrencyIdentifier", "Token shop currency",
						"Currency identifier for buying GPS token packages; empty uses Wallet default.",
						teleportTokenShopCurrencyIdentifier, "", AdminSettingsType.STRING),
				entry("teleportTokenPackage1Price", "One-token package price",
						"Price for the one-token GPS Shop package.", teleportTokenPackage1Price, "25",
						AdminSettingsType.INTEGER),
				entry("teleportTokenPackage10Price", "Ten-token package price",
						"Price for the ten-token GPS Shop package.", teleportTokenPackage10Price, "200",
						AdminSettingsType.INTEGER),
				entry("teleportTokenPackage50Price", "Fifty-token package price",
						"Price for the fifty-token GPS Shop package.", teleportTokenPackage50Price, "900",
						AdminSettingsType.INTEGER),
				AdminSettingsEntry.group("discord", "Discord", "Optional Discord event forwarding for GPS events."),
				entry("enableDiscordStaticGPSEvents", "Static GPS events",
						"Forwards static GPS events to Discord.", enableDiscordStaticGPSEvents, "false",
						AdminSettingsType.BOOLEAN),
				entry("enableDiscordPrivateGPSEvents", "Private GPS events",
						"Forwards private marker GPS events to Discord.", enableDiscordPrivateGPSEvents, "false",
						AdminSettingsType.BOOLEAN),
				entry("enableDiscordGroupGPSEvents", "Group GPS events",
						"Forwards group marker GPS events to Discord.", enableDiscordGroupGPSEvents, "false",
						AdminSettingsType.BOOLEAN),
				entry("enableDiscordGlobalGPSEvents", "Global GPS events",
						"Forwards global marker GPS events to Discord.", enableDiscordGlobalGPSEvents, "false",
						AdminSettingsType.BOOLEAN),
				entry("discordStaticGPSChannelId", "Static GPS channel",
						"Discord channel id for static GPS events; 0 uses default behavior.",
						discordStaticGPSChannelId, "0", AdminSettingsType.STRING),
				entry("discordPrivateGPSChannelId", "Private GPS channel",
						"Discord channel id for private GPS events; 0 uses default behavior.",
						discordPrivateGPSChannelId, "0", AdminSettingsType.STRING),
				entry("discordGroupGPSChannelId", "Group GPS channel",
						"Discord channel id for group GPS events; 0 uses default behavior.",
						discordGroupGPSChannelId, "0", AdminSettingsType.STRING),
				entry("discordGlobalGPSChannelId", "Global GPS channel",
						"Discord channel id for global GPS events; 0 uses default behavior.",
						discordGlobalGPSChannelId, "0", AdminSettingsType.STRING),
				entry("discordGPSIncludeMarkerPosition", "Include marker position",
						"Includes marker coordinates in Discord GPS event messages.",
						discordGPSIncludeMarkerPosition, "true", AdminSettingsType.BOOLEAN),
				entry("discordGPSIncludeMarkerName", "Include marker name",
						"Includes marker names in Discord GPS event messages.", discordGPSIncludeMarkerName, "true",
						AdminSettingsType.BOOLEAN));
	}

	private AdminSettingsEntry entry(String key, String label, String description, Object value, String defaultValue,
			AdminSettingsType type) {
		return new AdminSettingsEntry(
				key,
				label,
				description,
				String.valueOf(value),
				defaultValue,
				type,
				false,
				newValue -> SettingsFileEditor.writeValue(settingsPath(), key, newValue));
	}

	private AdminSettingsEntry readOnlyEntry(String key, String label, String description, Object value,
			String defaultValue, AdminSettingsType type) {
		return new AdminSettingsEntry(
				key,
				label,
				description,
				String.valueOf(value),
				defaultValue,
				type,
				false,
				null);
	}

	private AdminSettingsEntry selectEntry(String key, String label, String description, Object value,
			String defaultValue, List<String> options) {
		return new AdminSettingsEntry(
				key,
				label,
				description,
				String.valueOf(value),
				defaultValue,
				AdminSettingsType.SELECT,
				false,
				newValue -> SettingsFileEditor.writeValue(settingsPath(), key, newValue),
				options);
	}

	private Path settingsPath() {
		return Paths.get((plugin.getPath() != null ? plugin.getPath() : ".") + "/settings.properties");
	}
}
