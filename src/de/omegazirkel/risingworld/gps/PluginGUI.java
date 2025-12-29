package de.omegazirkel.risingworld.gps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.ui.CreateMarkerOverlay;
import de.omegazirkel.risingworld.gps.ui.GPSGridOverlay;
import de.omegazirkel.risingworld.gps.ui.TeleportOverlay;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.AssetManager;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.MenuItem;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import de.omegazirkel.risingworld.tools.ui.PluginMenuManager;
import net.risingworld.api.Plugin;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.SpawnPointType;
import net.risingworld.api.utils.Vector3f;

public class PluginGUI {
    private static PluginGUI instance = null;
    private static PluginSettings s = PluginSettings.getInstance();
    private static final Integer markersPerPage = 5;

    public static final List<String> markerKeys = Arrays.asList(
            "icon-ki-arctic-01", "icon-ki-arctic-02", "icon-ki-arctic-03", "icon-ki-arctic-04", "icon-ki-arctic-05",
            "icon-ki-cave-01", "icon-ki-cave-02", "icon-ki-cave-03", "icon-ki-cave-04", "icon-ki-cave-05",
            "icon-ki-coast-01", "icon-ki-coast-02", "icon-ki-coast-03", "icon-ki-coast-04", "icon-ki-coast-05",
            "icon-ki-desert-01", "icon-ki-desert-02", "icon-ki-desert-03", "icon-ki-desert-04", "icon-ki-desert-05",
            "icon-ki-forest-01", "icon-ki-forest-02", "icon-ki-forest-03", "icon-ki-forest-04", "icon-ki-forest-05",
            "icon-ki-mountain-01", "icon-ki-mountain-02", "icon-ki-mountain-03", "icon-ki-mountain-04",
            "icon-ki-mountain-05",
            "icon-ki-savanna-01", "icon-ki-savanna-02", "icon-ki-savanna-03", "icon-ki-savanna-04",
            "icon-ki-savanna-05",
            "icon-ki-sleep-01", "icon-ki-sleep-02", "icon-ki-sleep-03", "icon-ki-sleep-04", "icon-ki-sleep-05",
            "icon-ki-village-01", "icon-ki-village-02", "icon-ki-village-03", "icon-ki-village-04",
            "icon-ki-village-05",
            "icon-ki-special-01");

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    private PluginGUI() {

    }

