package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
// import de.omegazirkel.risingworld.gps.PluginSettings;
import de.omegazirkel.risingworld.gps.GPSEconomy;
import de.omegazirkel.risingworld.gps.GPSDatabase;
import de.omegazirkel.risingworld.gps.GPSPlayerPreferences;
import de.omegazirkel.risingworld.gps.Marker;
import de.omegazirkel.risingworld.gps.MarkerPermissions;
import de.omegazirkel.risingworld.gps.TeleportCooldowns;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.BaseButton;
import de.omegazirkel.risingworld.tools.ui.ButtonFactory;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.Timer;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;
import net.risingworld.api.ui.style.Wrap;

public class TeleportOverlay extends OZUIElement {

    // private static final PluginSettings s = PluginSettings.getInstance();
    private static final float GOLD_R = 0.95f;
    private static final float GOLD_G = 0.75f;
    private static final float GOLD_B = 0.25f;

    Callback<Boolean> onTeleportConfirm = null;
    Callback<Boolean> onMarkerChanged = null;
    Marker marker = null;
    private Timer cooldownTimer = null;
    private UILabel cooldownLabel = null;
    private BaseButton teleportButton = null;

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    public TeleportOverlay(Player player, Marker marker, Callback<Boolean> onTeleportConfirm) {
        this(player, marker, onTeleportConfirm, changed -> {
        });
    }

    public TeleportOverlay(Player player, Marker marker, Callback<Boolean> onTeleportConfirm,
            Callback<Boolean> onMarkerChanged) {
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
        this.setBackgroundColor(0, 0, 0, 0.4f);
        this.onTeleportConfirm = onTeleportConfirm;
        this.onMarkerChanged = onMarkerChanged;
        this.marker = marker;

        setupActions(player);
    }

    private void setupActions(Player player) {
        boolean canManageMarker = MarkerPermissions.canManage(player, marker);

        OZUIElement actionsPanel = new OZUIElement();
        actionsPanel.setSize(canManageMarker ? 28 : 25, canManageMarker ? 32 : 25, true);
        actionsPanel.setPivot(Pivot.MiddleCenter);
        actionsPanel.setPosition(50, 50, true);
        actionsPanel.setBackgroundColor(0, 0, 0, 0.86f);
        actionsPanel.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.6f);
        actionsPanel.setBorder(1);
        actionsPanel.setBorderEdgeRadius(6, false);

        this.addChild(actionsPanel);

        // header

        UILabel title = new UILabel(
                t().get("TC_LABEL_MARKER_TITLE", player).replace("PH_MARKER_NAME", marker.getName()));
        title.setSize(100, 10, true);
        title.setFont(Font.DefaultBold);
        title.setFontSize(17);
        title.setPivot(Pivot.UpperLeft);
        title.setPosition(5, 5, true);
        title.setTextAlign(TextAnchor.MiddleLeft);
        actionsPanel.addChild(title);

