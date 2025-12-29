package de.omegazirkel.risingworld.gps.ui;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.ui.OZUIElement;
import net.risingworld.api.callbacks.Callback;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UITextField;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.Pivot;
import net.risingworld.api.ui.style.TextAnchor;

public class SetMarkerNamePanel extends OZUIElement {

    private static I18n t() {
        return I18n.getInstance(GPS.name);
    }

    private Player uiPlayer;
    private UITextField input;

    public SetMarkerNamePanel(Player uiPlayer) {
        this(uiPlayer, null);
    }

    public SetMarkerNamePanel(Player uiPlayer, String name) {
        super();
        this.setSize(25, 10, true);
        this.setPivot(Pivot.UpperCenter);
        this.setPosition(55, 15, true);
        this.setBackgroundColor(0, 0, 0, 0.85f);
        this.setBorderColor(1, 1, 1, 0.4f);
        this.setBorder(2);
        this.style.paddingBottom.set(5);
        this.style.paddingTop.set(5);
        this.style.paddingLeft.set(5);
        this.style.paddingRight.set(5);
        this.uiPlayer = uiPlayer;

        setupTitle();
        setupInput(name);
    }

    private void setupTitle() {
        UILabel title = new UILabel(t().get("TC_LABEL_MARKER_NAME", uiPlayer));
        title.setSize(90, 10, true);
        title.setFontSize(17);
        title.setTextAlign(TextAnchor.MiddleCenter);
        title.setFont(Font.DefaultBold);
        title.setPivot(Pivot.UpperLeft);
        title.setPosition(5, 5, true);
        this.addChild(title);
    }

    private void setupInput(String value) {
        input = new UITextField(value);
        input.setSize(90, 30, true);
        input.setReadOnly(false);
        input.setPivot(Pivot.LowerLeft);
        input.setPosition(5, 95, true);

        this.addChild(input);
    }

    public void getCurrentText(Player player, Callback<String> onText){
        input.getCurrentText(player, onText);
    }

}
