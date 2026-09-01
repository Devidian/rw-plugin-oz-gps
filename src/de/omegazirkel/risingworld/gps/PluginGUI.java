package de.omegazirkel.risingworld.gps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.GPSEconomy.EconomyResult;
import de.omegazirkel.risingworld.gps.ui.CreateMarkerOverlay;
import de.omegazirkel.risingworld.gps.ui.GPSGridOverlay;
import de.omegazirkel.risingworld.gps.ui.TeleportOverlay;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.AssetManager;
import de.omegazirkel.risingworld.tools.ui.MenuItem;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import de.omegazirkel.risingworld.tools.ui.PluginInfoStatusProviders;
import de.omegazirkel.risingworld.tools.ui.PluginMenuManager;
import net.risingworld.api.Plugin;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UITarget;
import net.risingworld.api.utils.SpawnPointType;
import net.risingworld.api.utils.Vector3f;

public class PluginGUI {
    private static PluginGUI instance = null;
    private static PluginSettings s = PluginSettings.getInstance();
    private static final Integer markersPerPage = 5;

    public static final List<String> markerKeys = Arrays.asList(
            "marker-arctic-polarbear", "marker-arctic-penguin", "marker-arctic-seal", "marker-arctic-northern-lights", "marker-arctic-iglu",
            "marker-cave-stalactite", "marker-cave-bat", "marker-cave-painting", "marker-cave-minerals", "marker-cave-cavelers",
            "marker-coast-jetty", "marker-coast-lighthouse", "marker-coast-boat", "marker-coast-house", "marker-coast-fishing",
            "marker-desert-palm", "marker-desert-pyramids", "marker-desert-camel", "marker-desert-oasis", "marker-desert-night",
            "marker-forest-default", "marker-forest-clearing", "marker-forest-log-cabin", "marker-forest-camping", "marker-forest-woodworker",
            "marker-mountain-near", "marker-mountain-house", "marker-mountain-lake", "marker-mountain-animals",
            "marker-mountain-cross",
            "marker-savanna-elephant", "marker-savanna-zebra", "marker-savanna-giraffe", "marker-savanna-lion",
            "marker-savanna-rhino",
            "marker-sleep-restroom", "marker-sleep-tent", "marker-sleep-king-size-bed", "marker-sleep-sign", "marker-sleep-rip",
            "marker-village-small", "marker-village-medium", "marker-village-market", "marker-village-well",
            "marker-village-pallisade",
            "marker-special-destination",

            "marker-jail-island",
            "marker-farm-alpine",
            "marker-farm-animals",
            "marker-farm-fields",
            "marker-factory-modern",
            "marker-farm-default",
            "marker-factory-old",
            "marker-space-station",
            "marker-factory-train-station");

    public static final List<String> serverPinKeys = Arrays.asList(
            "marker-server-1", "marker-server-2", "marker-server-3", "marker-server-4", "marker-server-5",
            "marker-server-6", "marker-server-7", "marker-server-8", "marker-server-9", "marker-server-10",
            "marker-server-11", "marker-server-12", "marker-server-13", "marker-server-14", "marker-server-15");

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    private PluginGUI() {

    }

