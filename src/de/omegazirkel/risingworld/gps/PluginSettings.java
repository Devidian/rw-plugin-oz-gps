package de.omegazirkel.risingworld.gps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	public boolean enablePrivateMarkers = false;
	public boolean enableGroupMarkers = false;
	public boolean enableGlobalMarkers = false;
	public boolean enableStaticMarkers = false;

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
	public String teleportTokenIcon = "icon-ki-gps-coin";
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
			// markers
			enablePrivateMarkers = settings.getProperty("enablePrivateMarkers", "false").contentEquals("true");
			enableGroupMarkers = settings.getProperty("enableGroupMarkers", "false").contentEquals("true");
			enableGlobalMarkers = settings.getProperty("enableGlobalMarkers", "false").contentEquals("true");
			enableStaticMarkers = settings.getProperty("enableStaticMarkers", "false").contentEquals("true");

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
			teleportTokenIcon = settings.getProperty("teleportTokenIcon", "icon-ki-gps-coin");
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
			discordGPSIncludeMarkerPosition = settings.getProperty("discordGPSIncludeMarkerPosition", "false")
					.contentEquals("true");
			discordGPSIncludeMarkerName = settings.getProperty("discordGPSIncludeMarkerName", "false")
					.contentEquals("true");

			logger().info(plugin.getName() + " Plugin settings loaded");
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
				entry("logLevel", "Log level", "Controls GPS logging verbosity.", logLevel, "ALL",
						AdminSettingsType.STRING),
				entry("reloadOnChange", "Reload on change",
						"Documents that GPS settings reload when settings.properties changes.", reloadOnChange, "true",
						AdminSettingsType.BOOLEAN),
				entry("sendPluginWelcome", "Welcome message", "Shows a short GPS message when a player joins.",
						enableWelcomeMessage, "false", AdminSettingsType.BOOLEAN),
				entry("enablePrivateMarkers", "Private markers", "Enables private marker workflows.",
						enablePrivateMarkers, "true", AdminSettingsType.BOOLEAN),
				entry("enableGroupMarkers", "Group markers", "Enables group marker workflows.", enableGroupMarkers,
						"true", AdminSettingsType.BOOLEAN),
				entry("enableGlobalMarkers", "Global markers", "Enables global marker workflows.", enableGlobalMarkers,
						"true", AdminSettingsType.BOOLEAN),
				entry("enableStaticMarkers", "Static markers", "Enables static marker workflows.", enableStaticMarkers,
						"true", AdminSettingsType.BOOLEAN),
				entry("maxPrivateMarkers", "Max private markers", "Maximum private markers per player; -1 is unlimited.",
						maxPrivateMarkers, "-1", AdminSettingsType.INTEGER),
				entry("maxGroupMarkers", "Max group markers", "Maximum group markers per player; -1 is unlimited.",
						maxGroupMarkers, "-1", AdminSettingsType.INTEGER),
				entry("travelCostMode", "Travel cost mode", "disabled, fixed, or distance.", travelCostMode,
						"disabled", AdminSettingsType.STRING),
				entry("travelCostCurrencyIdentifier", "Travel cost currency",
						"Wallet currency identifier for fixed and distance travel costs; empty uses Wallet default.",
						travelCostCurrencyIdentifier, "", AdminSettingsType.STRING),
				entry("travelDistanceCostPerBlock", "Distance cost per block",
						"Whole-number cost per Manhattan-distance block when travelCostMode=distance.",
						travelDistanceCostPerBlock, "1", AdminSettingsType.INTEGER),
				entry("useStaticMarkerCost", "Static travel cost", "Wallet cost for using static markers.",
						useStaticMarkerCost, "10", AdminSettingsType.INTEGER),
				entry("usePrivateMarkerCost", "Private travel cost", "Wallet cost for using private markers.",
						usePrivateMarkerCost, "10", AdminSettingsType.INTEGER),
				entry("useGroupMarkerCost", "Group travel cost", "Wallet cost for using group markers.",
						useGroupMarkerCost, "10", AdminSettingsType.INTEGER),
				entry("useGlobalMarkerCost", "Global travel cost", "Wallet cost for using global markers.",
						useGlobalMarkerCost, "10", AdminSettingsType.INTEGER),
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
				entry("enableTeleportTokens", "Teleport tokens", "Registers GPS teleport tokens in Wallet.",
						enableTeleportTokens, "false", AdminSettingsType.BOOLEAN),
				entry("enableTeleportTokenShopOffers", "Token shop offers",
						"Registers GPS teleport-token packages in OZ Shop when Shop is available.",
						enableTeleportTokenShopOffers, "false", AdminSettingsType.BOOLEAN),
				entry("allowAdminOverride", "Allow admin override",
						"Allows admins with their personal GPS override enabled to bypass costs and limits.",
						allowAdminOverride, "false", AdminSettingsType.BOOLEAN));
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

	private Path settingsPath() {
		return Paths.get((plugin.getPath() != null ? plugin.getPath() : ".") + "/settings.properties");
	}
}
