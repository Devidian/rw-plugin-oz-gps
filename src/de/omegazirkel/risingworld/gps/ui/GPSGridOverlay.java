package de.omegazirkel.risingworld.gps.ui;

import java.util.List;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.GPSDatabase;
import de.omegazirkel.risingworld.gps.GPSAccessPolicy;
import de.omegazirkel.risingworld.gps.GPSAreaAccessPolicy;
import de.omegazirkel.risingworld.gps.GPSPlayerPreferences;
import de.omegazirkel.risingworld.gps.GPSEconomy;
import de.omegazirkel.risingworld.gps.GPSEventUtils;
import de.omegazirkel.risingworld.gps.Marker;
import de.omegazirkel.risingworld.gps.MarkerPermissions;
import de.omegazirkel.risingworld.gps.MarkerType;
import de.omegazirkel.risingworld.gps.PluginSettings;
import de.omegazirkel.risingworld.gps.ServerPin;
import de.omegazirkel.risingworld.gps.ServerPinAddress;
import de.omegazirkel.risingworld.gps.TeleportCooldowns;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.AssetManager;
import de.omegazirkel.risingworld.tools.ui.AdvancedButton;
import de.omegazirkel.risingworld.tools.ui.AdvancedButtonFactory;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.Timer;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Area;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UIScrollView;
import net.risingworld.api.ui.UIScrollView.ScrollViewMode;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.ScaleMode;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;
import net.risingworld.api.ui.style.Wrap;
import net.risingworld.api.utils.SpawnPointType;
import net.risingworld.api.utils.Vector3f;

public class GPSGridOverlay extends OZUIElement {

    private static final PluginSettings s = PluginSettings.getInstance();
    private static final float PANEL_ALPHA = 0.86f;
    private static final float BODY_ALPHA = 0.55f;
    private static final float CARD_ALPHA = 0.92f;
    private static final float GOLD_R = 0.95f;
    private static final float GOLD_G = 0.75f;
    private static final float GOLD_B = 0.25f;

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    private UIElement mainViewPanel = null;
    private UIElement tabSelectionHeader = null;
    private UIElement cooldownStatusBar = null;
    private UILabel cooldownStatusLabel = null;
    private UILabel markerLimitStatusLabel = null;
    private UIScrollView gridScrollView = null;
    private MarkerType currentMarkerType = MarkerType.PRIVATE;
    private boolean serverWarpTab;
    private Timer cooldownTimer = null;
    private boolean previousNonStaticAccess;

    private static final float scaleFactor = 0.75f;
    private static final Integer cardMargin = 5;

    public GPSGridOverlay(Player player) {
        super();
        previousNonStaticAccess = GPSAccessPolicy.canUseNonStatic(player);
        if (!previousNonStaticAccess) {
            currentMarkerType = MarkerType.STATIC;
        }
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
        setBackgroundColor(0, 0, 0, 0.4f);

        setupMainView(player);
        refreshGrid(player);
        refreshHeader(player);
        startCooldownTimer(player);
    }