    public static PluginGUI getInstance(Plugin p) {
        AssetManager.loadIconFromPlugin(p, "gps-marker-create"); // add marker
        AssetManager.loadIconFromPlugin(p, "next-page"); // next page
        AssetManager.loadIconFromPlugin(p, "previous-page"); // previous page
        AssetManager.loadIconFromPlugin(p, "menu-global-marker");
        AssetManager.loadIconFromPlugin(p, "menu-server-warp");
        AssetManager.loadIconFromPlugin(p, "gps-server-pin");
        AssetManager.loadIconFromPlugin(p, "gps-server-pin-create");
        AssetManager.loadIconFromPlugin(p, "menu-marker-group-alt");
        AssetManager.loadIconFromPlugin(p, "menu-marker-group");
        AssetManager.loadIconFromPlugin(p, "oz-gps");
        AssetManager.loadIconFromPlugin(p, "menu-marker-private");
        AssetManager.loadIconFromPlugin(p, "menu-marker-static");
        AssetManager.loadIconFromPlugin(p, "menu-grid-view");
        AssetManager.loadIconFromPlugin(p, "coin-gps-token");

        AssetManager.loadIconFromPlugin(p, "gps-marker-delete");
        AssetManager.loadIconFromPlugin(p, "gps-marker-edit");

        // Marker icons

        for (String key : markerKeys) {
            AssetManager.loadIconFromPlugin(p, key);
        }
        for (String key : serverPinKeys) AssetManager.loadIconFromPlugin(p, key);

        return getInstance();
    }

    public static PluginGUI getInstance() {
        if (instance == null) {
            instance = new PluginGUI();
        }
        return instance;
    }

    private MenuItem fromMarker(Marker marker, Callback<Boolean> onTeleportAborted) {
        return new MenuItem(
                marker.getIcon(),
                marker.getName(),
                (Player p) -> {
                    OZUIElement overlay = null;
                    // remove existing overlays from this plugin before adding
                    if (p.hasAttribute("gps-ui-overlay")) {
                        overlay = (OZUIElement) p.getAttribute("gps-ui-overlay");
                        p.removeUIElement(overlay);
                    }
                    TeleportOverlay to = new TeleportOverlay(p, marker, b -> {
                        if (!b) {
                            p.sendTextMessage(t().get("tc.gps.canceled", p));
                            onTeleportAborted.onCall(true);
                            return;
                        }
                        GPSEventUtils.executeTeleport(p, marker.getPosition(), marker.getName(), marker.getType());
                    }, markerChanged -> {
                        onTeleportAborted.onCall(true);
                    });
                    p.setAttribute("gps-ui-overlay", to);

                    p.hideRadialMenu(true);
                    p.addUIElement(to, UITarget.Modal);
                });
    }

    public void openMainMenu(Player uiPlayer) {
        List<MenuItem> menuItems = new ArrayList<>();
        Callback<Player> onBackReopen = (Player player) -> openMainMenu(player);
        boolean nonStaticAccess = GPSAccessPolicy.canUseNonStatic(uiPlayer);

        if (nonStaticAccess && s.enablePrivateMarkers)
            menuItems.add(
                    new MenuItem("menu-marker-private",
                            t().get("tc.menu.private.marker", uiPlayer),
                            (Player p) -> openPrivateTeleportMenu(p, 0, onBackReopen)));
        if (nonStaticAccess && s.enableGroupMarkers)
            menuItems
                    .add(new MenuItem("menu-marker-group-alt",
                            t().get("tc.menu.group.marker", uiPlayer),
                            (Player p) -> openGroupTeleportMenu(p, 0, onBackReopen)));
        if (nonStaticAccess && s.enableGlobalMarkers)
            menuItems.add(
                    new MenuItem("menu-global-marker", t().get("tc.menu.global.marker", uiPlayer),
                            (Player p) -> openGlobalTeleportMenu(p, 0, onBackReopen)));
        if (s.enableStaticMarkers)
            menuItems.add(
                    new MenuItem("menu-marker-static", t().get("tc.menu.static.marker", uiPlayer),
                            (Player p) -> openStaticTeleportMenu(p, onBackReopen)));
		if (uiPlayer.isAdmin() && GPSAreaAccessPolicy.areaFeaturesEnabled() && uiPlayer.getCurrentArea() != null)
			menuItems.add(new MenuItem("marker-special-destination", t().get("tc.gps.area.manage", uiPlayer),
					(Player p) -> openGPSAreaManagement(p)));

        menuItems.add(PluginInfoStatusProviders.menuItem(t().get("tc.menu.info.status", uiPlayer), GPS.name));
        menuItems.add(MenuItem.closeMenu(uiPlayer));
        PluginMenuManager.showMenu(uiPlayer, menuItems);
    }

