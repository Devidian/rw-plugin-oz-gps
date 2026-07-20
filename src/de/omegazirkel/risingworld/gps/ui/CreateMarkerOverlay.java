package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.Marker;
import de.omegazirkel.risingworld.gps.MarkerType;
import de.omegazirkel.risingworld.gps.GPSAccessPolicy;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.AdvancedButton;
import de.omegazirkel.risingworld.tools.ui.AdvancedButtonFactory;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.style.Pivot;

public class CreateMarkerOverlay extends OZUIElement {

    private static final float GOLD_R = 0.95f;
    private static final float GOLD_G = 0.75f;
    private static final float GOLD_B = 0.25f;

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    private SelectMarkerIconPanel markerIconSelection;
    private SetMarkerNamePanel markerName;
    private MarkerType type = null;
    private String groupName = null;
    private Marker existingMarker = null;

    private Callback<Marker> onMarkerCreated;

    public CreateMarkerOverlay(Player player, Callback<Marker> onMarkerCreated) {
        this(player, MarkerType.GLOBAL, null, onMarkerCreated);
    }

    public CreateMarkerOverlay(Player player, MarkerType type, Callback<Marker> onMarkerCreated) {
        this(player, type, null, onMarkerCreated);
    }

    public CreateMarkerOverlay(Player player, String groupName, Callback<Marker> onMarkerCreated) {
        this(player, MarkerType.GROUP, groupName, onMarkerCreated);
    }

    public CreateMarkerOverlay(Player player, MarkerType type, String groupName, Callback<Marker> onMarkerCreated) {
        this(player, type, groupName, null, onMarkerCreated);
    }

    public CreateMarkerOverlay(Player player, Marker marker, Callback<Marker> onMarkerCreated) {
        this(player, marker.getType(), marker.getGroup(), marker, onMarkerCreated);
    }

    private CreateMarkerOverlay(Player player, MarkerType type, String groupName, Marker existingMarker,
            Callback<Marker> onMarkerCreated) {
        super();
        setClickable(false);
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
        setBackgroundColor(0, 0, 0, 0.4f);

        this.onMarkerCreated = onMarkerCreated;
        this.type = type;
        this.groupName = groupName;
        this.existingMarker = existingMarker;

        setupMarkerIconSelection(player);
        setupMarkerName(player);
        setupMarkerActions(player);
    }

    private void setupMarkerIconSelection(Player player) {
        markerIconSelection = new SelectMarkerIconPanel(player, existingMarker == null ? null : existingMarker.getIcon());
        this.addChild(markerIconSelection);
    }

    private void setupMarkerName(Player player) {
        markerName = new SetMarkerNamePanel(player, existingMarker == null ? null : existingMarker.getName());
        this.addChild(markerName);
    }

    private void setupMarkerActions(Player player) {
        OZUIElement actionsPanel = new OZUIElement();
        actionsPanel.setSize(25, 5, true);
        actionsPanel.setPivot(Pivot.UpperCenter);
        actionsPanel.setPosition(67, 30, true);
        actionsPanel.setBackgroundColor(0, 0, 0, 0.86f);
        actionsPanel.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.6f);
        actionsPanel.setBorder(1);
        actionsPanel.setBorderEdgeRadius(6, false);

        actionsPanel.addChild(setupCreateButton(player));
        actionsPanel.addChild(setupCancelButton(player));
        this.addChild(actionsPanel);
    }

    private UIElement setupCreateButton(Player player) {
        AdvancedButton createButton = AdvancedButtonFactory.ok(t().get("TC_BTN_SAVE", player), event -> {
            if (!GPSAccessPolicy.canUse(player, type)) {
                player.sendTextMessage(t().get("TC_GPS_PLAYTIME_REQUIRED", player)
                        .replace("PH_REQUIRED_MINUTES", String.valueOf(GPSAccessPolicy.requiredMinutes()))
                        .replace("PH_REMAINING_MINUTES", String.valueOf(GPSAccessPolicy.remainingMinutes(player))));
                return;
            }

            String selectedMarkerKey = markerIconSelection.getSelectedKey();
            markerName.getCurrentText(player, selectedMarkerName -> {
                if (selectedMarkerKey == null || selectedMarkerName == null || selectedMarkerName.isEmpty()) {
                    // error messsage to player?
                    return;
                }
                Marker marker = existingMarker;
                if (marker == null) {
                    marker = new Marker(
                            player.getDbID(),
                            type,
                            groupName,
                            player.getPosition(),
                            selectedMarkerName,
                            selectedMarkerKey,
                            0xFFFFFFFF,
                            0);
                } else {
                    marker.setName(selectedMarkerName);
                    marker.setIcon(selectedMarkerKey);
                }
                close(player);
                this.onMarkerCreated.onCall(marker);
            });

        });

        createButton.setPivot(Pivot.MiddleLeft);
        createButton.setPosition(51, 50, true);
        createButton.setBorderEdgeRadius(4, false);
        return createButton;
    }

    private UIElement setupCancelButton(Player player) {
        AdvancedButton cancelButton = AdvancedButtonFactory.cancel(t().get("TC_BTN_CANCEL", player), event -> {
            close(event.getPlayer());
        });
        cancelButton.setPivot(Pivot.MiddleRight);
        cancelButton.setPosition(49, 50, true);
        cancelButton.setBorderEdgeRadius(4, false);
        return cancelButton;
    }

    public void close(Player player) {
        player.removeUIElement(this);
        player.deleteAttribute("gps-ui-overlay");
        CursorManager.hide(player);
    }

}
