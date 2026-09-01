package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.gps.GPSPlayerPreferences;
import de.omegazirkel.risingworld.gps.Marker;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.AdvancedButton;
import de.omegazirkel.risingworld.tools.ui.AdvancedButtonFactory;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
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

public class ConfirmMarkerDeleteOverlay extends OZUIElement {
    private static final float GOLD_R = 0.95f;
    private static final float GOLD_G = 0.75f;
    private static final float GOLD_B = 0.25f;

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    public ConfirmMarkerDeleteOverlay(Player player, Marker marker, Callback<Boolean> onDelete,
            Callback<Boolean> onCancel) {
        setPivot(Pivot.UpperLeft);
        setPosition(0, 0, true);
        setSize(100, 100, true);
        setBackgroundColor(0, 0, 0, 0.45f);
        setClickable(true);

        UIElement panel = new UIElement();
        panel.setSize(34, 24, true);
        panel.setPivot(Pivot.MiddleCenter);
        panel.setPosition(50, 50, true);
        panel.setBackgroundColor(0, 0, 0, 0.9f);
        panel.setBorder(2);
        panel.setBorderColor(GOLD_R, GOLD_G, GOLD_B, 0.62f);
        panel.setBorderEdgeRadius(6, false);
        addChild(panel);

        UILabel title = new UILabel(t().get("tc.confirm.marker.delete.title", player));
        title.setSize(90, 18, true);
        title.setPivot(Pivot.UpperLeft);
        title.setPosition(5, 7, true);
        title.setFont(Font.DefaultBold);
        title.setFontSize(18);
        title.setTextAlign(TextAnchor.MiddleCenter);
        panel.addChild(title);

        UILabel message = new UILabel(t().get("tc.confirm.marker.delete.text", player)
                .replace("PH_MARKER_NAME", marker.getName()));
        message.setSize(90, 34, true);
        message.setPivot(Pivot.UpperLeft);
        message.setPosition(5, 28, true);
        message.setFontSize(14);
        message.setTextAlign(TextAnchor.MiddleCenter);
        message.setTextWrap(true);
        panel.addChild(message);

        UIElement footer = new UIElement();
        footer.setPivot(Pivot.LowerCenter);
        footer.setPosition(50, 95, true);
        footer.setSize(94, 30, true);
        footer.style.display.set(DisplayStyle.Flex);
        footer.style.flexDirection.set(FlexDirection.Row);
        footer.style.flexWrap.set(Wrap.Wrap);
        footer.style.justifyContent.set(Justify.Center);
        footer.style.alignItems.set(Align.Center);
        panel.addChild(footer);

        footer.addChild(button(AdvancedButtonFactory.cancel(t().get("tc.btn.no", player), event -> {
            close(event.getPlayer());
            onCancel.onCall(true);
        }), 26));
        footer.addChild(button(AdvancedButtonFactory.danger(t().get("tc.btn.yes", player), event -> {
            close(event.getPlayer());
            onDelete.onCall(false);
        }), 22));
        footer.addChild(button(AdvancedButtonFactory.danger(t().get("tc.btn.yes.dont.ask", player), event -> {
            GPSPlayerPreferences.setConfirmMarkerDelete(event.getPlayer(), false);
            close(event.getPlayer());
            onDelete.onCall(true);
        }), 42));
    }

    private UIElement button(AdvancedButton button, int widthPercent) {
        button.setPivot(Pivot.UpperLeft);
        button.style.display.set(DisplayStyle.Flex);
        button.style.justifyContent.set(Justify.Center);
        button.style.alignItems.set(Align.Center);
        button.style.width.set(widthPercent, Unit.Percent);
        button.style.height.set(34, Unit.Pixel);
        button.style.marginBottom.set(2, Unit.Pixel);
        button.style.marginTop.set(2, Unit.Pixel);
        button.style.marginLeft.set(2, Unit.Pixel);
        button.style.marginRight.set(2, Unit.Pixel);
        button.setBorderEdgeRadius(4, false);
        return button;
    }

    public void close(Player player) {
        player.removeUIElement(this);
    }
}
