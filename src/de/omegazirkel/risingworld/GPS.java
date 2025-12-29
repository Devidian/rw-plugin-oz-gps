package de.omegazirkel.risingworld;

import java.nio.file.Path;

import de.omegazirkel.risingworld.gps.GPSDatabase;
import de.omegazirkel.risingworld.gps.PluginGUI;
import de.omegazirkel.risingworld.gps.DiscordConnect;
import de.omegazirkel.risingworld.gps.PluginSettings;
import de.omegazirkel.risingworld.gps.ui.GPSGridOverlay;
import de.omegazirkel.risingworld.tools.Colors;
import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.OZLogger;
import de.omegazirkel.risingworld.tools.db.SQLite;
import de.omegazirkel.risingworld.tools.ui.AssetManager;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.MenuItem;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import de.omegazirkel.risingworld.tools.ui.PluginMenuManager;
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
		GPSDatabase.getInstance(new SQLite(this));
		gui = PluginGUI.getInstance(this);
		// Load Plugin Menu into Main Plugin Menu
		PluginMenuManager
				.registerPluginMenu(
						new MenuItem(AssetManager.getIcon("icon-ki-gps-plugin"), "GPS", (Player p) -> {
							gui.openMainMenu(p);
						}));
		// connect plugins
		DiscordConnect.init(this);
		logger().info("✅ " + this.getName() + " Plugin is enabled version:" + this.getDescription("version"));
	}

	@Override
	public void onDisable() {
	}

	@Override
	public void onSettingsChanged(Path settingsPath) {
		s.initSettings(settingsPath.toString());
		logger().setLevel(s.logLevel);
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
				case "status":
					String statusMessage = t.get("TC_CMD_STATUS", lang)
							.replace("PH_VERSION", c.okay + this.getDescription("version") + c.endTag)
							.replace("PH_LANGUAGE",
									c.info + player.getLanguage() + " / " + player.getSystemLanguage() + c.endTag)
							.replace("PH_USEDLANG", c.okay + t.getLanguageUsed(lang) + c.endTag)
							.replace("PH_LANG_AVAILABLE", c.warning + t.getLanguageAvailable() + c.endTag);
					player.sendTextMessage(c.okay + this.getName() + ":> " + c.text + statusMessage);
					break;
				case "help":
					String helpMessage = t.get("TC_CMD_HELP", player).replaceAll("PH_PLUGIN_CMD", pluginCMD);
					player.sendTextMessage(c.okay + this.getName() + ":> " + c.endTag + helpMessage);
					break;
				case "open":
					gui.openMainMenu(player);
					break;
				case "opengrid":
					OZUIElement overlay = new GPSGridOverlay(player);
					player.setAttribute("gps-ui-overlay", overlay);
					CursorManager.show(player);
					player.addUIElement(overlay);

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

		if (s.enableWelcomeMessage) {
			// Player player = event.getPlayer();
			String lang = player.getSystemLanguage();
			player.sendTextMessage(t.get("TC_MSG_PLUGIN_WELCOME", lang)
					.replace("PH_PLUGIN_NAME", getDescription("name"))
					.replace("PH_PLUGIN_CMD", pluginCMD)
					.replace("PH_PLUGIN_VERSION", getDescription("version")));
		}
	}

	@EventMethod
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player player = event.getPlayer();

		player.setAttribute("death-location", event.getDeathPosition());
	}

}