        UIElement body = new UIElement();
        body.setSize(90, canManageMarker ? 42 : 50, true);
        body.setPivot(Pivot.UpperLeft);
        body.setPosition(5, 24, true);
        body.setBackgroundColor(0.08f, 0.08f, 0.08f, 0.55f);
        body.setBorder(1);
        body.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.48f);
        body.setBorderEdgeRadius(4, false);
        actionsPanel.addChild(body);

        UILabel markerName = new UILabel(marker.getName());
        markerName.setSize(90, 28, true);
        markerName.setPivot(Pivot.UpperLeft);
        markerName.setPosition(5, 8, true);
        markerName.setFont(Font.DefaultBold);
        markerName.setFontSize(18);
        markerName.setTextAlign(TextAnchor.MiddleCenter);
        markerName.setTextWrap(true);
        body.addChild(markerName);

        cooldownLabel = new UILabel("");
        cooldownLabel.setSize(90, 18, true);
        cooldownLabel.setPivot(Pivot.LowerLeft);
        cooldownLabel.setPosition(5, 94, true);
        cooldownLabel.setFont(Font.Default);
        cooldownLabel.setFontSize(13);
        cooldownLabel.setTextAlign(TextAnchor.MiddleCenter);
        cooldownLabel.setTextWrap(true);
        body.addChild(cooldownLabel);

        UILabel costLabel = new UILabel(costText(player));
        costLabel.setSize(90, 16, true);
        costLabel.setPivot(Pivot.LowerLeft);
        costLabel.setPosition(5, 78, true);
        costLabel.setFont(Font.Default);
        costLabel.setFontSize(12);
        costLabel.setTextAlign(TextAnchor.MiddleCenter);
        costLabel.setTextWrap(false);
        body.addChild(costLabel);

        // footer

        OZUIElement panelFooter = new OZUIElement();
        panelFooter.setSize(100, canManageMarker ? 34 : 25, true);
        panelFooter.setPivot(Pivot.LowerCenter);
        panelFooter.setPosition(50, 100, true);
        panelFooter.setMargin(10);
        panelFooter.style.display.set(DisplayStyle.Flex);
        panelFooter.style.flexDirection.set(FlexDirection.Row);
        panelFooter.style.flexWrap.set(Wrap.Wrap);
        panelFooter.style.justifyContent.set(Justify.Center);
        panelFooter.style.alignItems.set(Align.Center);
        actionsPanel.addChild(panelFooter);

        panelFooter.addChild(setupCancelButton(player, canManageMarker));
        if (canManageMarker) {
            panelFooter.addChild(setupEditButton(player));
            panelFooter.addChild(setupRemoveButton(player));
        }
        panelFooter.addChild(setupTeleportButton(player, canManageMarker));
        refreshCooldownState(player);
        startCooldownTimer(player);
    }

    private UIElement setupCancelButton(Player player, boolean twoColumnFooter) {
        BaseButton btn = ButtonFactory.cancel(t().get("TC_BTN_CANCEL", player), event -> {
            stopCooldownTimer();
            event.getPlayer().removeUIElement(this);
            CursorManager.hide(event.getPlayer());
            onTeleportConfirm.onCall(false);
        });
        styleFooterButton(btn, twoColumnFooter);
        return btn;
    }

    private UIElement setupEditButton(Player player) {
        BaseButton btn = ButtonFactory.info(t().get("TC_BTN_EDIT", player), event -> {
            stopCooldownTimer();
            player.removeUIElement(this);
            CreateMarkerOverlay overlay = new CreateMarkerOverlay(player, editableMarkerCopy(),
                    editedMarker -> {
                        if (GPSDatabase.getInstance().updateMarkerDetails(marker, player, editedMarker.getName(),
                                editedMarker.getIcon())) {
                            player.sendTextMessage(t().get("TC_GPS_MARKER_UPDATED", player)
                                    .replace("PH_MARKER_NAME", editedMarker.getName()));
                            reopenTeleportOverlay(player);
                        }
                    });
            player.setAttribute("gps-ui-overlay", overlay);
            player.addUIElement(overlay);
        });
        styleFooterButton(btn, true);
        return btn;
    }

    private void reopenTeleportOverlay(Player player) {
        TeleportOverlay overlay = new TeleportOverlay(player, marker, onTeleportConfirm, onMarkerChanged);
        player.setAttribute("gps-ui-overlay", overlay);
        CursorManager.show(player);
        player.addUIElement(overlay);
    }

    private Marker editableMarkerCopy() {
        return new Marker(
                marker.getId(),
                marker.getPlayerId(),
                marker.getType(),
                marker.getGroup(),
                marker.getCreatedAt(),
                marker.getPosition(),
                marker.getName(),
                marker.getIcon(),
                marker.getColor(),
                marker.getCost());
    }

    private UIElement setupTeleportButton(Player player, boolean twoColumnFooter) {
        teleportButton = ButtonFactory.ok(t().get("TC_BTN_TELEPORT", player), event -> {
            stopCooldownTimer();
            event.getPlayer().removeUIElement(this);
            CursorManager.hide(event.getPlayer());
            onTeleportConfirm.onCall(true);
        });
        styleFooterButton(teleportButton, twoColumnFooter);
        return teleportButton;
    }

    private UIElement setupRemoveButton(Player player) {
        BaseButton btn = ButtonFactory.danger(t().get("TC_BTN_REMOVE", player), event -> {
            deleteMarker(event.getPlayer());
        });
        styleFooterButton(btn, true);
        return btn;
    }

    private String costText(Player player) {
        GPSEconomy economy = GPSEconomy.getInstance();
        if (economy == null) {
            return "";
        }
        long cost = economy.teleportCost(player, marker.getPosition(), marker.getType());
        return cost <= 0 ? "" : t().get("TC_GPS_COST_LABEL", player)
                .replace("PH_COST", economy.costLabel(cost, economy.teleportCurrency(marker.getType())));
    }

    private void startCooldownTimer(Player player) {
        stopCooldownTimer();
        if (!TeleportCooldowns.isEnabled(marker.getType())) {
            return;
        }
        cooldownTimer = new Timer(1, 0, -1, () -> {
            if (!player.isConnected()) {
                stopCooldownTimer();
                return;
            }
            refreshCooldownState(player);
        });
        cooldownTimer.start();
    }

    private void stopCooldownTimer() {
        if (cooldownTimer != null) {
            cooldownTimer.kill();
            cooldownTimer = null;
        }
    }

    private void refreshCooldownState(Player player) {
        if (teleportButton == null || cooldownLabel == null) {
            return;
        }

        int remainingSeconds = TeleportCooldowns.remainingSeconds(player, marker.getType());
        if (remainingSeconds > 0) {
            String secondsText = String.valueOf(remainingSeconds);
            cooldownLabel.setVisible(true);
            cooldownLabel.setText(t().get("TC_GPS_COOLDOWN_STATUS", player)
                    .replace("PH_SECONDS", secondsText)
                    .replace("PH_MARKER_TYPE", t().get(TeleportCooldowns.displayTypeKey(marker.getType()), player)));
            teleportButton.setText(t().get("TC_BTN_TELEPORT_COOLDOWN", player).replace("PH_SECONDS", secondsText));
            teleportButton.setClickable(false);
            teleportButton.setBackgroundColor(0x333333AA);
            return;
        }

        if (TeleportCooldowns.isEnabled(marker.getType())) {
            cooldownLabel.setText(t().get("TC_GPS_COOLDOWN_READY", player)
                    .replace("PH_MARKER_TYPE", t().get(TeleportCooldowns.displayTypeKey(marker.getType()), player)));
            cooldownLabel.setVisible(true);
        } else {
            cooldownLabel.setText("");
            cooldownLabel.setVisible(false);
        }
        teleportButton.setText(t().get("TC_BTN_TELEPORT", player));
        teleportButton.setClickable(true);
        teleportButton.setBackgroundColor(0x208A28FF);
    }

    private void deleteMarker(Player player) {
        if (!GPSPlayerPreferences.confirmMarkerDelete(player)) {
            performDelete(player);
            return;
        }

        ConfirmMarkerDeleteOverlay confirmOverlay = new ConfirmMarkerDeleteOverlay(player, marker,
                dontAskAgain -> performDelete(player),
                ignored -> {
                });
        player.addUIElement(confirmOverlay);
    }

    private void performDelete(Player player) {
        stopCooldownTimer();
        player.removeUIElement(this);
        CursorManager.hide(player);
        if (GPSDatabase.getInstance().deleteMarker(marker, player)) {
            player.sendTextMessage(t().get("TC_GPS_DELETED", player).replace("PH_MARKER_NAME", marker.getName()));
            onMarkerChanged.onCall(true);
        }
    }

    private void styleFooterButton(BaseButton btn, boolean twoColumnFooter) {
        btn.setPivot(Pivot.UpperLeft);
        btn.style.display.set(DisplayStyle.Flex);
        btn.style.justifyContent.set(Justify.Center);
        btn.style.alignItems.set(Align.Center);
        btn.style.width.set(twoColumnFooter ? 43 : 42, Unit.Percent);
        btn.style.height.set(36, Unit.Pixel);
        int margin = 2;
        btn.style.marginBottom.set(margin, Unit.Pixel);
        btn.style.marginTop.set(margin, Unit.Pixel);
        btn.style.marginLeft.set(margin, Unit.Pixel);
        btn.style.marginRight.set(margin, Unit.Pixel);
        btn.setBorderEdgeRadius(4, false);
    }
}
