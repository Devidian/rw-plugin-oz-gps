package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.Marker;
import de.omegazirkel.risingworld.gps.MarkerType;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.BaseButton;
import de.omegazirkel.risingworld.tools.ui.ButtonFactory;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.style.Pivot;

public class CreateMarkerOverlay extends OZUIElement {

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    private SelectMarkerIconPanel markerIconSelection;
    private SetMarkerNamePanel markerName;
    private MarkerType type = null;
    private String groupName = null;

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
        super();
        setClickable(false);
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
        setBackgroundColor(0, 0, 0, 0.4f);

        this.onMarkerCreated = onMarkerCreated;
        this.type = type;
        this.groupName = groupName;

        setupMarkerIconSelection(player);
        setupMarkerName(player);
        setupMarkerActions(player);
    }

    private void setupMarkerIconSelection(Player player) {
        markerIconSelection = new SelectMarkerIconPanel(player);
        this.addChild(markerIconSelection);
    }

    private void setupMarkerName(Player player) {
        markerName = new SetMarkerNamePanel(player);
        this.addChild(markerName);
    }

    private void setupMarkerActions(Player player) {
        OZUIElement actionsPanel = new OZUIElement();
        actionsPanel.setSize(25, 5, true);
        actionsPanel.setPivot(Pivot.UpperCenter);
        actionsPanel.setPosition(55, 30, true);
        actionsPanel.setBackgroundColor(0, 0, 0, 0.85f);
        actionsPanel.setBorderColor(1, 1, 1, 0.4f);
        actionsPanel.setBorder(2);

        actionsPanel.addChild(setupCreateButton(player));
        actionsPanel.addChild(setupCancelButton(player));
        this.addChild(actionsPanel);
    }

    private UIElement setupCreateButton(Player player) {
        BaseButton createButton = ButtonFactory.ok(t().get("TC_BTN_SAVE", player), event -> {

            String selectedMarkerKey = markerIconSelection.getSelectedKey();
            markerName.getCurrentText(player, selectedMarkerName -> {
                if (selectedMarkerKey == null || selectedMarkerName == null || selectedMarkerName.isEmpty()) {
                    // error messsage to player?
                    return;
                }
                Marker marker = new Marker(
                        player.getDbID(),
                        type,
                        groupName,
                        player.getPosition(),
                        selectedMarkerName,
                        selectedMarkerKey,
                        0xFFFFFFFF,
                        0);
                CursorManager.hide(player);
                player.removeUIElement(this);
                this.onMarkerCreated.onCall(marker);
            });

        });

        createButton.setPivot(Pivot.MiddleLeft);
        createButton.setPosition(51, 50, true);
        return createButton;
    }

    private UIElement setupCancelButton(Player player) {
        BaseButton cancelButton = ButtonFactory.cancel(t().get("TC_BTN_CANCEL", player), event -> {
            event.getPlayer().removeUIElement(this);
            CursorManager.hide(event.getPlayer());
        });
        cancelButton.setPivot(Pivot.MiddleRight);
        cancelButton.setPosition(49, 50, true);
        return cancelButton;
    }

}