    public void openPreferredEntry(Player uiPlayer) {
        if (GPSPlayerPreferences.ENTRY_MODE_RADIAL.equals(GPSPlayerPreferences.entryMode(uiPlayer))) {
            openMainMenu(uiPlayer);
            return;
        }
        openGridView(uiPlayer);
    }

    public void openGridView(Player uiPlayer) {
        OZUIElement overlay = null;
        if (uiPlayer.hasAttribute("gps-ui-overlay")) {
            overlay = (OZUIElement) uiPlayer.getAttribute("gps-ui-overlay");
            uiPlayer.removeUIElement(overlay);
        }
        overlay = new GPSGridOverlay(uiPlayer);
        uiPlayer.setAttribute("gps-ui-overlay", overlay);

        uiPlayer.hideRadialMenu(true);
        uiPlayer.addUIElement(overlay, UITarget.Modal);
    }

	private void openGPSAreaManagement(Player player) {
		if (!player.isAdmin() || !GPSAreaAccessPolicy.areaFeaturesEnabled() || player.getCurrentArea() == null) {
			return;
		}
		long areaId = player.getCurrentArea().getID();
		boolean marked = GPSDatabase.getInstance().isGPSAreaAllowed(areaId);
		boolean updated = marked ? GPSDatabase.getInstance().deleteGPSArea(areaId)
				: GPSDatabase.getInstance().saveGPSArea(areaId, player.getDbID(), true);
		if (updated) {
			player.sendTextMessage(t().get(marked ? "tc.gps.area.unmarked" : "tc.gps.area.marked", player));
		}
		openGridView(player);
	}

    /**
     * player created teleport marker
     * 
     * @param uiPlayer
     */
    public void openPrivateTeleportMenu(Player uiPlayer, Integer level, Callback<Player> onBack) {
        if (!requireNonStaticAccess(uiPlayer)) {
            openMainMenu(uiPlayer);
            return;
        }
        List<MenuItem> menuItems = new ArrayList<>();
        Callback<Player> onBackReopen = (Player player) -> openPrivateTeleportMenu(player, level, onBack);
        // Get markers from db
        String orderBy = GPSPlayerPreferences.markerSortOrder(uiPlayer);
        List<Marker> markers = GPSDatabase.getInstance().getPrivateMarker(uiPlayer.getDbID(), level, markersPerPage,
                orderBy);

        // Add marker menu item
        if (GPSAreaAccessPolicy.markerCreationDenialKey(uiPlayer, MarkerType.PRIVATE) == null) menuItems.add(
                new MenuItem("gps-marker-create",
                        t().get("tc.menu.add.marker.private", uiPlayer),
                        (Player p) -> {
                            if (!canCreateMarker(p, MarkerType.PRIVATE)) {
                                openPrivateTeleportMenu(p, level, onBack);
                                return;
                            }
                            OZUIElement overlay = null;
                            // remove existing overlays from this plugin before adding
                            if (p.hasAttribute("gps-ui-overlay")) {
                                overlay = (OZUIElement) p.getAttribute("gps-ui-overlay");
                                p.removeUIElement(overlay);
                            }
                            overlay = new CreateMarkerOverlay(p, MarkerType.PRIVATE, marker -> {

                                if (!saveMarkerWithEconomy(p, marker)) {
                                    return;
                                }
                                openPrivateTeleportMenu(p, level, onBack);

                            });
                            p.setAttribute("gps-ui-overlay", overlay);

                            p.hideRadialMenu(true);
                            p.addUIElement(overlay, UITarget.Modal);

                        }));

        menuItems.add(MenuItem.closeMenu(uiPlayer));
        menuItems.add(MenuItem.backMenu(uiPlayer, onBack));

        for (Marker marker : markers) {
            menuItems.add(fromMarker(marker, b -> {
                openPrivateTeleportMenu(uiPlayer, level, onBack);
            }));
        }

        if (markers.size() >= markersPerPage)
            menuItems.add(
                    new MenuItem("next-page", t().get("tc.menu.next.page", uiPlayer),
                            (Player p) -> openPrivateTeleportMenu(p, level + 1, onBackReopen)));

        PluginMenuManager.showMenu(uiPlayer, menuItems);
    }

