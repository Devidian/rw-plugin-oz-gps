package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.discordconnect.PluginSettings;
import de.omegazirkel.risingworld.gps.GPSDatabase;
import de.omegazirkel.risingworld.gps.Marker;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.BaseButton;
import de.omegazirkel.risingworld.tools.ui.ButtonFactory;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.Unit;
import net.risingworld.api.ui.style.Wrap;

public class TeleportOverlay extends OZUIElement {

    private static final PluginSettings s = PluginSettings.getInstance();

    Callback<Boolean> onTeleportConfirm = null;
    Marker marker = null;

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    public TeleportOverlay(Player player, Marker marker, Callback<Boolean> onTeleportConfirm) {
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
        this.setBackgroundColor(0, 0, 0, 0.4f);
        this.onTeleportConfirm = onTeleportConfirm;
        this.marker = marker;

        setupActions(player);
    }

    private void setupActions(Player player) {
        OZUIElement actionsPanel = new OZUIElement();
        actionsPanel.setSize(25, 25, true);
        actionsPanel.setPivot(Pivot.MiddleCenter);
        actionsPanel.setPosition(50, 50, true);
        actionsPanel.setBackgroundColor(0, 0, 0, 0.85f);
        actionsPanel.setBorderColor(1, 1, 1, 0.4f);
        actionsPanel.setBorder(2);

        this.addChild(actionsPanel);

        // header

        UILabel title = new UILabel(
                t().get("TC_LABEL_MARKER_TITLE", player).replace("PH_MARKER_NAME", marker.getName()));
        title.setSize(100, 10, true);
        title.setFontSize(17);
        title.setPivot(Pivot.UpperLeft);
        title.setPosition(5, 5, true);
        actionsPanel.addChild(title);

        // footer

        OZUIElement panelFooter = new OZUIElement();
        panelFooter.setSize(100, 25, true);
        panelFooter.setPivot(Pivot.LowerCenter);
        panelFooter.setPosition(50, 95, true);
        panelFooter.style.display.set(DisplayStyle.Flex);
        panelFooter.style.flexDirection.set(FlexDirection.Row);
        panelFooter.style.flexWrap.set(Wrap.Wrap);
        panelFooter.style.justifyContent.set(Justify.Center);
        actionsPanel.addChild(panelFooter);

        panelFooter.addChild(setupCancelButton(player));
        panelFooter.addChild(setupRemoveButton(player));
        panelFooter.addChild(setupTeleportButton(player));
    }

    private UIElement setupCancelButton(Player player) {
        BaseButton btn = ButtonFactory.cancel(t().get("TC_BTN_CANCEL", player), event -> {
            event.getPlayer().removeUIElement(this);
            CursorManager.hide(event.getPlayer());
            onTeleportConfirm.onCall(false);
        });
        btn.setPivot(Pivot.UpperLeft);
        btn.style.display.set(DisplayStyle.Flex);
        btn.style.justifyContent.set(Justify.Center);
        btn.style.alignItems.set(Align.Center);
        btn.style.width.set(30, Unit.Percent);
        int margin = 2;
        btn.style.marginBottom.set(margin, Unit.Pixel);
        btn.style.marginTop.set(margin, Unit.Pixel);
        btn.style.marginLeft.set(margin, Unit.Pixel);
        btn.style.marginRight.set(margin, Unit.Pixel);
        return btn;
    }

    private UIElement setupTeleportButton(Player player) {
        BaseButton btn = ButtonFactory.ok(t().get("TC_BTN_TELEPORT", player), event -> {
            event.getPlayer().removeUIElement(this);
            CursorManager.hide(event.getPlayer());
            onTeleportConfirm.onCall(true);
        });
        btn.setPivot(Pivot.UpperLeft);
        btn.style.display.set(DisplayStyle.Flex);
        btn.style.justifyContent.set(Justify.Center);
        btn.style.alignItems.set(Align.Center);
        btn.style.width.set(30, Unit.Percent);
        int margin = 2;
        btn.style.marginBottom.set(margin, Unit.Pixel);
        btn.style.marginTop.set(margin, Unit.Pixel);
        btn.style.marginLeft.set(margin, Unit.Pixel);
        btn.style.marginRight.set(margin, Unit.Pixel);
        return btn;
    }

    private UIElement setupRemoveButton(Player player) {
        BaseButton btn = ButtonFactory.danger(t().get("TC_BTN_REMOVE", player), event -> {
            event.getPlayer().removeUIElement(this);
            CursorManager.hide(event.getPlayer());
            GPSDatabase.getInstance().deleteMarker(marker.getId(), player.getDbID());
            player.sendTextMessage(t().get("TC_GPS_DELETED", player).replace("PH_MARKER_NAME", marker.getName()));
            onTeleportConfirm.onCall(false);
        });
        btn.setPivot(Pivot.UpperLeft);
        btn.style.display.set(DisplayStyle.Flex);
        btn.style.justifyContent.set(Justify.Center);
        btn.style.alignItems.set(Align.Center);
        btn.style.width.set(30, Unit.Percent);
        int margin = 2;
        btn.style.marginBottom.set(margin, Unit.Pixel);
        btn.style.marginTop.set(margin, Unit.Pixel);
        btn.style.marginLeft.set(margin, Unit.Pixel);
        btn.style.marginRight.set(margin, Unit.Pixel);
        return btn;
    }
}