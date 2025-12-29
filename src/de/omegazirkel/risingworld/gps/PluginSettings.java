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

public class PluginSettings {
	private static PluginSettings instance = null;

	private static GPS plugin;

	private static OZLogger logger() {
		return OZLogger.getInstance("OZ.GPS.Settings");
	}

	// Settings
	public String logLevel = "ALL";
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
	public Integer usePrivateMarkerCooldownSeconds = 10;
	public Integer useGroupMarkerCooldownSeconds = 10;
	public Integer useGlobalMarkerCooldownSeconds = 10;

	public Integer createPrivateMarkerCost = 100;
	public Integer createGroupMarkerCost = 100;

	public Integer useStaticMarkerCost = 10;
	public Integer usePrivateMarkerCost = 10;
	public Integer useGroupMarkerCost = 10;
	public Integer useGlobalMarkerCost = 10;

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
					.parseInt(settings.getProperty("usePrivateMarkerCooldownSeconds", "10"));
			useGroupMarkerCooldownSeconds = Integer
					.parseInt(settings.getProperty("useGroupMarkerCooldownSeconds", "10"));
			useGlobalMarkerCooldownSeconds = Integer
					.parseInt(settings.getProperty("useGlobalMarkerCooldownSeconds", "10"));

			createPrivateMarkerCost = Integer.parseInt(settings.getProperty("createPrivateMarkerCost", "100"));
			createGroupMarkerCost = Integer.parseInt(settings.getProperty("createGroupMarkerCost", "100"));

			useStaticMarkerCost = Integer.parseInt(settings.getProperty("useStaticMarkerCost", "10"));
			usePrivateMarkerCost = Integer.parseInt(settings.getProperty("usePrivateMarkerCost", "10"));
			useGroupMarkerCost = Integer.parseInt(settings.getProperty("useGroupMarkerCost", "10"));
			useGlobalMarkerCost = Integer.parseInt(settings.getProperty("useGlobalMarkerCost", "10"));

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
}
