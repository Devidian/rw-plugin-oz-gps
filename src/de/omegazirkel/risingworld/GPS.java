package de.omegazirkel.risingworld;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import de.omegazirkel.risingworld.gps.DiscordConnect;
import de.omegazirkel.risingworld.gps.GPSEconomy;
import de.omegazirkel.risingworld.gps.GPSDatabase;
import de.omegazirkel.risingworld.gps.GPSPluginInfoStatusProvider;
import de.omegazirkel.risingworld.gps.GPSPlayerPreferences;
import de.omegazirkel.risingworld.gps.PluginGUI;
import de.omegazirkel.risingworld.gps.PluginSettings;
import de.omegazirkel.risingworld.gps.ui.GPSPlayerPluginData;
import de.omegazirkel.risingworld.gps.ui.GPSPlayerPluginSettings;
import de.omegazirkel.risingworld.tools.Colors;
import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.PlayerSettings;
import de.omegazirkel.risingworld.tools.db.SQLiteConnectionFactory;
import de.omegazirkel.risingworld.tools.settings.PlayerPluginAdminSettings;
import de.omegazirkel.risingworld.tools.ui.AssetManager;
import de.omegazirkel.risingworld.tools.ui.MenuItem;
import de.omegazirkel.risingworld.tools.ui.PlayerPluginSettingsOverlay;
import de.omegazirkel.risingworld.tools.ui.PluginInfoStatusProviders;
import de.omegazirkel.risingworld.tools.ui.PluginMenuManager;
import de.omegazirkel.risingworld.tools.ui.PluginShortcutVisibility;
import net.risingworld.api.Plugin;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.events.player.PlayerDeathEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.objects.Player;

public class GPS extends Plugin implements Listener, FileChangeListener {
	static final String pluginCMD = "gps";
	static final Colors c = Colors.getInstance();
	private static I18n t = null;
	private static PluginSettings s = null;
	private static PluginGUI gui;
	public static String name;
	public static Connection db;
	public static PlayerSettings ps;

	public static OZLogger logger() {
		return OZLogger.getInstance("OZ.GPS");
	}

	@Override
	public void onEnable() {
		name = this.getDescription("name");
		s = PluginSettings.getInstance(this);
		t = I18n.getInstance(this);
		registerEventListener(this);
		s.initSettings();
		db = SQLiteConnectionFactory.open(this);
		ps = new PlayerSettings(db);
		GPSDatabase.getInstance(db);
		gui = PluginGUI.getInstance(this);
		// Load Plugin Menu into Main Plugin Menu
		PluginMenuManager
				.registerPluginMenu(
						new MenuItem(name, "icon-ki-gps-plugin", "GPS", (Player p) -> {
							GPSPlayerPreferences.load(p);
							gui.openPreferredEntry(p);
						}));
		PluginShortcutVisibility.register(name, GPSPlayerPreferences::shortcutVisible);
		// connect plugins
		DiscordConnect.init(this);
		GPSEconomy.init(this, s);

		// register plugin settings
		PlayerPluginSettingsOverlay.registerPlayerPluginSettings(new GPSPlayerPluginSettings(getDescription("version")));
		PlayerPluginSettingsOverlay.registerPlayerPluginData(new GPSPlayerPluginData(getDescription("version")));
		PlayerPluginSettingsOverlay.registerPlayerPluginAdminSettings(
				new PlayerPluginAdminSettings(name, getDescription("version"), () -> s.adminSettingsEntries(),
						s::initSettings));
		PluginInfoStatusProviders.registerProvider(new GPSPluginInfoStatusProvider(this, getDescription("version")));

		logger().info("✅ " + this.getName() + " Plugin is enabled version:" + this.getDescription("version"));
	}

	@Override
	public void onDisable() {
		if (name != null) {
			PluginShortcutVisibility.unregister(name);
			PluginInfoStatusProviders.unregisterProvider(name);
		}
		if (db != null) {
			try {
				db.close();
			} catch (SQLException ex) {
				logger().error("Failed to close GPS database connection: " + ex.getMessage());
			}
		}
	}

	@Override
	public void onSettingsChanged(Path settingsPath) {
		s.initSettings(settingsPath.toString());
		logger().setLevel(s.logLevel);
		if (GPSEconomy.getInstance() != null) {
			GPSEconomy.getInstance().updateSettings(s);
		}
	}

	@EventMethod
	public void onPlayerCommand(PlayerCommandEvent event) {
		Player player = event.getPlayer();
		String lang = player.getSystemLanguage();
		String commandLine = event.getCommand();

		String[] cmdParts = commandLine.split(" ", 2);
		String command = cmdParts[0];

		if (command.equals("/" + pluginCMD)) {
			// Invalid number of arguments (0)
			if (cmdParts.length < 2) {
				gui.openMainMenu(player);
				return;
			}
			String option = cmdParts[1];
			switch (option) {
				case "info":
				case "status":
					PluginInfoStatusProviders.show(player, name);
					break;
				case "help":
					String helpMessage = t.get("TC_CMD_HELP", player).replaceAll("PH_PLUGIN_CMD", pluginCMD);
					player.sendTextMessage(c.okay + this.getName() + ":> " + c.endTag + helpMessage);
					break;
				case "open":
					gui.openMainMenu(player);
					break;
				case "opengrid":
					gui.openGridView(player);
					break;
				case "sortasc":
					GPSPlayerPreferences.setMarkerSortOrder(player, "ASC");
					player.sendTextMessage(
							t.get("TC_MSG_SORT_ORDER_CHANGED", player).replace("PH_SORT_ORDER", "ASC"));
					break;
				case "sortdesc":
					GPSPlayerPreferences.setMarkerSortOrder(player, "DESC");
					player.sendTextMessage(
							t.get("TC_MSG_SORT_ORDER_CHANGED", player).replace("PH_SORT_ORDER", "DESC"));
					break;
				default:
					player.sendTextMessage(t.get("TC_ERR_CMD_UNKNOWN").replace("PH_PLUGIN_CMD", pluginCMD));
					break;
			}
		}
	}

	@EventMethod
	public void onPlayerSpawnEvent(PlayerSpawnEvent event) {
		Player player = event.getPlayer();

		GPSPlayerPreferences.load(player);

		if (s.enableWelcomeMessage) {
			// Player player = event.getPlayer();
			String lang = player.getSystemLanguage();
			player.sendTextMessage(t.get("TC_MSG_PLUGIN_WELCOME", lang)
					.replace("PH_PLUGIN_NAME", getDescription("name"))
					.replace("PH_PLUGIN_CMD", pluginCMD)
					.replace("PH_PLUGIN_VERSION", getDescription("version")));
		}
		if (player.isAdmin() && (s.enableTeleportTokens || !s.travelCostMode.equals("disabled")
				|| s.enableMarkerCreateCosts)) {
			if (GPSEconomy.getInstance() != null && !GPSEconomy.getInstance().walletAvailable()) {
				player.sendTextMessage(c.warning + "OZ - GPS economy features require OZ - Wallet.");
			}
		}
	}

	@EventMethod
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player player = event.getPlayer();

		player.setAttribute("death-location", event.getDeathPosition());
	}

}