    private void setupMainView(Player player) {
        mainViewPanel = new UIElement();
        mainViewPanel.setSize(75, 75, true);
        mainViewPanel.setPivot(Pivot.MiddleCenter);
        mainViewPanel.setPosition(50, 50, true);
        mainViewPanel.setBackgroundColor(0, 0, 0, PANEL_ALPHA);
        mainViewPanel.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.6f);
        mainViewPanel.setBorder(3);
        mainViewPanel.setBorderEdgeRadius(6, false);
        mainViewPanel.addChild(setupTabSelectionHeader(player));
        mainViewPanel.addChild(setupGridScrollView(player));
        mainViewPanel.addChild(setupCooldownStatus(player));
        addChild(mainViewPanel);
    }

    private UIElement setupTabSelectionHeader(Player player) {
        tabSelectionHeader = new UIElement();
        tabSelectionHeader.setSize(100, 10, true);
        tabSelectionHeader.setPivot(Pivot.UpperLeft);
        tabSelectionHeader.setPosition(0, 0, true);
        tabSelectionHeader.style.display.set(DisplayStyle.Flex);
        tabSelectionHeader.style.flexDirection.set(FlexDirection.Row);
        tabSelectionHeader.style.flexWrap.set(Wrap.Wrap);
        tabSelectionHeader.style.justifyContent.set(Justify.SpaceBetween);
        tabSelectionHeader.style.alignItems.set(Align.Stretch);

        return tabSelectionHeader;
    }

    private void refreshHeader(Player player) {
        tabSelectionHeader.removeAllChilds();

        // static marker selection
        if (s.enableStaticMarkers)
            tabSelectionHeader.addChild(createTab("TC_MENU_STATIC_MARKER", MarkerType.STATIC, player));
        // private marker selection
        if (GPSAccessPolicy.canUseNonStatic(player) && s.enablePrivateMarkers)
            tabSelectionHeader.addChild(createTab("TC_MENU_PRIVATE_MARKER", MarkerType.PRIVATE, player));
        // group marker selection
        if (GPSAccessPolicy.canUseNonStatic(player) && s.enableGroupMarkers)
            tabSelectionHeader.addChild(createTab("TC_MENU_GROUP_MARKER", MarkerType.GROUP, player));
        // global marker selection
        if (GPSAccessPolicy.canUseNonStatic(player) && s.enableGlobalMarkers)
            tabSelectionHeader.addChild(createTab("TC_MENU_GLOBAL_MARKER", MarkerType.GLOBAL, player));
        tabSelectionHeader.addChild(createServerWarpTab(player));
        // close button
        tabSelectionHeader.addChild(setupCloseTab(player));

    }

    private OZUIElement createTab(String labelKey, MarkerType type, Player player) {

        OZUIElement tab = new OZUIElement();
        tab.setSize(16.66f, 100, true);
        tab.setPivot(Pivot.UpperLeft);
        if (!serverWarpTab && type == currentMarkerType) {
            tab.setBackgroundColor(0.2f, 0.6f, 1.0f, 0.8f);
        } else {
            tab.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.6f);
        }
        tab.setClickable(true);
        tab.setClickAction(event -> {
            serverWarpTab = false;
            currentMarkerType = type;
            refreshHeader(player);
            refreshGrid(player);
            refreshCooldownStatus(player);
        });
        OZUIElement icon = new OZUIElement();
        icon.setSize(100, 70, true);
        icon.setPivot(Pivot.UpperLeft);
        icon.setPosition(0, 5, true);
        switch (type) {
            case PRIVATE:
                icon.style.backgroundImage.set(AssetManager.getIcon(player, "menu-marker-private"));
                break;
            case GROUP:
                icon.style.backgroundImage.set(AssetManager.getIcon(player, "menu-marker-group-alt"));
                break;
            case GLOBAL:
                icon.style.backgroundImage.set(AssetManager.getIcon(player, "menu-global-marker"));
                break;
            case STATIC:
                icon.style.backgroundImage.set(AssetManager.getIcon(player, "menu-marker-static"));
                break;
        }
        icon.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
        tab.addChild(icon);
        // label
        UILabel label = new UILabel(t().get(labelKey, player));
        label.setSize(100, 25, true);
        label.setFontSize(14);
        label.setPivot(Pivot.LowerLeft);
        label.setPosition(0, 100, true);
        label.setTextAlign(TextAnchor.MiddleCenter);
        tab.addChild(label);
        return tab;
    }

    private OZUIElement createServerWarpTab(Player player) {
        OZUIElement tab = new OZUIElement();
        tab.setSize(16.66f, 100, true); tab.setPivot(Pivot.UpperLeft);
        tab.setBackgroundColor(serverWarpTab ? .2f : .1f, serverWarpTab ? .6f : .1f, serverWarpTab ? 1f : .1f, serverWarpTab ? .8f : .6f);
        tab.setClickable(true);
        tab.setClickAction(event -> { serverWarpTab = true; refreshHeader(player); refreshGrid(player); refreshCooldownStatus(player); });
        OZUIElement icon = new OZUIElement();
        icon.setSize(100, 70, true); icon.setPivot(Pivot.UpperLeft); icon.setPosition(0, 5, true);
        icon.style.backgroundImage.set(AssetManager.getIcon(player, "menu-server-warp")); icon.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
        tab.addChild(icon);
        UILabel label = new UILabel(t().get("TC_MENU_SERVER_WARP", player));
        label.setSize(100, 25, true); label.setFontSize(14); label.setPivot(Pivot.LowerLeft); label.setPosition(0, 100, true); label.setTextAlign(TextAnchor.MiddleCenter);
        tab.addChild(label);
        return tab;
    }

    private UIElement setupGridScrollView(Player player) {
        gridScrollView = new UIScrollView(ScrollViewMode.Vertical);
        gridScrollView.setSize(100, 84, true);
        gridScrollView.setPivot(Pivot.UpperLeft);
        gridScrollView.setPosition(0, 10, true);
        gridScrollView.setBackgroundColor(0.08f, 0.08f, 0.08f, BODY_ALPHA);
        gridScrollView.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.48f);
        gridScrollView.style.borderTopWidth.set(2);
        return gridScrollView;
    }

    private UIElement setupCooldownStatus(Player player) {
        cooldownStatusBar = new UIElement();
        cooldownStatusBar.setSize(100, 6, true);
        cooldownStatusBar.setPivot(Pivot.LowerLeft);
        cooldownStatusBar.setPosition(0, 100, true);
        cooldownStatusBar.setBackgroundColor(0.06f, 0.06f, 0.06f, 0.74f);
        cooldownStatusBar.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.48f);
        cooldownStatusBar.style.borderTopWidth.set(2);

        cooldownStatusLabel = new UILabel("");
        cooldownStatusLabel.setSize(96, 100, true);
        cooldownStatusLabel.setPivot(Pivot.UpperLeft);
        cooldownStatusLabel.setPosition(2, 0, true);
        cooldownStatusLabel.style.width.set(60, Unit.Percent);
        cooldownStatusLabel.setFontSize(13);
        cooldownStatusLabel.setTextAlign(TextAnchor.MiddleLeft);
        cooldownStatusLabel.setTextWrap(false);
        cooldownStatusBar.addChild(cooldownStatusLabel);

        markerLimitStatusLabel = new UILabel("");
        markerLimitStatusLabel.setPivot(Pivot.UpperRight);
        markerLimitStatusLabel.setPosition(98, 0, true);
        markerLimitStatusLabel.style.width.set(36, Unit.Percent);
        markerLimitStatusLabel.style.height.set(100, Unit.Percent);
        markerLimitStatusLabel.setFontSize(13);
        markerLimitStatusLabel.setTextAlign(TextAnchor.MiddleRight);
        markerLimitStatusLabel.setTextWrap(false);
        cooldownStatusBar.addChild(markerLimitStatusLabel);

        refreshCooldownStatus(player);
        return cooldownStatusBar;
    }

    private void refreshGrid(Player player) {
        if (serverWarpTab) { refreshServerPins(player); return; }
        refreshGrid(currentMarkerType, player);
    }

    private void refreshServerPins(Player player) {
        gridScrollView.removeAllChilds();
        UIElement iconGrid = new UIElement();
        iconGrid.style.width.set(100, Unit.Percent); iconGrid.style.height.set(100, Unit.Percent);
        iconGrid.style.display.set(DisplayStyle.Flex); iconGrid.style.flexDirection.set(FlexDirection.Row); iconGrid.style.flexWrap.set(Wrap.Wrap); iconGrid.style.justifyContent.set(Justify.FlexStart);
        gridScrollView.addChild(iconGrid);
        if (player.isAdmin()) iconGrid.addChild(createAddMarkerCard(player, t().get("TC_MENU_ADD_SERVER_PIN", player), "gps-server-pin-create", ignored -> openServerPinEditor(player, null)));
        for (ServerPin pin : GPSDatabase.getInstance().getServerPins()) iconGrid.addChild(createServerPinCard(player, pin));
    }

    private UIElement createServerPinCard(Player player, ServerPin pin) {
        Callback<Boolean> onEdit = player.isAdmin() ? ignored -> openServerPinEditor(player, pin) : null;
        Callback<Boolean> onDelete = player.isAdmin() ? ignored -> {
            if (GPSDatabase.getInstance().deleteServerPin(pin.getId())) { player.sendTextMessage(t().get("TC_GPS_SERVER_PIN_DELETED", player).replace("PH_SERVER_PIN_NAME", pin.getName())); refreshServerPins(player); }
        } : null;
        return createMarkerCard(player, pin.getName(), AssetManager.getIcon(player, pin.getIcon()), onDelete, onEdit, ignored -> connectToServer(player, pin));
    }

    private void openServerPinEditor(Player player, ServerPin pin) {
        ServerPinOverlay overlay = new ServerPinOverlay(player, pin, saved -> {
            if (GPSDatabase.getInstance().saveServerPin(saved)) {
                player.sendTextMessage(t().get(pin == null ? "TC_GPS_SERVER_PIN_CREATED" : "TC_GPS_SERVER_PIN_UPDATED", player).replace("PH_SERVER_PIN_NAME", saved.getName()));
            } else player.sendTextMessage(t().get("TC_GPS_SERVER_PIN_ADDRESS_INVALID", player));
            resume(player); CursorManager.show(player); player.addUIElement(this); startCooldownTimer(player); refreshServerPins(player);
        });
        player.setAttribute("gps-ui-overlay", overlay); stopCooldownTimer(); player.removeUIElement(this); player.addUIElement(overlay);
    }

    private void connectToServer(Player player, ServerPin pin) {
        if (!ServerPinAddress.isValid(pin.getAddress())) { player.sendTextMessage(t().get("TC_GPS_SERVER_PIN_ADDRESS_INVALID", player)); return; }
        player.connectToOtherServer(pin.getAddress(), pin.getPassword(), success -> player.sendTextMessage(t().get(Boolean.TRUE.equals(success) ? "TC_GPS_SERVER_WARP_SUCCESS" : "TC_GPS_SERVER_WARP_FAILED", player).replace("PH_SERVER_PIN_NAME", pin.getName())));
    }

    private void refreshGrid(MarkerType type, Player uiPlayer) {
        gridScrollView.removeAllChilds();
        // Create icon grid
        UIElement iconGrid = new UIElement();
        // iconGrid.setBackgroundColor(0x44880099);
        iconGrid.style.width.set(100, Unit.Percent);
        iconGrid.style.height.set(100, Unit.Percent);
        iconGrid.style.display.set(DisplayStyle.Flex);
        iconGrid.style.flexDirection.set(FlexDirection.Row);
        iconGrid.style.flexWrap.set(Wrap.Wrap);
        iconGrid.style.justifyContent.set(Justify.FlexStart);

		gridScrollView.addChild(iconGrid);
		if (type != MarkerType.STATIC) addGPSAreaManagementCard(iconGrid, uiPlayer);

        if (!GPSAccessPolicy.canUse(uiPlayer, type)) {
            return;
        }

        List<Marker> markers = null;

        Vector3f lastDeathPosition = (Vector3f) uiPlayer.getAttribute("death-location");
        Vector3f lastPositionBeforePort = (Vector3f) uiPlayer.getAttribute("pre-port-location");
        Vector3f primarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Primary);
        Vector3f secondarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Secondary);
        Vector3f tertiarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Tertiary);
        Vector3f quaternarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Quaternary);
        Vector3f defaultSpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Default);
        String orderBy = GPSPlayerPreferences.markerSortOrder(uiPlayer);

        switch (type) {
            case PRIVATE:
                markers = GPSDatabase.getInstance().getPrivateMarkers(uiPlayer.getDbID(), orderBy);
                break;
            case GROUP:
                markers = GPSDatabase.getInstance().getGroupMarkers(uiPlayer.getPermissionGroup(), orderBy);
                break;
            case GLOBAL:
                markers = GPSDatabase.getInstance().getGlobalMarkers(orderBy);
                break;
            case STATIC:
                if (primarySpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, costLabel(uiPlayer, t().get("TC_MENU_STATIC_PRIMARY_SPAWN", uiPlayer), primarySpawnPos, MarkerType.STATIC),
                            AssetManager.getIcon(uiPlayer, "marker-sleep-restroom"), null, null, onTeleport -> {
                                executeGridTeleport(uiPlayer, primarySpawnPos,
                                        t().get("TC_MENU_STATIC_PRIMARY_SPAWN", uiPlayer), MarkerType.STATIC);
                            }));
                if (secondarySpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, costLabel(uiPlayer, t().get("TC_MENU_STATIC_SECONDARY_SPAWN", uiPlayer), secondarySpawnPos, MarkerType.STATIC),
                            AssetManager.getIcon(uiPlayer, "marker-sleep-tent"), null, null, onTeleport -> {
                                executeGridTeleport(uiPlayer, secondarySpawnPos,
                                        t().get("TC_MENU_STATIC_SECONDARY_SPAWN", uiPlayer), MarkerType.STATIC);
                            }));
                if (tertiarySpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, costLabel(uiPlayer, t().get("TC_MENU_STATIC_TERTIARY_SPAWN", uiPlayer), tertiarySpawnPos, MarkerType.STATIC),
                            AssetManager.getIcon(uiPlayer, "marker-sleep-king-size-bed"), null, null, onTeleport -> {
                                executeGridTeleport(uiPlayer, tertiarySpawnPos,
                                        t().get("TC_MENU_STATIC_TERTIARY_SPAWN", uiPlayer), MarkerType.STATIC);
                            }));
                if (quaternarySpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, costLabel(uiPlayer, t().get("TC_MENU_STATIC_QUATERNARY_SPAWN", uiPlayer), quaternarySpawnPos, MarkerType.STATIC),
                            AssetManager.getIcon(uiPlayer, "marker-sleep-sign"), null, null, onTeleport -> {
                                executeGridTeleport(uiPlayer, quaternarySpawnPos,
                                        t().get("TC_MENU_STATIC_QUATERNARY_SPAWN", uiPlayer), MarkerType.STATIC);
                            }));
                if (defaultSpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, costLabel(uiPlayer, t().get("TC_MENU_STATIC_DEFAULT_SPAWN", uiPlayer), defaultSpawnPos, MarkerType.STATIC),
                            AssetManager.getIcon(uiPlayer, "marker-coast-jetty"), null, null, onTeleport -> {
                                executeGridTeleport(uiPlayer, defaultSpawnPos,
                                        t().get("TC_MENU_STATIC_DEFAULT_SPAWN", uiPlayer), MarkerType.STATIC);
                            }));
                if (lastDeathPosition != null)
                    iconGrid.addChild(
                            createMarkerCard(uiPlayer, costLabel(uiPlayer, t().get("TC_MENU_STATIC_DEATHPORT", uiPlayer), lastDeathPosition, MarkerType.STATIC),
                                    AssetManager.getIcon(uiPlayer, "icon-ki-death-skull"), null, null, onTeleport -> {
                                        executeGridTeleport(uiPlayer, lastDeathPosition,
                                                t().get("TC_MENU_STATIC_DEATHPORT", uiPlayer), MarkerType.STATIC,
                                                false);
                                    }));
                if (lastPositionBeforePort != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer,
                            costLabel(uiPlayer, t().get("TC_MENU_STATIC_BACKPORT", uiPlayer), lastPositionBeforePort, MarkerType.STATIC),
                            AssetManager.getIcon(uiPlayer, "marker-special-destination"), null, null, onTeleport -> {
                                executeGridTeleport(uiPlayer, lastPositionBeforePort,
                                        t().get("TC_MENU_STATIC_BACKPORT", uiPlayer), MarkerType.STATIC, false);
                            }));
                break;
            default:
                break;
        }

        switch (type) {
            case GLOBAL:
                if (uiPlayer.isAdmin()) {
                    iconGrid.addChild(createAddMarkerCard(uiPlayer, t().get("TC_MENU_ADD_MARKER_GLOBAL", uiPlayer),
                            onCreateNewMarker -> {
                                CreateMarkerOverlay overlay = new CreateMarkerOverlay(uiPlayer, MarkerType.GLOBAL,
                                        createdMarker -> {
                                            GPSDatabase.getInstance().saveMarker(createdMarker);
                                            uiPlayer.sendTextMessage(t().get("TC_GPS_GLOBAL_CREATED", uiPlayer)
                                                    .replace("PH_MARKER_NAME", createdMarker.getName())
                                                    .replace("PH_MARKER_POS", createdMarker.getPosition() + ""));
                                            resume(uiPlayer);
                                            CursorManager.show(uiPlayer);
                                            uiPlayer.addUIElement(this);
                                            startCooldownTimer(uiPlayer);
                                            refreshGrid(uiPlayer);
                                        });

                                uiPlayer.setAttribute("gps-ui-overlay", overlay);
                                stopCooldownTimer();
                                uiPlayer.removeUIElement(this);
                                uiPlayer.addUIElement(overlay);
                            }));
                }
                break;
            case GROUP:
                if (GPSAreaAccessPolicy.markerCreationDenialKey(uiPlayer, MarkerType.GROUP) == null) iconGrid.addChild(createAddMarkerCard(uiPlayer, addMarkerLabel(uiPlayer, t().get("TC_MENU_ADD_MARKER_GROUP", uiPlayer), MarkerType.GROUP),
                        onCreateNewMarker -> {
                            if (!canCreateMarker(uiPlayer, MarkerType.GROUP)) {
                                return;
                            }
                            CreateMarkerOverlay overlay = new CreateMarkerOverlay(uiPlayer, MarkerType.GROUP,
                                    uiPlayer.getPermissionGroup(), createdMarker -> {
                                        if (!saveMarkerWithEconomy(uiPlayer, createdMarker)) {
                                            return;
                                        }
                                        resume(uiPlayer);
                                        CursorManager.show(uiPlayer);
                                        uiPlayer.addUIElement(this);
                                        startCooldownTimer(uiPlayer);
                                        refreshGrid(uiPlayer);
                                    });

                            uiPlayer.setAttribute("gps-ui-overlay", overlay);
                            stopCooldownTimer();
                            uiPlayer.removeUIElement(this);
                            uiPlayer.addUIElement(overlay);
                        }));
                break;
            case PRIVATE:
                if (GPSAreaAccessPolicy.markerCreationDenialKey(uiPlayer, MarkerType.PRIVATE) == null) iconGrid.addChild(createAddMarkerCard(uiPlayer, addMarkerLabel(uiPlayer, t().get("TC_MENU_ADD_MARKER_PRIVATE", uiPlayer), MarkerType.PRIVATE),
                        onCreateNewMarker -> {
                            if (!canCreateMarker(uiPlayer, MarkerType.PRIVATE)) {
                                return;
                            }
                            CreateMarkerOverlay overlay = new CreateMarkerOverlay(uiPlayer, MarkerType.PRIVATE,
                                    createdMarker -> {
                                        if (!saveMarkerWithEconomy(uiPlayer, createdMarker)) {
                                            return;
                                        }
                                        resume(uiPlayer);
                                        CursorManager.show(uiPlayer);
                                        uiPlayer.addUIElement(this);
                                        startCooldownTimer(uiPlayer);
                                        refreshGrid(uiPlayer);
                                    });

                            uiPlayer.setAttribute("gps-ui-overlay", overlay);
                            stopCooldownTimer();
                            uiPlayer.removeUIElement(this);
                            uiPlayer.addUIElement(overlay);
                        }));
                break;
            default:
                break;
        }

        if (markers != null) {
            for (Marker marker : markers) {
                iconGrid.addChild(createMarkerCardFromMarker(marker, uiPlayer));
            }
        }
    }

	private void addGPSAreaManagementCard(UIElement iconGrid, Player player) {
		Area area = player.getCurrentArea();
		if (!player.isAdmin() || !GPSAreaAccessPolicy.areaFeaturesEnabled() || area == null) {
			return;
		}
		boolean marked = GPSDatabase.getInstance().isGPSAreaAllowed(area.getID());
		String label = t().get(marked ? "TC_GPS_AREA_UNMARK" : "TC_GPS_AREA_MARK", player)
				.replace("PH_AREA_NAME", area.getName() == null ? String.valueOf(area.getID()) : area.getName());
		iconGrid.addChild(createMarkerCard(player, label,
				AssetManager.getIcon(player, "marker-special-destination"), null, null, ignored -> {
					boolean updated = marked ? GPSDatabase.getInstance().deleteGPSArea(area.getID())
							: GPSDatabase.getInstance().saveGPSArea(area.getID(), player.getDbID(), true);
					if (updated) {
						player.sendTextMessage(t().get(marked ? "TC_GPS_AREA_UNMARKED" : "TC_GPS_AREA_MARKED", player));
						refreshGrid(player);
					}
				}));
	}

    private UIElement createMarkerCardFromMarker(Marker marker, Player player) {
        Callback<Boolean> onDeleteCallback = onDelete -> {
            deleteMarker(player, marker, onDeleted -> refreshGrid(player));
        };
        Callback<Boolean> onEditCallback = onEdit -> {
            CreateMarkerOverlay overlay = new CreateMarkerOverlay(player, marker, editedMarker -> {
                if (GPSDatabase.getInstance().updateMarkerDetails(marker, player, editedMarker.getName(),
                        editedMarker.getIcon())) {
                    player.sendTextMessage(t().get("TC_GPS_MARKER_UPDATED", player)
                            .replace("PH_MARKER_NAME", editedMarker.getName()));
                }
                resume(player);
                CursorManager.show(player);
                player.addUIElement(this);
                startCooldownTimer(player);
                refreshGrid(player);
            });

            player.setAttribute("gps-ui-overlay", overlay);
            stopCooldownTimer();
            player.removeUIElement(this);
            player.addUIElement(overlay);
        };
        if (!MarkerPermissions.canManage(player, marker)) {
            onDeleteCallback = null;
            onEditCallback = null;
        }
        String restriction = GPSAreaAccessPolicy.teleportDenialKey(player, marker.getPosition(), marker.getType());
        if (restriction != null) {
            return createMarkerCard(player, marker.getName() + "\n" + t().get(restriction, player),
                    AssetManager.getIcon(player, marker.getIcon()), null, null, null);
        }
        return createMarkerCard(player, costLabel(player, marker.getName(), marker.getPosition(), marker.getType()), AssetManager.getIcon(player, marker.getIcon()), onDeleteCallback, onEditCallback, onTeleport -> {
                    executeGridTeleport(player, marker.getPosition(), marker.getName(), marker.getType());
                });
    }

    private UIElement createMarkerCard(Player player, String name, TextureAsset icon, Callback<Boolean> onDelete,
            Callback<Boolean> onEdit, Callback<Boolean> onTeleport) {
		boolean staticAreaBlocked = currentMarkerType == MarkerType.STATIC
				&& GPSAreaAccessPolicy.gpsUseBlocked(player, MarkerType.STATIC);
		String label = staticAreaBlocked ? name + "\n" + t().get("TC_GPS_AREA_REQUIRED", player) : name;
		Callback<Boolean> teleportAction = staticAreaBlocked ? null : onTeleport;

        UIElement card = new UIElement();
        card.setSize(250 * scaleFactor, 300 * scaleFactor, false);
        card.setPivot(Pivot.UpperLeft);
        card.setBackgroundColor(teleportAction == null ? 0.38f : 0.14f, teleportAction == null ? 0.08f : 0.13f,
                teleportAction == null ? 0.08f : 0.12f, CARD_ALPHA);
        card.setBorder(1);
        card.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.26f);
        card.setBorderEdgeRadius(6, false);
        int margin = cardMargin;
        card.style.marginBottom.set(margin, Unit.Pixel);
        card.style.marginTop.set(margin, Unit.Pixel);
        card.style.marginLeft.set(margin, Unit.Pixel);
        card.style.marginRight.set(margin, Unit.Pixel);

        // marker icon + teleport click action
        OZUIElement markerIcon = new OZUIElement();
        markerIcon.setSize(240 * scaleFactor, 240 * scaleFactor, false);
        markerIcon.setPivot(Pivot.UpperLeft);
        markerIcon.setPosition(5 * scaleFactor, 5 * scaleFactor, false);
        markerIcon.style.backgroundImage.set(icon);
        markerIcon.setClickable(teleportAction != null);
        if (teleportAction != null) markerIcon.setClickAction(event -> teleportAction.onCall(true));
        card.addChild(markerIcon);

        // marker edit/delete actions
        if (onEdit != null) {
            AdvancedButton editButton = AdvancedButtonFactory.defaultButton("", event -> {
                onEdit.onCall(true);
            });
            editButton.setSize(30 * scaleFactor, 30 * scaleFactor, false);
            editButton.setPivot(Pivot.LowerRight);
            editButton.setPosition(210 * scaleFactor, 295 * scaleFactor, false);
            editButton.setBorderEdgeRadius(5, false);
            editButton.setBorder(1);
            editButton.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.32f);
            editButton.style.paddingBottom.set(5);
            editButton.style.paddingTop.set(5);
            editButton.style.paddingLeft.set(5);
            editButton.style.paddingRight.set(5);
            editButton.setSurfaceIcon(AssetManager.getIcon(player, "gps-marker-edit"));
            card.addChild(editButton);
        }
        if (onDelete != null) {
            AdvancedButton deleteButton = AdvancedButtonFactory.danger("", event -> {
                onDelete.onCall(true);
            });
            deleteButton.setSize(30 * scaleFactor, 30 * scaleFactor, false);
            deleteButton.setPivot(Pivot.LowerRight);
            deleteButton.setPosition(245 * scaleFactor, 295 * scaleFactor, false);
            deleteButton.setBorderEdgeRadius(5, false);
            deleteButton.setBorder(1);
            deleteButton.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.32f);
            deleteButton.style.paddingBottom.set(5);
            deleteButton.style.paddingTop.set(5);
            deleteButton.style.paddingLeft.set(5);
            deleteButton.style.paddingRight.set(5);
            deleteButton.setSurfaceIcon(AssetManager.getIcon(player, "gps-marker-delete"));
            card.addChild(deleteButton);
        }

        // marker label
        UILabel markerLabel = new UILabel(label);
        markerLabel.setSize((onEdit != null || onDelete != null ? 170 : 210) * scaleFactor, 40 * scaleFactor, false);
        markerLabel.setFontSize(14 * scaleFactor);
        markerLabel.setTextAlign(TextAnchor.UpperLeft);
        markerLabel.setPivot(Pivot.LowerLeft);
        markerLabel.setPosition(5 * scaleFactor, 295 * scaleFactor, false);
        markerLabel.setTextWrap(true);
        card.addChild(markerLabel);
        return card;
    }

    private String costLabel(Player player, String name, Vector3f position, MarkerType type) {
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy == null) {
            return name;
        }
        long cost = economy.teleportCost(player, position, type);
        return cost <= 0 ? name : name + "\n" + t().get("TC_GPS_COST_LABEL", player)
                .replace("PH_COST", economy.costLabel(cost, economy.teleportCurrency(type)));
    }

    private String addMarkerLabel(Player player, String label, MarkerType type) {
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy == null) {
            return label;
        }
        long cost = economy.markerCreateCost(type);
        String result = cost <= 0 ? label : label + "\n" + t().get("TC_GPS_COST_LABEL", player)
                .replace("PH_COST", economy.costLabel(cost, s.markerCreateCostCurrencyIdentifier));
        return result;
    }

    private boolean canCreateMarker(Player player, MarkerType type) {
        if (!GPSAccessPolicy.canUse(player, type)) {
            player.sendTextMessage(t().get("TC_GPS_PLAYTIME_REQUIRED", player)
                    .replace("PH_REQUIRED_MINUTES", String.valueOf(GPSAccessPolicy.requiredMinutes()))
                    .replace("PH_REMAINING_MINUTES", String.valueOf(GPSAccessPolicy.remainingMinutes(player))));
            return false;
        }
		String restrictionDenialKey = GPSAreaAccessPolicy.markerCreationDenialKey(player, type);
		if (restrictionDenialKey != null) {
			player.sendTextMessage(t().get(restrictionDenialKey, player));
			return false;
		}
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy != null && economy.markerLimitReached(player, type)) {
            player.sendTextMessage(t().get("TC_GPS_MARKER_LIMIT_REACHED", player)
                    .replace("PH_LIMIT", String.valueOf(economy.markerLimit(type))));
            return false;
        }
        return true;
    }

    private boolean saveMarkerWithEconomy(Player player, Marker marker) {
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy != null) {
            GPSEconomy.EconomyResult charge = economy.chargeMarkerCreation(player, marker.getType());
            if (!charge.success()) {
                player.sendTextMessage(t().get("TC_GPS_ECONOMY_FAILED", player)
                        .replace("PH_MESSAGE", charge.message()));
                return false;
            }
            if (!charge.message().isBlank()) {
                player.sendTextMessage(t().get("TC_GPS_COST_CHARGED", player)
                        .replace("PH_COST", charge.message()));
            }
        }
        GPSDatabase.getInstance().saveMarker(marker);
        String key = marker.getType() == MarkerType.GROUP ? "TC_GPS_GROUP_CREATED" : "TC_GPS_PRIVATE_CREATED";
        player.sendTextMessage(t().get(key, player)
                .replace("PH_MARKER_NAME", marker.getName())
                .replace("PH_MARKER_POS", marker.getPosition() + ""));
        return true;
    }

    private void deleteMarker(Player player, Marker marker, Callback<Boolean> onDeleted) {
        if (!GPSPlayerPreferences.confirmMarkerDelete(player)) {
            performDelete(player, marker, onDeleted);
            return;
        }

        ConfirmMarkerDeleteOverlay confirmOverlay = new ConfirmMarkerDeleteOverlay(player, marker,
                dontAskAgain -> performDelete(player, marker, onDeleted),
                ignored -> {
                });
        player.addUIElement(confirmOverlay);
    }

    private void performDelete(Player player, Marker marker, Callback<Boolean> onDeleted) {
        if (GPSDatabase.getInstance().deleteMarker(marker, player)) {
            player.sendTextMessage(t().get("TC_GPS_DELETED", player).replace("PH_MARKER_NAME", marker.getName()));
            onDeleted.onCall(true);
        }
    }

    private OZUIElement createAddMarkerCard(Player player, String labelText, Callback<Boolean> onCreateNewMarker) {
        return createAddMarkerCard(player, labelText, "gps-marker-create", onCreateNewMarker);
    }

    private OZUIElement createAddMarkerCard(Player player, String labelText, String iconKey, Callback<Boolean> onCreateNewMarker) {
        OZUIElement card = new OZUIElement();
        card.setSize(250 * scaleFactor, 300 * scaleFactor, false);
        card.setPivot(Pivot.UpperLeft);
        card.setBackgroundColor(0.14f, 0.13f, 0.12f, CARD_ALPHA);
        card.setBorder(1);
        card.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.26f);
        card.setBorderEdgeRadius(6, false);
        int margin = cardMargin;
        card.style.marginBottom.set(margin, Unit.Pixel);
        card.style.marginTop.set(margin, Unit.Pixel);
        card.style.marginLeft.set(margin, Unit.Pixel);
        card.style.marginRight.set(margin, Unit.Pixel);

        // add marker icon + click action
        OZUIElement addIcon = new OZUIElement();
        addIcon.setSize(240 * scaleFactor, 240 * scaleFactor, false);
        addIcon.setPivot(Pivot.UpperLeft);
        addIcon.setPosition(5 * scaleFactor, 5 * scaleFactor, false);
        addIcon.style.backgroundImage.set(AssetManager.getIcon(player, iconKey));
        addIcon.setClickable(true);
        addIcon.setClickAction(event -> {
            onCreateNewMarker.onCall(true);
        });
        card.addChild(addIcon);

        // add marker label
        UILabel addLabel = new UILabel(labelText);
        addLabel.setSize(210 * scaleFactor, 40 * scaleFactor, false);
        addLabel.setFontSize(14 * scaleFactor);
        addLabel.setTextAlign(TextAnchor.UpperLeft);
        addLabel.setPivot(Pivot.LowerLeft);
        addLabel.setPosition(5 * scaleFactor, 295 * scaleFactor, false);
        addLabel.setTextWrap(true);
        card.addChild(addLabel);
        return card;
    }

    private UIElement setupCloseTab(Player player) {
        OZUIElement tab = new OZUIElement();
        tab.setSize(16.66f, 100, true);
        tab.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.6f);
        tab.setPivot(Pivot.UpperLeft);
        tab.setClickable(true);
        tab.setClickAction(event -> {
            close(event.getPlayer());
        });
        // icon
        OZUIElement icon = new OZUIElement();
        icon.setSize(100, 70, true);
        icon.setPivot(Pivot.UpperLeft);
        icon.setPosition(0, 5, true);
        icon.style.backgroundImage.set(AssetManager.getIcon(player, "menu-exit"));
        icon.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
        tab.addChild(icon);
        // label
        UILabel label = new UILabel(t().get("TC_BTN_CLOSE", player));
        label.setSize(100, 25, true);
        label.setFontSize(14);
        label.setPivot(Pivot.LowerLeft);
        label.setPosition(0, 100, true);
        label.setTextAlign(TextAnchor.MiddleCenter);
        tab.addChild(label);

        return tab;
    }

    private void executeGridTeleport(Player player, Vector3f position, String label, MarkerType type) {
        executeGridTeleport(player, position, label, type, true);
    }

    private void executeGridTeleport(Player player, Vector3f position, String label, MarkerType type,
            boolean saveLastPosition) {
        if (!GPSEventUtils.executeTeleport(player, position, label, type, saveLastPosition)) {
            refreshCooldownStatus(player);
            return;
        }
        close(player);
    }

    private void startCooldownTimer(Player player) {
        stopCooldownTimer();
        cooldownTimer = new Timer(1, 0, -1, () -> {
            if (!player.isConnected()) {
                stopCooldownTimer();
                return;
            }
            boolean nonStaticAccess = GPSAccessPolicy.canUseNonStatic(player);
            if (nonStaticAccess != previousNonStaticAccess) {
                previousNonStaticAccess = nonStaticAccess;
                refreshHeader(player);
                refreshGrid(player);
            }
            refreshCooldownStatus(player);
        });
        cooldownTimer.start();
    }

    private void stopCooldownTimer() {
        if (cooldownTimer != null) {
            cooldownTimer.kill();
            cooldownTimer = null;
        }
    }

    public void close(Player player) {
        stopCooldownTimer();
        player.removeUIElement(this);
        player.deleteAttribute("gps-ui-overlay");
        CursorManager.hide(player);
    }

    private void resume(Player player) {
        player.setAttribute("gps-ui-overlay", this);
    }

    private void refreshCooldownStatus(Player player) {
        if (cooldownStatusLabel == null || markerLimitStatusLabel == null || cooldownStatusBar == null) {
            return;
        }

        if (serverWarpTab) { cooldownStatusBar.setVisible(false); gridScrollView.setSize(100, 90, true); return; }
        String markerType = t().get(TeleportCooldowns.displayTypeKey(currentMarkerType), player);
        if (!GPSAccessPolicy.canUseNonStatic(player)) {
            cooldownStatusBar.setVisible(true);
            gridScrollView.setSize(100, 84, true);
            cooldownStatusLabel.style.width.set(96, Unit.Percent);
            cooldownStatusLabel.setText(t().get("TC_GPS_PLAYTIME_STATUS", player)
                    .replace("PH_REQUIRED_MINUTES", String.valueOf(GPSAccessPolicy.requiredMinutes()))
                    .replace("PH_REMAINING_MINUTES", String.valueOf(GPSAccessPolicy.remainingMinutes(player))));
            markerLimitStatusLabel.setText("");
            return;
        }
		if (GPSAreaAccessPolicy.gpsUseBlocked(player, currentMarkerType)) {
			cooldownStatusBar.setVisible(true);
			gridScrollView.setSize(100, 84, true);
			cooldownStatusLabel.style.width.set(96, Unit.Percent);
			cooldownStatusLabel.setText(t().get("TC_GPS_AREA_REQUIRED", player));
			markerLimitStatusLabel.setText("");
			return;
		}
        cooldownStatusLabel.style.width.set(60, Unit.Percent);
        String limitText = markerLimitStatus(player);
        boolean cooldownEnabled = TeleportCooldowns.isEnabled(currentMarkerType);
        boolean limitVisible = !limitText.isBlank();

        if (!cooldownEnabled && !limitVisible) {
            cooldownStatusBar.setVisible(false);
            gridScrollView.setSize(100, 90, true);
            return;
        }

        cooldownStatusBar.setVisible(true);
        gridScrollView.setSize(100, 84, true);
        markerLimitStatusLabel.setText(limitText);

        if (!cooldownEnabled) {
            cooldownStatusLabel.setText("");
            return;
        }

        int remainingSeconds = TeleportCooldowns.remainingSeconds(player, currentMarkerType);
        if (remainingSeconds > 0) {
            cooldownStatusLabel.setText(t().get("TC_GPS_COOLDOWN_STATUS", player)
                    .replace("PH_MARKER_TYPE", markerType)
                    .replace("PH_SECONDS", String.valueOf(remainingSeconds)));
            return;
        }

        String ready = t().get("TC_GPS_COOLDOWN_READY", player).replace("PH_MARKER_TYPE", markerType);
		if (GPSAreaAccessPolicy.sectorRestrictionEnabled(currentMarkerType)) {
			ready += "\n" + t().get("TC_GPS_SECTOR_HINT", player);
		}
        cooldownStatusLabel.setText(ready);
    }

    private String markerLimitStatus(Player player) {
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy == null) {
            return "";
        }
        int limit = economy.markerLimit(currentMarkerType);
        if (limit < 0) {
            return "";
        }
        int current = economy.markerCount(player, currentMarkerType);
        return t().get("TC_GPS_LIMIT_STATUS", player)
                .replace("PH_CURRENT", String.valueOf(current))
                .replace("PH_LIMIT", String.valueOf(limit));
    }
}