    public static PluginGUI getInstance(Plugin p) {
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-add-marker"); // add marker
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-next-page"); // next page
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-previous-page"); // previous page
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-global");
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-group-alt");
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-group");
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-plugin");
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-private");
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-static");
        AssetManager.loadIconFromPlugin(p, "icon-ki-gps-grid-view");
        AssetManager.loadIconFromPlugin(p, "icon-gpt-exit");

        AssetManager.loadIconFromPlugin(p, "trash-xmark");

        // Marker icons

        for (String key : markerKeys) {
            AssetManager.loadIconFromPlugin(p, key);
        }

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
                AssetManager.getIcon(marker.getIcon()),
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
                            p.sendTextMessage(t().get("TC_GPS_CANCELED", p));
                            onTeleportAborted.onCall(true);
                            return;
                        }
                        p.setPosition(marker.getPosition());
                        switch (marker.getType()) {
                            case PRIVATE:
                                GPSEventUtils.onPrivateGPSEvent(p, marker.getName(), marker.getPosition());
                                break;
                            case GROUP:
                                GPSEventUtils.onGroupGPSEvent(p, marker.getName(), marker.getPosition());
                                break;
                            case GLOBAL:
                                GPSEventUtils.onGlobalGPSEvent(p, marker.getName(), marker.getPosition());
                                break;
                            case STATIC:
                                GPSEventUtils.onStaticGPSEvent(p, marker.getName(), marker.getPosition());
                                break;
                        }
                    });
                    p.setAttribute("gps-ui-overlay", to);

                    p.hideRadialMenu(true);
                    CursorManager.show(p);
                    p.addUIElement(to);
                });
    }

    public void openMainMenu(Player uiPlayer) {
        List<MenuItem> menuItems = new ArrayList<>();
        Callback<Player> onBackReopen = (Player player) -> openMainMenu(player);

        menuItems.add(
                new MenuItem(AssetManager.getIcon("icon-ki-gps-grid-view"), t().get("TC_MENU_GRID_VIEW", uiPlayer),
                        (Player p) -> {
                            OZUIElement overlay = null;
                            // remove existing overlays from this plugin before adding
                            if (p.hasAttribute("gps-ui-overlay")) {
                                overlay = (OZUIElement) p.getAttribute("gps-ui-overlay");
                                p.removeUIElement(overlay);
                            }
                            overlay = new GPSGridOverlay(p);
                            p.setAttribute("gps-ui-overlay", overlay);

                            p.hideRadialMenu(true);
                            CursorManager.show(p);
                            p.addUIElement(overlay);
                        }));

        if (s.enablePrivateMarkers)
            menuItems.add(
                    new MenuItem(AssetManager.getIcon("icon-ki-gps-private"),
                            t().get("TC_MENU_PRIVATE_MARKER", uiPlayer),
                            (Player p) -> openPrivateTeleportMenu(p, 0, onBackReopen)));
        if (s.enableGroupMarkers)
            menuItems
                    .add(new MenuItem(AssetManager.getIcon("icon-ki-gps-group-alt"),
                            t().get("TC_MENU_GROUP_MARKER", uiPlayer),
                            (Player p) -> openGroupTeleportMenu(p, 0, onBackReopen)));
        if (s.enableGlobalMarkers)
            menuItems.add(
                    new MenuItem(AssetManager.getIcon("icon-ki-gps-global"), t().get("TC_MENU_GLOBAL_MARKER", uiPlayer),
                            (Player p) -> openGlobalTeleportMenu(p, 0, onBackReopen)));
        if (s.enableStaticMarkers)
            menuItems.add(
                    new MenuItem(AssetManager.getIcon("icon-ki-gps-static"), t().get("TC_MENU_STATIC_MARKER", uiPlayer),
                            (Player p) -> openStaticTeleportMenu(p, onBackReopen)));

        menuItems.add(MenuItem.closeMenu(uiPlayer));
        PluginMenuManager.showMenu(uiPlayer, menuItems);
    }

    /**
     * player created teleport marker
     * 
     * @param uiPlayer
     */
    public void openPrivateTeleportMenu(Player uiPlayer, Integer level, Callback<Player> onBack) {
        List<MenuItem> menuItems = new ArrayList<>();
        Callback<Player> onBackReopen = (Player player) -> openPrivateTeleportMenu(player, level, onBack);
        // Get markers from db
        String orderBy = uiPlayer.getAttribute("gps.sort-order").toString();
        List<Marker> markers = GPSDatabase.getInstance().getPrivateMarker(uiPlayer.getDbID(), level, markersPerPage, orderBy);

        // Add marker menu item
        menuItems.add(
                new MenuItem(AssetManager.getIcon("icon-ki-gps-add-marker"),
                        t().get("TC_MENU_ADD_MARKER_PRIVATE", uiPlayer),
                        (Player p) -> {
                            OZUIElement overlay = null;
                            // remove existing overlays from this plugin before adding
                            if (p.hasAttribute("gps-ui-overlay")) {
                                overlay = (OZUIElement) p.getAttribute("gps-ui-overlay");
                                p.removeUIElement(overlay);
                            }
                            overlay = new CreateMarkerOverlay(p, MarkerType.PRIVATE, marker -> {

                                GPSDatabase.getInstance().saveMarker(marker);
                                openPrivateTeleportMenu(p, level, onBack);
                                p.sendTextMessage(t().get("TC_GPS_PRIVATE_CREATED", p)
                                        .replace("PH_MARKER_NAME", marker.getName())
                                        .replace("PH_MARKER_POS", marker.getPosition() + ""));

                            });
                            p.setAttribute("gps-ui-overlay", overlay);

                            p.hideRadialMenu(true);
                            CursorManager.show(p);
                            p.addUIElement(overlay);

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
                    new MenuItem(AssetManager.getIcon("icon-ki-gps-next-page"), t().get("TC_MENU_NEXT_PAGE", uiPlayer),
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
                    player.setAttribute("pre-port-location", player.getPosition());
                    player.setPosition(pos);// .
                    GPSEventUtils.onStaticGPSEvent(uiPlayer, label, pos);
                    player.hideRadialMenu(false);
                };

        if (primarySpawnPos != null)
            menuItems.add(new MenuItem(AssetManager.getIcon("icon-ki-sleep-01"),
                    t().get("TC_MENU_STATIC_PRIMARY_SPAWN", uiPlayer),
                    teleportAction.apply(primarySpawnPos, t().get("TC_MENU_STATIC_PRIMARY_SPAWN", uiPlayer))));
        if (secondarySpawnPos != null)
            menuItems.add(
                    new MenuItem(AssetManager.getIcon("icon-ki-sleep-02"),
                            t().get("TC_MENU_STATIC_SECONDARY_SPAWN", uiPlayer),
                            teleportAction.apply(secondarySpawnPos,
                                    t().get("TC_MENU_STATIC_SECONDARY_SPAWN", uiPlayer))));
        if (tertiarySpawnPos != null)
            menuItems.add(new MenuItem(AssetManager.getIcon("icon-ki-sleep-03"),
                    t().get("TC_MENU_STATIC_TERTIARY_SPAWN", uiPlayer),
                    teleportAction.apply(tertiarySpawnPos, t().get("TC_MENU_STATIC_TERTIARY_SPAWN", uiPlayer))));
        if (quaternarySpawnPos != null)
            menuItems.add(
                    new MenuItem(AssetManager.getIcon("icon-ki-sleep-04"),
                            t().get("TC_MENU_STATIC_QUATERNARY_SPAWN", uiPlayer),
                            teleportAction.apply(quaternarySpawnPos,
                                    t().get("TC_MENU_STATIC_QUATERNARY_SPAWN", uiPlayer))));
        if (defaultSpawnPos != null)
            menuItems.add(new MenuItem(AssetManager.getIcon("icon-ki-coast-01"),
                    t().get("TC_MENU_STATIC_DEFAULT_SPAWN", uiPlayer),
                    teleportAction.apply(defaultSpawnPos, t().get("TC_MENU_STATIC_DEFAULT_SPAWN", uiPlayer))));
        if (lastPositionBeforePort != null)
            menuItems.add(
                    new MenuItem(AssetManager.getIcon("icon-ki-special-01"),
                            t().get("TC_MENU_STATIC_BACKPORT", uiPlayer),
                            (Player p) -> {
                                p.setPosition(lastPositionBeforePort);// .
                                GPSEventUtils.onStaticGPSEvent(uiPlayer, t().get("TC_MENU_STATIC_BACKPORT", uiPlayer),
                                        lastPositionBeforePort);
                                p.hideRadialMenu(false);
                            }));
        if (lastDeathPosition != null)
            menuItems.add(
                    new MenuItem(AssetManager.getIcon("icon-ki-sleep-05"),
                            t().get("TC_MENU_STATIC_DEATHPORT", uiPlayer),
                            (Player p) -> {
                                p.setPosition(lastDeathPosition);// .
                                GPSEventUtils.onStaticGPSEvent(uiPlayer, t().get("TC_MENU_STATIC_DEATHPORT", uiPlayer),
                                        lastPositionBeforePort);
                                p.hideRadialMenu(false);
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
        List<MenuItem> menuItems = new ArrayList<>();
        Callback<Player> onBackReopen = (Player player) -> openGroupTeleportMenu(player, level, onBack);
        // Get markers from db
        String orderBy = uiPlayer.getAttribute("gps.sort-order").toString();
        List<Marker> markers = GPSDatabase.getInstance().getGroupMarker(uiPlayer.getPermissionGroup(), level,
                markersPerPage, orderBy);

        // Add marker menu item
        menuItems
                .add(new MenuItem(AssetManager.getIcon("icon-ki-gps-add-marker"),
                        t().get("TC_MENU_ADD_MARKER_GROUP", uiPlayer),
                        (Player p) -> {
                            OZUIElement overlay = null;
                            // remove existing overlays from this plugin before adding
                            if (p.hasAttribute("gps-ui-overlay")) {
                                overlay = (OZUIElement) p.getAttribute("gps-ui-overlay");
                                p.removeUIElement(overlay);
                            }
                            overlay = new CreateMarkerOverlay(p, p.getPermissionGroup(), marker -> {

                                GPSDatabase.getInstance().saveMarker(marker);
                                openGroupTeleportMenu(p, level, onBack);
                                p.sendTextMessage(t().get("TC_GPS_GROUP_CREATED", p)
                                        .replace("PH_MARKER_NAME", marker.getName())
                                        .replace("PH_MARKER_POS", marker.getPosition() + ""));

                            });
                            p.setAttribute("gps-ui-overlay", overlay);

                            p.hideRadialMenu(true);
                            CursorManager.show(p);
                            p.addUIElement(overlay);
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
                    new MenuItem(AssetManager.getIcon("icon-ki-gps-next-page"), t().get("TC_MENU_NEXT_PAGE", uiPlayer),
                            (Player p) -> openGroupTeleportMenu(p, level + 1, onBackReopen)));

        PluginMenuManager.showMenu(uiPlayer, menuItems);
    }

    /**
     * Global teleport marker
     * 
     * @param uiPlayer
     */
    public void openGlobalTeleportMenu(Player uiPlayer, Integer level, Callback<Player> onBack) {
        List<MenuItem> menuItems = new ArrayList<>();
        Callback<Player> onBackReopen = (Player player) -> openGlobalTeleportMenu(player, level, onBack);
        // Get markers from db
        String orderBy = uiPlayer.getAttribute("gps.sort-order").toString();
        List<Marker> markers = GPSDatabase.getInstance().getGlobalMarker(level, markersPerPage, orderBy);

        // Add marker menu item
        if (uiPlayer.isAdmin())
            menuItems.add(
                    new MenuItem(AssetManager.getIcon("icon-ki-gps-add-marker"),
                            t().get("TC_MENU_ADD_MARKER_GLOBAL", uiPlayer),
                            (Player p) -> {
                                OZUIElement overlay = null;
                                // remove existing overlays from this plugin before adding
                                if (p.hasAttribute("gps-ui-overlay")) {
                                    overlay = (OZUIElement) p.getAttribute("gps-ui-overlay");
                                    p.removeUIElement(overlay);
                                }
                                overlay = new CreateMarkerOverlay(p, marker -> {

                                    GPSDatabase.getInstance().saveMarker(marker);
                                    openGlobalTeleportMenu(p, level, onBack);
                                    p.sendTextMessage(t().get("TC_GPS_GLOBAL_CREATED", p)
                                            .replace("PH_MARKER_NAME", marker.getName())
                                            .replace("PH_MARKER_POS", marker.getPosition() + ""));

                                });
                                p.setAttribute("gps-ui-overlay", overlay);

                                p.hideRadialMenu(true);
                                CursorManager.show(p);
                                p.addUIElement(overlay);
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
                    new MenuItem(AssetManager.getIcon("icon-ki-gps-next-page"), t().get("TC_MENU_NEXT_PAGE", uiPlayer),
                            (Player p) -> openGlobalTeleportMenu(p, level + 1, onBackReopen)));

        PluginMenuManager.showMenu(uiPlayer, menuItems);
    }

}