    /**
     * Static spawns like serverspawn, bed, tent, lastdeath, etc.
     *
     * @param uiPlayer
     */
    public void openStaticTeleportMenu(Player uiPlayer, Callback<Player> onBack) {
        List<MenuItem> menuItems = new ArrayList<>();

        Vector3f lastDeathPosition = (Vector3f) uiPlayer.getAttribute("death-location");
        Vector3f lastPositionBeforePort = (Vector3f) uiPlayer.getAttribute("pre-port-location");
        Vector3f primarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Primary);
        Vector3f secondarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Secondary);
        Vector3f tertiarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Tertiary);
        Vector3f quaternarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Quaternary);
        Vector3f defaultSpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Default);

        BiFunction<Vector3f, String, Callback<Player>> teleportAction = (Vector3f pos,
                String label) -> (Player player) -> {
                    if (GPSEventUtils.executeTeleport(player, pos, label, MarkerType.STATIC)) {
                        player.hideRadialMenu(false);
                    }
                };

        if (primarySpawnPos != null)
            menuItems.add(new MenuItem("marker-sleep-restroom",
                    t().get("tc.menu.static.primary.spawn", uiPlayer),
                    teleportAction.apply(primarySpawnPos, t().get("tc.menu.static.primary.spawn", uiPlayer))));
        if (secondarySpawnPos != null)
            menuItems.add(
                    new MenuItem("marker-sleep-tent",
                            t().get("tc.menu.static.secondary.spawn", uiPlayer),
                            teleportAction.apply(secondarySpawnPos,
                                    t().get("tc.menu.static.secondary.spawn", uiPlayer))));
        if (tertiarySpawnPos != null)
            menuItems.add(new MenuItem("marker-sleep-king-size-bed",
                    t().get("tc.menu.static.tertiary.spawn", uiPlayer),
                    teleportAction.apply(tertiarySpawnPos, t().get("tc.menu.static.tertiary.spawn", uiPlayer))));
        if (quaternarySpawnPos != null)
            menuItems.add(
                    new MenuItem("marker-sleep-sign",
                            t().get("tc.menu.static.quaternary.spawn", uiPlayer),
                            teleportAction.apply(quaternarySpawnPos,
                                    t().get("tc.menu.static.quaternary.spawn", uiPlayer))));
        if (defaultSpawnPos != null)
            menuItems.add(new MenuItem("marker-coast-jetty",
                    t().get("tc.menu.static.default.spawn", uiPlayer),
                    teleportAction.apply(defaultSpawnPos, t().get("tc.menu.static.default.spawn", uiPlayer))));
        if (lastPositionBeforePort != null)
            menuItems.add(
                    new MenuItem("marker-special-destination",
                            t().get("tc.menu.static.backport", uiPlayer),
                            (Player p) -> {
                                if (GPSEventUtils.executeTeleport(uiPlayer, lastPositionBeforePort,
                                        t().get("tc.menu.static.backport", uiPlayer), MarkerType.STATIC, false)) {
                                    p.hideRadialMenu(false);
                                }
                            }));
        if (lastDeathPosition != null)
            menuItems.add(
                    new MenuItem("marker-sleep-rip",
                            t().get("tc.menu.static.deathport", uiPlayer),
                            (Player p) -> {
                                if (GPSEventUtils.executeTeleport(uiPlayer, lastDeathPosition,
                                        t().get("tc.menu.static.deathport", uiPlayer), MarkerType.STATIC, false)) {
                                    p.hideRadialMenu(false);
                                }
                            }));

        menuItems.add(MenuItem.closeMenu(uiPlayer));
        menuItems.add(MenuItem.backMenu(uiPlayer, onBack));
        PluginMenuManager.showMenu(uiPlayer, menuItems);
    }

    /**
     * Group teleport marker [permission group]
     * 
     * @param uiPlayer
     */
    public void openGroupTeleportMenu(Player uiPlayer, Integer level, Callback<Player> onBack) {
        if (!requireNonStaticAccess(uiPlayer)) {
            openMainMenu(uiPlayer);
            return;
        }
        List<MenuItem> menuItems = new ArrayList<>();
        Callback<Player> onBackReopen = (Player player) -> openGroupTeleportMenu(player, level, onBack);
        // Get markers from db
        String orderBy = GPSPlayerPreferences.markerSortOrder(uiPlayer);
        List<Marker> markers = GPSDatabase.getInstance().getGroupMarker(uiPlayer.getPermissionGroup(), level,
                markersPerPage, orderBy);

        // Add marker menu item
        if (GPSAreaAccessPolicy.markerCreationDenialKey(uiPlayer, MarkerType.GROUP) == null) menuItems
                .add(new MenuItem("gps-marker-create",
                        t().get("tc.menu.add.marker.group", uiPlayer),
                        (Player p) -> {
                            if (!canCreateMarker(p, MarkerType.GROUP)) {
                                openGroupTeleportMenu(p, level, onBack);
                                return;
                            }
                            OZUIElement overlay = null;
                            // remove existing overlays from this plugin before adding
                            if (p.hasAttribute("gps-ui-overlay")) {
                                overlay = (OZUIElement) p.getAttribute("gps-ui-overlay");
                                p.removeUIElement(overlay);
                            }
                            overlay = new CreateMarkerOverlay(p, p.getPermissionGroup(), marker -> {

                                if (!saveMarkerWithEconomy(p, marker)) {
                                    return;
                                }
                                openGroupTeleportMenu(p, level, onBack);

                            });
                            p.setAttribute("gps-ui-overlay", overlay);

                            p.hideRadialMenu(true);
                            p.addUIElement(overlay, UITarget.Modal);
                        }));
        menuItems.add(MenuItem.closeMenu(uiPlayer));
        menuItems.add(MenuItem.backMenu(uiPlayer, onBack));

        for (Marker marker : markers) {
            menuItems.add(fromMarker(marker, b -> {
                openGroupTeleportMenu(uiPlayer, level, onBack);
            }));
        }

        if (markers.size() >= markersPerPage)
            menuItems.add(
                    new MenuItem("next-page", t().get("tc.menu.next.page", uiPlayer),
                            (Player p) -> openGroupTeleportMenu(p, level + 1, onBackReopen)));

        PluginMenuManager.showMenu(uiPlayer, menuItems);
    }

    /**
     * Global teleport marker
     * 
     * @param uiPlayer
     */
    public void openGlobalTeleportMenu(Player uiPlayer, Integer level, Callback<Player> onBack) {
        if (!requireNonStaticAccess(uiPlayer)) {
            openMainMenu(uiPlayer);
            return;
        }
        List<MenuItem> menuItems = new ArrayList<>();
        Callback<Player> onBackReopen = (Player player) -> openGlobalTeleportMenu(player, level, onBack);
        // Get markers from db
        String orderBy = GPSPlayerPreferences.markerSortOrder(uiPlayer);
        List<Marker> markers = GPSDatabase.getInstance().getGlobalMarker(level, markersPerPage, orderBy);

        // Add marker menu item
        if (uiPlayer.isAdmin())
            menuItems.add(
                    new MenuItem("gps-marker-create",
                            t().get("tc.menu.add.marker.global", uiPlayer),
                            (Player p) -> {
                                if (!canCreateMarker(p, MarkerType.GLOBAL)) {
                                    openGlobalTeleportMenu(p, level, onBack);
                                    return;
                                }
                                OZUIElement overlay = null;
                                // remove existing overlays from this plugin before adding
                                if (p.hasAttribute("gps-ui-overlay")) {
                                    overlay = (OZUIElement) p.getAttribute("gps-ui-overlay");
                                    p.removeUIElement(overlay);
                                }
                                overlay = new CreateMarkerOverlay(p, marker -> {

                                    GPSDatabase.getInstance().saveMarker(marker);
                                    openGlobalTeleportMenu(p, level, onBack);
                                    p.sendTextMessage(t().get("tc.gps.global.created", p)
                                            .replace("PH_MARKER_NAME", marker.getName())
                                            .replace("PH_MARKER_POS", marker.getPosition() + ""));

                                });
                                p.setAttribute("gps-ui-overlay", overlay);

                                p.hideRadialMenu(true);
                                p.addUIElement(overlay, UITarget.Modal);
                            }));
        menuItems.add(MenuItem.closeMenu(uiPlayer));
        menuItems.add(MenuItem.backMenu(uiPlayer, onBack));

        for (Marker marker : markers) {
            menuItems.add(fromMarker(marker, b -> {
                openGlobalTeleportMenu(uiPlayer, level, onBack);
            }));
        }

        if (markers.size() >= markersPerPage)
            menuItems.add(
                    new MenuItem("next-page", t().get("tc.menu.next.page", uiPlayer),
                            (Player p) -> openGlobalTeleportMenu(p, level + 1, onBackReopen)));

        PluginMenuManager.showMenu(uiPlayer, menuItems);
    }

    private boolean canCreateMarker(Player player, MarkerType type) {
        if (!requireAccess(player, type)) {
            return false;
        }
		String restrictionDenialKey = GPSAreaAccessPolicy.markerCreationDenialKey(player, type);
		if (restrictionDenialKey != null) {
			player.sendTextMessage(t().get(restrictionDenialKey, player));
			return false;
		}
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy != null && economy.markerLimitReached(player, type)) {
            player.sendTextMessage(t().get("tc.gps.marker.limit.reached", player)
                    .replace("PH_LIMIT", String.valueOf(economy.markerLimit(type))));
            return false;
        }
        return true;
    }

    private boolean requireNonStaticAccess(Player player) {
        return requireAccess(player, MarkerType.PRIVATE);
    }

    private boolean requireAccess(Player player, MarkerType type) {
        if (GPSAccessPolicy.canUse(player, type)) {
            return true;
        }
        player.sendTextMessage(t().get("tc.gps.playtime.required", player)
                .replace("PH_REQUIRED_MINUTES", String.valueOf(GPSAccessPolicy.requiredMinutes()))
                .replace("PH_REMAINING_MINUTES", String.valueOf(GPSAccessPolicy.remainingMinutes(player))));
        return false;
    }

    private boolean saveMarkerWithEconomy(Player player, Marker marker) {
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy != null) {
            EconomyResult charge = economy.chargeMarkerCreation(player, marker.getType());
            if (!charge.success()) {
                player.sendTextMessage(t().get("tc.gps.economy.failed", player)
                        .replace("PH_MESSAGE", charge.message()));
                return false;
            }
            if (!charge.message().isBlank()) {
                player.sendTextMessage(t().get("tc.gps.cost.charged", player)
                        .replace("PH_COST", charge.message()));
            }
        }
        GPSDatabase.getInstance().saveMarker(marker);
        String key = marker.getType() == MarkerType.GROUP ? "tc.gps.group.created" : "tc.gps.private.created";
        player.sendTextMessage(t().get(key, player)
                .replace("PH_MARKER_NAME", marker.getName())
                .replace("PH_MARKER_POS", marker.getPosition() + ""));
        return true;
    }

}
