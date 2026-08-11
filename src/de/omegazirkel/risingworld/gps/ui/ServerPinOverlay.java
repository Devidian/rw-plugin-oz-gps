package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.PluginGUI;
import de.omegazirkel.risingworld.gps.ServerPin;
import de.omegazirkel.risingworld.gps.ServerPinAddress;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.AdvancedButton;
import de.omegazirkel.risingworld.tools.ui.AdvancedButtonFactory;
import de.omegazirkel.risingworld.tools.ui.CursorManager;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UITextField;
import net.risingworld.api.ui.style.Align;
import net.risingworld.api.ui.style.DisplayStyle;
import net.risingworld.api.ui.style.FlexDirection;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Justify;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.ui.style.Unit;

/** Admin-only editor for a server pin. It intentionally shares the flex action footer with marker dialogs. */
public class ServerPinOverlay extends OZUIElement {
    private static final float GOLD_R = .95f, GOLD_G = .75f, GOLD_B = .25f;
    private final ServerPin existingPin;
    private final Callback<ServerPin> onSaved;
    private final SelectMarkerIconPanel iconSelection;
    private UITextField nameInput;
    private UITextField addressInput;
    private UITextField passwordInput;

    private static I18n t() { return I18n.getInstance(GPS.name); }

    public ServerPinOverlay(Player player, ServerPin existingPin, Callback<ServerPin> onSaved) {
        this.existingPin = existingPin;
        this.onSaved = onSaved;
        setClickable(false);
        setPivot(Pivot.UpperLeft);
        setSize(100, 100, true);
        setBackgroundColor(0, 0, 0, .4f);
        iconSelection = new SelectMarkerIconPanel(player, existingPin == null ? "marker-server-1" : existingPin.getIcon(), PluginGUI.serverPinKeys);
        addChild(iconSelection);
        addChild(detailsPanel(player));
        addChild(actionsPanel(player));
    }

    private UIElement detailsPanel(Player player) {
        OZUIElement panel = new OZUIElement();
        panel.setSize(24, 31, true);
        panel.setPivot(Pivot.UpperCenter);
        panel.setPosition(68, 22, true);
        panel.setBackgroundColor(0, 0, 0, .86f);
        panel.setBorderColor(GOLD_R, GOLD_G, GOLD_B, .6f);
        panel.setBorder(1);
        panel.setBorderEdgeRadius(6, false);
        panel.style.paddingBottom.set(5); panel.style.paddingTop.set(5); panel.style.paddingLeft.set(5); panel.style.paddingRight.set(5);
        addField(panel, player, "TC_LABEL_SERVER_PIN_NAME", existingPin == null ? "" : existingPin.getName(), 7, input -> nameInput = input);
        addField(panel, player, "TC_LABEL_SERVER_PIN_ADDRESS", existingPin == null ? "" : existingPin.getAddress(), 37, input -> addressInput = input);
        addField(panel, player, "TC_LABEL_SERVER_PIN_PASSWORD", existingPin == null ? "" : existingPin.getPassword(), 67, input -> passwordInput = input);
        return panel;
    }

    private void addField(UIElement panel, Player player, String labelKey, String value, int top, Callback<UITextField> result) {
        UILabel label = new UILabel(t().get(labelKey, player));
        label.setSize(90, 12, true); label.setFontSize(15); label.setFont(Font.DefaultBold);
        label.setTextAlign(TextAnchor.MiddleLeft); label.setPivot(Pivot.UpperLeft); label.setPosition(5, top, true);
        panel.addChild(label);
        UITextField input = new UITextField(value);
        input.setSize(90, 20, true); input.setReadOnly(false); input.setPivot(Pivot.UpperLeft); input.setPosition(5, top + 10, true);
        input.setBackgroundColor(.02f, .02f, .02f, .78f); input.setBorder(1); input.setBorderColor(GOLD_R, GOLD_G, GOLD_B, .46f); input.setBorderEdgeRadius(4, false);
        panel.addChild(input); result.onCall(input);
    }

    private UIElement actionsPanel(Player player) {
        OZUIElement panel = new OZUIElement();
        panel.setSize(24, 7, true); panel.setPivot(Pivot.UpperCenter); panel.setPosition(68, 55, true);
        panel.setBackgroundColor(0, 0, 0, .86f); panel.setBorderColor(GOLD_R, GOLD_G, GOLD_B, .6f); panel.setBorder(1); panel.setBorderEdgeRadius(6, false);
        panel.style.display.set(DisplayStyle.Flex); panel.style.flexDirection.set(FlexDirection.Row); panel.style.justifyContent.set(Justify.Center); panel.style.alignItems.set(Align.Center);
        panel.addChild(actionButton(AdvancedButtonFactory.ok(t().get("TC_BTN_SAVE", player), event -> save(player))));
        panel.addChild(actionButton(AdvancedButtonFactory.cancel(t().get("TC_BTN_CANCEL", player), event -> close(event.getPlayer()))));
        return panel;
    }

    private UIElement actionButton(AdvancedButton button) {
        button.setPivot(Pivot.UpperLeft);
        button.style.display.set(DisplayStyle.Flex); button.style.justifyContent.set(Justify.Center); button.style.alignItems.set(Align.Center);
        button.style.width.set(42, Unit.Percent); button.style.height.set(36, Unit.Pixel); button.style.marginLeft.set(2, Unit.Pixel); button.style.marginRight.set(2, Unit.Pixel); button.setBorderEdgeRadius(4, false);
        return button;
    }

    private void save(Player player) {
        nameInput.getCurrentText(player, name -> addressInput.getCurrentText(player, address -> passwordInput.getCurrentText(player, password -> {
            String icon = iconSelection.getSelectedKey();
            if (name == null || name.isBlank() || icon == null) { player.sendTextMessage(t().get("TC_GPS_SERVER_PIN_INVALID", player)); return; }
            if (!ServerPinAddress.isValid(address)) { player.sendTextMessage(t().get("TC_GPS_SERVER_PIN_ADDRESS_INVALID", player)); return; }
            ServerPin pin = existingPin == null ? new ServerPin(player.getDbID(), name.trim(), icon, address.trim(), password) : existingPin;
            if (existingPin != null) { pin.setName(name.trim()); pin.setIcon(icon); pin.setAddress(address.trim()); pin.setPassword(password); }
            close(player); onSaved.onCall(pin);
        })));
    }

    public void close(Player player) { player.removeUIElement(this); player.deleteAttribute("gps-ui-overlay"); CursorManager.hide(player); }
}
