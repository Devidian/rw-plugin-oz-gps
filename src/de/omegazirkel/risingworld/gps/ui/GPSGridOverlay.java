package de.omegazirkel.risingworld.gps.ui;

import java.util.List;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.GPSDatabase;
import de.omegazirkel.risingworld.gps.GPSEventUtils;
import de.omegazirkel.risingworld.gps.Marker;
import de.omegazirkel.risingworld.gps.MarkerType;
import de.omegazirkel.risingworld.gps.PluginSettings;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.AssetManager;
import de.omegazirkel.risingworld.tools.ui.BaseButton;
import de.omegazirkel.risingworld.tools.ui.ButtonFactory;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
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

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    private UIElement mainViewPanel = null;
    private UIElement tabSelectionHeader = null;
    private UIScrollView gridScrollView = null;
    private MarkerType currentMarkerType = MarkerType.PRIVATE;

    private static final float scaleFactor = 0.75f;
    private static final Integer markerPerPage = 19;
    private static final Integer cardMargin = 5;

    public GPSGridOverlay(Player player) {
        super();
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
        setBackgroundColor(0, 0, 0, 0.4f);

        setupMainView(player);
        refreshGrid(player);
        refreshHeader(player);
    }

    private void setupMainView(Player player) {
        mainViewPanel = new UIElement();
        mainViewPanel.setSize(75, 75, true);
        mainViewPanel.setPivot(Pivot.MiddleCenter);
        mainViewPanel.setPosition(50, 50, true);
        mainViewPanel.setBackgroundColor(0, 0, 0, 0.85f);
        mainViewPanel.setBorderColor(1, 1, 1, 0.4f);
        mainViewPanel.setBorder(2);
        mainViewPanel.addChild(setupTabSelectionHeader(player));
        mainViewPanel.addChild(setupGridScrollView(player));
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
        if (s.enablePrivateMarkers)
            tabSelectionHeader.addChild(createTab("TC_MENU_PRIVATE_MARKER", MarkerType.PRIVATE, player));
        // group marker selection
        if (s.enableGroupMarkers)
            tabSelectionHeader.addChild(createTab("TC_MENU_GROUP_MARKER", MarkerType.GROUP, player));
        // global marker selection
        if (s.enableGlobalMarkers)
            tabSelectionHeader.addChild(createTab("TC_MENU_GLOBAL_MARKER", MarkerType.GLOBAL, player));
        // close button
        tabSelectionHeader.addChild(setupCloseTab(player));

    }

    private OZUIElement createTab(String labelKey, MarkerType type, Player player) {

        OZUIElement tab = new OZUIElement();
        tab.setSize(20, 100, true);
        tab.setPivot(Pivot.UpperLeft);
        if (type == currentMarkerType) {
            tab.setBackgroundColor(0.2f, 0.6f, 1.0f, 0.8f);
        } else {
            tab.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.6f);
        }
        tab.setClickable(true);
        tab.setClickAction(event -> {
            currentMarkerType = type;
            refreshHeader(player);
            refreshGrid(player);
        });
        OZUIElement icon = new OZUIElement();
        icon.setSize(100, 70, true);
        icon.setPivot(Pivot.UpperLeft);
        icon.setPosition(0, 5, true);
        switch (type) {
            case PRIVATE:
                icon.style.backgroundImage.set(AssetManager.getIcon("icon-ki-gps-private"));
                break;
            case GROUP:
                icon.style.backgroundImage.set(AssetManager.getIcon("icon-ki-gps-group-alt"));
                break;
            case GLOBAL:
                icon.style.backgroundImage.set(AssetManager.getIcon("icon-ki-gps-global"));
                break;
            case STATIC:
                icon.style.backgroundImage.set(AssetManager.getIcon("icon-ki-gps-static"));
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

    private UIElement setupGridScrollView(Player player) {
        gridScrollView = new UIScrollView(ScrollViewMode.Vertical);
        gridScrollView.setSize(100, 90, true);
        gridScrollView.setPivot(Pivot.UpperLeft);
        gridScrollView.setPosition(0, 10, true);
        gridScrollView.setBorderColor(1, 1, 1, 0.4f);
        gridScrollView.style.borderTopWidth.set(2);
        return gridScrollView;
    }

    private void refreshGrid(Player player) {
        refreshGrid(currentMarkerType, player, 0);
    }

    private void refreshGrid(MarkerType type, Player uiPlayer, Integer page) {
        gridScrollView.removeAllChilds();
        // Create icon grid
        UIElement iconGrid = new UIElement();
        // iconGrid.setBackgroundColor(0x44880099);
        iconGrid.style.width.set(100, Unit.Percent);
        iconGrid.style.height.set(100, Unit.Percent);
        iconGrid.style.display.set(DisplayStyle.Flex);
        iconGrid.style.flexDirection.set(FlexDirection.Row);
        iconGrid.style.flexWrap.set(Wrap.Wrap);
        iconGrid.style.justifyContent.set(Justify.Center);

        gridScrollView.addChild(iconGrid);

        List<Marker> markers = null;

        Vector3f lastDeathPosition = (Vector3f) uiPlayer.getAttribute("death-location");
        Vector3f lastPositionBeforePort = (Vector3f) uiPlayer.getAttribute("pre-port-location");
        Vector3f primarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Primary);
        Vector3f secondarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Secondary);
        Vector3f tertiarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Tertiary);
        Vector3f quaternarySpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Quaternary);
        Vector3f defaultSpawnPos = uiPlayer.getSpawnPosition(SpawnPointType.Default);

        switch (type) {
            case PRIVATE:
                markers = GPSDatabase.getInstance().getPrivateMarker(uiPlayer.getDbID(), page, markerPerPage);
                break;
            case GROUP:
                markers = GPSDatabase.getInstance().getGroupMarker(uiPlayer.getPermissionGroup(), page, markerPerPage);
                break;
            case GLOBAL:
                markers = GPSDatabase.getInstance().getGlobalMarker(page, markerPerPage);
                break;
            case STATIC:
                if (primarySpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, t().get("TC_MENU_STATIC_PRIMARY_SPAWN", uiPlayer),
                            AssetManager.getIcon("icon-ki-sleep-01"), null, onTeleport -> {
                                uiPlayer.setAttribute("pre-port-location", uiPlayer.getPosition());
                                uiPlayer.setPosition(primarySpawnPos);// .
                                GPSEventUtils.onStaticGPSEvent(uiPlayer,
                                        t().get("TC_MENU_STATIC_PRIMARY_SPAWN", uiPlayer), primarySpawnPos);
                                CursorManager.hide(uiPlayer);
                                uiPlayer.removeUIElement(this);
                            }));
                if (secondarySpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, t().get("TC_MENU_STATIC_SECONDARY_SPAWN", uiPlayer),
                            AssetManager.getIcon("icon-ki-sleep-02"), null, onTeleport -> {
                                uiPlayer.setAttribute("pre-port-location", uiPlayer.getPosition());
                                uiPlayer.setPosition(secondarySpawnPos);// .
                                GPSEventUtils.onStaticGPSEvent(uiPlayer,
                                        t().get("TC_MENU_STATIC_SECONDARY_SPAWN", uiPlayer), secondarySpawnPos);
                                CursorManager.hide(uiPlayer);
                                uiPlayer.removeUIElement(this);
                            }));
                if (tertiarySpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, t().get("TC_MENU_STATIC_TERTIARY_SPAWN", uiPlayer),
                            AssetManager.getIcon("icon-ki-sleep-03"), null, onTeleport -> {
                                uiPlayer.setAttribute("pre-port-location", uiPlayer.getPosition());
                                uiPlayer.setPosition(tertiarySpawnPos);// .
                                GPSEventUtils.onStaticGPSEvent(uiPlayer,
                                        t().get("TC_MENU_STATIC_TERTIARY_SPAWN", uiPlayer), tertiarySpawnPos);
                                CursorManager.hide(uiPlayer);
                                uiPlayer.removeUIElement(this);
                            }));
                if (quaternarySpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, t().get("TC_MENU_STATIC_QUATERNARY_SPAWN", uiPlayer),
                            AssetManager.getIcon("icon-ki-sleep-04"), null, onTeleport -> {
                                uiPlayer.setAttribute("pre-port-location", uiPlayer.getPosition());
                                uiPlayer.setPosition(quaternarySpawnPos);// .
                                GPSEventUtils.onStaticGPSEvent(uiPlayer,
                                        t().get("TC_MENU_STATIC_QUATERNARY_SPAWN", uiPlayer), quaternarySpawnPos);
                                CursorManager.hide(uiPlayer);
                                uiPlayer.removeUIElement(this);
                            }));
                if (defaultSpawnPos != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer, t().get("TC_MENU_STATIC_DEFAULT_SPAWN", uiPlayer),
                            AssetManager.getIcon("icon-ki-coast-01"), null, onTeleport -> {
                                uiPlayer.setAttribute("pre-port-location", uiPlayer.getPosition());
                                uiPlayer.setPosition(defaultSpawnPos);// .
                                GPSEventUtils.onStaticGPSEvent(uiPlayer,
                                        t().get("TC_MENU_STATIC_DEFAULT_SPAWN", uiPlayer), defaultSpawnPos);
                                CursorManager.hide(uiPlayer);
                                uiPlayer.removeUIElement(this);
                            }));
                if (lastDeathPosition != null)
                    iconGrid.addChild(
                            createMarkerCard(uiPlayer, t().get("TC_MENU_STATIC_DEATHPORT", uiPlayer),
                                    AssetManager.getIcon("icon-ki-death-skull"), null, onTeleport -> {
                                        uiPlayer.setAttribute("icon-ki-sleep-05", uiPlayer.getPosition());
                                        uiPlayer.setPosition(lastDeathPosition);// .
                                        GPSEventUtils.onStaticGPSEvent(uiPlayer,
                                                t().get("TC_MENU_STATIC_DEATHPORT", uiPlayer),
                                                lastDeathPosition);
                                        CursorManager.hide(uiPlayer);
                                        uiPlayer.removeUIElement(this);
                                    }));
                if (lastPositionBeforePort != null)
                    iconGrid.addChild(createMarkerCard(uiPlayer,
                            t().get("TC_MENU_STATIC_BACKPORT", uiPlayer),
                            AssetManager.getIcon("icon-ki-special-01"), null, onTeleport -> {
                                uiPlayer.setAttribute("pre-port-location", uiPlayer.getPosition());
                                uiPlayer.setPosition(lastPositionBeforePort);// .
                                GPSEventUtils.onStaticGPSEvent(uiPlayer,
                                        t().get("TC_MENU_STATIC_BACKPORT", uiPlayer),
                                        lastPositionBeforePort);
                                CursorManager.hide(uiPlayer);
                                uiPlayer.removeUIElement(this);
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
                                            uiPlayer.setAttribute("gps-ui-overlay", this);
                                            CursorManager.show(uiPlayer);
                                            uiPlayer.addUIElement(this);
                                            refreshGrid(uiPlayer);
                                        });

                                uiPlayer.setAttribute("gps-ui-overlay", overlay);
                                uiPlayer.removeUIElement(this);
                                uiPlayer.addUIElement(overlay);
                            }));
                }
                break;
            case GROUP:
                iconGrid.addChild(createAddMarkerCard(uiPlayer, t().get("TC_MENU_ADD_MARKER_GROUP", uiPlayer),
                        onCreateNewMarker -> {
                            CreateMarkerOverlay overlay = new CreateMarkerOverlay(uiPlayer, MarkerType.GROUP,
                                    uiPlayer.getPermissionGroup(), createdMarker -> {
                                        GPSDatabase.getInstance().saveMarker(createdMarker);
                                        uiPlayer.sendTextMessage(t().get("TC_GPS_GROUP_CREATED", uiPlayer)
                                                .replace("PH_MARKER_NAME", createdMarker.getName())
                                                .replace("PH_MARKER_POS", createdMarker.getPosition() + ""));
                                        uiPlayer.setAttribute("gps-ui-overlay", this);
                                        CursorManager.show(uiPlayer);
                                        uiPlayer.addUIElement(this);
                                        refreshGrid(uiPlayer);
                                    });

                            uiPlayer.setAttribute("gps-ui-overlay", overlay);
                            uiPlayer.removeUIElement(this);
                            uiPlayer.addUIElement(overlay);
                        }));
                break;
            case PRIVATE:
                iconGrid.addChild(createAddMarkerCard(uiPlayer, t().get("TC_MENU_ADD_MARKER_PRIVATE", uiPlayer),
                        onCreateNewMarker -> {
                            CreateMarkerOverlay overlay = new CreateMarkerOverlay(uiPlayer, MarkerType.PRIVATE,
                                    createdMarker -> {
                                        GPSDatabase.getInstance().saveMarker(createdMarker);
                                        uiPlayer.sendTextMessage(t().get("TC_GPS_PRIVATE_CREATED", uiPlayer)
                                                .replace("PH_MARKER_NAME", createdMarker.getName())
                                                .replace("PH_MARKER_POS", createdMarker.getPosition() + ""));
                                        uiPlayer.setAttribute("gps-ui-overlay", this);
                                        CursorManager.show(uiPlayer);
                                        uiPlayer.addUIElement(this);
                                        refreshGrid(uiPlayer);
                                    });

                            uiPlayer.setAttribute("gps-ui-overlay", overlay);
                            uiPlayer.removeUIElement(this);
                            uiPlayer.addUIElement(overlay);
                        }));
                break;
            default:
                break;
        }

        // previous page ?
        if (page > 0) {
            iconGrid.addChild(createPreviousPageCard(uiPlayer, type, page - 1, onPreviousPage -> {
            }));
        }
        // next page
        if (markers != null && markers.size() >= markerPerPage) {
            // Add a "more" button or similar indicator
            iconGrid.addChild(createNextPageCard(uiPlayer, type, page + 1, onNextPage -> {
            }));
        }
    }

    private UIElement createMarkerCardFromMarker(Marker marker, Player player) {
        Callback<Boolean> onDeleteCallback = onDelete -> {
            GPSDatabase.getInstance().deleteMarker(marker.getId(), player.getDbID());
            player.sendTextMessage(t().get("TC_GPS_DELETED", player).replace("PH_MARKER_NAME", marker.getName()));
            refreshGrid(player);
        };
        if (marker.getType() == MarkerType.GLOBAL && !player.isAdmin()) {
            onDeleteCallback = null;
        }
        if (marker.getPlayerId() != player.getDbID() && !player.isAdmin()) {
            onDeleteCallback = null;
        }
        return createMarkerCard(player, marker.getName(), AssetManager.getIcon(marker.getIcon()), onDeleteCallback,
                onTeleport -> {
                    player.setAttribute("pre-port-location", player.getPosition());
                    player.setPosition(marker.getPosition());
                    switch (marker.getType()) {
                        case PRIVATE:
                            GPSEventUtils.onPrivateGPSEvent(player, marker.getName(), marker.getPosition());
                            break;
                        case GROUP:
                            GPSEventUtils.onGroupGPSEvent(player, marker.getName(), marker.getPosition());
                            break;
                        case GLOBAL:
                            GPSEventUtils.onGlobalGPSEvent(player, marker.getName(), marker.getPosition());
                            break;
                        case STATIC:
                            GPSEventUtils.onStaticGPSEvent(player, marker.getName(), marker.getPosition());
                            break;
                    }
                    CursorManager.hide(player);
                    player.removeUIElement(this);
                });
    }

    private UIElement createMarkerCard(Player player, String name, TextureAsset icon, Callback<Boolean> onDelete,
            Callback<Boolean> onTeleport) {

        UIElement card = new UIElement();
        card.setSize(250 * scaleFactor, 300 * scaleFactor, false);
        card.setPivot(Pivot.UpperLeft);
        card.setBorder(1);
        card.setBorderColor(1, 1, 1, 0.2f);
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
        markerIcon.setClickable(true);
        markerIcon.setClickAction(event -> {
            onTeleport.onCall(true);
        });
        card.addChild(markerIcon);

        // marker delete trash can
        if (onDelete != null) {
            BaseButton deleteButton = ButtonFactory.danger("", event -> {
                onDelete.onCall(true);
            });
            deleteButton.setSize(30 * scaleFactor, 30 * scaleFactor, false);
            deleteButton.setPivot(Pivot.LowerRight);
            deleteButton.setPosition(245 * scaleFactor, 295 * scaleFactor, false);
            deleteButton.setBorderEdgeRadius(5, false);
            deleteButton.setBorder(1);
            deleteButton.setBorderColor(1, 1, 1, 0.2f);
            deleteButton.style.paddingBottom.set(5);
            deleteButton.style.paddingTop.set(5);
            deleteButton.style.paddingLeft.set(5);
            deleteButton.style.paddingRight.set(5);
            deleteButton.style.backgroundImage.set(AssetManager.getIcon("trash-xmark"));
            deleteButton.style.backgroundImageScaleMode.set(ScaleMode.ScaleToFit);
            card.addChild(deleteButton);
        }

        // marker label
        UILabel markerLabel = new UILabel(name);
        markerLabel.setSize(210 * scaleFactor, 40 * scaleFactor, false);
        markerLabel.setFontSize(14 * scaleFactor);
        markerLabel.setTextAlign(TextAnchor.UpperCenter);
        markerLabel.setPivot(Pivot.LowerLeft);
        markerLabel.setPosition(5 * scaleFactor, 295 * scaleFactor, false);
        markerLabel.setTextWrap(true);
        card.addChild(markerLabel);
        return card;
    }

    private OZUIElement createPreviousPageCard(Player player, MarkerType type, Integer previousPage,
            Callback<Boolean> onPreviousPage) {
        return createNavigationCard(player, type, previousPage, onPreviousPage, "icon-ki-gps-previous-page",
                "TC_MENU_PREVIOUS_PAGE");
    }

    private OZUIElement createNextPageCard(Player player, MarkerType type, Integer nextPage,
            Callback<Boolean> onNextPage) {
        return createNavigationCard(player, type, nextPage, onNextPage, "icon-ki-gps-next-page", "TC_MENU_NEXT_PAGE");
    }

    private OZUIElement createNavigationCard(Player player, MarkerType type, Integer targetPage,
            Callback<Boolean> onNavigate, String iconKey, String cardLabel) {
        OZUIElement card = new OZUIElement();
        card.setSize(250 * scaleFactor, 300 * scaleFactor, false);
        card.setPivot(Pivot.UpperLeft);
        card.setBorder(1);
        card.setBorderColor(1, 1, 1, 0.2f);
        int margin = cardMargin;
        card.style.marginBottom.set(margin, Unit.Pixel);
        card.style.marginTop.set(margin, Unit.Pixel);
        card.style.marginLeft.set(margin, Unit.Pixel);
        card.style.marginRight.set(margin, Unit.Pixel);

        // next page icon + click action
        OZUIElement nextIcon = new OZUIElement();
        nextIcon.setSize(240 * scaleFactor, 240 * scaleFactor, false);
        nextIcon.setPivot(Pivot.UpperLeft);
        nextIcon.setPosition(5 * scaleFactor, 5 * scaleFactor, false);
        nextIcon.style.backgroundImage.set(AssetManager.getIcon(iconKey));
        nextIcon.setClickable(true);
        nextIcon.setClickAction(event -> {
            refreshGrid(type, player, targetPage);
            onNavigate.onCall(true);
        });
        card.addChild(nextIcon);

        // next page label
        UILabel nextLabel = new UILabel(t().get(cardLabel, player));
        nextLabel.setSize(210 * scaleFactor, 40 * scaleFactor, false);
        nextLabel.setFontSize(14 * scaleFactor);
        nextLabel.setTextAlign(TextAnchor.UpperCenter);
        nextLabel.setPivot(Pivot.LowerLeft);
        nextLabel.setPosition(5 * scaleFactor, 295 * scaleFactor, false);
        nextLabel.setTextWrap(true);
        card.addChild(nextLabel);
        return card;
    }

    private OZUIElement createAddMarkerCard(Player player, String labelText, Callback<Boolean> onCreateNewMarker) {
        OZUIElement card = new OZUIElement();
        card.setSize(250 * scaleFactor, 300 * scaleFactor, false);
        card.setPivot(Pivot.UpperLeft);
        card.setBorder(1);
        card.setBorderColor(1, 1, 1, 0.2f);
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
        addIcon.style.backgroundImage.set(AssetManager.getIcon("icon-ki-gps-add-marker"));
        addIcon.setClickable(true);
        addIcon.setClickAction(event -> {
            onCreateNewMarker.onCall(true);
        });
        card.addChild(addIcon);

        // add marker label
        UILabel addLabel = new UILabel(labelText);
        addLabel.setSize(210 * scaleFactor, 40 * scaleFactor, false);
        addLabel.setFontSize(14 * scaleFactor);
        addLabel.setTextAlign(TextAnchor.UpperCenter);
        addLabel.setPivot(Pivot.LowerLeft);
        addLabel.setPosition(5 * scaleFactor, 295 * scaleFactor, false);
        addLabel.setTextWrap(true);
        card.addChild(addLabel);
        return card;
    }

    private UIElement setupCloseTab(Player player) {
        OZUIElement tab = new OZUIElement();
        tab.setSize(20, 100, true);
        tab.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.6f);
        tab.setPivot(Pivot.UpperLeft);
        tab.setClickable(true);
        tab.setClickAction(event -> {
            event.getPlayer().removeUIElement(this);
            CursorManager.hide(event.getPlayer());
        });
        // icon
        OZUIElement icon = new OZUIElement();
        icon.setSize(100, 70, true);
        icon.setPivot(Pivot.UpperLeft);
        icon.setPosition(0, 5, true);
        icon.style.backgroundImage.set(AssetManager.getIcon("icon-gpt-exit"));
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
}
